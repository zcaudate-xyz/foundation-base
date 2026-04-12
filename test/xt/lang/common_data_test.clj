(ns xt.lang.common-data-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script- :lua
 {:runtime :basic,
  :require [[xt.lang.common-data :as xtd]
            [xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-data/not-empty? :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/not-empty? nil)
    (xtd/not-empty? "")
    (xtd/not-empty? "123")
    (xtd/not-empty? [])
    (xtd/not-empty? [1 2 3])
    (xtd/not-empty? {})
    (xtd/not-empty? {:a 1, :b 2})])
  => [false false true false true false true])

^{:refer xt.lang.common-data/lu-create :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var lu (xtd/lu-create))
   (xtd/lu-get lu "missing"))
  => nil)

^{:refer xt.lang.common-data/lu-del :added "4.1"}
(fact "TODO"
  
  (!.lua
    (var lu (xtd/lu-create))
    (xtd/lu-set lu "a" 1)
    (xtd/lu-del lu "a")
    (xtd/lu-get lu "a"))
  => nil)

^{:refer xt.lang.common-data/lu-get :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 1)
   (xtd/lu-get lu "a"))
  => 1)

^{:refer xt.lang.common-data/lu-set :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 2)
   (xtd/lu-get lu "a"))
  => 2)

^{:refer xt.lang.common-data/lu-eq :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var lu0 (xtd/lu-create))
   (var lu1 (xtd/lu-create))
   [(xtd/lu-eq lu0 lu0)
    (xtd/lu-eq lu0 lu1)])
  => [true false])

^{:refer xt.lang.common-data/first :added "4.1"}
(fact "TODO"

  (!.lua (xtd/first [1 2 3]))
  => 1)

^{:refer xt.lang.common-data/second :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/second [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/nth :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/nth [1 2 3] 0)
          (xtd/nth [1 2 3] 2)])
  => [1 3])

^{:refer xt.lang.common-data/last :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/last [1 2 3]))
  => 3)

^{:refer xt.lang.common-data/second-last :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/second-last [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/arr-empty? :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/arr-empty? nil)
          (xtd/arr-empty? [])
          (xtd/arr-empty? [1])])
  => [true true false])

^{:refer xt.lang.common-data/arr-not-empty? :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/arr-not-empty? nil)
          (xtd/arr-not-empty? [])
          (xtd/arr-not-empty? [1])])
  => [false false true])

^{:refer xt.lang.common-data/arrayify :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/arrayify 1)
          (xtd/arrayify [1])])
  => [[1] [1]])

^{:refer xt.lang.common-data/arr-lookup :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/obj-keys (xtd/arr-lookup ["a" "b"]))))
  => #{"a" "b"})

^{:refer xt.lang.common-data/arr-omit :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-omit [1 2 3] 1))
  => [1 3])

^{:refer xt.lang.common-data/arr-reverse :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-reverse [1 2 3]))
  => [3 2 1])

^{:refer xt.lang.common-data/arr-zip :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-zip ["a" "b"] [1 2]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/arr-clone :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var src [1 2])
   (var out (xtd/arr-clone src))
   (xt/x:arr-push src 3)
   out)
  => [1 2])

^{:refer xt.lang.common-data/arr-append :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-append [1 2] [3 4]))
  => [1 2 3 4])

^{:refer xt.lang.common-data/arr-slice :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-slice [1 2 3 4] 1 3))
  => [2 3])

^{:refer xt.lang.common-data/arr-rslice :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-rslice [1 2 3 4] 1 3))
  => [3 2])

^{:refer xt.lang.common-data/arr-tail :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-tail [1 2 3 4] 2))
  => [4 3])

^{:refer xt.lang.common-data/arr-range :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/arr-range 5)
          (xtd/arr-range [2 6])
          (xtd/arr-range [2 7 2])])
  => [[0 1 2 3 4] [2 3 4 5] [2 4 6]])

