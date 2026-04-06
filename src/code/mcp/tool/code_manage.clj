(ns code.mcp.tool.code-manage
  (:require [code.manage :as manage]
            [code.mcp.tool.common :as common]
            [std.task :as task]))

(defn manage-fn
  [_ {:keys [task target options]}]
  (let [func (get manage/+tasks+ (keyword task))
        target-arg (common/read-edn target)
        options-arg (common/merge-print-options (common/read-edn options {}))
        args [target-arg options-arg]]
    (if func
      (let [raw-result (try
                         (apply func args)
                         (catch Throwable e
                           (println "Error executing task:" e)
                           (.printStackTrace e)
                            {:error (.getMessage e)}))
            raw-result (if (and (map? raw-result)
                                (or (empty? raw-result)
                                    (and (contains? raw-result :changes) (empty? (:changes raw-result)))
                                    (and (contains? raw-result :updated) (not (:updated raw-result)))))
                         "Task completed, but no changes were reported or found."
                         raw-result)]
        (common/response raw-result))
      (common/error-response (str "Task not found: " task)))))

(def manage-tool
  {:name "code-manage"
   :description (str "Run a named `code.manage` maintenance, analysis, scaffold, or refactor task against the "
                     "repository from inside MCP. Use this for long-context autopilot work over namespace cleanup, "
                     "test scaffolding, grep/replace tasks, incomplete or unchecked reports, refactors, and other "
                     "project-specific maintenance flows already exposed by `code.manage`.")
   :inputSchema {:type "object"
                 :properties {"task" {:type "string"
                                      :description "The `code.manage` task name, such as `analyse`, `scaffold`, `create-tests`, `grep`, `unchecked`, `require-file`, or `heal-code`."}
                              "target" {:type "string"
                                        :description "Optional EDN string for the target namespace, symbol, or vector/set of namespaces, such as `code.test`, `[code.mcp]`, or `:all`."}
                              "options" {:type "string"
                                         :description "Optional EDN string for the task options map, such as `{:write true}`, `{:tag :all}`, or `{:files [\"src/code/mcp/server.clj\"]}`. Quiet print options are merged in by default."}}
                 :required ["task"]}
   :implementation #'manage-fn})
