(ns xt.lang.override.custom-js-test
  (:require [std.lang :as l]
            [xt.lang.override.custom-js :refer :all])
  (:use code.test))

(l/script :js
  {:runtime :oneshot
   :config  {:program :nodejs}
   :layout :full
   :require [[xt.lang :as k]]})

^{:refer xt.lang.override.custom-js/pad-left :added "4.0"}
(fact "override for pad left"
  ^:hidden
  
  (!.js
   (k/pad-left "000" 5 "-"))
  => "--000")

^{:refer xt.lang.override.custom-js/pad-right :added "4.0"}
(fact "override for pad right"
  ^:hidden
  
  (!.js
   (k/pad-right "000" 5 "-"))
  => "000--")

^{:refer xt.lang.override.custom-js/arr-each :added "4.0"}
(fact "override for arr each"
  ^:hidden
  
  (!.js
   (var a := [])
   (k/arr-each [1 2 3 4 5] (fn [e]
                             (x:arr-push a (+ 1 e))))
   a)
  => [2 3 4 5 6])

^{:refer xt.lang.override.custom-js/arr-every :added "4.0"}
(fact "override for every"
  ^:hidden
  
  (!.js
   [(k/arr-every [1 2 3] (fn [e]
                           (return (or (== e 1)
                                       (== e 3)))))
     (k/arr-every [1 3] (fn [e]
                          (return (or (== e 1)
                                      (== e 3)))))])
  => [false true])

^{:refer xt.lang.override.custom-js/arr-some :added "4.0"}
(fact "override for some"
  ^:hidden
  
  (!.js
   [(k/arr-some [1 2 3] (fn [e]
                          (return (== e 2))))
     (k/arr-some [1 3] (fn [e]
                         (return (== e 2))))])
  => [true false])

^{:refer xt.lang.override.custom-js/arr-foldl :added "4.0"}
(fact "override for foldl"
  ^:hidden
  
  (!.js
   (k/arr-foldl [1 2 3 4 5]
                (fn [acc e]
                  (return (+ acc e)))
                0))
  => 15)

^{:refer xt.lang.override.custom-js/arr-foldr :added "4.0"}
(fact "override for foldr"
  ^:hidden
  
  (!.js
   (k/arr-foldr [1 2 3 4 5]
                (fn [acc e]
                  (x:arr-push acc e)
                  (return acc))
                []))
  => [5 4 3 2 1])
