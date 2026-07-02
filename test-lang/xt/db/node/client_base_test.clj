(ns xt.db.node.client-base-test
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
   :require [[js.net.http-fetch :as js-fetch]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.http-util :as http-util]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.runtime :as runtime]
             [xt.db.node.client-base :as client]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.system.main :as main]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.net.addon-supabase :as addon]]})


(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))


(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(fact:global
 {:setup [(l/rt:restart)
          ]
  :teardown [(l/rt:stop)]})

(comment
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (adaptor/init-handlers node)
    (-> (client/init-base node {} {} {} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  )

^{:refer xt.db.node.client-base/init-base :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "invokes a local base handler"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (adaptor/init-handlers node)
    (-> (client/init-base node {"primary" {"type" "supabase"
                                           "defaults" (@! local-min/+config-supabase-anon+)}
                                "caching" {"type" "sqlite"
                                           "defaults" {"filename" ":memory:"}}}
                          -/Schema
                          -/SchemaLookup
                          {})
        (repl/notify)))
  => (contains-in
      {"handlers" {}, "services" {"db/caching" map?, "db/primary" map?, "db/common" map?},
       "id" "node-LzspZD",
       "spaces" {"__NODE__" {"id" "__NODE__", "state" {}, "meta" {}}},
       "::" "substrate"})

  
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (adaptor/init-handlers node)
    (-> (substrate/request node
                           nil
                           "@xt.db/init-base"
                           [{"primary" {"type" "supabase"
                                        "defaults" (@! local-min/+config-supabase-anon+)}
                             "caching" {"type" "sqlite"
                                        "defaults" {"filename" ":memory:"}}}
                            -/Schema
                            -/SchemaLookup])
        (repl/notify)))
  => (contains-in
      {"handlers" {}, "services" {"db/caching" map?, "db/primary" map?, "db/common" map?},
       "id" "node-LzspZD",
       "spaces" {"__NODE__" {"id" "__NODE__", "state" {}, "meta" {}}},
       "::" "substrate"})

  (notify/wait-on :js
    (var server (substrate/node-create {}))
    (var client (substrate/node-create {}))
    (runtime/init-server server)
    (runtime/init-server-proxy client)
    (transport-memory/link-pair server client)
    (repl/notify server)
    #_(-> (client/init-base client {"primary" {"type" "supabase"
                                               "defaults" (@! local-min/+config-supabase-anon+)}
                                    "caching" {"type" "sqlite"
                                               "defaults" {"filename" ":memory:"}}}
                            -/Schema
                            -/SchemaLookup
                            {})
          (repl/notify)))
  )

^{:refer xt.db.node.client-base/init-base :added "4.1"}
(fact "forwards a base request through a proxy-base node"

  (notify/wait-on :js
    (var server (-/make-node "server"))
    (var client (-/make-node "client"))
    (proxy-base/init-proxy-handlers client)
    (substrate/register-handler server "@xt.db/init-base" -/mock-init-base-handler nil)
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (client/init-base client {} {} {} {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => {"status" "ok" "node_id" "server"})
