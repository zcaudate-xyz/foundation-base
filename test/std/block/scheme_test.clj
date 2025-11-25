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


^{:refer std.block.scheme/op :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/op-string :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/gen-label :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/emit :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-sequence :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-if :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-define :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-app :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-block :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-expr :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/resolve-labels :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-op-stream :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile-input :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/compile! :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/run-vm :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/print-assembly :added "4.0"}
(fact "TODO")

^{:refer std.block.scheme/visualize-run :added "4.0"}
(fact "TODO")