(ns std.vm.toy-interpreter-test
  (:use code.test)
  (:require [std.vm.toy-interpreter :as toy]
            [std.block.construct :as construct]
            [std.block.parse :as parse]
            [std.block.base :as base]
            [std.lib.zip :as zip]))

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
(fact "creates a zipper for toy code"
  (let [root (construct/block '(+ 1 2))]
    (toy/block-zip root) => map?))

^{:refer std.vm.toy-interpreter/expression? :added "4.0"}
(fact "checks if a block is an expression"
  (toy/expression? (construct/token 1)) => true
  (toy/expression? (construct/space)) => false)

^{:refer std.vm.toy-interpreter/value? :added "4.0"}
(fact "checks if a block is a value"
  (toy/value? (construct/token 1)) => true
  (toy/value? (construct/block '(+ 1 2))) => false)

^{:refer std.vm.toy-interpreter/symbol-token? :added "4.0"}
(fact "checks if a block is a symbol token"
  (toy/symbol-token? (construct/token 'a)) => true
  (toy/symbol-token? (construct/token 1)) => false)

^{:refer std.vm.toy-interpreter/list-expression? :added "4.0"}
(fact "checks if a block is a list expression"
  (toy/list-expression? (construct/block '(1 2))) => true)

^{:refer std.vm.toy-interpreter/get-expressions :added "4.0"}
(fact "gets expression children from a node"
  (let [node (construct/block '(+ 1 2))]
    (count (toy/get-expressions node)) => 3))

^{:refer std.vm.toy-interpreter/lookup-symbol :added "4.0"}
(fact "looks up a symbol in the environment"
  (toy/lookup-symbol '+ {}) => fn?
  (toy/lookup-symbol 'x {'x 10}) => 10)

^{:refer std.vm.toy-interpreter/macro? :added "4.0"}
(fact "checks if a symbol is a macro"
  (toy/macro? 'if) => true
  (toy/macro? '+) => false)

^{:refer std.vm.toy-interpreter/find-redex-list :added "4.0"}
(fact "finds redex in a list"
  (let [root (construct/block '(+ 1 (* 2 3)))
        z (toy/block-zip root)
        rz (toy/find-redex-list z)]
    (base/block-value (first (toy/get-expressions (zip/right-element rz)))) => '*))

^{:refer std.vm.toy-interpreter/find-redex :added "4.0"}
(fact "finds the next reducible expression"
  (let [root (construct/block '(+ 1 (* 2 3)))
        z (toy/block-zip root)
        rz (toy/find-redex z)]
    (base/block-value (first (toy/get-expressions (zip/right-element rz)))) => '*))

^{:refer std.vm.toy-interpreter/eval-primitive :added "4.0"}
(fact "evaluates a primitive function"
  (toy/eval-primitive + [1 2]) => 3)

^{:refer std.vm.toy-interpreter/substitute :added "4.0"}
(fact "substitutes a symbol in a body"
  (let [body (construct/block '(+ x 1))
        res (toy/substitute body 'x (construct/token 10))]
    (base/block-value (second (toy/get-expressions res))) => 10))

^{:refer std.vm.toy-interpreter/reduce-expression :added "4.0"}
(fact "reduces an expression"
  (let [root (construct/block '(+ 1 2))
        z (toy/block-zip root)
        nz (toy/reduce-expression z {})]
    (base/block-value (zip/right-element nz)) => 3))

^{:refer std.vm.toy-interpreter/highlight-node :added "4.0"}
(fact "highlights the current redex"
  (let [root (construct/block '(+ 1 2))
        z (toy/block-zip root)]
    (toy/highlight-node z) => map?))

^{:refer std.vm.toy-interpreter/visualize :added "4.0"}
(fact "visualizes the current state"
  (let [root (construct/block '(+ 1 2))
        z (toy/block-zip root)]
    (toy/visualize z) => any))

^{:refer std.vm.toy-interpreter/step :added "4.0"}
(fact "steps through execution"
  (let [root (construct/block '(+ 1 (* 2 3)))
        z (toy/block-zip root)
        next-z (toy/step z {})]
    (base/block-value (last (toy/get-expressions (zip/right-element next-z)))) => 6))

^{:refer std.vm.toy-interpreter/run :added "4.0"}
(fact "runs from string"
  (toy/run "(+ 1 2)") => any)

^{:refer std.vm.toy-interpreter/run-form :added "4.0"}
(fact "runs from form"
  (toy/run-form '(+ 1 2)) => any)
