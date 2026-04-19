#!/usr/bin/env python3
"""
Inference script for trained std.lang model

Test the trained model on xtalk translation tasks.

Usage:
    python inference.py --model model_output/final --input "(if (> x 0) (return 1) (return 0))"
"""

import argparse
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer


def generate_translation(
    model, tokenizer, xtalk_code: str, max_length: int = 512
) -> str:
    """Generate translation using the trained model"""

    prompt = f"""### Instruction:
Compile the following xtalk DSL code to both Python and JavaScript.

### Input:
{xtalk_code}

### Response:"""

    # Tokenize
    inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

    # Generate
    with torch.no_grad():
        outputs = model.generate(
            **inputs,
            max_new_tokens=max_length,
            do_sample=True,
            temperature=0.7,
            top_p=0.9,
            pad_token_id=tokenizer.eos_token_id,
        )

    # Decode
    generated_text = tokenizer.decode(outputs[0], skip_special_tokens=True)

    # Extract just the response part
    if "### Response:" in generated_text:
        response = generated_text.split("### Response:")[1].strip()
    else:
        response = generated_text[len(prompt) :].strip()

    return response


def interactive_mode(model, tokenizer):
    """Run interactive mode"""
    print("\n=== Interactive Mode ===")
    print("Enter xtalk code to translate (type 'quit' to exit):\n")

    while True:
        try:
            user_input = input("xtalk> ").strip()

            if user_input.lower() in ["quit", "exit", "q"]:
                break

            if not user_input:
                continue

            print("\nGenerating...")
            result = generate_translation(model, tokenizer, user_input)
            print(f"\n{result}\n")

        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"Error: {e}")

    print("\nGoodbye!")


def main():
    parser = argparse.ArgumentParser(description="Test trained std.lang model")
    parser.add_argument(
        "--model", type=str, required=True, help="Path to trained model"
    )
    parser.add_argument(
        "--input", type=str, help="Single xtalk expression to translate"
    )
    parser.add_argument(
        "--interactive", "-i", action="store_true", help="Run in interactive mode"
    )

    args = parser.parse_args()

    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     INFERENCE - Testing Trained Model                         ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    print(f"\nLoading model: {args.model}")

    # Load model
    tokenizer = AutoTokenizer.from_pretrained(args.model)
    model = AutoModelForCausalLM.from_pretrained(
        args.model, torch_dtype=torch.float16, device_map="auto"
    )

    print("✓ Model loaded")

    if args.interactive:
        interactive_mode(model, tokenizer)
    elif args.input:
        print(f"\nInput: {args.input}")
        print("\nGenerating...")
        result = generate_translation(model, tokenizer, args.input)
        print(f"\n{result}")
    else:
        # Run example tests
        test_cases = [
            "(if (> x 0) (return 1) (return 0))",
            "(defn add [a b] (return (+ a b)))",
            "(for [(var i := 0) (< i 10) [(:= i (+ i 1))]] (print i))",
        ]

        print("\n=== Running Example Tests ===\n")

        for test in test_cases:
            print(f"Input: {test}")
            result = generate_translation(model, tokenizer, test)
            print(f"Output:\n{result}\n")
            print("-" * 60)


if __name__ == "__main__":
    main()
