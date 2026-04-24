(ns std.lang.base.preprocess-base-test
  (:use code.test)
  (:require [std.lang.base.preprocess-base :refer :all]))

^{:refer std.lang.base.preprocess-base/macro-form :added "4.1"}
(fact "gets the current macro form"
  (binding [*macro-form* 'hello]
    (macro-form))
  => 'hello)

^{:refer std.lang.base.preprocess-base/macro-opts :added "4.1"}
(fact "gets current macro-opts"
  (binding [*macro-opts* {:a 1}]
    (macro-opts))
  => {:a 1})

^{:refer std.lang.base.preprocess-base/macro-grammar :added "4.1"}
(fact "gets the current grammar"
  (binding [*macro-grammar* {:a 1}]
    (macro-grammar))
  => {:a 1})

^{:refer std.lang.base.preprocess-base/with:macro-opts :added "4.1"}
(fact "bind macro opts"
  (with:macro-opts [{:a 1}]
    (macro-opts))
  => {:a 1})
