(ns xt.db.poc-v3.n02-wallet-asset-test
  (:use code.test)
  (:require [hara.lang :as l]
            [scaffold.supabase.local-min :as local-min]
            [postgres.sample.scratch-v3 :as scratch-v3]
            [postgres.core :as pg]
            [postgres.core.supabase :as s]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v3 :as scratch-v3]
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
  (s/grant-usage #{"scratch_v3"}))

(fact:global
 {
  :setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc-v3.n02-wallet-asset-test/seed-currency :added "4.1"}
(fact "seeds a currency for asset tests"
  (let [out (scratch-v3/insert-currency
             "STC" "digital" "S" "S" 2
             "StatsCoin" "StatsCoins" "scratch v3 currency"
             {})]
    [(map? out)
     (= "STC" (get out :id))])
  => [true true])

^{:refer xt.db.poc-v3.n02-wallet-asset-test/ensure-wallet :added "4.1"}
(fact "creates a default Wallet for a User"
  (let [id (str (java.util.UUID/randomUUID))
        _ (scratch-v3/insert-user id "poc-wallet" "wallet@poc.local" true false {})
        wallet (scratch-v3/ensure-wallet id {})]
    [(map? wallet)
     (= id (str (get wallet :owner-id)))
     (= "default" (get wallet :slug))])
  => [true true true])

^{:refer xt.db.poc-v3.n02-wallet-asset-test/ensure-asset :added "4.1"}
(fact "creates an Asset for a wallet/currency pair"
  (let [id (str (java.util.UUID/randomUUID))
        _ (scratch-v3/insert-user id "poc-asset" "asset@poc.local" true false {})
        wallet (scratch-v3/ensure-wallet id {})
        asset (scratch-v3/ensure-asset (str (get wallet :id)) "STC" {})]
    [(map? asset)
     (= (str (get wallet :id)) (str (get asset :wallet-id)))
     (= "STC" (get asset :currency-id))
     (= 0 (get asset :balance))])
  => [true true true true])

^{:refer xt.db.poc-v3.n02-wallet-asset-test/credit-deduct-asset :added "4.1"}
(fact "credits and deducts an Asset balance"
  (let [id (str (java.util.UUID/randomUUID))
        _ (scratch-v3/insert-user id "poc-balance" "balance@poc.local" true false {})
        wallet (scratch-v3/ensure-wallet id {})
        asset (scratch-v3/ensure-asset (str (get wallet :id)) "STC" {})
        asset-id (str (get asset :id))
        credited (scratch-v3/asset-credit asset-id 100 {})
        deducted (scratch-v3/asset-deduct asset-id 30 {})]
    [(= 100 (get credited :balance))
     (= 1 (get credited :count-tx))
     (= 70 (get deducted :balance))
     (= 2 (get deducted :count-tx))])
  => [true true true true])
