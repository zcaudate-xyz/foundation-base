(ns js.cell.service.db-supabase
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[js.cell.service.db-query :as db-query]
             [xt.lang.common-lib :as k]]
   :export  [MODULE]})

(defn.xt supabase-capable?
  "checks that the db descriptor can execute compiled supabase queries"
  {:added "4.0"}
  [db]
  (return (k/is-function? (k/get-key db "execute"))))

(defn.xt compile-select-item
  "compiles a returning entry to Supabase select syntax"
  {:added "4.0"}
  [item]
  (if (k/is-string? item)
    (return item)
    (do (var rel (k/first item))
        (var entries (k/second item))
        (return (k/cat rel
                       "("
                       (k/join ","
                               (k/arr-map entries -/compile-select-item))
                       ")")))))

(defn.xt compile-select
  "compiles a return vector to Supabase select syntax"
  {:added "4.0"}
  [entries]
  (return (k/join ","
                  (k/arr-map entries -/compile-select-item))))

(defn.xt compile-filters-into
  "compiles nested where params into Supabase filter descriptors"
  {:added "4.0"}
  [prefix obj out]
  (k/for:object [[key value] obj]
    (var path (:? (k/not-empty? prefix)
                  (k/cat prefix "." key)
                  key))
    (cond (and (k/is-object? value)
               (not (k/is-array? value)))
          (-/compile-filters-into path value out)

          (and (k/is-array? value)
               (== "in" (k/first value)))
          (x:arr-push out {"path" path
                           "op" "in"
                           "value" (k/get-in value [1 0])})

          :else
          (x:arr-push out {"path" path
                           "op" "eq"
                           "value" value})))
  (return out))

(defn.xt compile-query
  "compiles a query plan into a Supabase request description"
  {:added "4.0"}
  [db query-plan view-context]
  (var table (k/first query-plan))
  (var second (k/second query-plan))
  (var third  (k/get-idx query-plan 2))
  (var where (:? (k/is-object? second)
                 second
                 {}))
  (var returning (:? (k/is-object? second)
                     third
                     second))
  (return {"table" table
           "select" (-/compile-select returning)
           "filters" (-/compile-filters-into "" where [])}))

(defn.xt execute-query
  "executes a compiled Supabase query via an injected executor"
  {:added "4.0"}
  [db query-plan view-context]
  (var compiled (-/compile-query db query-plan view-context))
  (var execute-fn (or (k/get-key db "execute")
                      (k/get-key view-context "execute")))
  (when (not (k/is-function? execute-fn))
    (return [false {:status "error"
                    :tag "db/supabase-execute-not-provided"
                    :data compiled}]))
  (return (execute-fn compiled view-context)))

(defn.xt map-supabase-error
  "maps an execution error into the local error contract"
  {:added "4.0"}
  [db error view-context]
  (var map-fn (or (k/get-key db "map_error")
                  (k/get-key view-context "map_error")))
  (if (k/is-function? map-fn)
    (return (map-fn error view-context))
    (return {:status "error"
             :tag "db/supabase-query-failed"
             :data error})))

(defn.xt run-supabase-query
  "prepares, compiles, and executes a Supabase query"
  {:added "4.0"}
  [db query-spec view-context]
  (var [ok query-plan] (db-query/prepare-query db query-spec view-context))
  (when (not ok)
    (return [ok query-plan]))
  (var [e-ok result] (-/execute-query db query-plan view-context))
  (when (not e-ok)
    (return [false (-/map-supabase-error db result view-context)]))
  (when (== "error" (k/get-key result "status"))
    (return [false (-/map-supabase-error db result view-context)]))
  (return [true result]))

(def.xt MODULE (!:module))
