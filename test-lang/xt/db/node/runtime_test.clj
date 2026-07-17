(ns xt.db.node.runtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [clojure.string :as str]
            [xt.lang.spec-base :as xt]
            [xt.lang.common-repl :as repl]
            [xt.lang.spec-promise :as promise]
            [xt.lang.common-notify :as notify]
            [xt.substrate :as substrate]
            [xt.substrate.transport-browser :as browser-transport]
            [scaffold.supabase.local-min :as local-min]
            [xt.db.node.client-base :as client-base]
            [xt.db.node.runtime :as runtime]))

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
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as runtime]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def +sharedworker-script+
  (runtime/sharedworket-init-string
   {"@sqlite.org/sqlite-wasm" "data:text/javascript,export default {}"
    "pg" "data:text/javascript,export default {Client: function() {}}"}))

(def +webworker-script+
  (runtime/webworker-init-string
   {"@sqlite.org/sqlite-wasm" "data:text/javascript,export default {}"
    "pg" "data:text/javascript,export default {Client: function() {}}"}))

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

^{:refer xt.db.node.runtime/sharedworket-init-string :added "4.1"}
(fact "emits a script string for booting a SharedWorker kernel"

  (let [script (runtime/sharedworket-init-string)]
    (and (string? script)
         (> (count script) 0)
         (str/includes? script "sharedworker")))
  => true)

^{:refer xt.db.node.runtime/webworker-init-string :added "4.1"}
(fact "emits a script string for booting a WebWorker kernel"

  (let [script (runtime/webworker-init-string)]
    (and (string? script)
         (> (count script) 0)
         (str/includes? script "webworker")))
  => true)

^{:refer xt.db.node.runtime/sharedworker-connect :added "4.1"}
(fact "connects a client to a SharedWorker kernel and initialises it"

  (notify/wait-on [:js 20000]
    (var client (substrate/node-create {"id" "sharedworker-connect-client"}))
    (-> (runtime/sharedworker-connect client
                                      {"primary" {"type" "memory" "defaults" {}}
                                       "caching" {"type" "memory" "defaults" {}}}
                                      {}
                                      {}
                                      (browser-transport/sharedworker-source
                                       (@! +sharedworker-script+)
                                       {"type" "module"})
                                      nil)
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"transport-attached" (xt/x:not-nil? (substrate/transport-get client "xt.db.default.transport"))})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"error" (xt/x:ex-message err)})))))
  => {"transport-attached" true})

^{:refer xt.db.node.runtime/webworker-connect :added "4.1"}
(fact "connects a client to a WebWorker kernel and initialises it"

  (notify/wait-on [:js 20000]
    (var client (substrate/node-create {"id" "webworker-connect-client"}))
    (-> (runtime/webworker-connect client
                                   {"primary" {"type" "memory" "defaults" {}}
                                    "caching" {"type" "memory" "defaults" {}}}
                                   {}
                                   {}
                                   (browser-transport/webworker-source
                                    (@! +webworker-script+)
                                    {"type" "module"})
                                   nil)
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"transport-attached" (xt/x:not-nil? (substrate/transport-get client "xt.db.default.transport"))})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"error" (xt/x:ex-message err)})))))
  => {"transport-attached" true})

(defn.js sharedworker-source-from-url
  "creates a SharedWorker source that reuses a fixed blob URL"
  {:added "4.1"}
  [url opts]
  (var worker-opts (or opts {}))
  (return
   {"create_fn"
    (fn [listener]
      (var shared (new SharedWorker url worker-opts))
      (var port (. shared ["port"]))
      (. port (start))
      (. port (addEventListener
               "message"
               (fn [e]
                 (return (listener (. e ["data"]))))
               false))
      (return port))}))

^{:refer xt.db.node.runtime/sharedworker-connect.multi-client
  :added "4.1"}
