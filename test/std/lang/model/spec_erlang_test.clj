(ns std.lang.model.spec-erlang-test
  (:use code.test)
  (:require [std.lang.model.spec-erlang :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-erlang/to-erlang-var :added "4.1"}
(fact "converts symbols to erlang vars"
  (to-erlang-var 'abc) => 'Abc
  (to-erlang-var 'Abc) => 'Abc
  (to-erlang-var "abc") => "abc")

^{:refer std.lang.model.spec-erlang/capitalize-locals :added "4.1"}
(fact "capitalizes locals in form"
  (capitalize-locals '(+ a b) #{'a 'b})
  => '(+ A B))

^{:refer std.lang.model.spec-erlang/emit-ast :added "4.1"}
(fact "emits ast using erlang context"
  (emit-ast '(+ 1 2)) => "+12")

^{:refer std.lang.model.spec-erlang/wrap-raw :added "4.1"}
(fact "wraps string in erl-raw"
  (wrap-raw "foo") => '(erl-raw "foo"))

^{:refer std.lang.model.spec-erlang/tf-erlang-defn :added "4.1"}
(fact "transforms defn to erlang function definition"
  (l/emit-as :erlang
    ['(defn add [a b]
      (+ a b))])
  => "add(A, B) -> A + B.")

^{:refer std.lang.model.spec-erlang/tf-erlang-case :added "4.1"}
(fact "transforms case"
  (l/emit-as :erlang
    ['(case n
        0 1
        _ (* n (fact (- n 1))))])
  => "case n of 0 -> 1; _ -> n * fact(n - 1) end")

^{:refer std.lang.model.spec-erlang/tf-erlang-tuple :added "4.1"}
(fact "transforms tuple"
  (l/emit-as :erlang
    ['(tuple 1 2)])
  => "{1, 2}")

^{:refer std.lang.model.spec-erlang/emit-erlang-var :added "4.1"}
(fact "emits var assignment"
  (l/emit-as :erlang
    ['(var x 10)])
  => "X = undefined")

^{:refer std.lang.model.spec-erlang/erlang-map-key :added "4.1"}
(fact "custom erlang map key"
  (l/emit-as :erlang
    [{:a 1 "b" 2}])
  => "#{a => 1, \"b\" => 2}")

^{:refer std.lang.model.spec-erlang/emit-erlang-defn :added "4.1"}
(fact "emits erlang function"
  (emit-erlang-defn '(_ add [A B] [(+ A B)]))
  => '(erl-raw "add(A, B) -> +AB."))

^{:refer std.lang.model.spec-erlang/emit-erlang-case :added "4.1"}
(fact "emits erlang case"
  (emit-erlang-case '(_ N ((0 1) (_ (* N (fact (- N 1)))))))
  => '(erl-raw "case N of 0 -> 1; _ -> *Nfact-N1 end"))

^{:refer std.lang.model.spec-erlang/emit-erlang-tuple :added "4.1"}
(fact "emits erlang tuple"
  (emit-erlang-tuple '(_ 1 2))
  => '(erl-raw "{1, 2}"))

(fact "erlang data structures"
  (l/emit-as :erlang
    [[1 2 3]])
  => "[1, 2, 3]")
