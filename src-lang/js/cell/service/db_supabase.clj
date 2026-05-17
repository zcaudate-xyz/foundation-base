(ns js.cell.service.db-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:export [MODULE]
   :require [[js.cell.service.db-query :as db-query]
              [xt.db.runtime :as xdb]
              [xt.lang.spec-base :as xt]
              [xt.db.text.pgrest :as pgrest]]})

(defn.xt normalize-db
  [db]
  (var out (xt/x:obj-assign {} db))
  (when (and (xt/x:nil? (xt/x:get-key out "base_url"))
             (xt/x:not-nil? (xt/x:get-key out "base-url")))
    (xt/x:set-key out "base_url" (xt/x:get-key out "base-url")))
  (when (and (xt/x:nil? (xt/x:get-key out "schema_name"))
             (xt/x:not-nil? (xt/x:get-key out "schema-name")))
    (xt/x:set-key out "schema_name" (xt/x:get-key out "schema-name")))
  (when (and (xt/x:nil? (xt/x:get-key out "schema_name"))
             (xt/x:not-nil? (xt/x:get-key out "schema")))
    (xt/x:set-key out "schema_name" (xt/x:get-key out "schema")))
  (when (and (xt/x:nil? (xt/x:get-key out "api_key"))
             (xt/x:not-nil? (xt/x:get-key out "api-key")))
    (xt/x:set-key out "api_key" (xt/x:get-key out "api-key")))
  (when (and (xt/x:nil? (xt/x:get-key out "auth_token"))
             (xt/x:not-nil? (xt/x:get-key out "auth-token")))
    (xt/x:set-key out "auth_token" (xt/x:get-key out "auth-token")))
  (return out))

(defn.xt supabase-capable?
  [db]
  (var db_input (-/normalize-db db))
  (return (or (xt/x:is-function? (xt/x:get-key db_input "execute"))
              (xt/x:not-nil? (xt/x:get-key db_input "client")))))

(def.xt compile-select-item pgrest/compile-select-item)

(def.xt compile-select pgrest/compile-select)

(def.xt compile-filters-into pgrest/compile-filters-into)

(defn.xt compile-query
  "compiles a query plan into a Supabase request description"
  {:added "4.0"}
  [db query-plan view-context]
  (return (pgrest/compile-query query-plan)))

(defn.xt execute-query
  "executes a compiled Supabase query via an injected executor"
  {:added "4.0"}
  [db query-plan view-context]
  (var compiled (-/compile-query db query-plan view-context))
  (var execute_fn (xt/x:get-key db "execute"))
  (when (xt/x:is-function? execute_fn)
    (return (execute_fn compiled view-context)))
  (var db_input (-/normalize-db db))
  (var result (xdb/db-pull-sync {"::" "db.supabase"
                                 :instance db_input
                                 :opts view-context}
                                (xt/x:get-key db_input "schema_name")
                                query-plan))
  (if (== "error" (xt/x:get-key result "status"))
    (return [false result])
    (return [true result])))

(defn.xt map-supabase-error
  [db error opts]
  (var map_error (xt/x:get-key db "map_error"))
  (if (xt/x:is-function? map_error)
    (return (map_error error opts))
    (return {"status" "error"
             "tag" "db/supabase-query-failed"
             "data" error})))

(defn.xt run-supabase-query
  "prepares, compiles, and executes a Supabase query"
  {:added "4.0"}
  [db query-spec view-context]
  (var [ok query-plan] (db-query/prepare-query db query-spec view-context))
  (when (not ok)
    (return [ok query-plan]))
  (var [e-ok result] (-/execute-query db query-plan view-context))
  (return [e-ok result]))

(def.xt MODULE (!:module))
