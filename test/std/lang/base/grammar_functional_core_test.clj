(ns std.lang.base.grammar-functional-core-test
  (:require [std.lang.base.grammar :as grammar]
            [std.lang.model-annex.spec-haskell :as spec-haskell]
            [std.lang.model-annex.spec-r :as spec-r])
  (:use code.test))

^{:refer std.lang.base.grammar/build-functional-core :added "4.1"}
(fact "functional core is opt-in and host-selected"
  (contains? (set (grammar/ops-list)) :functional-core)
  => true

  (contains? (grammar/build) :letrec)
  => false

  (contains? (grammar/build) :match)
  => false

  (grammar/build-functional-core)
  => (contains {:letrec map?
                :match map?})

  (-> (grammar/to-reserved (grammar/build-functional-core))
      (get 'letrec)
      :op)
  => :letrec

  (-> (grammar/to-reserved (grammar/build-functional-core))
      (get 'letfn)
      :op)
  => :letrec

  (-> (grammar/to-reserved (grammar/build-functional-core))
      (get 'match)
      :op)
  => :match

  spec-haskell/+features+
  => (contains {:letrec map?
                :match map?})

  (contains? spec-r/+features+ :letrec)
  => false

  (contains? spec-r/+features+ :match)
  => false)
