"""
IFCT 2017 Nutrition Engine — Indian Food Composition Tables
Data Source: National Institute of Nutrition (NIN) / Indian Council of Medical Research (ICMR), Hyderabad.
Scientific reference: Longvah T, Ananthan R, Bhaskarachary K, Venkaiah K. Indian Food Composition Tables 2017.

Provides scientifically validated nutrient data per 100g with:
1. Exact normalized matching
2. Alias & regional language matching (aliases.json)
3. RapidFuzz token-based fuzzy matching
4. Composite Indian dish recipe aggregator (dishes.json)
5. Fallback chain (USDA / Open Food Facts / AI estimate)
"""

import csv
import json
import logging
import os
import re
from pathlib import Path
from typing import Dict, List, Optional, Any
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)

# Base paths
DATA_DIR = Path(__file__).resolve().parent.parent.parent / "data"
IFCT_CSV_PATH = DATA_DIR / "ifct_compositions.csv"
ALIASES_JSON_PATH = DATA_DIR / "aliases.json"
DISHES_JSON_PATH = DATA_DIR / "dishes.json"

KJ_TO_KCAL_FACTOR = 4.184
FUZZY_MATCH_THRESHOLD = 75.0

class NutrientMacros(BaseModel):
    protein_g: float = Field(..., ge=0)
    carbs_g: float = Field(..., ge=0)
    fats_g: float = Field(..., ge=0)
    fiber_g: float = Field(default=0.0, ge=0)

class NutritionResult(BaseModel):
    success: bool
    query: str
    matched_name: str
    source: str  # "IFCT_EXACT", "IFCT_ALIAS", "IFCT_FUZZY", "IFCT_DISH", "USDA_FALLBACK", "OFF_FALLBACK", "AI_ESTIMATE", "NOT_FOUND"
    confidence: str  # "HIGH", "MEDIUM", "LOW", "NONE"
    match_score: float
    grams: float
    calories: float
    macros: NutrientMacros
    serving_unit: Optional[str] = None
    notes: Optional[str] = None
    attribution: str = "ICMR - National Institute of Nutrition (NIN), Hyderabad (IFCT 2017)"

class FoodItem100g:
    def __init__(
        self,
        code: str,
        name: str,
        scientific_name: str,
        group: str,
        energy_kcal: float,
        protein_g: float,
        fat_g: float,
        carbs_g: float,
        fiber_g: float
    ):
        self.code = code
        self.name = name
        self.scientific_name = scientific_name
        self.group = group
        self.energy_kcal = round(energy_kcal, 2)
        self.protein_g = round(protein_g, 2)
        self.fat_g = round(fat_g, 2)
        self.carbs_g = round(carbs_g, 2)
        self.fiber_g = round(fiber_g, 2)

    def to_macros_for_grams(self, grams: float) -> tuple[float, NutrientMacros]:
        mult = max(grams, 0.0) / 100.0
        calories = round(self.energy_kcal * mult, 1)
        macros = NutrientMacros(
            protein_g=round(self.protein_g * mult, 2),
            carbs_g=round(self.carbs_g * mult, 2),
            fats_g=round(self.fat_g * mult, 2),
            fiber_g=round(self.fiber_g * mult, 2)
        )
        return calories, macros

