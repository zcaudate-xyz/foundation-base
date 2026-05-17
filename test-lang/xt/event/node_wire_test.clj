(ns xt.event.node-wire-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.event.node-wire :as node-wire]
             [xt.event.node-json :as node-json]
             [xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]
             [xt.event.node-transport-json :as json-transport]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.event.node-wire :as node-wire]
             [xt.event.node-json :as node-json]
             [xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]
             [xt.event.node-transport-json :as json-transport]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.event.node-wire :as node-wire]
             [xt.event.node-json :as node-json]
             [xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]
             [xt.event.node-transport-json :as json-transport]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-wire/memory-pair :added "4.1"}
(fact "memory-pair forwards raw text between peers"

  (!.js
   (var wire (node-wire/memory-pair {"left_id" "host"
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
   (var wire (node-wire/memory-pair {"left_id" "host"
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
   (var wire (node-wire/memory-pair {"left_id" "host"
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

^{:refer xt.event.node-wire/memory-pair :added "4.1"}
(fact "memory wires interoperate with json text transports"

  (!.js
   (var wire (node-wire/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var outbound [])
   (var inbound [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push outbound
                     {"text" (json-transport/event-text event)
                      "ctx" ctx})
      (return true)))
   (var transport
        (json-transport/text-endpoint
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
   (var wire (node-wire/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var outbound [])
   (var inbound [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push outbound
                     {"text" (json-transport/event-text event)
                      "ctx" ctx})
      (return true)))
   (var transport
        (json-transport/text-endpoint
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
   (var wire (node-wire/memory-pair {"left_id" "host"
                                     "right_id" "peer"}))
   (var outbound [])
   (var inbound [])
   ((. (. wire ["left"]) ["start_fn"])
    (fn [event ctx]
      (xt/x:arr-push outbound
                     {"text" (json-transport/event-text event)
                      "ctx" ctx})
      (return true)))
   (var transport
        (json-transport/text-endpoint
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
