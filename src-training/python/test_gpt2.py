#!/usr/bin/env python3
"""
Test GPT-2 model with LoRA adapter
"""

import torch
from transformers import AutoModelForCausalLM, AutoTokenizer
from peft import PeftModel


def test_gpt2():
    print("Loading base GPT-2 model...")
    base_model = AutoModelForCausalLM.from_pretrained(
        "gpt2", torch_dtype=torch.float16, device_map="auto"
    )

    print("Loading LoRA adapter...")
    model = PeftModel.from_pretrained(base_model, "/tmp/xtalk_gpt2/final")

    tokenizer = AutoTokenizer.from_pretrained("/tmp/xtalk_gpt2/final")
    tokenizer.pad_token = tokenizer.eos_token

    print("\n=== Testing xtalk translation ===\n")

    test_cases = [
        "(if (> x 0) (return 1) (return 0))",
        "(defn add [a b] (return (+ a b)))",
        "(for [(var i := 0) (< i 10) [(:= i (+ i 1))]] (print i))",
        "(when (== x 5) (print 'five'))",
        "(cond (> x 10) 'big' (> x 5) 'medium' 'small')",
    ]

    for xtalk in test_cases:
        prompt = f"""### Instruction:
Compile the following xtalk DSL code to both Python and JavaScript.

### Input:
{xtalk}

### Response:"""

        print(f"Input: {xtalk}")

        inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

        with torch.no_grad():
            outputs = model.generate(
                **inputs,
                max_new_tokens=200,
                do_sample=True,
                temperature=0.3,
                top_p=0.9,
                pad_token_id=tokenizer.eos_token_id,
            )

        generated = tokenizer.decode(outputs[0], skip_special_tokens=True)

        if "### Response:" in generated:
            response = generated.split("### Response:")[1].strip()
        else:
            response = generated[len(prompt) :].strip()

        print(f"Output:\n{response}\n")
        print("-" * 60)


if __name__ == "__main__":
    test_gpt2()
