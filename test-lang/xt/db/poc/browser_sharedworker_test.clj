(ns xt.db.poc.browser-sharedworker-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [xt.db.system.main]
            [xt.db.poc.db-model-service-worker-sqlite]))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(def +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (. port (postMessage {"type" "worker-connected"}))
            (var schema {"Log" {"id" {"ident" "id"
                                       "type" "uuid"
                                       "primary" true
                                       "order" 0}
                                "message" {"ident" "message"
                                           "type" "text"
                                           "order" 1}}})
            (var lookup {"Log" {"position" 0}})
            (var primary-impl (xt.db.system.main/create-impl
                               "supabase"
                               (@! local-min/+config-supabase-anon+)
                               schema
                               lookup))
            (var caching-impl (xt.db.system.main/create-impl
                               "sqlite"
                               {}
                               schema
                               lookup))
            (. (. (xt.db.system.main/create-impl-init primary-impl)
                  (then
                   (fn [_]
                     (. port (postMessage {"type" "primary-connected"}))
                     (return (xt.db.system.main/create-impl-init caching-impl)))))
               (then
                (fn [caching-impl]
                  (. port (postMessage {"type" "sqlite-connected"}))
                  (return
                   (xt.db.poc.db-model-service-worker-sqlite/run-server
                    port
                    {"primary" {"impl" primary-impl}
                     "caching" {"impl" caching-impl}}
                    schema
                    lookup
                    "room/a"
                    "demo"
                    {"entry" ["Log"]}
                    {"signal" "ready"
                     "worker" "db-model-server-sqlite"}
                    (fn [node]
                      (. port (postMessage {"type" "impl-initialized"}))
                      (return nil))))))
               (catch
                (fn [err]
                  (. port (postMessage {"type" "error"
                                        "stage" "init"
                                        "message" (. err ["message"])
                                        "stack" (. err ["stack"])}))))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [js.worker.link :as worker-link]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.db-model-service-worker-sqlite/run-server
  :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "debug SharedWorker sqlite init"

  (notify/wait-on [:js 5000]
    (var messages [])
    (var blob (new Blob [(@! +sharedworker-script+)] {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (var shared (new SharedWorker url {"type" "module"}))
    (var port (. shared ["port"]))
    (. port (start))
    (. port (addEventListener
              "message"
              (fn [event]
                (. messages (push {"kind" "message" "data" (. event ["data"])})))
              false))
    (. shared (addEventListener
               "error"
               (fn [event]
                 (. messages (push {"kind" "error" "message" (. event ["message"])})))
               false))
    (. (!:G URL) (revokeObjectURL url))
    ((!:G setTimeout) (fn []
                        (repl/notify messages))
     2000)
    (return shared))
  => (contains-in
      [{"kind" "message" "data" {"type" "worker-connected"}}
       {"kind" "message" "data" {"type" "primary-connected"}}
       {"kind" "message" "data" {"type" "sqlite-connected"}}
       {"kind" "message" "data" {"type" "impl-initialized"}}]))
