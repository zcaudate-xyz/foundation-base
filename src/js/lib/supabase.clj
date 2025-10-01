(ns js.lib.supabase
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]
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

(defmacro js-rpc
  [function args
   &
   [{:keys [host
            key]
     :or {key  '(System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")
          host '(System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")}}]]
  (let [key  (if (h/form? key)  (eval key)  key)
        host (if (h/form? host) (eval host) host)
        {:keys [id]
         :static/keys [schema]} (deref (deref (resolve function)))]
    (h/$
     (xt.lang.base-notify/wait-on :js
       (. (js.lib.supabase/rpc
           (. (js.lib.supabase/createSupabaseClient
               ~host
               ~key)
              (schema ~(or schema "public")))
           ~(std.string/snake-case (str id))
           ~args)
          (then (xt.lang.base-repl/>notify)))))))

(defn api-call
  [{:keys [key
           host
           route
           method
           type
           headers
           auth]
    :or {host (System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")
         method :post
         type :anon}}
   body]
  (let [key (or key
                (case type
                  :anon (System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")
                  :service (System/getenv "DEFAULT_SUPABASE_API_KEY_SERVICE")
                  :public ""))
        headers-default (case type
                          :public {"Content-Type" "application/json"}
                          {"apikey" key
                           "Authorization" (str "Bearer " (or auth key))
                           "Content-Type" "application/json"})
        headers (merge
                 headers-default
                 headers)
        call-fn (case method
                  :delete http/delete
                  :get http/get
                  :post http/post)]
    (-> (call-fn (str host route)
                 {:headers headers
                  :body   (std.json/write body)})
        (update :body json/read)
        (select-keys [:status :body]))))

(defn api-rpc
  [{:keys [fn
           args]
    :as opts}]
  (let [{:keys [id]
         :static/keys [schema]} (deref fn)
        headers (if schema
                  {"Content-Profile" schema})
        route  (str "/rest/v1/rpc/" (str/snake-case (str id)))
        opts (merge opts
                    {:headers headers
                     :route route})]
    (-> (api-call opts args))))

(defn api-signup
  [{:keys [email
           password]
    :as body}
   & [opts]]
  (api-call (merge opts
                   {:route "/auth/v1/signup"})
            body))

(defn api-signup-delete
  [id & [opts]]
  (api-call (merge opts
                   {:method :delete
                    :type :service
                    :route (str "/auth/v1/admin/users/" id)})
            {}))

