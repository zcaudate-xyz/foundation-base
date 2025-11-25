(ns std.dispatch.core-test
  (:use code.test)
  (:require [std.dispatch.core :refer :all]
            [std.dispatch.common :as common]
            [std.concurrent :as cc]
            [std.lib.component :as component]))

(defonce ^:dynamic *output*  (atom []))

(defonce ^:dynamic *dispatch* (atom nil))

(def +test-config+
  {:type :core
   :handler (fn [loop entries])
   :hooks   {:on-startup (fn [_]
                           (reset! *output* []))
             :on-process (fn [_ entry]
                           (swap! *output* conj entry))}
   :options {:pool  {:size 2}}})

(defn test-scaffold [config times sleep]
  (component/with [main-fn (create-dispatch config)]
                  (reset! *dispatch* main-fn)
                  (do (dotimes [i times]
                        (main-fn i))

                      (Thread/sleep sleep)
                      @*output*)))

^{:refer std.dispatch.core/submit-dispatch :added "3.0"}
(fact "submits to the core dispatch"
  (test-scaffold (assoc +test-config+
                        :handler (fn [_ _] (Thread/sleep 10)))
                 10
                 200)
  => [0 1 2 3 4 5 6 7 8 9])

^{:refer std.dispatch.core/create-dispatch :added "3.0"}
(fact "creates a core dispatch"

  ;; POOL SIZE 2, NO SLEEP
  (test-scaffold +test-config+ 100 10)
  => #(-> % count (= 100))

  ;; POOL SIZE 2, WITH SLEEP
  (test-scaffold (assoc +test-config+
                        :handler (fn [_ _] (Thread/sleep 200)))
                 5
                 100)
  => #(-> % count (= 2))

  ;; POOL SIZE 50, WITH SLEEP
  (test-scaffold (-> +test-config+
                     (assoc :handler (fn [_ _] (Thread/sleep 200)))
                     (assoc-in [:options :pool :size] 50))
                 80
                 30)
  => #(-> % count (>= 50)))
