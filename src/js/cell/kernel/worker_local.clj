(ns js.cell.kernel.worker-local
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :js
  {:require [[js.core :as j]
             [js.cell.kernel.base-util :as util]
             [js.cell.kernel.worker-state :as state]
             [xt.lang.common-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]]})


(defspec.xt actions-baseline
  [:fn [] js.cell.kernel.spec/WorkerActionMap])

(defspec.xt actions-init
  [:fn [js.cell.kernel.spec/WorkerActionMap :xt/any] :xt/any])

(defn.js actions-baseline
  "returns the base actions"
  {:added "4.0"}
  []
  ;; (@! (cons 'tab +baselines+))
  (return
   (tab
    ["@worker/trigger"
     {:handler
      (js.cell.kernel.worker-state/fn-self
       js.cell.kernel.worker-state/fn-trigger),
      :is-async false,
      :args ["op" "signal" "status" "body"]}]
    ["@worker/trigger-async"
     {:handler
      (js.cell.kernel.worker-state/fn-self
       js.cell.kernel.worker-state/fn-trigger-async),
      :is-async true,
      :args ["op" "signal" "status" "body" "ms"]}]
    ["@worker/set-final-status"
     {:handler
      (js.cell.kernel.worker-state/fn-self
       js.cell.kernel.worker-state/fn-set-final-status),
      :is-async false,
      :args ["suppress"]}]
    ["@worker/get-final-status"
     {:handler
      (js.cell.kernel.worker-state/fn-self
       js.cell.kernel.worker-state/fn-get-final-status),
      :is-async false,
      :args []}]
    ["@worker/set-eval-status"
     {:handler
      (js.cell.kernel.worker-state/fn-self
       js.cell.kernel.worker-state/fn-set-eval-status),
      :is-async false,
      :args ["status" "suppress"]}]
    ["@worker/get-eval-status"
     {:handler js.cell.kernel.worker-state/fn-get-eval-status,
      :is-async false,
      :args []}]
    ["@worker/get-action-list"
     {:handler js.cell.kernel.worker-state/fn-get-action-list,
      :is-async false,
      :args []}]
    ["@worker/get-action-entry"
     {:handler js.cell.kernel.worker-state/fn-get-action-entry,
      :is-async false,
      :args ["name"]}]
    ["@worker/ping"
     {:handler js.cell.kernel.worker-state/fn-ping,
      :is-async false,
      :args []}]
    ["@worker/ping.async"
     {:handler js.cell.kernel.worker-state/fn-ping-async,
      :is-async true,
      :args ["ms"]}]
    ["@worker/echo"
     {:handler js.cell.kernel.worker-state/fn-echo,
      :is-async false,
      :args ["arg"]}]
    ["@worker/echo.async"
     {:handler js.cell.kernel.worker-state/fn-echo-async,
      :is-async true,
      :args ["arg" "ms"]}]
    ["@worker/error"
     {:handler js.cell.kernel.worker-state/fn-error,
      :is-async false,
      :args []}]
    ["@worker/error.async"
     {:handler js.cell.kernel.worker-state/fn-error-async,
      :is-async true,
      :args ["ms"]}])))

(defn.js actions-init
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (return
   (state/set-actions (k/obj-assign (-/actions-baseline)
                                    actions)
                      worker)))

;;
;; Generation Template
;;

(defn tmpl-baseline-action
  "templates a baseline function"
  {:added "4.0"}
  [entry]
  (let [{:cell/keys [action static is-async]} (meta (second (:form entry)))
        handler (cond->> (l/sym-full entry)
                  (not static) (list `state/fn-self))
        args    (nth (:form entry) 2)]
    [action {:handler handler
             :is-async (true? is-async)
             :args  (mapv str (if static
                                args
                                (rest args)))}]))

(def +baselines+
  (mapv tmpl-baseline-action
        (l/module-entries :js 'js.cell.kernel.worker-state
                          (fn [entry]
                            (:cell/action (meta (second (:form entry))))))))
