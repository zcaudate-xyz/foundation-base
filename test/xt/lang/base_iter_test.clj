(ns xt.lang.base-iter-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.json :as json]
            [net.http :as http]
            [xt.lang.base-notify :as notify]
            [rt.basic :as basic]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-macro :as k]
             [xt.lang.base-lib :as lib]
             [xt.lang.base-iter :as it]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-macro :as k]
             [xt.lang.base-lib :as lib]
             [xt.lang.base-iter :as it]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-macro :as k]
             [xt.lang.base-lib :as lib]
             [xt.lang.base-iter :as it]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-iter/for:iter :added "4.0"}
(fact "helper function to `for:iter` macro"
  ^:hidden
  
  (!.js
   (var out := [])
   (for:iter [e [1 2 3 4]] (x:arr-push out e))
   out)
  => [1 2 3 4]

  (!.lua
   (var out := [])
   (for:iter [e (it/iter-from-arr [1 2 3 4])]
             (x:arr-push out e))
   out)
  => [1 2 3 4]
  
  (!.py
   (var out := [])
   (for:iter [e [1 2 3 4]] (x:arr-push out e))
   out)
  => [1 2 3 4])

^{:refer xt.lang.base-iter/iter-from-obj :added "4.0"}
(fact "creates iterator from object"
  ^:hidden
  
  (l/emit-as
   :js '[(xt.lang.base-iter/iter-from-obj sym)])
  => "Object.entries(sym)[Symbol.iterator]()"

  (l/emit-as
   :lua '[(xt.lang.base-iter/iter-from-obj sym)])
  => "coroutine.wrap(function ()\n  for k, v in pairs(sym) do\n    coroutine.yield({k,v})\n  end\nend)"

  (l/emit-as
   :python '[(xt.lang.base-iter/iter-from-obj sym)])
  => "iter(sym.items())"

  (set (!.js
        (it/arr< (it/iter-from-obj {:a 1 :b 2}))))
  => #{["b" 2] ["a" 1]}

  (set (!.lua
        (it/arr< (it/iter-from-obj {:a 1 :b 2}))))
  => #{["b" 2] ["a" 1]}

  (set (!.py
        (it/arr< (it/iter-from-obj {:a 1 :b 2}))))
  => #{["b" 2] ["a" 1]})

^{:refer xt.lang.base-iter/iter-from-arr :added "4.0"}
(fact "creates iterator from arr"
  ^:hidden
  
  (l/emit-as
   :js '[(xt.lang.base-iter/iter-from-arr sym)])
  => "sym[Symbol.iterator]()"
  
  (l/emit-as
   :lua '[(xt.lang.base-iter/iter-from-arr sym)])
  => "coroutine.wrap(function ()\n  for _, v in ipairs(sym) do\n    coroutine.yield(v)\n  end\nend)"

  (l/emit-as
   :python '[(xt.lang.base-iter/iter-from-arr sym)])
  => "iter(sym)")

^{:refer xt.lang.base-iter/iter-from :added "4.0"}
(fact "creates iterator from generic"
  ^:hidden
  
  (l/emit-as
   :js '[(xt.lang.base-iter/iter-from sym)])
  => "sym[Symbol.iterator]()"

  (l/emit-as
   :lua '[(xt.lang.base-iter/iter-from sym)])
  => "coroutine.wrap(function ()\n  for e in sym['iterator'] do\n    coroutine.yield(v)\n  end\nend)"

  (l/emit-as
   :python '[(xt.lang.base-iter/iter-from sym)])
  => "iter(sym)")

^{:refer xt.lang.base-iter/iter-next :added "4.0"}
(fact "gets next value of iterator"
  ^:hidden
  
  (!.js
   (it/iter-next (it/iter [1 2 3])))
  => {"value" 1, "done" false}

  (!.lua
   (it/iter-next (it/iter [1 2 3])))
  => 1

  (!.py
   (it/iter-next (it/iter [1 2 3])))
  => 1)

^{:refer xt.lang.base-iter/iter-has? :added "4.0"}
(fact "checks that type has iterator (for generics)"
  ^:hidden
  
  (!.js
   [(it/iter-has? 123)
    (it/iter-has? [])])
  => [false true]

  (!.lua
   [(it/iter-has? 123)
    (it/iter-has? [1 2])])
  => [false false]

  (!.py
   [(it/iter-has? 123)
    (it/iter-has? [])])
  => [false true])

^{:refer xt.lang.base-iter/iter-native? :added "4.0"}
(fact "checks that input is an iterator"
  ^:hidden
  
  (!.js
   [(it/iter-native? (it/iter []))
    (it/iter-native? 1)])
  => [true false]

  (!.lua
   [(it/iter-native? (it/iter []))
    (it/iter-native? 1)])
  => [true false]
  
  (!.py
   [(it/iter-native? (it/iter []))
    (it/iter-native? 1)])
  => [true false])

^{:refer xt.lang.base-iter/iter-eq :added "4.0"}
(fact "checks that two iterators are equal"
  ^:hidden
  
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

^{:refer xt.lang.base-iter/iter-null :added "4.0"}
(fact "creates a null iterator"
  ^:hidden
  
  (!.js
   (it/arr< (it/iter-null)))
  => []

  (!.lua
   (it/arr< (it/iter-null)))
  => {}

  (!.py
   (it/arr< (it/iter-null)))
  => [])

^{:refer xt.lang.base-iter/iter? :added "4.0"}
(fact "checks object is an iter")

^{:refer xt.lang.base-iter/iter :added "4.0"}
(fact "converts to an iterator"
  ^:hidden

  (!.js (it/arr< (it/iter [0 1 2])))
  => [0 1 2]
  
  (!.lua (it/arr< (it/iter [0 1 2])))
  => [0 1 2]

  (!.py (it/arr< (it/iter [0 1 2])))
  => [0 1 2])

^{:refer xt.lang.base-iter/collect :added "4.0"}
(fact "collects an iterator"
  ^:hidden
  
  (!.js
   (it/collect [1 2 3 4]
               lib/step-push
               []))
  => [1 2 3 4]

  (!.lua
   (it/collect (it/iter  [1 2 3 4])
               lib/step-push
               []))
  => [1 2 3 4]
  
  (!.py
   (it/collect [1 2 3 4]
               lib/step-push
               []))
  => [1 2 3 4])

^{:refer xt.lang.base-iter/nil< :added "4.0"}
(fact "consumes an iterator, returns nil"
  ^:hidden
  
  (!.js
   (it/nil< (it/iter [1 2 3 4])))
  => nil

  (!.lua
   (it/nil< (it/iter [1 2 3 4])))
  => nil
  
  (!.py
   (it/nil< (it/iter [1 2 3 4])))
  => nil)

^{:refer xt.lang.base-iter/arr< :added "4.0"}
(fact "converts an array to iterator"
  ^:hidden
  
  (!.js
   (it/arr< (it/iter [1 2 3 4])))
  => [1 2 3 4]
  
  (!.js
   (it/arr< (it/iter [1 2 3 4])))
  => [1 2 3 4]

  (!.js
   (it/arr< (it/iter [1 2 3 4])))
  => [1 2 3 4])

^{:refer xt.lang.base-iter/obj< :added "4.0"}
(fact "converts an array to object"
  ^:hidden

  (!.js
   (it/obj< (it/iter [["a" 2] ["b" 4]])))
  => {"a" 2, "b" 4}
  
  (!.lua
   (it/obj< (it/iter [["a" 2] ["b" 4]])))
  => {"a" 2, "b" 4}

  (!.py
   (it/obj< [["a" 2] ["b" 4]]))
  => {"a" 2, "b" 4})

^{:refer xt.lang.base-iter/constantly :added "4.0"}
(fact "constantly outputs the same value"
  ^:hidden
  
  (!.js
   (it/arr< (it/take 4 (it/constantly 1))))
  => [1 1 1 1]

  (!.lua
   (it/arr< (it/take 4 (it/constantly 1))))
  => [1 1 1 1]

  (!.py
   (it/arr< (it/take 4 (it/constantly 1))))
  => [1 1 1 1])

^{:refer xt.lang.base-iter/iterate :added "4.0"}
(fact "iterates a function and a starting value"
  ^:hidden
  
  (!.js
   (it/arr< (it/take 4 (it/iterate k/inc 1))))
  => [1 2 3 4]

  (!.lua
   (it/arr< (it/take 4 (it/iterate k/inc 1))))
  => [1 2 3 4]
  
  (!.py
   (it/arr< (it/take 4 (it/iterate k/inc 1))))
  => [1 2 3 4])

^{:refer xt.lang.base-iter/repeatedly :added "4.0"}
(fact "repeatedly calls a function"
  ^:hidden
  
  (!.js
   (var f (it/iterate k/inc 1))
   (it/arr< (it/take 4 f)))
  => [1 2 3 4]

  (!.lua
   (var f (it/iterate k/inc 1))
   (it/arr< (it/take 4 f)))
  => [1 2 3 4]
  
  (!.py
   (var f (it/iterate k/inc 1))
   (it/arr< (it/take 4 f)))
  => [1 2 3 4])

^{:refer xt.lang.base-iter/cycle :added "4.0"}
(fact "cycles a function"
  ^:hidden
  
  (!.js 
   (it/arr< (it/take 5 (it/cycle [1 2 3]))))
  => [1 2 3 1 2]

  (!.lua
   (it/arr< (it/take 5 (it/cycle [1 2 3]))))
  => [1 2 3 1 2]

  (!.py 
   (it/arr< (it/take 5 (it/cycle [1 2 3]))))
  => [1 2 3 1 2])

^{:refer xt.lang.base-iter/range :added "4.0"}
(fact "setup a range function"
  ^:hidden
  
  (!.js
   (it/arr< (it/range [-10 -3])))
  => [-10 -9 -8 -7 -6 -5 -4]

  (!.lua
   (it/arr< (it/range [-10 -3])))
  => [-10 -9 -8 -7 -6 -5 -4]

  (!.py
   (it/arr< (it/range [-10 -3])))
  => [-10 -9 -8 -7 -6 -5 -4])

^{:refer xt.lang.base-iter/drop :added "4.0"}
(fact "drop elements from seq"
  ^:hidden
  
  (!.js
   (it/arr< (it/drop 3 (it/range 10))))
  => [3 4 5 6 7 8 9]

  (!.js
   (it/arr< (it/drop 3 (it/range 10))))
  => [3 4 5 6 7 8 9]

  (!.js
   (it/arr< (it/drop 3 (it/range 10))))
  => [3 4 5 6 7 8 9])

^{:refer xt.lang.base-iter/peek :added "4.0"}
(fact "peeks at value and passes it on"
  ^:hidden
  
  (!.js
   (var out := [])
   (it/nil< (it/peek (fn [e]
                       (lib/step-push out e))
                     [1 2 3 4 5]))
   out)
  => [1 2 3 4 5]

  (!.lua
   (var out := [])
   (it/nil< (it/peek (fn [e]
                       (lib/step-push out e))
                    [1 2 3 4 5]))
   out)
  => [1 2 3 4 5]

  (!.py
   (var out := [])
   (it/nil< (it/peek (fn [e]
                      (lib/step-push out e))
                    [1 2 3 4 5]))
   out)
  => [1 2 3 4 5])

^{:refer xt.lang.base-iter/take :added "4.0"}
(fact "take elements from seq"
  ^:hidden
  
  (!.js
   (it/arr< (it/take 4 (it/range [10 60 5]))))
  => [10 15 20 25]

  (!.lua
   (it/arr< (it/take 4 (it/range [10 60 5]))))
  => [10 15 20 25]

  (!.py
   (it/arr< (it/take 4 (it/range [10 60 5]))))
  => [10 15 20 25])

^{:refer xt.lang.base-iter/map :added "4.0"}
(fact "maps a function across seq"
  ^:hidden
  
  (!.js
   (it/arr< (it/map k/inc [1 2 3])))
  => [2 3 4]

  (!.lua
   (it/arr< (it/map k/inc [1 2 3])))
  => [2 3 4]

  (!.py
   (it/arr< (it/map k/inc [1 2 3])))
  => [2 3 4])

^{:refer xt.lang.base-iter/mapcat :added "4.0"}
(fact "maps a function a concats"
  ^:hidden
  
  (!.js
   [(it/arr< (it/mapcat (fn:> [x] [x x]) [1 2 3]))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [[1 2 3] [4 5 6]]))
    (it/arr< (it/mapcat (fn:> [x] (it/range x))
                       (it/range 4)))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [(it/range 3) (it/range 3)]))])
  => [[1 1 2 2 3 3]
      [1 2 3 4 5 6]
      [0 0 1 0 1 2]
      [0 1 2 0 1 2]]

  (!.lua
   [(it/arr< (it/mapcat (fn:> [x] [x x]) [1 2 3]))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [[1 2 3] [4 5 6]]))
    (it/arr< (it/mapcat (fn:> [x] (it/range x))
                       (it/range 4)))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [(it/range 3) (it/range 3)]))])
  => [[1 1 2 2 3 3]
      [1 2 3 4 5 6]
      [0 0 1 0 1 2]
      [0 1 2 0 1 2]]

  
  (!.py
   [(it/arr< (it/mapcat (fn:> [x] [x x]) [1 2 3]))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [[1 2 3] [4 5 6]]))
    (it/arr< (it/mapcat (fn:> [x] (it/range x))
                       (it/range 4)))
    (it/arr< (it/mapcat (fn:> [x] x)
                       [(it/range 3) (it/range 3)]))])
  => [[1 1 2 2 3 3]
      [1 2 3 4 5 6]
      [0 0 1 0 1 2]
      [0 1 2 0 1 2]])

^{:refer xt.lang.base-iter/concat :added "4.0"}
(fact "concats seqs into iterator"
  ^:hidden
  
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

^{:refer xt.lang.base-iter/filter :added "4.0"}
(fact "filters a seq using a function"
  ^:hidden

  (!.js
   (it/arr< (it/filter k/odd? [1 2 3 4])))
  => [1 3]

  (!.lua
   (it/arr< (it/filter k/odd? [1 2 3 4])))
  => [1 3]

  (!.py
   (it/arr< (it/filter k/odd? [1 2 3 4])))
  => [1 3])

^{:refer xt.lang.base-iter/keep :added "4.0"}
(fact "keeps a seq using a function"
  ^:hidden
  
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

^{:refer xt.lang.base-iter/partition :added "4.0"}
(fact "partition seq into n items"
  ^:hidden
  
  (!.js
   (it/arr< (it/partition 3 (it/range 10))))
  => [[0 1 2] [4 5 6] [8 9]]

  (!.lua
   (it/arr< (it/partition 3 (it/range 10))))
  => [[0 1 2] [4 5 6] [8 9]]
  
  (!.py
   (it/arr< (it/partition 3 (it/range 10))))
  => [[0 1 2] [4 5 6] [8 9]])

^{:refer xt.lang.base-iter/take-nth :added "4.0"}
(fact "takes first and then every nth item of a seq"
  ^:hidden
  
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
