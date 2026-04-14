(ns js.cell.e2e.common
  (:require [js.cell.kernel.worker-local :as worker-local]
            [js.cell.kernel.worker-state :as worker-state]
            [js.cell.runtime.emit :as emit]
            [std.lib.template :as template]
            [std.lang :as l]
            [xt.db :as xdb]
            [xt.lang.common-spec :as xt]
            [xt.lang.common-data :as xtd]))

(def +schema+
  {"Order"
   {"id" {"ident" "id" "type" "text" "order" 0}
    "status" {"ident" "status" "type" "text" "order" 1}}})

(def +views+
  {"Order"
   {"select"
    {"by_status"
     {"input" [{"symbol" "i_status" "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
              "type" "select"
              "tag" "by_status"
              "access" {"roles" {}}
              "guards" []
              "query" {"status" "{{i_status}}"}}}}
    "return"
    {"summary"
     {"input" [{"symbol" "i_order_id" "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
              "type" "return"
              "tag" "summary"
              "access" {"roles" {}}
              "guards" []
              "query" ["id" "status"]}}}}})

(def +lookup+
  {"Order" {"position" 0}})

(def +seed-sync+
  {"Order" [{"id" "ord-1" "status" "open"}
            {"id" "ord-2" "status" "closed"}]})

(def +sync-entry+
  {"id" "ord-2" "status" "open"})

(l/script :js
  {:require [[js.cell.kernel :as cl]
              [js.cell.kernel.base-util :as util]
              [js.cell.kernel.base-model :as base-model]
              [js.cell.kernel.worker-impl :as worker-impl]
              [js.cell.kernel.worker-local :as worker-local]
             [js.cell.runtime.env-node :as env-node]
             [js.cell.runtime.link :as runtime-link]
              [js.cell.service :as service]
              [js.cell.service.db-query :as db-query]
              [js.cell.service.db-sync :as db-sync]
              [js.lib.driver-sqlite-wasm :as sqlite-wasm]
              [xt.db :as xdb]
              [xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-runtime :as rt :with [defvar.js]]
              [xt.lang.event-view :as event-view]
              [xt.sys.conn-dbsql :as dbsql]]})

(defn node-remote-script
  []
  (l/emit-script
    '(do
      (var worker (js.cell.runtime.env-node/make-node-worker))
      (var existing-db (!:G __E2E_REMOTE_DB))
      (when (xt.lang.common-spec/x:nil? existing-db)
        (var schema {"Order"
                     {"id" {"ident" "id" "type" "text" "order" 0}
                      "status" {"ident" "status" "type" "text" "order" 1}}})
        (var views {"Order"
                    {"select"
                     {"by_status"
                      {"input" [{"symbol" "i_status" "type" "text"}]
                       "return" "jsonb"
                       "view" {"table" "Order"
                               "type" "select"
                               "tag" "by_status"
                               "access" {"roles" {}}
                               "guards" []
                               "query" {"status" "{{i_status}}"}}}}
                     "return"
                     {"summary"
                      {"input" [{"symbol" "i_order_id" "type" "text"}]
                       "return" "jsonb"
                       "view" {"table" "Order"
                               "type" "return"
                               "tag" "summary"
                               "access" {"roles" {}}
                               "guards" []
                               "query" ["id" "status"]}}}}})
        (:= existing-db
            (xt.lang.common-data/obj-assign
             (xt.db/db-create {"::" "db.cache"}
                              schema
                              {"Order" {"position" 0}}
                              nil)
             {"schema" schema
              "views" views}))
        (xt.db/sync-event existing-db
                          ["add"
                           {"Order" [{"id" "ord-1" "status" "open"}
                                     {"id" "ord-2" "status" "closed"}]}])
        (:= (!:G __E2E_REMOTE_DB) existing-db))
      (js.cell.kernel.worker-local/actions-init
       {"@e2e/query"
        {"handler"
         (fn [query-plan]
           (var rows (xt.db/db-pull-sync
                      (!:G __E2E_REMOTE_DB)
                      {"Order"
                       {"id" {"ident" "id" "type" "text" "order" 0}
                        "status" {"ident" "status" "type" "text" "order" 1}}}
                      query-plan))
           (. rows (sort (fn [a b]
                           (cond (< (. a ["id"]) (. b ["id"])) (return -1)
                                 (> (. a ["id"]) (. b ["id"])) (return 1)
                                 :else                         (return 0)))))
           (return rows))
         "is_async" false
         "args" ["query_plan"]}
        "@e2e/sync"
        {"handler"
         (fn [sync-request]
           (var db-sync (xt.lang.common-spec/x:get-key sync-request "db/sync"))
           (when (and (xt.lang.common-spec/x:is-object? db-sync)
                      (xt.lang.common-data/not-empty? db-sync))
             (xt.db/sync-event (!:G __E2E_REMOTE_DB) ["add" db-sync]))
           (return sync-request))
         "is_async" false
         "args" ["sync_request"]}}
       worker)
      (js.cell.kernel.worker-impl/worker-init worker)
      (js.cell.kernel.worker-impl/worker-init-signal worker {:done true}))
   {:lang :js
    :layout :flat}))

(defn remote-worker-setup-eval
  []
  (l/emit-script
   '(do
      (var existing-db (!:G __E2E_REMOTE_DB))
      (when (xt.lang.common-spec/x:nil? existing-db)
        (var schema {"Order"
                     {"id" {"ident" "id" "type" "text" "order" 0}
                      "status" {"ident" "status" "type" "text" "order" 1}}})
        (var views {"Order"
                    {"select"
                     {"by_status"
                      {"input" [{"symbol" "i_status" "type" "text"}]
                       "return" "jsonb"
                       "view" {"table" "Order"
                               "type" "select"
                               "tag" "by_status"
                               "access" {"roles" {}}
                               "guards" []
                               "query" {"status" "{{i_status}}"}}}}
                     "return"
                     {"summary"
                      {"input" [{"symbol" "i_order_id" "type" "text"}]
                       "return" "jsonb"
                       "view" {"table" "Order"
                               "type" "return"
                               "tag" "summary"
                               "access" {"roles" {}}
                               "guards" []
                               "query" ["id" "status"]}}}}})
        (:= existing-db
            (xt.lang.common-data/obj-assign
             (xt.db/db-create {"::" "db.cache"}
                              schema
                              {"Order" {"position" 0}}
                              nil)
             {"schema" schema
              "views" views}))
        (xt.db/sync-event existing-db
                          ["add"
                           {"Order" [{"id" "ord-1" "status" "open"}
                                     {"id" "ord-2" "status" "closed"}]}])
        (:= (!:G __E2E_REMOTE_DB) existing-db))
      (js.cell.kernel.worker-state/set-actions
       (xt.lang.common-data/obj-assign
        (js.cell.kernel.worker-local/actions-baseline)
        {"@e2e/query"
         {"handler"
          (fn [query-plan]
            (var rows (xt.db/db-pull-sync
                       (!:G __E2E_REMOTE_DB)
                       {"Order"
                        {"id" {"ident" "id" "type" "text" "order" 0}
                         "status" {"ident" "status" "type" "text" "order" 1}}}
                       query-plan))
            (. rows (sort (fn [a b]
                            (cond (< (. a ["id"]) (. b ["id"])) (return -1)
                                  (> (. a ["id"]) (. b ["id"])) (return 1)
                                  :else                         (return 0)))))
            (return rows))
          "is_async" false
          "args" ["query_plan"]}
         "@e2e/sync"
         {"handler"
         (fn [sync-request]
           (var db-sync (xt.lang.common-spec/x:get-key sync-request "db/sync"))
           (when (and (xt.lang.common-spec/x:is-object? db-sync)
                      (xt.lang.common-data/not-empty? db-sync))
             (xt.db/sync-event (!:G __E2E_REMOTE_DB) ["add" db-sync]))
           (return sync-request))
         "is_async" false
         "args" ["sync_request"]}}
       (!:G __CELL_WORKER))
       "ready"))
   {:lang :js
     :layout :flat}))

