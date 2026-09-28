import io
import sys
from pathlib import Path

# Add Backend_Python root directory to sys.path
backend_dir = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(backend_dir))

from PIL import Image
from fastapi.testclient import TestClient
from app.main import app, API_KEY
from app.services.vision_service import vision_service, VisionResult, RawDetectedItem
from app.services.nutrition_engine import nutrition_engine

def create_dummy_image(width=1600, height=1200, color="green") -> bytes:
    img = Image.new("RGB", (width, height), color=color)
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    return buf.getvalue()

def run_tests():
    print("\n=======================================================")
    print("   RUNNING FOOD VISION SCANNER TEST SUITE")
    print("=======================================================\n")

    # 1. Test image resizing
    print("[1/5] Testing Image Resizing to <= 1024px...")
    large_image = create_dummy_image(1920, 1080)
    resized_bytes = vision_service.resize_image_if_needed(large_image, max_dim=1024)
    with Image.open(io.BytesIO(resized_bytes)) as img:
        w, h = img.size
        assert max(w, h) <= 1024, f"Dimension {max(w,h)} exceeded 1024"
        print(f"      PASS: 1920x1080 resized to {w}x{h} (max dim: {max(w,h)})")

    # 2. Test SHA256 hashing and LRU cache
    print("[2/5] Testing SHA-256 Hashing & In-Memory LRU Cache...")
    image_bytes = create_dummy_image(300, 300, color="red")
    hash_val = vision_service.compute_sha256(image_bytes)
    assert len(hash_val) == 64
    dummy_result = VisionResult(
        is_food=True,
        primary_food_name="Paneer Butter Masala",
        cuisine="Indian",
        estimated_grams=200.0,
        confidence=0.98,
        items=[RawDetectedItem(name="Paneer Butter Masala", estimated_grams=200.0)],
        top_alternatives=["Shahi Paneer", "Kadai Paneer"],
        vision_provider="test_cache"
    )
    vision_service._put_in_cache(hash_val, dummy_result)
    cached_res = vision_service._get_from_cache(hash_val)
    assert cached_res is not None
    assert cached_res.primary_food_name == "Paneer Butter Masala"
    assert cached_res.estimated_grams == 200.0
    print(f"      PASS: Cache hit verified for SHA-256 {hash_val[:12]}...")

    # 3. Test multi-dish plate nutrition derivation
    print("[3/5] Testing Multi-Item Plate Nutrition Resolution...")
    items = [
        {"name": "Roti", "estimated_grams": 60.0},
        {"name": "Dal tadka", "estimated_grams": 150.0},
        {"name": "Rice", "estimated_grams": 100.0}
    ]
    total_cal = 0.0
    total_prot = 0.0
    for it in items:
        nut = nutrition_engine.get_nutrition(it["name"], grams=it["estimated_grams"])
        assert nut.success is True, f"Failed for {it['name']}"
        assert nut.source.startswith("IFCT")
        total_cal += nut.calories
        total_prot += nut.macros.protein_g
        print(f"      Component '{it['name']}' ({it['estimated_grams']}g): {nut.calories:.1f} kcal, P: {nut.macros.protein_g:.1f}g")

    assert total_cal > 300.0
    assert total_prot > 8.0
    print(f"      PASS: Total Plate -> {total_cal:.1f} kcal, {total_prot:.1f}g protein (IFCT Derived)")

    # 4. Test /scan-food API endpoint
    print("[4/5] Testing /scan-food FastAPI Endpoint...")
    client = TestClient(app)
    scan_img = create_dummy_image(400, 400, color="orange")
    response = client.post(
        "/scan-food",
        files={"file": ("plate.jpg", scan_img, "image/jpeg")},
        headers={"X-API-KEY": API_KEY}
    )
    assert response.status_code == 200, f"Status: {response.status_code}, detail: {response.text}"
    data = response.json()
    assert data["success"] is True
    assert data["source"] == "IFCT_2017"
    assert data["macros"]["calories"] > 0
    assert "items" in data
    assert len(data["items"]) >= 1
    print(f"      PASS: /scan-food returned '{data['food_name']}' ({data['calories']} kcal, {data['source']})")

    # 5. Test /api/scan/feedback API endpoint
    print("[5/5] Testing /api/scan/feedback Feedback Recording Endpoint...")
    payload = {
        "scan_id": "test_scan_123",
        "predicted_food": "Roti",
        "corrected_food": "Paratha",
        "rating": 5,
        "comments": "Accurate detection, just slightly thicker paratha"
    }
    response = client.post(
        "/api/scan/feedback",
        json=payload,
        headers={"X-API-KEY": API_KEY}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "Feedback recorded" in data["message"]
    print("      PASS: /api/scan/feedback recorded telemetry successfully.")

    print("\n-------------------------------------------------------")
    print("   ALL 5 FOOD VISION PIPELINE TESTS PASSED (100%)")
    print("-------------------------------------------------------\n")

if __name__ == "__main__":
    run_tests()
