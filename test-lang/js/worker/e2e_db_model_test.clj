(ns js.worker.e2e-db-model-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [js.worker.link]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.lang.common-string :as str]
              [xt.lang.spec-promise :as promise]
              [xt.db.instance :as db-instance]
              [xt.db.node :as db-node]
              [xt.db.node.test-fixtures :as fixtures]
              [xt.db.text.sql-manage :as sql-manage]
              [xt.db.text.sql-util :as sql-util]
              [xt.event.node :as event-node]
              [xt.event.node-frame :as event-frame]
              [js.worker.link :as worker-link]
              [xt.event.node-transport-browser :as worker-transport]]})

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
      (var node (xt.event.node/node-create {"id" "worker-db-node"}))
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
            (xt.db.node/install
             node
             {"schema" xt.db.node.test-fixtures/Schema
              "lookup" xt.db.node.test-fixtures/Lookup
              "views" xt.db.node.test-fixtures/Views
              "db" db
              "db_opts" db-opts})
            (. (xt.event.node/attach-transport
                node
                "host"
                (xt.event.node-transport-browser/self-endpoint worker))
               (then
                (fn [_]
                  (. worker (postMessage {"signal" "ready"
                                          "worker" "worker-db-node"
                                          "dbtype" (. db ["::"])}))
                  (return node)))))))
      node)
    {:lang :js
     :layout :full}))

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:scaffold-imports :js)]
    :teardown [(l/rt:stop)]})

(fact "remote and local db models stay in sync when remote db/sync is mirrored locally"
  (notify/wait-on [:js 10000]
    (var link (worker-link/make-node-link (@! +nodeworker-script+) {}))
    (var endpoint (worker-transport/worker-endpoint link))
    (var host (event-node/node-create {"id" "host-db-model"}))
    (db-node/install host fixtures/InstallOpts)
    (db-node/model-put host "room/a" "orders" fixtures/ModelSpec)
    (var sync-id "sync-remote")
    (var query-one-id "query-ord-1")
    (var query-two-id "query-ord-2")
    (var state {"ready" nil
                "query-one" nil})
    ((. endpoint ["start_fn"])
     (fn [frame ctx]
        (cond (== (. frame ["signal"]) "ready")
               (do
                 (xt/x:set-key state "ready" frame)
                 (. (db-node/sync
                     host
                     "room/a"
                    {"db/sync" {"Order" [{"id" "ord-1"
                                          "status" "open"}
                                         {"id" "ord-2"
                                          "status" "open"}]}})
                   (then
                     (fn [_]
                       (promise/x:with-delay
                        50
                        (fn []
                          ((. endpoint ["send_fn"])
                           (event-frame/request-frame
                            "room/a"
                            db-node/ACTION_SYNC
                            [{"db/sync" {"Order" [{"id" "ord-1"
                                                   "status" "open"}
                                                  {"id" "ord-2"
                                                   "status" "open"}]}}]
                            {"id" sync-id})))))))
                 nil)

              (event-frame/response-frame? frame)
               (cond (== (. frame ["status"]) "error")
                     (do ((. endpoint ["stop_fn"]) nil)
                        (repl/notify {"stage" "error"
                                      "frame" frame
                                      "error-message" (xtd/get-in frame ["error" "message"])
                                      "error-stack" (xtd/get-in frame ["error" "stack"])
                                      "error-text" (xt/x:to-string (. frame ["error"]))}))

                     (== (. frame ["reply_to"]) sync-id)
                     (do
                       (. (db-node/model-refresh host "room/a" "orders")
                          (then
                           (fn [_]
                             ((. endpoint ["send_fn"])
                              (event-frame/request-frame
                                "room/a"
                                db-node/ACTION_QUERY
                                [{"query" {"table" "Order"
                                           "return_method" "default"
                                           "return_id" "ord-1"}}]
                                {"id" query-one-id})))))
                       nil)

                    (== (. frame ["reply_to"]) query-one-id)
                    (do
                      (xt/x:set-key state "query-one" (. frame ["data"]))
                      ((. endpoint ["send_fn"])
                       (event-frame/request-frame
                         "room/a"
                         db-node/ACTION_QUERY
                         [{"query" {"table" "Order"
                                    "return_method" "default"
                                    "return_id" "ord-2"}}]
                         {"id" query-two-id}))
                      nil)

                    (== (. frame ["reply_to"]) query-two-id)
                    (do
                      ((. endpoint ["stop_fn"]) nil)
                      (var local-open (or (db-node/view-val host "room/a" "orders" "open")
                                          []))
                      (repl/notify
                       {"local-status" (xtd/get-in (db-node/view-get host "room/a" "orders" "main")
                                                   ["status"])
                        "local-main-status" (xtd/get-in (db-node/view-val host "room/a" "orders" "main")
                                                        [0 "status"])
                        "local-open-ids" (xt/x:arr-map local-open
                                                       (fn [row]
                                                         (return (. row ["id"]))))
                        "remote-ord-1-status" (xtd/get-in (xt/x:get-key state "query-one")
                                                          ["value" 0 "status"])
                        "remote-ord-2-status" (xtd/get-in (. frame ["data"])
                                                          ["value" 0 "status"])})
                      nil)

                    :else
                    nil)

             :else
             nil)))
    true)
  => {"local-status" "ready"
      "local-main-status" "open"
      "local-open-ids" ["ord-1" "ord-2"]
      "remote-ord-1-status" "open"
      "remote-ord-2-status" "open"})
