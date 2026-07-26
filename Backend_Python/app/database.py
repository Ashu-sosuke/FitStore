import os
import ssl
import certifi
from motor.motor_asyncio import AsyncIOMotorClient
from dotenv import load_dotenv

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")

# Configuration for MongoDB Atlas vs Local
client_kwargs = {}
if "mongodb+srv" in MONGO_URI:
    # Cloud settings (Atlas)
    client_kwargs["tls"] = True
    client_kwargs["tlsCAFile"] = certifi.where()
    client_kwargs["serverSelectionTimeoutMS"] = 5000
else:
    # Local settings (Development)
    client_kwargs["tlsAllowInvalidCertificates"] = True
    client_kwargs["serverSelectionTimeoutMS"] = 2000

client = AsyncIOMotorClient(MONGO_URI, **client_kwargs)
db = client.get_database("fitness-tracker")

# Collection helpers
user_profiles_collection = db.get_collection("userprofiles")
workouts_collection = db.get_collection("workouts")
meals_collection = db.get_collection("meals")
nutrients_collection = db.get_collection("nutrients")

async def ping_db():
    try:
        # The 'ping' command is cheap and checks if we can talk to the server
        await client.admin.command('ping')
        print(f"[OK] Connected to MongoDB: {'Cloud' if 'mongodb+srv' in MONGO_URI else 'Local'}")
    except Exception as e:
        print(f"[ERROR] MongoDB connection failed: {e}")
