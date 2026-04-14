(ns js.cell.e2e.common-test
  (:require [clojure.string :as str]
            [js.cell.e2e.common :as common]
            [js.cell.runtime.emit :as emit]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-runtime :as rt :with [defvar.js]]
             [xt.lang.event-view :as event-view]
             [js.cell.kernel :as cl]
             [js.cell.e2e.common :as common]
             [js.cell.runtime.link :as runtime-link]
             [js.cell.service :as service]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(def +node-script+
  (common/node-remote-script))

(def +remote-runtime-init-script+
  (emit/emit-worker-script
   '[(js.cell.e2e.common/remote-runtime-init)]
   :flat))

(defmacro with-node-remote
  [script & body]
  (template/$
    (notify/wait-on :js
      (var remote-cell
           (cl/make-cell
            (runtime-link/make-node-link ~script {})))
      (. (. remote-cell ["init"])
         (then (fn []
                 (do ~@body)))))))

(defmacro with-node-remote-sqlite
  [script & body]
  (template/$
    (notify/wait-on :js
      (var remote-cell
           (cl/make-cell
            (runtime-link/make-node-link ~script {})))
      (. (. remote-cell ["init"])
         (then
          (fn []
            (common/connect-sqlite
              {:success (fn [sqlite-conn]
                          (do ~@body))
               :error (fn [err]
                       (repl/notify {"error" err}))})))))))

^{:refer js.cell.e2e.common/node-remote-script :added "4.1"}
(fact "emits a Node worker bootstrap for the e2e remote cache"
  ^:hidden
  (str/includes? +node-script+ "worker_threads")
  => true
  (str/includes? +node-script+ "__E2E_REMOTE_DB")
  => true
  (str/includes? +node-script+ "@e2e/query")
  => true)

^{:refer js.cell.e2e.common/remote-worker-setup-eval :added "4.1"}
(fact "installs the e2e worker actions through eval"
  ^:hidden
  (with-node-remote (emit/node-script)
    (. (. (cl/call remote-cell
                   {:op "eval"
                    :id "setup-1"
                    :body ~(common/remote-worker-setup-eval)})
          (then (fn [status]
                  (. (common/call-remote-query remote-cell "open")
                     (then (fn [rows]
                             (repl/notify {"status" status
                                           "rows" rows})))))))
       (catch (fn [err]
                (repl/notify {"error" err})))))
  => (contains-in
      {"status" "ready"
       "rows" [{"id" "ord-1"
                "status" "open"}]}))

^{:refer js.cell.e2e.common/shared-desc :added "4.1"}
(fact "returns the shared schema and view descriptor"
  ^:hidden
  (!.js
   (common/shared-desc))
  => {"schema" {"Order"
                {"id" {"ident" "id" "type" "text" "order" 0}
                 "status" {"ident" "status" "type" "text" "order" 1}}}
      "views" {"Order"
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
                          "query" ["id" "status"]}}}}}})

^{:refer js.cell.e2e.common/query-spec :added "4.1"}
(fact "returns the canonical Order query spec"
  ^:hidden
  (!.js
   (common/query-spec))
  => {"table" "Order"
      "select_method" "by_status"
      "return_method" "summary"})

^{:refer js.cell.e2e.common/sort-strings :added "4.1"}
(fact "sorts strings without mutating the input value"
  ^:hidden
  (!.js
   (var input ["beta" "alpha" "gamma"])
   {"input" input
    "sorted" (common/sort-strings input)})
  => {"input" ["beta" "alpha" "gamma"]
      "sorted" ["alpha" "beta" "gamma"]})

^{:refer js.cell.e2e.common/sort-orders :added "4.1"}
(fact "sorts order rows by id"
  ^:hidden
  (!.js
   (common/sort-orders
    [{"id" "ord-2" "status" "closed"}
     {"id" "ord-1" "status" "open"}]))
  => [{"id" "ord-1" "status" "open"}
      {"id" "ord-2" "status" "closed"}])

^{:refer js.cell.e2e.common/create-cache-db :added "4.1"}
(fact "creates and seeds the remote cache database"
  ^:hidden
  (!.js
   (var db (common/create-cache-db))
   {"schema" (k/obj-keys (k/get-key db "schema"))
    "views" (k/obj-keys (k/get-key db "views"))
    "open" (common/remote-action-query
            (common/order-query-plan "open"))})
  => {"schema" ["Order"]
      "views" ["Order"]
      "open" [{"id" "ord-1" "status" "open"}]})

^{:refer js.cell.e2e.common/REMOTE_DB :added "4.1"}
(fact "caches the remote db through the defvar getter"
  ^:hidden
  (!.js
   (common/REMOTE_DB-reset nil)
   (var db (common/get-remote-db))
   {"initial" nil
    "cached?" (== db (common/REMOTE_DB))
    "has_schema" (k/obj? (k/get-key db "schema"))})
  => {"initial" nil
      "cached?" true
      "has_schema" true})

