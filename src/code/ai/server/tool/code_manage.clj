(ns code.ai.server.tool.code-manage
  (:require [code.manage :as manage]
            [std.task :as task]
            [std.lib :as h]))

(defn manage-fn
  [_ {:keys [task args]}]
  (let [func (get manage/+tasks+ (keyword task))
        args (if args (read-string args) []) ;; Args should be a vector of arguments
        args-processed (if (map? args) [args] args)] ;; Handle if user passes a map instead of vector
    (if func
      (let [result (with-out-str
                     (try
                       (apply func args-processed)
                       (catch Throwable e
                         (println "Error executing task:" e)
                         (.printStackTrace e))))]
        {:content [{:type "text" :text result}]
         :isError false})
      {:content [{:type "text" :text (str "Task not found: " task)}]
       :isError true})))

(def manage-tool
  {:name "code-manage"
   :description "Execute a code.manage task"
   :inputSchema {:type "object"
                 :properties {"task" {:type "string" :description "Task name (e.g. analyse, clean, etc.)"}
                              "args" {:type "string" :description "EDN string of arguments (vector) for the task"}}
                 :required ["task"]}
   :implementation #'manage-fn})
