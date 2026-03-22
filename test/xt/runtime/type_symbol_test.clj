(ns xt.runtime.type-symbol-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-symbol :as sym]
             [xt.runtime.interface-common :as tc]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-symbol :as sym]
             [xt.runtime.interface-common :as tc]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-symbol/symbol-hash :added "4.0"}
(fact "gets the symbol hash"
  ^:hidden
  
  (!.js
   [(sym/symbol-hash (sym/symbol "hello" "world"))
    (tc/hash (sym/symbol "hello" "world"))])
  => (contains-in
      [integer?
       integer?])
  
  (!.lua
   [(sym/symbol-hash (sym/symbol "hello" "world"))
    (tc/hash (sym/symbol "hello" "world"))])
  => (contains-in
      [integer?
       integer?]))

^{:refer xt.runtime.type-symbol/symbol-show :added "4.0"}
(fact "shows the symbol"
  ^:hidden
  
  (!.js
   (sym/symbol-show (sym/symbol "hello" "world")))
  => "hello/world"

  (!.lua
   (sym/symbol-show (sym/symbol "hello" "world")))
  => "hello/world")

^{:refer xt.runtime.type-symbol/symbol-eq :added "4.0"}
(fact "gets symbol equality"
  ^:hidden
  
  (!.js
   [(sym/symbol-eq (sym/symbol "hello" "world")
                   (sym/symbol "hello" "world"))
    (sym/symbol-eq (sym/symbol "hello" "world")
                   1)
    (sym/symbol-eq (sym/symbol "hello" "world")
                   (sym/symbol "hello" "world1"))
    (sym/symbol-eq (sym/symbol "hello1" "world")
                   (sym/symbol "hello" "world1"))])
  => [true false false false]

  (!.lua
   [(sym/symbol-eq (sym/symbol "hello" "world")
                   (sym/symbol "hello" "world"))
    (sym/symbol-eq (sym/symbol "hello" "world")
                   1)
    (sym/symbol-eq (sym/symbol "hello" "world")
                   (sym/symbol "hello" "world1"))
    (sym/symbol-eq (sym/symbol "hello1" "world")
                   (sym/symbol "hello" "world1"))])
  => [true false false false])

^{:refer xt.runtime.type-symbol/symbol-create :added "4.0"}
(fact "creates a symbol")

^{:refer xt.runtime.type-symbol/symbol :added "4.0"}
(fact "creates the symbol or pulls it from cache")
