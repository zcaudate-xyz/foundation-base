#!/usr/bin/env python3
"""
Test BitNet model loading with LoRA
"""

import torch
import sys

sys.path.append(".")

# Import from train.py
from train import setup_model

MODEL_PATH = "./models/microsoft_bitnet-b1.58-2B-4T-bf16"


def test_bitnet_lora():
    print("Testing BitNet with LoRA...")

    try:
        model, tokenizer, is_t5, is_bitnet = setup_model(MODEL_PATH)

        print(f"\n✓ Model loaded successfully")
        print(f"  Model type: BitNetForCausalLM")
        print(f"  Dtype: {model.dtype}")
        print(f"  Device: {model.device}")
        print(f"  is_t5: {is_t5}")
        print(f"  is_bitnet: {is_bitnet}")

        # Test tokenizer
        test_text = "(if (> x 0) (return 1) (return 0))"
        tokens = tokenizer.encode(test_text)
        print(f"\nTokenizer test:")
        print(f"  Input: {test_text}")
        print(f"  Token IDs: {tokens}")
        print(f"  Decoded: {tokenizer.decode(tokens)}")

        # Test forward pass
        print("\nTesting forward pass...")
        input_ids = torch.tensor([tokens]).to(model.device)
        with torch.no_grad():
            outputs = model(input_ids)
            print(f"  Output shape: {outputs.logits.shape}")
            print(f"  Output dtype: {outputs.logits.dtype}")

        # Check trainable parameters
        print("\nTrainable parameters:")
        trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
        total_params = sum(p.numel() for p in model.parameters())
        print(f"  Trainable: {trainable_params:,}")
        print(f"  Total: {total_params:,}")
        print(f"  Percentage: {100 * trainable_params / total_params:.2f}%")

        print("\n✓ All tests passed!")
        return True

    except Exception as e:
        print(f"\n✗ Test failed: {e}")
        import traceback

        traceback.print_exc()
        return False


if __name__ == "__main__":
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     TEST BITNET WITH LoRA                                     ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    success = test_bitnet_lora()
    if success:
        print("\n✓ BitNet is ready for training!")
        print(
            f"\nNext: python train.py --model {MODEL_PATH} --data-dir ../../training --output /tmp/xtalk_bitnet"
        )
    else:
        print("\n✗ BitNet setup failed")
