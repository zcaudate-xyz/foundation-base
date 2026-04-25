(ns xt.lang.common-tree-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic,
   :require [[xt.lang.common-tree :as xtt]
             [xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic,
   :require [[xt.lang.common-tree :as xtt]
             [xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic,
   :require [[xt.lang.common-tree :as xtt]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-tree/eq-nested-loop :added "4.1"}
(fact "switch for nested check"

  (!.js
   [(xtt/eq-nested-loop {:a 1} {:a 1} xtt/eq-nested-obj xtt/eq-nested-arr nil)
    (xtt/eq-nested-loop {:a 1} {:a 2} xtt/eq-nested-obj xtt/eq-nested-arr nil)])
  => [true false]

  (!.lua
   [(xtt/eq-nested-loop {:a 1} {:a 1} xtt/eq-nested-obj xtt/eq-nested-arr nil)
    (xtt/eq-nested-loop {:a 1} {:a 2} xtt/eq-nested-obj xtt/eq-nested-arr nil)])
  => [true false]

  (!.py
   [(xtt/eq-nested-loop {:a 1} {:a 1} xtt/eq-nested-obj xtt/eq-nested-arr nil)
    (xtt/eq-nested-loop {:a 1} {:a 2} xtt/eq-nested-obj xtt/eq-nested-arr nil)])
  => [true false])

^{:refer xt.lang.common-tree/eq-nested-obj :added "4.1"}
(fact "checks object equality"

  (!.js
   [(xtt/eq-nested-obj {:a 1} {:a 1} xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))
    (xtt/eq-nested-obj {:a 1} {:a 2} xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))])
  => [true false]

  (!.lua
   [(xtt/eq-nested-obj {:a 1} {:a 1} xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))
    (xtt/eq-nested-obj {:a 1} {:a 2} xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))])
  => [true false]

  (!.py
   [(xtt/eq-nested-obj {:a 1} {:a 1} xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))
    (xtt/eq-nested-obj {:a 1} {:a 2} xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))])
  => [true false])

^{:refer xt.lang.common-tree/eq-nested-arr :added "4.1"}
(fact "checks array equality"

  (!.js
   [(xtt/eq-nested-arr [1 2] [1 2] xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))
    (xtt/eq-nested-arr [1 2] [2 1] xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))])
  => [true false]

  (!.lua
   [(xtt/eq-nested-arr [1 2] [1 2] xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))
    (xtt/eq-nested-arr [1 2] [2 1] xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))])
  => [true false]

  (!.py
   [(xtt/eq-nested-arr [1 2] [1 2] xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))
    (xtt/eq-nested-arr [1 2] [2 1] xtt/eq-nested-obj xtt/eq-nested-arr (xt/x:lu-create))])
  => [true false])

