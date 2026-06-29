(ns xt.db.poc-v3.worker-threads-test
  "Minimal debug test."
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.sample.scratch-v3 :as scratch-v3]
            [postgres.core.supabase :as s]
            [xt.db.poc-v3.worker-threads :as worker-threads]
            [xt.db.poc-v3.worker-threads-script :as worker-threads-script]
            [xt.db.poc-v3.sharedworker :as sharedworker]))

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
             [xt.db.poc-v3.worker-threads :as worker-threads]
             [xt.db.poc-v3.sharedworker :as sharedworker]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/wait-for-postgrest-ready "scratch_v3" "UserProfile")
          (scratch-v3/insert-user +account-id+ "poc-alice" "alice@poc.local" true false {})
          (scratch-v3/insert-user-profile +account-id+ "Alicia" "Adams" "EN" "scratch v3 user" {})
          (def +worker-script-path+
            (worker-threads-script/write-worker-script!
             (str (System/getProperty "user.dir") "/.tmp")))]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc-v3.worker-threads-test/var-then
  :added "4.1"}
(fact "promise-then on var"
  (notify/wait-on [:js 30000]
    (var p (promise/x:promise (fn [] (return {"x" 1}))))
    (return
     (promise/x:promise-then
      p
      (fn [v]
        (return (repl/notify {"v" v}))))))
  => (contains-in {"v" {"x" 1}}))
