(ns js.worker.env-node-test
  (:require [js.worker.emit :as emit]
            [hara.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [js.worker.env-node :as env-node]
             [js.worker.link :as worker-link]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defmacro node-runtime-init-check
  []
  (template/$
    (notify/wait-on :js
      (var link (worker-link/make-node-link ~(emit/node-script) {}))
      ((. link ["create_fn"])
       (fn [data]
         (repl/notify data))))))

^{:refer js.worker.env-node/make-node-worker :added "4.1"}
(fact "creates a Node worker adapter"
  (!.js
   (xt/x:obj-keys (env-node/make-node-worker)))
  => (contains ["postMessage" "addEventListener"]))

^{:refer js.worker.env-node/init-worker :added "4.1"}
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
                   "message" {"signal" "@cell/::INIT"
                              "body" {"done" true}}}))

^{:refer js.worker.env-node/runtime-init :added "4.1"}
(fact "boots js.worker inside a Node worker thread"
  (node-runtime-init-check)
  => (contains-in {"signal" "@cell/::INIT"
                   "body" {"done" true}}))
