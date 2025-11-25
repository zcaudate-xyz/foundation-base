(ns std.concurrent.queue
  (:require [std.lib :as h])
  (:import (java.util.concurrent BlockingQueue
                                 BlockingDeque
                                 ArrayBlockingQueue
                                 LinkedBlockingQueue
                                 LinkedBlockingDeque
                                 SynchronousQueue
                                 TimeUnit)
           (hara.lib.concurrent LimitedQueue))
  (:refer-clojure :exclude [take pop take-last peek remove]))

(def ^:private +units+
  {:ns TimeUnit/NANOSECONDS
   :us TimeUnit/MICROSECONDS
   :ms TimeUnit/MILLISECONDS
   :s  TimeUnit/SECONDS
   :m  TimeUnit/MINUTES
   :h  TimeUnit/HOURS
   :d  TimeUnit/DAYS})

(defn ->timeunit
  "returns the timeunit
 
   (q/->timeunit :ms)
   => java.util.concurrent.TimeUnit/MILLISECONDS"
  {:added "3.0"}
  (^TimeUnit [obj]
   (cond (keyword? obj)
         (get +units+ obj)

         :else
         obj)))

(defn ^LinkedBlockingDeque deque
  "constructs a blocking deque
 
   (q/deque 1 2 3)
   => java.util.concurrent.LinkedBlockingDeque"
  {:added "3.0"}
  ([]
   (LinkedBlockingDeque.))
  ([& elements]
   (LinkedBlockingDeque. ^java.util.Collection elements)))

(defn ^BlockingQueue queue
  "constructs a blocking queue
 
   (q/queue 1 2 3)
   => java.util.concurrent.LinkedBlockingQueue"
  {:added "3.0"}
  ([]
   (LinkedBlockingQueue.))
  ([& elements]
   (LinkedBlockingQueue. ^java.util.Collection elements)))

(defn queue?
  "checks if object is a `BlockingQueue`"
  {:added "3.0"}
  ([obj]
   (instance? BlockingQueue obj)))

(defn deque?
  "checks if object is a `BlockingDeque`"
  {:added "3.0"}
  ([obj]
   (instance? BlockingDeque obj)))

(defmethod print-method BlockingDeque
  ([v ^java.io.Writer w]
   (.write w (str (into [] v)))))

(defmethod print-method BlockingQueue
  ([v ^java.io.Writer w]
   (.write w (str (into [] v)))))

(defn ^BlockingQueue queue:fixed
  "constructs a fixed size blocking queue
 
   (type (q/queue:fixed 10))
   => java.util.concurrent.ArrayBlockingQueue"
  {:added "3.0"}
  ([size]
   (ArrayBlockingQueue. size)))

(defn ^LimitedQueue queue:limited
  "constructs a limited queue
 
   (q/queue:limited 10)"
  {:added "3.0"}
  ([size]
   (LimitedQueue. size)))

(defn take
  "takes an element from the queue
 
   ((juxt q/take  #(into [] %)) (q/queue 1 2 3))
   => [1 [2 3]]"
  {:added "3.0"}
  ([^BlockingQueue queue]
   (take queue nil))
  ([^BlockingQueue queue timeout]
   (take queue timeout :ms))
  ([^BlockingQueue queue timeout timeunit]
   (if (nil? timeout)
     (.take queue)
     (.poll queue timeout (->timeunit timeunit)))))

(defn drain
  "drains elements to another vector
 
   ((juxt #(q/drain % 2)
          #(into [] %))
    (q/queue 1 2 3 4))
   => [[1 2] [3 4]]"
  {:added "3.0"}
  ([^BlockingQueue queue]
   (let [target (java.util.ArrayList. (count queue))]
     (.drainTo queue target)
     target))
  ([^BlockingQueue queue ^long max]
   (let [target (java.util.ArrayList. max)]
     (drain queue max target)
     target))
  ([^BlockingQueue queue ^long max ^java.util.Collection target]
   (.drainTo queue target max)
   target))

(defn put
  "puts an element at the back
 
   (->> (doto (q/queue) (q/put 1))
        (into []))
   => [1]"
  {:added "3.0"}
  ([^BlockingQueue queue element]
   (put queue element nil))
  ([^BlockingQueue queue element timeout]
   (put queue element timeout :ms))
  ([^BlockingQueue queue element timeout timeunit]
   (if (nil? timeout)
     (.put queue element)
     (.offer queue element timeout (->timeunit timeunit)))))

(defn peek
  "takes element at the front of the queue
 
   (q/peek (q/queue))"
  {:added "3.0"}
  ([^BlockingQueue queue]
   (.peek queue)))

(defn remaining-capacity
  "returns the remaining capacity
 
   (q/remaining-capacity (q/queue))
   => 2147483647
 
   (q/remaining-capacity (doto (q/queue:fixed 2) (q/put 1)))
   => 1"
  {:added "3.0"}
  ([^BlockingQueue queue]
   (.remainingCapacity queue)))

(defn peek-first
  "peeks from the front of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue]
   (.peekFirst queue)))

(defn peek-last
  "peeks from the back of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue]
   (.peekLast queue)))

(defn put-first
  "puts at the front of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue element]
   (put-first queue element nil))
  ([^BlockingDeque queue element timeout]
   (put-first queue element timeout :ms))
  ([^BlockingDeque queue element timeout timeunit]
   (if (nil? timeout)
     (.putFirst queue element)
     (.offerFirst queue element timeout (->timeunit timeunit)))))

(defn put-last
  "puts at the back of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue element]
   (put-last queue element nil))
  ([^BlockingDeque queue element timeout]
   (put-last queue element timeout :ms))
  ([^BlockingDeque queue element timeout timeunit]
   (if (nil? timeout)
     (.putLast queue element)
     (.offerLast queue element timeout (->timeunit timeunit)))))

(defn take-first
  "takes from the front of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue]
   (.takeFirst queue))
  ([^BlockingDeque queue timeout]
   (take-first queue timeout :ms))
  ([^BlockingDeque queue timeout timeunit]
   (if (nil? timeout)
     (.takeFirst queue)
     (.pollFirst queue timeout (->timeunit timeunit)))))

(defn take-last
  "takes from the back of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue]
   (.takeLast queue))
  ([^BlockingDeque queue timeout]
   (take-last queue timeout :ms))
  ([^BlockingDeque queue timeout timeunit]
   (if (nil? timeout)
     (.takeLast queue)
     (.pollLast queue timeout (->timeunit timeunit)))))

(defn push
  "puts at the front of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue element]
   (.push queue element)))

(defn pop
  "pops from the front of the queue"
  {:added "3.0"}
  ([^BlockingDeque queue]
   (.pop queue)))

(defn remove
  "removes element from queue"
  {:added "3.0"}
  ([^BlockingQueue queue obj]
   (.remove queue obj)))

(defn process-bulk
  "processes elements in the queue
 
   (def +state+ (atom []))
 
   (q/process-bulk (fn [elems] (swap! +state+ conj elems))
                   (q/queue 1 2 3 4 5) 3)
 
   @+state+
   => [[1 2 3] [4 5]]"
  {:added "3.0"}
  ([f queue maximum]
   (loop [total  (count queue)]
     (let [n      (if (> total maximum)
                    maximum
                    total)
           txs (drain queue n)
           _   (when-not (empty? txs)
                 (f txs))]
       (let [more (count queue)]
         (if-not (zero? more)
           (recur more)))))))

