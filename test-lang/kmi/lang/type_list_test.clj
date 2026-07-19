(ns kmi.lang.type-list-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.type-list :as t]
             [xt.lang.spec-base :as xt]
             [kmi.lang.common-coll :as coll]
             [kmi.lang.common-util :as util]
             [kmi.lang.protocol-base :as p]
             [xt.lang.common-lib :as k]
             [xt.lang.common-iter :as it]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.type-collection/coll-into-array :added "4.0" :adopt true}
(fact "list from array"

  (!.js
    (coll/coll-into-array
     (t/list)
     [1 2 3 4]))

  (!.js
    (t/list-to-array
     (coll/coll-into-array
      (t/list)
      [1 2 3 4])))
  => [4 3 2 1])

^{:refer kmi.lang.type-collection/coll-into-iter :added "4.0" :adopt true}
(fact "list form iter"

  (!.js
   (t/list-to-array
    (coll/coll-into-iter
     (t/list)
     (it/range [0 10]))))
  => [9 8 7 6 5 4 3 2 1 0])

^{:refer kmi.lang.type-list/list-to-iter :added "4.0"}
(fact "list to iterator"

  (!.js
   (it/arr<
    (it/take
     10
     (t/list-to-iter
      (t/list [ 1 2 3 4])))))
  => [1 2 3 4])

^{:refer kmi.lang.type-list/list-to-array :added "4.0"}
(fact "list to array"

  (!.js
   (t/list-to-array
    (t/list [ 1 2 3 4])))
  => [1 2 3 4])

^{:refer kmi.lang.type-list/list-size :added "4.0"}
(fact "gets the list size"

  (!.js
   (t/list-size (t/list [ 1 2 3])))
  => 3)

^{:refer kmi.lang.type-list/list-new :added "4.0"}
(fact "creates a new list"

  (!.js
   (var out (t/list-new 1 t/EMPTY_LIST))
   [(xt/x:get-key out "::")
    (xt/x:get-key out "_head")
    (t/list-to-array out)])
  => ["list" 1 [1]])

^{:refer kmi.lang.type-list/list-push :added "4.0"}
(fact "pushs onto the front of the list"

  (!.js
   (t/list-to-array
    (t/list-push (t/list [ 1 2 3])
                 10)))
  => [10 1 2 3])

^{:refer kmi.lang.type-list/list-pop :added "4.0"}
(fact "pops an element from front of list"

  (!.js
   (t/list-to-array
    (t/list-pop (t/list [ 1 2 3]))))
  => [2 3])

^{:refer kmi.lang.type-list/list-empty :added "4.0"}
(fact "gets the empty list"

  (!.js
   (t/list-to-array
    (t/list-empty (t/list [ 1 2 3]))))
  => [])

^{:refer kmi.lang.type-list/list-create :added "4.0"}
(fact "creates a list"

  (!.js
    [(p/show
      (->> t/EMPTY_LIST
           (t/list-create 3)
           (t/list-create 2)
           (t/list-create 1)))])
  => ["(1, 2, 3)"])

^{:refer kmi.lang.type-list/list :added "4.0"}
(fact "creates a list given arguments"

  (!.js
   (t/list-to-array
    (t/list [ 1 2 3 4 5])))
  => [1 2 3 4 5])

^{:refer kmi.lang.type-list/list-map :added "4.0"}
(fact "maps function across list"

  (!.js
   (t/list-to-array
    (t/list-map (t/list [ 1 2 3 4 5])
                k/inc)))
  => [2 3 4 5 6])

(comment
  
  
  (!.js
    (ic/show
     (t/list [ 1 2 3 4 5])))
  
  )
