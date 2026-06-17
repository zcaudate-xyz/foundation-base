(ns verilog.examples.hello
  (:require [hara.lang :as l]))

(l/script :verilog
  {})

(defn.verilog hello
  "A minimal testbench that prints a message and stops simulation."
  []
  (initial
   ($display "hello from verilog")
   ($finish)))

(comment
  ;; Print the generated module source:
  ;; (l/ptr-print hello)
  ;;
  ;; With iverilog/vvp installed:
  ;; (!.verilog ($display "hello from verilog"))
  ;; => "hello from verilog"
  (l/emit-as :verilog '[hello]))
