(ns xtbench.ruby.lang.common-data-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :ruby
  {:runtime :basic,
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-data/is-empty? :added "4.1"}
(fact "checks that array is empty"

  (!.rb
   [(xtd/is-empty? nil)
    (xtd/is-empty? "")
    (xtd/is-empty? "123")
    (xtd/is-empty? [])
    (xtd/is-empty? [1 2 3])
    (xtd/is-empty? {})
    (xtd/is-empty? {:a 1, :b 2})])
  => [true true false true false true false])

^{:refer xt.lang.common-data/not-empty? :added "4.1"}
(fact "checks that array is not empty"

  (!.rb
   [(xtd/not-empty? nil)
    (xtd/not-empty? "")
    (xtd/not-empty? "123")
    (xtd/not-empty? [])
    (xtd/not-empty? [1 2 3])
    (xtd/not-empty? {})
    (xtd/not-empty? {:a 1, :b 2})])
  => [false false true false true false true])

^{:refer xt.lang.common-data/lu-create :added "4.1"}
(fact "creates a lookup table"

  (!.rb
   (var lu (xtd/lu-create))
   (xtd/lu-get lu "missing"))
  => nil)

^{:refer xt.lang.common-data/lu-del :added "4.1"}
(fact "deletes a lookup entry"

  (!.rb
    (var lu (xtd/lu-create))
    (xtd/lu-set lu "a" 1)
    (xtd/lu-del lu "a")
    (xtd/lu-get lu "a"))
  => nil)

^{:refer xt.lang.common-data/lu-get :added "4.1"}
(fact "gets a lookup entry"

  (!.rb
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 1)
   (xtd/lu-get lu "a"))
  => 1

  (!.rb
    (var lu (xtd/lu-create))
    (xtd/lu-set lu lu 2)
    (xtd/lu-get lu lu))
  => 2)

^{:refer xt.lang.common-data/lu-set :added "4.1"}
(fact "sets a lookup entry"

  (!.rb
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 2)
   (xtd/lu-get lu "a"))
  => 2)

^{:refer xt.lang.common-data/lu-eq :added "4.1"}
(fact "checks lookup equality"

  (!.rb
   (var lu0 (xtd/lu-create))
   (var lu1 (xtd/lu-create))
   [(xtd/lu-eq lu0 lu0)
    (xtd/lu-eq lu0 lu1)])
  => [true false])

^{:refer xt.lang.common-data/first :added "4.1"}
(fact "first"

  (!.rb (xtd/first [1 2 3]))
  => 1)

