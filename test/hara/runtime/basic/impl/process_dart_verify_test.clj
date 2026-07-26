tahto/runtime/basic/impl/process_dart_verify_test.clj:1:(ns tahto.runtime.basic.impl.process-dart-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :dart
tahto/runtime/basic/impl/process_dart_verify_test.clj:8:  tahto.runtime.basic.impl.process-dart-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl/process_dart_verify_test.clj:11:^{:refer tahto.runtime.basic.impl.process-dart/CANARY :added "4.0"}
(fact "starts the dart verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/dart]))

(fact:global
 {:skip (not (env/program-exists? "dart"))})

tahto/runtime/basic/impl/process_dart_verify_test.clj:19:^{:refer tahto.runtime.basic.impl.process-dart/!.dt :added "4.0"}
(fact "validates a simple dart expression through the runtime"
  (do (defn.dt test-expr [] (return (+ 1 2 3)))
      (string? (!.dt (test-expr))))
  => true)
