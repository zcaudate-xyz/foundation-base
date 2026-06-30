(ns xt.db.node.client-supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.db.node.proxy-supabase :as proxy-supabase]
             [xt.db.node.client-supabase :as client]]})

(defn.js make-node
  "creates a bare node"
  {:added "4.1"}
  [id]
  (return (substrate/node-create {"id" id
                                  "spaces" {"room/a" {"state" {}}}})))

(defn.js mock-health-handler
  "mock server-side supabase health handler"
  {:added "4.1"}
  [space args request node]
  (return {"name" "MockGoTrue"}))

(defn.js link-nodes
  "links two nodes with an in-memory transport wire"
  {:added "4.1"}
  [server client]
  (var wire (transport-memory/memory-pair {"left_id" "client"
                                           "right_id" "server"}))
  (return
   (promise/x:promise-all
    [(substrate/attach-transport
      client
      "server"
      (transport-memory/text-endpoint (. wire ["left"])))
     (substrate/attach-transport
      server
      "client"
      (transport-memory/text-endpoint (. wire ["right"])))])))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.client-supabase/request :added "4.1"}
(fact "calls a supabase action through substrate/request"

  (notify/wait-on :js
    (var node (-/make-node "local"))
    (substrate/register-handler node "@xt.supabase/health" -/mock-health-handler nil)
    (-> (client/request node "auth/supabase" "@xt.supabase/health"
                        ["auth/supabase" {}]
                        {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"name" "MockGoTrue"}))

^{:refer xt.db.node.client-supabase/health :added "4.1"}
(fact "invokes a local supabase handler"

  (notify/wait-on :js
    (var node (-/make-node "local"))
    (substrate/register-handler node "@xt.supabase/health" -/mock-health-handler nil)
    (-> (client/health node "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"name" "MockGoTrue"}))

^{:refer xt.db.node.client-supabase/health :added "4.1"}
(fact "forwards a supabase request through a proxy-supabase node"

  (notify/wait-on :js
    (var server (-/make-node "server"))
    (var client (-/make-node "client"))
    (proxy-supabase/init-proxy-handlers client)
    (substrate/register-handler server "@xt.supabase/health" -/mock-health-handler nil)
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (client/set-default-transport client "server")
           (return (client/health client "auth/supabase" {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"name" "MockGoTrue"}))
