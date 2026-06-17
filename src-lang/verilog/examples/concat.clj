(ns verilog.examples.concat
  (:require [hara.lang :as l]))

(l/script :verilog
  {})

(defn.verilog byte-merge
  "Concatenates two 4-bit nibbles into an 8-bit bus."
  [hi lo out]
  (input [3 0] hi)
  (input [3 0] lo)
  (output [7 0] out)
  (assign out (cat hi lo)))

(comment
  (l/emit-as :verilog '[byte-merge]))
