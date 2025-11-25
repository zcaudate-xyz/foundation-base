(ns std.block.visual.scheme-interpreter-test
  (:require [std.block.visual.scheme-interpreter :as scheme]))

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

(defn -main []
  (test-lambda)
  (test-fact))
