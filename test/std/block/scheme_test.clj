(ns std.block.scheme-test
  (:require [std.block.scheme :as scheme]))

(defn test-arithmetic []
  (println "\n=== Arithmetic ===")
  (scheme/visualize-run "(+ 1 (* 2 3))"))

(defn test-logic []
  (println "\n=== Logic (If) ===")
  (scheme/visualize-run "(if (= 1 1) 100 200)"))

(defn test-define []
  (println "\n=== Define ===")
  (scheme/visualize-run "(begin (define x 10) (+ x 5))"))

(defn -main []
  (test-arithmetic)
  (test-logic)
  (test-define))
