(ns xt.db.runtime.sql-postgres-python-test
  (:require [hara.lang :as l]
            [xt.db.helpers.test-fixtures :as fixtures]
            [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.core :as pg]
             [postgres.sample.scratch-v1 :as scratch]]})

^{:seedgen/root {:all true}}
(l/script- :python
  {:runtime :basic
   :require [[xt.db.node.schema-query :as schema-query]
             [xt.db.node.event-type :as event-type]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-util :as ut]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.node.state :as state]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [python.lib.driver-postgres :as py-pg]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.runtime.sql/sql-pull-sync :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "pulls decoded rows from the live scratch postgres runtime"

  (pg/t:select scratch/Entry)
  => (contains-in
      [{:tags ["guide" "sql"],
        :name "alpha",
        :time-updated nil,
        :time-created nil,
        :id string?}
       {:tags ["guide"],
        :name "beta",
        :time-updated nil,
        :time-created nil,
        :id string?}])

  (!.py
    (var conn (py-pg/wrap-connection
               (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var db-opts (ut/postgres-opts (@! fixtures/+lookup+)))
    (var state (state/base-state
                {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "views" {}}))
    (xt/x:set-key state "::" event-type/STATE_TAG)
    (xt/x:set-key state "schema" (@! fixtures/+schema+))
    (xt/x:set-key state "views" {})
    (xt/x:set-key state "lookup" (@! fixtures/+lookup+))
    (xt/x:set-key state "queries" {})
    (xt/x:set-key state "watch" {})
    (xt/x:set-key state "view_watch" {})
    (xt/x:set-key state "pending" {})
    (xt/x:set-key state "remote" {})
    (xt/x:set-key state "db" nil)
    (var [ok prepared]
         (schema-query/prepare-query
          state
          {:table "Entry",
           :select-entry
           {"input"
            [{"symbol" "i_name",
              "type" "text"}],
            "view"
            {"query"
             {"name" "{{i_name}}",
              "__deleted__" false}}},
           :select-args ["alpha"],
           :return-entry
           {"input"
            [{"symbol" "i_entry_id",
              "type" "text"}],
            "view"
            {"query" ["name" "tags"]}}}
          {"args" []}))
    (var out
         (impl-sql/sql-pull-sync
          conn
          (@! fixtures/+schema+)
          (xt/x:get-key prepared "plan")
          db-opts))
    (sql/disconnect conn)
    {"ok" ok
     "value" out})
  => {"ok" true
      "value" [{"name" "alpha"
                "tags" ["guide" "sql"]}]})

^{:refer xt.db.runtime.sql/sql-pull-sync.string :added "4.1"}
(fact "rejects string pull query results"
  
  (!.py
    (impl-sql/sql-pull-sync
     (sql/connection-create
      {}
      {"query_sync" (fn [_conn _input]
                      (return "[{\"id\":\"ENTRY-0\"}]"))})
     (@! fixtures/+schema+)
     ["Entry" ["id"]]
     (ut/postgres-opts (@! fixtures/+lookup+))))
  => (throws))

^{:refer xt.db.runtime.sql/sql-delete-sync :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "deletes live scratch rows through query-sync"

  (!.py
    (var conn (py-pg/wrap-connection
               (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var db-opts (ut/postgres-opts (@! fixtures/+lookup+)))
    (var state (state/base-state
                {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "views" {}}))
    (xt/x:set-key state "::" event-type/STATE_TAG)
    (xt/x:set-key state "schema" (@! fixtures/+schema+))
    (xt/x:set-key state "views" {})
    (xt/x:set-key state "lookup" (@! fixtures/+lookup+))
    (xt/x:set-key state "queries" {})
    (xt/x:set-key state "watch" {})
    (xt/x:set-key state "view_watch" {})
    (xt/x:set-key state "pending" {})
    (xt/x:set-key state "remote" {})
    (xt/x:set-key state "db" nil)
    (var alpha-id
         (sql/query-sync
          conn
          "SELECT \"id\" FROM \"scratch\".\"Entry\" WHERE \"name\" = 'alpha';"))
    (impl-sql/sql-delete-sync
     conn
     (@! fixtures/+schema+)
     "Entry"
     [alpha-id]
     db-opts)
    (var [ok prepared]
         (schema-query/prepare-query
          state
          (@! fixtures/+model-query+)
          {"args" []}))
    (var out
         (impl-sql/sql-pull-sync
          conn
          (@! fixtures/+schema+)
          (xt/x:get-key prepared "plan")
          db-opts))
    (sql/disconnect conn)
    {"ok" ok
     "count" (xt/x:len out)
     "name" (xtd/get-in out [0 "name"])})
  => {"ok" true
      "count" 1
      "name" "beta"})

^{:refer xt.db.runtime.sql/sql-clear :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "treats clear as a live no-op success"

  (!.py
    (var conn (py-pg/wrap-connection
               (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var cleared (impl-sql/sql-clear conn))
    (var count
         (sql/query-sync
          conn
          "SELECT COUNT(*)::int FROM \"scratch\".\"Entry\";"))
    (sql/disconnect conn)
    [cleared count])
  => [true 2])
