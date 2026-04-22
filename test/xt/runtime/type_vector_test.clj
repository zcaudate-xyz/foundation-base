(ns xt.runtime.type-vector-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-vector :as v]
             [xt.runtime.type-vector-node :as node]
             [xt.runtime.interface-common :as ic]
             [xt.runtime.interface-collection :as coll]
             [xt.lang.common-data :as k]
             [xt.lang.common-iter :as it]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-vector :as v]
             [xt.runtime.type-vector-node :as node]
             [xt.runtime.interface-common :as ic]
             [xt.runtime.interface-collection :as coll]
             [xt.lang.common-data :as k]
             [xt.lang.common-iter :as it]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-vector/vector-get-idx :added "4.0"}
(fact "gets the index in the persistent vector"

  (!.js
   (v/vector-get-idx
    (k/arr-foldl
     (k/arr-range [0 2048])
     v/vector-push-last
     v/EMPTY_VECTOR)
    2047))
  => 2047

  (!.lua
   (v/vector-get-idx
    (k/arr-foldl
     (k/arr-range [0 2048])
     v/vector-push-last
     v/EMPTY_VECTOR)
    2047))
  => 2047)

^{:refer xt.runtime.type-vector/vector-to-iter :added "4.0"}
(fact "converts vector to iterator"

  (!.js
   (it/arr<
    (v/vector-to-iter
     (v/vector 1 2 3 4))))
  => [1 2 3 4])

^{:refer xt.runtime.type-vector/vector-to-array :added "4.0"}
(fact "converts vector to array"

  (!.js
   (v/vector-to-array
    (v/vector 1 2 3 4)))
  => [1 2 3 4]

  (!.lua
   (v/vector-to-array
    (v/vector 1 2 3 4)))
  => [1 2 3 4])

^{:refer xt.runtime.type-vector/vector-new :added "4.0"}
(fact "creates a new vector"

  (!.js
   (var out (v/vector-new (node/node-create nil [])
                          2
                          node/BITS
                          [1 2]
                          nil))
   [(. out ["::"])
    (. out _size)
    (. out _shift)
    (v/vector-to-array out)])
  => ["vector" 2 5 [1 2]]

  (!.lua
   (var out (v/vector-new (node/node-create nil [])
                          2
                          node/BITS
                          [1 2]
                          nil))
   [(. out ["::"])
    (. out _size)
    (. out _shift)
    (v/vector-to-array out)])
  => ["vector" 2 5 [1 2]])

^{:refer xt.runtime.type-vector/vector-empty :added "4.0"}
(fact "creates an empty vector from current"

  (!.js
   (v/vector-to-array
    (v/vector-empty
     (v/vector [1 2 3 4]))))
  => [])

^{:refer xt.runtime.type-vector/vector-is-editable :added "4.0"}
(fact "checks that vector is editable"

  (!.js
   [(v/vector-is-editable (v/vector 1 2 3))
    (v/vector-is-editable (v/vector-empty-mutable))])
  => [false true]

  (!.lua
   [(v/vector-is-editable (v/vector 1 2 3))
    (v/vector-is-editable (v/vector-empty-mutable))])
  => [false true])

^{:refer xt.runtime.type-vector/vector-push-last :added "4.0"}
(fact "push-lastoins an element to the vector"

  (!.js
   (v/vector-to-array
    (v/vector-push-last v/EMPTY_VECTOR
                        1)))
  => [1]

  (!.lua
   (v/vector-to-array
    (v/vector-push-last v/EMPTY_VECTOR
                        1)))
  => [1])

^{:refer xt.runtime.type-vector/vector-pop-last :added "4.0"}
(fact "pops the last element off vector"

  (!.js
   (v/vector-to-array
    (k/arr-foldl
     (k/arr-range [0 5])
     v/vector-pop-last
     (k/arr-foldl
      (k/arr-range [0 10])
      v/vector-push-last
      v/EMPTY_VECTOR))))
  => [0 1 2 3 4]

  (!.lua
   (v/vector-to-array
    (k/arr-foldl
     (k/arr-range [0 5])
     v/vector-pop-last
     (k/arr-foldl
      (k/arr-range [0 10])
      v/vector-push-last
      v/EMPTY_VECTOR))))
  => [0 1 2 3 4])

