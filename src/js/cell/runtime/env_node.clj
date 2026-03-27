(ns js.cell.runtime.env-node
  "Node worker runtime script entrypoint for js.cell.kernel."
  (:require [js.cell.kernel.worker-impl]
            [js.cell.kernel.worker-local]
            [std.lang :as l]))

(l/script :js
  {})

(defn- forms*
  []
  ['(var #{parentPort} (require "worker_threads"))
   '(var worker
         {:postMessage (fn [data]
                         (. parentPort (postMessage data)))
          :addEventListener (fn [event listener]
                              (when (== event "message")
                                (. parentPort (on "message"
                                                  (fn [data]
                                                    (listener {:data data}))))))})
   '(js.cell.kernel.worker-local/actions-init
     (js.cell.kernel.worker-local/actions-baseline)
     worker)
   '(js.cell.kernel.worker-impl/worker-init worker)
   '(js.cell.kernel.worker-impl/worker-init-signal worker {:done true})])

(defn script-source
  "emits the Node worker bootstrap script"
  ([]
   (script-source :full))
  ([layout]
   (l/emit-script
    (cons 'do (forms*))
    {:lang :js
     :layout layout})))

(defn.js forms
  "returns the Node worker bootstrap forms"
  []
  (return (@! (mapv pr-str (forms*)))))

(defn.js script
  "emits the Node worker bootstrap script"
  []
  (return (@! (script-source))))

(defn.js runtime-script
  "emits the Node worker bootstrap script"
  []
  (return (-/script)))
