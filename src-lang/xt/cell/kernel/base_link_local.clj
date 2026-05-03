(ns xt.cell.kernel.base-link-local
  (:require [xt.cell.kernel.inner-local :as inner-local]
            [hara.lang :as l]
            [hara.lang.typed.xtalk :refer [defspec.xt]]
            [std.lib.foundation :as f]
            [std.string.wrap]))

(l/script :xtalk
  {:require [[xt.cell.kernel.base-link :as link] [xt.lang.spec-base :as xt]]})


(defspec.xt trigger
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/str :xt/str :xt/str :xt/any] :xt/any])

(defspec.xt trigger-async
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/str :xt/str :xt/str :xt/any :xt/int] :xt/any])

(defspec.xt set-final-status
  [:fn [xt.cell.kernel.spec/LinkRecord [:xt/maybe :xt/bool]] :xt/any])

(defspec.xt get-final-status
  [:fn [xt.cell.kernel.spec/LinkRecord] :xt/any])

(defspec.xt set-eval-status
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/bool [:xt/maybe :xt/bool]] :xt/any])

(defspec.xt get-eval-status
  [:fn [xt.cell.kernel.spec/LinkRecord] :xt/any])

(defspec.xt get-action-list
  [:fn [xt.cell.kernel.spec/LinkRecord] :xt/any])

(defspec.xt get-action-entry
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/str] :xt/any])

(defspec.xt ping
  [:fn [xt.cell.kernel.spec/LinkRecord] :xt/any])

(defspec.xt ping-async
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/int] :xt/any])

(defspec.xt echo
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/any] :xt/any])

(defspec.xt echo-async
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/any :xt/int] :xt/any])

(defspec.xt error
  [:fn [xt.cell.kernel.spec/LinkRecord] :xt/any])

(defspec.xt error-async
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/int] :xt/any])

(defn.xt
  trigger
  [link op signal status body]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call",
     :action "@cell/trigger",
     :body [op signal status body]})))

(defn.xt
  trigger-async
  [link op signal status body ms]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call",
     :action "@cell/trigger-async",
     :body [op signal status body ms]})))

(defn.xt
  set-final-status
  [link suppress]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call",
     :action "@cell/set-final-status",
     :body [suppress]})))

(defn.xt
  get-final-status
  [link]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/get-final-status", :body []})))

(defn.xt
  set-eval-status
  [link status suppress]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call",
     :action "@cell/set-eval-status",
     :body [status suppress]})))

(defn.xt
  get-eval-status
  [link]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/get-eval-status", :body []})))

(defn.xt
  get-action-list
  [link]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/get-action-list", :body []})))

(defn.xt
  get-action-entry
  [link name]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call",
     :action "@cell/get-action-entry",
     :body [name]})))

(defn.xt
  ping
  [link]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/ping", :body []})))

(defn.xt
  ping-async
  [link ms]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/ping.async", :body [ms]})))

(defn.xt
  echo
  [link arg]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/echo", :body [arg]})))

(defn.xt
  echo-async
  [link arg ms]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/echo.async", :body [arg ms]})))

(defn.xt
  error
  [link]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/error", :body []})))

(defn.xt
  error-async
  [link ms]
  (return
   (xt.cell.kernel.base-link/call
    link
    {:op "call", :action "@cell/error.async", :body [ms]})))


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
    (list 'defn.xt (with-meta sym (f/template-meta))
          (vec (cons 'link args)) 
          (list 'return (list `link/call
                              'link
                              {:op "call"
                               :action action
                               :body (vec args)})))))
  
(def +tmpl-inputs+
  (mapv (juxt (fn [{:keys [id]}]
                ((std.string.wrap/wrap subs) id 3))
              l/sym-full)
        (l/module-entries :xtalk 'xt.cell.kernel.inner-state
                          (fn [entry]
                            (:cell/action (meta (second (:form entry))))))))

(def +tmpl-forms+
  (comment
    (f/template-ensure
     +tmpl-inputs+
     (f/template-entries [tmpl-link-action]
       [[trigger xt.cell.kernel.inner-state/fn-trigger]
        [trigger-async xt.cell.kernel.inner-state/fn-trigger-async]
        [set-final-status xt.cell.kernel.inner-state/fn-set-final-status]
        [get-final-status xt.cell.kernel.inner-state/fn-get-final-status]
        [set-eval-status xt.cell.kernel.inner-state/fn-set-eval-status]
        [get-eval-status xt.cell.kernel.inner-state/fn-get-eval-status]
        [get-action-list xt.cell.kernel.inner-state/fn-get-action-list]
        [get-action-entry xt.cell.kernel.inner-state/fn-get-action-entry]
        [ping xt.cell.kernel.inner-state/fn-ping]
        [ping-async xt.cell.kernel.inner-state/fn-ping-async]
        [echo xt.cell.kernel.inner-state/fn-echo]
        [echo-async xt.cell.kernel.inner-state/fn-echo-async]
        [error xt.cell.kernel.inner-state/fn-error]
        [error-async xt.cell.kernel.inner-state/fn-error-async]]))))
