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
             [js.worker.link :as worker-link]
             [js.net.http-fetch :as http-fetch]
             [xt.event.base-model :as event-model]
             [xt.db.node.adaptor-base :as adaptor-base]
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
            (var node (xt.substrate/node-create {"id" "db-model-server"}))

            ;;
            ;; SETUP GROUP WITH MODEL
            ;;
            (xt.substrate.page-core/add-group-attach
             node
             "room/a"
             "demo"
             {"tree-view" (xt.db.node.adaptor-base/create-tree-view-model
                           {"caching_id"    "db/caching"
                            "primary_id"    "db/primary"}
                           {"table" "Log"
                            "select_entry" {"input" []
                                            "view" {"table" "Log"
                                                    "type" "select"
                                                    "query" {}}}
                            "return_entry" {"input" []
                                            "view" {"table" "Log"
                                                    "type" "return"
                                                    "query" ["id" "message"]}}
                            "pipeline" {}
                            "options" {}
                            "defaults" {"select_args" []
                                        "return_args" []}})})
            
            (xt.substrate.page-proxy/install node)
            
            (var schema xt.db.poc.s02-shared-tree-test/Schema)
            (var lookup xt.db.poc.s02-shared-tree-test/SchemaLookup)

            
            (-> (xt.db.node.adaptor-base/init-adaptor-main
                 node
                 {"primary" {"type" "supabase"
                             "defaults" (@! local-min/+config-supabase-anon+)}
                  "caching" {"type" "memory" "defaults" {}}})
                (. (then (fn []
                           (return
                            (xt.substrate.transport-browser/boot-self
                             node
                             {"transport_id" "host"
                              "target" port
                              "ready" {"signal" "ready"
                                       "transport" "browser"
                                       "worker" "db-model-server"}})))))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.browser-sharedworker-tree-test/attach-tree-view-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "tree")]}
(fact "client can open a remote tree-view model and read its output"

  (notify/wait-on [:js 2000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (-> (browser-transport/connect-sharedworker
         client
         {"transport_id" "worker"
          "source" (worker-link/make-sharedworker-link (@! +sharedworker-script+))})
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
            (base-page/remote-call client "room/a" "demo" "tree-view" [[] []] true))))
        (promise/x:promise-then
         (fn [res]
           (return
            (promise/x:with-delay
             1000
             (fn []
               (repl/notify {"result" res
                             "output" (event-model/get-current model nil)}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"output" [{"message" "tree"}]}))
