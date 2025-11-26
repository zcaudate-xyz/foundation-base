(ns std.vm.scheme-interpreter-test
  (:use code.test)
  (:require [std.vm.scheme-interpreter :as scheme]))

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


^{:refer std.vm.scheme-interpreter/block-zip :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/expression? :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/list-expression? :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/symbol-token? :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/value? :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/get-exprs :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/block-val :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/lambda? :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/lookup :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/substitute :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/apply-lambda :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/reduce-expr :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/find-redex :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/highlight :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/clear-screen :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/visualize :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/step :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/run-step :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/animate :added "4.0"}
(fact "TODO")

^{:refer std.vm.scheme-interpreter/run :added "4.0"}
(fact "TODO")