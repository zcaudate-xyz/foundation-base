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
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/emit-indent-body :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/emit-lines-with :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/ml-invoke :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/ml-args :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/parse-match-clauses :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/body-expr :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/tf-defn :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/tf-match :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/tf-if :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/tf-letrec :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ocaml/tf-lambda :added "4.1"}
(fact "TODO")