#!/usr/bin/env python3
"""
LLM Training Pipeline for std.lang

Step 4: Train a model to translate between xtalk DSL and Python/JavaScript

Usage:
    python train.py --data-dir ../../training --model llama-2-7b --output model_output

Requirements:
    pip install transformers datasets torch accelerate peft bitsandbytes
"""

import os
import json
import argparse
from pathlib import Path
from typing import List, Dict, Any
import torch
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    T5ForConditionalGeneration,
)
from transformers.training_args import TrainingArguments
from transformers.trainer import Trainer
from transformers.data.data_collator import DataCollatorForLanguageModeling
from datasets import Dataset
from peft import LoraConfig, get_peft_model, prepare_model_for_kbit_training

# Configuration
DEFAULT_MODEL = "google/t5-small-lm-adapt"  # 60M params - perfect for translation
MAX_LENGTH = 512
BATCH_SIZE = 8
LEARNING_RATE = 5e-4
NUM_EPOCHS = 5


def load_training_data(data_dir: str) -> List[Dict[str, Any]]:
    """Load training data from our generated JSONL files"""
    data_dir = Path(data_dir)
    examples = []

    sources = [
        "step2/multi_target_data.json",
        "step1/example.json",
        "ROSETTA_BIBLE_1000.jsonl",
        "CONTROL_FLOW_PAIRS.jsonl",
        "FORMAL_SPECS_COMPLETE.jsonl",
        "COMPREHENSIVE_BIBLE_1000.jsonl",
        "ENHANCED_PATTERNS_COMBINED.jsonl",
    ]

    for source in sources:
        file_path = data_dir / source
        if file_path.exists():
            print(f"Loading {source}...")

            if source.endswith(".jsonl"):
                with open(file_path, "r") as f:
                    for line in f:
                        try:
                            data = json.loads(line.strip())
                            examples.append(data)
                        except json.JSONDecodeError:
                            continue
            else:
                with open(file_path, "r") as f:
                    data = json.load(f)
                    if isinstance(data, list):
                        examples.extend(data)
                    else:
                        examples.append(data)

    print(f"✓ Loaded {len(examples)} training examples")
    return examples


def format_training_example(example: Dict) -> Dict:
    """Format a training example for translation task"""

    # Forward: xtalk -> Python
    if "xtalk" in example and "python" in example:
        return {
            "instruction": "translate xtalk to python",
            "input": example["xtalk"].strip(),
            "output": example["python"].strip(),
        }

    # Forward: xtalk -> JavaScript
    if "xtalk" in example and ("javascript" in example or "js" in example):
        js_field = "javascript" if "javascript" in example else "js"
        return {
            "instruction": "translate xtalk to javascript",
            "input": example["xtalk"].strip(),
            "output": example[js_field].strip(),
        }

    return None


def format_training_prompt(example: Dict) -> str:
    """Format for causal LM (GPT-style)"""
    formatted = format_training_example(example)
    if not formatted:
        return ""

    return f"""### Instruction:
{formatted["instruction"]}

### Input:
{formatted["input"]}

### Output:
{formatted["output"]}"""


def format_t5_example(example: Dict) -> Dict:
    """Format for T5 (seq2seq)"""
    formatted = format_training_example(example)
    if not formatted:
        return None

    return {
        "input_text": f"{formatted['instruction']}: {formatted['input']}",
        "target_text": formatted["output"],
    }


def prepare_dataset(examples: List[Dict], is_t5: bool = False) -> Dataset:
    """Prepare dataset for training"""

    print("Formatting training examples...")
    formatted = []

    for ex in examples:
        if is_t5:
            result = format_t5_example(ex)
            if result:
                formatted.append(result)
        else:
            prompt = format_training_prompt(ex)
            if prompt:
                formatted.append({"text": prompt})

    print(f"✓ Formatted {len(formatted)} examples")

    dataset = Dataset.from_list(formatted)
    return dataset


