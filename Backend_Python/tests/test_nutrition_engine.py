"""
Comprehensive Test Suite for IFCT 2017 Nutrition Engine
Evaluates exact staples, Hindi/Hinglish aliases, composite Indian dishes, and typos.
"""

import sys
from pathlib import Path

# Add backend directory to path
backend_dir = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(backend_dir))

from app.services.nutrition_engine import nutrition_engine

# 45+ Real-world test queries with expected category and minimum expected calories/macros
TEST_QUERIES = [
    # 1. Raw Staples & Cereals
    {"query": "rice", "grams": 100, "expected_source": ["IFCT_EXACT", "IFCT_ALIAS"], "should_match": True},
    {"query": "chawal", "grams": 100, "expected_source": ["IFCT_ALIAS"], "should_match": True},
    {"query": "atta", "grams": 100, "expected_source": ["IFCT_ALIAS"], "should_match": True},
    {"query": "wheat flour", "grams": 100, "expected_source": ["IFCT_EXACT", "IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "poha", "grams": 100, "expected_source": ["IFCT_ALIAS"], "should_match": True},
    {"query": "suji", "grams": 100, "expected_source": ["IFCT_ALIAS"], "should_match": True},
    {"query": "oats", "grams": 100, "expected_source": ["IFCT_EXACT", "IFCT_ALIAS"], "should_match": True},
    {"query": "ragi", "grams": 100, "expected_source": ["IFCT_ALIAS"], "should_match": True},
    
    # 2. Pulses & Legumes (Dals)
    {"query": "toor dal", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "arhar dal", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "moong dal", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "chana dal", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "rajma", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "kabuli chana", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "besan", "grams": 100, "expected_source": ["IFCT_ALIAS"], "should_match": True},
    {"query": "soya chunks", "grams": 100, "expected_source": ["IFCT_ALIAS"], "should_match": True},

    # 3. Vegetables & Produce
    {"query": "aloo", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "potato", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "pyaz", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "tamatar", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "palak", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "bhindi", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "methi", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "gobi", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},

    # 4. Dairy & Animal Protein
    {"query": "paneer", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "dahi", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "cow milk", "grams": 100, "expected_source": ["IFCT_EXACT", "IFCT_ALIAS"], "should_match": True},
    {"query": "ghee", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "egg", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},
    {"query": "egg white", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_EXACT", "IFCT_FUZZY"], "should_match": True},
    {"query": "chicken breast", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_EXACT"], "should_match": True},
    {"query": "mutton", "grams": 100, "expected_source": ["IFCT_ALIAS", "IFCT_FUZZY"], "should_match": True},

    # 5. Composite Cooked Indian Dishes
    {"query": "roti", "grams": 35, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "chapati", "grams": 40, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "aloo paratha", "grams": 120, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "paneer paratha", "grams": 130, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "dal tadka", "grams": 180, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "dal makhani", "grams": 200, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "paneer butter masala", "grams": 220, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "palak paneer", "grams": 220, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "chicken biryani", "grams": 320, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "butter chicken", "grams": 240, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "idli", "grams": 40, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "plain dosa", "grams": 80, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "masala dosa", "grams": 160, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "samosa", "grams": 80, "expected_source": ["IFCT_DISH"], "should_match": True},
    {"query": "kanda poha", "grams": 160, "expected_source": ["IFCT_DISH"], "should_match": True},

    # 6. Misspellings / Typos / Hinglish queries
    {"query": "paner", "grams": 100, "expected_source": ["IFCT_FUZZY", "IFCT_ALIAS"], "should_match": True},
    {"query": "chiken breast", "grams": 100, "expected_source": ["IFCT_FUZZY", "IFCT_ALIAS"], "should_match": True},
    {"query": "baryani", "grams": 300, "expected_source": ["IFCT_DISH", "IFCT_FUZZY"], "should_match": True},
    {"query": "chapatty", "grams": 40, "expected_source": ["IFCT_DISH", "IFCT_FUZZY"], "should_match": True},
    {"query": "idly", "grams": 40, "expected_source": ["IFCT_DISH", "IFCT_FUZZY"], "should_match": True},
    {"query": "dhosa", "grams": 80, "expected_source": ["IFCT_DISH", "IFCT_FUZZY"], "should_match": True},
    {"query": "almonds", "grams": 25, "expected_source": ["IFCT_ALIAS", "IFCT_EXACT", "IFCT_FUZZY"], "should_match": True}
]

def run_nutrition_tests():
    passed = 0
    failed = 0
    misses = []

    print(f"\n=======================================================")
    print(f"   RUNNING IFCT 2017 NUTRITION ENGINE TEST SUITE")
    print(f"=======================================================\n")

    for idx, test in enumerate(TEST_QUERIES, 1):
        q = test["query"]
        grams = test.get("grams", 100)
        res = nutrition_engine.get_nutrition(q, grams=grams)

        is_success = res.success
        source_matched = res.source in test["expected_source"] if test.get("expected_source") else is_success
        has_positive_macros = res.calories > 0 and (res.macros.protein_g > 0 or res.macros.carbs_g > 0 or res.macros.fats_g > 0)

        if is_success and has_positive_macros and source_matched:
            passed += 1
            print(f"[{idx:02d}/55] PASS: '{q}' ({grams}g) -> {res.matched_name} | {res.calories} kcal [P:{res.macros.protein_g}g, C:{res.macros.carbs_g}g, F:{res.macros.fats_g}g] ({res.source}, {res.confidence})")
        else:
            failed += 1
            misses.append({
                "index": idx,
                "query": q,
                "grams": grams,
                "success": res.success,
                "source": res.source,
                "matched_name": res.matched_name,
                "expected": test.get("expected_source")
            })
            print(f"[{idx:02d}/55] FAIL: '{q}' -> Matched: '{res.matched_name}' (Source: {res.source}, Expected: {test.get('expected_source')})")

    total = len(TEST_QUERIES)
    pass_rate = (passed / total) * 100.0

    print(f"\n-------------------------------------------------------")
    print(f"TEST RESULTS SUMMARY:")
    print(f"Total Queries Tested: {total}")
    print(f"Passed: {passed}")
    print(f"Failed: {failed}")
    print(f"Pass Rate: {pass_rate:.1f}%")
    print(f"-------------------------------------------------------")

    if misses:
        print(f"\nDetailed Misses / Failed Cases:")
        for m in misses:
            print(f" - Query: '{m['query']}' | Result: '{m['matched_name']}' | Source: {m['source']} | Expected: {m['expected']}")
    else:
        print(f"\nAll {total} test queries passed with 100% accuracy!")

    return pass_rate, misses

if __name__ == "__main__":
    run_nutrition_tests()
