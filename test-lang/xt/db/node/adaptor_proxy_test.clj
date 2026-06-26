(ns xt.db.node.adaptor-proxy-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.lang.common-protocol :as proto]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.node.adaptor-base :as adaptor-base]
             [xt.db.node.adaptor-proxy :as adaptor-proxy]]})

(defn.js make-server-node
  "creates a server node with db adaptor handlers"
  {:added "4.1"}
  []
  (return (substrate/node-create {"id" "server"
                                  "spaces" {"room/a" {"state" {}}}})))

(defn.js make-client-node
  "creates a bare client node"
  {:added "4.1"}
  []
  (return (substrate/node-create {"id" "client"
                                  "spaces" {"room/a" {"state" {}}}})))

(defn.js setup-server-db
  "initialises memory-backed db services on the server"
  {:added "4.1"}
  [server]
  (return
   (adaptor-base/init-adaptor-main
    server
    {"primary" {"type" "memory" "defaults" {}}
     "caching" {"type" "memory" "defaults" {}}}
    {}
    {})))

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

^{:refer xt.db.node.adaptor-proxy/init-adaptor :added "4.1"}
(fact "client proxy forwards init-adaptor and installs service stubs"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (adaptor-proxy/set-default-transport client "server")
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-proxy/init-adaptor
             client
             {"primary" {"type" "memory" "defaults" {}}
              "caching" {"type" "memory" "defaults" {}}}
             {}
             {}
             {}))))
        (promise/x:promise-then
         (fn [summary]
           (var primary (substrate/get-service client "db/primary"))
           (var caching (substrate/get-service client "db/caching"))
           (var common  (substrate/get-service client "db/common"))
           (repl/notify
            {"status"       (xt/x:get-key summary "status")
             "primary_meta" (xtd/get-in primary ["metadata"])
             "caching_meta" (xtd/get-in caching ["metadata"])
             "common_stub"  (xt/x:not-nil? common)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => {"status" "ok"
      "primary_meta" {"common_id"  "db/common"
                      "primary_id" "db/primary"
                      "caching_id" "db/caching"}
      "caching_meta" {"common_id"  "db/common"
                      "primary_id" "db/primary"
                      "caching_id" "db/caching"}
      "common_stub"  true})

^{:refer xt.db.node.adaptor-proxy/attach-pull-model :added "4.1"}
(fact "client proxy attaches a pull model and opens it as a proxy"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (adaptor-proxy/set-default-transport client "server")
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-proxy/attach-pull-model
             client
             "room/a"
             "demo"
             "pull"
             {"metadata" {"caching_id" "db/caching"
                          "primary_id" "db/primary"}}
             {"pipeline" {}
              "options" {}
              "defaults" {"args" []
                          "output" {}}}
             {}))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "pull"]))
           (repl/notify
            {"has_group"  (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output"     (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"has_group"  true
       "model_type" "event.model"
       "output"     nil}))

^{:refer xt.db.node.adaptor-proxy/call-rpc :added "4.1"}
(fact "client proxy forwards call-rpc to the server"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (adaptor-proxy/set-default-transport client "server")
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           ;; install a mock rpc service on the server
           (var mock {"::" "xt.db.node.adaptor_proxy_test/MockRpc"
                      "metadata" {"caching_id" "db/caching"
                                  "primary_id" "db/primary"}})
           (proto/register-protocol-impl
            (xt/x:get-key impl-common/ISourceRemote "on")
            "xt.db.node.adaptor_proxy_test/MockRpc"
            {"rpc_call_async" (fn [impl rpc-spec args]
                                (return {"message" "hello-proxy"}))})
           (substrate/set-service server "db/mock" mock)
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-proxy/call-rpc
             client
             "room/a"
             "db/mock"
             {"id" "demo"}
             []
             {}))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"message" "hello-proxy"}))

^{:refer xt.db.node.adaptor-proxy/detach-pull-model :added "4.1"}
(fact "client proxy detaches a model and closes the proxy group"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (adaptor-proxy/set-default-transport client "server")
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-proxy/attach-pull-model
             client
             "room/a"
             "demo"
             "pull"
             {"metadata" {"caching_id" "db/caching"
                          "primary_id" "db/primary"}}
             {"pipeline" {}
              "options" {}
              "defaults" {"args" []
                          "output" {}}}
             {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-proxy/detach-pull-model
             client
             "room/a"
             "demo"
             "pull"
             {"metadata" {"caching_id" "db/caching"}}
             {}))))
        (promise/x:promise-then
         (fn [_]
           (var group (page-core/group-get client "room/a" "demo"))
           (repl/notify {"group_closed" (xt/x:nil? group)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => {"group_closed" true})
