(ns code.manage.ai-dispatcher
  (:require [std.task :as task :refer [deftask]]
            [std.concurrent.executor :as exe]
            [std.lib :as h]))

;; 0. Setup: The Dispatcher (Thread Pool)
;; We use a fixed pool of 4 threads to simulate rate-limiting.
(defonce ai-pool (exe/executor:pool 4 4 1000 {}))

;; Helper for basic task construction
(def basic-construct
  {:input  (fn [_] [])
   :lookup (fn [_ _] {})
   :env    (fn [m] m)})

;; 1. Producer: Generate 10 AI Jobs
(deftask generate-jobs
  {:template :default
   :construct basic-construct
   :params {:title "STEP 1: GENERATE JOBS"
            :print {:item false :result false :summary true}}
   :item {:list (fn [_ _] (vec (range 10)))}
   :main {:fn (fn [id _ _ _]
                {:job-id id
                 :prompt (format "Explain concept #%d" id)})
          :count 4}})

;; 2. Dispatcher: Submit to Pool (Async)
;; This task will complete almost instantly because it only SUBMITS work.
(deftask dispatch-jobs
  {:template :default
   :construct basic-construct
   :params {:title "STEP 2: DISPATCH (ASYNC)"
            :print {:item false :result true :summary true}}
   :item {:list (fn [_ env] (or (:pipeline-data env) []))}
   :main {:fn (fn [job _ _ _]
                (let [future (exe/submit ai-pool
                                         (fn []
                                           (Thread/sleep 500) ;; Simulate AI Latency
                                           (str "AI Response for " (:job-id job))))]
                  {:job-id (:job-id job)
                   :status :queued
                   :handle future})) ;; Pass the Future to the next step
          :count 4}
   :result {:keys {:job-id :job-id :status :status}
            :columns [{:key :job-id :length 10}
                      {:key :status :length 10 :color #{:blue}}]}})

;; 3. Collector: Await Results (Blocking)
;; This task absorbs the latency as it waits for futures to complete.
(deftask await-results
  {:template :default
   :construct basic-construct
   :params {:title "STEP 3: AWAIT RESULTS"
            :print {:item false :result true :summary true}}
   :item {:list (fn [_ env] (or (:pipeline-data env) []))}
   :main {:fn (fn [item _ _ _]
                (let [future (:handle item)
                      result @future] ;; BLOCKING WAIT
                  {:job-id (:job-id item)
                   :response result}))
          :count 4}
   :result {:keys {:job-id :job-id :response :response}
            :columns [{:key :job-id :length 10}
                      {:key :response :length 40 :color #{:green}}]}})

(defn -main [& args]
  (println "\n--- AI DISPATCHER DEMO ---\n")
  (flush)

  (try
    ;; 1. Generate
    (let [jobs (generate-jobs :all {:return :items :package :vector})]
      (println (format "Generated %d jobs." (count jobs)))

      ;; 2. Dispatch
      (let [job-data (mapv second jobs)
            start    (System/currentTimeMillis)
            dispatched (dispatch-jobs :all {:pipeline-data job-data
                                            :return :items
                                            :package :vector})
            end      (System/currentTimeMillis)]

        (println (format "Dispatch took %d ms (Instant!)" (- end start)))
        (flush)

        ;; 3. Await
        (let [handles (mapv second dispatched)]
          (await-results :all {:pipeline-data handles}))))

    (catch Throwable t
      (println "ERROR:" (.getMessage t))
      (.printStackTrace t)))

  ;; Cleanup the pool so the process exits
  (exe/exec:shutdown ai-pool)
  (System/exit 0))
