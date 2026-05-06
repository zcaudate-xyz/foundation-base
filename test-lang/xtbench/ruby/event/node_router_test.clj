(ns xtbench.ruby.event.node-router-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node :as node]
             [xt.event.node-router :as router]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-router/subscribe-frame :added "4.1"}
(fact "constructs router control frames with space and signal metadata"

  (!.rb
    (var sub (router/subscribe-frame "room/a" "event/ping" "sub-a" {:via "tab"}))
    (var unsub (router/unsubscribe-frame "room/a" "event/ping" "sub-a" nil))
    [(. sub ["kind"])
     (. sub ["space"])
     (. sub ["signal"])
     (. sub ["meta"] ["via"])
     (. unsub ["kind"])
     (. unsub ["id"])])
  => ["subscribe" "room/a" "event/ping" "tab" "unsubscribe" "sub-a"])

^{:refer xt.event.node-router/unsubscribe-frame :added "4.1"}
(fact "constructs unsubscribe control frames"

  (!.rb
    (var frame (router/unsubscribe-frame nil "event/ping" nil {:via "tab"}))
    [(. frame ["kind"])
     (. frame ["space"])
     (. frame ["signal"])
     (. frame ["meta"] ["via"])
     (xt/x:is-string? (. frame ["id"]))])
  => ["unsubscribe" "$node" "event/ping" "tab" true])

^{:refer xt.event.node-router/ensure-router :added "4.1"}
(fact "creates router state and signal tables on demand"

  (!.rb
    (var n {"router" nil})
    (var router-state (router/ensure-router n))
    (var signal-subs (router/ensure-signal-subscriptions n "room/a" "event/ping"))
    [(xt/x:obj-keys router-state)
     (xt/x:obj-keys (router/ensure-space-subscriptions n "room/a"))
     signal-subs])
  => [["connections" "subscriptions"]
      ["event/ping"]
      {}])

^{:refer xt.event.node-router/get-connections :added "4.1"}
(fact "exposes router connection state"

  (!.rb
    (var n (node/node-create {}))
    (router/register-connection n "peer-a" {:role "edge"})
    (. (router/get-connections n) ["peer-a"] ["meta"] ["role"]))
  => "edge")

