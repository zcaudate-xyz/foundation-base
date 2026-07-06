(ns documentation.std-lib-walk
  (:require [std.lib.walk :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.walk` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Traversal"}]]

"`postwalk` and `prewalk` apply a function to every node of a nested data structure. `postwalk` visits children first; `prewalk` visits the parent first."

(fact "increment every number in a nested structure"
  (postwalk (fn [x] (if (number? x) (inc x) x))
            {:a [1 2 {:b 3}]})
  => {:a [2 3 {:b 4}]}

  (prewalk (fn [x] (if (number? x) (inc x) x))
           {:a [1 2 {:b 3}]})
  => {:a [2 3 {:b 4}]})

[[:section {:title "Key conversion"}]]

"These helpers recursively convert map keys between keywords and strings, including snake_case and spear-case variants."

(fact "convert string keys to keywords"
  (keywordize-keys {"a" 1 "b" {"c" 2}})
  => {:a 1 :b {:c 2}})

(fact "convert keyword keys to strings"
  (stringify-keys {:a 1 :b {:c 2}})
  => {"a" 1 "b" {"c" 2}})

(fact "convert between snake_case and spear-case"
  (keyword-spearify-keys {"a_b_c" [{"e_f_g" 1}]})
  => {:a-b-c [{:e-f-g 1}]}

  (string-snakify-keys {:a-b-c [{:e-f-g 1}]})
  => {"a_b_c" [{"e_f_g" 1}]})

[[:section {:title "Replacement"}]]

"`prewalk-replace` and `postwalk-replace` substitute matching forms throughout a tree. They are handy for templating and symbolic substitution."

(fact "replace symbols throughout a form"
  (prewalk-replace {'x 10 'y 20}
                   '(+ x (* y x)))
  => '(+ 10 (* 20 10))

  (postwalk-replace {'x 10 'y 20}
                    '(+ x (* y x)))
  => '(+ 10 (* 20 10)))

[[:section {:title "Searching"}]]

"`walk:find` collects every node satisfying a predicate, and `walk:keep` collects the non-nil results of applying a function to each node."

(fact "find nested vectors starting with an even number"
  (walk:find (fn [x]
               (and (vector? x)
                    (even? (first x))))
             [[1] [[3 [4 [6]]]]])
  => #{[4 [6]] [6]})

(fact "keep transformed odd numbers"
  (walk:keep (fn [x]
               (if (odd? x)
                 (+ 10 x)))
             [[1] [[3 [4 [6]]]]])
  => #{11 13})

[[:section {:title "End-to-end: normalise external JSON data"}]]

"A common pipeline is to keywordize keys from an external source, transform the values, then stringify the keys again for serialisation."

(fact "keywordize, transform, and stringify nested data"
  (->> {"user_name" "Ada"
        "user_age"  36
        "address"   {"street_name" "Maple"}}
       keywordize-keys
       (postwalk (fn [x] (if (string? x) (.toUpperCase x) x)))
       stringify-keys)
  => {"user_name" "ADA"
      "user_age"  36
      "address"   {"street_name" "MAPLE"}})

[[:chapter {:title "API" :link "std.lib.walk"}]]

[[:api {:namespace "std.lib.walk"}]]