^{:refer xt.lang.common-data/arr-intersection :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-intersection [1 2 3] [2 3 4]))
  => [2 3])

^{:refer xt.lang.common-data/arr-difference :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-difference [1 2] [2 3 4]))
  => [3 4])

^{:refer xt.lang.common-data/arr-union :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/arr-union [1 2] [2 3])))
  => #{1 2 3})

^{:refer xt.lang.common-data/arr-shuffle :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/arr-shuffle [1 2 3])))
  => #{1 2 3})

^{:refer xt.lang.common-data/arr-pushl :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-pushl [1 2 3] 4 3))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-pushr :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-pushr [1 2 3] 0 3))
  => [0 1 2])

^{:refer xt.lang.common-data/arr-interpose :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-interpose [1 2 3] 0))
  => [1 0 2 0 3])

^{:refer xt.lang.common-data/arr-random :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-random [1]))
  => 1)

^{:refer xt.lang.common-data/arr-sample :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-sample ["a" "b" "c"] [0 1 0]))
  => "b")

^{:refer xt.lang.common-data/obj-empty? :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/obj-empty? {})
          (xtd/obj-empty? {:a 1})])
  => [true false])

^{:refer xt.lang.common-data/obj-not-empty? :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/obj-not-empty? {})
          (xtd/obj-not-empty? {:a 1})])
  => [false true])

^{:refer xt.lang.common-data/obj-first-key :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-first-key {:a 1}))
  => "a")

^{:refer xt.lang.common-data/obj-first-val :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-first-val {:a 1}))
  => 1)

^{:refer xt.lang.common-data/obj-assign-nested :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-assign-nested {:a {:b 1}} {:a {:c 2}}))
  => {"a" {"b" 1, "c" 2}})

^{:refer xt.lang.common-data/obj-assign-with :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/obj-assign-with
    {:a 1, :b 2}
    {:a 3, :c 4}
    (fn [x y] (return (+ x y)))))
  => {"a" 4, "b" 2, "c" 4})

^{:refer xt.lang.common-data/obj-del :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-del {:a 1, :b 2, :c 3} ["a" "c"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-del-all :added "4.1"}
(fact "TODO"
  
  (!.lua
    (var out {:a 1, :b 2})
    (xtd/obj-del-all out)
    out)
  => {})

^{:refer xt.lang.common-data/obj-pick :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-pick {:a 1, :b 2} ["a"]))
  => {"a" 1})

^{:refer xt.lang.common-data/obj-omit :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-omit {:a 1, :b 2} ["a"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-transpose :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-transpose {:a "x", :b "y"}))
  => {"x" "a", "y" "b"})

^{:refer xt.lang.common-data/obj-nest :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/obj-keys :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/obj-keys {:a 1, :b 2})))
  => #{"a" "b"})

^{:refer xt.lang.common-data/obj-vals :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/obj-vals {:a 1, :b 2})))
  => #{1 2})

^{:refer xt.lang.common-data/obj-pairs :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/obj-pairs {:a 1, :b 2})))
  => #{["a" 1] ["b" 2]})

^{:refer xt.lang.common-data/obj-clone :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var src {:a 1})
   (var out (xtd/obj-clone src))
   (xt/x:set-key src "b" 2)
   out)
  => {"a" 1})

^{:refer xt.lang.common-data/obj-assign :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/obj-from-pairs :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/get-in :added "4.1"}
(fact "TODO"
  
(!.lua [(xtd/get-in {:a {:b {:c 1}}} ["a" "b"])
        (xtd/get-in {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1])

^{:refer xt.lang.common-data/set-in :added "4.1"}
(fact "TODO"

  [(!.lua
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "b"] 2)
     a)
   (!.lua
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "d"] 2)
     a)]
  => [{"a" {"b" 2}}
      {"a" {"b" {"c" 1}, "d" 2}}])

