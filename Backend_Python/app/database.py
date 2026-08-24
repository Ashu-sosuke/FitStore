import os
import certifi
from motor.motor_asyncio import AsyncIOMotorClient
from dotenv import load_dotenv

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
DB_NAME = os.getenv("DB_NAME", "fitness-tracker")

client_kwargs = {
    "serverSelectionTimeoutMS": 10000,
    "connectTimeoutMS": 10000,
    "socketTimeoutMS": 20000,
}

if "mongodb+srv" in MONGO_URI:
    # Cloud settings (MongoDB Atlas)
    client_kwargs["tls"] = True
    client_kwargs["tlsCAFile"] = certifi.where()
    client_kwargs["tlsAllowInvalidCertificates"] = True
else:
    # Local settings
    client_kwargs["tlsAllowInvalidCertificates"] = True

client = AsyncIOMotorClient(MONGO_URI, **client_kwargs)
db = client.get_database(DB_NAME)

# Collection helpers
user_profiles_collection = db.get_collection("userprofiles")
workouts_collection = db.get_collection("workouts")
meals_collection = db.get_collection("meals")
nutrients_collection = db.get_collection("nutrients")
exercises_catalog_collection = db.get_collection("exercises_catalog")

async def ping_db():
    try:
        await client.admin.command('ping')
        print(f"[OK] Connected to MongoDB: {'Cloud' if 'mongodb+srv' in MONGO_URI else 'Local'}")
    except Exception as e:
        print(f"[ERROR] MongoDB connection failed: {e}")
