(ns std.lang.rewrite.destructure-test
  (:use code.test)
  (:require [std.lang.rewrite.destructure :as destruct]))

^{:refer std.lang.rewrite.destructure/destructure-bindings :added "4.1"}
(fact "builds sorted set destructuring bindings"
  [(destruct/destructure-target? '#{beta alpha})
   (destruct/destructure-target? '[beta alpha])
   (vec (destruct/destructure-bindings '#{beta alpha} 'TMP))
   (vec (destruct/destructure-bindings '#{beta alpha} 'TMP #(str "key/" (name %))))]
  => [true
      false
      '[[alpha (x:get-key TMP "alpha" nil)]
        [beta (x:get-key TMP "beta" nil)]]
      '[[alpha (x:get-key TMP "key/alpha" nil)]
        [beta (x:get-key TMP "key/beta" nil)]]])
