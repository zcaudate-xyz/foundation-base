(ns js.cell.link-fn
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [js.cell.base-fn :as base-fn]))

(l/script :js
  {:require [[js.cell.link-raw :as link-raw]]})

(defn tmpl-link-route
  "performs a template"
  {:added "4.0"}
  [[sym src]]
  (let [{:api/keys [route static]
         :as entry} @@(resolve src)
        args   (cond-> (nth (:form entry) 2)
                 (not static) rest)]
    (list 'defn.js (with-meta sym (h/template-meta))
          (vec (cons 'link args)) 
          (list 'return (list `link-raw/call
                              'link
                              {:op "route"
                               :route route
                               :body (vec args)})))))

(h/template-ensure
 (mapv (juxt (fn [{:keys [id]}]
               ((str/wrap subs) id 3))
             l/sym-full)
       (l/module-entries :js 'js.cell.base-fn
                         :api/route))
 (h/template-entries [tmpl-link-route]
   [[trigger base-fn/fn-trigger]
    [trigger-async base-fn/fn-trigger-async]
    [final-set base-fn/fn-final-set]
    [final-status base-fn/fn-final-status]
    [eval-enable base-fn/fn-eval-enable]
    [eval-disable base-fn/fn-eval-disable]
    [eval-status base-fn/fn-eval-status]
    [route-list base-fn/fn-route-list]
    [route-entry base-fn/fn-route-entry]
    [ping base-fn/fn-ping]
    [ping-async base-fn/fn-ping-async]
    [echo base-fn/fn-echo]
    [echo-async base-fn/fn-echo-async]
    [error base-fn/fn-error]
    [error-async base-fn/fn-error-async]]))

