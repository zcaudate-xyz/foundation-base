(ns std.lang.model.spec-julia-test
  (:use code.test)
  (:require [std.lang.model.spec-julia :as julia]
            [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(fact "emits julia defn"
  (l/emit-as :julia ['(defn foo [a b] (+ a b))])
  => "function foo(a,b)
  a + b
end")

(fact "emits julia for loop with range"
  (l/emit-as :julia ['(for:index [i [1 10]] (println i))])
  => "for i in 1:10
  println(i)
end")

(fact "emits julia for loop with range and step"
  (l/emit-as :julia ['(for:index [i [1 10 2]] (println i))])
  => "for i in 1:2:10
  println(i)
end")

(fact "emits julia range with expressions"
  (l/emit-as :julia ['(for:index [i [(+ 1 1) (* 2 5)]] (println i))])
  => "for i in (1 + 1):(2 * 5)
  println(i)
end")

(fact "emits julia dict"
  (l/emit-as :julia ['(dict :a 1 :b 2)])
  => "Dict(\"a\" => 1,\"b\" => 2)")

(fact "emits julia push!"
  (l/emit-as :julia ['(push! arr 1)])
  => "push!(arr,1)")

(fact "end-to-end julia execution"
  ^:unchecked
  (if (not-empty (try (h/sh "which" "julia" {:wrap false}) (catch Throwable _ nil)))
    (do
      (let [code (l/emit-as :julia ['(defn add [a b] (+ a b)) '(println (add 2 3))])
            _ (spit "test.jl" code)
            out (h/sh "julia" "test.jl" {:wrap false})]
        (str/trim out) => "5"))
    "skipped"))
