(ns
 xtbench.js.runtime.type-hashmap-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.runtime.type-hashmap :as hm]
   [xt.runtime.type-hashmap-node :as node]
   [xt.runtime.interface-common :as ic]
   [xt.lang.common-iter :as it]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-hashmap/hashmap, :added "4.1"}
(fact
 "creates a hashmap from alternating key/value arguments"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap "a" 1 "b" 2))
  [(. out _size)
   (hm/hashmap-lookup-key out "a" "missing")
   (hm/hashmap-lookup-key out "b" "missing")])
 =>
 [2 1 2])

^{:refer xt.runtime.type-hashmap/hashmap-find-key, :added "4.1"}
(fact
 "finds entries and preserves nil values"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap-assoc hm/EMPTY_HASHMAP "a" nil))
  (var entry (hm/hashmap-find-key out "a"))
  [(. entry _key)
   (. entry _val)
   (hm/hashmap-lookup-key out "missing" "fallback")])
 =>
 ["a" nil "fallback"])

^{:refer xt.runtime.type-hashmap/hashmap-assoc, :added "4.1"}
(fact
 "keeps persistent updates immutable"
 ^{:hidden true}
 (!.js
  (var h0 (hm/hashmap "a" 1))
  (var h1 (hm/hashmap-assoc h0 "b" 2))
  [(hm/hashmap-lookup-key h0 "b" "missing")
   (hm/hashmap-lookup-key h1 "b" "missing")
   (. h1 _size)])
 =>
 ["missing" 2 2])

^{:refer xt.runtime.type-hashmap/hashmap-to-mutable!, :added "4.1"}
(fact
 "supports mutable edits and roundtrips back to persistent"
 ^{:hidden true}
 (!.js
  (var
   out
   (->
    (hm/hashmap "a" 1)
    (hm/hashmap-to-mutable!)
    (hm/hashmap-assoc! "b" 2)
    (hm/hashmap-dissoc! "a")
    (hm/hashmap-to-persistent!)))
  [(ic/is-persistent? out)
   (hm/hashmap-lookup-key out "a" "missing")
   (hm/hashmap-lookup-key out "b" "missing")
   (. out _size)])
 =>
 [true "missing" 2 1])

^{:refer xt.runtime.type-hashmap/hashmap-eq, :added "4.1"}
(fact
 "compares and hashes maps independent of insertion order"
 ^{:hidden true}
 (!.js
  (var h1 (hm/hashmap "a" 1 "b" 2))
  (var h2 (hm/hashmap "b" 2 "a" 1))
  [(hm/hashmap-eq h1 h2) (== (. h1 (hash)) (. h2 (hash)))])
 =>
 [true true])

^{:refer xt.runtime.type-hashmap/hashmap-collect-pairs, :added "4.1"}
(fact
 "collects pair objects from the trie root"
 ^{:hidden true}
 (!.js
  (var
   entries
   (hm/hashmap-collect-pairs (. (hm/hashmap "a" 1 "b" 2) _root) []))
  [(ic/count entries)
   (ic/show (xt/x:get-idx entries (xt/x:offset 0)))
   (ic/show (xt/x:get-idx entries (xt/x:offset 1)))])
 =>
 [2 "[\"b\", 2]" "[\"a\", 1]"])

^{:refer xt.runtime.type-hashmap/hashmap-to-iter, :added "4.1"}
(fact
 "iterates over hashmap entries as pairs"
 ^{:hidden true}
 (!.js
  (var entries (it/arr< (hm/hashmap-to-iter (hm/hashmap "a" 1 "b" 2))))
  [(ic/count entries)
   (ic/show (xt/x:get-idx entries (xt/x:offset 0)))
   (ic/show (xt/x:get-idx entries (xt/x:offset 1)))])
 =>
 [2 "[\"b\", 2]" "[\"a\", 1]"])

^{:refer xt.runtime.type-hashmap/hashmap-to-array, :added "4.1"}
(fact
 "converts hashmap entries into entry arrays"
 ^{:hidden true}
 (!.js (hm/hashmap-to-array (hm/hashmap "a" 1 "b" 2)))
 =>
 [["b" 2] ["a" 1]])

^{:refer xt.runtime.type-hashmap/hashmap-new, :added "4.1"}
(fact
 "creates a bare hashmap object with the provided root and size"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap-new node/EMPTY_HASHMAP_NODE 3 nil))
  [(. out ["::"]) (. out _size) (xt/x:get-key (. out _root) "::")])
 =>
 ["hashmap" 3 "hashmap.node"])

^{:refer xt.runtime.type-hashmap/hashmap-empty, :added "4.1"}
(fact
 "creates an empty hashmap from an existing hashmap"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap-empty (hm/hashmap "a" 1)))
  [(. out _size) (hm/hashmap-show out)])
 =>
 [0 "{}"])

