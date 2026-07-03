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
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.kernel-base :as adaptor]]})

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
   (base-page/group-add server
                        "room/a"
                        "demo"
                        {"main" {"defaults" {"args" ["hello"]
                                            "output" {}
                                            "process" (fn [x] (return x))
                                            "init" (fn [] (return nil))}
                                 "handler" (fn [ctx]
                                             (var data (xtd/get-in ctx ["input" "data"]))
                                             (return {"value" (xt/x:first data)}))
                                 "options" {"trigger" true}}})))

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


^{:refer xt.substrate.page-proxy/group-list-proxy :added "4.1"}
(fact "lists remote groups and model ids available on the server"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install server)
    (-/setup-server-page server)
    (-> (page-proxy/group-list-proxy server "room/a" {})
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

(fact "response-ok sends response over transport"
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

^{:refer xt.substrate.page-proxy/group-open-proxy :added "4.1"}
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
                   (page-proxy/group-create-proxy client "room/a" "demo" snapshot
                                                   {"transport_id" "server-conn"})
                   (return (base-page/group-get client "room/a" "demo"))))))))
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

^{:refer xt.substrate.page-proxy/group-close-proxy :added "4.1"}
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
                   (page-proxy/group-create-proxy client "room/a" "demo" snapshot
                                                   {"transport_id" "server-conn"})
                   (return
                    (-/pump-promise
                     linked server client
                     (page-proxy/group-close-proxy
                      client "room/a" "demo"
                      {"transport_id" "server-conn"})))))))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"group"       (base-page/group-get client
                                                "room/a" "demo")
             "output_subs" (router/list-subscriptions server
                                                      "room/a" page-proxy/SIGNAL_OUTPUT)
             "input_subs"  (router/list-subscriptions server
                                                      "room/a" page-proxy/SIGNAL_INPUT)})))))
  => {"group" nil
      "output_subs" []
      "input_subs" []})

^{:refer xt.substrate.page-proxy/kernel-init-handler :added "4.1"}
(fact "client can call @xt.db/kernel-init on the server through the proxy"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (var client (-/make-client-node))
    (var schema {"Log" {"id" {"ident" "id"
                               "type" "uuid"
                               "primary" true
                               "order" 0}
                        "message" {"ident" "message"
                                   "type" "text"
                                   "order" 1}}})
    (var lookup {"Log" {"position" 0}})
    (substrate/set-service server "db/common" {"schema" schema
                                                "lookup" lookup})
    (adaptor/init-handlers server)
    (page-proxy/install server)
    (page-proxy/install client)
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [linked]
           (return
            (-/send-and-receive linked server client
                                "room/a" "@xt.db/kernel-init"
                                [{"primary" {"type" "memory" "defaults" {}}
                                  "caching" {"type" "memory" "defaults" {}}
                                  "common" {}}
                                 schema
                                 lookup]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify {"init" out
                         "primary" (substrate/get-service server "db/primary")
                         "caching" (substrate/get-service server "db/caching")})))))
  => (contains-in
      {"init" {"status" "setup"
               "data" {"primary" map?
                       "caching" map?
                       "common" map?}}
       "primary" {"::" "xt.db.system.impl_memory/ImplMemory"}
       "caching" {"::" "xt.db.system.impl_memory/ImplMemory"}}))


^{:refer xt.substrate.page-proxy/model-serialize-input :added "4.1"}
(fact "extracts current and updated from an input record"

  (!.js
    (page-proxy/model-serialize-input {"current" {"data" [1]}
                                 "updated" 100
                                 "default" nil}))
  => {"current" {"data" [1]}
      "updated" 100})

^{:refer xt.substrate.page-proxy/model-serialize-output :added "4.1"}
(fact "extracts transportable fields from an output record"

  (!.js
    (page-proxy/model-serialize-output {"type" "output"
                                  "current" {"value" 1}
                                  "updated" 100
                                  "elapsed" 5
                                  "pending" false
                                  "disabled" false
                                  "errored" false
                                  "tag" "main"}))
  => {"type" "output"
      "current" {"value" 1}
      "updated" 100
      "elapsed" 5
      "pending" false
      "disabled" false
      "errored" false
      "tag" "main"})

^{:refer xt.substrate.page-proxy/model-serialize :added "4.1"}
(fact "captures a serializable snapshot of model state"

  (!.js
    (var node (substrate/node-create {"id" "server"
                                      "spaces" {"room/a" {"state" {}}}}))
    (var model (base-page/create-model
                node "room/a" "demo" "main"
                {"handler" (fn [ctx] (return {"ok" true}))
                 "defaults" {"args" [1 2]
                             "output" {"value" 0}}}))
    (event-model/init-model model)
    (page-proxy/model-serialize model))
  => (contains-in {"input" {"current" {"data" [1 2]}}
                   "output" {"type" "output"
                             "current" nil}}))

