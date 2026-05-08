(ns xtbench.python.db.runtime.parity-supabase-websocket-test
  (:use code.test)
  (:require [clojure.string :as str]
  	        [hara.lang :as l]
            [std.config.global :as config.global]
            [xt.lang.common-notify :as notify]))

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.db.websocket.supabase-client :as realtime]]})

(def +live-supabase-api-env+
  ["DEFAULT_SUPABASE_API_ENDPOINT"
   "DEFAULT_SUPABASE_API_KEY_SERVICE"])

(def +live-supabase-postgres-env+
  ["DEFAULT_RT_POSTGRES_HOST"
   "DEFAULT_RT_POSTGRES_PORT"
   "DEFAULT_RT_POSTGRES_USER"
   "DEFAULT_RT_POSTGRES_PASS"
   "DEFAULT_RT_POSTGRES_DBNAME"])

(def +live-supabase-required-env+
  (vec (concat +live-supabase-api-env+
               +live-supabase-postgres-env+)))

(def +live-supabase-schema+
  "scratch-sample-db")

(def +live-supabase-table+
  "UserAccount")

(defn live-config
  []
  (config.global/global :all {:cached false}))

(defn config-key-path
  [k]
  (->> (str/split (str/lower-case k) #"_")
       (mapv keyword)))

(defn live-config-value
  [k]
  (let [config (live-config)]
    (or (get config k)
        (get config (keyword k))
        (get config (keyword (str/lower-case k)))
        (get-in config (config-key-path k))
        (System/getProperty k)
        (System/getenv k))))

(defn live-config-present?
  [k]
  (some? (live-config-value k)))

(defn live-supabase?
  []
  (every? live-config-present? +live-supabase-required-env+))

(defn live-supabase-endpoint
  []
  (live-config-value "DEFAULT_SUPABASE_API_ENDPOINT"))

(defn live-supabase-service-key
  []
  (live-config-value "DEFAULT_SUPABASE_API_KEY_SERVICE"))

(defn live-postgres-jdbc-url
  []
  (let [host   (live-config-value "DEFAULT_RT_POSTGRES_HOST")
        port   (live-config-value "DEFAULT_RT_POSTGRES_PORT")
        dbname (live-config-value "DEFAULT_RT_POSTGRES_DBNAME")
        ssl?   (not (contains? #{"127.0.0.1" "localhost"} host))]
    (str "jdbc:pgsql://" host ":" port "/" dbname
         "?sslmode=" (if ssl? "require" "disable"))))

(defn live-postgres-connection
  []
  (Class/forName "com.impossibl.postgres.jdbc.PGDriver")
  (java.sql.DriverManager/getConnection
   (live-postgres-jdbc-url)
   (live-config-value "DEFAULT_RT_POSTGRES_USER")
   (live-config-value "DEFAULT_RT_POSTGRES_PASS")))

(defn jdbc-exec!
  [sql]
  (with-open [conn (live-postgres-connection)
              stmt (.createStatement conn)]
    (.execute stmt sql)))

(defn jdbc-exec-best-effort!
  [sql]
  (try
    (jdbc-exec! sql)
    (catch Throwable _
      nil)))

(defn ensure-live-realtime-table!
  []
  (jdbc-exec-best-effort!
   (str "ALTER TABLE \"" +live-supabase-schema+ "\".\"" +live-supabase-table+
        "\" REPLICA IDENTITY FULL"))
  (jdbc-exec-best-effort!
   (str "ALTER PUBLICATION supabase_realtime ADD TABLE \""
        +live-supabase-schema+ "\".\"" +live-supabase-table+ "\"")))

(defn live-user-sql-literal
  [s]
  (str "'" (str/replace s "'" "''") "'"))

(defn cleanup-live-user!
  [nickname]
  (jdbc-exec-best-effort!
   (str "DELETE FROM \"" +live-supabase-schema+ "\".\"" +live-supabase-table+
        "\" WHERE nickname = " (live-user-sql-literal nickname))))

(defn mutate-live-user!
  [nickname]
  (cleanup-live-user! nickname)
  (jdbc-exec!
   (str "INSERT INTO \"" +live-supabase-schema+ "\".\"" +live-supabase-table+
        "\" (nickname, password_hash, password_salt)"
        " VALUES (" (live-user-sql-literal nickname)
        ", 'live-hash', 'live-salt')"))
  (Thread/sleep 400)
  (jdbc-exec!
   (str "UPDATE \"" +live-supabase-schema+ "\".\"" +live-supabase-table+
        "\" SET is_suspended = TRUE"
        " WHERE nickname = " (live-user-sql-literal nickname)))
  (Thread/sleep 400)
  (jdbc-exec!
   (str "DELETE FROM \"" +live-supabase-schema+ "\".\"" +live-supabase-table+
        "\" WHERE nickname = " (live-user-sql-literal nickname))))

(fact:global
 {:setup [(l/rt:restart)
                  (when (live-supabase?)
                    (ensure-live-realtime-table!))]
  :teardown [(l/rt:stop)]})

(fact "js lua and python clients build equivalent websocket endpoints"

  (!.js
   (var client
        (realtime/create-client
         "https://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
          "params" {"log_level" "debug"}
          "transport" (fn [_]
                        (return {"send" (fn [_msg] (return true))
                                 "close" (fn [] (return true))
                                 "addEventListener" (fn [_event _handler]
                                                      (return true))}))}))
    (var url ((xt/x:get-key client "endpointURL")))
    [(xt/x:str-starts-with url "wss://demo.supabase.co/realtime/v1/websocket?")
     (< -1 (xt/x:str-index-of url "log_level=debug"))
     (and (== "anon-key" (xt/x:get-key client "apikey"))
          (< -1 (xt/x:str-index-of url "apikey=")))
     (< -1 (xt/x:str-index-of url "vsn=1.0.0"))])
  => [true true true true]

  (!.lua
   (var client
        (realtime/create-client
         "https://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
          "params" {"log_level" "debug"}
          "transport" (fn [_]
                        (return {"send" (fn [_msg] (return true))
                                 "close" (fn [] (return true))
                                 "addEventListener" (fn [_event _handler]
                                                      (return true))}))}))
    (var url ((xt/x:get-key client "endpointURL")))
    [(xt/x:str-starts-with url "wss://demo.supabase.co/realtime/v1/websocket?")
     (< -1 (xt/x:str-index-of url "log_level=debug"))
     (and (== "anon-key" (xt/x:get-key client "apikey"))
          (< -1 (xt/x:str-index-of url "apikey=")))
     (< -1 (xt/x:str-index-of url "vsn=1.0.0"))])
  => [true true true true]

  (!.py
   (var client
        (realtime/create-client
         "https://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
          "params" {"log_level" "debug"}
          "transport" (fn [_]
                        (return {"send" (fn [_msg] (return true))
                                 "close" (fn [] (return true))
                                 "addEventListener" (fn [_event _handler]
                                                      (return true))}))}))
    (var url ((xt/x:get-key client "endpointURL")))
    [(xt/x:str-starts-with url "wss://demo.supabase.co/realtime/v1/websocket?")
     (< -1 (xt/x:str-index-of url "log_level=debug"))
     (and (== "anon-key" (xt/x:get-key client "apikey"))
          (< -1 (xt/x:str-index-of url "apikey=")))
     (< -1 (xt/x:str-index-of url "vsn=1.0.0"))])
  => [true true true true])

(fact "js lua and python clients stay in parity for channel join and auth flows"

  (!.js
   (var handlers {})
   (var sent [])
   (var current nil)
   (var status-log [])
   (var payload-log [])
   (var find-sent-by-event
        (fn [event-name]
          (var out nil)
          (xt/for:array [raw sent]
             (var msg (realtime/default-decode raw))
            (when (and (xt/x:nil? out)
                       (== (xt/x:get-key msg "event")
                           event-name))
              (:= out msg)))
          (return out)))
   (var Transport
        (fn [url]
          (var ws {"url" url
                   "send" (fn [msg]
                            (xt/x:arr-push sent msg)
                            (return true))
                   "close" (fn [] (return true))
                   "addEventListener" (fn [event handler]
                                        (xt/x:set-key handlers event handler)
                                        (return true))})
          (xt/x:set-key ws "emit"
                        (fn [event payload]
                          ((xt/x:get-key handlers event) payload)))
          (:= current ws)
          (return ws)))
   (var client
        (realtime/create-client
         "wss://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
          "transport" Transport
          "schedule-interval" (fn [_handler _ms] (return nil))
          "clear-interval" (fn [_id] (return nil))}))
   (var channel ((xt/x:get-key client "channel") "public:messages" {"config" {"broadcast" {"self" true}}}))
   ((xt/x:get-key channel "on")
    "postgres_changes"
    {"event" "*"
     "schema" "public"
     "table" "messages"}
    (fn [payload _ref]
      (xt/x:arr-push payload-log payload)))
   ((xt/x:get-key channel "subscribe")
    (fn [status _err]
      (xt/x:arr-push status-log status)))
   ((xt/x:get-key current "emit") "open" {})
    (var join-msg (realtime/default-decode (xt/x:first sent)))
   ((xt/x:get-key current "emit")
    "message"
    {"data"
      (realtime/default-encode
        {"topic" "realtime:public:messages"
        "event" "phx_reply"
        "payload" {"status" "ok"
                   "response" {"postgres_changes"
                               [{"id" "bind-1"
                                 "event" "*"
                                 "schema" "public"
                                 "table" "messages"}]}}
        "ref" (xt/x:get-key join-msg "ref")})})
   ((xt/x:get-key current "emit")
    "message"
    {"data"
      (realtime/default-encode
       {"topic" "realtime:public:messages"
       "event" "postgres_changes"
       "payload" {"ids" ["bind-1"]
                  "data" {"schema" "public"
                          "table" "messages"
                          "commit_timestamp" "2026-05-06T00:00:00Z"
                          "type" "INSERT"
                          "record" {"id" 1
                                    "body" "hello"}
                          "old_record" {}}}
       "ref" nil})})
    ((xt/x:get-key client "setAuth") "token-2")
    ((xt/x:get-key client "removeChannel") channel)
    (var auth-msg (find-sent-by-event "access_token"))
    (var leave-msg (find-sent-by-event "phx_leave"))
    [(xt/x:get-key join-msg "topic")
     (xt/x:get-key join-msg "event")
     status-log
     (xt/x:get-key (xt/x:first payload-log) "eventType")
     (xt/x:get-key (xt/x:get-key (xt/x:first payload-log) "new") "body")
     (xt/x:get-key auth-msg "event")
     (xt/x:get-key (xt/x:get-key auth-msg "payload") "access_token")
     (xt/x:get-key leave-msg "event")
     (xt/x:len ((xt/x:get-key client "getChannels")))])
  => ["realtime:public:messages"
      "phx_join"
      ["SUBSCRIBED"]
      "INSERT"
      "hello"
      "access_token"
      "token-2"
      "phx_leave"
      0]

  (!.lua
   (var handlers {})
   (var sent [])
   (var current nil)
   (var status-log [])
   (var payload-log [])
   (var find-sent-by-event
        (fn [event-name]
          (var out nil)
          (xt/for:array [raw sent]
             (var msg (realtime/default-decode raw))
            (when (and (xt/x:nil? out)
                       (== (xt/x:get-key msg "event")
                           event-name))
              (:= out msg)))
          (return out)))
   (var Transport
        (fn [url]
          (var ws {"url" url
                   "send" (fn [msg]
                            (xt/x:arr-push sent msg)
                            (return true))
                   "close" (fn [] (return true))
                   "addEventListener" (fn [event handler]
                                        (xt/x:set-key handlers event handler)
                                        (return true))})
          (xt/x:set-key ws "emit"
                        (fn [event payload]
                          ((xt/x:get-key handlers event) payload)))
          (:= current ws)
          (return ws)))
   (var client
        (realtime/create-client
         "wss://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
          "transport" Transport
          "schedule-interval" (fn [_handler _ms] (return nil))
          "clear-interval" (fn [_id] (return nil))}))
   (var channel ((xt/x:get-key client "channel") "public:messages" {"config" {"broadcast" {"self" true}}}))
   ((xt/x:get-key channel "on")
    "postgres_changes"
    {"event" "*"
     "schema" "public"
     "table" "messages"}
    (fn [payload _ref]
      (xt/x:arr-push payload-log payload)))
   ((xt/x:get-key channel "subscribe")
    (fn [status _err]
      (xt/x:arr-push status-log status)))
   ((xt/x:get-key current "emit") "open" {})
    (var join-msg (realtime/default-decode (xt/x:first sent)))
   ((xt/x:get-key current "emit")
    "message"
    {"data"
      (realtime/default-encode
        {"topic" "realtime:public:messages"
        "event" "phx_reply"
        "payload" {"status" "ok"
                   "response" {"postgres_changes"
                               [{"id" "bind-1"
                                 "event" "*"
                                 "schema" "public"
                                 "table" "messages"}]}}
        "ref" (xt/x:get-key join-msg "ref")})})
   ((xt/x:get-key current "emit")
    "message"
    {"data"
      (realtime/default-encode
       {"topic" "realtime:public:messages"
       "event" "postgres_changes"
       "payload" {"ids" ["bind-1"]
                  "data" {"schema" "public"
                          "table" "messages"
                          "commit_timestamp" "2026-05-06T00:00:00Z"
                          "type" "INSERT"
                          "record" {"id" 1
                                    "body" "hello"}
                          "old_record" {}}}
       "ref" nil})})
    ((xt/x:get-key client "setAuth") "token-2")
    ((xt/x:get-key client "removeChannel") channel)
    (var auth-msg (find-sent-by-event "access_token"))
    (var leave-msg (find-sent-by-event "phx_leave"))
    [(xt/x:get-key join-msg "topic")
     (xt/x:get-key join-msg "event")
     status-log
     (xt/x:get-key (xt/x:first payload-log) "eventType")
     (xt/x:get-key (xt/x:get-key (xt/x:first payload-log) "new") "body")
     (xt/x:get-key auth-msg "event")
     (xt/x:get-key (xt/x:get-key auth-msg "payload") "access_token")
     (xt/x:get-key leave-msg "event")
     (xt/x:len ((xt/x:get-key client "getChannels")))])
  => ["realtime:public:messages"
      "phx_join"
      ["SUBSCRIBED"]
      "INSERT"
      "hello"
      "access_token"
      "token-2"
      "phx_leave"
      0]

  (!.py
   (var handlers {})
   (var sent [])
   (var current nil)
   (var status-log [])
   (var payload-log [])
   (var find-sent-by-event
        (fn [event-name]
          (var out nil)
          (xt/for:array [raw sent]
             (var msg (realtime/default-decode raw))
            (when (and (xt/x:nil? out)
                       (== (xt/x:get-key msg "event")
                           event-name))
              (:= out msg)))
          (return out)))
   (var Transport
        (fn [url]
          (var ws {"url" url
                   "send" (fn [msg]
                            (xt/x:arr-push sent msg)
                            (return true))
                   "close" (fn [] (return true))
                   "addEventListener" (fn [event handler]
                                        (xt/x:set-key handlers event handler)
                                        (return true))})
          (xt/x:set-key ws "emit"
                        (fn [event payload]
                          ((xt/x:get-key handlers event) payload)))
          (:= current ws)
          (return ws)))
   (var client
        (realtime/create-client
         "wss://demo.supabase.co/realtime/v1"
         {"apikey" "anon-key"
          "transport" Transport
          "schedule-interval" (fn [_handler _ms] (return nil))
          "clear-interval" (fn [_id] (return nil))}))
   (var channel ((xt/x:get-key client "channel") "public:messages" {"config" {"broadcast" {"self" true}}}))
   ((xt/x:get-key channel "on")
    "postgres_changes"
    {"event" "*"
     "schema" "public"
     "table" "messages"}
    (fn [payload _ref]
      (xt/x:arr-push payload-log payload)))
   ((xt/x:get-key channel "subscribe")
    (fn [status _err]
      (xt/x:arr-push status-log status)))
   ((xt/x:get-key current "emit") "open" {})
    (var join-msg (realtime/default-decode (xt/x:first sent)))
   ((xt/x:get-key current "emit")
    "message"
    {"data"
      (realtime/default-encode
        {"topic" "realtime:public:messages"
        "event" "phx_reply"
        "payload" {"status" "ok"
                   "response" {"postgres_changes"
                               [{"id" "bind-1"
                                 "event" "*"
                                 "schema" "public"
                                 "table" "messages"}]}}
        "ref" (xt/x:get-key join-msg "ref")})})
   ((xt/x:get-key current "emit")
    "message"
    {"data"
      (realtime/default-encode
       {"topic" "realtime:public:messages"
       "event" "postgres_changes"
       "payload" {"ids" ["bind-1"]
                  "data" {"schema" "public"
                          "table" "messages"
                          "commit_timestamp" "2026-05-06T00:00:00Z"
                          "type" "INSERT"
                          "record" {"id" 1
                                    "body" "hello"}
                          "old_record" {}}}
       "ref" nil})})
    ((xt/x:get-key client "setAuth") "token-2")
    ((xt/x:get-key client "removeChannel") channel)
    (var auth-msg (find-sent-by-event "access_token"))
    (var leave-msg (find-sent-by-event "phx_leave"))
    [(xt/x:get-key join-msg "topic")
     (xt/x:get-key join-msg "event")
     status-log
     (xt/x:get-key (xt/x:first payload-log) "eventType")
     (xt/x:get-key (xt/x:get-key (xt/x:first payload-log) "new") "body")
     (xt/x:get-key auth-msg "event")
     (xt/x:get-key (xt/x:get-key auth-msg "payload") "access_token")
     (xt/x:get-key leave-msg "event")
     (xt/x:len ((xt/x:get-key client "getChannels")))])
  => ["realtime:public:messages"
      "phx_join"
      ["SUBSCRIBED"]
      "INSERT"
      "hello"
      "access_token"
      "token-2"
      "phx_leave"
      0])

