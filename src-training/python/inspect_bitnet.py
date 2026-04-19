#!/usr/bin/env python3
"""
Inspect BitNet architecture for LoRA target modules
"""

import torch
from transformers import AutoModelForCausalLM, AutoTokenizer
import sys

MODEL_PATH = "./models/microsoft_bitnet-b1.58-2B-4T-bf16"


def inspect_model():
    print("Loading BitNet model...")

    try:
        tokenizer = AutoTokenizer.from_pretrained(MODEL_PATH)
        print(f"✓ Tokenizer loaded (vocab size: {tokenizer.vocab_size})")
    except Exception as e:
        print(f"✗ Failed to load tokenizer: {e}")
        return

    try:
        model = AutoModelForCausalLM.from_pretrained(
            MODEL_PATH,
            torch_dtype=torch.bfloat16,
            device_map="auto",
            use_safetensors=True,
        )
        print("✓ Model loaded successfully")
    except Exception as e:
        print(f"✗ Failed to load model: {e}")
        return

    print("\n=== MODEL ARCHITECTURE ===")
    print(f"Model class: {model.__class__.__name__}")
    print(f"Model dtype: {model.dtype}")
    print(f"Device: {model.device}")

    # Get all named modules
    print("\n=== NAMED MODULES (potential LoRA targets) ===")

    linear_layers = []
    bitlinear_layers = []
    attention_layers = []

    for name, module in model.named_modules():
        module_type = module.__class__.__name__

        if "Linear" in module_type:
            linear_layers.append((name, module_type))
            if "BitLinear" in module_type:
                bitlinear_layers.append((name, module_type))

        if "Attention" in module_type:
            attention_layers.append((name, module_type))

    print(f"\nTotal linear layers: {len(linear_layers)}")
    print(f"BitLinear layers: {len(bitlinear_layers)}")
    print(f"Attention layers: {len(attention_layers)}")

    if linear_layers:
        print("\nFirst 20 linear layers:")
        for name, typ in linear_layers[:20]:
            print(f"  {name} ({typ})")

    if bitlinear_layers:
        print("\nAll BitLinear layers:")
        for name, typ in bitlinear_layers:
            print(f"  {name} ({typ})")

    # Look for typical GPT-2 style module names
    print("\n=== GPT-2 STYLE MODULE NAMES ===")
    gpt2_patterns = ["c_attn", "c_proj", "q_proj", "k_proj", "v_proj", "out_proj"]
    for pattern in gpt2_patterns:
        matches = [(name, typ) for name, typ in linear_layers if pattern in name]
        if matches:
            print(f"\nMatches for '{pattern}':")
            for name, typ in matches[:5]:
                print(f"  {name} ({typ})")
            if len(matches) > 5:
                print(f"  ... and {len(matches) - 5} more")

    # Suggest LoRA target modules
    print("\n=== SUGGESTED LoRA TARGET MODULES ===")

    # Check for common patterns
    candidate_targets = []

    # Pattern 1: c_attn, c_proj (GPT-2 style)
    c_attn = [name for name, typ in linear_layers if "c_attn" in name]
    c_proj = [name for name, typ in linear_layers if "c_proj" in name]

    if c_attn and c_proj:
        print("✓ GPT-2 style modules found (c_attn, c_proj)")
        candidate_targets.extend(c_attn[:1] + c_proj[:1])

    # Pattern 2: q_proj, v_proj (Llama style)
    q_proj = [name for name, typ in linear_layers if "q_proj" in name]
    v_proj = [name for name, typ in linear_layers if "v_proj" in name]

    if q_proj and v_proj:
        print("✓ Llama style modules found (q_proj, v_proj)")
        candidate_targets.extend(q_proj[:1] + v_proj[:1])

    # Pattern 3: BitLinear layers
    if bitlinear_layers:
        print("✓ BitLinear layers found")
        # Take first BitLinear from attention and MLP if possible
        attention_bitlinear = [
            name for name, typ in bitlinear_layers if "attention" in name.lower()
        ]
        mlp_bitlinear = [
            name for name, typ in bitlinear_layers if "mlp" in name.lower()
        ]

        if attention_bitlinear:
            candidate_targets.append(attention_bitlinear[0])
        if mlp_bitlinear:
            candidate_targets.append(mlp_bitlinear[0])

    if not candidate_targets and linear_layers:
        # Fallback: take first few linear layers
        candidate_targets = [name for name, typ in linear_layers[:4]]

    if candidate_targets:
        print("\nSuggested target_modules for LoRA:")
        for target in candidate_targets[:4]:  # Limit to 4
            print(f'  "{target}",')
    else:
        print("\n⚠ No clear target modules found")

    print("\n=== TEST FORWARD PASS ===")
    try:
        # Simple test
        input_ids = torch.tensor([[1, 2, 3]]).to(model.device)
        with torch.no_grad():
            outputs = model(input_ids)
            print(f"✓ Forward pass successful")
            print(f"  Output shape: {outputs.logits.shape}")
    except Exception as e:
        print(f"✗ Forward pass failed: {e}")


if __name__ == "__main__":
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     INSPECT BITNET ARCHITECTURE                               ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    inspect_model()
