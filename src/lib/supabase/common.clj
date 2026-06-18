(ns lib.supabase.common
  (:require [clojure.string :as str]
            [std.lib.foundation :as f]
            [lib.supabase.api :as api]
            [net.openapi.call :as call]))

(def ^:dynamic *default*)

(defn callback
  [client]
  (call/call (get lib.supabase.api/+admin+ "callback")
             {}
             client))

(defn authorize
  [{:as query :keys [redirect-to]} client]
  (call/call (get lib.supabase.api/+admin+ "authorize")
             {:query query}
             client))

(defn admin-generate-link
  [{:as body :keys [data email new-email password redirect-to type]} client]
  (call/call (get lib.supabase.api/+admin+ "admin-generate-link")
             {:body body}
             client))

(defn settings
  [client]
  (call/call (get lib.supabase.api/+admin+ "settings")
             {}
             client))

(defn health
  [client]
  (call/call (get lib.supabase.api/+admin+ "health")
             {}
             client))

(defn otp
  [{:as body :keys [create-user data email phone]} client]
  (call/call (get lib.supabase.api/+admin+ "otp")
             {:body body}
             client))

(defn verify-post
  [{:as body :keys [email phone redirect-to token type]} client]
  (call/call (get lib.supabase.api/+admin+ "verify-post")
             {:body body}
             client))

(defn token-password
  [{:as body :keys [email password phone]} client]
  (call/call (get lib.supabase.api/+admin+ "token-password")
             {:body body}
             client))

(defn admin-create-user
  [{:as body :keys [role aud ban-duration email-confirm email user-metadata app-metadata phone phone-confirm password]} client]
  (call/call (get lib.supabase.api/+admin+ "admin-create-user")
             {:body body}
             client))

(defn invite
  [{:as body :keys [data email]} client]
  (call/call (get lib.supabase.api/+admin+ "invite")
             {:body body}
             client))

(defn recovery
  [{:as body :keys [email]} client]
  (call/call (get lib.supabase.api/+admin+ "recovery")
             {:body body}
             client))

(defn signup
  [{:as body :keys [data email password phone]} client]
  (call/call (get lib.supabase.api/+admin+ "signup")
             {:body body}
             client))

(defn user-get
  [client]
  (call/call (get lib.supabase.api/+admin+ "user-get")
             {}
             client))

(defn token-refresh
  [{:as body :keys [refresh-token]} client]
  (call/call (get lib.supabase.api/+admin+ "token-refresh")
             {:body body}
             client))

(defn admin-get-user
  [{:as path :keys [user-id]} client]
  (call/call (get lib.supabase.api/+admin+ "admin-get-user")
             {:path path}
             client))

(defn admin-delete-user
  [{:as path :keys [user-id]} client]
  (call/call (get lib.supabase.api/+admin+ "admin-delete-user")
             {:path path}
             client))

(defn user-put
  [{:as body :keys [app-metadata data email nonce password phone]} client]
  (call/call (get lib.supabase.api/+admin+ "user-put")
             {:body body}
             client))

(defn logout
  [client]
  (call/call (get lib.supabase.api/+admin+ "logout")
             {}
             client))

(defn admin-update-user
  [{:as path :keys [user-id]} client]
  (call/call (get lib.supabase.api/+admin+ "admin-update-user")
             {:path path}
             client))

(defn admin-list-users
  [client]
  (call/call (get lib.supabase.api/+admin+ "admin-list-users")
             {}
             client))

(defn verify-get
  [{:as query :keys [type token email phone redirect-to]} client]
  (call/call (get lib.supabase.api/+admin+ "verify-get")
             {:query query}
             client))