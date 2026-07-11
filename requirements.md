# Technical Requirements & Setup

## 🛠️ Prerequisites

### Backend (Python)
- **Python**: v3.10 or higher
- **pip**: v23.x or higher
- **MongoDB Atlas**: Account with active cluster.
- **Firebase Project**: Service account credentials JSON for verification.

### Android
- **Android Studio**: Ladybug (2024.2.1)+
- **JDK**: Version 21
- **Android SDK**: API Level 35 (minSdk 26 required for Health Connect)

---

## ⚙️ Environment Configuration

### Backend Setup
1. Create a virtual environment: `python -m venv venv`
2. Activate venv: `source venv/bin/activate` (or `venv\Scripts\activate` on Windows)
3. Install dependencies: `pip install -r requirements.txt`
4. Set up Firebase: Add your Firebase Admin SDK service account key configuration file and place its path in `.env` (e.g., `FIREBASE_CREDENTIALS=path/to/firebase-key.json`).
5. Configure `.env`:
    - `MONGO_URI`: Your MongoDB connection string.
    - `PORT`: 5000 (default)

### Food Analyser Setup
1. Navigate to `Backend_Python/food-analyser/`
2. Install specific ML dependencies: `pip install -r requirements.txt`
3. Ensure weights are present in `weights/food_mobilenetv2.pth`.

### Android Setup
1. Define local API keys: Create a key `API_KEY="your_api_key_value"` inside `local.properties` in your Android root. This key will be automatically injected into `BuildConfig` at compile-time.
2. Link Firebase: Obtain your `google-services.json` from the Firebase Console and place it into the `GymFitness/app/` directory (you can copy and rename `google-services.json.template` as a base). This file is untracked by Git for security.

---

## 📦 Key Dependencies

### Backend (Python)
- `fastapi`: Web framework.
- `uvicorn`: ASGI server.
- `motor`: Async MongoDB driver.
- `pydantic`: Data validation.
- `firebase-admin`: Firebase Authentication backend validation.
- `torch/torchvision`: ML inference (for food-analyser).

### Android (Gradle)
- `Jetpack Compose`: UI components.
- `Hilt`: Dependency Injection.
- `Retrofit 2` & `OkHttp 4`: Network calls.
- `Room`: SQLite local caching (Realm dependencies removed).
- `Health Connect`: AndroidX client for step and sleep sessions records.
- `Jetpack Glance`: Declarative Homescreen Widgets.
- `Firebase SDK`: FCM Messaging, In-App Messaging, Crashlytics, Analytics, Common Core.
- `JUnit 4`, `MockK`, `Turbine`: Testing framework.

---
*Last Updated: 2026-06-05*
