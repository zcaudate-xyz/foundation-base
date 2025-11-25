(ns std.concurrent.queue-test
  (:use code.test)
  (:require [std.concurrent.queue :as q]))

^{:refer std.concurrent.queue/->timeunit :added "3.0"}
(fact "returns the timeunit"

  (q/->timeunit :ms)
  => java.util.concurrent.TimeUnit/MILLISECONDS)

^{:refer std.concurrent.queue/deque :added "3.0"}
(fact "constructs a blocking deque"

  (q/deque 1 2 3)
  => java.util.concurrent.LinkedBlockingDeque)

^{:refer std.concurrent.queue/queue :added "3.0"}
(fact "constructs a blocking queue"

  (q/queue 1 2 3)
  => java.util.concurrent.LinkedBlockingQueue)

^{:refer std.concurrent.queue/queue? :added "3.0"}
(fact "checks if object is a `BlockingQueue`")

^{:refer std.concurrent.queue/deque? :added "3.0"}
(fact "checks if object is a `BlockingDeque`")

^{:refer std.concurrent.queue/queue:fixed :added "3.0"}
(fact "constructs a fixed size blocking queue"

  (type (q/queue:fixed 10))
  => java.util.concurrent.ArrayBlockingQueue)

^{:refer std.concurrent.queue/queue:limited :added "3.0"}
(fact "constructs a limited queue"

  (q/queue:limited 10))

^{:refer std.concurrent.queue/take :added "3.0"}
(fact "takes an element from the queue"

  ((juxt q/take  #(into [] %)) (q/queue 1 2 3))
  => [1 [2 3]])

^{:refer std.concurrent.queue/drain :added "3.0"}
(fact "drains elements to another vector"

  ((juxt #(q/drain % 2)
         #(into [] %))
   (q/queue 1 2 3 4))
  => [[1 2] [3 4]])

^{:refer std.concurrent.queue/put :added "3.0"}
(fact "puts an element at the back"

  (->> (doto (q/queue) (q/put 1))
       (into []))
  => [1])

^{:refer std.concurrent.queue/peek :added "3.0"}
(fact "takes element at the front of the queue"

  (q/peek (q/queue)))

^{:refer std.concurrent.queue/remaining-capacity :added "3.0"}
(fact "returns the remaining capacity"

  (q/remaining-capacity (q/queue))
  => 2147483647

  (q/remaining-capacity (doto (q/queue:fixed 2) (q/put 1)))
  => 1)

^{:refer std.concurrent.queue/peek-first :added "3.0"}
(fact "peeks from the front of the queue"
  (q/peek-first (q/deque 1 2 3))
  => 1)

^{:refer std.concurrent.queue/peek-last :added "3.0"}
(fact "peeks from the back of the queue"
  (q/peek-last (q/deque 1 2 3))
  => 3)

^{:refer std.concurrent.queue/put-first :added "3.0"}
(fact "puts at the front of the queue"
  (-> (doto (q/deque) (q/put-first 1))
      (vec))
  => [1])

^{:refer std.concurrent.queue/put-last :added "3.0"}
(fact "puts at the back of the queue"
  (-> (doto (q/deque) (q/put-last 1))
      (vec))
  => [1])

^{:refer std.concurrent.queue/take-first :added "3.0"}
(fact "takes from the front of the queue"
  (q/take-first (q/deque 1 2 3))
  => 1)

^{:refer std.concurrent.queue/take-last :added "3.0"}
(fact "takes from the back of the queue"
  (q/take-last (q/deque 1 2 3))
  => 3)

^{:refer std.concurrent.queue/push :added "3.0"}
(fact "puts at the front of the queue"
  (-> (doto (q/deque) (q/push 1))
      (vec))
  => [1])

^{:refer std.concurrent.queue/pop :added "3.0"}
(fact "pops from the front of the queue"
  (q/pop (q/deque 1 2 3))
  => 1)

^{:refer std.concurrent.queue/remove :added "3.0"}
(fact "removes element from queue"
  (let [q (q/deque 1 2 3)]
    (q/remove q 2)
    (into [] q))
  => [1 3])

^{:refer std.concurrent.queue/process-bulk :added "3.0"}
(fact "processes elements in the queue"

  (def +state+ (atom []))

  (q/process-bulk (fn [elems] (swap! +state+ conj elems))
                  (q/queue 1 2 3 4 5) 3)

  @+state+
  => [[1 2 3] [4 5]])
