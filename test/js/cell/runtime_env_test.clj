(ns js.cell.runtime-env-test
  (:require [js.cell.runtime.emit :as emit]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.cell.kernel :as cl]
             [js.cell.runtime.env-node :as env-node]
             [js.cell.runtime.env-webworker :as env-webworker]
             [js.cell.runtime.env-sharedworker :as env-sharedworker]
             [js.cell.runtime.link :as runtime-link]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defmacro node-runtime-init-check
  []
  (template/$
    (notify/wait-on :js
      (var cell (cl/make-cell
                 (runtime-link/make-node-link ~(emit/node-script) {})))
      (. (. cell ["init"])
         (then (fn []
                 (repl/notify (cl/list-models cell))))))))

^{:refer js.cell.runtime.env-node/init-worker :added "4.0"}
(fact "boots a Node worker adapter"
  ^:hidden
  (!.js
   (var messages [])
   (var worker {:listeners []
                :postMessage (fn [msg] (messages.push msg))
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (env-node/init-worker worker)
   (return {"listeners" (k/len worker.listeners)
            "message" (k/first messages)
            "has-setup-service" (k/not-nil? (. worker.actions ["@cell/setup-service"]))}))
  => (contains-in {"listeners" 1
                   "has-setup-service" true
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-node/runtime-init :added "4.0"}
(fact "boots js.cell inside a Node worker thread"
  ^:hidden
  (node-runtime-init-check)
  => [])

^{:refer js.cell.runtime.env-webworker/init-worker :added "4.0"}
(fact "boots a WebWorker adapter"
  ^:hidden
  (!.js
   (var messages [])
   (var worker {:listeners []
                :postMessage (fn [msg] (messages.push msg))
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (env-webworker/init-worker worker)
   (return {"listeners" (k/len worker.listeners)
            "message" (k/first messages)
            "has-setup-service" (k/not-nil? (. worker.actions ["@cell/setup-service"]))}))
  => (contains-in {"listeners" 1
                   "has-setup-service" true
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-webworker/runtime-init :added "4.0"}
(fact "boots js.cell inside a WebWorker"
  ^:hidden
  (!.js
   (var previous-self (!:G self))
   (var messages [])
   (var worker {:listeners []
                :postMessage (fn [msg] (messages.push msg))
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (:= (!:G self) worker)
   (env-webworker/runtime-init)
   (var out {"listeners" (k/len worker.listeners)
             "message" (k/first messages)})
   (:= (!:G self) previous-self)
   (return out))
  => (contains-in {"listeners" 1
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-sharedworker/init-port :added "4.0"}
(fact "boots a SharedWorker port"
  ^:hidden
  (!.js
   (var messages [])
   (var port {:listeners []
              :postMessage (fn [msg] (messages.push msg))
              :addEventListener (fn [event listener capture]
                                  (port.listeners.push listener))
              :start (fn [] true)})
   (env-sharedworker/init-port port)
   (return {"listeners" (k/len port.listeners)
            "message" (k/first messages)
            "has-setup-service" (k/not-nil? (. port.actions ["@cell/setup-service"]))}))
  => (contains-in {"listeners" 1
                   "has-setup-service" true
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-sharedworker/runtime-init :added "4.0"}
(fact "boots js.cell inside a SharedWorker"
  ^:hidden
  (!.js
   (var previous-self (!:G self))
   (var messages [])
   (var starts [])
   (var port {:listeners []
              :postMessage (fn [msg] (messages.push msg))
              :addEventListener (fn [event listener capture]
                                  (port.listeners.push listener))
              :start (fn [] (starts.push true))})
   (var worker {})
   (:= (!:G self) worker)
   (env-sharedworker/runtime-init)
   ((. worker ["onconnect"]) {"ports" [port]})
   (var out {"listeners" (k/len port.listeners)
             "starts" (k/len starts)
             "message" (k/first messages)})
   (:= (!:G self) previous-self)
   (return out))
  => (contains-in {"listeners" 1
                   "starts" 1
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))
