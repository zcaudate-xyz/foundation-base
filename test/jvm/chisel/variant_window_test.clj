(ns jvm.chisel.variant-window-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant.measure :as m]
            [jvm.chisel.variant.window :as win]))

^{:refer jvm.chisel.variant.window/k-window-ref :added "4.1"}
(fact "k-window-ref equals per-window k-measure and matches the recurrence"
  ;; concrete length-4 windows over an 8-bit string
  (win/k-window-ref [1 0 1 1 1 1 0 1] 4)
  => [[3 [0 1 1 1]] [3 [0 1 0 2]] [4 [0 0 0 3]] [3 [0 0 1 2]] [3 [0 1 1 1]]]
  ;; incremental recurrence == direct per-window k-measure (several inputs)
  (let [bits [1 0 1 0 1 1 0 0 1 1]]
    (win/k-window-ref bits 5)
    => (mapv (fn [i] (m/k-measure-ref (subvec (vec bits) i (+ i 5))))
             (range (- (count bits) 5 -1))))
  (let [bits [0 0 1 1 0 1 0]]
    (win/k-window-ref bits 3)
    => (mapv (fn [i] (m/k-measure-ref (subvec (vec bits) i (+ i 3))))
             (range (- (count bits) 3 -1)))))

^{:refer jvm.chisel.variant.window/k-window-module :added "4.1"}
(fact "k-window-module is sequential: shift reg + five counters, sub/add bumps"
  (let [fir (ch/emit-firrtl (win/k-window-module {:length 4 :name "KWin4"}))]
    (.contains fir "module KWin4")  => true
    (.contains fir "p : UInt<3>")   => true
    (.contains fir "k0 : UInt<3>")  => true
    (count (re-seq #"regreset" fir)) => 6  ;; shift reg + p + k0..k3
    (count (re-seq #"add\(" fir))    => 5  ;; p and k0..k3 each: -leave +enter
    (count (re-seq #"sub\(" fir))    => 5))

^{:refer jvm.chisel.variant.window/k-window-module :added "4.1"}
(fact "k-window-module emits SystemVerilog"
  (let [sv (ch/emit-system-verilog (win/k-window-module {:length 4 :name "KWin4SV"}))]
    (.contains sv "module KWin4SV") => true))
