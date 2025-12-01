(ns rt.circom.grammar-test
  (:use code.test)
  (:require [rt.circom.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer rt.circom.grammar/format-string :added "4.1"}
(fact "formats a string for circom"
  (format-string "hello") => "\"hello\""
  (format-string 123) => 123)

^{:refer rt.circom.grammar/tf-template :added "4.1"}
(fact "transforms template definition"
  (l/emit-as :circom
   ['(template Multiplier [n]
       (signal input a)
       (signal input b)
       (signal output c)
       (<== c (* a b)))])
  => "template Multiplier(n) { signal input a;\nsignal input b;\nsignal output c;\nc <== a * b;; \n}")

^{:refer rt.circom.grammar/tf-component :added "4.1"}
(fact "transforms component instantiation"
  (l/emit-as :circom
   ['(component comp (Multiplier 2))])
  => "component comp = Multiplier(2);")

^{:refer rt.circom.grammar/tf-signal :added "4.1"}
(fact "transforms signal declaration"
  (l/emit-as :circom
   ['(signal input x)
    '(signal output y)
    '(signal intermediate)])
  => "signal input x;\n\nsignal output y;\n\nsignal intermediate;")

^{:refer rt.circom.grammar/tf-var :added "4.1"}
(fact "transforms var declaration"
  (l/emit-as :circom
   ['(var x 10)])
  => "var x = 10;")

^{:refer rt.circom.grammar/tf-pragma :added "4.1"}
(fact "transforms pragma"
  (l/emit-as :circom
   ['(pragma circom "2.0.0")])
  => "pragma circom \"2.0.0\";")

^{:refer rt.circom.grammar/tf-include :added "4.1"}
(fact "transforms include"
  (l/emit-as :circom
   ['(include "circomlib/circuits/poseidon.circom")])
  => "include \"circomlib/circuits/poseidon.circom\";")

^{:refer rt.circom.grammar/tf-main :added "4.1"}
(fact "transforms main component definition"
  (l/emit-as :circom
   ['(main {:public [a]} (Multiplier 2))])
  => "component main {public [a]} = Multiplier(2);")

^{:refer rt.circom.grammar/tf-constraint :added "4.1"}
(fact "transforms constraints"
  (l/emit-as :circom
   ['(<== a b)
    '(==> c d)
    '(=== e f)
    '(<-- g h)
    '(--> i j)])
  => "a <== b;\n\nc ==> d;\n\ne === f;\n\ng <-- h;\n\ni --> j;")

^{:refer rt.circom.grammar/tf-for :added "4.1"}
(fact "transforms for loop"
  (l/emit-as :circom
   ['(for [i 0 10]
       (=== i 5))])
  => "for ( var i = 0 ; i < 10 ; i++ ) { i === 5;; \n}")

(fact "emit if"
  (l/emit-as :circom
   ['(if (> a b)
       (return a)
       (return b))])
  => "if(a > b){\n  return a;\n}\nelse{\n  return b;\n}")
