(ns code.manage.pipeline-gen
  (:require [std.task :as task]
            [std.lib :as h]))

;; Helper for basic task construction
(def basic-construct
  {:input  (fn [_] [])
   :lookup (fn [_ _] {})
   :env    (fn [m] m)})

;; 1. The Pipeline Builder
(defn make-task
  "Generates a Task object at runtime"
  [name logic-fn & [opts]]
  (task/task :default (str name)
             (h/merge-nested
              {:template :default
               :construct basic-construct
               :params {:print {:item false :result true :summary true}}

               ;; Default to reading 'pipeline-data' from env
               :item {:list (fn [_ env] (or (:pipeline-data env) []))}

               :main {:fn logic-fn :count 4}}
              opts)))

(defn run-chain
  "Executes a list of tasks in sequence, passing data between them"
  [initial-data tasks]
  (println "\n--- STARTING PIPELINE ---")
  (loop [data initial-data
         [current-task & rest-tasks] tasks]
    (if current-task
      (do
        (println (format "\n>>> Running Task: %s with %d items"
                         (:name current-task) (count data)))

        ;; Execute the task directly (it acts as a function)
        (let [results (current-task :all
                                    {:return :items
                                     :package :vector
                                     :pipeline-data data})
              ;; Extract the :data payload from the result
              next-data (mapv second results)]

          (recur next-data rest-tasks)))

      (println "\n--- PIPELINE FINISHED ---"))))

;; 2. Define Logic Functions (Pure Clojure)

(defn logic-produce [_ _ _ _]
  {:id (long (rand-int 1000))
   :timestamp (System/currentTimeMillis)})

(defn logic-enrich [item _ _ _]
  (assoc item :type (if (even? (:id item)) :even :odd)))

(defn logic-format [item _ _ _]
  {:msg (format "Item %d is %s" (:id item) (:type item))
   :lag (- (System/currentTimeMillis) (:timestamp item))})

;; 3. Define the Pipeline

(def producer-task
  (make-task "producer" logic-produce
             {:item {:list (fn [_ _] (vec (range 5)))} ;; Override source for first task
              :result {:columns [{:key :id :length 10}
                                 {:key :timestamp :length 20}]}}))

(def enrich-task
  (make-task "enricher" logic-enrich
             {:result {:columns [{:key :id :length 10}
                                 {:key :type :length 10 :color #{:cyan}}]}}))

(def format-task
  (make-task "formatter" logic-format
             {:result {:columns [{:key :msg :length 30}
                                 {:key :lag :length 10 :align :right}]}}))

(defn -main [& args]
  (run-chain [] [producer-task enrich-task format-task])
  (System/exit 0))
