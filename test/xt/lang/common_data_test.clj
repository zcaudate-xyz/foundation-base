(ns xt.lang.common-data-test
 (:require [std.lang :as l])
 (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic,
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic,
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic,
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-data/not-empty? :added "4.1"}
(fact "checks that array is not empty"

  (!.js
   [(xtd/not-empty? nil)
    (xtd/not-empty? "")
    (xtd/not-empty? "123")
    (xtd/not-empty? [])
    (xtd/not-empty? [1 2 3])
    (xtd/not-empty? {})
    (xtd/not-empty? {:a 1, :b 2})])
  => [false false true false true false true]

  (!.lua
   [(xtd/not-empty? nil)
    (xtd/not-empty? "")
    (xtd/not-empty? "123")
    (xtd/not-empty? [])
    (xtd/not-empty? [1 2 3])
    (xtd/not-empty? {})
    (xtd/not-empty? {:a 1, :b 2})])
  => [false false true false true false true]

  (!.py
   [(xtd/not-empty? nil)
    (xtd/not-empty? "")
    (xtd/not-empty? "123")
    (xtd/not-empty? [])
    (xtd/not-empty? [1 2 3])
    (xtd/not-empty? {})
    (xtd/not-empty? {:a 1, :b 2})])
  => [false false true false true false true])

^{:refer xt.lang.common-data/lu-create :added "4.1"}
(fact "creates a lookup table"

  (!.js
   (var lu (xtd/lu-create))
   (xtd/lu-get lu "missing"))
  => nil

  (!.lua
   (var lu (xtd/lu-create))
   (xtd/lu-get lu "missing"))
  => nil

  (!.py
   (var lu (xtd/lu-create))
   (xtd/lu-get lu "missing"))
  => nil)

^{:refer xt.lang.common-data/lu-del :added "4.1"}
(fact "deletes a lookup entry"

  (!.js
    (var lu (xtd/lu-create))
    (xtd/lu-set lu "a" 1)
    (xtd/lu-del lu "a")
    (xtd/lu-get lu "a"))
  => nil

  (!.lua
    (var lu (xtd/lu-create))
    (xtd/lu-set lu "a" 1)
    (xtd/lu-del lu "a")
    (xtd/lu-get lu "a"))
  => nil

  (!.py
    (var lu (xtd/lu-create))
    (xtd/lu-set lu "a" 1)
    (xtd/lu-del lu "a")
    (xtd/lu-get lu "a"))
  => nil)

^{:refer xt.lang.common-data/lu-get :added "4.1"}
(fact "gets a lookup entry"

  (!.js
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 1)
   (xtd/lu-get lu "a"))
  => 1

  (!.js
    (var lu (xtd/lu-create))
    (xtd/lu-set lu lu 2)
    (xtd/lu-get lu lu))
  => 2

  (!.lua
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 1)
   (xtd/lu-get lu "a"))
  => 1

  (!.lua
    (var lu (xtd/lu-create))
    (xtd/lu-set lu lu 2)
    (xtd/lu-get lu lu))
  => 2

  (!.py
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 1)
   (xtd/lu-get lu "a"))
  => 1

  (!.py
    (var lu (xtd/lu-create))
    (xtd/lu-set lu lu 2)
    (xtd/lu-get lu lu))
  => 2)

^{:refer xt.lang.common-data/lu-set :added "4.1"}
(fact "sets a lookup entry"

  (!.js
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 2)
   (xtd/lu-get lu "a"))
  => 2

  (!.lua
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 2)
   (xtd/lu-get lu "a"))
  => 2

  (!.py
   (var lu (xtd/lu-create))
   (xtd/lu-set lu "a" 2)
   (xtd/lu-get lu "a"))
  => 2)

^{:refer xt.lang.common-data/lu-eq :added "4.1"}
(fact "checks lookup equality"

  (!.js
   (var lu0 (xtd/lu-create))
   (var lu1 (xtd/lu-create))
   [(xtd/lu-eq lu0 lu0)
    (xtd/lu-eq lu0 lu1)])
  => [true false]

  (!.lua
   (var lu0 (xtd/lu-create))
   (var lu1 (xtd/lu-create))
   [(xtd/lu-eq lu0 lu0)
    (xtd/lu-eq lu0 lu1)])
  => [true false]

  (!.py
   (var lu0 (xtd/lu-create))
   (var lu1 (xtd/lu-create))
   [(xtd/lu-eq lu0 lu0)
    (xtd/lu-eq lu0 lu1)])
  => [true false])

^{:refer xt.lang.common-data/first :added "4.1"}
(fact "first"

  (!.js (xtd/first [1 2 3]))
  => 1

  (!.lua (xtd/first [1 2 3]))
  => 1

  (!.py (xtd/first [1 2 3]))
  => 1)

