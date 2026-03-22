(ns xt.runtime.interface-common-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.interface-common :as v]
             [xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-repl :as repl]
             [xt.runtime.type-keyword :as kw]
             [xt.runtime.type-symbol :as sym]
             [xt.runtime.type-list :as list]
             [xt.runtime.type-vector :as vec]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.interface-common :as v]
             [xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-repl :as repl]
             [xt.runtime.type-keyword :as kw]
             [xt.runtime.type-symbol :as sym]
             [xt.runtime.type-list :as list]
             [xt.runtime.type-vector :as vec]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.interface-common/impl-normalise :added "4.0"}
(fact "normalises the value"
  ^:hidden
  
  (!.js
   (== v/NIL (v/impl-normalise nil)))
  => true

  (!.lua
   (== v/NIL (v/impl-normalise nil)))
  => true)

^{:refer xt.runtime.interface-common/impl-denormalise :added "4.0"}
(fact "denormalises the value"
  ^:hidden
  
  (!.js
   (v/impl-denormalise v/NIL))
  => nil

  (!.lua
   (v/impl-denormalise v/NIL))
  => nil)

^{:refer xt.runtime.interface-common/is-managed? :added "4.0"}
(fact "checks if object is managed via the runtime"
  ^:hidden
  
  (!.js
   [(v/is-managed?  (kw/keyword nil "hello"))
    (v/is-managed?  (sym/symbol nil "hello"))])
  => [true true]

  (!.lua
   [(v/is-managed?  (kw/keyword nil "hello"))
    (v/is-managed?  (sym/symbol nil "hello"))])
  => [true true])

^{:refer xt.runtime.interface-common/is-syntax? :added "4.0"}
(fact "checks if object is of type syntax")

^{:refer xt.runtime.interface-common/hash :added "4.0"}
(fact "gets the hash of an object"
  ^:hidden
  
  (!.js
   [(v/hash "hello")
    (v/hash (list/list 1 2 3 4))
    (v/hash (vec/vector 1 2 3 4))])
  => [667819 1875393 1090685]
  
  (!.lua
   [(v/hash "hello")
    (v/hash (list/list 1 2 3 4))
    (v/hash (vec/vector 1 2 3 4))])
  => [667819 1875393 1090685])

^{:refer xt.runtime.interface-common/get-name :added "4.0"}
(fact "gets the name of a symbol, keyword or var")

^{:refer xt.runtime.interface-common/get-namespace :added "4.0"}
(fact "gets the namespace of a symbol, keyword or var")

^{:refer xt.runtime.interface-common/hash-with-cache :added "4.0"}
(fact "gets a memoized cache id")

^{:refer xt.runtime.interface-common/wrap-with-cache :added "4.0"}
(fact "wraps hash-fn call with caching")

