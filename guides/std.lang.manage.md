# `std.lang.manage` Guide: Interactive Xtalk Language Development

This guide explains how GitHub Copilot agents can work interactively with `code.test`, `code.manage`, and `std.lang.manage` to audit, extend, and test xtalk language support. It uses PHP as a worked example.

## Background: The Xtalk System

`std.lang` uses **xtalk** (cross-talk) as a portable abstraction layer for polyglot code generation. Each target language (JavaScript, Python, PHP, Ruby, Perl, etc.) provides implementations of a common set of xtalk operators, allowing the same high-level code to be transpiled to multiple languages.

### Key Directories

```
src/std/lang/model/spec_xtalk/       # core xtalk operator definitions (JS, Lua, Python, R)
src/std/lang/model_annex/spec_xtalk/ # annex language operator tables (PHP, Ruby, Perl, etc.)
src/std/lang/model_annex/            # language grammar specs (spec_php.clj, spec_perl.clj, etc.)
src/rt/basic/impl_annex/             # runtime process definitions (process_php.clj, etc.)
test/std/lang/model_annex/           # language grammar/fn tests
test/rt/basic/impl_annex/            # runtime process tests
```

---

## Part 1: Auditing Language Coverage with `std.lang.manage`

### 1.1 Check Overall Coverage

From a REPL (or `lein exec`):

```clojure
(require '[std.lang.manage :as manage])

;; Get a summary of all xtalk languages and their coverage
(manage/xtalk-coverage-summary)
;; => {:php {:lang :php, :coverage 0.72, :ready? false, ...}, ...}

;; Visualise a support matrix for all languages
(manage/visualize-support {:view :matrix})
;; Prints an ASCII table: Y=implemented, A=abstract, .=missing

;; See what's missing for PHP specifically
(manage/missing-by-language {:langs [:php]})
;; => {:php [:x-arr-sort :x-arr-clone :x-arr-each ...]}
```

### 1.2 Check Model (Grammar/Fn) Inventory

```clojure
;; Which fn_*.clj files exist for each language?
(manage/xtalk-model-inventory)
;; => {:php {:lang :php, :model-files ["src/.../fn_php.clj"], :model-forms [:fn], :model-count 1}, ...}

;; Which test files exist?
(manage/xtalk-test-inventory)
;; => {:php {:lang :php, :test-files ["test/.../fn_php_test.clj"], ...}, ...}
```

### 1.3 Check Runtime Inventory

```clojure
;; What runtimes are installed?
(manage/xtalk-runtime-inventory)

;; Full status per language (combines model + runtime + test)
(manage/xtalk-status)
;; => {:php {:lang :php, :model-files [...], :test-count N, :coverage 0.8, :ready? false}, ...}
```

---

## Part 2: Using `code.manage` Wrappers

`code.manage.xtalk` wraps `std.lang.manage` for CLI and task-based usage:

```bash
# From CLI
lein manage xtalk-status "[]" "{}"
lein manage missing-by-language "[]" "{:langs [:php]}"
lein manage visualize-support "[]" "{:view :summary}"
```

```clojure
;; From REPL
(require '[code.manage.xtalk :as xtalk])

(xtalk/xtalk-coverage-summary)
(xtalk/missing-by-language {:langs [:php]})
(xtalk/visualize-support {:view :matrix})
```

---

## Part 3: Writing Tests with `code.test`

### 3.1 Testing Xtalk Fn Transform Functions

Each language's `fn_*.clj` defines transform functions that convert abstract xtalk operator calls into concrete language AST forms. Tests live in `test/std/lang/model_annex/spec_xtalk/fn_<lang>_test.clj`.

The test pattern:

```clojure
(ns std.lang.model-annex.spec-xtalk.fn-php-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-xtalk.fn-php :refer :all]))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-len :added "4.1"}
(fact "returns count of array"
  ;; Input: an xtalk form (a list with the op as first element)
  (php-tf-x-len '(:x-len arr))
  ;; Output: the equivalent PHP AST form
  => '(count arr))
```

Key points:
- Use `^{:refer <full/path> :added "4.1"}` metadata on each `fact`
- Input is always a quoted list with the xtalk op keyword as the first element
- Expected output is the equivalent language-specific AST form

### 3.2 Testing Grammar Emission

Tests in `test/std/lang/model_annex/spec_<lang>_test.clj` verify that the full grammar emits correct language strings:

```clojure
(ns std.lang.model-annex.spec-php-test
  (:require [std.lang :as l]
            [std.lang.model-annex.spec-php :as spec-php])
  (:use [code.test]))

(l/script :php)

(fact "test php variable declaration"
  (l/emit-as :php '[(var x 42)])
  => "$x = 42;")
```

### 3.3 Testing Runtime Processes

Tests in `test/rt/basic/impl_annex/process_<lang>_test.clj` verify the runtime bootstrap and client generation:

