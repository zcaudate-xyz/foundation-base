(ns
 xtbench.lua.runtime.type-list-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.runtime.type-list :as t]
   [xt.runtime.interface-common :as ic]
   [xt.runtime.interface-collection :as coll]
   [xt.lang.common-lib :as k]
   [xt.lang.common-iter :as it]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-collection/coll-into-array,
  :adopt true,
  :added "4.0"}
(fact
 "list from array"
 ^{:hidden true}
 (!.lua (t/list-to-array (coll/coll-into-array (t/list) [1 2 3 4])))
 =>
 [4 3 2 1])

^{:refer xt.runtime.type-collection/coll-into-iter,
  :adopt true,
  :added "4.0"}
(fact
 "list form iter"
 ^{:hidden true}
 (!.lua
  (t/list-to-array (coll/coll-into-iter (t/list) (it/range [0 10]))))
 =>
 [9 8 7 6 5 4 3 2 1 0])

^{:refer xt.runtime.type-list/list-to-iter, :added "4.0"}
(fact
 "list to iterator"
 ^{:hidden true}
 (!.lua (it/arr< (it/take 10 (t/list-to-iter (t/list 1 2 3 4)))))
 =>
 [1 2 3 4])

^{:refer xt.runtime.type-list/list-to-array, :added "4.0"}
(fact
 "list to array"
 ^{:hidden true}
 (!.lua (t/list-to-array (t/list 1 2 3 4)))
 =>
 [1 2 3 4])

^{:refer xt.runtime.type-list/list-size, :added "4.0"}
(fact
 "gets the list size"
 ^{:hidden true}
 (!.lua (t/list-size (t/list 1 2 3)))
 =>
 3)

^{:refer xt.runtime.type-list/list-new, :added "4.0"}
(fact
 "creates a new list"
 ^{:hidden true}
 (!.lua
  (var out (t/list-new 1 t/EMPTY_LIST nil))
  [(. out ["::"]) (. out _head) (t/list-to-array out)])
 =>
 ["list" 1 [1]])

^{:refer xt.runtime.type-list/list-push, :added "4.0"}
(fact
 "pushs onto the front of the list"
 ^{:hidden true}
 (!.lua (t/list-to-array (t/list-push (t/list 1 2 3) 10)))
 =>
 [10 1 2 3])

^{:refer xt.runtime.type-list/list-pop, :added "4.0"}
(fact
 "pops an element from front of list"
 ^{:hidden true}
 (!.lua (t/list-to-array (t/list-pop (t/list 1 2 3))))
 =>
 [2 3])

^{:refer xt.runtime.type-list/list-empty, :added "4.0"}
(fact
 "gets the empty list"
 ^{:hidden true}
 (!.lua (t/list-to-array (t/list-empty (t/list 1 2 3))))
 =>
 {})

^{:refer xt.runtime.type-list/list-create, :added "4.0"}
(fact
 "creates a list"
 ^{:hidden true}
 (!.lua
  [(ic/show
    (->>
     t/EMPTY_LIST
     (t/list-create 3)
     (t/list-create 2)
     (t/list-create 1)))])
 =>
 ["(1, 2, 3)"])

^{:refer xt.runtime.type-list/list, :added "4.0"}
(fact
 "creates a list given arguments"
 ^{:hidden true}
 (!.lua (t/list-to-array (t/list 1 2 3 4 5)))
 =>
 [1 2 3 4 5])

^{:refer xt.runtime.type-list/list-map, :added "4.0"}
(fact
 "maps function across list"
 ^{:hidden true}
 (!.lua (t/list-to-array (t/list-map (t/list 1 2 3 4 5) k/inc)))
 =>
 [2 3 4 5 6])
