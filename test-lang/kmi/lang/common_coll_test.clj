(ns kmi.lang.common-coll-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.common-coll :as coll]
             [kmi.lang.type-vector :as v]
             [kmi.lang.type-list :as lst]
             [kmi.lang.type-pair :as pair]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-hashset :as hs]
             [xt.lang.common-iter :as it]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.common-coll/start-string :added "4.1"}
(fact "returns the start string of a collection"

  (!.js
   [(coll/start-string (v/vector [ 1 2]))
    (coll/start-string (lst/list [ 1 2]))
    (coll/start-string (pair/pair 1 2))
    (coll/start-string (v/vector))])
  => ["[" "(" "[" "["])

^{:refer kmi.lang.common-coll/end-string :added "4.1"}
(fact "returns the end string of a collection"

  (!.js
   [(coll/end-string (v/vector [ 1 2]))
    (coll/end-string (lst/list [ 1 2]))
    (coll/end-string (pair/pair 1 2))
    (coll/end-string (v/vector))])
  => ["]" ")" "]" "]"])

^{:refer kmi.lang.common-coll/sep-string :added "4.1"}
(fact "returns the separator string of a collection"

  (!.js
   [(coll/sep-string (v/vector [ 1 2]))
    (coll/sep-string (lst/list [ 1 2]))
    (coll/sep-string (pair/pair 1 2))])
  => [", " ", " ", "])

^{:refer kmi.lang.common-coll/is-ordered? :added "4.1"}
(fact "returns whether the collection is ordered"

  (!.js
   [(coll/is-ordered? (v/vector [ 1 2]))
    (coll/is-ordered? (lst/list [ 1 2]))
    (coll/is-ordered? (pair/pair 1 2))])
  => [true false true])

^{:refer kmi.lang.common-coll/coll-size :added "4.1"}
(fact "returns the size of a collection"

  (!.js
   [(coll/coll-size (v/vector [ 1 2 3]))
    (coll/coll-size (hs/hashset [ 1 2 3 2]))
    (coll/coll-size (hm/hashmap [ 1 2 3 4]))
    (coll/coll-size (v/vector))])
  => [3 3 2 0])

^{:refer kmi.lang.common-coll/coll-reduce :added "4.1"}
(fact "reduces a collection with a function and initial value"

  (!.js
   [(coll/coll-reduce (v/vector [ 1 2 3 4]) (fn:> [acc x] (+ acc x)) 0)
    (coll/coll-reduce (lst/list [ 1 2 3]) (fn:> [acc x] (* acc x)) 1)
    (coll/coll-reduce (v/vector) (fn:> [acc x] (+ acc x)) 100)])
  => [10 6 100])

^{:refer kmi.lang.common-coll/coll-hash-ordered :added "4.1"}
(fact "hashes an ordered collection"

  (!.js
   (coll/coll-hash-ordered (v/vector [ 1 2 3])))
  => integer?

  (!.js
   [(== (coll/coll-hash-ordered (v/vector [ 1 2 3]))
        (coll/coll-hash-ordered (v/vector [ 1 2 3])))
    (not= (coll/coll-hash-ordered (v/vector [ 1 2 3]))
          (coll/coll-hash-ordered (v/vector [ 3 2 1])))])
  => [true true])

^{:refer kmi.lang.common-coll/coll-hash-unordered :added "4.1"}
(fact "hashes an unordered collection independent of element order"

  (!.js
   (coll/coll-hash-unordered (hs/hashset [ 1 2 3])))
  => integer?

  (!.js
   [(== (coll/coll-hash-unordered (hs/hashset [ 1 2 3 4]))
        (coll/coll-hash-unordered (hs/hashset [ 4 3 2 1])))
    (not= (coll/coll-hash-unordered (hs/hashset [ 1 2 3]))
          (coll/coll-hash-unordered (hs/hashset [ 1 2])))])
  => [true true])

^{:refer kmi.lang.common-coll/coll-show-with :added "4.1"}
(fact "shows a collection with custom delimiters"

  (!.js
   [(coll/coll-show-with (v/vector [ 1 2]) "<" ">" "; ")
    (coll/coll-show-with (lst/list [ 1 2]) "[" "]" "|")
    (coll/coll-show-with (pair/pair 1 2) "(" ")" " ")
    (coll/coll-show-with (v/vector) "{" "}" "|")])
  => ["<1; 2>" "[1|2]" "(1 2)" "{}"])

^{:refer kmi.lang.common-coll/coll-show :added "4.1"}
(fact "shows a collection with its default delimiters"

  (!.js
   [(coll/coll-show (v/vector [ 1 2]))
    (coll/coll-show (lst/list [ 1 2]))
    (coll/coll-show (pair/pair 1 2))
    (coll/coll-show (v/vector))])
  => ["[1, 2]" "(1, 2)" "[1, 2]" "[]"])

^{:refer kmi.lang.common-coll/coll-into-iter :added "4.1"}
(fact "collects an iterator into a collection"

  (!.js
   [(v/vector-to-array
     (coll/coll-into-iter (v/vector-empty-mutable) (it/iter [1 2 3])))
    (v/vector-to-array
     (coll/coll-into-iter v/EMPTY_VECTOR (it/iter [4 5])))
    (v/vector-to-array
     (coll/coll-into-iter (v/vector-empty-mutable) (it/iter [])))
    (lst/list-to-array
     (coll/coll-into-iter lst/EMPTY_LIST (it/iter ["a" "b"])))])
  => [[1 2 3] [4 5] [] ["b" "a"]])

^{:refer kmi.lang.common-coll/coll-into-array :added "4.1"}
(fact "collects an array into a collection"

  (!.js
   [(v/vector-to-array
     (coll/coll-into-array (v/vector-empty-mutable) [1 2 3]))
    (v/vector-to-array
     (coll/coll-into-array v/EMPTY_VECTOR [4 5]))
    (v/vector-to-array
     (coll/coll-into-array (v/vector-empty-mutable) []))
    (lst/list-to-array
     (coll/coll-into-array lst/EMPTY_LIST ["a" "b"]))])
  => [[1 2 3] [4 5] [] ["b" "a"]])

^{:refer kmi.lang.common-coll/coll-eq :added "4.1"}
(fact "checks equality of two collections by iterating elements"

  (!.js
   [(coll/coll-eq (v/vector) (v/vector))
    (coll/coll-eq (lst/list) (lst/list))
    (coll/coll-eq (v/vector [(hm/hashmap [1 2])]) (v/vector [(hm/hashmap [1 2])]))
    (coll/coll-eq (v/vector [(hm/hashmap [1 2])]) (v/vector [(hm/hashmap [1 3])]))
    (coll/coll-eq (pair/pair (hm/hashmap [ 1 2]) (hm/hashmap [ 3 4]))
                  (pair/pair (hm/hashmap [ 1 2]) (hm/hashmap [ 3 4])))
    (coll/coll-eq (pair/pair (hm/hashmap [ 1 2]) (hm/hashmap [ 3 4]))
                  (pair/pair (hm/hashmap [ 1 2]) (hm/hashmap [ 3 5])))
    (coll/coll-eq (v/vector [(hm/hashmap [1 2]) (hm/hashmap [3 4])])
                  (v/vector [(hm/hashmap [1 2]) (hm/hashmap [3 4])]))])
  => [true true true false true false true])
