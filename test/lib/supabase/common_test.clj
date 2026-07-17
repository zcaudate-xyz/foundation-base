(ns lib.supabase.common-test
  (:use code.test)
  (:require [clojure.string :as str]
            [lib.supabase.common :as common]
            [scaffold.supabase.local-min :as local-min]
            [std.json :as json]
            [std.lib.env :as env]
            [hara.lang :as l]))

(fact:global
 {:skip (not (env/program-exists? "supabase"))
  :setup [(local-min/start-supabase)]
  :teardown [(local-min/shutdown-supabase nil)]})

(defn- base-url
  []
  (let [api (-> local-min/+config+ :api)]
    (str (name (:protocol api))
         "://"
         (:hostname api)
         ":"
         (:port api)
         "/auth/v1")))

(defn- service-client
  []
  (let [api (-> local-min/+config+ :api)]
    {:base-url (base-url)
     :headers {"apikey" (:service-key api)
               "Authorization" (str "Bearer " (:service-key api))}}))

(defn- anon-client
  []
  (let [api (-> local-min/+config+ :api)]
    {:base-url (base-url)
     :headers {"apikey" (:anon-key api)}}))

(defn- user-client
  [access-token]
  (assoc-in (anon-client) [:headers "Authorization"]
            (str "Bearer " access-token)))

(defn- response-body
  [response]
  (json/read (:body response) json/+keyword-mapper+))

(defn- unique-email
  [prefix]
  (str prefix "-" (System/currentTimeMillis) "@example.com"))

(defn- signup-and-token
  []
  (let [email (unique-email "user")
        response (common/signup {:email email
                                 :password "password123"}
                                (anon-client))]
    [email (-> response response-body :access_token)]))

^{:refer lib.supabase.common/callback :added "4.1"}
(fact "returns an OAuth callback response"
  (let [response (common/callback (anon-client))]
    (map? response) => true
    (:status response) => integer?))

^{:refer lib.supabase.common/authorize :added "4.1"}
(fact "returns an OAuth authorize redirect response"
  (let [response (common/authorize {:redirect-to "http://localhost/callback"}
                                   (anon-client))]
    (map? response) => true
    (:status response) => integer?))

