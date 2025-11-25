(ns std.block.visual.jvm-interpreter-test
  (:use code.test)
  (:require [std.block.visual.jvm-interpreter :as jvm]))

(defn demo []
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


^{:refer std.block.visual.jvm-interpreter/block-zip :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/clear-screen :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/highlight :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/block-val :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/filter-valid :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/make-jvm :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/push-frame :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/pop-frame :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/current-frame :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/update-current-frame :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/exec-inst :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/visualize :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.jvm-interpreter/animate :added "4.0"}
(fact "TODO")
