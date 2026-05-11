(ns xt.protocol.type-pubsub-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.type-pubsub :as pubp]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.type-pubsub :as pubp]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.type-pubsub :as pubp]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.type-pubsub/ITypePubSub :added "4.1"}
(fact "defines the pubsub protocol surface"

  (!.js
    [pubp/ITypePubSub
     pubp/ITypeRuntimePubSub])
  => [["publish"
       "receive_publish"
       "subscribe"
       "unsubscribe"
       "list_subscriptions"]
      ["publish"
       "receive_publish"
       "subscribe"
       "unsubscribe"
       "list_subscriptions"]]

  (!.lua
    [pubp/ITypePubSub
     pubp/ITypeRuntimePubSub])
  => [["publish"
       "receive_publish"
       "subscribe"
       "unsubscribe"
       "list_subscriptions"]
      ["publish"
       "receive_publish"
       "subscribe"
       "unsubscribe"
       "list_subscriptions"]]

  (!.py
    [pubp/ITypePubSub
     pubp/ITypeRuntimePubSub])
  => [["publish"
       "receive_publish"
       "subscribe"
       "unsubscribe"
       "list_subscriptions"]
      ["publish"
       "receive_publish"
       "subscribe"
       "unsubscribe"
       "list_subscriptions"]])
