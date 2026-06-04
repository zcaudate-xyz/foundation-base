(ns xt.db.system.client-supabase-live-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.event-host-util :as live]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as js-fetch]
             [js.lib.driver-postgres :as js-pg]
             [xt.db.system.client-supabase :as client]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]
             [xt.protocol.impl.connection-sql :as sql]]})

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

^{:refer xt.db.system.client-supabase/pull :added "4.1"}
(fact "pulls scratch-v1 data through a live supabase client after postgres-driver setup"

  (do
    (live/cleanup-scratch-entry! live/+live-entry-name+)
    (try
      (notify/wait-on [:js 10000]
        (-> (sql/connect
             (js-pg/driver)
             {"host" (@! live/+postgres-host+)
              "port" (@! live/+postgres-port+)
              "user" "postgres"
              "password" "postgres"
              "database" (@! live/+postgres-database+)})
            (promise/x:promise-then
             (fn [conn]
               (return
                (-> (sql/query-async conn
                               (xt/x:cat
                                "INSERT INTO \"scratch\".\"Entry\" (name, tags) "
                                "VALUES ('"
                                (@! live/+live-entry-name+)
                                "', '[\"copilot\",\"supabase\",\"pull\"]'::jsonb) "
                                "ON CONFLICT (name) DO UPDATE SET tags = EXCLUDED.tags;"))
                    (promise/x:promise-then
                     (fn [_]
                       (return conn)))))))
            (promise/x:promise-then
             (fn [conn]
               (return
                (-> (sql/ensure-promise (sql/disconnect conn))
                    (promise/x:promise-then
                     (fn [_]
                       (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
                       (var client-config (xt/x:obj-clone (. instance ["client"])))
                       (xt/x:set-key client-config "transport" (js-fetch/client {}))
                       (xt/x:set-key instance "client" client-config)
                       (var db (client/client instance))
                       (return
                        (client/pull db
                                     nil
                                     (@! live/+live-entry-query+)
                                     {})))))))
            (promise/x:promise-then
             (fn [result]
               (repl/notify result)))))
      (finally
        (live/cleanup-scratch-entry! live/+live-entry-name+))))
  => [{"name" "copilot_supabase_pull_live"
       "tags" ["copilot" "supabase" "pull"]}])
