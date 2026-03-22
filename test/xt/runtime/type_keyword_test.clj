(ns xt.runtime.type-keyword-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-keyword :as kw]
             [xt.runtime.interface-common :as tc]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-keyword :as kw]
             [xt.runtime.interface-common :as tc]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-keyword/keyword-hash :added "4.0"}
(fact "gets the keyword hash"
  ^:hidden
  
  (!.js
   [(kw/keyword-hash (kw/keyword "hello" "world"))
    (tc/hash (kw/keyword "hello" "world"))])
  => (contains-in
      [integer?
       integer?])
  
  (!.lua
   [(kw/keyword-hash (kw/keyword "hello" "world"))
    (tc/hash (kw/keyword "hello" "world"))])
  => (contains-in
      [integer?
       integer?]))

^{:refer xt.runtime.type-keyword/keyword-show :added "4.0"}
(fact "shows the keyword"
  ^:hidden
  
  (!.js
   (kw/keyword-show (kw/keyword "hello" "world")))
  => ":hello/world"

  (!.lua
   (kw/keyword-show (kw/keyword "hello" "world")))
  => ":hello/world")

^{:refer xt.runtime.type-keyword/keyword-eq :added "4.0"}
(fact "gets keyword equality"
  ^:hidden
  
  (!.js
   [(kw/keyword-eq (kw/keyword "hello" "world")
                   (kw/keyword "hello" "world"))
    (kw/keyword-eq (kw/keyword "hello" "world")
                   1)
    (kw/keyword-eq (kw/keyword "hello" "world")
                   (kw/keyword "hello" "world1"))
    (kw/keyword-eq (kw/keyword "hello1" "world")
                   (kw/keyword "hello" "world1"))])
  => [true false false false]

  (!.lua
   [(kw/keyword-eq (kw/keyword "hello" "world")
                   (kw/keyword "hello" "world"))
    (kw/keyword-eq (kw/keyword "hello" "world")
                   1)
    (kw/keyword-eq (kw/keyword "hello" "world")
                   (kw/keyword "hello" "world1"))
    (kw/keyword-eq (kw/keyword "hello1" "world")
                   (kw/keyword "hello" "world1"))])
  => [true false false false])

^{:refer xt.runtime.type-keyword/keyword-create :added "4.0"}
(fact "creates a keyword")

^{:refer xt.runtime.type-keyword/keyword :added "4.0"}
(fact "creates the keyword or pulls it from cache")