^{:refer xt.lang.common-data/second :added "4.1"}
(fact "gets the second item"

  (!.rb (xtd/second [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/nth :added "4.1"}
(fact "gets the nth item (index 0)"

  (!.rb [(xtd/nth [1 2 3] 0)
          (xtd/nth [1 2 3] 2)])
  => [1 3])

^{:refer xt.lang.common-data/last :added "4.1"}
(fact "gets the last item"

  (!.rb (xtd/last [1 2 3]))
  => 3)

^{:refer xt.lang.common-data/second-last :added "4.1"}
(fact "gets the second-last item"

  (!.rb (xtd/second-last [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/arr-empty? :added "4.1"}
(fact "checks that arrect is empty"

  (!.rb [(xtd/arr-empty? nil)
          (xtd/arr-empty? [])
          (xtd/arr-empty? [1])])
  => [true true false])

^{:refer xt.lang.common-data/arr-not-empty? :added "4.1"}
(fact "checks that arrect is not empty"

  (!.rb [(xtd/arr-not-empty? nil)
          (xtd/arr-not-empty? [])
          (xtd/arr-not-empty? [1])])
  => [false false true])

^{:refer xt.lang.common-data/arrayify :added "4.1"}
(fact "makes something into an array"

  (!.rb [(xtd/arrayify 1)
          (xtd/arrayify [1])])
  => [[1] [1]])

^{:refer xt.lang.common-data/arr-lookup :added "4.1"}
(fact "constructs a lookup given keys"

  (!.rb (xtd/obj-keys (xtd/arr-lookup ["a" "b"])))
  => (just ["a" "b"] :in-any-order))

^{:refer xt.lang.common-data/arr-omit :added "4.1"}
(fact "emits index from new array"

  (!.rb (xtd/arr-omit [1 2 3] 1))
  => [1 3])

^{:refer xt.lang.common-data/arr-reverse :added "4.1"}
(fact "reverses the array"

  (!.rb (xtd/arr-reverse [1 2 3]))
  => [3 2 1])

^{:refer xt.lang.common-data/arr-zip :added "4.1"}
(fact "zips two arrays together into a map"

  (!.rb (xtd/arr-zip ["a" "b"] [1 2]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/arr-clone :added "4.1"}
(fact "clones an array"

  (!.rb
   (var src [1 2])
   (var out (xtd/arr-clone src))
   (xt/x:arr-push src 3)
   out)
  => [1 2])

^{:refer xt.lang.common-data/arr-assign :added "4.1"}
(fact "appends to the end of an array"

  (!.rb (xtd/arr-assign [1 2] [3 4]))
  => [1 2 3 4])

^{:refer xt.lang.common-data/arr-concat :added "4.1"}
(fact "concatenates arrays without mutating the input"

  (!.rb
   (var src [1 2])
   [(xtd/arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]])

^{:refer xt.lang.common-data/arr-slice :added "4.1"}
(fact "slices an array"

  (!.rb (xtd/arr-slice [1 2 3 4] 1 3))
  => [2 3])

^{:refer xt.lang.common-data/arr-rslice :added "4.1"}
(fact "gets the reverse of a slice"

  (!.rb (xtd/arr-rslice [1 2 3 4] 1 3))
  => [3 2])

^{:refer xt.lang.common-data/arr-tail :added "4.1" :lang-exceptions {:dart {:skip true}}}
(fact "gets the tail of the array"

  (!.rb (xtd/arr-tail [1 2 3 4] 2))
  => [4 3])

^{:refer xt.lang.common-data/arr-range :added "4.1"}
(fact "creates a range array"

  (!.rb [(xtd/arr-range 5)
          (xtd/arr-range [2 6])
          (xtd/arr-range [2 7 2])])
  => [[0 1 2 3 4] [2 3 4 5] [2 4 6]])

^{:refer xt.lang.common-data/arr-intersection :added "4.1"}
(fact "gets the intersection of two arrays"

  (!.rb (xtd/arr-intersection [1 2 3] [2 3 4]))
  => [2 3])

^{:refer xt.lang.common-data/arr-difference :added "4.1"}
(fact "gets the difference of two arrays"

  (!.rb (xtd/arr-difference [1 2] [2 3 4]))
  => [3 4])

^{:refer xt.lang.common-data/arr-union :added "4.1"}
(fact "gets the union of two arrays"

  (!.rb (xtd/arr-union [1 2] [2 3]))
  => (contains [1 2 3] :in-any-order))

^{:refer xt.lang.common-data/arr-shuffle :added "4.1"}
(fact "shuffles the array"

  (!.rb (xtd/arr-shuffle [1 2 3]))
  => (contains [1 2 3] :in-any-order))

^{:refer xt.lang.common-data/arr-pushl :added "4.1"}
(fact "pushs an element into array"

  (!.rb (xtd/arr-pushl [1 2 3] 4 3))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-pushr :added "4.1"}
(fact "pushs an element into array"

  (!.rb (xtd/arr-pushr [1 2 3] 0 3))
  => [0 1 2])

^{:refer xt.lang.common-data/arr-interpose :added "4.1"}
(fact "puts element between array"

  (!.rb (xtd/arr-interpose [1 2 3] 0))
  => [1 0 2 0 3])

^{:refer xt.lang.common-data/arr-random :added "4.1"}
(fact "gets a random element from array"

  (!.rb (xtd/arr-random [1]))
  => 1)

^{:refer xt.lang.common-data/arr-sample :added "4.1"}
(fact "samples array according to probability"

  (!.rb (xtd/arr-sample ["a" "b" "c"] [0 1 0]))
  => "b")

^{:refer xt.lang.common-data/obj-empty? :added "4.1"}
(fact "checks that object is empty"

  (!.rb [(xtd/obj-empty? {})
          (xtd/obj-empty? {:a 1})])
  => [true false])

^{:refer xt.lang.common-data/obj-not-empty? :added "4.1"}
(fact "checks that object is not empty"

  (!.rb [(xtd/obj-not-empty? {})
          (xtd/obj-not-empty? {:a 1})])
  => [false true])

^{:refer xt.lang.common-data/obj-first-key :added "4.1"}
(fact "gets the first key"

  (!.rb (xtd/obj-first-key {:a 1}))
  => "a")

^{:refer xt.lang.common-data/obj-first-val :added "4.1"}
(fact "gets the first val"

  (!.rb (xtd/obj-first-val {:a 1}))
  => 1)

^{:refer xt.lang.common-data/obj-keys :added "4.1"}
(fact "gets keys of an object"

  (!.rb (xtd/obj-keys {:a 1, :b 2}))
  => (just ["a" "b"] :in-any-order))

^{:refer xt.lang.common-data/obj-vals :added "4.1"}
(fact "gets vals of an object"

  (!.rb (xtd/obj-vals {:a 1, :b 2}))
  => (just [1 2] :in-any-order))

^{:refer xt.lang.common-data/obj-pairs :added "4.1"}
(fact "creates entry pairs from object"

  (!.rb (xtd/obj-pairs {:a 1, :b 2}))
  => (just [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.common-data/obj-clone :added "4.1"}
(fact "clones an object"

  (!.rb
   (var src {:a 1})
   (var out (xtd/obj-clone src))
   (xt/x:set-key src "b" 2)
   out)
  => {"a" 1})

^{:refer xt.lang.common-data/obj-assign :added "4.1"}
(fact "merges key value pairs from into another"

  (!.rb (xtd/obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2}

  (!.rb
   (var out {})
   [(xtd/obj-assign out {:a 1})
    out])
  => [{"a" 1} {"a" 1}])

^{:refer xt.lang.common-data/obj-assign-nested :added "4.1"}
(fact "merges objects at a nesting level"

  (!.rb (xtd/obj-assign-nested {:a {:b 1}} {:a {:c 2}}))
  => {"a" {"b" 1, "c" 2}}

  (!.rb
   (var out {})
   [(xtd/obj-assign-nested out {:a {:c 2}})
    out])
  => [{"a" {"c" 2}} {"a" {"c" 2}}])

^{:refer xt.lang.common-data/obj-assign-with :added "4.1"}
(fact "merges second into first given a function"

  (!.rb
   (xtd/obj-assign-with
    {:a 1, :b 2}
    {:a 3, :c 4}
    (fn [x y] (return (+ x y)))))
  => {"a" 4, "b" 2, "c" 4})

^{:refer xt.lang.common-data/obj-from-pairs :added "4.1"}
(fact "creates an object from pairs"

  (!.rb (xtd/obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/obj-del :added "4.1"}
(fact "deletes multiple keys"

  (!.rb (xtd/obj-del {:a 1, :b 2, :c 3} ["a" "c"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-del-all :added "4.1"}
(fact "obj del all"

  (!.rb
    (var out {:a 1, :b 2})
    (xtd/obj-del-all out)
    out)
  => {})

^{:refer xt.lang.common-data/obj-pick :added "4.1"}
(fact "select keys in object"

  (!.rb (xtd/obj-pick {:a 1, :b 2} ["a"]))
  => {"a" 1})

^{:refer xt.lang.common-data/obj-omit :added "4.1"}
(fact "new object with missing keys"

  (!.rb (xtd/obj-omit {:a 1, :b 2} ["a"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-transpose :added "4.1"}
(fact "obj-transposes a map"

  (!.rb (xtd/obj-transpose {:a "x", :b "y"}))
  => {"x" "a", "y" "b"})

^{:refer xt.lang.common-data/obj-nest :added "4.1"}
(fact "creates a nested object"

  (!.rb (xtd/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/get-in :added "4.1"}
(fact "gets item in object"

  (!.rb [(xtd/get-in {:a {:b {:c 1}}} ["a" "b"])
          (xtd/get-in {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1])

^{:refer xt.lang.common-data/set-in :added "4.1"}
(fact "sets item in object"

  [(!.rb
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "b"] 2)
     a)
   (!.rb
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "d"] 2)
     a)]
  => [{"a" {"b" 2}}
      {"a" {"b" {"c" 1}, "d" 2}}])

^{:refer xt.lang.common-data/obj-intersection :added "4.1"}
(fact "finds the intersection between map lookups"

  (!.rb (xtd/obj-intersection {:a true, :b true} {:b true, :c true}))
  => ["b"])

^{:refer xt.lang.common-data/obj-keys-nested :added "4.1"}
(fact "gets nested keys"

  (!.rb (xtd/obj-keys-nested {:a {:b 1, :c 2}} []))
  => (just [[["a" "b"] 1]
            [["a" "c"] 2]]
           :in-any-order))

^{:refer xt.lang.common-data/obj-difference :added "4.1"}
(fact "finds the difference between two map lookups"

  (!.rb (xtd/obj-difference {:a true, :b true} {:b true, :c true}))
  => ["c"])

^{:refer xt.lang.common-data/swap-key :added "4.1"}
(fact "swaps a value in the key with a function"

  (!.rb
    (var out {:a 1})
    (xtd/swap-key out "a" (fn [x y] (return (+ x y))) [2])
    out)
  => {"a" 3})

^{:refer xt.lang.common-data/to-flat :added "4.1"}
(fact "flattens pairs of object into array"

  (!.rb (xtd/to-flat [["a" 1] ["b" 2]]))
  => ["a" 1 "b" 2])

^{:refer xt.lang.common-data/set-pair-step :added "4.1"}
(fact "sets a pair into an object and returns it"

  (!.rb
    (var out {})
    [(xt/x:get-key (xtd/set-pair-step out "a" 1) "a")
     (xt/x:get-key (xtd/set-pair-step out "b" 2) "b")
     out])
  => [1 2 {"a" 1, "b" 2}])

^{:refer xt.lang.common-data/from-flat :added "4.1"}
(fact "creates object from flattened pair array"

  (!.rb
   (xtd/from-flat
     ["a" 1 "b" 2]
     xtd/set-pair-step
     {}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/arr-every :added "4.1"}
(fact "checks that every element fulfills thet predicate"

  (!.rb
   [(xtd/arr-every [2 4 6] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-every [2 3 6] (fn [x] (return (== 0 (mod x 2)))))])
  => [true false])

^{:refer xt.lang.common-data/arr-some :added "4.1"}
(fact "checks that the array contains an element"

  (!.rb
   [(xtd/arr-some [1 3 5] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-some [1 2 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [false true])

^{:refer xt.lang.common-data/arr-each :added "4.1"}
(fact "performs a function call for each element"

  (!.rb
   (var out [])
   (xtd/arr-each [1 2 3] (fn [x] (xt/x:arr-push out (+ x 1))))
   out)
  => [2 3 4])

^{:refer xt.lang.common-data/arr-find :added "4.1"}
(fact "finds first index matching predicate"

  (!.rb
   [(xtd/arr-find [1 3 4] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-find [1 3 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [2 -1])

^{:refer xt.lang.common-data/arr-map :added "4.1"}
(fact "maps a function across an array"

  (!.rb (xtd/arr-map [1 2 3] (fn [x] (return (+ x 1)))))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-mapcat :added "4.1"}
(fact "maps an array function, concatenting results"

  (!.rb (xtd/arr-mapcat [1 2 3] (fn [x] (return [x x]))))
  => [1 1 2 2 3 3])

^{:refer xt.lang.common-data/arr-partition :added "4.1"}
(fact "partitions an array into arrays of length n"

  (!.rb (xtd/arr-partition [1 2 3 4 5] 2))
  => [[1 2] [3 4] [5]])

^{:refer xt.lang.common-data/arr-filter :added "4.1"}
(fact "applies a filter across an array"

  (!.rb (xtd/arr-filter [1 2 3 4] (fn [x] (return (== 0 (mod x 2))))))
  => [2 4])

^{:refer xt.lang.common-data/arr-keep :added "4.1" :lang-exceptions {:python {:skip true}}}
(fact "keeps items in an array if output is not nil"

  (!.rb
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/arr-keep
     [1 2 3 4]
     x5-fn))
  => [20 40])

^{:refer xt.lang.common-data/arr-keepf :added "4.1"}
(fact "keeps items in an array with transform if predicate holds"

  (!.rb
   (xtd/arr-keepf
    [1 2 3 4]
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => [10 30])

^{:refer xt.lang.common-data/arr-juxt :added "4.1"}
(fact "constructs a map given a array of pairs"

  (!.rb
   (xtd/arr-juxt
    [1 2]
    (fn [x] (return (xt/x:to-string x)))
    (fn [x] (return (* x x)))))
  => {"1" 1, "2" 4})

^{:refer xt.lang.common-data/arr-foldl :added "4.1"}
(fact "performs reduce on an array"

  (!.rb (xtd/arr-foldl [1 2 3] (fn [acc x] (return (+ acc x))) 0))
  => 6)

^{:refer xt.lang.common-data/arr-foldr :added "4.1"}
(fact "performs right reduce"

  (!.rb (xtd/arr-foldr [1 2 3] (fn [acc x] (return (+ (* acc 10) x))) 0))
  => 321)

^{:refer xt.lang.common-data/arr-pipel :added "4.1"}
(fact "thrushes an input through a function pipeline"

  (!.rb
   (xtd/arr-pipel
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 4)

^{:refer xt.lang.common-data/arr-piper :added "4.1"}
(fact "thrushes an input through a function pipeline from reverse"

  (!.rb
   (xtd/arr-piper
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 3)

^{:refer xt.lang.common-data/arr-group-by :added "4.1"}
(fact "groups elements by key and view functions"

  (!.rb
   (xtd/arr-group-by
    [1 2 3 4]
    (fn [x] (return (xt/x:to-string (mod x 2))))
    (fn [x] (return x))))
  => {"0" [2 4], "1" [1 3]})

^{:refer xt.lang.common-data/arr-repeat :added "4.1"}
(fact "repeat function or value n times"

  (!.rb (xtd/arr-repeat 1 3))
  => [1 1 1])

^{:refer xt.lang.common-data/arr-normalise :added "4.1"}
(fact "normalises array elements to 1"

  (!.rb (xtd/arr-normalise [2 3 5]))
  => [0.2 0.3 0.5])

^{:refer xt.lang.common-data/arr-sort :added "4.1"}
(fact "arr-sort using key function and comparator"

  (!.rb
   (var out [3 1 2])
   (xtd/arr-sort
    out
    (fn [x] (return x))
    (fn [a b] (return (< a b))))
   out)
  => [1 2 3])

^{:refer xt.lang.common-data/arr-sorted-merge :added "4.1"}
(fact "performs a merge on two sorted arrays"

  (!.rb
   (xtd/arr-sorted-merge
    [1 3 5]
    [2 4 6]
    (fn [a b] (return (< a b)))))
  => [1 2 3 4 5 6])

^{:refer xt.lang.common-data/obj-map :added "4.1"}
(fact "maps a function across the values of an object"

  (!.rb (xtd/obj-map {:a 1, :b 2} (fn [x] (return (+ x 1)))))
  => {"a" 2, "b" 3})

^{:refer xt.lang.common-data/obj-filter :added "4.1"}
(fact "applies a filter across the values of an object"

  (!.rb (xtd/obj-filter {:a 1, :b 2, :c 3} (fn [x] (return (== 1 (mod x 2))))))
  => {"a" 1, "c" 3})

^{:refer xt.lang.common-data/obj-keep :added "4.1"}
(fact "applies a transform across the values of an object, keeping non-nil values"

  (!.rb
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20}

  (!.rb
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20})

^{:refer xt.lang.common-data/obj-keepf :added "4.1"}
(fact "applies a transform and filter across the values of an object"

  (!.rb
   (xtd/obj-keepf
    {:a 1, :b 2, :c 3}
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => {"a" 10, "c" 30})

^{:refer xt.lang.common-data/clone-shallow :added "4.1"}
(fact "shallow clones an object or array"

  (!.rb [(xtd/clone-shallow nil)
          (xtd/clone-shallow 1)])
  => [nil 1])

^{:refer xt.lang.common-data/clone-nested-loop :added "4.1"}
(fact "clone nested objects loop"

  (!.rb
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested-loop src (xtd/lu-create)))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/clone-nested :added "4.1"}
(fact "cloning nested xects"

  (!.rb
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested src))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/memoize-key-step :added "4.1"}
(fact "computes and caches a memoized value"

  (!.rb
   (var state {"n" 0})
   (var cache {})
   (var f-raw
        (fn [key]
          (do
            (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
            (return (xt/x:cat key "-value")))))
   [(xtd/memoize-key-step
     f-raw
     "a"
     cache)
    (xt/x:get-key cache "a")
    (xt/x:get-key state "n")])
  => ["a-value" "a-value" 1])

^{:refer xt.lang.common-data/memoize-key :added "4.1"}
(fact "memoize for functions of single argument"

  (!.rb
    (var state {"n" 0})
    (var f-raw (fn [x]
                 (do
                   (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
                   (return (* x 10)))))
    (var f (xtd/memoize-key f-raw))
    [(f 2) (f 2) (f 3) (xt/x:get-key state "n")])
  => [20 20 30 2])

(comment

  (s/seedgen-benchadd 'xt.lang.common-data {:lang [:r] :write true})
  (s/seedgen-langadd 'xt.lang.common-data {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-data {:lang [:lua :python] :write true})
  
  )
