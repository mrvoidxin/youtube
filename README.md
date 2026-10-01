# YouTube Clone - Production Engineering Blueprint

A production-grade, native YouTube-clone app written entirely in Kotlin (Android) with a Node.js/Express/TypeScript backend.

## 🏗️ Architecture Overview

This project follows a **three-tier architecture** as specified in Section 3:

1. **Client (Kotlin/Android app)** - Never talks to YouTube Data API directly, only calls backend REST API
2. **Backend (Express/Node.js)** - Sole holder of YouTube API key, proxies all YouTube Data API calls with Redis caching
3. **Database Layer** - PostgreSQL (system of record) + Redis (pure cache)

```
┌─────────────────────────────────────────────────────────────────┐
│                         ANDROID CLIENT                               │
│  ┌─────────────┐  ┌─────────────┐  ┌───────────────────────────┐  │
│  │   UI Layer  │  │ ViewModels  │  │      Repository Layer       │  │
│  │  (Compose)  │  │   (Hilt)    │  │  (Retrofit + Room + Backend)│  │
│  └─────────────┘  └─────────────┘  └───────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTPS/REST
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       BACKEND SERVER                                │
│  ┌─────────────┐  ┌─────────────┐  ┌───────────────────────────┐  │
│  │ Controllers │  │  Services   │  │   YouTube API Service      │  │
│  │   (Express) │  │ (Business)   │  │   (Proxy + Redis Cache)    │  │
│  └─────────────┘  └─────────────┘  └───────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                    JWT Authentication                           │  │
│  │  (Access Token: 15min, Refresh Token: 30days)                   │  │
│  └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────────┬───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│    PostgreSQL    │  │      Redis      │  │  YouTube Data   │
│  (System of     │  │    (Cache)      │  │    API v3       │
│   Record)       │  │                 │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- Android Studio (latest stable version)
- Node.js 20+ 
- Docker & Docker Compose
- Java 17+
- Android SDK 35+

### Backend Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/mrvoidxin/youtube.git
   cd youtube/youtube-clone
   ```

2. **Copy environment file:**
   ```bash
   cd backend
   cp .env.example .env
   ```

3. **Edit `.env` file with your keys:**
   ```env
   # YouTube Data API v3 Key (from Google Cloud Console)
   YOUTUBE_API_KEY=YOUR_YOUTUBE_API_KEY
   
   # Database URL (for Docker Compose, use the service name)
   DATABASE_URL=postgresql://postgres:postgres@postgres:5432/youtube_clone
   
   # Redis URL
   REDIS_URL=redis://redis:6379
   
   # JWT Secrets (generate with: node -e "console.log(require('crypto').randomBytes(32).toString('hex'))")
   JWT_ACCESS_SECRET=your_random_access_secret_here
   JWT_REFRESH_SECRET=your_random_refresh_secret_here
   
   # Google OAuth (optional, for Google Sign-In)
   GOOGLE_OAUTH_CLIENT_ID=your_google_client_id
   GOOGLE_OAUTH_CLIENT_SECRET=your_google_client_secret
   ```

4. **Install dependencies and start backend:**
   ```bash
   npm install
   npm run dev
   ```

   Or with Docker Compose:
   ```bash
   cd ..  # Go to project root
   docker-compose up -d
   ```

### Android Setup

1. **Open the project in Android Studio:**
   - File → Open → Select `youtube-clone` directory

2. **Update backend URL in Constants.kt:**
   - If using emulator: `http://10.0.2.2:3000/api/`
   - If using physical device: Use your computer's local IP (e.g., `http://192.168.x.x:3000/api/`)

3. **Build and run:**
   - Build the project (▶️ Run)
   - Select an emulator or device

### Using Docker Compose (Recommended)

Start all services (PostgreSQL, Redis, Backend):
```bash
docker-compose up -d
```

Stop all services:
```bash
docker-compose down
```

## 📁 Project Structure

### Backend (`/backend`)

