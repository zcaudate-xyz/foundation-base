(ns std.lang.model-annex.spec-lean-test
  (:require [std.lang.base.impl :as impl]
            [std.lang.model-annex.spec-lean :as spec-lean])
  (:use code.test))

^{:refer std.lang.model-annex.spec-lean/CANARY :adopt true :added "4.1"}
(fact "basic emit"
  (impl/emit-script '(defn hello [x] x) {:lang :lean})
  => "def hello x := x"

  (impl/emit-as :lean ['(letrec [fib (fn [n] n)]
                          (fib x))])
  => "let rec fib := fun n => n\nfib x"

  (impl/emit-as :lean ['(match x
                          0 "zero"
                          n [:when (> n 0) "positive"]
                          _ "other")])
  => "match x with\n  | 0 => \"zero\"\n  | n => if n > 0 then \"positive\" else match x with\n    | _ => \"other\""

  (impl/emit-as :lean ['(if true 1 2)])
  => "if true then 1 else 2"

  (impl/emit-as :lean ['(fn [x] (+ x 1))])
  => "fun x => x + 1")
