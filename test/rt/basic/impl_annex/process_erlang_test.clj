(ns rt.basic.impl-annex.process-erlang-test
  (:require [rt.basic.impl-annex.process-erlang :refer :all])
  (:use code.test))

^{:refer rt.basic.impl-annex.process-erlang/default-body-wrap :added "4.1"}
(fact "passes forms through without wrapping"
  (default-body-wrap '[1 2 3])
  => '[1 2 3])

^{:refer rt.basic.impl-annex.process-erlang/default-body-transform :added "4.1"}
(fact "applies return-transform for erlang"
  (default-body-transform '[1 2 3] {})
  => '[[1 2 3]]

  (default-body-transform '[1 2 3] {:bulk true})
  => '[1 2 3])