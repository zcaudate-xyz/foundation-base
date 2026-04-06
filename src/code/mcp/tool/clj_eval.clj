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
   :description "Evaluates a Clojure form"
   :inputSchema {:type "object"
                 :properties {"code" {:type "string"}}
                 :required ["code"]}
   :implementation #'clj-eval-fn})
