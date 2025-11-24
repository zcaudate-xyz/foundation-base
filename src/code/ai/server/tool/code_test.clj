(ns code.ai.server.tool.code-test
  (:require [code.test.task :as task]
            [std.lib :as h]))

(defn run-tests-fn
  [_ {:keys [target args]}]
  (let [target (if target (keyword target) :test)
        args (if args (read-string args) {})
        ;; Capture output
        out-str (with-out-str
                  (try
                    (task/run target args)
                    (catch Throwable e
                      (println "Error running tests:" e))))]
    {:content [{:type "text"
                :text out-str}]
     :isError false}))

(def run-tests-tool
  {:name "code-test"
   :description "Runs tests using code.test"
   :inputSchema {:type "object"
                 :properties {"target" {:type "string" :description "Task target (e.g., test, test:unit)"}
                              "args" {:type "string" :description "EDN string of arguments (e.g., {:only code.ai.server-test})"}}
                 :required []}
   :implementation #'run-tests-fn})
