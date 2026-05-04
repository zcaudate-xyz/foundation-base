(ns hara.runtime.basic.impl.process-bash
  (:require [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.spec-bash :as spec]))

(def +program-init+
  (common/put-program-options
   :bash      {:default  {:oneshot     :bash
                          :ws-client   false}
               :env      {:bash      {:exec "bash"
                                      :flags {:oneshot ["-c"]
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

(def +bash-oneshot+
  [(rt/install-type!
    :bash :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])
