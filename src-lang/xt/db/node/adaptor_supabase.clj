(ns xt.db.node.adaptor-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.addon-supabase :as addon]
             [xt.db.system.impl-common :as impl-common]]})

(defn.xt normalise-auth-response
  "extracts the session payload from a supabase auth response"
  {:added "4.1"}
  [response]
  (return (xt/x:get-key response "body")))

(defn.xt supabase-sign-up-handler
  "signs up a new user and starts session auto-refresh"
  {:added "4.1"}
  [space args request node]
  (var service-id  (xt/x:first args))
  (var credentials (xt/x:second args))
  (var opts        (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (var client (xt/x:get-key impl "client"))
          (return
           (-> (http-fetch/request-http client (addon/cmd-signup credentials opts))
               (promise/x:promise-then -/normalise-auth-response)
               (promise/x:promise-then
                (fn [session]
                  (impl-common/set-session impl session)
                  (impl-common/start-auto-refresh impl opts)
                  (return session))))))))))

(defn.xt supabase-sign-in-handler
  "signs in with password and starts session auto-refresh"
  {:added "4.1"}
  [space args request node]
  (var service-id  (xt/x:first args))
  (var credentials (xt/x:second args))
  (var opts        (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (var client (xt/x:get-key impl "client"))
          (return
           (-> (http-fetch/request-http client (addon/cmd-token-password credentials opts))
               (promise/x:promise-then -/normalise-auth-response)
               (promise/x:promise-then
                (fn [session]
                  (impl-common/set-session impl session)
                  (impl-common/start-auto-refresh impl opts)
                  (return session))))))))))

(defn.xt supabase-sign-out-handler
  "signs out the current user and stops auto-refresh"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (impl-common/stop-auto-refresh impl)
          (var client (xt/x:get-key impl "client"))
          (return
           (-> (http-fetch/request-http client (addon/cmd-logout opts))
               (promise/x:promise-then
                (fn [_]
                  (impl-common/set-session impl nil)
                  (return {"status" "ok"}))))))))))

(defn.xt supabase-refresh-handler
  "refreshes the current session on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (return (impl-common/refresh-session impl)))))))

(defn.xt supabase-current-session-handler
  "returns the current session stored on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var impl (substrate/get-service node service-id))
  (return (impl-common/get-session impl)))

(defn.xt supabase-signed-in-handler
  "returns true if the requested service has an active access token"
  {:added "4.1"}
  [space args request node]
  (var impl (substrate/get-service node (xt/x:first args)))
  (var session (impl-common/get-session impl))
  (return (and (xt/x:not-nil? session)
               (xt/x:not-nil? (xt/x:get-key session "access_token")))))

(defn.xt supabase-user-info-handler
  "returns current authenticated user info for the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (return (impl-common/session-info impl)))))))

(defn.xt supabase-cmd-handler
  "generic handler that builds a command from addon-supabase and executes it"
  {:added "4.1"}
  [cmd-fn]
  (fn [space args request node]
    (var service-id (xt/x:first args))
    (var cmd-args (. args (slice 1)))
    (return
     (-> (promise/x:promise-run (substrate/get-service node service-id))
         (promise/x:promise-then
          (fn [impl]
            (var client (xt/x:get-key impl "client"))
            (return
             (-> (http-fetch/request-http client (xt/x:apply cmd-fn cmd-args))
                 (promise/x:promise-then -/normalise-auth-response)))))))))

(defn.xt init-handlers
  "installs supabase adaptor handlers on a node"
  {:added "4.1"}
  [node]
  (substrate/register-handler node "@xt.db/supabase-sign-up" -/supabase-sign-up-handler nil)
  (substrate/register-handler node "@xt.db/supabase-sign-in" -/supabase-sign-in-handler nil)
  (substrate/register-handler node "@xt.db/supabase-sign-out" -/supabase-sign-out-handler nil)
  (substrate/register-handler node "@xt.db/supabase-refresh" -/supabase-refresh-handler nil)
  (substrate/register-handler node "@xt.db/supabase-current-session" -/supabase-current-session-handler nil)
  (substrate/register-handler node "@xt.db/supabase-signed-in?" -/supabase-signed-in-handler nil)
  (substrate/register-handler node "@xt.db/supabase-user-info" -/supabase-user-info-handler nil)

  (substrate/register-handler node "@xt.db/supabase-rpc-call" (-/supabase-cmd-handler addon/cmd-rpc-call) nil)
  (substrate/register-handler node "@xt.db/supabase-query-table" (-/supabase-cmd-handler addon/cmd-query-table) nil)
  (substrate/register-handler node "@xt.db/supabase-health" (-/supabase-cmd-handler addon/cmd-health) nil)
  (substrate/register-handler node "@xt.db/supabase-admin-create-user" (-/supabase-cmd-handler addon/cmd-admin-create-user) nil)
  (substrate/register-handler node "@xt.db/supabase-admin-delete-user" (-/supabase-cmd-handler addon/cmd-admin-delete-user) nil)
  (substrate/register-handler node "@xt.db/supabase-admin-generate-link" (-/supabase-cmd-handler addon/cmd-admin-generate-link) nil)
  (substrate/register-handler node "@xt.db/supabase-admin-get-user" (-/supabase-cmd-handler addon/cmd-admin-get-user) nil)
  (substrate/register-handler node "@xt.db/supabase-admin-list-users" (-/supabase-cmd-handler addon/cmd-admin-list-users) nil)
  (substrate/register-handler node "@xt.db/supabase-admin-update-user" (-/supabase-cmd-handler addon/cmd-admin-update-user) nil)
  (substrate/register-handler node "@xt.db/supabase-authorize" (-/supabase-cmd-handler addon/cmd-authorize) nil)
  (substrate/register-handler node "@xt.db/supabase-callback" (-/supabase-cmd-handler addon/cmd-callback) nil)
  (substrate/register-handler node "@xt.db/supabase-invite" (-/supabase-cmd-handler addon/cmd-invite) nil)
  (substrate/register-handler node "@xt.db/supabase-otp" (-/supabase-cmd-handler addon/cmd-otp) nil)
  (substrate/register-handler node "@xt.db/supabase-recovery" (-/supabase-cmd-handler addon/cmd-recovery) nil)
  (substrate/register-handler node "@xt.db/supabase-settings" (-/supabase-cmd-handler addon/cmd-settings) nil)
  (substrate/register-handler node "@xt.db/supabase-token-refresh" (-/supabase-cmd-handler addon/cmd-token-refresh) nil)
  (substrate/register-handler node "@xt.db/supabase-user-get" (-/supabase-cmd-handler addon/cmd-user-get) nil)
  (substrate/register-handler node "@xt.db/supabase-user-put" (-/supabase-cmd-handler addon/cmd-user-put) nil)
  (substrate/register-handler node "@xt.db/supabase-verify-get" (-/supabase-cmd-handler addon/cmd-verify-get) nil)
  (substrate/register-handler node "@xt.db/supabase-verify-post" (-/supabase-cmd-handler addon/cmd-verify-post) nil)
  (return node))
