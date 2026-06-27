(ns kmi.lang.runtime-macro-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js]}}
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
(fact "defmacro expands and evaluates"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(do (defmacro double [x] (list (quote *) x 2))\n                        (double 5))")
    "value"))
  => 10)

^{:refer kmi.lang.runtime/read-string :added "4.1"}
(fact "syntax-quote and unquote construct forms"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(do (defmacro my-when [test body] `(if ~test ~body))\n                        (my-when true 42))")
    "value"))
  => 42)

^{:refer kmi.lang.runtime/read-many :added "4.1"}
(fact "unquote-splicing splices collections"
  (!.js
   (var out (rt/eval-string (rt/empty-runtime)
                            "(do (defmacro splice-list [xs] `(list ~@xs))\n                                (splice-list [1 2 3]))"))
   (var value (xt/x:get-key out "value"))
   [(rev/list? value)
    (proto/to-array value)])
  => [true [1 2 3]])

^{:refer kmi.lang.runtime/eval-form :added "4.1"}
(fact "var and deref access vars"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(do (def x 10)\n                        (deref (var x)))")
    "value"))
  => 10)

^{:refer kmi.lang.runtime/empty-runtime :added "4.1"}
(fact "unquote outside syntax-quote errors"
  (!.js
   (xt/x:not-nil? (xt/x:get-key (rt/eval-string (rt/empty-runtime) "(~ 1)") "error")))
  => true)
