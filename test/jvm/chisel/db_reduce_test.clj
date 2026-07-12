(ns jvm.chisel.db-reduce-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.reduce :as reduce]))

^{:refer jvm.chisel.db.reduce/reduce-ref :added "4.1"}
(fact "reduce-ref computes masked aggregates"
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r11111111 :sum 8)   => 36
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r11111111 :count 8) => 8
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r11111111 :min 8)   => 1
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r11111111 :max 8)   => 8
  ;; mask lanes 1,3,5,7 -> values 2,4,6,8
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r10101010 :sum 8)   => 20
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r10101010 :count 8) => 4
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r10101010 :min 8)   => 2
  (reduce/reduce-ref [1 2 3 4 5 6 7 8] 2r10101010 :max 8)   => 8)

^{:refer jvm.chisel.db.reduce/reduce-module :added "4.1" :id test-reduce-module-1}
(fact "reduce :sum builds an adder tree of the right width"
  (let [fir (ch/emit-firrtl
             (reduce/reduce-module {:lanes 8 :width 8 :op :sum :name "Sum8"}))]
    (.contains fir "module Sum8") => true
    (.contains fir "result : UInt<11>") => true
    (count (re-seq #"add\(" fir)) => 7))

^{:refer jvm.chisel.db.reduce/reduce-module :added "4.1" :id test-reduce-module-2}
(fact "reduce :count/:min/:max elaborate"
  (let [count-fir (ch/emit-firrtl (reduce/reduce-module {:lanes 8 :width 8 :op :count :name "Count8"}))
        min-fir   (ch/emit-firrtl (reduce/reduce-module {:lanes 8 :width 8 :op :min   :name "Min8"}))
        max-fir   (ch/emit-firrtl (reduce/reduce-module {:lanes 8 :width 8 :op :max   :name "Max8"}))]
    (.contains count-fir "module Count8") => true
    (.contains count-fir "result : UInt<4>") => true
    (.contains min-fir "module Min8") => true
    (.contains max-fir "module Max8") => true))

^{:refer jvm.chisel.db.reduce/reduce-module :added "4.1" :id test-reduce-module-3}
(fact "reduce emits SystemVerilog"
  (let [sv (ch/emit-system-verilog
            (reduce/reduce-module {:lanes 8 :width 8 :op :sum :name "Sum8SV"}))]
    (.contains sv "module Sum8SV") => true))
