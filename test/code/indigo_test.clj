(ns indigo-test
  (:use code.test)
  (:require [indigo :refer :all]))

^{:refer indigo/tests-in-wrong-file :added "3.0"}
(fact "checks for tests in the wrong file"

  (tests-in-wrong-file))

^{:refer indigo/to-test-path :added "3.0"}
(fact "converts a given namespace symbol into its corresponding test file path"

  (to-test-path 'indigo-test)
  => "test/indigo_test.clj"

  (to-test-path 'code.dev-test)
  => "test/code/dev_test.clj")

^{:refer indigo/fix-tests :added "3.0"}
(fact "fix tests that are in wrong file")

^{:refer indigo/rename-tests :added "3.0"}
(comment "rename tests given namespaces"

  (rename-tests 'hara.util.transform 'std.lib.stream.xform))

^{:refer indigo/rename-test-var :added "3.0"}
(comment "rename test vars"

  (rename-test-var 'std.lib.class 'class:array:primitive? 'primitive:array?)
  (rename-test-var 'std.lib.return 'ret:resolve 'return-resolve))
