(ns circom.core-test
  (:require [circom.core]
            [hara.lang :as l]
            [hara.lang.impl :as impl]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :circom
  {:runtime :twostep})

(defn.circom Multiplier2
  "a multiplier template used by the runtime test"
  {:added "4.1"}
  []
  (signal input a)
  (signal input b)
  (signal output c)
  (<== c (* a b)))

(fact:global
 {:skip (not (env/program-exists? "circom"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer circom.core/Multiplier :added "4.1"}
(fact "emits a simple multiplier template"
  (impl/emit-script '(template Multiplier []
                       (signal input a)
                       (signal input b)
                       (signal output c)
                       (<== c (* a b)))
                    {:lang :circom})
  => string?)

^{:refer circom.core/AND :added "4.1"}
(fact "emits an AND gate template"
  (impl/emit-script '(template AND []
                       (signal input a)
                       (signal input b)
                       (signal output out)
                       (<== out (* a b)))
                    {:lang :circom})
  => string?)

^{:refer circom.core/IsZero :added "4.1"}
(fact "emits constraints for the IsZero template"
  (impl/emit-script '(template IsZero []
                       (signal input in)
                       (signal output out)
                       (signal inv)
                       (=== (* in out) 0)
                       (=== (+ (* in inv) out) 1))
                    {:lang :circom})
  => string?)

^{:refer circom.core/SumOfSquares :added "4.1"}
(fact "emits local var declarations in a template"
  (impl/emit-script '(template SumOfSquares []
                       (signal input a)
                       (signal input b)
                       (signal output c)
                       (var aa (* a a))
                       (var bb (* b b))
                       (<== c (+ aa bb)))
                    {:lang :circom})
  => string?)

^{:refer circom.core/Fibonacci :added "4.1"}
(fact "emits a for loop in a template"
  (impl/emit-script '(template Fibonacci [n]
                       (signal input in)
                       (signal output out)
                       (var a 0)
                       (var b 1)
                       (var i 0)
                       (for [i 0 n]
                         (var t a)
                         (:= a b)
                         (:= b (+ t b)))
                       (<== out a))
                    {:lang :circom})
  => string?)

^{:refer circom.core-test/Multiplier2 :added "4.1"}
(fact "compiles a circom template through the twostep runtime"
  (str (!.circom (-/Multiplier2)))
  => #(clojure.string/includes? % "Everything went okay"))
