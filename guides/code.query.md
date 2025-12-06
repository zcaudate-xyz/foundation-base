# `code.query` Guide

`code.query` enables advanced searching and modification of Clojure source code using a pattern-matching DSL over zippers. It allows you to find code structures that match a specific shape and transform them.

## Core Concepts

- **ZLoc**: A zipper location representing a node in the syntax tree.
- **Selector**: A vector or list pattern describing the structure to match.
- **Directives**: Special symbols (e.g., `^:%`, `|`, `^:?`) in patterns that control matching behavior (capturing, optionality, cursor placement).

## Usage

### The `$` Macro

The `$` macro is the main interface. It takes a context (string, file, zipper), a selector path, and optional arguments (transformation function, options).

```clojure
(require '[code.query :as query])
```

### Scenarios

#### 1. Finding Specific Forms

**Scenario: Find all `defn` names in a string.**

We match `(defn name ...)` and capture the name.

```clojure
(def code "(defn foo [x] x) (defn bar [y] y)")

(query/$ code
         ;; Pattern: List starting with defn, capture the second element (symbol)
         '[(defn ^:% symbol? & _)])
;; => (foo bar)
```

**Scenario: Find all maps containing a specific key.**

```clojure
(def code "{:a 1 :b 2} {:a 3}")

(query/$ code
         ;; Pattern: Map containing key :a
         '[{:a _}])
;; => ({:a 1 :b 2} {:a 3})
```

#### 2. Structural Editing

**Scenario: Add a docstring to a function if it's missing.**

We need to match `defn` forms that *don't* have a string as the third element.

```clojure
(def code "(defn my-fn [x] x)")

(query/$ code
         ;; Match defn where 3rd element is a vector (args), not a string
         '[(defn symbol? ^:% vector? & _)]

         ;; Transformation function: insert docstring before args
         (fn [zloc]
           (std.block.navigate/insert-left zloc "Added docstring")))
;; => "(defn my-fn \"Added docstring\" [x] x)"
```

#### 3. Context-Aware Modification

**Scenario: Rename a variable only within a specific binding scope.**

This often requires a multi-step approach or using `|` to position the cursor exactly where the modification should happen.

```clojure
(def code "(let [x 1] (+ x 1))")

(query/$ code
         ;; Match `let` block, then find `x` inside the vector, position cursor (|) on it
         '[(let [| x _] & _)]
         (fn [zloc]
           (std.block.navigate/set-value zloc 'y)))
;; => "(let [y 1] (+ x 1))"
;; Note: This only changed the binding name. A full refactor would need to traverse the body too.
```

#### 4. Advanced Pattern Matching Directives

- `^:%` **Capture**: Returns the matched element.
- `^:?` **Optional**: The element may or may not exist.
- `^:+` **One or more**: Repeat match.
- `^:*` **Zero or more**: Repeat match.
- `|` **Cursor**: Sets the "focus" of the match. The transformation function applies to *this* node, not the whole pattern root.
- `& _` **Rest**: Matches the rest of the collection (like `& args` in functions).

**Scenario: Extracting dependencies from `ns` form.**

```clojure
(def ns-form "(ns my.ns (:require [a.b :as ab] [c.d :refer [x]]))")

(query/$ ns-form
         ;; Find :require, then inside it, match vectors
         '[(ns _ (:require ^:%+ vector?))])
;; => ([a.b :as ab] [c.d :refer [x]])
```
