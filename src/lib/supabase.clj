(ns lib.supabase
  (:require [lib.supabase.admin :as admin]
            [lib.supabase.auth :as auth]
            [lib.supabase.common :as common]
            [lib.supabase.query :as query]
            [lib.supabase.realtime :as realtime]
            [lib.supabase.rpc :as rpc]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [update]))

(f/intern-in common/client?
             common/create-client
             common/raw-state
             common/join-url
             common/auth-url
             common/rest-url
             common/admin-url
             common/decode-body
             common/api-call

             auth/has-session?
             auth/token-response->session
             auth/set-session!
             auth/clear-session!
             auth/get-session
             auth/get-user
             auth/api-signup
             auth/api-signin
             auth/api-impersonate
             auth/sign-up
             auth/sign-in
             auth/sign-in-with-password
             auth/refresh-session
             auth/sign-out
             auth/logout

             query/api-select-all
             query/select
             query/insert
             query/upsert
             query/update
             query/delete

             rpc/api-rpc
             rpc/rpc

             admin/api-signup-create
             admin/api-signup-delete
             admin/list-users
             admin/get-user-by-id
             admin/create-user
             admin/update-user
             admin/delete-user
             admin/invite-user-by-email

             realtime/trim-trailing-slash
             realtime/derive-websocket-url
             realtime/prepare-connect-url
             realtime/connected?
             realtime/connect
             realtime/disconnect
             realtime/resolve-topic
             realtime/join-payload
             realtime/join-frame
             realtime/leave-frame
             realtime/send-frame!
             realtime/join-channel
             realtime/leave-channel
             realtime/subscribe-broadcast
             realtime/subscribe-postgres-changes
             realtime/unsubscribe
             realtime/broadcast!)
