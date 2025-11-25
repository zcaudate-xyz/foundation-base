(ns std.dispatch.board-test
  (:use code.test)
  (:require [std.dispatch.board :refer :all]
            [std.concurrent :as cc]
            [std.lib :as h]))

(defonce ^:dynamic *output*  (atom []))

(defonce ^:dynamic *executor* (atom nil))

(def +test-config+
  {:type :board
   :handler (fn [executor {:keys [id lookup] :as entry}]
              (swap! *output* conj (lookup id)))
   :hooks   {:on-startup (fn [_]
                           (reset! *output* []))}
   :options {:pool {:size 30}
             :board {:groups-fn (fn [_ entry] [(:id entry)])}}})

(def +scaffold-config+
  (-> +test-config+
      (assoc :handler (fn [executor {:keys [groups] :as entry}]
                        (swap! *output* conj entry)
                        (Thread/sleep (* 50 (count groups)))))
      (assoc-in [:options :board :groups-fn]
                (fn [_ entry] (:groups entry)))))

(defn test-scaffold [config groups]
  (h/with:component [board-fn (create-dispatch config)]
                    (reset! *executor* board-fn)
                    (doall (map-indexed (fn [id groups]
                                          (Thread/sleep 1)
                                          (board-fn {:id id :groups groups}))
                                        groups))
                    (Thread/sleep 1000)
                    @*output*))

^{:refer std.dispatch.board/get-ticket :added "3.0"}
(fact "gets a ticket"
  (get-ticket) => integer?)

^{:refer std.dispatch.board/new-board :added "3.0"}
(fact "creates a new board"
  @(new-board {}) => (contains {:return map? :submitted cc/queue?}))

^{:refer std.dispatch.board/submit-ticket :added "3.0"}
(fact "adds ticket to the board"

  (submit-ticket {} ["a" "b"] "t1")
  => {"a" ["t1"], "b" ["t1"]} ^:hidden

  (-> {}
      (submit-ticket ["a"] "t0")
      (submit-ticket ["a" "b"] "t1"))
  => {"a" ["t0" "t1"], "b" ["t1"]})

^{:refer std.dispatch.board/submit-board :added "3.0"}
(fact "submits an entry to a board"

  (def -ex- (create-dispatch +test-config+))

  (-> -ex-
      (submit-board {:id "a"} "t0"))
  => (contains ["t0" ["a"] h/future?]) ^:hidden

  (update @(:board (:runtime -ex-)) :submitted seq)
  => (contains {:lookup {"t0" {:id "a"}},
                :queues {"a" ["t0"]},
                :busy {}, :dependent {},
                :submitted ["t0"]})

  (def -sc- (doto (create-dispatch +scaffold-config+)
              (submit-board {:id "a" :groups [:a :b :c]} "t0")))

  (:queues @(:board (:runtime -sc-)))
  => {:c ["t0"], :b ["t0"], :a ["t0"]})

^{:refer std.dispatch.board/clear-board :added "3.0"}
(fact "clears the board"

  (def -ex- (doto (-> (create-dispatch +test-config+))
              (submit-board {:id "a"} "t0")
              (poll-board "a")))

  (clear-board -ex- "a" "t0")
  => {:group "a", :dependent #{"a"}} ^:hidden

  (update @(:board (:runtime -ex-)) :submitted seq)
  => {:queues {}, :lookup {}, :busy {}, :dependent {} :submitted nil :return {}})

^{:refer std.dispatch.board/add-dependents :added "3.0"}
(fact  "add dependents to a given lookup"

  (add-dependents {:a #{:d}} :a #{:b :c})
  => {:a #{:c :b :d}})

^{:refer std.dispatch.board/poll-board :added "3.0"}
(fact "polls the board for job entry"

  (def -ex- (doto (create-dispatch +test-config+)
              (submit-board {:id "a"} "t0")))

  (poll-board -ex- "a")
  => (contains {:groups ["a"], :entry {:id "a"}, :ticket "t0"}))

^{:refer std.dispatch.board/poll-dispatch :added "3.0"}
(fact "polls the executor for more work"
  (def -ex- (doto (create-dispatch +test-config+)
              (start-dispatch)
              (submit-board {:id "a"} "t0")))
  (poll-dispatch -ex- ["a"]) => (contains [h/future?]))

^{:refer std.dispatch.board/submit-dispatch :added "3.0"}
(fact "submits to the board executor"
  (def -ex- (doto (create-dispatch +test-config+) (start-dispatch)))
  (submit-dispatch -ex- {:id "a"}) => h/future?)

^{:refer std.dispatch.board/start-dispatch :added "3.0"}
(fact "starts the board executor"

  (test-scaffold +scaffold-config+
                 [[:a :b :c]
                  [:a :b]
                  [:c]
                  [:b :d]
                  [:b :c :d]
                  [:c]
                  [:a]
                  [:b :c]
                  [:c]
                  [:d]
                  [:a]])
  => (contains
      [{:id 0,  :groups [:a :b :c]}
       {:id 2,  :groups [:c]}
       {:id 1,  :groups [:a :b]}
       {:id 3,  :groups [:b :d]}
       {:id 6,  :groups [:a]}
       {:id 10, :groups [:a]}
       {:id 4,  :groups [:b :c :d]}
       {:id 5,  :groups [:c]}
       {:id 9,  :groups [:d]}
       {:id 7,  :groups [:b :c]}
       {:id 8,  :groups [:c]}]
      :in-any-order))

^{:refer std.dispatch.board/stop-dispatch :added "3.0"}
(fact "stops the board executor"
  (def -ex- (doto (create-dispatch +test-config+) (start-dispatch)))
  (stop-dispatch -ex-))

^{:refer std.dispatch.board/create-dispatch :added "3.0"}
(fact "creates the board executor"
  (create-dispatch +test-config+) => map?)
