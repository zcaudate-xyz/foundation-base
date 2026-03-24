# xt.db Specific Refactorings with Code Examples

This document provides concrete code changes for the highest-priority improvements identified in the analysis.

---

## Refactoring 1: Eliminate View Duplication (P0)

### Problem
`sql_view.clj` (234 lines) and `cache_view.clj` (114 lines) share ~80% identical code. Only difference is the final query execution.

### Current Structure

**sql_view.clj**:
```clojure
(defn.xt query-select
  [schema entry args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (-/query-fill-clause entry access-id))
  (var itree  (-/tree-select schema entry clause opts))
  (var qtree  (-/query-fill-input itree args (k/arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))
```

**cache_view.clj**:
```clojure
(defn.xt query-select
  [schema entry args]
  (var #{input} entry)
  (var itree  (-/tree-select schema entry))
  (return (-/query-fill-input itree args (k/arr-clone input) false)))
```

### Proposed Solution

Create `xt.db.view.core` with shared logic, then backend-specific wrappers.

#### Step 1: Create `src/xt/db/view/core.clj`

```clojure
(ns xt.db.view.core
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-scope :as base-scope]
             [xt.db.sql-graph :as sql-graph]
             [xt.db.cache-pull :as cache-pull]]})

;; ============================================
;; SHARED TREE BUILDING
;; ============================================

(defn.xt tree-base
  "Creates a tree structure for query execution.
   Backend-agnostic - returns tree structure."
  {:added "4.0"}
  [schema table-name sel-query clause returning custom-query]
  (var tarr (base-scope/merge-queries sel-query (or custom-query [])))
  (var tree (k/arr-append [table-name] (or tarr [])))
  (when returning
    (x:arr-push tree returning))
  (return tree))

(defn.xt tree-select
  "Builds tree for select operation (gets IDs)."
  {:added "4.0"}
  [schema entry clause opts]
  (var #{view control} entry)
  (var #{table query} view)
  (return (-/tree-base schema table query clause ["id"] [])))

(defn.xt tree-return
  "Builds tree for return operation (gets full data)."
  {:added "4.0"}
  [schema entry sel-query clause opts]
  (var #{view} entry)
  (var #{table query} view)
  (return (-/tree-base schema table sel-query clause query opts)))

(defn.xt tree-combined
  "Builds tree for combined select+return operation."
  {:added "4.0"}
  [schema sel-entry ret-entry ret-omit clause opts]
  (var #{control} sel-entry)
  (var sel-table   (k/get-path sel-entry ["view" "table"]))
  (var ret-table   (k/get-path ret-entry ["view" "table"]))
  (var sel-query   (or (k/get-path sel-entry ["view" "query"]) {}))
  (var ret-query   (or (k/get-path ret-entry ["view" "query"]) {}))
  (return (-/tree-base schema sel-table sel-query clause
                       (k/arr-append (k/arr-clone ret-query)
                                     (-/tree-control-array control))
                       opts)))

;; ============================================
;; SHARED INPUT PROCESSING
;; ============================================

(defn.xt query-fill-clause
  "Fills the clause with access-id for row-level security."
  {:added "4.0"}
  [entry access-id]
  (if (k/nil? access-id)
    (return {})
    (var clause (or (k/get-in entry ["view" "access" "query" "clause"]) {}))
    (return (k/walk clause
                    (fn [x] (return (:? (== x "{{<%>}}") access-id x)))
                    k/identity)))

(defn.xt query-fill-input
  "Substitutes template parameters in query tree."
  {:added "4.0"}
  [tree args input-spec drop-first]
  (var arg-map {})
  (when drop-first
    (x:arr-pop-first input-spec))
  (when (== 0 (k/len input-spec))
    (return tree))
  (k/for:array [[i e] input-spec]
    (:= (. arg-map [(k/cat "{{" (. e ["symbol"]) "}}")]) (. args [i])))
  (var out (k/walk tree
                   k/identity
                   (fn [x]
                     (return (:? (and (k/is-string? x) (k/has-key? arg-map x))
                                 (k/get-key arg-map x)
                                 x)))))
  (return out))

(defn.xt query-access-check
  "Checks if select access grants return access."
  {:added "4.0"}
  [sel-access ret-access]
  (cond (k/nil? (k/get-key sel-access "symbol"))
        (return false)
        (== (k/get-key sel-access "symbol")
            (k/get-key ret-access "symbol"))
        (return true)
        (< 0 (k/len (k/obj-intersection
                     (k/get-key sel-access "roles")
                     (k/get-key ret-access "roles"))))
        (return true)
        :else
        (return false)))

;; ============================================
;; BACKEND ABSTRACTION
;; ============================================

(def.xt VIEW_EXECUTE
  {:sql   (fn [schema tree opts]
            (return (sql-graph/select-return schema tree 0 opts)))
   :cache (fn [schema tree opts rows]
            (return (cache-pull/pull rows schema tree opts)))})

(defn.xt execute-query
  "Executes a query tree using the specified backend.
   Backend: :sql or :cache"
  {:added "4.0"}
  [backend schema tree opts]
  (var exec-fn (k/get-key -/VIEW_EXECUTE backend))
  (when (k/nil? exec-fn)
    (k/err (k/cat "Invalid backend: " backend)))
  (return (exec-fn schema tree opts)))
```

