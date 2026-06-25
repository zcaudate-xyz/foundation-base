(ns xt.db.node.adaptor-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.addon-supabase :as addon]
             [xt.db.system.impl-supabase :as impl-supabase]
             [xt.db.system.impl-supabase-session :as session]]})

(defn.xt supabase-request
  "executes a supabase command against the requested service"
  {:added "4.1"}
  [node service-id cmd]
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (var client (xt/x:get-key impl "client"))
          (return
           (-> (http-fetch/request-http client cmd)
               (promise/x:promise-then impl-supabase/normalise-body))))))))

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
               (promise/x:promise-then impl-supabase/normalise-body)
               (promise/x:promise-then
                (fn [session]
                  (session/set-session impl session)
                  (session/auto-refresh-start impl)
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
               (promise/x:promise-then impl-supabase/normalise-body)
               (promise/x:promise-then
                (fn [session]
                  (session/set-session impl session)
                  (session/auto-refresh-start imp)
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
          (session/auto-refresh-stop impl)
          (var client (xt/x:get-key impl "client"))
          (return
           (-> (http-fetch/request-http client (addon/cmd-logout opts))
               (promise/x:promise-then
                (fn [_]
                  (session/set-session impl nil)
                  (return {"status" "ok"}))))))))))


;;
;;
;;

(defn.xt supabase-refresh-handler
  "refreshes the current session on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (return (session/refresh-session impl)))))))

(defn.xt supabase-current-session-handler
  "returns the current session stored on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var impl (substrate/get-service node service-id))
  (return (session/get-session impl)))

(defn.xt supabase-rpc-call-handler
  "calls an rpc entry on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var rpc-name   (xt/x:second args))
  (var data       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 3)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-rpc-call rpc-name data opts))))

(defn.xt supabase-query-table-handler
  "queries a table on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var table-name (xt/x:second args))
  (var query      (xt/x:get-idx args (xt/x:offset 2)))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 3)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-query-table table-name query opts))))

(defn.xt supabase-health-handler
  "calls the auth health endpoint on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-health opts))))

(defn.xt supabase-admin-create-user-handler
  "creates a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-create-user data opts))))

(defn.xt supabase-admin-delete-user-handler
  "deletes a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var user-id    (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-delete-user user-id opts))))

(defn.xt supabase-admin-generate-link-handler
  "generates an admin link on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-generate-link data opts))))

(defn.xt supabase-admin-get-user-handler
  "fetches a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var user-id    (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-get-user user-id opts))))

(defn.xt supabase-admin-list-users-handler
  "lists users through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-list-users opts))))

(defn.xt supabase-admin-update-user-handler
  "updates a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var user-id    (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-update-user user-id opts))))

(defn.xt supabase-authorize-handler
  "starts an OAuth authorization request"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-authorize data opts))))

(defn.xt supabase-callback-handler
  "handles an OAuth callback request"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-callback opts))))

(defn.xt supabase-invite-handler
  "sends an invite on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-invite data opts))))

(defn.xt supabase-otp-handler
  "requests a passwordless OTP on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-otp data opts))))

(defn.xt supabase-recovery-handler
  "requests a recovery email on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-recovery data opts))))

(defn.xt supabase-settings-handler
  "reads auth settings on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-settings opts))))

(defn.xt supabase-token-refresh-handler
  "refreshes a session with a refresh token on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-token-refresh data opts))))

(defn.xt supabase-user-get-handler
  "fetches the current authenticated user on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-user-get opts))))

(defn.xt supabase-user-put-handler
  "updates the current authenticated user on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-user-put data opts))))

(defn.xt supabase-verify-get-handler
  "verifies a token via GET on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-verify-get data opts))))

(defn.xt supabase-verify-post-handler
  "verifies a token via POST on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-verify-post data opts))))

(defn.xt init-handlers
  "installs supabase adaptor handlers on a node"
  {:added "4.1"}
  [node]
  (substrate/register-handler node "@xt.db/supabase-sign-up" -/supabase-sign-up-handler nil)
  (substrate/register-handler node "@xt.db/supabase-sign-in" -/supabase-sign-in-handler nil)
  (substrate/register-handler node "@xt.db/supabase-sign-out" -/supabase-sign-out-handler nil)
  (substrate/register-handler node "@xt.db/supabase-refresh" -/supabase-refresh-handler nil)
  (substrate/register-handler node "@xt.db/supabase-current-session" -/supabase-current-session-handler nil)
  
  (substrate/register-handler node "@xt.db/supabase-rpc-call" -/supabase-rpc-call-handler nil)
  (substrate/register-handler node "@xt.db/supabase-query-table" -/supabase-query-table-handler nil)
  (substrate/register-handler node "@xt.db/supabase-health" -/supabase-health-handler nil)
  (substrate/register-handler node "@xt.db/supabase-admin-create-user" -/supabase-admin-create-user-handler nil)
  (substrate/register-handler node "@xt.db/supabase-admin-delete-user" -/supabase-admin-delete-user-handler nil)
  (substrate/register-handler node "@xt.db/supabase-admin-generate-link" -/supabase-admin-generate-link-handler nil)
  (substrate/register-handler node "@xt.db/supabase-admin-get-user" -/supabase-admin-get-user-handler nil)
  (substrate/register-handler node "@xt.db/supabase-admin-list-users" -/supabase-admin-list-users-handler nil)
  (substrate/register-handler node "@xt.db/supabase-admin-update-user" -/supabase-admin-update-user-handler nil)
  (substrate/register-handler node "@xt.db/supabase-authorize" -/supabase-authorize-handler nil)
  (substrate/register-handler node "@xt.db/supabase-callback" -/supabase-callback-handler nil)
  (substrate/register-handler node "@xt.db/supabase-invite" -/supabase-invite-handler nil)
  (substrate/register-handler node "@xt.db/supabase-otp" -/supabase-otp-handler nil)
  (substrate/register-handler node "@xt.db/supabase-recovery" -/supabase-recovery-handler nil)
  (substrate/register-handler node "@xt.db/supabase-settings" -/supabase-settings-handler nil)
  (substrate/register-handler node "@xt.db/supabase-token-refresh" -/supabase-token-refresh-handler nil)
  (substrate/register-handler node "@xt.db/supabase-user-get" -/supabase-user-get-handler nil)
  (substrate/register-handler node "@xt.db/supabase-user-put" -/supabase-user-put-handler nil)
  (substrate/register-handler node "@xt.db/supabase-verify-get" -/supabase-verify-get-handler nil)
  (substrate/register-handler node "@xt.db/supabase-verify-post" -/supabase-verify-post-handler nil)
  (return node))
