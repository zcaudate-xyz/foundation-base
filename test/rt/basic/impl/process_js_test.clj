(ns rt.basic.impl.process-js-test
  (:require [rt.basic.impl.process-js :refer :all]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :oneshot
   :config {:program :nodejs}})

^{:refer rt.basic.impl.process-js/CANARY :adopt true :added "4.0"}
(fact "EVALUATE js code"

  (!.js (+ 1 2 3 4))
  => 10

  (default-oneshot-wrap "1")
  => string?)
