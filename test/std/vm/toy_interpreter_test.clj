(ns std.vm.toy-interpreter-test
  (:use code.test)
  (:require [std.vm.toy-interpreter :as toy]
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


^{:refer std.vm.toy-interpreter/block-zip :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/expression? :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/value? :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/symbol-token? :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/list-expression? :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/get-expressions :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/lookup-symbol :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/macro? :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/find-redex-list :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/find-redex :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/eval-primitive :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/substitute :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/reduce-expression :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/highlight-node :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/visualize :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/step :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/run :added "4.0"}
(fact "TODO")

^{:refer std.vm.toy-interpreter/run-form :added "4.0"}
(fact "TODO")
