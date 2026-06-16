(ns xtbench.dart.lang.common-protocol-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer :all]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(comment
  (s/snapto '[xt.lang.common-protocol])

  (s/seedgen-langadd '[xt.lang.common-protocol] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.common-protocol] {:lang [:lua :python] :write true}))
