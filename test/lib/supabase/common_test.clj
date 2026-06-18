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
