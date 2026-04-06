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
(fact "emits haskell vectors and type signatures"
  (impl/emit-as :haskell ['[1 2 3]])
  => "[1,2,3]"

  (impl/emit-as :haskell ['[:> List Int]])
  => "List Int")

^{:refer std.lang.model-annex.spec-haskell/emit-raw-str :added "4.1"}
(fact "emits a raw string without processing"
  (spec-haskell/emit-raw-str [:raw-str "hello world"] nil nil)
  => "hello world")

^{:refer std.lang.model-annex.spec-haskell/emit-indent-body :added "4.1"}
(fact "indents body output by 2 spaces"
  (impl/emit-as :haskell ['(letrec [x 1] x)])
  => (fn [s] (clojure.string/includes? s "  ")))

^{:refer std.lang.model-annex.spec-haskell/tf-defn :added "4.1"}
(fact "emits Haskell function definition"
  (impl/emit-script '(defn hello [x] x) {:lang :haskell})
  => "hello x = x")

^{:refer std.lang.model-annex.spec-haskell/parse-match-clauses :added "4.1"}
(fact "parses match clauses into pattern/body pairs"
  (spec-haskell/parse-match-clauses '(0 "zero" 1 "one"))
  => [{:pattern 0 :body "zero"}
      {:pattern 1 :body "one"}]

  (spec-haskell/parse-match-clauses '(n [:when (> n 0) "positive"]))
  => [{:pattern 'n :guard '(> n 0) :body "positive"}])

^{:refer std.lang.model-annex.spec-haskell/tf-match :added "4.1"}
(fact "transforms match expression"
  (impl/emit-as :haskell ['(match x 0 "zero" _ "other")])
  => "case x of\n  0 -> \"zero\"\n  _ -> \"other\"")

^{:refer std.lang.model-annex.spec-haskell/tf-if :added "4.1"}
(fact "transforms if expression"
  (impl/emit-as :haskell ['(if true 1 2)])
  => "if true then 1 else 2")

^{:refer std.lang.model-annex.spec-haskell/tf-letrec :added "4.1"}
(fact "transforms letrec expression"
  (impl/emit-as :haskell ['(letrec [x 1] x)])
  => "let\n  x = 1\nin x")

^{:refer std.lang.model-annex.spec-haskell/tf-lambda :added "4.1"}
(fact "transforms lambda expression"
  (impl/emit-as :haskell ['(fn [x] x)])
  => "\\ x -> x")

^{:refer std.lang.model-annex.spec-haskell/tf-do :added "4.1"}
(fact "transforms do block"
  (impl/emit-as :haskell ['(do (print "a") (print "b"))])
  => "do\n  print \"a\"\n  print \"b\"")

^{:refer std.lang.model-annex.spec-haskell/haskell-args :added "4.1"}
(fact "emits space-separated Haskell arguments"
  (impl/emit-script '(defn add [x y] (+ x y)) {:lang :haskell})
  => "add x y = x + y")

^{:refer std.lang.model-annex.spec-haskell/haskell-invoke :added "4.1"}
(fact "emits function application wrapping complex args"
  (impl/emit-as :haskell ['(f (+ 1 2) x)])
  => "f (1 + 2) x")