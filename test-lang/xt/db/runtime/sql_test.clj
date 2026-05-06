(ns xt.db.runtime.sql-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.protocol.impl.connection-sql :as conn-sql]
             [xt.db.runtime.sql :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.sql/sql-gen-delete :added "4.1"}
(fact "generates delete statements"

  (!.js
   (impl/sql-gen-delete "HELLO"
                        ["A" "B"]
                        (ut/sqlite-opts nil)))
  => ["DELETE FROM \"HELLO\" WHERE \"id\" = 'A';"
      "DELETE FROM \"HELLO\" WHERE \"id\" = 'B';"])

^{:refer xt.db.runtime.sql/sql-process-event-sync :added "4.1"}
(fact "emits or executes upsert statements"

  (!.js
   (impl/sql-process-event-sync
    (conn-sql/connection-create
     {"queries" []}
     {"query_sync" (fn [state input]
                     (xt/x:arr-push (. state ["queries"]) input)
                     (return input))})
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    (ut/sqlite-opts nil)))
  => #"INSERT INTO \"UserAccount\""

  (!.js
   (var state {"raw" {"queries" []}})
   (xt/x:set-key state
                 "conn"
                 (conn-sql/connection-create
                  (. state ["raw"])
                  {"query_sync" (fn [input-state input]
                                  (xt/x:arr-push (. input-state ["queries"]) input)
                                  (return input))}))
   (var touched
        (impl/sql-process-event-sync
         (. state ["conn"])
         "add"
         {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         (ut/sqlite-opts nil)))
   [(xtd/arr-lookup touched)
    (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.runtime.sql/sql-process-event-remove :added "4.1"}
(fact "emits or executes delete statements"

  (!.js
   (impl/sql-process-event-remove
    (conn-sql/connection-create
     {"queries" []}
     {"query_sync" (fn [state input]
                     (xt/x:arr-push (. state ["queries"]) input)
                     (return input))})
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    (ut/sqlite-opts nil)))
  => #"DELETE FROM \"UserAccount\""

  (!.js
   (var state {"raw" {"queries" []}})
   (xt/x:set-key state
                 "conn"
                 (conn-sql/connection-create
                  (. state ["raw"])
                  {"query_sync" (fn [input-state input]
                                  (xt/x:arr-push (. input-state ["queries"]) input)
                                  (return input))}))
   (var touched
        (impl/sql-process-event-remove
         (. state ["conn"])
         "remove"
         {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         (ut/sqlite-opts nil)))
   [(xtd/arr-lookup touched)
    (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.runtime.sql/sql-pull-sync :added "4.1"}
(fact "decodes pull query results from json"

  (!.js
   (impl/sql-pull-sync
    (conn-sql/connection-create
     {}
     {"query_sync" (fn [_conn _input]
                     (return "[{\"id\":\"USER-0\"}]"))})
    sample/Schema
    ["UserAccount" ["id"]]
    (ut/sqlite-opts nil)))
  => [{"id" "USER-0"}])

^{:refer xt.db.runtime.sql/sql-delete-sync :added "4.1"}
(fact "runs delete statements through query-sync"

  (!.js
   (impl/sql-delete-sync
    (conn-sql/connection-create
     {"queries" []}
     {"query_sync" (fn [state input]
                     (xt/x:arr-push (. state ["queries"]) input)
                     (return input))})
    sample/Schema
    "UserAccount"
    ["USER-0"]
    (ut/sqlite-opts nil)))
  => #"DELETE FROM \"UserAccount\"")

^{:refer xt.db.runtime.sql/sql-clear :added "4.1"}
(fact "treats clear as a no-op success"

  (!.js
   (impl/sql-clear {}))
  => true)
