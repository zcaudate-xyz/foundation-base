(ns xtbench.dart.db.system.impl-sqlite-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:require [[xt.lang.spec-base :as xt]
          [xt.lang.common-data :as xtd]
          [xt.lang.common-repl :as repl]
          [xt.lang.spec-promise :as promise]
          [xt.db.system.impl-sqlite :as impl]
          [xt.db.text.sql-util :as ut]
          [xt.db.text.sql-table :as sql-table]
          [xt.db.text.base-flatten :as f]
          [xt.db.helpers.data-main-test :as sample]
          [xt.net.conn-sql :as conn-sql]
          [dart.net.conn-sqlite :as dart-sqlite]]
          :runtime :twostep})

(defn.js connect-impl
  []
  (return
   (-> (impl/impl-sqlite (js-sqlite/create {"filename" ":memory:"})
                         sample/Schema
                         sample/SchemaLookup)
       (impl/impl-sqlite-init))))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-sqlite/pull :added "4.1"}
(fact "pull reads tree and shorthand query forms from sqlite impl context"

  (notify/wait-on :dart
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (impl/record-add impl "Currency" [{"id" "USD" "name" "USD"}
                                               {"id" "AUD" "name" "AUD"}])
           (repl/notify
            (impl/pull impl ["Currency"]))))))
  => (contains [(contains {"id" "USD" "name" "USD"})
                (contains {"id" "AUD" "name" "AUD"})]
               :in-any-order))

^{:refer xt.db.system.impl-sqlite/pull-async :added "4.1"}
(fact "pull-async reads through async sqlite semantics"

  (notify/wait-on :dart
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (impl/record-add impl "Currency" [{"id" "USD" "name" "USD"}
                                               {"id" "AUD" "name" "AUD"}])
           (return
            (impl/pull-async impl ["Currency"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains [(contains {"id" "USD" "name" "USD"})
                (contains {"id" "AUD" "name" "AUD"})]
               :in-any-order))

^{:refer xt.db.system.impl-sqlite/record-add :added "4.1"}
(fact "record-add uses stored sqlite context"

  (notify/wait-on :dart
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (repl/notify
            (impl/record-add impl "Currency" [{"id" "USD" "name" "USD"}
                                                {"id" "AUD" "name" "AUD"}]))))))
  => [])

^{:refer xt.db.system.impl-sqlite/clear-db :added "4.1"}
(fact "clear-db removes all data and keeps schema intact"

  (notify/wait-on :dart
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (impl/record-add impl "Currency" [{"id" "USD" "name" "USD"}
                                               {"id" "AUD" "name" "AUD"}])
           (impl/clear-db impl)
           (repl/notify
            (impl/pull impl ["Currency"]))))))
  => [])

^{:refer xt.db.system.impl-sqlite/process-add-event :added "4.1"}
(fact "process-add-event executes sql upserts through stored sqlite context"

  (notify/wait-on :dart
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl/process-add-event impl {"UserAccount" [sample/RootUser]}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => ["UserAccount" "UserProfile"])

^{:refer xt.db.system.impl-sqlite/process-remove-event :added "4.1"}
(fact "process-remove-event executes sql deletes through stored sqlite context"

  (notify/wait-on :dart
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl/process-remove-event impl {"UserAccount" [sample/RootUser]}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => ["UserAccount" "UserProfile"])

^{:refer xt.db.system.impl-sqlite/impl-sqlite :added "4.1"}
(fact "creates the sqlite impl config before connection init"

  (!.dt
    (impl/impl-sqlite (js-sqlite/create {"filename" ":memory:"})
                      sample/Schema
                      sample/SchemaLookup))
  => map?)

^{:refer xt.db.system.impl-sqlite/impl-sqlite-init :added "4.1"}
(fact "impl-sqlite-init wires up js.net.conn-sqlite and stores the connection"

  (notify/wait-on [:dart 5000]
    (-> (impl/impl-sqlite (js-sqlite/create {"filename" ":memory:"})
                          sample/Schema
                          sample/SchemaLookup)
        (impl/impl-sqlite-init)
        (promise/x:promise-then
         (fn [impl]
           (var #{client} impl)
           (repl/notify
            (conn-sql/query client "SELECT 1;"))))))
  => 1)

^{:refer xt.db.system.impl-sqlite/rpc-call-async :added "4.1"}
(fact "sqlite impl does not support remote rpc calls"

  (notify/wait-on :dart
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (return (impl/rpc-call-async impl {"id" "demo"} []))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"has_error" (xt/x:not-nil? err)})))))
  => (contains-in {"has_error" true}))
