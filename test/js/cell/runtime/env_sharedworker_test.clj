(ns js.cell.runtime.env-sharedworker-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
              [js.cell.runtime.env-sharedworker :as env-sharedworker]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.cell.runtime.env-sharedworker/init-port :added "4.1"}
(fact "boots a SharedWorker port"
  (!.js
   (var messages [])
   (var port {:listeners []
              :postMessage (fn [msg] (messages.push msg))
              :addEventListener (fn [event listener capture]
                                  (port.listeners.push listener))
              :start (fn [] true)})
   (env-sharedworker/init-port port)
   (return {"listeners" (xt/x:len port.listeners)
            "message" (xt/x:first messages)}))
  => (contains-in {"listeners" 1
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-sharedworker/runtime-init :added "4.1"}
(fact "boots js.cell inside a SharedWorker"
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
   (var out {"listeners" (xt/x:len port.listeners)
             "starts" (xt/x:len starts)
             "message" (xt/x:first messages)})
   (:= (!:G self) previous-self)
   (return out))
  => (contains-in {"listeners" 1
                   "starts" 1
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))