```
backend/
├── prisma/
│   ├── schema.prisma        # PostgreSQL schema
│   └── seed.ts              # Database seeding
├── src/
│   ├── controllers/         # Express route controllers
│   │   ├── authController.ts
│   │   ├── feedController.ts
│   │   ├── searchController.ts
│   │   ├── videosController.ts
│   │   ├── channelsController.ts
│   │   ├── commentsController.ts
│   │   └── historyController.ts
│   ├── services/            # Business logic services
│   │   ├── AuthService.ts
│   │   ├── YouTubeApiService.ts
│   │   └── ...
│   ├── middleware/           # Express middleware
│   │   ├── authenticate.ts
│   │   └── errorHandler.ts
│   ├── config/              # Configuration
│   │   ├── env.ts           # Environment variables
│   │   ├── database.ts      # Prisma client
│   │   └── redis.ts         # Redis client
│   ├── utils/               # Utility functions
│   │   ├── jwt.ts           # JWT token handling
│   │   └── validation.ts    # Zod validation schemas
│   └── index.ts             # Main entry point
├── Dockerfile
├── package.json
├── tsconfig.json
└── .env.example
```

### Android Client (`/youtube-clone/app`)

```
app/
├── src/main/java/com/youtubeclone/
│   ├── data/
│   │   ├── api/               # API services
│   │   │   ├── BackendApiService.kt
│   │   │   ├── YouTubeApiService.kt (legacy, for reference)
│   │   │   └── ...
│   │   ├── repository/        # Data repositories
│   │   │   ├── AuthRepository.kt
│   │   │   ├── BackendVideoRepository.kt
│   │   │   ├── VideoRepository.kt (legacy)
│   │   │   └── ...
│   │   └── local/             # Local storage
│   │       ├── PreferencesManager.kt
│   │       ├── TokenManager.kt
│   │       ├── AppDatabase.kt
│   │       └── dao/
│   ├── domain/
│   │   ├── models/           # Domain models
│   │   │   ├── Video.kt
│   │   │   ├── Channel.kt
│   │   │   ├── Comment.kt
│   │   │   └── AuthState.kt
│   │   └── usecases/          # Use cases
│   │       └── ...
│   ├── di/                  # Dependency injection (Hilt)
│   │   ├── NetworkModule.kt
│   │   ├── RepositoryModule.kt
│   │   ├── DatabaseModule.kt
│   │   └── AuthModule.kt
│   ├── presentation/
│   │   ├── screens/          # Compose screens
│   │   │   ├── auth/
│   │   │   │   ├── AuthScreen.kt
│   │   │   │   └── AuthViewModel.kt
│   │   │   ├── register/
│   │   │   │   ├── RegisterScreen.kt
│   │   │   │   └── RegisterViewModel.kt
│   │   │   ├── splash/
│   │   │   │   ├── SplashScreen.kt
│   │   │   │   └── SplashViewModel.kt
│   │   │   ├── home/
│   │   │   ├── search/
│   │   │   ├── watch/
│   │   │   ├── channel/
│   │   │   ├── shorts/
│   │   │   └── library/
│   │   ├── components/        # Reusable Compose components
│   │   │   ├── VideoCard.kt
│   │   │   ├── SearchBar.kt
│   │   │   ├── MiniPlayer.kt
│   │   │   └── ...
│   │   ├── theme/            # Material 3 theme
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Theme.kt
│   │   └── navigation/       # Navigation
│   │       ├── Screen.kt
│   │       └── NavGraph.kt
│   ├── service/              # Android services
│   │   └── PlaybackService.kt
│   └── utils/                # Utility classes
│       ├── Constants.kt
│       ├── Extensions.kt
│       └── FormatUtils.kt
├── build.gradle.kts
└── ...
```

## 🔌 API Documentation

### Backend REST API

All endpoints are prefixed with `/api/` and use JWT Bearer token authentication where required.

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login with email/password |
| POST | `/api/auth/google` | Google Sign-In |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Get current user profile |

#### Feed

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/feed` | Get trending videos (paginated) |

Query Parameters:
- `page` (number): Page number (default: 1)
- `pageSize` (number): Items per page (default: 20)
- `categoryId` (string): Video category filter

#### Search

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/search` | Search videos, channels, playlists |

Query Parameters:
- `q` (string, required): Search query
- `type` (string): Filter by type (video, channel, playlist)
- `page` (number): Page number
- `pageSize` (number): Items per page
- `duration` (string): Filter by duration (short, medium, long)
- `order` (string): Sort order (date, rating, relevance, title, videoCount, viewCount)
- `categoryId` (string): Category filter

