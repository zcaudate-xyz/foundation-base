(ns std.lang.model-annex.spec-haskell-test
  (:require [std.lang.base.impl :as impl]
            [std.lang.model-annex.spec-haskell :as spec-haskell])
  (:use code.test))

^{:refer std.lang.model-annex.spec-haskell/CANARY :adopt true :added "4.1"}
(fact "basic emit"

  (impl/emit-script '(defn hello [x] x) {:lang :haskell})
  => "hello x = x"

  (impl/emit-as :haskell ['(letrec [fib (fn [n]
                                          (match n
                                            0 0
                                            1 1
                                            k (+ (fib (- k 1))
                                                 (fib (- k 2)))))]
                             (fib x))])
  => "let\n  fib = \\ n -> case n of\n    0 -> 0\n    1 -> 1\n    k -> fib (k - 1) + fib (k - 2)\nin fib x"

  (impl/emit-as :haskell ['(if true 1 2)])
  => "if true then 1 else 2"

  (impl/emit-as :haskell ['(match x
                             0 "zero"
                             n [:when (> n 0) "positive"]
                             _ "other")])
  => "case x of\n  0 -> \"zero\"\n  n | n > 0 -> \"positive\"\n  _ -> \"other\""

  (impl/emit-as :haskell ['(fn [x] (+ x 1))])
  => "\\ x -> x + 1"

  (impl/emit-as :haskell ['(cons 1 [2 3])])
  => "1 : [2,3]"

  (impl/emit-as :haskell ['(concat [1] [2])])
  => "[1] ++ [2]"

  (impl/emit-as :haskell ['(do (print "a") (print "b"))])
  => "do\n  print \"a\"\n  print \"b\""

  (impl/emit-as :haskell ['[1 2 3]])
  => "[1,2,3]")

(fact "types emit"
  (impl/emit-as :haskell ['[:> List Int]])
  => "List Int"

  (impl/emit-as :haskell ['[:> Map String Int]])
  => "Map String Int")

^{:refer std.lang.model-annex.spec-haskell/haskell-typesystem :added "4.1"}
(fact "defn with type hint"

  (impl/emit-script '(defn ^Int add [^Int x ^Int y] (+ x y)) {:lang :haskell})
  => "add :: Int -> Int -> Int\nadd x y = x + y")



^{:refer std.lang.model-annex.spec-haskell/haskell-vector :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/emit-raw-str :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/emit-indent-body :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/tf-defn :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/parse-match-clauses :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/tf-match :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/tf-if :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/tf-letrec :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/tf-lambda :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/tf-do :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-haskell/haskell-args :added "4.1"}
(fact "TODO")


^{:refer std.lang.model-annex.spec-haskell/haskell-invoke :added "4.1"}
(fact "TODO")