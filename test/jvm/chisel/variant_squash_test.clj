(ns jvm.chisel.variant-squash-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant.squash :as sq]))

^{:refer jvm.chisel.variant.squash/k-squash-ref :added "4.1"}
(fact "k-squash-ref matches math.variant k-squash (ties -> 1)"
  (sq/k-squash-ref [1 0 1 1 1 1 0 1 0] 3) => [1 1 0]
  ;; R=4, threshold=2: popcount must be > 2
  (sq/k-squash-ref [1 0 1 0 1 1 0 1] 4)   => [0 1]
  (sq/k-squash-ref [0 0 0 0 1 1 1 1] 4)   => [0 1]
  (sq/k-squash-ref [1 1 1 1 1 1] 3)       => [1 1])

^{:refer jvm.chisel.variant.squash/k-squash-module :added "4.1"}
(fact "k-squash-module is combinational, one popcount-threshold per block"
  (let [fir (ch/emit-firrtl (sq/k-squash-module {:blocks 2 :resolution 3 :name "KSq2x3"}))]
    (.contains fir "module KSq2x3")   => true
    (.contains fir "out : UInt<2>")   => true
    (.contains fir "bits : UInt<6>")  => true
    (count (re-seq #"regreset" fir))  => 0
    (count (re-seq #"add\(" fir))     => 4)) ;; 2 blocks x sum(3 bits)=2 adds

^{:refer jvm.chisel.variant.squash/k-squash-module :added "4.1"}
(fact "k-squash-module emits SystemVerilog"
  (let [sv (ch/emit-system-verilog (sq/k-squash-module {:blocks 2 :resolution 3 :name "KSq2x3SV"}))]
    (.contains sv "module KSq2x3SV") => true))
