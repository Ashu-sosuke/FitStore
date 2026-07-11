# FitStore Features & Roadmap

## 🌟 Current Features

### 1. User Management & Personalization
- **Onboarding:** Initial flow to capture user physical metrics (height, weight, age, activity level) and fitness goals.
- **Profile:** Management of user-specific health data and settings.

### 2. Workout Tracking
- **Exercises Library:** Database of exercises available for selection.
- **Workout Sessions:** Ability to log and track specific workout routines.
- **Workout Details:** Deep dive into specific exercises, sets, and reps.

### 3. Nutrition & Diet (Meals)
- **Meal Logging:** Daily tracking of food intake (Breakfast, Lunch, Dinner, Snacks).
- **Food Analysis:**
    - **Barcode Scanning:** Using ML Kit to identify products.
    - **AI Food Analysis:** Using the integrated `food-analyser` backend.
    - **Manual Search & Custom Input:** Query matching foods from the database or submit manual entries if not shown, which updates the backend database for future queries and logs the meal locally and remotely.

### 4. Progress & Analytics
- **Health Dashboard:** High-level overview of daily activity and goals.
- **Progress Visualization:** Charts and metrics showing improvements over time.

---

## 🗺️ Roadmap & Planned Enhancements

### Phase 1: Backend Finalization & Sync
- [x] Migrate Backend from Node.js/Express to Python (FastAPI).
- [x] Implement API Key security layer (Obfuscated using `local.properties`).
- [x] Implement Firebase Auth JWT token-based authentication.
- [x] Refine schemas with pagination and relationships.
- [x] Dockerize the backend for cloud deployment.
- [x] Finalize Backend API integration for all mobile screens.

### Phase 2: Android Infrastructure & Data Layer
- [x] Set up Hilt Dependency Injection for Networking and Repositories.
- [x] Implement API interfaces (Profile, Workout, Meal).
- [x] Create DTOs and Data Mappers for clean architecture sync.
- [x] Refactor Repositories to handle local caching + remote sync.
- [x] Define Domain models and core Use Cases.
- [x] Implement robust error handling (Snackbar notifications) for offline/online transitions.
- [x] Sync local Room data with MongoDB Atlas seamlessly.
- [x] Remove unused Realm dependencies to keep build clean.
- [x] Implement Room database migrations (`MIGRATION_6_7`) to prevent user crashes.

### Phase 3: Premium UI/UX Development (Jetpack Compose)
- [x] Create a modern, fluid user interface with premium aesthetics ("Midnight & Neon").
- [x] Implement smooth transitions and micro-animations.
- [x] Finalize the "WOW" factor for the dashboard and logging flows.

### Phase 4: AI & Sensor Integration
- [x] Integrate ML Kit for real-time barcode detection in `MealScreen.kt`.
- [x] Hook up FastAPI Smart Capture in `MealViewModel` for food identification.
- [x] Implement `WorkoutViewModel` for real-time tracking.
- [x] Add interactive workout timer and logging UI in `WorkoutDetailedScreen.kt`.
- [x] Finalize background synchronization with MongoDB Atlas for all new entities via WorkManager.
- [x] Health Connect Integration: Fetch daily steps dynamically from the device.
- [x] Health Connect Integration: Sync sleep data dynamically from the device.

---

*Last Updated: 2026-07-02*
- **Credentials Security & Leak Prevention:** Safely untracked `google-services.json` from the public Git repository and added it to `.gitignore` to resolve the GitHub Secret Scanning warning. Created a generic `google-services.json.template` for collaborative development.
- **Smart AI Camera Scanner Fixes & Polish:** Replaced the buggy raw byte buffer parsing in `toBitmap` with CameraX's native YUV-to-Bitmap conversion (`toRotatedBitmap`) to resolve the silent "not analyzing" runtime bug. Corrected stretching and 90-degree rotation issues by properly sizing and setting `FILL_CENTER` on the `PreviewView` layout.
- **Scanner UI Visual Upgrades:** Redesigned the scanner UI overlay with a custom neon border, an animated laser scanning guide, and direct "Analyzing item..." visual progress indicators in Compose.
- **Unified Data Flow & Sync:** Rerouted the scanned meal saving mechanism through the Hilt-managed `MealRepository` (instead of saving straight to the local DAO), enabling the background `SyncWorker` to automatically sync scanned meals to MongoDB Atlas and update the Home Screen macro rings reactively.
- **Security & Quality Polish:** Replaced hardcoded keys with obfuscation via `local.properties`, implemented HTTPS certificate pinning, and set up Firebase Auth (JWT) token verification in Android and FastAPI.
- **Dependency Optimization:** Completely removed Realm in favor of Room as the single source of local cache truth.
- **Gamification:** Added a Daily Streak system with consecutive launch tracking logic, visual fire (🔥) badge, Room schema migrations, and sync with MongoDB.
- **User Engagement & Utilities:** Created a Calorie Glance Widget, enabled CSV data export using `FileProvider`, and integrated FCM + Crashlytics.
- **CI/CD & Testing:** Authored a GitHub Actions pipeline, created ProGuard obfuscation rules, and wrote 12 unit tests using MockK/Turbine.
