(ns xt.event.node-transport-json-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.event.node-frame :as frame]
             [xt.event.node-json :as node-json]
             [xt.event.node-wire :as node-wire]
             [xt.event.node-transport-json :as json-transport]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.event.node-frame :as frame]
             [xt.event.node-json :as node-json]
             [xt.event.node-wire :as node-wire]
             [xt.event.node-transport-json :as json-transport]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.event.node-frame :as frame]
             [xt.event.node-json :as node-json]
             [xt.event.node-wire :as node-wire]
             [xt.event.node-transport-json :as json-transport]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-json/encode-frame :added "4.1"}
(fact "encodes and decodes node frames with normalized JSON-safe errors"

  (!.js
    (var text (node-json/encode-frame
               (frame/response-error-frame
                "req-1"
                "room/a"
                "boom"
                nil)))
    (var out (node-json/decode-frame text))
    [(xt/x:is-string? text)
     (. out ["kind"])
     (. out ["reply_to"])
     (. out ["status"])
     (. out ["error"] ["message"])])
  => [true "response" "req-1" "error" "boom"]

  (!.lua
    (var text (node-json/encode-frame
               (frame/response-error-frame
                "req-1"
                "room/a"
                "boom"
                nil)))
    (var out (node-json/decode-frame text))
    [(xt/x:is-string? text)
     (. out ["kind"])
     (. out ["reply_to"])
     (. out ["status"])
     (. out ["error"] ["message"])])
  => [true "response" "req-1" "error" "boom"]

  (!.py
    (var text (node-json/encode-frame
               (frame/response-error-frame
                "req-1"
                "room/a"
                "boom"
                nil)))
    (var out (node-json/decode-frame text))
    [(xt/x:is-string? text)
     (. out ["kind"])
     (. out ["reply_to"])
     (. out ["status"])
     (. out ["error"] ["message"])])
  => [true "response" "req-1" "error" "boom"])

^{:refer xt.event.node-transport-json/text-endpoint :added "4.1"}
(fact "json endpoints emit encoded text and decode inbound frames over a wire"

  (notify/wait-on :js
    (var wire (node-wire/memory-pair {"left_id" "host"
                                      "right_id" "peer"}))
    (var outbound [])
    (var inbound [])
    ((. (. wire ["left"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push outbound {"text" (json-transport/event-text event)
                                "ctx" ctx})
       (return true)))
    (var endpoint (json-transport/text-endpoint (. wire ["right"])))
    ((. endpoint ["start_fn"])
     (fn [frame ctx]
       (xt/x:arr-push inbound {"frame" frame
                               "ctx" ctx})
       (return true)))
    ((. endpoint ["send_fn"])
     (frame/stream-frame "room/a" "event/pinged" {"count" 1} nil nil))
    ((. (. wire ["left"]) ["write_fn"])
     (node-json/encode-frame
      (frame/request-frame "room/a" "demo/echo" [{"ping" 1}] {"id" "req-echo"})))
    (repl/notify
     {"sent" (node-json/decode-frame (. (xt/x:first outbound) ["text"]))
      "received" (. (xt/x:first inbound) ["frame"])}))
  => (contains-in
      {"sent" {"kind" "stream"
               "space" "room/a"
               "signal" "event/pinged"
               "data" {"count" 1}}
       "received" {"kind" "request"
                   "space" "room/a"
                   "action" "demo/echo"
                   "args" [{"ping" 1}]
                   "id" "req-echo"}})

  (notify/wait-on :lua
    (var wire (node-wire/memory-pair {"left_id" "host"
                                      "right_id" "peer"}))
    (var outbound [])
    (var inbound [])
    ((. (. wire ["left"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push outbound {"text" (json-transport/event-text event)
                                "ctx" ctx})
       (return true)))
    (var endpoint (json-transport/text-endpoint (. wire ["right"])))
    ((. endpoint ["start_fn"])
     (fn [frame ctx]
       (xt/x:arr-push inbound {"frame" frame
                               "ctx" ctx})
       (return true)))
    ((. endpoint ["send_fn"])
     (frame/stream-frame "room/a" "event/pinged" {"count" 1} nil nil))
    ((. (. wire ["left"]) ["write_fn"])
     (node-json/encode-frame
      (frame/request-frame "room/a" "demo/echo" [{"ping" 1}] {"id" "req-echo"})))
    (repl/notify
     {"sent" (node-json/decode-frame (. (xt/x:first outbound) ["text"]))
      "received" (. (xt/x:first inbound) ["frame"])}))
  => (contains-in
      {"sent" {"kind" "stream"
               "space" "room/a"
               "signal" "event/pinged"
               "data" {"count" 1}}
       "received" {"kind" "request"
                   "space" "room/a"
                   "action" "demo/echo"
                   "args" [{"ping" 1}]
                   "id" "req-echo"}})

  (notify/wait-on :python
    (var wire (node-wire/memory-pair {"left_id" "host"
                                      "right_id" "peer"}))
    (var outbound [])
    (var inbound [])
    ((. (. wire ["left"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push outbound {"text" (json-transport/event-text event)
                                "ctx" ctx})
       (return true)))
    (var endpoint (json-transport/text-endpoint (. wire ["right"])))
    ((. endpoint ["start_fn"])
     (fn [frame ctx]
       (xt/x:arr-push inbound {"frame" frame
                               "ctx" ctx})
       (return true)))
    ((. endpoint ["send_fn"])
     (frame/stream-frame "room/a" "event/pinged" {"count" 1} nil nil))
    ((. (. wire ["left"]) ["write_fn"])
     (node-json/encode-frame
      (frame/request-frame "room/a" "demo/echo" [{"ping" 1}] {"id" "req-echo"})))
    (repl/notify
     {"sent" (node-json/decode-frame (. (xt/x:first outbound) ["text"]))
      "received" (. (xt/x:first inbound) ["frame"])}))
  => (contains-in
      {"sent" {"kind" "stream"
               "space" "room/a"
               "signal" "event/pinged"
               "data" {"count" 1}}
       "received" {"kind" "request"
                   "space" "room/a"
                   "action" "demo/echo"
                   "args" [{"ping" 1}]
                   "id" "req-echo"}}))
