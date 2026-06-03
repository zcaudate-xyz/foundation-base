(ns xt.db.system.client-sql-test
  (:require [hara.lang :as l])
  (:require [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.system.client-sql :as client]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.client-sql/client? :added "4.1"}
(fact "detects tagged sql clients"

  (!.js
    [(client/client? (client/client {"instance" "conn-1"}))
     (client/client? {"::" "db.client.sql"})
     (client/client? nil)])
  => [true true false])

^{:refer xt.db.system.client-sql/client :added "4.1"}
(fact "creates a tagged sql client"

  (!.js
    (var db (client/client {"instance" "conn-1"}))
    [(. db ["::"])
     (. db ["instance"])])
  => ["db.client.sql" "conn-1"])

^{:refer xt.db.system.client-sql/sql-gen-delete :added "4.1"}
(fact "generates one delete statement per id"

  (!.js
    (var out (client/sql-gen-delete
              "UserAccount"
              ["USER-0" "USER-1"]
              (ut/sqlite-opts nil)))
    [(xt/x:len out)
     (. (xt/x:first out) (includes "DELETE FROM \"UserAccount\""))
     (. (xt/x:second out) (includes "'USER-1'"))])
  => [2 true true])

^{:refer xt.db.system.client-sql/prepare-sync-input :added "4.1"}
(fact "emits sql upsert statements"

  (!.js
    (client/prepare-sync-input
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil)))
  => #"INSERT INTO \"UserAccount\"")

^{:refer xt.db.system.client-sql/process-event-sync :added "4.1"}
(fact "executes sql upsert statements"

  (!.js
    (var state {"raw" {"queries" []}})
    (xt/x:set-key state
                  "conn"
                  (dbsql/connection-create
                   (. state ["raw"])
                   {"query_sync" (fn [input_state input]
                                   (xt/x:arr-push (. input_state ["queries"]) input)
                                   (return input))}))
    (var touched
         (client/process-event-sync
          (client/client {"instance" (. state ["conn"])})
          "add"
          {"UserAccount" [sample/RootUser]}
          sample/Schema
          sample/SchemaLookup
          (ut/sqlite-opts nil)))
    [    (xtd/arr-lookup touched)
     (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.system.client-sql/prepare-event-input :added "4.1"}
(fact "emits sql delete statements"

  (!.js
   (client/prepare-event-input
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    (ut/sqlite-opts nil)))
  => #"DELETE FROM \"UserAccount\"")

^{:refer xt.db.system.client-sql/process-event-remove :added "4.1"}
(fact "executes sql delete statements"

  (!.js
   (var state {"raw" {"queries" []}})
    (xt/x:set-key state
                  "conn"
                  (dbsql/connection-create
                   (. state ["raw"])
                   {"query_sync" (fn [input_state input]
                                   (xt/x:arr-push (. input_state ["queries"]) input)
                                   (return input))}))
    (var touched
         (client/process-event-remove
          (client/client {"instance" (. state ["conn"])})
          "remove"
          {"UserAccount" [sample/RootUser]}
          sample/Schema
          sample/SchemaLookup
          (ut/sqlite-opts nil)))
    [(xtd/arr-lookup touched)
     (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.system.client-sql/exec-sync :added "4.1"}
(fact "executes raw sql through query-sync"

  (!.js
    (var state {"queries" []})
    (var out
         (client/exec-sync
          (client/client {"instance"
                          (dbsql/connection-create
                           state
                           {"query_sync" (fn [input_state input]
                                           (xt/x:arr-push (. input_state ["queries"]) input)
                                           (return {"ok" input}))})})
          "SELECT 1;"))
    [(xtd/get-in state ["queries" 0]) out])
  => ["SELECT 1;" {"ok" "SELECT 1;"}])

^{:refer xt.db.system.client-sql/pull-sync :added "4.1"}
(fact "returns decoded pull query results for tree and shorthand query forms"

  (!.js
    (client/pull-sync
     (client/client {"instance"
                     (dbsql/connection-create
                      {}
                      {"query_sync" (fn [_conn _input]
                                      (return [{"id" "USER-0"}]))})})
     sample/Schema
     ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}]
     (ut/sqlite-opts nil)))
  => [{"id" "USER-0"}]

  (!.js
    (var state {"queries" []})
    (var rows
        (client/pull-sync
         (client/client {"instance"
                         (dbsql/connection-create
                          state
                          {"query_sync" (fn [input_state input]
                                          (xt/x:arr-push (. input_state ["queries"]) input)
                                          (return [{"id" "USER-0"}]))})})
         sample/Schema
         ["UserAccount"
          {"id" "USER-0"}
          ["id"]]
         (ut/sqlite-opts nil)))
    [(xtd/get-in state ["queries" 0]) rows])
  => ["SELECT json_group_array(json_object('id', \"id\")) FROM \"UserAccount\"\n  WHERE \"id\" = 'USER-0'"
     [{"id" "USER-0"}]])

^{:refer xt.db.system.client-sql/pull :added "4.1"}
(fact "runs pulls through async sql semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var conn
         (dbsql/connection-create
          state
          {"query" (fn [input_state input]
                     (xt/x:arr-push (. input_state ["queries"]) input)
                     (return
                      (promise/x:promise-run
                       [{"id" "USER-0"}])))}))
    (promise/x:promise-catch
     (promise/x:promise-then
      (client/pull
       (client/client {"instance" conn})
       sample/Schema
       ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}]
       (ut/sqlite-opts nil))
      (fn [rows]
        (repl/notify [(xt/x:len (. state ["queries"]))
                      rows])))
     (fn [err]
       (repl/notify err))))
  => [1 [{"id" "USER-0"}]])

^{:refer xt.db.system.client-sql/record-add-sync :added "4.1"}
(fact "runs add statements through query-sync"

  (!.js
    (client/record-add-sync
     (client/client {"instance"
                     (dbsql/connection-create
                      {"queries" []}
                      {"query_sync" (fn [state input]
                                      (xt/x:arr-push (. state ["queries"]) input)
                                      (return input))})})
     sample/Schema
     "UserAccount"
     [{"id" "USER-0" "nickname" "root"}]
     (ut/sqlite-opts nil)))
  => #"INSERT INTO \"UserAccount\"")

^{:refer xt.db.system.client-sql/record-add :added "4.1"}
(fact "runs add statements through async sql semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var conn
         (dbsql/connection-create
          state
          {"query" (fn [input_state input]
                     (xt/x:arr-push (. input_state ["queries"]) input)
                     (return
                      (promise/x:promise-run input)))}))
    (promise/x:promise-then
     (client/record-add
      (client/client {"instance" conn})
      sample/Schema
      "UserAccount"
      [{"id" "USER-0" "nickname" "root"}]
      (ut/sqlite-opts nil))
     (fn [sql-input]
       (repl/notify [(xt/x:len (. state ["queries"]))
                     (. sql-input (includes "INSERT INTO \"UserAccount\""))]))))
  => [1 true])

^{:refer xt.db.system.client-sql/record-delete-sync :added "4.1"}
(fact "runs delete statements through query-sync"

  (!.js
    (client/record-delete-sync
     (client/client {"instance"
                     (dbsql/connection-create
                      {"queries" []}
                      {"query_sync" (fn [state input]
                                      (xt/x:arr-push (. state ["queries"]) input)
                                      (return input))})})
     sample/Schema
     "UserAccount"
     ["USER-0"]
     (ut/sqlite-opts nil)))
  => #"DELETE FROM \"UserAccount\"")

^{:refer xt.db.system.client-sql/record-delete :added "4.1"}
(fact "runs delete statements through async sql semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var conn
        (dbsql/connection-create
         state
         {"query" (fn [input_state input]
                    (xt/x:arr-push (. input_state ["queries"]) input)
                    (return
                     (promise/x:promise-run input)))}))
    (promise/x:promise-then
     (client/record-delete
     (client/client {"instance" conn})
     sample/Schema
     "UserAccount"
     ["USER-0"]
     (ut/sqlite-opts nil))
     (fn [sql-input]
      (repl/notify [(xt/x:len (. state ["queries"])) sql-input]))))
  => [1 "DELETE FROM \"UserAccount\" WHERE \"id\" = 'USER-0';"])