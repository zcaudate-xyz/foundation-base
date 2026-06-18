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
  "returns an OAuth callback response"
  {:added "4.1"}
  [client]
  (call/call (get api/+admin+ "callback")
             {}
             client))

(defn authorize
  "returns an OAuth authorize redirect response"
  {:added "4.1"}
  [{:as query :keys [redirect-to]} client]
  (call/call (get api/+admin+ "authorize")
             {:query (map->snake query)}
             client))

(defn admin-generate-link
  "generates an admin action link"
  {:added "4.1"}
  [{:as body :keys [data email new-email password redirect-to type]} client]
  (call/call (get api/+admin+ "admin-generate-link")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn settings
  "returns auth settings"
  {:added "4.1"}
  [client]
  (call/call (get api/+admin+ "settings")
             {}
             client))

(defn health
  "returns GoTrue health information"
  {:added "4.1"}
  [client]
  (call/call (get api/+admin+ "health")
             {}
             client))

(defn otp
  "initiates an OTP sign-in request"
  {:added "4.1"}
  [{:as body :keys [create-user data email phone]} client]
  (call/call (get api/+admin+ "otp")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn verify-post
  "accepts a verify request"
  {:added "4.1"}
  [{:as body :keys [email phone redirect-to token type]} client]
  (call/call (get api/+admin+ "verify-post")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn token-password
  "exchanges password for access token"
  {:added "4.1"}
  [{:as body :keys [email password phone]} client]
  (call/call (get api/+admin+ "token-password")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn admin-create-user
  "creates a user"
  {:added "4.1"}
  [{:as body :keys [role aud ban-duration email-confirm email user-metadata app-metadata phone phone-confirm password]} client]
  (call/call (get api/+admin+ "admin-create-user")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn invite
  "invites a user by email"
  {:added "4.1"}
  [{:as body :keys [data email]} client]
  (call/call (get api/+admin+ "invite")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn recovery
  "requests a password recovery"
  {:added "4.1"}
  [{:as body :keys [email]} client]
  (call/call (get api/+admin+ "recovery")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn signup
  "registers a new user"
  {:added "4.1"}
  [{:as body :keys [data email password phone]} client]
  (call/call (get api/+admin+ "signup")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn user-get
  "retrieves the current user"
  {:added "4.1"}
  [client]
  (call/call (get api/+admin+ "user-get")
             {}
             client))

(defn token-refresh
  "refreshes an access token"
  {:added "4.1"}
  [{:as body :keys [refresh-token]} client]
  (call/call (get api/+admin+ "token-refresh")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn admin-get-user
  "retrieves a user by id"
  {:added "4.1"}
  [{:as path :keys [user-id]} client]
  (call/call (get api/+admin+ "admin-get-user")
             {:path (map->snake path)}
             client))

(defn admin-delete-user
  "deletes a user by id"
  {:added "4.1"}
  [{:as path :keys [user-id]} client]
  (call/call (get api/+admin+ "admin-delete-user")
             {:path (map->snake path)}
             client))

(defn user-put
  "updates the current user"
  {:added "4.1"}
  [{:as body :keys [app-metadata data email nonce password phone]} client]
  (call/call (get api/+admin+ "user-put")
             {:body (map->snake body) :content-type "application/json"}
             client))

(defn logout
  "logs out the current user"
  {:added "4.1"}
  [client]
  (call/call (get api/+admin+ "logout")
             {}
             client))

(defn admin-update-user
  "updates a user by id"
  {:added "4.1"}
  [{:as path :keys [user-id]} {:as body :keys [app-metadata data email nonce password phone]} client]
  (call/call (get api/+admin+ "admin-update-user")
             {:body (map->snake body) :content-type "application/json" :path (map->snake path)}
             client))

(defn admin-list-users
  "lists existing users"
  {:added "4.1"}
  [client]
  (call/call (get api/+admin+ "admin-list-users")
             {}
             client))

(defn verify-get
  "accepts a verify query request"
  {:added "4.1"}
  [{:as query :keys [type token email phone redirect-to]} client]
  (call/call (get api/+admin+ "verify-get")
             {:query (map->snake query)}
             client))