(ns indigo.server.test-runner
  (:require [code.test.base.listener :as listener]
            [code.test.compile :as compile]
            [code.test.task :as task]
            [indigo.server.dispatch :as dispatch]
            [std.lib.signal :as signal]))

(defn browser-test-listener
  [{:keys [result] :as data}]
  (let [summary (listener/summarise-verify result)
        msg     {:type "test-result"
                 :data summary}]
    (dispatch/broadcast! msg)))

(defn install-browser-listener []
  (signal/signal:install :test/browser-listener {:test :check} #'browser-test-listener))

(defn run-test [ns var-name]
  (install-browser-listener)
  (let [ns-sym (symbol ns)
        var-sym (symbol var-name)]
    (task/run:load ns-sym)
    (task/run:test ns-sym var-sym)))

(defn run-ns-tests [ns]
  (install-browser-listener)
  (let [ns-sym (symbol ns)]
    (task/run:load ns-sym)
    (task/run:test ns-sym)))
