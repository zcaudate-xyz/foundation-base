(ns hara.lang.rewrite.walk-test
  (:use code.test)
  (:require [hara.lang.rewrite.walk :as walk]))

^{:refer hara.lang.rewrite.walk/rewrite-coll :added "4.0"}
(fact "applies rewrite-item to every element, returning a sequence"
  (walk/rewrite-coll [1 2 3] inc)
  => '(2 3 4))

^{:refer hara.lang.rewrite.walk/rewrite-map-entry :added "4.0"}
(fact "applies rewrite-item to both key and value of a map entry"
  (walk/rewrite-map-entry [:a 1]
                         (fn [x]
                           (cond (keyword? x) (name x)
                                 (number? x) (inc x)
                                 :else x)))
  => ["a" 2])

^{:refer hara.lang.rewrite.walk/rewrite-map :added "4.0"}
(fact "applies rewrite-item to every key and value while preserving map type"
  (let [m (hash-map :a 1 :b 2)]
    [(walk/rewrite-map m (fn [x]
                           (cond (keyword? x) (name x)
                                 (number? x) (inc x)
                                 :else x)))
     (type (walk/rewrite-map m identity))])
  => [{"a" 2 "b" 3} clojure.lang.PersistentHashMap])

^{:refer hara.lang.rewrite.walk/rewrite-vector :added "4.0"}
(fact "rewrites each element and preserves metadata"
  (let [v (with-meta [1 2 3] {:line 5})]
    [(walk/rewrite-vector v inc)
     (meta (walk/rewrite-vector v inc))])
  => [[2 3 4] {:line 5}])

^{:refer hara.lang.rewrite.walk/rewrite-set :added "4.0"}
(fact "rewrites each element and preserves metadata"
  (let [s (with-meta #{1 2 3} {:line 5})]
    [(walk/rewrite-set s inc)
     (meta (walk/rewrite-set s inc))])
  => [#{2 3 4} {:line 5}])

^{:refer hara.lang.rewrite.walk/rewrite-map-form :added "4.0"}
(fact "rewrites map entries and preserves metadata"
  (let [m (with-meta {:a 1} {:line 5})]
    [(walk/rewrite-map-form m (fn [x]
                                (cond (keyword? x) (name x)
                                      (number? x) (inc x)
                                      :else x)))
     (meta (walk/rewrite-map-form m identity))])
  => [{"a" 2} {:line 5}])

^{:refer hara.lang.rewrite.walk/rewrite-form :added "4.1"}
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

^{:refer hara.lang.rewrite.walk/rewrite-binding-vector :added "4.1"}
(fact "rewrites the rhs of binding vectors"
  (walk/rewrite-binding-vector
   '[a (+ 1 2) :meta]
   (fn [form]
     (if (seq? form)
       '(+ 2 3)
       form)))
  => '[a (+ 2 3) :meta])