(defn.js shared-desc
  []
  (return {"schema" (@! +schema+)
           "views" (@! +views+)}))

(defn.js query-spec
  []
  (return {"table" "Order"
           "select_method" "by_status"
           "return_method" "summary"}))

(defn.js sort-strings
  [arr]
  (var out (xt/x:arr-clone (or arr [])))
  (. out (sort))
  (return out))

(defn.js sort-orders
  [rows]
  (var out (xt/x:arr-clone (or rows [])))
  (. out (sort (fn [a b]
                 (var aid (. a ["id"]))
                 (var bid (. b ["id"]))
                 (cond (< aid bid) (return -1)
                       (> aid bid) (return 1)
                       :else       (return 0)))))
  (return out))

(defn.js create-cache-db
  []
  (var desc (-/shared-desc))
  (var schema (xt/x:get-key desc "schema"))
  (var db (xtd/obj-assign
           (xdb/db-create {"::" "db.cache"}
                          schema
                          (@! +lookup+)
                          nil)
           desc))
  (xdb/sync-event db ["add" (@! +seed-sync+)])
  (return db))

(defvar.js
  REMOTE_DB
  []
  (return nil))

(defn.js get-remote-db
  []
  (var existing (-/REMOTE_DB))
  (when existing
    (return existing))
  (var db (-/create-cache-db))
  (-/REMOTE_DB-reset db)
  (return db))

