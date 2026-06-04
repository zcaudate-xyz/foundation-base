(ns xt.db.system.impl-supabase-live-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.event-host-util :as live]))

(def +rpc-name+
  "copilot_ping")

(defn ensure-scratch-rpc!
  []
  (live/pg-exec!
   (str "CREATE OR REPLACE FUNCTION \"" live/+scratch-schema+ "\".\"" +rpc-name+ "\"() "
        "RETURNS jsonb LANGUAGE sql STABLE AS $$ "
        "SELECT jsonb_build_object('status', 'ok', 'source', 'supabase') "
        "$$;\n"
        "GRANT EXECUTE ON FUNCTION \"" live/+scratch-schema+ "\".\"" +rpc-name+ "\"() "
        "TO anon, authenticated, service_role;"))
  (live/reload-postgrest!))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as js-fetch]
             [xt.db.system.impl-supabase :as impl]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lib.supabase :as supabase]
             [xt.db.helpers.test-fixtures :as fixtures]
             [scaffold.supabase.event-host-util :as live]]})

(fact:global
  {:setup [(l/rt:restart)
           (do (live/init-live-postgres-runtime!)
               (l/rt:setup (live/pg-rt) live/+postgres-module+)
               (live/grant-scratch-schema!)
               (ensure-scratch-rpc!)
               (live/refresh-live-supabase-config!)
               (live/cleanup-scratch-entry! live/+live-entry-name+)
               true)]
   :teardown [(do (live/cleanup-scratch-entry! live/+live-entry-name+)
                  (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                  (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                  (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                  true)
              (l/rt:stop)]})

^{:refer xt.db.system.impl-supabase/supabase-client-init :added "4.1"}
(fact "supabase-client-init stores a live local supabase client"

  (notify/wait-on [:js 10000]
    (var settings (xt/x:obj-clone (. (@! live/+live-supabase-config+) ["client"])))
    (xt/x:set-key settings "transport" (js-fetch/client {}))
    (promise/x:promise-then
     (impl/supabase-client-init
      (impl/supabase-client
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       {}
       settings))
     (fn [client]
       (repl/notify
        {"tag" (. client ["::"])
         "instance" (supabase/client? (. client ["instance"]))}))))
  => {"tag" "db.client.supabase"
      "instance" true})

^{:refer xt.db.system.impl-supabase/pull-async :added "4.1"}
(fact "pull-async reads scratch Entry rows through the local supabase rest api"

  (do
    (live/setup-scratch-entry! live/+live-entry-name+ live/+live-entry-tags+)
    (try
      (notify/wait-on [:js 15000]
        (var settings (xt/x:obj-clone (. (@! live/+live-supabase-config+) ["client"])))
        (xt/x:set-key settings "transport" (js-fetch/client {}))
        (promise/x:promise-then
         (impl/supabase-client-init
          (impl/supabase-client
           (@! fixtures/+schema+)
           (@! fixtures/+lookup+)
           {}
           settings))
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

^{:refer xt.db.system.impl-supabase/rpc-call-async :added "4.1"}
(fact "rpc-call-async calls scratch rpc endpoints through the local supabase rest api"

  (notify/wait-on [:js 15000]
    (var settings (xt/x:obj-clone (. (@! live/+live-supabase-config+) ["client"])))
    (xt/x:set-key settings "transport" (js-fetch/client {}))
    (promise/x:promise-then
     (impl/supabase-client-init
      (impl/supabase-client
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       {}
       settings))
     (fn [client]
       (promise/x:promise-then
        (impl/rpc-call-async client "copilot-ping" nil)
        (fn [out]
          (repl/notify out))))))
  => {"status" "ok"
      "source" "supabase"})
