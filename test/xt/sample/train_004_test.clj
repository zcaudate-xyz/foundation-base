(ns xt.sample.train-004-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :python
  {:runtime :basic})

^{:refer xt.lang.common-spec/x:return-eval :added "4.1"
  :seedgen/lang {:python {:suppress true}}}
(fact "suppresses incomplete checks for python"
  "TODO")

^{:refer xt.lang.common-spec/x:return-wrap :added "4.1"}
(fact "still reports missing python coverage without suppression"
  "TODO")
