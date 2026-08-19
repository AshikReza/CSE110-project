# CircleFTP Audio Fix 🎬

A Chrome extension that fixes the **AC-3 / DTS / MKV audio codec** issue on [circleftp.net](http://new.circleftp.net).

## The Problem

The videos on circleftp.net are `.mkv` files with **AC-3 (Dolby Digital)** or **DTS** audio. Chrome does not support these codecs natively, so the video plays but **there is no sound**.

## The Fix

This extension injects a **floating toolbar** on every circleftp.net page that:

- 🟡 **Open in VLC** — Sends the video URL directly to VLC Media Player via `vlc://` protocol. VLC supports ALL codecs including AC-3/DTS. This is the recommended fix.
- ⚫ **Open in MPV** — Opens in MPV player via `mpv://` protocol.
- 📋 **Copy URL** — Copies the raw video URL so you can paste it into VLC, MPC-HC, or any other player manually.
- ⬇️ **Download** — Triggers a direct download of the video file.

## Installation

1. Open Chrome and go to `chrome://extensions/`
2. Enable **Developer Mode** (toggle in the top-right corner)
3. Click **"Load unpacked"**
4. Select the `circleftp-audio-fix` folder
5. The extension is now active!

## Usage

1. Go to any video page on **circleftp.net** (e.g., `http://new.circleftp.net/content/105131`)
2. A **floating panel** will appear in the bottom-right corner automatically
3. Click **"Open in VLC"** — the video will open in VLC with working audio

> **💡 Tip:** Make sure VLC is installed. VLC registers the `vlc://` protocol handler on Windows automatically during installation.

## Files

| File | Purpose |
|------|---------|
| `manifest.json` | Extension configuration (Manifest V3) |
| `content.js` | Injected into circleftp.net pages; detects video URL and shows floating toolbar |
| `popup.html` | Extension popup UI |
| `popup.js` | Popup logic; queries active tab for video info |
| `icon128.png` | Extension icon |
