"""
Asynchronous MongoDB connection via Motor.

Exposes a singleton client that is created on FastAPI startup and torn down on
shutdown, preventing connection leaks.
"""

import certifi
from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase
from app.config import MONGO_URI, DATABASE_NAME

_client: AsyncIOMotorClient | None = None
_db: AsyncIOMotorDatabase | None = None
_is_online: bool = False


async def connect_db() -> None:
    """Initialise the Motor client and verify connectivity."""
    global _client, _db, _is_online

    # Detect if we are on localhost to skip SSL requirements
    is_localhost = "localhost" in MONGO_URI or "127.0.0.1" in MONGO_URI
    
    client_kwargs = {
        "maxPoolSize": 10,
        "minPoolSize": 2,
        "serverSelectionTimeoutMS": 5000,
    }
    
    if not is_localhost:
        # Using certifi for CA bundle on Cloud (Atlas) to avoid SSL handshake errors
        client_kwargs["tlsCAFile"] = certifi.where()
    
    _client = AsyncIOMotorClient(MONGO_URI, **client_kwargs)
    _db = _client[DATABASE_NAME]
    
    # Verify connection
    try:
        await _client.admin.command("ping")
        print(f"[OK] Connected to MongoDB - database: {DATABASE_NAME}")
        _is_online = True
    except Exception as e:
        print(f"[WARNING] MongoDB connection failed: {e}")
        print("[INFO] Application will continue without database features (logging/macros).")
        _is_online = False


async def close_db() -> None:
    """Gracefully close the Motor client."""
    global _client, _db, _is_online
    if _client is not None:
        _client.close()
        _client = None
        _db = None
        _is_online = False
        print("[OK] MongoDB connection closed")


def get_database() -> AsyncIOMotorDatabase:
    """Return the active database handle (call after connect_db)."""
    if _db is None:
        raise RuntimeError("Database not initialised - call connect_db() first")
    return _db


def is_database_online() -> bool:
    """Check if the database connection was successfully verified on startup."""
    return _is_online
