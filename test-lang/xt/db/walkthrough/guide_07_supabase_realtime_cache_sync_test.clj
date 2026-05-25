(ns xt.db.walkthrough.guide-07-supabase-realtime-cache-sync-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.db.helpers.supabase-pull-live-test :as live]))

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
     :require [[postgres.sample.scratch-v1 :as scratch]]}))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-websocket :as js-ws]
             [xt.db.runtime :as xdb]
             [xt.db.runtime.supabase-realtime :as realtime]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
  {:setup [(l/rt:restart)
           (do (live/init-live-postgres-runtime!)
               (l/rt:setup (live/pg-rt) live/+postgres-module+)
               (live/ensure-public-entry-table!)
               (live/enable-public-entry-realtime!)
               (live/reload-postgrest!)
               (live/refresh-live-supabase-config!)
               (live/cleanup-public-entry! live/+live-realtime-entry-name+)
               true)]
   :teardown [(do (live/cleanup-public-entry! live/+live-realtime-entry-name+)
                  (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                  (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                  (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                  true)
              (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-07-supabase-realtime-cache-sync/STEP.00-sync-live-realtime-into-cache
  :added "4.1"
  :setup [(live/cleanup-public-entry! live/+live-realtime-entry-name+)]}
(fact "step 00: sync live supabase realtime postgres_changes into a cache db"

  (do
    (future
      (Thread/sleep 4000)
      (live/setup-public-entry!
       live/+live-realtime-entry-name+
       live/+live-realtime-entry-tags+))
    (notify/wait-on [:js 15000]
      (var schema-name (@! live/+public-schema+))
      (var table-name (@! live/+scratch-entry-table+))
      (var cache
          (xtd/obj-assign
           (xdb/db-create {"::" "db.cache"}
                          (@! fixtures/+schema+)
                          (@! fixtures/+lookup+)
                          nil)
            {"schema" (@! fixtures/+schema+)}))
      (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
      (var client-config (xt/x:obj-clone (. instance ["client"])))
      (var statuses [])
      (var request-out nil)
      (xt/x:set-key client-config "transport" (js-ws/driver {}))
      (xt/x:set-key client-config "schema_name" schema-name)
      (xt/x:set-key client-config "table_name" table-name)
      (promise/x:promise-then
       (realtime/subscribe
        {"client" client-config}
        cache
        {"schema_name" schema-name
         "table_name" table-name
         "on_status"
         (fn [status frame]
           (xt/x:arr-push statuses status))
         "on_request"
         (fn [request payload frame]
           (when (and (xt/x:not-nil? request)
                      (== (@! live/+live-realtime-entry-name+)
                          (xtd/get-in request ["db/sync" table-name 0 "name"])))
             (:= request-out request)))})
       (fn [sub]
         (var topic
              (realtime/resolve-topic
               {"client" client-config}
               {"schema_name" schema-name
                "table_name" table-name}))
         (var poll-id
              (setInterval
               (fn []
                 (when (xt/x:not-nil? request-out)
                   (clearInterval poll-id)
                   (var cached-row
                        (xt/x:get-idx
                         (xdb/db-pull-sync
                          cache
                          (@! fixtures/+schema+)
                          (@! live/+live-realtime-entry-query+))
                         0))
                   (repl/notify
                    {"status" (xt/x:first statuses)
                     "topic" topic
                     "request-name" (xtd/get-in request-out ["db/sync" table-name 0 "name"])
                     "request-tags" (xtd/get-in request-out ["db/sync" table-name 0 "tags"])
                     "cached-row" cached-row})))
               100))
         (return sub))))
  )
  => {"status" "SUBSCRIBED"
      "topic" "realtime:public:Entry"
      "request-name" "copilot_supabase_realtime_live"
      "request-tags" ["copilot" "supabase" "realtime"]
      "cached-row" {"name" "copilot_supabase_realtime_live"
                    "tags" ["copilot" "supabase" "realtime"]}})
