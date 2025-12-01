(ns std.lib.stream.async-test
  (:use code.test)
  (:require [std.lib.stream.async :refer :all]
            [std.lib.return]
            [std.concurrent.queue :as q]
            [std.lib.future :as f]
            [std.lib.stream :as s]
            [std.lib :as h])
  (:refer-clojure :exclude [realized?]))

^{:refer std.lib.stream.async/x:async :added "3.0"}
(fact "constructs an async transducer"

  (->> (sequence (x:async inc)
                 (range 5))
       (map deref))
  => '(1 2 3 4 5))

^{:refer std.lib.stream.async/x:step :added "3.0"}
(fact "constructs a step transducer" ^:hidden

  (sequence (x:step inc)
            (range 3))
  => (all (contains [mono? mono? mono?])
          (fn [res]
            (= (map deref res)
               [1 2 3])))

  (->> (sequence (comp (x:step inc)
                       (x:step inc)
                       (x:step inc))
                 (range 3))
       (map deref))
  => '(3 4 5)

  (defn slow-inc
    [x]
    (Thread/sleep 10)
    (inc x))

  (def -accept- (q/queue:limited 10))

  (realize (s/unit (comp (x:step  slow-inc)
                         (x:step  slow-inc {:collect [{:path [:time :A]
                                                       :wrap wrap-time}]})
                         (x:step  inc)
                         (x:guard number? -accept-))
                   1))
  => (contains [true 4 anything]))

^{:refer std.lib.stream.async/x:guard :added "3.0"}
(fact "constructs a guard transducer" ^:hidden

  (def -accept- (flux))
  (def -reject- (flux))

  (->> (sequence (comp (x:step inc)
                       (x:guard odd? -accept- -reject-))
                 (range 5))
       (map (comp first deref)))
  =>  [true false true false true]

  (->> (s/produce -accept-)
       (take 3)
       (map deref)
       (sort))
  => '(1 3 5)

  (->> (s/produce -reject-)
       (take 2)
       (map deref)
       (sort))
  => '(2 4))

^{:refer std.lib.stream.async/blocking? :added "3.0"}
(fact "checks that object implements `IBLocking`"

  (blocking? (q/queue))
  => true)

^{:refer std.lib.stream.async/take-element :added "3.0"}
(fact "takes an element from a queue" ^:hidden

  (def |q| (q/queue:fixed 1))

  (f/future (Thread/sleep 100)
            (q/put |q| 1))

  (take-element |q|)
  => 1)

^{:refer std.lib.stream.async/stage? :added "3.0"}
(fact "checks if object implements `IStage`"

  (stage? (f/completed 1))
  => true ^:hidden

  (stage? (mono {}))
  => true)

^{:refer std.lib.stream.async/stage-unit :added "3.0"
  :let [|f| (f/incomplete)]}
(fact "returns the base future unit"

  (stage-unit (mono {:future |f|}))
  => |f|)

^{:refer std.lib.stream.async/stage-realize :added "3.0"}
(fact "returns the unwrapped result"
   ^:hidden
  
  (stage-realize (f/future (Thread/sleep 10) 1))
  => 1

  (stage-realize (mono {:future (f/future (f/future 1))}))
  => 1)

^{:refer std.lib.stream.async/stage-realized? :added "3.0"}
(fact "checks if unwrapped result is realized"

  (stage-realized? (mono {:future (f/completed (f/completed 1))}))
  => true)

^{:refer std.lib.stream.async/blocking-seq :added "3.0"}
(fact "constructs a blocking seq" ^:hidden

  (def -q- (q/queue))

  (def -time- (promise))
  (future
    (deliver -time- (h/bench-ms
                     (into [] (take 3 (blocking-seq -q-))))))
  (doseq [i (range 4)]
    (Thread/sleep 100)
    (q/put -q- i))

  @-time-
  => #(>= % 300))

^{:refer std.lib.stream.async/ipending? :added "3.0"}
(fact "true for promise and future"

  (ipending? (promise)) => true)

^{:refer std.lib.stream.async/realize :added "3.0"}
(fact "retrieves linked future result"

  (realize (f/future (f/future (f/future 3))))
  => 3)

^{:refer std.lib.stream.async/realized? :added "3.0"}
(fact "checks if all linked futures are realized"

  (realized? (f/completed (f/completed 1)))
  => true ^:hidden

  (realized? (f/completed (f/incomplete)))
  => false)

^{:refer std.lib.stream.async/mono? :added "3.0"}
(fact "checks if object is a mono"

  (mono? (mono))
  => any?)

^{:refer std.lib.stream.async/mono :added "3.0"}
(fact "constructs a mono"

  (mono {:future (f/completed 1)
         :data (atom {:name "hello"})
         :parent (mono)})
  => mono?)

^{:refer std.lib.stream.async/wrap-stage :added "3.0"}
(fact "helper for unwrapping nested futures"

  @@((wrap-stage inc) (f/completed (f/completed 1)))
  => 2)

^{:refer std.lib.stream.async/next-stage :added "3.0"}
(fact "constructs the next stage"

  @(next-stage (mono {:future (f/completed 1)})
               inc
               {})
  => 2)

^{:refer std.lib.stream.async/i:async :added "4.1"}
(fact "TODO")

^{:refer std.lib.stream.async/wrap-time :added "3.0"}
(fact "wraps timing given the output"

  ((wrap-time inc) 1)
  => (contains [number? 2]))

^{:refer std.lib.stream.async/wrap-collectors :added "3.0"}
(fact "wrap collectors given async function" ^:hidden

  (def -a- (atom {}))

  ((wrap-collectors inc {:data -a-}
                    [{:wrap wrap-time
                      :path [:a :inc]}
                     {:wrap wrap-time
                      :path [:b :inc]}])
   1)

  @-a-
  => (contains-in {:a {:inc number?},
                   :b {:inc number?}}))

^{:refer std.lib.stream.async/process-mono :added "3.0"}
(fact "chains monos together"

  @(process-mono (mono) inc 1 {})
  => 2 ^:hidden

  @(process-mono (mono)
                 inc
                 (mono {:future (f/future (f/completed 1))})
                 {:pool :async
                  :delay 100})
  => 2)

^{:refer std.lib.stream.async/i:step :added "4.1"}
(fact "TODO")

^{:refer std.lib.stream.async/i:guard :added "4.1"}
(fact "TODO")

^{:refer std.lib.stream.async/produce-flux :added "3.0"}
(fact "takes from a flux"

  (map realized? (take 2 (produce-flux (flux))))
  => '(false false))

^{:refer std.lib.stream.async/collect-flux :added "3.0"}
(fact "writes to a flux"

  (collect-flux (flux) identity (range 5))
  => any?)

^{:refer std.lib.stream.async/flux? :added "3.0"}
(fact "checks if object is a flux"

  (flux? (flux))
  => true)

^{:refer std.lib.stream.async/flux :added "3.0"}
(comment "constructs a flux"
  ^:hidden
  
  (flux)
  
  (def -f- (s/*> (range 10)
                 (flux)))
  
  (def -s- (s/*> -f-
                 (x:step inc)
                 (s/<*>)))
  
  (mapv deref (take 10 -s-))
  => '(1 2 3 4 5 6 7 8 9 10)

  (def -g- (s/*> (range 10)
                 (comp (x:step inc {:collect [{:path [:time :A]
                                               :wrap wrap-time}]})
                       (x:step inc {:collect [{:path [:time :B]
                                               :wrap wrap-time}
                                              {:path [:time :C]
                                               :wrap wrap-time}]}))
                 (flux "hello")))
  
  @(:data (first (s/produce -g-)))
  => (contains-in
      {:time {:A number?, :B number?, :C number?},
       :flux {"hello" {:enter number? :exit number?}}}))

(comment

  (def -a- (f/incomplete))
  (def -b- (f/completed 0))

  (f/on:complete -b- (fn [_ _]
                       (f/future:force -a- 1)))

  @-a-
  (def -f- (flux))

  (s/*> (range 10)
        -f-)

  (def -s- (s/*> (produce-flux -f-)
                 [:stage inc]
                 (s/*)))

  (into {} (first -s-))
  (+ 1 2)
  (map deref (take 20 -s-))

  (def -m- (into {} @(first (produce-flux -f-))))

  (.get (f/completed 1))
  @(:future -m-)

  (try @(:future (into {} @(first -s-)))
       (catch Throwable t
         (.printStackTrace t)))

  (realize (s/unit (comp (x:async)
                         (x:async inc)
                         (x:async inc)
                         (x:async))
                   1))

  {:flux {"hello" {:enter 1598303017142143460, :exit 1598303023052046987}}, :time {:A 5029, :B 2882, :C 10119}}
  @(:data -s-))