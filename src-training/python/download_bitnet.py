#!/usr/bin/env python3
"""
Download only BitNet b1.58 model
"""

import os
from pathlib import Path
from transformers import AutoModelForCausalLM, AutoTokenizer
import torch

MODEL_NAME = "microsoft/bitnet-b1.58-2B-4T"


def download_bitnet():
    local_path = Path(f"./models/{MODEL_NAME.replace('/', '_')}")

    if local_path.exists():
        print(f"✓ {MODEL_NAME} already cached at {local_path}")
        return local_path

    print(f"Downloading {MODEL_NAME}...")
    local_path.mkdir(parents=True, exist_ok=True)

    try:
        # Load model
        model = AutoModelForCausalLM.from_pretrained(
            MODEL_NAME,
            torch_dtype=torch.float16,
            device_map="auto" if torch.cuda.is_available() else "cpu",
            use_safetensors=True,
        )

        tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

        # Save locally
        model.save_pretrained(local_path)
        tokenizer.save_pretrained(local_path)

        print(f"  Saved to {local_path}")
        return local_path

    except Exception as e:
        print(f"  Error downloading {MODEL_NAME}: {e}")
        import traceback

        traceback.print_exc()
        return None


if __name__ == "__main__":
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     DOWNLOADING BITNET b1.58-2B                              ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    # Create models directory
    Path("./models").mkdir(exist_ok=True)

    path = download_bitnet()
    if path:
        print(f"\n✓ Download complete! Model saved to: {path}")
    else:
        print("\n✗ Download failed")
