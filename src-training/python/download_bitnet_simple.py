#!/usr/bin/env python3
"""
Download BitNet b1.58-2B-4T-bf16 with progress tracking
"""

import os
from pathlib import Path
from huggingface_hub import snapshot_download, hf_hub_download
import torch
from transformers import AutoTokenizer

MODEL_NAME = "microsoft/bitnet-b1.58-2B-4T-bf16"
LOCAL_DIR = Path(f"./models/{MODEL_NAME.replace('/', '_')}")


def download_with_progress():
    if LOCAL_DIR.exists():
        # Check for model files
        safetensors = list(LOCAL_DIR.glob("*.safetensors"))
        if safetensors:
            print(f"✓ Model already exists at {LOCAL_DIR}")
            return LOCAL_DIR

    print(f"Downloading {MODEL_NAME}...")
    print("This may take a while (model size ~4GB in bfloat16)...")

    try:
        # Download entire repository snapshot
        snapshot_path = snapshot_download(
            repo_id=MODEL_NAME,
            local_dir=LOCAL_DIR,
            local_dir_use_symlinks=False,
            resume_download=True,
            allow_patterns=["*.safetensors", "*.json", "*.txt", "*.py", "tokenizer*"],
            ignore_patterns=["*.msgpack", "*.h5", "*.ot", "*.tflite"],
        )

        print(f"\n✓ Download complete!")
        print(f"  Model saved to: {LOCAL_DIR}")

        # Verify we can load tokenizer
        print("  Testing tokenizer...")
        tokenizer = AutoTokenizer.from_pretrained(LOCAL_DIR)
        print(f"  Tokenizer vocab size: {tokenizer.vocab_size}")

        return LOCAL_DIR

    except Exception as e:
        print(f"\n✗ Download failed: {e}")
        import traceback

        traceback.print_exc()
        return None


if __name__ == "__main__":
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     DOWNLOADING BITNET b1.58-2B-4T (bfloat16)                ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    Path("./models").mkdir(exist_ok=True)

    path = download_with_progress()
    if path:
        print(f"\n✓ Success! Model ready for fine-tuning.")
        print(f"\nNext steps:")
        print(f"  1. Update train.py to handle BitNet (torch.bfloat16)")
        print(f"  2. Determine LoRA target modules for BitLinear layers")
        print(f"  3. Run training with: python train.py --model {path}")
    else:
        print("\n✗ Download failed. Check your internet connection and try again.")
