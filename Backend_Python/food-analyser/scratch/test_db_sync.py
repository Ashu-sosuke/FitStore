import os
import pymongo
from dotenv import load_dotenv

load_dotenv()
uri = os.getenv("MONGO_URI")
print(f"Connecting to: {uri}")

try:
    client = pymongo.MongoClient(uri, serverSelectionTimeoutMS=5000)
    print("Ping...")
    client.admin.command('ping')
    print("Success!")
except Exception as e:
    print(f"Error: {e}")
