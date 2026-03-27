(ns js.cell.runtime.env-webworker
  "WebWorker runtime script entrypoint for js.cell.kernel."
  (:require [js.cell.kernel.worker-impl]
            [js.cell.kernel.worker-local]
            [std.lang :as l]))

(l/script :js
  {})

(defn- forms*
  []
  [(list 'js.cell.kernel.worker-local/actions-init
         (list 'js.cell.kernel.worker-local/actions-baseline)
         'self)
   (list 'js.cell.kernel.worker-impl/worker-init 'self)
   (list 'js.cell.kernel.worker-impl/worker-init-signal 'self {:done true})])

(defn script-source
  "emits the WebWorker bootstrap script"
  ([]
   (script-source :full))
  ([layout]
   (l/emit-script
    (cons 'do (forms*))
    {:lang :js
     :layout layout})))

(defn.js forms
  "returns the WebWorker bootstrap forms"
  []
  (return (@! (mapv pr-str (forms*)))))

(defn.js script
  "emits the WebWorker bootstrap script"
  []
  (return (@! (script-source))))

(defn.js runtime-script
  "emits the WebWorker bootstrap script"
  []
  (return (-/script)))
