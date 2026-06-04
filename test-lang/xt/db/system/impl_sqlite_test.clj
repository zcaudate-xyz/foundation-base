(ns xt.db.system.impl-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.system.impl-sqlite :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn.js mock-client
  [conn settings]
  (var client
       (impl/sqlite-client
        sample/Schema
        sample/SchemaLookup
        (ut/sqlite-opts nil)
        settings))
  (xt/x:set-key client "instance" conn)
  (return client))

(defn.js connect-client
  []
  (return
   (impl/sqlite-client-init
    (impl/sqlite-client
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil)
     {"filename" ":memory:"})
    js-sqlite/driver)))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-sqlite/pull :added "4.1"}
(fact "pull reads tree and shorthand query forms from sqlite client context"

  (!.js
    (var client
         (-/mock-client
          (dbsql/connection-create
           {}
           {"query" (fn [_conn _input]
                      (return [{"id" "USER-0"}]))})
          {"filename" "local.sqlite"}))
    (impl/pull
     client
     ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}]))
  => [{"id" "USER-0"}]

  (!.js
    (var state {"queries" []})
    (var client
         (-/mock-client
          (dbsql/connection-create
           state
           {"query" (fn [input-state input]
                      (xt/x:arr-push (. input-state ["queries"]) input)
                      (return [{"id" "USER-0"}]))})
          {"filename" "local.sqlite"}))
    (var rows
         (impl/pull
          client
          ["UserAccount"
           {"id" "USER-0"}
           ["id"]]))
    [(xtd/get-in state ["queries" 0]) rows])
  => ["SELECT json_group_array(json_object('id', \"id\")) FROM \"UserAccount\"\n  WHERE \"id\" = 'USER-0'"
      [{"id" "USER-0"}]])

^{:refer xt.db.system.impl-sqlite/pull-async :added "4.1"}
(fact "pull-async reads through async sqlite semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (-/mock-client
          (dbsql/connection-create
           state
           {"query_async" (fn [input-state input]
                            (xt/x:arr-push (. input-state ["queries"]) input)
                            (return
                             (promise/x:promise-run
                              [{"id" "USER-0"}])))})
          {"filename" "local.sqlite"}))
    (promise/x:promise-then
     (impl/pull-async
      client
      ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}])
     (fn [rows]
       (repl/notify [(xt/x:len (. state ["queries"]))
                     rows]))))
  => [1 [{"id" "USER-0"}]])

^{:refer xt.db.system.impl-sqlite/record-add :added "4.1"}
(fact "record-add uses stored sqlite context"

  (!.js
    (impl/record-add
     (-/mock-client
      (dbsql/connection-create
       {"queries" []}
       {"query" (fn [state input]
                  (xt/x:arr-push (. state ["queries"]) input)
                  (return input))})
      {"filename" "local.sqlite"})
     "UserAccount"
     [{"id" "USER-0" "nickname" "root"}]))
  => #"INSERT INTO \"UserAccount\"")

^{:refer xt.db.system.impl-sqlite/record-add-async :added "4.1"}
(fact "record-add-async uses async sqlite semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (-/mock-client
          (dbsql/connection-create
           state
           {"query_async" (fn [input-state input]
                            (xt/x:arr-push (. input-state ["queries"]) input)
                            (return
                             (promise/x:promise-run input)))})
          {"filename" "local.sqlite"}))
    (promise/x:promise-then
     (impl/record-add-async
      client
      "UserAccount"
      [{"id" "USER-0" "nickname" "root"}])
     (fn [sql-input]
       (repl/notify [(xt/x:len (. state ["queries"]))
                     (. sql-input (includes "INSERT INTO \"UserAccount\""))]))))
  => [1 true])

