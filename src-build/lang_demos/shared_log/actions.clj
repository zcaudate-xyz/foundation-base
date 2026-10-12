(ns lang-demos.shared-log.actions
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.db.node.client-base :as client-base]
             [lang-demos.shared-log.entries :as entries]]})

(defn.js append-log
  [node message]
  (return
   (client-base/rpc-call node
                         "auth/supabase"
                         entries/LogAppendRPC
                         [message]
                         {})))

(defn.js delete-log
  [node entry-id]
  (return
   (client-base/rpc-call node
                         "auth/supabase"
                         entries/LogDeleteRPC
                         [entry-id]
                         {})))