^{:refer xt.lang.common-data/second :added "4.1"}
(fact "gets the second item"

  (!.js (xtd/second [1 2 3]))
  => 2

  (!.lua (xtd/second [1 2 3]))
  => 2

  (!.py (xtd/second [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/nth :added "4.1"}
(fact "gets the nth item (index 0)"

  (!.js [(xtd/nth [1 2 3] 0)
          (xtd/nth [1 2 3] 2)])
  => [1 3]

  (!.lua [(xtd/nth [1 2 3] 0)
          (xtd/nth [1 2 3] 2)])
  => [1 3]

  (!.py [(xtd/nth [1 2 3] 0)
          (xtd/nth [1 2 3] 2)])
  => [1 3])

^{:refer xt.lang.common-data/last :added "4.1"}
(fact "gets the last item"

  (!.js (xtd/last [1 2 3]))
  => 3

  (!.lua (xtd/last [1 2 3]))
  => 3

  (!.py (xtd/last [1 2 3]))
  => 3)

^{:refer xt.lang.common-data/second-last :added "4.1"}
(fact "gets the second-last item"

  (!.js (xtd/second-last [1 2 3]))
  => 2

  (!.lua (xtd/second-last [1 2 3]))
  => 2

  (!.py (xtd/second-last [1 2 3]))
  => 2)

^{:refer xt.lang.common-data/arr-empty? :added "4.1"}
(fact "checks that arrect is empty"

  (!.js [(xtd/arr-empty? nil)
          (xtd/arr-empty? [])
          (xtd/arr-empty? [1])])
  => [true true false]

  (!.lua [(xtd/arr-empty? nil)
          (xtd/arr-empty? [])
          (xtd/arr-empty? [1])])
  => [true true false]

  (!.py [(xtd/arr-empty? nil)
          (xtd/arr-empty? [])
          (xtd/arr-empty? [1])])
  => [true true false])

^{:refer xt.lang.common-data/arr-not-empty? :added "4.1"}
(fact "checks that arrect is not empty"

  (!.js [(xtd/arr-not-empty? nil)
          (xtd/arr-not-empty? [])
          (xtd/arr-not-empty? [1])])
  => [false false true]

  (!.lua [(xtd/arr-not-empty? nil)
          (xtd/arr-not-empty? [])
          (xtd/arr-not-empty? [1])])
  => [false false true]

  (!.py [(xtd/arr-not-empty? nil)
          (xtd/arr-not-empty? [])
          (xtd/arr-not-empty? [1])])
  => [false false true])

^{:refer xt.lang.common-data/arrayify :added "4.1"}
(fact "makes something into an array"

  (!.js [(xtd/arrayify 1)
          (xtd/arrayify [1])])
  => [[1] [1]]

  (!.lua [(xtd/arrayify 1)
          (xtd/arrayify [1])])
  => [[1] [1]]

  (!.py [(xtd/arrayify 1)
          (xtd/arrayify [1])])
  => [[1] [1]])

^{:refer xt.lang.common-data/arr-lookup :added "4.1"}
(fact "constructs a lookup given keys"

  (!.js (xtd/obj-keys (xtd/arr-lookup ["a" "b"])))
  => (just ["a" "b"] :in-any-order)

  (!.lua (xtd/obj-keys (xtd/arr-lookup ["a" "b"])))
  => (just ["a" "b"] :in-any-order)

  (!.py (xtd/obj-keys (xtd/arr-lookup ["a" "b"])))
  => (just ["a" "b"] :in-any-order))

^{:refer xt.lang.common-data/arr-omit :added "4.1"}
(fact "emits index from new array"

  (!.js (xtd/arr-omit [1 2 3] 1))
  => [1 3]

  (!.lua (xtd/arr-omit [1 2 3] 1))
  => [1 3]

  (!.py (xtd/arr-omit [1 2 3] 1))
  => [1 3])

^{:refer xt.lang.common-data/arr-reverse :added "4.1"}
(fact "reverses the array"

  (!.js (xtd/arr-reverse [1 2 3]))
  => [3 2 1]

  (!.lua (xtd/arr-reverse [1 2 3]))
  => [3 2 1]

  (!.py (xtd/arr-reverse [1 2 3]))
  => [3 2 1])

^{:refer xt.lang.common-data/arr-zip :added "4.1"}
(fact "zips two arrays together into a map"

  (!.js (xtd/arr-zip ["a" "b"] [1 2]))
  => {"a" 1, "b" 2}

  (!.lua (xtd/arr-zip ["a" "b"] [1 2]))
  => {"a" 1, "b" 2}

  (!.py (xtd/arr-zip ["a" "b"] [1 2]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/arr-clone :added "4.1"}
(fact "clones an array"

  (!.js
   (var src [1 2])
   (var out (xtd/arr-clone src))
   (xt/x:arr-push src 3)
   out)
  => [1 2]

  (!.lua
   (var src [1 2])
   (var out (xtd/arr-clone src))
   (xt/x:arr-push src 3)
   out)
  => [1 2]

  (!.py
   (var src [1 2])
   (var out (xtd/arr-clone src))
   (xt/x:arr-push src 3)
   out)
  => [1 2])

^{:refer xt.lang.common-data/arr-assign :added "4.1"}
(fact "appends to the end of an array"

  (!.js (xtd/arr-assign [1 2] [3 4]))
  => [1 2 3 4]

  (!.lua (xtd/arr-assign [1 2] [3 4]))
  => [1 2 3 4]

  (!.py (xtd/arr-assign [1 2] [3 4]))
  => [1 2 3 4])

^{:refer xt.lang.common-data/arr-concat :added "4.1"}
(fact "concatenates arrays without mutating the input"

  (!.js
   (var src [1 2])
   [(xtd/arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]]

  (!.lua
   (var src [1 2])
   [(xtd/arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]]

  (!.py
   (var src [1 2])
   [(xtd/arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]])

^{:refer xt.lang.common-data/arr-slice :added "4.1"}
(fact "slices an array"

  (!.js (xtd/arr-slice [1 2 3 4] 1 3))
  => [2 3]

  (!.lua (xtd/arr-slice [1 2 3 4] 1 3))
  => [2 3]

  (!.py (xtd/arr-slice [1 2 3 4] 1 3))
  => [2 3])

^{:refer xt.lang.common-data/arr-rslice :added "4.1"}
(fact "gets the reverse of a slice"

  (!.js (xtd/arr-rslice [1 2 3 4] 1 3))
  => [3 2]

  (!.lua (xtd/arr-rslice [1 2 3 4] 1 3))
  => [3 2]

  (!.py (xtd/arr-rslice [1 2 3 4] 1 3))
  => [3 2])

^{:refer xt.lang.common-data/arr-tail :added "4.1" :lang-exceptions {:dart {:skip true}}}
(fact "gets the tail of the array"

  (!.js (xtd/arr-tail [1 2 3 4] 2))
  => [4 3]

  (!.lua (xtd/arr-tail [1 2 3 4] 2))
  => [4 3]

  (!.py (xtd/arr-tail [1 2 3 4] 2))
  => [4 3])

^{:refer xt.lang.common-data/arr-range :added "4.1"}
(fact "creates a range array"

  (!.js [(xtd/arr-range 5)
          (xtd/arr-range [2 6])
          (xtd/arr-range [2 7 2])])
  => [[0 1 2 3 4] [2 3 4 5] [2 4 6]]

  (!.lua [(xtd/arr-range 5)
          (xtd/arr-range [2 6])
          (xtd/arr-range [2 7 2])])
  => [[0 1 2 3 4] [2 3 4 5] [2 4 6]]

  (!.py [(xtd/arr-range 5)
          (xtd/arr-range [2 6])
          (xtd/arr-range [2 7 2])])
  => [[0 1 2 3 4] [2 3 4 5] [2 4 6]])

^{:refer xt.lang.common-data/arr-intersection :added "4.1"}
(fact "gets the intersection of two arrays"

  (!.js (xtd/arr-intersection [1 2 3] [2 3 4]))
  => [2 3]

  (!.lua (xtd/arr-intersection [1 2 3] [2 3 4]))
  => [2 3]

  (!.py (xtd/arr-intersection [1 2 3] [2 3 4]))
  => [2 3])

^{:refer xt.lang.common-data/arr-difference :added "4.1"}
(fact "gets the difference of two arrays"

  (!.js (xtd/arr-difference [1 2] [2 3 4]))
  => [3 4]

  (!.lua (xtd/arr-difference [1 2] [2 3 4]))
  => [3 4]

  (!.py (xtd/arr-difference [1 2] [2 3 4]))
  => [3 4])

^{:refer xt.lang.common-data/arr-union :added "4.1"}
(fact "gets the union of two arrays"

  (!.js (xtd/arr-union [1 2] [2 3]))
  => (contains [1 2 3] :in-any-order)

  (!.lua (xtd/arr-union [1 2] [2 3]))
  => (contains [1 2 3] :in-any-order)

  (!.py (xtd/arr-union [1 2] [2 3]))
  => (contains [1 2 3] :in-any-order))

^{:refer xt.lang.common-data/arr-shuffle :added "4.1"}
(fact "shuffles the array"

  (!.js (xtd/arr-shuffle [1 2 3]))
  => (contains [1 2 3] :in-any-order)

  (!.lua (xtd/arr-shuffle [1 2 3]))
  => (contains [1 2 3] :in-any-order)

  (!.py (xtd/arr-shuffle [1 2 3]))
  => (contains [1 2 3] :in-any-order))

^{:refer xt.lang.common-data/arr-pushl :added "4.1"}
(fact "pushs an element into array"

  (!.js (xtd/arr-pushl [1 2 3] 4 3))
  => [2 3 4]

  (!.lua (xtd/arr-pushl [1 2 3] 4 3))
  => [2 3 4]

  (!.py (xtd/arr-pushl [1 2 3] 4 3))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-pushr :added "4.1"}
(fact "pushs an element into array"

  (!.js (xtd/arr-pushr [1 2 3] 0 3))
  => [0 1 2]

  (!.lua (xtd/arr-pushr [1 2 3] 0 3))
  => [0 1 2]

  (!.py (xtd/arr-pushr [1 2 3] 0 3))
  => [0 1 2])

^{:refer xt.lang.common-data/arr-interpose :added "4.1"}
(fact "puts element between array"

  (!.js (xtd/arr-interpose [1 2 3] 0))
  => [1 0 2 0 3]

  (!.lua (xtd/arr-interpose [1 2 3] 0))
  => [1 0 2 0 3]

  (!.py (xtd/arr-interpose [1 2 3] 0))
  => [1 0 2 0 3])

^{:refer xt.lang.common-data/arr-random :added "4.1"}
(fact "gets a random element from array"

  (!.js (xtd/arr-random [1]))
  => 1

  (!.lua (xtd/arr-random [1]))
  => 1

  (!.py (xtd/arr-random [1]))
  => 1)

^{:refer xt.lang.common-data/arr-sample :added "4.1"}
(fact "samples array according to probability"

  (!.js (xtd/arr-sample ["a" "b" "c"] [0 1 0]))
  => "b"

  (!.lua (xtd/arr-sample ["a" "b" "c"] [0 1 0]))
  => "b"

  (!.py (xtd/arr-sample ["a" "b" "c"] [0 1 0]))
  => "b")

^{:refer xt.lang.common-data/obj-empty? :added "4.1"}
(fact "checks that object is empty"

  (!.js [(xtd/obj-empty? {})
          (xtd/obj-empty? {:a 1})])
  => [true false]

  (!.lua [(xtd/obj-empty? {})
          (xtd/obj-empty? {:a 1})])
  => [true false]

  (!.py [(xtd/obj-empty? {})
          (xtd/obj-empty? {:a 1})])
  => [true false])

^{:refer xt.lang.common-data/obj-not-empty? :added "4.1"}
(fact "checks that object is not empty"

  (!.js [(xtd/obj-not-empty? {})
          (xtd/obj-not-empty? {:a 1})])
  => [false true]

  (!.lua [(xtd/obj-not-empty? {})
          (xtd/obj-not-empty? {:a 1})])
  => [false true]

  (!.py [(xtd/obj-not-empty? {})
          (xtd/obj-not-empty? {:a 1})])
  => [false true])

^{:refer xt.lang.common-data/obj-first-key :added "4.1"}
(fact "gets the first key"

  (!.js (xtd/obj-first-key {:a 1}))
  => "a"

  (!.lua (xtd/obj-first-key {:a 1}))
  => "a"

  (!.py (xtd/obj-first-key {:a 1}))
  => "a")

^{:refer xt.lang.common-data/obj-first-val :added "4.1"}
(fact "gets the first val"

  (!.js (xtd/obj-first-val {:a 1}))
  => 1

  (!.lua (xtd/obj-first-val {:a 1}))
  => 1

  (!.py (xtd/obj-first-val {:a 1}))
  => 1)

^{:refer xt.lang.common-data/obj-assign-nested :added "4.1"}
(fact "merges objects at a nesting level"

  (!.js (xtd/obj-assign-nested {:a {:b 1}} {:a {:c 2}}))
  => {"a" {"b" 1, "c" 2}}

  (!.lua (xtd/obj-assign-nested {:a {:b 1}} {:a {:c 2}}))
  => {"a" {"b" 1, "c" 2}}

  (!.py (xtd/obj-assign-nested {:a {:b 1}} {:a {:c 2}}))
  => {"a" {"b" 1, "c" 2}})

^{:refer xt.lang.common-data/obj-assign-with :added "4.1"}
(fact "merges second into first given a function"

  (!.js
   (xtd/obj-assign-with
    {:a 1, :b 2}
    {:a 3, :c 4}
    (fn [x y] (return (+ x y)))))
  => {"a" 4, "b" 2, "c" 4}

  (!.lua
   (xtd/obj-assign-with
    {:a 1, :b 2}
    {:a 3, :c 4}
    (fn [x y] (return (+ x y)))))
  => {"a" 4, "b" 2, "c" 4}

  (!.py
   (xtd/obj-assign-with
    {:a 1, :b 2}
    {:a 3, :c 4}
    (fn [x y] (return (+ x y)))))
  => {"a" 4, "b" 2, "c" 4})

^{:refer xt.lang.common-data/obj-del :added "4.1"}
(fact "deletes multiple keys"

  (!.js (xtd/obj-del {:a 1, :b 2, :c 3} ["a" "c"]))
  => {"b" 2}

  (!.lua (xtd/obj-del {:a 1, :b 2, :c 3} ["a" "c"]))
  => {"b" 2}

  (!.py (xtd/obj-del {:a 1, :b 2, :c 3} ["a" "c"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-del-all :added "4.1" :lang-exceptions {:python {:skip true}}}
(fact "obj del all"

  (!.js
    (var out {:a 1, :b 2})
    (xtd/obj-del-all out)
    out)
  => {}

  (!.lua
    (var out {:a 1, :b 2})
    (xtd/obj-del-all out)
    out)
  => {}

  (!.py
    (var out {:a 1, :b 2})
    (xtd/obj-del-all out)
    out)
  => {})

^{:refer xt.lang.common-data/obj-pick :added "4.1"}
(fact "select keys in object"

  (!.js (xtd/obj-pick {:a 1, :b 2} ["a"]))
  => {"a" 1}

  (!.lua (xtd/obj-pick {:a 1, :b 2} ["a"]))
  => {"a" 1}

  (!.py (xtd/obj-pick {:a 1, :b 2} ["a"]))
  => {"a" 1})

^{:refer xt.lang.common-data/obj-omit :added "4.1"}
(fact "new object with missing keys"

  (!.js (xtd/obj-omit {:a 1, :b 2} ["a"]))
  => {"b" 2}

  (!.lua (xtd/obj-omit {:a 1, :b 2} ["a"]))
  => {"b" 2}

  (!.py (xtd/obj-omit {:a 1, :b 2} ["a"]))
  => {"b" 2})

^{:refer xt.lang.common-data/obj-transpose :added "4.1"}
(fact "obj-transposes a map"

  (!.js (xtd/obj-transpose {:a "x", :b "y"}))
  => {"x" "a", "y" "b"}

  (!.lua (xtd/obj-transpose {:a "x", :b "y"}))
  => {"x" "a", "y" "b"}

  (!.py (xtd/obj-transpose {:a "x", :b "y"}))
  => {"x" "a", "y" "b"})

^{:refer xt.lang.common-data/obj-nest :added "4.1"}
(fact "creates a nested object"

  (!.js (xtd/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}}

  (!.lua (xtd/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}}

  (!.py (xtd/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/obj-keys :added "4.1"}
(fact "gets keys of an object"

  (!.js (xtd/obj-keys {:a 1, :b 2}))
  => (just ["a" "b"] :in-any-order)

  (!.lua (xtd/obj-keys {:a 1, :b 2}))
  => (just ["a" "b"] :in-any-order)

  (!.py (xtd/obj-keys {:a 1, :b 2}))
  => (just ["a" "b"] :in-any-order))

^{:refer xt.lang.common-data/obj-vals :added "4.1"}
(fact "gets vals of an object"

  (!.js (xtd/obj-vals {:a 1, :b 2}))
  => (just [1 2] :in-any-order)

  (!.lua (xtd/obj-vals {:a 1, :b 2}))
  => (just [1 2] :in-any-order)

  (!.py (xtd/obj-vals {:a 1, :b 2}))
  => (just [1 2] :in-any-order))

^{:refer xt.lang.common-data/obj-pairs :added "4.1"}
(fact "creates entry pairs from object"

  (!.js (xtd/obj-pairs {:a 1, :b 2}))
  => (just [["a" 1] ["b" 2]] :in-any-order)

  (!.lua (xtd/obj-pairs {:a 1, :b 2}))
  => (just [["a" 1] ["b" 2]] :in-any-order)

  (!.py (xtd/obj-pairs {:a 1, :b 2}))
  => (just [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.common-data/obj-clone :added "4.1"}
(fact "clones an object"

  (!.js
   (var src {:a 1})
   (var out (xtd/obj-clone src))
   (xt/x:set-key src "b" 2)
   out)
  => {"a" 1}

  (!.lua
   (var src {:a 1})
   (var out (xtd/obj-clone src))
   (xt/x:set-key src "b" 2)
   out)
  => {"a" 1}

  (!.py
   (var src {:a 1})
   (var out (xtd/obj-clone src))
   (xt/x:set-key src "b" 2)
   out)
  => {"a" 1})

^{:refer xt.lang.common-data/obj-assign :added "4.1"}
(fact "merges key value pairs from into another"

  (!.js (xtd/obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2}

  (!.lua (xtd/obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2}

  (!.py (xtd/obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/obj-from-pairs :added "4.1"}
(fact "creates an object from pairs"

  (!.js (xtd/obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2}

  (!.lua (xtd/obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2}

  (!.py (xtd/obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/get-in :added "4.1"}
(fact "gets item in object"

  (!.js [(xtd/get-in {:a {:b {:c 1}}} ["a" "b"])
          (xtd/get-in {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1]

  (!.lua [(xtd/get-in {:a {:b {:c 1}}} ["a" "b"])
          (xtd/get-in {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1]

  (!.py [(xtd/get-in {:a {:b {:c 1}}} ["a" "b"])
          (xtd/get-in {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1])

^{:refer xt.lang.common-data/set-in :added "4.1"}
(fact "sets item in object"

  [(!.js
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "b"] 2)
     a)
   (!.js
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "d"] 2)
     a)]
  => [{"a" {"b" 2}}
      {"a" {"b" {"c" 1}, "d" 2}}]

  [(!.lua
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "b"] 2)
     a)
   (!.lua
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "d"] 2)
     a)]
  => [{"a" {"b" 2}}
      {"a" {"b" {"c" 1}, "d" 2}}]

  [(!.py
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "b"] 2)
     a)
   (!.py
     (var a {:a {:b {:c 1}}})
     (xtd/set-in a ["a" "d"] 2)
     a)]
  => [{"a" {"b" 2}}
      {"a" {"b" {"c" 1}, "d" 2}}])

^{:refer xt.lang.common-data/obj-intersection :added "4.1"}
(fact "finds the intersection between map lookups"

  (!.js (xtd/obj-intersection {:a true, :b true} {:b true, :c true}))
  => ["b"]

  (!.lua (xtd/obj-intersection {:a true, :b true} {:b true, :c true}))
  => ["b"]

  (!.py (xtd/obj-intersection {:a true, :b true} {:b true, :c true}))
  => ["b"])

^{:refer xt.lang.common-data/obj-keys-nested :added "4.1"}
(fact "gets nested keys"

  (!.js (xtd/obj-keys-nested {:a {:b 1, :c 2}} []))
  => (just [[["a" "b"] 1]
            [["a" "c"] 2]]
           :in-any-order)

  (!.lua (xtd/obj-keys-nested {:a {:b 1, :c 2}} []))
  => (just [[["a" "b"] 1]
            [["a" "c"] 2]]
           :in-any-order)

  (!.py (xtd/obj-keys-nested {:a {:b 1, :c 2}} []))
  => (just [[["a" "b"] 1]
            [["a" "c"] 2]]
           :in-any-order))

^{:refer xt.lang.common-data/obj-difference :added "4.1"}
(fact "finds the difference between two map lookups"

  (!.js (xtd/obj-difference {:a true, :b true} {:b true, :c true}))
  => ["c"]

  (!.lua (xtd/obj-difference {:a true, :b true} {:b true, :c true}))
  => ["c"]

  (!.py (xtd/obj-difference {:a true, :b true} {:b true, :c true}))
  => ["c"])

^{:refer xt.lang.common-data/swap-key :added "4.1"}
(fact "swaps a value in the key with a function"

  (!.js
   (var out {:a 1})
   (xtd/swap-key out "a" (fn [x y] (return (+ x y))) [2])
   out)
  => {"a" 3}

  (!.lua
   (var out {:a 1})
   (xtd/swap-key out "a" (fn [x y] (return (+ x y))) [2])
   out)
  => {"a" 3}

  (!.py
   (var out {:a 1})
   (xtd/swap-key out "a" (fn [x y] (return (+ x y))) [2])
   out)
  => {"a" 3})

^{:refer xt.lang.common-data/to-flat :added "4.1"}
(fact "flattens pairs of object into array"

  (!.js (xtd/to-flat [["a" 1] ["b" 2]]))
  => ["a" 1 "b" 2]

  (!.lua (xtd/to-flat [["a" 1] ["b" 2]]))
  => ["a" 1 "b" 2]

  (!.py (xtd/to-flat [["a" 1] ["b" 2]]))
  => ["a" 1 "b" 2])

^{:refer xt.lang.common-data/from-flat :added "4.1"}
(fact "creates object from flattened pair array"

  (!.js
   (xtd/from-flat
     ["a" 1 "b" 2]
     xtd/set-pair-step
     {}))
  => {"a" 1, "b" 2}

  (!.lua
   (xtd/from-flat
     ["a" 1 "b" 2]
     xtd/set-pair-step
     {}))
  => {"a" 1, "b" 2}

  (!.py
   (xtd/from-flat
     ["a" 1 "b" 2]
     xtd/set-pair-step
     {}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-data/arr-every :added "4.1"}
(fact "checks that every element fulfills thet predicate"

  (!.js
   [(xtd/arr-every [2 4 6] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-every [2 3 6] (fn [x] (return (== 0 (mod x 2)))))])
  => [true false]

  (!.lua
   [(xtd/arr-every [2 4 6] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-every [2 3 6] (fn [x] (return (== 0 (mod x 2)))))])
  => [true false]

  (!.py
   [(xtd/arr-every [2 4 6] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-every [2 3 6] (fn [x] (return (== 0 (mod x 2)))))])
  => [true false])

^{:refer xt.lang.common-data/arr-some :added "4.1"}
(fact "checks that the array contains an element"

  (!.js
   [(xtd/arr-some [1 3 5] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-some [1 2 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [false true]

  (!.lua
   [(xtd/arr-some [1 3 5] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-some [1 2 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [false true]

  (!.py
   [(xtd/arr-some [1 3 5] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-some [1 2 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [false true])

^{:refer xt.lang.common-data/arr-each :added "4.1"}
(fact "performs a function call for each element"

  (!.js
   (var out [])
   (xtd/arr-each [1 2 3] (fn [x] (xt/x:arr-push out (+ x 1))))
   out)
  => [2 3 4]

  (!.lua
   (var out [])
   (xtd/arr-each [1 2 3] (fn [x] (xt/x:arr-push out (+ x 1))))
   out)
  => [2 3 4]

  (!.py
   (var out [])
   (xtd/arr-each [1 2 3] (fn [x] (xt/x:arr-push out (+ x 1))))
   out)
  => [2 3 4])

^{:refer xt.lang.common-data/arr-find :added "4.1"}
(fact "finds first index matching predicate"

  (!.js
   [(xtd/arr-find [1 3 4] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-find [1 3 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [2 -1]

  (!.lua
   [(xtd/arr-find [1 3 4] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-find [1 3 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [2 -1]

  (!.py
   [(xtd/arr-find [1 3 4] (fn [x] (return (== 0 (mod x 2)))))
    (xtd/arr-find [1 3 5] (fn [x] (return (== 0 (mod x 2)))))])
  => [2 -1])

^{:refer xt.lang.common-data/arr-map :added "4.1"}
(fact "maps a function across an array"

  (!.js (xtd/arr-map [1 2 3] (fn [x] (return (+ x 1)))))
  => [2 3 4]

  (!.lua (xtd/arr-map [1 2 3] (fn [x] (return (+ x 1)))))
  => [2 3 4]

  (!.py (xtd/arr-map [1 2 3] (fn [x] (return (+ x 1)))))
  => [2 3 4])

^{:refer xt.lang.common-data/arr-mapcat :added "4.1"}
(fact "maps an array function, concatenting results"

  (!.js (xtd/arr-mapcat [1 2 3] (fn [x] (return [x x]))))
  => [1 1 2 2 3 3]

  (!.lua (xtd/arr-mapcat [1 2 3] (fn [x] (return [x x]))))
  => [1 1 2 2 3 3]

  (!.py (xtd/arr-mapcat [1 2 3] (fn [x] (return [x x]))))
  => [1 1 2 2 3 3])

^{:refer xt.lang.common-data/arr-partition :added "4.1"}
(fact "partitions an array into arrays of length n"

  (!.js (xtd/arr-partition [1 2 3 4 5] 2))
  => [[1 2] [3 4] [5]]

  (!.lua (xtd/arr-partition [1 2 3 4 5] 2))
  => [[1 2] [3 4] [5]]

  (!.py (xtd/arr-partition [1 2 3 4 5] 2))
  => [[1 2] [3 4] [5]])

^{:refer xt.lang.common-data/arr-filter :added "4.1"}
(fact "applies a filter across an array"

  (!.js (xtd/arr-filter [1 2 3 4] (fn [x] (return (== 0 (mod x 2))))))
  => [2 4]

  (!.lua (xtd/arr-filter [1 2 3 4] (fn [x] (return (== 0 (mod x 2))))))
  => [2 4]

  (!.py (xtd/arr-filter [1 2 3 4] (fn [x] (return (== 0 (mod x 2))))))
  => [2 4])

^{:refer xt.lang.common-data/arr-keep :added "4.1" :lang-exceptions {:python {:skip true}}}
(fact "keeps items in an array if output is not nil"

  (!.js
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/arr-keep
     [1 2 3 4]
     x5-fn))
  => [20 40]

  (!.lua
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/arr-keep
     [1 2 3 4]
     x5-fn))
  => [20 40]

  (!.py
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/arr-keep
     [1 2 3 4]
     x5-fn))
  => [20 40])

^{:refer xt.lang.common-data/arr-keepf :added "4.1"}
(fact "keeps items in an array with transform if predicate holds"

  (!.js
   (xtd/arr-keepf
    [1 2 3 4]
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => [10 30]

  (!.lua
   (xtd/arr-keepf
    [1 2 3 4]
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => [10 30]

  (!.py
   (xtd/arr-keepf
    [1 2 3 4]
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => [10 30])

^{:refer xt.lang.common-data/arr-juxt :added "4.1"}
(fact "constructs a map given a array of pairs"

  (!.js
   (xtd/arr-juxt
    [1 2]
    (fn [x] (return (xt/x:to-string x)))
    (fn [x] (return (* x x)))))
  => {"1" 1, "2" 4}

  (!.lua
   (xtd/arr-juxt
    [1 2]
    (fn [x] (return (xt/x:to-string x)))
    (fn [x] (return (* x x)))))
  => {"1" 1, "2" 4}

  (!.py
   (xtd/arr-juxt
    [1 2]
    (fn [x] (return (xt/x:to-string x)))
    (fn [x] (return (* x x)))))
  => {"1" 1, "2" 4})

^{:refer xt.lang.common-data/arr-foldl :added "4.1"}
(fact "performs reduce on an array"

  (!.js (xtd/arr-foldl [1 2 3] (fn [acc x] (return (+ acc x))) 0))
  => 6

  (!.lua (xtd/arr-foldl [1 2 3] (fn [acc x] (return (+ acc x))) 0))
  => 6

  (!.py (xtd/arr-foldl [1 2 3] (fn [acc x] (return (+ acc x))) 0))
  => 6)

^{:refer xt.lang.common-data/arr-foldr :added "4.1"}
(fact "performs right reduce"

  (!.js (xtd/arr-foldr [1 2 3] (fn [acc x] (return (+ (* acc 10) x))) 0))
  => 321

  (!.lua (xtd/arr-foldr [1 2 3] (fn [acc x] (return (+ (* acc 10) x))) 0))
  => 321

  (!.py (xtd/arr-foldr [1 2 3] (fn [acc x] (return (+ (* acc 10) x))) 0))
  => 321)

^{:refer xt.lang.common-data/arr-pipel :added "4.1"}
(fact "thrushes an input through a function pipeline"

  (!.js
   (xtd/arr-pipel
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 4

  (!.lua
   (xtd/arr-pipel
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 4

  (!.py
   (xtd/arr-pipel
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 4)

^{:refer xt.lang.common-data/arr-piper :added "4.1"}
(fact "thrushes an input through a function pipeline from reverse"

  (!.js
   (xtd/arr-piper
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 3

  (!.lua
   (xtd/arr-piper
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 3

  (!.py
   (xtd/arr-piper
    [(fn [x] (return (+ x 1)))
     (fn [x] (return (* x 2)))]
    1))
  => 3)

^{:refer xt.lang.common-data/arr-group-by :added "4.1"}
(fact "groups elements by key and view functions"

  (!.js
   (xtd/arr-group-by
    [1 2 3 4]
    (fn [x] (return (xt/x:to-string (mod x 2))))
    (fn [x] (return x))))
  => {"0" [2 4], "1" [1 3]}

  (!.lua
   (xtd/arr-group-by
    [1 2 3 4]
    (fn [x] (return (xt/x:to-string (mod x 2))))
    (fn [x] (return x))))
  => {"0" [2 4], "1" [1 3]}

  (!.py
   (xtd/arr-group-by
    [1 2 3 4]
    (fn [x] (return (xt/x:to-string (mod x 2))))
    (fn [x] (return x))))
  => {"0" [2 4], "1" [1 3]})

^{:refer xt.lang.common-data/arr-repeat :added "4.1"}
(fact "repeat function or value n times"

  (!.js (xtd/arr-repeat 1 3))
  => [1 1 1]

  (!.lua (xtd/arr-repeat 1 3))
  => [1 1 1]

  (!.py (xtd/arr-repeat 1 3))
  => [1 1 1])

^{:refer xt.lang.common-data/arr-normalise :added "4.1"}
(fact "normalises array elements to 1"

  (!.js (xtd/arr-normalise [2 3 5]))
  => [0.2 0.3 0.5]

  (!.lua (xtd/arr-normalise [2 3 5]))
  => [0.2 0.3 0.5]

  (!.py (xtd/arr-normalise [2 3 5]))
  => [0.2 0.3 0.5])

^{:refer xt.lang.common-data/arr-sort :added "4.1"}
(fact "arr-sort using key function and comparator"

  (!.js
   (xtd/arr-sort
    [3 1 2]
    (fn [x] (return x))
    (fn [a b] (return (< a b)))))
  => [1 2 3]

  (!.lua
   (xtd/arr-sort
    [3 1 2]
    (fn [x] (return x))
    (fn [a b] (return (< a b)))))
  => [1 2 3]

  (!.py
   (xtd/arr-sort
    [3 1 2]
    (fn [x] (return x))
    (fn [a b] (return (< a b)))))
  => [1 2 3])

^{:refer xt.lang.common-data/arr-sorted-merge :added "4.1"}
(fact "performs a merge on two sorted arrays"

  (!.js
   (xtd/arr-sorted-merge
    [1 3 5]
    [2 4 6]
    (fn [a b] (return (< a b)))))
  => [1 2 3 4 5 6]

  (!.lua
   (xtd/arr-sorted-merge
    [1 3 5]
    [2 4 6]
    (fn [a b] (return (< a b)))))
  => [1 2 3 4 5 6]

  (!.py
   (xtd/arr-sorted-merge
    [1 3 5]
    [2 4 6]
    (fn [a b] (return (< a b)))))
  => [1 2 3 4 5 6])

^{:refer xt.lang.common-data/obj-map :added "4.1"}
(fact "maps a function across the values of an object"

  (!.js (xtd/obj-map {:a 1, :b 2} (fn [x] (return (+ x 1)))))
  => {"a" 2, "b" 3}

  (!.lua (xtd/obj-map {:a 1, :b 2} (fn [x] (return (+ x 1)))))
  => {"a" 2, "b" 3}

  (!.py (xtd/obj-map {:a 1, :b 2} (fn [x] (return (+ x 1)))))
  => {"a" 2, "b" 3})

^{:refer xt.lang.common-data/obj-filter :added "4.1"}
(fact "applies a filter across the values of an object"

  (!.js (xtd/obj-filter {:a 1, :b 2, :c 3} (fn [x] (return (== 1 (mod x 2))))))
  => {"a" 1, "c" 3}

  (!.lua (xtd/obj-filter {:a 1, :b 2, :c 3} (fn [x] (return (== 1 (mod x 2))))))
  => {"a" 1, "c" 3}

  (!.py (xtd/obj-filter {:a 1, :b 2, :c 3} (fn [x] (return (== 1 (mod x 2))))))
  => {"a" 1, "c" 3})

^{:refer xt.lang.common-data/obj-keep :added "4.1"}
(fact "applies a transform across the values of an object, keeping non-nil values"

  (!.js
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20}

  (!.js
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20}

  (!.lua
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20}

  (!.lua
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20}

  (!.py
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20}

  (!.py
    (var x5-fn
         (fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil))))
    (xtd/obj-keep
     {:a 1, :b 2, :c 3}
     x5-fn))
  => {"b" 20})

^{:refer xt.lang.common-data/obj-keepf :added "4.1"}
(fact "applies a transform and filter across the values of an object"

  (!.js
   (xtd/obj-keepf
    {:a 1, :b 2, :c 3}
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => {"a" 10, "c" 30}

  (!.lua
   (xtd/obj-keepf
    {:a 1, :b 2, :c 3}
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => {"a" 10, "c" 30}

  (!.py
   (xtd/obj-keepf
    {:a 1, :b 2, :c 3}
    (fn [x] (return (== 1 (mod x 2))))
    (fn [x] (return (* x 10)))))
  => {"a" 10, "c" 30})

^{:refer xt.lang.common-data/clone-shallow :added "4.1"}
(fact "shallow clones an object or array"

  (!.js [(xtd/clone-shallow nil)
          (xtd/clone-shallow 1)])
  => [nil 1]

  (!.lua [(xtd/clone-shallow nil)
          (xtd/clone-shallow 1)])
  => [nil 1]

  (!.py [(xtd/clone-shallow nil)
          (xtd/clone-shallow 1)])
  => [nil 1])

^{:refer xt.lang.common-data/clone-nested-loop :added "4.1"}
(fact "clone nested objects loop"

  (!.js
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested-loop src (xtd/lu-create)))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}}

  (!.lua
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested-loop src (xtd/lu-create)))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}}

  (!.py
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested-loop src (xtd/lu-create)))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/clone-nested :added "4.1"}
(fact "cloning nested xects"

  (!.js
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested src))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}}

  (!.lua
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested src))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}}

  (!.py
   (var src {:a {:b 1}})
   (var out (xtd/clone-nested src))
   (xtd/set-in src ["a" "b"] 2)
   out)
  => {"a" {"b" 1}})

^{:refer xt.lang.common-data/memoize-key :added "4.1"}
(fact "memoize for functions of single argument"

  (!.js
   (var state {"n" 0})
   (var f-raw (fn [x]
                (do
                  (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
                  (return (* x 10)))))
   (var f (xtd/memoize-key f-raw))
   [(f 2) (f 2) (f 3) (xt/x:get-key state "n")])
  => [20 20 30 2]

  (!.lua
   (var state {"n" 0})
   (var f-raw (fn [x]
                (do
                  (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
                  (return (* x 10)))))
   (var f (xtd/memoize-key f-raw))
   [(f 2) (f 2) (f 3) (xt/x:get-key state "n")])
  => [20 20 30 2]

  (!.py
   (var state {"n" 0})
   (var f (xtd/memoize-key
           (fn f-raw [x]
             (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
             (return (* x 10)))))
   [(f 2) (f 2) (f 3) (xt/x:get-key state "n")])
  => [20 20 30 2])

^{:refer xt.lang.common-data/is-empty? :added "4.1"}
(fact "checks that array is empty"

  (!.js
   [(xtd/is-empty? nil)
    (xtd/is-empty? "")
    (xtd/is-empty? "123")
    (xtd/is-empty? [])
    (xtd/is-empty? [1 2 3])
    (xtd/is-empty? {})
    (xtd/is-empty? {:a 1, :b 2})])
  => [true true false true false true false]

  (!.lua
   [(xtd/is-empty? nil)
    (xtd/is-empty? "")
    (xtd/is-empty? "123")
    (xtd/is-empty? [])
    (xtd/is-empty? [1 2 3])
    (xtd/is-empty? {})
    (xtd/is-empty? {:a 1, :b 2})])
  => [true true false true false true false]

  (!.py
   [(xtd/is-empty? nil)
    (xtd/is-empty? "")
    (xtd/is-empty? "123")
    (xtd/is-empty? [])
    (xtd/is-empty? [1 2 3])
    (xtd/is-empty? {})
    (xtd/is-empty? {:a 1, :b 2})])
  => [true true false true false true false])

^{:refer xt.lang.common-data/set-pair-step :added "4.1"}
(fact "sets a pair into an object and returns it"

  (!.js
   (var out {})
   [(xt/x:get-key (xtd/set-pair-step out "a" 1) "a")
    (xt/x:get-key (xtd/set-pair-step out "b" 2) "b")
    out])
  => [1 2 {"a" 1, "b" 2}]

  (!.lua
   (var out {})
   [(xt/x:get-key (xtd/set-pair-step out "a" 1) "a")
    (xt/x:get-key (xtd/set-pair-step out "b" 2) "b")
    out])
  => [1 2 {"a" 1, "b" 2}]

  (!.py
   (var out {})
   [(xt/x:get-key (xtd/set-pair-step out "a" 1) "a")
    (xt/x:get-key (xtd/set-pair-step out "b" 2) "b")
    out])
  => [1 2 {"a" 1, "b" 2}])

^{:refer xt.lang.common-data/tree-type-native :added "4.1"}
(fact "gets the normalized native type for tree helpers"

  (!.js
   [(xtd/tree-type-native {:a 1})
    (xtd/tree-type-native [1])
    (xtd/tree-type-native "hello")
    (xtd/tree-type-native 1)
    (xtd/tree-type-native true)])
  => ["object" "array" "string" "number" "boolean"]

  (!.lua
   [(xtd/tree-type-native {:a 1})
    (xtd/tree-type-native [1])
    (xtd/tree-type-native "hello")
    (xtd/tree-type-native 1)
    (xtd/tree-type-native true)])
  => ["object" "array" "string" "number" "boolean"]

  (!.py
   [(xtd/tree-type-native {:a 1})
    (xtd/tree-type-native [1])
    (xtd/tree-type-native "hello")
    (xtd/tree-type-native 1)
    (xtd/tree-type-native true)])
  => ["object" "array" "string" "number" "boolean"])

^{:refer xt.lang.common-data/tree-get-data :added "4.1"}
(fact "normalizes nested data values for inspection"

  (!.js
   (xtd/tree-get-data
    {:a 1
     :nested {:flag true
              :arr [1 2]
              :callback (fn:>)}}))
  => {"a" 1,
      "nested" {"flag" true,
                "arr" [1 2],
                "callback" "<function>"}}

  (!.lua
   (xtd/tree-get-data
    {:a 1
     :nested {:flag true
              :arr [1 2]
              :callback (fn:>)}}))
  => {"a" 1,
      "nested" {"flag" true,
                "arr" [1 2],
                "callback" "<function>"}}

  (!.py
   (xtd/tree-get-data
    {:a 1
     :nested {:flag true
              :arr [1 2]
              :callback (fn:>)}}))
  => {"a" 1,
      "nested" {"flag" true,
                "arr" [1 2],
                "callback" "<function>"}})

^{:refer xt.lang.common-data/tree-get-spec :added "4.1"}
(fact "normalizes nested values to their runtime-native types"

  (!.js
   (xtd/tree-get-spec
    {:a 1
     :nested {:flag true
              :arr [1 2]
              :callback (fn:>)}}))
  => {"a" "number",
      "nested" {"flag" "boolean",
                "arr" ["number" "number"],
                "callback" "function"}}

  (!.lua
   (xtd/tree-get-spec
    {:a 1
     :nested {:flag true
              :arr [1 2]
              :callback (fn:>)}}))
  => {"a" "number",
      "nested" {"flag" "boolean",
                "arr" ["number" "number"],
                "callback" "function"}}

  (!.py
   (xtd/tree-get-spec
    {:a 1
     :nested {:flag true
              :arr [1 2]
              :callback (fn:>)}}))
  => {"a" "number",
      "nested" {"flag" "boolean",
                "arr" ["number" "number"],
                "callback" "function"}})

^{:refer xt.lang.common-data/memoize-key-step :added "4.1"}
(fact "computes and caches a memoized value"

  (!.js
   (var state {"n" 0})
   (var cache {})
   (var f-raw
        (fn [key]
          (do
            (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
            (return (xt/x:cat key "-value")))))
   [(xtd/memoize-key-step
     f-raw
     "a"
     cache)
    (xt/x:get-key cache "a")
    (xt/x:get-key state "n")])
  => ["a-value" "a-value" 1]

  (!.lua
   (var state {"n" 0})
   (var cache {})
   (var f-raw
        (fn [key]
          (do
            (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
            (return (xt/x:cat key "-value")))))
   [(xtd/memoize-key-step
     f-raw
     "a"
     cache)
    (xt/x:get-key cache "a")
    (xt/x:get-key state "n")])
  => ["a-value" "a-value" 1]

  (!.py
   (var state {"n" 0})
   (var cache {})
   (var f-raw
        (fn [key]
          (do
            (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
            (return (xt/x:cat key "-value")))))
   [(xtd/memoize-key-step
     f-raw
     "a"
     cache)
    (xt/x:get-key cache "a")
    (xt/x:get-key state "n")])
  => ["a-value" "a-value" 1])

(comment
  
  (s/seedgen-langadd 'xt.lang.common-data {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-data {:lang [:lua :python] :write true})
  
  )
