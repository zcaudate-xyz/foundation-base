#!/usr/bin/env python3
"""
Interactive training loop for std.lang translation.

Features:
- Loads pre-generated patterns from file
- Splits into train/validation sets
- Continuously samples random batches
- Trains model incrementally
- Evaluates on validation set periodically
- Saves checkpoints
- Tracks metrics

Usage:
    python train_loop.py --model ./models/microsoft_bitnet-b1.58-2B-4T-bf16 --data ../../training/ENHANCED_PATTERNS_COMBINED.jsonl

"""

import os
import sys
import json
import random
import time
import argparse
from pathlib import Path
from typing import List, Dict, Any, Optional, Tuple
import numpy as np
import torch
from torch.utils.data import Dataset, DataLoader
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    T5ForConditionalGeneration,
    DataCollatorForLanguageModeling,
    get_linear_schedule_with_warmup,
)
from peft import LoraConfig, get_peft_model, prepare_model_for_kbit_training
import wandb  # optional, for logging


# Configuration
DEFAULT_MODEL = "./models/microsoft_bitnet-b1.58-2B-4T-bf16"
DEFAULT_DATA = "../../training/ENHANCED_PATTERNS_COMBINED.jsonl"
MAX_LENGTH = 512
BATCH_SIZE = 2
GRADIENT_ACCUMULATION_STEPS = 4
LEARNING_RATE = 5e-4
NUM_EPOCHS = 100  # Large number for continuous training
WARMUP_STEPS = 50
LOGGING_INTERVAL = 10  # batches
EVAL_INTERVAL = 100  # batches
CHECKPOINT_INTERVAL = 500  # batches
VALIDATION_SIZE = 0.1  # 10% for validation


class StreamingDataset(Dataset):
    """Dataset that streams random batches from a pool of examples."""

    def __init__(self, examples: List[Dict], is_t5: bool = False):
        self.examples = examples
        self.is_t5 = is_t5
        self.indices = list(range(len(examples)))

    def __len__(self):
        return len(self.examples)

    def __getitem__(self, idx):
        """Get item by index (used for validation)."""
        return self.format_example(self.examples[idx])

    def format_example(self, example: Dict) -> Dict:
        """Format example for training."""
        if self.is_t5:
            # T5 format: input_text -> target_text
            instruction = "Translate this xtalk DSL code to Python"
            if "python" in example:
                return {
                    "input_text": f"{instruction}: {example['xtalk']}",
                    "target_text": example["python"],
                }
            elif "js" in example:
                return {
                    "input_text": f"{instruction}: {example['xtalk']}",
                    "target_text": example["js"],
                }
            else:
                raise ValueError("Example must have python or js key")
        else:
            # Causal LM format: instruction + input -> output
            instruction = "Translate this xtalk DSL code to Python"
            target = example.get("python") or example.get("js", "")
            text = f"""### Instruction:
{instruction}

### Input:
{example["xtalk"]}

### Output:
{target}"""
            return {"text": text}

    def sample_batch(self, batch_size: int) -> List[Dict]:
        """Sample a random batch of examples."""
        sampled_indices = random.sample(
            self.indices, min(batch_size, len(self.indices))
        )
        return [self.format_example(self.examples[i]) for i in sampled_indices]


def load_examples(data_path: str) -> List[Dict[str, Any]]:
    """Load examples from JSONL file."""
    examples = []
    print(f"Loading data from {data_path}")

    with open(data_path, "r") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                data = json.loads(line)
                # Ensure required fields
                if "xtalk" in data and ("python" in data or "js" in data):
                    examples.append(data)
            except json.JSONDecodeError:
                continue

    print(f"✓ Loaded {len(examples)} examples")
    return examples


