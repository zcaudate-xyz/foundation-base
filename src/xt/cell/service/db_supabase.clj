(ns xt.cell.service.db-supabase
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.cell.service.db-query :as db-query]
             [xt.lang.common-spec :as xt]]
   :export  [MODULE]})

(defn.xt supabase-capable?
  "checks that the db descriptor can execute compiled supabase queries"
  {:added "4.0"}
  [db]
  (return (xt/x:is-function? (xt/x:get-key db "execute"))))

(defn.xt compile-select-item
  "compiles a returning entry to Supabase select syntax"
  {:added "4.0"}
  [item]
  (if (xt/x:is-string? item)
    (return item)
    (do (var rel (xt/x:first item))
        (var entries (xt/x:second item))
        (return (xt/x:cat rel
                       "("
                       (xt/x:join ","
                               (xt/x:arr-map entries -/compile-select-item))
                       ")")))))

(defn.xt compile-select
  "compiles a return vector to Supabase select syntax"
  {:added "4.0"}
  [entries]
  (return (xt/x:join ","
                  (xt/x:arr-map entries -/compile-select-item))))

(defn.xt compile-filters-into
  "compiles nested where params into Supabase filter descriptors"
  {:added "4.0"}
  [prefix obj out]
  (xt/for:object [[key value] obj]
    (var path (:? (xt/x:not-empty? prefix)
                  (xt/x:cat prefix "." key)
                  key))
    (cond (and (xt/is-object? value)
               (not (xt/is-array? value)))
          (-/compile-filters-into path value out)

          (and (xt/is-array? value)
               (== "in" (xt/x:first value)))
          (x:arr-push out {"path" path
                           "op" "in"
                           "value" (xt/x:get-in value [1 0])})

          :else
          (x:arr-push out {"path" path
                           "op" "eq"
                           "value" value})))
  (return out))

(defn.xt compile-query
  "compiles a query plan into a Supabase request description"
  {:added "4.0"}
  [db query-plan view-context]
  (var table (xt/x:first query-plan))
  (var second (xt/x:second query-plan))
  (var third  (xt/x:get-idx query-plan 2))
  (var where (:? (xt/is-object? second)
                 second
                 {}))
  (var returning (:? (xt/is-object? second)
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
  (var execute-fn (or (xt/x:get-key db "execute")
                      (xt/x:get-key view-context "execute")))
  (when (not (xt/x:is-function? execute-fn))
    (return [false {:status "error"
                    :tag "db/supabase-execute-not-provided"
                    :data compiled}]))
  (return (execute-fn compiled view-context)))

(defn.xt map-supabase-error
  "maps an execution error into the local error contract"
  {:added "4.0"}
  [db error view-context]
  (var map-fn (or (xt/x:get-key db "map_error")
                  (xt/x:get-key view-context "map_error")))
  (if (xt/x:is-function? map-fn)
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
  (when (== "error" (xt/x:get-key result "status"))
    (return [false (-/map-supabase-error db result view-context)]))
  (return [true result]))

(def.xt MODULE (!:module))
