(ns indigo.infra-check-test
  (:require [indigo.infra-check :refer :all])
  (:use code.test))

^{:refer indigo.infra-check/tests-in-wrong-file :added "4.1"}
(fact "TODO")

^{:refer indigo.infra-check/to-test-path :added "4.1"}
(fact "TODO")

^{:refer indigo.infra-check/fix-tests :added "4.1"}
(fact "TODO")

^{:refer indigo.infra-check/rename-tests :added "4.1"}
(fact "TODO")

^{:refer indigo.infra-check/rename-test-var :added "4.1"}
(fact "TODO")