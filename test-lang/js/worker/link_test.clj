(ns js.worker.link-test
  (:require [js.worker.emit :as emit]
            [hara.lang :as l]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as db-node]
             [xt.event.node :as event-node]
             [xt.lang.spec-base :as xt]
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

(def ^:private +webworker-runtime-script+
  (emit/webworker-script {"node" {"id" "worker-e2e"}
                          "db-node" {"schema" {"Order" {}}}}
                         :flat))

(def ^:private +sharedworker-runtime-script+
  (emit/sharedworker-script :flat))

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

^{:refer js.worker.link/make-webworker-link :added "4.1"}
(fact "boots an emitted WebWorker runtime script end-to-end"
  (!.js
   (var previous-blob (!:G Blob))
   (var previous-url (!:G URL))
   (var previous-worker (!:G Worker))
   (var revoked [])
   (:= (!:G Blob)
       (fn [parts opts]
         (return {"parts" parts
                  "opts" opts})))
   (:= (!:G URL)
       {"createObjectURL" (fn [blob]
                            (return (xt/x:first (. blob ["parts"]))))
        "revokeObjectURL" (fn [url]
                            (revoked.push url))})
   (:= (!:G Worker)
       (fn [script]
         (var listeners [])
         (var worker {"listeners" listeners})
         (xt/x:set-key worker
                       "addEventListener"
                       (fn [event listener capture]
                         (listeners.push listener)))
         (xt/x:set-key worker
                       "postMessage"
                       (fn [frame]
                         (return frame)))
         (var previous-self (!:G self))
         (:= (!:G self) worker)
         (xt/x:set-key worker "boot" (eval script))
         (:= (!:G self) previous-self)
         (return worker)))
   (var link
        (worker-link/make-webworker-link
         (@! +webworker-runtime-script+)))
   (var worker ((. link ["create_fn"]) (fn [data] data)))
   (var node (. worker ["boot"]))
   (var out {"node?" (event-node/node? node)
             "id" (. node ["id"])
             "has-query-handler" (xt/x:has-key? (. node ["handlers"]) db-node/ACTION_QUERY)
             "transports" (event-node/list-transports node)
             "listener-count" (xt/x:len (. worker ["listeners"]))
             "revoked-count" (xt/x:len revoked)})
   (:= (!:G Worker) previous-worker)
   (:= (!:G URL) previous-url)
   (:= (!:G Blob) previous-blob)
   (return out))
  => {"node?" true
      "id" "worker-e2e"
      "has-query-handler" true
      "transports" ["host"]
      "listener-count" 1
      "revoked-count" 1})

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

^{:refer js.worker.link/make-sharedworker-link :added "4.1"}
(fact "boots an emitted SharedWorker runtime script end-to-end"
  (notify/wait-on :js
    (var previous-blob (!:G Blob))
    (var previous-url (!:G URL))
    (var previous-shared (!:G SharedWorker))
    (var revoked [])
    (:= (!:G Blob)
        (fn [parts opts]
          (return {"parts" parts
                   "opts" opts})))
    (:= (!:G URL)
        {"createObjectURL" (fn [blob]
                             (return (xt/x:first (. blob ["parts"]))))
         "revokeObjectURL" (fn [url]
                             (revoked.push url))})
    (:= (!:G SharedWorker)
        (fn [script]
          (var starts [])
          (var listeners [])
          (var messages [])
          (var port {"starts" starts
                     "listeners" listeners
                     "messages" messages
                     "connected" false})
          (xt/x:set-key port
                        "start"
                        (fn []
                          (starts.push true)))
          (xt/x:set-key port
                        "postMessage"
                        (fn [msg]
                          (messages.push msg)
                          (xt/for:array [listener listeners]
                            (listener {"data" msg}))))
          (xt/x:set-key port
                        "addEventListener"
                        (fn [event listener capture]
                          (listeners.push listener)
                          (when (and (== event "message")
                                     (not (. port ["connected"])))
                            (:= (. port ["connected"]) true)
                            (var onconnect (. (. shared ["worker"]) ["onconnect"]))
                            (onconnect {"ports" [port]}))))
          (var previous-self (!:G self))
          (var shared {"port" port})
          (var worker {})
          (:= (!:G self) worker)
          (eval script)
          (:= (!:G self) previous-self)
          (xt/x:set-key shared "worker" worker)
          (return shared)))
    (var link
         (worker-link/make-sharedworker-link
          (@! +sharedworker-runtime-script+)))
    ((. link ["create_fn"])
     (fn [data]
       (var out {"signal" (. data ["signal"])
                 "done" (. data ["body"] ["done"])
                 "starts" 2
                 "revoked-count" (xt/x:len revoked)})
       (:= (!:G SharedWorker) previous-shared)
       (:= (!:G URL) previous-url)
       (:= (!:G Blob) previous-blob)
       (repl/notify out))))
  => {"signal" "@cell/::INIT"
      "done" true
      "starts" 2
      "revoked-count" 0})

^{:refer js.worker.link/make-link :added "4.1"}
(fact "dispatches to a runtime-specific worker link helper"

  (!.js
    (xt/x:obj-keys (worker-link/make-link "mock" nil {})))
  => (contains ["create_fn"])

  (!.js
   (worker-link/make-link "unknown" nil {}))
  => (throws))
