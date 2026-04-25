(ns xt.lang.common-iter-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-iter :as it]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-iter :as it]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-iter :as it]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

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
  => [true false false false]

  (!.lua
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
  => [true false false false]

  (!.py
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
  => {}

  (!.js
    (it/arr< (it/iter-null)))

  (!.lua
    (it/arr< (it/iter-null)))
  => {}

  (!.lua
    (it/arr< (it/iter-null)))

  (!.py
    (it/arr< (it/iter-null)))
  => {}

  (!.py
    (it/arr< (it/iter-null))))

^{:refer xt.lang.common-iter/iter? :added "4.1"}
(fact "checks if the input is an iterator"

  (!.js
   [(it/iter? (it/iter [1 2 3]))
    (it/iter? [1 2 3])
    (it/iter? nil)])
  => [true false false]

  (!.lua
   [(it/iter? (it/iter [1 2 3]))
    (it/iter? [1 2 3])
    (it/iter? nil)])
  => [true false false]

  (!.py
   [(it/iter? (it/iter [1 2 3]))
    (it/iter? [1 2 3])
    (it/iter? nil)])
  => [true false false])

^{:refer xt.lang.common-iter/iter :added "4.1"}
(fact "converts values to iterators"

  (!.js
   (it/arr< (it/iter [0 1 2])))
  => [0 1 2]

  (!.js
    (it/arr< (it/iter {:a 1, :b 2})))
  => (just [["a" 1] ["b" 2]] :in-any-order)

  (!.js
   (it/arr< (it/iter nil)))
  => empty?

  (!.lua
   (it/arr< (it/iter [0 1 2])))
  => [0 1 2]

  (!.lua
    (it/arr< (it/iter {:a 1, :b 2})))
  => (just [["a" 1] ["b" 2]] :in-any-order)

  (!.lua
   (it/arr< (it/iter nil)))
  => empty?

  (!.py
   (it/arr< (it/iter [0 1 2])))
  => [0 1 2]

  (!.py
    (it/arr< (it/iter {:a 1, :b 2})))
  => (just [["a" 1] ["b" 2]] :in-any-order)

  (!.py
    (it/arr< (it/iter nil)))
  => empty?)

^{:refer xt.lang.common-iter/collect :added "4.1"}
(fact "collects an iterator"

  (!.js
   (it/collect (it/iter [1 2 3 4])
               (fn [acc e]
                 (xt/x:arr-push acc e)
                 (return acc))
               []))
  => [1 2 3 4]

  (!.lua
   (it/collect (it/iter [1 2 3 4])
               (fn [acc e]
                 (xt/x:arr-push acc e)
                 (return acc))
               []))
  => [1 2 3 4]

  (!.py
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
  => nil

  (!.lua
   (it/nil< (it/iter [1 2 3 4])))
  => nil

  (!.py
   (it/nil< (it/iter [1 2 3 4])))
  => nil)

^{:refer xt.lang.common-iter/arr< :added "4.1"}
(fact "converts an iterator to an array"

  (!.js
   (it/arr< (it/iter [1 2 3 4])))
  => [1 2 3 4]

  (!.lua
   (it/arr< (it/iter [1 2 3 4])))
  => [1 2 3 4]

  (!.py
   (it/arr< (it/iter [1 2 3 4])))
  => [1 2 3 4])

^{:refer xt.lang.common-iter/obj< :added "4.1"}
(fact "converts an iterator of pairs to an object"

  (!.js
   (it/obj< (it/iter [["a" 2] ["b" 4]])))
  => {"a" 2, "b" 4}

  (!.lua
   (it/obj< (it/iter [["a" 2] ["b" 4]])))
  => {"a" 2, "b" 4}

  (!.py
   (it/obj< (it/iter [["a" 2] ["b" 4]])))
  => {"a" 2, "b" 4})

^{:refer xt.lang.common-iter/constantly :added "4.1"}
(fact "constantly outputs the same value"

  (!.js
   (it/arr< (it/take 4 (it/constantly 1))))
  => [1 1 1 1]

  (!.lua
   (it/arr< (it/take 4 (it/constantly 1))))
  => [1 1 1 1]

  (!.py
   (it/arr< (it/take 4 (it/constantly 1))))
  => [1 1 1 1])

^{:refer xt.lang.common-iter/iterate :added "4.1"}
(fact "iterates a function and a starting value"

  (!.js
   (it/arr< (it/take 4 (it/iterate k/inc 1))))
  => [1 2 3 4]

  (!.lua
   (it/arr< (it/take 4 (it/iterate k/inc 1))))
  => [1 2 3 4]

  (!.py
   (it/arr< (it/take 4 (it/iterate k/inc 1))))
  => [1 2 3 4])

^{:refer xt.lang.common-iter/repeatedly :added "4.1"}
(fact "repeatedly calls a function"

  (!.js
    (var box {:count 0})
    (it/arr< (it/take 4
                      (it/repeatedly (fn []
                                       (:+= (. box ["count"]) 1)
                                       (return (. box ["count"])))))))
  => [1 2 3 4]

  (!.lua
    (var box {:count 0})
    (it/arr< (it/take 4
                      (it/repeatedly (fn []
                                       (:+= (. box ["count"]) 1)
                                       (return (. box ["count"])))))))
  => [1 2 3 4]

  (!.py
    (var box {:count 0})
    (it/arr< (it/take 4
                      (it/repeatedly (fn []
                                       (:+= (. box ["count"]) 1)
                                       (return (. box ["count"])))))))
  => [1 2 3 4])

^{:refer xt.lang.common-iter/cycle :added "4.1"}
(fact "cycles a sequence"

  (!.js
   (it/arr< (it/take 5 (it/cycle [1 2 3]))))
  => [1 2 3 1 2]

  (!.lua
   (it/arr< (it/take 5 (it/cycle [1 2 3]))))
  => [1 2 3 1 2]

  (!.py
   (it/arr< (it/take 5 (it/cycle [1 2 3]))))
  => [1 2 3 1 2])

^{:refer xt.lang.common-iter/range :added "4.1"}
(fact "creates a range iterator"

  (!.js
   [(it/arr< (it/range 5))
    (it/arr< (it/range [-10 -3]))
    (it/arr< (it/range [10 1 -3]))])
  => [[0 1 2 3 4] [-10 -9 -8 -7 -6 -5 -4] [10 7 4]]

  (!.lua
   [(it/arr< (it/range 5))
    (it/arr< (it/range [-10 -3]))
    (it/arr< (it/range [10 1 -3]))])
  => [[0 1 2 3 4] [-10 -9 -8 -7 -6 -5 -4] [10 7 4]]

  (!.py
   [(it/arr< (it/range 5))
    (it/arr< (it/range [-10 -3]))
    (it/arr< (it/range [10 1 -3]))])
  => [[0 1 2 3 4] [-10 -9 -8 -7 -6 -5 -4] [10 7 4]])

^{:refer xt.lang.common-iter/drop :added "4.1"}
(fact "drops elements from a sequence"

  (!.js
   (it/arr< (it/drop 3 (it/range 10))))
  => [3 4 5 6 7 8 9]

  (!.lua
   (it/arr< (it/drop 3 (it/range 10))))
  => [3 4 5 6 7 8 9]

  (!.py
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
  => [1 2 3 4 5]

  (!.lua
   (var out [])
   (it/nil< (it/peek (fn [e]
                       (xt/x:arr-push out e))
                     [1 2 3 4 5]))
   out)
  => [1 2 3 4 5]

  (!.py
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
  => [10 15 20 25]

  (!.lua
   (it/arr< (it/take 4 (it/range [10 60 5]))))
  => [10 15 20 25]

  (!.py
   (it/arr< (it/take 4 (it/range [10 60 5]))))
  => [10 15 20 25])

^{:refer xt.lang.common-iter/map :added "4.1"}
(fact "maps a function across a sequence"

  (!.js
   (it/arr< (it/map k/inc [1 2 3])))
  => [2 3 4]

  (!.lua
   (it/arr< (it/map k/inc [1 2 3])))
  => [2 3 4]

  (!.py
   (it/arr< (it/map k/inc [1 2 3])))
  => [2 3 4])

^{:refer xt.lang.common-iter/mapcat :added "4.1"}
(fact "maps a function and concatenates the results"

  (!.js
   [(it/arr< (it/mapcat (fn:> [x] [x x]) [1 2 3]))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [[1 2 3] [4 5 6]]))])
  => [[1 1 2 2 3 3] [1 2 3 4 5 6]]

  (!.lua
   [(it/arr< (it/mapcat (fn:> [x] [x x]) [1 2 3]))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [[1 2 3] [4 5 6]]))])
  => [[1 1 2 2 3 3] [1 2 3 4 5 6]]

  (!.py
   [(it/arr< (it/mapcat (fn:> [x] [x x]) [1 2 3]))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [[1 2 3] [4 5 6]]))])
  => [[1 1 2 2 3 3] [1 2 3 4 5 6]])

