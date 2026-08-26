# CineNova 🎬

A premium, authentication-free movie & TV streaming app UI built with **Jetpack Compose + Material 3 (Material You)**.

> Demo project — all artwork is generic placeholder imagery. No copyrighted content.

## Features

- **No login / no accounts** — launches straight into Home
- Cinematic dark-first Material 3 design with light/dark/system themes
- Home with hero carousel, Continue Watching, and 9 curated content rails
- Explore + full search experience (suggestions, recents, results, no-results state)
- Movie & TV details: cast, trailers, reviews, episodes with season selector
- Media3/ExoPlayer video player: ±10s, seek, speed, PiP, fullscreen, auto-fading controls
- Watchlist, Downloads (queue/pause/resume/storage), Notification center
- Profile = pure settings/preferences area
- Responsive: NavigationBar on phones, NavigationRail on tablets/desktop
- Full CI/CD via GitHub Actions (test → lint → build → release APK)

## Build

Open in Android Studio (Koala+), or:

```bash
gradle assembleDebug   # debug APK at app/build/outputs/apk/debug/
```

## CI/CD

`.github/workflows/ci-cd.yml` runs on every push/PR to `main`:

1. Unit tests + Android Lint
2. Debug APK artifact on every push; release APK on `v*` tags
3. Tagged releases (`git tag v1.0.0 && git push --tags`) publish a GitHub Release with the APK

## Tech

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · Coil · Media3 ExoPlayer
