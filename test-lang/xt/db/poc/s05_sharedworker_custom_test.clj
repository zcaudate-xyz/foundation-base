(ns xt.db.poc.s05-sharedworker-custom-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [xt.substrate]
            [xt.substrate.transport-browser]
            [xt.substrate.page-proxy]
            [xt.substrate.page-core]
            [xt.event.base-model]
            [xt.db.node.kernel-base]))

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
             [xt.event.base-model :as event-model]
             [xt.db.node.runtime :as runtime]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-proxy :as page-proxy]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(def +sharedworker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "db-model-server"}))
      (xt.db.node.runtime/init-server node)
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
                        "worker" "db-model-server"}})))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))


^{:refer xt.db.poc.s05-sharedworker-custom-test/attach-pull-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "client can open a remote pull model and read its output"

  (notify/wait-on [:js 5000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (runtime/init-server-proxy client)
    (-> client
        (browser-transport/connect-sharedworker
         {"transport_id" "worker"
          "source" (worker-link/make-sharedworker-link-opts (@! +sharedworker-script+)
                                                            {"type" "module"})})
        (promise/x:promise-then
         (fn [conn]
           (return
            (substrate/request client
                               "room/a"
                               "@xt.db/init-base"
                               [{"primary" {"type" "supabase"
                                           "defaults" (@! local-min/+config-supabase-anon+)}
                                "caching" {"type" "sqlite"
                                           "defaults" {"filename" ":memory:"}}}
                                -/Schema
                                -/SchemaLookup]))))
        (repl/notify)))
  => {"success" true})


^{:refer xt.db.poc.s05-sharedworker-custom-test/CONNECT
  :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "client can open a remote pull model and read its output"

  (notify/wait-on [:js 5000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (runtime/init-server-proxy client)
    (-> client
        (browser-transport/connect-sharedworker
         {"transport_id" "worker"
          "source" (worker-link/make-sharedworker-link-opts (@! +sharedworker-script+)
                                                            {"type" "module"})})
        (promise/x:promise-then
         (fn [conn]
           (return
            (substrate/request client
                               "room/a"
                               "@xt.db/init-base"
                               [{"primary" {"type" "supabase"
                                           "defaults" (@! local-min/+config-supabase-anon+)}
                                "caching" {"type" "sqlite"
                                           "defaults" {"filename" ":memory:"}}}
                                -/Schema
                                -/SchemaLookup]))))
        (repl/notify)))
  => {"success" true})

^{:refer xt.db.poc.s05-sharedworker-custom-test/supabase-reachable
  :added "4.1"
  :setup [(scratch-v0/log-append-public "hello")]}
(fact "browser page can reach supabase rest endpoint"

  (notify/wait-on [:js 20000]
    (var client (js.net.http-fetch/create
                  (@! local-min/+config-supabase-anon+)
                  (xt.net.addon-supabase/middleware-supabase)))
    (-> (js.net.http-fetch/request-http
         client
         {"path" "/rest/v1/Log"
          "method" "GET"
          "headers" {"Accept-Profile" "scratch_v0"
                     "apikey" (@! (-> local-min/+config+ :api :anon-key))}})
        (promise/x:promise-then
         (fn [res]
           (repl/notify (. res ["body"]))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify err)))))
  => (contains-in
      [{"id" string?
        "message" "hello"}]))
