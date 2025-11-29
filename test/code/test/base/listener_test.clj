(ns code.test.base.listener-test
  (:use code.test)
  (:require [code.test.base.listener :refer :all]))

^{:refer code.test.base.listener/summarise-verify :added "3.0"}
(fact "extract the comparison into a valid format ")

^{:refer code.test.base.listener/summarise-evaluate :added "3.0"}
(fact "extract the form into a valid format")

^{:refer code.test.base.listener/form-printer :added "3.0"}
(fact "prints out result for each form")

^{:refer code.test.base.listener/check-printer :added "3.0"}
(fact "prints out result per check")

^{:refer code.test.base.listener/form-error-accumulator :added "3.0"}
(fact "accumulator for thrown errors")

^{:refer code.test.base.listener/check-error-accumulator :added "3.0"}
(fact "accumulator for errors on checks")

^{:refer code.test.base.listener/fact-printer :added "3.0"}
(fact "prints out results after every fact")

^{:refer code.test.base.listener/fact-accumulator :added "3.0"}
(fact "accumulator for fact results")

^{:refer code.test.base.listener/bulk-printer :added "3.0"}
(fact "prints out the end summary")

^{:refer code.test.base.listener/install-listeners :added "3.0"}
(fact "installs all listeners")
