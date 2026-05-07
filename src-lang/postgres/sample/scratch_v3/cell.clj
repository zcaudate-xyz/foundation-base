(ns postgres.sample.scratch-v3.cell
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.cell.service :as service]
             [js.cell.binding :as binding]
             [js.cell.binding.model :as binding-model]
             [js.cell.service.db-sync :as db-sync]
             [xt.db.instance :as xdb]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [postgres.sample.scratch-v3 :as scratch]
             [postgres.sample.scratch-v3.view-all :as view-all]
             [postgres.sample.scratch-v3.realtime :as realtime]]
   :export [MODULE]})

(def.js BINDINGS
  {"currency"
   {"all_local"
    {"query" {"db" "currency-local"
              "table" "Currency"
              "select_method" "all_active"
              "return_method" "default"}
     "default_output" []}
    "all_remote"
    {"query" {"db" "currency-remote"
              "table" "Currency"
              "select_method" "all_active"
              "return_method" "default"
              "target" "supabase"}
     "default_output" []}
    "by_id_local"
    {"query" {"db" "currency-local"
              "table" "Currency"
              "select_method" "by_id"
              "return_method" "default"}
     "default_output" nil}
    "by_id_remote"
    {"query" {"db" "currency-remote"
              "table" "Currency"
              "select_method" "by_id"
              "return_method" "default"
              "target" "supabase"}
     "default_output" nil}
    "by_type_local"
    {"query" {"db" "currency-local"
              "table" "Currency"
              "select_method" "by_type"
              "return_method" "default"}
     "default_output" []}
    "by_type_remote"
    {"query" {"db" "currency-remote"
              "table" "Currency"
              "select_method" "by_type"
              "return_method" "default"
              "target" "supabase"}
     "default_output" []}}})

(defn.js create-local-db
  "creates the local cache db used by js.cell views"
  {:added "4.1"}
  []
  (var schema (scratch/get-schema))
  (var lookup (scratch/get-schema-lookup))
  (var views (view-all/get-views))
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
            "views" (view-all/get-views)}
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
