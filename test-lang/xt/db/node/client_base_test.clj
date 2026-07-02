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
             [xt.db.node.kernel-base :as kernel-base]
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


(comment
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (kernel-base/init-handlers node)
    (-> (client/kernel-init node {} {} {} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  )

(comment

  (notify/wait-on :js
    (var server (substrate/node-create {}))
    (var client (substrate/node-create {}))
    (runtime/init-server server)
    (runtime/init-server-proxy client)
    (transport-memory/link-pair server client)
    
    (-> (client/kernel-init client {"primary" {"type" "supabase"
                                             "defaults" (@! local-min/+config-supabase-anon+)}
                                  "caching" {"type" "sqlite"
                                             "defaults" {"filename" ":memory:"}}}
                          -/Schema
                          -/SchemaLookup
                          {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out message))))))

  )

^{:refer xt.db.node.client-base/kernel-init :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "invokes a local base handler"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (kernel-base/init-handlers node)
    (-> (client/kernel-init node {"primary" {"type" "supabase"
                                           "defaults" (@! local-min/+config-supabase-anon+)}
                                "caching" {"type" "sqlite"
                                           "defaults" {"filename" ":memory:"}}}
                          -/Schema
                          -/SchemaLookup
                          {})
        (repl/notify)))
  => (contains-in
      {"::" "substrate"
       "services" {"db/caching" map?, "db/primary" map?, "db/common" map?}})


  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (kernel-base/init-handlers node)
    (-> (substrate/request node
                           nil
                           "@xt.db/kernel-init"
                           [{"primary" {"type" "supabase"
                                        "defaults" (@! local-min/+config-supabase-anon+)}
                             "caching" {"type" "sqlite"
                                        "defaults" {"filename" ":memory:"}}}
                            -/Schema
                            -/SchemaLookup])
        (repl/notify)))
  => (contains-in
      {"::" "substrate"
       "services" {"db/caching" map?, "db/primary" map?, "db/common" map?}}))


