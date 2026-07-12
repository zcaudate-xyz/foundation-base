(ns jvm.chisel.variant-measure-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant.measure :as m]))

^{:refer jvm.chisel.variant.measure/k-measure-ref :added "4.1"}
(fact "k-measure-ref matches math.variant k-measure"
  (m/k-measure-ref [1 0 1])       => [2 [0 1 1 0]]
  (m/k-measure-ref [1 0 1 0 1 1]) => [4 [0 2 2 1]]
  (m/k-measure-ref [1])           => [1 [0 0 0 0]]
  (m/k-measure-ref [0 0 0])       => [0 [2 0 0 0]]
  (m/k-measure-ref [1 1 1 1])     => [4 [0 0 0 3]])

^{:refer jvm.chisel.variant.measure/c-measure-ref :added "4.1"}
(fact "c-measure-ref matches math.variant c-measure (c = k2)"
  (m/c-measure-ref [1 0 1])       => [2 1]
  (m/c-measure-ref [1 0 1 0 1 1]) => [4 2]
  (m/c-measure-ref [0 0 0 0])     => [0 0]
  (m/c-measure-ref [1 1 1 1])     => [4 0])

^{:refer jvm.chisel.variant.measure/k-measure-module :added "4.1" :id test-k-measure-module-1}
(fact "k-measure-module builds popcount + four transition adder trees"
  (let [fir (ch/emit-firrtl (m/k-measure-module {:n 8 :name "KMeasure8"}))]
    (.contains fir "module KMeasure8") => true
    (.contains fir "p : UInt<4>")      => true
    (.contains fir "k0 : UInt<4>")     => true
    ;; popcount(8) = 7 adds ; four counters x sum(7 pairs) = 4 x 6 = 24 ; total 31
    (count (re-seq #"add\(" fir))      => 31))

^{:refer jvm.chisel.variant.measure/c-measure-module :added "4.1"}
(fact "c-measure-module elaborates with the right widths"
  (let [fir (ch/emit-firrtl (m/c-measure-module {:n 8 :name "CMeasure8"}))]
    (.contains fir "module CMeasure8") => true
    (.contains fir "p : UInt<4>")      => true
    (.contains fir "c : UInt<4>")      => true))

^{:refer jvm.chisel.variant.measure/k-measure-module :added "4.1" :id test-k-measure-module-2}
(fact "k-measure-module emits SystemVerilog"
  (let [sv (ch/emit-system-verilog (m/k-measure-module {:n 8 :name "KMeasure8SV"}))]
    (.contains sv "module KMeasure8SV") => true))
