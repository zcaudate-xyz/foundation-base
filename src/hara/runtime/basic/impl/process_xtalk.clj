(ns hara.runtime.basic.impl.process-xtalk
  (:require [clojure.string]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.spec-xtalk :as spec]
            [std.lib.foundation :as f]))

(def +program-init+
  (common/put-program-options
   :xtalk  {:default  {:oneshot     :chez
                       :verify      :chez
                       :interactive false
                       :ws-client   false}
            :env      {:chez    {:exec "chez"
                                 :pipe true
                                 :stderr true
                                 :raw true
                                 :extension "scm"
                                 :flags  {:oneshot ["-q"]
                                          :verify ["--script"]
                                          :interactive false
                                          :ws-client false}}}}))

(defn read-output
  "read output for scheme"
  {:added "4.0"}
  [{:keys [exit out err]}]
  (if (not-empty err)
    (f/error err)
    (let [out (-> out
                  (clojure.string/replace #"#t" "true")
                  (clojure.string/replace #"#f" "false"))]
      (try (read-string out)
           (catch Throwable t
             (f/wrapped out))))))

(defn transform-form
  "transforms output from shell"
  {:added "4.0"}
  [form {:keys [bulk] :as opts}]
  (if bulk
    `((~'lambda [] ~@form))
    form))

(def +xtalk-oneshot-config+
  (common/set-context-options
   [:xtalk :oneshot :default]
   {:default  {:emit  {:body  {:transform #'transform-form}
                       :json  #'read-output}}}))

(def +xtalk-verify-config+
  (common/set-context-options
   [:xtalk :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +xtalk-oneshot+
  [(rt/install-type!
    :xtalk :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +xtalk-verify+
  [(rt/install-type!
    :xtalk :verify
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])
  