^{:refer xt.substrate.page-proxy/group-snapshot :added "4.1"}
(fact "captures a serializable snapshot of all models in a group"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (-/setup-server-page server)
    (var group (base-page/group-get server "room/a" "demo"))
    (-> (xt/x:get-key group "init")
        (promise/x:promise-then
         (fn [_]
           (repl/notify (page-proxy/group-snapshot server "room/a" "demo"))))))
  => (contains-in {"main" {"input" {"current" {"data" ["hello"]}}
                           "output" {"current" {"value" "hello"}}}}))

^{:refer xt.substrate.page-proxy/publish-model-output :added "4.1"}
(fact "returns nil when there are no output subscribers"

  (!.js
    (var server (-/make-server-node))
    (-/setup-server-page server)
    (var group (base-page/group-get server "room/a" "demo"))
    (var model (xtd/get-in group ["models" "main"]))
    (page-proxy/publish-model-output server "room/a" ["demo" "main"]
                                     (event-model/get-output model nil)))
  => nil)

^{:refer xt.substrate.page-proxy/publish-model-input :added "4.1"}
(fact "returns nil when there are no input subscribers"

  (!.js
    (var server (-/make-server-node))
    (-/setup-server-page server)
    (var group (base-page/group-get server "room/a" "demo"))
    (var model (xtd/get-in group ["models" "main"]))
    (page-proxy/publish-model-input server "room/a" ["demo" "main"]
                                    (event-model/get-input model)))
  => nil)

^{:refer xt.substrate.page-proxy/ensure-model-listeners :added "4.1"}
(fact "adds proxy output and input listeners when absent"

  (!.js
    (var node (substrate/node-create {"id" "server"
                                      "spaces" {"room/a" {"state" {}}}}))
    (var model (base-page/create-model
                node "room/a" "demo" "main"
                {"handler" (fn [ctx] (return {}))
                 "defaults" {"args" []}}))
    (page-proxy/ensure-model-listeners node "room/a" "demo" "main" model)
    (var listeners (xt/x:get-key model "listeners"))
    {"output" (xt/x:not-nil? (xt/x:get-key listeners page-proxy/LISTENER_OUTPUT))
     "input" (xt/x:not-nil? (xt/x:get-key listeners page-proxy/LISTENER_INPUT))})
  => {"output" true
      "input" true})

^{:refer xt.substrate.page-proxy/group-handle-list :added "4.1"}
(fact "lists available groups and their model ids"

  (!.js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (page-proxy/group-handle-list nil ["room/a"] {} server))
  => {"demo" {"models" ["main"]}})

^{:refer xt.substrate.page-proxy/group-handle-open :added "4.1"}
(fact "returns a model snapshot and subscribes the transport"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (-> (substrate/attach-transport server "conn"
                                    {"send_fn" (fn [frame] (return frame))})
        (promise/x:promise-then
         (fn [_]
           (-> (page-proxy/group-handle-open nil
                                             [{"space" "room/a"
                                               "group" "demo"}]
                                             {"meta" {"transport_id" "conn"}}
                                             server)
               (promise/x:promise-then
                (fn [response]
                  (repl/notify
                   {"space" (xt/x:get-key response "space")
                    "group" (xt/x:get-key response "group")
                    "has_models" (xt/x:not-nil? (xt/x:get-key response "models"))
                    "output_subs" (router/list-subscriptions
                                   server "room/a" page-proxy/SIGNAL_OUTPUT)}))))))))
  => (contains-in {"space" "room/a"
                   "group" "demo"
                   "has_models" true
                   "output_subs" ["conn"]}))

^{:refer xt.substrate.page-proxy/group-handle-close :added "4.1"}
(fact "removes subscriptions and returns a closed status"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (-> (substrate/attach-transport server "conn"
                                    {"send_fn" (fn [frame] (return frame))})
        (promise/x:promise-then
         (fn [_]
           (-> (page-proxy/group-handle-open nil
                                             [{"space" "room/a"
                                               "group" "demo"}]
                                             {"meta" {"transport_id" "conn"}}
                                             server)
               (promise/x:promise-then
                (fn [_]
                  (var closed (page-proxy/group-handle-close
                               nil
                               [{"space" "room/a"
                                 "group" "demo"}]
                               {"meta" {"transport_id" "conn"}}
                               server))
                  (repl/notify
                   {"closed" closed
                    "output_subs" (router/list-subscriptions
                                   server "room/a" page-proxy/SIGNAL_OUTPUT)
                    "input_subs" (router/list-subscriptions
                                  server "room/a" page-proxy/SIGNAL_INPUT)}))))))))
  => (contains-in {"closed" {"status" "closed"
                             "space" "room/a"
                             "group" "demo"}
                   "output_subs" []
                   "input_subs" []}))

