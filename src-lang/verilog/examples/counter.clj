(ns verilog.examples.counter
  (:require [hara.lang :as l]))

(l/script :verilog
  {})

(defn.verilog counter
  "A 4-bit counter with synchronous reset."
  [clk rst out]
  (input clk)
  (input rst)
  (output [3 0] out)
  (reg [3 0] out)
  (always [posedge clk]
    (if rst
      (<= out 0)
      (<= out (+ out 1)))))

(comment
  (l/emit-as :verilog '[counter]))
