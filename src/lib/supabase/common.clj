(ns lib.supabase.common
  (:require [clojure.string :as str]
            [std.lib.foundation :as f]
            [lib.supabase.api :as api]
            [net.openapi.call :as call]))

(def ^:dynamic *default*)

(defn- kebab->snake
  [k]
  (-> (name k)
      (str/replace #"-" "_")))

(defn- map->snake
  [m]
  (->> m
       (map (fn [[k v]]
              [(keyword (kebab->snake k)) v]))
       (into {})))

(defn callback
  [client]
  (call/call (get api/+admin+ "callback")
             {}
             client))

(defn authorize
  [{:as query :keys [redirect-to]} client]
  (call/call (get api/+admin+ "authorize")
             {:query (map->snake query)}
             client))

(defn admin-generate-link
  [{:as body :keys [data email new-email password redirect-to type]} client]
  (call/call (get api/+admin+ "admin-generate-link")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn settings
  [client]
  (call/call (get api/+admin+ "settings")
             {}
             client))

(defn health
  [client]
  (call/call (get api/+admin+ "health")
             {}
             client))

(defn otp
  [{:as body :keys [create-user data email phone]} client]
  (call/call (get api/+admin+ "otp")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn verify-post
  [{:as body :keys [email phone redirect-to token type]} client]
  (call/call (get api/+admin+ "verify-post")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn token-password
  [{:as body :keys [email password phone]} client]
  (call/call (get api/+admin+ "token-password")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn admin-create-user
  [{:as body :keys [role aud ban-duration email-confirm email user-metadata app-metadata phone phone-confirm password]} client]
  (call/call (get api/+admin+ "admin-create-user")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn invite
  [{:as body :keys [data email]} client]
  (call/call (get api/+admin+ "invite")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn recovery
  [{:as body :keys [email]} client]
  (call/call (get api/+admin+ "recovery")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn signup
  [{:as body :keys [data email password phone]} client]
  (call/call (get api/+admin+ "signup")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn user-get
  [client]
  (call/call (get api/+admin+ "user-get")
             {}
             client))

(defn token-refresh
  [{:as body :keys [refresh-token]} client]
  (call/call (get api/+admin+ "token-refresh")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn admin-get-user
  [{:as path :keys [user-id]} client]
  (call/call (get api/+admin+ "admin-get-user")
             {:path (map->snake path)}
             client))

(defn admin-delete-user
  [{:as path :keys [user-id]} client]
  (call/call (get api/+admin+ "admin-delete-user")
             {:path (map->snake path)}
             client))

(defn user-put
  [{:as body :keys [app-metadata data email nonce password phone]} client]
  (call/call (get api/+admin+ "user-put")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn logout
  [client]
  (call/call (get api/+admin+ "logout")
             {}
             client))

(defn admin-update-user
  [{:as path :keys [user-id]} {:as body :keys [app-metadata data email nonce password phone]} client]
  (call/call (get api/+admin+ "admin-update-user")
             {:body (map->snake body) :content-type "application/json" :path (map->snake path)}
             client))

(defn admin-list-users
  [client]
  (call/call (get api/+admin+ "admin-list-users")
             {}
             client))

(defn verify-get
  [{:as query :keys [type token email phone redirect-to]} client]
  (call/call (get api/+admin+ "verify-get")
             {:query (map->snake query)}
             client))