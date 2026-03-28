(ns js.cell.runtime.env-sharedworker
  "SharedWorker runtime script entrypoint for js.cell.kernel."
  (:require [js.cell]
            [js.cell.kernel.worker-impl]
            [std.lang :as l]))

(l/script :js
  {:require [[js.cell :as cl]
             [js.cell.kernel.worker-impl :as worker-impl]]})

(defn.js init-port
  "boots kernel actions on a SharedWorker port"
  [port]
  (cl/actions-init {} port)
  (worker-impl/worker-init port)
  (worker-impl/worker-init-signal port {:done true})
  (return port))

(defn.js runtime-init
  "boots js.cell inside a SharedWorker"
  []
  (:= (. (!:G self) ["onconnect"])
      (fn [e]
        (var port (. e ["ports"] [0]))
        (. port (start))
        (-/init-port port)
        (return port)))
  (return true))
