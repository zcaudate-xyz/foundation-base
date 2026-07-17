(ns xt.db.node.proxy-util-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.node.proxy-util :as proxy-util]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-util :as proxy-util]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn.js server-node
  "creates a server node with base and page-proxy handlers installed"
  []
  (var node (substrate/node-create {"id" "proxy-util-server"}))
  (page-proxy/install-handlers node)
  (return node))

(defn.js client-node
  "creates a client node with proxy handlers installed"
  []
  (var node (substrate/node-create {"id" "proxy-util-client"}))
  (proxy-base/init-proxy-handlers node)
  (return node))

(defn.js bare-client-node
  "creates a bare client node for direct proxy tests"
  []
  (var node (substrate/node-create {"id" "proxy-util-bare-client"}))
  (page-proxy/install-handlers node)
  (return node))

(defn.js link-nodes
  "links two substrate nodes with an in-memory transport pair"
  [server client]
  (var wire (transport-memory/memory-pair {"left_id" "client" "right_id" "server"}))
  (return
   (promise/x:promise-all
    [(substrate/attach-transport client "server" (transport-memory/text-endpoint (. wire ["left"])))
     (substrate/attach-transport server "client" (transport-memory/text-endpoint (. wire ["right"])))])))

(defn.js with-linked
  "creates a linked server/client pair and invokes f with both nodes"
  [f]
  (var server (-/server-node))
  (var client (-/client-node))
  (-> (-/link-nodes server client)
      (promise/x:promise-then
       (fn [_]
         (proxy-util/set-default-transport client "server")
         (return (f server client))))))

^{:refer xt.db.node.proxy-util/set-default-transport :added "4.1"}
(fact "sets the default proxy transport id on a node"

  (!.js
   (var node (substrate/node-create {}))
   (proxy-util/set-default-transport node "my-server")
   (xtd/get-in node ["state" "adaptor_proxy" "default_transport_id"]))
  => "my-server")

^{:refer xt.db.node.proxy-util/get-default-transport :added "4.1"}
(fact "retrieves the default proxy transport id"

  (!.js
   (var node (substrate/node-create {}))
   (proxy-util/set-default-transport node "my-server")
   (proxy-util/get-default-transport node))
  => "my-server")

^{:refer xt.db.node.proxy-util/get-transport-id :added "4.1"}
(fact "resolves transport id from opts first, then node default"

  (!.js
   (var node (substrate/node-create {}))
   (proxy-util/set-default-transport node "default-server")
   [(proxy-util/get-transport-id node {"transport_id" "opt-server"})
    (proxy-util/get-transport-id node {})])
  => ["opt-server" "default-server"])

^{:refer xt.db.node.proxy-util/request-meta :added "4.1"}
(fact "builds request meta with explicit transport_id"

  (!.js
   (var node (substrate/node-create {}))
   (proxy-util/set-default-transport node "default-server")
   [(proxy-util/request-meta node {"transport_id" "opt-server"})
    (proxy-util/request-meta node {})])
  => [{"transport_id" "opt-server"}
      {"transport_id" "default-server"}])

^{:refer xt.db.node.proxy-util/request-proxy :added "4.1"}
(fact "forwards a proxy request to a server handler over the configured transport"

  (notify/wait-on :js
    (-/with-linked
     (fn [server client]
       (substrate/register-handler
        server
        "@xt.db/rpc-call"
        (fn [space args request node]
          (return {"forwarded" true}))
        nil)
       (-> (proxy-util/request-proxy
            nil
            ["db/primary" {} ["arg"]]
            {"action" "@xt.db/rpc-call"
             "meta" {}}
            client)
           (promise/x:promise-then
            (fn [out]
              (repl/notify out)))))))
  => {"forwarded" true})

^{:refer xt.db.node.proxy-util/request-client :added "4.1"}
(fact "calls an action through substrate/request with proxy meta"

  (notify/wait-on :js
    (-/with-linked
     (fn [server client]
       (substrate/register-handler
        server
        "@xt.db/rpc-call"
        (fn [space args request node]
          (return {"received" args
                   "transport" (xtd/get-in request ["meta" "transport_id"])}))
        nil)
       (-> (proxy-util/request-client client
                                      "@xt.db/rpc-call"
                                      ["db/primary" {} ["arg"]]
                                      {})
           (promise/x:promise-then
            (fn [res]
              (repl/notify res)))))))
  => (contains-in {"received" ["db/primary" {} ["arg"]]
                   "transport" "client"}))
