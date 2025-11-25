(ns std.block.visual.scheme-animation-demo
  (:require [std.block.visual.scheme-interpreter :as scheme]))

(defn -main []
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
