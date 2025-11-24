(ns std.lib.walk-test
  (:use code.test)
  (:require [std.lib.walk :refer :all]))

^{:refer std.lib.walk/walk :added "3.0"}
(fact "Traverses form, an arbitrary data structure"
  (resolve 'walk) => var?)

^{:refer std.lib.walk/postwalk :added "3.0"}
(fact "Performs a depth-first, post-order traversal of form"
  (resolve 'postwalk) => var?)

^{:refer std.lib.walk/prewalk :added "3.0"}
(fact "Like postwalk, but does pre-order traversal."
  (resolve 'prewalk) => var?)

^{:refer std.lib.walk/keywordize-keys :added "3.0"}
(fact "Recursively transforms all map keys from strings to keywords."
  (resolve 'keywordize-keys) => var?)

^{:refer std.lib.walk/keyword-spearify-keys :added "4.0"}
(fact "recursively transfroms all map keys to spearcase"

  (keyword-spearify-keys  {"a_b_c" [{"e_f_g" 1}]})
  => {:a-b-c [{:e-f-g 1}]})

^{:refer std.lib.walk/stringify-keys :added "3.0"}
(fact "Recursively transforms all map keys from keywords to strings.")

^{:refer std.lib.walk/string-snakify-keys :added "4.0"}
(fact "recursively transforms keyword to string keys"

  (string-snakify-keys
   {:a-b-c [{:e-f-g 1}]})
  => {"a_b_c" [{"e_f_g" 1}]})

^{:refer std.lib.walk/prewalk-replace :added "3.0"}
(fact "Recursively transforms form by replacing keys in smap with their values.")

^{:refer std.lib.walk/postwalk-replace :added "3.0"}
(fact "Recursively transforms form by replacing keys in smap with their values.")

^{:refer std.lib.walk/macroexpand-all :added "3.0"}
(fact "Recursively performs all possible macroexpansions in form.")

^{:refer std.lib.walk/walk:contains :added "4.0"}
(fact "recursively walks form to check for containment")

^{:refer std.lib.walk/walk:find :added "4.0"}
(fact "recursively walks to find all matching forms"
  ^:hidden
  
  (walk:find (fn [x]
               (and (vector? x)
                    (even? (first x))))
             [[1] [[3 [4 [6]]]]])
  => #{[4 [6]]
       [6]})

^{:refer std.lib.walk/walk:keep :added "4.0"}
(fact "recursively walks and keeps all processed forms"
  ^:hidden
  
  (walk:keep (fn [x]
               (if (odd? x)
                 (+ 10 x)))
             [[1] [[3 [4 [6]]]]])
  => #{13 11})
