(ns xt.db.node.adapter-events-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.system.impl-common :as impl-common]
            [xt.lang.common-protocol :as proto]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.db.node.adaptor-base :as adaptor]
             [xt.db.node.adapter-events :as events]
             [xt.db.system :as xdb]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.helpers.data-main-test :as sample]
             [xt.lang.common-protocol :as proto]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.adapter-events/topic-for :added "4.1"}
(fact "builds entity-scoped topics"

  (!.js (events/topic-for "User" "u-1"))
  => "User:u-1"

  (!.js (events/topic-for "Organisation" "org-1"))
  => "Organisation:org-1")

^{:refer xt.db.node.adapter-events/topics-for :added "4.1"}
(fact "builds topics for entity collections"

  (!.js (events/topics-for
         [["User" ["u-1" "u-2"]]
          ["Topic" "t-1"]]))
  => ["User:u-1" "User:u-2" "Topic:t-1"]

  (!.js (events/topics-for
         {"User" ["u-1"]
          "Topic" ["t-1" "t-2"]}))
  => ["User:u-1" "Topic:t-1" "Topic:t-2"])

^{:refer xt.db.node.adapter-events/apply-broadcast :added "4.1"}
(fact "applies a broadcast payload to db/caching"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main
         {"primary" {"type" "memory" "defaults" {}}
          "caching" {"type" "memory" "defaults" {}}}
         sample/Schema
         sample/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (-> (events/apply-broadcast
                node
                {"db/sync" {"Currency" [{"id" "USD"
                                          "name" "US Dollar"
                                          "symbol" "$"}]}})
               (promise/x:promise-then
                (fn [_]
                  (var caching (substrate/get-service node "db/caching"))
                  (return (repl/notify
                           (impl-memory/pull caching
                                             ["Currency"
                                              {"id" "USD"}
                                              ["name"]]))))))))))
  => [{"name" "US Dollar"}])

^{:refer xt.db.node.adapter-events/make-broadcast-callback :added "4.1"}
(fact "callback applies xt.db event vectors to db/caching"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main
         {"primary" {"type" "memory" "defaults" {}}
          "caching" {"type" "memory" "defaults" {}}}
         sample/Schema
         sample/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (var callback (events/make-broadcast-callback node))
           (-> (callback ["add" {"Currency" [{"id" "USD"
                                              "name" "US Dollar"
                                              "symbol" "$"}]}])
               (promise/x:promise-then
                (fn [_]
                  (var caching (substrate/get-service node "db/caching"))
                  (return (repl/notify
                           (impl-memory/pull caching
                                             ["Currency"
                                              {"id" "USD"}
                                              ["name"]]))))))))))
  => [{"name" "US Dollar"}])

^{:refer xt.db.node.adapter-events/apply-broadcast-handler :added "4.1"}
(fact "@xt.db/apply-broadcast applies payloads to db/caching"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main
         {"primary" {"type" "memory" "defaults" {}}
          "caching" {"type" "memory" "defaults" {}}}
         sample/Schema
         sample/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (events/init-adaptor-events node {})
           (-> (substrate/request node
                                  "room/a"
                                  "@xt.db/apply-broadcast"
                                  [{"db/sync" {"Currency" [{"id" "USD"
                                                             "name" "US Dollar"
                                                             "symbol" "$"}]}}]
                                  {})
               (promise/x:promise-then
                (fn [_]
                  (var caching (substrate/get-service node "db/caching"))
                  (return (repl/notify
                           (impl-memory/pull caching
                                             ["Currency"
                                              {"id" "USD"}
                                              ["name"]]))))))))))
  => [{"name" "US Dollar"}])

^{:refer xt.db.node.adapter-events/init-handlers :added "4.1"}
(fact "registers the @xt.db/apply-broadcast handler"

  (!.js
   (var node (substrate/node-create {}))
   (events/init-handlers node)
   (xtd/get-in node ["handlers" "@xt.db/apply-broadcast" "id"]))
  => "@xt.db/apply-broadcast")

(defn.js make-mock-pubsub
  "creates a mock pubsub impl by manually registering IPubSub methods"
  []
  (var impl {"::" "xt.db.node.adapter_events_test/MockPubSub"
             "state" {"calls" [] "callback" nil}})
  (proto/register-protocol-impl
   (xt/x:get-key impl-common/IPubSub "on")
   "xt.db.node.adapter_events_test/MockPubSub"
   {"subscribe" (fn [impl topic opts callback]
                  (var state (xt/x:get-key impl "state"))
                  (var calls (or (xt/x:get-key state "calls") []))
                  (xt/x:arr-push calls {"type" "subscribe" "topic" topic "opts" opts})
                  (xt/x:set-key state "calls" calls)
                  (xt/x:set-key state "callback" callback)
                  (return (promise/x:promise-run
                           {"id" "mock-1" "impl" impl "topic" topic})))
    "unsubscribe" (fn [impl handle] (return (promise/x:promise-run true)))
    "publish" (fn [impl topic message opts] (return (promise/x:promise-run nil)))})
  (return impl))

^{:refer xt.db.node.adapter-events/subscribe-topic :added "4.1"}
(fact "subscribes a pubsub service to an entity topic"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main
         {"primary" {"type" "memory" "defaults" {}}
          "caching" {"type" "memory" "defaults" {}}}
         sample/Schema
         sample/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (var mock (-/make-mock-pubsub))
           (substrate/set-service node "db/pubsub" mock)
           (-> (events/subscribe-topic node "db/pubsub" "User:u-1" {})
               (promise/x:promise-then
                (fn [handle]
                  (var state (xt/x:get-key mock "state"))
                  (return (repl/notify
                           {"topic" (xt/x:get-key handle "topic")
                            "calls" (xt/x:get-key state "calls")})))))))))
  => {"topic" "User:u-1"
      "calls" [{"type" "subscribe" "topic" "User:u-1" "opts" {}}]})

^{:refer xt.db.node.adapter-events/subscribe-entities :added "4.1"}
(fact "subscribes to multiple entity topics"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main
         {"primary" {"type" "memory" "defaults" {}}
          "caching" {"type" "memory" "defaults" {}}}
         sample/Schema
         sample/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (var mock (-/make-mock-pubsub))
           (substrate/set-service node "db/pubsub" mock)
           (-> (promise/x:promise-all
                (events/subscribe-entities
                 node
                 "db/pubsub"
                 {"User" ["u-1" "u-2"]
                  "Topic" ["t-1"]}
                 {}))
               (promise/x:promise-then
                (fn [handles]
                  (return (repl/notify
                           (xt/x:arr-map handles
                                         (fn [h]
                                           (return (xt/x:get-key h "topic")))))))))))))
  => ["User:u-1" "User:u-2" "Topic:t-1"])


^{:refer xt.db.node.adapter-events/unsubscribe-topic :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.adapter-events/init-adaptor-events :added "4.1"}
(fact "TODO")