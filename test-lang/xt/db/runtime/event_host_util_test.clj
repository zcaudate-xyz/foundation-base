(ns xt.db.runtime.event-host-util-test
  (:require [clojure.string :as str]
            [xt.db.runtime.event-host-util :as event-host-util])
  (:import (com.sun.net.httpserver HttpExchange HttpHandler HttpServer)
           (java.net InetSocketAddress))
  (:use code.test))

^{:refer xt.db.runtime.event-host-util/request-event :added "4.1.4"}
(fact "returns canonical xt.db event names"
  [(event-host-util/request-event {"db/sync" {"Entry" []}})
   (event-host-util/request-event {"db/remove" {"Entry" ["id-1"]}})
   (event-host-util/request-event {"db/query" {"Entry" []}})]
  => ["db/sync" "db/remove" nil])


^{:refer xt.db.runtime.event-host-util/shell-command :added "4.1"}
(fact "joins shell command parts with spaces"
  (event-host-util/shell-command "echo" "hello" "world")
  => "echo hello world")

^{:refer xt.db.runtime.event-host-util/supabase-shell-command :added "4.1"}
(fact "prefixes shell commands with the supabase cli invocation"
  (event-host-util/supabase-shell-command "status" "-o" "env")
  => "npx supabase status -o env")

^{:refer xt.db.runtime.event-host-util/startup-shell-command :added "4.1"}
(fact "builds a startup script that checks postgres and starts supabase"
  (let [cmd (event-host-util/startup-shell-command)]
    [(str/includes? cmd "npx supabase start")
     (str/includes? cmd "127.0.0.1")
     (str/includes? cmd "55122")])
  => [true true true])