^{:refer xt.substrate.page-proxy/group-handle-update :added "4.1"}
(fact "refreshes every model in the group and returns ok"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (-> (page-proxy/group-handle-update nil
                                        [{"space" "room/a"
                                          "group" "demo"
                                          "event" {}}]
                                        {}
                                        server)
        (promise/x:promise-then
         (fn [response]
           (repl/notify response)))))
  => {"status" "ok"})

^{:refer xt.substrate.page-proxy/model-handle-update :added "4.1"}
(fact "refreshes a single model and returns ok"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (-> (page-proxy/model-handle-update nil
                                        [{"space" "room/a"
                                          "group" "demo"
                                          "model" "main"
                                          "event" {}}]
                                        {}
                                        server)
        (promise/x:promise-then
         (fn [response]
           (repl/notify response)))))
  => {"path" ["demo" "main"]
      "post" [false]
      "::" "model.run"
      "pre" [false]
      "main" [true {"value" "hello"}]})

^{:refer xt.substrate.page-proxy/model-handle-set-input :added "4.1"}
(fact "sets model input and refreshes the model"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (-> (page-proxy/model-handle-set-input nil
                                            [{"space" "room/a"
                                              "group" "demo"
                                              "model" "main"
                                              "current" {"data" ["world"]}}]
                                            {}
                                            server)
        (promise/x:promise-then
         (fn [response]
           (var group (base-page/group-get server "room/a" "demo"))
           (var model (xtd/get-in group ["models" "main"]))
           (repl/notify
            {"status" (xt/x:get-key response "status")
             "input" (xtd/get-in model ["input" "current" "data"])
             "output" (event-model/get-output model nil)})))))
  => (contains-in {"status" "ok"
                   "input" ["world"]
                   "output" {"current" {"value" "world"}}}))

^{:refer xt.substrate.page-proxy/model-handle-trigger :added "4.1"}
(fact "triggers a matching model and reports triggered"

  (!.js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (page-proxy/model-handle-trigger nil
                                      [{"space" "room/a"
                                        "group" "demo"
                                        "model" "main"
                                        "signal" true}]
                                      {}
                                      server))
  => {"status" "ok"
      "triggered" true})

^{:refer xt.substrate.page-proxy/group-handle-trigger :added "4.1"}
(fact "triggers matching models in the group"

  (!.js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (page-proxy/group-handle-trigger nil
                                      [{"space" "room/a"
                                        "group" "demo"
                                        "signal" true}]
                                      {}
                                      server))
  => {"status" "ok"
      "models" ["main"]})

^{:refer xt.substrate.page-proxy/model-handle-proxy-call :added "4.1"}
(fact "invokes a remote call on the model and returns ok"

  (notify/wait-on :js
    (var server (-/make-server-node))
    (page-proxy/install-handlers server)
    (-/setup-server-page server)
    (-> (page-proxy/model-handle-proxy-call nil
                                             [{"space" "room/a"
                                               "group" "demo"
                                               "model" "main"
                                               "args" []
                                               "save_output" true}]
                                             {}
                                             server)
        (promise/x:promise-then
         (fn [response]
           (repl/notify response)))))
  => {"status" "ok"})

^{:refer xt.substrate.page-proxy/install-handlers :added "4.1"}
(fact "registers all page-proxy action handlers"

  (!.js
    (var node (substrate/node-create {"id" "node"}))
    (page-proxy/install-handlers node)
    (substrate/list-handlers node))
  => ["page.group/close"
      "page.group/list"
      "page.group/open"
      "page.group/trigger"
      "page.group/update"
      "page.model/proxy-call"
      "page.model/set-input"
      "page.model/trigger"
      "page.model/update"])

^{:refer xt.substrate.page-proxy/model-create-proxy :added "4.1"}
(fact "creates a proxy model from a server snapshot"

  (!.js
    (var node (-/make-client-node))
    (var model (page-proxy/model-create-proxy
                node "room/a" "demo" "main"
                {"input" {"current" {"data" [1]}
                          "updated" 10}
                 "output" {"type" "output"
                           "current" {"value" 1}
                           "updated" 10
                           "elapsed" 5
                           "pending" false
                           "disabled" false
                           "errored" false
                           "tag" "init"}}))
    {"type" (xt/x:get-key model "::")
     "input" (xtd/get-in model ["input" "current"])
     "output" (event-model/get-output model nil)})
  => (contains-in {"type" "event.model"
                   "input" {"data" [1]}
                   "output" {"current" {"value" 1}}}))

