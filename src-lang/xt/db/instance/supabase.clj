(ns xt.db.instance.supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt supabase-capable?
  "checks that the db descriptor can execute compiled supabase queries"
  {:added "4.1"}
  [db]
  (return (xt/x:is-function? (xt/x:get-key db "execute"))))

(defn.xt compile-select-item
  "compiles a returning entry to Supabase select syntax"
  {:added "4.1"}
  [item]
  (if (xt/x:is-string? item)
    (return item)
    (do (var rel (xt/x:first item))
        (var entries (xt/x:second item))
        (return (xt/x:cat rel
                          "("
                          (xt/x:str-join ","
                                         (xt/x:arr-map entries -/compile-select-item))
                          ")")))))

(defn.xt compile-select
  "compiles a return vector to Supabase select syntax"
  {:added "4.1"}
  [entries]
  (return (xt/x:str-join ","
                         (xt/x:arr-map (or entries []) -/compile-select-item))))

(defn.xt compile-filters-into
  "compiles nested where params into Supabase filter descriptors"
  {:added "4.1"}
  [prefix obj out]
  (xt/for:object [[key value] obj]
    (var path (:? (xtd/not-empty? prefix)
                  (xt/x:cat prefix "." key)
                  key))
    (cond (and (xt/x:is-object? value)
               (not (xt/x:is-array? value)))
          (-/compile-filters-into path value out)

          (and (xt/x:is-array? value)
               (== "in" (xt/x:first value)))
          (xt/x:arr-push out {"path" path
                              "op" "in"
                              "value" (xtd/get-in value [1 0])})

          :else
          (xt/x:arr-push out {"path" path
                              "op" "eq"
                              "value" value})))
  (return out))

(defn.xt compile-query
  "compiles a query plan into a Supabase request description"
  {:added "4.1"}
  [query-plan]
  (var table (xt/x:first query-plan))
  (var second (xt/x:second query-plan))
  (var third  (xt/x:get-idx query-plan 2))
  (var where (:? (xt/x:is-object? second)
                 second
                 {}))
  (var returning (:? (xt/x:is-object? second)
                     third
                     second))
  (return {"table" table
           "select" (-/compile-select returning)
           "filters" (-/compile-filters-into "" where [])}))

(defn.xt execute-query
  "executes a compiled Supabase query via an injected executor"
  {:added "4.1"}
  [db compiled opts]
  (var execute-fn (or (xt/x:get-key db "execute")
                      (xt/x:get-key opts "execute")))
  (when (not (xt/x:is-function? execute-fn))
    (return [false {:status "error"
                    :tag "db/supabase-execute-not-provided"
                    :data compiled}]))
  (return (execute-fn compiled opts)))

(defn.xt map-supabase-error
  "maps an execution error into the local error contract"
  {:added "4.1"}
  [db error opts]
  (var map-fn (or (xt/x:get-key db "map_error")
                  (xt/x:get-key opts "map_error")))
  (if (xt/x:is-function? map-fn)
    (return (map-fn error opts))
    (return {:status "error"
             :tag "db/supabase-query-failed"
             :data error})))

(defn.xt supabase-pull-sync
  "compiles a query tree and executes it against a Supabase backend"
  {:added "4.1"}
  [db schema tree opts]
  (var compiled (-/compile-query tree))
  (var output (-/execute-query db compiled opts))
  (cond (and (xt/x:is-array? output)
             (== 2 (xt/x:len output))
             (xt/x:is-boolean? (xt/x:first output)))
        (do (var ok (xt/x:first output))
            (var result (xt/x:second output))
            (when (not ok)
              (return (-/map-supabase-error db result opts)))
            (when (== "error" (xt/x:get-key result "status"))
              (return (-/map-supabase-error db result opts)))
            (return result))

        (== "error" (xt/x:get-key output "status"))
        (return (-/map-supabase-error db output opts))

        :else
        (return output)))
