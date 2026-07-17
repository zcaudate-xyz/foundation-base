^{:seedgen/skip true}
(ns xt.db.node.runtime-basic-test
  (:use code.test)
  (:require [hara.lang :as l]
            [clojure.string :as str]
            [xt.lang.common-notify :as notify]
            [xt.db.node.runtime :as runtime]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.db.node.runtime :as runtime]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.runtime/nodeworker-init-string :added "4.1"}
(fact "emits a script string for booting a Node.js worker kernel"

  (let [script (runtime/nodeworker-init-string)]
    (and (string? script)
         (> (count script) 0)
         (str/includes? script "nodeworker")))
  => true)

^{:refer xt.db.node.runtime/nodeworker-init-kernel :added "4.1"}
(fact "boots a node worker kernel and attaches the transport"

  (notify/wait-on :js
    (var node (substrate/node-create {"id" "worker-node"}))
    (:= (!:G parentPort)
        {"postMessage" (fn [data] (return data))
         "on" (fn [event listener] (return true))})
    (promise/x:promise-then
     (runtime/nodeworker-init-kernel node "transport" "worker")
     (fn [conn]
       (repl/notify
        {"transport-attached" (xt/x:not-nil? (substrate/transport-get node "transport"))
         "worker" (xt/x:get-key conn ["ready" "worker"])}))))
  => {"transport-attached" true})

^{:refer xt.db.node.runtime/webworker-init-kernel :added "4.1"}
(fact "boots a web worker kernel and attaches the transport"

  (notify/wait-on :js
    (var posted [])
    (var listeners [])
    (:= (!:G addEventListener)
        (fn [event listener]
          (xt/x:arr-push listeners [event listener])
          (return true)))
    (:= (!:G removeEventListener)
        (fn [event listener] (return true)))
    (:= (!:G postMessage)
        (fn [data]
          (xt/x:arr-push posted data)
          (return data)))
    (var node (substrate/node-create {"id" "web-node"}))
    (promise/x:promise-then
     (runtime/webworker-init-kernel node "transport" "web")
     (fn [conn]
       (repl/notify
        {"transport-attached" (xt/x:not-nil? (substrate/transport-get node "transport"))
         "ready-signal" (xt/x:get-key conn ["ready" "signal"])
         "posted" posted}))))
  => {"transport-attached" true
      "posted" [{"signal" "ready"
                 "transport" "transport"
                 "worker" "web"}]})

^{:refer xt.db.node.runtime/sharedworker-init-kernel :added "4.1"}
(fact "sets up a SharedWorker kernel onconnect handler"

  (!.js
   (var node (substrate/node-create {"id" "shared-node"}))
   (runtime/sharedworker-init-kernel node "transport" "shared")
   (var handler (. (!:G globalThis) ["onconnect"]))
   {"has_handler" (xt/x:is-function? handler)})
  => {"has_handler" true})

^{:refer xt.db.node.runtime/nodeworker-connect :added "4.1"}
(fact "connects a client to a Node.js worker kernel and initialises it"

  (notify/wait-on [:js 30000]
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
             "transport-attached" (xt/x:get-key out "transport-attached")
             "transport" (xt/x:get-key out "transport")})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"error" (xt/x:ex-message err)})))))
  => {"init" true, "transport-attached" true, "transport" "xt.db.default.transport"})
