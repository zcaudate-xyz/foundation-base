(ns std.dispatch.debounce-test
  (:use code.test)
  (:require [std.dispatch.debounce :refer :all]
            [std.lib.component :as component]
            [std.lib :as h]))

(defonce ^:dynamic *output*  (atom []))

(defonce ^:dynamic *executor* (atom nil))

(defonce ^:dynamic *counter*  (atom {}))

(def +test-config+
  {:type :debounce
   :handler (fn [executor {:keys [id lookup] :as entry}]
              (swap! *output* conj (lookup id)))
   :hooks   {:on-startup (fn [_]
                           (reset! *output* [])
                           (reset! *counter* {}))}
   :options {:pool {:size 30}
             :debounce {:strategy :eager
                        :interval 800
                        :group-fn (fn [_ entry] (:id entry))}}})

(defn test-scaffold [config trials ids]
  (component/with [debounce-fn (create-dispatch config)]
                  (reset! *executor* debounce-fn)
                  (do (dotimes [n trials]
                        (dotimes [id ids]
                          (do (swap! *counter* assoc id n)
                              (debounce-fn {:id id
                                            :lookup (fn [id]
                                                      {:id id :state (get @*counter* id)})})
                              (Thread/sleep 20))))
                      (Thread/sleep 1000)
                      @*output*)))

^{:refer std.dispatch.debounce/submit-eager :added "3.0"}
(fact "submits and executes eagerly"

  (test-scaffold +test-config+
                 10 2)
  => [{:id 0, :state 0} {:id 1, :state 0}])

^{:refer std.dispatch.debounce/submit-delay :added "3.0"}
(fact "submits and executes after delay"

  (test-scaffold (-> +test-config+
                     (assoc-in [:options :debounce :strategy] :delay))
                 10 2)
  => [{:id 0, :state 9} {:id 1, :state 9}])

^{:refer std.dispatch.debounce/submit-notify :added "3.0"}
(fact "submits and executes on as well and after delay"
  ^:hidden

  (test-scaffold (assoc-in +test-config+
                           [:options :debounce :strategy] :notify)
                 10 2)
  => [{:id 0, :state 0} {:id 1, :state 0} {:id 0, :state 9} {:id 1, :state 9}]

  (test-scaffold (-> +test-config+
                     (assoc-in [:options :debounce :strategy] :notify)
                     (assoc-in [:options :debounce :delay] 50))
                 10 2)
  => (contains-in
      [{:id 0, :state nat-int?}
       {:id 1, :state nat-int?}
       {:id 0, :state 9}
       {:id 1, :state 9}])

  (test-scaffold (-> +test-config+
                     (assoc-in [:options :debounce :strategy] :notify)
                     (assoc-in [:options :debounce :delay] 100)
                     (assoc-in [:options :debounce :interval] 200))
                 10 2)
  => (contains-in
      [{:id 0, :state pos-int?}
       {:id 1, :state pos-int?}
       {:id 0, :state 9}
       {:id 1, :state 9}]))

^{:refer std.dispatch.debounce/submit-dispatch :added "3.0"}
(fact "submits to the debounce executor"
  (test-scaffold +test-config+ 1 1) => [{:id 0, :state 0}])

^{:refer std.dispatch.debounce/start-dispatch :added "3.0"}
(fact "starts the debounce executor"
  (let [d (create-dispatch +test-config+)]
    (start-dispatch d)
    (component/stop d)) => anything)

^{:refer std.dispatch.debounce/stop-dispatch :added "3.0"}
(fact "stops the debounce executor"
  (let [d (create-dispatch +test-config+)]
    (start-dispatch d)
    (stop-dispatch d)) => anything)

^{:refer std.dispatch.debounce/create-dispatch :added "3.0"}
(fact "creates a debource executor"
  (create-dispatch +test-config+) => map?)

(comment
  (code.manage/import)
  (dotimes [i 10]
    (submit-dispatch -ex-eager- 10))

  (dotimes [i 10]
    (submit-dispatch -ex-delay- 10))

  (dotimes [i 10]
    (submit-dispatch -ex-notify- 10)))
