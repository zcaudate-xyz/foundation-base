(ns jvm.chisel-test
  (:use code.test)
  (:require [jvm.chisel :as ch]))

^{:refer jvm.chisel/uint :added "4.1"}
(fact "uint builds a Chisel UInt type"
  (instance? chisel3.UInt (ch/uint 8)) => true
  (instance? chisel3.UInt (ch/uint))   => true)

^{:refer jvm.chisel/sint :added "4.1"}
(fact "sint builds a Chisel SInt type"
  (instance? chisel3.SInt (ch/sint 16)) => true)

^{:refer jvm.chisel/bool :added "4.1"}
(fact "bool builds a Chisel Bool type"
  (instance? chisel3.Bool (ch/bool)) => true)

^{:refer jvm.chisel/bits :added "4.1"}
(fact "bits builds a Chisel Bits type"
  (instance? chisel3.Bits (ch/bits 32)) => true)

^{:refer jvm.chisel/vec :added "4.1"}
(fact "vec builds a Chisel Vec type"
  (instance? chisel3.Vec (ch/vec 8 (ch/uint 8))) => true)

^{:refer jvm.chisel/bundle :added "4.1"}
(fact "bundle builds a Chisel Record"
  (instance? chisel3.Record (ch/bundle [[:a (ch/uint 8)] [:b (ch/bool)]])) => true)

^{:refer jvm.chisel/u :added "4.1"}
(fact "u builds an unsigned literal"
  (instance? chisel3.UInt (ch/u 5 4)) => true)

^{:refer jvm.chisel/s :added "4.1"}
(fact "s builds a signed literal"
  (instance? chisel3.SInt (ch/s -3 8)) => true)

^{:refer jvm.chisel/b :added "4.1"}
(fact "b builds a boolean literal"
  (instance? chisel3.Bool (ch/b true))  => true
  (instance? chisel3.Bool (ch/b false)) => true)
