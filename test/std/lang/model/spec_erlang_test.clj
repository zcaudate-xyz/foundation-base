(ns std.lang.model.spec-erlang-test
  (:use code.test)
  (:require [std.lang.model.spec-erlang :as spec-erlang]
            [std.lang :as l]
            [std.lib :as h]))

(fact "basic erlang emission"
  (l/emit-script
    '(defn add [a b]
      (+ a b))
    {:lang :erlang :book spec-erlang/+book+})
  => "add(A, B) -> A + B.")

(fact "erlang case"
  (l/emit-script
    '(defn fact [n]
      (case n
        0 1
        _ (* n (fact (- n 1)))))
    {:lang :erlang :book spec-erlang/+book+})
  => "fact(N) -> case N of 0 -> 1; _ -> N * fact(N - 1) end.")

(fact "erlang data structures"
  (l/emit-script
    [1 2 3]
    {:lang :erlang :book spec-erlang/+book+})
  => "[1, 2, 3]"

  (l/emit-script
    '(tuple 1 2)
    {:lang :erlang :book spec-erlang/+book+})
  => "{1, 2}"

  (l/emit-script
    {:a 1 :b 2}
    {:lang :erlang :book spec-erlang/+book+})
  => "#{a => 1, b => 2}")


^{:refer std.lang.model.spec-erlang/to-erlang-var :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/capitalize-locals :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/emit-ast :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/wrap-raw :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/tf-erlang-defn :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/emit-erlang-defn :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/tf-erlang-case :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/emit-erlang-case :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/tf-erlang-tuple :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/emit-erlang-tuple :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/emit-erlang-var :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-erlang/erlang-map-key :added "4.1"}
(fact "TODO")