(ns
 xtbench.lua.runtime.type-pair-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.runtime.type-pair :as pair]
   [xt.runtime.interface-common :as ic]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-pair/pair-new, :added "4.0"}
(fact
 "creates a pair new"
 ^{:hidden true}
 (!.lua
  (var out (pair/pair-new "a" 1 nil))
  [(. out ["::"]) (. out _key) (. out _val)])
 =>
 ["pair" "a" 1])

^{:refer xt.runtime.type-pair/pair, :added "4.0"}
(fact
 "creates a pair"
 ^{:hidden true}
 (!.lua (var out (pair/pair "a" 1)) [(ic/show out) (ic/count out)])
 =>
 ["[\"a\", 1]" 2])
