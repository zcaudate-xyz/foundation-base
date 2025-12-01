(ns std.lang.model.spec-erlang-test
  (:use code.test)
  (:require [std.lang.model.spec-erlang :as spec-erlang]
            [std.lang :as l]
            [std.lib :as h]))

(fact "basic erlang emission"
  (l/emit-as :erlang
    '[(defn add [a b]
      (+ a b))]
    {:book spec-erlang/+book+})
  => "add(A, B) -> A + B.")

(fact "erlang case"
  (l/emit-as :erlang
    '[(defn fact [n]
      (case n
        0 1
        _ (* n (fact (- n 1)))))]
    {:book spec-erlang/+book+})
  => "fact(N) -> case N of 0 -> 1; _ -> N * fact(N - 1) end.")

(fact "erlang data structures"
  (l/emit-as :erlang
    [[1 2 3]]
    {:book spec-erlang/+book+})
  => "[1, 2, 3]"

  (l/emit-as :erlang
    '[(tuple 1 2)]
    {:book spec-erlang/+book+})
  => "{1, 2}"

  (l/emit-as :erlang
    [{:a 1 :b 2}]
    {:book spec-erlang/+book+})
  => "#{a => 1, b => 2}")
