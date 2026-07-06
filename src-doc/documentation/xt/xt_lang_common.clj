(ns documentation.xt-lang-common
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
             [xt.lang.common-tree :as xtt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.lang.common"
         :subtitle "Data, iteration, math, string, tree, and utility layers."
         :lead "The `xt.lang.common-*` namespaces are the everyday standard library for generated xtalk programs."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Portable programs need predictable helpers for maps, arrays, object traversal, string formatting, sorting, tracing, and tree operations. These namespaces smooth over target runtime differences."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Data access"}]]

"`xt.lang.common-data` provides empty checks, lookup tables, and array/object helpers."

(fact "check emptiness and build lookups"
  ^{:refer xt.lang.common-data/is-empty? :added "4.0"}
  (!.js
    [(xtd/is-empty? nil)
     (xtd/is-empty? "")
     (xtd/is-empty? [1 2 3])
     (xtd/is-empty? {:a 1})])
  => [true true false false]

  ^{:refer xt.lang.common-data/arr-zip :added "4.0"}
  (!.js
    (xtd/arr-zip ["a" "b"] [1 2]))
  => {"a" 1 "b" 2}

  ^{:refer xt.lang.common-data/lu-create :added "4.0"}
  (!.js
    (var lu (xtd/lu-create))
    (xtd/lu-set lu "a" 1)
    (xtd/lu-get lu "a"))
  => 1)

[[:section {:title "String helpers"}]]

"`xt.lang.common-string` normalises split, join, replace, and symbol-path behaviour across targets."

(fact "split, join, replace, and split symbol paths"
  ^{:refer xt.lang.common-string/split :added "4.0"}
  (!.js
    (xts/split "hello/world" "/"))
  => ["hello" "world"]

  ^{:refer xt.lang.common-string/join :added "4.0"}
  (!.js
    (xts/join "/" ["hello" "world"]))
  => "hello/world"

  ^{:refer xt.lang.common-string/replace :added "4.0"}
  (!.js
    (xts/replace "hello/world" "/" "_"))
  => "hello_world"

  ^{:refer xt.lang.common-string/sym-pair :added "4.0"}
  (!.js
    (xts/sym-pair "xt.lang/common-string"))
  => ["xt.lang" "common-string"])

[[:section {:title "Tree operations"}]]

"`xt.lang.common-tree` compares and transforms nested data without depending on target-specific equality."

(fact "compare and transform nested data"
  ^{:refer xt.lang.common-tree/eq-nested :added "4.0"}
  (!.js
    [(xtt/eq-nested {:a {:b 1}} {:a {:b 1}})
     (xtt/eq-nested {:a {:b 1}} {:a {:b 2}})])
  => [true false]

  ^{:refer xt.lang.common-tree/tree-walk :added "4.0"}
  (!.js
    (xtt/tree-walk
     {:a 1 :b [2 {:c 3}]}
     (fn [x] (return x))
     (fn [x]
       (if (xt/x:is-number? x)
         (return (+ x 1))
         (return x)))))
  => {"a" 2 "b" [3 {"c" 4}]})

[[:section {:title "End-to-end: reshaping nested data"}]]

"Combining data and tree helpers makes it easy to normalise records before emitting them."

(fact "build a lookup and walk a tree"
  ^{:refer xt.lang.common-data/arr-zip :added "4.0"}
  (!.js
    (var ids (xtd/arr-zip ["usd" "xlm"] ["US Dollar" "Stellar"]))
    (var out {})
    (xt/for:object [[k v] ids]
      (xt/x:set-key out k {:name v :active true}))
    out)
  => {"usd" {"name" "US Dollar" "active" true}
      "xlm" {"name" "Stellar" "active" true}})

[[:chapter {:title "Examples and usage" :link "usage"}]]

"Use common-data for object and array access, common-iter for loops, common-string for portable string behavior, common-sort-by/topo for ordering, and common-tree for nested structures. Pages in xt.db and xt.substrate build on these helpers heavily."

[[:chapter {:title "API" :link "api"}]]
