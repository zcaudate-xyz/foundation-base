(ns xt.db.poc-v3.worker-threads-script
  "CLJ-side script generation for the `xt.db.poc-v3.worker-threads` worker.

   Produces two files under the supplied root:
     - worker.mjs   tiny ESM wrapper that captures parentPort
     - bundle.mjs   self-contained xtalk bootstrap

   The worker bundle is fully self-contained: it embeds the scratch-v3 schema
   and lookup, initialises the Supabase primary + SQLite cache adaptor, and
   exposes the node over parentPort. The client only has to connect and attach
   proxy models."
  (:require [hara.lang :as l]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [scaffold.supabase.local-min :as local-min]
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
            [xt.db.node.adaptor-client]
            [xt.db.poc-v3.sharedworker :as sharedworker]))

(l/script :js
  {:require [[xt.db.poc-v3.sharedworker :as sharedworker]]})

(defn worker-threads-script-str
  "Returns the worker bootstrap as a self-contained JavaScript string.
   Expects `globalThis.__parentPort` to hold the worker's MessagePort."
  []
  (let [config-anon (json/write-str local-min/+config-supabase-anon+)]
    (-> (l/emit-script
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

            ;; allow remote clients to open proxy groups on this node
            (xt.substrate.page-proxy/install node)

            ;; initialise the scratch-v3 adaptor on the worker side
            (var config {"primary" {"id" "db/primary"
                                    "type" "supabase"
                                    "defaults" __CONFIG_ANON__}
                         "caching" {"id" "db/caching"
                                    "type" "sqlite"
                                    "defaults" {}}})

            ;; expose the node over parentPort once the adaptor is ready
            (. (xt.db.node.adaptor-base/init-base-main
                node
                config
                xt.db.poc-v3.sharedworker/Schema
                xt.db.poc-v3.sharedworker/Lookup)
               (then (fn [_]
                       (. (xt.substrate.transport-browser/boot-self
                           node
                           {"transport_id" "host"
                            "target" worker
                            "ready" {"signal" "ready"
                                     "transport" "worker_threads"
                                     "worker" "poc-v3-worker-threads-server"}})
                          (then (fn [_]
                                  (return node)))))))
            node)
         {:lang :js
          :layout :full})
        (str/replace "__CONFIG_ANON__" config-anon))))

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
