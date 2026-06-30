(ns xt.db.node.client-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.db.node.proxy-util :as proxy-util]]})

;;
;; Client-side API for the Supabase actions registered by
;; xt.db.node.adaptor-supabase (server-side) and xt.db.node.proxy-supabase
;; (client-side proxy).
;;
;; All functions below issue substrate/request calls, so the same code works
;; against a node that has the adaptor handlers installed locally or the proxy
;; handlers installed and forwarding to a remote transport.
;;



(defn.xt sign-up
  "signs up a new user through the node"
  {:added "4.1"}
  [node service-id credentials opts]
  (return
   (proxy-util/request-client node "@xt.supabase/sign-up"
                              [service-id credentials (or opts {})]
                              opts)))

(defn.xt sign-in
  "signs in with password through the node"
  {:added "4.1"}
  [node service-id credentials opts]
  (return
   (proxy-util/request-client node "@xt.supabase/sign-in"
                              [service-id credentials (or opts {})]
                              opts)))

(defn.xt sign-out
  "signs out the current user through the node"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/sign-out"
                              [service-id (or opts {})]
                              opts)))

(defn.xt refresh
  "refreshes the current session through the node"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/refresh"
                              [service-id]
                              opts)))

(defn.xt signed-in?
  "returns whether the requested service currently has a session"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/signed-in?"
                              [service-id]
                              opts)))

(defn.xt current-session
  "returns the current session stored on the requested service"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/current-session"
                              [service-id]
                              opts)))

(defn.xt rpc-call
  "calls an rpc entry on the requested service"
  {:added "4.1"}
  [node service-id rpc-name data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/rpc-call"
                              [service-id rpc-name (or data {}) (or opts {})]
                              opts)))

(defn.xt query-table
  "queries a table on the requested service"
  {:added "4.1"}
  [node service-id table-name query opts]
  (return
   (proxy-util/request-client node "@xt.supabase/query-table"
                              [service-id table-name query (or opts {})]
                              opts)))

(defn.xt health
  "calls the auth health endpoint on the requested service"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/health"
                              [service-id (or opts {})]
                              opts)))

(defn.xt admin-create-user
  "creates a user through the admin endpoint"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/admin-create-user"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt admin-delete-user
  "deletes a user through the admin endpoint"
  {:added "4.1"}
  [node service-id user-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/admin-delete-user"
                              [service-id user-id (or opts {})]
                              opts)))

(defn.xt admin-generate-link
  "generates an admin link on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/admin-generate-link"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt admin-get-user
  "fetches a user through the admin endpoint"
  {:added "4.1"}
  [node service-id user-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/admin-get-user"
                              [service-id user-id (or opts {})]
                              opts)))

(defn.xt admin-list-users
  "lists users through the admin endpoint"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/admin-list-users"
                              [service-id (or opts {})]
                              opts)))

(defn.xt admin-update-user
  "updates a user through the admin endpoint"
  {:added "4.1"}
  [node service-id user-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/admin-update-user"
                              [service-id user-id (or opts {})]
                              opts)))

(defn.xt authorize
  "starts an OAuth authorization request"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/authorize"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt callback
  "handles an OAuth callback request"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/callback"
                              [service-id (or opts {})]
                              opts)))

(defn.xt invite
  "sends an invite on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/invite"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt otp
  "requests a passwordless OTP on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/otp"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt recovery
  "requests a recovery email on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/recovery"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt settings
  "reads auth settings on the requested service"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/settings"
                              [service-id (or opts {})]
                              opts)))

(defn.xt token-refresh
  "refreshes a session with a refresh token on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/token-refresh"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt user-get
  "fetches the current authenticated user on the requested service"
  {:added "4.1"}
  [node service-id opts]
  (return
   (proxy-util/request-client node "@xt.supabase/user-get"
                              [service-id (or opts {})]
                              opts)))

(defn.xt user-info
  "alias for user-get"
  {:added "4.1"}
  [node service-id opts]
  (return
   (-/user-get node opts)))

(defn.xt user-put
  "updates the current authenticated user on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/user-put"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt verify-get
  "verifies a token via GET on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/verify-get"
                              [service-id data (or opts {})]
                              opts)))

(defn.xt verify-post
  "verifies a token via POST on the requested service"
  {:added "4.1"}
  [node service-id data opts]
  (return
   (proxy-util/request-client node "@xt.supabase/verify-post"
                              [service-id data (or opts {})]
                              opts)))
