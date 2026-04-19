(ns js.lib.supabase
  (:require [net.http :as http]
            [std.json :as json]
            [std.lang :as l]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [xt.lang.common-notify]
            [xt.lang.common-repl]
            [std.string.case :as case])
  (:refer-clojure :exclude [not or range]))

(l/script :js
  {:import [["@supabase/supabase-js" :as [* SupabaseClient]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SupabaseClient"
                                   :tag "js"}]
  [SupabaseClient
   createClient
   [createSupabaseClient createClient]])

(f/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree []
                                   :tag "js"}]
  [[rpc             [method] {:vargs args}]
   [schema          [s] {}]
   [select          []      {:optional [cols opts]}]
   [insert          [vals]  {:optional [opts]}]
   [upsert          [vals]  {:optional [opts]}]
   [insert          [vals opts]]
   [delete          [opts]]
   [eq              [col val]]
   [neq             [col val]]
   [gt              [col val]]
   [gte             [col val]]
   [lt              [col val]]
   [lte             [col val]]
   [like            [col pattern]]
   [ilike           [col pattern]]
   [is              [col val]]
   [in              [col values]]
   [contains        [col value]]
   [containedBy     [col value]]
   [rangeGt         [col range]]
   [rangeGte        [col range]]
   [rangeLt         [col range]]
   [rangeLte        [col range]]
   [rangeAdjacent   [col range]]
   [overlays        [col value]]
   [textSerch       [col query] {:optional [opts]}]
   [match           [query]]
   [not             [col op value]]
   [or              [filters opts]]
   [order           [col opts]]
   [limit           [count opts]]
   [range           [from to  opts]]
   [single          []]
   [csv             []]
   [explai          [opts]]
   [then            [f]]
   [finally         [f]]])

(f/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["auth"]
                                   :tag "js"}]
  [[exchangeCodeForSession [authCode]]
   [fetchJwk   [kid jws]]
   [getClaims  [jwt] {:optional [options]}]
   [getSession []]
   [getUser    [] {:optional [jwt]}]
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

(f/template-entries [l/tmpl-macro {:base "SupabaseClient"
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

(f/template-entries [l/tmpl-macro {:base "SupabaseClient"
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

(f/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["auth" "admin" "mfa"]
                                   :tag "js"}]
  [[[adminDeleteFactor
     deleteFactor]      [options] {}]])

(f/template-entries [l/tmpl-macro {:base "SupabaseClient"
                                   :inst "supabase"
                                   :subtree ["functions"]
                                   :tag "js"}]
  [[[invokeFunction
     invoke]      [name] {:optional [options]}]])

(defmacro js-rpc
  "creates a js rpc call"
  {:added "4.0"}
  [function args
   &
   [{:keys [host
            key]
     :or {key  '(System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")
          host '(System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")}}]]
  (let [key  (if (collection/form? key)  (eval key)  key)
        host (if (collection/form? host) (eval host) host)
        {:keys [id]
         :static/keys [schema]} (deref (deref (resolve function)))]
    (template/$
     (xt.lang.common-notify/wait-on :js
       (. (js.lib.supabase/rpc
           (. (js.lib.supabase/createSupabaseClient
               ~host
               ~key)
               (schema ~(or schema "public")))
           ~(case/snake-case (str id))
           ~args)
           (then (xt.lang.common-repl/>notify)))))))
