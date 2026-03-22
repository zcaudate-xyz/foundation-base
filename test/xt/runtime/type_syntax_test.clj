(ns xt.runtime.type-syntax-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-syntax :as syn]
             [xt.runtime.interface-common :as tc]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-syntax :as syn]
             [xt.runtime.interface-common :as tc]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-syntax/syntax-wrap :added "4.0"}
(fact "wraps a function to use syntax"
  ^:hidden
  
  (!.js
   ((syn/syntax-wrap k/identity)
    {:_value 1}))
  => 1

  (!.lua
   ((syn/syntax-wrap k/identity)
    {:_value 1}))
  => 1)

^{:refer xt.runtime.type-syntax/syntax-create :added "4.0"}
(fact "creates a syntax"

  (!.js
   (tc/count
    (syn/syntax-create [1 2 3] "hello"))))

^{:refer xt.runtime.type-syntax/get-metadata :added "4.0"}
(fact "gets metadata"
  ^:hidden
  
  (!.lua
   [(syn/get-metadata nil)
    (syn/get-metadata
     (syn/syntax-create 1 "hello"))])
  => [nil "hello"]

  (!.js
   [(syn/get-metadata nil)
    (syn/get-metadata
     (syn/syntax-create 1
                        "hello"))])
  => [nil "hello"])

^{:refer xt.runtime.type-syntax/syntax :added "4.0"}
(fact "creates a syntax")
