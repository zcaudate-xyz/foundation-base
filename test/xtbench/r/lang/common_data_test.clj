(ns xtbench.r.lang.common-data-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script- :r
  {:runtime :basic,
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-data/not-empty? :added "4.1"}
(fact "checks that array is not empty"

  (!.R
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

  (!.R
   (var lu (xtd/lu-create))
   (xtd/lu-get lu "missing"))
  => nil)

^{:refer xt.lang.common-data/lu-del :added "4.1"}
(fact "deletes a lookup entry"

  (!.R
    (var lu (xtd/lu-create))
    (xtd/lu-set lu "a" 1)
    (xtd/lu-del lu "a")
    (xtd/lu-get lu "a"))
  => nil)

^{:refer xt.lang.common-data/lu-get :added "4.1"}
(fact "gets a lookup entry"

  (!.R
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 1)
   (xtd/lu-get lu "a"))
  => 1

  (!.R
    (var lu (xtd/lu-create))
    (xtd/lu-set lu lu 2)
    (xtd/lu-get lu lu))
  => 2)

^{:refer xt.lang.common-data/lu-set :added "4.1"}
(fact "sets a lookup entry"

  (!.R
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 2)
   (xtd/lu-get lu "a"))
  => 2)

^{:refer xt.lang.common-data/lu-eq :added "4.1"}
(fact "checks lookup equality"

  (!.R
   (var lu0 (xtd/lu-create))
   (var lu1 (xtd/lu-create))
   [(xtd/lu-eq lu0 lu0)
    (xtd/lu-eq lu0 lu1)])
  => [true false])

^{:refer xt.lang.common-data/first :added "4.1"}
(fact "first"

  (!.R (xtd/first [1 2 3]))
  => 1)

