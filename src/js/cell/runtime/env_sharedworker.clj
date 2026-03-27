(ns js.cell.runtime.env-sharedworker
  "SharedWorker runtime script entrypoint for js.cell.kernel."
  (:require [js.cell.kernel.worker-impl]
            [js.cell.kernel.worker-local]
            [std.lang :as l]))

(l/script :js
  {})

(defn- forms*
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

(defn script-source
  "emits the SharedWorker bootstrap script"
  ([]
   (script-source :full))
  ([layout]
   (l/emit-script
    (cons 'do (forms*))
    {:lang :js
     :layout layout})))

(defn.js forms
  "returns the SharedWorker bootstrap forms"
  []
  (return (@! (mapv pr-str (forms*)))))

(defn.js script
  "emits the SharedWorker bootstrap script"
  []
  (return (@! (script-source))))

(defn.js runtime-script
  "emits the SharedWorker bootstrap script"
  []
  (return (-/script)))
