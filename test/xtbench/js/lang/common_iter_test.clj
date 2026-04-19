(ns xtbench.js.lang.common-iter-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-iter :as it]
             [xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-iter/iter-eq :added "4.1"}
(fact "checks that two iterators are equal"
  (!.js
   (var eq-fn (fn:> [a b] (== a b)))
   [(it/iter-eq (it/iter [1 2 4 4])
                (it/iter [1 2 4 4])
                eq-fn)
    (it/iter-eq (it/iter [1 2 4 4])
                (it/iter [1 2 3 4])
                eq-fn)
    (it/iter-eq (it/iter [1 2 4])
                (it/iter [1 2 4 4])
                eq-fn)
    (it/iter-eq (it/iter [1 2 4 4])
                (it/iter [1 2 4])
                eq-fn)])
  => [true false false false])

^{:refer xt.lang.common-iter/iter-null :added "4.1"}
(fact "creates a null iterator"
  (!.js
   (it/arr< (it/iter-null)))
  => {})

^{:refer xt.lang.common-iter/iter? :added "4.1"}
(fact "checks if the input is an iterator"
  (!.js
   [(it/iter? (it/iter [1 2 3]))
    (it/iter? [1 2 3])
    (it/iter? nil)])
  => [true false false])

^{:refer xt.lang.common-iter/iter :added "4.1"}
(fact "converts values to iterators"
  (!.js
   (it/arr< (it/iter [0 1 2])))
  => [0 1 2]

  (set (!.js
        (it/arr< (it/iter {:a 1, :b 2}))))
  => #{["a" 1] ["b" 2]}

  (!.js
   (it/arr< (it/iter nil)))
  => {})

^{:refer xt.lang.common-iter/collect :added "4.1"}
(fact "collects an iterator"
  (!.js
   (it/collect (it/iter [1 2 3 4])
               (fn [acc e]
                 (xt/x:arr-push acc e)
                 (return acc))
               []))
  => [1 2 3 4])

^{:refer xt.lang.common-iter/nil< :added "4.1"}
(fact "consumes an iterator and returns nil"
  (!.js
   (it/nil< (it/iter [1 2 3 4])))
  => nil)

^{:refer xt.lang.common-iter/arr< :added "4.1"}
(fact "converts an iterator to an array"
  (!.js
   (it/arr< (it/iter [1 2 3 4])))
  => [1 2 3 4])

^{:refer xt.lang.common-iter/obj< :added "4.1"}
(fact "converts an iterator of pairs to an object"
  (!.js
   (it/obj< (it/iter [["a" 2] ["b" 4]])))
  => {"a" 2, "b" 4})

^{:refer xt.lang.common-iter/constantly :added "4.1"}
(fact "constantly outputs the same value"
  (!.js
   (it/arr< (it/take 4 (it/constantly 1))))
  => [1 1 1 1])

^{:refer xt.lang.common-iter/iterate :added "4.1"}
(fact "iterates a function and a starting value"
  (!.js
   (it/arr< (it/take 4 (it/iterate k/inc 1))))
  => [1 2 3 4])

^{:refer xt.lang.common-iter/repeatedly :added "4.1"}
(fact "repeatedly calls a function"
  (!.js
   (var i := 0)
   (it/arr< (it/take 4
                     (it/repeatedly (fn []
                                      (:= i (+ i 1))
                                      (return i))))))
  => [1 2 3 4])

^{:refer xt.lang.common-iter/cycle :added "4.1"}
(fact "cycles a sequence"
  (!.js
   (it/arr< (it/take 5 (it/cycle [1 2 3]))))
  => [1 2 3 1 2])

^{:refer xt.lang.common-iter/range :added "4.1"}
(fact "creates a range iterator"
  (!.js
   [(it/arr< (it/range 5))
    (it/arr< (it/range [-10 -3]))
    (it/arr< (it/range [10 1 -3]))])
  => [[0 1 2 3 4] [-10 -9 -8 -7 -6 -5 -4] [10 7 4]])

^{:refer xt.lang.common-iter/drop :added "4.1"}
(fact "drops elements from a sequence"
  (!.js
   (it/arr< (it/drop 3 (it/range 10))))
  => [3 4 5 6 7 8 9])

^{:refer xt.lang.common-iter/peek :added "4.1"}
(fact "peeks at each value and passes it through"
  (!.js
   (var out [])
   (it/nil< (it/peek (fn [e]
                       (xt/x:arr-push out e))
                     [1 2 3 4 5]))
   out)
  => [1 2 3 4 5])

^{:refer xt.lang.common-iter/take :added "4.1"}
(fact "takes elements from a sequence"
  (!.js
   (it/arr< (it/take 4 (it/range [10 60 5]))))
  => [10 15 20 25])

^{:refer xt.lang.common-iter/map :added "4.1"}
(fact "maps a function across a sequence"
  (!.js
   (it/arr< (it/map k/inc [1 2 3])))
  => [2 3 4])

^{:refer xt.lang.common-iter/mapcat :added "4.1"}
(fact "maps a function and concatenates the results"
  (!.js
   [(it/arr< (it/mapcat (fn:> [x] [x x]) [1 2 3]))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [[1 2 3] [4 5 6]]))])
  => [[1 1 2 2 3 3] [1 2 3 4 5 6]])

^{:refer xt.lang.common-iter/concat :added "4.1"}
(fact "concatenates sequences into an iterator"
  (!.js
   (it/arr< (it/concat [(it/range 3)
                        (it/range [4 6])])))
  => [0 1 2 4 5])

^{:refer xt.lang.common-iter/filter :added "4.1"}
(fact "filters a sequence using a predicate"
  (!.js
   (it/arr< (it/filter k/odd? [1 2 3 4])))
  => [1 3])

^{:refer xt.lang.common-iter/keep :added "4.1"}
(fact "keeps mapped values that are not nil"
  (!.js
   (it/arr< (it/keep (fn:> [x] (:? (k/odd? x) {:a x}))
                     [1 2 3 4])))
  => [{"a" 1} {"a" 3}])

^{:refer xt.lang.common-iter/partition :added "4.1"}
(fact "partitions a sequence into chunks"
  (!.js
   (it/arr< (it/partition 3 (it/range 10))))
  => [[0 1 2] [4 5 6] [8 9]])

^{:refer xt.lang.common-iter/take-nth :added "4.1"}
(fact "takes the first and then every nth item of a sequence"
  (!.js
   [(it/arr< (it/take-nth 2 (it/range 10)))
    (it/arr< (it/take-nth 3 (it/range 10)))
    (it/arr< (it/take-nth 4 (it/drop 1 (it/range 10))))])
  => [[0 2 4 6 8]
      [0 3 6 9]
      [1 5 9]])
