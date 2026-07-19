(ns kmi.lang.type-pair-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-pair :as pair]
             [xt.lang.spec-base :as xt]
             [kmi.lang.common-util :as ic]
             [xt.lang.common-iter :as it]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.type-pair/pair-new :added "4.0"}
(fact "creates a pair new"

  (!.js
   (var out (pair/pair-new "a" 1 nil))
   [(xt/x:get-key out "::")
    (xt/x:get-key out "_key")
    (xt/x:get-key out "_val")])
  => ["pair" "a" 1])

^{:refer kmi.lang.type-pair/pair :added "4.0"}
(fact "creates a pair"

  (!.js
   (var out (pair/pair "a" 1))
   [(ic/show out)
    (ic/count out)])
  => ["[\"a\", 1]" 2])

^{:refer kmi.lang.type-pair/pair-to-iter :added "4.1"}
(fact "converts pair to iterator"

  (!.js
   (it/arr< (pair/pair-to-iter (pair/pair "a" 1))))
  => ["a" 1])

^{:refer kmi.lang.type-pair/pair-to-array :added "4.1"}
(fact "converts pair to array"

  (!.js
   (pair/pair-to-array (pair/pair "a" 1)))
  => ["a" 1])

^{:refer kmi.lang.type-pair/pair-nth :added "4.1"}
(fact "gets the nth element of a pair"

  (!.js
   (pair/pair-nth (pair/pair "a" 1) 0))
  => "a"

  (!.js
   (pair/pair-nth (pair/pair "a" 1) 1))
  => 1

  (!.js
   (pair/pair-nth (pair/pair "a" 1) 2))
  => nil

  (!.js
   (pair/pair-nth (pair/pair "a" 1) -1))
  => nil)
