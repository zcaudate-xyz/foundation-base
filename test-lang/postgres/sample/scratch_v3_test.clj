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
   [xt.db.system :as xdb]
   [postgres.sample.scratch-v3 :as scratch]
   [postgres.sample.scratch-v3.view-currency :as view-currency]
   [postgres.sample.scratch-v3.route-currency :as route-currency]
   [postgres.sample.scratch-v3.api-currency :as api-currency]
   [postgres.sample.scratch-v3.realtime :as realtime]
   [postgres.sample.scratch-v3.cell :as cell]
   [js.cell.binding :as binding]
   [js.cell.binding.model :as binding-model]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer postgres.sample.scratch-v3/Currency :added "4.1"}
(fact "registers Currency in the scratch-v3 application schema"
  (let [appv (app/app-create "scratch-v3")]
    [(contains? (:tables appv) 'Currency)
     (some? (get-in appv [:schema :tree :Currency]))])
  => [true true])

^{:refer postgres.sample.scratch-v3/Currency :added "4.1"}
(fact "defines currency queries, mutations, and schema bindings"
  (let [by-id-sql (l/emit-as :postgres `[(scratch/currency-by-id "AUD")])
        insert-sql (l/emit-as :postgres
                              `[(scratch/insert-currency
                                 "AUD" "fiat" "$" "$AU" 2
                                 "Australian Dollar"
                                 "Australian Dollars"
                                 "Currency for Australia"
                                 {})])]
    [(str/includes? by-id-sql "currency_by_id")
     (str/includes? insert-sql "insert_currency")])
  => [true true]

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
(fact "converts realtime postgres_changes payloads into js.cell sync updates"
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

^{:refer postgres.sample.scratch-v3.cell/compile-bindings :added "4.1"}
(fact "links the generated view registry to js.cell local and remote bindings"
  (!.js
   (var service (cell/create-service
                 {"execute" (fn [compiled _]
                              (return [true compiled]))}))
   (var [ok compiled]
        (cell/compile-bindings service))
   (var local-view (xtd/get-in compiled ["currency" "all_local"]))
   (var remote-view (xtd/get-in compiled ["currency" "all_remote"]))
   (var local-handler (xt/x:get-key local-view "handler"))
   (var remote-handler (xt/x:get-key remote-view "remoteHandler"))
   (xdb/sync-event
    (xtd/get-in service ["dbs" "currency-local"])
    ["add"
     {"Currency" [{"id" "AUD"
                   "type" "fiat"
                   "symbol" "$"
                   "native" "$AU"
                   "decimal" 2
                   "name" "Australian Dollar"
                   "plural" "Australian Dollars"
                    "description" "Currency for Australia"
                    "time_updated" 1
                    "__deleted__" false}]}])
   [ok
    (xt/x:is-function? local-handler)
    (remote-handler {"id" "link-1"})])
  => (fn [[ok local-ok remote-out]]
       (and ok
            local-ok
            (= "Currency" (xt/x:get-key remote-out "table"))
            (= "*/standard" (xt/x:get-key remote-out "select"))
            (= [] (xt/x:get-key remote-out "filters"))))

  (!.js
   (var local-db (cell/create-local-db))
   (cell/apply-realtime
    local-db
    {"eventType" "INSERT"
     "new" {"id" "USD"
            "type" "fiat"
            "symbol" "$"
            "native" "$US"
            "decimal" 2
            "name" "US Dollar"
            "plural" "US Dollars"
            "description" "Currency for the United States"
            "time_updated" 2}})
   (xdb/db-pull-sync local-db
                     (scratch/get-schema)
                     ["Currency"
                      ["id" "type" "name" "time_updated"]]))
  => [{"id" "USD"
       "type" "fiat"
       "name" "US Dollar"
       "time_updated" 2}])


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