(defn.js order-query-plan
  [status]
  (var [ok plan] (db-query/prepare-query
                  (-/shared-desc)
                  (-/query-spec)
                  {"args" [status]}))
  (when (not ok)
    (throw plan))
  (return plan))

(defn.js remote-action-query
  [query-plan]
  (return
   (-/sort-orders
    (xdb/db-pull-sync
     (-/get-remote-db)
     (@! +schema+)
     query-plan))))

(defn.js remote-action-sync
  [sync-request]
  (var db (-/get-remote-db))
  (var db-sync (xt/x:get-key sync-request "db/sync"))
  (when (and (xt/x:is-object? db-sync)
             (xtd/not-empty? db-sync))
    (xdb/sync-event db ["add" db-sync]))
  (return sync-request))

(defn.js remote-actions
  []
  (return
   {"@e2e/query" {"handler" -/remote-action-query
                  "is_async" false
                  "args" ["query_plan"]}
    "@e2e/sync" {"handler" -/remote-action-sync
                 "is_async" false
                 "args" ["sync_request"]}}))

(defn.js remote-runtime-init
  []
  (var worker (env-node/make-node-worker))
  (worker-local/actions-init (-/remote-actions) worker)
  (worker-impl/worker-init worker)
  (worker-impl/worker-init-signal worker {:done true})
  (return worker))

(defn.js connect-sqlite
  [callback]
  (return
   (dbsql/connect {:constructor sqlite-wasm/connect-constructor}
                  callback)))

(defn.js sqlite-exec
  [conn raw]
  (var columns [])
  (var values (. conn (exec {:sql         raw
                             :rowMode     "array"
                             :columnNames columns
                             :returnValue "resultRows"})))
  (return (:? (xt/x:len columns)
              [{"columns" columns
                "values" values}]
              values)))

(defn.js sqlite-init
  [conn]
  (-/sqlite-exec conn "CREATE TABLE IF NOT EXISTS orders_cache (id TEXT PRIMARY KEY, status TEXT NOT NULL);")
  (return conn))

(defn.js sqlite-result-rows
  [result]
  (var entry (xt/x:first (or result [])))
  (when (xt/x:nil? entry)
    (return []))
  (var columns (or (xt/x:get-key entry "columns") []))
  (return
   (xt/x:arr-map
    (or (xt/x:get-key entry "values") [])
     (fn [values]
      (return (xtd/arr-zip columns values))))))

(defn.js sqlite-select-orders
  [conn]
  (return
   (-/sqlite-result-rows
    (-/sqlite-exec conn "SELECT id, status FROM orders_cache ORDER BY id;"))))

