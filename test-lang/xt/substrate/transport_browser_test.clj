^{:seedgen/skip true}
(ns xt.substrate.transport-browser-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-browser :as browser-transport]]})

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

^{:refer xt.substrate.transport-browser/worker-endpoint :added "4.1"}
(fact "adapts a host-side Worker endpoint to the node transport contract"
  (!.js
   (var posted [])
   (var listeners [])
   (var removed [])
   (var terminated [])
   (var received [])
   (var worker {"postMessage" (fn [msg] (posted.push msg))
                "addEventListener" (fn [event listener capture]
                                     (listeners.push listener))
                "removeEventListener" (fn [event listener capture]
                                        (removed.push listener))
                "terminate" (fn [] (terminated.push true))})
   (var transport (browser-transport/worker-endpoint worker))
   ((. transport ["start_fn"])
    (fn [frame ctx]
      (received.push frame)))
   ((. transport ["send_fn"]) {"kind" "request" "id" "req-1"})
   ((xt/x:first listeners) {"data" {"kind" "response" "id" "res-1"}})
   ((. transport ["stop_fn"]) nil)
   (return {"posted" posted
            "received" received
            "removed" (xt/x:len removed)
            "terminated" (xt/x:len terminated)}))
  => (contains-in {"posted" [{"kind" "request" "id" "req-1"}]
                   "received" [{"kind" "response" "id" "res-1"}]
                   "removed" 1
                   "terminated" 1}))

^{:refer xt.substrate.transport-browser/messageport-endpoint :added "4.1"}
(fact "adapts a MessagePort-like endpoint to the node transport contract"
  (!.js
   (var posted [])
   (var listeners [])
   (var started [])
   (var closed [])
   (var received [])
   (var port {"postMessage" (fn [msg] (posted.push msg))
              "start" (fn [] (started.push true))
              "addEventListener" (fn [event listener capture]
                                   (listeners.push listener))
              "removeEventListener" (fn [event listener capture]
                                      listener)
              "close" (fn [] (closed.push true))})
   (var transport (browser-transport/messageport-endpoint port))
   ((. transport ["start_fn"])
    (fn [frame ctx]
      (received.push frame)))
   ((. transport ["send_fn"]) {"kind" "stream" "id" "evt-1"})
   ((xt/x:first listeners) {"data" {"kind" "request" "id" "req-3"}})
   ((. transport ["stop_fn"]) nil)
   (return {"posted" posted
            "received" received
            "started" (xt/x:len started)
            "closed" (xt/x:len closed)}))
  => (contains-in {"posted" [{"kind" "stream" "id" "evt-1"}]
                   "received" [{"kind" "request" "id" "req-3"}]
                   "started" 1
                   "closed" 1}))

^{:refer xt.substrate.transport-browser/sharedworker-endpoint :added "4.1"}
(fact "adapts a SharedWorker by using its port"
  (!.js
   (var posted [])
   (var listeners [])
   (var started [])
   (var received [])
   (var port {"postMessage" (fn [msg] (posted.push msg))
              "start" (fn [] (started.push true))
              "addEventListener" (fn [event listener capture]
                                   (listeners.push listener))
              "removeEventListener" (fn [event listener capture]
                                      listener)})
   (var shared {"port" port})
   (var transport (browser-transport/sharedworker-endpoint shared))
   ((. transport ["start_fn"])
    (fn [frame ctx]
      (received.push frame)))
   ((. transport ["send_fn"]) {"kind" "request" "id" "req-4"})
   ((xt/x:first listeners) {"data" {"kind" "response" "id" "res-4"}})
   (return {"posted" posted
            "received" received
            "started" (xt/x:len started)}))
  => (contains-in {"posted" [{"kind" "request" "id" "req-4"}]
                   "received" [{"kind" "response" "id" "res-4"}]
                   "started" 1}))

^{:refer xt.substrate.transport-browser/self-endpoint :added "4.1"}
(fact "adapts worker self to the node transport contract"
  (!.js
   (var posted [])
   (var listeners [])
   (var removed [])
   (var received [])
   (var worker-self
        {"postMessage" (fn [msg] (posted.push msg))
         "addEventListener" (fn [event listener capture]
                              (listeners.push listener))
         "removeEventListener" (fn [event listener capture]
                                 (removed.push listener))})
   (var transport (browser-transport/self-endpoint worker-self))
   ((. transport ["start_fn"])
    (fn [frame ctx]
      (received.push frame)))
   ((. transport ["send_fn"]) {"kind" "stream" "id" "evt-1"})
   ((xt/x:first listeners) {"data" {"kind" "request" "id" "req-3"}})
   ((. transport ["stop_fn"]) nil)
   (return {"posted" posted
            "received" received
            "removed" (xt/x:len removed)}))
  => (contains-in {"posted" [{"kind" "stream" "id" "evt-1"}]
                   "received" [{"kind" "request" "id" "req-3"}]
                   "removed" 1}))

