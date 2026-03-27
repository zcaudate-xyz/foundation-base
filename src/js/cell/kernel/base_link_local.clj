(ns js.cell.kernel.base-link-local
  (:require [js.cell.kernel.worker-local :as worker-local]
            [std.lang :as l]
            [std.lib.foundation :as f]
            [std.string.wrap]))

(l/script :js
  {:require [[js.cell.kernel.base-link :as link]]})

(defn.js
  trigger
  [link op signal status body]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action",
     :action "@worker/trigger",
     :body [op signal status body]})))

(defn.js
  trigger-async
  [link op signal status body ms]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action",
     :action "@worker/trigger-async",
     :body [op signal status body ms]})))

(defn.js
  set-final-status
  [link suppress]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action",
     :action "@worker/set-final-status",
     :body [suppress]})))

(defn.js
  get-final-status
  [link]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/get-final-status", :body []})))

(defn.js
  set-eval-status
  [link status suppress]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action",
     :action "@worker/set-eval-status",
     :body [status suppress]})))

(defn.js
  get-eval-status
  [link]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/get-eval-status", :body []})))

(defn.js
  get-action-list
  [link]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/get-action-list", :body []})))

(defn.js
  get-action-entry
  [link name]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action",
     :action "@worker/get-action-entry",
     :body [name]})))

(defn.js
  ping
  [link]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/ping", :body []})))

(defn.js
  ping-async
  [link ms]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/ping.async", :body [ms]})))

(defn.js
  echo
  [link arg]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/echo", :body [arg]})))

(defn.js
  echo-async
  [link arg ms]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/echo.async", :body [arg ms]})))

(defn.js
  error
  [link]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/error", :body []})))

(defn.js
  error-async
  [link ms]
  (return
   (js.cell.kernel.base-link/call
    link
    {:op "action", :action "@worker/error.async", :body [ms]})))


;;
;; Generation Template
;;

(defn tmpl-link-action
  "performs a template"
  {:added "4.0"}
  [[sym src]]
  (let [entry @@(resolve src)
        {:cell/keys [action static]} (meta (second (:form entry)))
        args   (cond-> (nth (:form entry) 2)
                 (not static) rest)]
    (list 'defn.js (with-meta sym (f/template-meta))
          (vec (cons 'link args)) 
          (list 'return (list `link/call
                              'link
                              {:op "action"
                               :action action
                               :body (vec args)})))))
  
(def +tmpl-inputs+
  (mapv (juxt (fn [{:keys [id]}]
                ((std.string.wrap/wrap subs) id 3))
              l/sym-full)
        (l/module-entries :js 'js.cell.kernel.worker-state
                          (fn [entry]
                            (:cell/action (meta (second (:form entry))))))))

(def +tmpl-forms+
  (comment
    (f/template-ensure
     +tmpl-inputs+
     (f/template-entries [tmpl-link-action]
       [[trigger js.cell.kernel.worker-state/fn-trigger]
        [trigger-async js.cell.kernel.worker-state/fn-trigger-async]
        [set-final-status js.cell.kernel.worker-state/fn-set-final-status]
        [get-final-status js.cell.kernel.worker-state/fn-get-final-status]
        [set-eval-status js.cell.kernel.worker-state/fn-set-eval-status]
        [get-eval-status js.cell.kernel.worker-state/fn-get-eval-status]
        [get-action-list js.cell.kernel.worker-state/fn-get-action-list]
        [get-action-entry js.cell.kernel.worker-state/fn-get-action-entry]
        [ping js.cell.kernel.worker-state/fn-ping]
        [ping-async js.cell.kernel.worker-state/fn-ping-async]
        [echo js.cell.kernel.worker-state/fn-echo]
        [echo-async js.cell.kernel.worker-state/fn-echo-async]
        [error js.cell.kernel.worker-state/fn-error]
        [error-async js.cell.kernel.worker-state/fn-error-async]]))))

