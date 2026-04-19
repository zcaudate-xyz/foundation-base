#!/usr/bin/env python3
"""
Debug BitNet training issue
"""

import torch
import sys

sys.path.append(".")

from train import setup_model
from transformers.data.data_collator import DataCollatorForLanguageModeling

MODEL_PATH = "./models/microsoft_bitnet-b1.58-2B-4T-bf16"


def debug_training_step():
    print("Loading model...")
    model, tokenizer, is_t5, is_bitnet = setup_model(MODEL_PATH)
    model.train()  # Set to training mode

    print(f"Model dtype: {model.dtype}")
    print(f"Model device: {model.device}")

    # Create a simple example
    test_text = "(if (> x 0) (return 1) (return 0))"
    print(f"Test text: {test_text}")

    # Tokenize
    encoding = tokenizer(
        test_text,
        truncation=True,
        padding="max_length",
        max_length=64,
        return_tensors="pt",
    )

    input_ids = encoding["input_ids"].to(model.device)
    attention_mask = encoding["attention_mask"].to(model.device)

    print(f"Input IDs shape: {input_ids.shape}")
    print(f"Input IDs dtype: {input_ids.dtype}")

    # Create labels (shifted input_ids for causal LM)
    labels = input_ids.clone()

    # Test forward with labels
    print("\nTesting forward with labels...")
    try:
        outputs = model(
            input_ids=input_ids, attention_mask=attention_mask, labels=labels
        )
        print(f"Success! Loss: {outputs.loss}")
        print(f"Loss dtype: {outputs.loss.dtype}")
    except Exception as e:
        print(f"Error with labels: {e}")
        import traceback

        traceback.print_exc()

    # Test forward without labels
    print("\nTesting forward without labels...")
    try:
        outputs = model(input_ids=input_ids, attention_mask=attention_mask)
        print(f"Success! Logits shape: {outputs.logits.shape}")
    except Exception as e:
        print(f"Error without labels: {e}")
        import traceback

        traceback.print_exc()

    # Test data collator
    print("\nTesting data collator...")
    data_collator = DataCollatorForLanguageModeling(
        tokenizer=tokenizer,
        mlm=False,
    )

    batch = data_collator([{"input_ids": input_ids[0]}, {"input_ids": input_ids[0]}])
    print(f"Batch keys: {batch.keys()}")
    print(f"Input IDs shape: {batch['input_ids'].shape}")
    print(f"Labels shape: {batch['labels'].shape}")

    # Test model with collated batch
    print("\nTesting model with collated batch...")
    try:
        batch = {k: v.to(model.device) for k, v in batch.items()}
        outputs = model(**batch)
        print(f"Success! Loss: {outputs.loss}")
    except Exception as e:
        print(f"Error with collated batch: {e}")
        import traceback

        traceback.print_exc()

    # Check gradient computation
    print("\nTesting gradient computation...")
    model.zero_grad()
    try:
        outputs = model(input_ids=input_ids, labels=labels)
        loss = outputs.loss
        print(f"Loss: {loss}")
        loss.backward()
        print(f"Gradient computation successful")

        # Check gradients
        for name, param in model.named_parameters():
            if param.requires_grad and param.grad is not None:
                print(f"  {name}: grad norm = {param.grad.norm().item():.6f}")
                break
    except Exception as e:
        print(f"Error in backward: {e}")
        import traceback

        traceback.print_exc()


if __name__ == "__main__":
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     DEBUG BITNET TRAINING                                     ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    debug_training_step()