#### Step 2: Rewrite `sql_view.clj` as thin wrapper

```clojure
(ns xt.db.sql-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.view.core :as view]
             [xt.db.sql-util :as sql-util]
             [xt.db.base-scope :as base-scope]]})

(defn.xt tree-control-array
  "Creates a control array from control map."
  {:added "4.0"}
  [control]
  (when (k/is-empty? control)
    (return []))
  (var out [])
  (var #{order-by order-sort limit offset} control)
  (when (k/arr? order-by)
    (x:arr-push out (sql-util/ORDER-BY order-by)))
  (when order-sort
    (x:arr-push out (sql-util/ORDER-SORT order-sort)))
  (when (k/is-number? limit)
    (x:arr-push out (sql-util/LIMIT limit)))
  (when (k/is-number? offset)
    (x:arr-push out (sql-util/OFFSET offset)))
  (return out))

;; ============================================
;; QUERY FUNCTIONS - SQL BACKEND
;; ============================================

(defn.xt query-select
  "Provides a view select query (SQL backend)."
  {:added "4.0"}
  [schema entry args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (view/query-fill-clause entry access-id))
  (var itree  (view/tree-select schema entry clause opts))
  (var qtree  (view/query-fill-input itree args (k/arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (view/execute-query :sql schema qtree opts))))

(defn.xt query-count
  "Provides the count statement (SQL backend)."
  {:added "4.0"}
  [schema entry args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (view/query-fill-clause entry access-id))
  (var itree  (view/tree-count schema entry clause opts))
  (var qtree  (view/query-fill-input itree args (k/arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (view/execute-query :sql schema qtree opts))))

(defn.xt query-return
  "Provides a view return query (SQL backend)."
  {:added "4.0"}
  [schema entry id args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (view/query-fill-clause entry access-id))
  (var itree (view/tree-return schema entry {:id id} clause opts))
  (var qtree (view/query-fill-input itree args (k/arr-clone input) true))
  (if as-tree
    (return qtree)
    (return (view/execute-query :sql schema qtree opts))))

(defn.xt query-return-bulk
  "Creates a bulk return statement (SQL backend)."
  {:added "4.0"}
  [schema entry ids args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (view/query-fill-clause entry access-id))
  (var itree  (view/tree-return schema
                               entry
                               {:id ["in" [ids]]}
                               clause
                               opts))
  (var qtree (view/query-fill-input itree args (k/arr-clone input) true))
  (if as-tree
    (return qtree)
    (return (view/execute-query :sql schema qtree opts))))

(defn.xt query-combined
  "Provides a view combine query (SQL backend)."
  {:added "4.0"}
  [schema sel-entry sel-args ret-entry ret-args ret-omit opts as-tree]
  (var #{access-id} opts)
  (var sel-input  (k/get-key sel-entry "input"))
  (var ret-input  (k/get-key ret-entry "input"))
  (var sel-access (k/get-path sel-entry ["view" "access"]))
  (var ret-access (k/get-path ret-entry ["view" "access"]))
  (var sel-clause [])
  (var ret-clause [])
  (when (not= nil (k/get-key sel-access "symbol"))
    (:= sel-clause (view/query-fill-clause sel-entry access-id)))
  (when (and (not= nil (k/get-key ret-access "symbol"))
             (not (view/query-access-check sel-access ret-access)))
    (:= ret-clause (view/query-fill-clause ret-entry access-id)))
  (var all-clause (base-scope/merge-queries sel-clause ret-clause))
  (var itree   (view/tree-combined schema
                                   sel-entry
                                   ret-entry
                                   ret-omit
                                   all-clause
                                   opts))
  (var qtree (view/query-fill-input itree
                                    (-> (k/arr-clone ret-args)
                                        (k/arr-append sel-args))
                                    (-> (k/arr-clone ret-input)
                                        (k/arr-append sel-input))
                                    true))
  (if as-tree
    (return qtree)
    (return (view/execute-query :sql schema qtree opts))))

;; ============================================
;; TREE HELPERS (SQL-specific)
;; ============================================

(defn.xt tree-count
  "Provides a view count query (SQL backend)."
  {:added "4.0"}
  [schema entry clause opts]
  (var #{view control} entry)
  (var #{table query} view)
  (return (view/tree-base schema table query clause
                          [{"::" "sql/count"}
                           (k/unpack (-/tree-control-array control))]
                          opts)))
```

