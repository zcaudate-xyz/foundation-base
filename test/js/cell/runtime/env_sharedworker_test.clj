(ns js.cell.runtime.env-sharedworker-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell.runtime.env-sharedworker :as env-sharedworker]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.cell.runtime.env-sharedworker/init-port :added "4.1"}
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
            "message" (k/first messages)}))
  => (contains-in {"listeners" 1
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-sharedworker/runtime-init :added "4.1"}
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
