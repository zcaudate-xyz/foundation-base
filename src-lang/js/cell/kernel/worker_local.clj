(ns js.cell.kernel.worker-local
  (:require [hara.lang :as l]
            [hara.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :js
  {:require [[js.core :as j] [js.cell.kernel.base-util :as util] [js.cell.kernel.worker-state :as state] [xt.lang.spec-base :as xt] [xt.lang.common-resource :as rt :with [defsingleton.js]]]})


(defspec.xt actions-baseline
  [:fn [[:xt/maybe :xt/any]] js.cell.kernel.spec/WorkerActionMap])

(defspec.xt actions-init
  [:fn [js.cell.kernel.spec/WorkerActionMap :xt/any] :xt/any])

(defn.js actions-baseline
  "returns the base actions"
  {:added "4.0"}
  [worker]
  (var bind-handler (fn [f]
                      (return (:? worker
                                  (state/fn-bind worker f)
                                  (state/fn-self f)))))
  ;; (@! (cons 'tab +baselines+))
  (return
   (tab
     ["@cell/trigger"
      {:handler
       (bind-handler js.cell.kernel.worker-state/fn-trigger),
       :is-async false,
       :args ["op" "signal" "status" "body"]}]
     ["@cell/trigger-async"
      {:handler
       (bind-handler js.cell.kernel.worker-state/fn-trigger-async),
       :is-async true,
       :args ["op" "signal" "status" "body" "ms"]}]
     ["@cell/set-final-status"
      {:handler
       (bind-handler js.cell.kernel.worker-state/fn-set-final-status),
       :is-async false,
       :args ["suppress"]}]
     ["@cell/get-final-status"
      {:handler
       (bind-handler js.cell.kernel.worker-state/fn-get-final-status),
       :is-async false,
       :args []}]
     ["@cell/set-eval-status"
      {:handler
       (bind-handler js.cell.kernel.worker-state/fn-set-eval-status),
       :is-async false,
       :args ["status" "suppress"]}]
    ["@cell/get-eval-status"
     {:handler js.cell.kernel.worker-state/fn-get-eval-status,
      :is-async false,
      :args []}]
    ["@cell/get-action-list"
     {:handler js.cell.kernel.worker-state/fn-get-action-list,
      :is-async false,
      :args []}]
    ["@cell/get-action-entry"
     {:handler js.cell.kernel.worker-state/fn-get-action-entry,
      :is-async false,
      :args ["name"]}]
    ["@cell/ping"
     {:handler js.cell.kernel.worker-state/fn-ping,
      :is-async false,
      :args []}]
    ["@cell/ping.async"
     {:handler js.cell.kernel.worker-state/fn-ping-async,
      :is-async true,
      :args ["ms"]}]
    ["@cell/echo"
     {:handler js.cell.kernel.worker-state/fn-echo,
      :is-async false,
      :args ["arg"]}]
    ["@cell/echo.async"
     {:handler js.cell.kernel.worker-state/fn-echo-async,
      :is-async true,
      :args ["arg" "ms"]}]
    ["@cell/error"
     {:handler js.cell.kernel.worker-state/fn-error,
      :is-async false,
      :args []}]
    ["@cell/error.async"
     {:handler js.cell.kernel.worker-state/fn-error-async,
      :is-async true,
      :args ["ms"]}])))

(defn.js actions-init
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (return
   (state/set-actions (xt/x:obj-assign (-/actions-baseline worker)
                                       (or actions {}))
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
