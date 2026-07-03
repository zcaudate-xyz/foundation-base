(ns kmi.lang.runtime.primitive-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.runtime.primitive :as prim]
             [kmi.lang.type-keyword :as kw]
             [kmi.lang.type-list :as list]
             [kmi.lang.type-vector :as vec]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-hashset :as hs]
             [kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.lang.runtime.primitive/count-value :added "4.1"}
(fact "counts a value"

  (!.js
   [(prim/count-value nil)
    (prim/count-value 42)
    (prim/count-value "hello")
    (prim/count-value (vec/vector 1 2 3))
    (prim/count-value (list/list 1 2 3))
    (prim/count-value (hm/hashmap (kw/keyword nil "a") 1 (kw/keyword nil "b") 2))
    (prim/count-value (hs/hashset 1 2 3))])
  => [0 0 5 3 3 2 3])

^{:refer kmi.lang.runtime.primitive/first-value :added "4.1"}
(fact "returns the first element"

  (!.js
   [(prim/first-value (vec/vector 1 2 3))
    (prim/first-value (list/list 1 2 3))
    (prim/first-value (vec/vector))])
  => [1 1 nil])

^{:refer kmi.lang.runtime.primitive/rest-value :added "4.1"}
(fact "returns the rest as a vector"

  (!.js
   (var r (prim/rest-value (vec/vector 1 2 3)))
   [(p/to-array r)
    (p/size r)])
  => [[2 3] 2])

^{:refer kmi.lang.runtime.primitive/nth-value :added "4.1"}
(fact "returns the nth element"

  (!.js
   [(prim/nth-value (vec/vector "a" "b" "c") 0)
    (prim/nth-value (vec/vector "a" "b" "c") 2)
    (prim/nth-value (vec/vector "a" "b" "c") 5)])
  => ["a" "c" nil])

^{:refer kmi.lang.runtime.primitive/str-value :added "4.1"}
(fact "concatenates values into a string"

  (!.js
   [(prim/str-value)
    (prim/str-value "a")
    (prim/str-value 1 2 3)
    (prim/str-value "hello" " " "world")])
  => ["" "\"a\"" "123" "\"hello\"\" \"\"world\""])

^{:refer kmi.lang.runtime.primitive/plus :added "4.1"}
(fact "adds numbers"

  (!.js
   [(prim/plus)
    (prim/plus 1)
    (prim/plus 1 2 3 4)])
  => [0 1 10])

^{:refer kmi.lang.runtime.primitive/minus :added "4.1"}
(fact "subtracts numbers"

  (!.js
   [(prim/minus 5 3)
    (prim/minus 0 7)])
  => [2 -7])

^{:refer kmi.lang.runtime.primitive/multiply :added "4.1"}
(fact "multiplies numbers"

  (!.js
   [(prim/multiply)
    (prim/multiply 5)
    (prim/multiply 2 3 4)])
  => [1 5 24])

^{:refer kmi.lang.runtime.primitive/divide :added "4.1"}
(fact "divides numbers"

  (!.js
   [(prim/divide 10 2)
    (prim/divide 1 2)])
  => [5 0.5])

^{:refer kmi.lang.runtime.primitive/less-than :added "4.1"}
(fact "compares with less than"

  (!.js
   [(prim/less-than 1 2)
    (prim/less-than 2 1)
    (prim/less-than 1 1)])
  => [true false false])

^{:refer kmi.lang.runtime.primitive/greater-than :added "4.1"}
(fact "compares with greater than"

  (!.js
   [(prim/greater-than 2 1)
    (prim/greater-than 1 2)
    (prim/greater-than 1 1)])
  => [true false false])

^{:refer kmi.lang.runtime.primitive/less-than-or-equal :added "4.1"}
(fact "compares with less than or equal"

  (!.js
   [(prim/less-than-or-equal 1 2)
    (prim/less-than-or-equal 2 2)
    (prim/less-than-or-equal 3 2)])
  => [true true false])

