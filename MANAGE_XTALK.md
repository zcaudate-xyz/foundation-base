# MANAGE_XTALK: Cross-Talk Management System

## Quick Start

```bash
# Show language inventory
./bin/lang inventory

# Audit XTalk support matrix
./bin/lang audit

# Scaffold Python model files
./bin/lang scaffold python model

# Or use lein directly
lein lang inventory
lein lang audit
lein lang scaffold python
```

## Table of Contents
1. [Quick Start](#quick-start)
2. [Overview](#overview)
3. [CLI Wrapper](#cli-wrapper)
4. [Core Concepts](#core-concepts)
5. [Architecture](#architecture)
6. [XTalk Model & Test Inventory](#xtalk-model--test-inventory)
7. [Usage Patterns](#usage-patterns)
8. [API Reference](#api-reference)
9. [Advanced Topics](#advanced-topics)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Overview

**MANAGE_XTALK** is the cross-talk management system in foundation-base that handles:
- **Language Management**: Track and manage multiple language specifications (Python, Go, Rust, JavaScript, etc.)
- **Model Inventory**: Maintain XTalk model specifications for each language
- **Test Coverage**: Organize and audit language-specific test suites
- **Runtime Support Matrix**: Track which languages are installed, supported, and their capabilities
- **Scaffolding**: Generate boilerplate code for new language models and tests

Located in `src/std/lang/manage/`, it provides both:
- **CLI Interface** via `./bin/lang` wrapper script
- **Leiningen Alias** via `lein lang` command
- **REPL API** for programmatic access

### Key Components

```
std.lang.manage/
├── manage.clj              # Main entry point and coordination
├── xtalk/
│   ├── xtalk-audit.clj     # Language auditing & support matrix
│   ├── xtalk-ops.clj       # Core operations
│   └── xtalk-scaffold.clj  # Code generation & scaffolding
└── model/
    └── spec_xtalk/         # Language specification models
        ├── fn_*.clj        # Function specifications
        └── com_*.clj       # Communication protocols
```

---

## CLI Wrapper

### Installation & Usage

The `./bin/lang` script provides a user-friendly command-line interface for XTalk management:

```bash
# Show help
./bin/lang help

# Show comprehensive language inventory
./bin/lang inventory

# Audit installed languages
./bin/lang audit

# Scaffold new language implementation
./bin/lang scaffold <lang> [model|test|both]
```

### Available Commands

#### inventory
Displays model and test inventory across all supported XTalk languages:

```bash
$ ./bin/lang inventory
[INFO] Fetching XTalk model and test inventory...

=== XTalk Model Inventory ===

{:python  {:lang :python
           :model-files [...] 
           :model-forms #{:fn :com}
           :model-count 15}
 :go      {:lang :go
           :model-files [...]
           :model-forms #{:fn :com}
           :model-count 12}
 ...}

=== XTalk Test Inventory ===
{...}

=== Runtime Language Configuration ===
Total supported languages: 8
Languages: go, javascript, lua, python, rust, xtalk, ...
```

**Output Components**:
- `model-files` - List of .clj files defining the language model
- `model-forms` - Types of specifications (`:fn` for functions, `:com` for communication)
- `model-count` - Number of model specifications
- `test-files` - Associated test files
- Inventory is grouped by language keyword (`:python`, `:go`, etc.)

#### audit
Audits installed runtime languages and support matrix:

```bash
$ ./bin/lang audit
[INFO] Auditing XTalk specifications and coverage...

=== Installed Languages ===

Found 3 installed languages:
  - python
  - go
  - javascript

=== Support Matrix ===

{:languages [:python :go :javascript]
 :features [:async :type-checking :bytecode-compilation ...]
 :status {:python {:installed true :version "3.11" :features [...]}
          :go {:installed true :version "1.21"}
          ...}
 :summary {:total 8 :installed 3 :missing 5}}
```

#### scaffold
Generate scaffolding for language implementations:

```bash
# Scaffold model files for Python
./bin/lang scaffold python model

# Scaffold test files
./bin/lang scaffold go test

# Scaffold both
./bin/lang scaffold rust both
```

**Generated Files**:
- Model files at `src/std/lang/model/spec_xtalk/`
- Test files at `test/std/lang/model/spec_xtalk/`
- Include basic templates for function and communication specs

#### help
Display comprehensive help message with all commands and options.

### Environment Variables

```bash
# Set custom project root
export PROJECT_ROOT=/custom/path

# Enable debug logging
export LANG_DEBUG=1

# Custom classpath cache location
export CPCACHE_DIR=./.custom-cache
```

### Error Handling

The script provides clear error messages:

```bash
$ ./bin/lang scaffold
[ERROR] Language must be specified
[INFO] Usage: ./bin/lang scaffold <lang> [model|test|both]
```

---

## Core Concepts

### 1. XTalk Language
A language definition in the XTalk system includes:
- **Specifications**: Function and communication protocol definitions
- **Models**: Runtime execution models for the language
- **Tests**: Language-specific test suites
- **Support Matrix**: Feature availability and runtime support

Example languages:
- `python` - Python 3.x runtime
- `go` - Go 1.x runtime  
- `rust` - Rust 1.x runtime
- `javascript` - Node.js/Browser runtime
- `lua` - Lua 5.x runtime
- `xtalk` - Foundation's internal language

### 2. Model Specification
Defines language-specific semantics:
- **`fn_*` files**: Function specifications for the language
- **`com_*` files**: Communication protocol specifications
- Stored as Clojure files in `src/std/lang/model/spec_xtalk/`

Example file: `fn_python.clj`
```clojure
^{:refer my.lang.model/python-function :added "4.1"}
(fact "python function definition"
  (python-function {:name "greet"
                   :args ["name"]
                   :body "return f'Hello {name}'"})
  => {:lang :python
      :type :fn
      :valid? true})
```

### 3. Support Matrix
Describes what each language supports:

```clojure
{:language :python
 :version "3.11"
 :installed true
 :features {
   :async true              ; Async/await support
   :type-checking true      ; Static type system available
   :bytecode-compilation true
   :interop true            ; Can call other languages
   :ffi-bindings true       ; Foreign function interface
   :package-manager true    ; Has package manager (pip)
   :repl true               ; Has interactive REPL
   :debugger true           ; Debug support
   :profiler true           ; Performance profiling
   :documentation-generation true
 }}
```

### 4. Inventory
Tracking of available specifications and tests:

```clojure
{:language :python
 :model-files ["src/std/lang/model/spec_xtalk/fn_python.clj"
               "src/std/lang/model/spec_xtalk/com_python.clj"]
 :model-forms #{:fn :com}
 :model-count 2
 :test-files ["test/std/lang/model/spec_xtalk/fn_python_test.clj"
              "test/std/lang/model/spec_xtalk/com_python_test.clj"]
 :test-count 2}
```

---

## Architecture

### System Overview

```
┌────────────────────────────────────────────────────┐
│          std.lang.manage (main entry)              │
│  - Coordinate all XTalk management operations      │
│  - Expose public API functions                     │
└────────────────┬─────────────────────────────────┘
                 │
     ┌───────────┼───────────┐
     ↓           ↓           ↓
┌─────────┐ ┌─────────┐ ┌──────────┐
│ Audit   │ │  Ops    │ │ Scaffold │
│ Module  │ │ Module  │ │ Module   │
└─────────┘ └─────────┘ └──────────┘
     │           │           │
     └───────────┼───────────┘
                 ↓
      ┌─────────────────────┐
      │  Model Registry     │
      │  (Specification     │
      │   Loading & Caching)│
      └─────────────────────┘
                 ↓
      ┌─────────────────────┐
      │  File System        │
      │  (Model & Test Dirs)│
      └─────────────────────┘
```

### Module Relationships

```
manage.clj (public API)
    │
    ├─→ xtalk-audit.clj
    │   ├─→ installed-languages()
    │   ├─→ support-matrix()
    │   └─→ audit-compatibility()
    │
    ├─→ xtalk-ops.clj
    │   ├─→ load-models()
    │   ├─→ validate-specs()
    │   └─→ run-tests()
    │
    └─→ xtalk-scaffold.clj
        ├─→ scaffold-language()
        ├─→ generate-model()
        └─→ generate-test()
```

### Data Flow

```
Request
  ↓
Parse Arguments
  ↓
Route to Handler (audit/scaffold/inventory)
  ↓
Load Model Registry
  ↓
Execute Operation
  ↓
Format Output
  ↓
Display Result
```

---

## XTalk Model & Test Inventory

### Model Files Organization

XTalk models are organized by language in `src/std/lang/model/spec_xtalk/`:

```
spec_xtalk/
├── fn_python.clj           # Python function specifications
├── com_python.clj          # Python communication specs
├── fn_go.clj               # Go function specifications
├── com_go.clj              # Go communication specs
├── fn_rust.clj             # Rust function specifications
├── com_rust.clj            # Rust communication specs
├── fn_javascript.clj       # JavaScript function specifications
└── com_javascript.clj      # JavaScript communication specs
```

### File Naming Convention

```
[type]_[language][_test].clj

type:       fn (function) | com (communication)
language:   lowercase language identifier
_test:      optional, for test files
```

Examples:
- `fn_python.clj` - Python function models (source)
- `fn_python_test.clj` - Python function models (tests)
- `com_rust.clj` - Rust communication models (source)
- `com_rust_test.clj` - Rust communication models (tests)

### Model File Structure

Each model file defines specifications using the `fact` macro:

```clojure
(ns std.lang.model.spec_xtalk.fn_python
  (:use code.test))

^{:refer std.lang.model.python/function-spec :added "4.1"}
(fact "python function with type hints"
  (python-function {:name "add"
                   :args [{:name "a" :type "int"}
                          {:name "b" :type "int"}]
                   :return-type "int"
                   :body "return a + b"})
  => {:lang :python
      :type :fn
      :signature "def add(a: int, b: int) -> int:"
      :valid? true})
```

### Test File Structure

Test files follow the same pattern:

```clojure
(ns std.lang.model.spec_xtalk.fn_python-test
  (:use code.test))

(fact "test Python AST generation"
  (generate-ast (python-function {...}))
  => {:type "FunctionDef" :name "add" ...})
```

### Inventory Tracking

Use the public API to query inventory:

```clojure
; Get model inventory
(std.lang.manage/xtalk-model-inventory)
=> {:python { :lang :python :model-files [...] :model-count 12}
    :go     { :lang :go     :model-files [...] :model-count 10}
    :rust   { :lang :rust   :model-files [...] :model-count 8}}

; Get test inventory
(std.lang.manage/xtalk-test-inventory)
=> {:python { :lang :python :test-files [...] :test-count 24}
    :go     { :lang :go     :test-files [...] :test-count 20}
    :rust   { :lang :rust   :test-files [...] :test-count 16}}

; Get inventory for specific roots
(std.lang.manage/xtalk-model-inventory 
  {:roots ["src/std/lang/model/spec_xtalk"]})
```

---

## Usage Patterns

### Pattern 1: Checking Language Support

```bash
# Check what languages are installed
./bin/lang audit

# Result shows:
# - Installed languages
# - Support matrix for each
# - Feature availability
```

In REPL:

```clojure
(require '[std.lang.manage :as m]
         '[std.lang.manage.xtalk-audit :as audit])

; Get installed languages
(audit/installed-languages)
=> ["python" "go" "javascript"]

; Check support matrix
(audit/support-matrix)
=> {:languages [...] :features [...] :status {...} :summary {...}}

; Check specific language support
(audit/support-matrix [:python :go])
=> {:languages [:python :go] :features [...] :status {...}}
```

### Pattern 2: Viewing Language Specifications

```bash
# Show all language models
./bin/lang inventory

# Sections show:
# - Model inventory (available specifications)
# - Test inventory (test coverage)
# - Runtime configuration
```

In REPL:

```clojure
(require '[std.lang.manage :as m])

; Get model inventory
(m/xtalk-model-inventory)
=> {:python {...} :go {...} :rust {...}}

; Get test inventory
(m/xtalk-test-inventory)
=> {:python {...} :go {...} :rust {...}}

; Get runtime languages
(m/*xtalk-runtime-langs*)
=> [:go :javascript :lua :python :rust :xtalk ...]
```

### Pattern 3: Working with Specific Language

```clojure
; List all Python specifications
(let [inventory (m/xtalk-model-inventory)]
  (get inventory :python))
=> {:lang :python
    :model-files ["src/std/lang/model/spec_xtalk/fn_python.clj"
                  "src/std/lang/model/spec_xtalk/com_python.clj"]
    :model-forms #{:fn :com}
    :model-count 2}

; Check test coverage for Python
(let [tests (m/xtalk-test-inventory)]
  (:model-files (get tests :python)))
=> ["test/std/lang/model/spec_xtalk/fn_python_test.clj"
    "test/std/lang/model/spec_xtalk/com_python_test.clj"]
```

### Pattern 4: Scaffolding New Language

```bash
# Generate Python language models
./bin/lang scaffold python model

# Generate Go test specifications
./bin/lang scaffold go test

# Generate Rust both model and test
./bin/lang scaffold rust both
```

In REPL:

```clojure
(require '[std.lang.manage.xtalk-scaffold :as scaffold])

; Preview scaffolding
(scaffold/scaffold-language :python
  {:write false :type :model})
; Returns preview, doesn't write

; Generate and persist
(scaffold/scaffold-language :go
  {:write true :type :model})
; Creates new model files

; Generate tests
(scaffold/scaffold-language :rust
  {:write true :type :test})
; Creates new test files
```

### Pattern 5: Finding Missing Specifications

```clojure
; Identify languages with incomplete coverage
(let [model-inv (m/xtalk-model-inventory)
      test-inv (m/xtalk-test-inventory)
      all-langs (set (keys model-inv))]
  (filter (fn [lang]
            (let [model-count (:model-count (get model-inv lang))
                  test-count (:model-count (get test-inv lang))]
              (< test-count model-count)))
          all-langs))
; Returns: languages needing more tests
```

---

## API Reference

### Main Entry Point (manage.clj)

#### xtalk-model-inventory
```clojure
(xtalk-model-inventory)
(xtalk-model-inventory {:roots [root-paths]})

Returns:
{:language-key {:lang :keyword
                :model-files [list-of-file-paths]
                :model-forms #{:fn :com}
                :model-count count}
 ...}

Example:
(xtalk-model-inventory)
=> {:python {:lang :python :model-files [...] :model-count 12}
    :go     {:lang :go     :model-files [...] :model-count 10}}
```

#### xtalk-test-inventory
```clojure
(xtalk-test-inventory)
(xtalk-test-inventory {:roots [root-paths]})

Returns test inventory structure similar to xtalk-model-inventory
but for *_test.clj files
```

#### *xtalk-runtime-langs*
```clojure
(def *xtalk-runtime-langs* [...])
; Dynamic var containing vector of supported runtime languages
; Automatically populated from scaffold configuration

Example:
[:go :javascript :lua :python :rust :xtalk] 
```

### Audit Module (xtalk-audit.clj)

#### installed-languages
```clojure
(audit/installed-languages)

Returns: vector of installed language names
Example: ["python" "go" "javascript"]

Checks:
- Runtime availability (can run in terminal)
- Version information
- Feature availability
```

#### support-matrix
```clojure
(audit/support-matrix)
(audit/support-matrix langs)
(audit/support-matrix langs features)

Returns:
{:languages [...]          ; Languages assessed
 :features [...]           ; Features checked
 :status {...}             ; Status for each language
 :summary {...}}           ; Overall summary

Example:
(audit/support-matrix [:python :go])
=> {:languages [:python :go]
    :features [:async :type-checking :ffi-bindings ...]
    :status {
      :python {:installed true :version "3.11" :features [...]}
      :go {:installed true :version "1.21" :features [...]}
    }
    :summary {:total 2 :installed 2 :missing 0}}
```

### Scaffold Module (xtalk-scaffold.clj)

#### scaffold-language
```clojure
(scaffold/scaffold-language lang)
(scaffold/scaffold-language lang {:write false :type :model})

Options:
:write  - Whether to write files (default: false)
:type   - :model | :test | :both (default: :model)
:force  - Overwrite existing files (default: false)

Returns: Scaffolding result or preview

Example:
(scaffold/scaffold-language :python {:write true :type :model})
; Creates fn_python.clj and com_python.clj
```

---

## Advanced Topics

### Extending with Custom Languages

To add a new language to XTalk:

1. **Add to Runtime Configuration** (xtalk-scaffold.clj):
```clojure
(def +runtime-lang-config+
  {:python {...}
   :go {...}
   :my-lang {:template-dir "templates/my-lang"
             :extensions [".ml"]
             :features [...]}})
```

2. **Create Model Specifications**:
```bash
./bin/lang scaffold my-lang model
```

3. **Add Tests**:
```bash
./bin/lang scaffold my-lang test
```

4. **Register Support Matrix**:
```clojure
; Update audit-compatibility in xtalk-audit.clj
```

### Performance Optimization

For large codebases:

1. **Cache Results**:
```clojure
(def cached-inventory (memoize m/xtalk-model-inventory))
```

2. **Parallel Processing**:
```clojure
(pmap (fn [lang] (validate-specs lang))
      (keys inventory))
```

3. **Incremental Scanning**:
```clojure
; Compare timestamps to avoid re-scanning
(defn needs-rescan? [cached-time file-path]
  (> (fs/last-modified file-path) cached-time))
```

### Integration with Build Systems

Integration with lein:

```clojure
; project.clj
{:aliases {"validate-lang" ["lang" "audit"]
           "gen-lang"      ["lang" "scaffold" "python" "model"]}}

; Command line
lein validate-lang
lein gen-lang
```

Integration with continuous integration:

```bash
#!/bin/bash
# ci/check-xtalk.sh
./bin/lang audit || exit 1
./bin/lang inventory | grep -q "model-count" || exit 1
```

---

## Best Practices

1. **Keep Models Synchronized**: Update both model and test files together
2. **Use Consistent Naming**: Follow `[type]_[lang][_test].clj` convention
3. **Document Specifications**: Add metadata to model files with `:added` version
4. **Version Support Matrix**: Keep support matrix updated when capabilities change
5. **Validate on Commit**: Run `./bin/lang audit` in pre-commit hooks
6. **Cache Wisely**: Use memoization for reads, invalidate on writes
7. **Test Coverage**: Maintain 1:1 ratio of test files to model files
8. **Performance**: Profile inventory scanning on large codebases

---

## Troubleshooting

### Issue: `Leiningen not found`

**Cause**: Lein not installed or not on PATH

**Solution**:
```bash
# Install leiningen
brew install leiningen  # macOS
# or
wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
chmod +x lein
```

### Issue: Slow inventory scanning

**Cause**: Large number of specifications or slow filesystem

**Solution**:
```clojure
; Use cached version
(def cached-inv (delay (m/xtalk-model-inventory)))
@cached-inv

; Or filter to specific roots
(m/xtalk-model-inventory {:roots ["src/std/lang/model/spec_xtalk"]})
```

### Issue: Missing language in inventory

**Cause**: Language specs not in expected directory

**Solution**:
```bash
# Check actual location
find . -name "fn_mylang.clj" -o -name "com_mylang.clj"

# Verify file naming convention
# Should be: fn_[lang].clj or com_[lang].clj
# Not: [lang]_fn.clj or [lang]_com.clj
```

### Issue: Audit shows language not installed

**Cause**: Runtime not available on system

**Solution**:
```bash
# Install runtime
python3 --version   # Check Python
go version          # Check Go
rustc --version     # Check Rust

# Or install missing
brew install python go rust
```

### Issue: File permission denied on bin/lang

**Cause**: Script not executable

**Solution**:
```bash
chmod +x /workspaces/foundation-base/bin/lang
```

---

## Related Documentation

- [code.manage Guide](guides/code.manage.md) - General code management
- [code.test Guide](guides/code.test.md) - Testing framework
- [Project README](README.md) - Project overview
- [Getting Started](GETTING_STARTED.md) - Initial setup

---

## Summary

MANAGE_XTALK provides a complete system for managing XTalk language specifications across the foundation-base platform:

**Key Capabilities**:
- ✅ Track multiple language implementations
- ✅ Audit runtime support and features
- ✅ Generate scaffolding for new languages
- ✅ Maintain specification inventory
- ✅ CLI + REPL + lein integration
- ✅ Extensible architecture

**How to Use**:
1. Check support: `./bin/lang audit`
2. View inventory: `./bin/lang inventory`
3. Add language: `./bin/lang scaffold <lang> model`
4. Write specs: Add `fn_<lang>.clj` and `com_<lang>.clj` files
5. Test: Add corresponding `*_test.clj` files

The system is designed to scale with the project and support multiple language implementations seamlessly.
