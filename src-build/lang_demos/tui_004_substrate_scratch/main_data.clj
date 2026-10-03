(ns lang-demos.tui-004-substrate-scratch.main-data
  (:require [lang.core :as l]
            [postgres.core :as pg]
            [postgres.gen.bind-macro :as pg-bind]
            [postgres.sample.scratch-v3]
            [xt.db.node.runtime :as runtime-gen]
            [lang-demos.js-004-xtdb-backbone.app.config :as app-config]))

(defn rpc-gen
  "Generates a substrate RPC descriptor from a PostgreSQL function."
  [function-symbol table]
  (assoc (pg-bind/bind-function (var-get (resolve function-symbol)))
         :table {"base" table
                 "type" "db/sync"}))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as data]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as runtime]
             [postgres.sample.scratch-v3.view-currency :as view-currency]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v3")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v3"))))

(def.js SUPABASE_CONFIG
  (@! (assoc (app-config/supabase-config)
             "host" (or (get (app-config/supabase-config) "hostname")
                        "127.0.0.1")
             "secured" (= "https"
                          (get (app-config/supabase-config) "protocol"))
             "schema_name" "scratch_v3"
             "auth_token" (or (get (app-config/supabase-config) "auth_token")
                              (get (app-config/supabase-config) "api_key"))
             "headers" {})))

(def.js NODEWORKER_SCRIPT
  (@! (runtime-gen/nodeworker-init-string
       {"@sqlite.org/sqlite-wasm" "@sqlite.org/sqlite-wasm"
        "pg" "data:text/javascript,export default {Client: function(){}}"})))

