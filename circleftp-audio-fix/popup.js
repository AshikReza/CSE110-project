/**
 * CircleFTP Audio Fix - Popup Script
 * Communicates with the active tab's content script to get video info.
 */

const mainContent = document.getElementById('main-content');

// ── Try to get video URL from the active tab ──────────────────────────────
async function init() {
  // Check if we're on a circleftp.net page
  let [tab] = await chrome.tabs.query({ active: true, currentWindow: true });

  if (!tab || !tab.url || !tab.url.includes('circleftp.net')) {
    renderNotOnSite();
    return;
  }

  renderChecking();

  // Run script in the tab to extract video URL
  let results;
  try {
    results = await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: () => {
        // Find video URL — same logic as content.js
        const video = document.querySelector('video');
        if (video && video.src && video.src !== window.location.href) {
          return { url: video.src, title: document.title };
        }
        const source = document.querySelector('video source, source[src]');
        if (source && source.src) return { url: source.src, title: document.title };

        // Search inline scripts
        const scripts = document.querySelectorAll('script:not([src])');
        const urlPattern = /(?:src|file|url|source)\s*[=:]\s*["']([^"']+\.(?:mkv|mp4|avi|mov|webm|m3u8|ts)[^"']*)/i;
        for (const script of scripts) {
          const match = script.textContent.match(urlPattern);
          if (match && match[1].startsWith('http')) {
            return { url: match[1], title: document.title };
          }
        }

        return null;
      }
    });
  } catch (e) {
    renderError('Could not access tab. Try refreshing the page.');
    return;
  }

  const result = results && results[0] && results[0].result;

  if (result && result.url) {
    renderVideoFound(result.url, result.title, tab);
  } else {
    renderNoVideo(tab);
  }
}

function renderChecking() {
  mainContent.innerHTML = `
    <div class="status-section">
      <div class="status-card">
        <div class="status-dot checking"></div>
        <div class="status-text">
          <strong>Scanning page…</strong>
          Looking for video sources
        </div>
      </div>
    </div>
  `;
}

function renderNotOnSite() {
  mainContent.innerHTML = `
    <div class="no-video">
      <span class="icon">🌐</span>
      <p><strong>Not on circleftp.net</strong><br>Navigate to a circleftp.net content page to use this extension.</p>
    </div>
  `;
}

function renderError(msg) {
  mainContent.innerHTML = `
    <div class="no-video">
      <span class="icon">⚠️</span>
      <p>${msg}</p>
    </div>
  `;
}

function renderNoVideo(tab) {
  mainContent.innerHTML = `
    <div class="status-section">
      <div class="status-card">
        <div class="status-dot inactive"></div>
        <div class="status-text">
          <strong>No video detected yet</strong>
          Try playing the video first, then reopen this popup.
        </div>
      </div>
    </div>
    <div class="btn-section">
      <button class="btn btn-secondary" id="refresh-btn">🔄 Refresh Detection</button>
    </div>
  `;
  document.getElementById('refresh-btn').addEventListener('click', () => {
    chrome.tabs.reload(tab.id, () => {
      setTimeout(() => init(), 2000);
    });
  });
}

function renderVideoFound(url, title, tab) {
  const fileName = url.split('/').pop().split('?')[0];
  const ext = fileName.split('.').pop().toUpperCase();
  const vlcUrl = 'vlc://' + url.replace(/^https?:\/\//, '');
  const hasAudioIssue = ['MKV', 'AVI', 'TS', 'MTS', 'M2TS'].includes(ext);
  const domain = new URL(url).hostname;

  mainContent.innerHTML = `
    <div class="status-section">
      <div class="status-card">
        <div class="status-dot ${hasAudioIssue ? 'checking' : ''}"></div>
        <div class="status-text">
          <strong>${hasAudioIssue ? '⚠️ Audio codec issue detected' : '✅ Video found'}</strong>
          ${hasAudioIssue ? 'AC-3/DTS not supported by Chrome' : 'Ready to play'}
        </div>
      </div>
    </div>

    <div class="info-section">
      <div class="info-row">
        <span class="info-label">Format</span>
        <span class="info-value ${hasAudioIssue ? 'warn' : 'ok'}">${ext}</span>
      </div>
      <div class="info-row">
        <span class="info-label">Server</span>
        <span class="info-value">${domain}</span>
      </div>
      <div class="info-row">
        <span class="info-label">Audio</span>
        <span class="info-value ${hasAudioIssue ? 'warn' : 'ok'}">${hasAudioIssue ? 'Unsupported (AC-3/DTS)' : 'Compatible'}</span>
      </div>
    </div>

    <div class="btn-section">
      <button class="btn btn-primary" id="popup-vlc-btn">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polygon points="5 3 19 12 5 21 5 3"/>
        </svg>
        Open in VLC
      </button>
      <button class="btn btn-km" id="popup-km-btn">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><polygon points="10 8 16 12 10 16 10 8"/>
        </svg>
        Open in KM Player
      </button>
      <button class="btn btn-secondary" id="popup-mpv-btn">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><polygon points="10 8 16 12 10 16 10 8"/>
        </svg>
        Open in MPV
      </button>
      <button class="btn btn-secondary" id="popup-copy-btn">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
        </svg>
        Copy Video URL
      </button>
    </div>
  `;

  document.getElementById('popup-vlc-btn').addEventListener('click', () => {
    chrome.tabs.update(tab.id, { url: vlcUrl });
  });

  document.getElementById('popup-km-btn').addEventListener('click', () => {
    chrome.tabs.update(tab.id, { url: 'kmplayer://play?url=' + encodeURIComponent(url) });
  });

  document.getElementById('popup-mpv-btn').addEventListener('click', () => {
    chrome.tabs.update(tab.id, { url: 'mpv://' + url });
  });

  document.getElementById('popup-copy-btn').addEventListener('click', () => {
    navigator.clipboard.writeText(url).then(() => {
      const btn = document.getElementById('popup-copy-btn');
      if (btn) {
        btn.textContent = '✅ Copied to clipboard!';
        btn.style.color = '#10b981';
        btn.style.borderColor = 'rgba(16,185,129,0.3)';
      }
    });
  });
}

// ── Init ──────────────────────────────────────────────────────────────────
init();
