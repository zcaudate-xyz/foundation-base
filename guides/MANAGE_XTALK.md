# Xtalk Management Guide

`xtalk` (cross-talk) is a polymorphic template language that transpiles lisp to multiple runtime targets (JavaScript, Lua, Python, R, Ruby, Dart, Go, and others). The xtalk management system provides auditing, scaffolding, and maintenance tools for tracking language support, test coverage, and runtime compatibility across the codebase.

## Core Concepts

### Xtalk Model Layers

The xtalk system is organized into distinct layers:

| Layer | Purpose | Location |
|-------|---------|----------|
| **Grammar** | Core syntax and operators for xtalk transpilation | `src/std/lang/base/grammar_xtalk.clj` |
| **Model** | Language-specific transpilation rules (fn_*, com_*) | `src/std/lang/model/spec_xtalk/` |
| **Typed** | Type analysis and declaration generation | `src/std/lang/typed/xtalk_*.clj` |
| **Runtime** | Language runtime implementations and live eval | `src/rt/` |
| **Tests** | Grammar tests and runtime integration tests | `test/std/lang/model/spec_xtalk/` |

### Key Concepts

- **Function Model** (`fn_<lang>.clj`): Defines how xtalk functions transpile to a specific language
- **Component Model** (`com_<lang>.clj`): Defines xtalk data structure transpilation (records, dicts, etc.)
- **Support Matrix**: A semantic grid tracking which xtalk features (operators, constructs) are implemented vs. abstract vs. missing for each language
- **Inventory**: Cataloging of model definitions, test coverage, and runtime availability

## Common Tasks

### 1. Status and Inventory

**Get overall xtalk status**

```bash
lein manage status :with xtalk
```

From REPL:
```clojure
(require '[code.manage.xtalk :as manage])

;; Get model file inventory by language
(manage/xtalk-model-inventory)
;; => {:js {:model-files [...], :model-forms #{:fn :com}, :model-count 2}
;;     :lua {:model-files [...], ...}
;;     :python {...}}

;; Get test coverage by language
(manage/xtalk-test-inventory)
;; => {:js {:test-files [...], :test-forms #{:fn :com}, :test-count 2}
;;     ...}

;; Get runtime installation and spec support
(manage/xtalk-runtime-inventory)
;; => {:js {:runtime-installed? true, :runtime-executable? true, 
;;          :spec-implemented 42, :spec-abstract 3, :spec-missing 0}
;;     :lua {:runtime-installed? false, ...}
;;     :python {...}}

;; Get spec implementation status
(manage/xtalk-spec-inventory)
;; => {:js {:spec-tracked? true, :spec-feature-count 15,
;;          :spec-implemented 12, :spec-abstract 2, :spec-missing 1}
;;     ...}

;; Unified language status (model + runtime + spec + test)
(manage/xtalk-language-status)
;; => {:js {:model-count 2, :test-count 2, :coverage 1.0,
;;          :runtime-installed? true, :spec-implemented 42, ...}
;;     ...}
```

### 2. Spec and Feature Auditing

**Audit xtalk feature support across languages**

```bash
# Full audit of all supported languages
lein manage audit-languages

# View feature status matrix
lein manage support-matrix
```

From REPL:
```clojure
;; Get list of xtalk categories (feature groups)
(manage/xtalk-categories)
;; => [:xtalk-conditional :xtalk-loop :xtalk-error ...]

;; Get all xtalk operations
(manage/xtalk-op-map)
;; => {:x:if {...}, :x:do {...}, :x:try {...}, ...}

;; Get all x:* symbols (xtalk operators)
(manage/xtalk-symbols)
;; => [:x:if :x:do :x:try :x:throw :x:let ...]

;; Check feature implementation for specific language
(manage/feature-status :js :x:if)
;; => :implemented

(manage/feature-status :lua :x:async)
;; => :missing

;; Get full support matrix (all languages + features)
(manage/support-matrix)
;; => {:languages [:js :lua :python :r :ruby]
;;     :features [:x:if :x:do :x:try ...]
;;     :status {:js {...}, :lua {...}, ...}
;;     :summary {:js {:implemented 40, :abstract 2, :missing 1}, ...}}

;; Get support matrix for specific languages
(manage/support-matrix [:js :python])

;; Get support matrix for specific features
(manage/support-matrix nil [:x:if :x:do :x:let])

;; Audit installed language runtimes
(manage/installed-languages)
;; => [:js :lua :python :r]

;; Find what's missing for each language
(manage/missing-by-language)
;; => {:lua [...], :python [...], :r [...]}

;; Find which languages are missing specific features
(manage/missing-by-feature)
;; => {:x:async [:lua :r], :x:spread [:python], ...}
```

