(ns lib.supabase.common-test
  (:use code.test)
  (:require [lib.supabase.api :as api]
            [lib.supabase.common :as common]
            [net.openapi.call :as call]
            [scaffold.supabase.config :as supabase-config]
            [scaffold.supabase.docker-min :as docker-min]
            [std.json :as json]))

(defn ensure-local-supabase!
  []
  (docker-min/start-supabase nil)
  true)

(defn admin-defaults
  []
  (let [api (:api docker-min/+config+)
        service-key (or (supabase-config/service-key)
                        (get api :service-key))
        base-url (str (or (supabase-config/api-base-url)
                          (get api :base-url)
                          (str (get api :protocol "http")
                               "://"
                               (get api :hostname "127.0.0.1")
                               ":"
                               (get api :port)))
                      "/auth/v1")]
    {:base-url base-url
     :headers {"apikey" service-key
               "Authorization" (str "Bearer " service-key)
               "Content-Type" "application/json"}}))

(defn delete-auth-user!
  [user-id]
  (when user-id
    (call/call (get api/+admin+ "admin-delete-user")
               {:path {"user_id" user-id}}
               (admin-defaults))))

(fact:global
  {:setup [(ensure-local-supabase!)]
   :teardown [(docker-min/stop-supabase nil)]})

^{:refer lib.supabase.common/admin-create-user :added "4.1"}
(fact "creates an auth user through the local Supabase admin endpoint"
  (let [email (str "copilot-" (System/currentTimeMillis) "@example.com")
        response (binding [common/*default* (admin-defaults)]
                   (common/admin-create-user {"email" email
                                              "password" "pass123456"
                                              "email_confirm" true}
                                             nil))
        body (json/read (:body response))
        user-id (get body "id")]
    (try
      [(:status response)
       (= email (get body "email"))
       (string? user-id)]
      (finally
        (delete-auth-user! user-id))))
  => [200 true true])