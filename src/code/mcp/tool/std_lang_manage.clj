(ns code.mcp.tool.std-lang-manage
  (:require [code.mcp.tool.common :as common]
            [std.lang.manage :as manage]))

(defn- supports-arity?
  [f n]
  (boolean
   (some (fn [arglist]
           (or (= n (count arglist))
               (and (seq arglist)
                    (>= (count arglist) 2)
                    (= '& (get arglist (- (count arglist) 2)))
                    (<= (- (count arglist) 2) n))))
         (:arglists (meta f)))))

(defn std-lang-manage-fn
  [_ {:keys [task args]}]
  (let [task-name    (or task "inventory")
        task-key     (keyword task-name)
        func         (or (get manage/+tasks+ task-key)
                         (ns-resolve 'std.lang.manage (symbol task-name)))
        parsed-args  (let [value (common/read-edn args [])]
                       (cond (nil? value) []
                             (vector? value) value
                             :else [value]))
        default-args {:print {:function false
                              :item     false
                              :result   false
                              :summary  false}}]
    (if-not func
      (common/error-response (str "std.lang.manage task not found: " task-name))
      (try
        (common/response
         (cond
           (seq parsed-args)
           (apply func parsed-args)

           (supports-arity? func 1)
           (func default-args)

           (supports-arity? func 2)
           (func :all default-args)

           :else
           (func)))
        (catch Throwable t
          (common/error-response
           (str "Error executing std.lang.manage task `" task-name "`: "
                (or (.getMessage t) (str t)))))))))

(def std-lang-manage-tool
  {:name "std-lang-manage"
   :description (str "Run high-level `std.lang.manage` inventory, audit, coverage, support-matrix, and "
                     "scaffolding tasks from the MCP server. Use this for long-context autopilot work over "
                     "language support, xtalk coverage, runtime suites, generated scaffolds, and other "
                     "cross-language maintenance workflows that already exist in `std.lang.manage`.")
   :inputSchema {:type "object"
                 :properties {"task" {:type "string"
                                      :description (str "The `std.lang.manage` task name, such as `inventory`, "
                                                        "`support-matrix`, `audit-languages`, `coverage-summary`, "
                                                        "`generate-xtalk-ops`, or `scaffold-runtime-template`.")}
                              "args" {:type "string"
                                      :description (str "Optional EDN vector of positional arguments to pass directly "
                                                        "to the task, such as `[{:langs [:python :go]}]` or "
                                                        "`[:js {:write true}]`. Leave this empty to let the tool "
                                                        "call the task with its default autopilot-friendly params.")}}
                 :required ["task"]}
   :implementation #'std-lang-manage-fn})