^{:refer xt.substrate.page-proxy/group-create-proxy :added "4.1"}
(fact "creates a proxy group and stores the remote spec"

  (!.js
    (var node (-/make-client-node))
    (page-proxy/group-create-proxy
     node "room/a" "demo"
     {"main" {"input" {"current" {"data" [1]}}
              "output" {"type" "output"
                        "current" {"value" 1}}}}
     {"transport_id" "server-conn"})
    (var group (base-page/group-get node "room/a" "demo"))
    {"remote" (xt/x:get-key group "remote")
     "model_type" (xtd/get-in group ["models" "main" "::"])})
  => {"remote" {"transport_id" "server-conn"}
      "model_type" "event.model"})

^{:refer xt.substrate.page-proxy/model-apply-output :added "4.1"}
(fact "applies an inbound output delta to a proxy model"

  (!.js
    (var node (-/make-client-node))
    (page-proxy/group-create-proxy
     node "room/a" "demo"
     {"main" {"input" {"current" {"data" [1]}}
              "output" {"type" "output"
                        "current" {"value" 1}}}}
     {"transport_id" "server-conn"})
    (page-proxy/model-apply-output "room/a"
                                   {"space" "room/a"
                                    "data" {"path" ["demo" "main"]
                                            "output" {"current" {"value" 2}}}}
                                   node)
    (var model (xtd/get-in (base-page/group-get node "room/a" "demo")
                            ["models" "main"]))
    (event-model/get-output model nil))
  => (contains-in {"current" {"value" 2}}))

^{:refer xt.substrate.page-proxy/model-apply-input :added "4.1"}
(fact "applies an inbound input delta to a proxy model"

  (!.js
    (var node (-/make-client-node))
    (page-proxy/group-create-proxy
     node "room/a" "demo"
     {"main" {"input" {"current" {"data" [1]}}
              "output" {"type" "output"
                        "current" {"value" 1}}}}
     {"transport_id" "server-conn"})
    (page-proxy/model-apply-input "room/a"
                                  {"space" "room/a"
                                   "data" {"path" ["demo" "main"]
                                           "input" {"current" {"data" [2]}}}}
                                  node)
    (var model (xtd/get-in (base-page/group-get node "room/a" "demo")
                            ["models" "main"]))
    (event-model/get-input model))
  => (contains-in {"current" {"data" [2]}}))

^{:refer xt.substrate.page-proxy/install-triggers :added "4.1"}
(fact "registers proxy output and input stream triggers"

  (!.js
    (var node (substrate/node-create {"id" "node"}))
    (page-proxy/install-triggers node)
    (substrate/list-triggers node))
  => ["page.model/input"
      "page.model/output"])

^{:refer xt.substrate.page-proxy/proxy-dispatcher :added "4.1"}
(fact "forwards a proxy operation to the remote server"

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
                   (page-proxy/group-create-proxy client "room/a" "demo" snapshot
                                                   {"transport_id" "server-conn"})
                   (var call-args ["main" [] true])
                   (return
                    (-/pump-promise
                     linked server client
                     (page-proxy/proxy-dispatcher
                      "proxy-call" client "room/a" "demo"
                      call-args)))))))))
        (promise/x:promise-then
         (fn [response]
           (repl/notify response)))))
  => {"status" "ok"})

^{:refer xt.substrate.page-proxy/install :added "4.1"}
(fact "installs handlers and triggers"

  (!.js
    (var node (substrate/node-create {"id" "node"}))
    (page-proxy/install node)
    {"handlers" (xt/x:len (substrate/list-handlers node))
     "triggers" (xt/x:len (substrate/list-triggers node))})
  => {"handlers" 9
      "triggers" 2})

^{:refer xt.substrate.page-proxy/model-proxy-call :added "4.1"}
(fact "issues a proxy-call request through the proxy group"

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
                   (page-proxy/group-create-proxy client "room/a" "demo" snapshot
                                                   {"transport_id" "server-conn"})
                   (return
                    (-/pump-promise
                     linked server client
                     (page-proxy/model-proxy-call client "room/a" "demo" "main"
                                            [] true {})))))))))
        (promise/x:promise-then
         (fn [response]
           (repl/notify response)))))
  => {"status" "ok"})

^{:refer xt.substrate.page-proxy/model-get-output :added "4.1"}
(fact "TODO")