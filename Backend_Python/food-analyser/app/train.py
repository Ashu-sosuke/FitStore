"""
Fine-tune MobileNetV2 on the downloaded food dataset.

Transfer learning strategy:
  1. Freeze the entire backbone (features layers)
  2. Train only the new classifier head for a few epochs (warm-up)
  3. Unfreeze the last N backbone layers and fine-tune end-to-end
  4. Save the best model based on validation accuracy

Usage:
    python -m app.train                          # default settings
    python -m app.train --epochs 30 --lr 0.001   # custom
    python -m app.train --full-finetune           # skip warm-up, train everything

Output:
    weights/food_mobilenetv2.pth          (state dict)
    weights/training_history.png          (loss/accuracy curves)
"""

from __future__ import annotations

import argparse
import copy
import json
import os
import sys
import time
from pathlib import Path

import matplotlib
matplotlib.use("Agg")  # non-interactive backend for servers
import matplotlib.pyplot as plt
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
from sklearn.metrics import classification_report, confusion_matrix
from torch.utils.data import DataLoader
from torchvision import datasets, models, transforms

from app.config import FOOD_LABELS

# ── Paths ────────────────────────────────────────────────────────────────────
DATA_DIR = Path(__file__).resolve().parent.parent / "data"
TRAIN_DIR = DATA_DIR / "train"
VAL_DIR = DATA_DIR / "val"
WEIGHTS_DIR = Path(__file__).resolve().parent.parent / "weights"
WEIGHTS_PATH = WEIGHTS_DIR / "food_mobilenetv2.pth"
HISTORY_PATH = WEIGHTS_DIR / "training_history.png"
REPORT_PATH = WEIGHTS_DIR / "classification_report.txt"

# ── Hyperparameters (defaults) ───────────────────────────────────────────────
INPUT_SIZE = 224
BATCH_SIZE = 16
NUM_WORKERS = 0  # Windows compatibility — set to 2+ on Linux

# ── Data augmentation ────────────────────────────────────────────────────────
_train_transforms = transforms.Compose([
    transforms.RandomResizedCrop(INPUT_SIZE, scale=(0.7, 1.0)),
    transforms.RandomHorizontalFlip(),
    transforms.RandomVerticalFlip(p=0.1),
    transforms.RandomRotation(20),
    transforms.ColorJitter(brightness=0.3, contrast=0.3, saturation=0.3, hue=0.1),
    transforms.RandomAffine(degrees=0, translate=(0.1, 0.1)),
    transforms.RandomPerspective(distortion_scale=0.2, p=0.3),
    transforms.GaussianBlur(kernel_size=3, sigma=(0.1, 1.0)),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
    transforms.RandomErasing(p=0.2),
])

_val_transforms = transforms.Compose([
    transforms.Resize((INPUT_SIZE, INPUT_SIZE)),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
])


def _build_model(num_classes: int) -> nn.Module:
    """Build MobileNetV2 with ImageNet backbone + custom classifier head."""
    model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.IMAGENET1K_V1)

    # Replace the classifier
    in_features = model.classifier[1].in_features  # 1280
    model.classifier = nn.Sequential(
        nn.Dropout(p=0.3),
        nn.Linear(in_features, 256),
        nn.ReLU(inplace=True),
        nn.Dropout(p=0.2),
        nn.Linear(256, num_classes),
    )
    return model


def _freeze_backbone(model: nn.Module) -> None:
    """Freeze all feature-extraction layers."""
    for param in model.features.parameters():
        param.requires_grad = False


def _unfreeze_last_n(model: nn.Module, n: int = 5) -> None:
    """Unfreeze the last N inverted-residual blocks of MobileNetV2."""
    layers = list(model.features.children())
    for layer in layers[-n:]:
        for param in layer.parameters():
            param.requires_grad = True


