(ns jvm.chisel.emit-test
  (:use code.test)
  (:require [clojure.string :as str]
            [jvm.chisel.emit :as emit]
            [jvm.chisel.db.reduce :as red]))

^{:refer jvm.chisel.emit/system-verilog :added "4.1"}
(fact "system-verilog delegates emission for a named builder"
  (let [sv (emit/system-verilog
            (red/reduce-module {:lanes 4 :width 8 :op :sum :name "EmitWrapper"}))]
    (str/includes? sv "module EmitWrapper") => true
    (str/includes? sv "output [9:0] io_result") => true))
