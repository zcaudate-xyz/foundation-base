(ns tahto.core.rewrite.destructure-test
  (:use code.test)
  (:require [tahto.core.rewrite.destructure :as destruct]))

^{:refer tahto.core.rewrite.destructure/destructure-target? :added "4.0"}
(fact "checks if form is a non-empty set of symbols"
  [(destruct/destructure-target? '#{a b})
   (destruct/destructure-target? '#{})
   (destruct/destructure-target? '[a b])
   (destruct/destructure-target? '#{a 1})]
  => [true nil false false])

^{:refer tahto.core.rewrite.destructure/destructure-symbols :added "4.0"}
(fact "returns the target symbols sorted by string representation"
  [(vec (destruct/destructure-symbols '#{beta alpha}))
   (vec (destruct/destructure-symbols '#{c a b}))
   (vec (destruct/destructure-symbols '#{beta alpha} #(str "k/" (name %))))]
  => ['[alpha beta]
      '[a b c]
      '[alpha beta]])

^{:refer tahto.core.rewrite.destructure/destructure-value :added "4.0"}
(fact "builds a key lookup form for a symbol"
  [(destruct/destructure-value 'TMP 'alpha)
   (destruct/destructure-value 'TMP 'alpha #(str "key/" (name %)))]
  => ['(x:get-key TMP "alpha" nil)
      '(x:get-key TMP "key/alpha" nil)])

^{:refer tahto.core.rewrite.destructure/destructure-bindings :added "4.1"}
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
