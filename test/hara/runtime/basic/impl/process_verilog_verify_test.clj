(ns hara.runtime.basic.impl.process-verilog-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :verilog
  hara.runtime.basic.impl.process-verilog-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-verilog/CANARY :added "4.0"}
(fact "starts the verilog verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/verilog]))

(fact:global
 {:skip (not (env/program-exists? "iverilog"))})

^{:refer hara.runtime.basic.impl.process-verilog/!.verilog :added "4.0"}
(fact "validates a simple verilog expression through the runtime"
  (do (defn.verilog test-expr [] (initial ($display "hello") ($finish)))
      (string? (!.verilog (test-expr))))
  => true)
