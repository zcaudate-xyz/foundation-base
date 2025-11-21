### `code.query.compile` Tutorial

**Module:** `code.query.compile`
**Source File:** `src/code/query/compile.clj`
**Test File:** `test/code/query/compile_test.clj`

The `code.query.compile` module is responsible for transforming a symbolic query path (a vector of Clojure forms and special keywords) into a structured map that can be used by the `code.query.match` module to find matching code structures. It handles cursor positions, metadata-driven flags, and special query operators like `:*` (multi-match) and `:n` (nth element).

#### Core Concepts

*   **Query Path:** A vector of Clojure forms and special symbols that describe a desired code structure.
*   **Cursor (`|`):** Marks the position of interest within the query path.
*   **Metadata Flags:** `^:+`, `^:-`, `^:?` on forms in the query path indicate insertion, deletion, or optional elements.
*   **Special Keywords:**
    *   `_`: Matches any single element.
    *   `:*`: Matches multiple elements (like `*` in regex).
    *   `:n` (e.g., `:1`, `:5`): Matches the nth element.
*   **Compiled Query Map:** The output of this module, a nested map describing the query for `code.query.match`.

#### Functions

##### `cursor-info`

`^{:refer code.query.compile/cursor-info :added "3.0"}`

Finds the information related to the cursor (`|`) within a sequence of selectors (query path). It returns a vector `[index type form]` where `index` is the position, `type` is `:cursor` or `:form`, and `form` is the element if `type` is `:form`.

```clojure
(cursor-info '[(defn ^:?& _ | & _)])
;; => '[0 :form (defn _ | & _)]

(cursor-info (expand-all-metas '[(defn ^:?& _ | & _)]))
;; => '[0 :form (defn _ | & _)]

(cursor-info '[defn if])
;; => [nil :cursor]

(cursor-info '[defn | if])
;; => [1 :cursor]
```

##### `expand-all-metas`

`^{:refer code.query.compile/expand-all-metas :added "3.0"}`

Converts shorthand metadata (e.g., `^:%?`) on forms within the query path into a map-based metadata (e.g., `{:? true, :% true}`).

```clojure
(meta (expand-all-metas '^:%? sym?))
;; => {:? true, :% true}

(-> (expand-all-metas '(^:%+ + 1 2))
    first meta)
;; => {:+ true, :% true}
```

##### `split-path`

`^{:refer code.query.compile/split-path :added "3.0"}`

Splits the query path into two parts: `up` (elements before the cursor/form) and `down` (elements from the cursor/form onwards).

```clojure
(split-path '[defn | if try] [1 :cursor])
;; => '{:up (defn), :down [if try]}

(split-path '[defn if try] [nil :cursor])
;; => '{:up [], :down [defn if try]}
```

##### `process-special`

`^{:refer code.query.compile/process-special :added "3.0"}`

Converts special keywords (`:*`, `:n`) in the query path into a map representation (e.g., `{:type :multi}`, `{:type :nth, :step 1}`).

```clojure
(process-special :*)
;; => {:type :multi}

(process-special :1)
;; => {:type :nth, :step 1}

(process-special :5)
;; => {:type :nth, :step 5}
```

##### `process-path`

`^{:refer code.query.compile/process-path :added "3.0"}`

Converts a raw query path (vector of forms and special keywords) into a more structured sequence of maps, where each map describes an element or a special operation.

```clojure
(process-path '[defn if try])
;; => '[{:type :step, :element defn}
;;      {:type :step, :element if}
;;      {:type :step, :element try}]

(process-path '[defn :* try :3 if])
;; => '[{:type :step, :element defn}
;;      {:element try, :type :multi}
;;      {:element if, :type :nth, :step 3}]
```

##### `compile-section-base`

`^{:refer code.query.compile/compile-section-base :added "3.0"}`

Compiles a single element section of the query path into its base matching criteria (e.g., `:form`, `:pattern`, `:is`).

```clojure
(compile-section-base '{:element defn})
;; => '{:form defn}

(compile-section-base '{:element (if & _)}
;; => '{:pattern (if & _)}

(compile-section-base '{:element _})
;; => {:is code.query.common/any}
```

##### `compile-section`

`^{:refer code.query.compile/compile-section :added "3.0"}`

Compiles a query section based on the traversal direction (`:up` or `:down`), previous context, and element details. It translates special types (`:multi`, `:nth`) into corresponding matching strategies (`:contains`, `:nth-ancestor`).

```clojure
(compile-section :up nil '{:element if, :type :nth, :step 3})
;; => '{:nth-ancestor [3 {:form if}]}

(compile-section :down nil '{:element if, :type :multi})
;; => '{:contains {:form if}}
```

##### `compile-submap`

`^{:refer code.query.compile/compile-submap :added "3.0"}`

Compiles a nested sub-query map based on the traversal direction and a processed path. It builds up the `:child` or `:parent` relationships in the query map.

```clojure
(compile-submap :down (process-path '[if try]))
;; => '{:child {:child {:form if}, :form try}}

(compile-submap :up (process-path '[defn if]))
;; => '{:parent {:parent {:form defn}, :form if}}
```

##### `prepare`

`^{:refer code.query.compile/prepare :added "3.0"}`

The main function for compiling a query. It takes a raw query path, expands metadata, splits the path, processes special elements, and returns a compiled query map along with cursor information.

```clojure
(prepare '[defn if])
;; => '[{:child {:form if}, :form defn} [nil :cursor]]

(prepare '[defn | if])
;; => '[{:parent {:form defn}, :form if} [1 :cursor]]
```