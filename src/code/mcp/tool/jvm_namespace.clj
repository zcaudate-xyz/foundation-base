(ns code.mcp.tool.jvm-namespace
  (:require [code.mcp.tool.common :as common]
            [jvm.namespace]))

(defn jvm-namespace-fn
  [_ {:keys [operation target options]}]
  (let [func        (when operation
                      (ns-resolve 'jvm.namespace (symbol operation)))
        target-arg  (common/read-edn target)
        options-arg (some-> options common/read-edn common/merge-print-options)]
    (if-not func
      (common/error-response (str "jvm.namespace operation not found: " operation))
      (try
        (common/response
         (cond
           (and (some? target-arg) (some? options-arg))
           (func target-arg options-arg)

           (some? target-arg)
           (func target-arg)

           (some? options-arg)
           (func options-arg)

           :else
           (func)))
        (catch Throwable t
          (common/error-response
           (str "Error executing jvm.namespace operation `" operation "`: "
                (or (.getMessage t) (str t)))))))))

(def jvm-namespace-tool
  {:name "jvm-namespace"
   :description (str "Inspect or mutate loaded JVM namespaces from inside the live project process. "
                     "Use this when an autopilot agent needs REPL-grade namespace control for tasks like "
                     "listing aliases, mappings, interns, or memory objects; checking whether a namespace "
                     "is loaded; reloading a namespace; or clearing aliases and mappings before re-evaluation.")
   :inputSchema {:type "object"
                 :properties {"operation" {:type "string"
                                           :description (str "The public `jvm.namespace` operation to invoke, "
                                                             "such as `list-aliases`, `list-mappings`, "
                                                             "`list-in-memory`, `loaded?`, `reset`, `reload-task`, "
                                                             "or `reload-all`.")}
                              "target" {:type "string"
                                        :description (str "Optional EDN string for the namespace target. "
                                                          "Examples: `jvm.namespace`, `[jvm.namespace]`, or "
                                                          "`#{jvm.namespace code.mcp.server}` depending on the operation.")}
                              "options" {:type "string"
                                         :description (str "Optional EDN string for an options map. Use this when "
                                                           "the underlying operation accepts print or return controls, "
                                                           "for example `{:return :summary}`.")}}
                 :required ["operation"]}
   :implementation #'jvm-namespace-fn})