^{:refer xt.event.node-router/get-subscriptions :added "4.1"}
(fact "exposes router subscription state"

  (!.rb
    (var n (node/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    (. (router/get-subscriptions n) ["room/a"] ["event/ping"] ["peer-a"] ["id"]))
  => "sub-a")

^{:refer xt.event.node-router/register-connection :added "4.1"}
(fact "registers connection entries"

  (!.rb
    (var n (node/node-create {}))
    (var entry (router/register-connection n "peer-a" {:role "edge"}))
    [(. entry ["id"])
     (. entry ["meta"] ["role"])
     (. (router/get-connections n) ["peer-a"] ["id"])])
  => ["peer-a" "edge" "peer-a"])

^{:refer xt.event.node-router/prune-subscription-signal-loop :added "4.1"}
(fact "prunes one connection across all signals in a space"

  (!.rb
    (var space-subs {"event/a" {"peer-a" {:id "sub-a"}
                                "peer-b" {:id "sub-b"}}
                     "event/b" {"peer-a" {:id "sub-c"}}})
    (router/prune-subscription-signal-loop space-subs ["event/a" "event/b"] "peer-a" 0)
    [(. space-subs ["event/a"] ["peer-b"] ["id"])
     (. space-subs ["event/a"] ["peer-a"])
     (. space-subs ["event/b"])])
  => ["sub-b" nil nil])

^{:refer xt.event.node-router/prune-subscription-space-loop :added "4.1"}
(fact "prunes one connection across all spaces"

  (!.rb
    (var subs {"room/a" {"event/a" {"peer-a" {:id "sub-a"}
                                    "peer-b" {:id "sub-b"}}}
               "room/b" {"event/b" {"peer-a" {:id "sub-c"}}}})
    (router/prune-subscription-space-loop subs ["room/a" "room/b"] "peer-a" 0)
    [(. subs ["room/a"] ["event/a"] ["peer-b"] ["id"])
     (. subs ["room/b"])
     (xt/x:obj-keys subs)])
  => ["sub-b" nil ["room/a"]])

^{:refer xt.event.node-router/unregister-connection :added "4.1"}
(fact "unregisters connections and removes their subscriptions"

  (!.rb
    (var n (node/node-create {}))
    (router/register-connection n "peer-a" {:role "edge"})
    (router/add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    (var prev (router/unregister-connection n "peer-a"))
    [(. prev ["id"])
     (. (router/get-connections n) ["peer-a"])
     (router/list-subscriptions n "room/a" "event/ping")])
  => ["peer-a" nil []])

^{:refer xt.event.node-router/ensure-space-subscriptions :added "4.1"}
(fact "creates per-space subscription maps"

  (!.rb
    (var n (node/node-create {}))
    (var space-subs (router/ensure-space-subscriptions n nil))
    [(xt/x:obj-keys (router/get-subscriptions n))
     space-subs])
  => [["$node"] {}])

^{:refer xt.event.node-router/ensure-signal-subscriptions :added "4.1"}
(fact "creates per-signal subscription maps"

  (!.rb
    (var n (node/node-create {}))
    (var signal-subs (router/ensure-signal-subscriptions n "room/a" "event/ping"))
    [(. (router/get-subscriptions n) ["room/a"] ["event/ping"])
     signal-subs])
  => [{} {}])

^{:refer xt.event.node-router/add-subscription :added "4.1"}
(fact "stores and removes raw router subscription entries"

  (!.rb
    (var n (node/node-create {}))
    (router/register-connection n "peer-a" nil)
    (router/add-subscription n "peer-a" "room/a" "event/ping" "sub-a" {:via "tab"})
    (var before
      (xt/x:get-key
       (xt/x:get-key
        (xt/x:get-key (router/get-subscriptions n) "room/a")
        "event/ping")
       "peer-a"))
    (router/remove-subscription n "peer-a" "room/a" "event/ping")
    [(. before ["id"])
     (. before ["meta"] ["via"])
     (router/list-subscriptions n "room/a" "event/ping")])
  => ["sub-a" "tab" []])

^{:refer xt.event.node-router/remove-subscription :added "4.1"}
(fact "removes router subscription entries"

  (!.rb
    (var n (node/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    (var prev (router/remove-subscription n "peer-a" "room/a" "event/ping"))
    [(. prev ["id"])
     (router/list-subscriptions n "room/a" "event/ping")])
  => ["sub-a" []])

^{:refer xt.event.node-router/list-subscriptions :added "4.1"}
(fact "lists router subscriptions at each level"

  (!.rb
    (var n (node/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    [(xt/x:obj-keys (router/list-subscriptions n nil nil))
     (xt/x:obj-keys (router/list-subscriptions n "room/a" nil))
     (router/list-subscriptions n "room/a" "event/ping")])
  => [["room/a"] ["event/ping"] ["peer-a"]])

^{:refer xt.event.node-router/target-ids :added "4.1"}
(fact "lists target connection ids for a stream route"

  (!.rb
    (var n (node/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    (router/target-ids n "room/a" "event/ping"))
  => ["peer-a"])

^{:refer xt.event.node-router/receive-subscribe :added "4.1"}
(fact "processes inbound subscribe frames using ctx transport ids"

  (!.rb
    (var n (node/node-create {}))
    (router/receive-subscribe
     n
     (router/subscribe-frame "room/a" "event/ping" "sub-a" {:via "tab"})
     {"transport-id" "peer-a"})
    (router/list-subscriptions n "room/a" "event/ping"))
  => ["peer-a"])

^{:refer xt.event.node-router/receive-unsubscribe :added "4.1"}
(fact "processes inbound unsubscribe frames using ctx transport ids"

  (!.rb
    (var n (node/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    (router/receive-unsubscribe
     n
     (router/unsubscribe-frame "room/a" "event/ping" "sub-a" nil)
     {"transport-id" "peer-a"})
    (router/list-subscriptions n "room/a" "event/ping"))
  => [])
