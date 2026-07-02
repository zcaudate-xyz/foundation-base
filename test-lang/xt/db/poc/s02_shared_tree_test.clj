(ns xt.db.poc.s02-shared-tree-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]))

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

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [js.net.http-fetch :as http-fetch]
             [xt.event.base-model :as event-model]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-proxy :as page-proxy]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (. port (postMessage {"type" "debug" "stage" "onconnect-fired"}))
            (try
              (var node (xt.substrate/node-create {"id" "db-model-server"}))

              ;;
              ;; SETUP GROUP WITH MODEL
              ;;
              (xt.substrate.page-core/add-group-attach
               node
               "room/a"
               "demo"
               {"tree-view" (xt.db.node.kernel-base/dataview-create-model
                             "db/primary"
                             {"table" "Log"
                              "select_entry" {"input" []
                                              "view" {"table" "Log"
                                                      "type" "select"
                                                      "query" {}}}
                              "return_entry" {"input" []
                                              "view" {"table" "Log"
                                                      "type" "return"
                                                      "query" ["id" "message"]}}}
                             {"pipeline" {}
                              "options" {}
                              "defaults" {"select_args" []
                                          "return_args" []}})})

              (xt.substrate.page-proxy/install node)
              (. port (postMessage {"type" "debug" "stage" "before-init"}))
              (. (!:G setTimeout)
                 (call
                  null
                  5000
                  (fn []
                    (. port (postMessage {"type" "debug" "stage" "init-timeout"})))))
              (var init-promise (xt.db.node.kernel-base/kernel-init-main
                                 node
                                 {"primary" {"type" "supabase"
                                             "defaults" (@! local-min/+config-supabase-anon+)}
                                  "caching" {"type" "memory" "defaults" {}}}
                                 xt.db.poc.s02-shared-tree-test/Schema
                                 xt.db.poc.s02-shared-tree-test/SchemaLookup))
              (. port (postMessage {"type" "debug" "stage" "init-called" "pending" (. init-promise ["status"])}))
              (-> init-promise
                  (xt.lang.spec-promise/x:promise-then
                   (fn []
                     (. port (postMessage {"type" "debug" "stage" "init-ok"}))
                     (return
                      (xt.substrate.transport-browser/boot-self
                       node
                       {"transport_id" "host"
                        "target" port
                        "ready" {"signal" "ready"
                                 "transport" "browser"
                                 "worker" "db-model-server"}}))))
                  (xt.lang.spec-promise/x:promise-catch
                   (fn [err]
                     (. port (postMessage {"type" "error"
                                           "message" (. err ["message"])
                                           "stack" (. err ["stack"])}))
                     (return
                      {"error" err
                       "message" (x:ex-message err)}))))
              (catch err
                (. port (postMessage {"type" "error"
                                      "stage" "onconnect"
                                      "message" (. err ["message"])
                                      "stack" (. err ["stack"])})))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(fact:global
 {
  :setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.s02-shared-tree-test/step-1-create-client
  :added "4.1"}
(fact "step 1: client can be created"
  (notify/wait-on [:js 5000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (repl/notify {"client-created" true}))
  => (contains-in {"client-created" true}))

^{:refer xt.db.poc.s02-shared-tree-test/step-2-install-proxy
  :added "4.1"}
(fact "step 2: page proxy can be installed"
  (notify/wait-on [:js 5000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (repl/notify {"proxy-installed" true}))
  => (contains-in {"proxy-installed" true}))

^{:refer xt.db.poc.s02-shared-tree-test/step-3-connect-sharedworker
  :added "4.1"}
(fact "step 3: sharedworker can be connected"
  (notify/wait-on [:js 10000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (-> (browser-transport/connect-sharedworker
         client
         {"transport_id" "worker"
          "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {"type" "module"})})
        (promise/x:promise-then
         (fn [conn]
           (repl/notify {"transport-id" (. conn ["transport_id"])})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)})))))
  => (contains-in {"transport-id" string?}))

^{:refer xt.db.poc.s02-shared-tree-test/step-4-open-proxy-group
  :added "4.1"
  :setup [(scratch-v0/log-append-public "tree")]}
(fact "step 4: proxy group can be opened"
  (notify/wait-on [:js 10000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (-> (browser-transport/connect-sharedworker
         client
         {"transport_id" "worker"
          "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {"type" "module"})})
        (promise/x:promise-then
         (fn [conn]
           (var transport-id (. conn ["transport_id"]))
           (return
            (page-proxy/open-proxy-group
             client
             "room/a"
             "demo"
             {"transport_id" transport-id}))))
        (promise/x:promise-then
         (fn [group]
           (repl/notify {"group-opened" true
                         "models" (xtd/get-in group ["models"])})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)})))))
  => (contains-in {"group-opened" true}))

^{:refer xt.db.poc.s02-shared-tree-test/step-5-remote-call
  :added "4.1"
  :setup [(scratch-v0/log-append-public "tree")]}
(fact "step 5: remote call returns data"
  (notify/wait-on [:js 10000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (-> (browser-transport/connect-sharedworker
         client
         {"transport_id" "worker"
          "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {"type" "module"})})
        (promise/x:promise-then
         (fn [conn]
           (var transport-id (. conn ["transport_id"]))
           (return
            (page-proxy/open-proxy-group
             client
             "room/a"
             "demo"
             {"transport_id" transport-id}))))
        (promise/x:promise-then
         (fn [group]
           (return
            (base-page/remote-call client "room/a" "demo" "tree-view" [[] []] true))))
        (promise/x:promise-then
         (fn [res]
           (repl/notify {"remote-result" res})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)})))))
  => (contains-in {"remote-result" map?}))

^{:refer xt.db.poc.browser-sharedworker-tree-test/attach-tree-view-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "tree")]}
(fact "step 6: client can open a remote tree-view model and read its output"
  (notify/wait-on [:js 15000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (-> (browser-transport/connect-sharedworker
         client
         {"transport_id" "worker"
          "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {"type" "module"})})
        (promise/x:promise-then
         (fn [conn]
           (var transport-id (. conn ["transport_id"]))
           (return
            (page-proxy/open-proxy-group
             client
             "room/a"
             "demo"
             {"transport_id" transport-id}))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "tree-view"]))
           (return
            (promise/x:promise-then
             (base-page/remote-call client "room/a" "demo" "tree-view" [[] []] true)
             (fn [res]
               (return
                (promise/x:with-delay
                 1000
                 (fn []
                   (repl/notify {"result" res
                                 "output" (event-model/get-current model nil)})))))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"output" [{"message" "tree"}]}))

^{:refer xt.db.poc.s02-shared-tree-test/debug-minimal-sharedworker
  :added "4.1"}
(fact "minimal sharedworker can echo messages"
  (notify/wait-on [:js 10000]
    (var messages [])
    (var notified false)
    (var script "self.onconnect = function(e) { var port = e.ports[0]; port.start(); port.postMessage({type: 'debug', stage: 'minimal-onconnect'}); };")
    (var blob (new Blob [script] {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (var shared (new SharedWorker url))
    (var port (. shared ["port"]))
    (. port (start))
    (. port (addEventListener
              "message"
              (fn [event]
                (var data (. event ["data"]))
                (. messages (push data))
                (when (not notified)
                  (:= notified true)
                  (repl/notify messages)))
              false))
    (. (!:G URL) (revokeObjectURL url))
    (. (!:G setTimeout)
       (call
        null
        5000
        (fn []
          (when (not notified)
            (:= notified true)
            (repl/notify messages))))))
  => (contains-in
      [{"type" "debug" "stage" "minimal-onconnect"}]))
