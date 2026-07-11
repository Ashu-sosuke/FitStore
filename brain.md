# FitStore System Architecture & Tech Stack

## 🏗️ System Architecture

### 📱 Android Frontend (Clean Architecture)
The mobile app is built using modern Android principles, divided into three main layers:
- **Presentation Layer:** Jetpack Compose, ViewModels (handling UI State via Kotlin Flow).
- **Domain Layer:** Use Cases, Domain Models, Repository Interfaces.
- **Data Layer:** Repositories, Retrofit (API Services + DTOs), Room (Local Persistence), WorkManager (Background Syncing).
- **Widget Layer:** Jetpack Glance API for custom Home Screen Widgets.

### 🖥️ Backend (Python - FastAPI)
The backend is built as a high-performance, asynchronous service:
- **Framework:** FastAPI (Asynchronous, ASGI).
- **ODM/Models:** Pydantic models (v2) for request/response validation.
- **Database:** MongoDB Atlas via Motor (Async MongoDB Driver).
- **Security:** Firebase Admin SDK for verification of JWT tokens.
- **Structure:**
    - `app/main.py`: Entry point, CORS middleware, JWT security integration.
    - `app/routes/`: Modular routers (profile, workout, meal).
    - `app/models/`: Pydantic data schemas.
    - `app/database.py`: Async connection logic.
    - `food-analyser/`: Integrated AI/ML service for nutritional analysis.

---

## 🛠️ Technology Stack

### Mobile (Android)
- **Language:** Kotlin (with Coroutines and Flows)
- **UI Framework:** Jetpack Compose
- **Widgets:** Jetpack Glance (App Widget Integration)
- **DI:** Hilt
- **Network:** Retrofit 2, OkHttp 4 (with Certificate Pinning and JWT Interceptor)
- **Persistence:** Room (with SQLite, using explicit schema migrations)
- **AI/ML:** ML Kit (Barcode Scanning)
- **Health APIs:** Health Connect (`androidx.health.connect:connect-client`)
- **Testing:** MockK, Turbine, kotlinx-coroutines-test, JUnit 4
- **Services:** Firebase Cloud Messaging (FCM), Firebase Crashlytics, Firebase In-App Messaging

### Backend (Python)
- **Language:** Python 3.10+
- **Framework:** FastAPI
- **Server:** Uvicorn
- **Database:** MongoDB (Motor)
- **Auth:** Firebase Admin SDK (JWT validation)
- **AI/ML Dependencies:** PyTorch, TorchVision, Pillow (for Food Analysis)

---

## 🧠 Development Specifics
- **Async First:** Both mobile and backend use asynchronous patterns (Coroutines/Flow on Android, Async/Await on Python).
- **Offline Sync Engine:** Background uploads are managed asynchronously via WorkManager `SyncWorker` and dependency-injected Hilt repositories.
- **Health Connect Integration:** Securely requests permissions and accesses on-device health data (like steps and sleep duration) rather than relying exclusively on raw device sensors.
- **JWT Data Isolation:** Authenticates requests using Firebase Authentication JWT. The backend decodes the token to obtain the unique user identifier, protecting user privacy.
- **Gamified Streaks:** Updates streaks when the user launches the app once on a new day. Saves streak metrics locally and matches them with MongoDB. Includes database migration `MIGRATION_6_7`.
- **Credentials Protection:** Untracked the critical Firebase configuration file `google-services.json` from Git via `.gitignore` and added a template configuration (`google-services.json.template`) to protect user API credentials.
- **Enhanced Camera Scanner & Mappings:** Fixed the CameraX analyzer runtime issue by using the native YUV-to-Bitmap conversion (`ImageProxy.toRotatedBitmap()`) and added a premium visual layout (neon borders, animated laser line, progress states). Aligned the scan-save data flow directly with `MealRepository` to enable seamless MongoDB syncing.
- **R8/ProGuard Obfuscation:** Enabled minification for release builds. Custom rules prevent Gson, Retrofit, and DTO structures from being shrunk or obfuscated in a way that breaks reflection at runtime.
- **CI/CD Build Automation:** Continuous integration via GitHub Actions verifies builds and tests on every branch push.

---
*Last Updated: 2026-07-02*
