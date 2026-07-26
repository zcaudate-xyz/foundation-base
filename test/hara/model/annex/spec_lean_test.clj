tahto/model/annex/spec_lean_test.clj:1:(ns tahto.model.annex.spec-lean-test
  (:require [tahto.core.impl :as impl]
tahto/model/annex/spec_lean_test.clj:3:            [tahto.model.annex.spec-lean :as spec-lean])
  (:use code.test))

tahto/model/annex/spec_lean_test.clj:6:^{:refer tahto.model.annex.spec-lean/CANARY :adopt true :added "4.1"}
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

tahto/model/annex/spec_lean_test.clj:27:^{:refer tahto.model.annex.spec-lean/emit-raw-str :added "4.1"}
(fact "emits raw strings")

tahto/model/annex/spec_lean_test.clj:30:^{:refer tahto.model.annex.spec-lean/emit-indent-body :added "4.1"}
(fact "emits indented bodies")

tahto/model/annex/spec_lean_test.clj:33:^{:refer tahto.model.annex.spec-lean/lean-args :added "4.1"}
(fact "formats lean function arguments")

tahto/model/annex/spec_lean_test.clj:36:^{:refer tahto.model.annex.spec-lean/lean-invoke :added "4.1"}
(fact "emits lean invocations")

tahto/model/annex/spec_lean_test.clj:39:^{:refer tahto.model.annex.spec-lean/parse-match-clauses :added "4.1"}
(fact "parses pattern match clauses")

tahto/model/annex/spec_lean_test.clj:42:^{:refer tahto.model.annex.spec-lean/catch-all-pattern? :added "4.1"}
(fact "checks catch-all patterns")

tahto/model/annex/spec_lean_test.clj:45:^{:refer tahto.model.annex.spec-lean/guarded-body :added "4.1"}
(fact "builds guarded bodies")

tahto/model/annex/spec_lean_test.clj:48:^{:refer tahto.model.annex.spec-lean/match-form :added "4.1"}
(fact "builds match forms")

tahto/model/annex/spec_lean_test.clj:51:^{:refer tahto.model.annex.spec-lean/tf-defn :added "4.1"}
(fact "transforms function definitions")

tahto/model/annex/spec_lean_test.clj:54:^{:refer tahto.model.annex.spec-lean/tf-match :added "4.1"}
(fact "transforms pattern match expressions")

tahto/model/annex/spec_lean_test.clj:57:^{:refer tahto.model.annex.spec-lean/tf-if :added "4.1"}
(fact "transforms if expressions")

tahto/model/annex/spec_lean_test.clj:60:^{:refer tahto.model.annex.spec-lean/tf-letrec :added "4.1"}
(fact "transforms letrec expressions")

tahto/model/annex/spec_lean_test.clj:63:^{:refer tahto.model.annex.spec-lean/tf-lambda :added "4.1"}
(fact "transforms lambda expressions")
