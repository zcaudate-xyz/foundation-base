(ns xt.db.system.main-client-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
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
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-supabase :as lib-supabase]
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
                                   (@! (local-min/+config+ :db)))
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
          "port" (@! (-> local-min/+config+ :api :port))
          "secured" false
          "basepath" ""
          "apikey" (@! (-> local-min/+config+ :api :anon-key))})
        (lib-supabase/health {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(xt/x:get-key out "status")
                         (xt/x:get-key (xt/x:get-key out "body") "name")])))))
  => [200 "GoTrue"])
