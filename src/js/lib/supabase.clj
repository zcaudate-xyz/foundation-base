(ns js.lib.supabase
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:macro-only true
   :bundle {:default   [["@supabase/supabase-js" :as [* SupabaseClient]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SupabaseClient"
                                   :tag "js"}]
  [SupabaseClient
   [createSupabaseClient createClient]])

#_(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SupabaseSSR"
                                   :tag "js"}]
  [createBrowserClient
   Session])

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


(comment
  
  (!.js
    (-/signInWithPassword supabase {:email "hello"})))
