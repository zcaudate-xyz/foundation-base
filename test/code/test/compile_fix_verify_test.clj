(ns code.test.compile-fix-verify-test
  (:use code.test))

(fact "Verify filename lost in let"
  (let [a 1]
    a => 2))

(fact "Verify line number lost on simple check"
  (let [b 2]
    (+ 1 b) => []))
