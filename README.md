# FitStore

![Android CI](https://github.com/yourusername/FitStore/actions/workflows/android-ci.yml/badge.svg)

FitStore is an offline-first Android fitness application with a bold Neo-Brutalism design. It provides seamless tracking of workouts, meals, health connect sync (steps, sleep), and syncs with a Python backend using WorkManager.

## Features
- **MVI / MVVM Architecture**: Built completely on modern Android standards.
- **Offline-first & Background Sync**: Uses Room for local database and WorkManager to sync with the backend.
- **Health Connect Integration**: Syncs steps and sleep data from Health Connect.
- **Data Export**: Export workout and meal data to CSV.
- **Calorie Glance Widget**: A beautiful homescreen widget to track your daily calorie intake.
- **Streak Gamification**: Motivates users by keeping track of the daily app usage streak.
- **Neo-Brutalism Design**: High contrast, bold shapes, and solid shadows.
- **Security & Obfuscation**: Uses R8/ProGuard rules, HTTPS certificate pinning, and Firebase JWT Auth.

## Tech Stack
- **Android**: Kotlin, Jetpack Compose, Hilt, Room (SQLite), Retrofit, OkHttp, WorkManager, Health Connect, Glance Widget, Firebase (FCM, In-App Messaging, Crashlytics)
- **Backend**: Python (FastAPI), MongoDB Atlas, Firebase Admin SDK
- **DevOps / QA**: GitHub Actions, MockK, Turbine, JUnit 4

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 21
- Firebase Console Account

### Demo GIF
> **Tip for recruiters / reviewers:** Replace this placeholder with a GIF demonstrating the main user flow.
![Demo GIF placeholder](https://via.placeholder.com/800x400.png?text=Add+Demo+GIF+Here)

### Running the App
1. Clone this repository.
2. Open `GymFitness` in Android Studio.
3. Configure `local.properties` in the root:
   ```properties
   API_KEY="your_secret_key"
   ```
4. Place your `google-services.json` from the Firebase console in the `GymFitness/app/` directory (you can copy and rename `google-services.json.template` as a reference/base). Note that `google-services.json` is untracked by Git for security.
5. Setup the Python FastAPI server (see instructions in [requirements.md](file:///d:/CodeHub/WOork/FitStore/requirements.md)).
6. Run on an emulator or a real device (API 26+).

## License
MIT License
