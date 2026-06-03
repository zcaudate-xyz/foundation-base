(ns js.cell.service.db-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:export [MODULE]
   :require [[js.cell.service.db-query :as db-query]
              [js.cell.service.db-view :as db-view]
              [xt.db.system.client-supabase :as client-supabase]
              [xt.lang.spec-base :as xt]
              [xt.lang.spec-promise :as promise]
              [xt.db.text.pgrest-graph :as pgrest-graph]
              [xt.db.text.pgrest-tree :as pgrest-tree]]})

(defn.xt normalize-db
  [db]
  (var out (xt/x:obj-assign {} db))
  (var source (or (xt/x:get-key out "client") {}))
  (when (and (xt/x:nil? (xt/x:get-key out "base_url"))
             (xt/x:not-nil? (xt/x:get-key source "base_url")))
    (xt/x:set-key out "base_url" (xt/x:get-key source "base_url")))
  (when (and (xt/x:nil? (xt/x:get-key out "base_url"))
             (xt/x:not-nil? (xt/x:get-key out "base-url")))
    (xt/x:set-key out "base_url" (xt/x:get-key out "base-url")))
  (when (and (xt/x:nil? (xt/x:get-key out "schema_name"))
             (xt/x:not-nil? (xt/x:get-key source "schema_name")))
    (xt/x:set-key out "schema_name" (xt/x:get-key source "schema_name")))
  (when (and (xt/x:nil? (xt/x:get-key out "schema_name"))
             (xt/x:not-nil? (xt/x:get-key out "schema-name")))
    (xt/x:set-key out "schema_name" (xt/x:get-key out "schema-name")))
  (when (and (xt/x:nil? (xt/x:get-key out "schema_name"))
             (xt/x:not-nil? (xt/x:get-key out "schema")))
    (xt/x:set-key out "schema_name" (xt/x:get-key out "schema")))
  (when (and (xt/x:nil? (xt/x:get-key out "api_key"))
             (xt/x:not-nil? (xt/x:get-key source "api_key")))
    (xt/x:set-key out "api_key" (xt/x:get-key source "api_key")))
  (when (and (xt/x:nil? (xt/x:get-key out "api_key"))
             (xt/x:not-nil? (xt/x:get-key out "api-key")))
    (xt/x:set-key out "api_key" (xt/x:get-key out "api-key")))
  (when (and (xt/x:nil? (xt/x:get-key out "auth_token"))
             (xt/x:not-nil? (xt/x:get-key source "auth_token")))
    (xt/x:set-key out "auth_token" (xt/x:get-key source "auth_token")))
  (when (and (xt/x:nil? (xt/x:get-key out "auth_token"))
             (xt/x:not-nil? (xt/x:get-key out "auth-token")))
    (xt/x:set-key out "auth_token" (xt/x:get-key out "auth-token")))
  (return out))

(defn.xt supabase-capable?
  [db]
  (var db_input (-/normalize-db db))
  (return (or (xt/x:is-function? (xt/x:get-key db_input "execute"))
              (xt/x:not-nil? (xt/x:get-key db_input "client")))))

(defn.xt compile-select-item
  [item]
  (cond (xt/x:is-string? item)
        (return item)

        (and (xt/x:is-array? item)
             (== 2 (xt/x:len item))
             (xt/x:is-string? (xt/x:first item))
             (xt/x:is-array? (xt/x:second item)))
        (return (xt/x:cat (xt/x:first item)
                          "("
                          (-/compile-select (xt/x:second item))
                          ")"))

        :else
        (return (xt/x:to-string item))))

(defn.xt compile-select
  [items]
  (return (xt/x:str-join ","
                         (xt/x:arr-map (or items [])
                                       -/compile-select-item))))

(defn.xt compile-filters-into
  [prefix clause out]
  (xt/for:array [key (xt/x:obj-keys (or clause {}))]
    (var value (xt/x:get-key clause key))
    (var path (:? (== prefix "")
                  key
                  (xt/x:cat prefix "." key)))
    (cond (and (xt/x:is-object? value)
               (not (xt/x:is-array? value)))
          (-/compile-filters-into path value out)

          (and (xt/x:is-array? value)
               (xt/x:is-string? (xt/x:first value)))
          (xt/x:arr-push out {"path" path
                              "op" (xt/x:first value)
                              "value" (xt/x:second value)})

          :else
          (xt/x:arr-push out {"path" path
                              "op" "eq"
                              "value" value})))
  (return out))

(defn.xt compile-query-fallback
  [query-plan]
  (var table (xt/x:first query-plan))
  (var second-item (xt/x:second query-plan))
  (var where (:? (and (xt/x:not-nil? second-item)
                      (xt/x:is-object? second-item)
                      (not (xt/x:is-array? second-item)))
                 second-item
                 {}))
  (var select-items (:? (xt/x:is-array? second-item)
                        second-item
                        (xt/x:get-idx query-plan 2)))
  (var filters (-/compile-filters-into "" where []))
  (var params [(xt/x:cat "select=" (-/compile-select select-items))])
  (xt/for:array [filter filters]
    (xt/x:arr-push params
                   (xt/x:cat (xt/x:get-key filter "path")
                             "="
                             (pgrest-graph/compile-filter-value
                              (xt/x:get-key filter "op")
                              (xt/x:get-key filter "value")))))
  (var path (xt/x:cat "/rest/v1/" table))
  (var query (pgrest-graph/compile-query-string params))
  (return {"type" "query"
           "table" table
           "method" "GET"
           "path" path
           "select" (-/compile-select select-items)
           "filters" filters
           "params" params
           "query" query
           "url" (pgrest-graph/compile-url path params)
           "headers" {}}))

