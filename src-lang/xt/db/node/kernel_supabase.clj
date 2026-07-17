(ns xt.db.node.kernel-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.http-util :as http-util]
             [xt.net.addon-supabase :as addon]
             [xt.db.system.impl-supabase :as impl-supabase]
             [xt.db.system.impl-supabase-session :as session]]})

(defn.xt supabase-rpc-error-detail
  "returns decoded structured gw rpc error details when available"
  {:added "4.1"}
  [body]
  (var detail (or (xt/x:get-key body "details")
                  (xt/x:get-key body "detail")))
  (cond (xt/x:is-string? detail)
        (try
          (return (xt/x:json-decode detail))
          (catch err
            (return detail)))

        :else
        (return detail)))

(defn.xt supabase-error-data
  "normalizes a failed supabase response into exception data"
  {:added "4.1"}
  [response]
  (var status (xt/x:get-key response "status"))
  (var body   (xt/x:get-key response "body"))
  (var detail (-/supabase-rpc-error-detail body))
  (var data body)
  (when (and (xt/x:is-object? detail)
             (== "gw.rpc.error" (xt/x:get-key detail "type")))
    (:= data detail))
  (when (xt/x:is-object? data)
    (xt/x:set-key data "status" status)
    (when (xt/x:nil? (xt/x:get-key data "http_status"))
      (xt/x:set-key data "http_status" status)))
  (return data))

(defn.xt supabase-response-data
  "extracts supabase response data and rejects failed responses"
  {:added "4.1"}
  [response]
  (var status (xt/x:get-key response "status"))
  (var data (-/supabase-error-data response))
  (var body (xt/x:get-key response "body"))
  (when (and status (>= status 400))
    (throw (xt/x:ex (or (xt/x:get-key data "message")
                        (xt/x:get-key body "message")
                        "Supabase request failed")
                    data)))
  (return (http-util/get-body-data response)))

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
               (promise/x:promise-then -/supabase-response-data))))))))

(defn.xt ^{:substrate/fn "@xt.supabase/sign-up"}
  supabase-sign-up-handler
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
               (promise/x:promise-then http-util/get-body-data)
               (promise/x:promise-then
                (fn [session]
                  (session/set-session impl session)
                  (session/auto-refresh-start impl)
                  (return session))))))))))

(defn.xt ^{:substrate/fn "@xt.supabase/sign-in"}
  supabase-sign-in-handler
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
               (promise/x:promise-then http-util/get-body-data)
               (promise/x:promise-then
                (fn [session]
                  (session/set-session impl session)
                  (session/auto-refresh-start impl)
                  (return session))))))))))

(defn.xt ^{:substrate/fn "@xt.supabase/sign-out"}
  supabase-sign-out-handler
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

(defn.xt ^{:substrate/fn "@xt.supabase/refresh"}
  supabase-refresh-handler
  "refreshes the current session on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (return
   (-> (promise/x:promise-run (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (return (session/refresh-session impl)))))))

(defn.xt ^{:substrate/fn "@xt.supabase/signed-in?"}
  supabase-signed-in-handler
  "returns whether the requested service currently has a session"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var impl (substrate/get-service node service-id))
  (return (xt/x:not-nil? (session/get-session impl))))

(defn.xt ^{:substrate/fn "@xt.supabase/current-session"}
  supabase-current-session-handler
  "returns the current session stored on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var impl (substrate/get-service node service-id))
  (return (session/get-session impl)))

(defn.xt ^{:substrate/fn "@xt.supabase/rpc-call"}
  supabase-rpc-call-handler
  "calls an rpc entry on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var rpc-name   (xt/x:second args))
  (var data       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 3)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-rpc-call rpc-name data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/query-table"}
  supabase-query-table-handler
  "queries a table on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var table-name (xt/x:second args))
  (var query      (xt/x:get-idx args (xt/x:offset 2)))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 3)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-query-table table-name query opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/health"}
  supabase-health-handler
  "calls the auth health endpoint on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-health opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-create-user"}
  supabase-admin-create-user-handler
  "creates a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-create-user data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-delete-user"}
  supabase-admin-delete-user-handler
  "deletes a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var user-id    (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-delete-user user-id opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-generate-link"}
  supabase-admin-generate-link-handler
  "generates an admin link on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-generate-link data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-get-user"}
  supabase-admin-get-user-handler
  "fetches a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var user-id    (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-get-user user-id opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-list-users"}
  supabase-admin-list-users-handler
  "lists users through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-list-users opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-update-user"}
  supabase-admin-update-user-handler
  "updates a user through the admin endpoint"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var user-id    (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-admin-update-user user-id opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/authorize"}
  supabase-authorize-handler
  "starts an OAuth authorization request"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-authorize data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/callback"}
  supabase-callback-handler
  "handles an OAuth callback request"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-callback opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/invite"}
  supabase-invite-handler
  "sends an invite on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-invite data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/otp"}
  supabase-otp-handler
  "requests a passwordless OTP on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-otp data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/recovery"}
  supabase-recovery-handler
  "requests a recovery email on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-recovery data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/settings"}
  supabase-settings-handler
  "reads auth settings on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-settings opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/token-refresh"}
  supabase-token-refresh-handler
  "refreshes a session with a refresh token on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-token-refresh data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/user-get"}
  supabase-user-get-handler
  "fetches the current authenticated user on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var opts       (or (xt/x:second args) {}))
  (var impl (substrate/get-service node service-id))
  (var session (session/get-session impl))
  (if (xt/x:not-nil? session)
    (return {"user" (xt/x:get-key session "user")})
    (return
     (-/supabase-request node service-id (addon/cmd-user-get opts)))))

