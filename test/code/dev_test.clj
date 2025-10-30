(ns code.dev-test
  (:use code.test)
  (:require [code.dev :refer :all]))

^{:refer code.dev/tests-in-wrong-file :added "3.0"}
(fact "checks for tests in the wrong file"

  (tests-in-wrong-file))

^{:refer code.dev/to-test-path :added "3.0"}
(fact "converts a given namespace symbol into its corresponding test file path"

  (to-test-path 'code.dev-test)
  => "test/code/dev_test.clj")

^{:refer code.dev/fix-tests :added "3.0"}
(fact "fix tests that are in wrong file")

^{:refer code.dev/rename-tests :added "3.0"}
(comment "rename tests given namespaces"

  (rename-tests 'hara.util.transform 'std.lib.stream.xform))

^{:refer code.dev/rename-test-var :added "3.0"}
(comment "rename test vars"

  (rename-test-var 'std.lib.class 'class:array:primitive? 'primitive:array?)
  (rename-test-var 'std.lib.return 'ret:resolve 'return-resolve))
