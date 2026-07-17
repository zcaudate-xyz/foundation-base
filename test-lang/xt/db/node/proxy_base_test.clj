(ns xt.db.node.proxy-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.kernel-base :as adaptor]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn.js server-node
  "creates a server node with kernel-base and page-proxy handlers installed"
  {:added "4.1"}
  []
  (var node (substrate/node-create {"id" "proxy-base-server"}))
  (adaptor/init-handlers node)
  (page-proxy/install-handlers node)
  (return node))

(defn.js client-node
  "creates a client node with proxy-base handlers installed"
  {:added "4.1"}
  []
  (var node (substrate/node-create {"id" "proxy-base-client"}))
  (proxy-base/init-proxy-handlers node)
  (return node))

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

(defn.js with-linked
  "creates a linked server/client pair and invokes f with both nodes"
  {:added "4.1"}
  [f]
  (var server (-/server-node))
  (var client (-/client-node))
  (-> (-/link-nodes server client)
      (promise/x:promise-then
       (fn [_]
         (proxy-util/set-default-transport client "server")
         (return (f server client))))))

(defn.js init-server-base
  "initialises memory primary/caching services on the server"
  {:added "4.1"}
  [server]
  (return
   (substrate/request server
                      nil
                      "@xt.db/kernel-init"
                      [{"primary" {"type" "memory" "defaults" {}}
                        "caching" {"type" "memory" "defaults" {}}}
                       {}
                       {}]
                      {})))

(defn.js with-linked-base
  "creates a linked pair and initialises server base services"
  {:added "4.1"}
  [f]
  (-/with-linked
   (fn [server client]
     (-> (-/init-server-base server)
         (promise/x:promise-then
          (fn [_]
            (return (f server client))))))))

^{:refer xt.db.node.proxy-util/request-proxy :added "4.1" :seedgen/base {:ruby {:suppress true}}}
(fact "forwards a call action to the server"

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
            ["db/primary" {} []]
            {"action" "@xt.db/rpc-call"
             "meta" {}}
            client)
           (promise/x:promise-then
            (fn [out]
              (repl/notify out)))))))
  => {"forwarded" true})

^{:refer xt.db.node.proxy-base/attach-forward-handler :added "4.1" :seedgen/base {:ruby {:suppress true}}}
(fact "forwards attach-model and opens a proxy group on the client"

  (notify/wait-on :js
    (-/with-linked
     (fn [server client]
       (substrate/register-handler
        server
        "@xt.db/attach-model"
        (fn [space args request node]
          (page-core/group-add-attach
           node
           "room/a"
           "demo"
           {"echo" {"handler" (fn [ctx] (return [1]))
                    "defaults" {"args" []}}})
          (return {"status" "attached"
                   "space" "room/a"
                   "group" "demo"
                   "model" "echo"}))
        nil)
       (promise/x:promise-catch
        (-> (proxy-base/attach-forward-handler
             nil
             ["db/primary"
              {"space_id" "room/a"
               "group_id" "demo"
               "model_id" "echo"}]
             {"action" "@xt.db/attach-model"
              "meta" {}}
             client)
            (promise/x:promise-then
             (fn [_]
               (var group (page-core/group-get client "room/a" "demo"))
               (repl/notify
                {"exists" (xt/x:not-nil? group)
                 "proxy"  (xt/x:not-nil? (page-core/proxy-group? group))}))))
        (fn [err]
          (repl/notify
           {"error" (xt/x:ex-message err)
            "data"  (xt/x:ex-data err)}))))))
  => {"exists" true
      "proxy"  true})

^{:refer xt.db.node.proxy-base/detach-forward-handler :added "4.1" :seedgen/base {:ruby {:suppress true}}}
(fact "forwards detach-model and closes the proxy group on the client"

  (notify/wait-on :js
    (-/with-linked
     (fn [server client]
       (substrate/register-handler
        server
        "@xt.db/attach-model"
        (fn [space args request node]
          (page-core/group-add-attach
           node
           "room/a"
           "demo"
           {"echo" {"handler" (fn [ctx] (return [1]))
                    "defaults" {"args" []}}})
          (return {"status" "attached"
                   "space" "room/a"
                   "group" "demo"
                   "model" "echo"}))
        nil)
       (substrate/register-handler
        server
        "@xt.db/detach-model"
        (fn [space args request node]
          (page-core/model-remove node "room/a" "demo" "echo")
          (return {"status" "removed"
                   "space" "room/a"
                   "group" "demo"
                   "model" "echo"}))
        nil)
       (promise/x:promise-catch
        (-> (proxy-base/attach-forward-handler
             nil
             ["db/primary"
              {"space_id" "room/a"
               "group_id" "demo"
               "model_id" "echo"}]
             {"action" "@xt.db/attach-model"
              "meta" {}}
             client)
            (promise/x:promise-then
             (fn [_]
               (return
                (proxy-base/detach-forward-handler
                 nil
                 ["db/primary"
                  {"space_id" "room/a"
                   "group_id" "demo"
                   "model_id" "echo"}]
                 {"action" "@xt.db/detach-model"
                  "meta" {}}
                 client))))
            (promise/x:promise-then
             (fn [_]
               (repl/notify
                (page-core/group-get client "room/a" "demo")))))
        (fn [err]
          (repl/notify
           {"error" (xt/x:ex-message err)
            "data"  (xt/x:ex-data err)}))))))
  => nil)

^{:refer xt.db.node.proxy-base/init-proxy-handlers :added "4.1"}
(fact "registers all base proxy actions on the client node"

  (!.js
   (var node (-/client-node))
   (var handlers (xt/x:get-key node "handlers"))
   (return {"count"   (xt/x:len (xtd/obj-keys handlers))
           "call"    (xt/x:get-key handlers "@xt.db/kernel-init")
           "attach"  (xt/x:get-key handlers "@xt.db/attach-model")
           "detach"  (xt/x:get-key handlers "@xt.db/detach-model")}))
  => {"count"   24
      "call"    {"id" "@xt.db/kernel-init" "meta" {}}
      "attach"  {"id" "@xt.db/attach-model" "meta" {}}
      "detach"  {"id" "@xt.db/detach-model" "meta" {}}})