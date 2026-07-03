(ns kmi.lang.common-util-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.common-util :as util]
             [kmi.lang.type-symbol :as sym]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-pair :as pair]
             [kmi.lang.type-vector :as vec]
             [kmi.lang.type-syntax :as syn]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[kmi.lang.common-util :as util]
             [kmi.lang.type-symbol :as sym]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-pair :as pair]
             [kmi.lang.type-vector :as vec]
             [kmi.lang.type-syntax :as syn]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.lang.common-util/is-managed? :added "4.1"}
(fact "checks if an object is managed"

  (!.js
   [(util/is-managed? nil)
    (util/is-managed? "hello")
    (util/is-managed? 1)
    (util/is-managed? [1 2])
    (util/is-managed? {})
    (util/is-managed? {"::" "demo"})
    (util/is-managed? (sym/symbol "hello" "world"))
    (util/is-managed? (kw/keyword "hello" "world"))
    (util/is-managed? (syn/syntax-create 1 "meta"))])
  => [false false false false false true true true true]

  (!.lua
   [(util/is-managed? nil)
    (util/is-managed? "hello")
    (util/is-managed? 1)
    (util/is-managed? [1 2])
    (util/is-managed? {})
    (util/is-managed? {"::" "demo"})
    (util/is-managed? (sym/symbol "hello" "world"))
    (util/is-managed? (kw/keyword "hello" "world"))
    (util/is-managed? (syn/syntax-create 1 "meta"))])
  => [false false false false false true true true true])

^{:refer kmi.lang.common-util/is-syntax? :added "4.1"}
(fact "checks if an object is syntax"

  (!.js
   [(util/is-syntax? nil)
    (util/is-syntax? {})
    (util/is-syntax? (sym/symbol nil "x"))
    (util/is-syntax? (kw/keyword nil "x"))
    (util/is-syntax? (syn/syntax-create 1 "meta"))])
  => [false false false false true]

  (!.lua
   [(util/is-syntax? nil)
    (util/is-syntax? {})
    (util/is-syntax? (sym/symbol nil "x"))
    (util/is-syntax? (kw/keyword nil "x"))
    (util/is-syntax? (syn/syntax-create 1 "meta"))])
  => [false false false false true])

^{:refer kmi.lang.common-util/hash :added "4.1"}
(fact "gets the hash of a value"

  (!.js
   [(util/hash nil)
    (util/hash 1)
    (util/hash "abc")
    (util/hash true)
    (util/hash (sym/symbol "a" "b"))
    (util/hash (kw/keyword "a" "b"))
    (util/hash (vec/vector 1 2 3))
    (util/hash {})])
  => (contains-in [0 1 1192459 1 integer? integer? integer? integer?])

  (!.lua
   [(util/hash nil)
    (util/hash 1)
    (util/hash "abc")
    (util/hash true)
    (util/hash (sym/symbol "a" "b"))
    (util/hash (kw/keyword "a" "b"))
    (util/hash (vec/vector 1 2 3))
    (util/hash {})])
  => (contains-in [0 1 1192459 1 integer? integer? integer? integer?]))

^{:refer kmi.lang.common-util/get-name :added "4.1"}
(fact "gets the name of a symbol or keyword"

  (!.js
   [(util/get-name (sym/symbol nil "hello"))
    (util/get-name (sym/symbol "ns" "hello"))
    (util/get-name (kw/keyword nil "hello"))
    (util/get-name (kw/keyword "ns" "hello"))])
  => ["hello" "hello" "hello" "hello"]

  (!.lua
   [(util/get-name (sym/symbol nil "hello"))
    (util/get-name (sym/symbol "ns" "hello"))
    (util/get-name (kw/keyword nil "hello"))
    (util/get-name (kw/keyword "ns" "hello"))])
  => ["hello" "hello" "hello" "hello"])

^{:refer kmi.lang.common-util/get-namespace :added "4.1"}
(fact "gets the namespace of a symbol or keyword"

  (!.js
   [(util/get-namespace (sym/symbol nil "hello"))
    (util/get-namespace (sym/symbol "ns" "hello"))
    (util/get-namespace (kw/keyword nil "hello"))
    (util/get-namespace (kw/keyword "ns" "hello"))])
  => [nil "ns" nil "ns"]

  (!.lua
   [(util/get-namespace (sym/symbol nil "hello"))
    (util/get-namespace (sym/symbol "ns" "hello"))
    (util/get-namespace (kw/keyword nil "hello"))
    (util/get-namespace (kw/keyword "ns" "hello"))])
  => [nil "ns" nil "ns"])

