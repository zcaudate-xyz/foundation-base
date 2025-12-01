(ns rt.circom.grammar-test
  (:use code.test)
  (:require [rt.circom.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

(fact "emit pragma"
  (l/emit-as :circom
   ['(pragma circom "2.0.0")])
  => "pragma circom \"2.0.0\";")

(fact "emit include"
  (l/emit-as :circom
   ['(include "circomlib/circuits/poseidon.circom")])
  => "include \"circomlib/circuits/poseidon.circom\";")

(fact "emit template"
  (l/emit-as :circom
   ['(template Multiplier [n]
       (signal input a)
       (signal input b)
       (signal output c)
       (<== c (* a b)))])
  => "template Multiplier(n) { \n  signal input a;\n  signal input b;\n  signal output c;\n  c <== (a * b);\n}")

(fact "emit signals"
  (l/emit-as :circom
   ['(signal input x)
    '(signal output y)
    '(signal intermediate)])
  => "signal input x;\n\nsignal output y;\n\nsignal intermediate;")

(fact "emit vars"
  (l/emit-as :circom
   ['(var x 10)])
  => "var x = 10;")

(fact "emit constraints"
  (l/emit-as :circom
   ['(<== a b)
    '(==> c d)
    '(=== e f)
    '(<-- g h)
    '(--> i j)])
  => "a <== b;\n\nc ==> d;\n\ne === f;\n\ng <-- h;\n\ni --> j;")

(fact "emit component instantiation"
  (l/emit-as :circom
   ['(component comp (Multiplier 2))])
  => "component comp = Multiplier(2);")

(fact "emit main component"
  (l/emit-as :circom
   ['(main {:public [a]} (Multiplier 2))])
  => "component main {public [a]} = Multiplier(2);")

(fact "emit for loop"
  (l/emit-as :circom
   ['(for [i 0 10]
       (=== i 5))])
  => "for(var i = 0; i < 10; i++){\n  i === 5;\n}")

(fact "emit if"
  (l/emit-as :circom
   ['(if (> a b)
       (return a)
       (return b))])
  => "if(a > b){\n  return a;\n}\nelse{\n  return b;\n}")


^{:refer rt.circom.grammar/format-string :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-template :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-component :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-signal :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-var :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-pragma :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-include :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-main :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-constraint :added "4.1"}
(fact "TODO")

^{:refer rt.circom.grammar/tf-for :added "4.1"}
(fact "TODO")