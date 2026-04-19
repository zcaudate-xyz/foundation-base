#!/usr/bin/env python3
"""
Test training with BitNet (small subset)
"""

import os
import sys
import torch
import json
from pathlib import Path

# Add parent directory to path
sys.path.append(".")

from train import (
    load_training_data,
    prepare_dataset,
    tokenize_dataset,
    setup_model,
    train_model,
)


def load_test_subset(data_dir: str, max_samples: int = 100):
    """Load only test subset"""
    subset_file = Path(data_dir) / "test_subset_100.jsonl"
    examples = []

    print(f"Loading test subset from {subset_file}")

    if not subset_file.exists():
        print(f"✗ File not found: {subset_file}")
        return []

    with open(subset_file, "r") as f:
        for i, line in enumerate(f):
            if i >= max_samples:
                break
            try:
                data = json.loads(line.strip())
                # Ensure required fields
                if "xtalk" in data and ("python" in data or "js" in data):
                    examples.append(data)
            except json.JSONDecodeError:
                continue

    print(f"✓ Loaded {len(examples)} examples")
    return examples


def main():
    MODEL_PATH = "./models/microsoft_bitnet-b1.58-2B-4T-bf16"
    DATA_DIR = "../../training"  # Contains test_subset_100.jsonl
    OUTPUT_DIR = "/tmp/xtalk_bitnet_test"
    MAX_SAMPLES = 10  # Use even smaller subset for quick test
    TEST_EPOCHS = 1
    TEST_BATCH_SIZE = 1  # Small batch for 12GB GPU

    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     BITNET TRAINING TEST (Small Subset)                       ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    # Check for GPU
    if torch.cuda.is_available():
        print(f"\n✓ GPU available: {torch.cuda.get_device_name(0)}")
        print(
            f"  Memory: {torch.cuda.get_device_properties(0).total_memory / 1e9:.1f} GB"
        )
    else:
        print("\n⚠ No GPU detected - test will be slow!")

    # Load test data
    examples = load_test_subset(DATA_DIR, MAX_SAMPLES)

    if len(examples) == 0:
        print("\n✗ No test data found!")
        print(f"Please create test_subset_100.jsonl in {DATA_DIR}")
        return

    # Setup model
    print(f"\nLoading model: {MODEL_PATH}")
    model, tokenizer, is_t5, is_bitnet = setup_model(MODEL_PATH)

    # Prepare dataset
    print("\nPreparing dataset...")
    dataset = prepare_dataset(examples, is_t5)

    # Tokenize
    print("Tokenizing...")
    tokenized_dataset = tokenize_dataset(dataset, tokenizer, is_t5, max_length=32)

    # Modify training for quick test
    print("\n=== Starting Test Training ===")
    print(f"  Samples: {len(examples)}")
    print(f"  Epochs: {TEST_EPOCHS}")
    print(f"  Batch size: {TEST_BATCH_SIZE}")
    print(f"  Output: {OUTPUT_DIR}")

    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Use bfloat16 for BitNet
    use_bf16 = torch.cuda.is_available() and is_bitnet
    use_fp16 = torch.cuda.is_available() and not is_bitnet

    from transformers.training_args import TrainingArguments
    from transformers.trainer import Trainer
    from transformers.data.data_collator import DataCollatorForLanguageModeling
    from peft import get_peft_model

    training_args = TrainingArguments(
        output_dir=OUTPUT_DIR,
        num_train_epochs=TEST_EPOCHS,
        per_device_train_batch_size=TEST_BATCH_SIZE,
        gradient_accumulation_steps=1,
        learning_rate=5e-4,
        warmup_steps=10,
        logging_steps=5,
        save_steps=50,
        save_total_limit=1,
        fp16=use_fp16,
        bf16=use_bf16,
        optim="adamw_torch",
        lr_scheduler_type="cosine",
        report_to="none",
        max_steps=20,  # Limit steps for quick test
    )

    data_collator = DataCollatorForLanguageModeling(
        tokenizer=tokenizer,
        mlm=False,
    )

    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=tokenized_dataset,
        data_collator=data_collator,
    )

    print("\nStarting training (max 20 steps)...")
    trainer.train()

    # Save final model
    final_dir = os.path.join(OUTPUT_DIR, "final")
    model.save_pretrained(final_dir)
    tokenizer.save_pretrained(final_dir)

    print(f"\n✓ Test training complete!")
    print(f"  Model saved to: {final_dir}")

    # Test inference
    print("\n=== Quick Inference Test ===")
    test_input = "(if (> x 0) (return 1) (return 0))"
    print(f"  Input: {test_input}")

    # Use model for generation (simple)
    input_ids = tokenizer.encode(test_input, return_tensors="pt").to(model.device)
    with torch.no_grad():
        outputs = model.generate(
            input_ids,
            max_new_tokens=50,
            do_sample=True,
            temperature=0.7,
        )

    generated = tokenizer.decode(outputs[0], skip_special_tokens=True)
    print(f"  Generated: {generated[:200]}...")


if __name__ == "__main__":
    main()