def _get_dataloaders(batch_size: int) -> tuple[DataLoader, DataLoader, list[str]]:
    """Create train and val dataloaders with the appropriate transforms."""
    if not TRAIN_DIR.exists() or not VAL_DIR.exists():
        print("[ERROR] Dataset not found. Run first:")
        print("        python -m app.download_data")
        sys.exit(1)

    train_dataset = datasets.ImageFolder(str(TRAIN_DIR), transform=_train_transforms)
    val_dataset = datasets.ImageFolder(str(VAL_DIR), transform=_val_transforms)

    class_names = train_dataset.classes
    print(f"\n  Classes found: {class_names}")
    print(f"  Train samples: {len(train_dataset)}")
    print(f"  Val samples:   {len(val_dataset)}")

    # Verify classes match our expected labels
    for expected in FOOD_LABELS:
        if expected not in class_names:
            print(f"  [WARNING] Expected class '{expected}' not found in dataset!")

    train_loader = DataLoader(
        train_dataset,
        batch_size=batch_size,
        shuffle=True,
        num_workers=NUM_WORKERS,
        pin_memory=True,
        drop_last=True,
    )
    val_loader = DataLoader(
        val_dataset,
        batch_size=batch_size,
        shuffle=False,
        num_workers=NUM_WORKERS,
        pin_memory=True,
    )
    return train_loader, val_loader, class_names


def _train_one_epoch(
    model: nn.Module,
    loader: DataLoader,
    criterion: nn.Module,
    optimizer: optim.Optimizer,
    device: torch.device,
) -> tuple[float, float]:
    """Train for one epoch. Returns (avg_loss, accuracy)."""
    model.train()
    running_loss = 0.0
    correct = 0
    total = 0

    for inputs, labels in loader:
        inputs, labels = inputs.to(device), labels.to(device)

        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs, labels)
        loss.backward()
        optimizer.step()

        running_loss += loss.item() * inputs.size(0)
        _, preds = torch.max(outputs, 1)
        correct += (preds == labels).sum().item()
        total += labels.size(0)

    avg_loss = running_loss / total
    accuracy = correct / total
    return avg_loss, accuracy


@torch.no_grad()
def _evaluate(
    model: nn.Module,
    loader: DataLoader,
    criterion: nn.Module,
    device: torch.device,
) -> tuple[float, float]:
    """Evaluate on validation set. Returns (avg_loss, accuracy)."""
    model.eval()
    running_loss = 0.0
    correct = 0
    total = 0

    for inputs, labels in loader:
        inputs, labels = inputs.to(device), labels.to(device)
        outputs = model(inputs)
        loss = criterion(outputs, labels)

        running_loss += loss.item() * inputs.size(0)
        _, preds = torch.max(outputs, 1)
        correct += (preds == labels).sum().item()
        total += labels.size(0)

    avg_loss = running_loss / total
    accuracy = correct / total
    return avg_loss, accuracy


@torch.no_grad()
def _generate_report(
    model: nn.Module,
    loader: DataLoader,
    class_names: list[str],
    device: torch.device,
) -> str:
    """Generate a full classification report + confusion matrix."""
    model.eval()
    all_preds = []
    all_labels = []

    for inputs, labels in loader:
        inputs = inputs.to(device)
        outputs = model(inputs)
        _, preds = torch.max(outputs, 1)
        all_preds.extend(preds.cpu().numpy())
        all_labels.extend(labels.numpy())

    report = classification_report(
        all_labels, all_preds,
        target_names=class_names,
        digits=3,
    )
    cm = confusion_matrix(all_labels, all_preds)

    text = "CLASSIFICATION REPORT\n"
    text += "=" * 60 + "\n"
    text += report + "\n\n"
    text += "CONFUSION MATRIX\n"
    text += "=" * 60 + "\n"
    text += f"Classes: {class_names}\n"
    text += str(cm) + "\n"
    return text


