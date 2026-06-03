(ns postgres.sample.scratch-v3.cell
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.cell.service :as service]
             [js.cell.binding :as binding]
             [js.cell.binding.model :as binding-model]
             [xt.db.system :as xdb]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [postgres.sample.scratch-v3 :as scratch]
             [postgres.sample.scratch-v3.view-currency :as view-currency]
             [postgres.sample.scratch-v3.realtime :as realtime]]
   :export [MODULE]})

(defn.js make-query
  [db-id select-method target]
  (var query {"db" db-id
              "table" "Currency"
              "select_method" select-method
              "return_method" "default"})
  (when target
    (xt/x:set-key query "target" target))
  (return query))

(defn.js make-binding
  [db-id select-method default-output target]
  (return {"query" (-/make-query db-id select-method target)
           "default_output" default-output}))

(defn.js make-binding-pair
  [view-id select-method default-output]
  (var out {})
  (xt/x:set-key out (+ view-id "_local")
                (-/make-binding "currency-local"
                                select-method
                                default-output
                                nil))
  (xt/x:set-key out (+ view-id "_remote")
                (-/make-binding "currency-remote"
                                select-method
                                default-output
                                "supabase"))
  (return out))

(def.js BINDINGS
  {"currency"
   (xtd/obj-assign
    (-/make-binding-pair "all" "all_active" [])
    (-/make-binding-pair "by_id" "by_id" nil)
    (-/make-binding-pair "by_type" "by_type" []))})

(defn.js create-local-db
  "creates the local cache db used by js.cell views"
  {:added "4.1"}
  []
  (var schema (scratch/get-schema))
  (var lookup (scratch/get-schema-lookup))
  (var views (view-currency/make-views))
  (return (xtd/obj-assign
           (xdb/db-create {"::" "db.cache"} schema lookup nil)
           {"schema" schema
            "views" views})))

(defn.js create-remote-db
  "creates the remote supabase db descriptor used by js.cell views"
  {:added "4.1"}
  [opts]
  (return (xtd/obj-assign
           {"::" "db.supabase"
            "schema" (scratch/get-schema)
           "views" (view-currency/make-views)}
           (or opts {}))))

(defn.js create-service
  "creates the js.cell service registry for currency views"
  {:added "4.1"}
  [remote-opts]
  (return
   (service/create-service
    {"currency-local" (-/create-local-db)
     "currency-remote" (-/create-remote-db remote-opts)})))

(defn.js get-bindings
  "returns the declarative js.cell bindings for currency views"
  {:added "4.1"}
  []
  (return -/BINDINGS))

(defn.js compile-bindings
  "compiles the declarative js.cell bindings into runtime view specs"
  {:added "4.1"}
  [service-registry]
  (return (binding/compile-bindings
           service-registry
           -/BINDINGS
           binding-model/compile-view-spec)))

(defn.js apply-realtime
  "applies a realtime postgres_changes payload to the local cache db"
  {:added "4.1"}
  [local-db payload]
  (var request (realtime/postgres-change->sync-request payload))
  (when request
    (var body-sync (xt/x:get-key request "db/sync"))
    (var body-remove (xt/x:get-key request "db/remove"))
    (when body-sync
      (xdb/sync-event local-db ["add" body-sync]))
    (when body-remove
      (xdb/sync-event local-db ["remove" body-remove]))
    (return [true request]))
  (return [true nil]))

(def.js MODULE (!:module))
