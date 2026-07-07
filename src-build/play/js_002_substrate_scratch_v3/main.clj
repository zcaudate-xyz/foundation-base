(ns play.js-002-substrate-scratch-v3.main
  (:require [hara.lang :as l]
            [postgres.core :as pg]
            [postgres.sample.scratch-v3]
            [xt.lang.common-lib]
            [xt.lang.common-data]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as data]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as runtime]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v3")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v3"))))

(def.js DEFAULT_USER_ID
  "00000000-0000-0000-0000-000000000001")

(def.js DEMOS
  [{"id" "currencies"
    "title" "Currency catalogue"
    "description" "Queries the scratch_v3 Currency table through a generated dataview."
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
    "source_id" "db/caching"}])

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
   (-> (runtime/sharedworker-connect client
                                     (-/backbone-config supabase-config)
                                     -/Schema
                                     -/SchemaLookup)
       (promise/x:promise-then
        (fn [_]
          (return client))))))

(defn.js currency-model
  []
  (return
   {"table" "Currency"
    "select_entry" {"input" []
                    "view" {"table" "Currency"
                            "type" "select"
                            "query" {"__deleted__" false}}}
    "return_entry" {"input" []
                    "view" {"table" "Currency"
                            "type" "return"
                            "query" ["id" "name" "type" "symbol" "decimal"]}}}))

(defn.js profile-model
  [user-id]
  (return
   {"table" "UserProfile"
    "select_entry" {"input" [{"symbol" "i_account_id" "type" "uuid"}]
                    "view" {"table" "UserProfile"
                            "type" "select"
                            "query" {"account" {"::" "sql/arg"
                                                  "name" "{{i_account_id}}"}}}}
    "return_entry" {"input" []
                    "view" {"table" "UserProfile"
                            "type" "return"
                            "query" ["id" "account" "first_name" "last_name"
                                     "language" "about"]}}
    "select_args" [user-id]}))

(defn.js wallet-model
  [user-id]
  (return
   {"table" "Wallet"
    "select_entry" {"input" [{"symbol" "i_owner_id" "type" "uuid"}]
                    "view" {"table" "Wallet"
                            "type" "select"
                            "query" {"owner" {"::" "sql/arg"
                                                "name" "{{i_owner_id}}"}}}}
    "return_entry" {"input" []
                    "view" {"table" "Wallet"
                            "type" "return"
                            "query" ["id" "slug" "owner" "entries"]}}
    "select_args" [user-id]}))

(defn.js demo-model
  [demo-id user-id]
  (cond (== demo-id "currencies")
        (return (-/currency-model))

        (== demo-id "profile")
        (return (-/profile-model user-id))

        (== demo-id "wallet")
        (return (-/wallet-model user-id))

        :else
        (return (-/currency-model))))

(defn.js attach-demo
  [client demo-id user-id]
  (var demo (data/arr-find -/DEMOS
                           (fn [entry]
                             (return (== (x:get-key entry "id") demo-id)))))
  (var source-id (x:get-key demo "source_id"))
  (var space-id (x:get-key demo "space_id"))
  (var group-id (x:get-key demo "group_id"))
  (var model-id (x:get-key demo "model_id"))
  (var model-spec (-/demo-model demo-id user-id))
  (return
   (-> (client-base/dataview-attach-model
        client
        source-id
        {"space_id" space-id
         "group_id" group-id
         "model_id" model-id}
        model-spec
        {"pipeline" {}
         "options" {}
         "defaults" {"select_args" (or (x:get-key model-spec "select_args") [])
                     "return_args" []}}
        {})
       (promise/x:promise-then
        (fn [_]
          (return (page-proxy/group-open-proxy client space-id group-id {}))))
       (promise/x:promise-then
        (fn [_]
          (return
           (page-proxy/model-proxy-call
            client space-id group-id model-id
            [{"select_args" (or (x:get-key model-spec "select_args") [])
              "return_args" []}]
            true {}))))
       (promise/x:promise-then
        (fn [_]
          (var group (page-core/group-get client space-id group-id))
          (var model (data/get-in group ["models" model-id]))
          (return
           {"demo" demo
            "model_type" (x:get-key model "::")
            "output" (event-model/get-current model nil)}))))))

(defn.js example-event
  [demo-id]
  (return
   {"id" "evt_play_scratch_v3"
    "type" (+ "scratch-v3/" demo-id "-loaded")
    "timestamp" (. (new Date) ["toISOString"])
    "sourceNode" "play-scratch-v3-worker"
    "targetSpace" "play/scratch-v3"
    "modelId" (+ "scratch-v3/" demo-id)
    "correlationId" "req_play_scratch_v3"
    "payload" {"demo_id" demo-id}
    "metadata" {"schema" "scratch_v3"}}))
