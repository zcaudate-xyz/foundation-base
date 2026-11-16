(ns app.manage.integration.postgres
  "Owns one disposable database on a local Supabase/PostgreSQL server, never an application DB."
  (:require [clojure.string :as str])
  (:import [java.net URI URLDecoder]
           [java.sql DriverManager]))

(defn- admin-config
  [url]
  (let [uri (URI. url)
        credentials (.getRawUserInfo uri)]
    (when-not (and (#{"postgresql" "postgres"} (.getScheme uri))
                   (#{"localhost" "127.0.0.1" "[::1]"} (.getHost uri))
                   (= "/postgres" (.getPath uri))
                   credentials
                   (nil? (.getRawQuery uri))
                   (nil? (.getRawFragment uri)))
      (throw (ex-info "Integration needs a loopback /postgres administrator URL without overrides"
                      {})))
    (let [[user pass] (str/split credentials #":" 2)
          decode #(URLDecoder/decode (str/replace (or % "") "+" "%2B") "UTF-8")]
      {:host (.getHost uri)
       :port (if (neg? (.getPort uri)) 5432 (.getPort uri))
       :user (decode user)
       :pass (decode pass)})))

(defn database
  "Describes a fresh database without connecting. The name is generated, never caller supplied."
  [{:keys [admin-url label] :or {label "app"}}]
  (when-not (re-matches #"[a-z][a-z0-9_]{0,15}" label)
    (throw (ex-info "Invalid integration database label" {})))
  (let [name (str "app_test_" label "_" (str/replace (str (java.util.UUID/randomUUID)) "-" ""))]
    {:admin (admin-config admin-url) :name name :created? (atom false)}))

(defn config
  [database]
  (assoc (:admin database) :vendor :postgresql :dbname (:name database) :sslmode "disable"))

(defn client-config
  [database]
  (let [{:keys [host port user pass]} (:admin database)]
    {:host host :port port :user user :password pass :database (:name database)}))

(defn- connect
  [database dbname]
  (Class/forName "org.postgresql.Driver")
  (let [{:keys [host port user pass]} (:admin database)]
    (DriverManager/getConnection (str "jdbc:postgresql://" host ":" port "/" dbname) user pass)))

(defn- owned-name!
  [database]
  (when-not (and (#{"localhost" "127.0.0.1" "[::1]"} (get-in database [:admin :host]))
                 (re-matches #"app_test_[a-z][a-z0-9_]{0,15}_[a-f0-9]{32}" (:name database)))
    (throw (ex-info "Refusing an unowned integration database" {})))
  (:name database))

(defn execute!
  "Runs fixture bootstrap SQL only inside the database this descriptor created."
  [database sql]
  (owned-name! database)
  (when-not @(:created? database) (throw (ex-info "Start the owned database before bootstrap" {})))
  (with-open [db (connect database (:name database))
              statement (.createStatement db)]
    (.execute statement sql)))

(defn start!
  [database]
  (owned-name! database)
  (when @(:created? database)
    (throw (ex-info "Integration database already exists" {})))
  (with-open [db (connect database "postgres")
              statement (.createStatement db)]
    (.execute statement (str "CREATE DATABASE \"" (:name database) "\" TEMPLATE template0")))
  (reset! (:created? database) true)
  database)

(defn stop!
  [database]
  (owned-name! database)
  (when @(:created? database)
    (with-open [db (connect database "postgres")
                statement (.createStatement db)]
      (.execute statement (str "DROP DATABASE \"" (:name database) "\" WITH (FORCE)")))
    (reset! (:created? database) false))
  true)

(defn resource
  [database]
  {:id :database :start (fn [_] (start! database)) :stop (fn [_ _] (stop! database))})

(defn exists?
  [database]
  (owned-name! database)
  (with-open [db (connect database "postgres")
              query (.prepareStatement db
                                       "SELECT EXISTS(SELECT 1 FROM pg_database WHERE datname=?)")]
    (.setString query 1 (:name database))
    (with-open [result (.executeQuery query)]
      (.next result)
      (.getBoolean result 1))))