```clojure
(ns rt.basic.impl-annex.process-php-test
  (:require [rt.basic.impl-annex.process-php :refer :all]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.basic.impl-annex.process-php/default-oneshot-wrap :added "4.0"}
(fact "creates the oneshot bootstrap form"
  (default-oneshot-wrap 1)
  => string?)

^{:refer rt.basic.impl-annex.process-php/default-basic-client :added "4.0"}
(fact "creates the basic client bootstrap"
  (default-basic-client 19000)
  => string?)
```

### 3.4 Running Tests

```bash
# Run tests for a specific file
./lein test :only std.lang.model-annex.spec-xtalk.fn-php-test

# Run tests for the PHP spec
./lein test :only std.lang.model-annex.spec-php-test

# Run tests for the PHP runtime
./lein test :only rt.basic.impl-annex.process-php-test
```

---

## Part 4: Extending a Language (PHP Example)

### Step 1: Check What's Missing

```clojure
(require '[std.lang.manage :as manage])
(manage/missing-by-language {:langs [:php]})
```

### Step 2: Implement Missing Operators in `fn_php.clj`

Add transform functions and register them in the appropriate `+php-<category>+` map.

Example – adding `x-arr-sort`:

```clojure
;; In src/std/lang/model_annex/spec_xtalk/fn_php.clj

(defn php-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (list 'usort arr key-fn))

;; Add to +php-arr+:
(def +php-arr+
  {:x-arr-push        {:macro #'php-tf-x-arr-push       :emit :macro}
   ;; ... existing entries ...
   :x-arr-sort        {:macro #'php-tf-x-arr-sort       :emit :macro}})
```

### Step 3: Add Tests for New Operators

In `test/std/lang/model_annex/spec_xtalk/fn_php_test.clj`:

```clojure
^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-arr-sort :added "4.1"}
(fact "sorts array with compare fn"
  (php-tf-x-arr-sort '(:x-arr-sort arr key-fn nil))
  => '(usort arr key-fn))
```

### Step 4: Verify Coverage

```clojure
;; Re-run the audit
(manage/xtalk-coverage-summary)
;; Coverage percentage should increase
```

### Step 5: Register Language in Scaffold Config

To enable scaffolding for PHP runtime tests, ensure PHP is in `src/std/lang/manage/xtalk_scaffold.clj`:

```clojure
(def +runtime-lang-config+
  {:js     {:script :js   :dispatch '!.js   :suffix "js"}
   :lua    {:script :lua  :dispatch '!.lua  :suffix "lua"}
   :python {:script :python :dispatch '!.py :suffix "python"}
   :r      {:script :r   :dispatch '!.R     :suffix "r"}
   :ruby   {:script :ruby :dispatch '!.rb   :suffix "rb"}
   :dart   {:script :dart :dispatch '!.dt   :suffix "dt"}
   :php    {:script :php  :dispatch '!.php  :suffix "php"}})   ;; <- add this
```

---

## Part 5: Reference – Xtalk Operator Categories

The xtalk system organises operators into categories. Each language should implement the relevant ones:

| Category | Description | Key Operators |
|----------|-------------|---------------|
| **core** | Fundamental operations | `x-len`, `x-cat`, `x-err`, `x-print`, `x-apply`, `x-eval`, `x-now-ms` |
| **math** | Mathematical functions | `x-m-abs`, `x-m-sqrt`, `x-m-pow`, `x-m-mod`, `x-m-max`, `x-m-min`, ... |
| **type** | Type checking/conversion | `x-is-string?`, `x-is-number?`, `x-is-array?`, `x-to-string`, ... |
| **arr**  | Array operations | `x-arr-push`, `x-arr-pop`, `x-arr-slice`, `x-arr-sort`, `x-arr-map`, ... |
| **str**  | String operations | `x-str-split`, `x-str-join`, `x-str-index-of`, `x-str-substring`, ... |
| **lu**   | Lookup/dict operations | `x-lu-create`, `x-lu-get`, `x-lu-set`, `x-lu-del`, ... |
| **json** | JSON encode/decode | `x-json-encode`, `x-json-decode` |
| **return** | Oneshot return helpers | `x-return-encode`, `x-return-wrap`, `x-return-eval` |

### Emit Modes

Each operator entry in a `+<lang>-*+` map supports these `:emit` modes:

- `:alias` – maps directly to a language function (`:raw 'native-fn`)
- `:macro` – uses a Clojure fn to transform the form (`:macro #'transform-fn`)
- `:abstract` – no language-specific implementation (will show as "A" in audit matrix)
- `:unit` – emits a constant value (`:default <value>`)

---

## Part 6: Quick Reference

### REPL Workflow for Adding PHP Support

```clojure
;; 1. Start REPL
;; ./lein repl

;; 2. Check current state
(require '[std.lang.manage :as manage])
(manage/visualize-support {:view :summary})

;; 3. Find gaps
(manage/missing-by-language {:langs [:php]})

;; 4. After adding implementations, re-check
(require '[std.lang.model-annex.spec-xtalk.fn-php] :reload)
(manage/visualize-support {:view :summary})

;; 5. Run tests
;; (from shell) ./lein test :only std.lang.model-annex.spec-xtalk.fn-php-test
```

### File Checklist for a New Annex Language (e.g., PHP)

