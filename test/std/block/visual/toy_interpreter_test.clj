(ns std.block.visual.toy-interpreter-test
  (:require [std.block.visual.toy-interpreter :as toy]
            [std.block.construct :as construct]))

(defn test-math []
  (println "\n=== Math Test ===")
  (toy/run-form '(+ 1 (* 2 3))))

(defn test-if []
  (println "\n=== If Test ===")
  (toy/run-form '(if true 10 20)))

(defn test-nested []
  (println "\n=== Nested Test ===")
  (toy/run-form '(+ (* 2 2) (* 3 3))))

(defn -main []
  (test-math)
  (test-if)
  (test-nested))
