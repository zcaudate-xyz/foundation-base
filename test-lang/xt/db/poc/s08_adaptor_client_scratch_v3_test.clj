^{:seedgen/skip true}
(ns xt.db.poc.s08-adaptor-client-scratch-v3-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.sample.scratch-v3 :as scratch-v3]))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.sample.scratch-v3 :as scratch-v3]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (do
      (pg/t:delete scratch-v0/Log)
      (s/grant-usage #{"scratch_v0"})
      (s/grant-usage #{"scratch_v3"})
      (s/grant-tables #{"scratch_v3"})
      (s/grant-privileges #{"scratch_v3"}))))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
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

(def.js USER_ID
  (@! "00000000-0000-0000-0000-000000000001"))

(defn seed-scratch-v3!
  "Seeds scratch-v3 with currencies, a user, profile, wallet and assets."
  {:added "4.1"}
  []
  (pg/t:delete scratch-v3/Asset)
  (pg/t:delete scratch-v3/Wallet)
  (pg/t:delete scratch-v3/UserProfile)
  (pg/t:delete scratch-v3/User)
  (pg/t:delete scratch-v3/Currency)
  (scratch-v3/insert-currency "USD" "fiat" "$" "$" 2 "US Dollar" "US Dollars" "" {})
  (scratch-v3/insert-currency "EUR" "fiat" "€" "€" 2 "Euro" "Euros" "" {})
  (scratch-v3/insert-currency "BTC" "crypto" "₿" "₿" 8 "Bitcoin" "Bitcoins" "" {})
  (scratch-v3/insert-user (java.util.UUID/fromString "00000000-0000-0000-0000-000000000001")
                          "alice"
                          "alice@example.com"
                          true false {})
  (scratch-v3/insert-user-profile (java.util.UUID/fromString "00000000-0000-0000-0000-000000000001")
                                  "Alice" "Smith" "EN" "About Alice" {})
  (let [wallet (scratch-v3/insert-wallet (java.util.UUID/fromString "00000000-0000-0000-0000-000000000001")
                                         "default" {})]
    (scratch-v3/insert-asset (:id wallet) "USD" 100 {})
    (scratch-v3/insert-asset (:id wallet) "BTC" 0.5 {})))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (seed-scratch-v3!)
          (local-min/restart-postgrest)
          (local-min/refresh-postgrest-schema "scratch_v3" "Currency")
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(defn.js connect-kernel-worker
  "connects to the shared worker, initialises the db adaptor, and invokes callback"
  {:added "4.1"}
  [client]
  (return
   (runtime/sharedworker-connect client
                                 {"primary" {"type" "supabase"
                                             "defaults" (@! local-min/+config-supabase-anon+)}
                                  "caching" {"type" "sqlite"
                                             "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup)))

^{:refer xt.db.poc.s08-adaptor-client-scratch-v3-test/dataview-select-args
  :added "4.1"}
(fact "dataview model with select args filters currencies by type"

  (notify/wait-on [:js 15000]
    (var client (substrate/node-create {}))
    (-> (-/connect-kernel-worker client)
        (promise/x:promise-then
         (fn [_]
           (return
            (client-base/dataview-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "currency-by-type"}
             {"table" "Currency"
              "select_entry" {"input" [{"symbol" "i_type" "type" "text"}]
                              "view" {"table" "Currency"
                                      "type" "select"
                                      "query" {"type" {"::" "sql/cast"
                                                       "args" [{"::" "sql/arg" "name" "{{i_type}}"}
                                                               {"::" "sql/defenum"
                                                                "schema" "scratch_v3"
                                                                "name" "EnumCurrencyType"}]}
                                               "__deleted__" false}}}
              "return_entry" {"input" []
                              "view" {"table" "Currency"
                                      "type" "return"
                                      "query" ["id" "name" "type"]}}}
             {"pipeline" {}
              "options" {}
              "defaults" {"select_args" []
                          "return_args" []}}
             {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/group-open-proxy client "room/a" "demo" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/model-proxy-call client "room/a" "demo" "currency-by-type"
                                         [{"select_args" ["fiat"]
                                           "return_args" []}]
                                         true {}))))
        (promise/x:promise-then
         (fn [_]
           (var group (page-core/group-get client "room/a" "demo"))
           (var model (xtd/get-in group ["models" "currency-by-type"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"has_group" false
             "error" (. err ["message"])
             "stack" (. err ["stack"])})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" (contains [(contains {"id" "USD" "type" "fiat"})
                           (contains {"id" "EUR" "type" "fiat"})]
                          :in-any-order)}))

^{:refer xt.db.poc.s08-adaptor-client-scratch-v3-test/dataview-select-and-return-args
  :added "4.1"}
(fact "dataview model with select args and return args returns annotated currencies"

  (notify/wait-on [:js 15000]
    (var client (substrate/node-create {}))
    (-> (-/connect-kernel-worker client)
        (promise/x:promise-then
         (fn [_]
           (return
            (client-base/dataview-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "currency-annotated"}
             {"table" "Currency"
              "select_entry" {"input" [{"symbol" "i_type" "type" "text"}]
                              "view" {"table" "Currency"
                                      "type" "select"
                                      "query" {"type" {"::" "sql/cast"
                                                       "args" [{"::" "sql/arg" "name" "{{i_type}}"}
                                                               {"::" "sql/defenum"
                                                                "schema" "scratch_v3"
                                                                "name" "EnumCurrencyType"}]}
                                               "__deleted__" false}}}
              "return_entry" {"input" [{"symbol" "i_currency_id" "type" "citext"}
                                       {"symbol" "i_note" "type" "text"}]
                              "view" {"table" "Currency"
                                      "type" "return"
                                      "query" ["id" "name" "type"
                                               {"::" "sql/arg" "name" "{{i_note}}"}]}}}
             {"pipeline" {}
              "options" {}
              "defaults" {"select_args" []
                          "return_args" []}}
             {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/group-open-proxy client "room/a" "demo" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/model-proxy-call client "room/a" "demo" "currency-annotated"
                                         [{"select_args" ["fiat"]
                                           "return_args" ["verified"]}]
                                         true {}))))
        (promise/x:promise-then
         (fn [_]
           (var group (page-core/group-get client "room/a" "demo"))
           (var model (xtd/get-in group ["models" "currency-annotated"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"has_group" false
             "error" (. err ["message"])
             "stack" (. err ["stack"])})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" (contains [(contains {"id" "USD" "type" "fiat"})
                           (contains {"id" "EUR" "type" "fiat"})]
                          :in-any-order)}))

^{:refer xt.db.poc.s08-adaptor-client-scratch-v3-test/dataview-return-id-args
  :added "4.1"}
(fact "dataview model with return-id and return args returns a single annotated currency"

  (notify/wait-on [:js 15000]
    (var client (substrate/node-create {}))
    (-> (-/connect-kernel-worker client)
        (promise/x:promise-then
         (fn [_]
           (return
            (client-base/dataview-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "currency-by-id"}
             {"table" "Currency"
              "return_entry" {"input" [{"symbol" "i_currency_id" "type" "citext"}
                                       {"symbol" "i_note" "type" "text"}]
                              "view" {"table" "Currency"
                                      "type" "return"
                                      "query" ["id" "name" "type"
                                               {"::" "sql/arg" "name" "{{i_note}}"}]}}
              "return_id" "USD"
              "return_args" ["verified"]}
             {"pipeline" {}
              "options" {}
              "defaults" {"select_args" []
                          "return_args" []}}
             {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/group-open-proxy client "room/a" "demo" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/model-proxy-call client "room/a" "demo" "currency-by-id"
                                         [{"select_args" []
                                           "return_args" ["verified"]}]
                                         true {}))))
        (promise/x:promise-then
         (fn [_]
           (var group (page-core/group-get client "room/a" "demo"))
           (var model (xtd/get-in group ["models" "currency-by-id"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"has_group" false
             "error" (. err ["message"])
             "stack" (. err ["stack"])})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [(contains {"id" "USD" "type" "fiat"})]}))

^{:refer xt.db.poc.s08-adaptor-client-scratch-v3-test/dataview-multi-table
  :added "4.1"}
(fact "dataview model with select args returns wallet, assets and currencies"

  (notify/wait-on [:js 15000]
    (var client (substrate/node-create {}))
    (-> (-/connect-kernel-worker client)
        (promise/x:promise-then
         (fn [_]
           (return
            (client-base/dataview-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "wallet-by-owner"}
             {"table" "Wallet"
              "select_entry" {"input" [{"symbol" "i_owner_id" "type" "uuid"}]
                              "view" {"table" "Wallet"
                                      "type" "select"
                                      "query" {"owner" {"id" "{{i_owner_id}}"}
                                               "__deleted__" false}}}
              "return_entry" {"input" []
                              "view" {"table" "Wallet"
                                      "type" "return"
                                      "query" ["id" "slug"
                                               ["entries" ["id" "balance"
                                                           ["currency" ["id" "name"]]]]]}}}
             {"pipeline" {}
              "options" {}
              "defaults" {"select_args" []
                          "return_args" []}}
             {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/group-open-proxy client "room/a" "demo" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/model-proxy-call client "room/a" "demo" "wallet-by-owner"
                                         [{"select_args" [-/USER_ID]
                                           "return_args" []}]
                                         true {}))))
        (promise/x:promise-then
         (fn [_]
           (return (promise/x:with-delay
                    500
                    (fn [] (return true))))))
        (promise/x:promise-then
         (fn [_]
           (var group (page-core/group-get client "room/a" "demo"))
           (var model (xtd/get-in group ["models" "wallet-by-owner"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"has_group" false
             "error" (. err ["message"])
             "stack" (. err ["stack"])})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [(contains
                  {"slug" "default"
                   "entries" (contains
                              [(contains {"balance" 100
                                          "currency" (contains {"id" "USD" "name" "US Dollar"})})
                               (contains {"balance" 0.5
                                          "currency" (contains {"id" "BTC" "name" "Bitcoin"})})]
                              :in-any-order)})]}))

^{:refer xt.db.poc.s08-adaptor-client-scratch-v3-test/pull-multi-table
  :added "4.1"}
(fact "pull model returns nested user profile, wallet, assets and currencies"

  (notify/wait-on [:js 15000]
    (var client (substrate/node-create {}))
    (-> (-/connect-kernel-worker client)
        (promise/x:promise-then
         (fn [_]
           (return
            (client-base/pull-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "user-detail"}
             ["User"
              {"nickname" "alice"}
              ["*/data"
               ["profile" ["id" "first_name" "last_name"]]
               ["wallets" ["id" "slug"
                           ["entries" ["id" "balance"
                                       ["currency" ["id" "name"]]]]]]]]
             {"pipeline" {}
              "options" {}
              "defaults" {"args" []
                          "output" {}}}
             {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/group-open-proxy client "room/a" "demo" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/model-proxy-call client "room/a" "demo" "user-detail" [] true {}))))
        (promise/x:promise-then
         (fn [_]
           (var group (page-core/group-get client "room/a" "demo"))
           (var model (xtd/get-in group ["models" "user-detail"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"has_group" false
             "error" (. err ["message"])
             "stack" (. err ["stack"])})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [(contains
                  {"nickname" "alice"
                   "profile" (contains {"first_name" "Alice"
                                        "last_name" "Smith"})
                   "wallets" (contains
                              {"slug" "default"
                               "entries" (contains
                                          [(contains {"balance" 100
                                                      "currency" (contains {"id" "USD"})})
                                           (contains {"balance" 0.5
                                                      "currency" (contains {"id" "BTC"})})]
                                          :in-any-order)})})]}))



