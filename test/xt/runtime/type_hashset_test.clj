(ns xt.runtime.type-hashset-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-hashset :as hs]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-hashset :as hs]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-hashset/hashset :added "4.1"}
(fact "creates a hashset and removes duplicates"
  ^:hidden

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
  ^:hidden

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
  ^:hidden

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
  ^:hidden

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
  ^:hidden

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
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-to-iter :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-new :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-empty :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-is-editable :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-to-persistent! :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-has? :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-push! :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-dissoc :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-dissoc! :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-hash :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-show :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-create :added "4.1"}
(fact "TODO")

^{:refer xt.runtime.type-hashset/hashset-empty-mutable :added "4.1"}
(fact "TODO")