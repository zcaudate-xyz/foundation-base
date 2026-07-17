(ns hara.runtime.basic.impl.process-verilog-test
  (:require [clojure.string]
            [hara.lang :as l]
            [hara.runtime.basic.impl.process-verilog :refer :all]
            [std.lib.env :as env]
            [std.lib.os :as os])
  (:use code.test))

^{:refer hara.runtime.basic.impl.process-verilog/transform-form :added "4.1"}
(fact "wraps non-module statements in a testbench module"
  (transform-form '[(reg x)
                    ($display "hello")
                    ($finish)]
                  {})
  => '((defn __hara_tb__ [] (initial (do (reg x)
                                          ($display "hello")
                                          ($finish))))))

^{:refer hara.runtime.basic.impl.process-verilog/transform-form :added "4.1"
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

^{:refer hara.runtime.basic.impl.process-verilog/transform-form :added "4.1"
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
       (defn __hara_tb__ [] (initial (do ($display "done")
                                         ($finish))))))

^{:refer hara.runtime.basic.impl.process-verilog/transform-form :added "4.1"
  :id test-transform-form-verilog-existing-blocks}
(fact "does not wrap existing initial/always blocks in an additional initial"
  (transform-form '[(initial
                      ($display "hello")
                      ($finish))]
                  {})
  => '((defn __hara_tb__ [] (do (initial
                                  ($display "hello")
                                  ($finish))))))

^{:refer hara.runtime.basic.impl.process-verilog/sh-exec-verilog :added "4.1"}
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

^{:refer hara.runtime.basic.impl.process-verilog-test/CANARY-IVERILOG :adopt true :added "4.1"}
(fact "evaluates a simple verilog expression through the runtime"
  (clojure.string/includes?
   (!.verilog ($display "hello verilog"))
   "hello verilog")
  => true)

^{:refer hara.runtime.basic.impl.process-verilog-test/CANARY-IVERILOG :adopt true :added "4.1"
  :id test-canary-iverilog-multi-statement}
(fact "evaluates a multi-statement testbench"
  (let [out (!.verilog
             (do ($display "line one")
                 ($display "line two")
                 ($finish)))]
    [(clojure.string/includes? out "line one")
     (clojure.string/includes? out "line two")])
  => [true true])
