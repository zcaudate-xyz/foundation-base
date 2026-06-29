(ns xt.db.node.adaptor-client-test
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
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.db.node.adaptor-base :as adaptor-base]
             [xt.db.node.adaptor-client :as adaptor-client]]})

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
   (adaptor-base/init-base-main
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

^{:refer xt.db.node.adaptor-client/attach-pull-model :added "4.1"}
(fact "client can remotely attach a pull-view model and open it as a proxy"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-client/attach-pull-model
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
             {"transport_id" "server"}))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "pull"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" nil}))

^{:refer xt.db.node.adaptor-client/attach-tree-view-model :added "4.1"}
(fact "client can remotely attach a tree-view model and open it as a proxy"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-client/attach-tree-view-model
             client
             "room/a"
             "demo"
             "tree"
             {"metadata" {"caching_id" "db/caching"
                          "primary_id" "db/primary"}}
             {"table" "Log"
              "select_entry" {}
              "return_entry" {}
              "pipeline" {}
              "options" {}
              "defaults" {"select_args" []
                          "return_args" []}}
             {"transport_id" "server"}))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "tree"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" nil}))

^{:refer xt.db.node.adaptor-client/attach-rpc-model :added "4.1"}
(fact "client can remotely attach an rpc model and open it as a proxy"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-client/attach-rpc-model
             client
             "room/a"
             "demo"
             "rpc"
             "db/primary"
             {"rpc_spec" {"id" "demo"}
              "pipeline" {}
              "options" {}
              "defaults" {"fn_args" []}}
             {"transport_id" "server"}))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "rpc"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" nil}))


^{:refer xt.db.node.adaptor-client/attach-model-request :added "4.1"}
(fact "sends an attach request to the server-side adaptor handler"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-client/attach-model-request
             client
             "room/a"
             "demo"
             "pull"
             "@xt.db/attach-pull-model"
             {"metadata" {"caching_id" "db/caching"
                          "primary_id" "db/primary"}}
             {"pipeline" {}
              "options" {}
              "defaults" {"args" []
                          "output" {}}}
             {"transport_id" "server"}))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify
            {"has_result" (xt/x:not-nil? result)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"has_result" true}))

^{:refer xt.db.node.adaptor-client/attach-model-and-open :added "4.1"}
(fact "wraps an attach request and opens the proxy group on the client"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (adaptor-base/init-handlers server)
    (-> (-/setup-server-db server)
        (promise/x:promise-then
         (fn [_]
           (return (-/link-nodes server client))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor-client/attach-model-and-open
             client
             "room/a"
             "demo"
             "pull"
             "@xt.db/attach-pull-model"
             {"metadata" {"caching_id" "db/caching"
                          "primary_id" "db/primary"}}
             {"pipeline" {}
              "options" {}
              "defaults" {"args" []
                          "output" {}}}
             {"transport_id" "server"}))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "pull"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" nil}))
