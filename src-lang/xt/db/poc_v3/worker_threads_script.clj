(ns xt.db.poc-v3.worker-threads-script
  "CLJ-side script generation for the `xt.db.poc-v3.worker-threads` worker.

   Produces two files under the supplied root:
     - worker.mjs   tiny ESM wrapper that captures parentPort
     - bundle.mjs   self-contained xtalk bootstrap

   Keeping the wrapper separate lets the bundle be a plain ESM module while
   still receiving the worker's MessagePort."
  (:require [hara.lang :as l]
            [clojure.java.io :as io]
            [xt.lang.spec-base]
            [xt.lang.spec-promise]
            [xt.lang.common-data]
            [xt.lang.common-lib]
            [xt.event.base-model]
            [xt.substrate]
            [xt.substrate.page-core]
            [xt.substrate.page-proxy]
            [xt.substrate.transport-browser]
            [xt.db.node.adaptor-base]
            [xt.db.node.adaptor-client]))

(defn worker-threads-script-str
  "Returns the worker bootstrap as a self-contained JavaScript string.
   Expects `globalThis.__parentPort` to hold the worker's MessagePort."
  []
  (l/emit-script
   '(do
      (var parent-port (. globalThis ["__parentPort"]))

      ;; adapter object with the MessagePort-like interface expected by
      ;; transport-browser/self-endpoint
      (var worker
           {"postMessage" (fn [data]
                            (. parent-port (postMessage data)))
            "addEventListener" (fn [event listener capture]
                                 (when (== event "message")
                                   (. parent-port (on "message"
                                                      (fn [data]
                                                        (listener {"data" data}))))))})

      (var node (xt.substrate/node-create {"id" "poc-v3-worker-threads-server"
                                           "spaces" {"room/a" {"state" {}}}}))

      ;; install the db adaptor request handlers
      (xt.db.node.adaptor-base/init-handlers node)

      ;; wrap init-adaptor so errors are serialised back to the client
      (xt.substrate/register-handler
       node
       "@xt.db/init-adaptor"
       (fn [space args request node]
         (return
          (. (xt.db.node.adaptor-base/init-adaptor-main
              node
              (. args [0])
              (. args [1])
              (. args [2]))
             (then (fn [_]
                     (return {"status" "ok"})))
             (catch (fn [err]
                      (return {"status" "error"
                               "message" (. err ["message"])
                               "stack" (. err ["stack"])}))))))
       nil)

      ;; allow remote clients to open proxy groups on this node
      (xt.substrate.page-proxy/install node)

      ;; expose the node over parentPort
      (. (xt.substrate.transport-browser/boot-self
          node
          {"transport_id" "host"
           "target" worker
           "ready" {"signal" "ready"
                    "transport" "worker_threads"
                    "worker" "poc-v3-worker-threads-server"}})
         (then (fn [_]
                 (return node)))))
   {:lang :js
    :layout :full}))

(defn write-worker-script!
  "Writes the worker wrapper and bundle under `root`/poc-v3-worker-threads.
   Returns the absolute path to the entry worker.mjs file."
  [root]
  (let [dir (io/file root "poc-v3-worker-threads")]
    (.mkdirs dir)
    (spit (io/file dir "bundle.mjs")
          (worker-threads-script-str))
    (spit (io/file dir "worker.mjs")
          (str "import { parentPort } from 'node:worker_threads';\n"
               "globalThis.__parentPort = parentPort;\n"
               "await import('./bundle.mjs');\n"))
    (.getCanonicalPath (io/file dir "worker.mjs"))))