^{:refer js.cell.e2e.common/get-remote-db :added "4.1"}
(fact "returns the same cached db across calls"
  ^:hidden
  (!.js
   (common/REMOTE_DB-reset nil)
   (var first (common/get-remote-db))
   (var second (common/get-remote-db))
   [(k/obj? first)
    (== first second)])
  => [true true])

^{:refer js.cell.e2e.common/order-query-plan :added "4.1"}
(fact "prepares the query plan for open orders"
  ^:hidden
  (!.js
   (common/order-query-plan "open"))
  => ["Order"
      {"status" "open"}
      ["id" "status"]])

^{:refer js.cell.e2e.common/remote-action-query :added "4.1"}
(fact "queries the remote cache through the local handler"
  ^:hidden
  (!.js
   (common/REMOTE_DB-reset nil)
   (common/remote-action-query
    (common/order-query-plan "open")))
  => [{"id" "ord-1" "status" "open"}])

^{:refer js.cell.e2e.common/remote-action-sync :added "4.1"}
(fact "applies sync requests to the cached remote db"
  ^:hidden
  (!.js
   (common/REMOTE_DB-reset nil)
   {"result"
    (common/remote-action-sync
     {"db/sync" {"Order" [{"id" "ord-2"
                           "status" "open"}]}})
    "rows"
    (common/remote-action-query
     (common/order-query-plan "open"))})
  => {"result" {"db/sync" {"Order" [{"id" "ord-2"
                                     "status" "open"}]}}
      "rows" [{"id" "ord-1" "status" "open"}
              {"id" "ord-2" "status" "open"}]})

^{:refer js.cell.e2e.common/remote-actions :added "4.1"}
(fact "exposes both remote worker actions"
  ^:hidden
  (!.js
   (var actions (common/remote-actions))
   {"names" (common/sort-strings (k/obj-keys actions))
    "query_args" (k/get-in actions ["@e2e/query" "args"])
    "sync_args" (k/get-in actions ["@e2e/sync" "args"])})
  => {"names" ["@e2e/query" "@e2e/sync"]
      "query_args" ["query_plan"]
      "sync_args" ["sync_request"]})

^{:refer js.cell.e2e.common/remote-runtime-init :added "4.1"}
(fact "boots the remote runtime entrypoint in a Node worker"
  ^:hidden
  (with-node-remote +remote-runtime-init-script+
    (. (. (common/call-remote-query remote-cell "open")
          (then (fn [rows]
                  (. (cl/call remote-cell
                              {:op "call"
                               :action "@worker/ping"
                               :body []})
                     (then (fn [pong]
                             (repl/notify {"rows" rows
                                           "pong" pong})))))))
       (catch (fn [err]
                (repl/notify {"error" err})))))
  => (contains-in
      {"rows" [{"id" "ord-1"
                "status" "open"}]
       "pong" ["pong" integer?]}))

^{:refer js.cell.e2e.common/connect-sqlite :added "4.1"}
(fact "opens a sqlite-wasm connection with the dbsql contract"
  ^:hidden
  (notify/wait-on :js
    (common/connect-sqlite
     {:success (fn [conn]
                 (repl/notify
                  (common/sort-strings
                   (k/obj-keys conn))))
      :error (fn [err]
               (repl/notify {"error" err}))}))
  => (contains ["::disconnect" "::query" "::query_sync"]))

^{:refer js.cell.e2e.common/sqlite-exec :added "4.1"}
(fact "runs a synchronous sqlite query"
  ^:hidden
  (notify/wait-on :js
    (common/connect-sqlite
     {:success (fn [conn]
                 (repl/notify
                  (common/sqlite-result-rows
                   (common/sqlite-exec conn "SELECT 1 AS value;"))))
      :error (fn [err]
               (repl/notify {"error" err}))}))
  => [{"value" 1}])

^{:refer js.cell.e2e.common/sqlite-init :added "4.1"}
(fact "creates the cache table in sqlite"
  ^:hidden
  (notify/wait-on :js
    (common/connect-sqlite
     {:success
      (fn [conn]
        (common/sqlite-init conn)
        (repl/notify
         (common/sqlite-result-rows
          (common/sqlite-exec
           conn
           "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'orders_cache';"))))
      :error
      (fn [err]
        (repl/notify {"error" err}))}))
  => [{"name" "orders_cache"}])

^{:refer js.cell.e2e.common/sqlite-result-rows :added "4.1"}
(fact "normalizes sqlite row payloads into maps"
  ^:hidden
  (!.js
   (common/sqlite-result-rows
    [{"columns" ["id" "status"]
      "values" [["ord-1" "open"]
                ["ord-2" "closed"]]}]))
  => [{"id" "ord-1" "status" "open"}
      {"id" "ord-2" "status" "closed"}])

