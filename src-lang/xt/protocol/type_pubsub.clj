(ns xt.protocol.type-pubsub
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt ITypePubSub
  ["publish"
   "receive_publish"
   "subscribe"
   "unsubscribe"
   "list_subscriptions"])

(def.xt ITypeRuntimePubSub
  (proto/iface-combine [-/ITypePubSub]))
