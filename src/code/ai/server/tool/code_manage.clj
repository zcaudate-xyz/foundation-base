(ns code.ai.server.tool.code-manage
  (:require [code.manage :as manage]
            [std.task :as task]
            [std.lib :as h]))

(defn manage-fn
  [_ {:keys [task target options]}]
  (let [func (get manage/+tasks+ (keyword task))
        target-arg (if target (read-string target))
        options-arg (if options (read-string options) {})
        options-arg (merge {:print {:result false :summary false :item false :function false}} options-arg)
        args [target-arg options-arg]]
    (if func
      (let [raw-result (try
                         (apply func args)
                         (catch Throwable e
                           (println "Error executing task:" e)
                           (.printStackTrace e)
                           {:error (.getMessage e)}))
            result-str (with-out-str
                         (if (and (map? raw-result)
                                  (or (empty? raw-result)
                                      (and (contains? raw-result :changes) (empty? (:changes raw-result)))
                                      (and (contains? raw-result :updated) (not (:updated raw-result)))))
                           (println "Task completed, but no changes were reported or found.")
                           (prn raw-result)))]
        {:content [{:type "text" :text result-str}]
         :isError (boolean (:error raw-result))})
      {:content [{:type "text" :text (str "Task not found: " task)}]
       :isError true})))

(def manage-tool
  {:name "code-manage"
   :description "Execute a code.manage task"
   :inputSchema {:type "object"
                 :properties {"task" {:type "string" :description "Task name (e.g. analyse, clean, etc.)"}
                              "target" {:type "string" :description "The target namespace(s) as a symbol or a vector of symbols (e.g., \"'code.test'\" or \"['code.test']\")."}
                              "options" {:type "string" :description "An EDN string representing the options map (e.g., \"{:write true}\")."}}
                 :required ["task"]}
   :implementation #'manage-fn})