#### Videos

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/videos/{id}` | Get video details |

#### Channels

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/channels/{id}` | Get channel details |
| POST | `/api/channels/{id}/subscribe` | Subscribe to channel |
| DELETE | `/api/channels/{id}/subscribe` | Unsubscribe from channel |

#### Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/comments/videos/{videoId}` | Get video comments |
| POST | `/api/comments/videos/{videoId}` | Create comment |
| POST | `/api/comments/{id}/like` | Like/dislike comment |

#### History

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/history` | Get watch history |
| POST | `/api/history` | Add to watch history |
| DELETE | `/api/history` | Clear watch history |
| DELETE | `/api/history/{id}` | Remove specific history item |

## 🔐 Authentication Flow

The app implements **JWT token-based authentication** with the following flow:

### 1. Email/Password Authentication

```
User enters email/password
       ↓
POST /api/auth/login
       ↓
Backend validates credentials
       ↓
Returns: accessToken (15min), refreshToken (30days)
       ↓
Client stores in EncryptedSharedPreferences
       ↓
Access token used for authenticated requests
```

### 2. Google Sign-In (Credential Manager API)

```
User clicks "Sign in with Google"
       ↓
CredentialManager.getCredential() (Android)
       ↓
User selects Google account
       ↓
Google ID token returned
       ↓
POST /api/auth/google { credential: idToken }
       ↓
Backend verifies token with Google
       ↓
Returns: accessToken, refreshToken, user profile
       ↓
Client stores tokens securely
```

### 3. Token Refresh

Access tokens expire after 15 minutes. The client automatically refreshes when:
- Token is about to expire (5 minute margin)
- API call returns 401 Unauthorized

```
Access token expired or about to expire
       ↓
POST /api/auth/refresh { refreshToken }
       ↓
Backend validates refresh token
       ↓
Returns: new accessToken, new refreshToken
       ↓
