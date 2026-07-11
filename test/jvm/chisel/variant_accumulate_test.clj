(ns jvm.chisel.variant-accumulate-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant.accumulate :as acc]))

^{:refer jvm.chisel.variant.accumulate/k-accumulate-ref :added "4.1"}
(fact "k-accumulate-ref matches math.variant k-accumulate"
  (acc/k-accumulate-ref [1 0 1 1 1 1 0 1])
  => [[1 [0 0 0 0]] [1 [0 0 1 0]] [2 [0 1 1 0]] [3 [0 1 1 1]]
      [4 [0 1 1 2]] [5 [0 1 1 3]] [5 [0 1 2 3]] [6 [0 2 2 3]]]
  ;; each prefix equals the block k-measure of that prefix
  (let [bits [1 0 1 0 1 1 0 0 1]]
    (acc/k-accumulate-ref bits)
    => (mapv (fn [i] ((requiring-resolve 'jvm.chisel.variant.measure/k-measure-ref)
                      (subvec (vec bits) 0 (inc i))))
             (range (count bits)))))

^{:refer jvm.chisel.variant.accumulate/k-accumulate-module :added "4.1"}
(fact "k-accumulate-module is sequential with six regs and right widths"
  (let [fir (ch/emit-firrtl (acc/k-accumulate-module {:n-max 16 :name "KAcc16"}))]
    (.contains fir "module KAcc16") => true
    (.contains fir "p : UInt<5>")    => true
    (.contains fir "k0 : UInt<5>")   => true
    (>= (count (re-seq #"regreset" fir)) 7) => true  ;; p, k0..k3, prev, started
    (pos? (count (re-seq #"add\(" fir)))     => true)) ;; popcount + transition adders

^{:refer jvm.chisel.variant.accumulate/k-accumulate-module :added "4.1"}
(fact "k-accumulate-module emits SystemVerilog"
  (let [sv (ch/emit-system-verilog (acc/k-accumulate-module {:n-max 16 :name "KAcc16SV"}))]
    (.contains sv "module KAcc16SV") => true))
