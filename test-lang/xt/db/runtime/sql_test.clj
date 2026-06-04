(ns xt.db.runtime.sql-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.sql/sql-gen-delete :added "4.1"}
(fact "generates delete statements"

  (!.js
   (impl-sql/sql-gen-delete "HELLO"
                            ["A" "B"]
                            (ut/sqlite-opts nil)))
  => ["DELETE FROM \"HELLO\" WHERE \"id\" = 'A';"
      "DELETE FROM \"HELLO\" WHERE \"id\" = 'B';"])

^{:refer xt.db.runtime.sql/sql-process-event-sync :added "4.1"}
(fact "emits or executes upsert statements"

  (!.js
   (impl-sql/sql-process-event-sync
    (dbsql/connection-create
     {"queries" []}
     {"query" (fn [state input]
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
                 (dbsql/connection-create
                  (. state ["raw"])
                  {"query" (fn [input-state input]
                                  (xt/x:arr-push (. input-state ["queries"]) input)
                                  (return input))}))
   (var touched
        (impl-sql/sql-process-event-sync
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
   (impl-sql/sql-process-event-remove
    (dbsql/connection-create
     {"queries" []}
     {"query" (fn [state input]
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
                 (dbsql/connection-create
                  (. state ["raw"])
                  {"query" (fn [input-state input]
                                  (xt/x:arr-push (. input-state ["queries"]) input)
                                  (return input))}))
   (var touched
        (impl-sql/sql-process-event-remove
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
(fact "returns decoded pull query results"

  (!.js
   (impl-sql/sql-pull-sync
    (dbsql/connection-create
     {}
     {"query" (fn [_conn _input]
                     (return [{"id" "USER-0"}]))})
    sample/Schema
    ["UserAccount" ["id"]]
    (ut/sqlite-opts nil)))
  => [{"id" "USER-0"}])

^{:refer xt.db.runtime.sql/sql-pull-sync.string :added "4.1"}
(fact "rejects string pull query results"

  (!.js
   (impl-sql/sql-pull-sync
    (dbsql/connection-create
     {}
     {"query" (fn [_conn _input]
                     (return "[{\"id\":\"USER-0\"}]"))})
    sample/Schema
    ["UserAccount" ["id"]]
    (ut/sqlite-opts nil)))
  => (throws))

^{:refer xt.db.runtime.sql/sql-delete-sync :added "4.1"}
(fact "runs delete statements through query"

  (!.js
   (impl-sql/sql-delete-sync
    (dbsql/connection-create
     {"queries" []}
     {"query" (fn [state input]
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
   (impl-sql/sql-clear {}))
  => true)


^{:refer xt.db.runtime.sql/sql-pull :added "4.1"}
(fact "runs pull statements through async query semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var conn
         (dbsql/connection-create
          state
          {"query" (fn [input-state input]
                     (xt/x:arr-push (. input-state ["queries"]) input)
                     (return
                      (spec-promise/x:promise-run
                       [{"id" "USER-0"}])))}))
    (spec-promise/x:promise-catch
     (spec-promise/x:promise-then
      (impl-sql/sql-pull
       conn
       sample/Schema
       ["UserAccount" ["id"]]
       (ut/sqlite-opts nil))
      (fn [rows]
        (repl/notify [(xt/x:len (. state ["queries"]))
                      rows])))
     (fn [err]
       (repl/notify err))))
  => [1
      [{"id" "USER-0"}]])

^{:refer xt.db.runtime.sql/sql-delete :added "4.1"}
(fact "runs delete statements through async query semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var conn
         (dbsql/connection-create
          state
          {"query" (fn [input-state input]
                     (xt/x:arr-push (. input-state ["queries"]) input)
                     (return
                      (spec-promise/x:promise-run
                       input)))}))
    (spec-promise/x:promise-catch
     (spec-promise/x:promise-then
      (impl-sql/sql-delete
       conn
       sample/Schema
       "UserAccount"
       ["USER-0"]
       (ut/sqlite-opts nil))
      (fn [stmt]
        (repl/notify [(xt/x:len (. state ["queries"]))
                      stmt])))
     (fn [err]
       (repl/notify err))))
  => [1
      "DELETE FROM \"UserAccount\" WHERE \"id\" = 'USER-0';"])