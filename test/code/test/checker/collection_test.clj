(ns code.test.checker.collection-test
  (:use [code.test :only [fact]])
  (:require [code.test.checker.collection :refer :all]
            [code.test.checker.common :as base]
            [code.test.base.process :as process]))

^{:refer code.test.checker.collection/verify-map :added "3.0"}
(fact "takes two maps and determines if they fit"
  (verify-map {:a (base/satisfies odd?)
               :b (base/satisfies even?)}
              {:a 1 :b 2})
  => true)

^{:refer code.test.checker.collection/verify-seq :added "3.0"}
(fact "takes two seqs and determines if they fit"
  (verify-seq [(base/satisfies 1) (base/satisfies 2)]
              [2 1]
              #{:in-any-order})
  => true

  (verify-seq [(base/satisfies 1) (base/satisfies 2)]
              [2 3 1]
              #{:in-any-order :gaps-ok})
  => true)

^{:refer code.test.checker.collection/contains-map :added "3.0"}
(fact "helper function for the `contains` checker, specifically for validating map structures"))

^{:refer code.test.checker.collection/contains-vector :added "3.0"}
(fact "helper function for the `contains` checker, specifically for validating vector structures"))

^{:refer code.test.checker.collection/contains-set :added "3.0"}
(fact "helper function for the `contains` checker, specifically for validating set structures")

  ((contains-set #{1 2 3}) [1 2 3 4 5])
  => true

  ((contains-set #{1 2 4}) [1 2 3 4 5])
  => false)

^{:refer code.test.checker.collection/contains :added "3.0" :class [:test/checker]}
(fact "checker for maps and vectors"

  ((contains {:a odd? :b even?}) {:a 1 :b 4})
  => true

  ((contains {:a 1 :b even?}) {:a 2 :b 4})
  => false

  ((contains [1 2 3]) [1 2 3 4])
  => true

  ((contains [1 3]) [1 2 3 4])
  => false

  ^:hidden
  ((contains [1 3] :gaps-ok) [1 2 3 4])
  => true

  ((contains [3 1] :gaps-ok) [1 2 3 4])
  => false

  ((contains [3 1] :in-any-order) [1 2 3 4])
  => false

  ((contains [3 1 2] :in-any-order) [1 2 3 4])
  => true

  ((contains [3 1] :in-any-order :gaps-ok) [1 2 3 4])
  => true)

^{:refer code.test.checker.collection/just-map :added "3.0"}
(fact "helper function for the `just` checker, specifically for validating exact map structures"))

^{:refer code.test.checker.collection/just-vector :added "3.0"}
(fact "helper function for the `just` checker, specifically for validating exact vector structures"))

^{:refer code.test.checker.collection/just-set :added "3.0"}
(fact "helper function for the `just` checker, specifically for validating exact set structures")

  ((just-set #{1 2 3}) [1 2 3])
  => true)

^{:refer code.test.checker.collection/just :added "3.0" :class [:test/checker]}
(fact "combination checker for both maps and vectors"

  ((just {:a odd? :b even?}) {:a 1 :b 4})
  => true

  ((just {:a 1 :b even?}) {:a 1 :b 2 :c 3})
  => false

  ((just [1 2 3 4]) [1 2 3 4])
  => true

  ((just [1 2 3]) [1 2 3 4])
  => false

  ((just [3 2 4 1] :in-any-order) [1 2 3 4])
  => true)

^{:refer code.test.checker.collection/contains-in :added "3.0" :class [:test/checker]}
(fact "shorthand for checking nested maps and vectors"

  ((contains-in {:a {:b {:c odd?}}}) {:a {:b {:c 1 :d 2}}})
  => true

  ((contains-in [odd? {:a {:b even?}}]) [3 {:a {:b 4 :c 5}}])
  => true)

^{:refer code.test.checker.collection/just-in :added "3.0" :class [:test/checker]}
(fact "shorthand for exactly checking nested maps and vectors"

  ((just-in {:a {:b {:c odd?}}}) {:a {:b {:c 1 :d 2}}})
  => false

  ((just-in [odd? {:a {:b even?}}]) [3 {:a {:b 4}}])

  ((just-in [odd? {:a {:b even?}}]) [3 {:a {:b 4}}])
  => true)

^{:refer code.test.checker.collection/throws-info :added "3.0" :class [:test/checker]}
(fact "checker that determines if an `ex-info` has been thrown"

  ((throws-info {:a "hello" :b "there"})
   (process/evaluate {:form '(throw (ex-info "hello" {:a "hello" :b "there"}))}))
  => true)

(comment

  (code.manage/import))
