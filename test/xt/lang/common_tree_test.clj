(ns xt.lang.common-tree-test
  (:use code.test)
  (:require [xt.lang.common-tree :refer :all]))


^{:refer xt.lang.common-data/eq-nested-basic :added "4.1"}
(fact "basic shallow equality comparator"

  (!.js [(xtd/eq-nested-basic 1 1 nil nil nil)
          (xtd/eq-nested-basic 1 2 nil nil nil)])
  => [true false]

  (!.lua [(xtd/eq-nested-basic 1 1 nil nil nil)
          (xtd/eq-nested-basic 1 2 nil nil nil)])
  => [true false]

  (!.py [(xtd/eq-nested-basic 1 1 nil nil nil)
          (xtd/eq-nested-basic 1 2 nil nil nil)])
  => [true false])

^{:refer xt.lang.common-data/eq-nested-loop :added "4.1"}
(fact "switch for nested check"

  (!.js
   [(xtd/eq-nested-loop {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr nil)
    (xtd/eq-nested-loop {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr nil)])
  => [true false]

  (!.lua
   [(xtd/eq-nested-loop {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr nil)
    (xtd/eq-nested-loop {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr nil)])
  => [true false]

  (!.py
   [(xtd/eq-nested-loop {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr nil)
    (xtd/eq-nested-loop {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr nil)])
  => [true false])

^{:refer xt.lang.common-data/eq-nested-obj :added "4.1"}
(fact "checks object equality"

  (!.js
   [(xtd/eq-nested-obj {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-obj {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false]

  (!.lua
   [(xtd/eq-nested-obj {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-obj {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false]

  (!.py
   [(xtd/eq-nested-obj {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-obj {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false])

^{:refer xt.lang.common-data/eq-nested-arr :added "4.1"}
(fact "checks array equality"

  (!.js
   [(xtd/eq-nested-arr [1 2] [1 2] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-arr [1 2] [2 1] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false]

  (!.lua
   [(xtd/eq-nested-arr [1 2] [1 2] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-arr [1 2] [2 1] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false]

  (!.py
   [(xtd/eq-nested-arr [1 2] [1 2] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-arr [1 2] [2 1] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false])

^{:refer xt.lang.common-data/eq-nested :added "4.1"}
(fact "checks for nested equality"

  (!.js
   [(xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
    (xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
    (xtd/eq-nested [1] [1])
    (xtd/eq-nested [1] [2])])
  => [true false true false]

  (!.lua
   [(xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
    (xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
    (xtd/eq-nested [1] [1])
    (xtd/eq-nested [1] [2])])
  => [true false true false]

  (!.py
   [(xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
    (xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
    (xtd/eq-nested [1] [1])
    (xtd/eq-nested [1] [2])])
  => [true false true false])

^{:refer xt.lang.common-data/eq-shallow :added "4.1"}
(fact "checks for shallow equality"

  (!.js
   (var arr [1])
   [(xtd/eq-shallow arr arr)
    (xtd/eq-shallow [1] [1])])
  => [true false]

  (!.lua
   (var arr [1])
   [(xtd/eq-shallow arr arr)
    (xtd/eq-shallow [1] [1])])
  => [true false]

  (!.py
   (var arr [1])
   [(xtd/eq-shallow arr arr)
    (xtd/eq-shallow [1] [1])])
  => [true false])

^{:refer xt.lang.common-data/tree-walk :added "4.1"}
(fact "walks over object"

  (!.js
    (var do-fn
         (fn [x]
           (if (xt/x:is-number? x)
         (return (+ x 1))
         (return x))))
    (xtd/tree-walk
     {:a 1, :b [2 {:c 3}]}
     (fn [x] (return x))
     do-fn))
  => {"a" 2, "b" [3 {"c" 4}]}

  (!.lua
    (var do-fn
         (fn [x]
           (if (xt/x:is-number? x)
         (return (+ x 1))
         (return x))))
    (xtd/tree-walk
     {:a 1, :b [2 {:c 3}]}
     (fn [x] (return x))
     do-fn))
  => {"a" 2, "b" [3 {"c" 4}]}

  (!.py
    (var do-fn
         (fn [x]
           (if (xt/x:is-number? x)
         (return (+ x 1))
         (return x))))
    (xtd/tree-walk
     {:a 1, :b [2 {:c 3}]}
     (fn [x] (return x))
     do-fn))
  => {"a" 2, "b" [3 {"c" 4}]})

^{:refer xt.lang.common-data/tree-diff :added "4.1"}
(fact "diffs only keys within map"

  (!.js (xtd/tree-diff {:a 1, :b 2} {:a 1, :c 2}))
  => {"c" 2}

  (!.lua (xtd/tree-diff {:a 1, :b 2} {:a 1, :c 2}))
  => {"c" 2}

  (!.py (xtd/tree-diff {:a 1, :b 2} {:a 1, :c 2}))
  => {"c" 2})

^{:refer xt.lang.common-data/tree-diff-nested :added "4.1"}
(fact "diffs nested keys within map"

  (!.js
   [(xtd/tree-diff-nested {:a 1, :b 2} {:a 1, :c 2})
    (xtd/tree-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
    (xtd/tree-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
  => [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}]

  (!.lua
   [(xtd/tree-diff-nested {:a 1, :b 2} {:a 1, :c 2})
    (xtd/tree-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
    (xtd/tree-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
  => [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}]

  (!.py
   [(xtd/tree-diff-nested {:a 1, :b 2} {:a 1, :c 2})
    (xtd/tree-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
    (xtd/tree-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
  => [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}])


^{:refer xt.lang.common-tree/eq-shallow-raw :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-tree/tree-get-data :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-tree/tree-get-spec :added "4.1"}
(fact "TODO")