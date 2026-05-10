(ns xt.db.runtime.supabase-pull-live-test
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [hara.runtime.postgres :as pg]
            [std.config.global :as config.global]
            [std.lib.component :as component]))

(l/script- :python
  {:runtime :basic
   :require [[python.lib.client-fetch :as py-fetch]
             [xt.db.instance :as xdb]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]})

(def +live-supabase-api-env+
  ["DEFAULT_SUPABASE_API_ENDPOINT"
   "DEFAULT_SUPABASE_API_KEY_SERVICE"])

(def +live-supabase-postgres-env+
  ["DEFAULT_RT_POSTGRES_HOST"
   "DEFAULT_RT_POSTGRES_PORT"
   "DEFAULT_RT_POSTGRES_USER"
   "DEFAULT_RT_POSTGRES_PASS"
   "DEFAULT_RT_POSTGRES_DBNAME"])

(def +live-supabase-cli-env+
  ["DEFAULT_SUPABASE_CLI_ROOT"])

(def +live-supabase-required-env+
  (vec (concat +live-supabase-api-env+
               +live-supabase-postgres-env+
               +live-supabase-cli-env+)))

(def +live-supabase-schema+
  "scratch-sample-db")

(def +live-supabase-table+
  "UserAccount")

(def +live-supabase-nickname+
  "copilot_supabase_pull_live")

(def +live-user-query+
  ["UserAccount"
   {"nickname" +live-supabase-nickname+}
   ["nickname"
    "is_suspended"]])

(def -pg-
  nil)

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

(def CANARY-SUPABASE-LIVE
  (live-supabase?))

(defn live-supabase-endpoint
  []
  (let [endpoint (live-config-value "DEFAULT_SUPABASE_API_ENDPOINT")]
    (cond
      (nil? endpoint)
      nil

      (str/ends-with? endpoint "/rest/v1")
      (subs endpoint 0 (- (count endpoint) (count "/rest/v1")))

      :else
      endpoint)))

(defn live-supabase-service-key
  []
  (live-config-value "DEFAULT_SUPABASE_API_KEY_SERVICE"))

(defn live-supabase-cli-root
  []
  (or (live-config-value "DEFAULT_SUPABASE_CLI_ROOT")
      (live-config-value "SUPABASE_CLI_ROOT")))

(def +live-supabase-config+
  {"::" "db.supabase"
   "base_url" (live-supabase-endpoint)
   "schema_name" +live-supabase-schema+
   "api_key" (live-supabase-service-key)
   "auth_token" (live-supabase-service-key)})

(defn live-postgres-runtime-config
  []
  {:host (live-config-value "DEFAULT_RT_POSTGRES_HOST")
   :port (parse-long (str (live-config-value "DEFAULT_RT_POSTGRES_PORT")))
   :user (live-config-value "DEFAULT_RT_POSTGRES_USER")
   :pass (live-config-value "DEFAULT_RT_POSTGRES_PASS")
   :dbname (live-config-value "DEFAULT_RT_POSTGRES_DBNAME")
   :startup {:args ["supabase" "start"]
             :root (live-supabase-cli-root)
             :ignore-errors true}
   :teardown {:args ["supabase" "stop"]
              :root (live-supabase-cli-root)
              :ignore-errors true}})

(defn pg-exec!
  [sql]
  (pg/raw-eval -pg- sql))

(defn pg-exec-best-effort!
  [sql]
  (try
    (pg-exec! sql)
    (catch Throwable _
      nil)))

(defn live-user-sql-literal
  [s]
  (str "'" (str/replace s "'" "''") "'"))

(defn cleanup-live-user!
  [nickname]
  (pg-exec-best-effort!
   (str "DELETE FROM \"" +live-supabase-schema+ "\".\"" +live-supabase-table+
        "\" WHERE nickname = " (live-user-sql-literal nickname))))

(defn setup-live-user!
  [nickname]
  (cleanup-live-user! nickname)
  (pg-exec!
   (str "INSERT INTO \"" +live-supabase-schema+ "\".\"" +live-supabase-table+
        "\" (nickname, password_hash, password_salt, is_suspended)"
        " VALUES (" (live-user-sql-literal nickname)
        ", 'live-hash', 'live-salt', TRUE)"))
  (Thread/sleep 200))

(fact:global
  {:setup [(l/rt:restart)
           (do (when CANARY-SUPABASE-LIVE
                 (alter-var-root #'-pg-
                                 (constantly (pg/rt-postgres (live-postgres-runtime-config))))
                 (cleanup-live-user! +live-supabase-nickname+))
                true)]
   :teardown [(do (when CANARY-SUPABASE-LIVE
                    (cleanup-live-user! +live-supabase-nickname+)
                    (component/stop -pg-)
                    (alter-var-root #'-pg- (constantly nil)))
                  true)
              (l/rt:stop)]})

^{:refer xt.db.instance/db-create :added "4.1.3"}
(fact "creates a live db.supabase instance backed by a python fetch client"

  (if CANARY-SUPABASE-LIVE
    (!.py
     (var instance (xt/x:obj-clone (@! +live-supabase-config+)))
     (var client (py-fetch/client (xt/x:obj-clone (@! +live-supabase-config+))))
     (xt/x:set-key instance "client" client)
     (var db (xdb/db-create instance nil nil {}))
     [(. db ["::"])
      (fetch/client? (. (. db ["instance"]) ["client"]))
      (. (. db ["instance"]) ["schema_name"])])
    :supabase-live-unavailable)
  => (any ["db.supabase" true "scratch-sample-db"]
          :supabase-live-unavailable))

^{:refer xt.db.instance/db-pull-sync :added "4.1.3"}
(fact "pulls seeded live data through db.supabase"

  (if CANARY-SUPABASE-LIVE
    (do
      (setup-live-user! +live-supabase-nickname+)
      (try
        (!.py
         (var instance (xt/x:obj-clone (@! +live-supabase-config+)))
         (var client (py-fetch/client (xt/x:obj-clone (@! +live-supabase-config+))))
         (xt/x:set-key instance "client" client)
         (var db (xdb/db-create instance nil nil {}))
         (xdb/db-pull-sync db nil (@! +live-user-query+)))
        (finally
          (cleanup-live-user! +live-supabase-nickname+))))
    :supabase-live-unavailable)
  => (any [{"nickname" "copilot_supabase_pull_live"
            "is_suspended" true}]
          :supabase-live-unavailable))
