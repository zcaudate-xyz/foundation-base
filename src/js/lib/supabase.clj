(ns js.lib.supabase
  (:require [std.lang :as l]
            [std.lib :as h]
            [net.http :as http]))

(l/script :js
  {:macro-only true
   :import [["@supabase/supabase-js" :as [* SupabaseClient]]]
   :bundle {:default   [["@supabase/supabase-js" :as [* SupabaseClient]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SupabaseClient"
                                   :tag "js"}]
  [SupabaseClient
   [createSupabaseClient createClient]])

(h/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree []
                                   :tag "js"}]
  [[rpc             [method] {:vargs args}]])

(h/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["auth"]
                                   :tag "js"}]
  [[exchangeCodeForSession [authCode]]
   [fetchJwk   [kid jws]]
   [getClaims  [jwt] {:optional [options]}]
   [getSession []]
   [getUser    [jwt]]
   [getUserIdentities []]
   [initialize []]
   [linkIdentity [credentials]]
   [onAuthStateChange [callback]]
   [reauthenticate []]
   [refreshSession [current]]
   [resend         [credentials]]
   [resetPasswordForEmail [email] {:optional [options]}]
   [setSession     [current]]
   [signInAnonymously  [credentials]]
   [signInWithIdToken  [credentials]]
   [signInWithOAuth    [credentials]]
   [signInWithOtp      [credentials]]
   [signInWithPassword [credentials]]
   [signInWithSSO      [params]]
   [signInWithSolana   [credentials]]
   [signInWithWeb3     [credentials]]
   [signOut            [] {:optional [options]}]
   [signUp             [credentials]]
   [startAutoRefresh   []]
   [stopAutoRefresh    []]
   [unlinkIdentity     [id]]
   [updateUser         [attr] {:optional [options]}]
   [verifyOtp          [params]]])

(h/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["auth" "mfa"]
                                   :tag "js"}]
  [[[mfaEnroll enroll]  [] {:optional [options]}]
   [[mfaAuthenticatorAssuranceLevel
     getAuthenticatorAssuranceLevel]  [] {}]
   [[mfaEnroll unenroll]  [] {:optional [options]}]
   [[mfaChallenge challenge]  [] {:optional [options]}]
   [[mfaVerify verify]  [] {:optional [options]}]
   [[mfaChallengeAndVerify challengeAndVerify]  [] {:optional [options]}]])

(h/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["auth" "admin"]
                                   :tag "js"}]
  [[[adminGetUser
     getUserById]     [id] {}]
   [[listUsers
     adminListUsers]  [] {:optional [options]}]
   [[adminCreateUser
     createUser]      [attrs] {}]
   [[adminDeleteUser
     deleteUser]      [id softDelete] {}]
   [[adminInviteUser
     inviteUserByEmail]  [email params]]
   [[adminGenerateLink
     generateLink]       [params]]
   [[adminUpdateUser
     getUserById]     [id params] {}]])

(h/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["auth" "admin" "mfa"]
                                   :tag "js"}]
  [[[adminDeleteFactor
     deleteFactor]      [options] {}]])

(h/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["functions"]
                                   :tag "js"}]
  [[[invokeFunction
     invoke]      [name] {:optional [options]}]])

(defmacro call-rpc
  [function args & [{:keys [host
                            key]}]]
  (let [{:keys [id]
         :static/keys [schema]} (deref (deref (resolve function)))]
    (h/$
     (xt.lang.base-notify/wait-on :js
       (. (js.lib.supabase/rpc
           (. (js.lib.supabase/createSupabaseClient
               ~(or host (System/getenv "DEFAULT_SUPABASE_API_ENDPOINT"))
               ~(or key (System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")))
              (schema ~(or schema "public")))
           ~(std.string/snake-case (str id))
           ~args)
          (then (xt.lang.base-repl/>notify)))))))

(defn call-api
  [method endpoint args]
  ((case method
     :delete http/delete
     :get http/get
     :post http/post)
   (str (System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")
        endpoint )
   {:headers {"apikey" (System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")
              "Content-Type" "application/json"}
    :body   (std.json/write
             args)}))

(defn call-admin
  [method endpoint & [args]]
  ((case method
     :delete http/delete
     :get http/get
     :post http/post)
   (str (System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")
        endpoint)
   {:headers {"apikey" (System/getenv "DEFAULT_SUPABASE_API_KEY_SERVICE")
              "Authorization" (str "Bearer "
                                   (System/getenv "DEFAULT_SUPABASE_API_KEY_SERVICE"))
              "Content-Type" "application/json"}
    :body  (std.json/write args)}))

(defn api-signup
  [{:keys [email
           password]
    :as args}]
  (call-api :post "/auth/v1/signup" args))

(defn api-signup-delete
  [id]
  (call-admin :delete
              (str "/auth/v1/admin/users/" id)
              nil))

