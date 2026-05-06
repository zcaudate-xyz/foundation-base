(ns xtbench.ruby.protocol.type-pubsub-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.protocol.type-pubsub :as pubp]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.type-pubsub/ITypePubSub :added "4.1"}
(fact "defines the pubsub protocol surface"

  (!.rb
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
