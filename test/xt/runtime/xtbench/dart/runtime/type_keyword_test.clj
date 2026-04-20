(ns
 xtbench.dart.runtime.type-keyword-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.runtime.type-keyword :as kw]
   [xt.runtime.interface-common :as tc]
   [xt.lang.common-lib :as k]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-keyword/keyword-hash, :added "4.0"}
(fact
 "gets the keyword hash"
 ^{:hidden true}
 (!.dt
  [(kw/keyword-hash (kw/keyword "hello" "world"))
   (tc/hash (kw/keyword "hello" "world"))])
 =>
 (contains-in [integer? integer?]))

^{:refer xt.runtime.type-keyword/keyword-show, :added "4.0"}
(fact
 "shows the keyword"
 ^{:hidden true}
 (!.dt (kw/keyword-show (kw/keyword "hello" "world")))
 =>
 ":hello/world")

^{:refer xt.runtime.type-keyword/keyword-eq, :added "4.0"}
(fact
 "gets keyword equality"
 ^{:hidden true}
 (!.dt
  [(kw/keyword-eq
    (kw/keyword "hello" "world")
    (kw/keyword "hello" "world"))
   (kw/keyword-eq (kw/keyword "hello" "world") 1)
   (kw/keyword-eq
    (kw/keyword "hello" "world")
    (kw/keyword "hello" "world1"))
   (kw/keyword-eq
    (kw/keyword "hello1" "world")
    (kw/keyword "hello" "world1"))])
 =>
 [true false false false])

^{:refer xt.runtime.type-keyword/keyword-create, :added "4.0"}
(fact
 "creates a keyword"
 ^{:hidden true}
 (!.dt
  (var out (kw/keyword-create "hello" "world"))
  [(. out ["::"])
   (tc/get-name out)
   (tc/get-namespace out)
   (kw/keyword-show out)])
 =>
 ["keyword" "world" "hello" ":hello/world"])

^{:refer xt.runtime.type-keyword/keyword, :added "4.0"}
(fact
 "creates the keyword or pulls it from cache"
 ^{:hidden true}
 (!.dt
  (var k0 (kw/keyword "hello" "world"))
  (var k1 (kw/keyword "hello" "world"))
  (var k2 (kw/keyword "hello" "other"))
  [(== k0 k1) (== k0 k2)])
 =>
 [true false])
