(ns rt.verilog.grammar-test
  (:use code.test)
  (:require [rt.verilog.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(fact "test verilog module"
  (l/emit-script
   ['(defn my_module [clk rst out]
       (reg [7 0] count)
       (assign out count)

       (always [posedge clk]
         (if rst
           (<= count 0)
           (<= count (+ count 1)))))]
   {:lang :verilog})
  => (str "\n"
          (std.string/join "\n"
                      ["  module my_module (clk, rst, out) ; "
                       " "
                       "  reg [7:0] count;"
                       "  assign out = count ;"
                       "  always @(posedge clk) begin "
                       "    if (rst) begin"
                       "      count <= 0 ;"
                       "    end"
                       "    else begin"
                       "      count <= count + 1 ;"
                       "    end"
                       "     "
                       "  end "
                       "endmodule"])
          "\n"))

(fact "test verilog initial"
  (l/emit-script
   ['(initial
      (delay 10)
      (:= clk 0)
      (delay 10)
      (:= clk 1))]
   {:lang :verilog})
  => (str "\n"
          (std.string/join "\n"
                      ["  initial begin "
                       "  #10;"
                       "  clk = 0 ;"
                       "  #10;"
                       "  clk = 1 ; "
                       "end"])
          "\n"))

(fact "test verilog wire"
  (l/emit-script
   ['(wire w1)]
   {:lang :verilog})
  => "wire w1;")

(fact "test concatenation"
  (l/emit-script
   ['(assign out (cat a b))]
   {:lang :verilog})
  => "assign out = {a, b} ;")
