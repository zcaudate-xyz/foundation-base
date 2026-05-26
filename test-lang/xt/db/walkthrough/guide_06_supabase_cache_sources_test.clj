(ns xt.db.walkthrough.guide-06-supabase-cache-sources-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.db.runtime.event-host-util :as live]
            [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch]))

(def +supabase-pg-config+
  {:host live/+postgres-host+
   :port live/+postgres-port+
   :user "postgres"
   :pass "postgres"
   :dbname "postgres"
   :startup {:args [live/+shell+ "-lc" (live/startup-shell-command)]
             :root live/+supabase-cli-root+
             :ignore-errors false}
   :teardown {:args [live/+shell+ "-lc" (live/supabase-shell-command "stop" "--no-backup" "--yes")]
             :root live/+supabase-cli-root+
             :ignore-errors true}})

(def +supabase-pg-rt+
  (l/script- :postgres
    {:runtime :jdbc.client
     :config +supabase-pg-config+
     :require [[postgres.core :as pg]
              [postgres.sample.scratch-v1 :as scratch]]}))

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
             [xt.db.runtime.event-host-util :as live]]})

(fact:global
  {:setup [(l/rt:restart)
          (do (live/init-live-postgres-runtime!)
              (l/rt:setup (live/pg-rt) live/+postgres-module+)
              (live/grant-scratch-schema!)
              (live/reload-postgrest!)
              (live/refresh-live-supabase-config!)
              true)]
  :teardown [(do (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                 (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                 (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                 true)
              (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-06-supabase-cache-sources/STEP.00-source-descriptors :added "4.1"
  :setup [(l/with:rt [(live/pg-rt)]
           (fixtures/seed-entry-rows))
         (Thread/sleep 400)]}
(fact "step 00: keep live supabase primary and cache caching as stable model source bindings"

  (l/with:rt [(live/pg-rt)]
    (pg/t:select scratch/Entry))
  => (contains-in
     [{:tags ["guide" "sql"]
       :name "alpha"
       :time-updated nil
       :time-created nil
       :id string?}
       {:tags ["guide"]
        :name "beta"
       :time-updated nil
       :time-created nil
       :id string?}])

  (live/refresh-live-supabase-config!)

  (notify/wait-on [:js 10000]
    (var primary-config (xt/x:obj-clone (@! live/+live-supabase-config+)))
    (var client-config (xt/x:obj-clone (. primary-config ["client"])))
    (xt/x:set-key client-config "transport" (js-fetch/client {}))
    (xt/x:set-key primary-config "client" client-config)
    (-> (node/create
         {"node_id" "admin-screen"
          "db" {"schema" (@! fixtures/+schema+)
               "lookup" (@! fixtures/+lookup+)
               "sources" {"primary" {"kind" "supabase"
                                     "config" primary-config}
                "caching" {"kind" "cache"
                           "config" {}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"sources"
              {"primary" {"resolver" (@! fixtures/+resolver-model-query+)}
               "caching" {"resolver" (@! fixtures/+resolver-model-query+)}} 
              "views"
              {"list" {"resolver" (@! fixtures/+resolver-model-query+)
                      "source" "caching"}
               "detail" {"resolver" (@! fixtures/+resolver-inline-query+)
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
                    {"summary" (node/summarise node)
                     "cached_first" (xtd/get-in
                                     (node/source-get node "screen/admin" "entries-screen" "caching")
                                     ["data" 0 "name"])
                     "list_name" (xtd/get-in
                                  (node/view-val node "screen/admin" "entries-screen" "list")
                                  [0 "name"])
                     "detail_default" (xtd/get-in
                                       (node/view-get node "screen/admin" "entries-screen" "detail")
                                       ["resolver" "select_args" 0])
                     "detail_name" (xtd/get-in
                                    (node/view-val node "screen/admin" "entries-screen" "detail")
                                    [0 "name"])})))))))))
  => {"summary"
      {"id" "admin-screen"
       "spaces"
       {"screen/admin"
        {"models"
         {"entries-screen"
          {"sources"
           {"primary" {"kind" "supabase"
                      "sync_from" nil
                      "live" true
                      "data_count" 1}
            "caching" {"kind" "cache"
                      "sync_from" "primary"
                      "live" true
                      "data_count" 2}}
           "views"
           {"list" {"source" "caching"
                    "status" "ready"
                    "resolver_keys" ["type" "table" "select_entry" "return_entry"]}
            "detail" {"source" "primary"
                     "status" "ready"
                     "resolver_keys" ["type" "table" "select_entry" "select_args" "return_entry"]}}}}}}}
      "cached_first" "alpha"
      "list_name" "alpha"
      "detail_default" "alpha"
      "detail_name" "alpha"})
