(ns std.vm.scheme-interpreter-test
  (:use code.test)
  (:require [std.vm.scheme-interpreter :as scheme]
            [std.block.parse :as parse]
            [std.block.construct :as construct]
            [std.block.base :as base]
            [std.lib.zip :as zip]))

(defn test-fact []
  (println "\n=== Factorial (Recursive) ===")
  (scheme/run "
(begin
  (define fact
    (lambda (n)
      (if (= n 0)
          1
          (* n (fact (- n 1))))))
  (fact 3))"))

(defn test-lambda []
  (println "\n=== Lambda Application ===")
  (scheme/run "((lambda (x) (+ x x)) 10)"))

(defn demo []
  (test-lambda)
  (test-fact))

(defn demo []
  (println "Starting Animation Demo (Factorial 3)...")
  (Thread/sleep 1000)
  (scheme/animate "
(begin
  (define fact
    (lambda (n)
      (if (= n 0)
          1
          (* n (fact (- n 1))))))
  (fact 3))" 500))


^{:refer std.vm.scheme-interpreter/block-zip :added "4.0"}
(fact "creates a zipper for Scheme code"
  (let [root (parse/parse-string "(+ 1 2)")]
    (scheme/block-zip root) => map?))

^{:refer std.vm.scheme-interpreter/expression? :added "4.0"}
(fact "checks if a block is an expression"
  (scheme/expression? (construct/token 1)) => true
  (scheme/expression? (construct/space)) => false)

^{:refer std.vm.scheme-interpreter/list-expression? :added "4.0"}
(fact "checks if a block is a list expression"
  (scheme/list-expression? (parse/parse-string "(1)")) => true)

^{:refer std.vm.scheme-interpreter/symbol-token? :added "4.0"}
(fact "checks if a block is a symbol token"
  (scheme/symbol-token? (construct/token 'a)) => true
  (scheme/symbol-token? (construct/token 1)) => false)

^{:refer std.vm.scheme-interpreter/value? :added "4.0"}
(fact "checks if a block is a value"
  (scheme/value? (construct/token 1)) => true
  (scheme/value? (parse/parse-string "(lambda (x) x)")) => true
  (scheme/value? (parse/parse-string "(+ 1 2)")) => false)

^{:refer std.vm.scheme-interpreter/get-exprs :added "4.0"}
(fact "gets expression children from a node"
  (let [node (parse/parse-string "(+ 1 2)")]
    (count (scheme/get-exprs node)) => 3))

^{:refer std.vm.scheme-interpreter/block-val :added "4.0"}
(fact "extracts value from a block"
  (scheme/block-val (construct/token 1)) => 1
  (scheme/block-val 1) => 1)

^{:refer std.vm.scheme-interpreter/lambda? :added "4.0"}
(fact "checks if a block is a lambda form"
  (scheme/lambda? (parse/parse-string "(lambda (x) x)")) => true
  (scheme/lambda? (parse/parse-string "(+ 1 2)")) => false)

^{:refer std.vm.scheme-interpreter/lookup :added "4.0"}
(fact "looks up a symbol in the environment"
  (scheme/lookup '+) => fn?
  (scheme/lookup 'unknown) => nil)

^{:refer std.vm.scheme-interpreter/substitute :added "4.0"}
(fact "substitutes a parameter with an argument"
  (let [body (construct/token 'x)
        arg (construct/token 10)
        res (scheme/substitute body 'x arg)]
    (scheme/block-val res) => 10))

^{:refer std.vm.scheme-interpreter/apply-lambda :added "4.0"}
(fact "applies a lambda to arguments"
  (let [lambda (parse/parse-string "(lambda (x) (+ x 1))")
        args [(construct/token 10)]
        res (scheme/apply-lambda lambda args)]
    (scheme/block-val (first (base/block-children res))) => '+))

^{:refer std.vm.scheme-interpreter/reduce-expr :added "4.0"}
(fact "reduces an expression"
  (let [root (parse/parse-string "(+ 1 2)")
        z (scheme/block-zip root)
        z (scheme/find-redex z) ;; points to (+ 1 2)
        nz (scheme/reduce-expr z)]
    (scheme/block-val (zip/right-element nz)) => 3))

^{:refer std.vm.scheme-interpreter/find-redex :added "4.0"}
(fact "finds the next reducible expression"
  (let [root (parse/parse-string "(+ (+ 1 2) 3)")
        z (scheme/block-zip root)
        rz (scheme/find-redex z)]
    ;; Should find (+ 1 2)
    (let [expr (zip/right-element rz)]
      (scheme/block-val (first (scheme/get-exprs expr))) => '+
      (scheme/block-val (second (scheme/get-exprs expr))) => 1)))

^{:refer std.vm.scheme-interpreter/highlight :added "4.0"}
(fact "highlights the current redex"
  (let [root (parse/parse-string "(+ 1 2)")
        z (scheme/block-zip root)]
    (scheme/highlight z) => map?))

^{:refer std.vm.scheme-interpreter/clear-screen :added "4.0"}
(fact "clears the screen"
  (scheme/clear-screen) => any)

^{:refer std.vm.scheme-interpreter/visualize :added "4.0"}
(fact "visualizes the current state"
  (let [root (parse/parse-string "(+ 1 2)")
        z (scheme/block-zip root)]
    (scheme/visualize z) => any))

^{:refer std.vm.scheme-interpreter/step :added "4.0"}
(fact "steps through the reduction"
  (let [root (parse/parse-string "(+ 1 2)")
        z (scheme/block-zip root)
        next-z (scheme/step z)]
    (scheme/block-val (zip/right-element next-z)) => 3))

^{:refer std.vm.scheme-interpreter/run-step :added "4.0"}
(fact "runs a single step"
  (scheme/run-step "(+ 1 2)") => map?)

^{:refer std.vm.scheme-interpreter/animate :added "4.0"}
(fact "animates execution"
  (scheme/animate "(+ 1 2)" 0) => any)

^{:refer std.vm.scheme-interpreter/run :added "4.0"}
(fact "runs the full execution"
  (scheme/run "(+ 1 2)") => any)


