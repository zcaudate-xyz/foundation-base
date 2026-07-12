(ns jvm.chisel.emit-test
  "SystemVerilog emission + Verilator lint for verified `jvm.chisel` modules.

   Skipped unless `verilator` is on PATH. Emits to `.build/lint/` (gitignored)."
  (:use code.test)
  (:require [clojure.string :as str]
            [std.lib.env :as env]
            [jvm.chisel.emit :as emit]
            [jvm.chisel.db.reduce :as red]
            [jvm.chisel.db.pipeline :as pipe]
            [jvm.chisel.variant.accumulate :as acc]))

(fact:global
 {:skip (not (env/program-exists? "verilator"))})

(def ^:private outdir ".build/lint")

^{:refer jvm.chisel.emit/spit-system-verilog :added "4.1"}
(fact "reduce :sum emits lint-clean 11-bit SystemVerilog (carries not truncated)"
  (let [b    (red/reduce-module {:lanes 8 :width 8 :op :sum :name "EmitReduceSum"})
        sv   (emit/system-verilog b)
        path (str outdir "/EmitReduceSum.sv")]
    ;; the :sum widen fix is visible in the netlist: 11-bit result, leaves
    ;; zero-extended by 3 bits before addition.
    (str/includes? sv "module EmitReduceSum") => true
    (str/includes? sv "[10:0]")               => true
    (str/includes? sv "{3'h0,")               => true
    (emit/spit-system-verilog b path)
    (:exit (emit/lint! path "EmitReduceSum")) => 0))

^{:refer jvm.chisel.emit/lint! :added "4.1" :id test-lint!-1}
(fact "sequential k-accumulate emits lint-clean SystemVerilog"
  (let [b    (acc/k-accumulate-module {:n-max 16 :name "EmitKAcc"})
        path (str outdir "/EmitKAcc.sv")]
    (str/includes? (emit/system-verilog b) "module EmitKAcc") => true
    (emit/spit-system-verilog b path)
    (:exit (emit/lint! path "EmitKAcc")) => 0))

^{:refer jvm.chisel.emit/lint! :added "4.1" :id test-lint!-2}
(fact "full composed pipeline emits lint-clean SystemVerilog"
  (let [b    (pipe/pipeline-module
              {:width 8 :lanes 8 :name "EmitPipeFull"
               :stages [{:op :scan :preds [[:gte 5] [:lte 250]]}
                        {:op :bloom-probe :bits-count 16 :ks [0x9E 0x5D]}
                        {:op :hash :buckets 16 :k 0x9E}
                        {:op :reduce :reduce-op :sum}]})
        path (str outdir "/EmitPipeFull.sv")]
    (str/includes? (emit/system-verilog b) "module EmitPipeFull") => true
    (emit/spit-system-verilog b path)
    (:exit (emit/lint! path "EmitPipeFull")) => 0))