(defn.js sqlite-upsert-orders
  [conn rows]
  (xt/for:array [row (or rows [])]
    (var id (. row ["id"]))
    (var status (. row ["status"]))
    (-/sqlite-exec
     conn
     (xt/x:cat "INSERT OR REPLACE INTO orders_cache (id, status) VALUES ('"
               id
               "', '"
               status
               "');")))
  (return (-/sqlite-select-orders conn)))

(defn.js create-service-registry
  [remote-cell sqlite-conn]
  (return
   (service/create-service
    {"remote-cache" (xtd/obj-assign
                      (-/shared-desc)
                      {"cell" remote-cell})
     "proxy-sqlite" {"conn" sqlite-conn}})))

(defn.js link-call
  [link event]
  (var #{worker active id} link)
  (var cid (or (. event ["id"])
               (util/rand-id (xt/x:cat id "-") 3)))
  (:= (. event ["id"]) cid)
  (var input (util/arg-encode event))
  (var p (new Promise
               (fn [resolve reject]
                 (:= (. active [cid])
                     {:resolve (fn [data]
                                 (resolve data))
                      :reject (fn [data]
                                (reject data))
                      :input input
                      :time (xt/x:now-ms)}))))
  (var #{postRequest} worker)
  (if postRequest
    (postRequest input)
    (. worker (postMessage input)))
  (return p))

(defn.js call-remote-query
  [remote-cell status]
  (var link (. remote-cell ["link"]))
  (var event {:op "call"
              :action "@e2e/query"
              :body [(-/order-query-plan status)]})
  (return
   (. (-/link-call link event)
       (then -/sort-orders))))

(defn.js call-remote-sync
  [remote-cell sync-request]
  (var link (. remote-cell ["link"]))
  (return
   (-/link-call link
                {:op "call"
                 :action "@e2e/sync"
                 :body [sync-request]})))

(defn.js make-proxy-model
  [service-registry]
  (var remote-db (service/get-db service-registry "remote-cache"))
  (var sqlite-db (service/get-db service-registry "proxy-sqlite"))
  (var remote-cell (xt/x:get-key remote-db "cell"))
  (var sqlite-conn (xt/x:get-key sqlite-db "conn"))
  (return
   {"by_status"
    {"handler" (fn [link status]
                 (return
                  (. (-/call-remote-query remote-cell status)
                     (then (fn [rows]
                             (-/sqlite-upsert-orders sqlite-conn rows)
                             (return rows))))))
     "defaultArgs" ["open"]
     "defaultOutput" []
     "deps" ["sync_status"]
     "options" {"context" {"kind" "remote-query"}}}
    "sync_status"
    {"pipeline"
     {"sync"
      {"handler"
       (fn [context]
         (var args (or (xt/x:get-key context "args") []))
         (var [ok sync-request] (db-sync/prepare-sync
                                 (-/shared-desc)
                                 {"sync" {"Order" args}}
                                 {}))
         (when (not ok)
           (throw sync-request))
         (return
          (. (-/call-remote-sync remote-cell sync-request)
             (then
              (fn [result]
                (var sync-rows (xtd/get-in result ["db/sync" "Order"]))
                (-/sqlite-upsert-orders sqlite-conn sync-rows)
                (return {"result" result
                         "update" (db-sync/result->update
                                   (-/shared-desc)
                                   result
                                   {"view-id" "sync_status"})
                         "rows" (-/sqlite-select-orders sqlite-conn)}))))))}}
     "defaultArgs" []
     "defaultInit" {"disabled" true}
     "defaultOutput" nil
     "options" {"context" {"kind" "remote-sync"}}}}))

(defn.js summarize-model
  [model]
  (when model
    (var views (or (. model ["views"])
                   model))
    (var deps (or (xt/x:get-key model "deps")
                  (do (var out {"orders" {}})
                      (xt/for:object [[view-id view-spec] views]
                        (xt/for:array [dep-id (or (. view-spec ["deps"]) [])]
                          (xtd/set-in out ["orders" dep-id view-id] true)))
                      out)))
    (return {"views" (-/sort-strings
                      (xtd/obj-keys views))
             "deps" deps})))

(defn.js summarize-view
  [view]
  (when view
    (return {"kind" (xtd/get-in view ["options" "context" "kind"])
             "has_main" (not (xt/x:nil? (xtd/get-in view ["pipeline" "main" "handler"])))
             "has_remote" (not (xt/x:nil? (xtd/get-in view ["pipeline" "remote" "handler"])))
             "has_sync" (not (xt/x:nil? (xtd/get-in view ["pipeline" "sync" "handler"])))
             "current" (event-view/get-current view)
             "updated" (event-view/get-time-updated view)})))

(defn.js boot-proxy-cell
  [service-registry]
  (var proxy-cell (cl/make-cell
                   (runtime-link/make-mock-link {})))
  (return
   (. (. proxy-cell ["init"])
      (then
       (fn []
         (return
          (. (. (cl/add-model "orders"
                              (-/make-proxy-model service-registry)
                              proxy-cell)
                ["init"])
             (then (fn []
                     (return proxy-cell))))))))))

(defn.js run-sync-view
  [cell model-id view-id args]
  (var [path context disabled]
       (base-model/prep-view cell model-id view-id {:args args}))
  (xt/x:set-key (. context ["acc"]) "path" path)
  (return
   (. (event-view/pipeline-run-sync
       context
       true
       base-model/async-fn
       nil
        (fn [x] (return x)))
      (then
       (fn []
         (return
          (. (base-model/refresh-view-dependents-unthrottled
              cell
              model-id
              view-id
              nil)
             (then (fn []
                     (return (. context ["acc"])))))))))))

(defn.js proxy-public-state
  [proxy-cell]
  (return
   {"list_models" (-/sort-strings
                   (cl/list-models proxy-cell))
    "list_views" (-/sort-strings
                  (cl/list-views "orders" proxy-cell))
    "model" (-/summarize-model
             (cl/get-model "orders" proxy-cell))
    "views"
    {"by_status" (-/summarize-view
                  (cl/get-view ["orders" "by_status"] proxy-cell))
     "sync_status" (-/summarize-view
                    (cl/get-view ["orders" "sync_status"] proxy-cell))}
    "final_vals" (cl/model-vals "orders" proxy-cell)
    "final_outputs" (cl/model-outputs "orders" proxy-cell)}))

(defn.js build-scenario-result
  [remote-seed proxy-cell initial-vals initial-outputs sync-run remote-after-sync sqlite-conn]
  (return
   {"remote_seed" remote-seed
    "proxy" (xtd/obj-assign
             (-/proxy-public-state proxy-cell)
             {"initial_vals" initial-vals
              "initial_outputs" initial-outputs
              "sync_run" sync-run})
    "sqlite_orders" (-/sqlite-select-orders sqlite-conn)
    "remote_after_sync" remote-after-sync}))

(defn.js run-scenario
  [remote-cell sqlite-conn]
  (-/sqlite-init sqlite-conn)
  (var service-registry (-/create-service-registry remote-cell sqlite-conn))
  (return
   (. (. (-/call-remote-query remote-cell "open")
         (then
          (fn [remote-seed]
            (return
             (. (-/boot-proxy-cell service-registry)
                (then
                 (fn [proxy-cell]
                   (var initial-vals (cl/model-vals "orders" proxy-cell))
                   (var initial-outputs (cl/model-outputs "orders" proxy-cell))
                   (return
                    (. (-/run-sync-view proxy-cell
                                        "orders"
                                        "sync_status"
                                        [(@! +sync-entry+)])
                       (then
                        (fn [sync-run]
                          (return
                           (. (-/call-remote-query remote-cell "open")
                              (then
                               (fn [remote-after-sync]
                                 (return
                                  (-/build-scenario-result
                                   remote-seed
                                   proxy-cell
                                   initial-vals
                                   initial-outputs
                                   sync-run
                                   remote-after-sync
                                   sqlite-conn)))))))))))))))))
      (catch
       (fn [err]
         (return {"error" err}))))))
