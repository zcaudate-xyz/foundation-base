(ns js.cell.runtime.env-node
  "Node worker runtime script entrypoint for js.cell.kernel."
  (:require [js.cell.kernel.worker-impl]
            [js.cell.kernel.worker-local]
            [std.lang :as l]))

(l/script :js
  {:require [[js.cell.kernel.worker-impl :as worker-impl]
             [js.cell.kernel.worker-local :as worker-local]]})

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
  "boots kernel actions on a Node worker adapter"
  [worker]
  (:= (!:G __CELL_WORKER) worker)
  (worker-local/actions-init (worker-local/actions-baseline) worker)
  (worker-impl/worker-init worker)
  (worker-impl/worker-init-signal worker {:done true})
  (return worker))

(defn.js runtime-init
  "boots js.cell inside a Node worker thread"
  []
  (return (-/init-worker (-/make-node-worker))))
