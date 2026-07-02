(ns xt.db.poc.s07-kernel-client-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

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
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.client-base :as client-base]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {
  :setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(def +sharedworker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "db-kernel-client-server"
                                           "spaces" {"room/a" {"state" {}}}}))
      ;; install db adaptor request handlers
      (xt.db.node.kernel-base/init-handlers node)
      ;; override init-base to return a clean status map
      (xt.substrate/register-handler
       node "@xt.db/kernel-init"
       (fn [space args request node]
         (return
          (. (xt.db.node.kernel-base/kernel-init-main
              node
              (. args [0])
              (. args [1])
              (. args [2]))
             (then
              (fn [_]
                (return {"status" "ok"})))
             (catch
              (fn [err]
                (return {"status" "error"
                         "message" (. err ["message"])
                         "stack" (. err ["stack"])}))))))
       nil)
      ;; install page-proxy so the client can open remote groups
      (xt.substrate.page-proxy/install node)
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (return
             (xt.substrate.transport-browser/boot-self
              node
              {"transport_id" "host"
               "target" port
               "ready" {"signal" "ready"
                        "transport" "browser"
                        "worker" "db-kernel-client-server"}})))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(defn.js with-kernel-worker
  "connects to the shared worker, initialises the db adaptor, and invokes callback"
  {:added "4.1"}
  [callback]
  (var client (substrate/node-create {"id" "db-kernel-client"
                                      "spaces" {"room/a" {"state" {}}}}))
  (page-proxy/install client)
  (return
   (promise/x:promise-then
    (browser-transport/connect-sharedworker
     client
     {"transport_id" "worker"
      "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {"type" "module"})})
    (fn [conn]
      (var transport-id (. conn ["transport_id"]))
      (return
       (promise/x:promise-then
        (substrate/request client
                           "room/a"
                           "@xt.db/kernel-init"
                           [{"primary" {"id" "db/primary"
                                        "type" "supabase"
                                        "defaults" (@! local-min/+config-supabase-anon+)}
                             "caching" {"id" "db/caching"
                                        "type" "memory"
                                        "defaults" {}}}
                            xt.db.poc.s07-kernel-client-test/Schema
                            xt.db.poc.s07-kernel-client-test/SchemaLookup]
                           {"transport_id" transport-id})
        (fn [_]
          (return (callback client transport-id)))))))))

^{:refer xt.db.poc.s07-kernel-client-test/attach-tree-view-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "kernel-client-tree")]}
(fact "client can init adaptor, attach a remote tree-view model, and read postgres data"

  (notify/wait-on [:js 20000]
    (-/with-kernel-worker
     (fn [client transport-id]
       (return
        (-> (client-base/dataview-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "tree-view"}
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
                          "return_args" []}}
             {"transport_id" transport-id})
            (promise/x:promise-then
             (fn [_]
               (return
                (page-proxy/open-proxy-group client "room/a" "demo" {"transport_id" transport-id}))))
            (promise/x:promise-then
             (fn [_]
               (return
                (base-page/remote-call client "room/a" "demo" "tree-view" [[] []] true))))
            (promise/x:promise-then
             (fn [_]
               (var group (base-page/group-get client "room/a" "demo"))
               (var model (xtd/get-in group ["models" "tree-view"]))
               (repl/notify
                {"has_group" (xt/x:not-nil? group)
                 "model_type" (xt/x:get-key model "::")
                 "output" (event-model/get-current model nil)}))))))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [{"message" "kernel-client-tree"}]}))

^{:refer xt.db.poc.s07-kernel-client-test/attach-pull-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "kernel-client-pull")]}
(fact "client can init adaptor, attach a remote pull-view model, and read postgres data"

  (notify/wait-on [:js 20000]
    (-/with-kernel-worker
     (fn [client transport-id]
       (return
        (-> (client-base/pull-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "pull"}
             ["Log"]
             {"pipeline" {}
              "options" {}
              "defaults" {"args" []
                         "output" {}}}
             {"transport_id" transport-id})
            (promise/x:promise-then
             (fn [_]
               (return
                (page-proxy/open-proxy-group client "room/a" "demo" {"transport_id" transport-id}))))
            (promise/x:promise-then
             (fn [_]
               (return
                ;; request all Log rows through the remote pull model
                (base-page/remote-call client "room/a" "demo" "pull" [["Log"]] true))))
            (promise/x:promise-then
             (fn [_]
               (var group (base-page/group-get client "room/a" "demo"))
               (var model (xtd/get-in group ["models" "pull"]))
               (repl/notify
                {"has_group" (xt/x:not-nil? group)
                 "model_type" (xt/x:get-key model "::")
                 "output" (event-model/get-current model nil)})))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                {"has_group" false
                 "error" (. err ["message"])
                 "stack" (. err ["stack"])}))))))))
  => (fn [notify]
       (and (get notify "has_group")
            (= "event.model" (get notify "model_type"))
            (some #(= "kernel-client-pull" (get % "message"))
                  (get notify "output")))))
