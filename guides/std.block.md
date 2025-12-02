# `std.block` Guide

`std.block` allows you to treat source code as data structure ("blocks") that preserves formatting, whitespace, and comments. This is essential for building formatters, linters, and refactoring tools.

## Core Concepts

- **Block**: The atom of the system. Can be a token (symbol, keyword), a container (list, vector), or void (whitespace, comment).
- **Construct**: Functions to build blocks programmatically.
- **Layout**: Engine to render blocks back to string with specific formatting rules.

## Usage

### Scenarios

#### 1. Programmatic Code Generation

Instead of building code with lists and then pretty-printing (which loses control over exact formatting), you can build blocks.

```clojure
(require '[std.block :as block])

;; Create a `(+ 1 2)` block
(def b (block/container :list
         [(block/token '+)
          (block/space)
          (block/token 1)
          (block/space)
          (block/token 2)]))

(block/string b) ;; => "(+ 1 2)"
```

#### 2. Layout Customization

You can control how a block is printed by setting its layout spec.

```clojure
;; Create a vector that MUST be on one line
(def v (block/container :vector
         [(block/token 1) (block/space) (block/token 2)]))

;; Layout usually decides based on width, but we can force it?
;; Note: Layout logic is internal, but you can inspect the result of `layout`.

(block/string (block/layout v))
```

To implement custom formatting rules (like `cond` or `let` binding alignment), you would typically extend the `std.block.layout` multimethods or manipulate the block structure before layout.

#### 3. Parsing and Analysis

Distinguishing between code and "void" (whitespace/comments) is key for analysis tools.

```clojure
(def root (block/parse-string "(+ 1 1) ;; comment"))

(def children (block/children root))

;; Filter only meaningful code
(filter block/code? children)
;; => (#blk{:string "(+ 1 1)" ...})

;; Find comments
(filter block/comment? children)
;; => (#blk{:string ";; comment" ...})
```

#### 4. Manipulating the Block Tree

You can traverse and modify the block tree manually, although `code.query` provides a higher-level API for this.

```clojure
;; Replace all occurrences of 1 with 2 in a block
(defn replace-one [blk]
  (if (and (block/token? blk) (= (block/value blk) 1))
    (block/token 2)
    (if (block/container? blk)
      (update blk :children #(mapv replace-one %))
      blk)))
```

#### 5. Handling Uneval forms

`#_` (uneval) forms are tricky in standard Clojure readers as they disappear. `std.block` preserves them.

```clojure
(def b (block/parse-string "#_(+ 1 2)"))
(block/type b) ;; => :uneval (or similar modifier type)
```
