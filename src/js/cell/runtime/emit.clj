(ns js.cell.runtime.emit
  "Host-side worker script emitters for js.cell.kernel."
  (:require [js.cell.runtime.env-node]
            [js.cell.runtime.env-sharedworker]
            [js.cell.runtime.env-webworker]
            [std.lang :as l]))

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
  '[(js.cell.runtime.env-webworker/runtime-init)])

(defn webworker-script
  "emits the WebWorker bootstrap script"
  ([]
   (webworker-script :full))
  ([layout]
   (emit-worker-script (webworker-forms) layout)))

(defn sharedworker-forms
  "returns the SharedWorker bootstrap forms"
  []
  '[(js.cell.runtime.env-sharedworker/runtime-init)])

(defn sharedworker-script
  "emits the SharedWorker bootstrap script"
  ([]
   (sharedworker-script :full))
  ([layout]
   (emit-worker-script (sharedworker-forms) layout)))

(defn node-forms
  "returns the Node worker bootstrap forms"
  []
  '[(js.cell.runtime.env-node/runtime-init)])

(defn node-script
  "emits the Node worker bootstrap script"
  ([]
   (node-script :full))
  ([layout]
   (emit-worker-script (node-forms) layout)))
