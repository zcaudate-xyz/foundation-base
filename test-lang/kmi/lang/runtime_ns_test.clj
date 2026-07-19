(ns kmi.lang.runtime-ns-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [kmi.lang.runtime :as rt]
             [kmi.lang.runtime.eval :as rev]
             [kmi.lang.protocol-base :as proto]
             [kmi.lang.type-list :as list]
             [kmi.lang.type-vector :as vec]]})

^{:refer kmi.lang.runtime/eval-string :added "4.1"}
(fact "vector destructuring in let"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(let [[a b] [1 2]] (+ a b))")
    "value"))
  => 3)

^{:refer kmi.lang.runtime/read-string :added "4.1"}
(fact "rest destructuring in let"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(let [[a & bs] [1 2 3]] (+ a (count bs)))")
    "value"))
  => 3)

^{:refer kmi.lang.runtime/read-many :added "4.1"}
(fact "nested destructuring in let"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(let [[[a] b] [[1] 2]] (+ a b))")
    "value"))
  => 3)

^{:refer kmi.lang.runtime/eval-form :added "4.1"}
(fact "map destructuring with keys and or"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(let [{:keys [a b] :or {a 10}} {:b 2}] (+ a b))")
    "value"))
  => 12)

^{:refer kmi.lang.runtime/empty-runtime :added "4.1"}
(fact "map destructuring with as"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(let [{:keys [b] :as m} {:b 2}] (+ b (get m :b)))")
    "value"))
  => 4)

^{:refer kmi.lang.runtime/handler-read :added "4.1"}
(fact "destructuring works in fn parameters"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "((fn [[a b]] (+ a b)) [3 4])")
    "value"))
  => 7)

^{:refer kmi.lang.runtime/handler-eval :added "4.1"}
(fact "in-ns switches current namespace"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(do (in-ns 'foo) (def x 5) (in-ns 'user) foo/x)")
    "value"))
  => 5)

^{:refer kmi.lang.runtime/handler-load :added "4.1"}
(fact "require with refer imports selected vars"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(do (in-ns 'math) (def square (fn [x] (* x x))) (in-ns 'user) (require '[math :refer [square]]) (square 4))")
    "value"))
  => 16)

^{:refer kmi.lang.runtime/handler-describe :added "4.1"}
(fact "use refers all vars from a namespace"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(do (in-ns 'math) (def pi 3) (in-ns 'user) (use 'math) pi)")
    "value"))
  => 3)

^{:refer kmi.lang.runtime/create-node :added "4.1"}
(fact "require with as creates an alias"

  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(do (in-ns 'math) (def tau 6) (in-ns 'user) (require '[math :as m]) m/tau)")
    "value"))
  => 6)
