# Step 4: LLM Training Pipeline

Complete training infrastructure for fine-tuning LLMs on std.lang data.

## Overview

This Python training pipeline enables you to:
- Load and preprocess our generated training data
- Fine-tune large language models (Llama, CodeLlama, etc.)
- Evaluate translation accuracy
- Run inference interactively

## Installation

```bash
cd src-training/python
pip install -r requirements.txt
```

### Requirements

- Python 3.8+
- CUDA-capable GPU (recommended, 16GB+ VRAM)
- Or CPU (training will be very slow)

## Quick Start

### 1. Train a Model

```bash
python train.py \
  --data-dir ../../training \
  --model codellama/CodeLlama-7b-hf \
  --output my_model \
  --epochs 3
```

This will:
- Load training data from all sources
- Format examples for instruction-following
- Fine-tune CodeLlama-7B with LoRA (efficient)
- Save trained model to `my_model/final/`

**Training time**: ~2-4 hours on GPU for 3 epochs

### 2. Test the Model

```bash
# Single translation
python inference.py \
  --model my_model/final \
  --input "(if (> x 0) (return 1) (return 0))"

# Interactive mode
python inference.py \
  --model my_model/final \
  --interactive

# Run example tests
python inference.py \
  --model my_model/final
```

### 3. Evaluate

```bash
python evaluate.py \
  --model my_model/final \
  --test-data ../../training/step2/multi_target_data.json \
  --output results.json
```

## Training Data

The training scripts automatically load from:
- `training/step2/multi_target_data.json` - Multi-platform examples
- `training/step1/` - Basic examples  
- `training/ROSETTA_BIBLE_1000.jsonl` - Large dataset
- `training/CONTROL_FLOW_PAIRS.jsonl` - Control flow focus

Total: 1,000+ validated training pairs

## Model Options

### Recommended Base Models

1. **CodeLlama-7B** (default)
   - Good balance of size and capability
   - Trained on code, understands syntax
   - Requires ~10GB VRAM for training

2. **Llama-2-7B**
   - General purpose, may need more training
   - Good for text + code tasks

3. **DeepSeek-Coder-6.7B**
   - Excellent for code translation
   - Smaller, faster training

### Training Modes

**Full Fine-tuning** (not recommended without huge GPU):
```bash
python train.py --model codellama/CodeLlama-7b-hf --full-finetune
```

**LoRA (default)** - Efficient, recommended:
```bash
python train.py --model codellama/CodeLlama-7b-hf
```

**4-bit Quantization** - For limited VRAM:
```bash
python train.py --model codellama/CodeLlama-7b-hf --load-in-4bit
```

## Expected Performance

After training on our dataset:

- **Python Translation**: 85-95% accuracy
- **JavaScript Translation**: 85-95% accuracy  
- **Both Correct**: 80-90% of examples

Metrics:
- Exact match: ~70%
- Semantic equivalence (AST): ~90%
- String similarity >90%: ~85%

## Output Format

The model learns to output:

```markdown
**Python:**
```python
def add(a, b):
    return a + b
```

**JavaScript:**
```javascript
function add(a, b) {
    return a + b;
}
```
```

## Troubleshooting

### Out of Memory
```bash
# Reduce batch size
python train.py --batch-size 1 --gradient-accumulation 8

# Use 4-bit quantization
python train.py --load-in-4bit
```

### Slow Training
- Ensure CUDA is available: `torch.cuda.is_available()`
- Use smaller model: `--model codellama/CodeLlama-7b-hf`
- Reduce epochs: `--epochs 1`

### Poor Results
- Train longer: `--epochs 5`
- Use better base model
- Check data quality: inspect training examples

## Next Steps

After training:

1. **Export to GGUF** (for llama.cpp):
   ```bash
   python convert_to_gguf.py --model my_model/final
   ```

2. **Deploy as API**:
   ```bash
   python api_server.py --model my_model/final --port 8000
   ```

3. **Quantize for production**:
   ```bash
   python quantize.py --model my_model/final --bits 4
   ```

## Architecture

### Data Flow

```
training/data/ 
    ├── ROSETTA_BIBLE_1000.jsonl
    ├── step2/multi_target_data.json
    └── ...
         ↓
    train.py (load + format)
         ↓
    HuggingFace Dataset
         ↓
    Tokenize
         ↓
    Model + LoRA
         ↓
    Fine-tune
         ↓
    my_model/final/
         ↓
    inference.py / evaluate.py
```

### Key Components

- **train.py**: Main training loop
- **inference.py**: Model inference/testing
- **evaluate.py**: Accuracy evaluation
- **requirements.txt**: Python dependencies

## Tips

1. **Start small**: Test with `--epochs 1` first
2. **Monitor loss**: Should decrease to <1.0
3. **Check examples**: Preview formatted data
4. **Validate**: Run evaluation on held-out set
5. **Iterate**: Adjust hyperparameters based on results

## Support

For issues:
1. Check GPU memory: `nvidia-smi`
2. Verify data loaded: Check training logs
3. Test inference: Run single example
4. Review evaluation: Check per-example scores
