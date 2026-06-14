(ns xt.db.walkthrough.guide-07-supabase-realtime-cache-sync-test
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [postgres.core :as pg]
            [scaffold.supabase.local-min :as live])
  (:import (java.net URI)
           (java.net.http HttpClient HttpRequest HttpResponse$BodyHandlers)))

(def +supabase-pg-config+
  {:host (get-in live/+config+ [:db :host])
   :port (get-in live/+config+ [:db :port])
   :user (get-in live/+config+ [:db :user])
   :pass (get-in live/+config+ [:db :password])
   :dbname (get-in live/+config+ [:db :database])
   :startup live/start-supabase
   :teardown live/stop-supabase})

(def +public-schema+
  "public")

(def +scratch-entry-table+
  "Entry")

(def +live-realtime-entry-name+
  "copilot_supabase_realtime_live")

(def +live-realtime-entry-tags+
  ["copilot" "supabase" "realtime"])

(def +live-realtime-entry-query+
  ["Entry"
   ["name"
    "tags"]])

(def ^:private +http-client+
  (HttpClient/newHttpClient))

(defn sql-literal
  [s]
  (str "'" (str/replace s "'" "''") "'"))

(defn pg-exec!
  [sql]
  (pg/raw-eval (l/rt-resolve :postgres) sql))

(defn pg-exec-best-effort!
  [sql]
  (try
    (pg-exec! sql)
    (catch Throwable _
      nil)))

(defn http-get
  [url headers]
  (let [builder (HttpRequest/newBuilder (URI/create url))]
    (doseq [[k v] headers]
      (.header builder k v))
    (let [request (.build (.GET builder))
          response (.send +http-client+
                          request
                          (HttpResponse$BodyHandlers/ofString))]
      {:status (.statusCode response)
       :body (.body response)})))

(defn cleanup-public-entry!
  [name]
  (pg-exec-best-effort!
   (str "DELETE FROM \"" +public-schema+ "\".\"" +scratch-entry-table+
        "\" WHERE name = " (sql-literal name))))

(defn ensure-public-entry-table!
  []
  (pg-exec!
   (str "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";\n"
        "CREATE TABLE IF NOT EXISTS \"" +public-schema+ "\".\"" +scratch-entry-table+ "\" (\n"
        "  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),\n"
        "  name text NOT NULL UNIQUE,\n"
        "  tags jsonb NOT NULL DEFAULT '[]'::jsonb\n"
        ");\n"
        "GRANT ALL ON TABLE \"" +public-schema+ "\".\"" +scratch-entry-table+ "\" TO anon, authenticated, service_role;")))

(defn enable-public-entry-realtime!
  []
  (pg-exec!
   (str "DO $$\n"
        "BEGIN\n"
        "  IF NOT EXISTS (\n"
        "    SELECT 1\n"
        "    FROM pg_publication_tables\n"
        "    WHERE pubname = 'supabase_realtime'\n"
        "      AND schemaname = '" +public-schema+ "'\n"
        "      AND tablename = '" +scratch-entry-table+ "'\n"
        "  ) THEN\n"
        "    EXECUTE 'ALTER PUBLICATION supabase_realtime ADD TABLE \"" +public-schema+ "\".\"" +scratch-entry-table+ "\"';\n"
        "  END IF;\n"
        "END $$;")))

(defn wait-postgrest-schema!
  ([schema-name]
   (wait-postgrest-schema! schema-name +scratch-entry-table+))
  ([schema-name table-name]
   (let [api (:api live/+config+)
         base-url (str (or (:protocol api) "http")
                       "://"
                       (or (:hostname api) "127.0.0.1")
                       ":"
                       (or (:port api) 55121))
         api-key (or (:service-key api) "")
         headers {"apikey" api-key
                  "Authorization" (str "Bearer " api-key)
                  "Accept-Profile" schema-name
                  "Content-Profile" schema-name}
         url (str base-url "/rest/v1/" table-name "?select=*&limit=1")]
     (loop [attempt 0]
       (let [response (try
                        (http-get url headers)
                        (catch Throwable _
                          nil))
             ready? (and response
                         (== 200 (:status response)))]
         (cond ready?
               true

               (< attempt 89)
               (do (Thread/sleep 1000)
                   (recur (inc attempt)))

               :else
               (throw (ex-info "Timed out waiting for PostgREST schema cache"
                               {:schema schema-name
                                :table table-name
                                :response response}))))))))

(defn reload-postgrest!
  ([] (reload-postgrest! +public-schema+ +scratch-entry-table+))
  ([schema-name]
   (reload-postgrest! schema-name +scratch-entry-table+))
  ([schema-name table-name]
   (pg-exec-best-effort! "NOTIFY pgrst, 'reload schema'")
   (wait-postgrest-schema! schema-name table-name)))

(l/script- :postgres
  {:runtime :jdbc.client
   :config +supabase-pg-config+
   :require [[postgres.sample.scratch-v1 :as scratch]]})

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
           (l/rt:setup :postgres)
           (ensure-public-entry-table!)
           (enable-public-entry-realtime!)
           (reload-postgrest! +public-schema+)
           (cleanup-public-entry! +live-realtime-entry-name+)]
   :teardown [(cleanup-public-entry! +live-realtime-entry-name+)
              (l/rt:teardown :postgres)
              (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-07-supabase-realtime-cache-sync/STEP.00-sync-live-realtime-into-cache
  :added "4.1"
  :setup [(cleanup-public-entry! +live-realtime-entry-name+)]}
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
      (var schema-name (@! +public-schema+))
      (var table-name (@! +scratch-entry-table+))
      (var payload
          {"eventType" "INSERT"
           "schema" schema-name
           "table" table-name
           "new" {"id" "00000000-0000-0000-0000-0000000000f7"
                  "name" (@! +live-realtime-entry-name+)
                  "tags" (@! +live-realtime-entry-tags+)}})
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
            (@! +live-realtime-entry-query+))
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
        "cached_row" cached-row})))
  => {"status" "SUBSCRIBED"
      "topic" "realtime:public:Entry"
      "request_name" "copilot_supabase_realtime_live"
      "request_tags" ["copilot" "supabase" "realtime"]
      "cached_row" {"name" "copilot_supabase_realtime_live"
                    "tags" ["copilot" "supabase" "realtime"]}})
