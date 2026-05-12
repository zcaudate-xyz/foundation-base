(ns xt.db.runtime.supabase-pull-live-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.helpers.supabase-pull-live-test :as live]))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as js-fetch]
             [xt.db.instance :as xdb]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
  {:setup [(l/rt:restart)
           (do (when live/CANARY-SUPABASE-LIVE
                 (live/init-live-postgres-runtime!)
                 (l/rt:setup :postgres)
                 (live/grant-scratch-schema!)
                 (live/refresh-live-supabase-config!)
                 (live/cleanup-scratch-entry! live/+live-entry-name+))
               true)]
   :teardown [(do (when live/CANARY-SUPABASE-LIVE
                    (live/cleanup-scratch-entry! live/+live-entry-name+)
                    (l/rt:teardown :postgres))
                  (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                  true)
              (l/rt:stop)]})

^{:refer xt.db.instance/db-create :added "4.1.3"}
(fact "creates a live db.supabase instance backed by a js fetch client for scratch-v1"

  (if live/CANARY-SUPABASE-LIVE
    (!.js
     (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
     (var client (js-fetch/client (xt/x:obj-clone (. instance ["client"]))))
     (xt/x:set-key instance "client" client)
     (var db (xdb/db-create instance nil nil {}))
     [(. db ["::"])
      (fetch/client? (. (. db ["instance"]) ["client"]))
      (. (. (. (. db ["instance"]) ["client"]) ["_raw"]) ["schema_name"])])
    :supabase-live-unavailable)
  => (any ["db.supabase" true "scratch"]
          :supabase-live-unavailable))

^{:refer xt.db.instance/db-pull-sync :added "4.1.3"}
(fact "pulls scratch-v1 data through a live supabase instance"

  (if live/CANARY-SUPABASE-LIVE
    (do
      (live/setup-scratch-entry! live/+live-entry-name+ live/+live-entry-tags+)
      (try
        (!.js
         (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
         (var client (js-fetch/client (xt/x:obj-clone (. instance ["client"]))))
         (xt/x:set-key instance "client" client)
         (var db (xdb/db-create instance nil nil {}))
         (xdb/db-pull-sync db nil (@! live/+live-entry-query+)))
        (finally
          (live/cleanup-scratch-entry! live/+live-entry-name+))))
    :supabase-live-unavailable)
  => (any [{"name" "copilot_supabase_pull_live"
            "tags" ["copilot" "supabase" "pull"]}]
          :supabase-live-unavailable))