def tokenize_dataset(
    dataset: Dataset, tokenizer, is_t5: bool = False, max_length: int = MAX_LENGTH
) -> Dataset:
    """Tokenize the dataset"""

    def tokenize_function(examples):
        if is_t5:
            return tokenizer(
                examples["input_text"],
                text_target=examples["target_text"],
                truncation=True,
                max_length=max_length,
                padding="max_length",
            )
        else:
            return tokenizer(
                examples["text"],
                truncation=True,
                max_length=max_length,
                padding="max_length",
            )

    print("Tokenizing dataset...")
    tokenized = dataset.map(
        tokenize_function, batched=True, remove_columns=dataset.column_names
    )

    print("✓ Tokenization complete")
    return tokenized


def setup_model(model_name: str):
    """Setup model - supports both CausalLM (GPT) and Seq2Seq (T5)"""

    print(f"Loading model: {model_name}")

    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(model_name)

    # Detect model type
    is_t5 = "t5" in model_name.lower()
    is_bitnet = "bitnet" in model_name.lower()
    is_causal = not is_t5

    if is_t5:
        print("  Using T5 (Seq2Seq architecture)")
        model = T5ForConditionalGeneration.from_pretrained(
            model_name,
            torch_dtype=torch.float16,
            device_map="auto" if torch.cuda.is_available() else "cpu",
            use_safetensors=True,
        )

        # Enable gradient checkpointing for memory efficiency
        if hasattr(model, "gradient_checkpointing_enable"):
            model.gradient_checkpointing_enable()
            print("  Gradient checkpointing enabled")

    else:
        tokenizer.pad_token = tokenizer.eos_token
        print("  Using CausalLM architecture")

        # BitNet requires bfloat16, others use float16
        dtype = torch.bfloat16 if is_bitnet else torch.float16
        print(f"  Using dtype: {dtype}")

        # Don't use 8-bit loading for BitNet - it's already natively quantized
        load_in_8bit = False  # disable for BitNet due to native quantization
        model = AutoModelForCausalLM.from_pretrained(
            model_name,
            torch_dtype=dtype,
            device_map="auto",
            use_safetensors=True,
            load_in_8bit=load_in_8bit,
        )
        # Only prepare for kbit training if using 4/8-bit quantization
        if load_in_8bit:
            model = prepare_model_for_kbit_training(model)

    # Enable gradient checkpointing for memory efficiency (disable for BitNet due to gradient issues)
    enable_gradient_checkpointing = not is_bitnet
    if enable_gradient_checkpointing and hasattr(
        model, "gradient_checkpointing_enable"
    ):
        model.gradient_checkpointing_enable()
        model.config.use_cache = (
            False  # Ensure cache disabled for gradient checkpointing
        )
        print("  Gradient checkpointing enabled (use_cache=False)")
    elif is_bitnet:
        model.config.use_cache = False
        print("  Gradient checkpointing disabled for BitNet (use_cache=False)")

    # Setup LoRA for efficiency
    if is_bitnet:
        print("  BitNet detected - using Llama-style target modules (q_proj, v_proj)")
        print("  Architecture: AutoBitLinear layers with q_proj/k_proj/v_proj/o_proj")
        target_modules = ["q_proj", "v_proj"]
    elif is_t5:
        target_modules = ["q", "v"]
    else:
        print("  Using GPT-2 style target modules (c_attn, c_proj)")
        target_modules = ["c_attn", "c_proj"]

    lora_config = LoraConfig(
        r=8,
        lora_alpha=16,
        target_modules=target_modules,
        lora_dropout=0.05,
        bias="none",
        task_type="SEQ_2_SEQ_LM" if is_t5 else "CAUSAL_LM",
    )

    model = get_peft_model(model, lora_config)

    print(f"✓ Model loaded with LoRA")
    print(f"  Trainable parameters: {model.print_trainable_parameters()}")

    return model, tokenizer, is_t5, is_bitnet


