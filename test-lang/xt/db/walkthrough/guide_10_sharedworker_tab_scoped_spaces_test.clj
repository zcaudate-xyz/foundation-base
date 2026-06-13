(ns xt.db.walkthrough.guide-10-sharedworker-tab-scoped-spaces-test
  (:use code.test)
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [js.worker.link]
            [js.worker.sharedworker :as worker-shared]
            [scaffold.supabase.docker-min :as live]
            [postgres.core :as pg]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.lang.common-notify :as notify]))

(def +public-schema+
  "public")

(def +scratch-entry-table+
  "Entry")

(defn primary-supabase-config
  []
  {"::" "db.supabase"
   "client" {"base_url" (str (or (-> live/+config+ :api :protocol) "http")
                             "://"
                             (or (-> live/+config+ :api :hostname) "127.0.0.1")
                             ":"
                             (or (-> live/+config+ :api :port) 55121))
             "api_key" (-> live/+config+ :api :service-key)
             "auth_token" (-> live/+config+ :api :service-key)}})

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

(defn cleanup-public-entry!
  [name]
  (pg-exec-best-effort!
   (str "DELETE FROM \"" +public-schema+ "\".\"" +scratch-entry-table+
        "\" WHERE name = " (sql-literal name))))

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
         url (str base-url "/rest/v1/" table-name "?select=*&limit=1")]
     (loop [attempt 0]
       (let [response (try
                        (let [builder (java.net.http.HttpRequest/newBuilder (java.net.URI/create url))]
                          (doseq [[k v] {"apikey" api-key
                                         "Authorization" (str "Bearer " api-key)
                                         "Accept-Profile" schema-name
                                         "Content-Profile" schema-name}]
                            (.header builder k v))
                          (let [request (.build (.GET builder))
                                response (.send (java.net.http.HttpClient/newHttpClient)
                                                request
                                                (java.net.http.HttpResponse$BodyHandlers/ofString))]
                            {:status (.statusCode response)
                             :body (.body response)}))
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

^{:seedgen/root {:all true}}
(l/script :js
  {:runtime :chromedriver.instance
   :require [[js.worker.link :as worker-link]
            [js.worker.sharedworker :as worker-shared]
            [xt.db.node :as db-node]
            [xt.substrate :as event-node]
            [xt.lang.common-data :as xtd]
            [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn make-sharedworker-script
  []
  (let [primary-config (-> (primary-supabase-config)
                           (assoc-in ["client" "schema_name"] +public-schema+))
        node-init-template
        '(xt.db.node/create
         {"node_id" "xtdb-shared-worker"
          "db" {"schema" __SCHEMA__
                "lookup" __LOOKUP__
                "sources"
                {"primary" {"kind" "supabase"
                            "config" ((fn []
                                        (var primary-config (xt.lang.spec-base/x:obj-clone __PRIMARY_CONFIG__))
                                        (var client-config (xt.lang.spec-base/x:obj-clone (. primary-config ["client"])))
                                        (xt.lang.spec-base/x:set-key client-config
                                                                     "transport"
                                                                     (js.lib.client-fetch/client {}))
                                        (xt.lang.spec-base/x:set-key primary-config "client" client-config)
                                        (return primary-config)))}
                 "caching" {"kind" "cache"
                            "sync_from" "primary"
                            "config" {}}}}
          "spaces" {}})
        node-init (walk/postwalk-replace {'__SCHEMA__ fixtures/+schema+
                                         '__LOOKUP__ fixtures/+lookup+
                                         '__PRIMARY_CONFIG__ primary-config}
                                        node-init-template)]
    (worker-shared/script {"shared_key" "__guide_tab_worker__"
                          "transport_prefix" "host-"
                          "ready" {"signal" "ready"
                                   "worker" "xtdb-shared-worker"}
                          "node_init" node-init})))

(def +sharedworker-script+ nil)

(fact:global
  {:setup [(l/rt:restart :js)
           (l/rt:scaffold-imports :js)
           (l/rt:setup :postgres)
           (ensure-public-entry-table!)
           (enable-public-entry-realtime!)
           (reload-postgrest! +public-schema+)
           (cleanup-public-entry! "alpha")
           (cleanup-public-entry! "beta")
           (do (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                                  4000)
               true)]
   :teardown [(do
                (cleanup-public-entry! "alpha")
                (cleanup-public-entry! "beta")
                (l/rt:teardown :postgres)
                true)
              (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-10-sharedworker-tab-scoped-spaces/STEP.00-prefix-browser-tabs-into-shared-worker-spaces
  :added "4.1"
  :setup [(do (cleanup-public-entry! "alpha")
              (cleanup-public-entry! "beta")
              (pg-exec!
               (str "INSERT INTO \"public\".\"Entry\" (id, name, tags) VALUES "
                    "('00000000-0000-0000-0000-0000000000d1', 'alpha', '[\"guide\",\"sql\"]'::jsonb), "
                    "('00000000-0000-0000-0000-0000000000d2', 'beta', '[\"guide\"]'::jsonb);"))
              true)
          (Thread/sleep 400)]}
(fact "step 00: a browser SharedWorker uses a shared Supabase primary while tab routes are prefixed into their own xt.db spaces"
  (string? (make-sharedworker-script))
  => true
  (alter-var-root #'+sharedworker-script+ (constantly (make-sharedworker-script)))
  (notify/wait-on [:js 12000]
    (var browser-node (event-node/node-create {"id" "browser-node"}))
    (var shared-space "worker/screen/admin")
    (var tab-a-space "tab-a/screen/admin")
    (var tab-b-space "tab-b/screen/admin")
    (var model-id "entries-screen")
    (var model-spec
           {"sources"
            {"primary" {"resolver" {"type" "db/query"
                                    "table" "Entry"
                                    "select_entry" {"input" []
                                                    "view" {"query" {}}}
                                    "return_entry" {"input" [{"symbol" "i_entry_id"
                                                              "type" "uuid"}]
                                                    "view" {"query" ["id"
                                                                     "name"
                                                                     "tags"]}}}}
             "caching" {"resolver" {"type" "db/query"
                                    "table" "Entry"
                                    "select_entry" {"input" []
                                                    "view" {"query" {}}}
                                    "return_entry" {"input" [{"symbol" "i_entry_id"
                                                              "type" "uuid"}]
                                                    "view" {"query" ["id"
                                                                     "name"
                                                                     "tags"]}}}}}
            "views"
            {"list" {"resolver" {"type" "db/query"
                                 "table" "Entry"
                                 "select_entry" {"input" []
                                                 "view" {"query" {}}}
                                 "return_entry" {"input" [{"symbol" "i_entry_id"
                                                           "type" "uuid"}]
                                                 "view" {"query" ["id"
                                                                  "name"
                                                                  "tags"]}}}
                     "source" "caching"}
             "detail" {"resolver" {"type" "db/query"
                                   "table" "Entry"
                                   "select_entry" {"input" [{"symbol" "i_name"
                                                             "type" "text"}]
                                                   "view" {"query" {"name" "{{i_name}}"}}}
                                   "return_entry" {"input" [{"symbol" "i_entry_id"
                                                             "type" "uuid"}]
                                                   "view" {"query" ["name"
                                                                    "tags"]}}}
                       "source" "primary"}}})
    (promise/x:promise-catch
     (promise/x:promise-then
      (worker-shared/connect
       browser-node
       {"transport_id" "worker"
        "shared_space" shared-space
        "source" (worker-link/make-sharedworker-link (@! +sharedworker-script+))})
       (fn [session]
         (return
          (-> (worker-shared/ensure-model session
                                          model-id
                                          model-spec)
               (promise/x:promise-then
                (fn [_]
                  (return
                   (promise/x:promise-all
                    [(worker-shared/open-tab session
                                             tab-a-space
                                             model-id
                                             model-spec
                                             "alpha")
                     (worker-shared/open-tab session
                                             tab-b-space
                                             model-id
                                             model-spec
                                             "beta")
                     (worker-shared/node-summary session)]))))
               (promise/x:promise-then
                (fn [[tab-a tab-b summary]]
                  (return
                   (promise/x:promise-then
                    (worker-shared/disconnect session)
                    (fn [_]
                      (var spaces (xt/x:get-key summary "spaces"))
                      (repl/notify
                       {"ready_worker" (xtd/get-in (. session ["ready"]) ["worker"])
                        "tab_a_space" (xt/x:get-key tab-a "space_id")
                        "tab_b_space" (xt/x:get-key tab-b "space_id")
                        "tab_a_list_count" (xt/x:get-key tab-a "list_count")
                        "tab_b_list_count" (xt/x:get-key tab-b "list_count")
                        "tab_a_list_source" (xt/x:get-key tab-a "list_source")
                        "tab_b_list_source" (xt/x:get-key tab-b "list_source")
                        "tab_a_detail_name" (xt/x:get-key tab-a "detail_name")
                        "tab_b_detail_name" (xt/x:get-key tab-b "detail_name")
                        "tab_b_cached_first" (xt/x:get-key tab-b "cached_first")
                        "space_count" (xt/x:len (xt/x:obj-keys spaces))
                        "has_shared_space" (xt/x:not-nil? (xt/x:get-key spaces shared-space))
                        "has_tab_a" (xt/x:not-nil? (xt/x:get-key spaces tab-a-space))
                        "has_tab_b" (xt/x:not-nil? (xt/x:get-key spaces tab-b-space))}))))))))))
      (fn [err]
       (repl/notify {"error" err}))))
  => {"ready_worker" "xtdb-shared-worker"
      "tab_a_space" "tab-a/screen/admin"
      "tab_b_space" "tab-b/screen/admin"
      "tab_a_list_count" 2
      "tab_b_list_count" 2
      "tab_a_list_source" "caching"
      "tab_b_list_source" "caching"
      "tab_a_detail_name" "alpha"
      "tab_b_detail_name" "beta"
      "tab_b_cached_first" "alpha"
      "space_count" 3
      "has_shared_space" true
      "has_tab_a" true
      "has_tab_b" true})
