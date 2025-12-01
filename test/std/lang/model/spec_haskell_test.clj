(ns std.lang.model.spec-haskell-test
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.lang.base.script :as script]
            [std.lang.model.spec-haskell :as spec-haskell]
            [code.test :as t]))

(script/install spec-haskell/+book+)

(l/script :haskell
  {:runtime :default
   :config {:book spec-haskell/+book+}})

(t/fact "basic emit"
  (l/emit-script '(defn hello [x] x) {:lang :haskell})
  => "hello x = x"

  (l/emit-as :haskell ['(let [x 1 y 2] (+ x y))])
  => "let\n  x = 1\n  y = 2\nin x + y"

  (l/emit-as :haskell ['(if true 1 2)])
  => "if true then 1 else 2"

  (l/emit-as :haskell ['(case x
                         1 "one"
                         2 "two")])
  => "case x of\n  1 -> \"one\"\n  2 -> \"two\""

  (l/emit-as :haskell ['(fn [x] (+ x 1))])
  => "\\ x -> x + 1"

  (l/emit-as :haskell ['(cons 1 [2 3])])
  => "1 : [2,3]"

  (l/emit-as :haskell ['(concat [1] [2])])
  => "[1] ++ [2]"

  (l/emit-as :haskell ['(do (print "a") (print "b"))])
  => "do\n  print \"a\"\n  print \"b\""

  (l/emit-as :haskell ['[1 2 3]])
  => "[1,2,3]")

(t/fact "types emit"
  (l/emit-as :haskell ['[:> List Int]])
  => "List Int"

  (l/emit-as :haskell ['[:> Map String Int]])
  => "Map String Int"
  )

(t/fact "defn with type hint"
  (l/emit-script '(defn ^Int add [^Int x ^Int y] (+ x y)) {:lang :haskell})
  => "add :: Int -> Int -> Int\nadd x y = x + y")
