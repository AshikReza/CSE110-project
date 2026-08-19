/**
 * CircleFTP Audio Fix - Content Script
 * Injected into circleftp.net pages.
 * Detects video/source elements and injects a floating overlay
 * with "Open in VLC" and other helper buttons.
 */

(function () {
  'use strict';

  // ── Prevent double-injection ──────────────────────────────────────────────
  if (document.getElementById('cftp-audio-fix-overlay')) return;

  // ── State ─────────────────────────────────────────────────────────────────
  let videoUrl = null;
  let pageTitle = document.title || 'Video';

  // ── Utility: find the video URL ───────────────────────────────────────────
  function findVideoUrl() {
    // 1. Direct <video src="...">
    const video = document.querySelector('video');
    if (video && video.src && video.src.length > 0 && video.src !== window.location.href) {
      return video.src;
    }

    // 2. <video><source src="..."></video>
    const source = document.querySelector('video source');
    if (source && source.src) return source.src;

    // 3. Any <source> tag anywhere
    const anySource = document.querySelector('source[src]');
    if (anySource && anySource.src) return anySource.src;

    // 4. Search page JS variables (common in custom players)
    // Look for src or file keys in inline scripts
    const scripts = document.querySelectorAll('script:not([src])');
    const urlPattern = /(?:src|file|url|source)\s*[=:]\s*["']([^"']+\.(?:mkv|mp4|avi|mov|webm|m3u8|ts)[^"']*)/i;
    for (const script of scripts) {
      const match = script.textContent.match(urlPattern);
      if (match) return match[1].startsWith('http') ? match[1] : null;
    }

    return null;
  }

  // ── Utility: copy text to clipboard ──────────────────────────────────────
  function copyToClipboard(text, btn) {
    navigator.clipboard.writeText(text).then(() => {
      const orig = btn.textContent;
      btn.textContent = '✅ Copied!';
      btn.style.background = 'linear-gradient(135deg, #10b981, #059669)';
      setTimeout(() => {
        btn.textContent = orig;
        btn.style.background = '';
      }, 2000);
    }).catch(() => {
      // Fallback
      const ta = document.createElement('textarea');
      ta.value = text;
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      document.body.removeChild(ta);
    });
  }

  // ── Build the floating overlay ────────────────────────────────────────────
  function createOverlay(url) {
    videoUrl = url;
    const vlcUrl = 'vlc://' + url.replace(/^https?:\/\//, '');
    const fileName = url.split('/').pop().split('?')[0];
    const ext = fileName.split('.').pop().toUpperCase();

    // Detect likely codec issue
    const hasAudioIssue = ['MKV', 'AVI', 'TS', 'MTS', 'M2TS'].includes(ext);

    const overlay = document.createElement('div');
    overlay.id = 'cftp-audio-fix-overlay';
    overlay.innerHTML = `
      <div id="cftp-panel">
        <div id="cftp-header">
          <span id="cftp-icon">🎬</span>
          <span id="cftp-title">Audio Fix</span>
          <span id="cftp-badge">${ext}</span>
          <button id="cftp-close" title="Close">✕</button>
        </div>

        ${hasAudioIssue ? `
        <div id="cftp-warning">
          <span>⚠️</span>
          <div>
            <strong>AC-3/DTS audio detected</strong><br>
            <small>Chrome cannot decode this codec.<br>Open in VLC for full audio support.</small>
          </div>
        </div>` : ''}

        <div id="cftp-filename" title="${fileName}">${fileName.length > 40 ? fileName.substring(0, 38) + '…' : fileName}</div>

        <div id="cftp-buttons">
          <button class="cftp-btn cftp-btn-primary" id="cftp-vlc-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="5 3 19 12 5 21 5 3"/>
            </svg>
            Open in VLC
          </button>

          <button class="cftp-btn cftp-btn-km" id="cftp-km-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polygon points="10 8 16 12 10 16 10 8"/>
            </svg>
            KM Player
          </button>

          <button class="cftp-btn cftp-btn-secondary" id="cftp-mpv-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M9.5 8.5l5 3.5-5 3.5V8.5z"/>
            </svg>
            MPV
          </button>

          <button class="cftp-btn cftp-btn-secondary" id="cftp-copy-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
            Copy URL
          </button>

          <button class="cftp-btn cftp-btn-download" id="cftp-dl-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Download
          </button>
        </div>

        <div id="cftp-tip">
          💡 Works with <strong>VLC</strong>, <strong>KM Player</strong>, or <strong>MPV</strong> — all support AC-3/DTS audio.
        </div>
      </div>
    `;

    // ── Styles ────────────────────────────────────────────────────────────
    const style = document.createElement('style');
    style.id = 'cftp-styles';
    style.textContent = `
      #cftp-audio-fix-overlay {
        position: fixed;
        bottom: 24px;
        right: 24px;
        z-index: 2147483647;
        font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        font-size: 13px;
      }

      #cftp-panel {
        background: linear-gradient(135deg, #0d1117 0%, #161b22 100%);
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: 16px;
        padding: 16px;
        width: 280px;
        box-shadow:
          0 20px 60px rgba(0,0,0,0.6),
          0 0 0 1px rgba(255,255,255,0.05),
          inset 0 1px 0 rgba(255,255,255,0.08);
        backdrop-filter: blur(20px);
        color: #e6edf3;
        animation: cftp-slide-in 0.35s cubic-bezier(0.16, 1, 0.3, 1);
      }

      @keyframes cftp-slide-in {
        from { opacity: 0; transform: translateY(20px) scale(0.95); }
        to   { opacity: 1; transform: translateY(0)   scale(1);    }
      }

      #cftp-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
      }

      #cftp-icon { font-size: 18px; }

      #cftp-title {
        font-weight: 700;
        font-size: 14px;
        color: #fff;
        flex: 1;
        letter-spacing: 0.3px;
      }

      #cftp-badge {
        background: linear-gradient(135deg, #f59e0b, #d97706);
        color: #000;
        font-size: 10px;
        font-weight: 700;
        padding: 2px 7px;
        border-radius: 20px;
        letter-spacing: 0.5px;
      }

      #cftp-close {
        background: none;
        border: none;
        color: #8b949e;
        cursor: pointer;
        font-size: 14px;
        padding: 2px 5px;
        border-radius: 6px;
        line-height: 1;
        transition: all 0.2s;
        margin-left: 4px;
      }
      #cftp-close:hover {
        background: rgba(255,255,255,0.1);
        color: #fff;
      }

      #cftp-warning {
        display: flex;
        gap: 10px;
        align-items: flex-start;
        background: rgba(245, 158, 11, 0.1);
        border: 1px solid rgba(245, 158, 11, 0.25);
        border-radius: 10px;
        padding: 10px 12px;
        margin-bottom: 12px;
        font-size: 12px;
        color: #fde68a;
        line-height: 1.5;
      }
      #cftp-warning span { font-size: 18px; flex-shrink: 0; }
      #cftp-warning strong { color: #fbbf24; display: block; margin-bottom: 2px; }
      #cftp-warning small { color: #d1a954; }

      #cftp-filename {
        background: rgba(255,255,255,0.04);
        border: 1px solid rgba(255,255,255,0.08);
        border-radius: 8px;
        padding: 7px 10px;
        font-size: 11px;
        color: #8b949e;
        word-break: break-all;
        margin-bottom: 12px;
        font-family: 'Consolas', 'Courier New', monospace;
        max-height: 40px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      #cftp-buttons {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 8px;
        margin-bottom: 12px;
      }

      .cftp-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 9px 12px;
        border: none;
        border-radius: 10px;
        font-size: 12px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
        font-family: inherit;
        letter-spacing: 0.2px;
        outline: none;
      }

      .cftp-btn:active {
        transform: scale(0.96);
      }

      .cftp-btn-primary {
        grid-column: 1 / -1;
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        color: #000;
        font-size: 13px;
        padding: 11px 16px;
        box-shadow: 0 4px 15px rgba(245, 158, 11, 0.3);
      }
      .cftp-btn-primary:hover {
        background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
        box-shadow: 0 6px 20px rgba(245, 158, 11, 0.45);
        transform: translateY(-1px);
      }

      .cftp-btn-secondary {
        background: rgba(255,255,255,0.07);
        color: #c9d1d9;
        border: 1px solid rgba(255,255,255,0.1);
      }
      .cftp-btn-secondary:hover {
        background: rgba(255,255,255,0.12);
        color: #fff;
        border-color: rgba(255,255,255,0.2);
        transform: translateY(-1px);
      }

      .cftp-btn-km {
        grid-column: 1 / -1;
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: #fff;
        box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
      }
      .cftp-btn-km:hover {
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        box-shadow: 0 6px 20px rgba(59, 130, 246, 0.45);
        transform: translateY(-1px);
      }

      .cftp-btn-download {
        grid-column: 1 / -1;
        background: rgba(99, 102, 241, 0.15);
        color: #a5b4fc;
        border: 1px solid rgba(99, 102, 241, 0.3);
      }
      .cftp-btn-download:hover {
        background: rgba(99, 102, 241, 0.25);
        color: #c7d2fe;
        border-color: rgba(99, 102, 241, 0.5);
        transform: translateY(-1px);
      }

      #cftp-tip {
        font-size: 11px;
        color: #6e7681;
        line-height: 1.5;
        border-top: 1px solid rgba(255,255,255,0.06);
        padding-top: 10px;
      }
      #cftp-tip strong { color: #8b949e; }
      #cftp-tip code {
        background: rgba(255,255,255,0.08);
        padding: 1px 5px;
        border-radius: 4px;
        font-size: 10px;
        color: #fbbf24;
      }
    `;

    document.head.appendChild(style);
    document.body.appendChild(overlay);

    // ── Event listeners ──────────────────────────────────────────────────
    document.getElementById('cftp-close').addEventListener('click', () => {
      overlay.style.animation = 'none';
      overlay.style.transition = 'opacity 0.3s, transform 0.3s';
      overlay.style.opacity = '0';
      overlay.style.transform = 'translateY(10px) scale(0.95)';
      setTimeout(() => overlay.remove(), 300);
    });

    document.getElementById('cftp-vlc-btn').addEventListener('click', () => {
      window.location.href = vlcUrl;
    });

    document.getElementById('cftp-km-btn').addEventListener('click', () => {
      // KM Player protocol: kmplayer://play?url=<encoded-url>
      window.location.href = 'kmplayer://play?url=' + encodeURIComponent(url);
    });

    document.getElementById('cftp-mpv-btn').addEventListener('click', () => {
      window.location.href = 'mpv://' + url;
    });

    document.getElementById('cftp-copy-btn').addEventListener('click', () => {
      copyToClipboard(url, document.getElementById('cftp-copy-btn'));
    });

    document.getElementById('cftp-dl-btn').addEventListener('click', () => {
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.target = '_blank';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    });
  }

  // ── Attempt to find the video URL (with retry) ────────────────────────────
  function tryInit(attempt = 0) {
    const url = findVideoUrl();
    if (url) {
      createOverlay(url);
    } else if (attempt < 15) {
      // Retry up to 15 times over 7.5 seconds (video src may load lazily)
      setTimeout(() => tryInit(attempt + 1), 500);
    }
  }

  // Start watching once DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => tryInit());
  } else {
    tryInit();
  }

  // Also watch for dynamically inserted video elements
  const observer = new MutationObserver(() => {
    if (!document.getElementById('cftp-audio-fix-overlay')) {
      const url = findVideoUrl();
      if (url) {
        createOverlay(url);
      }
    }
  });
  observer.observe(document.body || document.documentElement, {
    childList: true,
    subtree: true
  });

})();
