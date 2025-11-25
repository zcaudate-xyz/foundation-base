(ns std.block.visual.forth-interpreter-test
  (:use code.test)
  (:require [std.block.visual.forth-interpreter :as forth]))


^{:refer std.block.visual.forth-interpreter/block-zip :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/make-vm :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/clear-screen :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/visualize :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/push :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/pop-stack :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/binary-op :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/unary-op :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/run-block :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/exec-word :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/step :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.forth-interpreter/animate :added "4.0"}
(fact "TODO")


(defn demo []
  (println "Starting Forth Demo...")
  (Thread/sleep 1000)
  
  ;; Arithmetic
  (forth/animate "[ 10 20 + 5 * . ]" 500)
  (Thread/sleep 1000)

  ;; Condition
  (forth/animate "[ 10 5 > [ \"Yes\" . ] [ \"No\" . ] if ]" 500))
