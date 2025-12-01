(ns rt.verilog.grammar-test
  (:use code.test)
  (:require [rt.verilog.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

^{:refer rt.verilog.grammar/tf-module :added "4.1"}
(fact "transforms module definition"
  (l/emit-as :verilog
   ['(defn my_module [clk rst out]
       (reg [7 0] count)
       (assign out count)

       (always [posedge clk]
         (if rst
           (<= count 0)
           (<= count (+ count 1)))))])
  => "module my_module (clk, rst, out) ; \n \n  reg [7:0] count;\n  assign out = count ;\n  always @(posedge clk) begin \n    if (rst) begin\n      count <= 0 ;\n    end\n    else begin\n      count <= count + 1 ;\n    end\n     \n  end \nendmodule")

^{:refer rt.verilog.grammar/tf-assign :added "4.1"}
(fact "transforms assign"
  (l/emit-as :verilog
   ['(assign out count)])
  => "assign out = count ;")

^{:refer rt.verilog.grammar/tf-initial :added "4.1"}
(fact "transforms initial block"
  (l/emit-as :verilog
   ['(initial
      (delay 10)
      (:= clk 0))])
  => "initial begin \n  #10;\n  clk = 0 ; \nend")

^{:refer rt.verilog.grammar/tf-always :added "4.1"}
(fact "transforms always block"
  (l/emit-as :verilog
   ['(always [posedge clk]
       (if rst (<= count 0)))])
  => "always @(posedge clk) begin \n  if (rst) begin\n    count <= 0 ;\n  end\n   \nend")

^{:refer rt.verilog.grammar/tf-non-blocking :added "4.1"}
(fact "transforms non-blocking assignment <="
  (l/emit-as :verilog
   ['(<= count 0)])
  => "count <= 0 ;")

^{:refer rt.verilog.grammar/tf-blocking :added "4.1"}
(fact "transforms blocking assignment ="
  (l/emit-as :verilog
   ['(:= clk 0)])
  => "clk = 0 ;")

^{:refer rt.verilog.grammar/tf-reg :added "4.1"}
(fact "transforms reg declaration"
  (l/emit-as :verilog
   ['(reg [7 0] count)])
  => "reg [7:0] count;")

^{:refer rt.verilog.grammar/tf-wire :added "4.1"}
(fact "transforms wire declaration"
  (l/emit-as :verilog
   ['(wire w1)])
  => "wire w1;")

^{:refer rt.verilog.grammar/tf-delay :added "4.1"}
(fact "transforms delay #10"
  (l/emit-as :verilog
   ['(delay 10)])
  => "#10;")

^{:refer rt.verilog.grammar/tf-concatenation :added "4.1"}
(fact "transforms concatenation {a, b}"
  (l/emit-as :verilog
   ['(cat a b)])
  => "{a, b}")
