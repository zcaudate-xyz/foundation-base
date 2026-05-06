(ns xtbench.ruby.protocol.impl.type-pubsub-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.protocol.impl.type-pubsub :as pub]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.type-pubsub/pubsub-runtime? :added "4.1"}
(fact "checks pubsub runtime wrappers"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return signal))
           :receive_publish (fn [node frame ctx]
                              (return frame))
           :subscribe (fn [node space signal subscription-id meta]
                        (return subscription-id))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return subscription-id))
           :list_subscriptions (fn [node space signal]
                                 (return [signal]))}))
    [(pub/pubsub-runtime? runtime)
     (pub/pubsub-runtime? nil)])
  => [true false])

^{:refer xt.protocol.impl.type-pubsub/require-pubsub-runtime :added "4.1"}
(fact "returns validated pubsub runtimes"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return signal))
           :receive_publish (fn [node frame ctx]
                              (return frame))
           :subscribe (fn [node space signal subscription-id meta]
                        (return subscription-id))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return subscription-id))
           :list_subscriptions (fn [node space signal]
                                 (return [signal]))}))
    (. (pub/require-pubsub-runtime runtime) ["::"]))
  => "type.pubsub")

^{:refer xt.protocol.impl.type-pubsub/pubsub-runtime-create :added "4.1"}
(fact "creates wrapped pubsub runtimes"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return signal))
           :receive_publish (fn [node frame ctx]
                              (return frame))
           :subscribe (fn [node space signal subscription-id meta]
                        (return subscription-id))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return subscription-id))
           :list_subscriptions (fn [node space signal]
                                 (return [signal]))}))
    (. runtime ["::"]))
  => "type.pubsub")

^{:refer xt.protocol.impl.type-pubsub/publish :added "4.1"}
(fact "dispatches publish calls through the wrapped implementation"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return [space signal (. data ["value"]) (. meta ["tag"])]))
           :receive_publish (fn [node frame ctx]
                              (return frame))
           :subscribe (fn [node space signal subscription-id meta]
                        (return subscription-id))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return subscription-id))
           :list_subscriptions (fn [node space signal]
                                 (return [signal]))}))
    (pub/publish runtime {} "room/a" "event/ping" {"value" 1} {"tag" "v"}))
  => ["room/a" "event/ping" 1 "v"])

^{:refer xt.protocol.impl.type-pubsub/receive-publish :added "4.1"}
(fact "dispatches receive-publish calls through the wrapped implementation"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return signal))
           :receive_publish (fn [node frame ctx]
                              (return [(. frame ["signal"])
                                       (. ctx ["transport-id"])]))
           :subscribe (fn [node space signal subscription-id meta]
                        (return subscription-id))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return subscription-id))
           :list_subscriptions (fn [node space signal]
                                 (return [signal]))}))
    (pub/receive-publish runtime {} {"signal" "event/ping"} {"transport-id" "peer-a"}))
  => ["event/ping" "peer-a"])

^{:refer xt.protocol.impl.type-pubsub/subscribe :added "4.1"}
(fact "dispatches subscribe calls through the wrapped implementation"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return signal))
           :receive_publish (fn [node frame ctx]
                              (return frame))
           :subscribe (fn [node space signal subscription-id meta]
                        (return [space signal subscription-id (. meta ["via"])]))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return subscription-id))
           :list_subscriptions (fn [node space signal]
                                 (return [signal]))}))
    (pub/subscribe runtime {} "room/a" "event/ping" "watcher" {"via" "tab"}))
  => ["room/a" "event/ping" "watcher" "tab"])

^{:refer xt.protocol.impl.type-pubsub/unsubscribe :added "4.1"}
(fact "dispatches unsubscribe calls through the wrapped implementation"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return signal))
           :receive_publish (fn [node frame ctx]
                              (return frame))
           :subscribe (fn [node space signal subscription-id meta]
                        (return subscription-id))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return [space signal subscription-id]))
           :list_subscriptions (fn [node space signal]
                                 (return [signal]))}))
    (pub/unsubscribe runtime {} "room/a" "event/ping" "watcher" nil))
  => ["room/a" "event/ping" "watcher"])

^{:refer xt.protocol.impl.type-pubsub/list-subscriptions :added "4.1"}
(fact "dispatches list-subscriptions calls through the wrapped implementation"

  (!.rb
    (var runtime
         (pub/pubsub-runtime-create
          {:publish (fn [node space signal data meta]
                      (return signal))
           :receive_publish (fn [node frame ctx]
                              (return frame))
           :subscribe (fn [node space signal subscription-id meta]
                        (return subscription-id))
           :unsubscribe (fn [node space signal subscription-id meta]
                          (return subscription-id))
           :list_subscriptions (fn [node space signal]
                                 (return [space signal]))}))
    (pub/list-subscriptions runtime {} "room/a" "event/ping"))
  => ["room/a" "event/ping"])