(fact "two clients connect to the same SharedWorker and pull data"

  (notify/wait-on [:js 30000]
    (var worker-url (browser-transport/blob-url (@! +sharedworker-script+)))
    (var source (-/sharedworker-source-from-url worker-url {"type" "module"}))
    (var client-a (substrate/node-create {"id" "sharedworker-client-a"}))
    (var client-b (substrate/node-create {"id" "sharedworker-client-b"}))
    (-> (runtime/sharedworker-connect client-a
                                      {"primary" {"type" "memory" "defaults" {}}
                                       "caching" {"type" "memory" "defaults" {}}}
                                      -/Schema
                                      -/SchemaLookup
                                      source
                                      nil)
        (promise/x:promise-then
         (fn [_]
           (return (client-base/sync-cached
                    client-a
                    "db/primary"
                    {"db/sync" {"Log" [{"id" 1 "message" "shared"}]}}
                    {}))))
        (promise/x:promise-then
         (fn [_]
           (return (client-base/pull-cached client-a "db/primary" ["Log" {"data" ["message"]}] {}))))
        (promise/x:promise-then
         (fn [output-a]
           (-> (runtime/sharedworker-connect client-b
                                             {"primary" {"type" "memory" "defaults" {}}
                                              "caching" {"type" "memory" "defaults" {}}}
                                             -/Schema
                                             -/SchemaLookup
                                             source
                                             nil)
               (promise/x:promise-then
                (fn [_]
                  (return (client-base/pull-cached client-b "db/primary" ["Log" {"data" ["message"]}] {}))))
               (promise/x:promise-then
                (fn [output-b]
                  (repl/notify
                   {"output-a" output-a
                    "output-b" output-b}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"error" (xt/x:ex-message err)
             "string" (xt/x:to-string err)
             "keys" (xt/x:obj-keys err)
             "json" (. (!:G JSON) (stringify err))})))))
  => (fn [res]
       (and (= "shared" (get-in res ["output-a" 0 "message"]))
            (= "shared" (get-in res ["output-b" 0 "message"])))))


^{:refer xt.db.node.runtime/sharedworker-init-kernel :added "4.1"}
(fact "sets up a SharedWorker onconnect handler that boots the transport"

  (notify/wait-on :js
    (var port-started false)
    (var listener nil)
    (var port {"start" (fn [] (:= port-started true))
               "addEventListener" (fn [event cb] (:= listener cb))
               "postMessage" (fn [data] nil)})
    (var shared {"port" port})
    (var node (substrate/node-create {"id" "shared-kernel"}))
    (runtime/sharedworker-init-kernel node "transport" "worker")
    ((. (!:G globalThis) ["onconnect"]) {"ports" [port]})
    (repl/notify {"started" port-started
                  "has-listener" (xt/x:is-function? listener)}))
  => {"started" true, "has-listener" true})

^{:refer xt.db.node.runtime/webworker-init-kernel :added "4.1"}
(fact "boots a WebWorker kernel and posts the ready signal"

  (notify/wait-on :js
    (var posted [])
    (var listeners [])
    (:= (!:G addEventListener) (fn [event listener] (xt/x:arr-push listeners [event listener])))
    (:= (!:G removeEventListener) (fn [event listener] nil))
    (:= (!:G postMessage) (fn [data] (xt/x:arr-push posted data)))
    (var node (substrate/node-create {"id" "web-kernel"}))
    (promise/x:promise-then
     (runtime/webworker-init-kernel node "transport" "web")
     (fn [conn]
       (repl/notify {"transport-attached" (xt/x:not-nil? (substrate/transport-get node "transport"))
                     "ready-signal" (xt/x:first posted)}))))
  => {"transport-attached" true
      "ready-signal" {"signal" "ready" "transport" "transport" "worker" "web"}})

^{:refer xt.db.node.runtime/nodeworker-init-kernel :added "4.1"}
(fact "boots a Node.js worker kernel using parentPort"

  (notify/wait-on :js
    (var posted [])
    (var on-handler nil)
    (:= (!:G parentPort)
        {"postMessage" (fn [data] (xt/x:arr-push posted data))
         "on" (fn [event listener] (:= on-handler listener))})
    (var node (substrate/node-create {"id" "node-kernel"}))
    (promise/x:promise-then
     (runtime/nodeworker-init-kernel node "transport" "worker")
     (fn [conn]
       (repl/notify {"transport-attached" (xt/x:not-nil? (substrate/transport-get node "transport"))
                     "ready-signal" (xt/x:first posted)}))))
  => {"transport-attached" true
      "ready-signal" {"signal" "ready" "transport" "transport" "worker" "worker"}})

^{:refer xt.db.node.runtime/nodeworker-init-string :added "4.1"}
(fact "emits a script string for booting a Node.js worker kernel"

  (let [script (runtime/nodeworker-init-string)]
    (and (string? script)
         (> (count script) 0)
         (str/includes? script "nodeworker")))
  => true)

^{:refer xt.db.node.runtime/nodeworker-connect :added "4.1"}
(fact "connects a client to a worker kernel and initialises it"

  (notify/wait-on [:js 20000]
    (var client (substrate/node-create {"id" "nodeworker-connect-client"}))
    (var onmessage nil)
    (var worker
         {"postMessage" (fn [data]
                          (var request-id (. data ["id"]))
                          (var kind (. data ["kind"]))
                          (when (== kind "request")
                            (onmessage {"kind" "response"
                                        "reply_to" request-id
                                        "status" "ok"
                                        "data" {"status" "ok"}})))
          "terminate" (fn [])})
    (var source
         {"create_fn"
          (fn [listener]
            (:= onmessage listener)
            (listener {"signal" "ready"
                       "transport" "xt.db.default.transport"
                       "worker" "worker"})
            (return worker))})
    ;; Note: source-endpoint passes the raw payload directly, not a {data: ...} event
    (-> (runtime/nodeworker-connect client
                                    {"primary" {"type" "memory" "defaults" {}}
                                     "caching" {"type" "memory" "defaults" {}}}
                                    {}
                                    {}
                                    source
                                    nil)
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"init" (xt/x:get-key out "init")
             "transport-attached" (xt/x:get-key out "transport-attached")
             "transport" (xt/x:get-key out "transport")})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"error" (xt/x:ex-message err)})))))
  => {"init" true, "transport-attached" true, "transport" "xt.db.default.transport"})