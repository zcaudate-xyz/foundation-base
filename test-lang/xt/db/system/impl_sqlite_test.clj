(ns xt.db.system.impl-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.system.impl-sqlite :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-sqlite/sqlite-client :added "4.1"}
(fact "creates the thin sqlite client record with stored context"

  (!.js
   (impl/sqlite-client "conn-1"
                       sample/Schema
                       sample/SchemaLookup
                       (ut/sqlite-opts nil)
                       {"filename" "local.sqlite"}))
  => {"::" "db.client.sqlite"
      "instance" "conn-1"
      "schema" sample/Schema
      "lookup" sample/SchemaLookup
      "opts" (ut/sqlite-opts nil)
      "settings" {"filename" "local.sqlite"}})

^{:refer xt.db.system.impl-sqlite/process-event-sync :added "4.1"}
(fact "executes sql upsert statements through stored sqlite context"

  (!.js
   (var state {"raw" {"queries" []}})
   (xt/x:set-key state
                 "conn"
                 (dbsql/connection-create
                  (. state ["raw"])
                  {"query" (fn [input-state input]
                                  (xt/x:arr-push (. input-state ["queries"]) input)
                                  (return input))}))
   (var client (impl/sqlite-client (. state ["conn"])
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil)
                                   {"filename" "local.sqlite"}))
   (var touched
        (impl/process-event-sync
         client
         "add"
         {"UserAccount" [sample/RootUser]}))
   [(xtd/arr-lookup touched)
    (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.system.impl-sqlite/process-event-remove :added "4.1"}
(fact "executes sql delete statements through stored sqlite context"

  (!.js
   (var state {"raw" {"queries" []}})
   (xt/x:set-key state
                 "conn"
                 (dbsql/connection-create
                  (. state ["raw"])
                  {"query" (fn [input-state input]
                                  (xt/x:arr-push (. input-state ["queries"]) input)
                                  (return input))}))
   (var client (impl/sqlite-client (. state ["conn"])
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil)
                                   {"filename" "local.sqlite"}))
   (var touched
        (impl/process-event-remove
         client
         "remove"
         {"UserAccount" [sample/RootUser]}))
   [(xtd/arr-lookup touched)
    (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.system.impl-sqlite/pull-sync :added "4.1"}
(fact "pull-sync reads tree and shorthand query forms from sqlite client context"

  (!.js
   (var client
        (impl/sqlite-client
         (dbsql/connection-create
          {}
          {"query" (fn [_conn _input]
                          (return [{"id" "USER-0"}]))})
         sample/Schema
         sample/SchemaLookup
         (ut/sqlite-opts nil)
         {"filename" "local.sqlite"}))
   (impl/pull-sync
    client
    ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}]))
  => [{"id" "USER-0"}]

  (!.js
   (var state {"queries" []})
   (var client
        (impl/sqlite-client
         (dbsql/connection-create
          state
          {"query" (fn [input-state input]
                          (xt/x:arr-push (. input-state ["queries"]) input)
                          (return [{"id" "USER-0"}]))})
         sample/Schema
         sample/SchemaLookup
         (ut/sqlite-opts nil)
         {"filename" "local.sqlite"}))
   (var rows
        (impl/pull-sync
         client
         ["UserAccount"
          {"id" "USER-0"}
          ["id"]]))
   [(xtd/get-in state ["queries" 0]) rows])
  => ["SELECT json_group_array(json_object('id', \"id\")) FROM \"UserAccount\"\n  WHERE \"id\" = 'USER-0'"
      [{"id" "USER-0"}]])

^{:refer xt.db.system.impl-sqlite/pull :added "4.1"}
(fact "pull reads through async sqlite semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (impl/sqlite-client
          (dbsql/connection-create
           state
           {"query" (fn [input-state input]
                      (xt/x:arr-push (. input-state ["queries"]) input)
                      (return
                       (promise/x:promise-run
                        [{"id" "USER-0"}])))}))
          sample/Schema
          sample/SchemaLookup
          (ut/sqlite-opts nil)
          {"filename" "local.sqlite"}))
    (promise/x:promise-then
     (impl/pull
      client
      ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}])
     (fn [rows]
       (repl/notify [(xt/x:len (. state ["queries"]))
                     rows]))))
  => [1 [{"id" "USER-0"}]])

