(ns xt.db.system.impl-sqlite-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.system.impl-sqlite :as impl]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.base-flatten :as f]
             [xt.db.helpers.data-main-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn.js mock-client
  [conn settings]
  (var client
       (impl/client-sqlite sample/Schema
                           sample/SchemaLookup
                           settings))
  (xt/x:set-key client "instance" conn)
  (return client))

(defn.js connect-client
  []
  (return
   (-> (impl/client-sqlite sample/Schema
                           sample/SchemaLookup
                           {"filename" ":memory:"})
       (impl/client-sqlite-init js-sqlite/driver))))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-sqlite/pull :added "4.1"}
(fact "pull reads tree and shorthand query forms from sqlite client context"
  
  (notify/wait-on :js
    (-> (-/connect-client)
        (promise/x:promise-then
         (fn [client]
           (impl/record-add client "Currency" [{"id" "USD" "name" "USD"}
                                               {"id" "AUD" "name" "AUD"}])
           (repl/notify
              (impl/pull client ["Currency"]))))))
  => (contains [(contains {"id" "USD" "name" "USD"})
                (contains {"id" "AUD" "name" "AUD"})]
               :in-any-order)


  
  
  
  (!.js
    (f/flatten-bulk sample/Schema
                    {"Currency" [{"id" "USD" "name" "USD"}]}))
  
  
  {"Currency" {"USD" {"ref_links" {}, "id" "USD", "rev_links" {}, "data" {"id" "USD"}}}}

  
  (!.js
    (xtd/arr-keepf [{"data" {"id" "USD"}, "id" "USD", "ref_links" {}, "rev_links" {}}]
                   sql-table/table-filter-id
                   sql-table/table-get-data))
  
  
  (!.js
    (sql-table/table-emit-flat-debug
     sql-table/table-emit-upsert
     sample/Schema
     sample/SchemaLookup
     {"Currency" {"USD" {"ref_links" {}, "id" "USD", "rev_links" {}, "data" {"id" "USD"}}}}
     (ut/sqlite-opts sample/SchemaLookup)))
  
  
  (!.js
    (sql-table/table-emit-flat-debug
     sql-table/table-emit-upsert
     sample/Schema
     sample/SchemaLookup
     {"Currency" {"USD" {"ref_links" {}, "id" "USD", "rev_links" {}, "data" {"id" "USD", "name" "USD"}}}}
     (ut/sqlite-opts sample/SchemaLookup)))
  
  (!.js
    (:= (!:G CLIENT) client))
  
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

^{:refer xt.db.system.impl-sqlite/client-sqlite :added "4.1"}
(fact "creates the sqlite client config before connection init"

  (!.js
    (var client
         (impl/client-sqlite
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

^{:refer xt.db.system.impl-sqlite/client-sqlite-init :added "4.1"}
(fact "client-sqlite-init wires up js.lib.driver-sqlite and stores the connection"

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

^{:refer xt.db.system.impl-sqlite/init-client :added "4.1"}
(fact "initiates the client"

  (notify/wait-on :js
    (-> (impl/init-client )))

  )
