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
(fact "TODO")

^{:refer xt.db.system.client-sql/client :added "4.1"}
(fact "creates a tagged sql client"

  (!.js
    (var db (client/client {"instance" "conn-1"}))
    [(. db ["::"])
     (. db ["instance"])])
  => ["db.client.sql" "conn-1"])

^{:refer xt.db.system.client-sql/sql-gen-delete :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-sql/process-event-sync :added "4.1"}
(fact "emits or executes sql upsert statements"

  (!.js
    (client/process-event-sync
     (client/client {"instance"
                     (dbsql/connection-create
                      {"queries" []}
                      {"query_sync" (fn [state input]
                                      (xt/x:arr-push (. state ["queries"]) input)
                                      (return input))})})
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
    [(xtd/arr-lookup touched)
     (xt/x:len (xtd/get-in state ["raw" "queries"]))])
  => [{"UserAccount" true "UserProfile" true} 1])

^{:refer xt.db.system.client-sql/process-event-remove :added "4.1"}
(fact "emits or executes sql delete statements"

  (!.js
    (client/process-event-remove
     (client/client {"instance"
                     (dbsql/connection-create
                      {"queries" []}
                      {"query_sync" (fn [state input]
                                      (xt/x:arr-push (. state ["queries"]) input)
                                      (return input))})})
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
(fact "TODO")

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

^{:refer xt.db.system.client-sql/delete-sync :added "4.1"}
(fact "runs delete statements through query-sync"

  (!.js
    (client/delete-sync
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

^{:refer xt.db.system.client-sql/delete :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-sql/clear :added "4.1"}
(fact "treats clear as a no-op success"

  (!.js
    (client/clear {}))
  => true)