^{:refer xt.runtime.type-vector/vector-pop-last! :added "4.0"}
(fact "pops the last element"

  (!.js
   (-> (v/vector 1 2 3 4 5 6)
       (v/vector-to-mutable!)
       (v/vector-pop-last!)
       (v/vector-pop-last!)
       (v/vector-pop-last!)
       (v/vector-to-array)))
  => [1 2 3]

  (!.lua
   (-> (v/vector 1 2 3 4 5 6)
       (v/vector-to-mutable!)
       (v/vector-pop-last!)
       (v/vector-pop-last!)
       (v/vector-pop-last!)
       (v/vector-to-array)))
  => [1 2 3])

^{:refer xt.runtime.type-vector/vector-push-last! :added "4.0"}
(fact "pushes the last element into vector"

  (!.js
   (var V0 (v/vector-empty-mutable))
   (k/arr-foldl
    (k/arr-range [0 10])
    v/vector-push-last!
    V0)
   V0)
  => (contains {"_tail" [0 1 2 3 4 5 6 7 8 9]})

  (!.lua
   (var V0 (v/vector-empty-mutable))
   (k/arr-foldl
    (k/arr-range [0 10])
    v/vector-push-last!
    V0)
   V0)
  => (contains {"_tail" [0 1 2 3 4 5 6 7 8 9]}))

^{:refer xt.runtime.type-vector/vector-to-mutable! :added "4.0"}
(fact "mutates the vector"

  (!.js
   (var out (v/vector-to-mutable! (v/vector 1 2 3)))
   [(v/vector-is-editable out)
    (v/vector-to-array out)])
  => [true [1 2 3]]

  (!.lua
   (var out (v/vector-to-mutable! (v/vector 1 2 3)))
   [(v/vector-is-editable out)
    (v/vector-to-array out)])
  => [true [1 2 3]])

^{:refer xt.runtime.type-vector/vector-to-persistent! :added "4.0"}
(fact "creates persistent vector"

  (!.js
   (var out (-> (v/vector 1 2 3)
                (v/vector-to-mutable!)
                (v/vector-push-last! 4)
                (v/vector-to-persistent!)))
   [(v/vector-is-editable out)
    (v/vector-to-array out)])
  => [false [1 2 3 4]]

  (!.lua
   (var out (-> (v/vector 1 2 3)
                (v/vector-to-mutable!)
                (v/vector-push-last! 4)
                (v/vector-to-persistent!)))
   [(v/vector-is-editable out)
    (v/vector-to-array out)])
  => [false [1 2 3 4]])

^{:refer xt.runtime.type-vector/vector-find-idx :added "4.0"}
(fact "finds the pair entry"

  (!.js
   (var entry (v/vector-find-idx (v/vector 1 2 3) 1))
   [(. entry _key)
    (. entry _val)
    (== nil (v/vector-find-idx (v/vector 1 2 3) 8))])
  => [1 2 true]

  (!.lua
   (var entry (v/vector-find-idx (v/vector 1 2 3) 1))
   [(. entry _key)
    (. entry _val)
    (== nil (v/vector-find-idx (v/vector 1 2 3) 8))])
  => [1 2 true])

^{:refer xt.runtime.type-vector/vector-lookup-idx :added "4.0"}
(fact "finds the value"

  (!.js
   [(v/vector-lookup-idx (v/vector 1 2 3) 1 "missing")
    (v/vector-lookup-idx (v/vector 1 2 3) 8 "missing")])
  => [2 "missing"]

  (!.lua
   [(v/vector-lookup-idx (v/vector 1 2 3) 1 "missing")
    (v/vector-lookup-idx (v/vector 1 2 3) 8 "missing")])
  => [2 "missing"])

^{:refer xt.runtime.type-vector/vector-create :added "4.0"}
(fact "creates a vector"

  (!.js
   (v/vector-create (node/node-create nil [])
                    0
                    node/BITS
                    []))
  => {"_tail" [],
      "::" "vector",
      "_size" 0,
      "_root" {"children" [], "::" "vector.node"},
      "_shift" 5}


  (!.lua
   (v/vector-create (node/node-create nil [])
                    0
                    node/BITS
                    []))
  => {"_tail" {},
      "::" "vector",
      "_size" 0,
      "_root" {"children" {}, "::" "vector.node"},
      "_shift" 5})

^{:refer xt.runtime.type-vector/vector-empty-mutable :added "4.0"}
(fact "creates an empty mutable vector"

  (!.js
   (v/vector-empty-mutable))
  => map?

  (!.lua
   (v/vector-empty-mutable))
  => map?)

^{:refer xt.runtime.type-vector/vector :added "4.0"}
(fact "creates a vector"

  (!.js
   (v/vector-to-array
    (v/vector 1 2 3 4)))
  => [1 2 3 4]

  (!.lua
   (v/vector-to-array
    (v/vector 1 2 3 4)))
  => [1 2 3 4])
