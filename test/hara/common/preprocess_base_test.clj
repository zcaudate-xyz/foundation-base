tahto/common/preprocess_base_test.clj:1:(ns tahto.common.preprocess-base-test
  (:use code.test)
tahto/common/preprocess_base_test.clj:3:  (:require [tahto.common.preprocess-base :refer :all]))

tahto/common/preprocess_base_test.clj:5:^{:refer tahto.common.preprocess-base/macro-form :added "4.1"}
(fact "gets the current macro form"
  (binding [*macro-form* 'hello]
    (macro-form))
  => 'hello)

tahto/common/preprocess_base_test.clj:11:^{:refer tahto.common.preprocess-base/macro-opts :added "4.1"}
(fact "gets current macro-opts"
  (binding [*macro-opts* {:a 1}]
    (macro-opts))
  => {:a 1})

tahto/common/preprocess_base_test.clj:17:^{:refer tahto.common.preprocess-base/macro-grammar :added "4.1"}
(fact "gets the current grammar"
  (binding [*macro-grammar* {:a 1}]
    (macro-grammar))
  => {:a 1})

tahto/common/preprocess_base_test.clj:23:^{:refer tahto.common.preprocess-base/with:macro-opts :added "4.1"}
(fact "bind macro opts"
  (with:macro-opts [{:a 1}]
    (macro-opts))
  => {:a 1})