#### Step 3: Rewrite `cache_view.clj` as thin wrapper

```clojure
(ns xt.db.cache-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.view.core :as view]
             [xt.db.base-scope :as base-scope]]})

;; ============================================
;; QUERY FUNCTIONS - CACHE BACKEND
;; ============================================

(defn.xt query-select
  "Tree for the query-select (cache backend)."
  {:added "4.0"}
  [schema entry args]
  (var #{input} entry)
  (var itree  (view/tree-select schema entry {} nil))
  (return (view/query-fill-input itree args (k/arr-clone input) false)))

(defn.xt query-return
  "Tree for the query-return (cache backend)."
  {:added "4.0"}
  [schema entry id args]
  (var #{input} entry)
  (var itree (view/tree-return schema entry {:id id} {} nil))
  (return (view/query-fill-input itree args (k/arr-clone input) true)))

(defn.xt query-return-bulk
  "Tree for query-return (cache backend)."
  {:added "4.0"}
  [schema entry ids args]
  (var #{input} entry)
  (var itree  (view/tree-return schema
                               entry
                               {:id ["in" [ids]]}
                               {}
                               nil))
  (return (view/query-fill-input itree args (k/arr-clone input) true)))

(defn.xt query-combined
  "Tree for query combined (cache backend)."
  {:added "4.0"}
  [schema sel-entry sel-args ret-entry ret-args ret-omit]
  (var sel-input  (k/get-key sel-entry "input"))
  (var ret-input  (k/get-key ret-entry "input"))
  (var itree   (view/tree-combined schema
                                   sel-entry
                                   ret-entry
                                   ret-omit))
  (return (view/query-fill-input itree
                                (-> (k/arr-clone ret-args)
                                    (k/arr-append sel-args))
                                (-> (k/arr-clone ret-input)
                                    (k/arr-append sel-input))
                                true)))
```

### Benefits
- **~400 lines saved** (eliminate duplication)
- Single source of truth for view logic
- Easier to add new query types
- Backend-specific code isolated to `execute-query` and `tree-control-array`

### Migration Strategy
1. Create `view/core.clj` with all shared functions
2. Update `sql-view` to use core (keep tests passing)
3. Update `cache-view` to use core (keep tests passing)
4. Remove duplicate code from both
5. Run full test suite

---

## Refactoring 2: Split sql_util.clj (P0)

### Current Structure (497 lines)

```
sql_util.clj contains:
├── Operators & type maps (lines 10-43)
├── SQLite-specific helpers (lines 45-67)
├── Encoding functions (lines 69-310)
│   ├── encode-bool, encode-number
│   ├── encode-operator, encode-json, encode-value
│   ├── encode-sql-arg, encode-sql-column
│   ├── encode-sql-tuple, encode-sql-table
│   ├── encode-sql-cast, encode-sql-keyword
│   ├── encode-sql-fn, encode-sql-select
│   └── encode-sql (dispatcher)
├── Loop function (lines 254-266)
├── Query segment encoding (lines 268-340)
├── Helper keywords (LIMIT, OFFSET, ORDER-BY)
└── Format functions (lines 379-779)
    ├── default-quote-fn
    ├── default-return-format-fn
    ├── default-table-fn
    ├── postgres-wrapper-fn, postgres-opts
    ├── sqlite-return-format-fn, sqlite-to-boolean
    └── sqlite-opts
```

