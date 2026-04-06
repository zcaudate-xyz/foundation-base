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


^{:refer std.lang.model-annex.spec-lean/emit-raw-str :added "4.1"}
(fact "emits a raw string without processing"
  (spec-lean/emit-raw-str [:raw-str "hello"] nil nil)
  => "hello")

^{:refer std.lang.model-annex.spec-lean/emit-indent-body :added "4.1"}
(fact "indents the body by 2 spaces"
  (spec-lean/emit-indent-body [:indent-body "hello"] nil nil)
  => "  \"hello\"")

^{:refer std.lang.model-annex.spec-lean/lean-args :added "4.1"}
(fact "emits space-separated Lean arguments"
  (impl/emit-script '(defn hello [x y] x) {:lang :lean})
  => "def hello x y := x")

^{:refer std.lang.model-annex.spec-lean/lean-invoke :added "4.1"}
(fact "emits function application with wrapping for complex args"
  (impl/emit-as :lean ['(f (+ 1 2) x)])
  => "f (1 + 2) x")

^{:refer std.lang.model-annex.spec-lean/parse-match-clauses :added "4.1"}
(fact "parses match clauses into pattern/body pairs"
  (spec-lean/parse-match-clauses '(0 "zero" 1 "one"))
  => [{:pattern 0 :body "zero"}
      {:pattern 1 :body "one"}]

  (spec-lean/parse-match-clauses '(n [:when (> n 0) "positive"]))
  => [{:pattern 'n :guard '(> n 0) :body "positive"}])

^{:refer std.lang.model-annex.spec-lean/catch-all-pattern? :added "4.1"}
(fact "returns true for wildcard and unnamespaced symbols"
  (spec-lean/catch-all-pattern? '_) => true
  (spec-lean/catch-all-pattern? 'x) => true
  (spec-lean/catch-all-pattern? 0)  => false)

^{:refer std.lang.model-annex.spec-lean/guarded-body :added "4.1"}
(fact "wraps guarded clause in an if expression"
  (let [clause {:guard '(> n 0) :body "positive"}
        remaining [{:pattern '_ :body "other"}]]
    (spec-lean/guarded-body 'x clause remaining))
  => list?)

^{:refer std.lang.model-annex.spec-lean/match-form :added "4.1"}
(fact "emits a Lean match with expression"
  (impl/emit-as :lean ['(match x 0 "zero" _ "other")])
  => "match x with\n  | 0 => \"zero\"\n  | _ => \"other\"")

^{:refer std.lang.model-annex.spec-lean/tf-defn :added "4.1"}
(fact "transforms defn to Lean def syntax"
  (impl/emit-script '(defn hello [x] x) {:lang :lean})
  => "def hello x := x")

^{:refer std.lang.model-annex.spec-lean/tf-match :added "4.1"}
(fact "transforms match expression"
  (impl/emit-as :lean ['(match x 0 "zero" _ "other")])
  => "match x with\n  | 0 => \"zero\"\n  | _ => \"other\"")

^{:refer std.lang.model-annex.spec-lean/tf-if :added "4.1"}
(fact "transforms if expression"
  (impl/emit-as :lean ['(if true 1 2)])
  => "if true then 1 else 2")

^{:refer std.lang.model-annex.spec-lean/tf-letrec :added "4.1"}
(fact "transforms letrec expression"
  (impl/emit-as :lean ['(letrec [x 1] x)])
  => "let rec x := 1\nx")

^{:refer std.lang.model-annex.spec-lean/tf-lambda :added "4.1"}
(fact "transforms lambda expression"
  (impl/emit-as :lean ['(fn [x] x)])
  => "fun x => x")