^{:refer xt.runtime.type-hashmap/hashmap-is-editable, :added "4.1"}
(fact
 "detects mutable vs persistent hashmaps"
 ^{:hidden true}
 (!.js
  [(hm/hashmap-is-editable (hm/hashmap "a" 1))
   (hm/hashmap-is-editable (hm/hashmap-empty-mutable))])
 =>
 [false true])

^{:refer xt.runtime.type-hashmap/hashmap-to-persistent!, :added "4.1"}
(fact
 "converts a mutable hashmap back into a persistent hashmap"
 ^{:hidden true}
 (!.js
  (var
   out
   (->
    (hm/hashmap "a" 1)
    (hm/hashmap-to-mutable!)
    (hm/hashmap-assoc! "b" 2)
    (hm/hashmap-to-persistent!)))
  [(hm/hashmap-is-editable out)
   (hm/hashmap-lookup-key out "b" "missing")])
 =>
 [false 2])

^{:refer xt.runtime.type-hashmap/hashmap-lookup-key, :added "4.1"}
(fact
 "looks up keys with support for nil values and defaults"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap-assoc hm/EMPTY_HASHMAP "a" nil))
  [(hm/hashmap-lookup-key out "a" "missing")
   (hm/hashmap-lookup-key out "b" "missing")])
 =>
 [nil "missing"])

^{:refer xt.runtime.type-hashmap/hashmap-keys, :added "4.1"}
(fact
 "returns the hashmap keys"
 ^{:hidden true}
 (!.js (hm/hashmap-keys (hm/hashmap "a" 1 "b" 2)))
 =>
 ["b" "a"])

^{:refer xt.runtime.type-hashmap/hashmap-vals, :added "4.1"}
(fact
 "returns the hashmap values"
 ^{:hidden true}
 (!.js (hm/hashmap-vals (hm/hashmap "a" 1 "b" 2)))
 =>
 [2 1])

^{:refer xt.runtime.type-hashmap/hashmap-assoc!, :added "4.1"}
(fact
 "mutably associates key/value pairs and updates existing keys"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap-empty-mutable))
  (hm/hashmap-assoc! out "a" 1)
  (hm/hashmap-assoc! out "b" 2)
  (hm/hashmap-assoc! out "b" 3)
  [(hm/hashmap-lookup-key out "a" "missing")
   (hm/hashmap-lookup-key out "b" "missing")
   (. out _size)])
 =>
 [1 3 2])

^{:refer xt.runtime.type-hashmap/hashmap-dissoc, :added "4.1"}
(fact
 "keeps persistent dissoc immutable"
 ^{:hidden true}
 (!.js
  (var h0 (hm/hashmap "a" 1 "b" 2))
  (var h1 (hm/hashmap-dissoc h0 "a"))
  [(hm/hashmap-lookup-key h0 "a" "missing")
   (hm/hashmap-lookup-key h1 "a" "missing")
   (. h1 _size)])
 =>
 [1 "missing" 1])

^{:refer xt.runtime.type-hashmap/hashmap-dissoc!, :added "4.1"}
(fact
 "mutably dissociates keys and keeps the remaining entries"
 ^{:hidden true}
 (!.js
  (var out (-> (hm/hashmap "a" 1 "b" 2) (hm/hashmap-to-mutable!)))
  (hm/hashmap-dissoc! out "a")
  (hm/hashmap-dissoc! out "missing")
  [(hm/hashmap-lookup-key out "a" "missing")
   (hm/hashmap-lookup-key out "b" "missing")
   (. out _size)])
 =>
 ["missing" 2 1])

^{:refer xt.runtime.type-hashmap/hashmap-hash, :added "4.1"}
(fact
 "computes the same unordered hash as the protocol hash function"
 ^{:hidden true}
 (!.js
  (var h1 (hm/hashmap "a" 1 "b" 2))
  (var h2 (hm/hashmap "b" 2 "a" 1))
  [(hm/hashmap-hash h1)
   (. h1 (hash))
   (== (hm/hashmap-hash h1) (hm/hashmap-hash h2))])
 =>
 [1875325 1875325 true])

^{:refer xt.runtime.type-hashmap/hashmap-show, :added "4.1"}
(fact
 "shows the hashmap as an EDN-like map string"
 ^{:hidden true}
 (!.js (hm/hashmap-show (hm/hashmap "a" 1 "b" 2)))
 =>
 #"\{(?:\"a\" 1, \"b\" 2|\"b\" 2, \"a\" 1)\}")

^{:refer xt.runtime.type-hashmap/hashmap-create, :added "4.1"}
(fact
 "creates a hashmap with the default prototype"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap-create node/EMPTY_HASHMAP_NODE 0))
  [(. out ["::"]) (. out _size) (hm/hashmap-show out)])
 =>
 ["hashmap" 0 "{}"])

^{:refer xt.runtime.type-hashmap/hashmap-empty-mutable, :added "4.1"}
(fact
 "creates an empty mutable hashmap"
 ^{:hidden true}
 (!.js
  (var out (hm/hashmap-empty-mutable))
  [(hm/hashmap-is-editable out) (. out _size) (hm/hashmap-show out)])
 =>
 [true 0 "{}"])
