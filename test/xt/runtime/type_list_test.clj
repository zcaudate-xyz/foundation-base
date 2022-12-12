(ns xt.runtime.type-list-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-list :as t]
             [xt.runtime.interface-common :as ic]
             [xt.runtime.interface-collection :as coll]
             [xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.type-list :as t]
             [xt.runtime.interface-common :as ic]
             [xt.runtime.interface-collection :as coll]
             [xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-collection/coll-into-array :adopt true :added "4.0"}
(fact "list from array"
  ^:hidden
  
  (!.js
   (t/list-to-array
    (coll/coll-into-array
     (t/list)
     [1 2 3 4])))
  => [4 3 2 1]

  (!.lua
   (t/list-to-array
    (coll/coll-into-array
     (t/list)
     [1 2 3 4])))
  => [4 3 2 1])

^{:refer xt.runtime.type-collection/coll-into-iter :adopt true :added "4.0"}
(fact "list form iter"
  ^:hidden

  (!.js
   (t/list-to-array
    (coll/coll-into-iter
     (t/list)
     (it/range [0 10]))))
  => [9 8 7 6 5 4 3 2 1 0]
  
  (!.lua
   (t/list-to-array
    (coll/coll-into-iter
     (t/list)
     (it/range [0 10]))))
  => [9 8 7 6 5 4 3 2 1 0])

^{:refer xt.runtime.type-list/list-to-iter :added "4.0"}
(fact "list to iterator"
  ^:hidden
  
  (!.js
   (it/arr<
    (it/take
     10
     (t/list-to-iter
      (t/list 1 2 3 4)))))
  => [1 2 3 4]

  (!.lua
   (it/arr<
    (it/take
     10
     (t/list-to-iter
      (t/list 1 2 3 4)))))
  => [1 2 3 4])

^{:refer xt.runtime.type-list/list-to-array :added "4.0"}
(fact "list to array"
  ^:hidden
  
  (!.js
   (t/list-to-array
    (t/list 1 2 3 4)))
  => [1 2 3 4]

  (!.lua
   (t/list-to-array
    (t/list 1 2 3 4)))
  => [1 2 3 4])

^{:refer xt.runtime.type-list/list-size :added "4.0"}
(fact "gets the list size"
  ^:hidden
  
  (!.js
   (t/list-size (t/list 1 2 3)))
  => 3

  (!.lua
   (t/list-size (t/list 1 2 3)))
  => 3)

^{:refer xt.runtime.type-list/list-new :added "4.0"}
(fact "creates a new list")

^{:refer xt.runtime.type-list/list-push :added "4.0"}
(fact "pushs onto the front of the list"
  ^:hidden
  
  (!.js
   (t/list-to-array
    (t/list-push (t/list 1 2 3)
                 10)))
  => [10 1 2 3]

  (!.lua
   (t/list-to-array
    (t/list-push (t/list 1 2 3)
                 10)))
  => [10 1 2 3])

^{:refer xt.runtime.type-list/list-pop :added "4.0"}
(fact "pops an element from front of list"
  ^:hidden
  
  (!.js
   (t/list-to-array
    (t/list-pop (t/list 1 2 3))))
  => [2 3]

  (!.lua
   (t/list-to-array
    (t/list-pop (t/list 1 2 3))))
  => [2 3])

^{:refer xt.runtime.type-list/list-empty :added "4.0"}
(fact "gets the empty list"
  ^:hidden
  
  (!.js
   (t/list-to-array
    (t/list-empty (t/list 1 2 3))))
  => []

  (!.lua
   (t/list-to-array
    (t/list-empty (t/list 1 2 3))))
  => {})

^{:refer xt.runtime.type-list/list-create :added "4.0"}
(fact "creates a list"
  ^:hidden
  
  (!.lua
   [(ic/show
     (->> t/EMPTY_LIST
          (t/list-create 3)
          (t/list-create 2)
          (t/list-create 1)))])
  => ["(1, 2, 3)"]
  
  (!.js
   [(ic/show
     (->> t/EMPTY_LIST
          (t/list-create 3)
          (t/list-create 2)
          (t/list-create 1)))])
  => ["(1, 2, 3)"])

^{:refer xt.runtime.type-list/list :added "4.0"}
(fact "creates a list given arguments"
  ^:hidden
  
  (!.js
   (t/list-to-array
    (t/list 1 2 3 4 5)))
  => [1 2 3 4 5]
  
  (!.lua
   (t/list-to-array
    (t/list 1 2 3 4 5)))
  => [1 2 3 4 5])

^{:refer xt.runtime.type-list/list-map :added "4.0"}
(fact "maps function across list"
  ^:hidden
  
  (!.lua
   (t/list-to-array
    (t/list-map (t/list 1 2 3 4 5)
                k/inc)))
  => [2 3 4 5 6])