(def.js RPC_SPECS
  (@! {"currency-all"
       (rpc-gen 'postgres.sample.scratch-v3/currency-all-active "Currency")
       "profile-by-account"
       (rpc-gen 'postgres.sample.scratch-v3/user-profile-by-account "UserProfile")
       "wallet-by-owner"
       (rpc-gen 'postgres.sample.scratch-v3/wallet-by-owner "Wallet")
       "wallet-assets"
       (rpc-gen 'postgres.sample.scratch-v3/asset-by-wallet "Asset")}))

(def.js DEFAULT_USER_ID
  "00000000-0000-0000-0000-000000000001")

(def.js DEMOS
  [{"id" "currencies"
    "title" "Currency catalogue"
    "description" "Queries the scratch_v3 Currency table through the generated currency_all_active RPC."
    "space_id" "play/scratch-v3"
    "group_id" "catalogue"
    "model_id" "currency-all"
    "source_id" "db/primary"}
   {"id" "profile"
    "title" "User profile"
    "description" "Loads UserProfile by account id and exposes the event model state."
    "space_id" "play/scratch-v3"
    "group_id" "account"
    "model_id" "profile-by-account"
    "source_id" "db/primary"}
   {"id" "wallet"
    "title" "Wallet and assets"
    "description" "Loads the default wallet and its asset balances from scratch_v3."
    "space_id" "play/scratch-v3"
    "group_id" "wallet"
    "model_id" "wallet-by-owner"
    "source_id" "db/primary"}
   {"id" "cache"
    "title" "Primary and SQLite cache"
    "description" "Shows the same model topology using primary and caching sources."
    "space_id" "play/scratch-v3"
    "group_id" "cache"
    "model_id" "currency-cache"
    "source_id" "db/primary"}])

(defn.js backbone-config
  [supabase-config]
  (return
   {"primary" {"type" "supabase"
                "defaults" supabase-config}
    "caching" {"type" "sqlite"
                "defaults" {}}}))

(defn.js connect
  [supabase-config]
  (var client (substrate/node-create {"id" "play-scratch-v3-client"}))
  (return
   (-> (runtime/nodeworker-connect
        client
        (-/backbone-config supabase-config)
        -/Schema
        -/SchemaLookup
        (browser-transport/node-worker-source -/NODEWORKER_SCRIPT {})
        nil)
       (promise/x:promise-then
        (fn [init]
          (return {"client" client
                   "connection" {"node" client
                                 "transport_id" (x:get-key init "transport")}}))))))

(defn.js disconnect
  [session]
  (return
   (browser-transport/disconnect
    (x:get-key session "connection"))))

(defn.js currency-model
  []
  (return {"rpc_args" []}))

(defn.js currency-cache-model
  []
  (var views (view-currency/make-views))
  (return
   {"table" "Currency"
    "select_entry" (data/get-in views ["Currency" "select" "all_active"])
    "select_args" []
    "return_entry" (data/get-in views ["Currency" "return" "default"])}))

(defn.js profile-model
  [user-id]
  (return {"rpc_args" [user-id]}))

(defn.js wallet-model
  [user-id]
  (return {"rpc_args" [user-id]}))

(defn.js demo-model
  [demo-id user-id]
  (cond (== demo-id "currencies")
        (return (-/currency-model))

        (== demo-id "profile")
        (return (-/profile-model user-id))

        (== demo-id "wallet")
        (return (-/wallet-model user-id))

        (== demo-id "cache")
        (return (-/currency-cache-model))

        :else
        (return (-/currency-model))))

(defn.js rpc-attach-and-call
  [client source-id space-id group-id model-id rpc-spec rpc-args]
  (var model-options {"pipeline" {}
                      "options" {}
                      "defaults" {"args" rpc-args
                                  "output" {}}})
  (return
   (-> (client-base/rpc-attach-model
        client source-id
        {"space_id" space-id
         "group_id" group-id
         "model_id" model-id}
        rpc-spec model-options {})
       (promise/x:promise-then
        (fn [_]
          (return (page-proxy/group-open-proxy client space-id group-id {}))))
       (promise/x:promise-then
        (fn [_]
          (return (page-proxy/model-proxy-call
                   client space-id group-id model-id rpc-args true {}))))
       (promise/x:promise-then
        (fn [_]
          (var group (page-core/group-get client space-id group-id))
          (var model (data/get-in group ["models" model-id]))
          (return {"model_type" (x:get-key model "::")
                   "output" (event-model/get-current model nil)}))))))

(defn.js attach-wallet-assets
  [client source-id space-id group-id demo model-result]
  (var wallet-output (x:get-key model-result "output"))
  (var wallet-row (:? (xt/x:is-array? wallet-output)
                      (xt/x:first wallet-output)
                      wallet-output))
  (var wallet-id (x:get-key wallet-row "id"))
  (return
   (:? (xt/x:not-nil? wallet-id)
       (-> (-/rpc-attach-and-call
            client source-id space-id group-id "wallet-assets"
            (x:get-key -/RPC_SPECS "wallet-assets")
            [wallet-id])
           (promise/x:promise-then
            (fn [assets-result]
              (return {"demo" demo
                       "model_type" (x:get-key model-result "model_type")
                       "output" {"wallet" wallet-output
                                 "assets" (x:get-key assets-result "output")}}))))
       {"demo" demo
        "model_type" (x:get-key model-result "model_type")
        "output" {"wallet" wallet-output
                  "assets" []}})))

(defn.js attach-demo
  [client demo-id user-id]
  (var demo-index (data/arr-find -/DEMOS
                                 (fn [entry]
                                   (return (== (x:get-key entry "id") demo-id)))))
  (var demo (data/get-in -/DEMOS [demo-index]))
  (var source-id (x:get-key demo "source_id"))
  (var space-id (x:get-key demo "space_id"))
  (var group-id (x:get-key demo "group_id"))
  (var model-id (x:get-key demo "model_id"))
  (var rpc-spec (x:get-key -/RPC_SPECS model-id))
  (var model-spec (-/demo-model demo-id user-id))
  (var defaults
       (:? rpc-spec
           {"args" (x:get-key model-spec "rpc_args")
            "output" {}}
           {"select_args" (or (x:get-key model-spec "select_args") [])
            "return_args" []}))
  (var model-options {"pipeline" {}
                      "options" {}
                      "defaults" defaults})
  (var attach-promise
       (:? rpc-spec
           (client-base/rpc-attach-model
            client source-id
            {"space_id" space-id
             "group_id" group-id
             "model_id" model-id}
            rpc-spec model-options {})
           (client-base/dataview-attach-model
            client source-id
            {"space_id" space-id
             "group_id" group-id
             "model_id" model-id}
            model-spec model-options {})))
  (return
   (-> attach-promise
       (promise/x:promise-then
        (fn [_]
          (return (page-proxy/group-open-proxy client space-id group-id {}))))
       (promise/x:promise-then
        (fn [_]
          (var model-args
               (:? rpc-spec
                   (x:get-key model-spec "rpc_args")
                   [{"select_args" (or (x:get-key model-spec "select_args") [])
                     "return_args" []}]))
          (return (page-proxy/model-proxy-call
                   client space-id group-id model-id model-args true {}))))
       (promise/x:promise-then
        (fn [_]
          (var group (page-core/group-get client space-id group-id))
          (var model (data/get-in group ["models" model-id]))
          (var model-result {"demo" demo
                             "model_type" (x:get-key model "::")
                             "output" (event-model/get-current model nil)})
          (return (:? (== demo-id "wallet")
                      (-/attach-wallet-assets
                       client source-id space-id group-id demo model-result)
                      model-result)))))))

(defn.js example-event
  [demo-id]
  (return
   {"id" "evt_play_scratch_v3"
    "type" (+ "scratch-v3/" demo-id "-loaded")
    "timestamp" (. (new Date) (toISOString))
    "sourceNode" "play-scratch-v3-worker"
    "targetSpace" "play/scratch-v3"
    "modelId" (+ "scratch-v3/" demo-id)
    "correlationId" "req_play_scratch_v3"
    "payload" {"demo_id" demo-id}
    "metadata" {"schema" "scratch_v3"}}))
