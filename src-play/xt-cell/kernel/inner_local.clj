(ns xt.cell.kernel.inner-local
  (:require [hara.lang :as l]
            [hara.typed :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.cell.kernel.base-util :as util]
             [xt.cell.kernel.inner-state :as state]
             [xt.lang.spec-base :as xt]]})

(defspec.xt actions-baseline
  [:fn [[:xt/maybe :xt/any]] xt.cell.kernel.spec/WorkerActionMap])

(defspec.xt actions-init
  [:fn [xt.cell.kernel.spec/WorkerActionMap :xt/any] :xt/any])

(defn.xt actions-baseline
  "returns the base actions"
  {:added "4.0"}
  [worker]
  (return
   (tab
      ["@cell/trigger"
       {:handler
        xt.cell.kernel.inner-state/fn-trigger,
        :static false,
        :is-async false,
        :args ["op" "signal" "status" "body"]}]
      ["@cell/trigger-async"
       {:handler
        xt.cell.kernel.inner-state/fn-trigger-async,
        :static false,
        :is-async true,
        :args ["op" "signal" "status" "body" "ms"]}]
      ["@cell/set-final-status"
       {:handler
        xt.cell.kernel.inner-state/fn-set-final-status,
        :static false,
        :is-async false,
        :args ["suppress"]}]
      ["@cell/get-final-status"
       {:handler
        xt.cell.kernel.inner-state/fn-get-final-status,
        :static false,
        :is-async false,
        :args []}]
      ["@cell/set-eval-status"
       {:handler
        xt.cell.kernel.inner-state/fn-set-eval-status,
        :static false,
        :is-async false,
        :args ["status" "suppress"]}]
     ["@cell/get-eval-status"
      {:handler xt.cell.kernel.inner-state/fn-get-eval-status,
       :static true,
       :is-async false,
       :args []}]
     ["@cell/get-action-list"
      {:handler xt.cell.kernel.inner-state/fn-get-action-list,
       :static true,
       :is-async false,
       :args []}]
     ["@cell/get-action-entry"
      {:handler xt.cell.kernel.inner-state/fn-get-action-entry,
       :static true,
       :is-async false,
       :args ["name"]}]
     ["@cell/ping"
      {:handler xt.cell.kernel.inner-state/fn-ping,
       :static true,
       :is-async false,
       :args []}]
     ["@cell/ping.async"
      {:handler xt.cell.kernel.inner-state/fn-ping-async,
       :static true,
       :is-async true,
       :args ["ms"]}]
     ["@cell/echo"
      {:handler xt.cell.kernel.inner-state/fn-echo,
       :static true,
       :is-async false,
       :args ["arg"]}]
     ["@cell/echo.async"
      {:handler xt.cell.kernel.inner-state/fn-echo-async,
       :static true,
       :is-async true,
       :args ["arg" "ms"]}]
     ["@cell/error"
      {:handler xt.cell.kernel.inner-state/fn-error,
       :static true,
       :is-async false,
       :args []}]
     ["@cell/error.async"
      {:handler xt.cell.kernel.inner-state/fn-error-async,
       :static true,
       :is-async true,
       :args ["ms"]}])))

(defn.xt actions-init
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (return
   (state/set-actions (xt/x:obj-assign (-/actions-baseline worker)
                                       (or actions {}))
                       worker)))


(comment
  ;;
  ;; Generation Template
  ;;

  (defn tmpl-baseline-action
    "templates a baseline function"
    {:added "4.0"}
    [entry]
    (let [{:cell/keys [action static is-async]} (meta (second (:form entry)))
          args    (nth (:form entry) 2)]
      [action {:handler (l/sym-full entry)
               :static (true? static)
               :is-async (true? is-async)
                :args  (mapv str (if static
                                   args
                                   (rest args)))}]))

  (def +baselines+
    (mapv tmpl-baseline-action
          (l/module-entries :xtalk 'xt.cell.kernel.inner-state
                            (fn [entry]
                              (:cell/action (meta (second (:form entry)))))))))
