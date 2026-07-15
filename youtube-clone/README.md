# YouTube Clone - Android App

A **production-ready YouTube clone** built with Kotlin + Jetpack Compose using the YouTube Data API v3.

[![Build APK](https://github.com/PsychoCoder7/youtubeClon/actions/workflows/build.yml/badge.svg)](https://github.com/PsychoCoder7/youtubeClon/actions/workflows/build.yml)

---

## 🚀 Tech Stack

- **100% Kotlin + Jetpack Compose** UI
- **MVVM + Clean Architecture** + Repository Pattern
- **Hilt** DI | **Room** DB | **DataStore** Preferences
- **Retrofit2 + OkHttp3** | **YouTube Data API v3**
- **Coil 3** image loading | **Paging 3** infinite scroll
- **Android YouTube Player v12.1.0** | **Lottie Compose**

---

## 📱 App Screens

| Screen | Features |
|--------|----------|
| **Home** | Trending videos, category chips, Paging3, shimmer loading, pull-to-refresh |
| **Watch** | Full video player, like/dislike, comments, related videos, subscribe, mini-player |
| **Search** | Debounced search, suggestions, recent searches, filter chips (All/Videos/Channels/Playlists) |
| **Shorts** | VerticalPager full-screen, autoplay shorts, action buttons overlay |
| **Channel** | Banner, avatar, subscribe/bell, tabs (Home/Videos/Shorts/Live/Playlists/About) |
| **Library** | History, Watch Later, Liked Videos, Downloads |
| **Settings** | Dark Mode, Autoplay, Quality, Speed, Captions, Notifications, Data Saver |

---

## 🔧 How to Build APK via GitHub Actions

### Step 1: Upload to GitHub

```bash
cd youtube-clone
git init
git add .
git commit -m "YouTube Clone v1.0"
git remote add origin https://github.com/YOUR-USERNAME/youtube-clone.git
git branch -M main
git push -u origin main
```

### Step 2: Build APK (3 ways)

| Method | How |
|--------|-----|
| **🟢 Auto-build** | Push to `main` → APK starts building automatically |
| **🔵 Manual trigger** | **Actions** → **"Build YouTube Clone APK"** → **Run workflow** → Choose `debug` or `release` |
| **🏷️ Tag release** | `git tag v1.0 && git push --tags` → APK auto-attaches to GitHub Release |

### Step 3: Download APK

1. Go to your repo → **Actions** tab
2. Click latest green ✅ run
3. Scroll to **Artifacts** → Download `YouTubeClone-debug`

---

## 🛠️ Local Build

```bash
# Prerequisites: Android Studio Koala+, JDK 17, Android SDK 35
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## 📐 Architecture

```
com.youtubeclone/
├── di/                    # Hilt DI modules
├── data/
│   ├── api/               # Retrofit + ApiInterceptor (auto-injects API key)
│   ├── models/            # Gson response models
│   ├── local/             # Room DB, 3 DAOs, 3 Entities, DataStore prefs
│   └── repository/        # VideoRepository, SearchRepository, LocalRepository
├── domain/
│   ├── models/            # Domain models + toDomain() mappers
│   └── usecases/          # GetTrendingVideos, SearchVideos, GetVideoDetails
├── presentation/
│   ├── navigation/        # NavGraph, Screen routes
│   ├── theme/             # Exact YouTube colors, Roboto, Dark/Light
│   ├── components/        # VideoCard, Shimmer, Chips, MiniPlayer, etc.
│   └── screens/           # 9 screens + 9 ViewModels
├── utils/                 # FormatUtils, Constants, Extensions
└── service/               # PlaybackService
```

---

## ⚙️ Build Config

- **Kotlin**: 2.0.0 | **AGP**: 8.5.0 | **Gradle**: 8.7
- **Compose**: BOM 2024.06.00 | **Compose Compiler**: Kotlin Plugin (no kotlinCompilerExtensionVersion)
- **Min SDK**: 26 | **Target SDK**: 35
- **YouTube API Key**: `AIzaSyBPTQbHM2wqBo9BwKIW3FVWYHlrePQzcAY`

---

## 📋 Features

- [x] Trending + category chips + Paging3
- [x] YouTube player (PiP + landscape)
- [x] Search with 300ms debounce + suggestions
- [x] Shorts vertical swipe + autoplay
- [x] Channel pages with tabs
- [x] Watch History (Room DB)
- [x] Watch Later (Room DB)
- [x] Liked Videos (Room DB)
- [x] Dark/Light theme (DataStore)
- [x] Shimmer loading skeletons
- [x] Offline detection banner
- [x] Mini-player
- [x] Settings screen with toggles
- [x] GitHub Actions CI/CD
- [x] ProGuard rules
