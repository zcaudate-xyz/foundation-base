(ns verilog.examples.adder
  (:require [hara.lang :as l]))

(l/script :verilog
  {})

(defn.verilog adder
  "A clocked 4-bit adder with 5-bit output to capture carry."
  [clk a b out]
  (input clk)
  (input [3 0] a)
  (input [3 0] b)
  (output [4 0] out)
  (reg [4 0] out)
  (always [posedge clk]
    (<= out (+ a b))))

(comment
  (l/emit-as :verilog '[adder]))