^{:refer xt.lang.common-iter/concat :added "4.1"}
(fact "concatenates sequences into an iterator"

  (!.js
   (it/arr< (it/concat [(it/range 3)
                        (it/range [4 6])])))
  => [0 1 2 4 5]

  (!.lua
   (it/arr< (it/concat [(it/range 3)
                        (it/range [4 6])])))
  => [0 1 2 4 5]

  (!.py
   (it/arr< (it/concat [(it/range 3)
                        (it/range [4 6])])))
  => [0 1 2 4 5])

^{:refer xt.lang.common-iter/filter :added "4.1"}
(fact "filters a sequence using a predicate"

  (!.js
   (it/arr< (it/filter k/odd? [1 2 3 4])))
  => [1 3]

  (!.lua
   (it/arr< (it/filter k/odd? [1 2 3 4])))
  => [1 3]

  (!.py
   (it/arr< (it/filter k/odd? [1 2 3 4])))
  => [1 3])

^{:refer xt.lang.common-iter/keep :added "4.1"}
(fact "keeps mapped values that are not nil"

  (!.js
   (it/arr< (it/keep (fn:> [x] (:? (k/odd? x) {:a x}))
                     [1 2 3 4])))
  => [{"a" 1} {"a" 3}]

  (!.lua
   (it/arr< (it/keep (fn:> [x] (:? (k/odd? x) {:a x}))
                     [1 2 3 4])))
  => [{"a" 1} {"a" 3}]

  (!.py
   (it/arr< (it/keep (fn:> [x] (:? (k/odd? x) {:a x}))
                     [1 2 3 4])))
  => [{"a" 1} {"a" 3}])

^{:refer xt.lang.common-iter/partition :added "4.1"}
(fact "partitions a sequence into chunks"

  (!.js
   (it/arr< (it/partition 3 (it/range 10))))
  => [[0 1 2] [4 5 6] [8 9]]

  (!.lua
   (it/arr< (it/partition 3 (it/range 10))))
  => [[0 1 2] [4 5 6] [8 9]]

  (!.py
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
      [1 5 9]]

  (!.lua
   [(it/arr< (it/take-nth 2 (it/range 10)))
    (it/arr< (it/take-nth 3 (it/range 10)))
    (it/arr< (it/take-nth 4 (it/drop 1 (it/range 10))))])
  => [[0 2 4 6 8]
      [0 3 6 9]
      [1 5 9]]

  (!.py
   [(it/arr< (it/take-nth 2 (it/range 10)))
    (it/arr< (it/take-nth 3 (it/range 10)))
    (it/arr< (it/take-nth 4 (it/drop 1 (it/range 10))))])
  => [[0 2 4 6 8]
      [0 3 6 9]
      [1 5 9]])

(comment
  (s/snapto)
  (s/seedgen-langadd 'xt.lang.common-iter {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-iter {:lang [:lua :python] :write true}))
