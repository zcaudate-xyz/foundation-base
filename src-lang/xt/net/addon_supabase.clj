(ns xt.net.addon-supabase
  (:require [hara.lang :as l :refer [defspec.xt]]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as str]
             [xt.lang.common-protocol :as proto :refer [defimpl.xt]]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as ut]]})

(defn.xt wrap-supabase-auth
  [handler]
  (return
   (fn [client input]
     (var #{defaults} client)
     (var apikey  (or (. input ["apikey"])
                      (. defaults ["apikey"])))
     (var token   (or (. input ["token"])
                      (. defaults ["token"])))
     (var headers (-> {"Content-Type" "application/json"
                       "Accept" "application/json"}
                      (xt/x:obj-assign (. input ["headers"]))
                      (xt/x:obj-assign (:? token  {"Authorization" (xt/x:cat "Bearer " token)}))
                      (xt/x:obj-assign (:? apikey {"apikey" apikey} ))))
     (return
      (handler client (-> {}
                          (xt/x:obj-assign input)
                          (xt/x:obj-assign {"headers" headers})))))))

(defn.xt middleware-supabase
  []
  (return
   [fetch/wrap-prepare-input
    -/wrap-supabase-auth
    fetch/wrap-normalise]))



;;
;; RPC call api
;;

(defn.xt cmd-rpc-call
  "calls an rpc entry"
  {:added "4.1"}
  [rpc-name data opts]
  (var path (xt/x:cat "/rest/v1/rpc/" rpc-name))
  (return
   (xt/x:obj-assign {:path path
                     :method "POST"
                     :body (xt/x:json-encode (or data {}))}
                    opts)))

(defn.xt cmd-query-table
  "TODO"
  {:added "4.1"}
  [table-name query opts]
  (var path (xt/x:cat "/rest/v1/" table-name "?" query))
  (return
   (xt/x:obj-assign {:path path
                     :method "GET"}
                    opts)))


(defn.xt cmd-health
  "calls the auth health endpoint against local supabase"
  {:added "4.1"}
  [opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/health"
                     :method "GET"}
                    opts)))

(defn.xt cmd-signup
  "signs up a user through the local auth endpoint"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/signup"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-admin-create-user
  "creates and cleans up a live auth user through the admin endpoint"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/admin/users"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-admin-delete-user
  "deletes a live auth user through the admin endpoint"
  {:added "4.1"}
  [user_id opts]
  (return
   (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                     :method "DELETE"}
                    opts)))

(defn.xt cmd-admin-generate-link
  "requires a bearer token for the admin generate-link endpoint"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/admin/generate_link"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-admin-get-user
  "fetches a live auth user through the admin endpoint"
  {:added "4.1"}
  [user_id opts]
  (return
   (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                     :method "GET"}
                    opts)))

(defn.xt cmd-admin-list-users
  "lists users through the local admin auth endpoint"
  {:added "4.1"}
  [opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/admin/users"
                     :method "GET"}
                    opts)))

(defn.xt cmd-admin-update-user
  "updates a live auth user through the admin endpoint"
  {:added "4.1"}
  [user_id opts]
  (return
   (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                     :method "PUT"}
                    opts)))

(defn.xt cmd-authorize
  "returns the live OAuth provider validation failure"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/authorize?" (ut/encode-query-params data))
                     :method "GET"}
                    opts)))

(defn.xt cmd-callback
  "returns the live OAuth callback state error"
  {:added "4.1"}
  [opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/callback"
                     :method "GET"}
                    opts)))

(defn.xt cmd-invite
  "requires authorization on the live invite endpoint"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/invite"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-logout
  "requires authorization on the live logout endpoint"
  {:added "4.1"}
  [opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/logout"
                     :method "POST"}
                    opts)))

(defn.xt cmd-otp
  "returns an empty success response for passwordless email OTP requests"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/otp"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-recovery
  "returns an empty success response for recovery emails"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/recover"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-settings
  "reads the live auth settings endpoint"
  {:added "4.1"}
  [opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/settings"
                     :method "GET"}
                    opts)))

(defn.xt cmd-token-password
  "returns the live validation failure for password sign-in"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/token?grant_type=password"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-token-refresh
  "refreshes a live auth session"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/token?grant_type=refresh_token"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-user-get
  "returns a bearer-token error when the anon has no session"
  {:added "4.1"}
  [opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/user"
                     :method "GET"}
                    opts)))


(defn.xt cmd-user-put
  "returns a bearer-token error when the anon has no session"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/user"
                     :method "PUT"
                     :body (xt/x:json-encode data)}
                    opts)))

(defn.xt cmd-verify-get
  "returns the live verify validation error when the token is missing"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/verify?" (ut/encode-query-params data))
                     :method "GET"}
                    opts)))

(defn.xt cmd-verify-post
  "returns the live verify validation error when the token is missing"
  {:added "4.1"}
  [data opts]
  (return
   (xt/x:obj-assign {:path "/auth/v1/verify"
                     :method "POST"
                     :body (xt/x:json-encode data)}
                    opts)))
