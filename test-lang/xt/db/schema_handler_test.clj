(ns xt.db.schema-handler-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.db.system :as db-system]
             [xt.db.text.sql-manage :as sql-manage]
             [xt.db.text.sql-util :as sql-util]
             [xt.protocol.impl.connection-sql :as dbsql]
             [js.lib.driver-sqlite :as js-sqlite]
             [js.lib.driver-sqlite-wasm :as js-sqlite-wasm]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.schema-handler-test/install-schema-handler :added "4.1"}
(fact "node with db service and a handler that installs schema"
  
  (notify/wait-on [:js 10000]
    (var n (event-node/node-create
            {"id" "node-schema"
             "services"
             {"db/primary"
              {"driver" (js-sqlite/driver)
               "filename" ":memory:"}}
             "handlers"
             {"db/install-schema"
              {"fn" (fn [space args request worker-node]
                      (var spec   (or (xtd/get-in args [0]) {}))
                      (var schema (xtd/get-in spec ["schema"]))
                      (var lookup (xtd/get-in spec ["lookup"]))
                      (var seed   (xtd/get-in spec ["seed"]))
                      (var svc    (event-node/get-service worker-node "db/primary"))
                      (return
                       (-> (dbsql/connect (xtd/get-in svc ["driver"]) svc)
                           (promise/x:promise-then
                            (fn [conn]
                              (var db (db-system/db-create
                                       {"::" "db.sql"
                                        :instance conn}
                                       schema
                                       lookup
                                       (sql-util/sqlite-opts nil)))
                              (dbsql/query-sync
                               conn
                               (sql-manage/table-create-all
                                schema
                                lookup
                                (sql-util/sqlite-opts nil)))
                              (when (xt/x:not-nil? seed)
                                (db-system/sync-event db ["add" seed]))
                              (xtd/set-in svc ["db-runtime"] db)
                              (return {"installed" true
                                       "tables" (xt/x:obj-keys schema)}))))))
               "meta" {"kind" "request"}}}}))

    (-> (event-node/request n nil "db/install-schema"
                            [{"schema" (@! fixtures/+schema+)
                              "lookup" (@! fixtures/+lookup+)
                              "seed"   (@! fixtures/+entry-seed+)}]
                            {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"installed" true
      "tables" ["Entry"]})


(comment

  (notify/wait-on :js
    (-> (dbsql/connect (js-sqlite/driver) {})
        (promise/x:promise-then
         (fn [conn]
           (repl/notify
            (db-system/db-create
               {"::" "db.sql"
                :instance conn}
               (@! fixtures/+schema+)
               (@! fixtures/+lookup+)
               (sql-util/sqlite-opts nil)))))))


  (notify/wait-on :js
    (-> (dbsql/connect (js-sqlite/driver) {})
        (promise/x:promise-then
         (fn [conn]
           (repl/notify
            (db-system/db-create
               {"::" "db.sql"
                :instance conn}
               (@! fixtures/+schema+)
               (@! fixtures/+lookup+)
               (sql-util/sqlite-opts nil)))))))
  
  
  
  (promise/x:promise-then
   (fn [conn]
     (var db (db-system/db-create
              {"::" "db.sql"
               :instance conn}
              schema
              lookup
              (sql-util/sqlite-opts nil)))
     (dbsql/query-sync
      conn
      (sql-manage/table-create-all
       schema
       lookup
       (sql-util/sqlite-opts nil)))
     (when (xt/x:not-nil? seed)
       (db-system/sync-event db ["add" seed]))
     (xtd/set-in svc ["db-runtime"] db)
     (return {"installed" true
              "tables" (xt/x:obj-keys schema)})))
  )
