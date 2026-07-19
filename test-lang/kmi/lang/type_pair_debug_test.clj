(ns kmi.lang.type-pair-debug-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-pair :as pair]
             [kmi.lang.common-util :as ic]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.type-pair/pair :id kmi-extra-1}
(fact "debug show"

  (!.js
   (var out (pair/pair "a" 1))
   (ic/show out))
  => "[\"a\", 1]")