^{:refer xt.substrate.transport-browser/event-data :added "4.1"}
(fact "unwraps browser worker message envelopes and passes raw payloads through"
  (!.js
   [(browser-transport/event-data {"data" {"id" "evt-1"}})
    (browser-transport/event-data {"id" "evt-2"})])
  => [{"id" "evt-1"}
     {"id" "evt-2"}])

^{:refer xt.substrate.transport-browser/connect-worker :added "4.1"}
(fact "connect-worker resolves after the ready signal and disconnect stops the worker"
  (notify/wait-on :js
    (var created 0)
    (var terminated 0)
    (var node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-catch
     (promise/x:promise-then
     (browser-transport/connect-worker
      node
      {"transport_id" "worker"
       "source"
       {"create_fn"
        (fn [listener]
          (:= created (+ created 1))
          (listener {"signal" "ready"
                     "worker" "unit-worker"} nil)
          (return {"postMessage" (fn [frame] frame)
                   "terminate" (fn [] (:= terminated (+ terminated 1)))}))}})
     (fn [conn]
       (return
        (promise/x:promise-then
         (browser-transport/disconnect conn)
         (fn [_]
           (repl/notify {"created" created
                         "terminated" terminated
                         "ready" (. conn ["ready"])
                         "transport_id" (. conn ["transport_id"])}))))))
     (fn [err]
      (repl/notify {"error" err}))))
  => {"created" 1
     "terminated" 1
     "ready" {"signal" "ready"
              "worker" "unit-worker"}
     "transport_id" "worker"})


^{:refer xt.substrate.transport-browser/ready-event? :added "4.1"}
(fact "matches ready signals or delegates to a custom ready predicate"
  (!.js
   [(browser-transport/ready-event? {"signal" "ready"} {})
    (browser-transport/ready-event? {"signal" "booted"} {"ready_signal" "booted"})
    (browser-transport/ready-event? {"signal" "ready"} {"ready_signal" "booted"})
    (browser-transport/ready-event? {"type" "custom"} {"ready_pred" (fn [event]
                                                                      (return (== (. event ["type"])
                                                                                  "custom")))} )])
  => [true true false true])

^{:refer xt.substrate.transport-browser/await-ready :added "4.1"}
(fact "waits for a connection state to record its ready payload"
  (notify/wait-on :js
    (var state {"ready" nil})
    (setTimeout
     (fn []
       (xt/x:set-key state "ready" {"signal" "ready"
                                    "id" "conn-1"}))
     20)
    (promise/x:promise-then
     (browser-transport/await-ready state)
     (fn [ready]
       (repl/notify ready))))
  => {"signal" "ready"
      "id" "conn-1"})

^{:refer xt.substrate.transport-browser/connection-record :added "4.1"}
(fact "builds connection records from attached node transports"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-then
     (event-node/attach-transport
      node
      "worker"
      {"meta" {"kind" "webworker"}
       "start_fn" (fn [listener]
                    (return {"listener" true}))})
     (fn [_]
       (repl/notify
        (browser-transport/connection-record
         node
         "worker"
         {"signal" "ready"})))))
  => (contains-in {"transport_id" "worker"
                   "ready" {"signal" "ready"}
                   "target" {"listener" true}}))

^{:refer xt.substrate.transport-browser/wrap-ready-endpoint :added "4.1"}
(fact "captures ready events in state and suppresses them from listener routing"
  (!.js
   (var state {"ready" nil})
   (var seen [])
   (var wrapped
        (browser-transport/wrap-ready-endpoint
         {"start_fn"
          (fn [listener]
            (listener {"signal" "ready"} nil)
            (listener {"signal" "data"
                       "value" 1} nil)
            (return true))}
         state
         {}))
   ((. wrapped ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push seen event)))
   {"ready" (. state ["ready"])
    "seen" seen})
  => {"ready" {"signal" "ready"}
      "seen" [{"signal" "data"
               "value" 1}]})

^{:refer xt.substrate.transport-browser/source-endpoint :added "4.1"}
(fact "creates browser source endpoints around create_fn targets"
  (!.js
   (var posted [])
   (var closed [])
   (var terminated [])
   (var received [])
   (var target nil)
   (var endpoint
        (browser-transport/source-endpoint
         {"create_fn"
          (fn [listener]
            (:= target {"postMessage" (fn [frame]
                                        (xt/x:arr-push posted frame))
                        "close" (fn []
                                  (xt/x:arr-push closed true))
                        "terminate" (fn []
                                      (xt/x:arr-push terminated true))
                        "emit" (fn [frame]
                                 (listener frame nil))})
            (return target))}
         "sharedworker"))
   ((. endpoint ["start_fn"])
    (fn [frame ctx]
      (xt/x:arr-push received frame)))
   ((. endpoint ["send_fn"]) {"id" "req-1"})
   ((. target ["emit"]) {"id" "res-1"})
   ((. endpoint ["stop_fn"]) nil)
   {"posted" posted
    "received" received
    "closed" (xt/x:len closed)
    "terminated" (xt/x:len terminated)})
  => {"posted" [{"id" "req-1"}]
      "received" [{"id" "res-1"}]
      "closed" 1
      "terminated" 1})