^{:refer xt.db.runtime.event-host-util/init-live-postgres-runtime! :added "4.1"}
(fact "creates and stores the live postgres runtime"
  (let [old event-host-util/+postgres-runtime+]
    (try
      (with-redefs [hara.lang.script/script-test
                    (fn [& args]
                      {:args args})]
        (let [rt (event-host-util/init-live-postgres-runtime!)]
          [(first (:args rt))
           (= rt (event-host-util/pg-rt))]))
      (finally
        (alter-var-root #'event-host-util/+postgres-runtime+ (constantly old)))))
  => [:postgres
      true])

^{:refer xt.db.runtime.event-host-util/pg-rt :added "4.1"}
(fact "returns the current postgres runtime var"
  (let [old event-host-util/+postgres-runtime+]
    (try
      (alter-var-root #'event-host-util/+postgres-runtime+ (constantly {:id "pg-live"}))
      (event-host-util/pg-rt)
      (finally
        (alter-var-root #'event-host-util/+postgres-runtime+ (constantly old)))))
  => {:id "pg-live"})

^{:refer xt.db.runtime.event-host-util/supabase-status-env :added "4.1"}
(fact "reads supabase status output through os/sh"
  (with-redefs [std.lib.os/sh (fn [_] (atom "API_URL=http://localhost:54321"))]
    (event-host-util/supabase-status-env))
  => "API_URL=http://localhost:54321")

^{:refer xt.db.runtime.event-host-util/parse-shell-env :added "4.1"}
(fact "parses shell env output into a map"
  (event-host-util/parse-shell-env "API_URL=\"http://localhost\"\nSERVICE_ROLE_KEY=abc\n\n")
  => {"API_URL" "http://localhost"
      "SERVICE_ROLE_KEY" "abc"})

^{:refer xt.db.runtime.event-host-util/http-get :added "4.1"}
(fact "performs a basic http get and returns status and body"
  (let [server (HttpServer/create (InetSocketAddress. 0) 0)]
    (try
      (.createContext server
                      "/ping"
                      (reify HttpHandler
                        (^void handle [_ ^HttpExchange exchange]
                          (let [body (.getBytes "ok")]
                            (.sendResponseHeaders exchange 200 (long (alength body)))
                            (with-open [out (.getResponseBody exchange)]
                              (.write out body))))))
      (.start server)
      (let [port (.getPort (.getAddress server))]
        (event-host-util/http-get (str "http://127.0.0.1:" port "/ping")
                                  {"X-Test" "1"}))
      (finally
        (.stop server 0))))
  => {:status 200
      :body "ok"})

^{:refer xt.db.runtime.event-host-util/refresh-live-supabase-config! :added "4.1"}
(fact "refreshes the cached live supabase config from status env"
  (let [old event-host-util/+live-supabase-config+]
    (try
      (with-redefs [event-host-util/supabase-status-env
                    (fn []
                      "API_URL=http://localhost:54321\nSERVICE_ROLE_KEY=service-key")]
        [(event-host-util/refresh-live-supabase-config!)
         event-host-util/+live-supabase-config+])
      (finally
        (alter-var-root #'event-host-util/+live-supabase-config+ (constantly old)))))
  => [{"::" "db.supabase"
       "client" {"base_url" "http://localhost:54321"
                 "schema_name" "scratch"
                 "api_key" "service-key"
                 "auth_token" "service-key"}}
      {"::" "db.supabase"
       "client" {"base_url" "http://localhost:54321"
                 "schema_name" "scratch"
                 "api_key" "service-key"
                 "auth_token" "service-key"}}])

^{:refer xt.db.runtime.event-host-util/pg-exec! :added "4.1"}
(fact "delegates raw sql execution to postgres runtime eval"
  (with-redefs [event-host-util/pg-rt (fn [] {:id "pg"})
                hara.runtime.postgres/raw-eval (fn [rt sql] [rt sql])]
    (event-host-util/pg-exec! "SELECT 1"))
  => [{:id "pg"} "SELECT 1"])

^{:refer xt.db.runtime.event-host-util/pg-exec-best-effort! :added "4.1"}
(fact "returns nil when postgres execution raises"
  [(with-redefs [event-host-util/pg-exec! (fn [_] :ok)]
     (event-host-util/pg-exec-best-effort! "SELECT 1"))
   (with-redefs [event-host-util/pg-exec! (fn [_] (throw (ex-info "boom" {})))]
     (event-host-util/pg-exec-best-effort! "SELECT 1"))]
  => [:ok nil])

^{:refer xt.db.runtime.event-host-util/sql-literal :added "4.1"}
(fact "quotes sql string literals and escapes apostrophes"
  (event-host-util/sql-literal "it's live")
  => "'it''s live'")

^{:refer xt.db.runtime.event-host-util/ensure-scratch-entry-table! :added "4.1"}
(fact "creates the scratch entry table schema"
  (let [out (atom nil)]
    (with-redefs [event-host-util/pg-exec! (fn [sql] (reset! out sql))]
      (event-host-util/ensure-scratch-entry-table!)
      [(str/includes? @out "CREATE SCHEMA IF NOT EXISTS \"scratch\";")
       (str/includes? @out "CREATE TABLE IF NOT EXISTS \"scratch\".\"Entry\"")
       (str/includes? @out "__deleted__ boolean NOT NULL DEFAULT false")]))
  => [true true true])

^{:refer xt.db.runtime.event-host-util/grant-scratch-schema! :added "4.1"}
(fact "grants scratch schema access after ensuring the table exists"
  (let [calls (atom [])]
    (with-redefs [event-host-util/ensure-scratch-entry-table! (fn [] (swap! calls conj :ensure))
                  event-host-util/pg-exec-best-effort! (fn [sql] (swap! calls conj sql))]
      (event-host-util/grant-scratch-schema!)
      [(first @calls)
       (count @calls)
       (str/includes? (second @calls) "GRANT USAGE ON SCHEMA \"scratch\"")
       (str/includes? (nth @calls 2) "GRANT ALL ON ALL TABLES IN SCHEMA \"scratch\"")]))
  => [:ensure 4 true true])

^{:refer xt.db.runtime.event-host-util/enable-scratch-entry-realtime! :added "4.1"}
(fact "adds the scratch entry table to the supabase realtime publication"
  (let [out (atom nil)]
    (with-redefs [event-host-util/pg-exec! (fn [sql] (reset! out sql))]
      (event-host-util/enable-scratch-entry-realtime!)
      [(str/includes? @out "pubname = 'supabase_realtime'")
       (str/includes? @out "schemaname = 'scratch'")
       (str/includes? @out "tablename = 'Entry'")]))
  => [true true true])

^{:refer xt.db.runtime.event-host-util/reload-postgrest! :added "4.1"}
(fact "reloads postgrest and waits for the schema cache"
  (let [calls (atom [])]
    (with-redefs [event-host-util/pg-exec-best-effort! (fn [sql] (swap! calls conj sql))
                  event-host-util/wait-postgrest-schema! (fn
                                                           ([schema]
                                                            (swap! calls conj [schema "Entry"]))
                                                           ([schema table]
                                                            (swap! calls conj [schema table])))]
      (event-host-util/reload-postgrest!)
      (event-host-util/reload-postgrest! "public")
      (event-host-util/reload-postgrest! "scratch_v0" "Log")
      @calls))
  => ["NOTIFY pgrst, 'reload schema'"
      ["scratch" "Entry"]
      "NOTIFY pgrst, 'reload schema'"
      ["public" "Entry"]
      "NOTIFY pgrst, 'reload schema'"
      ["scratch_v0" "Log"]])

^{:refer xt.db.runtime.event-host-util/cleanup-scratch-entry! :added "4.1"}
(fact "deletes named rows from the scratch entry table"
  (let [out (atom nil)]
    (with-redefs [event-host-util/pg-exec-best-effort! (fn [sql] (reset! out sql))]
      (event-host-util/cleanup-scratch-entry! "entry-a")
      [(str/includes? @out "DELETE FROM \"scratch\".\"Entry\"")
       (str/includes? @out "'entry-a'")]))
  => [true true])

^{:refer xt.db.runtime.event-host-util/setup-scratch-entry! :added "4.1"}
(fact "recreates a scratch entry row with json tags"
  (let [calls (atom [])]
    (with-redefs [event-host-util/cleanup-scratch-entry! (fn [name] (swap! calls conj [:cleanup name]))
                  event-host-util/pg-exec! (fn [sql] (swap! calls conj sql))]
      (event-host-util/setup-scratch-entry! "entry-a" ["a" "b"])
      [(first @calls)
       (str/includes? (second @calls) "INSERT INTO \"scratch\".\"Entry\"")
       (str/includes? (second @calls) "\"a\"")]))
  => [[:cleanup "entry-a"] true true])

^{:refer xt.db.runtime.event-host-util/ensure-public-entry-table! :added "4.1"}
(fact "creates the public entry table if needed"
  (let [out (atom nil)]
    (with-redefs [event-host-util/pg-exec! (fn [sql] (reset! out sql))]
      (event-host-util/ensure-public-entry-table!)
      [(str/includes? @out "CREATE TABLE IF NOT EXISTS \"public\".\"Entry\"")
       (str/includes? @out "GRANT ALL ON TABLE \"public\".\"Entry\"")]))
  => [true true])

^{:refer xt.db.runtime.event-host-util/enable-public-entry-realtime! :added "4.1"}
(fact "adds the public entry table to the realtime publication"
  (let [out (atom nil)]
    (with-redefs [event-host-util/pg-exec! (fn [sql] (reset! out sql))]
      (event-host-util/enable-public-entry-realtime!)
      [(str/includes? @out "schemaname = 'public'")
       (str/includes? @out "tablename = 'Entry'")]))
  => [true true])

^{:refer xt.db.runtime.event-host-util/wait-postgrest-schema! :added "4.1"}
(fact "polls postgrest until the schema responds"
  (with-redefs [event-host-util/supabase-status-env
                (fn []
                  "API_URL=http://localhost:54321\nSERVICE_ROLE_KEY=service-key")
                event-host-util/http-get
                (fn [url headers]
                  {:status 200
                   :url url
                   :headers headers})]
    [(event-host-util/wait-postgrest-schema! "scratch")
     (event-host-util/wait-postgrest-schema! "scratch_v0" "Log")])
  => [true true])

^{:refer xt.db.runtime.event-host-util/cleanup-public-entry! :added "4.1"}
(fact "deletes named rows from the public entry table"
  (let [out (atom nil)]
    (with-redefs [event-host-util/pg-exec-best-effort! (fn [sql] (reset! out sql))]
      (event-host-util/cleanup-public-entry! "entry-b")
      [(str/includes? @out "DELETE FROM \"public\".\"Entry\"")
       (str/includes? @out "'entry-b'")]))
  => [true true])

^{:refer xt.db.runtime.event-host-util/setup-public-entry! :added "4.1"}
(fact "recreates a public entry row with json tags"
  (let [calls (atom [])]
    (with-redefs [event-host-util/cleanup-public-entry! (fn [name] (swap! calls conj [:cleanup name]))
                  event-host-util/pg-exec! (fn [sql] (swap! calls conj sql))]
      (event-host-util/setup-public-entry! "entry-b" ["x" "y"])
      [(first @calls)
       (str/includes? (second @calls) "INSERT INTO \"public\".\"Entry\"")
       (str/includes? (second @calls) "\"x\"")]))
  => [[:cleanup "entry-b"] true true])

^{:refer xt.db.runtime.event-host-util/send-realtime-broadcast! :added "4.1"}
(fact "sends realtime broadcast payloads through postgres"
  (let [out (atom nil)]
    (with-redefs [event-host-util/pg-exec! (fn [sql] (reset! out sql))]
      (event-host-util/send-realtime-broadcast! "room:entries" "db/sync" {"db/sync" {"Entry" []}})
      [(str/includes? @out "SELECT realtime.send(")
       (str/includes? @out "'db/sync'")
       (str/includes? @out "'room:entries'")]))
  => [true true true])

^{:refer xt.db.runtime.event-host-util/send-realtime-request! :added "4.1"}
(fact "derives the canonical request event before sending realtime payloads"
  [(let [calls (atom [])]
     (with-redefs [event-host-util/send-realtime-broadcast! (fn [topic event payload]
                                                              (swap! calls conj [topic event payload]))]
       (event-host-util/send-realtime-request! "room:entries" {"db/remove" {"Entry" ["id-1"]}})
       @calls))
   (try
     (event-host-util/send-realtime-request! "room:entries" {"db/query" {"Entry" []}})
     :no-throw
     (catch Throwable t
       (ex-message t)))]
  => [[["room:entries" "db/remove" {"db/remove" {"Entry" ["id-1"]}}]]
      "Unsupported xt.db realtime request"])