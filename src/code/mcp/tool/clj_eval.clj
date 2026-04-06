(ns code.mcp.tool.clj-eval)

(defn clj-eval-safe
  [code]
  (try
    (let [form (read-string code)
          value (eval form)]
      {:ok true
       :text (pr-str value)
       :value value})
    (catch Throwable t
      {:ok false
       :text (str (or (.getMessage t)
                      (str t)))})))

(defn clj-eval-fn
  [_ {:keys [code]}]
  (let [{:keys [ok text]} (clj-eval-safe code)]
    {:content [{:type "text" :text text}]
     :isError (not ok)}))

(def clj-eval-tool
  {:name "clj-eval"
   :description (str "Evaluate a Clojure form inside the project JVM and return the printed result. "
                     "Use this for targeted live probes, one-off inspection, or validating assumptions when "
                     "a more specific MCP tool does not yet cover the workflow you need.")
   :inputSchema {:type "object"
                 :properties {"code" {:type "string"
                                      :description "A single Clojure form to read and evaluate, such as `(keys code.manage/+tasks+)`."}}
                 :required ["code"]}
   :implementation #'clj-eval-fn})
