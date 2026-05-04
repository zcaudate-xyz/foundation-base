(ns xt.protocol.impl.type-pubsub-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.type-pubsub :as pub]
             [xt.event.node :as node]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.impl.type-pubsub :as pub]
             [xt.event.node :as node]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.impl.type-pubsub :as pub]
             [xt.event.node :as node]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.type-pubsub/pubsub-runtime-create :added "4.1"}
(fact "wraps node publish and subscribe functions behind the pubsub protocol"
  
  ^*(!.js
    (var runtime
         (pub/pubsub-runtime-create
          {:publish node/publish
           :receive_publish node/receive-publish
           :subscribe node/subscribe
           :unsubscribe node/unsubscribe
           :list_subscriptions node/list-subscriptions}))
    (var n (node/node-create {}))
    (pub/subscribe runtime
                   n
                   "event/seen"
                   "watcher"
                   (fn [id data t meta]
                     (return nil))
                   nil
                   nil)
    [(pub/pubsub-runtime? runtime)
     (pub/pubsub-runtime? nil)
     (pub/list-subscriptions runtime n "event/seen")
     #_#_
     (. (pub/unsubscribe runtime n "event/seen" "watcher") ["meta"] ["listener/id"])
     (pub/list-subscriptions runtime n "event/seen")])
  => [true false ["watcher"] "watcher" []])
