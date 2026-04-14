(ns xt.runtime.type-hashmap-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-hashmap :as hm]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-hashmap :as hm]
             [xt.runtime.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-hashmap/hashmap :added "4.1"}
(fact "creates a hashmap from alternating key/value arguments"
  ^:hidden

  (!.js
   (var out (hm/hashmap "a" 1 "b" 2))
   [(. out _size)
    (hm/hashmap-lookup-key out "a" "missing")
    (hm/hashmap-lookup-key out "b" "missing")])
  => [2 1 2]

  (!.lua
   (var out (hm/hashmap "a" 1 "b" 2))
   [(. out _size)
    (hm/hashmap-lookup-key out "a" "missing")
    (hm/hashmap-lookup-key out "b" "missing")])
  => [2 1 2])

^{:refer xt.runtime.type-hashmap/hashmap-find-key :added "4.1"}
(fact "finds entries and preserves nil values"
  ^:hidden

  (!.js
   (var out (hm/hashmap "a" nil))
   (var entry (hm/hashmap-find-key out "a"))
   [(. entry _key)
    (. entry _val)
    (hm/hashmap-lookup-key out "missing" "fallback")])
  => ["a" nil "fallback"]

  (!.lua
   (var out (hm/hashmap "a" nil))
   (var entry (hm/hashmap-find-key out "a"))
   [(. entry _key)
    (. entry _val)
    (hm/hashmap-lookup-key out "missing" "fallback")])
  => ["a" nil "fallback"])

^{:refer xt.runtime.type-hashmap/hashmap-assoc :added "4.1"}
(fact "keeps persistent updates immutable"
  ^:hidden

  (!.js
   (var h0 (hm/hashmap "a" 1))
   (var h1 (hm/hashmap-assoc h0 "b" 2))
   [(hm/hashmap-lookup-key h0 "b" "missing")
    (hm/hashmap-lookup-key h1 "b" "missing")
    (. h1 _size)])
  => ["missing" 2 2]

  (!.lua
   (var h0 (hm/hashmap "a" 1))
   (var h1 (hm/hashmap-assoc h0 "b" 2))
   [(hm/hashmap-lookup-key h0 "b" "missing")
    (hm/hashmap-lookup-key h1 "b" "missing")
    (. h1 _size)])
  => ["missing" 2 2])

^{:refer xt.runtime.type-hashmap/hashmap-to-mutable! :added "4.1"}
(fact "supports mutable edits and roundtrips back to persistent"
  ^:hidden

  (!.js
   (var out (-> (hm/hashmap "a" 1)
                (hm/hashmap-to-mutable!)
                (hm/hashmap-assoc! "b" 2)
                (hm/hashmap-dissoc! "a")
                (hm/hashmap-to-persistent!)))
   [(ic/is-persistent? out)
    (hm/hashmap-lookup-key out "a" "missing")
    (hm/hashmap-lookup-key out "b" "missing")
    (. out _size)])
  => [true "missing" 2 1]

  (!.lua
   (var out (-> (hm/hashmap "a" 1)
                (hm/hashmap-to-mutable!)
                (hm/hashmap-assoc! "b" 2)
                (hm/hashmap-dissoc! "a")
                (hm/hashmap-to-persistent!)))
   [(ic/is-persistent? out)
    (hm/hashmap-lookup-key out "a" "missing")
    (hm/hashmap-lookup-key out "b" "missing")
    (. out _size)])
  => [true "missing" 2 1])

^{:refer xt.runtime.type-hashmap/hashmap-eq :added "4.1"}
(fact "compares and hashes maps independent of insertion order"
  ^:hidden

  (!.js
   (var h1 (hm/hashmap "a" 1 "b" 2))
   (var h2 (hm/hashmap "b" 2 "a" 1))
   [(hm/hashmap-eq h1 h2)
    (== (. h1 (hash))
        (. h2 (hash)))])
  => [true true]

  (!.lua
   (var h1 (hm/hashmap "a" 1 "b" 2))
   (var h2 (hm/hashmap "b" 2 "a" 1))
   [(hm/hashmap-eq h1 h2)
    (== (. h1 (hash))
        (. h2 (hash)))])
  => [true true])
