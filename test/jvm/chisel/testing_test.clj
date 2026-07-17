(ns jvm.chisel.testing-test
  (:use code.test)
  (:require [jvm.chisel.testing :as ct]))

^{:refer jvm.chisel.testing/pack :added "4.1"}
(fact "pack uses lane zero as the least-significant bit"
  (ct/pack []) => 0
  (ct/pack [1 0 1 1]) => 2r1101
  (ct/pack [0 1 0 0 1]) => 2r10010)

^{:refer jvm.chisel.testing/unpack :added "4.1"}
(fact "unpack returns exactly the requested low bits in lane order"
  (ct/unpack 2r1101 4) => [1 0 1 1]
  (ct/unpack 2r1101 2) => [1 0]
  (ct/unpack 0 0) => [])

^{:refer jvm.chisel.testing/popcount-int :added "4.1"}
(fact "popcount-int counts set bits in a machine integer"
  (ct/popcount-int 0) => 0
  (ct/popcount-int 2r10110100) => 4
  (ct/popcount-int -1) => 64)
