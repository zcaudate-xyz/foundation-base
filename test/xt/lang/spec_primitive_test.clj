(ns xt.lang.spec-primitive-test
  (:use code.test)
  (:require [std.lang.typed.xtalk :as typed]
            [xt.lang.spec-primitive]))

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "registers primitive grammar declarations for typed analysis"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'xt.lang.spec-primitive)
  [(boolean (typed/get-macro 'xt.lang.spec-primitive/if))
   (boolean (typed/get-macro 'xt.lang.spec-primitive/let))
   (boolean (typed/get-macro 'xt.lang.spec-primitive/for:index))
   (nil? (typed/get-spec 'xt.lang.spec-primitive/proto:get))
   (boolean (typed/get-macro 'xt.lang.spec-primitive/proto:get))
   (boolean (typed/get-macro 'xt.lang.spec-primitive/->))]
  => [true true true true true true])
