(ns lang-demos.js-009-log-sharedworker.app.sharedworker
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.substrate :as substrate]
             [xt.db.node.runtime :as db-runtime]
             [xt.db.system.main :as db-main]
             [lang-demos.js-009-log-sharedworker.generated.runtime :as app-runtime]]})

(defn.js runtime-init
  []
  (var node (substrate/node-create {"id" "log-sharedworker-server"}))
  (substrate/set-service
   node
   "auth/supabase"
   (db-main/create-impl "supabase" app-runtime/SupabaseDefaults nil nil))
  (return (db-runtime/sharedworker-init-kernel node nil "log-sharedworker")))