^{:refer js.cell.e2e.common/sqlite-select-orders :added "4.1"}
(fact "reads cached orders from sqlite in id order"
  ^:hidden
  (notify/wait-on :js
    (common/connect-sqlite
     {:success
      (fn [conn]
        (common/sqlite-init conn)
        (common/sqlite-upsert-orders
         conn
         [{"id" "ord-2" "status" "closed"}
          {"id" "ord-1" "status" "open"}])
        (repl/notify (common/sqlite-select-orders conn)))
      :error
      (fn [err]
        (repl/notify {"error" err}))}))
  => [{"id" "ord-1" "status" "open"}
      {"id" "ord-2" "status" "closed"}])

^{:refer js.cell.e2e.common/sqlite-upsert-orders :added "4.1"}
(fact "upserts and replaces cached sqlite rows"
  ^:hidden
  (notify/wait-on :js
    (common/connect-sqlite
     {:success
      (fn [conn]
        (common/sqlite-init conn)
        (common/sqlite-upsert-orders conn [{"id" "ord-2" "status" "closed"}])
        (repl/notify
         (common/sqlite-upsert-orders
          conn
          [{"id" "ord-1" "status" "open"}
           {"id" "ord-2" "status" "open"}])))
      :error
      (fn [err]
        (repl/notify {"error" err}))}))
  => [{"id" "ord-1" "status" "open"}
      {"id" "ord-2" "status" "open"}])

^{:refer js.cell.e2e.common/create-service-registry :added "4.1"}
(fact "registers the remote and sqlite services under stable names"
  ^:hidden
  (!.js
   (var registry
        (common/create-service-registry
         {"id" "remote"}
         {"id" "sqlite"}))
   {"names" (common/sort-strings
             (k/obj-keys (service/get-dbs registry)))
    "remote" (k/get-in (service/get-db registry "remote-cache")
                       ["cell" "id"])
    "sqlite" (k/get-in (service/get-db registry "proxy-sqlite")
                       ["conn" "id"])})
  => {"names" ["proxy-sqlite" "remote-cache"]
      "remote" "remote"
      "sqlite" "sqlite"})

^{:refer js.cell.e2e.common/call-remote-query :added "4.1"}
(fact "queries the remote worker through the public helper"
  ^:hidden
  (with-node-remote +node-script+
    (. (common/call-remote-query remote-cell "open")
       (then (repl/>notify))))
  => [{"id" "ord-1" "status" "open"}])

^{:refer js.cell.e2e.common/call-remote-sync :added "4.1"}
(fact "syncs rows through the public remote helper"
  ^:hidden
  (with-node-remote +node-script+
    (. (. (common/call-remote-sync
           remote-cell
           {"db/sync" {"Order" [{"id" "ord-2"
                                 "status" "open"}]}})
          (then (fn [result]
                  (. (common/call-remote-query remote-cell "open")
                     (then (fn [rows]
                             (repl/notify {"result" result
                                           "rows" rows})))))))
       (catch (fn [err]
                (repl/notify {"error" err})))))
  => {"result" {"db/sync" {"Order" [{"id" "ord-2"
                                     "status" "open"}]}}
      "rows" [{"id" "ord-1" "status" "open"}
              {"id" "ord-2" "status" "open"}]})

^{:refer js.cell.e2e.common/make-proxy-model :added "4.1"}
(fact "builds the proxy model contract for query and sync views"
  ^:hidden
  (!.js
   (var model
        (common/make-proxy-model
         (common/create-service-registry
          {"id" "remote"}
          {"id" "sqlite"})))
   {"views" (common/sort-strings (k/obj-keys model))
    "deps" (k/get-in model ["by_status" "deps"])
    "sync_init" (k/get-in model ["sync_status" "defaultInit"])
    "query_kind" (k/get-in model ["by_status" "options" "context" "kind"])
    "sync_kind" (k/get-in model ["sync_status" "options" "context" "kind"])})
  => {"views" ["by_status" "sync_status"]
      "deps" ["sync_status"]
      "sync_init" {"disabled" true}
      "query_kind" "remote-query"
      "sync_kind" "remote-sync"})

^{:refer js.cell.e2e.common/summarize-model :added "4.1"}
(fact "summarizes model view names and dependencies"
  ^:hidden
  (!.js
   (common/summarize-model
    (common/make-proxy-model
     (common/create-service-registry
      {"id" "remote"}
      {"id" "sqlite"}))))
  => {"views" ["by_status" "sync_status"]
      "deps" {"orders" {"sync_status" {"by_status" true}}}})

