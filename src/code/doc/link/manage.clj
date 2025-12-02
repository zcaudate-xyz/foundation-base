(ns code.doc.link.manage
  (:require [code.manage :as manage]
            [std.task :as task]
            [std.lib :as h]))

(defn run-manage-task
  "runs a manage task and returns the output string"
  {:added "3.0"}
  ([task args]
   (let [task-fn (if (symbol? task)
                   (ns-resolve (find-ns 'code.manage) task)
                   task)]
     (if task-fn
       (with-out-str
         (apply task-fn (or args [])))
       (str "Task not found: " task)))))

(defn link-manage
  "links manage tasks to the document"
  {:added "3.0"}
  ([interim name]
   (update-in interim [:articles name :elements]
              (fn [elements]
                (mapv (fn [element]
                        (if (= :manage (:type element))
                          (let [{:keys [task args]} element
                                output (run-manage-task task args)]
                            {:type :block
                             :code output})
                          element))
                      elements)))))
