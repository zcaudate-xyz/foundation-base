(ns std.block.visual.llvm-interpreter-test
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
