(ns xt.db.system.impl-sqlite-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true
                 :js           {:extra [[js.net.conn-sqlite :as js-sqlite]]}
                 :lua.nginx    {:extra [[lua.nginx.conn-sqlite :as lua-sqlite]]}
                 :python       {:extra [[python.net.conn-sqlite :as py-sqlite]]}
                 :dart         {:extra [[dart.net.conn-sqlite :as dart-sqlite]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-sqlite :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.base-flatten :as f]
             [xt.db.helpers.data-main-test :as sample]
             [xt.net.conn-sql :as conn-sql]
             ^{:seedgen/extra true}
             [js.net.conn-sqlite :as js-sqlite]]})

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
  
  (notify/wait-on :js
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

  (notify/wait-on :js
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

  (notify/wait-on :js
    (-> (-/connect-impl)
        (promise/x:promise-then
         (fn [impl]
           (repl/notify
            (impl/record-add impl "Currency" [{"id" "USD" "name" "USD"}
                                                {"id" "AUD" "name" "AUD"}]))))))
  => [])

^{:refer xt.db.system.impl-sqlite/record-delete :added "4.1"}
(fact "record-delete uses stored sqlite context")

^{:refer xt.db.system.impl-sqlite/clear-db :added "4.1"}
(fact "clear-db removes all data and keeps schema intact"

  (notify/wait-on :js
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

  (notify/wait-on :js
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
  
  (notify/wait-on :js
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

  (!.js
    (impl/impl-sqlite (js-sqlite/create {"filename" ":memory:"})
                      sample/Schema
                      sample/SchemaLookup))
  => map?)

^{:refer xt.db.system.impl-sqlite/impl-sqlite-init :added "4.1"}
(fact "impl-sqlite-init wires up js.net.conn-sqlite and stores the connection"
  
  (notify/wait-on [:js 5000]
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
(fact "TODO")