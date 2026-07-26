tahto/runtime/basic/impl/process_verilog_test.clj:1:(ns tahto.runtime.basic.impl.process-verilog-test
  (:require [clojure.string]
            [tahto.core :as l]
tahto/runtime/basic/impl/process_verilog_test.clj:4:            [tahto.runtime.basic.impl.process-verilog :refer :all]
            [std.lib.env :as env]
            [std.lib.os :as os])
  (:use code.test))

tahto/runtime/basic/impl/process_verilog_test.clj:9:^{:refer tahto.runtime.basic.impl.process-verilog/transform-form :added "4.1"}
(fact "wraps non-module statements in a testbench module"
  (transform-form '[(reg x)
                    ($display "hello")
                    ($finish)]
                  {})
tahto/runtime/basic/impl/process_verilog_test.clj:15:  => '((defn __tahto_tb__ [] (initial (do (reg x)
                                          ($display "hello")
                                          ($finish))))))

tahto/runtime/basic/impl/process_verilog_test.clj:19:^{:refer tahto.runtime.basic.impl.process-verilog/transform-form :added "4.1"
  :id test-transform-form-verilog-modules}
(fact "leaves pure module definitions at the top level"
  (transform-form '[(defn counter [clk]
                      (reg out)
                      (always [posedge clk]
                              (<= out clk)))]
                  {})
  => '((defn counter [clk]
         (reg out)
         (always [posedge clk]
                 (<= out clk)))))

tahto/runtime/basic/impl/process_verilog_test.clj:32:^{:refer tahto.runtime.basic.impl.process-verilog/transform-form :added "4.1"
  :id test-transform-form-verilog-mixed-program}
(fact "separates module definitions from executable statements"
  (transform-form '[(defn counter [clk]
                      (reg out)
                      (always [posedge clk]
                              (<= out clk)))
                    ($display "done")]
                  {})
  => '((defn counter [clk]
         (reg out)
         (always [posedge clk]
                 (<= out clk)))
tahto/runtime/basic/impl/process_verilog_test.clj:45:       (defn __tahto_tb__ [] (initial (do ($display "done")
                                         ($finish))))))

tahto/runtime/basic/impl/process_verilog_test.clj:48:^{:refer tahto.runtime.basic.impl.process-verilog/transform-form :added "4.1"
  :id test-transform-form-verilog-existing-blocks}
(fact "does not wrap existing initial/always blocks in an additional initial"
  (transform-form '[(initial
                      ($display "hello")
                      ($finish))]
                  {})
tahto/runtime/basic/impl/process_verilog_test.clj:55:  => '((defn __tahto_tb__ [] (do (initial
                                  ($display "hello")
                                  ($finish))))))

tahto/runtime/basic/impl/process_verilog_test.clj:59:^{:refer tahto.runtime.basic.impl.process-verilog/sh-exec-verilog :added "4.1"}
(fact "compiles with iverilog and runs with vvp"
  (let [calls (atom [])]
    (with-redefs [os/sh (fn [opts]
                          (swap! calls conj opts)
                          :proc)
                  os/sh-wait (fn [& _] nil)
                  os/sh-output (fn [_]
                                 (if (= 1 (count @calls))
                                   {:exit 0 :out "" :err ""}
                                   {:exit 0 :out "hello\n" :err ""}))]
      [(sh-exec-verilog ["iverilog"] "module tb; endmodule" {:root "/tmp"})
       (count @calls)
       (first (:args (first @calls)))
       (first (:args (second @calls)))]))
  => ["hello" 2 "iverilog" "vvp"])

(fact:global {:skip (not (env/program-exists? "iverilog")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(l/script- :verilog
  {:runtime :twostep :test-mode true})

tahto/runtime/basic/impl/process_verilog_test.clj:81:^{:refer tahto.runtime.basic.impl.process-verilog-test/CANARY-IVERILOG :adopt true :added "4.1"}
(fact "evaluates a simple verilog expression through the runtime"
  (clojure.string/includes?
   (!.verilog ($display "hello verilog"))
   "hello verilog")
  => true)

tahto/runtime/basic/impl/process_verilog_test.clj:88:^{:refer tahto.runtime.basic.impl.process-verilog-test/CANARY-IVERILOG :adopt true :added "4.1"
  :id test-canary-iverilog-multi-statement}
(fact "evaluates a multi-statement testbench"
  (let [out (!.verilog
             (do ($display "line one")
                 ($display "line two")
                 ($finish)))]
    [(clojure.string/includes? out "line one")
     (clojure.string/includes? out "line two")])
  => [true true])
