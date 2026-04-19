#!/usr/bin/env python3
"""
Download and cache models locally for offline training
"""

import os
from pathlib import Path
from transformers import AutoModelForCausalLM, AutoTokenizer, T5ForConditionalGeneration
import torch

MODELS_TO_CACHE = [
    # Small models for pattern learning
    ("gpt2", AutoModelForCausalLM),
    ("google/t5-small-lm-adapt", T5ForConditionalGeneration),
    # ~0.6B range
    ("Qwen/Qwen2-0.5B", AutoModelForCausalLM),
    # 1-bit models
    ("microsoft/bitnet-b1.58-2B-4T", AutoModelForCausalLM),
    # Larger but efficient
    ("TinyLlama/TinyLlama-1.1B-Chat-v1.0", AutoModelForCausalLM),
]


def download_model(model_name, model_class):
    """Download and save a model locally"""
    local_path = Path(f"./models/{model_name.replace('/', '_')}")

    if local_path.exists():
        print(f"✓ {model_name} already cached at {local_path}")
        return local_path

    print(f"Downloading {model_name}...")
    local_path.mkdir(parents=True, exist_ok=True)

    try:
        # Load model (but don't keep in memory)
        if model_class == T5ForConditionalGeneration:
            model = model_class.from_pretrained(
                model_name,
                torch_dtype=torch.float16,
                use_safetensors=True,
            )
        else:
            model = model_class.from_pretrained(
                model_name,
                torch_dtype=torch.float16,
                device_map="auto" if torch.cuda.is_available() else "cpu",
                use_safetensors=True,
            )

        tokenizer = AutoTokenizer.from_pretrained(model_name)

        # Save locally
        model.save_pretrained(local_path)
        tokenizer.save_pretrained(local_path)

        print(f"  Saved to {local_path}")
        return local_path

    except Exception as e:
        print(f"  Error downloading {model_name}: {e}")
        return None


def main():
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     DOWNLOADING MODELS FOR OFFLINE USE                        ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    # Create models directory
    Path("./models").mkdir(exist_ok=True)

    downloaded = {}

    for model_name, model_class in MODELS_TO_CACHE:
        path = download_model(model_name, model_class)
        if path:
            downloaded[model_name] = str(path)

    print("\n✓ Download complete!")
    print("\nLocal paths:")
    for name, path in downloaded.items():
        print(f"  {name}: {path}")

    # Write mapping file
    with open("./models/model_paths.txt", "w") as f:
        for name, path in downloaded.items():
            f.write(f"{name}={path}\n")

    print("\nModel paths saved to ./models/model_paths.txt")


if __name__ == "__main__":
    main()
