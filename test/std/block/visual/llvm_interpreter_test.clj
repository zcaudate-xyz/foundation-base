(ns std.block.visual.llvm-interpreter-test
  (:use code.test)
  (:require [std.block.visual.llvm-interpreter :as llvm]))

(defn -main []
  (println "Starting LLVM Demo...")
  (Thread/sleep 1000)

  (llvm/animate "
(define @main (i32)
  (entry
    [%cnt = add i32 0 5]
    [br loop])

  (loop
    [%cond = icmp eq i32 %cnt 0]
    [br i1 %cond exit body])

  (body
    [%cnt = sub i32 %cnt 1]
    [br loop])

  (exit
    [ret i32 %cnt]))
" 500))


^{:refer std.block.visual.llvm-interpreter/block-zip :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/clear-screen :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/highlight :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/block-val :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/instruction? :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/get-instructions :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/make-vm :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/resolve-val :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/update-reg :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/jump :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/next-inst :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/exec-inst :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/get-current-inst-block :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/highlight-ip :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/visualize :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.llvm-interpreter/animate :added "4.0"}
(fact "TODO")
