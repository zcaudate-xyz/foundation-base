(ns js.worker.e2e-db-node-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [js.worker.link]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-string :as str]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.instance :as db-instance]
             [xt.db.node :as db-node]
             [xt.db.node.test-fixtures :as fixtures]
             [xt.db.text.sql-manage :as sql-manage]
             [xt.db.text.sql-util :as sql-util]
             [xt.substrate :as event-node]
             [xt.substrate.base-frame :as event-frame]
             [js.worker.link :as worker-link]
             [xt.substrate.transport-browser :as worker-transport]]})

(def ^:private +nodeworker-script+
  (l/emit-script
   '(do
      (var worker-threads (require "worker_threads"))
      (var parent-port (. worker-threads ["parentPort"]))
      (var worker {"postMessage" (fn [data]
                                   (. parent-port (postMessage data)))
                   "addEventListener" (fn [event listener capture]
                                        (when (== event "message")
                                          (. parent-port (on "message"
                                                             (fn [data]
                                                               (listener {"data" data}))))))})
      (var raw-query
           (fn [db query]
             (var columns [])
             (var values (. db (exec {:sql         query
                                      :rowMode     "array"
                                      :columnNames columns
                                      :returnValue "resultRows"})))
             (when (and (== 1 (xt.lang.spec-base/x:len values))
                        (== 1 (xt.lang.spec-base/x:len (. values [0]))))
               (return (. values [0] [0])))
             (return (:? (xt.lang.spec-base/x:len columns)
                         [{"columns" columns
                           "values" values}]
                         values))))
      (var node (xt.substrate/node-create {"id" "worker-db-node"}))
      (var sqlite3-module (require "@sqlite.org/sqlite-wasm"))
      (var init-module (or (. sqlite3-module ["default"])
                           sqlite3-module))
      (. (init-module)
         (then
          (fn [sqlite3]
            (var raw (new (. sqlite3 ["oo1"] ["DB"]) ":memory:" "c"))
            (var conn (xt.protocol.impl.connection-sql/connection-create
                       raw
                       {"disconnect" (fn [inner]
                                       (. inner (close))
                                       (return true))
                        "query" (fn [inner query]
                                  (return (raw-query inner query)))
                        "query_sync" (fn [inner query]
                                       (return (raw-query inner query)))}))
            (var db-opts (xt.db.text.sql-util/sqlite-opts nil))
            (var db (xt.db.instance/db-create
                     {"::" "db.sql"
                      :instance conn}
                     xt.db.node.test-fixtures/Schema
                     xt.db.node.test-fixtures/Lookup
                     db-opts))
            (xt.db.instance/db-exec-sync
             db
             (xt.lang.common-string/join
              "\n\n"
              (xt.db.text.sql-manage/table-create-all
               xt.db.node.test-fixtures/Schema
               xt.db.node.test-fixtures/Lookup
               db-opts)))
            (xt.db.instance/db-exec-sync
             db
             "INSERT INTO \"Order\" (\"id\", \"status\") VALUES ('ord-1', 'open');")
            (xt.db.node/install
             node
             {"schema" xt.db.node.test-fixtures/Schema
               "lookup" xt.db.node.test-fixtures/Lookup
               "views" xt.db.node.test-fixtures/Views
               "db" db
               "db_opts" db-opts})
            (. (xt.substrate/attach-transport
                node
                "host"
                (xt.substrate.transport-browser/self-endpoint worker))
               (then
                (fn [_]
                  (. worker (postMessage {"signal" "ready"
                                          "worker" "worker-db-node"
                                          "dbtype" (. db ["::"])}))
                  (return node)))))))
      node)
   {:lang :js
    :layout :flat}))

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

(fact "make-node-link hosts xt.db.node on sqlite and answers requests from outside the worker"
  (notify/wait-on [:js 10000]
    (var link (worker-link/make-node-link (@! +nodeworker-script+) {}))
    (var endpoint (worker-transport/worker-endpoint link))
    (var query-id "query-1")
    (var state {"ready" nil})
    ((. endpoint ["start_fn"])
     (fn [frame ctx]
       (cond (== (. frame ["signal"]) "ready")
             (do (xt/x:set-key state "ready" frame)
                  (promise/x:with-delay
                   50
                   (fn []
                     ((. endpoint ["send_fn"])
                      (event-frame/request-frame
                       "room/a"
                       db-node/ACTION_QUERY
                       [{"query" {"table" "Order"
                                   "return_method" "default"
                                   "return_id" "ord-1"}}]
                        {"id" query-id}))))
                  nil)

             (event-frame/response-frame? frame)
             (cond (== (. frame ["status"]) "error")
                   (do ((. endpoint ["stop_fn"]) nil)
                       (repl/notify {"stage" "error"
                                     "frame" frame}))

                   (== (. frame ["reply_to"]) query-id)
                   (do ((. endpoint ["stop_fn"]) nil)
                       (repl/notify {"ready" (xt/x:get-key state "ready")
                                     "query" (. frame ["data"])}))

                   :else
                   nil)

             :else
             nil))))
  => (contains-in
        {"ready" {"signal" "ready"
                  "worker" "worker-db-node"
                  "dbtype" "db.sql"}
        "query" {"value" [{"status" "open"}]
                  "tables" {"Order" true}}}))
