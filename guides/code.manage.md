# `code.manage` Guide

`code.manage` provides a suite of tasks for maintaining code quality, managing tests, and refactoring. It is typically invoked via `lein manage` or from the REPL.

## Core Concepts

- **Tasks**: Operations that run over a set of namespaces (e.g., `analyse`, `grep`).
- **Templates**: Preset configurations for tasks (e.g., `:code.transform`, `:code.locate`).
- **Selectors**: Arguments to target specific namespaces (e.g., vector of symbols `['my.ns]`).

## Usage

### Invocation

```bash
# From CLI
lein manage <task> <namespaces> <options>

# Example
lein manage analyse "['code.manage]" "{:print {:summary true}}"
```

```clojure
;; From REPL
(require '[code.manage :as manage])
(manage/analyse ['code.manage] {:print {:summary true}})
```

### Scenarios

#### 1. Codebase Cleanup Workflow

A typical cleanup session might involve identifying messy code and then standardizing it.

**Step A: Identify "Unclean" Code**
Find files with top-level comment blocks (often used for debugging/scratch) that shouldn't be committed.

```clojure
(manage/unclean ['my.project] {:print {:item true}})
```

**Step B: Remove Docstrings from Source**
If you prefer keeping docstrings in tests or external docs, you can purge them.

```clojure
(manage/purge ['my.project] {:write true})
```

**Step C: Standardize Namespace Declarations**
Ensure `ns` forms are formatted consistently (requires sorting, indentation).

```clojure
(manage/ns-format ['my.project] {:write true})
```

#### 2. Test Coverage & Management

`code.manage` integrates tightly with `code.test` to ensure coverage.

**Step A: Find Missing Tests**
List functions that have no corresponding `fact`.

```clojure
(manage/missing ['my.project])
```

**Step B: Scaffold New Tests**
Generate test files and stubs for the missing functions.

```clojure
(manage/scaffold ['my.project] {:write true})
```

**Step C: Identify "Orphaned" Tests**
Find tests that refer to non-existent functions (e.g., after a rename/delete).

```clojure
(manage/orphaned ['my.project])
```

#### 3. Large Scale Refactoring

Use `refactor-code` or `grep-replace` for bulk changes.

**Scenario: Renaming a function across the codebase**

If simple grep isn't enough (e.g., context sensitive), you can write a custom transform script. However, for simple string replacement:

```clojure
(manage/grep-replace ['my.project]
                     {:query "old-fn-name"
                      :replace "new-fn-name"
                      :write true})
```

**Scenario: Custom AST Modification**

You can use `refactor-code` with a custom edit function that operates on the zipper.

```clojure
(require '[code.query :as query]
         '[std.block.navigate :as edit])

(manage/refactor-code ['my.project]
  {:edits [(fn [zloc]
             ;; Use code.query to find/modify
             (query/modify zloc
                           '[defn old-name]
                           (fn [node]
                             (edit/set-value node 'new-name))))]
   :write true})
```

#### 4. Search and Analysis

**Scenario: Find all usages of a specific var**

Useful when checking impact before a change.

```clojure
(manage/find-usages ['my.project]
                    {:var 'my.project.core/my-func})
```

**Scenario: Grep with highlighting**

Quickly scan for a pattern.

```clojure
(manage/grep ['my.project]
             {:query "TODO"
              :highlight true})
```
