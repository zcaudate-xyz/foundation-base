(ns scaffold.neon
  "Persistent cloud Neon environments with a local Foundation bridge."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [std.json :as json])
  (:import [java.net URI URLDecoder]))

(defn environment-name [environment]
  (let [selected (name (or environment :min))]
    (when-not (#{"min" "dev"} selected)
      (throw (ex-info "Neon environment must be min or dev" {:environment selected})))
    selected))

(defn script-path []
  (or (some (fn [path] (when (.isFile (io/file path)) (.getCanonicalPath (io/file path))))
            ["src-js/neon-bridge/environments.mjs"
             "ref/foundation/src-js/neon-bridge/environments.mjs"])
      (throw (ex-info "Run the Neon scaffold from Foundation or StatsTrade's root" {}))))

(defn run-command [command environment]
  (let [result (shell/sh "node" (script-path) command (environment-name environment))]
    (when-not (zero? (:exit result))
      (throw (ex-info "Neon scaffold command failed; check configuration and CLI authentication"
                      {:command command :environment (environment-name environment)})))
    (:out result)))

(defn environment-config [environment]
  (json/read (run-command "config" environment)))

(defn adapter-defaults [environment]
  (let [config (environment-config environment)]
    {"data_url" (get config "NEON_DATA_API_URL")
     "bridge_url" (get config "NEON_BRIDGE_URL")}))

(defn start-neon [environment & _] (run-command "up" environment) true)
(defn stop-neon [environment & _] (run-command "down" environment) true)

(defn jdbc-config
  "TLS JDBC config; shutdown stops only the local bridge, retaining the branch."
  ([environment] (jdbc-config environment false))
  ([environment shutdown?]
   (let [selected (environment-name environment)
         config (environment-config environment)
         uri (URI. (get config "DATABASE_URL_UNPOOLED"))
         [user password] (str/split (.getRawUserInfo uri) #":" 2)
         decode #(URLDecoder/decode (str/replace % "+" "%2B") "UTF-8")]
     {:rt/id (keyword (str "test.neon-" selected))
      :host (.getHost uri) :port (if (neg? (.getPort uri)) 5432 (.getPort uri))
      :user (decode user) :pass (decode password)
      :dbname (subs (.getPath uri) 1) :sslmode "require"
      :startup (partial start-neon selected)
      :shutdown (when shutdown? (partial stop-neon selected))})))
