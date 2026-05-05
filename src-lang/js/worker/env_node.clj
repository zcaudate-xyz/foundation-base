(ns js.worker.env-node
  "Node worker runtime script entrypoint for js.worker."
  (:require [hara.lang :as l]))

(l/script :js
  {:require []})

(defn.js make-node-worker
  "creates a worker-like adapter around parentPort"
  []
  (var #{parentPort} (require "worker_threads"))
  (return {:postMessage (fn [data]
                          (. parentPort (postMessage data)))
           :addEventListener (fn [event listener]
                               (when (== event "message")
                                 (. parentPort (on "message"
                                                   (fn [data]
                                                     (listener {:data data}))))))}))

(defn.js init-worker
  "boots a Node worker adapter"
  [worker]
  (:= (!:G __CELL_WORKER) worker)
  (. worker (addEventListener
             "message"
             (fn [e]
               (return e))))
  (. worker (postMessage {:signal "@cell/::INIT"
                          :body {:done true}}))
  (return worker))

(defn.js runtime-init
  "boots js.worker inside a Node worker thread"
  []
  (return (-/init-worker (-/make-node-worker))))
