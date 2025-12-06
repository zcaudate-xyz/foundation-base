(ns std.pipe.example-monitor
  (:require [std.pipe :as pipe]
            [std.task :as task]))

(defn long-running-pipeline []
  (println "Starting long running pipeline with monitor...")
  (pipe/pipe (task/task :default "long-running-example"
                   {:item {:list (constantly (range 50))}
                    :main {:fn (fn [x _ _ _]
                                 ;; Simulate work with random duration
                                 (Thread/sleep (long (+ 100 (rand-int 400))))
                                 ;; Simulate random failure
                                 (when (< (rand) 0.1)
                                   (throw (ex-info "Random failure" {:x x})))
                                 (* x 10))}})
        :list
        {:bulk true
         :monitor true
         :parallel true ;; Show parallel execution in monitor
         }))

(long-running-pipeline)
