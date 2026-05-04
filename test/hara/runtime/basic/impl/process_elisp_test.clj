(ns hara.runtime.basic.impl.process-elisp-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :elisp
  {:runtime :basic})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.basic.impl.process-elisp/+elisp-basic-config+ :added "4.1"}
(fact "elisp basic runtime evaluates simple expressions"
  (!.elisp (+ 1 2))
  => 3)
