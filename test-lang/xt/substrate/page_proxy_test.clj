(ns xt.substrate.page-proxy-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.base-router :as router]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]]})

(defn.js make-server-node
  "creates a bare server node"
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

(defn.js link-nodes
  "links two nodes with capture transports for manual frame exchange"
  {:added "4.1"}
  [server client]
  (var server-outbound [])
  (var client-outbound [])
  (return
   (promise/x:promise-all
    [(substrate/attach-transport server
                                 "server-conn"
                                 {"send_fn" (fn [frame]
                                              (xt/x:arr-push server-outbound frame)
                                              (return frame))})
     (substrate/attach-transport client
                                 "server-conn"
                                 {"send_fn" (fn [frame]
                                              (xt/x:arr-push client-outbound frame)
                                              (return frame))})
     server-outbound
     client-outbound])))

(defn.js server-receive
  "delivers the most recent client outbound frame to the server"
  {:added "4.1"}
  [server client-outbound]
  (var frame (xt/x:get-idx client-outbound
                           (xt/x:offset (- (xt/x:len client-outbound) 1))))
  (return (substrate/receive-frame server frame {"transport_id" "server-conn"})))

(defn.js client-receive
  "delivers the most recent server outbound frame to the client"
  {:added "4.1"}
  [client server-outbound]
  (var frame (xt/x:get-idx server-outbound
                           (xt/x:offset (- (xt/x:len server-outbound) 1))))
  (return (substrate/receive-frame client frame {"transport_id" "server-conn"})))

(defn.js setup-server-page
  "creates a simple demo page on the server"
  {:added "4.1"}
  [server]
  (return
   (page-core/add-group server
                        "room/a"
                        "demo"
                        {"main" {"defaults" {"args" ["hello"]
                                            "output" {}
                                            "process" (fn [x] (return x))
                                            "init" (fn [] (return nil))}
                                 "handler" (fn [ctx]
                                             (var data (xtd/get-in ctx ["input" "data"]))
                                             (return {"value" (xt/x:first data)}))
                                 "trigger" true
                                 "options" {}}})))

(defn.js send-and-receive
  "sends a request from client to server and pumps captured frames"
  {:added "4.1"}
  [linked server client space action args]
  (var client-outbound (xtd/nth linked 3))
  (var server-outbound (xtd/nth linked 2))
  (var p (substrate/request client space action args {"transport_id" "server-conn"}))
  (return
   (promise/x:promise-then
    (-/server-receive server client-outbound)
    (fn [_]
      (-/client-receive client server-outbound)
      (return p)))))

(defn.js pump-promise
  "pumps captured frames for a request already in flight"
  {:added "4.1"}
  [linked server client p]
  (var client-outbound (xtd/nth linked 3))
  (var server-outbound (xtd/nth linked 2))
  (return
   (promise/x:promise-then
    (-/server-receive server client-outbound)
    (fn [_]
      (-/client-receive client server-outbound)
      (return p)))))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})


^{:refer xt.substrate.page-proxy/list-proxy-groups :added "4.1"}
(fact "lists remote groups and model ids available on the server"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install server)
    (-/setup-server-page server)
    (-> (page-proxy/list-proxy-groups server "room/a" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"demo" {"models" ["main"]}})

^{:refer xt.substrate.page-proxy/echo-local :added "4.1"}
(fact "handler can be invoked locally on the server"

  (notify/wait-on :js
    (var server (substrate/node-create {"id" "server"
                                        "spaces" {"room/a" {"state" {}}}}))
    (substrate/register-handler server "echo/ping"
                                (fn [space args request node]
                                  (return {"pong" (xt/x:first args)}))
                                nil)
    (-> (substrate/request server "room/a" "echo/ping" ["hello"] {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"pong" "hello"})

(fact "respond-ok sends response over transport"
  (notify/wait-on :js
    (var server (substrate/node-create {"id" "server"
                                        "spaces" {"room/a" {"state" {}}}}))
    (substrate/register-handler server "echo/ping"
                                (fn [space args request node]
                                  (return {"pong" "hello"}))
                                nil)
    (var out [])
    (-> (substrate/attach-transport server "conn"
                                    {"send_fn" (fn [frame]
                                                 (xt/x:arr-push out frame)
                                                 (return frame))})
        (promise/x:promise-then
         (fn [_]
           (var req {"kind" "request"
                     "id" "r1"
                     "space" "room/a"
                     "action" "echo/ping"
                     "args" ["hello"]
                     "meta" {"transport_id" "conn"}})
           (-> (substrate/receive-frame server req {"transport_id" "conn"})
               (promise/x:promise-then
                (fn [_]
                  (repl/notify {"out" out}))))))))
  => (contains-in
      {"out" [{"reply_to" "r1" "status" "ok" "data" {"pong" "hello"}}]}))

^{:refer xt.substrate.page-proxy/echo :added "4.1"}
(fact "manual transport pair can exchange requests"

  (notify/wait-on :js
    (var server (substrate/node-create {"id" "server"
                                        "spaces" {"room/a" {"state" {}}}}))
    (var client (substrate/node-create {"id" "client"
                                        "spaces" {"room/a" {"state" {}}}}))
    (substrate/register-handler server "echo/ping"
                                (fn [space args request node]
                                  (return {"pong" "hello"}))
                                nil)
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [linked]
           (return (-/send-and-receive linked server client
                                       "room/a" "echo/ping" ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => {"pong" "hello"})

^{:refer xt.substrate.page-proxy/open-proxy-group :added "4.1"}
(fact "opens a remote group and creates proxy models on the client"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (-/setup-server-page server)
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [linked]
           (return
            (-> (-/send-and-receive linked server client
                                    "room/a" page-proxy/ACTION_GROUP_OPEN
                                    [{"space" "room/a" "group" "demo"}])
                (promise/x:promise-then
                 (fn [response]
                   (var snapshot (xt/x:get-key response "models"))
                   (page-proxy/create-proxy-group client "room/a" "demo" snapshot
                                                   {"transport_id" "server-conn"})
                   (return (page-core/group-get client "room/a" "demo"))))))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "main"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-output model nil)})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" {"current" {"value" "hello"}}}))

^{:refer xt.substrate.page-proxy/close-proxy-group :added "4.1"}
(fact "closes a remote group and removes router subscriptions"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (page-proxy/install server)
    (page-proxy/install client)
    (-/setup-server-page server)
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [linked]
           (return
            (-> (-/send-and-receive linked server client
                                    "room/a" page-proxy/ACTION_GROUP_OPEN
                                    [{"space" "room/a" "group" "demo"}])
                (promise/x:promise-then
                 (fn [response]
                   (var snapshot (xt/x:get-key response "models"))
                   (page-proxy/create-proxy-group client "room/a" "demo" snapshot
                                                   {"transport_id" "server-conn"})
                   (return
                    (-/pump-promise
                     linked server client
                     (page-proxy/close-proxy-group
                      client "room/a" "demo"
                      {"transport_id" "server-conn"})))))))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"group"       (page-core/group-get client
                                                "room/a" "demo")
             "output_subs" (router/list-subscriptions server
                                                      "room/a" page-proxy/SIGNAL_OUTPUT)
             "input_subs"  (router/list-subscriptions server
                                                      "room/a" page-proxy/SIGNAL_INPUT)})))))
  => {"group" nil
      "output_subs" []
      "input_subs" []})
