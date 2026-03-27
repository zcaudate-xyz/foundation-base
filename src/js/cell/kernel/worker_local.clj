(ns js.cell.kernel.worker-local
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [js.cell.kernel.base-util :as util]
             [js.cell.kernel.worker-state :as state]
             [xt.lang.base-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]]})

(defn tmpl-baseline-action
  "templates a baseline function"
  {:added "4.0"}
  [entry]
  (let [{:cell/keys [action static async]} (meta (second (:form entry)))
        handler (cond->> (l/sym-full entry)
                  (not static) (list `state/fn-self))
        args    (nth (:form entry) 2)]
    [action {:handler handler
             :async (true? async)
             :args  (mapv str (if static
                                args
                                (rest args)))}]))

(def +baselines+
  (mapv tmpl-baseline-action
        (l/module-entries :js 'js.cell.kernel.worker-state
                          (fn [entry]
                            (:cell/action (meta (second (:form entry))))))))

(defn.js actions-baseline
  "returns the base actions"
  {:added "4.0"}
  []
  (return (@! (cons 'tab +baselines+))))

(defn.js actions-init
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (cond worker
        (do (k/set-key worker "actions" actions)
            (return worker))
        
        :else
        (return (state/WORKER_ACTIONS-reset
                 (k/obj-assign (-/actions-base)
                               actions)))))