^{:refer kmi.lang.common-util/hash-with-cache :added "4.1"}
(fact "gets a memoized hash id"

  (!.js
   (var calls 0)
   (var preset {"_hash" 123})
   (var fresh {"_hash" nil})
   (var h-preset (util/hash-with-cache preset
                                       (fn [x] (:= calls (+ calls 1)) (return 999))))
   (var h0 (util/hash-with-cache fresh
                                 (fn [x] (:= calls (+ calls 1)) (return 42))))
   (var h1 (util/hash-with-cache fresh
                                 (fn [x] (:= calls (+ calls 1)) (return 999))))
   [calls h-preset h0 h1 preset._hash fresh._hash])
  => [1 123 42 42 123 42]

  (!.lua
   (var calls 0)
   (var preset {"_hash" 123})
   (var fresh {"_hash" nil})
   (var h-preset (util/hash-with-cache preset
                                       (fn [x] (:= calls (+ calls 1)) (return 999))))
   (var h0 (util/hash-with-cache fresh
                                 (fn [x] (:= calls (+ calls 1)) (return 42))))
   (var h1 (util/hash-with-cache fresh
                                 (fn [x] (:= calls (+ calls 1)) (return 999))))
   [calls h-preset h0 h1 preset._hash fresh._hash])
  => [1 123 42 42 123 42])

^{:refer kmi.lang.common-util/wrap-with-cache :added "4.1"}
(fact "wraps a hash function with caching"

  (!.js
   (var editable-calls 0)
   (var editable-obj {})
   (var editable-fn
        (util/wrap-with-cache (fn [x] (:= editable-calls (+ editable-calls 1)) (return 42))
                              (fn [x] (return true))))
   (var e0 (editable-fn editable-obj))
   (var e1 (editable-fn editable-obj))

   (var cached-calls 0)
   (var cached-obj {})
   (var cached-fn
        (util/wrap-with-cache (fn [x] (:= cached-calls (+ cached-calls 1)) (return 42))
                              (fn [x] (return false))))
   (var c0 (cached-fn cached-obj))
   (var c1 (cached-fn cached-obj))

   (var default-calls 0)
   (var default-obj {})
   (var default-fn
        (util/wrap-with-cache (fn [x] (:= default-calls (+ default-calls 1)) (return 42))))
   (var d0 (default-fn default-obj))
   (var d1 (default-fn default-obj))

   [editable-calls e0 e1
    cached-calls c0 c1 cached-obj._hash
    default-calls d0 d1 default-obj._hash])
  => [2 42 42
      1 42 42 42
      1 42 42 42]

  (!.lua
   (var editable-calls 0)
   (var editable-obj {})
   (var editable-fn
        (util/wrap-with-cache (fn [x] (:= editable-calls (+ editable-calls 1)) (return 42))
                              (fn [x] (return true))))
   (var e0 (editable-fn editable-obj))
   (var e1 (editable-fn editable-obj))

   (var cached-calls 0)
   (var cached-obj {})
   (var cached-fn
        (util/wrap-with-cache (fn [x] (:= cached-calls (+ cached-calls 1)) (return 42))
                              (fn [x] (return false))))
   (var c0 (cached-fn cached-obj))
   (var c1 (cached-fn cached-obj))

   (var default-calls 0)
   (var default-obj {})
   (var default-fn
        (util/wrap-with-cache (fn [x] (:= default-calls (+ default-calls 1)) (return 42))))
   (var d0 (default-fn default-obj))
   (var d1 (default-fn default-obj))

   [editable-calls e0 e1
    cached-calls c0 c1 cached-obj._hash
    default-calls d0 d1 default-obj._hash])
  => [2 42 42
      1 42 42 42
      1 42 42 42])

