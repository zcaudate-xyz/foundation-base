(ns js.cell.runtime.emit
  "Host-side worker script emitters for js.cell.kernel."
  (:require [std.lang :as l]))

(defn worker-bootstrap-forms
  "returns the common worker bootstrap forms"
  ([]
   (worker-bootstrap-forms 'worker))
  ([worker-sym]
   [(list 'js.cell.kernel.worker-local/actions-init
          (list 'js.cell.kernel.worker-local/actions-baseline)
          worker-sym)
    (list 'js.cell.kernel.worker-impl/worker-init worker-sym)
    (list 'js.cell.kernel.worker-impl/worker-init-signal worker-sym {:done true})]))

(defn emit-worker-script
  "emits a worker bootstrap script"
  ([forms]
   (emit-worker-script forms :full))
  ([forms layout]
   (l/emit-script (cons 'do forms)
                  {:lang :js
                   :layout layout})))

(defn webworker-forms
  "returns the WebWorker bootstrap forms"
  []
  (worker-bootstrap-forms 'self))

(defn webworker-script
  "emits the WebWorker bootstrap script"
  ([]
   (webworker-script :full))
  ([layout]
   (emit-worker-script (webworker-forms) layout)))

(defn sharedworker-forms
  "returns the SharedWorker bootstrap forms"
  []
  [(list 'var 'boot
         (list 'fn ['port]
               (list 'js.cell.kernel.worker-local/actions-init
                     (list 'js.cell.kernel.worker-local/actions-baseline)
                     'port)
               (list 'js.cell.kernel.worker-impl/worker-init 'port)
               (list 'js.cell.kernel.worker-impl/worker-init-signal
                     'port
                     {:done true})))
   '(:= (. (!:G self) ["onconnect"])
        (fn [e]
          (var port (. e ["ports"] [0]))
          (. port (start))
          (boot port)
          (return port)))])

(defn sharedworker-script
  "emits the SharedWorker bootstrap script"
  ([]
   (sharedworker-script :full))
  ([layout]
   (emit-worker-script (sharedworker-forms) layout)))

(defn node-forms
  "returns the Node worker bootstrap forms"
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

(defn node-script
  "emits the Node worker bootstrap script"
  ([]
   (node-script :full))
  ([layout]
   (emit-worker-script (node-forms) layout)))
