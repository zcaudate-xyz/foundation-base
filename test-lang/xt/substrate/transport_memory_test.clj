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


^{:refer xt.substrate.transport-memory/event-text :added "4.1"}
(fact "unwraps memory endpoint events and passes raw text through"
  (!.js
   [(transport-memory/event-text {"data" "ping"})
    (transport-memory/event-text {"text" "pong"})
    (transport-memory/event-text "echo")])
  => ["ping" "pong" "echo"])

^{:refer xt.substrate.transport-memory/network-targets :added "4.1"}
(fact "normalizes link config into peer id arrays"
  (!.js
   [(transport-memory/network-targets nil)
    (transport-memory/network-targets ["a" "b"])
    (transport-memory/network-targets "hub")])
  => [[] ["a" "b"] ["hub"]])

^{:refer xt.substrate.transport-memory/ensure-network-state :added "4.1"}
(fact "creates and reuses shared network state for endpoint ids"
  (!.js
   (var network {"states" {}})
   (var state-a (transport-memory/ensure-network-state network "peer-a"))
   (var state-b (transport-memory/ensure-network-state network "peer-a"))
   [(. state-a ["id"])
    (== nil (. state-a ["listener"]))
    (. state-a ["peers"])
    (== state-a state-b)
    (xt/x:obj-keys (. network ["states"]))])
  => ["peer-a" true [] true ["peer-a"]])

^{:refer xt.substrate.transport-memory/ensure-network-targets-loop :added "4.1"}
(fact "materializes peer states for each configured target"
  (!.js
   (var network {"states" {}})
   (transport-memory/ensure-network-targets-loop network ["peer-a" "peer-b"] 0)
   (xt/x:obj-keys (. network ["states"])))
  => ["peer-a" "peer-b"])

^{:refer xt.substrate.transport-memory/configure-network-links-loop :added "4.1"}
(fact "applies link config and populates peer relationships"
  (!.js
   (var network {"states" {}})
   (transport-memory/configure-network-links-loop
    network
    {"hub" ["peer-a" "peer-b"]}
    ["hub"]
    0)
   {"keys" (xt/x:obj-keys (. network ["states"]))
    "hub-peers" (. (. (. network ["states"]) ["hub"]) ["peers"])})
  => {"keys" ["hub" "peer-a" "peer-b"]
      "hub-peers" ["peer-a" "peer-b"]})

^{:refer xt.substrate.transport-memory/create-network-endpoints-loop :added "4.1"}
(fact "builds memory endpoints from configured network state"
  (!.js
   (var network {"states" {}})
   (transport-memory/configure-network-links-loop
    network
    {"hub" ["peer-a"]}
    ["hub"]
    0)
   (var out (transport-memory/create-network-endpoints-loop
             network
             ["hub" "peer-a"]
             {}
             0))
   [(xt/x:obj-keys out)
    (. (. out ["hub"]) ["meta"] ["kind"])
    (. (. out ["peer-a"]) ["meta"] ["id"])])
  => [["hub" "peer-a"] "wire.memory" "peer-a"])

^{:refer xt.substrate.transport-memory/deliver-network-loop :added "4.1"}
(fact "delivers text to configured peers in order with wire context"
  (notify/wait-on :js
    (var seen [])
    (var network {"states" {"hub" {"id" "hub"}
                            "peer-a" {"id" "peer-a"
                                      "listener" (fn [event ctx]
                                                   (xt/x:arr-push seen {"peer" "peer-a"
                                                                        "text" (transport-memory/event-text event)
                                                                        "ctx" ctx})
                                                   (return true))}
                            "peer-b" {"id" "peer-b"
                                      "listener" (fn [event ctx]
                                                   (xt/x:arr-push seen {"peer" "peer-b"
                                                                        "text" (transport-memory/event-text event)
                                                                        "ctx" ctx})
                                                   (return true))}}})
    (promise/x:promise-then
     (transport-memory/deliver-network-loop
      network
      {"id" "hub"}
      ["peer-a" "peer-b"]
      "ping"
      0)
     (fn [_]
       (repl/notify seen))))
  => [{"peer" "peer-a"
       "text" "ping"
       "ctx" {"wire" "hub"
              "peer" "peer-a"}}
      {"peer" "peer-b"
       "text" "ping"
       "ctx" {"wire" "hub"
              "peer" "peer-b"}}])

^{:refer xt.substrate.transport-memory/memory-endpoint :added "4.1"}
(fact "writes to its peer listener and clears the listener on stop"
  (notify/wait-on :js
    (var seen [])
    (var peer {"id" "peer"
               "listener" (fn [event ctx]
                            (xt/x:arr-push seen {"text" (transport-memory/event-text event)
                                                 "ctx" ctx})
                            (return true))})
    (var state {"id" "host"
                "listener" nil
                "peer" peer})
    (var endpoint (transport-memory/memory-endpoint state))
    ((. endpoint ["start_fn"]) (fn [event ctx] event))
    ((. endpoint ["write_fn"]) "pong")
    ((. endpoint ["stop_fn"]) nil)
    (repl/notify
     {"seen" seen
      "listener" (. state ["listener"])}))
  => {"seen" [{"text" "pong"
               "ctx" {"wire" "host"
                      "peer" "peer"}}]
      "listener" nil})

^{:refer xt.substrate.transport-memory/memory-pair :added "4.1"}
(fact "creates a bidirectional in-memory pair with configured ids"
  (notify/wait-on :js
    (var pair (transport-memory/memory-pair {"left_id" "host"
                                             "right_id" "peer"}))
    (var seen [])
    ((. (. pair ["right"]) ["start_fn"])
     (fn [event ctx]
       (xt/x:arr-push seen {"text" (transport-memory/event-text event)
                            "ctx" ctx})
       (return true)))
    ((. (. pair ["left"]) ["write_fn"]) "hello")
    (repl/notify
     {"left" (. (. pair ["left"]) ["meta"] ["id"])
      "right" (. (. pair ["right"]) ["meta"] ["id"])
      "seen" seen}))
  => {"left" "host"
      "right" "peer"
      "seen" [{"text" "hello"
               "ctx" {"wire" "host"
                      "peer" "peer"}}]})