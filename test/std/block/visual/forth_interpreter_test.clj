(ns std.block.visual.forth-interpreter-test
  (:require [std.block.visual.forth-interpreter :as forth]))

(defn -main []
  (println "Starting Forth Demo...")
  (Thread/sleep 1000)

  ;; Arithmetic
  (forth/animate "[ 10 20 + 5 * . ]" 500)
  (Thread/sleep 1000)

  ;; Condition
  (forth/animate "[ 10 5 > [ \"Yes\" . ] [ \"No\" . ] if ]" 500))
