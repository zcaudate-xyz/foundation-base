(ns js.cell.runtime.link-test
  (:require [clojure.string :as str]
            [js.cell.runtime.emit :as emit]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [js.cell.kernel :as cl]
              [js.cell.runtime.link :as runtime-link]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defmacro node-link-init-check
  []
  (template/$
    (notify/wait-on :js
      (var cell (cl/make-cell
                 (runtime-link/make-node-link ~(emit/node-script) {})))
      (. (. cell ["init"])
         (then (fn []
                 (repl/notify (cl/list-models cell))))))))

^{:refer js.cell.runtime.link/make-mock-link :added "4.1"}
(fact "creates a mock worker link"
  
  (!.js
    (var worker ((. (runtime-link/make-mock-link {})
                    ["create_fn"])
                 (fn [data] data)))
    (return (xt/x:obj-keys worker)))
  => (contains ["::" "listeners"
                "postMessage" "postRequest"]))

^{:refer js.cell.runtime.link/make-node-link :added "4.1"}
(fact "creates a Node worker link"
  
  (node-link-init-check)
  => [])

^{:refer js.cell.runtime.link/resolve-script :added "4.1"}
(fact "resolves script values or thunks"
  
  (!.js
    (return (runtime-link/resolve-script "abc")))
  => "abc"
  
  (!.js
    (return (runtime-link/resolve-script
            (fn []
              (return "xyz")))))
  => "xyz")

^{:refer js.cell.runtime.link/make-blob-url :added "4.1"}
(fact "creates a blob url from a script"
  ^:hidden
  (!.js
    (var previous-url (!:G URL))
    (:= (!:G URL) {"createObjectURL" (fn [blob]
                                       (return "blob:test"))})
    (var out (runtime-link/make-blob-url "self.postMessage(1)"))
    (:= (!:G URL) previous-url)
    (return out))
  => "blob:test")

^{:refer js.cell.runtime.link/make-webworker-link :added "4.1"}
(fact "creates a WebWorker link"
  ^:hidden
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
   (var link (runtime-link/make-webworker-link "worker-script"))
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

^{:refer js.cell.runtime.link/make-sharedworker-link :added "4.1"}
(fact "creates a SharedWorker link"
  ^:hidden
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
   (var link (runtime-link/make-sharedworker-link "worker-script"))
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

^{:refer js.cell.runtime.link/make-link :added "4.1"}
(fact "dispatches to a runtime-specific worker link helper"
  ^:hidden

  (!.js
    (xt/x:obj-keys (runtime-link/make-link "mock" nil {})))
  => (contains ["create_fn"])

  (!.js
   (runtime-link/make-link "unknown" nil {}))
  => (throws))
