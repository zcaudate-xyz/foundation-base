(ns kmi.lang.type-pair-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-pair :as pair]
             [kmi.lang.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[kmi.lang.type-pair :as pair]
             [kmi.lang.interface-common :as ic]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.lang.type-pair/pair-new :added "4.0"}
(fact "creates a pair new"

  (!.js
   (var out (pair/pair-new "a" 1 nil))
   [(. out ["::"])
    (. out _key)
    (. out _val)])
  => ["pair" "a" 1]

  (!.lua
   (var out (pair/pair-new "a" 1 nil))
   [(. out ["::"])
    (. out _key)
    (. out _val)])
  => ["pair" "a" 1])

^{:refer kmi.lang.type-pair/pair :added "4.0"}
(fact "creates a pair"

  (!.js
   (var out (pair/pair "a" 1))
   [(ic/show out)
    (ic/count out)])
  => ["[\"a\", 1]" 2]

  (!.lua
   (var out (pair/pair "a" 1))
   [(ic/show out)
    (ic/count out)])
  => ["[\"a\", 1]" 2])
