(ns js.cell.e2e.t001-node-dual-cell-cache-and-sql-test
  (:require [js.cell.e2e.common :as common]
            [js.cell.runtime.emit :as emit]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [js.cell.kernel :as cl]
             [js.cell.e2e.common :as common]
             [js.cell.runtime.link :as runtime-link]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defmacro node-dual-cell-scenario
  []
  )


(comment

  #_(. (. (. remote-cell ["init"])
            (then
             (fn []
               (common/connect-sqlite
                {:success
                 (fn [sqlite-conn]
                   (. (. (common/run-scenario remote-cell sqlite-conn)
                         (then (fn [result]
                                 (repl/notify result))))
                      (catch (fn [err]
                               (repl/notify {"error" err})))))
                 :error
                 (fn [err]
                   (repl/notify {"error" err}))}))))
         (catch (fn [err]
                  (repl/notify {"error" err})))))

^{:refer js.cell.e2e.common/run-scenario :added "4.1"}
(fact "runs a Node dual-cell cache/sqlite-wasm scenario"
  ^:hidden

  (notify/wait-on :js
    (var remote-cell
         (cl/make-cell
          (runtime-link/make-node-link (@! (common/node-remote-script)) {})))
    )
  
  => (contains-in
      {"remote_seed" [{"id" "ord-1"
                       "status" "open"}]
       "proxy"
       {"list_models" ["orders"]
        "list_views" ["by_status" "sync_status"]
        "model" {"views" ["by_status" "sync_status"]
                 "deps" {"orders" {"sync_status" {"by_status" true}}}}
        "views"
        {"by_status" {"kind" "remote-query"
                      "has_main" true
                      "has_remote" false
                      "has_sync" false
                      "current" [{"id" "ord-1"
                                  "status" "open"}
                                 {"id" "ord-2"
                                  "status" "open"}]
                      "updated" integer?}
         "sync_status" {"kind" "remote-sync"
                        "has_main" false
                        "has_remote" false
                        "has_sync" true
                        "current" {"result" {"db/sync" {"Order" [{"id" "ord-2"
                                                                  "status" "open"}]}}
                                   "update" {"type" "sync"
                                             "body" {"db/sync" {"Order" ["ord-2"]}}}
                                   "rows" [{"id" "ord-1"
                                            "status" "open"}
                                           {"id" "ord-2"
                                            "status" "open"}]}
                        "updated" integer?}}
        "initial_vals" {"by_status" [{"id" "ord-1"
                                      "status" "open"}]
                        "sync_status" nil}
        "initial_outputs" {"by_status" {"current" [{"id" "ord-1"
                                                    "status" "open"}]
                                        "updated" integer?}
                           "sync_status" {"current" nil}}
        "sync_run" {"path" ["orders" "sync_status"]
                    "sync" [true {"result" {"db/sync" {"Order" [{"id" "ord-2"
                                                                  "status" "open"}]}}
                                  "update" {"type" "sync"
                                            "body" {"db/sync" {"Order" ["ord-2"]}}}
                                  "rows" [{"id" "ord-1"
                                           "status" "open"}
                                          {"id" "ord-2"
                                           "status" "open"}]}]}
        "final_vals" {"by_status" [{"id" "ord-1"
                                    "status" "open"}
                                   {"id" "ord-2"
                                    "status" "open"}]
                      "sync_status" {"result" {"db/sync" {"Order" [{"id" "ord-2"
                                                                    "status" "open"}]}}
                                     "update" {"type" "sync"
                                               "body" {"db/sync" {"Order" ["ord-2"]}}}
                                     "rows" [{"id" "ord-1"
                                              "status" "open"}
                                             {"id" "ord-2"
                                              "status" "open"}]}}
        "final_outputs" {"by_status" {"current" [{"id" "ord-1"
                                                  "status" "open"}
                                                 {"id" "ord-2"
                                                  "status" "open"}]
                                      "updated" integer?}
                         "sync_status" {"current" {"result" {"db/sync" {"Order" [{"id" "ord-2"
                                                                                  "status" "open"}]}}
                                                  "update" {"type" "sync"
                                                            "body" {"db/sync" {"Order" ["ord-2"]}}}
                                                  "rows" [{"id" "ord-1"
                                                           "status" "open"}
                                                          {"id" "ord-2"
                                                           "status" "open"}]}
                                        "updated" integer?}}}
       "sqlite_orders" [{"id" "ord-1"
                         "status" "open"}
                        {"id" "ord-2"
                         "status" "open"}]
       "remote_after_sync" [{"id" "ord-1"
                             "status" "open"}
                            {"id" "ord-2"
                             "status" "open"}]}))
