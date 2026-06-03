(ns xt.db.system.client-sql-postgres-test
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
   :require [[xt.db.system.client-sql :as client]
             [xt.db.text.sql-util :as ut]
             [xt.protocol.impl.connection-sql :as sql]
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

^{:refer xt.db.system.client-sql/process-event-sync :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "emits insert sql through process-event-sync and roundtrips it through live postgres"

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
           (var db (client/client {"instance" conn}))
           (var db-opts (ut/postgres-opts (@! fixtures/+lookup+)))
           (var insert-sql
                (client/process-event-sync
                 db
                 "input"
                 {"Entry"
                  [{"id" "00000000-0000-0000-0000-0000000000c3"
                    "name" "client-postgres-gamma"
                    "tags" ["guide" "client"]
                    "__deleted__" false}]}
                 (@! fixtures/+schema+)
                 (@! fixtures/+lookup+)
                 db-opts))
           (return
            (-> (sql/query conn "DELETE FROM \"scratch\".\"Entry\" WHERE \"name\" = 'client-postgres-gamma';")
                (spec-promise/x:promise-then
                 (fn [_]
                   (return
                    (-> (sql/query conn insert-sql)
                        (spec-promise/x:promise-then
                         (fn [_]
                           (return
                            (-> (client/pull
                                 db
                                 (@! fixtures/+schema+)
                                 ["Entry"
                                  {"name" "client-postgres-gamma"
                                   "__deleted__" false}
                                  ["name" "tags"]]
                                 db-opts)
                                (spec-promise/x:promise-then
                                 (fn [out]
                                   (return #{conn insert-sql out}))))))))))))))))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn insert-sql out} interim)
           (return
            (-> (sql/ensure-promise (sql/disconnect conn))
                (spec-promise/x:promise-then
                 (fn [_]
                   (repl/notify
                    {"sql" insert-sql
                     "row" (xt/x:first out)})))))))))
  => {"sql" #"INSERT INTO \"Entry\""
      "row" {"name" "client-postgres-gamma"
             "tags" ["guide" "client"]}})

^{:refer xt.db.system.client-sql/process-event-remove :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "emits delete sql through process-event-remove and applies it through live postgres"

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (spec-promise/x:promise-then
         (fn [conn]
           (var db (client/client {"instance" conn}))
           (var db-opts (ut/postgres-opts (@! fixtures/+lookup+)))
           (var insert-sql
                (client/process-event-sync
                 db
                 "input"
                 {"Entry"
                  [{"id" "00000000-0000-0000-0000-0000000000c3"
                    "name" "client-postgres-gamma"
                    "tags" ["guide" "client"]
                    "__deleted__" false}]}
                 (@! fixtures/+schema+)
                 (@! fixtures/+lookup+)
                 db-opts))
           (var remove-sql
                (client/process-event-remove
                 db
                 "input"
                 {"Entry"
                  [{"id" "00000000-0000-0000-0000-0000000000c3"
                    "name" "client-postgres-gamma"
                    "tags" ["guide" "client"]
                    "__deleted__" false}]}
                 (@! fixtures/+schema+)
                 (@! fixtures/+lookup+)
                 db-opts))
           (return
            (-> (sql/query conn "DELETE FROM \"scratch\".\"Entry\" WHERE \"name\" = 'client-postgres-gamma';")
                (spec-promise/x:promise-then
                 (fn [_]
                   (return
                    (-> (sql/query conn insert-sql)
                        (spec-promise/x:promise-then
                         (fn [_]
                           (return
                            (-> (sql/query conn remove-sql)
                                (spec-promise/x:promise-then
                                 (fn [_]
                                   (return #{conn db db-opts remove-sql}))))))))))))))))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn db db-opts remove-sql} interim)
           (return
            (-> (client/pull
                 db
                 (@! fixtures/+schema+)
                 ["Entry"
                  {"name" "client-postgres-gamma"
                   "__deleted__" false}
                  ["name"]]
                 db-opts)
                (spec-promise/x:promise-then
                 (fn [out]
                   (return #{conn remove-sql out})))))))
        (spec-promise/x:promise-then
         (fn [interim]
           (var #{conn remove-sql out} interim)
           (return
            (-> (sql/ensure-promise (sql/disconnect conn))
                (spec-promise/x:promise-then
                 (fn [_]
                   (repl/notify
                    [remove-sql (xt/x:len out)])))))))))
  => [#"DELETE FROM \"Entry\" WHERE \"id\" = '00000000-0000-0000-0000-0000000000c3';"
      0])
