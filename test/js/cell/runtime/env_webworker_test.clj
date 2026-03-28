(ns js.cell.runtime.env-webworker-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell.runtime.env-webworker :as env-webworker]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.cell.runtime.env-webworker/init-worker :added "4.1"}
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
            "message" (k/first messages)}))
  => (contains-in {"listeners" 1
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-webworker/runtime-init :added "4.1"}
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