^{:refer xt.db.system.impl-sqlite/record-add-sync :added "4.1"}
(fact "record-add-sync uses stored sqlite context"

  (!.js
   (impl/record-add-sync
    (impl/sqlite-client
     (dbsql/connection-create
      {"queries" []}
      {"query" (fn [state input]
                      (xt/x:arr-push (. state ["queries"]) input)
                      (return input))})
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil)
     {"filename" "local.sqlite"})
    "UserAccount"
    [{"id" "USER-0" "nickname" "root"}]))
  => #"INSERT INTO \"UserAccount\"")

^{:refer xt.db.system.impl-sqlite/record-add :added "4.1"}
(fact "record-add uses async sqlite semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (impl/sqlite-client
          (dbsql/connection-create
           state
           {"query" (fn [input-state input]
                      (xt/x:arr-push (. input-state ["queries"]) input)
                      (return
                       (promise/x:promise-run input)))}))
          sample/Schema
          sample/SchemaLookup
          (ut/sqlite-opts nil)
          {"filename" "local.sqlite"}))
    (promise/x:promise-then
     (impl/record-add
      client
      "UserAccount"
      [{"id" "USER-0" "nickname" "root"}])
     (fn [sql-input]
       (repl/notify [(xt/x:len (. state ["queries"]))
                     (. sql-input (includes "INSERT INTO \"UserAccount\""))]))))
  => [1 true])

^{:refer xt.db.system.impl-sqlite/record-delete-sync :added "4.1"}
(fact "record-delete-sync uses stored sqlite context"

  (!.js
   (impl/record-delete-sync
    (impl/sqlite-client
     (dbsql/connection-create
      {"queries" []}
      {"query" (fn [state input]
                      (xt/x:arr-push (. state ["queries"]) input)
                      (return input))})
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil)
     {"filename" "local.sqlite"})
    "UserAccount"
    ["USER-0"]))
  => #"DELETE FROM \"UserAccount\"")

^{:refer xt.db.system.impl-sqlite/record-delete :added "4.1"}
(fact "record-delete uses async sqlite semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (impl/sqlite-client
          (dbsql/connection-create
           state
           {"query" (fn [input-state input]
                      (xt/x:arr-push (. input-state ["queries"]) input)
                      (return
                       (promise/x:promise-run input)))}))
          sample/Schema
          sample/SchemaLookup
          (ut/sqlite-opts nil)
          {"filename" "local.sqlite"}))
    (promise/x:promise-then
     (impl/record-delete
      client
      "UserAccount"
      ["USER-0"])
     (fn [sql-input]
       (repl/notify [(xt/x:len (. state ["queries"])) sql-input]))))
  => [1 "DELETE FROM \"UserAccount\" WHERE \"id\" = 'USER-0';"])

^{:refer xt.db.system.impl-sqlite/exec-sync :added "4.1"}
(fact "exec-sync runs raw sql through the sqlite client"

  (!.js
   (var state {"queries" []})
   (var out
        (impl/exec-sync
         (impl/sqlite-client
          (dbsql/connection-create
           state
           {"query" (fn [input-state input]
                           (xt/x:arr-push (. input-state ["queries"]) input)
                           (return {"ok" input}))})
          sample/Schema
          sample/SchemaLookup
          (ut/sqlite-opts nil)
          {"filename" "local.sqlite"})
         "SELECT 1;"))
   [(xtd/get-in state ["queries" 0]) out])
  => ["SELECT 1;" {"ok" "SELECT 1;"}])