^{:refer kmi.lang.common-util/show :added "4.1"}
(fact "returns a string representation"

  (!.js
   [(util/show nil)
    (util/show "hello")
    (util/show 123)
    (util/show true)
    (util/show (sym/symbol "ns" "name"))
    (util/show (kw/keyword "ns" "name"))
    (util/show (pair/pair "a" 1))
    (util/show (vec/vector 1 2 3))
    (util/show (vec/vector))])
  => ["nil" "\"hello\"" "123" "true" "ns/name" ":ns/name" "[\"a\", 1]" "[1, 2, 3]" "[]"]

  (!.lua
   [(util/show nil)
    (util/show "hello")
    (util/show 123)
    (util/show true)
    (util/show (sym/symbol "ns" "name"))
    (util/show (kw/keyword "ns" "name"))
    (util/show (pair/pair "a" 1))
    (util/show (vec/vector 1 2 3))
    (util/show (vec/vector))])
  => ["nil" "\"hello\"" "123" "true" "ns/name" ":ns/name" "[\"a\", 1]" "[1, 2, 3]" "[]"])

^{:refer kmi.lang.common-util/eq :added "4.1"}
(fact "checks equivalence"

  (!.js
   (var sym (sym/symbol "a" "b"))
   (var s (syn/syntax-create sym "meta"))
   [(util/eq 1 1)
    (util/eq 1 2)
    (util/eq "x" "x")
    (util/eq nil nil)
    (util/eq (sym/symbol "a" "b") (sym/symbol "a" "b"))
    (util/eq (sym/symbol "a" "b") (sym/symbol "a" "c"))
    (util/eq (kw/keyword "a" "b") (kw/keyword "a" "b"))
    (util/eq (sym/symbol "a" "b") (kw/keyword "a" "b"))
    (util/eq (sym/symbol "a" "b") "a/b")
    (util/eq s sym)
    (util/eq sym s)
    (util/eq s s)
    (util/eq (syn/syntax-create 1 "m") 2)])
  => [true false true true true false true false false true true true false]

  (!.lua
   (var sym (sym/symbol "a" "b"))
   (var s (syn/syntax-create sym "meta"))
   [(util/eq 1 1)
    (util/eq 1 2)
    (util/eq "x" "x")
    (util/eq nil nil)
    (util/eq (sym/symbol "a" "b") (sym/symbol "a" "b"))
    (util/eq (sym/symbol "a" "b") (sym/symbol "a" "c"))
    (util/eq (kw/keyword "a" "b") (kw/keyword "a" "b"))
    (util/eq (sym/symbol "a" "b") (kw/keyword "a" "b"))
    (util/eq (sym/symbol "a" "b") "a/b")
    (util/eq s sym)
    (util/eq sym s)
    (util/eq s s)
    (util/eq (syn/syntax-create 1 "m") 2)])
  => [true false true true true false true false false true true true false])

^{:refer kmi.lang.common-util/count :added "4.1"}
(fact "gets the count of a value"

  (!.js
   [(util/count nil)
    (util/count "abc")
    (util/count [1 2 3])
    (util/count (pair/pair "a" 1))
    (util/count (vec/vector 1 2 3))
    (util/count (vec/vector))])
  => [nil 3 3 2 3 0]

  (!.lua
   [(util/count nil)
    (util/count "abc")
    (util/count [1 2 3])
    (util/count (pair/pair "a" 1))
    (util/count (vec/vector 1 2 3))
    (util/count (vec/vector))])
  => [nil 3 3 2 3 0])

^{:refer kmi.lang.common-util/impl-normalise :added "4.1"}
(fact "normalises nil to the sentinel nil value"

  (!.js
   [(util/impl-normalise 1)
    (== (util/impl-normalise nil) util/NIL)
    (util/impl-normalise "hello")])
  => [1 true "hello"]

  (!.lua
   [(util/impl-normalise 1)
    (== (util/impl-normalise nil) util/NIL)
    (util/impl-normalise "hello")])
  => [1 true "hello"])

^{:refer kmi.lang.common-util/impl-denormalise :added "4.1"}
(fact "denormalises the sentinel nil value back to nil"

  (!.js
   [(util/impl-denormalise 1)
    (util/impl-denormalise util/NIL)
    (== (util/impl-denormalise nil) nil)
    (util/impl-denormalise "hello")])
  => [1 nil true "hello"]

  (!.lua
   [(util/impl-denormalise 1)
    (util/impl-denormalise util/NIL)
    (== (util/impl-denormalise nil) nil)
    (util/impl-denormalise "hello")])
  => [1 nil true "hello"])
