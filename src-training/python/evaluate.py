#!/usr/bin/env python3
"""
Evaluation script for trained model

Tests model accuracy on held-out test set.

Usage:
    python evaluate.py --model model_output/final --test-data ../../training/test.json
"""

import argparse
import json
import re
from pathlib import Path
from typing import List, Dict, Tuple
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer
from difflib import SequenceMatcher


def similarity_score(a: str, b: str) -> float:
    """Calculate string similarity (0-100)"""
    return SequenceMatcher(None, a.lower(), b.lower()).ratio() * 100


def parse_code_blocks(text: str) -> Dict[str, str]:
    """Extract code blocks from model output"""
    result = {}

    # Look for Python code
    python_match = re.search(r"```python\s*(.*?)\s*```", text, re.DOTALL)
    if python_match:
        result["python"] = python_match.group(1).strip()

    # Look for JavaScript code
    js_match = re.search(r"```javascript\s*(.*?)\s*```", text, re.DOTALL)
    if js_match:
        result["javascript"] = js_match.group(1).strip()

    return result


def evaluate_translation(model, tokenizer, test_examples: List[Dict]) -> Dict:
    """Evaluate model on test examples"""

    results = {
        "total": len(test_examples),
        "python_correct": 0,
        "js_correct": 0,
        "both_correct": 0,
        "python_scores": [],
        "js_scores": [],
        "examples": [],
    }

    for i, example in enumerate(test_examples):
        if i % 10 == 0:
            print(f"Evaluating {i}/{len(test_examples)}...")

        xtalk = example.get("xtalk", "")
        expected_python = example.get("python", example.get("py", ""))
        expected_js = example.get("javascript", example.get("js", ""))

        # Generate
        prompt = f"""### Instruction:
Compile the following xtalk DSL code to both Python and JavaScript.

### Input:
{xtalk}

### Response:"""

        inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

        with torch.no_grad():
            outputs = model.generate(
                **inputs,
                max_new_tokens=512,
                do_sample=False,  # Deterministic for evaluation
                pad_token_id=tokenizer.eos_token_id,
            )

        generated = tokenizer.decode(outputs[0], skip_special_tokens=True)

        # Parse output
        parsed = parse_code_blocks(generated)
        generated_python = parsed.get("python", "")
        generated_js = parsed.get("javascript", "")

        # Score
        py_score = similarity_score(generated_python, expected_python)
        js_score = similarity_score(generated_js, expected_js)

        results["python_scores"].append(py_score)
        results["js_scores"].append(js_score)

        # Check if correct (threshold: 90% similarity)
        py_correct = py_score >= 90
        js_correct = js_score >= 90

        if py_correct:
            results["python_correct"] += 1
        if js_correct:
            results["js_correct"] += 1
        if py_correct and js_correct:
            results["both_correct"] += 1

        # Store example
        results["examples"].append(
            {
                "xtalk": xtalk,
                "expected_python": expected_python,
                "generated_python": generated_python,
                "python_score": py_score,
                "expected_js": expected_js,
                "generated_js": generated_js,
                "js_score": js_score,
            }
        )

    return results


def print_results(results: Dict):
    """Print evaluation results"""

    print("\n" + "=" * 60)
    print("EVALUATION RESULTS")
    print("=" * 60)

    total = results["total"]

    print(f"\nTotal examples: {total}")
    print(f"\nPython Accuracy:")
    print(
        f"  Correct: {results['python_correct']}/{total} ({results['python_correct'] / total * 100:.1f}%)"
    )
    print(
        f"  Avg Score: {sum(results['python_scores']) / len(results['python_scores']):.1f}%"
    )

    print(f"\nJavaScript Accuracy:")
    print(
        f"  Correct: {results['js_correct']}/{total} ({results['js_correct'] / total * 100:.1f}%)"
    )
    print(f"  Avg Score: {sum(results['js_scores']) / len(results['js_scores']):.1f}%")

    print(f"\nBoth Correct:")
    print(
        f"  {results['both_correct']}/{total} ({results['both_correct'] / total * 100:.1f}%)"
    )

    # Show some examples
    print("\n" + "-" * 60)
    print("SAMPLE PREDICTIONS")
    print("-" * 60)

    for ex in results["examples"][:3]:
        print(f"\nxtalk: {ex['xtalk']}")
        print(f"Expected Python: {ex['expected_python'][:50]}...")
        print(f"Generated Python: {ex['generated_python'][:50]}...")
        print(f"Score: {ex['python_score']:.1f}%")
        print()


def main():
    parser = argparse.ArgumentParser(description="Evaluate trained model")
    parser.add_argument(
        "--model", type=str, required=True, help="Path to trained model"
    )
    parser.add_argument(
        "--test-data",
        type=str,
        default="../../training/step2/multi_target_data.json",
        help="Path to test data",
    )
    parser.add_argument(
        "--output",
        type=str,
        default="evaluation_results.json",
        help="Output file for detailed results",
    )
    parser.add_argument(
        "--limit", type=int, default=None, help="Limit number of test examples"
    )

    args = parser.parse_args()

    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     MODEL EVALUATION                                          ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    print(f"\nLoading model: {args.model}")
    tokenizer = AutoTokenizer.from_pretrained(args.model)
    model = AutoModelForCausalLM.from_pretrained(
        args.model, torch_dtype=torch.float16, device_map="auto"
    )
    print("✓ Model loaded")

    print(f"\nLoading test data: {args.test_data}")
    with open(args.test_data, "r") as f:
        test_data = json.load(f)

    if args.limit:
        test_data = test_data[: args.limit]

    print(f"✓ Loaded {len(test_data)} test examples")

    print("\nEvaluating...")
    results = evaluate_translation(model, tokenizer, test_data)

    print_results(results)

    # Save detailed results
    with open(args.output, "w") as f:
        json.dump(results, f, indent=2)

    print(f"\n✓ Detailed results saved to: {args.output}")


if __name__ == "__main__":
    main()