### Proposed Structure

```
src/xt/db/
├── sql_util.clj              (facade - requires all submodules)
├── sql_encode/
│   ├── core.clj              (main dispatcher)
│   ├── value.clj             (encode-value, encode-bool, encode-number)
│   ├── column.clj            (encode-sql-column, encode-sql-tuple)
│   ├── function.clj          (encode-sql-fn, encode-sql-select)
│   └── cast.clj              (encode-sql-cast, encode-sql-keyword)
│
├── sql_types/
│   ├── core.clj              (OPERATORS, INFIX)
│   ├── postgres.clj          (PG map)
│   └── sqlite.clj            (SQLITE, SQLITE_FN maps)
│
└── sql_helpers/
    ├── format.clj            (format functions)
    ├── limit_offset.clj      (LIMIT, OFFSET, ORDER-BY)
    └── opts.clj              (postgres-opts, sqlite-opts)
```

### Implementation Example

#### 1. `src/xt/db/sql_encode/value.clj`

```clojure
(ns xt.db.sql-encode.value
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(l/script :lua
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt encode-bool
  "Encodes a boolean to SQL."
  {:added "4.0"}
  [b]
  (cond (== b true) (return "TRUE")
        (== b false) (return "FALSE")
        :else (k/err "Not Valid")))

(defn.lua encode-number
  "Encodes a number (for Lua dates)."
  {:added "4.0"}
  [v]
  (var '[rv fv] (math.modf v))
  (if (== fv 0)
    (return (k/cat "'" (string.format "%.f" v) "'"))
    (return (k/cat "'" (k/to-string v) "'"))))

(defn.xt encode-number
  "Encodes a number (for XT)."
  {:added "4.0"}
  [v]
  (return (k/cat "'" (k/to-string v) "'")))

(defn.xt encode-json
  "Encodes a JSON value."
  {:added "4.0"}
  [v]
  (return (k/cat "'" (k/replace (k/json-encode v) "'" "''") "'")))

(defn.xt encode-value
  "Encodes a value to SQL."
  {:added "4.0"}
  [v]
  (cond (k/nil? v) (return "NULL")
        (k/is-string? v) (return (k/cat "'" (k/replace v "'" "''") "'"))
        (k/is-boolean? v) (return (-/encode-bool v))
        (or (k/arr? v) (k/obj? v)) (return (-/encode-json v))
        (k/is-number? v) (return (-/encode-number v))
        :else (return (k/cat "'" (k/to-string v) "'"))))
```

#### 2. `src/xt/db/sql_encode/function.clj`

```clojure
(ns xt.db.sql-encode.function
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.sql-encode.value :as value]
             [xt.db.sql_util :as ut]]})  ; For INFIX, SQLITE_FN

(defn.xt encode-sql-fn
  "Encodes an SQL function."
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{name args} v)
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var fargs (k/arr-map args arg-fn))
  (cond (k/has-key? -/INFIX name)
        (return (k/cat "(" (k/arr-join fargs (k/cat " " name " ")) ")"))
        :else
        (do (var lu (k/get-path opts ["values" "replace"]))
            (var fspec (k/get-key lu name))
            (cond (k/nil? fspec)
                  (return (k/cat name "(" (k/arr-join fargs ", ") ")"))
                  (== "alias" (k/get-key fspec "type"))
                  (return (k/cat (k/get-key fspec "name") "(" (k/arr-join fargs ", ") ")"))
                  (== "macro" (k/get-key fspec "type"))
                  (return ((k/get-key fspec "fn") (k/unpack fargs)))
                  :else
                  (k/err (k/cat "Invalid Spec Type - " (k/get-key fspec "type")))))))

(defn.xt encode-sql-select
  "Encodes an SQL select statement."
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{args} v)
  (var #{querystr-fn} opts)
  (var arg-fn (fn [arg]
                (cond (and (k/obj? arg) (not (k/has-key? arg "::")))
                      (return (querystr-fn arg "" opts))
                      :else
                      (return (loop-fn arg column-fn opts loop-fn)))))
  (var fargs (k/arr-map args arg-fn))
  (return (k/cat "(SELECT " (k/arr-join fargs " " ")")))
```

#### 3. `src/xt/db/sql_encode/core.clj` (dispatcher)

