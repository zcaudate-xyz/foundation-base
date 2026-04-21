(ns js.cell.runtime.env-node-test
  (:require [js.cell.runtime.emit :as emit]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
              [xt.lang.common-repl :as repl]
              [js.cell.kernel :as cl]
              [js.cell.runtime.env-node :as env-node]
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

^{:refer js.cell.runtime.env-node/make-node-worker :added "4.1"}
(fact "creates a Node worker adapter"
  (!.js
   (xt/x:obj-keys (env-node/make-node-worker)))
  => (contains ["postMessage" "addEventListener"]))

^{:refer js.cell.runtime.env-node/init-worker :added "4.1"}
(fact "boots a Node worker adapter"
  (!.js
   (var messages [])
   (var worker {:listeners []
                :postMessage (fn [msg] (messages.push msg))
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (env-node/init-worker worker)
   (return {"listeners" (xt/x:len worker.listeners)
            "message" (xt/x:first messages)}))
  => (contains-in {"listeners" 1
                   "message" {"signal" "@worker/::INIT"
                              "body" {"done" true}}}))

^{:refer js.cell.runtime.env-node/runtime-init :added "4.1"}
(fact "boots js.cell inside a Node worker thread"
  (node-runtime-init-check)
  => [])
