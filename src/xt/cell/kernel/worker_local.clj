(ns xt.cell.kernel.worker-local
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.cell.kernel.base-util :as util]
             [xt.cell.kernel.worker-state :as state]
             [xt.lang.common-runtime :as rt]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})


(defspec.xt actions-baseline
  [:fn [] xt.cell.kernel.spec/WorkerActionMap])

(defspec.xt actions-init
  [:fn [xt.cell.kernel.spec/WorkerActionMap :xt/any] :xt/any])

(defn.xt actions-baseline
  "returns the base actions"
  {:added "4.0"}
  []
  ;; (@! (cons 'tab +baselines+))
  (return
   (tab
    ["@worker/trigger"
     {:handler
      (xt.cell.kernel.worker-state/fn-self
       xt.cell.kernel.worker-state/fn-trigger),
      :is-async false,
      :args ["op" "signal" "status" "body"]}]
    ["@worker/trigger-async"
     {:handler
      (xt.cell.kernel.worker-state/fn-self
       xt.cell.kernel.worker-state/fn-trigger-async),
      :is-async true,
      :args ["op" "signal" "status" "body" "ms"]}]
    ["@worker/set-final-status"
     {:handler
      (xt.cell.kernel.worker-state/fn-self
       xt.cell.kernel.worker-state/fn-set-final-status),
      :is-async false,
      :args ["suppress"]}]
    ["@worker/get-final-status"
     {:handler
      (xt.cell.kernel.worker-state/fn-self
       xt.cell.kernel.worker-state/fn-get-final-status),
      :is-async false,
      :args []}]
    ["@worker/set-eval-status"
     {:handler
      (xt.cell.kernel.worker-state/fn-self
       xt.cell.kernel.worker-state/fn-set-eval-status),
      :is-async false,
      :args ["status" "suppress"]}]
    ["@worker/get-eval-status"
     {:handler xt.cell.kernel.worker-state/fn-get-eval-status,
      :is-async false,
      :args []}]
    ["@worker/get-action-list"
     {:handler xt.cell.kernel.worker-state/fn-get-action-list,
      :is-async false,
      :args []}]
    ["@worker/get-action-entry"
     {:handler xt.cell.kernel.worker-state/fn-get-action-entry,
      :is-async false,
      :args ["name"]}]
    ["@worker/ping"
     {:handler xt.cell.kernel.worker-state/fn-ping,
      :is-async false,
      :args []}]
    ["@worker/ping.async"
     {:handler xt.cell.kernel.worker-state/fn-ping-async,
      :is-async true,
      :args ["ms"]}]
    ["@worker/echo"
     {:handler xt.cell.kernel.worker-state/fn-echo,
      :is-async false,
      :args ["arg"]}]
    ["@worker/echo.async"
     {:handler xt.cell.kernel.worker-state/fn-echo-async,
      :is-async true,
      :args ["arg" "ms"]}]
    ["@worker/error"
     {:handler xt.cell.kernel.worker-state/fn-error,
      :is-async false,
      :args []}]
    ["@worker/error.async"
     {:handler xt.cell.kernel.worker-state/fn-error-async,
      :is-async true,
      :args ["ms"]}])))

(defn.xt actions-init
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (return
   (state/set-actions (xtd/obj-assign (-/actions-baseline)
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
        (l/module-entries :xtalk 'xt.cell.kernel.worker-state
                          (fn [entry]
                            (:cell/action (meta (second (:form entry))))))))