def _plot_history(history: dict, save_path: Path) -> None:
    """Plot training curves and save to disk."""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

    epochs_range = range(1, len(history["train_loss"]) + 1)

    # Loss
    ax1.plot(epochs_range, history["train_loss"], "b-o", label="Train Loss", markersize=3)
    ax1.plot(epochs_range, history["val_loss"], "r-o", label="Val Loss", markersize=3)
    ax1.set_title("Loss", fontsize=14, fontweight="bold")
    ax1.set_xlabel("Epoch")
    ax1.set_ylabel("Cross-Entropy Loss")
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    # Accuracy
    ax2.plot(epochs_range, history["train_acc"], "b-o", label="Train Acc", markersize=3)
    ax2.plot(epochs_range, history["val_acc"], "r-o", label="Val Acc", markersize=3)
    ax2.set_title("Accuracy", fontsize=14, fontweight="bold")
    ax2.set_xlabel("Epoch")
    ax2.set_ylabel("Accuracy")
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    # Mark the best epoch
    best_epoch = int(np.argmax(history["val_acc"]))
    best_acc = history["val_acc"][best_epoch]
    ax2.axvline(x=best_epoch + 1, color="green", linestyle="--", alpha=0.5)
    ax2.annotate(
        f"Best: {best_acc:.1%}",
        xy=(best_epoch + 1, best_acc),
        fontsize=10,
        color="green",
        fontweight="bold",
    )

    plt.tight_layout()
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"  Training curves saved to: {save_path}")


