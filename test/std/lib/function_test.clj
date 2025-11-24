(ns std.lib.function-test
  (:use code.test)
  (:require [std.lib.function :refer :all]))

^{:refer std.lib.function/fn-form :added "3.0"}
(fact "creates a lambda form"
  (fn-form "Supplier" "get" '[x] '(inc x))
  => seq?)

^{:refer std.lib.function/fn-tags :added "3.0"}
(fact "creates tags for fn"

  (fn-tags ^{:tag 'long} [])
  => ["Long" nil nil]

  (fn-tags ^{:tag 'long} [^{:tag 'long} [] ^{:tag 'int} []])
  => ["Long" "Long" "Int"])

^{:refer std.lib.function/fn:supplier :added "3.0"
  :style/indent 1}
(fact "creates a java supplier" ^:hidden

  (-> (fn:supplier [] (+ 1 2 3))
      (.get))
  => 6

  (-> (fn:supplier ^long [] (+ 1 2 3))
      (.getAsLong))
  => 6)

^{:refer std.lib.function/fn:predicate :added "3.0"
  :style/indent 1}
(fact "creates a java predicate"

  (-> (fn:predicate [^long a] (< a 10))
      (.test 3))
  => true)

^{:refer std.lib.function/fn:lambda :added "3.0"
  :style/indent 1}
(fact "creates java unary/binary functions" ^:hidden

  (-> (fn:lambda [a] (+ a 10))
      (.apply 1))
  => 11

  (-> (fn:lambda [a b] (+ a b))
      (.apply 1 2))
  => 3)

^{:refer std.lib.function/fn:consumer :added "3.0"
  :style/indent 1}
(fact "creates a java unary function" ^:hidden

  (-> (fn:consumer [^long x] (+ x 1))
      (.accept 2))

  (-> (fn:consumer [x] (+ x 1))
      (.accept 2))
  => nil)

^{:refer std.lib.function/vargs? :added "3.0"}
(fact "checks that function contain variable arguments"

  (vargs? (fn [x])) => false

  (vargs? (fn [x & xs])) => true)

^{:refer std.lib.function/varg-count :added "3.0"}
(fact "counts the number of arguments types before variable arguments"

  (varg-count (fn [x y & xs])) => 2

  (varg-count (fn [x])) => nil)

^{:refer std.lib.function/arg-count :added "3.0"}
(fact "counts the number of non-varidic argument types"

  (arg-count (fn [x])) => [1]

  (arg-count (fn [x & xs])) => []

  (arg-count (fn ([x]) ([x y]))) => [1 2])

^{:refer std.lib.function/arg-check :added "3.0"}
(fact "counts the number of non-varidic argument types"

  (arg-check (fn [x]) 1) => true

  (arg-check (fn [x & xs]) 1) => true

  (arg-check (fn [x & xs]) 0)
  => (throws-info {:required 0 :actual [() 1]}))

^{:refer std.lib.function/fn:init-args :added "3.0"}
(fact "creates init args"

  (fn:init-args '[x] '(inc x) [])
  => '["" {} ([x] (inc x))])

^{:refer std.lib.function/fn:create-args :added "3.0"}
(fact "creates args for the body"

  (fn:create-args '[[x] (inc x) nil nil])
  => '("" {} [x] (inc x)) ^:hidden

  (fn:create-args '["doc" {:a 1} [x] (inc x)])
  => '("doc" {:a 1} [x] (inc x))

  (fn:create-args '["doc" [x] (inc x) nil])
  => '("doc" {} [x] (inc x))

  (fn:create-args '[{:a 1} [x] (inc x) nil])
  => '("" {:a 1} [x] (inc x)))

^{:refer std.lib.function/fn:def-form :added "3.0"}
(fact "creates a def form"
  (fn:def-form 'hello nil {} '[x] '(inc x))
  => '(def hello (inc x)))