^{:refer js.cell.e2e.common/summarize-view :added "4.1"}
(fact "summarizes the important public view state"
  ^:hidden
  (!.js
   (common/summarize-view
    {"options" {"context" {"kind" "remote-query"}}
     "pipeline" {"main" {"handler" (fn [] (return nil))}
                 "sync" {"handler" (fn [] (return nil))}}
     "output" {"current" [{"id" "ord-1"}]
               "updated" 10}}))
  => {"kind" "remote-query"
      "has_main" true
      "has_remote" false
      "has_sync" true
      "current" [{"id" "ord-1"}]
      "updated" 10})

^{:refer js.cell.e2e.common/boot-proxy-cell :added "4.1"}
(fact "boots the proxy cell with the orders model attached"
  ^:hidden
  (with-node-remote-sqlite +node-script+
    (common/sqlite-init sqlite-conn)
    (. (common/boot-proxy-cell
        (common/create-service-registry remote-cell sqlite-conn))
       (then (fn [proxy-cell]
               (repl/notify
                (common/proxy-public-state proxy-cell))))))
  => (contains-in
      {"list_models" ["orders"]
       "list_views" ["by_status" "sync_status"]
       "model" {"views" ["by_status" "sync_status"]}}))

^{:refer js.cell.e2e.common/run-sync-view :added "4.1"}
(fact "runs the sync pipeline and refreshes dependent views"
  ^:hidden
  (with-node-remote-sqlite +node-script+
    (common/sqlite-init sqlite-conn)
    (. (. (common/boot-proxy-cell
           (common/create-service-registry remote-cell sqlite-conn))
          (then (fn [proxy-cell]
                  (. (common/run-sync-view
                      proxy-cell
                      "orders"
                      "sync_status"
                      [{"id" "ord-2" "status" "open"}])
                     (then (fn [acc]
                             (repl/notify
                              {"sync" (k/get-key acc "sync")
                               "sqlite" (common/sqlite-select-orders sqlite-conn)})))))))
       (catch (fn [err]
                (repl/notify {"error" err})))))
  => (contains-in
      {"sync" [true {"rows" [{"id" "ord-1" "status" "open"}
                             {"id" "ord-2" "status" "open"}]}]
       "sqlite" [{"id" "ord-1" "status" "open"}
                 {"id" "ord-2" "status" "open"}]}))

^{:refer js.cell.e2e.common/proxy-public-state :added "4.1"}
(fact "reports the proxy cell public model and view state"
  ^:hidden
  (with-node-remote-sqlite +node-script+
    (common/sqlite-init sqlite-conn)
    (. (common/boot-proxy-cell
        (common/create-service-registry remote-cell sqlite-conn))
       (then (fn [proxy-cell]
               (repl/notify
                (common/proxy-public-state proxy-cell))))))
  => (contains-in
      {"list_models" ["orders"]
       "views"
       {"by_status" {"kind" "remote-query"
                     "has_main" true
                     "has_remote" false
                     "has_sync" false}
        "sync_status" {"kind" "remote-sync"
                       "has_main" false
                       "has_remote" false
                       "has_sync" true}}}))

^{:refer js.cell.e2e.common/build-scenario-result :added "4.1"}
(fact "assembles a full scenario snapshot from live cell state"
  ^:hidden
  (with-node-remote-sqlite +node-script+
    (common/sqlite-init sqlite-conn)
    (. (. (common/boot-proxy-cell
           (common/create-service-registry remote-cell sqlite-conn))
          (then (fn [proxy-cell]
                  (common/sqlite-upsert-orders
                   sqlite-conn
                   [{"id" "ord-1" "status" "open"}])
                  (repl/notify
                   (common/build-scenario-result
                    [{"id" "ord-1" "status" "open"}]
                    proxy-cell
                    {"by_status" []}
                    {"by_status" {"current" []}}
                    {"sync" [true]}
                    [{"id" "ord-1" "status" "open"}]
                    sqlite-conn)))))
       (catch (fn [err]
                (repl/notify {"error" err})))))
  => (contains-in
      {"remote_seed" [{"id" "ord-1" "status" "open"}]
       "proxy" {"list_models" ["orders"]}
       "sqlite_orders" [{"id" "ord-1" "status" "open"}]
       "remote_after_sync" [{"id" "ord-1" "status" "open"}]}))

^{:refer js.cell.e2e.common/run-scenario :added "4.1"}
(fact "runs the full dual-cell cache and sqlite scenario"
  ^:hidden
  (with-node-remote-sqlite +node-script+
    (. (common/run-scenario remote-cell sqlite-conn)
       (then (repl/>notify))))
  => (contains-in
      {"remote_seed" [{"id" "ord-1" "status" "open"}]
       "sqlite_orders" [{"id" "ord-1" "status" "open"}
                        {"id" "ord-2" "status" "open"}]
       "remote_after_sync" [{"id" "ord-1" "status" "open"}
                            {"id" "ord-2" "status" "open"}]}))
