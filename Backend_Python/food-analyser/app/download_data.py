"""
Download training images for the 6 food categories using Bing Image Search.

Usage:
    python -m app.download_data                 # default 80 images per class
    python -m app.download_data --per-class 120  # custom count

Images are saved into:
    data/
    +-- train/  (80% split)
    |   +-- Egg/
    |   +-- Chicken/
    |   +-- ...
    +-- val/    (20% split)
        +-- Egg/
        +-- Chicken/
        +-- ...
"""

from __future__ import annotations

import argparse
import os
import random
import shutil
import sys
from pathlib import Path

from icrawler.builtin import BingImageCrawler
from PIL import Image

# ── Configuration ────────────────────────────────────────────────────────────
BASE_DIR = Path(__file__).resolve().parent.parent / "data"
RAW_DIR = BASE_DIR / "raw"
TRAIN_DIR = BASE_DIR / "train"
VAL_DIR = BASE_DIR / "val"

# Search queries per class – multiple queries per food give more variety
SEARCH_QUERIES: dict[str, list[str]] = {
    "Egg": [
        "fried egg on plate",
        "boiled egg food",
        "scrambled eggs dish",
        "raw egg white yolk",
    ],
    "Chicken": [
        "grilled chicken breast plate",
        "roasted chicken food",
        "chicken meat cooked",
        "baked chicken dinner",
    ],
    "Milk": [
        "glass of milk white",
        "milk bottle dairy",
        "pouring milk glass",
        "fresh milk cup",
    ],
    "Broccoli": [
        "fresh broccoli vegetable",
        "steamed broccoli plate",
        "broccoli florets green",
        "raw broccoli food",
    ],
    "Avocado": [
        "sliced avocado half",
        "avocado toast food",
        "fresh avocado fruit",
        "cut avocado green",
    ],
    "Salmon": [
        "grilled salmon fillet",
        "salmon fish cooked",
        "raw salmon sushi",
        "baked salmon plate",
    ],
}

TRAIN_SPLIT = 0.80  # 80% train, 20% validation


def _download_images(label: str, queries: list[str], total: int) -> None:
    """Download images for a single food class."""
    out_dir = RAW_DIR / label
    out_dir.mkdir(parents=True, exist_ok=True)

    per_query = max(total // len(queries), 5)
    print(f"\n{'='*60}")
    print(f"  Downloading: {label}  ({per_query} x {len(queries)} queries)")
    print(f"{'='*60}")

    for query in queries:
        crawler = BingImageCrawler(
            storage={"root_dir": str(out_dir)},
            downloader_threads=4,
            feeder_threads=1,
            parser_threads=1,
        )
        crawler.crawl(
            keyword=query,
            max_num=per_query,
            min_size=(100, 100),
            file_idx_offset="auto",
        )


def _validate_images(folder: Path) -> int:
    """Remove corrupt / non-image files. Returns count of valid images."""
    removed = 0
    for fpath in folder.iterdir():
        if fpath.is_dir():
            continue
        try:
            with Image.open(fpath) as img:
                img.verify()
        except Exception:
            fpath.unlink()
            removed += 1

    valid = len(list(folder.glob("*")))
    if removed:
        print(f"  Removed {removed} corrupt files from {folder.name}")
    return valid


def _split_dataset() -> None:
    """Split raw images into train / val folders."""
    for split_dir in (TRAIN_DIR, VAL_DIR):
        if split_dir.exists():
            shutil.rmtree(split_dir)

    for label_dir in sorted(RAW_DIR.iterdir()):
        if not label_dir.is_dir():
            continue

        label = label_dir.name
        images = sorted(label_dir.glob("*"))
        random.shuffle(images)

        split_idx = int(len(images) * TRAIN_SPLIT)
        train_imgs = images[:split_idx]
        val_imgs = images[split_idx:]

        # Copy to train/
        train_out = TRAIN_DIR / label
        train_out.mkdir(parents=True, exist_ok=True)
        for img in train_imgs:
            shutil.copy2(img, train_out / img.name)

        # Copy to val/
        val_out = VAL_DIR / label
        val_out.mkdir(parents=True, exist_ok=True)
        for img in val_imgs:
            shutil.copy2(img, val_out / img.name)

        print(f"  {label:12s} -> train: {len(train_imgs):3d}  |  val: {len(val_imgs):3d}")


def main():
    parser = argparse.ArgumentParser(description="Download food training images")
    parser.add_argument(
        "--per-class",
        type=int,
        default=80,
        help="Number of images to download per food class (default: 80)",
    )
    args = parser.parse_args()

    print("\n[1/3] Downloading images from Bing...")
    for label, queries in SEARCH_QUERIES.items():
        _download_images(label, queries, args.per_class)

    print("\n[2/3] Validating downloaded images...")
    total_valid = 0
    for label_dir in sorted(RAW_DIR.iterdir()):
        if label_dir.is_dir():
            count = _validate_images(label_dir)
            total_valid += count
            print(f"  {label_dir.name:12s}: {count} valid images")
    print(f"  Total valid: {total_valid}")

    print("\n[3/3] Splitting into train/val...")
    _split_dataset()

    print("\n[DONE] Dataset ready!")
    print(f"  Train: {TRAIN_DIR}")
    print(f"  Val:   {VAL_DIR}")


if __name__ == "__main__":
    main()
