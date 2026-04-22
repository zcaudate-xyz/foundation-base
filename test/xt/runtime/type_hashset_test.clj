(ns xt.runtime.type-hashset-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-hashset :as hs]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-iter :as it]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-hashset :as hs]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-iter :as it]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-hashset/hashset :added "4.1"}
(fact "creates a hashset and removes duplicates"

  (!.js
   (var out (hs/hashset "a" "b" "a"))
   [(. out _size)
    (hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (hs/hashset-has? out "c")])
  => [2 true true false]

  (!.lua
   (var out (hs/hashset "a" "b" "a"))
   [(. out _size)
    (hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (hs/hashset-has? out "c")])
  => [2 true true false])

^{:refer xt.runtime.type-hashset/hashset-find :added "4.1"}
(fact "finds existing values and preserves nil members"

  (!.js
   (var out (-> hs/EMPTY_HASHSET
                (hs/hashset-push nil)))
   [(hs/hashset-has? out nil)
    (== nil (hs/hashset-find out "missing"))])
  => [true true]

  (!.lua
   (var out (-> hs/EMPTY_HASHSET
                (hs/hashset-push nil)))
   [(hs/hashset-has? out nil)
    (== nil (hs/hashset-find out "missing"))])
  => [true true])

^{:refer xt.runtime.type-hashset/hashset-push :added "4.1"}
(fact "keeps persistent updates immutable"

  (!.js
   (var s0 (hs/hashset "a"))
   (var s1 (hs/hashset-push s0 "b"))
   [(hs/hashset-has? s0 "b")
    (hs/hashset-has? s1 "b")
    (. s1 _size)])
  => [false true 2]

  (!.lua
   (var s0 (hs/hashset "a"))
   (var s1 (hs/hashset-push s0 "b"))
   [(hs/hashset-has? s0 "b")
    (hs/hashset-has? s1 "b")
    (. s1 _size)])
  => [false true 2])

^{:refer xt.runtime.type-hashset/hashset-to-mutable! :added "4.1"}
(fact "supports mutable edits and roundtrips back to persistent"

  (!.js
   (var out (-> (hs/hashset "a")
                (hs/hashset-to-mutable!)
                (hs/hashset-push! "b")
                (hs/hashset-dissoc! "a")
                (hs/hashset-to-persistent!)))
   [(ic/is-persistent? out)
    (hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (. out _size)])
  => [true false true 1]

  (!.lua
   (var out (-> (hs/hashset "a")
                (hs/hashset-to-mutable!)
                (hs/hashset-push! "b")
                (hs/hashset-dissoc! "a")
                (hs/hashset-to-persistent!)))
   [(ic/is-persistent? out)
    (hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (. out _size)])
  => [true false true 1])

^{:refer xt.runtime.type-hashset/hashset-eq :added "4.1"}
(fact "compares and hashes sets independent of insertion order"

  (!.js
   (var s1 (hs/hashset "a" "b"))
   (var s2 (hs/hashset "b" "a"))
   [(hs/hashset-eq s1 s2)
    (== (. s1 (hash))
        (. s2 (hash)))])
  => [true true]

  (!.lua
   (var s1 (hs/hashset "a" "b"))
   (var s2 (hs/hashset "b" "a"))
   [(hs/hashset-eq s1 s2)
    (== (. s1 (hash))
        (. s2 (hash)))])
  => [true true])


^{:refer xt.runtime.type-hashset/hashset-to-array :added "4.1"}
(fact "converts the hashset to an array"

  (!.js
   (hs/hashset-to-array
    (hs/hashset "a" "b")))
  => ["b" "a"]

  (!.lua
   (hs/hashset-to-array
    (hs/hashset "a" "b")))
  => ["b" "a"])

^{:refer xt.runtime.type-hashset/hashset-to-iter :added "4.1"}
(fact "iterates over the hashset values"

  (!.js
   (it/arr<
    (hs/hashset-to-iter
     (hs/hashset "a" "b"))))
  => ["b" "a"]

  (!.lua
   (it/arr<
    (hs/hashset-to-iter
     (hs/hashset "a" "b"))))
  => ["b" "a"])

^{:refer xt.runtime.type-hashset/hashset-new :added "4.1"}
(fact "creates a bare hashset from an underlying hashmap"

  (!.js
   (var out (hs/hashset-new hm/EMPTY_HASHMAP nil))
   [(. out ["::"])
    (. out _size)
    (. (. out _map) ["::"])])
  => ["hashset" 0 "hashmap"]

  (!.lua
   (var out (hs/hashset-new hm/EMPTY_HASHMAP nil))
   [(. out ["::"])
    (. out _size)
    (. (. out _map) ["::"])])
  => ["hashset" 0 "hashmap"])

^{:refer xt.runtime.type-hashset/hashset-empty :added "4.1"}
(fact "creates an empty hashset from the current hashset"

  (!.js
   (var out (hs/hashset-empty (hs/hashset "a")))
   [(. out _size)
    (hs/hashset-show out)])
  => [0 "#{}"]

  (!.lua
   (var out (hs/hashset-empty (hs/hashset "a")))
   [(. out _size)
    (hs/hashset-show out)])
  => [0 "#{}"])

^{:refer xt.runtime.type-hashset/hashset-is-editable :added "4.1"}
(fact "detects mutable vs persistent hashsets"

  (!.js
   [(hs/hashset-is-editable (hs/hashset "a"))
    (hs/hashset-is-editable (hs/hashset-empty-mutable))])
  => [false true]

  (!.lua
   [(hs/hashset-is-editable (hs/hashset "a"))
    (hs/hashset-is-editable (hs/hashset-empty-mutable))])
  => [false true])

^{:refer xt.runtime.type-hashset/hashset-to-persistent! :added "4.1"}
(fact "converts a mutable hashset back into a persistent hashset"

  (!.js
   (var out (-> (hs/hashset "a")
                (hs/hashset-to-mutable!)
                (hs/hashset-push! "b")
                (hs/hashset-to-persistent!)))
   [(hs/hashset-is-editable out)
    (hs/hashset-has? out "b")])
  => [false true]

  (!.lua
   (var out (-> (hs/hashset "a")
                (hs/hashset-to-mutable!)
                (hs/hashset-push! "b")
                (hs/hashset-to-persistent!)))
   [(hs/hashset-is-editable out)
    (hs/hashset-has? out "b")])
  => [false true])

^{:refer xt.runtime.type-hashset/hashset-has? :added "4.1"}
(fact "checks membership in the hashset"

  (!.js
   [(hs/hashset-has? (hs/hashset "a" "b") "a")
    (hs/hashset-has? (hs/hashset "a" "b") "c")])
  => [true false]

  (!.lua
   [(hs/hashset-has? (hs/hashset "a" "b") "a")
    (hs/hashset-has? (hs/hashset "a" "b") "c")])
  => [true false])

^{:refer xt.runtime.type-hashset/hashset-push! :added "4.1"}
(fact "mutably adds values without growing for duplicates"

  (!.js
   (var out (hs/hashset-empty-mutable))
   (hs/hashset-push! out "a")
   (hs/hashset-push! out "b")
   (hs/hashset-push! out "b")
   [(hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (. out _size)])
  => [true true 2]

  (!.lua
   (var out (hs/hashset-empty-mutable))
   (hs/hashset-push! out "a")
   (hs/hashset-push! out "b")
   (hs/hashset-push! out "b")
   [(hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (. out _size)])
  => [true true 2])

^{:refer xt.runtime.type-hashset/hashset-dissoc :added "4.1"}
(fact "keeps persistent removals immutable"

  (!.js
   (var s0 (hs/hashset "a" "b"))
   (var s1 (hs/hashset-dissoc s0 "a"))
   [(hs/hashset-has? s0 "a")
    (hs/hashset-has? s1 "a")
    (. s1 _size)])
  => [true false 1]

  (!.lua
   (var s0 (hs/hashset "a" "b"))
   (var s1 (hs/hashset-dissoc s0 "a"))
   [(hs/hashset-has? s0 "a")
    (hs/hashset-has? s1 "a")
    (. s1 _size)])
  => [true false 1])

^{:refer xt.runtime.type-hashset/hashset-dissoc! :added "4.1"}
(fact "mutably removes values and ignores missing values"

  (!.js
   (var out (-> (hs/hashset "a" "b")
                (hs/hashset-to-mutable!)))
   (hs/hashset-dissoc! out "a")
   (hs/hashset-dissoc! out "missing")
   [(hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (. out _size)])
  => [false true 1]

  (!.lua
   (var out (-> (hs/hashset "a" "b")
                (hs/hashset-to-mutable!)))
   (hs/hashset-dissoc! out "a")
   (hs/hashset-dissoc! out "missing")
   [(hs/hashset-has? out "a")
    (hs/hashset-has? out "b")
    (. out _size)])
  => [false true 1])

^{:refer xt.runtime.type-hashset/hashset-hash :added "4.1"}
(fact "computes the same unordered hash as the protocol hash function"

  (!.js
   (var s1 (hs/hashset "a" "b"))
   (var s2 (hs/hashset "b" "a"))
   [(== (hs/hashset-hash s1)
        (. s1 (hash)))
    (== (hs/hashset-hash s1)
        (hs/hashset-hash s2))])
  => [true true]

  (!.lua
   (var s1 (hs/hashset "a" "b"))
   (var s2 (hs/hashset "b" "a"))
   [(== (hs/hashset-hash s1)
        (. s1 (hash)))
    (== (hs/hashset-hash s1)
        (hs/hashset-hash s2))])
  => [true true])

^{:refer xt.runtime.type-hashset/hashset-show :added "4.1"}
(fact "shows the hashset as an EDN-like set string"

  (!.js
   (hs/hashset-show
    (hs/hashset "a" "b")))
  => #"\#\{(?:\"a\", \"b\"|\"b\", \"a\")\}"

  (!.lua
   (hs/hashset-show
    (hs/hashset "a" "b")))
  => #"\#\{(?:\"a\", \"b\"|\"b\", \"a\")\}")

^{:refer xt.runtime.type-hashset/hashset-create :added "4.1"}
(fact "creates a hashset with the default prototype"

  (!.js
   (var out (hs/hashset-create hm/EMPTY_HASHMAP))
   [(. out ["::"])
    (. out _size)
    (hs/hashset-show out)])
  => ["hashset" 0 "#{}"]

  (!.lua
   (var out (hs/hashset-create hm/EMPTY_HASHMAP))
   [(. out ["::"])
    (. out _size)
    (hs/hashset-show out)])
  => ["hashset" 0 "#{}"])

^{:refer xt.runtime.type-hashset/hashset-empty-mutable :added "4.1"}
(fact "creates an empty mutable hashset"

  (!.js
   (var out (hs/hashset-empty-mutable))
   [(hs/hashset-is-editable out)
    (. out _size)
    (hs/hashset-show out)])
  => [true 0 "#{}"]

  (!.lua
   (var out (hs/hashset-empty-mutable))
   [(hs/hashset-is-editable out)
    (. out _size)
    (hs/hashset-show out)])
  => [true 0 "#{}"])
