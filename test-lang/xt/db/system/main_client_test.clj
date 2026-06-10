(ns xt.db.system.main-client-test
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
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.lib-supabase :as lib-supabase]
             [xt.net.conn-sql :as conn-sql]
             [xt.db.system.main-client :as main-client]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.system.main-client/create-client :added "4.1"}
(fact "returns nil for unsupported client types"

  (!.js
    (main-client/create-client "memory" {}))
  => nil)

^{:refer xt.db.system.main-client/create-client.sqlite :added "4.1"}
(fact "creates a sqlite client"
  
  (!.js
    (main-client/create-client "sqlite" {"filename" ":memory:"}))
  => (contains-in
      {"::" "js.net.conn-sqlite"
       "defaults" {"filename" ":memory:"}}))

^{:refer xt.db.system.main-client/create-client.postgres :added "4.1"}
(fact "creates a postgres client"

  (notify/wait-on :js
    (-> (main-client/create-client "postgres"
                                   (@! (docker-min/+config+ :db)))
        (conn-sql/connect {})
        (promise/x:promise-then
         (fn [client]
           (return
            (conn-sql/query-async client "SELECT 1;"))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => 1)

^{:refer xt.db.system.main-client/create-client.supabase :added "4.1"}
(fact "creates a live supabase client"

  (notify/wait-on :js
    (-> (main-client/create-client
         "supabase"
         {"host" "127.0.0.1"
          "port" (@! (-> docker-min/+config+ :api :port))
          "secured" false
          "basepath" ""
          "apikey" (@! (-> docker-min/+config+ :api :anon-key))})
        (lib-supabase/health {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(xt/x:get-key out "status")
                         (xt/x:get-key (xt/x:get-key out "body") "name")])))))
  => [200 "GoTrue"])