^{:refer xt.lang.common-tree/eq-nested :added "4.1"}
(fact "checks for nested equality"

  (!.js
   [(xtt/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
    (xtt/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
    (xtt/eq-nested [1] [1])
    (xtt/eq-nested [1] [2])])
  => [true false true false]

  (!.lua
   [(xtt/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
    (xtt/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
    (xtt/eq-nested [1] [1])
    (xtt/eq-nested [1] [2])])
  => [true false true false]

  (!.py
   [(xtt/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
    (xtt/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
    (xtt/eq-nested [1] [1])
    (xtt/eq-nested [1] [2])])
  => [true false true false])

^{:refer xt.lang.common-tree/eq-shallow-raw :added "4.1"}
(fact "basic shallow equality comparator"

  (!.js [(xtt/eq-shallow-raw 1 1 nil nil nil)
          (xtt/eq-shallow-raw 1 2 nil nil nil)])
  => [true false]

  (!.lua [(xtt/eq-shallow-raw 1 1 nil nil nil)
          (xtt/eq-shallow-raw 1 2 nil nil nil)])
  => [true false]

  (!.py [(xtt/eq-shallow-raw 1 1 nil nil nil)
          (xtt/eq-shallow-raw 1 2 nil nil nil)])
  => [true false])

^{:refer xt.lang.common-tree/eq-shallow :added "4.1"}
(fact "checks for shallow equality"

  (!.js
   (var arr [1])
   [(xtt/eq-shallow arr arr)
    (xtt/eq-shallow [1] [1])])
  => [true false]

  (!.lua
   (var arr [1])
   [(xtt/eq-shallow arr arr)
    (xtt/eq-shallow [1] [1])])
  => [true false]

  (!.py
   (var arr [1])
   [(xtt/eq-shallow arr arr)
    (xtt/eq-shallow [1] [1])])
  => [true false])

^{:refer xt.lang.common-tree/tree-walk :added "4.1"}
(fact "walks over object"

  (!.js
    (xtt/tree-walk
     {:a 1, :b [2 {:c 3}]}
     (fn [x] (return x))
     (fn inc-fn[x]
       (if (xt/x:is-number? x)
         (return (+ x 1))
         (return x)))))
  => {"a" 2, "b" [3 {"c" 4}]}

  (!.lua
    (xtt/tree-walk
     {:a 1, :b [2 {:c 3}]}
     (fn [x] (return x))
     (fn inc-fn [x]
       (if (xt/x:is-number? x)
         (return (+ x 1))
         (return x)))))
  => {"a" 2, "b" [3 {"c" 4}]}

  (!.py
    (xtt/tree-walk
     {:a 1, :b [2 {:c 3}]}
     (fn [x] (return x))
     (fn inc-fn [x]
       (if (xt/x:is-number? x)
         (return (+ x 1))
         (return x)))))
  => {"a" 2, "b" [3 {"c" 4}]})

^{:refer xt.lang.common-tree/tree-get-data :added "4.1"}
(fact "gets the data component"

  (!.js
    (xtt/tree-get-data {:name "hello"
                        :job  {:patients [[1 "alice"]
                                          [1 "body"]]
                               :name "doctor"
                               :heal (fn [])}}))
  => {"job" {"name" "doctor", "heal" "<function>", "patients" [[1 "alice"] [1 "body"]]}, "name" "hello"}

  (!.lua
    (xtt/tree-get-data {:name "hello"
                        :job  {:patients [[1 "alice"]
                                          [1 "body"]]
                               :name "doctor"
                               :heal (fn [])}}))
  => {"job" {"name" "doctor", "heal" "<function>", "patients" [[1 "alice"] [1 "body"]]}, "name" "hello"}
  
  (!.py
    (xtt/tree-get-data {:name "hello"
                        :job  {:patients [[1 "alice"]
                                          [1 "body"]]
                               :name "doctor"
                               :heal (fn [])}}))
  => {"job" {"name" "doctor", "heal" "<function>", "patients" [[1 "alice"] [1 "body"]]}, "name" "hello"})

^{:refer xt.lang.common-tree/tree-get-spec :added "4.1"}
(fact "gets the shape of the current data"

  (!.js
    (xtt/tree-get-spec {:name "hello"
                        :job  {:patients [[1 "alice"]
                                          [1 "body"]]
                               :name "doctor"
                               :heal (fn [])}}))
  => {"job" {"name" "string", "heal" "function",
             "patients" [["number" "string"]
                         ["number" "string"]]},
      "name" "string"}

  (!.lua
    (xtt/tree-get-spec {:name "hello"
                        :job  {:patients [[1 "alice"]
                                          [1 "body"]]
                               :name "doctor"
                               :heal (fn [])}}))
  => {"job" {"name" "string", "heal" "function",
             "patients" [["number" "string"]
                         ["number" "string"]]},
      "name" "string"}

  (!.py
    (xtt/tree-get-spec {:name "hello"
                        :job  {:patients [[1 "alice"]
                                          [1 "body"]]
                               :name "doctor"
                               :heal (fn [])}}))
  => {"job" {"name" "string", "heal" "function",
             "patients" [["number" "string"]
                         ["number" "string"]]},
      "name" "string"})

^{:refer xt.lang.common-tree/tree-diff :added "4.1"}
(fact "diffs only keys within map"

  (!.js (xtt/tree-diff {:a 1, :b 2} {:a 1, :c 2}))
  => {"c" 2}

  (!.lua (xtt/tree-diff {:a 1, :b 2} {:a 1, :c 2}))
  => {"c" 2}

  (!.py (xtt/tree-diff {:a 1, :b 2} {:a 1, :c 2}))
  => {"c" 2})

^{:refer xt.lang.common-tree/tree-diff-nested :added "4.1"}
(fact "diffs nested keys within map"

  (!.js
   [(xtt/tree-diff-nested {:a 1, :b 2} {:a 1, :c 2})
    (xtt/tree-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
    (xtt/tree-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
  => [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}]

  (!.lua
   [(xtt/tree-diff-nested {:a 1, :b 2} {:a 1, :c 2})
    (xtt/tree-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
    (xtt/tree-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
  => [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}]

  (!.py
   [(xtt/tree-diff-nested {:a 1, :b 2} {:a 1, :c 2})
    (xtt/tree-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
    (xtt/tree-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
  => [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}])

(comment
  
  (code.manage/isolate 'xt.lang.common-tree-test {:suffix "-fix"})
  (s/seedgen-langadd 'xt.lang.common-tree {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-tree {:lang [:lua :python] :write true})
  
  )
