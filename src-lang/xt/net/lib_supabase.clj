(ns xt.net.lib-supabase
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as str]
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

(defspec.xt create-client
  [:fn [:xt/any :xt/str :xt/str :xt/bool :xt/str :xt/str]
   SupabaseClient])

(defspec.xt request
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defspec.xt request-get
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defspec.xt request-json
  [:fn [SupabaseClient
        :xt/str
        :xt/str
        [:xt/maybe SupabaseBody]
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])


;;
;; HELPERS
;;

(defn.xt create-client
  [methods host port secured basepath apikey]
  (return
   (fetch/create-base "net.superbase"
                      methods
                      {:secured secured
                       :host host
                       :port port
                       :headers {"apikey" apikey
                                 "Content-Type" "application/json"
                                 "Accept" "application/json"}
                       :basepath ""})))

(defn.xt request
  [client opts]
  (:= opts (or opts {}))
  (var #{token
         apikey} opts)
  (var headers (xt/x:obj-clone (or (xt/x:get-key opts "headers") {})))
  (when token
    (xt/x:set-key headers "Authorization" (xt/x:cat "Bearer " token)))
  (when apikey
    (xt/x:set-key headers "apikey" apikey))
  (return
   (-> (fetch/request-http client (xt/x:obj-assign opts
                                                   {:headers headers}))
       (fetch/then-normalise))))

(defn.xt request-get
  [client path opts]
  (return
   (-/request client (xt/x:obj-assign {:path path
                                       :method "GET"}
                                      opts))))

(defn.xt request-json
  [client path method data opts]
  (return
   (-/request client (xt/x:obj-assign {:path path
                                       :method method
                                       :body (xt/x:json-encode data)}
                                      opts))))

(defspec.xt rpc-call-api
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseBody]
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt rpc-call-api
  [client rpc_name data opts]
  (var path (xt/x:cat "/rest/v1/rpc/" (xt/x:str-replace rpc_name "-" "_")))
  (return
   (-/request-json client path "POST" (or data {}) opts)))

;;
;; API
;;

(defspec.xt health
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt health
  [client opts]
  (return
   (-/request-get client "/auth/v1/health" opts)))

(defspec.xt signup
  [:fn [SupabaseClient
        SupabaseSignupBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt signup
  [client data opts]
  (return
   (-/request-json client "/auth/v1/signup" "POST" data opts)))

(defspec.xt admin-create-user
  [:fn [SupabaseClient
        SupabaseAdminCreateUserBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-create-user
  [client data opts]
  (return
   (-/request-json client "/auth/v1/admin/users" "POST" data opts)))

(defspec.xt admin-delete-user
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-delete-user
  [client user_id opts]
  (return
   (-/request client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                                       :method "DELETE"}
                                      opts))))

(defspec.xt admin-generate-link
  [:fn [SupabaseClient
        SupabaseAdminGenerateLinkBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-generate-link
  [client data opts]
  (return
   (-/request-json client "/auth/v1/admin/generate_link" "POST" data opts)))

(defspec.xt admin-get-user
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-get-user
  [client user_id opts]
  (return
   (-/request-get client (xt/x:cat "/auth/v1/admin/users/" user_id) opts)))

(defspec.xt admin-list-users
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-list-users
  [client opts]
  (return
   (-/request-get client "/auth/v1/admin/users" opts)))

(defspec.xt admin-update-user
  [:fn [SupabaseClient
        :xt/str
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt admin-update-user
  [client user_id opts]
  (return
   (-/request client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/users/" user_id)
                                       :method "PUT"}
                                      opts))))

(defspec.xt authorize
  [:fn [SupabaseClient
        SupabaseAuthorizeQuery
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt authorize
  [client data opts]
  (return
   (-/request-get client (xt/x:cat "/auth/v1/authorize?" (ut/encode-query-params data)) opts)))

(defspec.xt callback
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt callback
  [client opts]
  (return
   (-/request-get client "/auth/v1/callback" opts)))

(defspec.xt invite
  [:fn [SupabaseClient
        SupabaseInviteBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt invite
  [client data opts]
  (return
   (-/request-json client "/auth/v1/invite" "POST" data opts)))

(defspec.xt logout
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt logout
  [client opts]
  (return
   (-/request client (xt/x:obj-assign {:path "/auth/v1/logout"
                                       :method "POST"}
                                      opts))))

(defspec.xt otp
  [:fn [SupabaseClient
        SupabaseOtpBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt otp
  [client data opts]
  (return
   (-/request-json client "/auth/v1/otp" "POST" data opts)))

(defspec.xt recovery
  [:fn [SupabaseClient
        SupabaseRecoveryBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt recovery
  [client data opts]
  (return
   (-/request-json client "/auth/v1/recover" "POST" data opts)))

(defspec.xt settings
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt settings
  [client opts]
  (return
   (-/request-get client "/auth/v1/settings" opts)))

(defspec.xt token-password
  [:fn [SupabaseClient
        SupabaseTokenPasswordBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt token-password
  [client data opts]
  (return
   (-/request-json client
                   "/auth/v1/token?grant_type=password"
                   "POST" data opts)))

(defspec.xt token-refresh
  [:fn [SupabaseClient
        SupabaseTokenRefreshBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt token-refresh
  [client data opts]
  (return
   (-/request-json client
                   "/auth/v1/token?grant_type=refresh_token"
                   "POST" data opts)))

(defspec.xt user-get
  [:fn [SupabaseClient
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt user-get
  [client opts]
  (return
   (-/request-get client "/auth/v1/user" opts)))

(defspec.xt user-put
  [:fn [SupabaseClient
        SupabaseUserPutBody
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt user-put
  [client data opts]
  (return
   (-/request-json client "/auth/v1/user" "PUT" data opts)))

(defspec.xt verify-get
  [:fn [SupabaseClient
        SupabaseVerifyQuery
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt verify-get
  [client data opts]
  (return
   (-/request-get client (xt/x:cat "/auth/v1/verify?" (ut/encode-query-params data)) opts)))

(defspec.xt verify-post
  [:fn [SupabaseClient
        SupabaseVerifyQuery
        [:xt/maybe SupabaseRequestOpts]]
   :xt/promise])

(defn.xt verify-post
  [client data opts]
  (return
   (-/request-json client "/auth/v1/verify" "POST" data opts)))
