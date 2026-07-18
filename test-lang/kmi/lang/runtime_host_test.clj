(ns kmi.lang.runtime-host-test
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
(fact "host interop reads a property"
  (!.js
   (xt/x:is-number?
    (xt/x:get-key
     (rt/eval-string (rt/empty-runtime)
                     "(host Math PI)")
     "value")))
  => true)

^{:refer kmi.lang.runtime/read-string :added "4.1"}
(fact "host interop calls a method"
  (!.js
   (xt/x:get-key
    (rt/eval-string (rt/empty-runtime)
                    "(host Math pow 2 3)")
    "value"))
  => 8)

^{:refer kmi.lang.runtime/read-many :added "4.1"}
(fact "throw returns an error result"
  (!.js
   (xt/x:not-nil?
    (xt/x:get-key
     (rt/eval-string (rt/empty-runtime)
                     "(throw \"boom\")")
     "error")))
  => true)

^{:refer kmi.lang.runtime/eval-form :added "4.1"}
(fact "throw message is preserved"
  (!.js
   (== "boom"
       (xt/x:get-key
        (rt/eval-string (rt/empty-runtime)
                        "(throw \"boom\")")
        "error")))
  => true)

^{:refer kmi.lang.runtime/empty-runtime :added "4.1"}
(fact "host global JSON is available"
  (!.js
   (xt/x:is-object?
    (xt/x:get-key
     (rt/eval-string (rt/empty-runtime)
                     "JSON")
     "value")))
  => true)
