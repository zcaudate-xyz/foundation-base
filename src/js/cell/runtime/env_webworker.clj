(ns js.cell.runtime.env-webworker
  "WebWorker runtime script entrypoint for js.cell.kernel."
  (:require [js.cell]
            [js.cell.kernel.worker-impl]
            [std.lang :as l]))

(l/script :js
  {:require [[js.cell :as cl]
             [js.cell.kernel.worker-impl :as worker-impl]]})

(defn.js init-worker
  "boots kernel actions on a WebWorker"
  [worker]
  (cl/actions-init {} worker)
  (worker-impl/worker-init worker)
  (worker-impl/worker-init-signal worker {:done true})
  (return worker))

(defn.js runtime-init
  "boots js.cell inside a WebWorker"
  []
  (return (-/init-worker self)))
