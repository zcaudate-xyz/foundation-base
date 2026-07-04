(ns postgres.sample.scratch-v3-test
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [hara.runtime.postgres.base.application :as app]
            [postgres.sample.scratch-v3 :as scratch])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [   [xt.lang.spec-base :as xt]
   [xt.lang.common-data :as xtd]
   [postgres.sample.scratch-v3 :as scratch]
   [postgres.sample.scratch-v3.view-currency :as view-currency]
   [postgres.sample.scratch-v3.route-currency :as route-currency]
   [postgres.sample.scratch-v3.api-currency :as api-currency]
   [postgres.sample.scratch-v3.realtime :as realtime]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer postgres.sample.scratch-v3/Currency :added "4.1"}
(fact "registers Currency in the scratch-v3 application schema"
  (let [appv (app/app-create "scratch_v3")]
    [(contains? (:tables appv) 'Currency)
     (some? (get-in appv [:schema :tree :Currency]))])
  => [true true])

^{:refer postgres.sample.scratch-v3/User :added "4.1"}
(fact "registers User/UserProfile/Wallet/Asset in the scratch_v3 schema"
  (let [appv (app/app-create "scratch_v3")]
    [(contains? (:tables appv) 'User)
     (contains? (:tables appv) 'UserProfile)
     (contains? (:tables appv) 'Wallet)
     (contains? (:tables appv) 'Asset)])
  => [true true true true])

^{:refer postgres.sample.scratch-v3/Currency :added "4.1"}
(fact "defines currency queries, mutations, and schema bindings"
  (let [by-id-sql (l/emit-as :postgres `[(scratch/currency-by-id "AUD")])
        insert-sql (l/emit-as :postgres
                              `[(scratch/insert-currency
                                 "AUD" "fiat" "$" "$AU" 2
                                 "Australian Dollar"
                                 "Australian Dollars"
                                 "Currency for Australia"
                                 {})])
        user-sql (l/emit-as :postgres `[(scratch/create-user "alice" "alice@example.com" {})])]
    [(str/includes? by-id-sql "currency_by_id")
     (str/includes? insert-sql "insert_currency")
     (str/includes? user-sql "create_user")])
  => [true true true]

  (!.js
   (xt/x:has-key? (scratch/get-schema-lookup) "Currency"))
  => true)

^{:refer postgres.sample.scratch-v3.view-currency/make-views :added "4.1"}
(fact "generates views from postgres.sample.scratch-v3 query definitions"
  (!.js
   (var views (view-currency/make-views))
   [(xtd/get-in views ["Currency" "select" "by_id" "view" "tag"])
    (xtd/get-in views ["Currency" "return" "default" "view" "tag"])])
  => ["by_id" "default"])

^{:refer postgres.sample.scratch-v3.route-currency/make-routes :added "4.1"}
(fact "generates routes for currency mutation functions"
  (!.js
   (var routes (route-currency/make-routes))
   [(xt/x:has-key? routes "api/currency/insert-currency")
    (xt/x:has-key? routes "api/currency/update-currency")
    (xt/x:has-key? routes "api/currency/delete-currency")])
  => [true true true])

^{:refer postgres.sample.scratch-v3.api-currency/CURRENCY_ALL :added "4.1"}
(fact "defines curated api descriptors over the generated views"
  (!.js
   [(xt/x:get-key api-currency/CURRENCY_ALL "table")
    (or (xt/x:has-key? api-currency/CURRENCY_ALL "event-sync")
        (xt/x:has-key? api-currency/CURRENCY_ALL "event_sync"))
    (xt/x:has-key? (xt/x:get-key api-currency/CURRENCY_BY_ID "base")
                   "brief")])
  => ["Currency" true true])

^{:refer postgres.sample.scratch-v3.realtime/postgres-change->sync-request :added "4.1"}
(fact "converts realtime postgres_changes payloads into db sync updates"
  (!.js
   [(realtime/postgres-change->sync-request
     {"eventType" "INSERT"
      "new" {"id" "AUD"
             "type" "fiat"}})
    (realtime/postgres-change->sync-request
     {"eventType" "DELETE"
      "old" {"id" "AUD"}})])
  => [{"db/sync" {"Currency" [{"id" "AUD"
                               "type" "fiat"
                               "__deleted__" false}]}}
      {"db/remove" {"Currency" ["AUD"]}}])

^{:refer postgres.sample.scratch-v3/insert-currency :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3/update-currency :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3/delete-currency :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3/get-schema :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3/get-schema-lookup :added "4.1"}
(fact "TODO")