def main():
    parser = argparse.ArgumentParser(description="Fine-tune MobileNetV2 for food classification")
    parser.add_argument("--epochs", type=int, default=25, help="Total training epochs (default: 25)")
    parser.add_argument("--warmup-epochs", type=int, default=5, help="Head-only warmup epochs (default: 5)")
    parser.add_argument("--lr", type=float, default=0.001, help="Learning rate for head training (default: 0.001)")
    parser.add_argument("--ft-lr", type=float, default=0.0001, help="Learning rate for fine-tuning (default: 0.0001)")
    parser.add_argument("--batch-size", type=int, default=BATCH_SIZE, help=f"Batch size (default: {BATCH_SIZE})")
    parser.add_argument("--unfreeze-layers", type=int, default=5, help="Backbone layers to unfreeze (default: 5)")
    parser.add_argument("--full-finetune", action="store_true", help="Skip warmup, train all layers from start")
    parser.add_argument("--patience", type=int, default=7, help="Early stopping patience (default: 7)")
    args = parser.parse_args()

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"\n{'='*60}")
    print(f"  Food Classifier Training Pipeline")
    print(f"  Device: {device}")
    print(f"{'='*60}")

    # ── Data ─────────────────────────────────────────────────────────────
    train_loader, val_loader, class_names = _get_dataloaders(args.batch_size)
    num_classes = len(class_names)

    # ── Model ────────────────────────────────────────────────────────────
    model = _build_model(num_classes)
    model = model.to(device)

    criterion = nn.CrossEntropyLoss(label_smoothing=0.1)

    # ── Training history ─────────────────────────────────────────────────
    history = {"train_loss": [], "val_loss": [], "train_acc": [], "val_acc": []}
    best_val_acc = 0.0
    best_model_state = None
    patience_counter = 0
    total_start = time.time()

    # ── Phase 1: Head-only warmup ────────────────────────────────────────
    if not args.full_finetune and args.warmup_epochs > 0:
        print(f"\n--- Phase 1: Head-only warmup ({args.warmup_epochs} epochs) ---")
        _freeze_backbone(model)

        optimizer = optim.Adam(
            filter(lambda p: p.requires_grad, model.parameters()),
            lr=args.lr,
            weight_decay=1e-4,
        )
        scheduler = optim.lr_scheduler.CosineAnnealingLR(
            optimizer, T_max=args.warmup_epochs,
        )

        for epoch in range(1, args.warmup_epochs + 1):
            t_loss, t_acc = _train_one_epoch(model, train_loader, criterion, optimizer, device)
            v_loss, v_acc = _evaluate(model, val_loader, criterion, device)
            scheduler.step()

            history["train_loss"].append(t_loss)
            history["val_loss"].append(v_loss)
            history["train_acc"].append(t_acc)
            history["val_acc"].append(v_acc)

            print(
                f"  Epoch {epoch:2d}/{args.warmup_epochs} | "
                f"Train: loss={t_loss:.4f} acc={t_acc:.3f} | "
                f"Val: loss={v_loss:.4f} acc={v_acc:.3f}"
            )

            if v_acc > best_val_acc:
                best_val_acc = v_acc
                best_model_state = copy.deepcopy(model.state_dict())

    # ── Phase 2: Fine-tuning with unfrozen backbone layers ───────────────
    remaining_epochs = args.epochs - (0 if args.full_finetune else args.warmup_epochs)
    if remaining_epochs > 0:
        if args.full_finetune:
            print(f"\n--- Full fine-tune ({remaining_epochs} epochs) ---")
            # Unfreeze everything
            for param in model.parameters():
                param.requires_grad = True
        else:
            print(f"\n--- Phase 2: Fine-tuning last {args.unfreeze_layers} backbone layers ({remaining_epochs} epochs) ---")
            _unfreeze_last_n(model, n=args.unfreeze_layers)

        optimizer = optim.Adam(
            filter(lambda p: p.requires_grad, model.parameters()),
            lr=args.ft_lr,
            weight_decay=1e-4,
        )
        scheduler = optim.lr_scheduler.ReduceLROnPlateau(
            optimizer, mode="max", factor=0.5, patience=3,
        )

        for epoch in range(1, remaining_epochs + 1):
            t_loss, t_acc = _train_one_epoch(model, train_loader, criterion, optimizer, device)
            v_loss, v_acc = _evaluate(model, val_loader, criterion, device)
            scheduler.step(v_acc)

            history["train_loss"].append(t_loss)
            history["val_loss"].append(v_loss)
            history["train_acc"].append(t_acc)
            history["val_acc"].append(v_acc)

            marker = ""
            if v_acc > best_val_acc:
                best_val_acc = v_acc
                best_model_state = copy.deepcopy(model.state_dict())
                patience_counter = 0
                marker = " *BEST*"
            else:
                patience_counter += 1

            current_lr = optimizer.param_groups[0]["lr"]
            print(
                f"  Epoch {epoch:2d}/{remaining_epochs} | "
                f"Train: loss={t_loss:.4f} acc={t_acc:.3f} | "
                f"Val: loss={v_loss:.4f} acc={v_acc:.3f} | "
                f"lr={current_lr:.6f}{marker}"
            )

            if patience_counter >= args.patience:
                print(f"\n  Early stopping triggered (patience={args.patience})")
                break

    # ── Save best model ──────────────────────────────────────────────────
    elapsed = time.time() - total_start
    print(f"\n{'='*60}")
    print(f"  Training complete in {elapsed/60:.1f} minutes")
    print(f"  Best validation accuracy: {best_val_acc:.1%}")

    if best_model_state is not None:
        WEIGHTS_DIR.mkdir(parents=True, exist_ok=True)

        # Save state dict
        torch.save(best_model_state, WEIGHTS_PATH)
        print(f"  Model saved to: {WEIGHTS_PATH}")

        # Save class mapping
        class_map_path = WEIGHTS_DIR / "class_mapping.json"
        class_mapping = {i: name for i, name in enumerate(class_names)}
        with open(class_map_path, "w") as f:
            json.dump(class_mapping, f, indent=2)
        print(f"  Class mapping saved to: {class_map_path}")

        # Generate classification report
        model.load_state_dict(best_model_state)
        report = _generate_report(model, val_loader, class_names, device)
        with open(REPORT_PATH, "w") as f:
            f.write(report)
        print(f"  Classification report saved to: {REPORT_PATH}")
        print(f"\n{report}")

        # Plot training curves
        _plot_history(history, HISTORY_PATH)
    else:
        print("  [WARNING] No model improvement was observed during training.")

    print(f"\n{'='*60}")
    print("  Next steps:")
    print("  1. Update model.py STATE_DICT_PATH (already done automatically)")
    print("  2. Restart the server: python run.py")
    print(f"{'='*60}\n")


if __name__ == "__main__":
    main()
