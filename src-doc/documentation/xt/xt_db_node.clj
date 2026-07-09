(ns documentation.xt-db-node
  (:require [hara.lang :as l]
            [xt.db.node.kernel-base :as kernel]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.kernel-base :as kernel]
             [xt.db.node.client-base :as client]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.substrate.page-core :as page-core]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.db.node"
         :subtitle "Client, kernel, proxy, and runtime node layers."
         :lead "`xt.db.node` packages database behavior for node-like runtimes: browser clients, Supabase clients, kernels, proxies, proxy utilities, and runtime adapters."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Client/server database flows need message boundaries. The node layer separates client calls, kernel execution, proxying, and runtime setup so the same database model can run in workers, browsers, and service contexts."

[[:chapter {:title "Internal usage" :link "internal"}]]

"Tests under `test-lang/xt/db/node` cover browser clients, base clients, Supabase clients, kernels, proxies, and runtime behavior. POC tests in `test-lang/xt/db/poc` show worker and shared-worker usage."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Kernel config and lifecycle"}]]

"The kernel layer sets up the three services every db node needs: a common config service, a primary backend, and a caching backend. `kernel-create-config` normalises the input, and `kernel-check-exists` verifies that all three services are present."

(fact "normalise a db node config"
  ^{:refer xt.db.node.kernel-base/kernel-create-config :added "4.1"}
  (!.js
    (kernel/kernel-create-config
     {:primary {:type "memory"}
      :caching {:type "memory"}}))
  => {"common" {"id" "db/common"}
      "primary" {"id" "db/primary" "type" "memory"}
      "caching" {"id" "db/caching" "type" "memory"}})

(fact "required services are missing before setup"
  ^{:refer xt.db.node.kernel-base/kernel-check-exists :added "4.1"}
  (!.js
    (kernel/kernel-check-exists
     (substrate/node-create {})
     {:primary {:type "memory"} :caching {:type "memory"}}))
  => false)

(fact "required services are present after they are registered"
  ^{:refer xt.db.node.kernel-base/kernel-check-exists :added "4.1"}
  (!.js
    (var node (substrate/node-create {}))
    (substrate/set-service node "db/common" {})
    (substrate/set-service node "db/primary" {})
    (substrate/set-service node "db/caching" {})
    (kernel/kernel-check-exists node {:primary {:type "memory"} :caching {:type "memory"}}))
  => true)

(fact "set up and tear down base services"
  ^{:refer xt.db.node.kernel-base/kernel-setup-main :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                  {:primary {:type "memory" :defaults {}}
                                   :caching {:type "memory" :defaults {}}}
                                  {}
                                  {})
        (promise/x:promise-then
         (fn []
           (return (kernel/kernel-teardown-main node
                                                {:primary {:type "memory" :defaults {}}
                                                 :caching {:type "memory" :defaults {}}}))))
        (promise/x:promise-then (fn [out] (repl/notify out)))))
  => {"status" "teardown"
      "data" {"common" {"id" "db/common"}
              "primary" {"id" "db/primary" "type" "memory" "defaults" {}}
              "caching" {"id" "db/caching" "type" "memory" "defaults" {}}}})

(fact "initialise services idempotently"
  ^{:refer xt.db.node.kernel-base/kernel-init-main :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var config {:primary {:type "memory" :defaults {}}
                 :caching {:type "memory" :defaults {}}})
    (-> (kernel/kernel-setup-main node config {} {})
        (promise/x:promise-then
         (fn []
           (return (kernel/kernel-init-main node config {} {}))))
        (promise/x:promise-then (fn [out] (repl/notify out)))))
  => {"status" "no_change"
      "data" {"common" {"id" "db/common"}
              "primary" {"id" "db/primary" "type" "memory" "defaults" {}}
              "caching" {"id" "db/caching" "type" "memory" "defaults" {}}}})

[[:section {:title "Installing handlers"}]]

"`init-handlers` registers all the `@xt.db/*` substrate actions on a node, so client functions can invoke them locally. `list-substrate-fn` lists the public actions exposed by a namespace."

(fact "register base db handlers on a node"
  ^{:refer xt.db.node.kernel-base/init-handlers :added "4.1"}
  (!.js
    (var node (substrate/node-create {}))
    (kernel/init-handlers node)
    (xt/x:len (substrate/list-handlers node)))
  => 24)

