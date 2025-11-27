(ns std.dispatch.hub-test
  (:use code.test)
  (:require [std.dispatch.hub :refer :all]
            [std.lib :as h]
            [std.dispatch.common :as common]))

(defn test-scaffold
  ([config n group-count batch-size]
   (let [dispatch (create-dispatch (assoc config :options {:hub {:max-batch batch-size
                                                                 :interval 10
                                                                 :cache {:duration 100
                                                                         :unit :milliseconds}}}))
         results (atom [])
         dispatch (assoc dispatch :handler (fn [_ entries]
                                             (swap! results concat entries)
                                             entries))
         _ (start-dispatch dispatch)
         groups (take group-count (cycle (range group-count)))]
     (try
       (doseq [i (range n)]
         (let [group (nth groups (mod i group-count))]
           (submit-dispatch dispatch {:id i :group group})))
       (Thread/sleep 500) ;; wait for processing
       @results
       (finally
         (stop-dispatch dispatch))))))

^{:refer std.dispatch.hub/create-dispatch :added "3.0"}
(fact "creates the hub executor"

  (->> (test-scaffold {} 20 5 5)
       (map :id)
       (sort))
  => (range 20))

^{:refer std.dispatch.hub/cache-eviction :added "4.0"}
(fact "verifies that the cache evicts unused hubs"
  (let [dispatch (create-dispatch {:options {:hub {:cache {:size 10
                                                           :duration 100
                                                           :unit :milliseconds}}}})
        runtime (:runtime dispatch)
        ^com.google.common.cache.Cache groups (:groups runtime)]

    ;; Add an entry
    (put-hub dispatch :a {:id 1})

    ;; Verify it exists
    (.getIfPresent groups :a) => some?

    ;; Wait for expiration
    (Thread/sleep 200)

    ;; Verify it is gone (or at least marked for eviction, .size is approximate but .getIfPresent should return nil if expired)
    ;; Note: Guava cache eviction is lazy, happens on write/read.
    (.cleanUp groups)
    (.getIfPresent groups :a) => nil))
