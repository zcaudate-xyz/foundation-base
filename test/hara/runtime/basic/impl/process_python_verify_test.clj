tahto/runtime/basic/impl/process_python_verify_test.clj:1:(ns tahto.runtime.basic.impl.process-python-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :python
tahto/runtime/basic/impl/process_python_verify_test.clj:8:  tahto.runtime.basic.impl.process-python-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl/process_python_verify_test.clj:11:^{:refer tahto.runtime.basic.impl.process-python/CANARY :added "4.0"}
(fact "starts the python verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/python]))

(fact:global
 {:skip (not (env/program-exists? "python3"))})

tahto/runtime/basic/impl/process_python_verify_test.clj:19:^{:refer tahto.runtime.basic.impl.process-python/!.py :added "4.0"}
(fact "validates a simple python expression through the runtime"
  (do (defrun.py test-expr []
        (+ 1 2 3))
      (string? (!.py test-expr)))
  => true)
