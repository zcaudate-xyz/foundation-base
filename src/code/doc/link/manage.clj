(ns code.doc.link.manage
  (:require [code.manage :as manage]
            [clojure.pprint :as pprint]
            [std.lib :as h]))

(defn format-manage-output
  "formats the output based on the formatter"
  {:added "3.0"}
  ([result formatter]
   (cond (fn? formatter)
         (formatter result)

         (symbol? formatter)
         ((resolve formatter) result)

         (= :edn formatter)
         (with-out-str (pprint/pprint result))

         :else
         (str result))))

(defn run-manage-task
  "runs a manage task and returns the output string"
  {:added "3.0"}
  ([task args formatter]
   (let [task-fn (if (symbol? task)
                   (ns-resolve (find-ns 'code.manage) task)
                   task)]
     (if task-fn
       (if formatter
         (let [args (if (map? (last args))
                      (update args (dec (count args)) h/merge-nested {:print {:result false :summary false :item false}})
                      (concat args [{:print {:result false :summary false :item false}}]))
               result (apply task-fn args)]
           (format-manage-output result formatter))
         (with-out-str
           (apply task-fn (or args []))))
       (str "Task not found: " task)))))

(defn link-manage
  "links manage tasks to the document"
  {:added "3.0"}
  ([interim name]
   (update-in interim [:articles name :elements]
              (fn [elements]
                (mapv (fn [element]
                        (if (= :manage (:type element))
                          (let [{:keys [task args formatter]} element
                                output (run-manage-task task args formatter)]
                            {:type :block
                             :code output})
                          element))
                      elements)))))
