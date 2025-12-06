(ns indigo.server.test-runner
  (:require [indigo.server.context :as context]
            [code.test.base.listener :as listener]
            [code.test.task :as task]
            [code.test.compile :as compile]
            [std.lib :as h]
            [std.json :as json]))

(defn browser-test-listener
  [{:keys [result] :as data}]
  (let [summary (listener/summarise-verify result)
        msg     (json/write {:type "test-result"
                             :data summary})]
    (context/broadcast! msg)))

(defn install-browser-listener []
  (h/signal:install :test/browser-listener {:test :check} #'browser-test-listener))

(defn run-test [ns var-name]
  (install-browser-listener)
  (let [ns-sym (symbol ns)
        var-sym (symbol var-name)]
    (require ns-sym)
    (task/run:test ns-sym var-sym)))

(defn run-ns-tests [ns]
  (install-browser-listener)
  (let [ns-sym (symbol ns)]
    (require ns-sym)
    (task/run:test ns-sym)))
