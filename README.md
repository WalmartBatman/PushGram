# 💪 PushGram

**Limit your Instagram Reels usage by earning credits through push-ups.**

Each perfect-form push-up = 1 Reel credit. No credits = no Reels.

---

## How It Works

1. **Do Push-ups** → Open PushGram, place phone on floor facing you, do push-ups
2. **Earn Credits** → Each perfect rep awards 1 Reel credit (stored persistently)
3. **Watch Reels** → Each time you enter Reels in Instagram, 1 credit is deducted
4. **No Credits?** → PushGram interrupts and redirects you back to workout

---

## Requirements

- Android 9.0+ (API 28+)
- Camera permission (for push-up detection)
- Accessibility Service permission (to monitor Instagram)
- Instagram installed

---

## Setup Instructions

### 1. Build & Install

```bash
# Clone / unzip the project
cd PushGram

# Build debug APK
./gradlew assembleDebug

# Install to connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open in **Android Studio** (Arctic Fox or newer) and click Run ▶

### 2. Enable Accessibility Service

1. Open PushGram
2. Tap **"Enable Instagram Monitor"**
3. In the Accessibility settings screen, find **"PushGram"**
4. Toggle it **ON** and confirm

### 3. Do Your First Workout

1. Tap **"START WORKOUT"**
2. Place your phone on the floor, camera facing up toward you
3. Get into push-up position over the phone
4. Do push-ups — the app will count and validate your form
5. Exit when done — credits are saved automatically

---

## Push-up Form Requirements (for credit to be awarded)

✅ **Body must be straight** — no sagging hips or raised butt  
✅ **Full range of motion** — arms must extend past 150° (top) and bend below 90° (bottom)  
✅ **Elbows not flared** — elbows must stay within 1.8x shoulder width  

If form is bad, the rep is counted but **no credit is awarded** (and you get real-time feedback).

---

## 🎵 Workout Music

### Features
- **Unlimited playlists** — no cap on number of playlists or songs
- **YouTube import** — paste any YouTube playlist URL or video URL
- **Spotify import** — paste any Spotify playlist URL or track URL  
- **No login required** — YouTube uses the free Data API; Spotify uses Client Credentials (app-only token)
- **Minimal bandwidth** — audio-only streaming (~1 MB/min vs ~8 MB/min for video)
- **Background playback** — continues while you're working out or in other apps
- **Lock screen controls** — media notification with play/pause/skip

### Setup API Keys (One-time, Free)

#### YouTube Data API v3
1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create a project → Enable **YouTube Data API v3**
3. Create **API Key** credentials
4. Paste into `app/src/main/res/values/strings.xml` as `youtube_api_key`
5. Free tier: **10,000 units/day** (each playlist import = ~1 unit)

> **Note:** Playlist import uses the API key only for metadata (title/artist/thumbnail).
> Audio stream resolution uses YouTube's internal player endpoint — no API key needed and no unit cost.

#### Spotify Web API
1. Go to [developer.spotify.com/dashboard](https://developer.spotify.com/dashboard)
2. Create an app → Copy **Client ID** and **Client Secret**
3. Paste into `strings.xml` as `spotify_client_id` / `spotify_client_secret`
4. Free forever — Client Credentials flow requires no user login

### How Spotify Playback Works

| Situation | Behavior |
|-----------|----------|
| Track has a preview URL | Plays free 30-second audio preview (128kbps MP3) directly |
| Track has no preview | Launches Spotify app via deep-link (`spotify:track:ID`) |
| Spotify not installed | Shows "Spotify app not installed" error |

Most tracks have preview URLs. For full track playback, the user needs the Spotify app.

### Bandwidth Usage

| Source | Quality | Usage |
|--------|---------|-------|
| YouTube (itag 140) | m4a 128kbps, audio only | ~1 MB/min |
| Spotify preview | MP3 128kbps, 30 seconds | ~384 KB per preview |
| Spotify deep-link | Handled by Spotify app | 0 bytes in PushGram |

---

```
PushGram/
├── camera/
│   ├── CameraProcessor.java      # CameraX setup + ML Kit integration
│   └── PushUpAnalyzer.java       # Pose detection & rep counting logic
├── model/
│   └── CreditManager.java        # Credit store (SharedPreferences)
├── service/
│   ├── InstagramMonitorService.java  # Accessibility service
│   └── BootReceiver.java
└── ui/
    ├── MainActivity.java          # Home screen + stats
    ├── WorkoutActivity.java       # Camera workout screen
    └── BlockerActivity.java       # "Earn credits" gate screen
```

### Key Dependencies
- **ML Kit Pose Detection** (`pose-detection:18.0.0-beta4`) — lightweight, on-device
- **CameraX** — camera lifecycle management
- **AccessibilityService** — Instagram package monitoring

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Push-ups not detected | Ensure good lighting; position phone flat on floor pointing up |
| Credits not deducting | Verify Accessibility Service is enabled and active |
| Blocker not appearing | Some Android versions need "Draw over apps" permission too |
| App crashes on camera | Grant camera permission in Settings > Apps > PushGram |

---

## Privacy

- All processing is **100% on-device** — no data leaves your phone
- Camera feed is never recorded or stored
- Credits are stored only in local SharedPreferences

---

## Known Limitations

- Instagram occasionally changes its internal activity class names; if monitoring stops working after an Instagram update, the `REELS_INDICATOR` string in `InstagramMonitorService.java` may need updating
- Accessibility Services can be cleared by battery optimization on some MIUI/OneUI devices — add PushGram to battery whitelist for reliability
- Detection accuracy depends on lighting and camera angle

---

*Built with ❤️ using ML Kit + CameraX*