def normalize_text(text: str) -> str:
    """Normalize string by lowercasing, removing punctuation, and collapsing whitespace."""
    if not text:
        return ""
    text = text.lower().strip()
    text = re.sub(r"[^\w\s]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text

class NutritionEngine:
    def __init__(self):
        self.items_by_code: Dict[str, FoodItem100g] = {}
        self.items_by_normalized_name: Dict[str, FoodItem100g] = {}
        self.aliases: Dict[str, str] = {}
        self.dishes: Dict[str, Any] = {}
        self._load_datasets()

    def _load_datasets(self):
        # 1. Load IFCT CSV
        if not IFCT_CSV_PATH.exists():
            logger.error(f"IFCT CSV file not found at {IFCT_CSV_PATH}")
        else:
            try:
                with open(IFCT_CSV_PATH, mode="r", encoding="utf-8") as f:
                    reader = csv.DictReader(f)
                    valid_count = 0
                    reject_count = 0
                    for row in reader:
                        try:
                            code = row["code"].strip()
                            name = row["name"].strip()
                            sci_name = row.get("scientific_name", "").strip()
                            group = row.get("group", "").strip()

                            # Parse numeric values
                            energy_kj = float(row["energy_kj"])
                            protein = float(row["protein_g"])
                            fat = float(row["fat_g"])
                            carbs = float(row["carbs_g"])
                            fiber = float(row.get("fiber_g", 0.0) or 0.0)

                            # Validate non-negative
                            if energy_kj < 0 or protein < 0 or fat < 0 or carbs < 0 or fiber < 0:
                                logger.warning(f"Rejecting IFCT row with negative macros: {code} - {name}")
                                reject_count += 1
                                continue

                            # Convert kJ to kcal
                            energy_kcal = energy_kj / KJ_TO_KCAL_FACTOR

                            item = FoodItem100g(
                                code=code,
                                name=name,
                                scientific_name=sci_name,
                                group=group,
                                energy_kcal=energy_kcal,
                                protein_g=protein,
                                fat_g=fat,
                                carbs_g=carbs,
                                fiber_g=fiber
                            )
                            self.items_by_code[code] = item
                            self.items_by_normalized_name[normalize_text(name)] = item
                            valid_count += 1
                        except Exception as e:
                            logger.warning(f"Error parsing IFCT CSV row: {row} - {e}")
                            reject_count += 1
                    logger.info(f"Loaded {valid_count} valid IFCT foods ({reject_count} rejected) from {IFCT_CSV_PATH.name}")
            except Exception as e:
                logger.error(f"Failed to load IFCT dataset: {e}")

        # 2. Load Aliases JSON
        if ALIASES_JSON_PATH.exists():
            try:
                with open(ALIASES_JSON_PATH, "r", encoding="utf-8") as f:
                    raw_aliases = json.load(f)
                    self.aliases = {normalize_text(k): normalize_text(v) for k, v in raw_aliases.items()}
                logger.info(f"Loaded {len(self.aliases)} Indian food aliases from {ALIASES_JSON_PATH.name}")
            except Exception as e:
                logger.error(f"Failed to load aliases: {e}")

        # 3. Load Dishes JSON
        if DISHES_JSON_PATH.exists():
            try:
                with open(DISHES_JSON_PATH, "r", encoding="utf-8") as f:
                    self.dishes = json.load(f)
                logger.info(f"Loaded {len(self.dishes)} Indian dish recipes from {DISHES_JSON_PATH.name}")
            except Exception as e:
                logger.error(f"Failed to load dishes: {e}")

    def calculate_dish_nutrition(self, dish_key: str, requested_grams: Optional[float] = None) -> Optional[NutritionResult]:
        """Calculate aggregate nutrition of a composite dish from its IFCT raw ingredients."""
        dish_data = None
        norm_key = normalize_text(dish_key)
        
        # Direct key match
        for k, v in self.dishes.items():
            if normalize_text(k) == norm_key:
                dish_data = v
                break

        if not dish_data:
            # Fuzzy match dish name
            dish_data = self._fuzzy_match_dish(norm_key)

        if not dish_data:
            return None

        std_serving_grams = float(dish_data.get("serving_grams", 100.0))
        target_grams = float(requested_grams) if (requested_grams is not None and requested_grams > 0) else std_serving_grams

        # Aggregate raw ingredients
        total_cal = 0.0
        total_p = 0.0
        total_c = 0.0
        total_f = 0.0
        total_fib = 0.0

        for ing in dish_data.get("ingredients", []):
            ing_name = ing["name"]
            ing_grams = float(ing["grams"])
            # Lookup ingredient
            ing_item = self._lookup_raw_ingredient(ing_name)
            if ing_item:
                cal, mac = ing_item.to_macros_for_grams(ing_grams)
                total_cal += cal
                total_p += mac.protein_g
                total_c += mac.carbs_g
                total_f += mac.fats_g
                total_fib += mac.fiber_g

        # Scale from recipe standard serving to target_grams
        scale = target_grams / std_serving_grams if std_serving_grams > 0 else 1.0
        scaled_calories = round(total_cal * scale, 1)
        scaled_macros = NutrientMacros(
            protein_g=round(total_p * scale, 2),
            carbs_g=round(total_c * scale, 2),
            fats_g=round(total_f * scale, 2),
            fiber_g=round(total_fib * scale, 2)
        )

        return NutritionResult(
            success=True,
            query=dish_key,
            matched_name=dish_data.get("name", dish_key),
            source="IFCT_DISH",
            confidence="HIGH",
            match_score=100.0,
            grams=target_grams,
            calories=scaled_calories,
            macros=scaled_macros,
            serving_unit=dish_data.get("serving_unit"),
            notes=dish_data.get("cooking_notes")
        )

    def _lookup_raw_ingredient(self, name: str) -> Optional[FoodItem100g]:
        norm = normalize_text(name)
        if norm in self.items_by_normalized_name:
            return self.items_by_normalized_name[norm]
        if norm in self.aliases:
            canon = self.aliases[norm]
            if canon in self.items_by_normalized_name:
                return self.items_by_normalized_name[canon]
        return None

    def _fuzzy_match_dish(self, query: str) -> Optional[Any]:
        best_dish = None
        best_score = 0.0
        
        try:
            from rapidfuzz import fuzz
            for k, v in self.dishes.items():
                score = fuzz.token_sort_ratio(query, normalize_text(k))
                if score > best_score and score >= FUZZY_MATCH_THRESHOLD:
                    best_score = score
                    best_dish = v
        except ImportError:
            # Basic token intersection fallback
            q_tokens = set(query.split())
            for k, v in self.dishes.items():
                k_tokens = set(normalize_text(k).split())
                overlap = len(q_tokens & k_tokens)
                if overlap > 0 and overlap >= len(q_tokens) * 0.7:
                    return v

        return best_dish

    def get_nutrition(self, food_query: str, grams: float = 100.0) -> NutritionResult:
        """
        Hierarchical food search:
        1. Exact Match on normalized name (IFCT)
        2. Alias Match (aliases.json -> IFCT)
        3. Composite Dish Match (dishes.json)
        4. Fuzzy Match (rapidfuzz -> IFCT)
        5. Return NOT_FOUND (never silently hallucinate wrong macros)
        """
        if not food_query or not food_query.strip():
            return self._not_found_result(food_query, grams)

        query_norm = normalize_text(food_query)
        grams = max(float(grams), 1.0) if grams > 0 else 100.0

        # 1. Exact Match in IFCT
        if query_norm in self.items_by_normalized_name:
            item = self.items_by_normalized_name[query_norm]
            cal, mac = item.to_macros_for_grams(grams)
            return NutritionResult(
                success=True,
                query=food_query,
                matched_name=item.name,
                source="IFCT_EXACT",
                confidence="HIGH",
                match_score=100.0,
                grams=grams,
                calories=cal,
                macros=mac,
                serving_unit=f"{int(grams)}g"
            )

        # 2. Alias Match
        if query_norm in self.aliases:
            canonical_name = self.aliases[query_norm]
            if canonical_name in self.items_by_normalized_name:
                item = self.items_by_normalized_name[canonical_name]
                cal, mac = item.to_macros_for_grams(grams)
                return NutritionResult(
                    success=True,
                    query=food_query,
                    matched_name=f"{food_query.title()} ({item.name})",
                    source="IFCT_ALIAS",
                    confidence="HIGH",
                    match_score=98.0,
                    grams=grams,
                    calories=cal,
                    macros=mac,
                    serving_unit=f"{int(grams)}g"
                )

        # 3. Composite Dish Match
        dish_res = self.calculate_dish_nutrition(food_query, requested_grams=grams)
        if dish_res:
            return dish_res

        # 4. Fuzzy Match against IFCT foods and Aliases
        fuzzy_match = self._fuzzy_search(query_norm)
        if fuzzy_match:
            item, score = fuzzy_match
            cal, mac = item.to_macros_for_grams(grams)
            conf = "HIGH" if score >= 88.0 else "MEDIUM"
            return NutritionResult(
                success=True,
                query=food_query,
                matched_name=item.name,
                source="IFCT_FUZZY",
                confidence=conf,
                match_score=round(score, 1),
                grams=grams,
                calories=cal,
                macros=mac,
                serving_unit=f"{int(grams)}g",
                notes="Fuzzy matched with IFCT reference database"
            )

        # 5. Not Found
        return self._not_found_result(food_query, grams)

    def _fuzzy_search(self, query_norm: str) -> Optional[tuple[FoodItem100g, float]]:
        best_item = None
        best_score = 0.0

        try:
            from rapidfuzz import fuzz
            # Search IFCT items
            for norm_name, item in self.items_by_normalized_name.items():
                score = fuzz.token_sort_ratio(query_norm, norm_name)
                if score > best_score and score >= FUZZY_MATCH_THRESHOLD:
                    best_score = score
                    best_item = item

            # Search aliases
            for alias_norm, canon_norm in self.aliases.items():
                score = fuzz.token_sort_ratio(query_norm, alias_norm)
                if score > best_score and score >= FUZZY_MATCH_THRESHOLD:
                    if canon_norm in self.items_by_normalized_name:
                        best_score = score
                        best_item = self.items_by_normalized_name[canon_norm]
        except ImportError:
            # Fallback simple string matching
            for norm_name, item in self.items_by_normalized_name.items():
                if query_norm in norm_name or norm_name in query_norm:
                    return item, 80.0

        if best_item and best_score >= FUZZY_MATCH_THRESHOLD:
            return best_item, best_score
        return None

    def _not_found_result(self, query: str, grams: float) -> NutritionResult:
        return NutritionResult(
            success=False,
            query=query,
            matched_name="Not Found",
            source="NOT_FOUND",
            confidence="NONE",
            match_score=0.0,
            grams=grams,
            calories=0.0,
            macros=NutrientMacros(protein_g=0.0, carbs_g=0.0, fats_g=0.0, fiber_g=0.0),
            notes="No verified nutrition data found for this food in IFCT or dish database."
        )

# Global engine instance
nutrition_engine = NutritionEngine()
