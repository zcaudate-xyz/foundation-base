(ns hara.model.annex.spec-verilog-test
  (:use code.test)
  (:require [hara.model.annex.spec-verilog :refer :all]))

^{:refer hara.model.annex.spec-verilog/tf-module :added "4.1"}
(fact "transforms module definitions"
  (tf-module '(defn counter [clk rst out]
                (assign out (cat clk rst))))
  => '(:- "module" counter "(clk, rst, out)" ";"
          (:- "\n")
          (\\ \\ (\| (do (assign out (cat clk rst)))))
          (:- "\nendmodule")))

^{:refer hara.model.annex.spec-verilog/tf-assign :added "4.1"}
(fact "transforms assign statements"
  (tf-assign '(assign out in))
  => '(:- "assign" out "=" in ";"))

^{:refer hara.model.annex.spec-verilog/tf-initial :added "4.1"}
(fact "transforms initial blocks"
  (tf-initial '(initial (:= ready 1) (delay 10)))
  => '(:- "initial"
          (:- "begin")
          (\\ \\ (\| (do (:= ready 1) (delay 10))))
          (:- "\nend")))

^{:refer hara.model.annex.spec-verilog/tf-always :added "4.1"}
(fact "transforms always blocks"
  (tf-always '(always [posedge clk]
                (<= out in)))
  => '(:- "always" "@(posedge clk)"
          (:- "begin")
          (\\ \\ (\| (do (<= out in))))
          (:- "\nend")))

^{:refer hara.model.annex.spec-verilog/tf-non-blocking :added "4.1"}
(fact "transforms non-blocking assignments"
  (tf-non-blocking '(<= out in))
  => '(:- out "<=" in ";"))

^{:refer hara.model.annex.spec-verilog/tf-blocking :added "4.1"}
(fact "transforms blocking assignments"
  (tf-blocking '(= out in))
  => '(:- out "=" in ";"))

^{:refer hara.model.annex.spec-verilog/tf-reg :added "4.1"}
(fact "transforms reg declarations"
  [(tf-reg '(reg counter))
   (tf-reg '(reg [7 0] counter))]
  => ['(:- "reg" "counter;")
      '(:- "reg" "[7:0] counter;")])

^{:refer hara.model.annex.spec-verilog/tf-wire :added "4.1"}
(fact "transforms wire declarations"
  [(tf-wire '(wire out))
   (tf-wire '(wire [3 0] out))]
  => ['(:- "wire" "out;")
      '(:- "wire" "[3:0] out;")])

^{:refer hara.model.annex.spec-verilog/tf-delay :added "4.1"}
(fact "transforms delay statements"
  (tf-delay '(delay 10))
  => '(:- "#10;"))

^{:refer hara.model.annex.spec-verilog/tf-concatenation :added "4.1"}
(fact "transforms concatenation expressions"
  (tf-concatenation '(cat a b c))
  => '(:- "{a, b, c}"))

^{:refer hara.model.annex.spec-verilog/tf-input :added "4.1"}
(fact "transforms input declarations"
  [(tf-input '(input clk))
   (tf-input '(input [7 0] bus))]
  => ['(:- "input" "clk;")
      '(:- "input" "[7:0] bus;")])

^{:refer hara.model.annex.spec-verilog/tf-output :added "4.1"}
(fact "transforms output declarations"
  [(tf-output '(output out))
   (tf-output '(output [3 0] out))]
  => ['(:- "output" "out;")
      '(:- "output" "[3:0] out;")])

^{:refer hara.model.annex.spec-verilog/tf-inout :added "4.1"}
(fact "transforms inout declarations"
  (tf-inout '(inout [1 0] io))
  => '(:- "inout" "[1:0] io;"))

^{:refer hara.model.annex.spec-verilog/tf-display :added "4.1"}
(fact "transforms $display calls"
  [(tf-display '($display "hello"))
   (tf-display '($display a b))]
  => ['(:- "$display(\"hello\")")
      '(:- "$display(a, b)")])

^{:refer hara.model.annex.spec-verilog/tf-finish :added "4.1"}
(fact "transforms $finish"
  (tf-finish '$finish)
  => '(:- "$finish;"))

^{:refer hara.model.annex.spec-verilog/tf-parameter :added "4.1"}
(fact "transforms parameter declarations"
  (tf-parameter '(parameter WIDTH 8))
  => '(:- "parameter" "WIDTH = 8;"))

^{:refer hara.model.annex.spec-verilog/tf-localparam :added "4.1"}
(fact "transforms localparam declarations"
  (tf-localparam '(localparam MASK 8))
  => '(:- "localparam" "MASK = 8;"))
