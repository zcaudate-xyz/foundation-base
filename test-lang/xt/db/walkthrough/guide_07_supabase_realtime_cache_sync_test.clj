(ns xt.db.walkthrough.guide-07-supabase-realtime-cache-sync-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [scaffold.supabase.event-host-util :as live]))

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
             [xt.db.system :as xdb]
             [xt.db.system.event-supabase :as realtime]
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
               (live/reload-postgrest! live/+public-schema+)
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
    (notify/wait-on [:js 5000]
      (var cache
          (xtd/obj-assign
           (xdb/db-create {"::" "db.cache"}
                          (@! fixtures/+schema+)
                          (@! fixtures/+lookup+)
                          nil)
           {"schema" (@! fixtures/+schema+)}))
      (var schema-name (@! live/+public-schema+))
      (var table-name (@! live/+scratch-entry-table+))
      (var payload
          {"eventType" "INSERT"
           "schema" schema-name
           "table" table-name
           "new" {"id" "00000000-0000-0000-0000-0000000000f7"
                  "name" (@! live/+live-realtime-entry-name+)
                  "tags" (@! live/+live-realtime-entry-tags+)}})
      (realtime/apply-postgres-change
       cache
       payload
       {"schema_name" schema-name
       "table_name" table-name}
       {})
      (var cached-row
          (xt/x:get-idx
           (xdb/db-pull-sync
            cache
            (@! fixtures/+schema+)
            (@! live/+live-realtime-entry-query+))
           0))
      (repl/notify
       {"status" "SUBSCRIBED"
       "topic" (realtime/resolve-topic
                {"client" {"schema_name" schema-name
                           "table_name" table-name}}
                {"schema_name" schema-name
                 "table_name" table-name})
       "request_name" (xt/x:get-key (. payload ["new"]) "name")
       "request_tags" (xt/x:get-key (. payload ["new"]) "tags")
       "cached_row" cached-row}))
    )
  => {"status" "SUBSCRIBED"
      "topic" "realtime:public:Entry"
      "request_name" "copilot_supabase_realtime_live"
      "request_tags" ["copilot" "supabase" "realtime"]
      "cached_row" {"name" "copilot_supabase_realtime_live"
                    "tags" ["copilot" "supabase" "realtime"]}})
