tahto/model/annex/spec_verilog_test.clj:1:(ns tahto.model.annex.spec-verilog-test
  (:use code.test)
tahto/model/annex/spec_verilog_test.clj:3:  (:require [tahto.model.annex.spec-verilog :refer :all]))

tahto/model/annex/spec_verilog_test.clj:5:^{:refer tahto.model.annex.spec-verilog/tf-module :added "4.1"}
(fact "transforms module definitions"
  (tf-module '(defn counter [clk rst out]
                (assign out (cat clk rst))))
  => '(:- "module" counter "(clk, rst, out)" ";"
          (:- "\n")
          (\\ \\ (\| (do (assign out (cat clk rst)))))
          (:- "\nendmodule")))

tahto/model/annex/spec_verilog_test.clj:14:^{:refer tahto.model.annex.spec-verilog/tf-assign :added "4.1"}
(fact "transforms assign statements"
  (tf-assign '(assign out in))
  => '(:- "assign" out "=" in ";"))

tahto/model/annex/spec_verilog_test.clj:19:^{:refer tahto.model.annex.spec-verilog/tf-initial :added "4.1"}
(fact "transforms initial blocks"
  (tf-initial '(initial (:= ready 1) (delay 10)))
  => '(:- "initial"
          (:- "begin")
          (\\ \\ (\| (do (:= ready 1) (delay 10))))
          (:- "\nend")))

tahto/model/annex/spec_verilog_test.clj:27:^{:refer tahto.model.annex.spec-verilog/tf-always :added "4.1"}
(fact "transforms always blocks"
  (tf-always '(always [posedge clk]
                (<= out in)))
  => '(:- "always" "@(posedge clk)"
          (:- "begin")
          (\\ \\ (\| (do (<= out in))))
          (:- "\nend")))

tahto/model/annex/spec_verilog_test.clj:36:^{:refer tahto.model.annex.spec-verilog/tf-non-blocking :added "4.1"}
(fact "transforms non-blocking assignments"
  (tf-non-blocking '(<= out in))
  => '(:- out "<=" in ";"))

tahto/model/annex/spec_verilog_test.clj:41:^{:refer tahto.model.annex.spec-verilog/tf-blocking :added "4.1"}
(fact "transforms blocking assignments"
  (tf-blocking '(= out in))
  => '(:- out "=" in ";"))

tahto/model/annex/spec_verilog_test.clj:46:^{:refer tahto.model.annex.spec-verilog/tf-reg :added "4.1"}
(fact "transforms reg declarations"
  [(tf-reg '(reg counter))
   (tf-reg '(reg [7 0] counter))]
  => ['(:- "reg" "counter;")
      '(:- "reg" "[7:0] counter;")])

tahto/model/annex/spec_verilog_test.clj:53:^{:refer tahto.model.annex.spec-verilog/tf-wire :added "4.1"}
(fact "transforms wire declarations"
  [(tf-wire '(wire out))
   (tf-wire '(wire [3 0] out))]
  => ['(:- "wire" "out;")
      '(:- "wire" "[3:0] out;")])

tahto/model/annex/spec_verilog_test.clj:60:^{:refer tahto.model.annex.spec-verilog/tf-delay :added "4.1"}
(fact "transforms delay statements"
  (tf-delay '(delay 10))
  => '(:- "#10;"))

tahto/model/annex/spec_verilog_test.clj:65:^{:refer tahto.model.annex.spec-verilog/tf-concatenation :added "4.1"}
(fact "transforms concatenation expressions"
  (tf-concatenation '(cat a b c))
  => '(:- "{a, b, c}"))

tahto/model/annex/spec_verilog_test.clj:70:^{:refer tahto.model.annex.spec-verilog/tf-input :added "4.1"}
(fact "transforms input declarations"
  [(tf-input '(input clk))
   (tf-input '(input [7 0] bus))]
  => ['(:- "input" "clk;")
      '(:- "input" "[7:0] bus;")])

tahto/model/annex/spec_verilog_test.clj:77:^{:refer tahto.model.annex.spec-verilog/tf-output :added "4.1"}
(fact "transforms output declarations"
  [(tf-output '(output out))
   (tf-output '(output [3 0] out))]
  => ['(:- "output" "out;")
      '(:- "output" "[3:0] out;")])

tahto/model/annex/spec_verilog_test.clj:84:^{:refer tahto.model.annex.spec-verilog/tf-inout :added "4.1"}
(fact "transforms inout declarations"
  (tf-inout '(inout [1 0] io))
  => '(:- "inout" "[1:0] io;"))

tahto/model/annex/spec_verilog_test.clj:89:^{:refer tahto.model.annex.spec-verilog/tf-display :added "4.1"}
(fact "transforms $display calls"
  [(tf-display '($display "hello"))
   (tf-display '($display a b))]
  => ['(:- "$display(\"hello\");")
      '(:- "$display(a, b);")])

tahto/model/annex/spec_verilog_test.clj:96:^{:refer tahto.model.annex.spec-verilog/tf-finish :added "4.1"}
(fact "transforms $finish"
  (tf-finish '$finish)
  => '(:- "$finish;"))

tahto/model/annex/spec_verilog_test.clj:101:^{:refer tahto.model.annex.spec-verilog/tf-parameter :added "4.1"}
(fact "transforms parameter declarations"
  (tf-parameter '(parameter WIDTH 8))
  => '(:- "parameter" "WIDTH = 8;"))

tahto/model/annex/spec_verilog_test.clj:106:^{:refer tahto.model.annex.spec-verilog/tf-localparam :added "4.1"}
(fact "transforms localparam declarations"
  (tf-localparam '(localparam MASK 8))
  => '(:- "localparam" "MASK = 8;"))


tahto/model/annex/spec_verilog_test.clj:112:^{:refer tahto.model.annex.spec-verilog/tf-port :added "4.1"}
(fact "transforms port declarations")
