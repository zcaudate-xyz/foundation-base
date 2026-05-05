(ns js.worker.link-test
  (:require [js.worker.emit :as emit]
            [hara.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [js.worker.link :as worker-link]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defmacro node-link-init-check
  []
  (template/$
    (notify/wait-on :js
      (var link (worker-link/make-node-link ~(emit/node-script) {}))
      ((. link ["create_fn"])
       (fn [data]
         (repl/notify data))))))

^{:refer js.worker.link/make-mock-link :added "4.1"}
(fact "creates a mock worker link"

  (!.js
    (var worker ((. (worker-link/make-mock-link {})
                    ["create_fn"])
                 (fn [data] data)))
    (return (xt/x:obj-keys worker)))
  => (contains ["::" "listeners"
                "postMessage" "postRequest"]))

^{:refer js.worker.link/make-node-link :added "4.1"}
(fact "creates a Node worker link"

  (node-link-init-check)
  => (contains-in {"signal" "@cell/::INIT"
                   "body" {"done" true}}))

^{:refer js.worker.link/resolve-script :added "4.1"}
(fact "resolves script values or thunks"

  (!.js
    (return (worker-link/resolve-script "abc")))
  => "abc"

  (!.js
    (return (worker-link/resolve-script
             (fn []
               (return "xyz")))))
  => "xyz")

^{:refer js.worker.link/make-blob-url :added "4.1"}
(fact "creates a blob url from a script"
  (!.js
    (var previous-url (!:G URL))
    (:= (!:G URL) {"createObjectURL" (fn [blob]
                                       (return "blob:test"))})
    (var out (worker-link/make-blob-url "self.postMessage(1)"))
    (:= (!:G URL) previous-url)
    (return out))
  => "blob:test")

^{:refer js.worker.link/make-webworker-link :added "4.1"}
(fact "creates a WebWorker link"
  (!.js
   (var previous-url (!:G URL))
   (var previous-worker (!:G Worker))
   (var messages [])
   (var revoked [])
   (var listeners [])
   (:= (!:G URL) {"createObjectURL" (fn [blob]
                                      (return "blob:web"))
                   "revokeObjectURL" (fn [url] (revoked.push url))})
   (:= (!:G Worker)
       (fn [url]
         (return {"addEventListener" (fn [event listener capture]
                                        (listeners.push listener))})))
   (var link (worker-link/make-webworker-link "worker-script"))
   (var worker ((. link ["create_fn"]) (fn [data] (messages.push data))))
   ((xt/x:first listeners) {"data" "hello"})
   (var out {"messages" messages
             "revoked" revoked
             "keys" (xt/x:obj-keys worker)})
   (:= (!:G Worker) previous-worker)
   (:= (!:G URL) previous-url)
   (return out))
  => (contains-in {"messages" ["hello"]
                   "revoked" ["blob:web"]}))

^{:refer js.worker.link/make-sharedworker-link :added "4.1"}
(fact "creates a SharedWorker link"
  (!.js
   (var previous-url (!:G URL))
   (var previous-shared (!:G SharedWorker))
   (var messages [])
   (var revoked [])
   (var listeners [])
   (var starts [])
   (var port {"start" (fn [] (starts.push true))
              "addEventListener" (fn [event listener capture]
                                   (listeners.push listener))})
   (:= (!:G URL) {"createObjectURL" (fn [blob]
                                      (return "blob:shared"))
                   "revokeObjectURL" (fn [url] (revoked.push url))})
   (:= (!:G SharedWorker)
       (fn [url]
         (return {"port" port})))
   (var link (worker-link/make-sharedworker-link "worker-script"))
   (var worker ((. link ["create_fn"]) (fn [data] (messages.push data))))
   ((xt/x:first listeners) {"data" "world"})
   (var out {"messages" messages
             "revoked" revoked
             "starts" (xt/x:len starts)
             "keys" (xt/x:obj-keys worker)})
   (:= (!:G SharedWorker) previous-shared)
   (:= (!:G URL) previous-url)
   (return out))
  => (contains-in {"messages" ["world"]
                   "revoked" ["blob:shared"]
                   "starts" 1}))

^{:refer js.worker.link/make-link :added "4.1"}
(fact "dispatches to a runtime-specific worker link helper"

  (!.js
    (xt/x:obj-keys (worker-link/make-link "mock" nil {})))
  => (contains ["create_fn"])

  (!.js
   (worker-link/make-link "unknown" nil {}))
  => (throws))
