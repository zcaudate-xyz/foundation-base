(ns dupasupa.verification
  "Isolated local DB/Auth fixtures for the shared page/model verification workbench."
  (:require [app.manage.integration.postgres :as database]
            [dupasupa.auth-fixture :as auth]
            [clojure.data.json :as json]
            [clojure.java.io :as io])
  (:import [java.sql DriverManager]))

(def ^:dynamic *fixture* nil)
(def CAMPAIGN "10000000-0000-0000-0000-000000000002")

(defn with-fixture
  [run]
  (let [db (database/database {:label "dupasupa_verify"
                               :admin-url
                               (or (System/getenv "APP_TEST_POSTGRES_URL")
                                   "postgresql://postgres:postgres@127.0.0.1:55122/postgres")})
        users (auth/fixture)]
    (try
      (database/start! db)
      (auth/start! users)
      (database/execute! db (slurp "generated/database/schema.sql"))
      (let [{:keys [host port user pass]} (:admin db)
            url (str "postgresql://" host ":" port "/" (:name db))]
        (with-open [connection (DriverManager/getConnection (str "jdbc:" url) user pass)]
          (doseq [[index account] (map-indexed vector @(:accounts users))]
            (with-open
              [statement
               (.prepareStatement
                connection
                "INSERT INTO dupasupa_type.\"User\" (id,handle,display_name) VALUES (?::uuid,?,?)")]
              (.setString statement 1 (:id account))
              (.setString statement 2 (:handle account))
              (.setString statement 3 (if (zero? index) "Alice Demo" "Bob Demo"))
              (.executeUpdate statement)))
          (with-open
            [statement
             (.prepareStatement
              connection
              "INSERT INTO dupasupa_type.\"Campaign\" (id,owner_id,title) VALUES (?::uuid,?::uuid,?)")]
            (.setString statement 1 CAMPAIGN)
            (.setString statement 2 (:id (first @(:accounts users))))
            (.setString statement 3 "Welcome campaign")
            (.executeUpdate statement)))
        (binding [*fixture* {:database db
                             :env {"DUPASUPA_DATABASE_URL"
                                   (str "postgresql://" (java.net.URLEncoder/encode user "UTF-8")
                                        ":" (java.net.URLEncoder/encode pass "UTF-8")
                                        "@" host
                                        ":" port
                                        "/" (:name db))
                                   "DUPASUPA_SUPABASE_URL" (get-in users [:config :url])
                                   "DUPASUPA_SUPABASE_KEY" (get-in users [:config :anon-key])
                                   "DUPASUPA_VERIFY_ACTORS"
                                   (json/write-str (into {}
                                                         (for [account @(:accounts users)]
                                                           [(:handle account)
                                                            (select-keys account [:id :token])])))
                                   "DUPASUPA_PAGE_VERIFY" "1"}}]
          (run)))
      (finally (try (auth/stop! users) (finally (database/stop! db)))))))

(defn execute
  [request]
  (if-not *fixture*
    (with-fixture #(execute request))
    (let [out (java.io.File/createTempFile "dupasupa-verify-" ".out")
          err (java.io.File/createTempFile "dupasupa-verify-" ".err")]
      (try (let [builder (doto (ProcessBuilder. ^java.util.List
                                                ["node" "generated/verification/worker.mjs"])
                           (.redirectOutput out)
                           (.redirectError err))]
             (doseq [[key value] (:env *fixture*)] (.put (.environment builder) key value))
             (let [process (.start builder)]
               (with-open [writer (io/writer (.getOutputStream process))]
                 (.write writer (json/write-str request)))
               (when-not (.waitFor process 30 java.util.concurrent.TimeUnit/SECONDS)
                 (.destroyForcibly process)
                 (throw (ex-info "Page verification timed out" {})))
               (when-not (zero? (.exitValue process))
                 (throw (ex-info "Page verification worker failed" {:stderr (slurp err)})))
               (json/read-str (slurp out) :key-fn keyword)))
           (finally (.delete out) (.delete err))))))
