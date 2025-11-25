(ns code.manage.pipeline-demo
  (:require [std.task :as task :refer [deftask task]]
            [std.lib :as h]))

(def basic-construct
  {:input  (fn [_] [])
   :lookup (fn [_ _] {})
   :env    (fn [m] m)})

(deftask find-numbers
  {:template :default
   :construct basic-construct
   :params {:title "STEP 1: FIND NUMBERS"
            :print {:item false :result false :summary true}}
   :item {:list (fn [_ _] (vec (range 10)))}
   :main {:fn (fn [n _ _ _]
                {:id n :value (* n 10)})
          :count 4}
   :result {:keys    {:id :id
                      :value :value}
            :columns [{:key :id :length 5}
                      {:key :value :length 10}]}})

(deftask square-numbers
  {:template :default
   :construct basic-construct
   :params {:title "STEP 2: SQUARE NUMBERS"
            :parallel false
            :print {:item false :result true :summary true}}
   :item {:list (fn [_ env]
                  (or (:pipeline-data env) []))}
   :main {:fn (fn [item _ _ _]
                (let [val (:value item)
                      sq  (* val val)]
                  {:original val
                   :squared  sq}))
          :count 4}
   :result {:keys    {:original :original
                      :squared  :squared}
            :columns [{:key :original :length 10 :align :right}
                      {:key :squared  :length 10 :align :right :color #{:yellow :bold}}]}})

(defn -main [& args]
  (try
    (println "\n--- STARTING PIPELINE ---\n")
    (flush)
    (let [step-1-results (find-numbers :all {:return :items :package :vector})]
      (println (format "\nPassed %d items to Step 2..." (count step-1-results)))
      (flush)
      (let [next-inputs (mapv second step-1-results)]
        (square-numbers :all {:pipeline-data next-inputs})
        (println "\nPipeline Complete.")))
    (catch Throwable t
      (println "ERROR:" (.getMessage t))
      (System/exit 1)))
  (System/exit 0))
