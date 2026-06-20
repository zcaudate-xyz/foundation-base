(ns xt.db.poc.db-model-service-worker-sqlite-debug-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [js.net.conn-sqlite]
            [xt.net.conn-sql]
            [xt.db.system.impl-sqlite]
            [xt.db.poc.db-model-service-worker-sqlite]))

(def +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (. port (postMessage {"type" "worker-connected"}))
            (var schema {"Log" {"id" {"type" "uuid" "primary" true "order" 0}
                                "message" {"type" "text" "order" 1}}})
            (var lookup {"Log" {"position" 0}})
            (var client (js.net.conn-sqlite/create {}))
            (. (xt.net.conn-sql/connect client {})
               (then
                (fn [connected-client]
                  (. port (postMessage {"type" "sqlite-connected"}))
                  (var raw-impl (xt.db.system.impl-sqlite/impl-sqlite
                                 connected-client
                                 schema
                                 lookup))
                  (. (xt.db.system.impl-sqlite/impl-sqlite-init raw-impl)
                     (then
                      (fn [caching-impl]
                        (. port (postMessage {"type" "impl-initialized"}))
                        (return
                         (xt.db.poc.db-model-service-worker-sqlite/run-server
                          port
                          {"primary" {"type" "memory"}
                           "caching" {"impl" caching-impl}}
                          schema
                          lookup
                          "room/a"
                          "demo"
                          {"entry" ["Log"]}
                          {"signal" "ready"
                           "worker" "db-model-server-sqlite"}
                          (fn [node]
                            (. port (postMessage {"type" "seeded"}))))))))
                (fn [err]
                  (. port (postMessage {"type" "error" "stage" "connect" "message" err})))))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"}}}))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [js.worker.link :as worker-link]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.db-model-service-worker-sqlite/run-server
  :added "4.1"}
(fact "debug SharedWorker sqlite init"

  (notify/wait-on [:js 30000]
    (var messages [])
    (var blob (new Blob [(@! +sharedworker-script+)] {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (var shared (new SharedWorker url {"type" "module"}))
    (var port (. shared ["port"]))
    (. port (start))
    (. port (addEventListener
              "message"
              (fn [event]
                (. messages (push {"kind" "message" "data" (. event ["data"])}))
                (repl/notify messages))
              false))
    (. shared (addEventListener
               "error"
               (fn [event]
                 (. messages (push {"kind" "error" "message" (. event ["message"])}))
                 (repl/notify messages))
               false))
    (. (!:G URL) (revokeObjectURL url))
    (. (!:G setTimeout) (fn []
                          (repl/notify messages))
       15000)
    (return shared))
  => (contains-in
      [{"kind" "message" "data" {"type" "worker-connected"}}
       {"kind" "message" "data" {"type" "sqlite-connected"}}
       {"kind" "message" "data" {"type" "impl-initialized"}}]))
