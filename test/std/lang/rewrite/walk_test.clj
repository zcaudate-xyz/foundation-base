(ns std.lang.rewrite.walk-test
  (:use code.test)
  (:require [std.lang.rewrite.walk :as walk]))

^{:refer std.lang.rewrite.walk/rewrite-form :added "4.1"}
(fact "rewrites vectors, sets and maps while preserving top-level metadata"
  (let [vec-form (with-meta ['a 1] {:line 10})
        set-form (with-meta '#{a 1} {:line 11})
        map-form (with-meta '{a 1} {:line 12})
        rewrite-item #(if (symbol? %) 'b %)
        vec-out (walk/rewrite-form vec-form identity rewrite-item)
        set-out (walk/rewrite-form set-form identity rewrite-item)
        map-out (walk/rewrite-form map-form identity rewrite-item)]
    [(= vec-out '[b 1])
     (= set-out '#{b 1})
     (= map-out '{b 1})
     (= (meta vec-out) {:line 10})
     (= (meta set-out) {:line 11})
     (= (meta map-out) {:line 12})])
  => [true true true true true true])

^{:refer std.lang.rewrite.walk/rewrite-binding-vector :added "4.1"}
(fact "rewrites the rhs of binding vectors"
  (walk/rewrite-binding-vector
   '[a (+ 1 2) :meta]
   (fn [form]
     (if (seq? form)
       '(+ 2 3)
       form)))
  => '[a (+ 2 3) :meta])