^{:refer xt.runtime.interface-common/show :added "4.0"}
(fact "show interface"
  ^:hidden
  
  (!.js
   [(v/show "hello")
    (v/show [1 2 3 4])
    (v/show (list/list 1 2 3 4))
    (v/show (vec/vector 1 2 3 4))])
  => ["\"hello\"" "1,2,3,4" "(1, 2, 3, 4)" "[1, 2, 3, 4]"]

  (!.lua
   [(v/show "hello")
    (v/show [1 2 3 4])
    (v/show (list/list 1 2 3 4))
    (v/show (vec/vector 1 2 3 4))])
  => (contains-in ["\"hello\"" #"table" "(1, 2, 3, 4)" "[1, 2, 3, 4]"]))

^{:refer xt.runtime.interface-common/eq :added "4.0"}
(fact "equivalence check")

^{:refer xt.runtime.interface-common/count :added "4.0"}
(fact "gets the count for a "
  ^:hidden
  
  (!.js
   [(v/count "hello")
    (v/count [1 2 3 4])
    (v/count (list/list 1 2 3 4))
    (v/count (vec/vector 1 2 3 4))])
  => [5 4 4 4]

  (!.lua
   [(v/count "hello")
    (v/count [1 2 3 4])
    (v/count (list/list 1 2 3 4))
    (v/count (vec/vector 1 2 3 4))])
  => [5 4 4 4])

^{:refer xt.runtime.interface-common/is-persistent? :added "4.0"}
(fact "checks if collection is persistent"
  ^:hidden
  
  (!.js
   [(v/is-persistent? (list/list))
    (v/is-persistent? (vec/vector))
    (v/is-persistent? (vec/vector-empty-mutable))])
  => [true true false]

  (!.lua
   [(v/is-persistent? (list/list))
    (v/is-persistent? (vec/vector))
    (v/is-persistent? (vec/vector-empty-mutable))])
  => [true true false])

^{:refer xt.runtime.interface-common/is-mutable? :added "4.0"}
(fact  "checks if collection is mutable"
  ^:hidden
  
  (!.js
   [(v/is-mutable? (list/list))
    (v/is-mutable? (vec/vector))
    (v/is-mutable? (vec/vector-empty-mutable))])
  => [true false true]

  (!.lua
   [(v/is-mutable? (list/list))
    (v/is-mutable? (vec/vector))
    (v/is-mutable? (vec/vector-empty-mutable))])
  => [true false true])

^{:refer xt.runtime.interface-common/to-persistent :added "4.0"}
(fact "converts to persistent")

^{:refer xt.runtime.interface-common/to-mutable :added "4.0"}
(fact "converts to mutable")

^{:refer xt.runtime.interface-common/push :added "4.0"}
(fact "pushs elements "
  ^:hidden
  
  (!.js
   [(-> (list/list)
        (v/push 1)
        (v/push 2)
        (v/push 3)
        (v/show))
    (-> (vec/vector)
        (v/push 1)
        (v/push 2)
        (v/push 3)
        (v/show))])
  => ["(3, 2, 1)"
      "[1, 2, 3]"]

  (!.lua
   [(-> (list/list)
        (v/push 1)
        (v/push 2)
        (v/push 3)
        (v/show))
    (-> (vec/vector)
        (v/push 1)
        (v/push 2)
        (v/push 3)
        (v/show))])
  => ["(3, 2, 1)"
      "[1, 2, 3]"])

^{:refer xt.runtime.interface-common/pop :added "4.0"}
(fact "pops element from collection"
  ^:hidden
  
  (!.js
   [(-> (list/list 1 2 3 4)
        (v/pop)
        (v/pop)
        (v/show))
    (-> (vec/vector  1 2 3 4)
        (v/pop)
        (v/pop)
        (v/show))])
  => ["(3, 4)" "[1, 2]"]

  (!.lua
   [(-> (list/list 1 2 3 4)
        (v/pop)
        (v/pop)
        (v/show))
    (-> (vec/vector  1 2 3 4)
        (v/pop)
        (v/pop)
        (v/show))])
  => ["(3, 4)" "[1, 2]"])

^{:refer xt.runtime.interface-common/nth :added "4.0"}
(fact "nth coll")

^{:refer xt.runtime.interface-common/push-mutable :added "4.0"}
(fact "pushes an element into an editable collection")

^{:refer xt.runtime.interface-common/pop-mutable :added "4.0"}
(fact "pops an element from an editable collection")

^{:refer xt.runtime.interface-common/assoc :added "4.0"}
(fact "associates a key value pair into a persistent collection")

^{:refer xt.runtime.interface-common/dissoc :added "4.0"}
(fact "disassociates a key from aa persistent collection")

^{:refer xt.runtime.interface-common/assoc-mutable :added "4.0"}
(fact "associates a key value pair into a mutable collection")

^{:refer xt.runtime.interface-common/dissoc-mutable :added "4.0"}
(fact "disassociates a key pair from a mutable collection")

^{:refer xt.runtime.interface-common/to-iter :added "4.0"}
(fact "to iter")

^{:refer xt.runtime.interface-common/to-array :added "4.0"}
(fact "to array")

^{:refer xt.runtime.interface-common/find :added "4.0"}
(fact "find coll")

^{:refer xt.runtime.interface-common/empty :added "4.0"}
(fact "empty coll")
