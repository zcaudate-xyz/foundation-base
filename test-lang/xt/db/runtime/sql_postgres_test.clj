(ns xt.db.runtime.sql-postgres-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise]
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
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.schema-query :as schema-query]
             [xt.db.node.event-type :as event-type]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-util :as ut]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.substrate.page-model :as page-model]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [js.lib.driver-postgres :as js-pg]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.runtime.sql/CANARY.setup :added "4.1"}
(fact "creating a driver"

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (spec-promise/x:promise-then
         (fn [conn]
           (repl/notify conn)))))
  => (contains-in
      {"::" "sql.connection"
       "_impl" {}
       "_raw" map?}))


^{:refer xt.db.runtime.sql/sql-pull :added "4.1"
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

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (spec-promise/x:promise-then
         (fn [conn]
           (var db-opts (ut/postgres-opts (@! fixtures/+lookup+)))
           (var state (page-model/base-state
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
           (return #{conn ok prepared db-opts})))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn prepared db-opts} interim)
           (return
            (impl-sql/sql-pull
             conn
             (@! fixtures/+schema+)
             (xt/x:get-key prepared "plan")
             db-opts))))
        (spec-promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => [{"name" "alpha"
       "tags" ["guide" "sql"]}])

^{:refer xt.db.runtime.sql/sql-pull.string :added "4.1"}
(fact "rejects string pull query results"

  (notify/wait-on [:js 10000]
    (-> (impl-sql/sql-pull
         (sql/connection-create
          {}
          {"query" (fn [_conn _input]
                     (return "[{\"id\":\"ENTRY-0\"}]"))})
         (@! fixtures/+schema+)
         ["Entry" ["id"]]
         (ut/postgres-opts (@! fixtures/+lookup+)))
        (spec-promise/x:promise-catch
         (fn [_]
           (repl/notify true)))))
  => true)

^{:refer xt.db.runtime.sql/sql-delete :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "deletes live scratch rows through async query semantics"

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (spec-promise/x:promise-then
         (fn [conn]
           (var db-opts (ut/postgres-opts (@! fixtures/+lookup+)))
           (var state (page-model/base-state
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
                 (@! fixtures/+model-query+)
                 {"args" []}))
           (return #{conn ok prepared db-opts})))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn ok prepared db-opts} interim)
           (return
            (-> (sql/query conn "SELECT \"id\" FROM \"scratch\".\"Entry\" WHERE \"name\" = 'alpha';")
                (spec-promise/x:promise-then
                 (fn [alpha-id]
                   (return #{conn ok prepared db-opts alpha-id})))))))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn ok prepared db-opts alpha-id} interim)
           (return
            (-> (impl-sql/sql-delete
                 conn
                 (@! fixtures/+schema+)
                 "Entry"
                 [alpha-id]
                 db-opts)
                (spec-promise/x:promise-then
                 (fn [_]
                   (return #{conn ok prepared db-opts})))))))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn ok prepared db-opts} interim)
           (return
            (-> (impl-sql/sql-pull
                 conn
                 (@! fixtures/+schema+)
                 (xt/x:get-key prepared "plan")
                 db-opts)
                (spec-promise/x:promise-then
                 (fn [out]
                   (return #{conn ok out})))))))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn ok out} interim)
           (return
            (-> (sql/ensure-promise (sql/disconnect conn))
                (spec-promise/x:promise-then
                 (fn [_]
                   (repl/notify
                    {"ok" ok
                     "count" (xt/x:len out)
                     "name" (xtd/get-in out [0 "name"])})))))))))
  => {"ok" true
      "count" 1
      "name" "beta"})

^{:refer xt.db.runtime.sql/sql-clear :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "treats clear as a live no-op success"

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (spec-promise/x:promise-then
         (fn [conn]
           (var cleared (impl-sql/sql-clear conn))
           (return #{conn cleared})))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn cleared} interim)
           (return
            (-> (sql/query conn "SELECT COUNT(*)::int FROM \"scratch\".\"Entry\";")
                (spec-promise/x:promise-then
                 (fn [count]
                   (return #{conn cleared count})))))))        
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn cleared count} interim)
           (return
            (-> (sql/ensure-promise (sql/disconnect conn))
                (spec-promise/x:promise-then
                 (fn [_]
                   (repl/notify [cleared count])))))))))
  => [true 2])
