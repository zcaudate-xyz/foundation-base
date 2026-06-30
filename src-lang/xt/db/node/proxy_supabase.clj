(ns xt.db.node.proxy-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
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

(defn.xt init-proxy-handlers
  "Registers client-side supabase proxy handlers so that the same substrate
   function ids used server-side can be invoked on a client node and forwarded
   to the server."
  {:added "4.1"}
  [node]
  (xt/for:array [action -/ACTIONS]
    (substrate/register-handler node action proxy-util/request-proxy nil))
  (return node))
