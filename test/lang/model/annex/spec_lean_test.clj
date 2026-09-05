(ns lang.model.annex.spec-lean-test
  (:require [lang.core.impl :as impl]
            [lang.model.annex.spec-lean :as spec-lean])
  (:use code.test))

^{:refer lang.model.annex.spec-lean/CANARY :adopt true :added "4.1"}
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

^{:refer lang.model.annex.spec-lean/emit-raw-str :added "4.1"}
(fact "emits raw strings")

^{:refer lang.model.annex.spec-lean/emit-indent-body :added "4.1"}
(fact "emits indented bodies")

^{:refer lang.model.annex.spec-lean/lean-args :added "4.1"}
(fact "formats lean function arguments")

^{:refer lang.model.annex.spec-lean/lean-invoke :added "4.1"}
(fact "emits lean invocations")

^{:refer lang.model.annex.spec-lean/parse-match-clauses :added "4.1"}
(fact "parses pattern match clauses")

^{:refer lang.model.annex.spec-lean/catch-all-pattern? :added "4.1"}
(fact "checks catch-all patterns")

^{:refer lang.model.annex.spec-lean/guarded-body :added "4.1"}
(fact "builds guarded bodies")

^{:refer lang.model.annex.spec-lean/match-form :added "4.1"}
(fact "builds match forms")

^{:refer lang.model.annex.spec-lean/tf-defn :added "4.1"}
(fact "transforms function definitions")

^{:refer lang.model.annex.spec-lean/tf-match :added "4.1"}
(fact "transforms pattern match expressions")

^{:refer lang.model.annex.spec-lean/tf-if :added "4.1"}
(fact "transforms if expressions")

^{:refer lang.model.annex.spec-lean/tf-letrec :added "4.1"}
(fact "transforms letrec expressions")

^{:refer lang.model.annex.spec-lean/tf-lambda :added "4.1"}
(fact "transforms lambda expressions")
