(ns xt.db.system.impl-supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> docker-min/+config+ :db :host)
              :port   (-> docker-min/+config+ :db :port)
              :user   (-> docker-min/+config+ :db :user)
              :pass   (-> docker-min/+config+ :db :password)
              :dbname (-> docker-min/+config+ :db :database)
              :startup  docker-min/start-supabase
              :shutdown docker-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}}))

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(l/script- :js
  {:runtime :basic
   :require [[js.net.http-fetch :as js-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-supabase :as impl]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})


^{:refer xt.db.system.impl-supabase/pull-async :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase/rpc-call-async :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase/impl-supabase :added "4.1"}
(fact "TODO")
