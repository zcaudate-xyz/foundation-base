(ns xt.db.node.proxy-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.proxy-util :as proxy-util]]})

(def.xt ACTIONS
  ["@xt.supabase/sign-up"
   "@xt.supabase/sign-in"
   "@xt.supabase/sign-out"
   "@xt.supabase/refresh"
   "@xt.supabase/signed-in?"
   "@xt.supabase/current-session"
   "@xt.supabase/rpc-call"
   "@xt.supabase/query-table"
   "@xt.supabase/health"
   "@xt.supabase/admin-create-user"
   "@xt.supabase/admin-delete-user"
   "@xt.supabase/admin-generate-link"
   "@xt.supabase/admin-get-user"
   "@xt.supabase/admin-list-users"
   "@xt.supabase/admin-update-user"
   "@xt.supabase/authorize"
   "@xt.supabase/callback"
   "@xt.supabase/invite"
   "@xt.supabase/otp"
   "@xt.supabase/recovery"
   "@xt.supabase/settings"
   "@xt.supabase/token-refresh"
   "@xt.supabase/user-get"
   "@xt.supabase/user-info"
   "@xt.supabase/user-put"
   "@xt.supabase/verify-get"
   "@xt.supabase/verify-post"])

(def.xt ATTACH_ACTIONS
  ["@xt.supabase/attach-model"])

(defn.xt attach-forward-handler
  "forwards a Supabase attach action to the server and opens the proxy group"
  {:added "4.1"}
  [space args request node]
  (var page-args (xt/x:second args))
  (var space-id (xtd/get-in page-args ["space_id"]))
  (var group-id (xtd/get-in page-args ["group_id"]))
  (var transport-id (proxy-util/get-transport-id node (xtd/get-in request ["meta"])))
  (page-proxy/group-create-proxy node space-id group-id {} {"transport_id" transport-id})
  (return
   (promise/x:promise-then
    (substrate/request node
                       nil
                       (xt/x:get-key request "action")
                       args
                       {"transport_id" transport-id})
    (fn [status]
      (return
       (promise/x:promise-then
        (page-proxy/group-open-proxy node space-id group-id {"transport_id" transport-id})
        (fn [_]
          (return status))))))))

(defn.xt init-proxy-handlers
  "Registers client-side supabase proxy handlers so that the same substrate
   function ids used server-side can be invoked on a client node and forwarded
   to the server."
  {:added "4.1"}
  [node]
  (xt/for:array [action -/ACTIONS]
    (substrate/register-handler node action proxy-util/request-proxy nil))
  (xt/for:array [action -/ATTACH_ACTIONS]
    (substrate/register-handler node action -/attach-forward-handler nil))
  (return node))
