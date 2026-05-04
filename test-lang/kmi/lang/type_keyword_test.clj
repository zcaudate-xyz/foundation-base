(ns kmi.lang.type-keyword-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-keyword :as kw]
             [kmi.lang.interface-common :as tc]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[kmi.lang.type-keyword :as kw]
             [kmi.lang.interface-common :as tc]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.lang.type-keyword/keyword-hash :added "4.0"}
(fact "gets the keyword hash"

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

^{:refer kmi.lang.type-keyword/keyword-show :added "4.0"}
(fact "shows the keyword"

  (!.js
   (kw/keyword-show (kw/keyword "hello" "world")))
  => ":hello/world"

  (!.lua
   (kw/keyword-show (kw/keyword "hello" "world")))
  => ":hello/world")

^{:refer kmi.lang.type-keyword/keyword-eq :added "4.0"}
(fact "gets keyword equality"

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

^{:refer kmi.lang.type-keyword/keyword-create :added "4.0"}
(fact "creates a keyword"

  (!.js
   (var out (kw/keyword-create "hello" "world"))
   [(. out ["::"])
    (tc/get-name out)
    (tc/get-namespace out)
    (kw/keyword-show out)])
  => ["keyword" "world" "hello" ":hello/world"]

  (!.lua
   (var out (kw/keyword-create "hello" "world"))
   [(. out ["::"])
    (tc/get-name out)
    (tc/get-namespace out)
    (kw/keyword-show out)])
  => ["keyword" "world" "hello" ":hello/world"])

^{:refer kmi.lang.type-keyword/keyword :added "4.0"}
(fact "creates the keyword or pulls it from cache"

  (!.js
   (var k0 (kw/keyword "hello" "world"))
   (var k1 (kw/keyword "hello" "world"))
   (var k2 (kw/keyword "hello" "other"))
   [(== k0 k1)
    (== k0 k2)])
  => [true false]

  (!.lua
   (var k0 (kw/keyword "hello" "world"))
   (var k1 (kw/keyword "hello" "world"))
   (var k2 (kw/keyword "hello" "other"))
   [(== k0 k1)
    (== k0 k2)])
  => [true false])
