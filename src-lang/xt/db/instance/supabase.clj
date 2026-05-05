(ns xt.db.instance.supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.pgrest :as pgrest]]})

(defn.xt thenable?
  "checks whether a value can be chained via .then"
  {:added "4.1"}
  [value]
  (return (and (xt/x:is-object? value)
               (xt/x:is-function? (xt/x:get-key value "then")))))

(defn.xt supabase-capable?
  "checks that the db descriptor can execute compiled supabase queries"
  {:added "4.1"}
  [db]
  (return (or (xt/x:is-function? (xt/x:get-key db "execute"))
              (pgrest/query-client? (xt/x:get-key db "supabase"))
              (pgrest/query-client? (xt/x:get-key db "client"))
              (pgrest/query-client? db))))

(def.xt compile-select-item pgrest/compile-select-item)

(def.xt compile-select pgrest/compile-select)

(def.xt compile-filters-into pgrest/compile-filters-into)

(def.xt compile-query pgrest/compile-query)

(defn.xt unwrap-query-output
  "unwraps execution output into rows or a mapped local error"
  {:added "4.1"}
  [db output opts]
  (cond (and (xt/x:is-array? output)
             (== 2 (xt/x:len output))
             (xt/x:is-boolean? (xt/x:first output)))
        (do (var ok (xt/x:first output))
            (var result (xt/x:second output))
            (when (not ok)
              (return (-/map-supabase-error db result opts)))
            (when (== "error" (xt/x:get-key result "status"))
              (return (-/map-supabase-error db result opts)))
            (when (and (xt/x:is-object? result)
                       (xt/x:not-nil? (xt/x:get-key result "error")))
              (return (-/map-supabase-error db
                                            (xt/x:get-key result "error")
                                            opts)))
            (when (and (xt/x:is-object? result)
                       (xt/x:has-key? result "data"))
              (return (xt/x:get-key result "data")))
            (return result))

        (and (xt/x:is-object? output)
             (xt/x:not-nil? (xt/x:get-key output "error")))
        (return (-/map-supabase-error db
                                      (xt/x:get-key output "error")
                                      opts))

        (== "error" (xt/x:get-key output "status"))
        (return (-/map-supabase-error db output opts))

        (and (xt/x:is-object? output)
             (xt/x:has-key? output "data"))
        (return (xt/x:get-key output "data"))

        :else
        (return output)))

(defn.xt execute-query
  "executes a compiled Supabase query via an injected executor"
  {:added "4.1"}
  [db compiled opts]
  (var execute-fn (or (xt/x:get-key db "execute")
                      (xt/x:get-key opts "execute")))
  (when (xt/x:is-function? execute-fn)
    (return (execute-fn compiled opts)))
  (var default-output (pgrest/execute-query-default db compiled opts))
  (when (xt/x:not-nil? default-output)
    (return default-output))
  (return [false {:status "error"
                  :tag "db/supabase-execute-not-provided"
                  :data compiled}]))

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
  (var compiled (pgrest/compile-query tree))
  (var output (-/execute-query db compiled opts))
  (if (-/thenable? output)
    (do (var chained
             (pgrest/invoke-method-1 output
                                     "then"
                                     (fn [result]
                                       (return (-/unwrap-query-output db result opts)))))
        (if (xt/x:is-function? (xt/x:get-key chained "catch"))
          (return
           (pgrest/invoke-method-1 chained
                                   "catch"
                                   (fn [err]
                                     (return (-/map-supabase-error db err opts)))))
          (return chained)))
    (return (-/unwrap-query-output db output opts))))
