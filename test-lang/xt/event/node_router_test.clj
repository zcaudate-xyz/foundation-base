(ns xt.event.node-router-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node :as node]
             [xt.event.node-router :as router]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-router/subscribe-frame :added "4.1"}
(fact "constructs router control frames with space and signal metadata"

  (!.js
    (var sub (router/subscribe-frame "room/a" "event/ping" "sub-a" {:via "tab"}))
    (var unsub (router/unsubscribe-frame "room/a" "event/ping" "sub-a" nil))
    [(. sub ["kind"])
     (. sub ["space"])
     (. sub ["signal"])
     (. sub ["meta"] ["via"])
     (. unsub ["kind"])
     (. unsub ["id"])])
  => ["subscribe" "room/a" "event/ping" "tab" "unsubscribe" "sub-a"])

^{:refer xt.event.node-router/add-subscription :added "4.1"}
(fact "stores and removes raw router subscription entries"

  (!.js
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

^{:refer xt.event.node-router/ensure-router :added "4.1"}
(fact "creates router state and signal tables on demand"

  (!.js
    (var n {"router" nil})
    (var router-state (router/ensure-router n))
    (var signal-subs (router/ensure-signal-subscriptions n "room/a" "event/ping"))
    [(xt/x:obj-keys router-state)
     (xt/x:obj-keys (router/ensure-space-subscriptions n "room/a"))
     signal-subs])
  => [["connections" "subscriptions"]
      ["event/ping"]
      {}])
