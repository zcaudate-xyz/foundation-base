(ns indigo.infra-check-test
  (:require [indigo.infra-check :refer :all])
  (:use code.test))

^{:refer indigo.infra-check/tests-in-wrong-file :added "4.1"}
(fact "checks for tests in the wrong file"
  (tests-in-wrong-file)
  => vector?)

^{:refer indigo.infra-check/to-test-path :added "4.1"}
(fact "converts namespace to test path"
  (to-test-path 'indigo.infra-check-test)
  => "test/indigo/infra_check_test.clj"

  (to-test-path 'std.lib.collection-test)
  => "test/std/lib/collection_test.clj")

^{:refer indigo.infra-check/fix-tests :added "4.1"}
(fact "fix-tests is available"
  (fn? fix-tests)
  => true)

^{:refer indigo.infra-check/rename-tests :added "4.1"}
(fact "rename-tests is available"
  (fn? rename-tests)
  => true)

^{:refer indigo.infra-check/rename-test-var :added "4.1"}
(fact "rename-test-var is available"
  (fn? rename-test-var)
  => true)