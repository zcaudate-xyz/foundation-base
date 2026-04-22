(ns std.lang.seedgen-test
  (:require [std.lang.seedgen :refer :all])
  (:use code.test))

^{:refer std.lang.seedgen/seedgen-root :added "4.1"}
(fact "runs the public root lookup task without crashing on scalar results"
  (seedgen-root '[xt.sample])

  (seedgen-root '[xt.sample] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items number?
                :results number?
                :total number?}))

^{:refer std.lang.seedgen/seedgen-readforms :added "4.1"}
(fact "returns summary information for public seedgen readforms analysis"
  (seedgen-readforms '[xt.sample.train-002] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items 1
                :results 1
                :total number?}))

^{:refer std.lang.seedgen/seedgen-incomplete :added "4.1"}
(fact "returns summary information for incomplete seedgen tasks"
  (seedgen-incomplete '[xt.sample] {:print {:result true :summary true}})

  (seedgen-incomplete '[xt.sample] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items number?
                :results number?
                :total number?}))


^{:refer std.lang.seedgen/seedgen-list :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-benchlist :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-langremove :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-langadd :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-benchadd :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-benchremove :added "4.1"}
(fact "TODO")
