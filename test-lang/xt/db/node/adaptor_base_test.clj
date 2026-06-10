(ns xt.db.node.adaptor-base-test
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
   :require [[js.net.conn-postgres :as js-postgres]
             [js.net.conn-sqlite :as js-sqlite]
             [js.net.http-fetch :as js-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-postgres :as impl-postgres]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-supabase :as impl-supabase]
             [xt.db.node.adaptor-base :as impl]
             [xt.net.lib-supabase :as lib-supabase]
             [xt.substrate :as substrate]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.node.adaptor-base/set-sqlite-service :added "4.1"}
(fact "set-sqlite-service registers an initialised sqlite service on the node"

  (notify/wait-on :js
    (-> (impl/set-sqlite-service
         (substrate/node-create {"services" {}})
         "db/caching"
         (js-sqlite/create {"filename" ":memory:"})
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"service_tag" (. (substrate/get-service node "db/caching") ["::"])
             "service_client" (. (. (substrate/get-service node "db/caching") ["client"]) ["::"])
             "service_count" (xt/x:len (xt/x:obj-keys (substrate/get-services node)))})))))
  => {"service_tag" "db.client.sqlite"
      "service_client" "js.net.conn-sqlite"
      "service_count" 1})

^{:refer xt.db.node.adaptor-base/set-postgres-service :added "4.1"}
(fact "set-postgres-service registers an initialised postgres service on the node"

  (notify/wait-on :js
    (-> (impl/set-postgres-service
         (substrate/node-create {"services" {}})
         "db/primary"
         (js-postgres/create {"database" "test-scratch"})
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"service_tag" (. (substrate/get-service node "db/primary") ["::"])
             "service_client" (. (. (substrate/get-service node "db/primary") ["client"]) ["::"])
             "service_count" (xt/x:len (xt/x:obj-keys (substrate/get-services node)))})))))
  => {"service_tag" "db.client.postgres"
      "service_client" "js.net.conn-postgres"
      "service_count" 1})

^{:refer xt.db.node.adaptor-base/set-memory-service :added "4.1"}
(fact "set-memory-service registers a memory service on the node"

  (notify/wait-on :js
    (-> (impl/set-memory-service
         (substrate/node-create {"services" {}})
         "db/cache"
         nil
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"service_tag" (. (substrate/get-service node "db/cache") ["::"])
             "service_count" (xt/x:len (xt/x:obj-keys (substrate/get-services node)))})))))
  => {"service_tag" "db.impl.memory"
      "service_count" 1})

^{:refer xt.db.node.adaptor-base/set-supabase-service :added "4.1"}
(fact "set-supabase-service registers a supabase service on the node"

  (notify/wait-on :js
    (-> (impl/set-supabase-service
         (substrate/node-create {"services" {}})
         "db/primary"
         (lib-supabase/create-client
          (js-fetch/create-methods)
          "127.0.0.1"
          (@! (-> docker-min/+config+ :api :port))
          false
          ""
          (@! (-> docker-min/+config+ :api :anon-key)))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"service_tag" (. (substrate/get-service node "db/primary") ["::"])
             "service_client" (. (. (substrate/get-service node "db/primary") ["client"]) ["::"])
             "service_count" (xt/x:len (xt/x:obj-keys (substrate/get-services node)))})))))
  => {"service_tag" "db.impl.supabase"
      "service_client" "net.superbase"
      "service_count" 1})

^{:refer xt.db.node.adaptor-base/db-init :added "4.1"}
(fact "TODO"

  
  
  {"services"
   {"db/common"   {"schema"  {}
                   "lookup"  {}}
    "db/primary"  'client
    "db/caching"  'client}})

^{:refer xt.db.node.adaptor-base/call-db-handler :added "4.1"}
(fact "TODO")
