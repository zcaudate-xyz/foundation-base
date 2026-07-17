(ns jvm.chisel.db.pipeline-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.pipeline :as pipe]
            [jvm.chisel.db.schedule :as sched]))

(def ^:private scan-reduce
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:gte 20] [:lte 80]]}
            {:op :reduce :reduce-op :sum}]})

(def ^:private scan-hash-reduce
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:gte 20] [:lte 80]]}
            {:op :hash :buckets 16 :k 0x9E}
            {:op :reduce :reduce-op :sum}]})

^{:refer jvm.chisel.db.pipeline/pipeline-module :added "4.1" :id test-pipeline-module-1}
(fact "scan->reduce elaborates: gating mask + right-width adder tree"
  (let [fir (ch/emit-firrtl (pipe/pipeline-module (assoc scan-reduce :name "SR8")))]
    (.contains fir "module SR8") => true
    (.contains fir "matchMask : UInt<8>") => true
    (.contains fir "result : UInt<11>") => true
    (.contains fir "c0") => true
    (.contains fir "c1") => true
    (count (re-seq #"add\(" fir)) => 7))

^{:refer jvm.chisel.db.pipeline/pipeline-module :added "4.1" :id test-pipeline-module-2}
(fact "scan->hash->reduce exposes per-lane buckets and a result"
  (let [fir (ch/emit-firrtl (pipe/pipeline-module (assoc scan-hash-reduce :name "SHR8")))]
    (.contains fir "module SHR8") => true
    (.contains fir "buckets[0]") => true
    (.contains fir "buckets[7]") => true
    (.contains fir "result : UInt<11>") => true))

^{:refer jvm.chisel.db.pipeline/pipeline-module :added "4.1" :id test-pipeline-module-3}
(fact "composed pipeline emits SystemVerilog"
  (let [sv (ch/emit-system-verilog (pipe/pipeline-module (assoc scan-hash-reduce :name "SHR8SV")))]
    (.contains sv "module SHR8SV") => true
    (.contains sv "matchMask") => true
    (.contains sv "result") => true))

^{:refer jvm.chisel.db.pipeline/pipeline-ref :added "4.1"}
(fact "pipeline-ref delegates to schedule/run-plan"
  (let [input {:values [10 20 30 40 50 60 70 80] :validMask 2r11111111}]
    (pipe/pipeline-ref scan-hash-reduce input) => (sched/run-plan scan-hash-reduce input)))