^{:refer xt.lang.common-data/obj-intersection :added "4.1"}
(fact "TODO"
  
      (set (!.lua (xtd/obj-intersection {:a true, :b true} {:b true, :c true})))
  => #{"b"})

^{:refer xt.lang.common-data/obj-keys-nested :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/obj-keys-nested {:a {:b 1, :c 2}} [])))
  => #{[["a" "b"] 1] [["a" "c"] 2]})

^{:refer xt.lang.common-data/obj-difference :added "4.1"}
(fact "TODO"
  
  (set (!.lua (xtd/obj-difference {:a true, :b true} {:b true, :c true})))
  => #{"c"})

^{:refer xt.lang.common-data/swap-key :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var out {:a 1})
   (xtd/swap-key out "a" (fn [x y] (return (+ x y))) [2])
   out)
  => {"a" 3})

^{:refer xt.lang.common-data/to-flat :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/to-flat [["a" 1] ["b" 2]]))
  => ["a" 1 "b" 2])

^{:refer xt.lang.common-data/from-flat :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/from-flat
    ["a" 1 "b" 2]
    (fn [out k v]
      (xt/x:set-key out k v)
      (return out))
    {}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/eq-nested-basic :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/eq-nested-basic 1 1 nil nil nil)
          (xtd/eq-nested-basic 1 2 nil nil nil)])
  => [true false])

^{:refer xt.lang.common-data/eq-nested-loop :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/eq-nested-loop {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr nil)
    (xtd/eq-nested-loop {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr nil)])
  => [true false])

^{:refer xt.lang.common-data/eq-nested-obj :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/eq-nested-obj {:a 1} {:a 1} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-obj {:a 1} {:a 2} xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false])

^{:refer xt.lang.common-data/eq-nested-arr :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/eq-nested-arr [1 2] [1 2] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))
    (xtd/eq-nested-arr [1 2] [2 1] xtd/eq-nested-obj xtd/eq-nested-arr (xtd/lu-create))])
  => [true false])

^{:refer xt.lang.common-data/eq-nested :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
    (xtd/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
    (xtd/eq-nested [1] [1])
    (xtd/eq-nested [1] [2])])
  => [true false true false])

^{:refer xt.lang.common-data/eq-shallow :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var arr [1])
   [(xtd/eq-shallow arr arr)
    (xtd/eq-shallow [1] [1])])
  => [true false])

^{:refer xt.lang.common-data/tree-walk :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/tree-walk
    {:a 1, :b [2 {:c 3}]}
    (fn [x] (return x))
    (fn [x]
      (if (xt/x:is-number? x)
        (return (+ x 1))
        (return x)))))
  => {"a" 2, "b" [3 {"c" 4}]})

^{:refer xt.lang.common-data/tree-diff :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/tree-diff {:a 1, :b 2} {:a 1, :c 2}))
  => {"c" 2})

^{:refer xt.lang.common-data/tree-diff-nested :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/tree-diff-nested {:a 1, :b 2} {:a 1, :c 2})
    (xtd/tree-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
    (xtd/tree-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
  => [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}])

^{:refer xt.lang.common-data/arr-every :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/arr-every [2 4 6] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-every [2 3 6] (fn [x] (return (== 0 (mod x 2)))))])
  => [true false])

^{:refer xt.lang.common-data/arr-some :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/arr-some [1 3 5] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-some [1 2 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [false true])

^{:refer xt.lang.common-data/arr-each :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var out [])
   (xtd/arr-each [1 2 3] (fn [x] (xt/x:arr-push out (+ x 1))))
   out)
  => [2 3 4])

^{:refer xt.lang.common-data/arr-find :added "4.1"}
(fact "TODO"
  
  (!.lua
   [(xtd/arr-find [1 3 4] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-find [1 3 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [2 -1])

^{:refer xt.lang.common-data/arr-map :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-map [1 2 3] (fn [x] (return (+ x 1)))))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-mapcat :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-mapcat [1 2 3] (fn [x] (return [x x]))))
  => [1 1 2 2 3 3])