^{:refer xt.substrate.transport-browser/connect-endpoint :added "4.1"}
(fact "attaches an endpoint and resolves once the ready event is observed"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-then
     (browser-transport/connect-endpoint
      node
      "port"
      {"start_fn"
       (fn [listener]
         (listener {"signal" "ready"
                    "via" "endpoint"} nil)
         (return {"listener" true}))}
      {})
     (fn [conn]
       (repl/notify
        {"transport_id" (. conn ["transport_id"])
         "ready" (. conn ["ready"])
         "target" (. conn ["target"])}))))
  => {"transport_id" "port"
      "ready" {"signal" "ready"
               "via" "endpoint"}
      "target" {"listener" true}})

^{:refer xt.substrate.transport-browser/connect-port :added "4.1"}
(fact "connects a MessagePort-like source and waits for readiness"
  (notify/wait-on :js
    (var started 0)
    (var node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-then
     (browser-transport/connect-port
     node
     {"transport_id" "port"
      "port" {"start" (fn []
                         (:= started (+ started 1)))
               "postMessage" (fn [frame] frame)
               "addEventListener" (fn [event listener capture]
                                    (when (== event "message")
                                      (setTimeout
                                       (fn []
                                         (listener {"data" {"signal" "ready"
                                                            "via" "port"}}))
                                       0)))
               "removeEventListener" (fn [event listener capture]
                                       true)
               "close" (fn [] true)}})
     (fn [conn]
       (repl/notify
        {"transport_id" (. conn ["transport_id"])
         "ready" (. conn ["ready"])
         "started" started}))))
  => {"transport_id" "port"
     "ready" {"signal" "ready"
              "via" "port"}
     "started" 1})

^{:refer xt.substrate.transport-browser/connect-sharedworker :added "4.1"}
(fact "connects SharedWorker sources through their port endpoint"
  (notify/wait-on :js
    (var started 0)
    (var node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-then
     (browser-transport/connect-sharedworker
     node
     {"transport_id" "shared"
      "source" {"port" {"start" (fn []
                                   (:= started (+ started 1)))
                         "postMessage" (fn [frame] frame)
                         "addEventListener" (fn [event listener capture]
                                              (when (== event "message")
                                                (setTimeout
                                                 (fn []
                                                   (listener {"data" {"signal" "ready"
                                                                      "via" "shared"}}))
                                                 0)))
                         "removeEventListener" (fn [event listener capture]
                                                 true)
                         "close" (fn [] true)}}})
     (fn [conn]
       (repl/notify
        {"transport_id" (. conn ["transport_id"])
         "ready" (. conn ["ready"])
         "started" started}))))
  => {"transport_id" "shared"
     "ready" {"signal" "ready"
              "via" "shared"}
     "started" 1})

^{:refer xt.substrate.transport-browser/boot-self :added "4.1"}
(fact "boots worker self targets and optionally emits a ready payload"
  (notify/wait-on :js
    (var posted [])
    (var node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-then
     (browser-transport/boot-self
      node
      {"transport_id" "host"
       "target" {"postMessage" (fn [frame]
                                (xt/x:arr-push posted frame)
                                (return (promise/x:promise-run true)))
                 "addEventListener" (fn [event listener capture]
                                      true)
                 "removeEventListener" (fn [event listener capture]
                                         true)}
       "ready" {"signal" "ready"
                "via" "self"}})
     (fn [conn]
       (repl/notify
        {"transport_id" (. conn ["transport_id"])
         "ready" (. conn ["ready"])
         "posted" posted}))))
  => {"transport_id" "host"
      "ready" {"signal" "ready"
               "via" "self"}
      "posted" [{"signal" "ready"
                 "via" "self"}]})

^{:refer xt.substrate.transport-browser/disconnect :added "4.1"}
(fact "disconnect detaches the recorded transport from the node"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-then
     (event-node/attach-transport
      node
      "worker"
      {"stop_fn" (fn [_]
                  (return true))})
     (fn [_]
       (promise/x:promise-then
        (browser-transport/disconnect
         {"node" node
          "transport_id" "worker"})
        (fn [_]
          (repl/notify
           {"detached" (xt/x:nil? (event-node/get-transport node "worker"))
            "transport_id" "worker"}))))))
  => {"detached" true
      "transport_id" "worker"})


^{:refer xt.substrate.transport-browser/blob-url :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.transport-browser/webworker-source :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.transport-browser/sharedworker-source :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.transport-browser/sharedworker-url-source :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.transport-browser/node-worker-source :added "4.1"}
(fact "TODO")