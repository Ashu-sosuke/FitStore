import os
import json
from typing import List, Dict, Any, Optional

candidate_1 = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "exercisedb_v1_sample"))
candidate_2 = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "exercisedb_v1_sample"))

if os.path.exists(candidate_1):
    DATASET_DIR = candidate_1
elif os.path.exists(candidate_2):
    DATASET_DIR = candidate_2
else:
    DATASET_DIR = candidate_1

# In-memory cached dataset
_CACHED_EXERCISES: Optional[List[Dict[str, Any]]] = None
_CACHED_BODY_PARTS: Optional[List[Dict[str, Any]]] = None
_CACHED_EQUIPMENTS: Optional[List[Dict[str, Any]]] = None
_CACHED_MUSCLES: Optional[List[Dict[str, Any]]] = None


def load_local_dataset() -> Dict[str, Any]:
    """Loads dataset files from the local exercisedb_v1_sample folder."""
    global _CACHED_EXERCISES, _CACHED_BODY_PARTS, _CACHED_EQUIPMENTS, _CACHED_MUSCLES
    
    if _CACHED_EXERCISES is not None:
        return {
            "exercises": _CACHED_EXERCISES,
            "bodyParts": _CACHED_BODY_PARTS or [],
            "equipments": _CACHED_EQUIPMENTS or [],
            "muscles": _CACHED_MUSCLES or []
        }
    
    exercises_file = os.path.join(DATASET_DIR, "exercises.json")
    body_parts_file = os.path.join(DATASET_DIR, "bodyParts.json")
    equipments_file = os.path.join(DATASET_DIR, "equipments.json")
    muscles_file = os.path.join(DATASET_DIR, "muscles.json")

    exercises = []
    body_parts = []
    equipments = []
    muscles = []

    if os.path.exists(exercises_file):
        with open(exercises_file, "r", encoding="utf-8") as f:
            exercises = json.load(f)
    if os.path.exists(body_parts_file):
        with open(body_parts_file, "r", encoding="utf-8") as f:
            body_parts = json.load(f)
    if os.path.exists(equipments_file):
        with open(equipments_file, "r", encoding="utf-8") as f:
            equipments = json.load(f)
    if os.path.exists(muscles_file):
        with open(muscles_file, "r", encoding="utf-8") as f:
            muscles = json.load(f)

    _CACHED_EXERCISES = exercises
    _CACHED_BODY_PARTS = body_parts
    _CACHED_EQUIPMENTS = equipments
    _CACHED_MUSCLES = muscles

    return {
        "exercises": exercises,
        "bodyParts": body_parts,
        "equipments": equipments,
        "muscles": muscles
    }


def get_cached_exercises() -> List[Dict[str, Any]]:
    dataset = load_local_dataset()
    return dataset["exercises"]


async def seed_exercise_catalog(catalog_collection) -> int:
    """Seeds the MongoDB exercise catalog from local ExerciseDB files if empty or forced."""
    dataset = load_local_dataset()
    exercises = dataset["exercises"]
    
    if not exercises:
        print("[WARNING] No exercises found in local ExerciseDB directory.")
        return 0

    count = await catalog_collection.count_documents({})
    if count == 0:
        # Create index on exerciseId
        await catalog_collection.create_index("exerciseId", unique=True)
        await catalog_collection.create_index("targetMuscles")
        await catalog_collection.create_index("bodyParts")
        await catalog_collection.create_index("equipments")
        
        # Insert exercises
        await catalog_collection.insert_many(exercises)
        print(f"[OK] Seeded {len(exercises)} exercises into MongoDB exercises_catalog.")
        return len(exercises)
    else:
        print(f"[INFO] exercises_catalog already contains {count} records.")
        return count
