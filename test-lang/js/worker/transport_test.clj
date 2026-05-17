(ns js.worker.transport-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-transport-browser :as worker-transport]]})

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
   (var transport (worker-transport/worker-endpoint worker))
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

^{:refer xt.event.node-transport-browser/worker-endpoint :added "4.1"}
(fact "uses create-fn worker sources without re-attaching listeners"
  (!.js
   (var posted [])
   (var received [])
   (var created 0)
   (var source
        {"create_fn"
         (fn [listener]
           (:= created (+ created 1))
           (return {"postMessage" (fn [msg] (posted.push msg))
                    "emit" (fn [msg] (listener msg nil))}))})
   (var transport (worker-transport/worker-endpoint source))
   (var worker
        ((. transport ["start_fn"])
         (fn [frame ctx]
           (received.push frame))))
   ((. transport ["send_fn"]) {"kind" "request" "id" "req-2"})
   ((. worker ["emit"]) {"kind" "response" "id" "res-2"})
   (return {"created" created
            "posted" posted
            "received" received}))
  => (contains-in {"created" 1
                   "posted" [{"kind" "request" "id" "req-2"}]
                   "received" [{"kind" "response" "id" "res-2"}]}))

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
   (var transport (worker-transport/self-endpoint worker-self))
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
(fact "unwraps worker message envelopes and passes raw payloads through"
  (!.js
   [(worker-transport/event-data {"data" {"id" "evt-1"}})
    (worker-transport/event-data {"id" "evt-2"})])
  => [{"id" "evt-1"}
      {"id" "evt-2"}])
