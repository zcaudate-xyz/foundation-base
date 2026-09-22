(ns foundation-deployment.task-components
  (:require [std.make :as make]
            [std.make.github :as github]))

(def ^:dynamic *exit*
  (fn [status]
    (System/exit status)))

(def +targets+
  {"web-index" ['component.build-web-index 'WEB-INDEX]
   "native-index" ['component.build-native-index 'COMPONENT-NATIVE]
   "native-v1" ['component.build-native-v1 'COMPONENT-NATIVE]
   "web-debug" ['playground.build-web-debug 'PLAYGROUND-WEB-DEBUG]})

^{:public true}
(defn target-config
  "Resolves a generated component project definition by target name."
  [target]
  (let [target-key (if (keyword? target) (name target) (str target))
        [namespace-name var-name] (get +targets+ target-key)]
    (when-not namespace-name
      (throw (ex-info "Unknown component target"
                      {:target target
                       :targets (sort (keys +targets+))})))
    (require namespace-name)
    (if-let [variable (ns-resolve namespace-name var-name)]
      (deref variable)
      (throw (ex-info "Component target definition not found"
                      {:target target
                       :namespace namespace-name
                       :var var-name})))))

^{:public true}
(defn build
  "Builds a generated component project."
  [target]
  (make/build-all (target-config target)))

^{:public true}
(defn init
  "Initialises a generated component project repository."
  ([target] (init target nil))
  ([target message]
   (github/gh-dwim-init (target-config target) message)))

^{:public true}
(defn publish
  "Builds and pushes a generated component project repository."
  ([target] (publish target nil))
  ([target message]
   (github/gh-dwim-push (target-config target) message)))

^{:public true}
(defn task-run
  "Dispatches a generated component deployment task."
  [task target & args]
  (case task
    "build" (build target)
    "init" (apply init target args)
    "publish" (apply publish target args)
    (throw (ex-info "Unknown component task"
                    {:task task
                     :tasks ["build" "init" "publish"]
                     :target target}))))

^{:public true}
(defn -main
  "Runs a generated component task and exits with a shell status."
  [& [task target & args]]
  (try
    (apply task-run task target args)
    (*exit* 0)
    (catch Throwable _
      (*exit* 1))))
