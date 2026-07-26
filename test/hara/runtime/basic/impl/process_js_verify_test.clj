tahto/runtime/basic/impl/process_js_verify_test.clj:1:(ns tahto.runtime.basic.impl.process-js-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :js
tahto/runtime/basic/impl/process_js_verify_test.clj:8:  tahto.runtime.basic.impl.process-js-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl/process_js_verify_test.clj:11:^{:refer tahto.runtime.basic.impl.process-js/CANARY :added "4.0"}
(fact "starts the js verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/js]))

(fact:global
 {:skip (not (env/program-exists? "node"))})

tahto/runtime/basic/impl/process_js_verify_test.clj:19:^{:refer tahto.runtime.basic.impl.process-js/!.js :added "4.0"}
(fact "validates a simple js expression through the runtime"
  (do (defrun.js test-expr []
        (+ 1 2 3))
      (string? (!.js test-expr)))
  => true)
