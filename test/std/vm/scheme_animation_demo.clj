(ns std.vm.scheme-animation-demo
  (:use code.test)
  (:require [std.vm.scheme-interpreter :as scheme]))

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
