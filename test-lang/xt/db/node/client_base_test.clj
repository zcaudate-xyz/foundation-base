(ns xt.db.node.client-base-test
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
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.client-base :as client]]})

(defn.js make-node
  "creates a bare node"
  {:added "4.1"}
  [id]
  (return (substrate/node-create {"id" id
                                  "spaces" {"room/a" {"state" {}}}})))

(defn.js mock-init-base-handler
  "mock server-side @xt.db/init-base handler"
  {:added "4.1"}
  [space args request node]
  (return {"status" "ok" "node_id" (xt/x:get-key node "id")}))

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

^{:refer xt.db.node.client-base/request :added "4.1"}
(fact "calls a base db action through substrate/request"

  (notify/wait-on :js
    (var node (-/make-node "local"))
    (substrate/register-handler node "@xt.db/init-base" -/mock-init-base-handler nil)
    (-> (client/request node "@xt.db/init-base" [{} {} {}] {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"status" "ok" "node_id" "local"})

^{:refer xt.db.node.client-base/init-base :added "4.1"}
(fact "invokes a local base handler"

  (notify/wait-on :js
    (var node (-/make-node "local"))
    (substrate/register-handler node "@xt.db/init-base" -/mock-init-base-handler nil)
    (-> (client/init-base node {} {} {} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"status" "ok" "node_id" "local"})

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
           (client/set-default-transport client "server")
           (return (client/init-base client {} {} {} {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => {"status" "ok" "node_id" "server"})