```clojure
(ns xt.db.sql-encode.core
  (:require [std.lang :as l]
             [xt.db.sql-encode.value :as value]
             [xt.db.sql-encode.column :as column]
             [xt.db.sql-encode.function :as function]
             [xt.db.sql-encode.cast :as cast]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(def.xt ENCODE_SQL
  {"sql/arg"      column/encode-sql-arg
   "sql/column"   column/encode-sql-column
   "sql/tuple"    column/encode-sql-tuple
   "sql/defenum"  column/encode-sql-table
   "sql/deftype"  column/encode-sql-table
   "sql/cast"     cast/encode-sql-cast
   "sql/fn"       function/encode-sql-fn
   "sql/keyword"  cast/encode-sql-keyword
   "sql/select"   function/encode-sql-select})

(defn.xt encode-sql
  "Encodes an SQL value (dispatcher)."
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var tcls (k/get-key v "::"))
  (var f (k/get-key -/ENCODE_SQL tcls))
  (when (k/nil? f)
    (k/err (k/cat "Unsupported Type - " tcls)))
  (return (f v column-fn opts loop-fn)))

(defn.xt encode-loop-fn
  "Loop function to encode."
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (cond (and (k/obj? v) (k/has-key? v "::"))
        (return (-/encode-sql v column-fn opts loop-fn))
        (k/is-string? v) (return v)
        :else (return (value/encode-value v))))
```

#### 4. Update `sql_util.clj` to be a facade

```clojure
(ns xt.db.sql-util
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.sql-encode.core :as encode]
             [xt.db.sql-types.core :as types]
             [xt.db.sql-helpers.format :as format]
             [xt.db.sql-helpers.limit_offset :as lo]
             [xt.db.sql-helpers.opts :as opts]]})

;; Re-export commonly used functions for backward compatibility
(def.xt encode-value encode/encode-value)
(def.xt encode-sql encode/encode-sql)
(def.xt encode-query-segment format/encode-query-segment)
(def.xt encode-query-string format/encode-query-string)
(def.xt LIMIT lo/LIMIT)
(def.xt OFFSET lo/OFFSET)
(def.xt ORDER-BY lo/ORDER-BY)
(def.xt default-quote-fn format/default-quote-fn)
(def.xt default-return-format-fn format/default-return-format-fn)
(def.xt postgres-opts opts/postgres-opts)
(def.xt sqlite-opts opts/sqlite-opts)

;; Keep type maps for backward compatibility
(def.xt OPERATORS types/OPERATORS)
(def.xt INFIX types/INFIX)
(def.xt PG types/PG)
(def.xt SQLITE types/SQLITE)
```

### Benefits
- **Separation of concerns**: Each module has single responsibility
- **Easier testing**: Can test encoding in isolation
- **Reduced merge conflicts**: Multiple developers can work on different modules
- **Clear dependencies**: Explicit requires show what depends on what

### Migration Strategy
1. Create new directories: `sql_encode/`, `sql_types/`, `sql_helpers/`
2. Move functions one category at a time, update tests
3. Update `sql_util.clj` to require and re-export
4. Run full test suite after each move
5. Update all `:require` clauses in other files to use new modules (or keep re-export for compatibility)

---

## Refactoring 3: Standardize Error Handling (P1)

### Current Problems

1. **Mixed return types**:
```clojure
;; Throws on error
(base-schema/table-columns schema "MissingTable")  ; k/err

;; Returns tuple
(check-args-type args targs)  ; => [true] or [false error-map]

;; Returns nil
(k/get-key rows table-key)  ; => nil if missing
```

2. **Inconsistent error messages**: Some use `k/err`, some return maps, some silent.

3. **No error context**: When error occurs, hard to trace which input caused it.

### Proposed Convention

**For validation errors** (user input, schema issues):
- Return `[:error {:tag "error-tag" :data {...}}]`
- Never throw - let caller decide

**For exceptional conditions** (programmer errors, invariant violations):
- Use `k/err` with descriptive message including function name and expected values

**For optional lookups**:
- Return `[:not-found]` or `[:ok value]` - explicit about missing

### Implementation

#### 1. Create `src/xt/db/error.clj`

