(ns jvm.chisel.db.scan-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.scan :as scan]))

^{:refer jvm.chisel.db.scan/scan-ref :added "4.1"}
(fact "scan-ref computes the match bitmask"
  (scan/scan-ref [10 20 30 40 50 60 70 80] 2r11111111 [[:eq 30]])   => 2r00000100
  (scan/scan-ref [10 20 30 40 50 60 70 80] 2r11111111 [[:gte 50]])  => 2r11110000
  (scan/scan-ref [10 20 30 40 50 60 70 80] 2r00001111 [[:gte 50]])  => 2r00000000
  (scan/scan-ref [10 20 30 40 50 60 70 80] 2r11111111 [[:gte 20] [:lte 50]]) => 2r00011110)

^{:refer jvm.chisel.db.scan/scan-module :added "4.1" :id test-scan-module-1}
(fact "scan-module elaborates a multi-predicate scan to FIRRTL"
  (let [fir (ch/emit-firrtl
             (scan/scan-module {:lanes 8 :width 8 :preds [[:eq 0] [:gte 1]] :name "Scan8"}))]
    (.contains fir "module Scan8") => true
    (.contains fir "matchMask : UInt<8>") => true
    (.contains fir "values[0]") => true
    (.contains fir "values[7]") => true
    (.contains fir "geq(") => true
    (.contains fir "connect io.matchMask") => true))

^{:refer jvm.chisel.db.scan/scan-module :added "4.1" :id test-scan-module-2}
(fact "scan-module emits SystemVerilog"
  (let [sv (ch/emit-system-verilog
            (scan/scan-module {:lanes 8 :width 8 :preds [[:eq 0]] :name "Scan8SV"}))]
    (.contains sv "module Scan8SV") => true
    (.contains sv "matchMask") => true))
