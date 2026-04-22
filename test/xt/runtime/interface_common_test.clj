(ns xt.runtime.interface-common-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.interface-common :as v]
              [xt.lang.common-lib :as k]
              [xt.lang.common-iter :as it]
              [xt.lang.common-repl :as repl]
              [xt.runtime.type-hashmap :as hm]
              [xt.runtime.type-keyword :as kw]
              [xt.runtime.type-symbol :as sym]
              [xt.runtime.type-list :as list]
              [xt.runtime.type-syntax :as syn]
              [xt.runtime.type-vector :as vec]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.interface-common :as v]
              [xt.lang.common-lib :as k]
              [xt.lang.common-iter :as it]
              [xt.lang.common-repl :as repl]
              [xt.runtime.type-hashmap :as hm]
              [xt.runtime.type-keyword :as kw]
              [xt.runtime.type-symbol :as sym]
              [xt.runtime.type-list :as list]
              [xt.runtime.type-syntax :as syn]
              [xt.runtime.type-vector :as vec]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.interface-common/impl-normalise :added "4.0"}
(fact "normalises the value"

  (!.js
   (== v/NIL (v/impl-normalise nil)))
  => true

  (!.lua
   (== v/NIL (v/impl-normalise nil)))
  => true)

^{:refer xt.runtime.interface-common/impl-denormalise :added "4.0"}
(fact "denormalises the value"

  (!.js
   (v/impl-denormalise v/NIL))
  => nil

  (!.lua
   (v/impl-denormalise v/NIL))
  => nil)

^{:refer xt.runtime.interface-common/is-managed? :added "4.0"}
(fact "checks if object is managed via the runtime"

  (!.js
   [(v/is-managed?  (kw/keyword nil "hello"))
    (v/is-managed?  (sym/symbol nil "hello"))])
  => [true true]

  (!.lua
   [(v/is-managed?  (kw/keyword nil "hello"))
    (v/is-managed?  (sym/symbol nil "hello"))])
  => [true true])

^{:refer xt.runtime.interface-common/is-syntax? :added "4.0"}
(fact "checks if object is of type syntax"

  (!.js
   [(v/is-syntax? nil)
    (v/is-syntax? (syn/syntax-create [1 2 3] "meta"))])
  => [false true]

  (!.lua
   [(v/is-syntax? nil)
    (v/is-syntax? (syn/syntax-create [1 2 3] "meta"))])
  => [false true])

