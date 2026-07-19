^{:no-test true}
(ns kmi.lang.type-pair-min-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-pair :as pair]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.type-pair/pair :id type-pair-loads}
(fact "type-pair loads"

  (!.js true)
  => true)
