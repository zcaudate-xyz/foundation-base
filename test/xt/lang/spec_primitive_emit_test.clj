(ns xt.lang.spec-primitive-emit-test
  (:require [std.lang :as l]
            [xt.lang.spec-base]
            [xt.lang.spec-primitive])
  (:use code.test))

(fact "lowers primitive pow to backend math helpers instead of raw `^`"

  [(boolean (re-find #"Math\.pow"
                     (l/emit-as :js ['(xt.lang.spec-primitive/pow 2 5)])))
   (boolean (re-find #"\*\*"
                     (l/emit-as :python ['(xt.lang.spec-primitive/pow 2 5)])))
   (boolean (re-find #"math\.pow"
                     (l/emit-as :lua ['(xt.lang.spec-primitive/pow 2 5)])))]
  => [true true true])