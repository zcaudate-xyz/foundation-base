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
  [db-id table select-method target]
  (var query {"db" db-id
              "table" table
              "select_method" select-method
              "return_method" "default"})
  (when target
    (xt/x:set-key query "target" target))
  (return query))

(defn.js make-binding
  [db-id table select-method default-output target]
  (return {"query" (-/make-query db-id table select-method target)
           "default_output" default-output}))

(defn.js make-binding-pair
  [db-id-prefix view-id table select-method default-output]
  (var out {})
  (xt/x:set-key out (+ view-id "_local")
                (-/make-binding (+ db-id-prefix "-local")
                                table
                                select-method
                                default-output
                                nil))
  (xt/x:set-key out (+ view-id "_remote")
                (-/make-binding (+ db-id-prefix "-remote")
                                table
                                select-method
                                default-output
                                "supabase"))
  (return out))

(def.js BINDINGS
  {"currency"
   (xtd/obj-assign
    (-/make-binding-pair "scratch" "all" "Currency" "all_active" [])
    (-/make-binding-pair "scratch" "by_id" "Currency" "by_id" nil)
    (-/make-binding-pair "scratch" "by_type" "Currency" "by_type" []))
   "user"
   (xtd/obj-assign
    (-/make-binding-pair "scratch" "all" "User" "all" [])
    (-/make-binding-pair "scratch" "by_id" "User" "by_id" nil)
    (-/make-binding-pair "scratch" "by_nickname" "User" "by_nickname" nil))
   "wallet"
   (xtd/obj-assign
    (-/make-binding-pair "scratch" "by_owner" "Wallet" "by_owner" nil))
   "asset"
   (xtd/obj-assign
    (-/make-binding-pair "scratch" "by_wallet" "Asset" "by_wallet" [])
    (-/make-binding-pair "scratch" "by_wallet_currency" "Asset" "by_wallet_currency" nil))})

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
  "creates the js.cell service registry for scratch-v3 views"
  {:added "4.1"}
  [remote-opts]
  (return
   (service/create-service
    {"scratch-local" (-/create-local-db)
     "scratch-remote" (-/create-remote-db remote-opts)})))

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
    (xdb/sync-event local-db request)
    (return [true request]))
  (return [true nil]))

(def.js MODULE (!:module))
