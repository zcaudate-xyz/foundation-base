(ns documentation.xt-substrate
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.substrate"
         :subtitle "Frames, pubsub, requests, routers, spaces, pages, and transports."
         :lead "`xt.substrate` provides the message and transport foundation used by portable clients, workers, websocket servers, pages, and proxy layers."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"A generated application often needs to send frames, route requests, multiplex spaces, publish events, and move messages through memory, browser, or websocket transports. The substrate layer makes those concerns explicit and reusable."

[[:chapter {:title "How to use it" :link "usage"}]]

"Start with frames and spaces, add request/router or pubsub behavior, then choose a transport. Page and proxy utilities are higher-level helpers for browser and worker usage."

[[:chapter {:title "API" :link "api"}]]

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Nodes and transports"}]]

"A node is a transport-agnostic runtime. `node-create` builds one from declarative config, and `node?` recognizes node values. Transports are created with `transport-create` and recognized with `transport?`."

(fact "create and identify nodes and transports"
  ^{:refer xt.substrate/node-create :added "4.1"}
  (!.js
    (var node (substrate/node-create {"id" "walkthrough-node"}))
    [(substrate/node? node)
     (. node ["id"])])
  => [true "walkthrough-node"]

  ^{:refer xt.substrate/transport-create :added "4.1"}
  (!.js
    (var t (substrate/transport-create "peer-a" {"meta" {"role" "test"}}))
    [(substrate/transport? t)
     (. t ["id"])
     (. t ["meta"] ["role"])])
  => [true "peer-a" "test"])

[[:section {:title "Services"}]]

"Nodes keep a shared service registry. `set-service` registers a runtime value, `get-service` retrieves it by id, and `get-services` returns the whole map."

(fact "register and query node services"
  ^{:refer xt.substrate/set-service :added "4.1"}
  (!.js
    (var node (substrate/node-create {}))
    (substrate/set-service node "cache" {"scope" "local"})
    [(. (substrate/get-service node "cache") ["scope"])
     (xt/x:obj-keys (substrate/get-services node))])
  => ["local" ["cache"]])

[[:section {:title "Request handlers"}]]

"Handlers answer request frames. They can be declared in `node-create` or registered at runtime with `register-handler`, then invoked with `request`. Built-in util handlers such as `@/ping` are installed automatically."

(fact "invoke local and util handlers"
  ^{:refer xt.substrate/register-handler :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create {"handlers"
                                      {"greet"
                                       {"fn" (fn [space args request node]
                                               (return {"hello" (xt/x:get-idx args 0)}))
                                        "meta" {"kind" "request"}}}}))
    (-> (substrate/request node nil "greet" ["world"] {})
        (repl/notify)))
  => {"hello" "world"}

  ^{:refer xt.substrate.base-util-handlers/ping :added "4.1"}
  (notify/wait-on :js
    (-> (substrate/request (substrate/node-create {"id" "ping-node"})
                           nil
                           "@/ping"
                           []
                           {})
        (repl/notify)))
  => {"pong" true "node" "ping-node"})

[[:section {:title "Spaces and state"}]]

"Spaces are named contexts with mutable state. `node-create` can declare them up front, and handlers can read or update state with `get-space-state` and `update-space-state`."

(fact "spaces keep state that handlers can update"
  ^{:refer xt.substrate/update-space-state :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create
                {"spaces" {"counter" {"state" {"count" 0}}}
                 "handlers"
                 {"counter/inc"
                  {"fn" (fn [space args request node]
                          (substrate/update-space-state
                           node
                           (. space ["id"])
                           (fn [state space node]
                             (return {"count" (+ (or (. state ["count"]) 0) 1)})))
                          (return (substrate/get-space-state node (. space ["id"]))))
                   "meta" {"kind" "request"}}}}))
    (-> (substrate/request node "counter" "counter/inc" [] {})
        (repl/notify)))
  => {"count" 1})

[[:section {:title "Transports"}]]

"Transports move frames between nodes. `transport-memory/link-pair` wires two nodes together so a client can call a remote handler on a server over an in-memory text wire."

(fact "request across an in-memory transport"
  ^{:refer xt.substrate/attach-transport :added "4.1"}
  (notify/wait-on :js
    (var server (substrate/node-create
                  {"id" "server"
                   "handlers"
                   {"demo/echo"
                    {"fn" (fn [space args request server-node]
                            (return {"space" (. space ["id"])
                                     "server" (. server-node ["id"])
                                     "args" args}))
                     "meta" {"kind" "request"}}}}))
    (var client (substrate/node-create {"id" "client"}))
    (-> (transport-memory/link-pair server client)
        (promise/x:promise-then
         (fn [_]
           (return (substrate/request client "room/a" "demo/echo" ["ping"] nil))))
        (repl/notify)))
  => {"space" "room/a" "server" "server" "args" ["ping"]})

[[:section {:title "Pubsub and triggers"}]]

"Stream frames fan out to subscribers. `register-trigger` installs a local callback for a signal, and `publish` delivers a stream frame so the trigger can react."

(fact "publish invokes a trigger that updates space state"
  ^{:refer xt.substrate/publish :added "4.1"}
  (notify/wait-on :js
    (var node (substrate/node-create
                {"spaces" {"room/a" {"state" {"count" 0}}}
                 "triggers"
                 {"event/inc"
                  {"fn" (fn [space stream node]
                          (var current (substrate/get-space-state node (. space ["id"])))
                          (substrate/set-space-state
                           node
                           (. space ["id"])
                           {"count" (+ (or (. current ["count"]) 0) 1)})
                          (return true))
                   "meta" {"kind" "stream"}}}}))
    (-> (substrate/publish node "room/a" "event/inc" {} nil)
        (promise/x:promise-then
         (fn [_]
           (return (substrate/get-space-state node "room/a"))))
        (repl/notify)))
  => {"count" 1})

