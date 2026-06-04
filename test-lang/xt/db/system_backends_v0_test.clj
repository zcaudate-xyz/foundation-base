(ns xt.db.system-backends-v0-test
  (:use code.test)
  (:require [hara.lang :as l]
            [postgres.core.supabase :as s]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.event-host-util :as live]))

(def +log-message+
  "copilot_system_backends_v0")

(defn cleanup-log!
  []
  (live/pg-exec-best-effort!
   (str "DELETE FROM \"" live/+scratch-v0-schema+ "\".\"Log\""
        " WHERE message = "
        (live/sql-literal +log-message+))))

(defn grant-scratch-v0-schema!
  []
  (doseq [sql [(l/emit-as :postgres `[(s/grant-usage ~live/+scratch-v0-schema+)])
               (str "GRANT ALL ON ALL TABLES IN SCHEMA \"" live/+scratch-v0-schema+
                    "\" TO anon, authenticated, service_role")
               (str "GRANT ALL ON ALL SEQUENCES IN SCHEMA \"" live/+scratch-v0-schema+
                    "\" TO anon, authenticated, service_role")
               (str "GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA \"" live/+scratch-v0-schema+
                    "\" TO anon, authenticated, service_role")
               (str "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \""
                    live/+scratch-v0-schema+
                    "\" GRANT ALL ON TABLES TO anon, authenticated, service_role")
               (str "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \""
                    live/+scratch-v0-schema+
                    "\" GRANT ALL ON SEQUENCES TO anon, authenticated, service_role")
               (str "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \""
                    live/+scratch-v0-schema+
                    "\" GRANT EXECUTE ON FUNCTIONS TO anon, authenticated, service_role")]]
    (live/pg-exec-best-effort! sql)))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.core :as pg]
             [postgres.sample.scratch-v0 :as v0]]})

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.system :as db-system]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [js.lib.client-fetch :as js-fetch]
             [scaffold.supabase.event-host-util :as live]]})

(fact:global
 {:setup [(l/rt:restart)
          (do (live/init-live-postgres-runtime!)
              (live/grant-scratch-schema!)
              (l/rt:setup (live/pg-rt) 'postgres.sample.scratch-v0)
              (grant-scratch-v0-schema!)
              (live/reload-postgrest! live/+scratch-v0-schema+ "Log")
              (live/refresh-live-supabase-config!)
              (cleanup-log!)
              true)]
  :teardown [(do (cleanup-log!)
                 (l/rt:teardown (live/pg-rt) 'postgres.sample.scratch-v0)
                 (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                 (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                 true)
             (l/rt:stop)]})

^{:refer xt.db.system-backends-v0-test/supabase-backend-v0 :added "4.1"}
(fact "supabase backend v0: query scratch-v0 Log through local postgrest"

  (do
    (cleanup-log!)
    (try
      (live/pg-exec!
       (str "INSERT INTO \"" live/+scratch-v0-schema+ "\".\"Log\" (message)"
            " VALUES (" (live/sql-literal +log-message+) ");"))
      (Thread/sleep 200)
      (notify/wait-on [:js 15000]
        (var live-config (@! live/+live-supabase-config+))
        (var live-client (. live-config ["client"]))
        (var schema
             {"Log"
              {"id" {"type" "uuid"
                     "primary" true
                     "scope" "id"
                     "order" 0
                     "ident" "id"}
               "message" {"type" "text"
                          "required" true
                          "scope" "data"
                          "order" 1
                          "ident" "message"}
               "author_id" {"type" "uuid"
                            "scope" "data"
                            "order" 2
                            "ident" "author_id"}}})
        (var lookup
             {"Log" {"position" 0}})
        (var client-config
             {"base_url" (. live-client ["base_url"])
              "schema_name" (@! live/+scratch-v0-schema+)
              "api_key" (. live-client ["api_key"])
              "auth_token" (. live-client ["auth_token"])
              "transport" (js-fetch/client {})})
        (var db (db-system/db-create
                 {"::" "db.supabase"
                  :instance client-config}
                 schema
                 lookup
                 nil))
        (promise/x:promise-then
         (db-system/db-pull
          db
          schema
          ["Log"
           {"message" (@! xt.db.system-backends-v0-test/+log-message+)}
           ["message"]])
         (fn [result]
           (repl/notify
            {"has_db" true
             "dbtype" (xt/x:get-key db "::")
             "message" (xtd/get-in result [0 "message"])}))))
      (finally
        (cleanup-log!))))
  => {"has_db" true
      "dbtype" "db.supabase"
      "message" "copilot_system_backends_v0"})


(comment
  
  (+ 1 2 3))