(defn.xt compile-query-plan
  [db query-plan view-context]
  (var schema (db-view/get-schema db))
  (if (== 0 (xt/x:len (xt/x:obj-keys (or schema {}))))
    (return (-/compile-query-fallback query-plan))
    (return (pgrest-graph/select-return
             schema
             query-plan
             0
             view-context))))

(defn.xt preserve-legacy-select
  [request entry]
  (var query (xt/x:get-path entry ["view" "query"]))
  (when (and (xt/x:is-array? query)
             (== 1 (xt/x:len query))
             (xt/x:is-string? (xt/x:first query))
             (== "*" (xt/x:get-key request "select")))
    (xt/x:set-key request "select" (xt/x:first query)))
  (return request))

(defn.xt prepare-request
  [db query-spec view-context]
  (var qm (db-query/normalize-query db query-spec view-context))
  (var #{table
         select-method
         select-args
         return-method
         return-count
         return-id
         return-bulk
         return-args
         return-omit
         data-only} qm)
  (var qe (db-view/view-query-entries db table qm data-only))
  (var #{select-entry
         return-entry} qe)
  (:= select-entry (db-query/view-local-transform select-entry))
  (:= return-entry (db-query/view-local-transform return-entry))
  (when (and select-method (not select-entry))
    (return [false {:status "error"
                    :tag "net/select-method-not-found"
                    :data {:input select-method}}]))
  (when (and return-method (not return-entry))
    (return [false {:status "error"
                    :tag "net/return-method-not-found"
                    :data {:input return-method}}]))
  (cond (and (xt/x:not-nil? select-entry)
             (xt/x:not-nil? return-entry))
        (do (var [s-ok s-err] (db-query/query-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (var [r-ok r-err] (db-query/query-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (var request
                 (pgrest-tree/pgrest-query-combined
                  (db-view/get-schema db)
                  select-entry
                  select-args
                  return-entry
                  return-args
                  return-omit
                  view-context))
            (return [true
                     (-/preserve-legacy-select request return-entry)]))

        (xt/x:not-nil? select-entry)
        (do (var [s-ok s-err] (db-query/query-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (return [true
                     (:? return-count
                         (pgrest-tree/pgrest-query-count
                          (db-view/get-schema db)
                          select-entry
                          select-args
                          view-context)
                         (pgrest-tree/pgrest-query-select
                          (db-view/get-schema db)
                          select-entry
                          select-args
                          view-context))]))

        (xt/x:not-nil? return-id)
        (do (var rargs [return-id (xt/x:unpack return-args)])
            (var [r-ok r-err] (db-query/query-check return-entry rargs false))
            (when (not r-ok)
              (return [r-ok r-err]))
            (var request
                 (pgrest-tree/pgrest-query-return
                  (db-view/get-schema db)
                  return-entry
                  return-id
                  return-args
                  view-context))
            (return [true
                     (-/preserve-legacy-select request return-entry)]))

        (xt/x:not-nil? return-bulk)
        (do (var [r-ok r-err] (db-query/query-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (var request
                 (pgrest-tree/pgrest-query-return-bulk
                  (db-view/get-schema db)
                  return-entry
                  return-bulk
                  return-args
                  view-context))
            (return [true
                     (-/preserve-legacy-select request return-entry)]))

        :else
        (return [true nil])))

(defn.xt compile-query
  "compiles a query plan into a Supabase request description"
  {:added "4.0"}
  [db query-plan view-context]
  (return (-/compile-query-plan db query-plan view-context)))

(defn.xt execute-request
  [db request view-context]
  (var execute_fn (xt/x:get-key db "execute"))
  (when (xt/x:is-function? execute_fn)
    (return (execute_fn request view-context)))
  (var db_input (-/normalize-db db))
  (var prepared (client-supabase/prepare-request db_input request view-context))
  (var fetch-client (client-supabase/resolve-client db_input view-context))
  (return
   (promise/x:promise-then
    ((xt/x:get-key fetch-client "request")
     prepared
     view-context)
    (fn [response]
      (var result (client-supabase/unwrap-response response))
      (if (== "error" (xt/x:get-key result "status"))
        (return [false result])
        (return [true result]))))))

(defn.xt execute-query
  "executes a compiled Supabase query via an injected executor"
  {:added "4.0"}
  [db query-plan view-context]
  (return (-/execute-request db
                             (-/compile-query db query-plan view-context)
                             view-context)))

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
  (var [ok request] (-/prepare-request db query-spec view-context))
  (when (not ok)
    (return [ok request]))
  (when (not request)
    (return [ok nil]))
  (var output (-/execute-request db request view-context))
  (if (promise/x:promise-native? output)
    (return
     (promise/x:promise-then
      output
      (fn [result]
        (return result))))
    (return output)))

(def.xt MODULE (!:module))