^{:refer xt.lang.common-data/arr-partition :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-partition [1 2 3 4 5] 2))
  => [[1 2] [3 4] [5]])

^{:refer xt.lang.common-data/arr-filter :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-filter [1 2 3 4] (fn [x] (return (== 0 (mod x 2))))))
  => [2 4])

^{:refer xt.lang.common-data/arr-keep :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-keep
    [1 2 3 4]
    (fn [x]
      (if (== 0 (mod x 2))
        (return (* x 10))
        (return nil)))))
  => [20 40])

^{:refer xt.lang.common-data/arr-keepf :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-keepf
    [1 2 3 4]
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => [10 30])

^{:refer xt.lang.common-data/arr-juxt :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-juxt
    [1 2]
    (fn [x] (return (xt/x:to-string x)))
    (fn [x] (return (* x x)))))
  => {"1" 1, "2" 4})

^{:refer xt.lang.common-data/arr-foldl :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-foldl [1 2 3] (fn [acc x] (return (+ acc x))) 0))
  => 6)

^{:refer xt.lang.common-data/arr-foldr :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-foldr [1 2 3] (fn [acc x] (return (+ (* acc 10) x))) 0))
  => 321)

^{:refer xt.lang.common-data/arr-pipel :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-pipel
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 4)

^{:refer xt.lang.common-data/arr-piper :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-piper
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 3)

^{:refer xt.lang.common-data/arr-group-by :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-group-by
    [1 2 3 4]
    (fn [x] (return (xt/x:to-string (mod x 2))))
    (fn [x] (return x))))
  => {"0" [2 4], "1" [1 3]})

^{:refer xt.lang.common-data/arr-repeat :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-repeat 1 3))
  => [1 1 1])

^{:refer xt.lang.common-data/arr-normalise :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/arr-normalise [2 3 5]))
  => [0.2 0.3 0.5])

^{:refer xt.lang.common-data/arr-sort :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-sort
    [3 1 2]
    (fn [x] (return x))
    (fn [a b] (return (< a b)))))
  => [1 2 3])

^{:refer xt.lang.common-data/arr-sorted-merge :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/arr-sorted-merge
    [1 3 5]
    [2 4 6]
    (fn [a b] (return (< a b)))))
  => [1 2 3 4 5 6])

^{:refer xt.lang.common-data/obj-map :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-map {:a 1, :b 2} (fn [x] (return (+ x 1)))))
  => {"a" 2, "b" 3})

^{:refer xt.lang.common-data/obj-filter :added "4.1"}
(fact "TODO"
  
  (!.lua (xtd/obj-filter {:a 1, :b 2, :c 3} (fn [x] (return (== 1 (mod x 2))))))
  => {"a" 1, "c" 3})

^{:refer xt.lang.common-data/obj-keep :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/obj-keep
    {:a 1, :b 2, :c 3}
    (fn [x]
      (if (== 0 (mod x 2))
        (return (* x 10))
        (return nil)))))
  => {"b" 20})

^{:refer xt.lang.common-data/obj-keepf :added "4.1"}
(fact "TODO"
  
  (!.lua
   (xtd/obj-keepf
    {:a 1, :b 2, :c 3}
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => {"a" 10, "c" 30})

^{:refer xt.lang.common-data/clone-shallow :added "4.1"}
(fact "TODO"
  
  (!.lua [(xtd/clone-shallow nil)
          (xtd/clone-shallow 1)])
  => [nil 1])

^{:refer xt.lang.common-data/clone-nested-loop :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested-loop src (xtd/lu-create)))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/clone-nested :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested src))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/memoize-key :added "4.1"}
(fact "TODO"
  
  (!.lua
   (var n 0)
   (var f (xtd/memoize-key
           (fn [x]
             (:= n (+ n 1))
             (return (* x 10)))))
   [(f 2) (f 2) (f 3) n])
  => [20 20 30 2])