(fact "list the substrate actions exposed by the kernel"
  ^{:refer xt.db.node.kernel-base/list-substrate-fn :added "4.1"}
  (set (map (comp :substrate/fn meta second)
            (kernel/list-substrate-fn 'xt.db.node.kernel-base)))
  => #{"@xt.db/attach-model"
       "@xt.db/dataview-attach-model"
       "@xt.db/dataview-call"
       "@xt.db/detach-model"
       "@xt.db/kernel-init"
       "@xt.db/kernel-setup"
       "@xt.db/kernel-teardown"
       "@xt.db/pull-attach-model"
       "@xt.db/pull-call"
       "@xt.db/pull-cached"
       "@xt.db/rpc-attach-model"
       "@xt.db/rpc-call"
       "@xt.db/subscribe-db"
       "@xt.db/sync-cached"
       "@xt.db/unsubscribe-db"})

[[:section {:title "Client actions"}]]

"The client namespace issues the same substrate requests, so calling `client-base/kernel-init` works whether the kernel handlers are installed locally or on the other side of a transport."

(fact "initialise a node through the client API"
  ^{:refer xt.db.node.client-base/kernel-init :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (kernel/init-handlers node)
    (-> (client/kernel-init node
                            {:primary {:type "memory" :defaults {}}
                             :caching {:type "memory" :defaults {}}}
                            {}
                            {}
                            {})
        (promise/x:promise-then (fn [out] (repl/notify out)))))
  => {"status" "setup"
      "data" {"common" {"id" "db/common"}
              "primary" {"id" "db/primary" "type" "memory" "defaults" {}}
              "caching" {"id" "db/caching" "type" "memory" "defaults" {}}}})

[[:section {:title "Proxy forwarding"}]]

"`proxy-base` installs client-side handlers that forward the same `@xt.db/*` actions to a remote kernel. `proxy-util/set-default-transport` tells the proxy which transport to use when no explicit `transport_id` is supplied."

(fact "forward a client request to a remote kernel over a memory transport"
  ^{:refer xt.db.node.proxy-base/init-proxy-handlers :added "4.1"}
  ^{:refer xt.db.node.proxy-util/set-default-transport :added "4.1"}
  (notify/wait-on :js
    (var server (substrate/node-create {}))
    (kernel/init-handlers server)
    (var client (substrate/node-create {}))
    (proxy-base/init-proxy-handlers client)
    (var wire (transport-memory/memory-pair {:left_id "client" :right_id "server"}))
    (-> (promise/x:promise-all
         [(substrate/attach-transport client "server" (transport-memory/text-endpoint (. wire ["left"])))
          (substrate/attach-transport server "client" (transport-memory/text-endpoint (. wire ["right"])))])
        (promise/x:promise-then
         (fn []
           (proxy-util/set-default-transport client "server")
           (return
            (client/kernel-init client
                                {:primary {:type "memory" :defaults {}}
                                 :caching {:type "memory" :defaults {}}}
                                {}
                                {}
                                {}))))
        (promise/x:promise-then (fn [out] (repl/notify out)))))
  => {"status" "setup"
      "data" {"common" {"id" "db/common"}
              "primary" {"id" "db/primary" "type" "memory" "defaults" {}}
              "caching" {"id" "db/caching" "type" "memory" "defaults" {}}}})

(fact "resolve the transport used for proxy requests"
  ^{:refer xt.db.node.proxy-util/get-transport-id :added "4.1"}
  ^{:refer xt.db.node.proxy-util/request-meta :added "4.1"}
  (!.js
    (var node (substrate/node-create {}))
    (proxy-util/set-default-transport node "my-server")
    [(proxy-util/get-transport-id node {"transport_id" "opt-server"})
     (proxy-util/get-transport-id node {})
     (proxy-util/request-meta node {})])
  => ["opt-server" "my-server" {"transport_id" "my-server"}])

[[:section {:title "Page models"}]]

"Page models attach stateful views to a node. Once a model is attached, its handler receives input events and produces output."

(fact "attach a model, send input, and read output"
  ^{:refer xt.db.node.client-base/attach-model :added "4.1"}
  ^{:refer xt.substrate.page-core/model-set-input :added "4.1"}
  ^{:refer xt.substrate.page-core/model-get-output :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (kernel/init-handlers node)
    (-> (client/kernel-init node
                            {:primary {:type "memory" :defaults {}}
                             :caching {:type "memory" :defaults {}}}
                            {} {} {})
        (promise/x:promise-then
         (fn []
           (return
            (client/attach-model node
                                 "db/caching"
                                 {:space_id "room/a"
                                  :group_id "demo"
                                  :model_id "echo"}
                                 {:handler (fn [ctx] (return (. ctx ["args"])))
                                  :defaults {:args [1 2 3]}}
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return (page-core/model-set-input node "room/a" "demo" "echo"
                                              {:data [4 5 6]}
                                              {}))))
        (promise/x:promise-then
         (fn []
           (return (page-core/model-get-output node "room/a" "demo" "echo"))))
        (promise/x:promise-then (fn [out] (repl/notify out)))))
  => [4 5 6])

(fact "detach a page model"
  ^{:refer xt.db.node.client-base/detach-model :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (kernel/init-handlers node)
    (-> (client/kernel-init node
                            {:primary {:type "memory" :defaults {}}
                             :caching {:type "memory" :defaults {}}}
                            {} {} {})
        (promise/x:promise-then
         (fn []
           (return
            (client/attach-model node
                                 "db/caching"
                                 {:space_id "room/a"
                                  :group_id "demo"
                                  :model_id "echo"}
                                 {:handler (fn [ctx] (return [1]))
                                  :defaults {:args []}}
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/detach-model node
                                 "db/caching"
                                 {:space_id "room/a"
                                  :group_id "demo"
                                  :model_id "echo"}
                                 {}))))
        (promise/x:promise-then (fn [out] (repl/notify out)))))
  => {"status" "removed"
      "space" "room/a"
      "group" "demo"
      "model" "echo"})

[[:chapter {:title "API" :link "api"}]]

