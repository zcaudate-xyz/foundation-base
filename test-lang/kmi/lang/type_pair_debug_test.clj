(ns kmi.lang.type-pair-debug-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-pair :as pair]
             [kmi.lang.common-util :as ic]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(fact "debug show"
  (!.js
   (var out (pair/pair "a" 1))
   (ic/show out))
  => "[\"a\", 1]")
