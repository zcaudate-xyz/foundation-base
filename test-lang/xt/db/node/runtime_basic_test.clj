(ns xt.db.node.runtime-basic-test
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [net.http :as net.http]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.sample.scratch-v3 :as scratch-v3]
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
    (s/grant-usage "scratch_v0")
    (s/grant-usage "scratch_v3")))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.runtime :as runtime]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def +worker-threads-script+
  (-> (l/emit-script
       '(do
          (var node (xt.substrate/node-create {"id" "worker-threads-server"}))
          (xt.db.node.runtime/worker-threads-init-kernel node "host" "worker-threads-server"))
       {:lang :js
        :layout :full
        :emit {:override {"@sqlite.org/sqlite-wasm"
                          "data:text/javascript,export default {}"
                          "pg"
                          "data:text/javascript,export default {Client: function() {}}"}}})
      (str/replace #"let \{parentPort\} = require\(\"worker_threads\"\);"
                   "import { parentPort } from \"worker_threads\";")))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v3" "Currency" 120000)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.node.runtime/worker-threads-init-kernel :added "4.1"}
(fact "boots a worker_threads worker and emits a ready signal"

  (notify/wait-on [:js 10000]
    (var source (browser-transport/node-worker-source (@! +worker-threads-script+) {:eval true}))
    ((xt/x:get-key source "create_fn")
     (fn [data]
       (repl/notify data))))
  => {"signal" "ready"
      "transport" "worker_threads"
      "worker" "worker-threads-server"})

^{:refer xt.db.node.runtime/worker-threads-connect-kernel :added "4.1"}
(fact "connects a client to a worker_threads kernel and initialises services"

  (notify/wait-on [:js 10000]
    (var client (substrate/node-create {"id" "worker-threads-client"}))
    (-> (runtime/worker-threads-connect-kernel
         client
         (browser-transport/node-worker-source (@! +worker-threads-script+) {:eval true})
         "host"
         {"primary" {"type" "memory" "defaults" {}}
          "caching" {"type" "memory" "defaults" {}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn [server]
           (repl/notify
            {"has-server" (xt/x:not-nil? server)
             "has-primary" (xt/x:not-nil? (substrate/get-service server "db/primary"))
             "has-caching" (xt/x:not-nil? (substrate/get-service server "db/caching"))})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)
                         "stack" (xt/x:get-key err "stack")})))))
  => {"has-server" true
      "has-primary" true
      "has-caching" true})
