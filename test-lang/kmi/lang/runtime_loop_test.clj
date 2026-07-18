(ns kmi.lang.runtime-loop-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python :dart]}}
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
(fact "loop returns final value without recur"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(loop [x 5] x)")
    "value"))
  => 5)

^{:refer kmi.lang.runtime/read-string :added "4.1"}
(fact "recur restarts a loop"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(loop [x 0] (if (< x 3) (recur (+ x 1)) x))")
    "value"))
  => 3)

^{:refer kmi.lang.runtime/read-many :added "4.1"}
(fact "loop bindings shadow outer names"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(let [x 100] (loop [x 0] (if (< x 2) (recur (+ x 1)) x)))")
    "value"))
  => 2)

^{:refer kmi.lang.runtime/eval-form :added "4.1"}
(fact "recur arity mismatch errors"
  (!.js
   (xt/x:not-nil?
    (xt/x:get-key
     (rt/eval-string (rt/empty-runtime)
                     "(loop [x 0] (recur x 1))")
     "error")))
  => true)

^{:refer kmi.lang.runtime/empty-runtime :added "4.1"}
(fact "recur outside loop errors"
  (!.js
   (xt/x:not-nil?
    (xt/x:get-key
     (rt/eval-string (rt/empty-runtime)
                     "(recur 1)")
     "error")))
  => true)

^{:refer kmi.lang.runtime/handler-read :added "4.1"}
(fact "apply calls a function with collected args"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(apply + [1 2 3])")
    "value"))
  => 6)

^{:refer kmi.lang.runtime/handler-eval :added "4.1"}
(fact "apply works with fixed leading args"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(apply + 1 2 [3 4])")
    "value"))
  => 10)

^{:refer kmi.lang.runtime/handler-load :added "4.1"}
(fact "variadic function collects rest args"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "((fn [x & ys] (+ x (count ys))) 1 2 3 4)")
    "value"))
  => 4)

^{:refer kmi.lang.runtime/handler-describe :added "4.1"}
(fact "variadic function with no rest args works"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "((fn [x & ys] x) 7)")
    "value"))
  => 7)
