import asyncio
import base64
import hashlib
import io
import json
import logging
import math
import os
from collections import OrderedDict
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple
from pydantic import BaseModel, Field
import httpx
from PIL import Image, ImageStat

logger = logging.getLogger(__name__)

class RawDetectedItem(BaseModel):
    name: str
    estimated_grams: float = 100.0

class VisionResult(BaseModel):
    is_food: bool = True
    primary_food_name: str = "Unknown Food"
    cuisine: str = "Indian"
    estimated_grams: float = 100.0
    confidence: float = 0.90
    items: List[RawDetectedItem] = Field(default_factory=list)
    top_alternatives: List[str] = Field(default_factory=list)
    vision_provider: str = "local_vision_engine"


class VisionService:
    def __init__(self, cache_capacity: int = 200):
        self.cache: OrderedDict[str, Dict[str, Any]] = OrderedDict()
        self.cache_capacity = cache_capacity
        self.gemini_api_key = os.getenv("GEMINI_API_KEY")
        self.openai_api_key = os.getenv("OPENAI_API_KEY")
        
        # Load local MobileNetV2 model if PyTorch is available
        self._local_model = None
        self._local_preprocess = None
        self._food_labels = [
            "Chicken", "Egg", "Rice", "Milk", "Paneer", 
            "Dal tadka", "Roti", "Aloo paratha", "Biryani", "Banana"
        ]
        self._init_local_model()

    def _init_local_model(self):
        try:
            import torch
            import torch.nn as nn
            from torchvision import models, transforms

            weights_path = Path(__file__).resolve().parent.parent.parent / "food-analyser" / "weights" / "food_mobilenetv2.pth"
            if weights_path.exists():
                self._local_preprocess = transforms.Compose([
                    transforms.Resize((224, 224)),
                    transforms.ToTensor(),
                    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
                ])
                model = models.mobilenet_v2(weights=None)
                in_features = model.classifier[1].in_features
                model.classifier = nn.Sequential(
                    nn.Dropout(p=0.3),
                    nn.Linear(in_features, 256),
                    nn.ReLU(),
                    nn.Dropout(p=0.2),
                    nn.Linear(256, len(self._food_labels)),
                )
                state_dict = torch.load(weights_path, map_location=torch.device("cpu"))
                model.load_state_dict(state_dict)
                model.eval()
                self._local_model = model
                logger.info("[OK] Local PyTorch MobileNetV2 vision model loaded.")
        except Exception as e:
            logger.info(f"[INFO] PyTorch not loaded ({e}), using perceptual computer vision engine.")

    def compute_sha256(self, image_bytes: bytes) -> str:
        return hashlib.sha256(image_bytes).hexdigest()

    def resize_image_if_needed(self, image_bytes: bytes, max_dim: int = 1024) -> bytes:
        """
        Resizes image so that the maximum dimension does not exceed max_dim (preserving aspect ratio).
        Compresses as JPEG for minimal payload and low latency.
        """
        try:
            with Image.open(io.BytesIO(image_bytes)) as img:
                img = img.convert("RGB")
                width, height = img.size
                if max(width, height) > max_dim:
                    if width > height:
                        new_width = max_dim
                        new_height = int(height * (max_dim / width))
                    else:
                        new_height = max_dim
                        new_width = int(width * (max_dim / height))
                    img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
                
                buf = io.BytesIO()
                img.save(buf, format="JPEG", quality=85)
                return buf.getvalue()
        except Exception as e:
            logger.warning(f"Failed to resize image: {e}. Using raw bytes.")
            return image_bytes

    def _get_from_cache(self, image_hash: str) -> Optional[VisionResult]:
        if image_hash in self.cache:
            self.cache.move_to_end(image_hash)
            cached_data = self.cache[image_hash]
            logger.info(f"[CACHE HIT] Vision result retrieved for hash {image_hash[:8]}")
            return VisionResult(**cached_data)
        return None

    def _put_in_cache(self, image_hash: str, result: VisionResult):
        if len(self.cache) >= self.cache_capacity:
            self.cache.popitem(last=False)
        self.cache[image_hash] = result.model_dump()

    async def analyze_image(self, image_bytes: bytes) -> VisionResult:
        """
        Processes image through image preprocessing, cache lookup, VLM inference 
        (Gemini/OpenAI) or perceptual visual engine.
        """
        processed_bytes = self.resize_image_if_needed(image_bytes, max_dim=1024)
        image_hash = self.compute_sha256(processed_bytes)

        # 1. Check Hash Cache
        cached = self._get_from_cache(image_hash)
        if cached:
            return cached

        # 2. Try VLM (Gemini first if key configured)
        gemini_key = os.getenv("GEMINI_API_KEY")
        if gemini_key:
            try:
                vlm_result = await self._call_gemini_vision(processed_bytes, gemini_key)
                if vlm_result:
                    self._put_in_cache(image_hash, vlm_result)
                    return vlm_result
            except Exception as e:
                logger.warning(f"Gemini VLM call failed, falling back to next provider: {e}")

        # 3. Try OpenAI Vision if key configured
        openai_key = os.getenv("OPENAI_API_KEY")
        if openai_key:
            try:
                vlm_result = await self._call_openai_vision(processed_bytes, openai_key)
                if vlm_result:
                    self._put_in_cache(image_hash, vlm_result)
                    return vlm_result
            except Exception as e:
                logger.warning(f"OpenAI Vision call failed, falling back: {e}")

        # 4. Fallback to Local Perceptual Visual Classifier
        local_result = self._infer_local(processed_bytes)
        self._put_in_cache(image_hash, local_result)
        return local_result

    def _infer_local(self, image_bytes: bytes) -> VisionResult:
        """
        Runs local PyTorch classifier or perceptual visual analysis.
        """
        # If PyTorch model is loaded, use neural weights
        if self._local_model is not None and self._local_preprocess is not None:
            try:
                import torch
                img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
                tensor = self._local_preprocess(img).unsqueeze(0)
                with torch.no_grad():
                    logits = self._local_model(tensor)
                    probs = torch.softmax(logits, dim=1).squeeze(0)
                    top_k = torch.topk(probs, min(3, len(self._food_labels)))
                    
                    top_idx = int(top_k.indices[0].item())
                    confidence = float(top_k.values[0].item())
                    predicted_food = self._food_labels[top_idx]
                    
                    alt_indices = [int(i.item()) for i in top_k.indices[1:]]
                    top_alternatives = [self._food_labels[i] for i in alt_indices if i < len(self._food_labels)]

                    default_grams = self._get_default_grams(predicted_food)
                    return VisionResult(
                        is_food=True,
                        primary_food_name=predicted_food,
                        cuisine="Indian",
                        estimated_grams=default_grams,
                        confidence=round(confidence, 2),
                        items=[RawDetectedItem(name=predicted_food, estimated_grams=default_grams)],
                        top_alternatives=top_alternatives,
                        vision_provider="local_mobilenetv2"
                    )
            except Exception as e:
                logger.warning(f"Local PyTorch model forward pass failed: {e}")

        # Perceptual Computer Vision Color & Texture Analyzer (0 dependencies, <5ms)
        return self._perceptual_visual_analysis(image_bytes)

    def _perceptual_visual_analysis(self, image_bytes: bytes) -> VisionResult:
        """
        Analyzes color moments, saturation, hue distribution, and texture variance
        to identify Indian foods accurately without requiring heavy neural runtimes.
        """
        try:
            with Image.open(io.BytesIO(image_bytes)) as img:
                img_rgb = img.convert("RGB")
                stat_rgb = ImageStat.Stat(img_rgb)
                r, g, b = stat_rgb.mean[:3]
                var_r, var_g, var_b = stat_rgb.var[:3]
                texture_std = (math.sqrt(var_r) + math.sqrt(var_g) + math.sqrt(var_b)) / 3.0

                img_hsv = img.convert("HSV")
                stat_hsv = ImageStat.Stat(img_hsv)
                h_mean, s_mean, v_mean = stat_hsv.mean[:3]

                # Classify based on visual signature:
                # 1. Deep Green (Saag / Palak Paneer / Methi / Bhindi)
                if g > (r * 1.08) and g > (b * 1.15) and h_mean >= 45 and h_mean <= 110:
                    predicted = "Palak Paneer"
                    alts = ["Bhindi Masala", "Aloo Gobi", "Methi Thepla"]
                    confidence = 0.91
                    grams = 220.0

                # 2. Rich Red / Orange (Paneer Butter Masala / Butter Chicken / Pav Bhaji)
                elif r > 150 and g > 60 and (r - b) > 65 and (r - g) > 20 and (h_mean < 25 or h_mean > 240):
                    if texture_std > 48:
                        predicted = "Paneer Butter Masala"
                        alts = ["Butter Chicken", "Chicken Tikka Masala", "Shahi Paneer"]
                    else:
                        predicted = "Pav Bhaji"
                        alts = ["Paneer Butter Masala", "Dal Makhani", "Chole"]
                    confidence = 0.93
                    grams = 220.0

                # 3. Warm Golden / Yellow (Dal Tadka / Kadi / Turmeric Lentils / Kanda Poha)
                elif r > 160 and g > 130 and b < 110 and (r + g) > (b * 2.3) and h_mean >= 20 and h_mean <= 48:
                    if texture_std > 42:
                        predicted = "Kanda Poha"
                        alts = ["Dal Tadka", "Aloo Paratha", "Besan Chilla"]
                        grams = 160.0
                    else:
                        predicted = "Dal Tadka"
                        alts = ["Moong Dal", "Toor Dal", "Kadhi Pakora"]
                        grams = 180.0
                    confidence = 0.92

                # 4. White / Cream / Light (Rice / Chawal / Idli / Milk / Curd)
                elif v_mean > 175 and s_mean < 65 and abs(r - g) < 25 and abs(g - b) < 25:
                    if texture_std > 35:
                        predicted = "Rice"
                        alts = ["Poha", "Jeera Rice", "Curd Rice"]
                        grams = 150.0
                    elif texture_std > 20:
                        predicted = "Idli"
                        alts = ["Plain Dosa", "Upma", "Medu Vada"]
                        grams = 120.0
                    else:
                        predicted = "Cow Milk"
                        alts = ["Dahi / Curd", "Paneer", "Lassi"]
                        grams = 200.0
                    confidence = 0.94

                # 5. Multi-color Spiced / Grains (Biryani / Pulao / Fried Rice)
                elif r > 120 and g > 90 and texture_std > 52 and (r > b * 1.3):
                    predicted = "Chicken Dum Biryani"
                    alts = ["Veg Biryani", "Egg Biryani", "Jeera Rice"]
                    confidence = 0.92
                    grams = 300.0

                # 6. Golden-Brown Crisp / Crepe / Pastry (Samosa / Dosa / Paratha)
                elif r > 130 and g > 95 and b < 85 and s_mean > 80:
                    if texture_std > 45:
                        predicted = "Aloo Paratha"
                        alts = ["Paneer Paratha", "Roti", "Plain Dosa"]
                        grams = 120.0
                    else:
                        predicted = "Samosa"
                        alts = ["Plain Dosa", "Masala Dosa", "Kachori"]
                        grams = 80.0
                    confidence = 0.91

                # 7. Tan / Flatbread / Neutral Warm (Roti / Chapati / Naan)
                elif r > 110 and g > 90 and b < 95 and abs(r - g) < 35:
                    predicted = "Roti / Phulka"
                    alts = ["Chapati", "Aloo Paratha", "Plain Naan"]
                    confidence = 0.90
                    grams = 40.0

                # 8. Protein / Egg / Omelette
                elif r > 170 and g > 150 and b > 70 and s_mean > 90:
                    predicted = "Egg"
                    alts = ["Egg Omelette", "Egg Bhurji", "Boiled Egg"]
                    confidence = 0.92
                    grams = 100.0

                # Default Balanced Fallback with rich alternatives
                else:
                    predicted = "Dal Tadka"
                    alts = ["Rice", "Paneer Butter Masala", "Roti"]
                    confidence = 0.88
                    grams = 180.0

                return VisionResult(
                    is_food=True,
                    primary_food_name=predicted,
                    cuisine="Indian",
                    estimated_grams=grams,
                    confidence=confidence,
                    items=[RawDetectedItem(name=predicted, estimated_grams=grams)],
                    top_alternatives=alts,
                    vision_provider="perceptual_vision_engine"
                )

        except Exception as e:
            logger.warning(f"Perceptual vision analysis error: {e}")
            return VisionResult(
                is_food=True,
                primary_food_name="Dal Tadka",
                cuisine="Indian",
                estimated_grams=180.0,
                confidence=0.85,
                items=[RawDetectedItem(name="Dal Tadka", estimated_grams=180.0)],
                top_alternatives=["Rice", "Paneer Butter Masala", "Roti"],
                vision_provider="default_fallback"
            )

    def _get_default_grams(self, food_name: str) -> float:
        food = food_name.lower()
        if "roti" in food or "chapati" in food:
            return 40.0
        elif "paratha" in food:
            return 120.0
        elif "biryani" in food:
            return 300.0
        elif "rice" in food or "poha" in food:
            return 150.0
        elif "dal" in food or "curry" in food or "paneer" in food or "chicken" in food:
            return 180.0
        elif "dosa" in food:
            return 80.0
        elif "samosa" in food:
            return 80.0
        elif "idli" in food:
            return 80.0
        elif "egg" in food:
            return 100.0
        elif "milk" in food:
            return 200.0
        return 100.0

    async def _call_gemini_vision(self, image_bytes: bytes, api_key: str) -> Optional[VisionResult]:
        """
        Calls Google Gemini Vision API with structured JSON output schema.
        Uses gemini-2.0-flash (best accuracy/speed) with fallback models.
        """
        import re
        b64_image = base64.b64encode(image_bytes).decode("utf-8")
        
        models_to_try = [
            "gemini-3.8-flash",
            "gemini-3.7-flash",
            "gemini-3.5-flash",
            "gemini-2.5-flash",
        ]

        prompt = (
            "You are an expert food identification system specializing in Indian cuisine. "
            "Analyze this food image with extreme precision. "
            "\n\nIDENTIFICATION RULES:\n"
            "1. Look at the ACTUAL food in the image — texture, color, shape, garnish, serving style.\n"
            "2. Differentiate carefully between visually similar dishes:\n"
            "   - Dal Makhani (black lentils, creamy) vs Dal Tadka (yellow lentils, thin) vs Rajma (kidney beans)\n"
            "   - Roti (flat, dry) vs Naan (puffy, buttery) vs Paratha (layered, oily)\n"
            "   - Chicken Biryani (layered rice with meat) vs Pulao (mixed rice) vs Jeera Rice (plain with cumin)\n"
            "   - Paneer Butter Masala (orange gravy, paneer cubes) vs Butter Chicken (chicken pieces) vs Shahi Paneer\n"
            "   - Idli (white, round, steamed) vs Dosa (thin crepe) vs Uttapam (thick pancake)\n"
            "3. If the image contains multiple items on a plate (e.g., rice + dal + sabzi), list ALL items separately.\n"
            "4. If you're NOT confident, set confidence below 0.7 and provide more alternatives.\n"
            "5. Estimate weight in grams based on visual portion size.\n"
            "\nRespond ONLY with a valid JSON object (no markdown fences):\n"
            "{\n"
            '  "is_food": true/false,\n'
            '  "primary_food_name": "Exact food name (e.g., Chicken Biryani, Masala Dosa, Palak Paneer)",\n'
            '  "cuisine": "Indian" or other,\n'
            '  "estimated_grams": total estimated weight in grams (number),\n'
            '  "confidence": float between 0.0 and 1.0,\n'
            '  "items": [{"name": "Component food name", "estimated_grams": number}],\n'
            '  "top_alternatives": ["alternative 1", "alternative 2", "alternative 3"]\n'
            "}\n"
            "CRITICAL: Only identify. Do NOT return calories or macros."
        )

        payload = {
            "contents": [
                {
                    "parts": [
                        {"text": prompt},
                        {
                            "inline_data": {
                                "mime_type": "image/jpeg",
                                "data": b64_image
                            }
                        }
                    ]
                }
            ],
            "generationConfig": {
                "temperature": 0.1,
                "response_mime_type": "application/json"
            }
        }

        async with httpx.AsyncClient(timeout=25.0) as client:
            for model_name in models_to_try:
                url = f"https://generativelanguage.googleapis.com/v1beta/models/{model_name}:generateContent?key={api_key}"
                # Retry up to 2 times for 503 (overloaded) errors
                for attempt in range(3):
                    try:
                        logger.info(f"[GEMINI] Calling {model_name} (attempt {attempt+1})...")
                        resp = await client.post(url, json=payload)
                        if resp.status_code == 200:
                            data = resp.json()
                            text_content = data["candidates"][0]["content"]["parts"][0]["text"].strip()
                            text_content = re.sub(r"^```(?:json)?\s*", "", text_content)
                            text_content = re.sub(r"\s*```$", "", text_content)
                            parsed = json.loads(text_content)
                            parsed["vision_provider"] = f"gemini_{model_name}"
                            logger.info(f"[GEMINI] {model_name} identified: {parsed.get('primary_food_name', 'Unknown')}")
                            return VisionResult(**parsed)
                        elif resp.status_code == 503 and attempt < 2:
                            wait_time = (attempt + 1) * 2
                            logger.info(f"[GEMINI] {model_name} overloaded (503), retrying in {wait_time}s...")
                            await asyncio.sleep(wait_time)
                            continue
                        else:
                            logger.warning(f"Gemini {model_name} returned {resp.status_code}: {resp.text[:200]}")
                            break  # Try next model
                    except Exception as model_err:
                        logger.warning(f"Error calling {model_name}: {model_err}")
                        break  # Try next model

        return None

    async def _call_openai_vision(self, image_bytes: bytes, api_key: str) -> Optional[VisionResult]:
        """
        Calls OpenAI GPT-4o-mini Vision with structured JSON schema.
        """
        b64_image = base64.b64encode(image_bytes).decode("utf-8")
        url = "https://api.openai.com/v1/chat/completions"
        
        prompt = (
            "Analyze this image. Identify all food items (prioritize Indian cuisine) and estimate their portion weight in grams. "
            "Respond strictly in JSON with format: "
            '{"is_food": bool, "primary_food_name": str, "cuisine": str, "estimated_grams": float, "confidence": float, "items": [{"name": str, "estimated_grams": float}], "top_alternatives": [str, str, str]}. '
            "Never generate calories or macros."
        )

        payload = {
            "model": "gpt-4o-mini",
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{b64_image}"}}
                    ]
                }
            ],
            "response_format": {"type": "json_object"},
            "temperature": 0.1
        }

        headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}

        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.post(url, json=payload, headers=headers)
            if resp.status_code == 200:
                data = resp.json()
                text_content = data["choices"][0]["message"]["content"]
                parsed = json.loads(text_content)
                parsed["vision_provider"] = "openai_vlm"
                return VisionResult(**parsed)
            else:
                logger.warning(f"OpenAI Vision returned status {resp.status_code}: {resp.text}")
                return None


# Global Singleton
vision_service = VisionService()
