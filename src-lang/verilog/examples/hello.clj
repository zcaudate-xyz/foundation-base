(ns verilog.examples.hello
  (:require [hara.lang :as l]))

(l/script :verilog
  {})

(defn.v hello
  "A minimal testbench that prints a message and stops simulation."
  []
  (initial
   ($display "hello from verilog")
   ($finish)))

(comment
  ;; With iverilog/vvp installed:
  ;; (!.v hello)
  ;; => "hello from verilog"
  (l/emit-as :verilog '[hello]))
