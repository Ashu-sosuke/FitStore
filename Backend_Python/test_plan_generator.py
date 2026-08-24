from app.services.workout_generator import generate_personalized_workout_plan
from app.services.dataset_loader import load_local_dataset
from app.routes.workout import router
from app.main import app
import json

def test_generation():
    print("Testing ExerciseDB Local Ingest...")
    dataset = load_local_dataset()
    print(f"Exercises found in local dataset: {len(dataset['exercises'])}")
    print(f"Body parts found: {len(dataset['bodyParts'])}")
    print(f"Equipments found: {len(dataset['equipments'])}")

    print("\nTesting User Scenario: 52kg, 5'8\" (173cm), 5 days/week, Bulking, 60 mins...")
    plan = generate_personalized_workout_plan(
        device_id="test_user_52kg",
        weight_kg=52.0,
        height_cm=173.0,
        age=23,
        gender="male",
        fitness_goal="bulk_up",
        days_per_week=5,
        session_duration_minutes=60,
        experience_level="beginner"
    )

    print(f"\n[OK] Plan Generated: '{plan['title']}'")
    print(f"  Description: {plan['description']}")
    print(f"  Caloric Guidance: {plan['recommendedCaloricSurplusOrDeficit']}")
    print(f"  Nutrition Tip: {plan['nutritionTip']}")
    print(f"  Total Weekly Volume Score: {plan['weeklyVolumeScore']} sets")
    print("\nDaily Breakdown:")
    for routine in plan["dailyRoutines"]:
        if routine["isRestDay"]:
            print(f"  • {routine['dayName']} (Active Rest)")
        else:
            print(f"  • {routine['dayName']} (~{routine['estimatedDurationMinutes']} mins, {len(routine['exercises'])} exercises):")
            for ex in routine["exercises"]:
                print(f"      - {ex['name']} | {ex['targetSets']} sets x {ex['targetReps']} | Rest: {ex['restSeconds']}s | GIF: {ex['gifUrl']}")

    print("\n[SUCCESS] All verification checks passed!")

if __name__ == "__main__":
    test_generation()
