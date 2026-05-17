(ns xt.event.node-transport-browser-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-transport-browser :as browser-transport]]})

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

^{:refer xt.event.node-transport-browser/worker-endpoint :added "4.1"}
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

^{:refer xt.event.node-transport-browser/messageport-endpoint :added "4.1"}
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

^{:refer xt.event.node-transport-browser/sharedworker-endpoint :added "4.1"}
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

^{:refer xt.event.node-transport-browser/self-endpoint :added "4.1"}
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

^{:refer xt.event.node-transport-browser/event-data :added "4.1"}
(fact "unwraps browser worker message envelopes and passes raw payloads through"
  (!.js
   [(browser-transport/event-data {"data" {"id" "evt-1"}})
    (browser-transport/event-data {"id" "evt-2"})])
  => [{"id" "evt-1"}
      {"id" "evt-2"}])
