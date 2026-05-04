(ns xt.protocol.impl.type-pubsub
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt pubsub-runtime?
  "checks if a value is a wrapped pubsub runtime"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "type.pubsub"
                   (xt/x:get-key obj "::")))))

(defn.xt require-pubsub-runtime
  "ensures a value is a wrapped pubsub runtime"
  {:added "4.1"}
  [value]
  (when (not (-/pubsub-runtime? value))
    (xt/x:err "Value is not a runtime pubsub type"))
  (return value))

(defn.xt pubsub-runtime-create
  "wraps an implementation map with the pubsub protocol"
  {:added "4.1"}
  [impl]
  (return {"::" "type.pubsub"
           "_impl" impl}))

(defn.xt publish
  "dispatches through the pubsub protocol"
  {:added "4.1"}
  [runtime node space signal data meta]
  (:= runtime (-/require-pubsub-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "publish")
           node space signal data meta)))

(defn.xt receive-publish
  "dispatches receive_publish through the pubsub protocol"
  {:added "4.1"}
  [runtime node frame]
  (:= runtime (-/require-pubsub-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "receive_publish")
           node frame)))

(defn.xt subscribe
  "dispatches subscribe through the pubsub protocol"
  {:added "4.1"}
  [runtime node signal subscription-id callback meta pred]
  (:= runtime (-/require-pubsub-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "subscribe")
           node signal subscription-id callback meta pred)))

(defn.xt unsubscribe
  "dispatches unsubscribe through the pubsub protocol"
  {:added "4.1"}
  [runtime node signal subscription-id]
  (:= runtime (-/require-pubsub-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "unsubscribe")
           node signal subscription-id)))

(defn.xt list-subscriptions
  "dispatches list_subscriptions through the pubsub protocol"
  {:added "4.1"}
  [runtime node signal]
  (:= runtime (-/require-pubsub-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "list_subscriptions")
           node signal)))