def train_model(
    model,
    tokenizer,
    dataset,
    output_dir: str,
    is_t5: bool = False,
    is_bitnet: bool = False,
):
    """Train the model"""

    print("\n=== Starting Training ===")

    use_gpu = torch.cuda.is_available()

    # BitNet uses bfloat16, others use float16
    if is_bitnet:
        print("  Using bfloat16 precision (BitNet)")
        use_bf16 = use_gpu
        use_fp16 = False
    else:
        use_bf16 = False
        use_fp16 = use_gpu
        if use_fp16:
            print("  Using float16 precision")

    # Adjust batch size and accumulation for BitNet due to memory constraints
    if is_bitnet:
        per_device_batch_size = 1
        gradient_accumulation_steps = 8
        print(
            f"  Using reduced batch size {per_device_batch_size} with gradient accumulation {gradient_accumulation_steps}"
        )
    else:
        per_device_batch_size = BATCH_SIZE
        gradient_accumulation_steps = 2

    training_args = TrainingArguments(
        output_dir=output_dir,
        num_train_epochs=NUM_EPOCHS,
        per_device_train_batch_size=per_device_batch_size,
        gradient_accumulation_steps=gradient_accumulation_steps,
        learning_rate=LEARNING_RATE,
        warmup_steps=50,
        logging_steps=10,
        save_steps=200,
        save_total_limit=3,
        fp16=use_fp16,
        bf16=use_bf16,
        optim="adamw_torch",
        lr_scheduler_type="cosine",
        report_to="none",
    )

    data_collator = DataCollatorForLanguageModeling(
        tokenizer=tokenizer,
        mlm=False,
    )

    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=dataset,
        data_collator=data_collator,
    )

    trainer.train()

    model.save_pretrained(os.path.join(output_dir, "final"))
    tokenizer.save_pretrained(os.path.join(output_dir, "final"))

    print(f"\n✓ Training complete! Model saved to {output_dir}/final")


def main():
    parser = argparse.ArgumentParser(description="Train LLM on std.lang data")
    parser.add_argument(
        "--data-dir",
        type=str,
        default="../../training",
        help="Directory containing training data",
    )
    parser.add_argument(
        "--model",
        type=str,
        default=DEFAULT_MODEL,
        help="Base model to fine-tune (default: gpt2 for 774M params). Options: gpt2 (774M), Qwen/Qwen2-0.5B (500M), TinyLlama/TinyLlama-1.1B (1.1B)",
    )
    parser.add_argument(
        "--output",
        type=str,
        default="model_output",
        help="Output directory for trained model",
    )
    parser.add_argument(
        "--epochs", type=int, default=NUM_EPOCHS, help="Number of training epochs"
    )
    parser.add_argument(
        "--no-quantize", action="store_true", help="Disable 4-bit quantization"
    )

    args = parser.parse_args()

    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     LLM TRAINING PIPELINE                                     ║")
    print("║     Step 4: Training std.lang Translator                      ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    # Check for GPU
    if torch.cuda.is_available():
        print(f"\n✓ GPU available: {torch.cuda.get_device_name(0)}")
    else:
        print("\n⚠ No GPU detected - training will be slow!")

    # Load data
    examples = load_training_data(args.data_dir)

    if len(examples) == 0:
        print("\n✗ No training data found!")
        print(f"Please ensure data exists in {args.data_dir}")
        return

    # Setup model first to know architecture
    model, tokenizer, is_t5, is_bitnet = setup_model(args.model)

    # Prepare dataset (format depends on model type)
    dataset = prepare_dataset(examples, is_t5)

    # Tokenize with appropriate max length
    max_len = 64 if is_bitnet else MAX_LENGTH
    tokenized_dataset = tokenize_dataset(dataset, tokenizer, is_t5, max_length=max_len)

    # Train
    train_model(model, tokenizer, tokenized_dataset, args.output, is_t5, is_bitnet)

    print("\n✓ Training pipeline complete!")
    print(f"\nNext steps:")
    print(f"  1. Test the model: python inference.py --model {args.output}/final")
    print(f"  2. Evaluate: python evaluate.py --model {args.output}/final")


if __name__ == "__main__":
    main()