^{:refer xt.lang.common-data/second :added "4.1"}
(fact "gets the second item"

  (!.R (xtd/second [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/nth :added "4.1"}
(fact "gets the nth item (index 0)"

  (!.R [(xtd/nth [1 2 3] 0)
          (xtd/nth [1 2 3] 2)])
  => [1 3])

^{:refer xt.lang.common-data/last :added "4.1"}
(fact "gets the last item"

  (!.R (xtd/last [1 2 3]))
  => 3)

^{:refer xt.lang.common-data/second-last :added "4.1"}
(fact "gets the second-last item"

  (!.R (xtd/second-last [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/arr-empty? :added "4.1"}
(fact "checks that arrect is empty"

  (!.R [(xtd/arr-empty? nil)
          (xtd/arr-empty? [])
          (xtd/arr-empty? [1])])
  => [true true false])

^{:refer xt.lang.common-data/arr-not-empty? :added "4.1"}
(fact "checks that arrect is not empty"

  (!.R [(xtd/arr-not-empty? nil)
          (xtd/arr-not-empty? [])
          (xtd/arr-not-empty? [1])])
  => [false false true])

^{:refer xt.lang.common-data/arrayify :added "4.1"}
(fact "makes something into an array"

  (!.R [(xtd/arrayify 1)
          (xtd/arrayify [1])])
  => [[1] [1]])

^{:refer xt.lang.common-data/arr-lookup :added "4.1"}
(fact "constructs a lookup given keys"

  (!.R (xtd/obj-keys (xtd/arr-lookup ["a" "b"])))
  => (just ["a" "b"] :in-any-order))

^{:refer xt.lang.common-data/arr-omit :added "4.1"}
(fact "emits index from new array"

  (!.R (xtd/arr-omit [1 2 3] 1))
  => [1 3])

^{:refer xt.lang.common-data/arr-reverse :added "4.1"}
(fact "reverses the array"

  (!.R (xtd/arr-reverse [1 2 3]))
  => [3 2 1])

^{:refer xt.lang.common-data/arr-zip :added "4.1"}
(fact "zips two arrays together into a map"

  (!.R (xtd/arr-zip ["a" "b"] [1 2]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/arr-clone :added "4.1"}
(fact "clones an array"

  (!.R
   (var src [1 2])
   (var out (xtd/arr-clone src))
   (xt/x:arr-push src 3)
   out)
  => [1 2])

^{:refer xt.lang.common-data/arr-assign :added "4.1"}
(fact "appends to the end of an array"

  (!.R (xtd/arr-assign [1 2] [3 4]))
  => [1 2 3 4])

^{:refer xt.lang.common-data/arr-concat :added "4.1"}
(fact "concatenates arrays without mutating the input"

  (!.R
   (var src [1 2])
   [(xtd/arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]])

^{:refer xt.lang.common-data/arr-slice :added "4.1"}
(fact "slices an array"

  (!.R (xtd/arr-slice [1 2 3 4] 1 3))
  => [2 3])

^{:refer xt.lang.common-data/arr-rslice :added "4.1"}
(fact "gets the reverse of a slice"

  (!.R (xtd/arr-rslice [1 2 3 4] 1 3))
  => [3 2])

^{:refer xt.lang.common-data/arr-tail :added "4.1" :lang-exceptions {:dart {:skip true}}}
(fact "gets the tail of the array"

  (!.R (xtd/arr-tail [1 2 3 4] 2))
  => [4 3])

^{:refer xt.lang.common-data/arr-range :added "4.1"}
(fact "creates a range array"

  (!.R [(xtd/arr-range 5)
          (xtd/arr-range [2 6])
          (xtd/arr-range [2 7 2])])
  => [[0 1 2 3 4] [2 3 4 5] [2 4 6]])

^{:refer xt.lang.common-data/arr-intersection :added "4.1"}
(fact "gets the intersection of two arrays"

  (!.R (xtd/arr-intersection [1 2 3] [2 3 4]))
  => [2 3])

^{:refer xt.lang.common-data/arr-difference :added "4.1"}
(fact "gets the difference of two arrays"

  (!.R (xtd/arr-difference [1 2] [2 3 4]))
  => [3 4])

^{:refer xt.lang.common-data/arr-union :added "4.1"}
(fact "gets the union of two arrays"

  (!.R (xtd/arr-union [1 2] [2 3]))
  => (contains [1 2 3] :in-any-order))

^{:refer xt.lang.common-data/arr-shuffle :added "4.1"}
(fact "shuffles the array"

  (!.R (xtd/arr-shuffle [1 2 3]))
  => (contains [1 2 3] :in-any-order))

^{:refer xt.lang.common-data/arr-pushl :added "4.1"}
(fact "pushs an element into array"

  (!.R (xtd/arr-pushl [1 2 3] 4 3))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-pushr :added "4.1"}
(fact "pushs an element into array"

  (!.R (xtd/arr-pushr [1 2 3] 0 3))
  => [0 1 2])

^{:refer xt.lang.common-data/arr-interpose :added "4.1"}
(fact "puts element between array"

  (!.R (xtd/arr-interpose [1 2 3] 0))
  => [1 0 2 0 3])

^{:refer xt.lang.common-data/arr-random :added "4.1"}
(fact "gets a random element from array"

  (!.R (xtd/arr-random [1]))
  => 1)

^{:refer xt.lang.common-data/arr-sample :added "4.1"}
(fact "samples array according to probability"

  (!.R (xtd/arr-sample ["a" "b" "c"] [0 1 0]))
  => "b")

^{:refer xt.lang.common-data/obj-empty? :added "4.1"}
(fact "checks that object is empty"

  (!.R [(xtd/obj-empty? {})
          (xtd/obj-empty? {:a 1})])
  => [true false])

^{:refer xt.lang.common-data/obj-not-empty? :added "4.1"}
(fact "checks that object is not empty"

  (!.R [(xtd/obj-not-empty? {})
          (xtd/obj-not-empty? {:a 1})])
  => [false true])

^{:refer xt.lang.common-data/obj-first-key :added "4.1"}
(fact "gets the first key"

  (!.R (xtd/obj-first-key {:a 1}))
  => "a")

^{:refer xt.lang.common-data/obj-first-val :added "4.1"}
(fact "gets the first val"

  (!.R (xtd/obj-first-val {:a 1}))
  => 1)

^{:refer xt.lang.common-data/obj-assign-nested :added "4.1"}
(fact "merges objects at a nesting level"

  (!.R (xtd/obj-assign-nested {:a {:b 1}} {:a {:c 2}}))
  => {"a" {"b" 1, "c" 2}})

^{:refer xt.lang.common-data/obj-assign-with :added "4.1"}
(fact "merges second into first given a function"

  (!.R
   (xtd/obj-assign-with
    {:a 1, :b 2}
    {:a 3, :c 4}
    (fn [x y] (return (+ x y)))))
  => {"a" 4, "b" 2, "c" 4})

^{:refer xt.lang.common-data/obj-del :added "4.1"}
(fact "deletes multiple keys"

  (!.R (xtd/obj-del {:a 1, :b 2, :c 3} ["a" "c"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-del-all :added "4.1" :lang-exceptions {:python {:skip true}}}
(fact "obj del all"

  (!.R
    (var out {:a 1, :b 2})
    (xtd/obj-del-all out)
    out)
  => {})

^{:refer xt.lang.common-data/obj-pick :added "4.1"}
(fact "select keys in object"

  (!.R (xtd/obj-pick {:a 1, :b 2} ["a"]))
  => {"a" 1})

^{:refer xt.lang.common-data/obj-omit :added "4.1"}
(fact "new object with missing keys"

  (!.R (xtd/obj-omit {:a 1, :b 2} ["a"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-transpose :added "4.1"}
(fact "obj-transposes a map"

  (!.R (xtd/obj-transpose {:a "x", :b "y"}))
  => {"x" "a", "y" "b"})

^{:refer xt.lang.common-data/obj-nest :added "4.1"}
(fact "creates a nested object"

  (!.R (xtd/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/obj-keys :added "4.1"}
(fact "gets keys of an object"

  (!.R (xtd/obj-keys {:a 1, :b 2}))
  => (just ["a" "b"] :in-any-order))

^{:refer xt.lang.common-data/obj-vals :added "4.1"}
(fact "gets vals of an object"

  (!.R (xtd/obj-vals {:a 1, :b 2}))
  => (just [1 2] :in-any-order))

^{:refer xt.lang.common-data/obj-pairs :added "4.1"}
(fact "creates entry pairs from object"

  (!.R (xtd/obj-pairs {:a 1, :b 2}))
  => (just [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.common-data/obj-clone :added "4.1"}
(fact "clones an object"

  (!.R
   (var src {:a 1})
   (var out (xtd/obj-clone src))
   (xt/x:set-key src "b" 2)
   out)
  => {"a" 1})

^{:refer xt.lang.common-data/obj-assign :added "4.1"}
(fact "merges key value pairs from into another"

  (!.R (xtd/obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/obj-from-pairs :added "4.1"}
(fact "creates an object from pairs"

  (!.R (xtd/obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/get-in :added "4.1"}
(fact "gets item in object"

  (!.R [(xtd/get-in {:a {:b {:c 1}}} ["a" "b"])
          (xtd/get-in {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1])

^{:refer xt.lang.common-data/set-in :added "4.1"}
(fact "sets item in object"

  [(!.R
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "b"] 2)
     a)
   (!.R
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "d"] 2)
     a)]
  => [{"a" {"b" 2}}
      {"a" {"b" {"c" 1}, "d" 2}}])

^{:refer xt.lang.common-data/obj-intersection :added "4.1"}
(fact "finds the intersection between map lookups"

  (!.R (xtd/obj-intersection {:a true, :b true} {:b true, :c true}))
  => ["b"])

^{:refer xt.lang.common-data/obj-keys-nested :added "4.1"}
(fact "gets nested keys"

  (!.R (xtd/obj-keys-nested {:a {:b 1, :c 2}} []))
  => (just [[["a" "b"] 1]
            [["a" "c"] 2]]
           :in-any-order))

^{:refer xt.lang.common-data/obj-difference :added "4.1"}
(fact "finds the difference between two map lookups"

  (!.R (xtd/obj-difference {:a true, :b true} {:b true, :c true}))
  => ["c"])

^{:refer xt.lang.common-data/swap-key :added "4.1"}
(fact "swaps a value in the key with a function"

  (!.R
   (var out {:a 1})
   (xtd/swap-key out "a" (fn [x y] (return (+ x y))) [2])
   out)
  => {"a" 3})

^{:refer xt.lang.common-data/to-flat :added "4.1"}
(fact "flattens pairs of object into array"

  (!.R (xtd/to-flat [["a" 1] ["b" 2]]))
  => ["a" 1 "b" 2])

^{:refer xt.lang.common-data/from-flat :added "4.1"}
(fact "creates object from flattened pair array"

  (!.R
   (xtd/from-flat
     ["a" 1 "b" 2]
     xtd/set-pair-step
     {}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/arr-every :added "4.1"}
(fact "checks that every element fulfills thet predicate"

  (!.R
   [(xtd/arr-every [2 4 6] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-every [2 3 6] (fn [x] (return (== 0 (mod x 2)))))])
  => [true false])

^{:refer xt.lang.common-data/arr-some :added "4.1"}
(fact "checks that the array contains an element"

  (!.R
   [(xtd/arr-some [1 3 5] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-some [1 2 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [false true])

^{:refer xt.lang.common-data/arr-each :added "4.1"}
(fact "performs a function call for each element"

  (!.R
   (var out [])
   (xtd/arr-each [1 2 3] (fn [x] (xt/x:arr-push out (+ x 1))))
   out)
  => [2 3 4])

^{:refer xt.lang.common-data/arr-find :added "4.1"}
(fact "finds first index matching predicate"

  (!.R
   [(xtd/arr-find [1 3 4] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-find [1 3 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [2 -1])

^{:refer xt.lang.common-data/arr-map :added "4.1"}
(fact "maps a function across an array"

  (!.R (xtd/arr-map [1 2 3] (fn [x] (return (+ x 1)))))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-mapcat :added "4.1"}
(fact "maps an array function, concatenting results"

  (!.R (xtd/arr-mapcat [1 2 3] (fn [x] (return [x x]))))
  => [1 1 2 2 3 3])

^{:refer xt.lang.common-data/arr-partition :added "4.1"}
(fact "partitions an array into arrays of length n"

  (!.R (xtd/arr-partition [1 2 3 4 5] 2))
  => [[1 2] [3 4] [5]])

^{:refer xt.lang.common-data/arr-filter :added "4.1"}
(fact "applies a filter across an array"

  (!.R (xtd/arr-filter [1 2 3 4] (fn [x] (return (== 0 (mod x 2))))))
  => [2 4])

^{:refer xt.lang.common-data/arr-keep :added "4.1" :lang-exceptions {:python {:skip true}}}
(fact "keeps items in an array if output is not nil"

  (!.R
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

  (!.R
   (xtd/arr-keepf
    [1 2 3 4]
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => [10 30])

^{:refer xt.lang.common-data/arr-juxt :added "4.1"}
(fact "constructs a map given a array of pairs"

  (!.R
   (xtd/arr-juxt
    [1 2]
    (fn [x] (return (xt/x:to-string x)))
    (fn [x] (return (* x x)))))
  => {"1" 1, "2" 4})

^{:refer xt.lang.common-data/arr-foldl :added "4.1"}
(fact "performs reduce on an array"

  (!.R (xtd/arr-foldl [1 2 3] (fn [acc x] (return (+ acc x))) 0))
  => 6)

^{:refer xt.lang.common-data/arr-foldr :added "4.1"}
(fact "performs right reduce"

  (!.R (xtd/arr-foldr [1 2 3] (fn [acc x] (return (+ (* acc 10) x))) 0))
  => 321)

^{:refer xt.lang.common-data/arr-pipel :added "4.1"}
(fact "thrushes an input through a function pipeline"

  (!.R
   (xtd/arr-pipel
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 4)

^{:refer xt.lang.common-data/arr-piper :added "4.1"}
(fact "thrushes an input through a function pipeline from reverse"

  (!.R
   (xtd/arr-piper
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 3)

^{:refer xt.lang.common-data/arr-group-by :added "4.1"}
(fact "groups elements by key and view functions"

  (!.R
   (xtd/arr-group-by
    [1 2 3 4]
    (fn [x] (return (xt/x:to-string (mod x 2))))
    (fn [x] (return x))))
  => {"0" [2 4], "1" [1 3]})

^{:refer xt.lang.common-data/arr-repeat :added "4.1"}
(fact "repeat function or value n times"

  (!.R (xtd/arr-repeat 1 3))
  => [1 1 1])

^{:refer xt.lang.common-data/arr-normalise :added "4.1"}
(fact "normalises array elements to 1"

  (!.R (xtd/arr-normalise [2 3 5]))
  => [0.2 0.3 0.5])

^{:refer xt.lang.common-data/arr-sort :added "4.1"}
(fact "arr-sort using key function and comparator"

  (!.R
   (xtd/arr-sort
    [3 1 2]
    (fn [x] (return x))
    (fn [a b] (return (< a b)))))
  => [1 2 3])

^{:refer xt.lang.common-data/arr-sorted-merge :added "4.1"}
(fact "performs a merge on two sorted arrays"

  (!.R
   (xtd/arr-sorted-merge
    [1 3 5]
    [2 4 6]
    (fn [a b] (return (< a b)))))
  => [1 2 3 4 5 6])

^{:refer xt.lang.common-data/obj-map :added "4.1"}
(fact "maps a function across the values of an object"

  (!.R (xtd/obj-map {:a 1, :b 2} (fn [x] (return (+ x 1)))))
  => {"a" 2, "b" 3})

^{:refer xt.lang.common-data/obj-filter :added "4.1"}
(fact "applies a filter across the values of an object"

  (!.R (xtd/obj-filter {:a 1, :b 2, :c 3} (fn [x] (return (== 1 (mod x 2))))))
  => {"a" 1, "c" 3})

^{:refer xt.lang.common-data/obj-keep :added "4.1"}
(fact "applies a transform across the values of an object, keeping non-nil values"

  (!.R
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20}

  (!.R
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

  (!.R
   (xtd/obj-keepf
    {:a 1, :b 2, :c 3}
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => {"a" 10, "c" 30})

^{:refer xt.lang.common-data/clone-shallow :added "4.1"}
(fact "shallow clones an object or array"

  (!.R [(xtd/clone-shallow nil)
          (xtd/clone-shallow 1)])
  => [nil 1])

^{:refer xt.lang.common-data/clone-nested-loop :added "4.1"}
(fact "clone nested objects loop"

  (!.R
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested-loop src (xtd/lu-create)))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/clone-nested :added "4.1"}
(fact "cloning nested xects"

  (!.R
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested src))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/memoize-key :added "4.1"}
(fact "memoize for functions of single argument"

  (!.R
   (var state {"n" 0})
   (var f-raw (fn [x]
                (do
                  (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
                  (return (* x 10)))))
   (var f (xtd/memoize-key f-raw))
   [(f 2) (f 2) (f 3) (xt/x:get-key state "n")])
  => [20 20 30 2])

^{:refer xt.lang.common-data/is-empty? :added "4.1"}
(fact "checks that array is empty"

  (!.R
   [(xtd/is-empty? nil)
    (xtd/is-empty? "")
    (xtd/is-empty? "123")
    (xtd/is-empty? [])
    (xtd/is-empty? [1 2 3])
    (xtd/is-empty? {})
    (xtd/is-empty? {:a 1, :b 2})])
  => [true true false true false true false])

^{:refer xt.lang.common-data/set-pair-step :added "4.1"}
(fact "sets a pair into an object and returns it"

  (!.R
   (var out {})
   [(xt/x:get-key (xtd/set-pair-step out "a" 1) "a")
    (xt/x:get-key (xtd/set-pair-step out "b" 2) "b")
    out])
  => [1 2 {"a" 1, "b" 2}])

^{:refer xt.lang.common-data/memoize-key-step :added "4.1"}
(fact "computes and caches a memoized value"

  (!.R
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

(comment
  
  (s/seedgen-langadd 'xt.lang.common-data {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-data {:lang [:lua :python] :write true})
  
  )