^{:refer xt.runtime.interface-common/hash :added "4.0"}
(fact "gets the hash of an object"

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
(fact "gets the name of a symbol, keyword or var"

  (!.js
   [(v/get-name (kw/keyword nil "hello"))
    (v/get-name (kw/keyword "world" "hello"))
    (v/get-name (sym/symbol "world" "hello"))])
  => ["hello" "hello" "hello"]

  (!.lua
   [(v/get-name (kw/keyword nil "hello"))
    (v/get-name (kw/keyword "world" "hello"))
    (v/get-name (sym/symbol "world" "hello"))])
  => ["hello" "hello" "hello"])

^{:refer xt.runtime.interface-common/get-namespace :added "4.0"}
(fact "gets the namespace of a symbol, keyword or var"

  (!.js
   [(v/get-namespace (kw/keyword nil "hello"))
    (v/get-namespace (kw/keyword "world" "hello"))
    (v/get-namespace (sym/symbol "world" "hello"))])
  => [nil "world" "world"]

  (!.lua
   [(v/get-namespace (kw/keyword nil "hello"))
    (v/get-namespace (kw/keyword "world" "hello"))
    (v/get-namespace (sym/symbol "world" "hello"))])
  => [nil "world" "world"])

^{:refer xt.runtime.interface-common/hash-with-cache :added "4.0"}
(fact "gets a memoized cache id"

  (!.js
   (var calls 0)
   (var obj {})
   [(v/hash-with-cache obj
                       (fn:> [x]
                         (:= calls (+ calls 1))
                         42))
    (v/hash-with-cache obj
                       (fn:> [x]
                         (:= calls (+ calls 1))
                         99))
    calls
    (. obj _hash)])
  => [42 42 1 42]

  (!.lua
   (var calls 0)
   (var obj {})
   [(v/hash-with-cache obj
                       (fn:> [x]
                         (:= calls (+ calls 1))
                         42))
    (v/hash-with-cache obj
                       (fn:> [x]
                         (:= calls (+ calls 1))
                         99))
    calls
    (. obj _hash)])
  => [42 42 1 42])

^{:refer xt.runtime.interface-common/wrap-with-cache :added "4.0"}
(fact "wraps hash-fn call with caching"

  (!.js
   (var cached-calls 0)
   (var cached (v/wrap-with-cache
                (fn:> [obj]
                  (:= cached-calls (+ cached-calls 1))
                  41)
                nil))
   (var editable-calls 0)
   (var editable (v/wrap-with-cache
                  (fn:> [obj]
                    (:= editable-calls (+ editable-calls 1))
                    (+ 50 editable-calls))
                  (fn:> [obj] true)))
   (var obj0 {})
   (var obj1 {})
   [(cached obj0)
    (cached obj0)
    cached-calls
    (. obj0 _hash)
    (editable obj1)
    (editable obj1)
    editable-calls
    (== nil (. obj1 _hash))])
  => [41 41 1 41 51 52 2 true]

  (!.lua
   (var cached-calls 0)
   (var cached (v/wrap-with-cache
                (fn:> [obj]
                  (:= cached-calls (+ cached-calls 1))
                  41)
                nil))
   (var editable-calls 0)
   (var editable (v/wrap-with-cache
                  (fn:> [obj]
                    (:= editable-calls (+ editable-calls 1))
                    (+ 50 editable-calls))
                  (fn:> [obj] true)))
   (var obj0 {})
   (var obj1 {})
   [(cached obj0)
    (cached obj0)
    cached-calls
    (. obj0 _hash)
    (editable obj1)
    (editable obj1)
    editable-calls
    (== nil (. obj1 _hash))])
  => [41 41 1 41 51 52 2 true])

^{:refer xt.runtime.interface-common/show :added "4.0"}
(fact "show interface"

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
(fact "equivalence check"

  (!.js
   [(v/eq (syn/syntax-create 1 nil) 1)
    (v/eq 1 (syn/syntax-create 1 nil))
    (v/eq (vec/vector 1 2 3) (vec/vector 1 2 3))
    (v/eq (vec/vector 1 2 3) (vec/vector 1 2 4))])
  => [true true true false]

  (!.lua
   [(v/eq (syn/syntax-create 1 nil) 1)
    (v/eq 1 (syn/syntax-create 1 nil))
    (v/eq (vec/vector 1 2 3) (vec/vector 1 2 3))
    (v/eq (vec/vector 1 2 3) (vec/vector 1 2 4))])
  => [true true true false])

^{:refer xt.runtime.interface-common/count :added "4.0"}
(fact "gets the count for a "

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
(fact "converts to persistent"

  (!.js
   (var out (-> (vec/vector 1 2 3)
                (v/to-mutable)
                (v/push-mutable 4)
                (v/to-persistent)))
   [(v/is-persistent? out)
    (v/show out)])
  => [true "[1, 2, 3, 4]"]

  (!.lua
   (var out (-> (vec/vector 1 2 3)
                (v/to-mutable)
                (v/push-mutable 4)
                (v/to-persistent)))
   [(v/is-persistent? out)
    (v/show out)])
  => [true "[1, 2, 3, 4]"])

^{:refer xt.runtime.interface-common/to-mutable :added "4.0"}
(fact "converts to mutable"

  (!.js
   (var out (v/to-mutable (vec/vector 1 2 3)))
   [(v/is-mutable? out)
    (v/show out)])
  => [true "[1, 2, 3]"]

  (!.lua
   (var out (v/to-mutable (vec/vector 1 2 3)))
   [(v/is-mutable? out)
    (v/show out)])
  => [true "[1, 2, 3]"])

^{:refer xt.runtime.interface-common/push :added "4.0"}
(fact "pushs elements "

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
(fact "nth coll"

  (!.js
   [(v/nth (vec/vector 1 2 3) 0)
    (v/nth (vec/vector 1 2 3) 2)])
  => [1 3]

  (!.lua
   [(v/nth (vec/vector 1 2 3) 0)
    (v/nth (vec/vector 1 2 3) 2)])
  => [1 3])

^{:refer xt.runtime.interface-common/push-mutable :added "4.0"}
(fact "pushes an element into an editable collection"

  (!.js
   (var out (vec/vector-empty-mutable))
   (v/push-mutable out 1)
   (v/push-mutable out 2)
   (v/show out))
  => "[1, 2]"

  (!.lua
   (var out (vec/vector-empty-mutable))
   (v/push-mutable out 1)
   (v/push-mutable out 2)
   (v/show out))
  => "[1, 2]")

^{:refer xt.runtime.interface-common/pop-mutable :added "4.0"}
(fact "pops an element from an editable collection"

  (!.js
   (var out (v/to-mutable (vec/vector 1 2 3 4)))
   (v/pop-mutable out)
   (v/pop-mutable out)
   (v/show out))
  => "[1, 2]"

  (!.lua
   (var out (v/to-mutable (vec/vector 1 2 3 4)))
   (v/pop-mutable out)
   (v/pop-mutable out)
   (v/show out))
  => "[1, 2]")

^{:refer xt.runtime.interface-common/assoc :added "4.0"}
(fact "associates a key value pair into a persistent collection"

  (!.js
   (var h0 (hm/hashmap "a" 1))
   (var h1 (v/assoc h0 "b" 2))
   [(hm/hashmap-lookup-key h0 "b" "missing")
    (hm/hashmap-lookup-key h1 "b" "missing")
    (. h1 _size)])
  => ["missing" 2 2]

  (!.lua
   (var h0 (hm/hashmap "a" 1))
   (var h1 (v/assoc h0 "b" 2))
   [(hm/hashmap-lookup-key h0 "b" "missing")
    (hm/hashmap-lookup-key h1 "b" "missing")
    (. h1 _size)])
  => ["missing" 2 2])

^{:refer xt.runtime.interface-common/dissoc :added "4.0"}
(fact "disassociates a key from aa persistent collection"

  (!.js
   (var out (v/dissoc (hm/hashmap "a" 1 "b" 2) "a"))
   [(hm/hashmap-lookup-key out "a" "missing")
    (. out _size)])
  => ["missing" 1]

  (!.lua
   (var out (v/dissoc (hm/hashmap "a" 1 "b" 2) "a"))
   [(hm/hashmap-lookup-key out "a" "missing")
    (. out _size)])
  => ["missing" 1])

^{:refer xt.runtime.interface-common/assoc-mutable :added "4.0"}
(fact "associates a key value pair into a mutable collection"

  (!.js
   (var out (v/to-mutable (hm/hashmap "a" 1)))
   (v/assoc-mutable out "b" 2)
   [(hm/hashmap-lookup-key out "b" "missing")
    (. out _size)])
  => [2 2]

  (!.lua
   (var out (v/to-mutable (hm/hashmap "a" 1)))
   (v/assoc-mutable out "b" 2)
   [(hm/hashmap-lookup-key out "b" "missing")
    (. out _size)])
  => [2 2])

^{:refer xt.runtime.interface-common/dissoc-mutable :added "4.0"}
(fact "disassociates a key pair from a mutable collection"

  (!.js
   (var out (v/to-mutable (hm/hashmap "a" 1 "b" 2)))
   (v/dissoc-mutable out "a")
   [(hm/hashmap-lookup-key out "a" "missing")
    (. out _size)])
  => ["missing" 1]

  (!.lua
   (var out (v/to-mutable (hm/hashmap "a" 1 "b" 2)))
   (v/dissoc-mutable out "a")
   [(hm/hashmap-lookup-key out "a" "missing")
    (. out _size)])
  => ["missing" 1])

^{:refer xt.runtime.interface-common/to-iter :added "4.0"}
(fact "to iter"

  (!.js
   (it/arr<
    (v/to-iter (vec/vector 1 2 3))))
  => [1 2 3]

  (!.lua
   (it/arr<
    (v/to-iter (vec/vector 1 2 3))))
  => [1 2 3])

^{:refer xt.runtime.interface-common/to-array :added "4.0"}
(fact "to array"

  (!.js
   (v/to-array (vec/vector 1 2 3)))
  => [1 2 3]

  (!.lua
   (v/to-array (vec/vector 1 2 3)))
  => [1 2 3])

^{:refer xt.runtime.interface-common/find :added "4.0"}
(fact "find coll"

  (!.js
   (var entry (v/find (vec/vector 1 2 3) 1))
   [(. entry _key)
    (. entry _val)
    (== nil (v/find (vec/vector 1 2 3) 9))])
  => [1 2 true]

  (!.lua
   (var entry (v/find (vec/vector 1 2 3) 1))
   [(. entry _key)
    (. entry _val)
    (== nil (v/find (vec/vector 1 2 3) 9))])
  => [1 2 true])

^{:refer xt.runtime.interface-common/empty :added "4.0"}
(fact "empty coll"

  (!.js
   [(v/show (v/empty (vec/vector 1 2 3)))
    (v/show (v/empty (hm/hashmap "a" 1)))])
  => ["[]" "{}"]

  (!.lua
   [(v/show (v/empty (vec/vector 1 2 3)))
    (v/show (v/empty (hm/hashmap "a" 1)))])
  => ["[]" "{}"])
