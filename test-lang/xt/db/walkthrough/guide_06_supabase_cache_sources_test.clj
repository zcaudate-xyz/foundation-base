(ns xt.db.walkthrough.guide-06-supabase-cache-sources-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.db.helpers.supabase-pull-live-test :as live]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [js.lib.client-fetch :as js-fetch]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.db.helpers.supabase-pull-live-test :as live]]})

(fact:global
  {:setup [(l/rt:restart)
           (do (live/init-live-postgres-runtime!)
               (l/rt:setup (live/pg-rt) live/+postgres-module+)
               (live/grant-scratch-schema!)
               (live/reload-postgrest!)
               (live/refresh-live-supabase-config!)
               (live/cleanup-scratch-entry! live/+live-entry-name+)
               true)]
   :teardown [(do (live/cleanup-scratch-entry! live/+live-entry-name+)
                  (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                  (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                  (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                  true)
              (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-06-supabase-cache-sources/STEP.00-source-descriptors :added "4.1"}
(fact "step 00: keep live supabase primary and cache caching as stable model source bindings"

  (do
    (live/setup-scratch-entry! "alpha" ["guide" "sql"])
    (live/setup-scratch-entry! "beta" ["guide"])
    (try
      (notify/wait-on [:js 10000]
        (var primary-config (xt/x:obj-clone (@! live/+live-supabase-config+)))
        (var client-config (xt/x:obj-clone (. primary-config ["client"])))
        (xt/x:set-key client-config "transport" (js-fetch/client {}))
        (xt/x:set-key primary-config "client" client-config)
        (-> (node/create
             {"node_id" "admin-screen"
              "db" {"schema" (@! fixtures/+schema+)
                    "lookup" (@! fixtures/+lookup+)
                    "sources"
                    {"primary" {"kind" "supabase"
                                "config" primary-config}
                     "caching" {"kind" "cache"
                                "config" {}}}}
              "spaces"
              {"screen/admin"
               {"models"
                {"entries-screen"
                 {"sources"
                  {"primary" {"query" (@! fixtures/+model-query+)}
                   "caching" {"query" (@! fixtures/+model-query+)}}
                  "views"
                  {"list" {"query" (@! fixtures/+model-query+)
                           "source" "caching"}
                   "detail" {"query" (@! fixtures/+inline-query+)
                             "source" "primary"}}}}}}})
            (promise/x:promise-then
             (fn [node]
               (return
                (-> (node/model-sync node "screen/admin" "entries-screen")
                    (promise/x:promise-then
                     (fn [_]
                       (return
                        (promise/x:promise-all
                         [(node/view-refresh node "screen/admin" "entries-screen" "list")
                          (node/view-refresh node "screen/admin" "entries-screen" "detail")]))))
                    (promise/x:promise-then
                     (fn [[list-refresh detail-refresh]]
                       (repl/notify
                        {"primary-kind" (xtd/get-in
                                         (node/source-get node "screen/admin" "entries-screen" "primary")
                                         ["kind"])
                         "caching-kind" (xtd/get-in
                                         (node/source-get node "screen/admin" "entries-screen" "caching")
                                         ["kind"])
                         "list-source" (xtd/get-in list-refresh ["source"])
                         "detail-source" (xtd/get-in detail-refresh ["source"])
                         "list-table" (xtd/get-in
                                       (node/view-get node "screen/admin" "entries-screen" "list")
                                       ["query" "table"])
                         "detail-query-keys" (xt/x:obj-keys
                                              (. (node/view-get node "screen/admin" "entries-screen" "detail")
                                                 ["query"]))
                         "node-id" (. node ["id"])
                         "caching-sync-from" (xtd/get-in
                                              (node/source-get node "screen/admin" "entries-screen" "caching")
                                              ["sync_from"])
                         "cached-first" (xtd/get-in
                                         (node/source-get node "screen/admin" "entries-screen" "caching")
                                         ["data" 0 "name"])
                         "list-name" (xtd/get-in
                                      (node/view-val node "screen/admin" "entries-screen" "list")
                                      [0 "name"])
                         "detail-default" (xtd/get-in
                                           (node/view-get node "screen/admin" "entries-screen" "detail")
                                           ["query" "select_args" 0])
                         "detail-name" (xtd/get-in
                                        (node/view-val node "screen/admin" "entries-screen" "detail")
                                        [0 "name"])})))))))))
      (finally
        (live/cleanup-scratch-entry! "alpha")
        (live/cleanup-scratch-entry! "beta"))))
  => {"primary-kind" "supabase"
      "caching-kind" "cache"
      "list-source" "caching"
      "detail-source" "primary"
      "list-table" "Entry"
      "detail-query-keys" ["table" "select_entry" "select_args" "return_entry"]
      "node-id" "admin-screen"
      "caching-sync-from" "primary"
      "cached-first" "alpha"
      "list-name" "alpha"
      "detail-default" "alpha"
      "detail-name" "alpha"})