### 3. Xtalk Operations Inventory

**Manage and document xtalk operator definitions**

```clojure
;; Generate operation inventory (reads grammar-xtalk)
(manage/generate-xtalk-ops)
;; Generates config/xtalk/xtalk_ops.edn with all operator details

;; The result includes:
;; {:op :x:if
;;  :category :xtalk-conditional
;;  :canonical-symbol std.lang.base.grammar-xtalk/x:if
;;  :symbols [x:if]
;;  :macro std.lang.base.grammar-xtalk/x:if
;;  :doc "Conditional expression"
;;  :cases [...]}
```

### 4. Test Scaffolding and Generation

**Generate missing tests for grammar operations**

```bash
# Scaffold grammar tests from operations inventory
lein manage scaffold-xtalk-grammar-tests
```

From REPL:
```clojure
;; Scaffold grammar operation tests
(manage/scaffold-xtalk-grammar-tests {:write true})
;; Generates test/std/lang/base/grammar_xtalk_ops_test.clj

;; Split grammar tests by runtime language
(manage/separate-runtime-tests {:lang :js :write true})
;; Generates: test/std/lang/base/grammar_xtalk_js_test.clj
```

### 5. Language Support and Diagnostics

**Check which language runtimes are installed and available**

```clojure
;; Get installed language runtimes
(manage/installed-languages)
;; => [:js :lua :python]

;; Get languages that use xtalk as parent grammar
(manage/audit-languages)
;; => [:js :lua :python :r :rb :dart]

;; Filter to only installed languages with xtalk parent
(manage/audit-languages [:js :python :go])
;; => [:js :python]  (go removed if not installed)

;; Visualize support matrix
(manage/visualize-support)
;; Formatted ASCII table showing language vs. feature support
```

### 6. Code Generation and Operations

**Generate xtalk operator implementations**

```clojure
;; Generate default implementations for missing operators
(manage/generate-xtalk-ops {:write true})
;; Creates/updates operator configuration

;; List all management operations
(manage/xtalk-op-map)
;; => All available management commands and their details
```

## Workflow Examples

### Workflow 1: Add Support for a New Language

Steps to add a new target language (e.g., Go):

1. **Create grammar specification**
   - Add `:go` to runtime languages in `src/std/lang/manage/xtalk_scaffold.clj`
   - Define `+runtime-lang-config+` entry for Go

2. **Create function model**
   - Create `src/std/lang/model/spec_xtalk/fn_go.clj`
   - Define how xtalk functions transpile to Go

3. **Create component model**
   - Create `src/std/lang/model/spec_xtalk/com_go.clj`
   - Define how xtalk data structures transpile to Go

4. **Create tests**
   - Create `test/std/lang/model/spec_xtalk/fn_go_test.clj`
   - Create `test/std/lang/model/spec_xtalk/com_go_test.clj`

5. **Audit coverage**
   ```clojure
   (manage/xtalk-language-status {:langs [:go]})
   ;; Check model-count, test-count, spec coverage
   ```

6. **Generate operator tests**
   ```clojure
   (manage/scaffold-xtalk-grammar-tests {:write true})
   ;; Generates grammar tests for Go
   ```

### Workflow 2: Improve Language Coverage

When you have missing features (`:spec-missing > 0`):

1. **Identify missing features**
   ```clojure
   (manage/missing-by-language)
   ;; Shows which operators are not yet implemented
   ```

2. **Update language model**
   - Edit `src/std/lang/model/spec_xtalk/fn_<lang>.clj`
   - Add implementation for missing operator (change from `:abstract` to `:emit`)

3. **Verify coverage**
   ```clojure
   (manage/support-matrix [:your-lang])
   ;; Check that missing count decreased
   ```

4. **Add tests**
   - Create appropriate test cases in language-specific test file
   - Run: `lein test :only std.lang.model.spec-xtalk.<lang>-test`

### Workflow 3: Audit Test Coverage

Ensure every model definition has corresponding tests:

```clojure
;; Get model and test inventory
(let [model (manage/xtalk-model-inventory)
      tests (manage/xtalk-test-inventory)]
  (doseq [[lang model-entry] model]
    (let [test-entry (get tests lang)
          model-count (:model-count model-entry)
          test-count (:test-count test-entry)
          coverage (if (pos? model-count) 
                     (double (/ test-count model-count)) 
                     0.0)]
      (println (format "%s: %d models, %d tests (%.1f%%)"
                       lang model-count test-count (* 100 coverage))))))
```

## Command Reference

### CLI Commands

All commands are invoked via `lein manage` with the pattern:

```bash
lein manage <task> [selectors] [options]
```

**Xtalk-specific tasks:**

| Task | Description | Example |
|------|-------------|---------|
| `status :with xtalk` | Show overall xtalk status | `lein manage status :with xtalk` |
| `audit-languages` | Check installed language runtimes | `lein manage audit-languages` |
| `support-matrix` | Display feature support matrix | `lein manage support-matrix` |
| `missing-by-language` | Show missing operators per language | `lein manage missing-by-language` |
| `missing-by-feature` | Show which languages lack features | `lein manage missing-by-feature` |
| `generate-xtalk-ops` | Generate operation inventory | `lein manage generate-xtalk-ops` |
| `scaffold-xtalk-grammar-tests` | Create test skeletons for operators | `lein manage scaffold-xtalk-grammar-tests` |
| `separate-runtime-tests` | Split grammar tests by language | `lein manage separate-runtime-tests :lang js` |

### API Reference

**From `code.manage.xtalk` namespace:**

#### Inventory Functions

```clojure
(xtalk-model-inventory)        ; => {:js {...}, :lua {...}, ...}
(xtalk-test-inventory)         ; => {:js {...}, :lua {...}, ...}
(xtalk-runtime-inventory)      ; => {:js {...}, :lua {...}, ...}
(xtalk-spec-inventory)         ; => {:js {...}, :lua {...}, ...}
(xtalk-language-status)        ; => {:js {...}, :lua {...}, ...}
```

#### Audit Functions

```clojure
(installed-languages)          ; => [:js :lua :python :r]
(audit-languages)              ; => [:js :lua :python]
(audit-languages [:js :go])    ; => [:js] (filtered to installed)
(support-matrix)               ; => {:languages [...] :features [...] :status {...}}
(support-matrix [:js :lua])    ; => Support matrix for specific languages
(support-matrix nil [:x:if])   ; => Support matrix for specific features
(missing-by-language)          ; => {:lua [...] :go [...] :r [...]}
(missing-by-feature)           ; => {:x:async [:lua] :x:spread [...] ...}
```

#### Metadata Functions

```clojure
(xtalk-categories)             ; => [:xtalk-conditional :xtalk-loop ...]
(xtalk-op-map)                 ; => {:x:if {...} :x:do {...} ...}
(xtalk-symbols)                ; => [:x:if :x:do :x:try ...]
```

#### Generation Functions

```clojure
(generate-xtalk-ops)           ; Generate/update operator inventory
(scaffold-xtalk-grammar-tests) ; Generate grammar test skeleton
(separate-runtime-tests)       ; Split tests by runtime language
(visualize-support)            ; Display formatted support matrix
```

## File Structure

### Source Organization

```
src/std/lang/
├── manage/
│   ├── xtalk_audit.clj        # Audit and feature tracking
│   ├── xtalk_ops.clj          # Operation inventory management
│   ├── xtalk_scaffold.clj     # Test generation and scaffolding
│   └── manage.clj             # Main coordination
├── model/
│   └── spec_xtalk/
│       ├── fn_js.clj          # JavaScript function transpilation
│       ├── fn_lua.clj         # Lua function transpilation
│       ├── fn_python.clj      # Python function transpilation
│       ├── com_js.clj         # JavaScript component transpilation
│       └── ...
├── typed/
│   ├── xtalk.clj              # Core typed system
│   ├── xtalk_analysis.clj     # Type analysis
│   ├── xtalk_check.clj        # Type checking
│   ├── xtalk_parse.clj        # Parsing typed definitions
│   └── xtalk_ops.clj          # Typed operations
└── base/
    └── grammar_xtalk.clj      # Core xtalk grammar definitions
```

### Configuration

```
config/xtalk/
└── xtalk_ops.edn             # Operation inventory (managed)
```

### Tests