^{:refer kmi.lang.runtime.primitive/greater-than-or-equal :added "4.1"}
(fact "compares with greater than or equal"

  (!.js
   [(prim/greater-than-or-equal 2 1)
    (prim/greater-than-or-equal 2 2)
    (prim/greater-than-or-equal 1 2)])
  => [true true false])

^{:refer kmi.lang.runtime.primitive/equal :added "4.1"}
(fact "checks equality"

  (!.js
   [(prim/equal 1 1)
    (prim/equal 1 2)
    (prim/equal "a" "a")
    (prim/equal nil nil)
    (prim/equal (kw/keyword nil "a") (kw/keyword nil "a"))
    (prim/equal true false)])
  => [true false true true true false])

^{:refer kmi.lang.runtime.primitive/not-equal :added "4.1"}
(fact "checks inequality"

  (!.js
   [(prim/not-equal 1 1)
    (prim/not-equal 1 2)
    (prim/not-equal nil nil)])
  => [false true false])

^{:refer kmi.lang.runtime.primitive/not-value :added "4.1"}
(fact "negates a boolean"

  (!.js
   [(prim/not-value true)
    (prim/not-value false)
    (prim/not-value nil)])
  => [false true true])

^{:refer kmi.lang.runtime.primitive/type-value :added "4.1"}
(fact "returns the native class tag"

  (!.js
   [(prim/type-value nil)
    (prim/type-value 1)
    (prim/type-value "hello")
    (prim/type-value (kw/keyword nil "a"))
    (prim/type-value (vec/vector 1 2))
    (prim/type-value (list/list 1 2))])
  => ["nil" "number" "string" "keyword" "vector" "list"])

^{:refer kmi.lang.runtime.primitive/list-value :added "4.1"}
(fact "creates a list"

  (!.js
   (var l (prim/list-value 1 2 3))
   [(p/to-array l)
    (p/size l)])
  => [[1 2 3] 3])

^{:refer kmi.lang.runtime.primitive/vector-value :added "4.1"}
(fact "creates a vector"

  (!.js
   (var v (prim/vector-value 1 2 3))
   [(p/to-array v)
    (p/size v)])
  => [[1 2 3] 3])

^{:refer kmi.lang.runtime.primitive/hash-map-value :added "4.1"}
(fact "creates a hash map"

  (!.js
   (var m (prim/hash-map-value (kw/keyword nil "a") 1 (kw/keyword nil "b") 2))
   [(p/size m)
    (hm/hashmap-lookup-key m (kw/keyword nil "a") "missing")
    (hm/hashmap-lookup-key m (kw/keyword nil "b") "missing")])
  => [2 1 2])

^{:refer kmi.lang.runtime.primitive/hash-set-value :added "4.1"}
(fact "creates a hash set"

  (!.js
   (var s (prim/hash-set-value 1 2 2 3))
   [(p/size s)
    (hs/hashset-has? s 2)
    (hs/hashset-has? s 4)])
  => [3 true false])

^{:refer kmi.lang.runtime.primitive/apply-value :added "4.1"}
(fact "applies a function to args"

  (!.js
   [(prim/apply-value prim/plus [1 2 3])
    (prim/apply-value prim/multiply [2 3 4])
    (prim/apply-value prim/str-value ["a" "b"])])
  => [6 24 "\"a\"\"b\""])

^{:refer kmi.lang.runtime.primitive/init-runtime :added "4.1"}
(fact "seeds a runtime with primitives"

  (!.js
   (var rt (prim/init-runtime {"ns" "user"
                                "namespaces" {"user" {"vars" {} "macros" {} "aliases" {} "refs" {}}
                                              "kmi.core" {"vars" {} "macros" {} "aliases" {} "refs" {}}}}))
   (var core (xt/x:get-key (xt/x:get-key rt "namespaces") "kmi.core"))
   (var vars (xt/x:get-key core "vars"))
   [(xt/x:has-key? vars "+")
    (xt/x:has-key? vars "-")
    (xt/x:has-key? vars "count")
    (xt/x:has-key? vars "list")
    (xt/x:has-key? vars "type")
    (== (xt/x:get-key rt "ns") "user")])
  => [true true true true true true])
