(ns rt.circom.grammar-test
  (:use code.test)
  (:require [rt.circom.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

(fact "emit pragma"
  (l/emit-script
   ['(pragma circom "2.0.0")]
   {:lang :circom})
  => "{pragma circom \"2.0.0\";}")

(fact "emit include"
  (l/emit-script
   ['(include "circomlib/circuits/poseidon.circom")]
   {:lang :circom})
  => "{include \"circomlib/circuits/poseidon.circom\";}")

(fact "emit template"
  (l/emit-script
   ['(template Multiplier [n]
       (signal input a)
       (signal input b)
       (signal output c)
       (<== c (* a b)))]
   {:lang :circom})
  => "{\n  template Multiplier(n) { \n  signal input a;\n  signal input b;\n  signal output c;\n  c <== a * b; \n}\n}")

(fact "emit signals"
  (l/emit-script
   ['(signal input x)
    '(signal output y)
    '(signal intermediate)]
   {:lang :circom})
  => "{signal input x;\nsignal output y;\nsignal intermediate;}")

(fact "emit vars"
  (l/emit-script
   ['(var x 10)]
   {:lang :circom})
  => "{var x = 10;}")

(fact "emit constraints"
  (l/emit-script
   ['(<== a b)
    '(==> c d)
    '(=== e f)
    '(<-- g h)
    '(--> i j)]
   {:lang :circom})
  => "{a <== b;\nc ==> d;\ne === f;\ng <-- h;\ni --> j;}")

(fact "emit component instantiation"
  (l/emit-script
   ['(component comp (Multiplier 2))]
   {:lang :circom})
  => "{component comp = Multiplier(2);}")

(fact "emit main component"
  (l/emit-script
   ['(main {:public [a]} (Multiplier 2))]
   {:lang :circom})
  => "{component main {public [a]} = Multiplier(2);}")

(fact "emit for loop"
  (l/emit-script
   ['(for [i 0 10]
       (=== i 5))]
   {:lang :circom})
  => "{\n  for(var i = 0; i < 10; i = i + 1){\n  i === 5;\n}\n}")

(fact "emit if"
  (l/emit-script
   ['(if (> a b)
       (return a)
       (return b))]
   {:lang :circom})
  => "{\n  if(a > b){\n  return a;\n}\nelse{\n  return b;\n}\n}")
