# FitStore Project Analysis

## 🚀 Project Overview
**FitStore** is a premium, full-stack fitness tracking ecosystem designed to help users track their workouts, manage nutrition/meals, and monitor progress. It consists of a **Python (FastAPI) Backend**, a **Native Android Application**, and an integrated **AI Food Analyser Service**.

---

## 🛠️ Technology Stack

### Backend (`/Backend_Python`)
- **Runtime:** Python 3.10+
- **Framework:** FastAPI (Asynchronous, high-performance ASGI framework)
- **ASGI Server:** Uvicorn
- **Database:** MongoDB Atlas (Cloud) / Local MongoDB via **Motor** (Asynchronous MongoDB driver)
- **Security:** Firebase Admin SDK JWT token validation and extraction.
- **Data Validation:** Pydantic (v2) models for request/response serialization and validation.

### Android Application (`/GymFitness`)
- **Language:** Kotlin (Coroutines, Flow)
- **UI Framework:** Jetpack Compose (Modern declarative UI)
- **Design System:** Custom **"Midnight & Neon"** theme (Midnight background, Electric Lime, and Cyan Neon accent colors).
- **Core Architecture:** Clean Architecture (Presentation, Domain, and Data Layers) following MVI/MVVM.
- **Dependency Injection:** Hilt
- **Network Layer:** Retrofit 2 & OkHttp 4 (with Certificate Pinning and JWT Interceptors).
- **Local Persistence:** **Room** (SQLite) for offline-first structured fitness and profile data. 
- **Offline Sync:** **WorkManager** with `SyncWorker` for background data synchronization with MongoDB Atlas.
- **Image/AI Capabilities:** ML Kit (for real-time barcode scanning) and CameraX.
- **Navigation:** Compose Navigation Component.
- **Widgets:** Jetpack Glance API for custom homescreen widgets.
- **QA/CI-CD:** GitHub Actions for automated build/tests, JUnit/MockK/Turbine tests, and R8/ProGuard minification rules.

---

## 📂 Project Structure Analysis

### 🖥️ Backend Structure (`/Backend_Python`)
```text
Backend_Python/
├── app/
│   ├── models/         # Pydantic schemas (meal, user_profile, workout)
│   ├── routes/         # Modular routers (profile, workout, meal)
│   ├── database.py     # Async Motor client configuration
│   └── main.py         # Entry point, Firebase Admin SDK integration, CORS & Auth
├── food-analyser/      # AI food identification microservice
│   ├── app/            # App routes, models, schemas, and seeding scripts
│   ├── weights/        # Trained PyTorch model weights
│   ├── requirements.txt
│   └── run.py          # Food Analyser runner (port 8000)
├── Dockerfile          # Container setup
└── requirements.txt    # Base backend dependencies
```

### 📱 Android App Structure (`/GymFitness`)
```text
app/src/main/java/com/example/gymfitness/
├── data/               # Room Database, API Services, DTOs, Mappers, Repositories
│   ├── local/          # Room DB entities, migrations, and DAOs
│   ├── remote/         # Retrofit API interfaces and DTOs
│   ├── repository/     # Data repository implementations
│   └── sync/           # WorkManager SyncWorker & SyncManager
├── di/                 # Hilt Modules (Database, Network, Repository)
├── domain/             # Core Business Logic
│   ├── models/         # Domain-specific clean models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use Cases (Meal, Profile, Weight, Workout)
├── fcm/                # Firebase Cloud Messaging Service
├── presentation/       # UI Layer (MVI/MVVM)
│   ├── components/     # Custom premium widgets (GlassCard, CalorieRing, etc.)
│   ├── navigation/     # NavHost and Route definitions
│   ├── screen/         # Compose screens (Home, Meals, Onboarding, Profile, Workout)
│   └── viewmodel/      # ViewModels handling screen state
├── ui/theme/           # Midnight & Neon Typography, Color, and Theme setup
├── utils/              # Helper utilities (Health Connect, ExportUtils, StepCounter)
└── widget/             # Jetpack Glance widgets (Calorie Widget)
```

---

## ✨ Key Features & Implementation Status

1. **User Personalization & Onboarding:**
   - **Onboarding Flow:** Collects height, weight, activity level, and targets.
   - **Profile Management:** Calculates BMR and TDEE dynamically and tracks weight logs.

2. **Fitness Tracking:**
   - **Workout Logging:** Real-time workout tracking UI, modular workout structures (workouts, exercises, and sets).
   - **Timer & Details:** Interactive workout timer and detail logging UI (`WorkoutDetailedScreen`).

3. **Smart Nutrition Management:**
   - **Meal Logging:** Daily tracking of calories and macronutrients (protein, carbs, fats).
   - **Barcode Detection:** Real-time ML Kit scanner to look up foods by barcode.
   - **AI Food Image Scanning:** CameraX integration sending images to `/scan-food` for PyTorch-based food classification (MobileNetV2 backbone). Features a premium UI with an animated scanning laser, neon frame border, instruction overlay, real-time "Analyzing..." loader feedback, and optimized YUV-to-Bitmap conversion (`ImageProxy.toRotatedBitmap()`).
   - **Manual Search & Logging:** Integrated a search bar in the Quick Add sheet to search the MongoDB nutrients database. If the food is not found, users can add it manually to update the backend database for future queries while logging it to their daily meals.

4. **Robust Offline-First Sync & Database Migrations:**
   - Room stores all transactions locally.
   - WorkManager periodically executes background syncing when network connectivity is restored, sending updates directly to MongoDB Atlas.
   - Room database migration `MIGRATION_6_7` safely upgrades local tables on update without deleting user data.

5. **Gamification & Engagement:**
   - **Streak Counter:** Daily application launch tracking increases/resets user streak with full backend synchronization and local persistence.
   - **FCM & In-App Messaging:** Delivers workout prompts and streak alerts via push notifications.
   - **Calorie Glance Widget:** Displays today's calorie targets directly on the home screen launcher using Jetpack Glance.

6. **Testing & DevOps:**
   - **Unit Testing:** 12 unit tests verify ViewModels and Repositories under simulated connection conditions using MockK and Turbine.
   - **Credentials Security:** Removed `google-services.json` from Git tracking and added it to `.gitignore` to prevent credential exposure in public repositories, providing a generic `google-services.json.template` for developer setups.
   - **Obfuscation (R8/ProGuard):** Active minification on Release builds with rule exceptions for network model persistence.
   - **CI/CD:** Automated builds, tests, and linting on GitHub Actions (`android-ci.yml`).

---

## 📈 Observations & Recommendations

- **Unified Persistence:** All Realm dependencies have been removed. The application now uses Room exclusively as its local database, ensuring zero bloat and a single source of truth.
- **Backend Auth Migration:** Swapping the header API key to Firebase JWT tokens isolates end-user data correctly and matches production standards.
- **Visual Fidelity:** Clean modern aesthetics conform to Neo-Brutalism themes (dense shadows, neon border highlights, animated camera scanner visuals).

---
*Analysis updated on 2026-07-02*