```
test/std/lang/
└── model/
    └── spec_xtalk/
        ├── fn_js_test.clj     # JavaScript function tests
        ├── fn_lua_test.clj    # Lua function tests
        ├── com_js_test.clj    # JavaScript component tests
        └── ...
```

## Common Issues and Solutions

### Issue 1: Missing Runtime for Language

**Problem:** `(manage/xtalk-runtime-inventory)` shows `:runtime-installed? false` for a language you need.

**Solution:**
- Install the runtime: `apt-get install nodejs` (for JavaScript), etc.
- Or, if it's already installed, ensure it's on your PATH: `which node`
- Verify with `(manage/installed-languages)`

### Issue 2: Features Showing as Missing

**Problem:** Language has `:spec-missing > 0` when adding a new language.

**Solution:**
1. Check which features are missing:
   ```clojure
   (manage/missing-by-language)
   ```
2. For each missing feature, update the grammar in the language's model files:
   - Edit `src/std/lang/model/spec_xtalk/fn_<lang>.clj`
   - Change operator status from `:abstract` to `:emit` with implementation
3. Optionally provide a partial implementation with `:abstract :emit` to indicate 
   work-in-progress

### Issue 3: Test Coverage is Low

**Problem:** A language has models but few tests (`:coverage < 1.0`).

**Solution:**
1. Identify uncovered models:
   ```clojure
   (let [model (manage/xtalk-model-inventory)
         tests (manage/xtalk-test-inventory)]
     (doseq [[lang entry] model]
       (when (> (:model-count entry) (get-in tests [lang :test-count] 0))
         (println "Coverage gap for" lang))))
   ```
2. Create corresponding test files in `test/std/lang/model/spec_xtalk/`
3. Run: `lein test :only std.lang.model.spec_xtalk.<lang>-test`

### Issue 4: Support Matrix Shows Incomplete Data

**Problem:** Some languages or features aren't appearing in `(manage/support-matrix)`.

**Solution:**
- Ensure the language has been required/loaded: `(manage/audit-languages)`
- Check that grammar definitions are properly tagged with `x:` prefix
- Verify the language grammar is properly registered in 
  `src/std/lang/base/registry.clj`

### Issue 5: Operator Inventory is Stale

**Problem:** New operators aren't appearing in `(manage/xtalk-op-map)`.

**Solution:**
1. Regenerate the inventory:
   ```clojure
   (manage/generate-xtalk-ops {:write true})
   ```
2. This reads the latest grammar definitions and updates `config/xtalk/xtalk_ops.edn`
3. Verify with: `(keys (manage/xtalk-op-map))`

## Advanced Tasks

### Analyzing Operator Metadata

```clojure
;; Get details for a specific operator
(get (manage/xtalk-op-map) :x:if)
;; => {:op :x:if
;;     :category :xtalk-conditional
;;     :canonical-symbol ...
;;     :symbols [x:if]
;;     :class :infix
;;     :requires [...]
;;     :emit :code
;;     :macro ...
;;     :doc "..."
;;     :cases [...]}
```

### Tracking Language Feature Gaps

```clojure
;; For each language, see which features block full implementation
(let [matrix (manage/support-matrix)]
  (doseq [[lang summary] (:summary matrix)]
    (let [missing (:missing summary)]
      (when (pos? missing)
        (println (format "%s needs %d features" lang missing))))))
```

### Custom Coverage Analysis

```clojure
;; Calculate detailed metrics
(defn coverage-report []
  (let [langs (manage/audit-languages)
        model (manage/xtalk-model-inventory)
        tests (manage/xtalk-test-inventory)
        specs (manage/xtalk-spec-inventory)]
    (doseq [lang langs
            :let [m (get model lang) t (get tests lang) s (get specs lang)]]
      (when m
        (printf "%-8s | Models: %3d | Tests: %3d | Cov: %5.1f%% | "
                lang (:model-count m) (:test-count t)
                (* 100.0 (/ (:test-count t 0) (:model-count m 1))))
        (printf "Features: %2d impl, %2d missing\n"
                (:spec-implemented s 0) (:spec-missing s 0))))))

(coverage-report)
```

## Related Guides

- [code.manage](code.manage.md) - General code management and maintenance tasks
- [code.test](code.test.md) - Testing framework used for xtalk tests
- [std.task](std.task.md) - Task execution engine underlying management operations
- [README.md](../README.md) - Overview of `std.lang` and xtalk system
