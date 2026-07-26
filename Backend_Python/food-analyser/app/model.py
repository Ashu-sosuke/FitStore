"""
Food classification model using a fine-tuned MobileNetV2 (PyTorch).

The model is loaded once at startup and kept in memory for low-latency
inference.  Preprocessing follows the ImageNet convention
(resize -> normalise) and runs entirely on the CPU tensor pipeline.

Architecture
------------
  Base:   MobileNetV2 pretrained on ImageNet-1K
  Head:   Dropout(0.3) -> Linear(1280, 256) -> ReLU -> Dropout(0.2) -> Linear(256, 6)
  Input:  224 x 224 RGB, ImageNet-normalised
  Output: softmax probability vector over FOOD_LABELS
"""

from __future__ import annotations

import io
import json
import logging
from functools import lru_cache
from pathlib import Path

import numpy as np
import torch
import torch.nn as nn
from PIL import Image
from torchvision import models, transforms

from app.config import CONFIDENCE_THRESHOLD, FOOD_LABELS

logger = logging.getLogger(__name__)

# -- Constants ----------------------------------------------------------------
_NUM_CLASSES: int = len(FOOD_LABELS)
_INPUT_SIZE: int = 224

# Weights directory (auto-detected relative to this file)
_WEIGHTS_DIR = Path(__file__).resolve().parent.parent / "weights"
_WEIGHTS_PATH = _WEIGHTS_DIR / "food_mobilenetv2.pth"
_CLASS_MAP_PATH = _WEIGHTS_DIR / "class_mapping.json"

# -- Preprocessing pipeline (ImageNet normalisation) --------------------------
_preprocess = transforms.Compose(
    [
        transforms.Resize((_INPUT_SIZE, _INPUT_SIZE)),
        transforms.ToTensor(),
        transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225],
        ),
    ]
)


def _build_model(num_classes: int) -> nn.Module:
    """Build MobileNetV2 with the same architecture used during training."""
    model = models.mobilenet_v2(weights=None)  # no pretrained weights needed

    in_features = model.classifier[1].in_features  # 1280
    model.classifier = nn.Sequential(
        nn.Dropout(p=0.3),
        nn.Linear(in_features, 256),
        nn.ReLU(inplace=True),
        nn.Dropout(p=0.2),
        nn.Linear(256, num_classes),
    )
    return model


def _build_model_pretrained(num_classes: int) -> nn.Module:
    """Build MobileNetV2 with ImageNet backbone (fallback when no fine-tuned weights)."""
    model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.IMAGENET1K_V1)

    in_features = model.classifier[1].in_features  # 1280
    model.classifier = nn.Sequential(
        nn.Dropout(p=0.3),
        nn.Linear(in_features, 256),
        nn.ReLU(inplace=True),
        nn.Dropout(p=0.2),
        nn.Linear(256, num_classes),
    )
    return model


# -- Model loader (singleton) -------------------------------------------------
@lru_cache(maxsize=1)
def _load_model() -> tuple[nn.Module, list[str]]:
    """
    Load the fine-tuned model and class mapping.

    Returns (model, class_names) where class_names is the ordered list
    of labels that the model was trained on.
    """
    # Try to load class mapping from training
    class_names = FOOD_LABELS  # fallback
    if _CLASS_MAP_PATH.exists():
        with open(_CLASS_MAP_PATH) as f:
            mapping = json.load(f)
        # mapping is {index_str: label, ...}
        class_names = [mapping[str(i)] for i in range(len(mapping))]
        logger.info("Loaded class mapping: %s", class_names)

    # Try to load fine-tuned weights
    if _WEIGHTS_PATH.exists():
        logger.info("Loading fine-tuned model from %s ...", _WEIGHTS_PATH)
        model = _build_model(len(class_names))
        state = torch.load(str(_WEIGHTS_PATH), map_location="cpu", weights_only=True)
        model.load_state_dict(state)
        logger.info("[OK] Fine-tuned food model loaded successfully")
    else:
        logger.warning(
            "No fine-tuned weights found at %s", _WEIGHTS_PATH,
        )
        logger.warning(
            "Using ImageNet backbone + random head. Run 'python -m app.train' first!",
        )
        model = _build_model_pretrained(len(class_names))

    model.eval()
    return model, class_names


# -- Public API ---------------------------------------------------------------

def predict_food(image_bytes: bytes) -> dict:
    """
    Classify raw JPEG/PNG bytes into one of the supported food labels.

    Parameters
    ----------
    image_bytes : bytes
        Raw image file content (JPEG or PNG).

    Returns
    -------
    dict
        {
            "food_label": str,       # predicted class name
            "confidence": float,     # softmax probability (0-1)
            "all_scores": dict,      # label -> probability for every class
        }
        If the top confidence is below CONFIDENCE_THRESHOLD the food_label
        is set to "Unknown".

    Performance
    -----------
      Image decode + transforms:  ~2-5 ms (CPU)
      MobileNetV2 forward pass:   ~15-30 ms (CPU)
      Total wall time:             <50 ms typical
    """
    model, class_names = _load_model()

    # -- Decode & preprocess ---------------------------------------------------
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    tensor = _preprocess(image).unsqueeze(0)  # (1, 3, 224, 224)

    # -- Inference (no grad for speed) -----------------------------------------
    with torch.no_grad():
        logits = model(tensor)                        # (1, N)
        probabilities = torch.softmax(logits, dim=1)  # (1, N)

    probs_np: np.ndarray = probabilities.squeeze().numpy()
    top_idx: int = int(np.argmax(probs_np))
    top_conf: float = float(probs_np[top_idx])

    # Build score map
    all_scores = {
        label: round(float(probs_np[i]), 4)
        for i, label in enumerate(class_names)
    }

    food_label = class_names[top_idx] if top_conf >= CONFIDENCE_THRESHOLD else "Unknown"

    logger.info(
        "Prediction: %s (%.2f%%) | threshold=%.2f",
        food_label,
        top_conf * 100,
        CONFIDENCE_THRESHOLD,
    )

    return {
        "food_label": food_label,
        "confidence": round(top_conf, 4),
        "all_scores": all_scores,
    }