| File | Purpose | Status |
|------|---------|--------|
| `src/std/lang/model_annex/spec_<lang>.clj` | Grammar + emission rules | Required |
| `src/std/lang/model_annex/spec_xtalk/fn_<lang>.clj` | Xtalk operator mappings | Required |
| `src/rt/basic/impl_annex/process_<lang>.clj` | Runtime bootstrap | Required |
| `test/std/lang/model_annex/spec_<lang>_test.clj` | Grammar emission tests | Required |
| `test/std/lang/model_annex/spec_xtalk/fn_<lang>_test.clj` | Operator transform tests | Required |
| `test/rt/basic/impl_annex/process_<lang>_test.clj` | Runtime bootstrap tests | Required |
| Entry in `xtalk_scaffold.clj` `+runtime-lang-config+` | Scaffold/audit registration | Required |

---

## Part 7: Iterative Development Workflow for Completing an Xtalk Language

This is the repeatable workflow for driving an xtalk language from "partially abstract"
to "implemented and tested". It works for PHP, but the same cycle also applies to any
other xtalk language.

### 7.1 The Core Loop

1. **Audit missing/abstract features**

   ```bash
   ./lein lang missing-by-language "{:langs [:php]}"
   ```

   This shows the current xtalk operators that are still `:abstract` for the target
   language. Treat this as the authoritative backlog for the next implementation pass.

2. **Implement the functions**

   Add the missing transforms and registrations in the language fn table, usually under:

   - `src/std/lang/model/spec_xtalk/fn_<lang>.clj` for core languages
   - `src/std/lang/model_annex/spec_xtalk/fn_<lang>.clj` for annex languages

   For PHP this means updating:

   - `src/std/lang/model_annex/spec_xtalk/fn_php.clj`
   - and, when necessary, its grammar/runtime counterparts:
     - `src/std/lang/model_annex/spec_php.clj`
     - `src/rt/basic/impl_annex/process_php.clj`

3. **Scaffold tests for the language model**

   High-level command:

   ```bash
   ./lein manage scaffold :with std.lang.model
   ```

   When focusing on PHP, prefer scoping the task to the relevant namespaces:

   ```bash
   ./lein exec -ep "(require '[code.manage :as manage])
   (manage/scaffold ['std.lang.model-annex.spec-php
                     'std.lang.model-annex.spec-xtalk.fn-php]
                    {:write false})"
   ```

   Use scaffold as a way to discover expected test stubs without blindly overwriting the
   existing test files.

4. **Check for incomplete tests**

   High-level command:

   ```bash
   ./lein manage incomplete :with std.lang.model
   ```

   PHP-focused variant:

   ```bash
   ./lein exec -ep "(require '[code.manage :as manage])
   (manage/incomplete ['std.lang.model-annex.spec-php
                       'std.lang.model-annex.spec-xtalk.fn-php]
                      {})"
   ```

   This identifies either missing facts or placeholder/TODO facts that still need to be
   filled in.

5. **Fill out the PHP tests**

   Complete or add focused facts in:

   - `test/std/lang/model_annex/spec_xtalk/fn_php_test.clj`
   - `test/std/lang/model_annex/spec_php_test.clj`
   - `test/rt/basic/impl_annex/process_php_test.clj` when runtime/bootstrap behavior changes

   Prefer small, operator-specific facts that assert on the transformed AST form, and add
   grammar/runtime tests only when the change affects actual emitted PHP strings or runtime
   execution.

6. **Run the PHP-focused model tests**

   High-level command:

   ```bash
   ./lein test :with std.lang.model
   ```

   For a PHP-focused development loop, run the narrower namespaces first:

   ```bash
   ./lein test :only std.lang.model-annex.spec-xtalk.fn-php-test
   ./lein test :only std.lang.model-annex.spec-php-test
   ./lein test :only rt.basic.impl-annex.process-php-test
   ```

7. **Fix errors and repeat the test pass**

   If tests fail:

   - inspect the emitted form or PHP string
   - fix the transform/grammar/runtime code
   - re-run the same PHP-focused tests

   Stay in this loop until the PHP test set is green.

8. **Re-audit missing/abstract features**

   ```bash
   ./lein lang missing-by-language "{:langs [:php]}"
   ```

   Compare the new output against the previous run. Any function removed from the audit has
   moved from `:abstract` to implemented. Repeat from step 2 until the remaining backlog is
   acceptable.

### 7.2 Why This Workflow Works

- `lein lang missing-by-language` gives the implementation backlog.
- `code.manage scaffold` and `code.manage incomplete` keep test coverage aligned with the
  source namespace.
- narrow `lein test :only ...` runs provide fast feedback while iterating.
- the final re-audit confirms that the language support matrix has actually improved.

### 7.3 Applying the Same Loop to Other Languages

To use this for a language other than PHP:

- replace `:php` with the target language keyword in the audit step
- replace the PHP model namespaces with that language's `spec_<lang>` and `fn_<lang>`
  namespaces
- keep the same audit -> implement -> scaffold -> incomplete -> test -> re-audit cycle

This makes the process suitable as a general xtalk language development cycle, not just a
one-off PHP procedure.