def setup_model(model_name: str):
    """Setup model with LoRA."""
    print(f"Loading model: {model_name}")

    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(model_name)

    # Detect model type
    is_t5 = "t5" in model_name.lower()
    is_bitnet = "bitnet" in model_name.lower()

    if is_t5:
        print("  Using T5 (Seq2Seq architecture)")
        model = T5ForConditionalGeneration.from_pretrained(
            model_name,
            torch_dtype=torch.float16,
            device_map="auto" if torch.cuda.is_available() else "cpu",
            use_safetensors=True,
        )
    else:
        tokenizer.pad_token = tokenizer.eos_token
        print("  Using CausalLM architecture")

        # BitNet requires bfloat16, others use float16
        dtype = torch.bfloat16 if is_bitnet else torch.float16
        print(f"  Using dtype: {dtype}")

        model = AutoModelForCausalLM.from_pretrained(
            model_name,
            torch_dtype=dtype,
            device_map="auto",
            use_safetensors=True,
        )

        # Disable cache for BitNet
        if is_bitnet:
            model.config.use_cache = False

    # Setup LoRA
    if is_bitnet:
        print("  BitNet detected - using Llama-style target modules (q_proj, v_proj)")
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
    model.print_trainable_parameters()

    return model, tokenizer, is_t5, is_bitnet


