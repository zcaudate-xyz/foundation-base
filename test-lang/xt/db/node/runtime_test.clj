(ns xt.db.node.runtime-test
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
  {:runtime :chromedriver.instance
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
             [xt.db.node.runtime :as runtime]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(defn.js with-timeout
  "races a promise against a timeout"
  {:added "4.1"}
  [promise ms]
  (return
   (. Promise
      (race [promise
             (new Promise
                  (fn [resolve _]
                    (. setTimeout (fn [] (resolve {"timeout" true})) ms)))]))))

(def +webworker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "webworker-server"}))
      (xt.db.node.runtime/webworker-init-kernel node "host" "webworker-server"))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "data:text/javascript,export default {}"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(def +sharedworker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "sharedworker-server"}))
      (xt.db.node.runtime/sharedworker-init-kernel node "host" "sharedworker-server"))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "data:text/javascript,export default {}"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.node.runtime/init-server :added "4.1"}
(fact "installs kernel and page-proxy handlers on a node"

  (!.js
   (var node (substrate/node-create {"id" "server"}))
   (runtime/init-server node)
   (var handlers (. node ["handlers"]))
   (and (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/kernel-init"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/kernel-setup"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/attach-model"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/detach-model"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/rpc-call"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.supabase/sign-up"))))
  => true)

^{:refer xt.db.node.runtime/init-server-proxy :added "4.1"}
(fact "installs proxy and page-proxy handlers on a node"

  (!.js
   (var node (substrate/node-create {"id" "proxy"}))
   (runtime/init-server-proxy node)
   (var handlers (. node ["handlers"]))
   (and (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/kernel-init"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/attach-model"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/detach-model"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.db/rpc-call"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.supabase/sign-up"))))
  => true)

^{:refer xt.db.node.runtime/sharedworker-init-kernel :added "4.1"}
(fact "boots a SharedWorker and emits a ready signal"

  (notify/wait-on [:js 10000]
    (var source (browser-transport/sharedworker-source (@! +sharedworker-script+) {}))
    (-> (-/with-timeout
         (new Promise
              (fn [resolve _]
                ((xt/x:get-key source "create_fn")
                 (fn [data]
                   (resolve data)))))
         5000)
        (promise/x:promise-then
         (fn [data]
           (repl/notify data)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)})))))
  => (fn [res]
       (or (and (map? res)
                (= "ready" (get res "signal"))
                (= "host" (get res "transport"))
                (= "sharedworker-server" (get res "worker")))
           (and (map? res)
                (true? (get res "timeout"))))))

^{:refer xt.db.node.runtime/sharedworket-init-string :added "4.1"}
(fact "emits a script string for booting a SharedWorker kernel"

  (let [script (runtime/sharedworket-init-string)]
    (and (string? script)
         (> (count script) 0)
         (str/includes? script "sharedworker")))
  => true)

^{:refer xt.db.node.runtime/sharedworker-connect :added "4.1"}
(fact "connects a client to a SharedWorker kernel and initialises it"

  (notify/wait-on [:js 10000]
    (var client (substrate/node-create {"id" "sharedworker-connect-client"}))
    (-> (-/with-timeout
         (runtime/sharedworker-connect client
                                       {"primary" {"type" "memory" "defaults" {}}
                                        "caching" {"type" "memory" "defaults" {}}}
                                       {}
                                       {}
                                       (browser-transport/sharedworker-source (@! +sharedworker-script+) {})
                                       nil)
         5000)
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"has-init" (xt/x:not-nil? (xt/x:get-key out "init"))
             "transport-attached" (xt/x:not-nil? (substrate/get-transport client "xt.db.default.transport"))})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"transport-attached" (xt/x:not-nil? (substrate/get-transport client "xt.db.default.transport"))
             "status" (xt/x:get-key err "status")
             "kind" (xt/x:get-key err "kind")})))))
  => (fn [res]
       (or (and (map? res)
                (true? (get res "transport-attached")))
           (and (map? res)
                (true? (get res "timeout"))))))

^{:refer xt.db.node.runtime/webworker-init-kernel :added "4.1"}
(fact "boots a WebWorker and emits a ready signal"

  (notify/wait-on [:js 10000]
    (var source (browser-transport/webworker-source (@! +webworker-script+)))
    (-> (-/with-timeout
         (new Promise
              (fn [resolve _]
                ((xt/x:get-key source "create_fn")
                 (fn [data]
                   (resolve data)))))
         5000)
        (promise/x:promise-then
         (fn [data]
           (repl/notify data)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)})))))
  => (fn [res]
       (or (and (map? res)
                (= "ready" (get res "signal"))
                (= "host" (get res "transport"))
                (= "webworker-server" (get res "worker")))
           (and (map? res)
                (true? (get res "timeout"))))))

^{:refer xt.db.node.runtime/webworker-init-string :added "4.1"}
(fact "emits a script string for booting a WebWorker kernel"

  (let [script (runtime/webworker-init-string)]
    (and (string? script)
         (> (count script) 0)
         (str/includes? script "webworker")))
  => true)

^{:refer xt.db.node.runtime/webworker-connect :added "4.1"}
(fact "connects a client to a WebWorker kernel and initialises it"

  (notify/wait-on [:js 10000]
    (var client (substrate/node-create {"id" "webworker-connect-client"}))
    (-> (-/with-timeout
         (runtime/webworker-connect client
                                    {"primary" {"type" "memory" "defaults" {}}
                                     "caching" {"type" "memory" "defaults" {}}}
                                    {}
                                    {}
                                    (browser-transport/webworker-source (@! +webworker-script+))
                                    nil)
         5000)
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"has-init" (xt/x:not-nil? (xt/x:get-key out "init"))
             "transport-attached" (xt/x:not-nil? (substrate/get-transport client "xt.db.default.transport"))})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"transport-attached" (xt/x:not-nil? (substrate/get-transport client "xt.db.default.transport"))
             "status" (xt/x:get-key err "status")
             "kind" (xt/x:get-key err "kind")})))))
  => (fn [res]
       (or (and (map? res)
                (true? (get res "transport-attached")))
           (and (map? res)
                (true? (get res "timeout"))))))
