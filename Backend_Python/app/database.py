import os
import logging
import certifi
from motor.motor_asyncio import AsyncIOMotorClient
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
DB_NAME = os.getenv("DB_NAME", "fitness-tracker")

client_kwargs = {
    "serverSelectionTimeoutMS": 10000,
    "connectTimeoutMS": 10000,
    "socketTimeoutMS": 20000,
}

if "mongodb+srv" in MONGO_URI:
    # Cloud settings (MongoDB Atlas) — enforce proper TLS verification
    client_kwargs["tls"] = True
    client_kwargs["tlsCAFile"] = certifi.where()
else:
    # Local development — skip TLS verification
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
        logger.info(f"Connected to MongoDB: {'Cloud' if 'mongodb+srv' in MONGO_URI else 'Local'}")
    except Exception as e:
        logger.error(f"MongoDB connection failed: {e}")