def train_continuous(
    model,
    tokenizer,
    train_dataset: StreamingDataset,
    val_dataset: StreamingDataset,
    output_dir: str,
    is_t5: bool = False,
    is_bitnet: bool = False,
    total_batches: int = 10000,
    batch_size: int = BATCH_SIZE,
    eval_interval: int = EVAL_INTERVAL,
    checkpoint_interval: int = CHECKPOINT_INTERVAL,
):
    """Continuous training loop with random batch sampling."""

    print("\n" + "=" * 60)
    print("STARTING CONTINUOUS TRAINING LOOP")
    print("=" * 60)
    print(f"  Total batches target: {total_batches}")
    print(f"  Batch size: {batch_size}")
    print(f"  Eval every {eval_interval} batches")
    print(f"  Checkpoint every {checkpoint_interval} batches")
    print(f"  Output directory: {output_dir}")

    # Setup device
    device = model.device
    print(f"  Using device: {device}")

    # Setup optimizer and scheduler
    optimizer = torch.optim.AdamW(model.parameters(), lr=LEARNING_RATE)
    total_steps = total_batches
    scheduler = get_linear_schedule_with_warmup(
        optimizer,
        num_warmup_steps=WARMUP_STEPS,
        num_training_steps=total_steps,
    )

    # Data collator
    data_collator = DataCollatorForLanguageModeling(
        tokenizer=tokenizer,
        mlm=False,
    )

    # Training loop
    global_step = 0
    train_losses = []
    val_losses = []
    best_val_loss = float("inf")

    # Create output directory
    os.makedirs(output_dir, exist_ok=True)
    checkpoint_dir = os.path.join(output_dir, "checkpoints")
    os.makedirs(checkpoint_dir, exist_ok=True)

    # Start timer
    start_time = time.time()

    try:
        while global_step < total_batches:
            model.train()

            # Sample a random batch
            batch_examples = train_dataset.sample_batch(batch_size)

            # Tokenize batch
            if is_t5:
                inputs = tokenizer(
                    [ex["input_text"] for ex in batch_examples],
                    truncation=True,
                    max_length=MAX_LENGTH,
                    padding="max_length",
                    return_tensors="pt",
                )
                targets = tokenizer(
                    [ex["target_text"] for ex in batch_examples],
                    truncation=True,
                    max_length=MAX_LENGTH,
                    padding="max_length",
                    return_tensors="pt",
                )
                inputs["labels"] = targets["input_ids"]
            else:
                inputs = tokenizer(
                    [ex["text"] for ex in batch_examples],
                    truncation=True,
                    max_length=MAX_LENGTH,
                    padding="max_length",
                    return_tensors="pt",
                )
                # For causal LM, labels are same as input_ids
                inputs["labels"] = inputs["input_ids"].clone()

            # Move to device
            inputs = {k: v.to(device) for k, v in inputs.items()}

            # Forward pass
            outputs = model(**inputs)
            loss = outputs.loss

            # Backward pass
            loss.backward()

            # Gradient accumulation
            if (global_step + 1) % GRADIENT_ACCUMULATION_STEPS == 0:
                torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
                optimizer.step()
                scheduler.step()
                optimizer.zero_grad()

            # Record loss
            train_losses.append(loss.item())

            # Logging
            if global_step % LOGGING_INTERVAL == 0:
                avg_loss = (
                    np.mean(train_losses[-LOGGING_INTERVAL:])
                    if len(train_losses) >= LOGGING_INTERVAL
                    else loss.item()
                )
                print(
                    f"  Batch {global_step}/{total_batches} | Loss: {loss.item():.4f} | Avg: {avg_loss:.4f}"
                )

            # Validation
            if global_step % eval_interval == 0 and global_step > 0:
                val_loss = evaluate(model, tokenizer, val_dataset, is_t5, batch_size)
                val_losses.append(val_loss)
                print(f"  Validation @ batch {global_step}: loss = {val_loss:.4f}")

                # Save best model
                if val_loss < best_val_loss:
                    best_val_loss = val_loss
                    model.save_pretrained(os.path.join(output_dir, "best"))
                    tokenizer.save_pretrained(os.path.join(output_dir, "best"))
                    print(f"  ✓ New best model saved (loss: {val_loss:.4f})")

            # Checkpoint
            if global_step % checkpoint_interval == 0 and global_step > 0:
                checkpoint_path = os.path.join(
                    checkpoint_dir, f"checkpoint-{global_step}"
                )
                model.save_pretrained(checkpoint_path)
                tokenizer.save_pretrained(checkpoint_path)
                print(f"  ✓ Checkpoint saved at {checkpoint_path}")

                # Save training stats
                stats = {
                    "global_step": global_step,
                    "train_losses": train_losses,
                    "val_losses": val_losses,
                    "best_val_loss": best_val_loss,
                }
                with open(os.path.join(output_dir, "training_stats.json"), "w") as f:
                    json.dump(stats, f, indent=2)

            global_step += 1

    except KeyboardInterrupt:
        print("\nTraining interrupted by user")

    finally:
        # Save final model
        final_dir = os.path.join(output_dir, "final")
        model.save_pretrained(final_dir)
        tokenizer.save_pretrained(final_dir)

        # Training summary
        end_time = time.time()
        training_time = end_time - start_time

        print("\n" + "=" * 60)
        print("TRAINING COMPLETE")
        print("=" * 60)
        print(f"  Total batches: {global_step}")
        print(f"  Training time: {training_time:.1f}s")
        print(f"  Best validation loss: {best_val_loss:.4f}")
        print(f"  Final model saved to: {final_dir}")

        if train_losses:
            print(f"  Final training loss: {train_losses[-1]:.4f}")

        # Plot loss curve if matplotlib available
        try:
            import matplotlib.pyplot as plt

            plt.figure(figsize=(10, 5))
            plt.plot(train_losses, label="Training Loss")
            if val_losses:
                val_steps = [i * eval_interval for i in range(len(val_losses))]
                plt.plot(val_steps, val_losses, label="Validation Loss", marker="o")
            plt.xlabel("Batch")
            plt.ylabel("Loss")
            plt.legend()
            plt.title("Training Progress")
            plt.grid(True, alpha=0.3)
            plt.savefig(os.path.join(output_dir, "loss_curve.png"))
            print(
                f"  Loss curve saved to: {os.path.join(output_dir, 'loss_curve.png')}"
            )
        except ImportError:
            print("  Install matplotlib to generate loss curve")