```clojure
(ns xt.db.error
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(def.xt ERROR_TAGS
  {:arg-typecheck-failed   "net/arg-typecheck-failed"
   :args-not-same-length   "net/args-not-same-length"
   :table-not-found        "db/table-not-found"
   :column-not-found       "db/column-not-found"
   :invalid-schema         "db/invalid-schema"
   :backend-not-supported  "db/backend-not-supported"
   :missing-required-field "db/missing-required-field"})

(defn.xt error-response
  "Creates a standardized error response."
  {:added "4.0"}
  [tag data]
  (return {:status "error"
           :tag (k/get-key -/ERROR_TAGS tag)
           :data data}))

(defn.xt ok-response
  "Wraps a successful result."
  {:added "4.0"}
  [data]
  (return {:status "ok" :data data}))
```

#### 2. Update `base_schema.clj` to use error responses

```clojure
(defn.xt table-columns
  "Gets the table columns."
  {:added "4.0"}
  [schema table-name]
  (var table (k/get-key schema table-name))
  (when (k/nil? table)
    (k/err (k/cat "Table not found in schema: " table-name)))
  (return (k/arr-map (-/table-entries schema table-name)
                     -/get-ident-id)))
```

Change to:

```clojure
(defn.xt table-columns
  "Gets the table columns."
  {:added "4.0"}
  [schema table-name]
  (var table (k/get-key schema table-name))
  (if (k/nil? table)
    (return [:error {:tag :table-not-found
                     :table table-name
                     :available-tables (k/obj-keys schema)}])
    (return [:ok (k/arr-map (-/table-entries schema table-name)
                            -/get-ident-id)])))
```

#### 3. Update callers to handle error tuples

In `sql_table.clj`:

```clojure
(defn.xt table-insert-single
  [schema table-name m opts]
  (var cols-result (base-schema/table-columns schema table-name))
  (when (== (k/first cols-result) :error)
    (return cols-result))  ; propagate error
  (var cols (k/second cols-result))
  ;; ... rest of function
)
```

### Benefits
- **Predictable error handling**: Callers know what to expect
- **Better error messages**: Include context (table name, available tables, etc.)
- **Composability**: Errors can be propagated or handled at any level

### Migration Strategy
1. Define error conventions in `ERROR_HANDLING.md`
2. Create `error.clj` with tags and helper functions
3. Update functions one module at a time, starting with `base_schema`
4. Update all callers to handle `[:error ...]` returns
5. Add tests for error paths

---

## Refactoring 4: Platform Abstraction (P1)

### Current Problem

Platform-specific code scattered with `defn.lua` and `defn.js`:

```clojure
;; sql_util.clj
(defn.lua encode-number
  [v]
  (var '[rv fv] (math.modf v))
  (if (== fv 0)
    (return (k/cat "'" (string.format "%.f" v) "'"))
    (return (k/cat "'" (k/to-string v) "'"))))

(defn.xt encode-number
  [v]
  (return (k/cat "'" (k/to-string v) "'")))
```

And in `cache_pull.clj`:

```clojure
(defn.js check-like-clause
  [x expr]
  (return 
   (. (new RegExp
           (+ "^"
              (-> expr
                  (j/replaceAll "_" ".")
                  (j/replaceAll "%" ".*"))
              "$"))
      (test x))))

(defn.xt check-like-clause
  [x expr]
  (return true))  ; BROKEN - always returns true!
```

### Proposed Solution

Use multi-methods or protocol-based dispatch. Since this is XT language, we can use conditional compilation or separate modules.

#### Option A: Separate Platform Modules

```
src/xt/db/
├── platform/
│   ├── lua/
│   │   ├── encode.clj
│   │   └── regex.clj
│   ├── js/
│   │   ├── encode.clj
│   │   └── regex.clj
│   └── xt/
│       ├── encode.clj
│       └── regex.clj
```

Each platform module implements the same functions. Main code dispatches based on `*platform*` var.

#### Option B: Conditional Compilation (Current Approach, but Cleaner)

Keep `defn.lua` and `defn.js` but ensure feature parity and document differences.

**Recommendation**: Option B with improvements:

1. **Create platform feature matrix** in `platform_features.md`:
```
Feature                Lua    JS    XT
encode-number          ✓      ✓    ✓  (Lua has fractional support)
check-like-clause      ✓      ✓    ✗  (XT stub - needs implementation)
regex-support          ✓      ✓    ✗  (XT needs polyfill)
```

2. **Add runtime assertions** to catch missing implementations:

