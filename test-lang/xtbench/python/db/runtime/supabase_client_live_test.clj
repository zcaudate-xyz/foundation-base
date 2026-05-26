(ns xtbench.python.db.runtime.supabase-client-live-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.runtime.event-host-util :as live]))

(l/script- :python
  {:runtime :basic
   :require [[python.lib.client-fetch :as py-fetch]
             [xt.db.runtime :as xdb]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
 {:setup [(l/rt:restart)
                 (do (live/init-live-postgres-runtime!)
                      (l/rt:setup (live/pg-rt) live/+postgres-module+)
                      (live/grant-scratch-schema!)
                      (live/reload-postgrest!)
                      (live/refresh-live-supabase-config!)
                      (live/cleanup-scratch-entry! live/+live-entry-name+)
                      true)]
  :teardown [(do (live/cleanup-scratch-entry! live/+live-entry-name+)
                (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                true)
                         (l/rt:stop)]})

^{:refer xt.db.runtime/db-create :added "4.1.3"}
(fact "creates a live db.supabase instance backed by a js fetch client for scratch-v1"

  (!.py
   (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
   (var client-config (xt/x:obj-clone (. instance ["client"])))
   (xt/x:set-key client-config "transport" (py-fetch/client {}))
   (xt/x:set-key instance "client" client-config)
   (var db (xdb/db-create instance nil nil {}))
   [(. db ["::"])
    (fetch/client? (. (. db ["instance"]) ["client"]))
    (. (. (. (. db ["instance"]) ["client"]) ["_raw"]) ["schema_name"])])
  => ["db.supabase" true "scratch"])

^{:refer xt.db.runtime/db-pull :added "4.1.3"}
(fact "pulls scratch-v1 data through a live supabase instance"

  (do
    (live/setup-scratch-entry! live/+live-entry-name+ live/+live-entry-tags+)
    (try
      (notify/wait-on [:python 5000]
        (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
        (var client-config (xt/x:obj-clone (. instance ["client"])))
        (xt/x:set-key client-config "transport" (py-fetch/client {}))
        (xt/x:set-key instance "client" client-config)
        (var db (xdb/db-create instance nil nil {}))
        (promise/x:promise-then
         (xdb/db-pull db nil (@! live/+live-entry-query+))
         (fn [result]
           (repl/notify result))))
      (finally
        (live/cleanup-scratch-entry! live/+live-entry-name+))))
  => [{"name" "copilot_supabase_pull_live"
       "tags" ["copilot" "supabase" "pull"]}])