(defn.xt ^{:substrate/fn "@xt.supabase/user-put"}
  supabase-user-put-handler
  "updates the current authenticated user on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-user-put data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/verify-get"}
  supabase-verify-get-handler
  "verifies a token via GET on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-verify-get data opts))))

(defn.xt ^{:substrate/fn "@xt.supabase/verify-post"}
  supabase-verify-post-handler
  "verifies a token via POST on the requested service"
  {:added "4.1"}
  [space args request node]
  (var service-id (xt/x:first args))
  (var data       (xt/x:second args))
  (var opts       (or (xt/x:get-idx args (xt/x:offset 2)) {}))
  (return
   (-/supabase-request node service-id (addon/cmd-verify-post data opts))))

;;
;; PAGE MODEL
;;

(defn.xt supabase-create-model
  "builds a page model spec backed by a Supabase command"
  {:added "4.1"}
  [service-id supabase-handler model]
  (var #{pipeline
         options
         defaults} model)
  (var model-handler
       (fn [context]
         (var node (. context ["node"]))
         (var cmd  (:? (xt/x:is-function? supabase-handler)
                       (supabase-handler context)
                       supabase-handler))
         (return (-/supabase-request node service-id cmd))))
  (return
   {"handler" model-handler
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler" model-handler}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.supabase/attach-model"}
  supabase-attach-model
  "attaches a Supabase-backed page model to the node"
  {:added "4.1"}
  [space args request node]
  (var service-id        (xt/x:first args))
  (var page-args         (xt/x:second args))
  (var supabase-handler  (xt/x:get-idx args (xt/x:offset 2)))
  (var model             (xt/x:get-idx args (xt/x:offset 3)))
  (var #{space-id
         group-id
         model-id} page-args)
  (var model-spec (-/supabase-create-model service-id supabase-handler model))
  (page-core/group-add-attach node
                              space-id
                              group-id
                              {model-id model-spec})
  (return {"status" "attached"
           "space" space-id
           "group" group-id
           "model" model-id}))

(defn.xt init-handlers
  "installs supabase adaptor handlers on a node"
  {:added "4.1"}
  [node]
  (substrate/register-handler node "@xt.supabase/sign-up" -/supabase-sign-up-handler nil)
  (substrate/register-handler node "@xt.supabase/sign-in" -/supabase-sign-in-handler nil)
  (substrate/register-handler node "@xt.supabase/sign-out" -/supabase-sign-out-handler nil)
  (substrate/register-handler node "@xt.supabase/refresh" -/supabase-refresh-handler nil)
  (substrate/register-handler node "@xt.supabase/signed-in?" -/supabase-signed-in-handler nil)
  (substrate/register-handler node "@xt.supabase/current-session" -/supabase-current-session-handler nil)
  
  (substrate/register-handler node "@xt.supabase/rpc-call" -/supabase-rpc-call-handler nil)
  (substrate/register-handler node "@xt.supabase/query-table" -/supabase-query-table-handler nil)
  (substrate/register-handler node "@xt.supabase/health" -/supabase-health-handler nil)
  (substrate/register-handler node "@xt.supabase/admin-create-user" -/supabase-admin-create-user-handler nil)
  (substrate/register-handler node "@xt.supabase/admin-delete-user" -/supabase-admin-delete-user-handler nil)
  (substrate/register-handler node "@xt.supabase/admin-generate-link" -/supabase-admin-generate-link-handler nil)
  (substrate/register-handler node "@xt.supabase/admin-get-user" -/supabase-admin-get-user-handler nil)
  (substrate/register-handler node "@xt.supabase/admin-list-users" -/supabase-admin-list-users-handler nil)
  (substrate/register-handler node "@xt.supabase/admin-update-user" -/supabase-admin-update-user-handler nil)
  (substrate/register-handler node "@xt.supabase/authorize" -/supabase-authorize-handler nil)
  (substrate/register-handler node "@xt.supabase/callback" -/supabase-callback-handler nil)
  (substrate/register-handler node "@xt.supabase/invite" -/supabase-invite-handler nil)
  (substrate/register-handler node "@xt.supabase/otp" -/supabase-otp-handler nil)
  (substrate/register-handler node "@xt.supabase/recovery" -/supabase-recovery-handler nil)
  (substrate/register-handler node "@xt.supabase/settings" -/supabase-settings-handler nil)
  (substrate/register-handler node "@xt.supabase/token-refresh" -/supabase-token-refresh-handler nil)
  (substrate/register-handler node "@xt.supabase/user-get" -/supabase-user-get-handler nil)
  (substrate/register-handler node "@xt.supabase/user-info" -/supabase-user-get-handler nil)
  (substrate/register-handler node "@xt.supabase/user-put" -/supabase-user-put-handler nil)
  (substrate/register-handler node "@xt.supabase/verify-get" -/supabase-verify-get-handler nil)
  (substrate/register-handler node "@xt.supabase/verify-post" -/supabase-verify-post-handler nil)
  (substrate/register-handler node "@xt.supabase/attach-model" -/supabase-attach-model nil)
  (return node))
