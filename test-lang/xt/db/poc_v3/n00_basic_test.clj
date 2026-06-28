(ns xt.db.poc-v3.n00-basic-test
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
  :skip (not (std.lib.env/program-exists? "supabase"))
  :setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc-v3.n00-basic-test/schema-has-tables :added "4.1"}
(fact "scratch_v3 schema contains the new User/UserProfile/Wallet/Asset tables"
  (let [appv (pg/app "scratch_v3")]
    [(contains? (:tables appv) 'User)
     (contains? (:tables appv) 'UserProfile)
     (contains? (:tables appv) 'Wallet)
     (contains? (:tables appv) 'Asset)
     (contains? (:tables appv) 'Currency)])
  => [true true true true true])
