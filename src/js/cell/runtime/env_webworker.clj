(ns js.cell.runtime.env-webworker
  "WebWorker runtime script entrypoint for js.cell.kernel."
  (:require [js.cell.kernel.worker-impl]
            [js.cell.kernel.worker-local]
            [std.lang :as l]))

(l/script :js
  {:require [[js.cell.kernel.worker-impl :as worker-impl]
             [js.cell.kernel.worker-local :as worker-local]]})

(defn.js init-worker
  "boots kernel actions on a WebWorker"
  [worker]
  (worker-local/actions-init (worker-local/actions-baseline) worker)
  (worker-impl/worker-init worker)
  (worker-impl/worker-init-signal worker {:done true})
  (return worker))

(defn.js runtime-init
  "boots js.cell inside a WebWorker"
  []
  (return (-/init-worker self)))