```clojure
(defn.xt check-like-clause
  [x expr]
  (k/err "check-like-clause not implemented in XT runtime"))
```

3. **Document platform differences** in each function's docstring:

```clojure
(defn.xt encode-number
  "Encodes a number to SQL string.
   
   Platform differences:
   - Lua: Preserves fractional seconds using string.format
   - JS/XT: Converts to string directly (may lose precision)"
  {:added "4.0"}
  [v] ...)
```

### Benefits
- Clear what's supported on each platform
- Prevent silent failures (like `check-like-clause` returning `true`)
- Easier to add new platform implementations

---

## Quick Wins (Can Apply Immediately)

### 1. Add Docstrings to Top 20 Functions

Example for `flatten-obj`:

```clojure
(defn.xt flatten-obj
  "Flattens a nested object graph into denormalized tables.
   
   Args:
   - schema: The schema map defining table structures
   - table-name: Root table name (string)
   - obj: The object to flatten (map or vector of maps)
   - parent: Parent context for nested links (map, optional)
   - acc: Accumulator for results (map, optional)
   
   Returns:
   - Accumulator map with structure:
     {table-name {id {:data {...}
                      :ref-links {...}
                      :rev-links {...}}}}
   
   Example:
   (flatten-obj schema \"UserAccount\" user-profile {})
   => {\"UserAccount\" {\"uid123\" {:data {...}
                                    :ref-links {\"profile\" {\"pid456\" true}}
                                    :rev-links {}}}}"
  {:added "4.0"}
  [schema table-name obj parent acc]
  ...)
```

### 2. Remove Commented Code

In `base_check.clj`, remove lines 92-113 commented out code. If needed, use version control history.

### 3. Standardize Indent Constants

In `sql_graph.clj`, replace magic `2` with:

```clojure
(def.xt INDENT_STEP 2)
```

Then use `(+ -/INDENT_STEP indent)` instead of `(+ 2 indent)`.

---

## Testing Strategy for Refactorings

Each refactoring must pass:

1. **Unit tests**: Existing test suite (930+ tests)
2. **Integration tests**: End-to-end data flow
3. **Performance benchmarks**: Ensure no regression
4. **Cross-platform tests**: Lua, JS, XT runtimes

Add to `test/xt/db/refactoring_validation_test.clj`:

```clojure
(ns xt.db.refactoring-validation-test
  (:require [std.lang :as l]
            [xt.db :as db]
            [xt.db.sample-test :as sample])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db :as db]
             [xt.db.sample-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db :as db]
             [xt.db.sample-test :as sample]]})

(fact:global
  {:setup [(bootstrap-js)
           (bootstrap-lua)]}

  "Refactoring validation: view duplication eliminated"
  
  (!.js
   (let [sql-result  (xt.db.sql-view/query-select sample/Schema sample/UserView [] {} false)
         cache-result (xt.db.cache-view/query-select sample/Schema sample/UserView [])]
     (== (k/type sql-result) (k/type cache-result))))
  => true)
```

---

## Rollback Plan

For each refactoring:

1. **Branch strategy**: Create feature branch `refactor/xxx`
2. **Incremental commits**: Small, testable steps
3. **Keep tests green**: Never merge failing tests
4. **Tag before merge**: `git tag -a v4.0-refactor-views -m "View deduplication"`
5. **Rollback procedure**: `git revert <merge-commit>` if issues found

---

## Success Criteria

- [ ] `sql_view.clj` and `cache_view.clj` reduced to <50 lines each (wrappers only)
- [ ] `sql_util.clj` reduced to <100 lines (facade only)
- [ ] All functions have docstrings with examples
- [ ] Error handling consistent across all modules
- [ ] All tests pass on Lua, JS, and XT runtimes
- [ ] No performance regression (benchmark before/after)

---

## Estimated Timeline

| Refactoring | Days | Dependencies |
|-------------|------|--------------|
| View deduplication | 2-3 | None |
| Split sql_util | 1-2 | View deduplication (to test properly) |
| Error handling | 2-3 | View deduplication |
| Platform abstraction | 1-2 | Error handling |
| Documentation | 1 | Can do in parallel |
| **Total** | **7-11 days** | |

---

## Next Steps

1. Get approval on this plan
2. Start with **Refactoring 1 (View Deduplication)** - highest impact, lowest risk
3. Run full test suite after each step
4. Document any surprises or adjustments needed
