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

;; BEGIN merged documentation: plans/slop/summary/std_lib_walk_summary.md
;; sha256: 1fade4c6fadefa28dc46e67aff57a5a06ea03c434327d89daba89770d665cb71
[[:chapter {:title "std.lib.walk: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-walk-summary-md"}]]
"## std.lib.walk: A Comprehensive Summary\n\nThe `std.lib.walk` namespace provides a set of powerful functions for traversing and transforming arbitrary Clojure data structures. It extends the core `clojure.walk` functionality with additional utilities for key transformation, macro expansion, and searching within nested data. This module is fundamental for tasks involving deep inspection, modification, or normalization of complex data.\n\n**Key Features and Concepts:**\n\n1.  **Core Traversal Functions:**\n    *   `walk`: The foundational function for traversing data structures. It takes `inner` and `outer` functions, which are applied to the children and the form itself, respectively. It handles various collection types (lists, maps, vectors, sets, records).\n    *   `postwalk`: Performs a depth-first, post-order traversal. The `f` function is applied to the form *after* its children have been processed.\n    *   `prewalk`: Performs a depth-first, pre-order traversal. The `f` function is applied to the form *before* its children have been processed.\n\n2.  **Key Transformation Utilities:**\n    *   `keywordize-keys`: Recursively transforms all map keys from strings or symbols to keywords.\n    *   `keyword-spearify-keys`: Recursively transforms string keys with underscores (e.g., \"a_b_c\") to kebab-case keywords (e.g., `:a-b-c`).\n    *   `stringify-keys`: Recursively transforms all map keys from keywords to strings.\n    *   `string-snakify-keys`: Recursively transforms kebab-case keyword keys (e.g., `:a-b-c`) to snake-case strings (e.g., \"a_b_c\"). These are particularly useful for interoperability with systems that prefer different key naming conventions (e.g., JSON APIs, databases).\n\n3.  **Replacement and Macro Expansion:**\n    *   `prewalk-replace`: Recursively replaces elements in a form based on a provided substitution map, performing a pre-order traversal.\n    *   `postwalk-replace`: Similar to `prewalk-replace`, but performs a post-order traversal.\n    *   `macroexpand-all`: Recursively performs all possible macroexpansions within a given form, which is invaluable for inspecting the fully expanded code generated by macros.\n\n4.  **Searching and Filtering:**\n    *   `walk:contains`: Recursively walks a form to check if any element satisfies a given predicate.\n    *   `walk:find`: Recursively walks a form to find all elements that satisfy a given predicate, returning them as a set.\n    *   `walk:keep`: Recursively walks a form, applies a function `f` to each element, and collects all non-nil results into a set.\n\n**Usage and Importance:**\n\nThe `std.lib.walk` module is a cornerstone for data manipulation and code analysis within the `foundation-base` project. It enables:\n\n*   **Data Normalization:** Easily convert data between different key naming conventions (e.g., snake_case strings to kebab-case keywords) for consistent internal representation or external API compatibility.\n*   **Code Transformation:** Perform complex transformations on Clojure code (represented as data) for tasks like static analysis, refactoring, or code generation.\n*   **Deep Inspection:** Efficiently search for specific patterns or values within deeply nested data structures without writing custom recursive logic.\n*   **Meta-programming:** The `macroexpand-all` function is crucial for understanding and debugging complex macros, which are heavily used in the `foundation-base` project for transpilation and runtime management.\n\nBy providing these versatile tools, `std.lib.walk` significantly enhances the project's ability to process, analyze, and transform both data and code, contributing to its overall flexibility and power.\n"
;; END merged documentation: plans/slop/summary/std_lib_walk_summary.md