def evaluate(
    model,
    tokenizer,
    val_dataset: StreamingDataset,
    is_t5: bool,
    batch_size: int = 4,
    num_batches: int = 10,
) -> float:
    """Evaluate model on validation set."""
    model.eval()
    losses = []

    with torch.no_grad():
        for _ in range(num_batches):
            # Sample validation batch
            batch_examples = val_dataset.sample_batch(batch_size)

            # Tokenize
            if is_t5:
                inputs = tokenizer(
                    [ex["input_text"] for ex in batch_examples],
                    truncation=True,
                    max_length=MAX_LENGTH,
                    padding="max_length",
                    return_tensors="pt",
                )
                targets = tokenizer(
                    [ex["target_text"] for ex in batch_examples],
                    truncation=True,
                    max_length=MAX_LENGTH,
                    padding="max_length",
                    return_tensors="pt",
                )
                inputs["labels"] = targets["input_ids"]
            else:
                inputs = tokenizer(
                    [ex["text"] for ex in batch_examples],
                    truncation=True,
                    max_length=MAX_LENGTH,
                    padding="max_length",
                    return_tensors="pt",
                )
                inputs["labels"] = inputs["input_ids"].clone()

            # Move to device
            inputs = {k: v.to(model.device) for k, v in inputs.items()}

            # Forward pass
            outputs = model(**inputs)
            losses.append(outputs.loss.item())

    model.train()
    return np.mean(losses)


def main():
    parser = argparse.ArgumentParser(
        description="Continuous training loop for std.lang"
    )
    parser.add_argument(
        "--model",
        type=str,
        default=DEFAULT_MODEL,
        help="Model to fine-tune",
    )
    parser.add_argument(
        "--data",
        type=str,
        default=DEFAULT_DATA,
        help="Path to JSONL training data",
    )
    parser.add_argument(
        "--output",
        type=str,
        default="model_output_continuous",
        help="Output directory",
    )
    parser.add_argument(
        "--total-batches",
        type=int,
        default=10000,
        help="Total number of batches to train",
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=BATCH_SIZE,
        help="Batch size",
    )
    parser.add_argument(
        "--eval-interval",
        type=int,
        default=EVAL_INTERVAL,
        help="Evaluate every N batches",
    )
    parser.add_argument(
        "--checkpoint-interval",
        type=int,
        default=CHECKPOINT_INTERVAL,
        help="Save checkpoint every N batches",
    )
    parser.add_argument(
        "--val-size",
        type=float,
        default=VALIDATION_SIZE,
        help="Validation set size fraction",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=42,
        help="Random seed",
    )

    args = parser.parse_args()

    # Set random seeds
    random.seed(args.seed)
    np.random.seed(args.seed)
    torch.manual_seed(args.seed)
    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(args.seed)

    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     CONTINUOUS TRAINING LOOP                                  ║")
    print("╚════════════════════════════════════════════════════════════════╝")

    # Load data
    examples = load_examples(args.data)
    if not examples:
        print("✗ No examples loaded!")
        return

    # Split train/validation
    random.shuffle(examples)
    split_idx = int(len(examples) * (1 - args.val_size))
    train_examples = examples[:split_idx]
    val_examples = examples[split_idx:]

    print(f"  Training examples: {len(train_examples)}")
    print(f"  Validation examples: {len(val_examples)}")

    # Setup model
    model, tokenizer, is_t5, is_bitnet = setup_model(args.model)

    # Adjust batch size for BitNet if needed
    if is_bitnet and args.batch_size > 1:
        print(f"  BitNet detected, reducing batch size to 1 for memory")
        args.batch_size = 1

    # Create datasets
    train_dataset = StreamingDataset(train_examples, is_t5)
    val_dataset = StreamingDataset(val_examples, is_t5)

    # Train
    train_continuous(
        model=model,
        tokenizer=tokenizer,
        train_dataset=train_dataset,
        val_dataset=val_dataset,
        output_dir=args.output,
        is_t5=is_t5,
        is_bitnet=is_bitnet,
        total_batches=args.total_batches,
        batch_size=args.batch_size,
        eval_interval=args.eval_interval,
        checkpoint_interval=args.checkpoint_interval,
    )

    print("\n✓ Done!")


if __name__ == "__main__":
    main()
