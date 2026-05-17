(ns xt.event.node-pubsub
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.node-router :as router]
             [xt.event.node-space :as space]]})

(defspec.xt subscribe
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       xt.event.node-frame/NodeFrame])

(defspec.xt unsubscribe
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       xt.event.node-frame/NodeFrame])

(defspec.xt invoke-trigger
  [:fn [:xt/any xt.event.node-frame/NodeFrame] :xt/promise])

(defspec.xt receive-publish
  [:fn [:xt/any xt.event.node-frame/NodeFrame] :xt/promise])

(defn.xt subscribe
  "constructs a subscription control frame"
  {:added "4.1"}
  [node space signal subscription-id meta]
  (return (router/subscribe-frame space
                                  signal
                                  subscription-id
                                  meta)))

(defn.xt unsubscribe
  "constructs an unsubscription control frame"
  {:added "4.1"}
  [node space signal subscription-id meta]
  (return (router/unsubscribe-frame space
                                    signal
                                    subscription-id
                                    meta)))

(defn.xt invoke-trigger
  "invokes a registered trigger for a stream"
  {:added "4.1"}
  [node stream]
  (var signal (xt/x:get-key stream "signal"))
  (var entry (xt/x:get-key (xt/x:get-key node "triggers")
                           signal))
  (when (xt/x:nil? entry)
    (return (promise/x:promise-run nil)))
  (var current-space (space/ensure-space node
                                         (xt/x:get-key stream "space")
                                         nil))
  (var trigger-fn (xt/x:get-key entry "fn"))
  (var output (trigger-fn current-space stream node))
  (if (promise/x:promise-native? output)
    (return output)
    (return (promise/x:promise-run output))))

(defn.xt receive-publish
  "handles an inbound stream frame from the transport"
  {:added "4.1"}
  [node stream]
  (space/ensure-space node
                      (xt/x:get-key stream "space")
                      nil)
  (return
   (promise/x:promise-then
    (-/invoke-trigger node stream)
    (fn [_]
      (return stream)))))