(fact "js client can consume live Supabase postgres_changes for sample-user tables"

  (if (live-supabase?)
    (let [nickname (str "live-parity-" (subs (str (java.util.UUID/randomUUID)) 0 8))
          runner   (future
                     (Thread/sleep 1500)
                     (mutate-live-user! nickname))
          out      (try
                     (notify/wait-on [:js 30000]
                       (var statuses [])
                       (var events [])
                        (var complete false)
                        (var client
                             (realtime/create-client
                              (@! (live-supabase-endpoint))
                              {"apikey" (@! (live-supabase-service-key))
                               "access-token" (@! (live-supabase-service-key))}))
                       (var channel
                            ((xt/x:get-key client "channel")
                             "xt.db.live.supabase"
                             {"config" {"broadcast" {"self" false}}}))
                       (var finish
                            (fn []
                              (when (and (not complete)
                                         (>= (xt/x:len events) 3))
                                (:= complete true)
                                ((xt/x:get-key client "removeChannel") channel)
                                (repl/notify [statuses events]))))
                       ((xt/x:get-key channel "on")
                        "postgres_changes"
                        {"event" "*"
                         "schema" (@! +live-supabase-schema+)
                         "table" (@! +live-supabase-table+)}
                        (fn [payload _ref]
                          (xt/x:arr-push events
                                         [(xt/x:get-key payload "eventType")
                                          (or (xt/x:get-key (xt/x:get-key payload "new") "nickname")
                                              (xt/x:get-key (xt/x:get-key payload "old") "nickname"))
                                          (xt/x:get-key (xt/x:get-key payload "new") "is_suspended")
                                          (xt/x:get-key (xt/x:get-key payload "old") "is_suspended")])
                          (finish)))
                       ((xt/x:get-key channel "subscribe")
                        (fn [status err]
                          (xt/x:arr-push statuses status)
                          (when (and (not complete)
                                     (== status "CHANNEL_ERROR"))
                            (:= complete true)
                            ((xt/x:get-key client "removeChannel") channel)
                            (repl/notify {"status" status
                                          "error" err}))))
                       nil)
                     (finally
                       @runner
                       (cleanup-live-user! nickname)))]
      out)
    :live-supabase-unavailable)
  => (any
      [["SUBSCRIBED"]
       [["INSERT" string? false nil]
        ["UPDATE" string? true false]
        ["DELETE" string? nil true]]]
      :live-supabase-unavailable))
