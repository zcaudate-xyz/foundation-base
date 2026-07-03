(ns xt.db.node.runtime-basic-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [clojure.string :as str]
            [net.http :as net.http]
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
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as runtime]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js CONFIG
  {"supabase" {"type" "supabase"
               "defaults" (@! local-min/+config-supabase-anon+)}
   "sqlite"   {"type" "sqlite"
               "defaults" {"filename" ":memory:"}}
   "memory"   {"type" "memory" "defaults" {}}})


(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})


^{:refer xt.db.node.runtime/create-impl-local :added "4.1"}
(fact "create-impl memory returns an impl"
  (!.js
   (var impl (xt.db.system.main/create-impl "memory" {} {} {}))
   (return {"type" (xt/x:get-key impl "::")}))
  => {"type" "xt.db.system.impl_memory/ImplMemory"})


^{:refer xt.db.node.runtime/nodeworker-init-kernel :added "4.1"}
(fact "boots a Node worker_threads kernel and emits a ready signal"

  (notify/wait-on [:js 10000]
    (var source (browser-transport/node-worker-source (@! (runtime/nodeworker-init-string)) {}))
    (-> (browser-transport/connect-worker
         (substrate/node-create {"id" "nodeworker-test-client"})
         {"transport_id" "xt.db.default.transport"
          "source" source})
        (promise/x:promise-then
         (fn [conn]
           (repl/notify (. conn ["ready"]))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)})))))
  => {"signal" "ready"
      "transport" "xt.db.default.transport"
      "worker" "xt.db.default.worker"})

^{:refer xt.db.node.runtime/nodeworker-init-string :added "4.1"}
(fact "emits a script string for booting a Node worker_threads kernel"

  (let [script (runtime/nodeworker-init-string)]
    (and (string? script)
         (> (count script) 0)
         (str/includes? script "nodeworker")))
  => true)

^{:refer xt.db.node.runtime/nodeworker-connect :added "4.1"}
(fact "connects a client to a Node worker_threads kernel and initialises it"

  (notify/wait-on [:js 10000]
    (var client (substrate/node-create {"id" "nodeworker-connect-client"}))
    (-> (runtime/nodeworker-connect client
                                    {"primary" {"type" "memory" "defaults" {}}
                                     "caching" {"type" "memory" "defaults" {}}}
                                    {}
                                    {}
                                    nil
                                    nil)
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"init" (xt/x:get-key out "init")
             "transport" (xt/x:get-key out "transport")
             "transport-attached" (xt/x:get-key out "transport-attached")})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"error" (xt/x:ex-message err)
             "status" (xt/x:get-key err "status")
             "data" (xt/x:get-key err "data")
             "frame-error" (xt/x:get-key err "error")})))))
  => {"init" true
      "transport" "xt.db.default.transport"
      "transport-attached" true})

^{:refer xt.db.node.runtime/nodeworker-connect :added "4.1"}
(fact "connects a client to a Node worker_threads kernel and pulls data from supabase"
  {:setup [(pg/t:delete scratch-v0/Log)
           (pg/t:insert scratch-v0/Log {:message "hello-nodeworker"})]}

  (notify/wait-on [:js 20000]
    (var client (substrate/node-create {"id" "nodeworker-supabase-client"}))
    (-> (runtime/nodeworker-connect client
                                    {"primary" (. -/CONFIG ["supabase"])
                                     "caching" (. -/CONFIG ["memory"])}
                                    -/Schema
                                    -/SchemaLookup
                                    nil
                                    nil)
        (promise/x:promise-then
         (fn [init-out]
           (return
            (substrate/request client
                               nil
                               "@xt.db/pull-call"
                               ["db/primary"
                                ["Log"]]
                               {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"pulled" (xt/x:not-nil? out)
             "count" (xt/x:len out)
             "first-message" (xtd/get-in out [0 "message"])})))
        (promise/x:promise-catch
         (fn [err]
           (var frame-error (xt/x:get-key err "error"))
           (repl/notify
            {"error" (xt/x:ex-message err)
             "status" (xt/x:get-key err "status")
             "data" (xt/x:get-key err "data")
             "frame-error" frame-error
             "frame-error-message" (xt/x:get-key frame-error "message")
             "frame-error-stack" (xt/x:get-key frame-error "stack")
             "frame-error-name" (xt/x:get-key frame-error "name")
             "raw" err})))))
  => {"pulled" true
      "count" 1
      "first-message" "hello-nodeworker"})

