(ns lang.runtime.basic.impl.process-bash
  (:require [lang.runtime.basic.type-common :as common]
            [lang.runtime.basic.type-oneshot :as oneshot]
            [lang.runtime.basic.type-verify :as type-verify]
            [lang.core.impl :as impl]
            [lang.core.runtime :as rt]
            [lang.model.spec-bash :as spec]))

(def +program-init+
  (common/put-program-options
   :bash      {:default  {:oneshot     :bash
                          :verify      :bash
                          :ws-client   false}
               :env      {:bash      {:exec "bash"
                                      :extension "sh"
                                      :flags {:oneshot ["-c"]
                                              :verify ["-n"]
                                              :ws-client false}}}}))

(defn- default-body-transform
  [input mopts]
  (rt/return-transform
   input mopts
   {:format-fn identity
    :wrap-fn (fn [forms]
               (apply list 'do forms))}))

(def +bash-oneshot-config+
  (common/put-context-options
   [:bash :oneshot]
   {:default  {:emit  {:body  {:transform #'default-body-transform}}
               :raw   true
               :json  false}}))

(def +bash-verify-config+
  (common/set-context-options
   [:bash :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +bash-oneshot+
  [(rt/install-type!
    :bash :oneshot
    {:type :lang/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +bash-verify+
  [(rt/install-type!
    :bash :verify
    {:type :lang/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])
