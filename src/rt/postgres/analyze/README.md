# gwdb.analyze - Static Analysis for deftype.pg and defn.pg

A Clojure library for performing static analysis on the PostgreSQL DSL forms
(`deftype.pg`, `defn.pg`, `defenum.pg`) used in the gwdb codebase.

## Overview

This library parses Clojure source files without requiring runtime evaluation
and extracts structural information from the PL/pgSQL DSL forms. It provides:

- **Form parsing** - Read `.clj` files and extract pg DSL forms
- **Type analysis** - Analyze `deftype.pg` columns, refs, enums, partitions
- **Function analysis** - Analyze `defn.pg` params, body refs, pg operations
- **Enum analysis** - Analyze `defenum.pg` values and naming
- **Cross-reference analysis** - Build dependency graphs, detect undefined refs
- **Validation** - Check naming conventions, missing fields, orphaned types
- **Reporting** - Generate human-readable schema summaries

## Namespace Structure

| Namespace | Purpose |
|-----------|---------|
| `rt.postgres.analyze` | Main API - unified interface for all analysis |
| `rt.postgres.analyze.parse` | File/form parsing using Clojure reader |
| `rt.postgres.analyze.deftype` | `deftype.pg` form analysis |
| `rt.postgres.analyze.defn` | `defn.pg` form analysis |
| `rt.postgres.analyze.defenum` | `defenum.pg` form analysis |
| `rt.postgres.analyze.refs` | Cross-reference and dependency analysis |
| `rt.postgres.analyze.report` | Formatting and validation reporting |

## Usage

### Analyze a single form

```clojure
(require '[rt.postgres.analyze :as analyze])

;; Analyze a deftype.pg form
(analyze/analyze-form
  '(deftype.pg User "User entity" {:added "0.1"}
     [:handle {:type :citext :scope :-/info :unique true}
      :bio    {:type :text}]))
;; => {:form-type :deftype, :name User, :columns [...], ...}

;; Analyze a defn.pg form
(analyze/analyze-form
  '(defn.pg create-user
     [:uuid i-user-id :jsonb m :jsonb o-op]
     (let [o-user (pg/t:insert -/User {:id i-user-id})]
       (return o-user))))
;; => {:form-type :defn, :name create-user, :params [...], :body-refs {...}, ...}
```

### Analyze a source string

```clojure
(def result (analyze/analyze-source source-code))

(:types result)      ;; => [{:name User, :columns [...], ...}]
(:functions result)  ;; => [{:name create-user, :params [...], ...}]
(:enums result)      ;; => [{:name EnumUserType, :values [...], ...}]
(:issues result)     ;; => [{:level :warning, :message "...", ...}]
(:stats result)      ;; => {:type-count 1, :fn-count 1, :enum-count 1}
```

### Analyze a directory

```clojure
(def result (analyze/analyze-directory "src/gwdb/core/system"))

;; Print human-readable report
(analyze/print-report result)

;; Access cross-reference data
(get-in result [:cross-refs :stats])
;; => {:type-count 38, :fn-count 52, :enum-count 8}

;; Find orphaned types
(get-in result [:cross-refs :orphaned-types])

;; Find undefined references  
(get-in result [:cross-refs :undefined-type-refs])
```

### Query helpers

```clojure
;; Find a specific type
(analyze/find-type result "User")

;; Find all types referencing User
(analyze/types-referencing result "User")

;; Find all functions using a type
(analyze/functions-using-type result "User")
```

### Validation

```clojure
;; Validate a single form
(analyze/validate-form
  '(deftype.pg user [:handle {:type :citext}]))
;; => [{:level :warning, :message "Type name 'user' should start with uppercase"}]

;; Get all issues from directory analysis
(analyze/all-issues result)
```

## What it analyzes

### deftype.pg

| Field | Description |
|-------|-------------|
| `:name` | Type symbol name |
| `:docstring` | Documentation string |
| `:entity` | Entity config from `:!` metadata (class, addons) |
| `:columns` | Column specs with type, scope, constraints |
| `:refs` | Referenced types (from `:ref` columns) |
| `:enums` | Referenced enums (from `:enum` columns) |
| `:params` | Additional params (partition-by, etc.) |

### defn.pg

| Field | Description |
|-------|-------------|
| `:name` | Function symbol name |
| `:language` | PL language (`:default`, `:sql`, `:js`, etc.) |
| `:return-type` | Return type from `:-` metadata |
| `:api-flags` | API exposure from `:api/flags` |
| `:props` | Properties like `[:security :definer]` |
| `:params` | Typed parameters with naming convention detection |
| `:body-refs` | Types, functions, and pg ops referenced in body |

### defenum.pg

| Field | Description |
|-------|-------------|
| `:name` | Enum symbol name |
| `:values` | Vector of enum values |
| `:value-types` | `:keyword`, `:string`, or `:mixed` |

## Running Tests

```bash
# With Babashka
bb -cp src:test -m clojure.test rt.postgres.analyze-test

# With Clojure CLI
clojure -M -m clojure.test rt.postgres.analyze-test
```
