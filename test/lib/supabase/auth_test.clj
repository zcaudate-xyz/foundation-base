(ns lib.supabase.auth-test
  (:use code.test)
  (:require [lib.supabase.auth :refer :all]
            [lib.supabase.common :as common]))

(defn sample-client
  []
  (common/create-client "http://localhost:54321" "key-123" {}))

(def sample-token
  {"access_token" "token-1"
   "refresh_token" "refresh-1"
   "expires_in" 60
   "user" {"id" "user-1"}})

^{:refer lib.supabase.auth/has-session? :added "4.1"}
(fact "checks for a session-like token payload"
  [(has-session? sample-token)
   (has-session? {"access_token" "token-1"})]
  => [true false])

^{:refer lib.supabase.auth/token-response->session :added "4.1"}
(fact "adds expires_at when normalizing a token response"
  (-> (token-response->session sample-token)
      (select-keys ["access_token" "refresh_token" "expires_at"]))
  => #(and (= "token-1" (get % "access_token"))
           (= "refresh-1" (get % "refresh_token"))
           (integer? (get % "expires_at"))))

^{:refer lib.supabase.auth/set-session! :added "4.1"}
(fact "stores session and user state on the client"
  (let [client (sample-client)]
    (set-session! client sample-token)
    ((juxt :auth_token :refresh_token :user) (common/raw-state client)))
  => ["token-1" "refresh-1" {"id" "user-1"}])

^{:refer lib.supabase.auth/clear-session! :added "4.1"}
(fact "clears auth state"
  (let [client (sample-client)]
    (set-session! client sample-token)
    (clear-session! client)
    ((juxt :session :user :auth_token :refresh_token) (common/raw-state client)))
  => [nil nil nil nil])

^{:refer lib.supabase.auth/get-session :added "4.1"}
(fact "reads the stored session"
  (let [client (sample-client)]
    (set-session! client sample-token)
    (get-session client))
  => {:data {:session sample-token}
      :error nil})

^{:refer lib.supabase.auth/get-user :added "4.1"}
(fact "reads the stored user"
  (let [client (sample-client)]
    (set-session! client sample-token)
    (get-user client))
  => {:data {:user {"id" "user-1"}}
      :error nil})

^{:refer lib.supabase.auth/auth-result :added "4.1"}
(fact "normalizes auth results and stores session state"
  (let [client (sample-client)]
    [(-> (auth-result client sample-token)
         :data
         (get :user))
     (:auth_token (common/raw-state client))])
  => [{"id" "user-1"} "token-1"])

^{:refer lib.supabase.auth/api-signup :added "4.1"}
(fact "routes signup through the auth API"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (api-signup {"email" "a@a.com"} {:key "key"}))
  => [{:key "key" :route "/auth/v1/signup"} {"email" "a@a.com"}])

^{:refer lib.supabase.auth/api-signin :added "4.1"}
(fact "routes signin through the auth token endpoint"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (api-signin {"email" "a@a.com"} {:key "key"}))
  => [{:key "key" :route "/auth/v1/token?grant_type=password"} {"email" "a@a.com"}])

^{:refer lib.supabase.auth/api-impersonate :added "4.1"}
(fact "routes impersonation through the service auth endpoint"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (api-impersonate "user-1" {:key "key"}))
  => [{:key "key"
       :route "/auth/v1/token?grant_type=impersonate"
       :type :service}
      {"user_id" "user-1"}])

^{:refer lib.supabase.auth/sign-up :added "4.1"}
(fact "wraps api-signup and normalizes auth data"
  (let [client (sample-client)]
    (with-redefs [api-signup (fn [_body _opts]
                               {:status 200
                                :body sample-token})]
      (-> (sign-up client {"email" "a@a.com"})
          :body
          :data
          (get :session)
          (get "access_token"))))
  => "token-1")

^{:refer lib.supabase.auth/sign-in :added "4.1"}
(fact "wraps api-signin and stores auth state"
  (let [client (sample-client)]
    (with-redefs [api-signin (fn [_body _opts]
                               {:status 200
                                :body sample-token})]
      [(-> (sign-in client {"email" "a@a.com"})
           :body
           :data
           (get :user))
       (:auth_token (common/raw-state client))]))
  => [{"id" "user-1"} "token-1"])

^{:refer lib.supabase.auth/refresh-session :added "4.1"}
(fact "refreshes the stored session via the auth API"
  (let [client (sample-client)]
    (common/swap-state! client assoc :refresh_token "refresh-1")
    (with-redefs [common/api-call (fn [_opts body]
                                    {:status 200
                                     :body (assoc sample-token
                                                  "refresh_token" (get body "refresh_token"))})]
      [(-> (refresh-session client)
           :body
           :data
           (get :session)
           (get "refresh_token"))
       (:auth_token (common/raw-state client))]))
  => ["refresh-1" "token-1"])

^{:refer lib.supabase.auth/sign-out :added "4.1"}
(fact "calls logout and clears stored auth state"
  (let [client (sample-client)]
    (set-session! client sample-token)
    (with-redefs [common/api-call (fn [_opts _body]
                                    {:status 204
                                     :body nil})]
      [(sign-out client)
       (:auth_token (common/raw-state client))]))
  => [{:status 204 :body nil} nil])
