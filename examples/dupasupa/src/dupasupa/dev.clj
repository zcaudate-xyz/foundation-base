(ns dupasupa.dev
  "Runs Next.js with an owned local PostgreSQL database; teardown drops only that database."
  (:require [app.manage.integration.postgres :as database]
            [lang.core :as l]
            [lang.core.script :as script]
            [postgres.core :as pg]
            [dupasupa.db.tables :as tables]
            [dupasupa.db.rpc]
            [dupasupa.auth-fixture :as auth]
            [clojure.data.json :as json]))

(defn -main
  [& _]
  (let [auth (auth/fixture)
        db (database/database {:label "dupasupa_dev"
                               :admin-url
                               (or (System/getenv "APP_TEST_POSTGRES_URL")
                                   "postgresql://postgres:postgres@127.0.0.1:55122/postgres")})
        child (atom nil)
        cleanup
        (fn []
          (try (when-let [process @child]
                 (doseq [handle (iterator-seq (.iterator (.descendants (.toHandle process))))]
                   (.destroy handle))
                 (.destroy process)
                 (.waitFor process))
               (finally (try (binding [*ns* (the-ns 'dupasupa.dev)] (l/rt:stop))
                             (finally (try (auth/stop! auth) (finally (database/stop! db))))))))
        hook (Thread. ^Runnable cleanup)]
    (.addShutdownHook (Runtime/getRuntime) hook)
    (try (auth/start! auth)
         (database/start! db)
         (binding [*ns* (the-ns 'dupasupa.dev)]
           (script/script-test :postgres
                               {:runtime :jdbc.client
                                :config (database/config db)
                                :require '[[postgres.core :as pg] [dupasupa.db.tables :as tables]
                                           [dupasupa.db.rpc :as rpc]]})
           (l/rt:setup :postgres)
           (doseq [[index account] (map-indexed vector @(:accounts auth))]
             (pg/t:insert tables/User
               {:id (:id account)
                :handle (:handle account)
                :display-name (if (zero? index) "Alice Demo" "Bob Demo")})
             (pg/t:insert tables/Campaign
               {:id (str "10000000-0000-0000-0000-00000000000" (+ index 2))
                :owner (:id account)
                :title (str (:handle account) "'s campaign")})))
         (let [{:keys [host port user pass]} (:admin db)
               builder (doto (ProcessBuilder. ^java.util.List ["npm" "run" "dev"]) (.inheritIO))
               encode #(java.net.URLEncoder/encode % "UTF-8")]
           (.put
            (.environment builder)
            "DUPASUPA_DATABASE_URL"
            (str "postgresql://" (encode user) ":" (encode pass) "@" host ":" port "/" (:name db)))
           (.put (.environment builder) "DUPASUPA_SUPABASE_URL" (get-in auth [:config :url]))
           (.put (.environment builder) "DUPASUPA_SUPABASE_KEY" (get-in auth [:config :anon-key]))
           (.put (.environment builder)
                 "DUPASUPA_DEV_ACCOUNTS"
                 (json/write-str (auth/public-accounts auth)))
           (reset! child (.start builder))
           (println "Dupasupa: http://127.0.0.1:8000/web-main — Next.js/React + PostgreSQL")
           (let [exit (.waitFor @child)]
             (when-not (zero? exit)
               (throw (ex-info "Next.js exited unsuccessfully" {:exit exit})))))
         (finally (.removeShutdownHook (Runtime/getRuntime) hook) (cleanup) (shutdown-agents)))))
