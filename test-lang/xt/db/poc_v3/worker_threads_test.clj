(ns xt.db.poc-v3.worker-threads-test
  "End-to-end test for the `xt.db.poc-v3.worker-threads` setup."
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.sample.scratch-v3 :as scratch-v3]
            [postgres.core.supabase :as s]
            [xt.db.poc-v3.worker-threads :as wt]
            [xt.db.poc-v3.worker-threads-script :as worker-threads-script]))

(def +account-id+ "11111111-1111-1111-1111-111111111111")

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

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.poc-v3.worker-threads :as wt]]
   :emit {:lang/format :commonjs}})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (scratch-v3/insert-user +account-id+ "poc-alice" "alice@poc.local" true false {})
          (scratch-v3/insert-user-profile +account-id+ "Alicia" "Adams" "EN" "scratch v3 user" {})
          (def +worker-script-path+
            (worker-threads-script/write-worker-script!
             (str (System/getProperty "user.dir") "/.tmp")))]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc-v3.worker-threads-test/profile-reads
  :added "4.1"}
(fact "UserProfile reads through worker-thread RPC"
  (notify/wait-on [:js 60000]
    (var p (wt/with-profile-read
             (@! +worker-script-path+)
             (@! +account-id+)))
    (-> p
        (promise/x:promise-then
         (fn [out]
           (repl/notify {"out" out})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"err" (. err ["message"])
                         "stack" (. err ["stack"])})))))
  => (contains-in {"out" [{"first_name" "Alicia"
                           "last_name" "Adams"}]}))

^{:refer xt.db.poc-v3.worker-threads-test/profile-updates
  :added "4.1"}
(fact "UserProfile updates through worker-thread RPC"
  (notify/wait-on [:js 60000]
    (var p (wt/with-profile-update
             (@! +worker-script-path+)
             (@! +account-id+)
             {"first_name" "Maria"}))
    (promise/x:promise-then p
     (fn [out]
       (repl/notify {"out" out}))))
  => (contains-in {"out" [{"first_name" "Maria"
                           "last_name" "Adams"}]}))
