(ns std.lang.model-annex.spec-ocaml-test
  (:require [std.lang.base.impl :as impl]
            [std.lang.model-annex.spec-ocaml :as spec-ocaml])
  (:use code.test))

^{:refer std.lang.model-annex.spec-ocaml/CANARY :adopt true :added "4.1"}
(fact "basic emit"
  (impl/emit-script '(defn hello [x] x) {:lang :ocaml})
  => "let rec hello x = x"

  (impl/emit-as :ocaml ['(letrec [fib (fn [n]
                                        (match n
                                          0 0
                                          1 1
                                          k (+ (fib (- k 1))
                                               (fib (- k 2)))))]
                           (fib x))])
  => "let rec fib = fun n -> match n with\n  | 0 -> 0\n  | 1 -> 1\n  | k -> fib (k - 1) + fib (k - 2)\nin fib x"

  (impl/emit-as :ocaml ['(match x
                           0 "zero"
                           n [:when (> n 0) "positive"]
                           _ "other")])
  => "match x with\n  | 0 -> \"zero\"\n  | n when n > 0 -> \"positive\"\n  | _ -> \"other\""

  (impl/emit-as :ocaml ['(if true 1 2)])
  => "if true then 1 else 2"

  (impl/emit-as :ocaml ['(fn [x] (+ x 1))])
  => "fun x -> x + 1")


^{:refer std.lang.model-annex.spec-ocaml/emit-raw-str :added "4.1"}
(fact "emits a raw string without processing"
  (spec-ocaml/emit-raw-str [:raw-str "hello"] nil nil)
  => "hello")

^{:refer std.lang.model-annex.spec-ocaml/emit-indent-body :added "4.1"}
(fact "indents the body by 2 spaces"
  (spec-ocaml/emit-indent-body [:indent-body "hello"] nil nil)
  => "  \"hello\"")

^{:refer std.lang.model-annex.spec-ocaml/emit-lines-with :added "4.1"}
(fact "joins forms with a separator"
  (impl/emit-as :ocaml ['(fn [x]
                            (:lines-with ";\n" (+ x 1) (* x 2)))])
  => (fn [s] (clojure.string/includes? s ";\n")))

^{:refer std.lang.model-annex.spec-ocaml/ml-invoke :added "4.1"}
(fact "wraps complex arguments for function application"
  (impl/emit-as :ocaml ['(f (+ 1 2) x)])
  => "f (1 + 2) x")

^{:refer std.lang.model-annex.spec-ocaml/ml-args :added "4.1"}
(fact "emits space-separated OCaml arguments"
  (impl/emit-script '(defn hello [x y] x) {:lang :ocaml})
  => "let rec hello x y = x")

^{:refer std.lang.model-annex.spec-ocaml/parse-match-clauses :added "4.1"}
(fact "parses match clauses into pattern/body pairs"
  (spec-ocaml/parse-match-clauses '(0 "zero" 1 "one"))
  => [{:pattern 0 :body "zero"}
      {:pattern 1 :body "one"}]

  (spec-ocaml/parse-match-clauses '(n [:when (> n 0) "positive"]))
  => [{:pattern 'n :guard '(> n 0) :body "positive"}])

^{:refer std.lang.model-annex.spec-ocaml/body-expr :added "4.1"}
(fact "wraps multi-form bodies in begin/end"
  (spec-ocaml/body-expr '((+ x 1)))
  => '(+ x 1)

  (spec-ocaml/body-expr '((+ x 1) (* x 2)))
  => list?)

^{:refer std.lang.model-annex.spec-ocaml/tf-defn :added "4.1"}
(fact "transforms defn to OCaml let rec"
  (impl/emit-script '(defn hello [x] x) {:lang :ocaml})
  => "let rec hello x = x")

^{:refer std.lang.model-annex.spec-ocaml/tf-match :added "4.1"}
(fact "transforms match expression"
  (impl/emit-as :ocaml ['(match x 0 "zero" _ "other")])
  => "match x with\n  | 0 -> \"zero\"\n  | _ -> \"other\"")

^{:refer std.lang.model-annex.spec-ocaml/tf-if :added "4.1"}
(fact "transforms if expression"
  (impl/emit-as :ocaml ['(if true 1 2)])
  => "if true then 1 else 2")

^{:refer std.lang.model-annex.spec-ocaml/tf-letrec :added "4.1"}
(fact "transforms letrec expression"
  (impl/emit-as :ocaml ['(letrec [x 1] x)])
  => "let rec x = 1\nin x")

^{:refer std.lang.model-annex.spec-ocaml/tf-lambda :added "4.1"}
(fact "transforms lambda expression"
  (impl/emit-as :ocaml ['(fn [x] x)])
  => "fun x -> x")