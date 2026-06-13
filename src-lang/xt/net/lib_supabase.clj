(ns xt.net.lib-supabase
  (:require [hara.lang :as l :refer [defspec.xt]]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as str]
             [xt.lang.common-protocol :as proto :refer [defimpl.xt]]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as ut]]})

;;
;; SPECS
;;

(defspec.xt SupabaseClient
  [:xt/dict :xt/str :xt/any])

(defspec.xt SupabaseRequestOpts
  [:xt/dict :xt/str :xt/any])

(defspec.xt SupabaseBody
  [:xt/dict :xt/str :xt/any])

(defspec.xt SupabaseAuthorizeQuery
  [:xt/record
   ["redirect_to" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseVerifyQuery
  [:xt/record
   ["type" [:xt/maybe :xt/str]]
   ["token" [:xt/maybe :xt/str]]
   ["email" [:xt/maybe :xt/str]]
   ["phone" [:xt/maybe :xt/str]]
   ["redirect_to" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseSignupBody
  [:xt/record
   ["data" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["email" [:xt/maybe :xt/str]]
   ["password" [:xt/maybe :xt/str]]
   ["phone" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseAdminCreateUserBody
  [:xt/record
   ["role" [:xt/maybe :xt/str]]
   ["aud" [:xt/maybe :xt/str]]
   ["ban_duration" [:xt/maybe :xt/str]]
   ["email_confirm" [:xt/maybe :xt/bool]]
   ["email" [:xt/maybe :xt/str]]
   ["user_metadata" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["app_metadata" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["phone" [:xt/maybe :xt/str]]
   ["phone_confirm" [:xt/maybe :xt/bool]]
   ["password" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseAdminGenerateLinkBody
  [:xt/record
   ["data" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["email" [:xt/maybe :xt/str]]
   ["new_email" [:xt/maybe :xt/str]]
   ["password" [:xt/maybe :xt/str]]
   ["redirect_to" [:xt/maybe :xt/str]]
   ["type" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseOtpBody
  [:xt/record
   ["create_user" [:xt/maybe :xt/bool]]
   ["data" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["email" [:xt/maybe :xt/str]]
   ["phone" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseRecoveryBody
  [:xt/record
   ["email" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseTokenPasswordBody
  [:xt/record
   ["email" [:xt/maybe :xt/str]]
   ["password" [:xt/maybe :xt/str]]
   ["phone" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseTokenRefreshBody
  [:xt/record
   ["refresh_token" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseUserPutBody
  [:xt/record
   ["app_metadata" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["data" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["email" [:xt/maybe :xt/str]]
   ["nonce" [:xt/maybe :xt/str]]
   ["password" [:xt/maybe :xt/str]]
   ["phone" [:xt/maybe :xt/str]]])

(defspec.xt SupabaseInviteBody
  [:xt/record
   ["data" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["email" [:xt/maybe :xt/str]]])


;;
;;
;;

(defn.xt request-http
  "TODO"
  {:added "4.1"}
  [client input]
  (var #{http defaults} client)
  (var apikey  (or (xt/x:get-key input "apikey")
                   (xt/x:get-key defaults "apikey")))
  (var token   (or (xt/x:get-key input "token")
                   (xt/x:get-key defaults "token")))
  (var headers (-> {"Content-Type" "application/json"
                    "Accept" "application/json"}
                   (xt/x:obj-assign (xt/x:get-key defaults "headers"))
                   (xt/x:obj-assign (xt/x:get-key input "headers"))
                   (xt/x:obj-assign (:? token  {"Authorization" (xt/x:cat "Bearer " token)}))
                   (xt/x:obj-assign (:? apikey {"apikey" apikey} ))))
  (var http-input (-> {}
                      (xt/x:obj-assign defaults)
                      (xt/x:obj-assign input)
                      (xt/x:obj-assign {"headers" headers})))
  (var http-client (xt/x:obj-assign
                    http
                    {"defaults" (xt/x:obj-assign
                                 (or (xt/x:get-key http "defaults") {})
                                 defaults)}))
  (return
   (-> (fetch/request-http http-client http-input)
       (fetch/then-normalise))))

(defimpl.xt HttpSupabaseClient
  [http defaults]
  fetch/IHttpClient
  {fetch/request-http -/request-http})

;;
;; RPC call api
;;


(defspec.xt rpc-call-api
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseBody]
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt rpc-call
  "calls an rpc entry"
  {:added "4.1"}
  [client rpc-name data opts]
  (var path (xt/x:cat "/rest/v1/rpc/" rpc-name))
  (return
   (fetch/request-http client (xt/x:obj-assign {:path path
                                                :method "POST"
                                                :body (xt/x:json-encode (or data {}))}
                                               opts))))

(defn.xt query-table
  "TODO"
  {:added "4.1"}
  [client table-name query opts]
  (var path (xt/x:cat "/rest/v1/rpc/" table-name "?" query))
  (return
   (fetch/request-http client (xt/x:obj-assign {:path path
                                                :method "GET"}
                                               opts))))


;;
;; API
;;

(defspec.xt health
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt health
  "calls the auth health endpoint against local supabase"
  {:added "4.1"}
  [client opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/health"
                                                :method "GET"}
                                               opts))))

(defspec.xt signup
  [:fn [SupabaseClient
        SupabaseSignupBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt signup
  "signs up a user through the local auth endpoint"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/signup"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt admin-create-user
  [:fn [SupabaseClient
        SupabaseAdminCreateUserBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-create-user
  "creates and cleans up a live auth user through the admin endpoint"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/admin/users"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt admin-delete-user
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-delete-user
  "deletes a live auth user through the admin endpoint"
  {:added "4.1"}
  [client user_id opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                                                :method "DELETE"}
                                               opts))))

(defspec.xt admin-generate-link
  [:fn [SupabaseClient
        SupabaseAdminGenerateLinkBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-generate-link
  "requires a bearer token for the admin generate-link endpoint"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/admin/generate_link"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt admin-get-user
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-get-user
  "fetches a live auth user through the admin endpoint"
  {:added "4.1"}
  [client user_id opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                                                :method "GET"}
                                               opts))))

(defspec.xt admin-list-users
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-list-users
  "lists users through the local admin auth endpoint"
  {:added "4.1"}
  [client opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/admin/users"
                                                :method "GET"}
                                               opts))))

(defspec.xt admin-update-user
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-update-user
  "updates a live auth user through the admin endpoint"
  {:added "4.1"}
  [client user_id opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                                                :method "PUT"}
                                               opts))))

(defspec.xt authorize
  [:fn [SupabaseClient
        SupabaseAuthorizeQuery
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt authorize
  "returns the live OAuth provider validation failure"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/authorize?" (ut/encode-query-params data))
                                                :method "GET"}
                                               opts))))

(defspec.xt callback
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt callback
  "returns the live OAuth callback state error"
  {:added "4.1"}
  [client opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/callback"
                                                :method "GET"}
                                               opts))))

(defspec.xt invite
  [:fn [SupabaseClient
        SupabaseInviteBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt invite
  "requires authorization on the live invite endpoint"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/invite"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt logout
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt logout
  "requires authorization on the live logout endpoint"
  {:added "4.1"}
  [client opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/logout"
                                                :method "POST"}
                                               opts))))

(defspec.xt otp
  [:fn [SupabaseClient
        SupabaseOtpBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt otp
  "returns an empty success response for passwordless email OTP requests"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/otp"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt recovery
  [:fn [SupabaseClient
        SupabaseRecoveryBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt recovery
  "returns an empty success response for recovery emails"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/recover"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt settings
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt settings
  "reads the live auth settings endpoint"
  {:added "4.1"}
  [client opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/settings"
                                                :method "GET"}
                                               opts))))

(defspec.xt token-password
  [:fn [SupabaseClient
        SupabaseTokenPasswordBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt token-password
  "returns the live validation failure for password sign-in"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/token?grant_type=password"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt token-refresh
  [:fn [SupabaseClient
        SupabaseTokenRefreshBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt token-refresh
  "refreshes a live auth session"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/token?grant_type=refresh_token"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt user-get
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt user-get
  "returns a bearer-token error when the anon client has no session"
  {:added "4.1"}
  [client opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/user"
                                                :method "GET"}
                                               opts))))

(defspec.xt user-put
  [:fn [SupabaseClient
        SupabaseUserPutBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt user-put
  "returns a bearer-token error when the anon client has no session"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/user"
                                                :method "PUT"
                                                :body (xt/x:json-encode data)}
                                               opts))))

(defspec.xt verify-get
  [:fn [SupabaseClient
        SupabaseVerifyQuery
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt verify-get
  "returns the live verify validation error when the token is missing"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/verify?" (ut/encode-query-params data))
                                                :method "GET"}
                                               opts))))

(defspec.xt verify-post
  [:fn [SupabaseClient
        SupabaseVerifyQuery
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt verify-post
  "returns the live verify validation error when the token is missing"
  {:added "4.1"}
  [client data opts]
  (return
   (fetch/request-http client (xt/x:obj-assign {:path "/auth/v1/verify"
                                                :method "POST"
                                                :body (xt/x:json-encode data)}
                                               opts))))
