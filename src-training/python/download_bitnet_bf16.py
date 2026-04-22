#!/usr/bin/env python3
"""
Download BitNet b1.58-2B-4T-bf16 variant (for fine-tuning)
"""

import os
from pathlib import Path
from transformers import AutoModelForCausalLM, AutoTokenizer
import torch

MODEL_NAME = "microsoft/bitnet-b1.58-2B-4T-bf16"


def download_bitnet_bf16():
    local_path = Path(f"./models/{MODEL_NAME.replace('/', '_')}")

    # Check if model files exist (not just directory)
    if local_path.exists():
        # Check for actual model files
        model_files = list(local_path.glob("*.safetensors")) + list(
            local_path.glob("*.bin")
        )
        if model_files:
            print(f"✓ {MODEL_NAME} already cached at {local_path}")
            return local_path
        else:
            print(f"⚠ Directory exists but no model files found. Re-downloading...")
            # Remove empty directory
            import shutil

            shutil.rmtree(local_path)

    print(f"Downloading {MODEL_NAME}...")
    local_path.mkdir(parents=True, exist_ok=True)

    try:
        # Load model - use bfloat16 as recommended
        print("  Loading model (this may take a few minutes)...")
        model = AutoModelForCausalLM.from_pretrained(
            MODEL_NAME,
            torch_dtype=torch.bfloat16,
            device_map="auto" if torch.cuda.is_available() else "cpu",
            use_safetensors=True,
        )

        tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

        # Save locally
        print("  Saving model to local cache...")
        model.save_pretrained(local_path)
        tokenizer.save_pretrained(local_path)

        print(f"  Saved to {local_path}")
        return local_path

    except Exception as e:
        print(f"  Error downloading {MODEL_NAME}: {e}")
        import traceback

        traceback.print_exc()
        # Clean up empty directory
        if local_path.exists():
            import shutil

            shutil.rmtree(local_path)
        return None


if __name__ == "__main__":
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     DOWNLOADING BITNET b1.58-2B-4T (bfloat16)                ║")
    print("║     Use this variant for fine-tuning                         ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    # Create models directory
    Path("./models").mkdir(exist_ok=True)

    path = download_bitnet_bf16()
    if path:
        print(f"\n✓ Download complete! Model saved to: {path}")
        print(f"\nModel info:")
        print(f"  - Name: {MODEL_NAME}")
        print(f"  - Dtype: bfloat16 (for fine-tuning)")
        print(f"  - Path: {path}")
    else:
        print("\n✗ Download failed")
