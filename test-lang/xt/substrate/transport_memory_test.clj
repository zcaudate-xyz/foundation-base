(ns xt.substrate.transport-memory-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-json :as node-json]
             [xt.substrate.transport-memory :as transport-memory]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-json :as node-json]
             [xt.substrate.transport-memory :as transport-memory]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-json :as node-json]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-json/encode-frame :added "4.1"}
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

^{:refer xt.substrate.transport-memory/text-endpoint :added "4.1"}
(fact "json endpoints emit encoded text and decode inbound frames over a wire"

  (notify/wait-on :js
    (var wire (transport-memory/memory-pair {"left_id" "host"
                                             "right_id" "peer"}))
    (var outbound [])
    (var inbound [])
    ((. (. wire ["left"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push outbound {"text" (transport-memory/event-text event)
                                "ctx" ctx})
       (return true)))
    (var endpoint (transport-memory/text-endpoint (. wire ["right"])))
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
    (var wire (transport-memory/memory-pair {"left_id" "host"
                                             "right_id" "peer"}))
    (var outbound [])
    (var inbound [])
    ((. (. wire ["left"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push outbound {"text" (transport-memory/event-text event)
                                "ctx" ctx})
       (return true)))
    (var endpoint (transport-memory/text-endpoint (. wire ["right"])))
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
    (var wire (transport-memory/memory-pair {"left_id" "host"
                                             "right_id" "peer"}))
    (var outbound [])
    (var inbound [])
    ((. (. wire ["left"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push outbound {"text" (transport-memory/event-text event)
                                "ctx" ctx})
       (return true)))
    (var endpoint (transport-memory/text-endpoint (. wire ["right"])))
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

^{:refer xt.substrate.transport-memory/memory-network :added "4.1"}
(fact "memory networks deliver outbound text to all configured peers"

  (notify/wait-on :js
    (var network (transport-memory/memory-network
                  {"hub" ["peer-a" "peer-b"]
                   "peer-a" ["hub"]
                   "peer-b" ["hub"]}))
    (var seen [])
    ((. (. network ["peer-a"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push seen {"id" "peer-a"
                            "text" (transport-memory/event-text event)
                            "wire" (. ctx ["wire"])
                            "peer" (. ctx ["peer"])})
       (return true)))
    ((. (. network ["peer-b"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push seen {"id" "peer-b"
                            "text" (transport-memory/event-text event)
                            "wire" (. ctx ["wire"])
                            "peer" (. ctx ["peer"])})
       (return true)))
    (-> ((. (. network ["hub"]) ["write_fn"]) "ping")
        (promise/x:promise-then
         (fn [_]
           (repl/notify seen)))))
  => [{"id" "peer-a"
       "text" "ping"
       "wire" "hub"
       "peer" "peer-a"}
      {"id" "peer-b"
       "text" "ping"
       "wire" "hub"
       "peer" "peer-b"}]

  (notify/wait-on :lua
    (var network (transport-memory/memory-network
                  {"hub" ["peer-a" "peer-b"]
                   "peer-a" ["hub"]
                   "peer-b" ["hub"]}))
    (var seen [])
    ((. (. network ["peer-a"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push seen {"id" "peer-a"
                            "text" (transport-memory/event-text event)
                            "wire" (. ctx ["wire"])
                            "peer" (. ctx ["peer"])})
       (return true)))
    ((. (. network ["peer-b"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push seen {"id" "peer-b"
                            "text" (transport-memory/event-text event)
                            "wire" (. ctx ["wire"])
                            "peer" (. ctx ["peer"])})
       (return true)))
    (-> ((. (. network ["hub"]) ["write_fn"]) "ping")
        (promise/x:promise-then
         (fn [_]
           (repl/notify seen)))))
  => [{"id" "peer-a"
       "text" "ping"
       "wire" "hub"
       "peer" "peer-a"}
      {"id" "peer-b"
       "text" "ping"
       "wire" "hub"
       "peer" "peer-b"}]

  (notify/wait-on :python
    (var network (transport-memory/memory-network
                  {"hub" ["peer-a" "peer-b"]
                   "peer-a" ["hub"]
                   "peer-b" ["hub"]}))
    (var seen [])
    ((. (. network ["peer-a"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push seen {"id" "peer-a"
                            "text" (transport-memory/event-text event)
                            "wire" (. ctx ["wire"])
                            "peer" (. ctx ["peer"])})
       (return true)))
    ((. (. network ["peer-b"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push seen {"id" "peer-b"
                            "text" (transport-memory/event-text event)
                            "wire" (. ctx ["wire"])
                            "peer" (. ctx ["peer"])})
       (return true)))
    (-> ((. (. network ["hub"]) ["write_fn"]) "ping")
        (promise/x:promise-then
         (fn [_]
           (repl/notify seen)))))
  => [{"id" "peer-a"
       "text" "ping"
       "wire" "hub"
       "peer" "peer-a"}
      {"id" "peer-b"
       "text" "ping"
       "wire" "hub"
       "peer" "peer-b"}])
