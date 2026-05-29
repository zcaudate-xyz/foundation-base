(ns lib.supabase.common-test
  (:use code.test)
  (:require [lib.supabase.api :as api]
            [lib.supabase.common :as common]
            [net.openapi.call :as call]
            [scaffold.supabase.config :as supabase-config]
            [scaffold.supabase.event-host-util :as live]
            [std.lib.os :as os]
            [std.json :as json]))

(defn ensure-local-supabase!
  []
  @(os/sh {:args [live/+shell+ "-lc" (live/startup-shell-command)]
           :root live/+supabase-cli-root+})
  true)

(defn supabase-status
  []
  (live/parse-shell-env (live/supabase-status-env)))

(defn admin-defaults
  []
  (let [status (supabase-status)
        service-key (or (supabase-config/service-key)
                        (get status "SERVICE_ROLE_KEY"))
        base-url (str (or (supabase-config/api-base-url)
                          (get status "API_URL")
                          (supabase-config/resolved-api-base-url))
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
   :teardown [true]})

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