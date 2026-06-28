(ns xt.db.poc-v3.n01-user-profile-test
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

^{:refer xt.db.poc-v3.n01-user-profile-test/insert-user :added "4.1"}
(fact "creates a User row through the helper"
  (let [id (str (java.util.UUID/randomUUID))
        user (scratch-v3/insert-user id "poc-alice" "alice@poc.local" true false {})]
    [(map? user)
     (= id (str (get user :id)))
     (= "poc-alice" (get user :nickname))
     (= "alice@poc.local" (get user :email))])
  => [true true true true])

^{:refer xt.db.poc-v3.n01-user-profile-test/create-and-read-profile :added "4.1"}
(fact "creates a UserProfile linked to a User"
  (let [id (str (java.util.UUID/randomUUID))
        user (scratch-v3/insert-user id "poc-bob" "bob@poc.local" true false {})
        profile (scratch-v3/insert-user-profile id
                                                "Bob"
                                                "Builder"
                                                "EN"
                                                "scratch v3 guide user"
                                                {})]
    [(map? profile)
     (= id (str (get profile :account-id)))
     (= "Bob" (get profile :first-name))
     (= "Builder" (get profile :last-name))])
  => [true true true true])

^{:refer xt.db.poc-v3.n01-user-profile-test/profile-by-account :added "4.1"}
(fact "reads a UserProfile by account id"
  (let [id (str (java.util.UUID/randomUUID))]
    (scratch-v3/insert-user id "poc-carol" "carol@poc.local" true false {})
    (scratch-v3/insert-user-profile id
                                    "Carol"
                                    "Danvers"
                                    "EN"
                                    ""
                                    {})
    (let [found (pg/t:get scratch-v3/UserProfile {:where {:account id}})]
      [(map? found)
       (= "Carol" (get found :first-name))
       (= "Danvers" (get found :last-name))]))
  => [true true true])
