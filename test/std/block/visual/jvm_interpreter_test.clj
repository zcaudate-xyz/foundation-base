(ns std.block.visual.jvm-interpreter-test
  (:require [std.block.visual.jvm-interpreter :as jvm]))

(defn -main []
  (println "Starting JVM Demo...")
  (Thread/sleep 1000)

  (jvm/animate "
(class Factorial
  (method main
    [
     [iconst_5]
     [istore_1]  ;; n = 5
     [iconst_1]
     [istore_2]  ;; res = 1

     ;; Loop Unrolled for Toy Demo (no labels/goto yet)
     [iload_2]
     [iload_1]
     [imul]
     [istore_2]  ;; res = res * n

     [iload_1]
     [iconst_1]
     [isub]
     [istore_1]  ;; n = n - 1

     [iload_2]
     [iload_1]
     [imul]
     [istore_2]  ;; res = res * n

     [iload_2]
     [ireturn]
    ]))" 500))