^{:refer xt.db.system.impl-sqlite/record-delete :added "4.1"}
(fact "record-delete uses stored sqlite context"

  (!.js
    (impl/record-delete
     (-/mock-client
      (dbsql/connection-create
       {"queries" []}
       {"query" (fn [state input]
                  (xt/x:arr-push (. state ["queries"]) input)
                  (return input))})
      {"filename" "local.sqlite"})
     "UserAccount"
     ["USER-0"]))
  => #"DELETE FROM \"UserAccount\"")

^{:refer xt.db.system.impl-sqlite/record-delete-async :added "4.1"}
(fact "record-delete-async uses async sqlite semantics"

  (notify/wait-on :js
    (var state {"queries" []})
    (var client
         (-/mock-client
          (dbsql/connection-create
           state
           {"query_async" (fn [input-state input]
                            (xt/x:arr-push (. input-state ["queries"]) input)
                            (return
                             (promise/x:promise-run input)))})
          {"filename" "local.sqlite"}))
    (promise/x:promise-then
     (impl/record-delete-async
      client
      "UserAccount"
      ["USER-0"])
     (fn [sql-input]
       (repl/notify [(xt/x:len (. state ["queries"])) sql-input]))))
  => [1 "DELETE FROM \"UserAccount\" WHERE \"id\" = 'USER-0';"])

^{:refer xt.db.system.impl-sqlite/process-add-event :added "4.1"}
(fact "process-add-event executes sql upserts through stored sqlite context"

  (!.js
    (var state {"raw" {"queries" []}})
    (xt/x:set-key state
                  "conn"
                  (dbsql/connection-create
                   (. state ["raw"])
                   {"query" (fn [input-state input]
                              (xt/x:arr-push (. input-state ["queries"]) input)
                              (return input))}))
    (var client (-/mock-client (. state ["conn"]) {"filename" "local.sqlite"}))
    (var touched
         (impl/process-add-event
          client
          {"UserAccount" [sample/RootUser]}))
    [(xtd/arr-lookup touched)
     (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.system.impl-sqlite/process-remove-event :added "4.1"}
(fact "process-remove-event executes sql deletes through stored sqlite context"

  (!.js
    (var state {"raw" {"queries" []}})
    (xt/x:set-key state
                  "conn"
                  (dbsql/connection-create
                   (. state ["raw"])
                   {"query" (fn [input-state input]
                              (xt/x:arr-push (. input-state ["queries"]) input)
                              (return input))}))
    (var client (-/mock-client (. state ["conn"]) {"filename" "local.sqlite"}))
    (var touched
         (impl/process-remove-event
          client
          {"UserAccount" [sample/RootUser]}))
    [(xtd/arr-lookup touched)
     (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.system.impl-sqlite/exec-sync :added "4.1"}
(fact "exec-sync runs raw sql through the wired sqlite client"

  (notify/wait-on [:js 5000]
    (promise/x:promise-then
     (-/connect-client)
     (fn [client]
       (var out (impl/exec-sync client "SELECT 1;"))
       (dbsql/disconnect (. client ["instance"]))
       (repl/notify out))))
  => 1)

^{:refer xt.db.system.impl-sqlite/sqlite-client :added "4.1"}
(fact "creates the sqlite client config before connection init"

  (!.js
   (var client
        (impl/sqlite-client
         sample/Schema
         sample/SchemaLookup
         (ut/sqlite-opts nil)
         {"filename" ":memory:"}))
   {"tag" (. client ["::"])
    "has_instance" (xt/x:has-key? client "instance")
    "filename" (. (. client ["settings"]) ["filename"])})
  => {"tag" "db.client.sqlite"
      "has_instance" false
      "filename" ":memory:"})

^{:refer xt.db.system.impl-sqlite/sqlite-client-init :added "4.1"}
(fact "sqlite-client-init wires up js.lib.driver-sqlite and stores the connection"

  (notify/wait-on [:js 5000]
    (promise/x:promise-then
     (-/connect-client)
     (fn [client]
       (var instance (. client ["instance"]))
       (var out {"tag" (. client ["::"])
                 "connection" (dbsql/connection? instance)
                 "query" (dbsql/query instance "SELECT 1;")
                 "filename" (. (. client ["settings"]) ["filename"])})
       (dbsql/disconnect instance)
       (repl/notify out))))
  => {"tag" "db.client.sqlite"
      "connection" true
      "query" 1
      "filename" ":memory:"})