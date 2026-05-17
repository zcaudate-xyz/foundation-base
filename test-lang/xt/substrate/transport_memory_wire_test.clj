(ns xt.substrate.transport-memory-wire-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate.transport-memory :as transport-memory]
             [xt.substrate.base-json :as node-json]
             [xt.lang.spec-base :as xt]
             [xt.substrate.base-frame :as frame]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.substrate.transport-memory :as transport-memory]
             [xt.substrate.base-json :as node-json]
             [xt.lang.spec-base :as xt]
             [xt.substrate.base-frame :as frame]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.substrate.transport-memory :as transport-memory]
             [xt.substrate.base-json :as node-json]
             [xt.lang.spec-base :as xt]
             [xt.substrate.base-frame :as frame]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.transport-memory/memory-pair :added "4.1"}
(fact "memory-pair forwards raw text between peers"

  (!.js
   (var wire (transport-memory/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var left [])
   (var right [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push left {"event" event "ctx" ctx})
      (return true)))
   ((. (. wire ["right"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push right {"event" event "ctx" ctx})
      (return true)))
   ((. (. wire ["left"]) ["write_fn"]) "ping-left")
   ((. (. wire ["right"]) ["write_fn"]) "ping-right")
   {"left" left
    "right" right})
  => {"left" [{"event" {"text" "ping-right"}
               "ctx" {"wire" "peer"
                      "peer" "host"}}]
      "right" [{"event" {"text" "ping-left"}
                "ctx" {"wire" "host"
                       "peer" "peer"}}]}

  (!.lua
   (var wire (transport-memory/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var left [])
   (var right [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push left {"event" event "ctx" ctx})
      (return true)))
   ((. (. wire ["right"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push right {"event" event "ctx" ctx})
      (return true)))
   ((. (. wire ["left"]) ["write_fn"]) "ping-left")
   ((. (. wire ["right"]) ["write_fn"]) "ping-right")
   {"left" left
    "right" right})
  => {"left" [{"event" {"text" "ping-right"}
               "ctx" {"wire" "peer"
                      "peer" "host"}}]
      "right" [{"event" {"text" "ping-left"}
                "ctx" {"wire" "host"
                       "peer" "peer"}}]}

  (!.py
   (var wire (transport-memory/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var left [])
   (var right [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push left {"event" event "ctx" ctx})
      (return true)))
   ((. (. wire ["right"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push right {"event" event "ctx" ctx})
      (return true)))
   ((. (. wire ["left"]) ["write_fn"]) "ping-left")
   ((. (. wire ["right"]) ["write_fn"]) "ping-right")
   {"left" left
    "right" right})
  => {"left" [{"event" {"text" "ping-right"}
               "ctx" {"wire" "peer"
                      "peer" "host"}}]
      "right" [{"event" {"text" "ping-left"}
                "ctx" {"wire" "host"
                       "peer" "peer"}}]})

^{:refer xt.substrate.transport-memory/memory-pair :added "4.1"}
(fact "memory wires interoperate with json text transports"

  (!.js
   (var wire (transport-memory/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var outbound [])
   (var inbound [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push outbound
                     {"text" (transport-memory/event-text event)
                      "ctx" ctx})
      (return true)))
   (var transport
        (transport-memory/text-endpoint
         (. wire ["right"])))
   ((. transport ["start_fn"])
    (fn [frame ctx]
      (xt/x:arr-push inbound {"frame" frame "ctx" ctx})
      (return true)))
   ((. transport ["send_fn"])
    (frame/stream-frame "room/a" "event/pinged" {"count" 1} nil nil))
   ((. (. wire ["left"]) ["write_fn"])
    (node-json/encode-frame
     (frame/request-frame "room/a" "demo/echo" [{"ping" 1}] {"id" "req-1"})))
   {"sent" (node-json/decode-frame (. (xt/x:first outbound) ["text"]))
    "received" (. (xt/x:first inbound) ["frame"])})
  => (contains-in
      {"sent" {"kind" "stream"
               "space" "room/a"
               "signal" "event/pinged"
               "data" {"count" 1}}
       "received" {"kind" "request"
                   "space" "room/a"
                   "action" "demo/echo"
                   "args" [{"ping" 1}]
                   "id" "req-1"}})

  (!.lua
   (var wire (transport-memory/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var outbound [])
   (var inbound [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push outbound
                     {"text" (transport-memory/event-text event)
                      "ctx" ctx})
      (return true)))
   (var transport
        (transport-memory/text-endpoint
         (. wire ["right"])))
   ((. transport ["start_fn"])
    (fn [frame ctx]
      (xt/x:arr-push inbound {"frame" frame "ctx" ctx})
      (return true)))
   ((. transport ["send_fn"])
    (frame/stream-frame "room/a" "event/pinged" {"count" 1} nil nil))
   ((. (. wire ["left"]) ["write_fn"])
    (node-json/encode-frame
     (frame/request-frame "room/a" "demo/echo" [{"ping" 1}] {"id" "req-1"})))
   {"sent" (node-json/decode-frame (. (xt/x:first outbound) ["text"]))
    "received" (. (xt/x:first inbound) ["frame"])})
  => (contains-in
      {"sent" {"kind" "stream"
               "space" "room/a"
               "signal" "event/pinged"
               "data" {"count" 1}}
       "received" {"kind" "request"
                   "space" "room/a"
                   "action" "demo/echo"
                   "args" [{"ping" 1}]
                   "id" "req-1"}})

  (!.py
   (var wire (transport-memory/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var outbound [])
   (var inbound [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push outbound
                     {"text" (transport-memory/event-text event)
                      "ctx" ctx})
      (return true)))
   (var transport
        (transport-memory/text-endpoint
         (. wire ["right"])))
   ((. transport ["start_fn"])
    (fn [frame ctx]
      (xt/x:arr-push inbound {"frame" frame "ctx" ctx})
      (return true)))
   ((. transport ["send_fn"])
    (frame/stream-frame "room/a" "event/pinged" {"count" 1} nil nil))
   ((. (. wire ["left"]) ["write_fn"])
    (node-json/encode-frame
     (frame/request-frame "room/a" "demo/echo" [{"ping" 1}] {"id" "req-1"})))
   {"sent" (node-json/decode-frame (. (xt/x:first outbound) ["text"]))
    "received" (. (xt/x:first inbound) ["frame"])})
  => (contains-in
      {"sent" {"kind" "stream"
               "space" "room/a"
               "signal" "event/pinged"
               "data" {"count" 1}}
       "received" {"kind" "request"
                   "space" "room/a"
                   "action" "demo/echo"
                   "args" [{"ping" 1}]
                   "id" "req-1"}}))
