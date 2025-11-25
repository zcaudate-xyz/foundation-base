(ns std.dispatch.queue-test
  (:use code.test)
  (:require [std.dispatch.queue :refer :all]
            [std.dispatch.common :as common]
            [std.concurrent :as cc]
            [std.lib :as h]))

(defonce ^:dynamic *batches*  (atom []))

(defonce ^:dynamic *executor* (atom nil))

(def +test-config+
  {:type :queue
   :handler (fn [loop entries]
              (Thread/sleep 100))
   :hooks   {:on-startup (fn [_]
                           (reset! *batches* []))
             :on-process (fn [_ entries]
                           (swap! *batches* conj (count entries)))}
   :options {:pool  {:queue {:size 5}}
             :queue {:max-batch 300
                     :interval 50
                     :delay 10}}})

(defn test-scaffold [config]
  (h/with:component [queue-fn (create-dispatch config)]
                    (reset! *executor* queue-fn)
                    (do (dotimes [i 1000]
                          (queue-fn i))

                        (Thread/sleep 1000)
                        @*batches*)))

^{:refer std.dispatch.queue/start-dispatch :added "3.0"}
(fact "starts a queue executor"

  (-> (create-dispatch +test-config+)
      (start-dispatch)
      (h/stop)))

^{:refer std.dispatch.queue/handler-fn :added "3.0"}
(fact "creates a queue handler function"
  (handler-fn (create-dispatch +test-config+))
  => fn?)

^{:refer std.dispatch.queue/submit-dispatch :added "3.0"}
(fact "submits to a queue executor"
  (test-scaffold (assoc-in +test-config+ [:options :queue :max-batch] 1000))
  => (contains [1000]))

^{:refer std.dispatch.queue/create-dispatch :added "3.0"}
(fact "creates a queue executor"

  ;; DELAY, MAX-BATCH 300
  (test-scaffold +test-config+)
  => (contains [300])

  ;; DELAY, MAX-BATCH 500
  (test-scaffold (-> +test-config+
                     (assoc-in [:options :queue :max-batch] 500)))
  => (contains [500])

  ;; NO DELAY, MAX-BATCH 300
  (test-scaffold (-> +test-config+
                     (update-in [:options :queue] dissoc :delay)))
  ;; [29 300 300 300 71]
  => #(-> % count (>= 4))

  ;; NO DELAY, MAX-BATCH 500
  (test-scaffold (-> +test-config+
                     (update-in [:options :queue] dissoc :delay)
                     (assoc-in  [:options :queue :max-batch] 500)))
  ;; [43 500 457]
  => #(-> % count (<= 3)))

(comment
  (code.manage/import))

(comment

  (reset! *executor* -q-)
  (def -q- (h/start (create-dispatch +test-config+)))

  (h/tracked [:raw])
  (set (map first (doall (for [i (range 10)]
                           (do (Thread/sleep 30)
                               (-q- 1)))))))


(comment
  (def +test-config+
    {:type :queue
     :handler (fn [_ entries]
                (Thread/sleep 1000)
                (h/prn entries))
     :options {:queue {:max-batch 300
                       :interval 100
                       :delay 10}}})
  
  (def -q- (h/start (create-dispatch +test-config+)))
  (do (dotimes [i 100]
        (-q- i))
      (cc/hub:wait (:queue (:runtime -q-))))
  
  (-q- 1)
    
  
  
  
  )
