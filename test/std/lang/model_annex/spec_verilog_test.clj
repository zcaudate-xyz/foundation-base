(ns std.lang.model-annex.spec-verilog-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-verilog :refer :all]))

^{:refer std.lang.model-annex.spec-verilog/tf-module :added "4.1"}
(fact "transforms module definitions"
  (tf-module '(defn counter [clk rst out]
                (assign out (cat clk rst))))
  => '(:- "module" counter "(clk, rst, out)" ";"
          (:- "\n")
          (\\ \\ (\| (do (assign out (cat clk rst)))))
          (:- "\nendmodule")))

^{:refer std.lang.model-annex.spec-verilog/tf-assign :added "4.1"}
(fact "transforms assign statements"
  (tf-assign '(assign out in))
  => '(:- "assign" out "=" in ";"))

^{:refer std.lang.model-annex.spec-verilog/tf-initial :added "4.1"}
(fact "transforms initial blocks"
  (tf-initial '(initial (:= ready 1) (delay 10)))
  => '(:- "initial"
          (:- "begin")
          (\\ \\ (\| (do (:= ready 1) (delay 10))))
          (:- "\nend")))

^{:refer std.lang.model-annex.spec-verilog/tf-always :added "4.1"}
(fact "transforms always blocks"
  (tf-always '(always [posedge clk]
                (<= out in)))
  => '(:- "always" "@(posedge clk)"
          (:- "begin")
          (\\ \\ (\| (do (<= out in))))
          (:- "\nend")))

^{:refer std.lang.model-annex.spec-verilog/tf-non-blocking :added "4.1"}
(fact "transforms non-blocking assignments"
  (tf-non-blocking '(<= out in))
  => '(:- out "<=" in ";"))

^{:refer std.lang.model-annex.spec-verilog/tf-blocking :added "4.1"}
(fact "transforms blocking assignments"
  (tf-blocking '(= out in))
  => '(:- out "=" in ";"))

^{:refer std.lang.model-annex.spec-verilog/tf-reg :added "4.1"}
(fact "transforms reg declarations"
  [(tf-reg '(reg counter))
   (tf-reg '(reg [7 0] counter))]
  => ['(:- "reg" "counter;")
      '(:- "reg" "[7:0] counter;")])

^{:refer std.lang.model-annex.spec-verilog/tf-wire :added "4.1"}
(fact "transforms wire declarations"
  [(tf-wire '(wire out))
   (tf-wire '(wire [3 0] out))]
  => ['(:- "wire" "out;")
      '(:- "wire" "[3:0] out;")])

^{:refer std.lang.model-annex.spec-verilog/tf-delay :added "4.1"}
(fact "transforms delay statements"
  (tf-delay '(delay 10))
  => '(:- "#10;"))

^{:refer std.lang.model-annex.spec-verilog/tf-concatenation :added "4.1"}
(fact "transforms concatenation expressions"
  (tf-concatenation '(cat a b c))
  => '(:- "{a, b, c}"))
