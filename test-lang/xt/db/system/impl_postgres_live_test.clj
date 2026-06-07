(ns xt.db.system.impl-postgres-live-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.event-host-util :as live]))

(l/script- :postgres
           {:runtime :jdbc.client
            :require [[postgres.sample.scratch-v0 :as scratch]]})

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.lib.driver-postgres :as js-pg]
             [xt.db.system.impl-postgres :as impl]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.helpers.test-fixtures :as fixtures]
             [scaffold.supabase.event-host-util :as live]]})

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

^{:refer xt.db.system.impl-postgres/pull-async :added "4.1"}
(fact "pull-async reads scratch Entry rows through the local supabase postgres instance"

  (do
    (live/setup-scratch-entry! live/+live-entry-name+ live/+live-entry-tags+)
    (try
      (notify/wait-on [:js 10000]
        (promise/x:promise-then
         (impl/client-postgres-init
          (impl/client-postgres
           (@! fixtures/+schema+)
           (@! fixtures/+lookup+)
           {}
           {"host" (@! live/+postgres-host+)
            "port" (@! live/+postgres-port+)
            "user" "postgres"
            "password" "postgres"
            "database" (@! live/+postgres-database+)
            "schema_name" (@! live/+scratch-schema+)})
          (js-pg/driver))
         (fn [client]
           (promise/x:promise-then
            (impl/pull-async
             client
             ["Entry"
              {"name" (@! live/+live-entry-name+)}
              ["name" "tags"]])
            (fn [rows]
              (repl/notify
               {"name" (xtd/get-in rows [0 "name"])
                "tag" (xtd/get-in rows [0 "tags" 0])}))))))
      (finally
        (live/cleanup-scratch-entry! live/+live-entry-name+))))
  => {"name" "copilot_supabase_pull_live"
      "tag" "copilot"})

^{:refer xt.db.system.impl-postgres/rpc-call-async :added "4.1"}
(fact "rpc-call-async calls scratch functions through the local supabase postgres instance"

  (notify/wait-on [:js 10000]
    (promise/x:promise-then
     (impl/client-postgres-init
      (impl/client-postgres
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       {}
       {"host" (@! live/+postgres-host+)
        "port" (@! live/+postgres-port+)
        "user" "postgres"
        "password" "postgres"
        "database" (@! live/+postgres-database+)
        "schema_name" (@! live/+scratch-schema+)})
      (js-pg/driver))
     (fn [client]
       (promise/x:promise-then
        (impl/rpc-call-async client "addf" [1 2])
        (fn [out]
          (repl/notify out))))))
  => 3)
