(ns std.dispatch.hub-test
  (:use code.test)
  (:require [std.dispatch.hub :refer :all]
            [std.dispatch.debounce :as debounce]
            [std.dispatch.hooks :as hooks]
            [std.lib.component :as component]
            [std.concurrent :as cc]
            [std.lib :as h]))

(defonce ^:dynamic *output*   (atom []))

(defonce ^:dynamic *counter*  (atom -1))

(defonce ^:dynamic *executor* (atom nil))

(def +test-config+
  {:type :hub
   :handler (fn [_ entries]
              (swap! *output* concat entries))
   :hooks   {:on-startup (fn [_]
                           (reset! *counter* -1)
                           (reset! *output* []))}
   :options {:pool  {:size 40}
             :hub  {:group-fn (fn [_ {:keys [group]}] group)
                    :max-batch 5
                    :interval 10
                    :delay 2}}})

(defn test-scaffold [config wait groups times]
  (component/with [main-fn (create-dispatch config)]
                  (reset! *executor* main-fn)
                  (do (try (dotimes [group groups]
                             (future (dotimes [i times]
                                       (let [id (swap! *counter* inc)]
                                         (future
                                           (Thread/sleep (long (rand-int wait)))
                                           (main-fn {:id  id
                                                     :group group
                                                     :val (+ i (* group groups))}))))
                                     (if wait (Thread/sleep (long wait)))))
                           (catch Throwable t
                             (.printStackTrace t)))

                      (Thread/sleep ;;(if wait (* threads groups wait))

                       500)
                      [@*counter* @*output*])))

^{:refer std.dispatch.hub/process-hub :added "3.0"}
(fact "activates on debounce submit hit"
  
  (def -hub- (cc/hub:new))
  (cc/hub:add-entries -hub- [1 2 3])
  (process-hub {:handler (fn [_ entries] entries) :options {:hub {:max-batch 10}}} :g -hub-)
  => [1 2 3])

^{:refer std.dispatch.hub/put-hub :added "3.0"}
(fact "puts an entry into the group hubs"

  (def -d- {:runtime {:groups (atom {})}})
  (put-hub -d- :g 1)
  => (contains [1])
  (get @(:groups (:runtime -d-)) :g) => cc/hub?)

^{:refer std.dispatch.hub/create-hub-handler :added "3.0"}
(fact "creates the hub handler"
  (create-hub-handler {:runtime {:groups (atom {})}}) => fn?)

^{:refer std.dispatch.hub/update-debounce-handler! :added "3.0"}
(fact "updates the debounce handler"
  (def -d- {:runtime {:debouncer (volatile! (atom {:handler nil})) :groups (atom {})}})
  (update-debounce-handler! -d-)
  (:handler @(deref (:debouncer (:runtime -d-)))) => fn?)

^{:refer std.dispatch.hub/create-debounce :added "3.0"}
(fact "creates the debounce executor"
  (create-debounce {:options {:hub {:interval 100 :delay 10}}})
  => map?)

^{:refer std.dispatch.hub/start-dispatch :added "3.0"}
(fact "starts the hub executor"
  (def -d- (create-dispatch +test-config+))
  (start-dispatch -d-)
  (started?-dispatch -d-) => true
  (stop-dispatch -d-))

^{:refer std.dispatch.hub/stop-dispatch :added "3.0"}
(fact "stops the hub executor"
  (def -d- (doto (create-dispatch +test-config+) (start-dispatch)))
  (stop-dispatch -d-)
  (started?-dispatch -d-) => false)

^{:refer std.dispatch.hub/kill-dispatch :added "3.0"}
(fact "kills the hub executor"
  (def -d- (doto (create-dispatch +test-config+) (start-dispatch)))
  (kill-dispatch -d-)
  (started?-dispatch -d-) => false)

^{:refer std.dispatch.hub/submit-dispatch :added "3.0"}
(fact "submits to the hub executor"
  (def -d- (doto (create-dispatch +test-config+) (start-dispatch)))
  (submit-dispatch -d- {:group :a :val 1})
  (stop-dispatch -d-))

^{:refer std.dispatch.hub/info-dispatch :added "3.0"}
(fact "returns dispatch info"

  (def -d- (doto (create-dispatch +test-config+) (start-dispatch)))
  (info-dispatch -d- nil) => map?
  (stop-dispatch -d-))

^{:refer std.dispatch.hub/create-dispatch :added "3.0"}
(fact "creates the hub executor"
  
  ;; Non Sorted
  (->> (test-scaffold +test-config+ 20 5 5)
       second
       (map :id)
       (sort))
  => (range 25))

(comment

  (def -exe- (-> (create-dispatch +test-config+)
                 (start-dispatch)))

  (-exe- 1)
  (stop-dispatch -exe-))

(comment
  (code.manage/import)

  (do (do (def *outputs*  (atom []))
          (def -exe- (-> (create-dispatch {:type :hub
                                           :options {:pool {:size 10}}

                                           :handler (fn [_ events]
                                                      (swap! *outputs* concat events))})
                         (start-dispatch))))

      (do (dotimes [t 8]
            (future
              (try (dotimes [n 1]
                     (dotimes [i 2]
                       ;;;(Thread/sleep (rand-int 10))
                       (submit-dispatch -exe- (swap! -counter- inc))))
                   (catch Throwable t
                     (.printStackTrace t)))))

          (Thread/sleep 200)

          ;;(submit-dispatch -exe- (swap! -counter- inc))
          ;;(Thread/sleep 100)
          [@-counter-
           (count @*outputs*)
           (-> -exe- :runtime :hub deref :same deref)
           ;;{:queue {}, :latest 16}
           @*outputs*])))

(comment

  @(:debouncer (:runtime -e-))

  (def -e- (-> (create-dispatch {:handler (fn [_ entries] (prn :ENTRIES entries))
                                 :options {:pool {:max 100
                                                  :size 100}
                                           :hub {:id-fn (fn [_ _] nil)
                                                 :interval  1000
                                                 :max-batch 10
                                                 :debounce {:type :eager
                                                            :run-final true}
                                                 :sort {:sequential false}}}})
               (start-dispatch)))

  (do (future (dotimes [i 1000]
                (Thread/sleep 1)
                (submit-dispatch -e- 0)))
      (future (dotimes [i 1000]
                (Thread/sleep 1)
                (submit-dispatch -e- 1))))
  (submit-dispatch -e- 2)
  (submit-dispatch -e- 1)
  (submit-dispatch -e- 3))
