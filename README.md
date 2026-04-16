# PushGram

**Physical Paywall for Instagram Reels.** Earn push-up credits to unlock scrolling.

## Core Loop
- 1 perfect-form push-up = 1 Reel credit
- Opening Instagram Reels spends 1 credit
- 0 credits = blocker screen, go do more push-ups

## Features
- 📱 ML Kit on-device pose detection (no data leaves phone)
- 🏆 7-tier ranking system + local & global leaderboard
- ⚔️ Real-time multiplayer battle mode (Firebase)
- 🎵 YouTube & Spotify playlist import (data-saver 48–64kbps)
- 💪 14-exercise progression library (5 levels each)

## Requirements
- Android 9.0+ (API 28+)
- Camera permission
- Accessibility Service permission
- Instagram installed

## Build
1. Add API keys in `app/src/main/res/values/strings.xml`
2. (Optional) Add `google-services.json` from Firebase console for battle mode
3. Push to GitHub → Actions tab → Run "Build PushGram APK"
4. Download artifact → install APK

## Setup Checklist
- [ ] `youtube_api_key` — [console.cloud.google.com](https://console.cloud.google.com)
- [ ] `spotify_client_id` + `spotify_client_secret` — [developer.spotify.com](https://developer.spotify.com/dashboard)
- [ ] `google-services.json` from [console.firebase.google.com](https://console.firebase.google.com) (optional)

## Privacy
All pose detection is 100% on-device. No camera data leaves your phone.
