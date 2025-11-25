(ns std.block.visual.toy-interpreter-test
  (:use code.test)
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

(defn demo []
  (test-math)
  (test-if)
  (test-nested))


^{:refer std.block.visual.toy-interpreter/block-zip :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/expression? :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/value? :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/symbol-token? :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/list-expression? :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/get-expressions :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/lookup-symbol :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/macro? :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/find-redex-list :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/find-redex :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/eval-primitive :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/substitute :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/reduce-expression :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/highlight-node :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/visualize :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/step :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/run :added "4.0"}
(fact "TODO")

^{:refer std.block.visual.toy-interpreter/run-form :added "4.0"}
(fact "TODO")