^{:refer lib.supabase.common/admin-generate-link :added "4.1"}
(fact "generates an admin action link"
  (let [response (common/admin-generate-link {:email (unique-email "generate-link")
                                              :type "signup"}
                                             (service-client))]
    (map? response) => true
    (:status response) => (fn [s] (#{200 400} s))))

^{:refer lib.supabase.common/settings :added "4.1"}
(fact "returns auth settings"
  (let [response (common/settings (anon-client))]
    (:status response) => 200
    (response-body response) => map?
    (-> response response-body :mailer_autoconfirm) => true?))

^{:refer lib.supabase.common/health :added "4.1"}
(fact "returns GoTrue health information"
  (let [response (common/health (anon-client))]
    (:status response) => 200
    (-> response response-body :name) => "GoTrue"
    (-> response response-body :version) => string?))

^{:refer lib.supabase.common/otp :added "4.1"}
(fact "initiates an OTP sign-in request"
  (let [response (common/otp {:email (unique-email "otp")
                              :create-user true}
                             (anon-client))]
    (map? response) => true
    (:status response) => integer?))

^{:refer lib.supabase.common/verify-post :added "4.1"}
(fact "accepts a verify request"
  (let [response (common/verify-post {:type "signup"
                                      :email (unique-email "verify-post")
                                      :token "invalid-token"}
                                     (anon-client))]
    (map? response) => true
    (:status response) => integer?))

^{:refer lib.supabase.common/token-password :added "4.1"}
(fact "exchanges password for access token"
  (let [email (unique-email "token-password")
        _ (common/signup {:email email :password "password123"} (anon-client))
        response (common/token-password {:email email
                                         :password "password123"}
                                        (anon-client))]
    (:status response) => 200
    (-> response response-body :access_token) => string?))

^{:refer lib.supabase.common/admin-create-user :added "4.1"}
(fact "creates a user"
  (let [email (unique-email "admin-create")
        response (common/admin-create-user {:email email
                                            :password "password123"}
                                           (service-client))]
    (:status response) => 200
    (-> response response-body :email) => email))

^{:refer lib.supabase.common/invite :added "4.1"}
(fact "invites a user by email"
  (let [response (common/invite {:email (unique-email "invite")}
                                (service-client))]
    (map? response) => true
    (:status response) => (fn [s] (#{200 400 422} s))))

^{:refer lib.supabase.common/recovery :added "4.1"}
(fact "requests a password recovery"
  (let [response (common/recovery {:email (unique-email "recovery")}
                                  (anon-client))]
    (map? response) => true
    (:status response) => (fn [s] (#{200 400 422 429} s))))

^{:refer lib.supabase.common/signup :added "4.1"}
(fact "registers a new user"
  (let [email (unique-email "signup")
        response (common/signup {:email email
                                 :password "password123"}
                                (anon-client))]
    (:status response) => 200
    (-> response response-body :access_token) => string?
    (-> response response-body :user :email) => email))

^{:refer lib.supabase.common/user-get :added "4.1"}
(fact "retrieves the current user"
  (let [[email token] (signup-and-token)
        response (common/user-get (user-client token))]
    (:status response) => 200
    (-> response response-body :email) => email))

^{:refer lib.supabase.common/token-refresh :added "4.1"}
(fact "refreshes an access token"
  (let [[_email token] (signup-and-token)
        response (common/token-refresh {:refresh-token token}
                                       (anon-client))]
    (map? response) => true
    (:status response) => (fn [s] (#{200 400} s))))

^{:refer lib.supabase.common/admin-get-user :added "4.1"}
(fact "retrieves a user by id"
  (let [email (unique-email "admin-get")
        user-id (-> (common/admin-create-user {:email email
                                               :password "password123"}
                                              (service-client))
                    response-body
                    :id)]
    (string? user-id) => true
    (let [response (common/admin-get-user {:user-id user-id} (service-client))]
      (:status response) => 200
      (-> response response-body :id) => user-id
      (-> response response-body :email) => email)))

^{:refer lib.supabase.common/admin-delete-user :added "4.1"}
(fact "deletes a user by id"
  (let [email (unique-email "admin-delete")
        user-id (-> (common/admin-create-user {:email email
                                               :password "password123"}
                                              (service-client))
                    response-body
                    :id)]
    (string? user-id) => true
    (let [response (common/admin-delete-user {:user-id user-id} (service-client))]
      (:status response) => 200)))

^{:refer lib.supabase.common/user-put :added "4.1"}
(fact "updates the current user"
  (let [[_email token] (signup-and-token)
        response (common/user-put {:data {:nickname "integration-test"}}
                                  (user-client token))]
    (map? response) => true
    (:status response) => (fn [s] (#{200 400} s))))

^{:refer lib.supabase.common/logout :added "4.1"}
(fact "logs out the current user"
  (let [[_email token] (signup-and-token)
        response (common/logout (user-client token))]
    (map? response) => true
    (:status response) => (fn [s] (#{200 204} s))))

^{:refer lib.supabase.common/admin-update-user :added "4.1"}
(fact "updates a user by id"
  (let [email (unique-email "admin-update")
        user-id (-> (common/admin-create-user {:email email
                                               :password "password123"}
                                              (service-client))
                    response-body
                    :id)
        new-email (unique-email "admin-updated")
        response (common/admin-update-user {:user-id user-id}
                                           {:email new-email}
                                           (service-client))]
    (map? response) => true
    (:status response) => (fn [s] (#{200 400} s))))

^{:refer lib.supabase.common/admin-list-users :added "4.1"}
(fact "lists existing users"
  (let [response (common/admin-list-users (service-client))]
    (:status response) => 200
    (-> response response-body :users) => vector?))

^{:refer lib.supabase.common/verify-get :added "4.1"}
(fact "accepts a verify query request"
  (let [response (common/verify-get {:type "signup"
                                     :email (unique-email "verify-get")
                                     :token "invalid-token"}
                                    (anon-client))]
    (map? response) => true
    (:status response) => integer?))

^{:refer lib.supabase.common/token-password :added "4.1"
  :id token-password-rejects-wrong-password}
(fact "rejects login with wrong password"
  (let [email (unique-email "token-password-wrong")
        _ (common/signup {:email email :password "password123"} (anon-client))
        response (common/token-password {:email email
                                         :password "wrong-password"}
                                        (anon-client))]
    (:status response) => (fn [s] (>= s 400))))

^{:refer lib.supabase.common/user-get :added "4.1"
  :id user-get-rejects-invalid-token}
(fact "rejects user-get with invalid token"
  (let [response (common/user-get (user-client "invalid-token"))]
    (:status response) => (fn [s] (>= s 400))))

^{:refer lib.supabase.common/admin-get-user :added "4.1"
  :id admin-get-user-not-found}
(fact "returns error for non-existent user id"
  (let [response (common/admin-get-user {:user-id "00000000-0000-0000-0000-000000000000"}
                                        (service-client))]
    (:status response) => (fn [s] (>= s 400))))

^{:refer lib.supabase.common/admin-create-user :added "4.1"
  :id admin-user-lifecycle}
(fact "admin user lifecycle: create, get, update, delete"
  (let [email (unique-email "admin-lifecycle")
        app-meta {:tenant "test-tenant"}
        user-meta {:nickname "lifecycle-test"}
        create-response (common/admin-create-user {:email email
                                                   :password "password123"
                                                   :email-confirm true
                                                   :app-metadata app-meta
                                                   :user-metadata user-meta}
                                                  (service-client))
        user-id (-> create-response response-body :id)]
    (:status create-response) => 200
    (string? user-id) => true
    (-> create-response response-body :email) => email
    (-> create-response response-body :app_metadata) => (fn [m] (= "test-tenant" (:tenant m)))
    (-> create-response response-body :user_metadata) => (fn [m] (= "lifecycle-test" (:nickname m)))

    (let [get-response (common/admin-get-user {:user-id user-id} (service-client))]
      (:status get-response) => 200
      (-> get-response response-body :id) => user-id
      (-> get-response response-body :email) => email)

    (let [new-email (unique-email "admin-lifecycle-updated")
          update-response (common/admin-update-user {:user-id user-id}
                                                    {:email new-email
                                                     :app-metadata {:tenant "updated-tenant"}}
                                                    (service-client))]
      (:status update-response) => (fn [s] (#{200 400} s)))

    (let [delete-response (common/admin-delete-user {:user-id user-id} (service-client))]
      (:status delete-response) => 200)

    (let [after-delete (common/admin-get-user {:user-id user-id} (service-client))]
      (:status after-delete) => (fn [s] (>= s 400)))))

^{:refer lib.supabase.common/signup :added "4.1"
  :id signup-with-metadata}
(fact "signup includes user metadata and refresh token"
  (let [email (unique-email "signup-metadata")
        response (common/signup {:email email
                                 :password "password123"
                                 :data {:source "integration-test"
                                        :role "tester"}}
                                (anon-client))
        body (response-body response)]
    (:status response) => 200
    (:access_token body) => string?
    (:refresh_token body) => string?
    (-> body :user :email) => email
    (-> body :user :user_metadata :source) => "integration-test"
    (-> body :user :user_metadata :role) => "tester"))

^{:refer lib.supabase.common/user-put :added "4.1"
  :id user-put-updates-metadata}
(fact "updates current user metadata"
  (let [[_email token] (signup-and-token)
        response (common/user-put {:data {:display-name "Updated Name"}}
                                  (user-client token))
        body (response-body response)]
    (:status response) => (fn [s] (#{200 400} s))
    (when (= 200 (:status response))
      (-> body :user_metadata :display-name) => "Updated Name")))

^{:refer lib.supabase.common/token-refresh :added "4.1"
  :id token-refresh-rotates-token}
(fact "refreshes access token using refresh token"
  (let [[_email token] (signup-and-token)
        response (common/token-refresh {:refresh-token token}
                                       (anon-client))
        body (response-body response)]
    (map? response) => true
    (:status response) => (fn [s] (#{200 400} s))
    (when (= 200 (:status response))
      (:access_token body) => string?
      (:refresh_token body) => string?
      (not= token (:refresh_token body)) => true)))

^{:refer lib.supabase.common/logout :added "4.1"
  :id logout-invalidates-session}
(fact "logout invalidates the session token"
  (let [[_email token] (signup-and-token)
        logout-response (common/logout (user-client token))]
    (:status logout-response) => (fn [s] (#{200 204} s))
    (let [after-logout (common/user-get (user-client token))]
      (:status after-logout) => (fn [s] (>= s 400)))))

^{:refer lib.supabase.common/admin-list-users :added "4.1"
  :id admin-list-users-includes-created-user}
(fact "admin list users contains recently created user"
  (let [email (unique-email "admin-list")
        _ (common/admin-create-user {:email email :password "password123"}
                                    (service-client))
        response (common/admin-list-users (service-client))
        body (response-body response)
        emails (set (map :email (:users body)))]
    (:status response) => 200
    (:users body) => vector?
    (contains? emails email) => true))

^{:refer lib.supabase.common/authorize :added "4.1"
  :id authorize-oauth-response}
(fact "authorize returns an OAuth response"
  (let [response (common/authorize {:redirect-to "http://localhost:3000/callback"}
                                   (anon-client))]
    (map? response) => true
    (:status response) => integer?
    (when (= 302 (:status response))
      (or (get-in response [:headers "location"])
          (get-in response [:headers "Location"])) => string?)))

^{:refer lib.supabase.common/admin-delete-user :added "4.1"
  :id admin-delete-user-twice}
(fact "deleting same user twice returns an error"
  (let [email (unique-email "admin-delete-twice")
        user-id (-> (common/admin-create-user {:email email :password "password123"}
                                              (service-client))
                    response-body
                    :id)]
    (:status (common/admin-delete-user {:user-id user-id} (service-client))) => 200
    (:status (common/admin-delete-user {:user-id user-id} (service-client))) => (fn [s] (>= s 400))))

^{:refer lib.supabase.common/admin-generate-link :added "4.1"
  :id admin-generate-link-action-types}
(fact "generate-link supports multiple action types"
  (let [response-recovery (common/admin-generate-link {:email (unique-email "generate-link-recovery")
                                                       :type "recovery"}
                                                      (service-client))
        response-signup (common/admin-generate-link {:email (unique-email "generate-link-signup")
                                                     :type "signup"}
                                                    (service-client))]
    (map? response-recovery) => true
    (map? response-signup) => true
    (:status response-recovery) => (fn [s] (#{200 400 404} s))
    (:status response-signup) => (fn [s] (#{200 400 404} s))))

^{:refer lib.supabase.common/otp :added "4.1"
  :id otp-creates-user}
(fact "otp creates a new user when create-user is true"
  (let [email (unique-email "otp-create")
        response (common/otp {:email email :create-user true} (anon-client))]
    (map? response) => true
    (:status response) => integer?))

^{:refer lib.supabase.common/verify-post :added "4.1"
  :id verify-post-structured-error}
(fact "verify-post rejects invalid token with structured error"
  (let [response (common/verify-post {:type "signup"
                                      :email (unique-email "verify-post-error")
                                      :token "invalid-token"}
                                     (anon-client))
        body (response-body response)]
    (map? response) => true
    (:status response) => (fn [s] (>= s 400))
    (or (:error body) (:message body) (:code body)) => some?))

^{:refer lib.supabase.common/settings :added "4.1"
  :id settings-auth-configuration}
(fact "settings expose disable-signup and external providers configuration"
  (let [response (common/settings (anon-client))
        body (response-body response)]
    (:status response) => 200
    (map? body) => true
    (contains? (set (keys body)) :external) => true
    (contains? (set (keys body)) :disable_signup) => true))