Client updates stored tokens
```

### 4. Token Storage Security

All tokens are stored using **AndroidX Security's EncryptedSharedPreferences**:

```kotlin
// In PreferencesManager.kt
val encryptedSharedPreferences = EncryptedSharedPreferences.create(
    "youtube_clone_prefs",
    masterKeyAlias,
    context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

## 📊 Data Flow

### Feed Data Flow

```
User opens app → HomeScreen
       ↓
HomeViewModel.requestTrendingVideos()
       ↓
BackendVideoRepository.getTrendingVideos()
       ↓
BackendApiService.getFeed()
       ↓
HTTP GET /api/feed?page=1&categoryId=...
       ↓
Backend: Check Redis cache
       │
       ├── Cache HIT → Return cached data with isCached=true
       │
       └── Cache MISS → Call YouTube API
           ↓
           YouTubeApiService.getTrendingVideos()
           ↓
           Check quota usage
           │
           ├── Quota OK → Fetch from YouTube
           │    ↓
           │    Transform to domain model
           │    ↓
           │    Cache for 5 minutes
           │    ↓
           │    Return to client
           │
           └── Quota Exceeded → Return cached data with quotaExceeded=true
                (or empty if no cache)
```

### Search Data Flow

```
User types search query
       ↓
Debounced via Flow.debounce(300ms)
       ↓
SearchViewModel.search()
       ↓
SearchRepository.search()
       ↓
BackendApiService.search()
       ↓
HTTP GET /api/search?q=...&type=video
       ↓
Backend: Check Redis cache (10 min TTL)
       │
       ├── Cache HIT → Return cached results
       │
       └── Cache MISS → Call YouTube search.list
           ↓
           Transform results
           ↓
           Cache results
           ↓
           Return to client
```

## 🎨 Design System

### Material 3 Theme

The app uses Material 3 with a **dark-first palette** matching YouTube's Android UI:

```kotlin
// In Theme.kt
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE51937),      // YouTube Red
    secondary = Color(0xFFFFFFFF),    // White
    tertiary = Color(0xFF606060),     // Gray
    surface = Color(0xFF121212),       // Dark background
    background = Color(0xFF000000),     // Black
    onPrimary = Color(0xFFFFFFFF),    // White text on red
    onSurface = Color(0xFFFFFFFF),    // White text
    onBackground = Color(0xFFFFFFFF)   // White text on black
)
```

### Spacing Scale

The app uses an **8dp spacing scale**:
- 4dp - Micro spacing
- 8dp - Small spacing
- 16dp - Medium spacing
- 24dp - Large spacing
- 32dp - XL spacing
- 48dp - 2XL spacing

### Typography

```kotlin
// In Type.kt
val Typography = Typography(
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
)
```

## 🎬 Features Implemented

### ✅ Phase 1: Foundation
- [x] Multi-module Gradle setup
- [x] Compose theme (Material 3, dark-first)
- [x] Hilt dependency injection
- [x] Retrofit + Room scaffolding
- [x] JWT authentication system
- [x] Google Sign-In via Credential Manager API
- [x] EncryptedSharedPreferences for token storage

### ✅ Phase 2: Core Browsing
- [x] Backend YouTube proxy with Redis caching
- [x] Home feed with Paging 3 (in progress)
- [x] Search functionality
- [x] Video detail screen
- [x] Channel screens
- [x] Rate limiting and circuit breaker

### 🚧 Phase 3: Social Layer
- [ ] Comments system
- [ ] Likes/dislikes
- [ ] Subscriptions
- [ ] Watch history

### 🚧 Phase 4: Shorts
- [ ] VerticalPager feed
- [ ] Lifecycle-aware autoplay

### 🚧 Phase 5: Premium Player Features
- [x] MediaSessionService for background playback
- [ ] Native Picture-in-Picture

### 🚧 Phase 6: Offline Architecture
- [ ] Media3 offline DownloadManager
- [ ] WorkManager for background scheduling
- [ ] Room-backed download queue

### 🚧 Phase 7: Polish & Hardening
- [ ] SharedTransitionLayout on navigation
- [ ] Shimmer/error/empty states
- [ ] Accessibility pass
- [ ] Baseline Profile generation
- [ ] Signed release build

## 🔧 Technology Stack

### Android Client
| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose + Material 3 |
| Async | Kotlin Coroutines + Flow |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Local DB | Room |
| Pagination | Paging 3 |
| Media Playback | Media3 (ExoPlayer) |
| Image Loading | Coil |
| Navigation | Navigation Compose |
| Security | EncryptedSharedPreferences |

### Backend Server
| Layer | Technology |
|-------|------------|
| Runtime | Node.js 20+ |
| Framework | Express |
| Language | TypeScript |
| ORM | Prisma |
| Database | PostgreSQL |
| Cache | Redis |
| Auth | JWT + Google OAuth |
| Validation | Zod |

### Infrastructure
| Component | Technology |
|-----------|------------|
| Containerization | Docker |
| Orchestration | Docker Compose |
| CI/CD | GitHub Actions (configured in `.github/workflows`) |

## 📦 Dependencies

### Android Client

Key dependencies in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.0.0"
agp = "8.5.0"
compose-bom = "2024.06.00"
hilt = "2.51.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
coil = "3.0.0"
room = "2.6.1"
paging = "3.3.2"
navigation = "2.8.0"
lifecycle = "2.8.4"
datastore = "1.1.1"
youtube-player = "12.1.0"
coroutines = "1.8.1"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# DI
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Image Loading
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network = { group = "io.coil-kt.coil3", name = "coil-network-okhttp", version.ref = "coil" }

# Database
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Pagination
paging-runtime = { group = "androidx.paging", name = "paging-runtime", version.ref = "paging" }
paging-compose = { group = "androidx.paging", name = "paging-compose", version.ref = "paging" }

# Lifecycle
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

# DataStore
datastore = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# YouTube Player
youtube-player = { group = "com.pierfrancescosoffritti.androidyoutubeplayer", name = "core", version.ref = "youtube-player" }

# Coroutines
coroutines = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
```

### Backend Server

Key dependencies in `backend/package.json`:

```json
{
  "dependencies": {
    "@prisma/client": "^5.17.0",
    "bcrypt": "^5.1.1",
    "cors": "^2.8.5",
    "dotenv": "^16.4.5",
    "express": "^4.20.0",
    "express-rate-limit": "^7.3.0",
    "google-auth-library": "^9.11.0",
    "helmet": "^7.1.0",
    "jsonwebtoken": "^9.0.2",
    "morgan": "^1.10.0",
    "redis": "^4.6.13",
    "uuid": "^9.0.1",
    "zod": "^3.23.8"
  }
}
```

## 🔒 Security Considerations

### API Key Management

✅ **YouTube API Key is NEVER in the client**
- Stored only in backend's `.env` file (git-ignored)
- Backend is the sole holder of the YouTube API key
- Client only calls backend REST API

✅ **Defense in Depth**
- Google Cloud Console: Restrict key by server IP
- Rate limiting on backend (`express-rate-limit`)
- Redis-backed rate limiting

### Token Storage

✅ **Secure Storage**
- JWT tokens stored in `EncryptedSharedPreferences`
- Uses AndroidX Security library
- AES256-GCM encryption

✅ **Token Rotation**
- Access token: 15 minutes expiry
- Refresh token: 30 days expiry
- Automatic refresh when token is about to expire

### Network Security

✅ **TLS by Default**
- No cleartext traffic
- Standard TLS validation
- HTTPS only

### Dependency Security

✅ **Vulnerability Scanning**
- `./gradlew dependencyCheckAnalyze` in CI
- Regular dependency updates

## 📊 Cache TTL Strategy

The backend implements **Redis caching** with different TTLs based on resource volatility:

| Resource | TTL | Rationale |
|----------|-----|-----------|
| Search Results | 10 minutes | User searches change frequently |
| Video Stats | 5 minutes | View counts, likes change often |
| Channel Info | 60 minutes | Channel metadata is relatively static |
| Trending Feed | 5 minutes | Trending videos change frequently |
| Comments | 10 minutes | New comments appear often |

## 🎯 YouTube API Quota Management

The backend implements **quota-aware caching** with circuit breaker pattern:

```typescript
// In YouTubeApiService.ts
private async checkQuota(endpoint: string): Promise<void> {
    const usage = await getQuotaUsage(endpoint);
    const totalUsage = await this.getTotalQuotaUsage();

    if (totalUsage >= this.QUOTA_LIMIT) {
        throw new QuotaExceededError('YouTube API quota exceeded for today');
    }

    // Increment quota for this endpoint
    await incrementQuota(endpoint);
}
```

When quota is exhausted:
1. Backend throws `QuotaExceededError`
2. Client receives 429 status
3. Client falls back to cached data (if available)
4. UI shows "Showing cached results" banner
5. Never shows blank screen

## 🚀 Deployment

### Backend Deployment

The backend can be deployed to:
- Railway
- Render
- Fly.io
- AWS EC2
- Google Cloud Run
- Azure App Service

**Example for Railway:**
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Deploy
railway up
```

### Android App Deployment

**For Testing (APK):**
```bash
./gradlew assembleDebug
# or for release
./gradlew assembleRelease
```

**For Production (AAB):**
```bash
./gradlew bundleRelease
```

**Signing:**
Configure in `android/app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        release {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## 🤖 CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci.yml`):

```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Android tests
        run: |
          cd youtube-clone
          ./gradlew ktlintCheck detekt test
      
      - name: Run backend tests
        run: |
          cd backend
          npm ci
          npm run lint
          npm test
```

## 📝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests: `./gradlew test` (Android) and `npm test` (backend)
5. Run lint: `./gradlew ktlintCheck detekt` (Android) and `npm run lint` (backend)
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

## 🤝 Code of Conduct

- Follow Kotlin best practices
- Use meaningful commit messages
- Keep changes focused and small
- Add tests for new functionality
- Update documentation when needed
- Respect the project's architecture decisions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- YouTube Data API v3
- AndroidX Jetpack libraries
- Compose Multiplatform
- Express.js community
- Prisma ORM
- All contributors and users

---

**Note:** This project is a **clone** for educational and development purposes. It does not attempt to replicate YouTube's proprietary features or violate their Terms of Service. The YouTube API key is used only for metadata retrieval (titles, thumbnails, descriptions) as permitted by the YouTube Data API v3.

**Important:** Do not use this project to extract or proxy raw video stream URLs, as this violates YouTube's Terms of Service.
