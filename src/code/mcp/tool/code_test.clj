(ns code.mcp.tool.code-test
  (:require [code.mcp.tool.common :as common]
            [code.test.task :as test]))

(def +operations+
  {"run"         test/run
   "run-current" test/run:current
   "run-test"    test/run:test
   "run-load"    test/run:load
   "run-unload"  test/run:unload
   "run-errored" test/run-errored
   "print-options" test/print-options})

(defn code-test-fn
  [_ {:keys [task target options]}]
  (let [task-name   (or task "run")
        func        (get +operations+ task-name)
        target-arg  (common/read-edn target :all)
        options-arg (common/merge-print-options
                     (common/read-edn options {}))]
    (if-not func
      (common/error-response (str "Test task not found: " task-name))
      (try
        (common/response
         (case task-name
           "run-errored"   (func)
           "print-options" (if options
                             (func (common/read-edn options :help))
                             (func))
           "run-current"   (func options-arg)
           "run-test"      (func options-arg)
           (func target-arg options-arg)))
        (catch Throwable t
          (common/error-response
           (str "Error executing code.test task `" task-name "`: "
                (or (.getMessage t) (str t)))))))))

(def code-test-tool
  {:name "code-test"
   :description (str "Run `code.test` operations inside the project JVM. Use this for long-running or "
                     "targeted test autopilot work, especially when you need to execute a single namespace, "
                     "reload test namespaces, rerun only errored tests, or inspect available print modes "
                     "without shelling out to `lein test`.")
   :inputSchema {:type "object"
                 :properties {"task" {:type "string"
                                      :description (str "The `code.test.task` operation to run. "
                                                        "Supported values: run, run-current, run-test, "
                                                        "run-load, run-unload, run-errored, print-options.")}
                              "target" {:type "string"
                                        :description (str "Optional EDN string for the namespace or namespace "
                                                          "set/vector to test, such as `code.mcp.server-test`, "
                                                          "`[code.mcp]`, or `:all`. Ignored by `run-current`, "
                                                          "`run-test`, `run-errored`, and `print-options`.")}
                              "options" {:type "string"
                                         :description (str "Optional EDN string for the options map passed to "
                                                           "`code.test`, such as `{:timeout 1000}` or "
                                                           "`{:files \"src/code/mcp/server.clj\"}`. Tool output "
                                                           "defaults to quiet print settings unless you override them.")}}
                 :required ["task"]}
   :implementation #'code-test-fn})
