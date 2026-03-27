(ns js.cell.link-fn
  (:require [js.cell.kernel.worker-fn :as base-fn]
            [std.lang :as l]
            [std.lib.foundation :as f]
            [std.string.wrap]))

(l/script :js
  {:require [[js.cell.link-raw :as link-raw]]})

(defn tmpl-link-action
  "performs a template"
  {:added "4.0"}
  [[sym src]]
  (let [{:cell/keys [action static]
         :as entry} @@(resolve src)
        args   (cond-> (nth (:form entry) 2)
                 (not static) rest)]
    (list 'defn.js (with-meta sym (f/template-meta))
          (vec (cons 'link args)) 
          (list 'return (list `link-raw/call
                              'link
                              {:op "action"
                               :action action
                               :body (vec args)})))))

(f/template-ensure
 (mapv (juxt (fn [{:keys [id]}]
               ((std.string.wrap/wrap subs) id 3))
             l/sym-full)
       (l/module-entries :js 'js.cell.kernel.worker-fn
                         :cell/action))
 (f/template-entries [tmpl-link-action]
   [[trigger base-fn/fn-trigger]
    [trigger-async base-fn/fn-trigger-async]
    [final-set base-fn/fn-final-set]
    [final-status base-fn/fn-final-status]
    [eval-enable base-fn/fn-eval-enable]
    [eval-disable base-fn/fn-eval-disable]
    [eval-status base-fn/fn-eval-status]
    [action-list base-fn/fn-action-list]
    [action-entry base-fn/fn-action-entry]
    [ping base-fn/fn-ping]
    [ping-async base-fn/fn-ping-async]
    [echo base-fn/fn-echo]
    [echo-async base-fn/fn-echo-async]
    [error base-fn/fn-error]
    [error-async base-fn/fn-error-async]]))

