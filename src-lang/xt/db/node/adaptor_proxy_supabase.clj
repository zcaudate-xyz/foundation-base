(ns xt.db.node.adaptor-proxy-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]]})

;;
;; Client-side proxy handlers for the server-side supabase adaptor handlers in
;; xt.db.node.adaptor-supabase.
;;
;; These mirror the server substrate handler ids (e.g. @xt.supabase/sign-in)
;; so that the same function ids can be invoked on a client node and be
;; forwarded to the server over the configured transport.
;;
;; Substrate routing will always prefer an attached transport when no
;; transport_id is given, so proxy handlers must supply transport_id
;; explicitly from the request meta.
;;

(defn.xt set-default-transport
  "sets the default server transport id for supabase proxy requests"
  {:added "4.1"}
  [node transport-id]
  (xtd/set-in node ["state" "adaptor_proxy_supabase" "default_transport_id"] transport-id)
  (return transport-id))

(defn.xt get-default-transport
  "gets the default server transport id for supabase proxy requests"
  {:added "4.1"}
  [node]
  (return (xtd/get-in node ["state" "adaptor_proxy_supabase" "default_transport_id"])))

(defn.xt get-transport-id
  "resolves the transport id from opts or the node default"
  {:added "4.1"}
  [node opts]
  (return (or (xtd/get-in opts ["transport_id"])
              (-/get-default-transport node))))

(defn.xt request-meta
  "builds request meta with an explicit transport_id"
  {:added "4.1"}
  [node request]
  (var transport-id (-/get-transport-id node request))
  (return {"transport_id" transport-id}))

(defn.xt ^{:substrate/fn "@xt.supabase/sign-up"}
  supabase-sign-up-handler
  "client proxy for @xt.supabase/sign-up"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/sign-up"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/sign-in"}
  supabase-sign-in-handler
  "client proxy for @xt.supabase/sign-in"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/sign-in"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/sign-out"}
  supabase-sign-out-handler
  "client proxy for @xt.supabase/sign-out"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/sign-out"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/refresh"}
  supabase-refresh-handler
  "client proxy for @xt.supabase/refresh"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/refresh"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/signed-in?"}
  supabase-signed-in-handler
  "client proxy for @xt.supabase/signed-in?"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/signed-in?"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/current-session"}
  supabase-current-session-handler
  "client proxy for @xt.supabase/current-session"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/current-session"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/rpc-call"}
  supabase-rpc-call-handler
  "client proxy for @xt.supabase/rpc-call"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/rpc-call"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/query-table"}
  supabase-query-table-handler
  "client proxy for @xt.supabase/query-table"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/query-table"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/health"}
  supabase-health-handler
  "client proxy for @xt.supabase/health"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/health"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-create-user"}
  supabase-admin-create-user-handler
  "client proxy for @xt.supabase/admin-create-user"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/admin-create-user"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-delete-user"}
  supabase-admin-delete-user-handler
  "client proxy for @xt.supabase/admin-delete-user"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/admin-delete-user"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-generate-link"}
  supabase-admin-generate-link-handler
  "client proxy for @xt.supabase/admin-generate-link"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/admin-generate-link"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-get-user"}
  supabase-admin-get-user-handler
  "client proxy for @xt.supabase/admin-get-user"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/admin-get-user"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-list-users"}
  supabase-admin-list-users-handler
  "client proxy for @xt.supabase/admin-list-users"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/admin-list-users"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/admin-update-user"}
  supabase-admin-update-user-handler
  "client proxy for @xt.supabase/admin-update-user"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/admin-update-user"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/authorize"}
  supabase-authorize-handler
  "client proxy for @xt.supabase/authorize"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/authorize"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/callback"}
  supabase-callback-handler
  "client proxy for @xt.supabase/callback"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/callback"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/invite"}
  supabase-invite-handler
  "client proxy for @xt.supabase/invite"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/invite"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/otp"}
  supabase-otp-handler
  "client proxy for @xt.supabase/otp"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/otp"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/recovery"}
  supabase-recovery-handler
  "client proxy for @xt.supabase/recovery"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/recovery"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/settings"}
  supabase-settings-handler
  "client proxy for @xt.supabase/settings"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/settings"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/token-refresh"}
  supabase-token-refresh-handler
  "client proxy for @xt.supabase/token-refresh"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/token-refresh"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/user-get"}
  supabase-user-get-handler
  "client proxy for @xt.supabase/user-get"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/user-get"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/user-info"}
  supabase-user-info-handler
  "client proxy for @xt.supabase/user-info"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/user-info"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/user-put"}
  supabase-user-put-handler
  "client proxy for @xt.supabase/user-put"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/user-put"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/verify-get"}
  supabase-verify-get-handler
  "client proxy for @xt.supabase/verify-get"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/verify-get"
                      args
                      (-/request-meta node request))))

(defn.xt ^{:substrate/fn "@xt.supabase/verify-post"}
  supabase-verify-post-handler
  "client proxy for @xt.supabase/verify-post"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      "@xt.supabase/verify-post"
                      args
                      (-/request-meta node request))))

(defn.xt init-proxy-handlers
  "Registers client-side supabase proxy handlers so that the same substrate
   function ids used server-side can be invoked on a client node and forwarded
   to the server."
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
  (substrate/register-handler node "@xt.supabase/user-info" -/supabase-user-info-handler nil)
  (substrate/register-handler node "@xt.supabase/user-put" -/supabase-user-put-handler nil)
  (substrate/register-handler node "@xt.supabase/verify-get" -/supabase-verify-get-handler nil)
  (substrate/register-handler node "@xt.supabase/verify-post" -/supabase-verify-post-handler nil)
  (return node))
