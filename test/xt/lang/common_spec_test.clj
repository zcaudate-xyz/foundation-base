(ns xt.lang.common-spec-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [std.lang.model.spec-lua :as lua]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root         {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-spec/for:array :added "4.1"}
(fact "iterates arrays in order"

  (!.js
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  (!.lua
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  (!.py
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])

^{:refer xt.lang.common-spec/for:object :added "4.1"}
(fact "iterates object key value pairs"

  (!.js
    (var out [])
    (xt/for:object [[k v] {:a 1 :b 2}]
      (xt/x:arr-push out [k v]))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order)

  (!.lua
    (var out [])
    (xt/for:object [[k v] {:a 1 :b 2}]
      (xt/x:arr-push out [k v]))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order)

  (!.py
    (var out [])
    (xt/for:object [[k v] {:a 1 :b 2}]
      (xt/x:arr-push out [k v]))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.common-spec/for:index :added "4.1"}
(fact "iterates a numeric range"

  (!.js
    (var out [])
    (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]]
      (xt/x:arr-push out i))
    out)
  => [0 2]

  (!.lua
    (var out [])
    (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]]
      (xt/x:arr-push out i))
    out)
  => [0 2]

  (!.py
    (var out [])
    (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]]
      (xt/x:arr-push out i))
    out)
  => [0 2])

^{:refer xt.lang.common-spec/for:iter :added "4.1"}
(fact "expands to the canonical iterator form"

  (!.js
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  (!.lua
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  (!.py
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3])

^{:refer xt.lang.common-spec/return-run :added "4.1"}
(fact "supports final returns through for:return"

  (!.js
    (xt/return-run [resolve reject]
      (resolve "OK")))
  => (throws)

  (!.lua
    (xt/return-run [resolve reject]
      (resolve "OK")))
  => (throws)

  (!.py
    (xt/return-run [resolve reject]
      (resolve "OK")))
  => (throws))

^{:refer xt.lang.common-spec/for:return :added "4.1"}
(fact "dispatches success and error branches"

  (!.js
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                               (reject "ERR"))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR"

  (!.lua
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                               (reject "ERR"))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR"

  (!.py
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                               (reject "ERR"))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR")

^{:refer xt.lang.common-spec/for:try :added "4.1"}
(fact "expands to the canonical try form"

  (!.js
    (var add (fn []
               (xt/for:try [[ok err] (do:> (xt/x:err "ERROR"))]
                 {:success (return ok)
                  :error   (return "ERR")})))
    (add))
  => "ERR"

  (!.lua
    (var add (fn []
               (xt/for:try [[ok err] (do:> (xt/x:err "ERROR"))]
                 {:success (return ok)
                  :error   (return "ERR")})))
    (add))
  => "ERR"

  (!.py
    (var add (fn []
               (xt/for:try [[ok err] (do:> (xt/x:err "ERROR"))]
                 {:success (return ok)
                  :error   (return "ERR")})))
    (add))
  => "ERR")

^{:refer xt.lang.common-spec/for:async :added "4.1"}
(fact "expands to the canonical async form"

  (notify/wait-on :js
    (for:async [[ok err] (xt/return-run [resolve reject]
                           (resolve "OK"))]
               {:success (repl/notify ok)
                :error   (repl/notify err)
                :finally (return true)}))
  => "OK"

  (notify/wait-on :lua
    (for:async [[ok err] (xt/return-run [resolve reject]
                           (resolve "OK"))]
               {:success (repl/notify ok)
                :error   (repl/notify err)
                :finally (return true)}))
  => "OK"

  (notify/wait-on :python
    (for:async [[ok err] (xt/return-run [resolve reject]
                           (resolve "OK"))]
               {:success (repl/notify ok)
                :error   (repl/notify err)
                :finally (return true)}))
  => "OK")

^{:refer xt.lang.common-spec/x:get-idx :added "4.1"}
(fact "reads the first indexed value"

  (!.js
    (xt/x:get-idx ["a" "b" "c"] (xt/x:offset 0)))
  => "a"

  (!.lua
    (xt/x:get-idx ["a" "b" "c"] (xt/x:offset 0)))
  => "a"

  (!.py
    (xt/x:get-idx ["a" "b" "c"] (xt/x:offset 0)))
  => "a")

^{:refer xt.lang.common-spec/x:set-idx :added "4.1"}
(fact "writes an indexed value"

  (!.js
    (var out ["a" "b" "c"])
    (xt/x:set-idx out (xt/x:offset 1) "B")
    out)
  => ["a" "B" "c"]

  (!.lua
    (var out ["a" "b" "c"])
    (xt/x:set-idx out (xt/x:offset 1) "B")
    out)
  => ["a" "B" "c"]

  (!.py
    (var out ["a" "b" "c"])
    (xt/x:set-idx out (xt/x:offset 1) "B")
    out)
  => ["a" "B" "c"])

^{:refer xt.lang.common-spec/x:first :added "4.1"}
(fact "gets the first array element"

  (!.js
    (xt/x:first ["a" "b" "c"]))
  => "a"

  (!.lua
    (xt/x:first ["a" "b" "c"]))
  => "a"

  (!.py
    (xt/x:first ["a" "b" "c"]))
  => "a")

^{:refer xt.lang.common-spec/x:second :added "4.1"}
(fact "gets the second array element"

  (!.js
    (xt/x:second ["a" "b" "c"]))
  => "b"

  (!.lua
    (xt/x:second ["a" "b" "c"]))
  => "b"

  (!.py
    (xt/x:second ["a" "b" "c"]))
  => "b")

^{:refer xt.lang.common-spec/x:last :added "4.1"}
(fact "gets the last array element"

  (!.js
    (xt/x:last ["a" "b" "c" "d"]))
  => "d"

  (!.lua
    (xt/x:last ["a" "b" "c" "d"]))
  => "d"

  (!.py
    (xt/x:last ["a" "b" "c" "d"]))
  => "d")

^{:refer xt.lang.common-spec/x:second-last :added "4.1"}
(fact "gets the element before the last"

  (!.js
    (xt/x:second-last ["a" "b" "c" "d"]))
  => "c"

  (!.lua
    (xt/x:second-last ["a" "b" "c" "d"]))
  => "c"

  (!.py
    (xt/x:second-last ["a" "b" "c" "d"]))
  => "c")

^{:refer xt.lang.common-spec/x:arr-remove :added "4.1"}
(fact "removes an element from an array"

  (!.js
    (do (var out ["a" "b" "c" "d"])
        (xt/x:arr-remove out (xt/x:offset 1))
        out))
  => ["a" "c" "d"]

  (!.lua
    (do (var out ["a" "b" "c" "d"])
        (xt/x:arr-remove out (xt/x:offset 1))
        out))
  => ["a" "c" "d"]

  (!.py
    (do (var out ["a" "b" "c" "d"])
        (xt/x:arr-remove out (xt/x:offset 1))
        out))
  => ["a" "c" "d"])

^{:refer xt.lang.common-spec/x:arr-push :added "4.1"}
(fact "pushes an element onto an array"

  (!.js
    (var out ["a" "b" "c"])
    (xt/x:arr-push out "D")
    out)
  => ["a" "b" "c" "D"]

  (!.lua
    (var out ["a" "b" "c"])
    (xt/x:arr-push out "D")
    out)
  => ["a" "b" "c" "D"]

  (!.py
    (var out ["a" "b" "c"])
    (xt/x:arr-push out "D")
    out)
  => ["a" "b" "c" "D"])

^{:refer xt.lang.common-spec/x:arr-pop :added "4.1"}
(fact "pops the last element from an array"

  (!.js
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop out) out])
  => ["d" ["a" "b" "c"]]

  (!.lua
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop out) out])
  => ["d" ["a" "b" "c"]]

  (!.py
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop out) out])
  => ["d" ["a" "b" "c"]])

^{:refer xt.lang.common-spec/x:arr-push-first :added "4.1"}
(fact "pushes an element to the front of an array"

  (!.js
    (var out ["a" "b" "c"])
    (xt/x:arr-push-first out "D")
    out)
  => ["D" "a" "b" "c"]

  (!.lua
    (var out ["a" "b" "c"])
    (xt/x:arr-push-first out "D")
    out)
  => ["D" "a" "b" "c"]

  (!.py
    (var out ["a" "b" "c"])
    (xt/x:arr-push-first out "D")
    out)
  => ["D" "a" "b" "c"])

^{:refer xt.lang.common-spec/x:arr-pop-first :added "4.1"}
(fact "pops the first element from an array"

  (!.js
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop-first out) out])
  => ["a" ["b" "c" "d"]]

  (!.lua
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop-first out) out])
  => ["a" ["b" "c" "d"]]

  (!.py
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop-first out) out])
  => ["a" ["b" "c" "d"]])

^{:refer xt.lang.common-spec/x:arr-insert :added "4.1"}
(fact "inserts an element into an array"

  (!.js
    (var out ["a" "b" "c"])
    (xt/x:arr-insert out (xt/x:offset 1) "D")
    out)
  => ["a" "D" "b" "c"]

  (!.lua
    (var out ["a" "b" "c"])
    (xt/x:arr-insert out (xt/x:offset 1) "D")
    out)
  => ["a" "D" "b" "c"]

  (!.py
    (var out ["a" "b" "c"])
    (xt/x:arr-insert out (xt/x:offset 1) "D")
    out)
  => ["a" "D" "b" "c"])

^{:refer xt.lang.common-spec/x:arr-slice :added "4.1"}
(fact "slices a range from an array"

  (!.js
    (xt/x:arr-slice ["a" "b" "c" "d" "e"]
                    (xt/x:offset 1)
                    (xt/x:offset 3)))
  => ["b" "c"]

  (!.lua
    (xt/x:arr-slice ["a" "b" "c" "d" "e"]
                    (xt/x:offset 1)
                    (xt/x:offset 3)))
  => ["b" "c"]

  (!.py
    (xt/x:arr-slice ["a" "b" "c" "d" "e"]
                    (xt/x:offset 1)
                    (xt/x:offset 3)))
  => ["b" "c"])

^{:refer xt.lang.common-spec/x:arr-reverse :added "4.1"}
(fact "reverses an array"

  (!.js
    (xt/x:arr-reverse ["a" "b" "c"]))
  => ["c" "b" "a"]

  (!.lua
    (xt/x:arr-reverse ["a" "b" "c"]))
  => ["c" "b" "a"]

  (!.py
    (xt/x:arr-reverse ["a" "b" "c"]))
  => ["c" "b" "a"])

^{:refer xt.lang.common-spec/x:del :added "4.1"}
(fact "expands and emits a lua delete form"

  (!.js
    (var out {:a 1 :b 2})
    (xt/x:del (. out ["a"]))
    out)
  => {"b" 2}

  (!.lua
    (var out {:a 1 :b 2})
    (xt/x:del (. out ["a"]))
    out)
  => {"b" 2}

  (!.py
    (var out {:a 1 :b 2})
    (xt/x:del (. out ["a"]))
    out)
  => {"b" 2})

^{:refer xt.lang.common-spec/x:cat :added "4.1"}
(fact "concatenates strings"

  (!.js
    (xt/x:cat "hello" "-" "world"))
  => "hello-world"

  (!.lua
    (xt/x:cat "hello" "-" "world"))
  => "hello-world"

  (!.py
    (xt/x:cat "hello" "-" "world"))
  => "hello-world")

^{:refer xt.lang.common-spec/x:len :added "4.1"}
(fact "gets the collection length"

  (!.js
    (xt/x:len ["a" "b" "c"]))
  => 3

  (!.lua
    (xt/x:len ["a" "b" "c"]))
  => 3

  (!.py
    (xt/x:len ["a" "b" "c"]))
  => 3)

^{:refer xt.lang.common-spec/x:err :added "4.1"}
(fact "expands and emits a lua error form"

  (!.js
    (var err-fn (fn []
                  (xt/x:err "ERR")))
    (err-fn))
  => (throws)

  (!.lua
    (var err-fn (fn []
                  (xt/x:err "ERR")))
    (err-fn))
  => (throws)

  (!.py
    (var err-fn (fn []
                  (xt/x:err "ERR")))
    (err-fn))
  => (throws))

^{:refer xt.lang.common-spec/x:type-native :added "4.1"}
(fact "expands and emits the lua type helper"

  (!.js
    (var type-fn (fn [obj]
                   (xt/x:type-native obj)))
    [(type-fn {})
     (type-fn [])])
  => ["object" "array"]

  (!.lua
    (var type-fn (fn [obj]
                   (xt/x:type-native obj)))
    [(type-fn {})
     (type-fn [])])
  => ["object" "array"]

  (!.py
    (var type-fn (fn [obj]
                   (xt/x:type-native obj)))
    [(type-fn {})
     (type-fn [])])
  => ["object" "array"])

^{:refer xt.lang.common-spec/x:offset :added "4.1"}
(fact "uses the grammar base offset"

  ^!.py
  (!.js    
    (xt/x:offset 10))
  => 10

  ^!.lua
  (!.js    
    (xt/x:offset 10))
  => 10

  ^!.py
  (!.js    
    (xt/x:offset 10))
  => 10)

^{:refer xt.lang.common-spec/x:offset-rev :added "4.1"}
(fact "uses the reverse grammar offset"

  ^!.py
  (!.js
    (xt/x:offset-rev 10))
  => 9

  ^!.lua
  (!.js
    (xt/x:offset-rev 10))
  => 9

  ^!.py
  (!.js
    (xt/x:offset-rev 10))
  => 9)

^{:refer xt.lang.common-spec/x:offset-len :added "4.1"}
(fact "uses the length grammar offset"

  ^!.py
  (!.js
    (xt/x:offset-len 10))
  => 9

  ^!.lua
  (!.js
    (xt/x:offset-len 10))
  => 9

  ^!.py
  (!.js
    (xt/x:offset-len 10))
  => 9)

^{:refer xt.lang.common-spec/x:offset-rlen :added "4.1"}
(fact "uses the reverse length grammar offset"

  ^!.py
  (!.js
    (xt/x:offset-rlen 10))
  => 10

  ^!.lua
  (!.js
    (xt/x:offset-rlen 10))
  => 10

  ^!.py
  (!.js
    (xt/x:offset-rlen 10))
  => 10)

^{:refer xt.lang.common-spec/x:lu-create :added "4.1"}
(fact "creates a lookup table wrapper"

  (!.js
    (var lu (xt/x:lu-create))
    (var lu-A1 {"A" "A"})
    (var lu-A2 {"A" "A"})
    (xt/x:lu-set lu lu-A1 "A1")
    (xt/x:lu-set lu lu-A2 "A2")
    [(xt/x:lu-get lu lu-A1)
     (xt/x:lu-get lu lu-A2)])
  => ["A1" "A2"]

  (!.lua
    (var lu (xt/x:lu-create))
    (var lu-A1 {"A" "A"})
    (var lu-A2 {"A" "A"})
    (xt/x:lu-set lu lu-A1 "A1")
    (xt/x:lu-set lu lu-A2 "A2")
    [(xt/x:lu-get lu lu-A1)
     (xt/x:lu-get lu lu-A2)])
  => ["A1" "A2"]

  (!.py
    (var lu (xt/x:lu-create))
    (var lu-A1 {"A" "A"})
    (var lu-A2 {"A" "A"})
    (xt/x:lu-set lu lu-A1 "A1")
    (xt/x:lu-set lu lu-A2 "A2")
    [(xt/x:lu-get lu lu-A1)
     (xt/x:lu-get lu lu-A2)])
  => ["A1" "A2"])

^{:refer xt.lang.common-spec/x:lu-eq :added "4.1"}
(fact "compares lookup keys using lua identity"

  (!.js
    (var obj-a {:id 1})
    (var obj-b {:id 1})
    [(xt/x:lu-eq obj-a obj-a)
     (xt/x:lu-eq obj-a obj-b)
     (xt/x:lu-eq obj-b obj-b)])
  => [true false true]

  (!.lua
    (var obj-a {:id 1})
    (var obj-b {:id 1})
    [(xt/x:lu-eq obj-a obj-a)
     (xt/x:lu-eq obj-a obj-b)
     (xt/x:lu-eq obj-b obj-b)])
  => [true false true]

  (!.py
    (var obj-a {:id 1})
    (var obj-b {:id 1})
    [(xt/x:lu-eq obj-a obj-a)
     (xt/x:lu-eq obj-a obj-b)
     (xt/x:lu-eq obj-b obj-b)])
  => [true false true])

^{:refer xt.lang.common-spec/x:lu-get :added "4.1"}
(fact "reads values from a lookup table"

  (!.js
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value"

  (!.lua
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value"

  (!.py
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value")

^{:refer xt.lang.common-spec/x:lu-set :added "4.1"}
(fact "writes values into a lookup table"

  (!.js
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value"

  (!.lua
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value"

  (!.py
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value")

^{:refer xt.lang.common-spec/x:lu-del :added "4.1"}
(fact "removes values from a lookup table"

  (!.js
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-del lu lu-key)
    (xt/x:lu-get lu lu-key))
  => nil

  (!.lua
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-del lu lu-key)
    (xt/x:lu-get lu lu-key))
  => nil

  (!.py
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-del lu lu-key)
    (xt/x:lu-get lu lu-key))
  => nil)

^{:refer xt.lang.common-spec/x:m-abs :added "4.1"}
(fact "computes absolute values"

  (!.js (xt/x:m-abs -3))
  => 3

  (!.lua (xt/x:m-abs -3))
  => 3

  (!.py (xt/x:m-abs -3))
  => 3)

^{:refer xt.lang.common-spec/x:m-acos :added "4.1"}
(fact "computes inverse cosine"

  (!.js (xt/x:m-acos 1))
  => (approx 0)

  (!.lua (xt/x:m-acos 1))
  => (approx 0)

  (!.py (xt/x:m-acos 1))
  => (approx 0))

^{:refer xt.lang.common-spec/x:m-asin :added "4.1"}
(fact "computes inverse sine"

  (!.js (xt/x:m-asin 0))
  => (approx 0)

  (!.lua (xt/x:m-asin 0))
  => (approx 0)

  (!.py (xt/x:m-asin 0))
  => (approx 0))

^{:refer xt.lang.common-spec/x:m-atan :added "4.1"}
(fact "computes inverse tangent"

  (!.js (xt/x:m-atan 0))
  => (approx 0)

  (!.lua (xt/x:m-atan 0))
  => (approx 0)

  (!.py (xt/x:m-atan 0))
  => (approx 0))

^{:refer xt.lang.common-spec/x:m-ceil :added "4.1"}
(fact "rounds numbers upward"

  (!.js (xt/x:m-ceil 1.2))
  => 2

  (!.lua (xt/x:m-ceil 1.2))
  => 2

  (!.py (xt/x:m-ceil 1.2))
  => 2)

^{:refer xt.lang.common-spec/x:m-cos :added "4.1"}
(fact "computes cosine"

  (!.js (xt/x:m-cos 0))
  => (approx 1)

  (!.lua (xt/x:m-cos 0))
  => (approx 1)

  (!.py (xt/x:m-cos 0))
  => (approx 1))

^{:refer xt.lang.common-spec/x:m-cosh :added "4.1"}
(fact "computes hyperbolic cosine"

  (!.js (xt/x:m-cosh 0))
  => (approx 1)

  (!.lua (xt/x:m-cosh 0))
  => (approx 1)

  (!.py (xt/x:m-cosh 0))
  => (approx 1))

^{:refer xt.lang.common-spec/x:m-exp :added "4.1"}
(fact "computes the exponential function"

  (!.js (xt/x:m-exp 0))
  => (approx 1)

  (!.lua (xt/x:m-exp 0))
  => (approx 1)

  (!.py (xt/x:m-exp 0))
  => (approx 1))

^{:refer xt.lang.common-spec/x:m-floor :added "4.1"}
(fact "rounds numbers downward"

  (!.js (xt/x:m-floor 1.8))
  => 1

  (!.lua (xt/x:m-floor 1.8))
  => 1

  (!.py (xt/x:m-floor 1.8))
  => 1)

^{:refer xt.lang.common-spec/x:m-loge :added "4.1"}
(fact "computes the natural logarithm"

  (!.js (xt/x:m-loge 1))
  => (approx 0)

  (!.lua (xt/x:m-loge 1))
  => (approx 0)

  (!.py (xt/x:m-loge 1))
  => (approx 0))

^{:refer xt.lang.common-spec/x:m-log10 :added "4.1"}
(fact "computes the base-10 logarithm"

  (!.js (xt/x:m-log10 100))
  => (approx 2)

  (!.lua (xt/x:m-log10 100))
  => (approx 2)

  (!.py (xt/x:m-log10 100))
  => (approx 2))

^{:refer xt.lang.common-spec/x:m-max :added "4.1"}
(fact "computes the maximum value"

  (!.js (xt/x:m-max 3 5))
  => 5

  (!.lua (xt/x:m-max 3 5))
  => 5

  (!.py (xt/x:m-max 3 5))
  => 5)

^{:refer xt.lang.common-spec/x:m-mod :added "4.1"}
(fact "computes modulo values"

  (!.js (xt/x:m-mod 10 3))
  => 1

  (!.lua (xt/x:m-mod 10 3))
  => 1

  (!.py (xt/x:m-mod 10 3))
  => 1)

^{:refer xt.lang.common-spec/x:m-min :added "4.1"}
(fact "computes the minimum value"

  (!.js (xt/x:m-min 3 5))
  => 3

  (!.lua (xt/x:m-min 3 5))
  => 3

  (!.py (xt/x:m-min 3 5))
  => 3)

^{:refer xt.lang.common-spec/x:m-pow :added "4.1"}
(fact "raises numbers to a power"

  (!.js (xt/x:m-pow 2 4))
  => 16

  (!.lua (xt/x:m-pow 2 4))
  => 16

  (!.py (xt/x:m-pow 2 4))
  => 16)

^{:refer xt.lang.common-spec/x:m-quot :added "4.1"}
(fact "computes integer quotients"

  (!.js (xt/x:m-quot 7 2))
  => 3

  (!.lua (xt/x:m-quot 7 2))
  => 3

  (!.py (xt/x:m-quot 7 2))
  => 3)

^{:refer xt.lang.common-spec/x:m-sin :added "4.1"}
(fact "computes sine"

  (!.js (xt/x:m-sin 0))
  => (approx 0)

  (!.lua (xt/x:m-sin 0))
  => (approx 0)

  (!.py (xt/x:m-sin 0))
  => (approx 0))

^{:refer xt.lang.common-spec/x:m-sinh :added "4.1"}
(fact "computes hyperbolic sine"

  (!.js (xt/x:m-sinh 0))
  => (approx 0)

  (!.lua (xt/x:m-sinh 0))
  => (approx 0)

  (!.py (xt/x:m-sinh 0))
  => (approx 0))

^{:refer xt.lang.common-spec/x:m-sqrt :added "4.1"}
(fact "computes square roots"

  (!.js (xt/x:m-sqrt 9))
  => (approx 3)

  (!.lua (xt/x:m-sqrt 9))
  => (approx 3)

  (!.py (xt/x:m-sqrt 9))
  => (approx 3))

^{:refer xt.lang.common-spec/x:m-tan :added "4.1"}
(fact "computes tangent"

  (!.js (xt/x:m-tan 0))
  => (approx 0)

  (!.lua (xt/x:m-tan 0))
  => (approx 0)

  (!.py (xt/x:m-tan 0))
  => (approx 0))

^{:refer xt.lang.common-spec/x:m-tanh :added "4.1"}
(fact "computes hyperbolic tangent"

  (!.js (xt/x:m-tanh 0))
  => (approx 0)

  (!.lua (xt/x:m-tanh 0))
  => (approx 0)

  (!.py (xt/x:m-tanh 0))
  => (approx 0))

^{:refer xt.lang.common-spec/x:not-nil? :added "4.1"}
(fact "checks for non-nil values"

  (!.js
    (xt/x:not-nil? 0))
  => true

  (!.lua
    (xt/x:not-nil? 0))
  => true

  (!.py
    (xt/x:not-nil? 0))
  => true)

^{:refer xt.lang.common-spec/x:nil? :added "4.1"}
(fact "checks for nil values"

  (!.js
    (xt/x:nil? nil))
  => true

  (!.lua
    (xt/x:nil? nil))
  => true

  (!.py
    (xt/x:nil? nil))
  => true)

^{:refer xt.lang.common-spec/x:add :added "4.1"}
(fact "adds numbers"

  (!.js
    (xt/x:add 1 2 3))
  => 6

  (!.lua
    (xt/x:add 1 2 3))
  => 6

  (!.py
    (xt/x:add 1 2 3))
  => 6)

^{:refer xt.lang.common-spec/x:sub :added "4.1"}
(fact "subtracts numbers"

  (!.js
    (xt/x:sub 10 3 2))
  => 5

  (!.lua
    (xt/x:sub 10 3 2))
  => 5

  (!.py
    (xt/x:sub 10 3 2))
  => 5)

^{:refer xt.lang.common-spec/x:mul :added "4.1"}
(fact "multiplies numbers"

  (!.js
    (xt/x:mul 2 3 4))
  => 24

  (!.lua
    (xt/x:mul 2 3 4))
  => 24

  (!.py
    (xt/x:mul 2 3 4))
  => 24)

^{:refer xt.lang.common-spec/x:div :added "4.1"}
(fact "divides numbers"

  (!.js
    (xt/x:div 20 5))
  => (approx 4)

  (!.lua
    (xt/x:div 20 5))
  => (approx 4)

  (!.py
    (xt/x:div 20 5))
  => (approx 4))

^{:refer xt.lang.common-spec/x:neg :added "4.1"}
(fact "negates a number"

  (!.js
    (xt/x:neg 2))
  => -2

  (!.lua
    (xt/x:neg 2))
  => -2

  (!.py
    (xt/x:neg 2))
  => -2)

^{:refer xt.lang.common-spec/x:inc :added "4.1"}
(fact "increments a number"

  (!.js
    (xt/x:inc 2))
  => 3

  (!.lua
    (xt/x:inc 2))
  => 3

  (!.py
    (xt/x:inc 2))
  => 3)

^{:refer xt.lang.common-spec/x:dec :added "4.1"}
(fact "decrements a number"

  (!.js
    (xt/x:dec 2))
  => 1

  (!.lua
    (xt/x:dec 2))
  => 1

  (!.py
    (xt/x:dec 2))
  => 1)

^{:refer xt.lang.common-spec/x:zero? :added "4.1"}
(fact "checks whether a number is zero"

  (!.js
    (xt/x:zero? 0))
  => true

  (!.lua
    (xt/x:zero? 0))
  => true

  (!.py
    (xt/x:zero? 0))
  => true)

^{:refer xt.lang.common-spec/x:pos? :added "4.1"}
(fact "checks whether a number is positive"

  (!.js
    (xt/x:pos? 2))
  => true

  (!.lua
    (xt/x:pos? 2))
  => true

  (!.py
    (xt/x:pos? 2))
  => true)

^{:refer xt.lang.common-spec/x:neg? :added "4.1"}
(fact "checks whether a number is negative"

  (!.js
    (xt/x:neg? -2))
  => true

  (!.lua
    (xt/x:neg? -2))
  => true

  (!.py
    (xt/x:neg? -2))
  => true)

^{:refer xt.lang.common-spec/x:even? :added "4.1"}
(fact "checks whether a number is even"

  (!.js
    (xt/x:even? 4))
  => true

  (!.lua
    (xt/x:even? 4))
  => true

  (!.py
    (xt/x:even? 4))
  => true)

^{:refer xt.lang.common-spec/x:odd? :added "4.1"}
(fact "checks whether a number is odd"

  (!.js
    (xt/x:odd? 5))
  => true

  (!.lua
    (xt/x:odd? 5))
  => true

  (!.py
    (xt/x:odd? 5))
  => true)

^{:refer xt.lang.common-spec/x:eq :added "4.1"}
(fact "checks equality"

  (!.js
    (xt/x:eq 2 2))
  => true

  (!.lua
    (xt/x:eq 2 2))
  => true

  (!.py
    (xt/x:eq 2 2))
  => true)

^{:refer xt.lang.common-spec/x:neq :added "4.1"}
(fact "checks inequality"

  (!.js
    (xt/x:neq 2 3))
  => true

  (!.lua
    (xt/x:neq 2 3))
  => true

  (!.py
    (xt/x:neq 2 3))
  => true)

^{:refer xt.lang.common-spec/x:lt :added "4.1"}
(fact "checks less than"

  (!.js
    (xt/x:lt 2 3))
  => true

  (!.lua
    (xt/x:lt 2 3))
  => true

  (!.py
    (xt/x:lt 2 3))
  => true)

^{:refer xt.lang.common-spec/x:lte :added "4.1"}
(fact "checks less than or equal"

  (!.js
    (xt/x:lte 3 3))
  => true

  (!.lua
    (xt/x:lte 3 3))
  => true

  (!.py
    (xt/x:lte 3 3))
  => true)

^{:refer xt.lang.common-spec/x:gt :added "4.1"}
(fact "checks greater than"

  (!.js
    (xt/x:gt 4 3))
  => true

  (!.lua
    (xt/x:gt 4 3))
  => true

  (!.py
    (xt/x:gt 4 3))
  => true)

^{:refer xt.lang.common-spec/x:gte :added "4.1"}
(fact "checks greater than or equal"

  (!.js
    (xt/x:gte 4 4))
  => true

  (!.lua
    (xt/x:gte 4 4))
  => true

  (!.py
    (xt/x:gte 4 4))
  => true)

^{:refer xt.lang.common-spec/x:has-key? :added "4.1"}
(fact "checks whether an object has a key"

  (!.js
    (var obj {:a 1})
    (xt/x:has-key? obj "a"))
  => true

  (!.lua
    (var obj {:a 1})
    (xt/x:has-key? obj "a"))
  => true

  (!.py
    (var obj {:a 1})
    (xt/x:has-key? obj "a"))
  => true)

^{:refer xt.lang.common-spec/x:del-key :added "4.1"}
(fact "deletes keys from objects"

  (!.js
    (var out {:a 1 :b 2})
    (xt/x:del-key out "a")
    out)
  => {"b" 2}

  (!.lua
    (var out {:a 1 :b 2})
    (xt/x:del-key out "a")
    out)
  => {"b" 2}

  (!.py
    (var out {:a 1 :b 2})
    (xt/x:del-key out "a")
    out)
  => {"b" 2})

^{:refer xt.lang.common-spec/x:get-key :added "4.1"}
(fact "gets a value by key with a fallback"

  (!.js
    (xt/x:get-key {} "missing" "fallback"))
  => "fallback"

  (!.lua
    (xt/x:get-key {} "missing" "fallback"))
  => "fallback"

  (!.py
    (xt/x:get-key {} "missing" "fallback"))
  => "fallback")

^{:refer xt.lang.common-spec/x:get-path :added "4.1"}
(fact "gets a nested value by path"

  (!.js
    (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
  => 2

  (!.lua
    (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
  => 2

  (!.py
    (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
  => 2)

^{:refer xt.lang.common-spec/x:set-key :added "4.1"}
(fact "sets a key on an object"

  (!.js
    (var out {:a 1})
    (xt/x:set-key out "b" 2)
    out)
  => {"a" 1, "b" 2}

  (!.lua
    (var out {:a 1})
    (xt/x:set-key out "b" 2)
    out)
  => {"a" 1, "b" 2}

  (!.py
    (var out {:a 1})
    (xt/x:set-key out "b" 2)
    out)
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:copy-key :added "4.1"}
(fact "copies a key from another object"

  (!.js
    (var out {:a 1})
    (xt/x:copy-key out {:a 9} ["c" "a"])
    out)
  => {"a" 1, "c" 9}

  (!.lua
    (var out {:a 1})
    (xt/x:copy-key out {:a 9} ["c" "a"])
    out)
  => {"a" 1, "c" 9}

  (!.py
    (var out {:a 1})
    (xt/x:copy-key out {:a 9} ["c" "a"])
    out)
  => {"a" 1, "c" 9})

^{:refer xt.lang.common-spec/x:obj-keys :added "4.1"}
(fact "lists object keys"

  (!.js
   (!.js
     (xt/x:obj-keys {:a 1 :b 2})))
  => #{"a" "b"}

  (!.lua
   (!.js
     (xt/x:obj-keys {:a 1 :b 2})))
  => #{"a" "b"}

  (!.py
   (!.js
     (xt/x:obj-keys {:a 1 :b 2})))
  => #{"a" "b"})

^{:refer xt.lang.common-spec/x:obj-vals :added "4.1"}
(fact "lists object values"

  (!.js
   (!.js
     (xt/x:obj-vals {:a 1 :b 2})))
  => #{1 2}

  (!.lua
   (!.js
     (xt/x:obj-vals {:a 1 :b 2})))
  => #{1 2}

  (!.py
   (!.js
     (xt/x:obj-vals {:a 1 :b 2})))
  => #{1 2})

^{:refer xt.lang.common-spec/x:obj-pairs :added "4.1"}
(fact "lists object pairs"

  (!.js
   (!.js
     (xt/x:obj-pairs {:a 1 :b 2})))
  => #{["a" 1] ["b" 2]}

  (!.lua
   (!.js
     (xt/x:obj-pairs {:a 1 :b 2})))
  => #{["a" 1] ["b" 2]}

  (!.py
   (!.js
     (xt/x:obj-pairs {:a 1 :b 2})))
  => #{["a" 1] ["b" 2]})

^{:refer xt.lang.common-spec/x:obj-clone :added "4.1"}
(fact "clones an object"

  (!.js
    (var src {:a 1})
    (var out (xt/x:obj-clone src))
    (xt/x:set-key src "b" 2)
    out)
  => {"a" 1}

  (!.lua
    (var src {:a 1})
    (var out (xt/x:obj-clone src))
    (xt/x:set-key src "b" 2)
    out)
  => {"a" 1}

  (!.py
    (var src {:a 1})
    (var out (xt/x:obj-clone src))
    (xt/x:set-key src "b" 2)
    out)
  => {"a" 1})

^{:refer xt.lang.common-spec/x:obj-assign :added "4.1"}
(fact "assigns object keys"

  (!.js
    (xt/x:obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2}

  (!.lua
    (xt/x:obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2}

  (!.py
    (xt/x:obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:to-string :added "4.1"}
(fact "converts a value to a string"

  (!.js
    (xt/x:to-string 12))
  => "12"

  (!.lua
    (xt/x:to-string 12))
  => "12"

  (!.py
    (xt/x:to-string 12))
  => "12")

^{:refer xt.lang.common-spec/x:to-number :added "4.1"}
(fact "converts a string to a number"

  (!.js
    (xt/x:to-number "12.5"))
  => 12.5

  (!.lua
    (xt/x:to-number "12.5"))
  => 12.5

  (!.py
    (xt/x:to-number "12.5"))
  => 12.5)

^{:refer xt.lang.common-spec/x:is-string? :added "4.1"}
(fact "recognises strings"

  (!.js
    (xt/x:is-string? "abc"))
  => true

  (!.lua
    (xt/x:is-string? "abc"))
  => true

  (!.py
    (xt/x:is-string? "abc"))
  => true)

^{:refer xt.lang.common-spec/x:is-number? :added "4.1"}
(fact "recognises numbers"

  (!.js
    (xt/x:is-number? 1.5))
  => true

  (!.lua
    (xt/x:is-number? 1.5))
  => true

  (!.py
    (xt/x:is-number? 1.5))
  => true)

^{:refer xt.lang.common-spec/x:is-integer? :added "4.1"}
(fact "recognises integers"

  (!.js
    (xt/x:is-integer? 2))
  => true

  (!.lua
    (xt/x:is-integer? 2))
  => true

  (!.py
    (xt/x:is-integer? 2))
  => true)

^{:refer xt.lang.common-spec/x:is-boolean? :added "4.1"}
(fact "recognises booleans"

  (!.js
    (xt/x:is-boolean? true))
  => true

  (!.lua
    (xt/x:is-boolean? true))
  => true

  (!.py
    (xt/x:is-boolean? true))
  => true)

^{:refer xt.lang.common-spec/x:is-object? :added "4.1"}
(fact "recognises objects"

  (!.js
    (xt/x:is-object? {:a 1}))
  => true

  (!.lua
    (xt/x:is-object? {:a 1}))
  => true

  (!.py
    (xt/x:is-object? {:a 1}))
  => true)

^{:refer xt.lang.common-spec/x:is-array? :added "4.1"}
(fact "recognises arrays"

  (!.js
    (xt/x:is-array? [1 2]))
  => true

  (!.lua
    (xt/x:is-array? [1 2]))
  => true

  (!.py
    (xt/x:is-array? [1 2]))
  => true)

^{:refer xt.lang.common-spec/x:print :added "4.1"}
(fact "expands and emits a lua print form"

  (!.js
    ^{:lang-exceptions {:dart {:skip true}}}
    (xt/x:nil? (xt/x:print "hello")))
  => true

  (!.lua
    ^{:lang-exceptions {:dart {:skip true}}}
    (xt/x:nil? (xt/x:print "hello")))
  => true

  (!.py
    ^{:lang-exceptions {:dart {:skip true}}}
    (xt/x:nil? (xt/x:print "hello")))
  => true)

^{:refer xt.lang.common-spec/x:str-len :added "4.1"}
(fact "gets the string length"

  (!.js
    (xt/x:str-len "hello"))
  => 5

  (!.lua
    (xt/x:str-len "hello"))
  => 5

  (!.py
    (xt/x:str-len "hello"))
  => 5)

^{:refer xt.lang.common-spec/x:str-comp :added "4.1"}
(fact "compares strings by sort order"

  (!.js (xt/x:str-comp "abc" "abd"))
  => true

  (!.lua (xt/x:str-comp "abc" "abd"))
  => true

  (!.py (xt/x:str-comp "abc" "abd"))
  => true)

^{:refer xt.lang.common-spec/x:str-lt :added "4.1"}
(fact "checks whether one string sorts before another"

  (!.js (xt/x:str-lt "abc" "abd"))
  => true

  (!.lua (xt/x:str-lt "abc" "abd"))
  => true

  (!.py (xt/x:str-lt "abc" "abd"))
  => true)

^{:refer xt.lang.common-spec/x:str-gt :added "4.1"}
(fact "checks whether one string sorts after another"

  (!.js (xt/x:str-gt "abd" "abc"))
  => true

  (!.lua (xt/x:str-gt "abd" "abc"))
  => true

  (!.py (xt/x:str-gt "abd" "abc"))
  => true)

^{:refer xt.lang.common-spec/x:str-pad-left :added "4.1"}
(fact "pads a string on the left"

  (!.js
    (xt/x:str-pad-left "7" 3 "0"))
  => "007"

  (!.lua
    (xt/x:str-pad-left "7" 3 "0"))
  => "007"

  (!.py
    (xt/x:str-pad-left "7" 3 "0"))
  => "007")

^{:refer xt.lang.common-spec/x:str-pad-right :added "4.1"}
(fact "pads a string on the right"

  (!.js
    (xt/x:str-pad-right "7" 3 "0"))
  => "700"

  (!.lua
    (xt/x:str-pad-right "7" 3 "0"))
  => "700"

  (!.py
    (xt/x:str-pad-right "7" 3 "0"))
  => "700")

^{:refer xt.lang.common-spec/x:str-starts-with :added "4.1"}
(fact "checks the string prefix"

  (!.js
    (xt/x:str-starts-with "hello" "he"))
  => true

  (!.lua
    (xt/x:str-starts-with "hello" "he"))
  => true

  (!.py
    (xt/x:str-starts-with "hello" "he"))
  => true)

^{:refer xt.lang.common-spec/x:str-ends-with :added "4.1"}
(fact "checks the string suffix"

  (!.js
    (xt/x:str-ends-with "hello" "lo"))
  => true

  (!.lua
    (xt/x:str-ends-with "hello" "lo"))
  => true

  (!.py
    (xt/x:str-ends-with "hello" "lo"))
  => true)

^{:refer xt.lang.common-spec/x:str-char :added "4.1"}
(fact "gets the character code at an index"

  (!.js
    (xt/x:str-char "abc" (xt/x:offset 1)))
  => 98

  (!.lua
    (xt/x:str-char "abc" (xt/x:offset 1)))
  => 98

  (!.py
    (xt/x:str-char "abc" (xt/x:offset 1)))
  => 98)

^{:refer xt.lang.common-spec/x:str-split :added "4.1"}
(fact "splits a string"

  (!.js
    (xt/x:str-split "a/b/c" "/"))
  => ["a" "b" "c"]

  (!.lua
    (xt/x:str-split "a/b/c" "/"))
  => ["a" "b" "c"]

  (!.py
    (xt/x:str-split "a/b/c" "/"))
  => ["a" "b" "c"])

^{:refer xt.lang.common-spec/x:str-join :added "4.1"}
(fact "joins string parts"

  (!.js
    (xt/x:str-join "-" ["a" "b" "c"]))
  => "a-b-c"

  (!.lua
    (xt/x:str-join "-" ["a" "b" "c"]))
  => "a-b-c"

  (!.py
    (xt/x:str-join "-" ["a" "b" "c"]))
  => "a-b-c")

^{:refer xt.lang.common-spec/x:str-index-of :added "4.1"}
(fact "finds the index of a substring"

  ^!.py
  (!.js
    (xt/x:str-index-of "hello/world" "/" (xt/x:offset 0)))
  => 5

  ^!.lua
  (!.js
    (xt/x:str-index-of "hello/world" "/" (xt/x:offset 0)))
  => 5

  ^!.py
  (!.js
    (xt/x:str-index-of "hello/world" "/" (xt/x:offset 0)))
  => 5)

^{:refer xt.lang.common-spec/x:str-substring :added "4.1"}
(fact "gets a substring"

  (!.js
    (xt/x:str-substring "hello/world"
                        (xt/x:offset 3)
                        (xt/x:offset 8)))
  => "lo/wo"

  (!.lua
    (xt/x:str-substring "hello/world"
                        (xt/x:offset 3)
                        (xt/x:offset 8)))
  => "lo/wo"

  (!.py
    (xt/x:str-substring "hello/world"
                        (xt/x:offset 3)
                        (xt/x:offset 8)))
  => "lo/wo")

^{:refer xt.lang.common-spec/x:str-to-upper :added "4.1"}
(fact "converts a string to upper case"

  (!.js
    (xt/x:str-to-upper "hello"))
  => "HELLO"

  (!.lua
    (xt/x:str-to-upper "hello"))
  => "HELLO"

  (!.py
    (xt/x:str-to-upper "hello"))
  => "HELLO")

^{:refer xt.lang.common-spec/x:str-to-lower :added "4.1"}
(fact "converts a string to lower case"

  (!.js
    (xt/x:str-to-lower "HELLO"))
  => "hello"

  (!.lua
    (xt/x:str-to-lower "HELLO"))
  => "hello"

  (!.py
    (xt/x:str-to-lower "HELLO"))
  => "hello")

^{:refer xt.lang.common-spec/x:str-to-fixed :added "4.1"}
(fact "formats a number with fixed decimals"

  (!.js
    (xt/x:str-to-fixed 1.2 2))
  => "1.20"

  (!.lua
    (xt/x:str-to-fixed 1.2 2))
  => "1.20"

  (!.py
    (xt/x:str-to-fixed 1.2 2))
  => "1.20")

^{:refer xt.lang.common-spec/x:str-replace :added "4.1"}
(fact "replaces matching substrings"

  (!.js (xt/x:str-replace "hello-world" "-" "/"))
  => "hello/world"

  (!.lua (xt/x:str-replace "hello-world" "-" "/"))
  => "hello/world"

  (!.py (xt/x:str-replace "hello-world" "-" "/"))
  => "hello/world")

^{:refer xt.lang.common-spec/x:str-trim :added "4.1"}
(fact "trims whitespace from both sides"

  (!.js (xt/x:str-trim "  hello  "))
  => "hello"

  (!.lua (xt/x:str-trim "  hello  "))
  => "hello"

  (!.py (xt/x:str-trim "  hello  "))
  => "hello")

^{:refer xt.lang.common-spec/x:str-trim-left :added "4.1"}
(fact "trims whitespace from the left side"

  (!.js (xt/x:str-trim-left "  hello"))
  => "hello"

  (!.lua (xt/x:str-trim-left "  hello"))
  => "hello"

  (!.py (xt/x:str-trim-left "  hello"))
  => "hello")

^{:refer xt.lang.common-spec/x:str-trim-right :added "4.1"}
(fact "trims whitespace from the right side"

  (!.js (xt/x:str-trim-right "hello  "))
  => "hello"

  (!.lua (xt/x:str-trim-right "hello  "))
  => "hello"

  (!.py (xt/x:str-trim-right "hello  "))
  => "hello")

^{:refer xt.lang.common-spec/x:arr-sort :added "4.1"}
(fact "sorts arrays using key and compare functions"

  (!.js
    (var out [{:id 3} {:id 1} {:id 2}])
    (xt/x:arr-sort out
                   (fn [e] (return (xt/x:get-key e "id")))
                   (fn [a b] (return (xt/x:lt a b))))
    out)
  => [{"id" 1} {"id" 2} {"id" 3}]

  (!.lua
    (var out [{:id 3} {:id 1} {:id 2}])
    (xt/x:arr-sort out
                   (fn [e] (return (xt/x:get-key e "id")))
                   (fn [a b] (return (xt/x:lt a b))))
    out)
  => [{"id" 1} {"id" 2} {"id" 3}]

  (!.py
    (var out [{:id 3} {:id 1} {:id 2}])
    (xt/x:arr-sort out
                   (fn [e] (return (xt/x:get-key e "id")))
                   (fn [a b] (return (xt/x:lt a b))))
    out)
  => [{"id" 1} {"id" 2} {"id" 3}])

^{:refer xt.lang.common-spec/x:arr-clone :added "4.1"}
(fact "clones an array"

  (!.js
    (var src [1 2])
    (var out (xt/x:arr-clone src))
    (xt/x:arr-push src 3)
    out)
  => [1 2]

  (!.lua
    (var src [1 2])
    (var out (xt/x:arr-clone src))
    (xt/x:arr-push src 3)
    out)
  => [1 2]

  (!.py
    (var src [1 2])
    (var out (xt/x:arr-clone src))
    (xt/x:arr-push src 3)
    out)
  => [1 2])

^{:refer xt.lang.common-spec/x:arr-each :added "4.1"}
(fact "iterates each element in an array"

  (!.js
    (var out [])
    (xt/x:arr-each [1 2 3]
                   (fn [e]
                     (xt/x:arr-push out (* e 2))))
    out)
  => [2 4 6]

  (!.lua
    (var out [])
    (xt/x:arr-each [1 2 3]
                   (fn [e]
                     (xt/x:arr-push out (* e 2))))
    out)
  => [2 4 6]

  (!.py
    (var out [])
    (xt/x:arr-each [1 2 3]
                   (fn [e]
                     (xt/x:arr-push out (* e 2))))
    out)
  => [2 4 6])

^{:refer xt.lang.common-spec/x:arr-every :added "4.1"}
(fact "checks whether every array element matches a predicate"

  (!.js
    (xt/x:arr-every [2 4 6]
                    (fn [e] (return (xt/x:even? e)))))
  => true

  (!.lua
    (xt/x:arr-every [2 4 6]
                    (fn [e] (return (xt/x:even? e)))))
  => true

  (!.py
    (xt/x:arr-every [2 4 6]
                    (fn [e] (return (xt/x:even? e)))))
  => true)

^{:refer xt.lang.common-spec/x:arr-some :added "4.1"}
(fact "checks whether any array element matches a predicate"

  (!.js
    (xt/x:arr-some [1 3 4]
                   (fn [e] (return (xt/x:even? e)))))
  => true

  (!.lua
    (xt/x:arr-some [1 3 4]
                   (fn [e] (return (xt/x:even? e)))))
  => true

  (!.py
    (xt/x:arr-some [1 3 4]
                   (fn [e] (return (xt/x:even? e)))))
  => true)

^{:refer xt.lang.common-spec/x:arr-map :added "4.1"}
(fact "maps an array"

  (!.js
    (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
  => [2 4 6]

  (!.lua
    (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
  => [2 4 6]

  (!.py
    (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
  => [2 4 6])

^{:refer xt.lang.common-spec/x:arr-assign :added "4.1"}
(fact "appends one array to another"

  (!.js
    (var out  [1 2])
    (xt/x:arr-assign out [3 4])
    out)
  => [1 2 3 4]

  (!.lua
    (var out  [1 2])
    (xt/x:arr-assign out [3 4])
    out)
  => [1 2 3 4]

  (!.py
    (var out  [1 2])
    (xt/x:arr-assign out [3 4])
    out)
  => [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-concat :added "4.1"}
(fact "concatenates arrays into a new array"

  (!.js
    (var src [1 2])
    [(xt/x:arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]]

  (!.lua
    (var src [1 2])
    [(xt/x:arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]]

  (!.py
    (var src [1 2])
    [(xt/x:arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]])

^{:refer xt.lang.common-spec/x:arr-filter :added "4.1"}
(fact "filters an array"

  (!.js
    (xt/x:arr-filter [2 3 4 5] (fn [e] (return (xt/x:even? e)))))
  => [2 4]

  (!.lua
    (xt/x:arr-filter [2 3 4 5] (fn [e] (return (xt/x:even? e)))))
  => [2 4]

  (!.py
    (xt/x:arr-filter [2 3 4 5] (fn [e] (return (xt/x:even? e)))))
  => [2 4])

^{:refer xt.lang.common-spec/x:arr-foldl :added "4.1"}
(fact "folds arrays from the left"

  (!.js
    (xt/x:arr-foldl [1 2 3 4 5]
                    (fn [out e] (return (+ out e)))
                    0))
  => 15

  (!.lua
    (xt/x:arr-foldl [1 2 3 4 5]
                    (fn [out e] (return (+ out e)))
                    0))
  => 15

  (!.py
    (xt/x:arr-foldl [1 2 3 4 5]
                    (fn [out e] (return (+ out e)))
                    0))
  => 15)

^{:refer xt.lang.common-spec/x:arr-foldr :added "4.1"}
(fact "folds arrays from the right"

  (!.js
    (xt/x:arr-foldr ["a" "b" "c" "d" "e"]
                    (fn [out e] (return (xt/x:cat out e)))
                    ""))
  => "edcba"

  (!.lua
    (xt/x:arr-foldr ["a" "b" "c" "d" "e"]
                    (fn [out e] (return (xt/x:cat out e)))
                    ""))
  => "edcba"

  (!.py
    (xt/x:arr-foldr ["a" "b" "c" "d" "e"]
                    (fn [out e] (return (xt/x:cat out e)))
                    ""))
  => "edcba")

^{:refer xt.lang.common-spec/x:arr-find :added "4.1"}
(fact "keeps the find wrapper pointed at the canonical op"

  (:arglists (meta #'xt/x:arr-find))
  => '([arr pred]))

^{:refer xt.lang.common-spec/x:is-function? :added "4.1"}
(fact "recognises function values"

  (!.js
    (xt/x:is-function? (fn [x] (return x))))
  => true

  (!.lua
    (xt/x:is-function? (fn [x] (return x))))
  => true

  (!.py
    (xt/x:is-function? (fn [x] (return x))))
  => true)

^{:refer xt.lang.common-spec/x:callback :added "4.1"}
(fact "dispatches node-style callbacks through for:return"

  [!.js
   (!.js
     (var out nil)
     (var failure (fn [cb]
                    (cb "ERR" nil)))
     (xt/for:return [[ret err] (failure (xt/x:callback))]
       {:success (:= out ret)
        :error   (:= out err)})
     out)]
  => ["OK" "ERR"]

  [!.lua
   (!.js
     (var out nil)
     (var failure (fn [cb]
                    (cb "ERR" nil)))
     (xt/for:return [[ret err] (failure (xt/x:callback))]
       {:success (:= out ret)
        :error   (:= out err)})
     out)]
  => ["OK" "ERR"]

  [!.py
   (!.js
     (var out nil)
     (var failure (fn [cb]
                    (cb "ERR" nil)))
     (xt/for:return [[ret err] (failure (xt/x:callback))]
       {:success (:= out ret)
        :error   (:= out err)})
     out)]
  => ["OK" "ERR"])

^{:refer xt.lang.common-spec/x:return-run :added "4.1"}
(fact "can be used directly inside for:return"

  (!.js
    (var out nil)
    (xt/for:return [[ok err] (xt/x:return-run
                              (fn [resolve reject]
                                (reject "ERR")))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR"

  (!.lua
    (var out nil)
    (xt/for:return [[ok err] (xt/x:return-run
                              (fn [resolve reject]
                                (reject "ERR")))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR"

  (!.py
    (var out nil)
    (xt/for:return [[ok err] (xt/x:return-run
                              (fn [resolve reject]
                                (reject "ERR")))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR")

^{:refer xt.lang.common-spec/x:eval :added "4.1"}
(fact "evaluates javascript expressions"

  (!.js
    (xt/x:eval "1 + 1"))
  => 2

  (!.lua
    (xt/x:eval "1 + 1"))
  => 2

  (!.py
    (xt/x:eval "1 + 1"))
  => 2)

^{:refer xt.lang.common-spec/x:apply :added "4.1"}
(fact "applies array arguments to functions"

  (!.js
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  (!.lua
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  (!.py
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6)

^{:refer xt.lang.common-spec/x:iter-from-obj :added "4.1"}
(fact "creates iterators over object pairs"

  (!.js
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-obj {:a 1 :b 2})]
      (xt/x:arr-push out e))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order)

  (!.lua
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-obj {:a 1 :b 2})]
      (xt/x:arr-push out e))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order)

  (!.py
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-obj {:a 1 :b 2})]
      (xt/x:arr-push out e))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.common-spec/x:iter-from-arr :added "4.1"}
(fact "creates iterators over arrays"

  (!.js
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  (!.lua
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  (!.py
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3])

^{:refer xt.lang.common-spec/x:iter-from :added "4.1"}
(fact "creates generic iterators from iterable values"

  (!.js
    (var out [])
    (xt/for:iter [e (xt/x:iter-from [2 4 6])]
      (xt/x:arr-push out e))
    out)
  => [2 4 6]

  (!.lua
    (var out [])
    (xt/for:iter [e (xt/x:iter-from [2 4 6])]
      (xt/x:arr-push out e))
    out)
  => [2 4 6]

  (!.py
    (var out [])
    (xt/for:iter [e (xt/x:iter-from [2 4 6])]
      (xt/x:arr-push out e))
    out)
  => [2 4 6])

^{:refer xt.lang.common-spec/x:iter-eq :added "4.1"}
(fact "checks iterator equality in js"

  (!.js
    (var eq-fn (fn [it0 it1 eq-fn]
                 (xt/x:iter-eq it0 it1 eq-fn)))
    [(eq-fn (xt/x:iter-from-arr [1 2 3])
                   (xt/x:iter-from-arr [1 2 3])
                   (fn [a b]
                     (return (== a b))))
     (eq-fn (xt/x:iter-from-arr [1 2 3])
            (xt/x:iter-from-arr [1 2 4])
            (fn [a b]
              (return (== a b))))])
  => [true false]

  (!.lua
    (var eq-fn (fn [it0 it1 eq-fn]
                 (xt/x:iter-eq it0 it1 eq-fn)))
    [(eq-fn (xt/x:iter-from-arr [1 2 3])
                   (xt/x:iter-from-arr [1 2 3])
                   (fn [a b]
                     (return (== a b))))
     (eq-fn (xt/x:iter-from-arr [1 2 3])
            (xt/x:iter-from-arr [1 2 4])
            (fn [a b]
              (return (== a b))))])
  => [true false]

  (!.py
    (var eq-fn (fn [it0 it1 eq-fn]
                 (xt/x:iter-eq it0 it1 eq-fn)))
    [(eq-fn (xt/x:iter-from-arr [1 2 3])
                   (xt/x:iter-from-arr [1 2 3])
                   (fn [a b]
                     (return (== a b))))
     (eq-fn (xt/x:iter-from-arr [1 2 3])
            (xt/x:iter-from-arr [1 2 4])
            (fn [a b]
              (return (== a b))))])
  => [true false])

^{:refer xt.lang.common-spec/x:iter-null :added "4.1"}
(fact "creates empty iterators"

  (!.js
    (xt/x:iter-native? (xt/x:iter-null)))
  => true

  (!.lua
    (xt/x:iter-native? (xt/x:iter-null)))
  => true

  (!.py
    (xt/x:iter-native? (xt/x:iter-null)))
  => true)

^{:refer xt.lang.common-spec/x:iter-next :added "4.1"}
(fact "advances iterators"

  (!.js
    (xt/x:iter-native? (xt/x:iter-from-arr [1 2 3])))
  => true

  (!.lua
    (xt/x:iter-native? (xt/x:iter-from-arr [1 2 3])))
  => true

  (!.py
    (xt/x:iter-native? (xt/x:iter-from-arr [1 2 3])))
  => true)

^{:refer xt.lang.common-spec/x:iter-has? :added "4.1"}
(fact "checks whether values are iterable"

  (!.js
    [(xt/x:iter-has? [1 2 3])
     (xt/x:iter-has? {:a 1})])
  => [true false]

  (!.lua
    [(xt/x:iter-has? [1 2 3])
     (xt/x:iter-has? {:a 1})])
  => [true false]

  (!.py
    [(xt/x:iter-has? [1 2 3])
     (xt/x:iter-has? {:a 1})])
  => [true false])

^{:refer xt.lang.common-spec/x:iter-native? :added "4.1"}
(fact "checks whether values are iterator instances"

  (!.js
    [(xt/x:iter-native? (xt/x:iter-from-arr [1 2 3]))
     (xt/x:iter-native? [1 2 3])])
  => [true false]

  (!.lua
    [(xt/x:iter-native? (xt/x:iter-from-arr [1 2 3]))
     (xt/x:iter-native? [1 2 3])])
  => [true false]

  (!.py
    [(xt/x:iter-native? (xt/x:iter-from-arr [1 2 3]))
     (xt/x:iter-native? [1 2 3])])
  => [true false])

^{:refer xt.lang.common-spec/x:return-encode :added "4.1"}
(fact "encodes return payloads as json"

  (!.js
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (xt/x:json-decode (encode-fn {:a 1} "id" "key")))
  => {"id" "id"
      "key" "key"
      "type" "data"
      "value" {"a" 1}}

  (!.lua
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (xt/x:json-decode (encode-fn {:a 1} "id" "key")))
  => {"id" "id"
      "key" "key"
      "type" "data"
      "value" {"a" 1}}

  (!.py
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (xt/x:json-decode (encode-fn {:a 1} "id" "key")))
  => {"id" "id"
      "key" "key"
      "type" "data"
      "value" {"a" 1}})

^{:refer xt.lang.common-spec/x:return-wrap :added "4.1"}
(fact "wraps return values through encoder functions"

  (!.js
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (xt/x:return-wrap gen-fn wrap-fn)))
    (xt/x:json-decode
     (wrap-fn (fn []
                (return 3))
              (fn [out]
                (return
                 (encode-fn out "id-A" "key-B"))))))
  => {"id" "id-A"
      "key" "key-B"
      "type" "data"
      "return" "number"
      "value" 3}

  (!.lua
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (xt/x:return-wrap gen-fn wrap-fn)))
    (xt/x:json-decode
     (wrap-fn (fn []
                (return 3))
              (fn [out]
                (return
                 (encode-fn out "id-A" "key-B"))))))
  => {"id" "id-A"
      "key" "key-B"
      "type" "data"
      "return" "number"
      "value" 3}

  (!.py
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (xt/x:return-wrap gen-fn wrap-fn)))
    (xt/x:json-decode
     (wrap-fn (fn []
                (return 3))
              (fn [out]
                (return
                 (encode-fn out "id-A" "key-B"))))))
  => {"id" "id-A"
      "key" "key-B"
      "type" "data"
      "return" "number"
      "value" 3})

^{:refer xt.lang.common-spec/x:return-eval :added "4.1"}
(fact "evaluates code through wrapped return handlers"

  (!.js
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (xt/x:return-wrap gen-fn wrap-fn)))
    (var eval-fn
         (fn [s re-wrap-fn]
           (xt/x:return-eval s re-wrap-fn)))
    (xt/x:json-decode
     (eval-fn "1 + 1"
              (fn [f]
                (return
                 (wrap-fn f
                          (fn [out]
                            (return
                             (encode-fn out "id-A" "key-B")))))))))
  => {"return" "number", "key" "key-B", "id" "id-A", "value" 2, "type" "data"}

  (!.lua
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (xt/x:return-wrap gen-fn wrap-fn)))
    (var eval-fn
         (fn [s re-wrap-fn]
           (xt/x:return-eval s re-wrap-fn)))
    (xt/x:json-decode
     (eval-fn "1 + 1"
              (fn [f]
                (return
                 (wrap-fn f
                          (fn [out]
                            (return
                             (encode-fn out "id-A" "key-B")))))))))
  => {"return" "number", "key" "key-B", "id" "id-A", "value" 2, "type" "data"}

  (!.py
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (xt/x:return-wrap gen-fn wrap-fn)))
    (var eval-fn
         (fn [s re-wrap-fn]
           (xt/x:return-eval s re-wrap-fn)))
    (xt/x:json-decode
     (eval-fn "1 + 1"
              (fn [f]
                (return
                 (wrap-fn f
                          (fn [out]
                            (return
                             (encode-fn out "id-A" "key-B")))))))))
  => {"return" "number", "key" "key-B", "id" "id-A", "value" 2, "type" "data"})

^{:refer xt.lang.common-spec/x:bit-and :added "4.1"}
(fact "computes bitwise and"

  (!.js (xt/x:bit-and 6 3))
  => 2

  (!.lua (xt/x:bit-and 6 3))
  => 2

  (!.py (xt/x:bit-and 6 3))
  => 2)

^{:refer xt.lang.common-spec/x:bit-or :added "4.1"}
(fact "computes bitwise or"

  (!.js (xt/x:bit-or 6 3))
  => 7

  (!.lua (xt/x:bit-or 6 3))
  => 7

  (!.py (xt/x:bit-or 6 3))
  => 7)

^{:refer xt.lang.common-spec/x:bit-lshift :added "4.1"}
(fact "computes bitwise left shifts"

  (!.js (xt/x:bit-lshift 3 2))
  => 12

  (!.lua (xt/x:bit-lshift 3 2))
  => 12

  (!.py (xt/x:bit-lshift 3 2))
  => 12)

^{:refer xt.lang.common-spec/x:bit-rshift :added "4.1"}
(fact "computes bitwise right shifts"

  (!.js (xt/x:bit-rshift 12 2))
  => 3

  (!.lua (xt/x:bit-rshift 12 2))
  => 3

  (!.py (xt/x:bit-rshift 12 2))
  => 3)

^{:refer xt.lang.common-spec/x:bit-xor :added "4.1"}
(fact "computes bitwise xor"

  (!.js (xt/x:bit-xor 6 3))
  => 5

  (!.lua (xt/x:bit-xor 6 3))
  => 5

  (!.py (xt/x:bit-xor 6 3))
  => 5)

^{:refer xt.lang.common-spec/x:global-set :added "4.1"}
(fact "writes values to the shared global map"

  (!.js
    (var set-fn
         (fn []
           (xt/x:global-set COMMON_SPEC_GLOBAL 1)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
    (var del-fn
         (fn []
           (xt/x:global-del COMMON_SPEC_GLOBAL)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
          
    [(set-fn)
     (!:G COMMON_SPEC_GLOBAL)
     (del-fn)])
  => [true 1 false]

  (!.lua
    (var set-fn
         (fn []
           (xt/x:global-set COMMON_SPEC_GLOBAL 1)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
    (var del-fn
         (fn []
           (xt/x:global-del COMMON_SPEC_GLOBAL)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
          
    [(set-fn)
     (!:G COMMON_SPEC_GLOBAL)
     (del-fn)])
  => [true 1 false]

  (!.py
    (var set-fn
         (fn []
           (xt/x:global-set COMMON_SPEC_GLOBAL 1)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
    (var del-fn
         (fn []
           (xt/x:global-del COMMON_SPEC_GLOBAL)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
          
    [(set-fn)
     (!:G COMMON_SPEC_GLOBAL)
     (del-fn)])
  => [true 1 false])

^{:refer xt.lang.common-spec/x:global-del :added "4.1"}
(fact "removes values from the shared global map"

  (!.js
    (xt/x:global-set COMMON_SPEC_DELETE 1)
    (xt/x:global-del COMMON_SPEC_DELETE)
    (!:G COMMON_SPEC_DELETE))
  => nil

  (!.lua
    (xt/x:global-set COMMON_SPEC_DELETE 1)
    (xt/x:global-del COMMON_SPEC_DELETE)
    (!:G COMMON_SPEC_DELETE))
  => nil

  (!.py
    (xt/x:global-set COMMON_SPEC_DELETE 1)
    (xt/x:global-del COMMON_SPEC_DELETE)
    (!:G COMMON_SPEC_DELETE))
  => nil)

^{:refer xt.lang.common-spec/x:global-has? :added "4.1"}
(fact "checks whether the shared global map contains a value"

  (!.js
    (var set-fn
         (fn []
           (xt/x:global-set COMMON_SPEC_GLOBAL 1)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
    (var del-fn
         (fn []
           (xt/x:global-del COMMON_SPEC_GLOBAL)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
          
    [(set-fn)
     (del-fn)])
  => [true false]

  (!.lua
    (var set-fn
         (fn []
           (xt/x:global-set COMMON_SPEC_GLOBAL 1)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
    (var del-fn
         (fn []
           (xt/x:global-del COMMON_SPEC_GLOBAL)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
          
    [(set-fn)
     (del-fn)])
  => [true false]

  (!.py
    (var set-fn
         (fn []
           (xt/x:global-set COMMON_SPEC_GLOBAL 1)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
    (var del-fn
         (fn []
           (xt/x:global-del COMMON_SPEC_GLOBAL)
           (return (xt/x:global-has? COMMON_SPEC_GLOBAL))))
          
    [(set-fn)
     (del-fn)])
  => [true false])

^{:refer xt.lang.common-spec/x:proto-get :added "4.1"}
(fact "retrieves object prototypes"

  (!.js
    (var set-fn
         (fn [obj proto]
           (xt/x:proto-set obj proto nil)))
    (var proto {:label "proto"})
    (var obj {})
    (set-fn obj proto)
    (. (xt/x:proto-get obj "label") ["label"]))
  => "proto"

  (!.lua
    (var set-fn
         (fn [obj proto]
           (xt/x:proto-set obj proto nil)))
    (var proto {:label "proto"})
    (var obj {})
    (set-fn obj proto)
    (. (xt/x:proto-get obj "label") ["label"]))
  => "proto"

  (!.py
    (var set-fn
         (fn [obj proto]
           (xt/x:proto-set obj proto nil)))
    (var proto {:label "proto"})
    (var obj {})
    (set-fn obj proto)
    (. (xt/x:proto-get obj "label") ["label"]))
  => "proto")

^{:refer xt.lang.common-spec/x:proto-set :added "4.1"}
(fact "assigns object prototypes"

  (!.js
    (var set-fn
         (fn [obj proto]
           (xt/x:proto-set obj proto nil)))
    (var proto {:label "proto"})
    (var obj {})
    (set-fn obj proto)
    (. (xt/x:proto-get obj "label") ["label"]))

  (!.lua
    (var set-fn
         (fn [obj proto]
           (xt/x:proto-set obj proto nil)))
    (var proto {:label "proto"})
    (var obj {})
    (set-fn obj proto)
    (. (xt/x:proto-get obj "label") ["label"]))

  (!.py
    (var set-fn
         (fn [obj proto]
           (xt/x:proto-set obj proto nil)))
    (var proto {:label "proto"})
    (var obj {})
    (set-fn obj proto)
    (. (xt/x:proto-get obj "label") ["label"])))

^{:refer xt.lang.common-spec/x:proto-create :added "4.1"}
(fact "creates prototypes with this-bound methods"

  (!.js
    (var proto (xt/x:proto-create
                {:describe (fn [self suffix]
                             (return (+ (. self ["name"]) suffix)))}))
    (var obj {})
    (xt/x:proto-set obj proto nil)
    (:= (. obj ["name"]) "alpha")
    (. obj (describe "!")))
  => "alpha!"

  (!.lua
    (var proto (xt/x:proto-create
                {:describe (fn [self suffix]
                             (return (+ (. self ["name"]) suffix)))}))
    (var obj {})
    (xt/x:proto-set obj proto nil)
    (:= (. obj ["name"]) "alpha")
    (. obj (describe "!")))
  => "alpha!"

  (!.py
    (var proto (xt/x:proto-create
                {:describe (fn [self suffix]
                             (return (+ (. self ["name"]) suffix)))}))
    (var obj {})
    (xt/x:proto-set obj proto nil)
    (:= (. obj ["name"]) "alpha")
    (. obj (describe "!")))
  => "alpha!")

^{:refer xt.lang.common-spec/x:proto-tostring :added "4.1"}
(fact "expands and emits the lua tostring metamethod key"

  (:arglists (meta #'xt/x:proto-tostring))
  => '([value]))

^{:refer xt.lang.common-spec/x:random :added "4.1"}
(fact "returns javascript random values"

  (!.js
    (var out (xt/x:random))
    (and (>= out 0)
         (< out 1)))
  => true

  (!.lua
    (var out (xt/x:random))
    (and (>= out 0)
         (< out 1)))
  => true

  (!.py
    (var out (xt/x:random))
    (and (>= out 0)
         (< out 1)))
  => true)

^{:refer xt.lang.common-spec/x:throw :added "4.1"}
(fact "expands to the canonical throw form"

  (:arglists (meta #'xt/x:throw))
  => '([value]))

^{:refer xt.lang.common-spec/x:now-ms :added "4.1"}
(fact "expands and emits a millisecond time expression"

  (!.js
    (> (xt/x:now-ms) 0))
  => true

  (!.lua
    (> (xt/x:now-ms) 0))
  => true

  (!.py
    (> (xt/x:now-ms) 0))
  => true)

^{:refer xt.lang.common-spec/x:unpack :added "4.1"}
(fact "spreads arrays into positional arguments"

  (!.js
    ((fn [a b c]
       (return (+ a b c)))
     (xt/x:unpack [1 2 3])))
  => 6

  (!.lua
    ((fn [a b c]
       (return (+ a b c)))
     (xt/x:unpack [1 2 3])))
  => 6

  (!.py
    ((fn [a b c]
       (return (+ a b c)))
     (xt/x:unpack [1 2 3])))
  => 6)

^{:refer xt.lang.common-spec/x:notify-http :added "4.1"}
(fact "posts encoded values through fetch"

  (!.js
    (var out nil)
    (:= (!:G fetch)
        (fn [url opts]
          (:= out [url
                   (xt/x:json-decode (. opts ["body"]))
                   (. opts ["method"])])))
    (xt/x:notify-http "localhost"
                      8080
                      {:a 1}
                      "id"
                      "key"
                      {})
    out)
  => ["http://localhost:8080"
      {"id" "id"
       "key" "key"
       "type" "data"
       "value" {"a" 1}}
      "POST"]

  (!.lua
    (var out nil)
    (:= (!:G fetch)
        (fn [url opts]
          (:= out [url
                   (xt/x:json-decode (. opts ["body"]))
                   (. opts ["method"])])))
    (xt/x:notify-http "localhost"
                      8080
                      {:a 1}
                      "id"
                      "key"
                      {})
    out)
  => ["http://localhost:8080"
      {"id" "id"
       "key" "key"
       "type" "data"
       "value" {"a" 1}}
      "POST"]

  (!.py
    (var out nil)
    (:= (!:G fetch)
        (fn [url opts]
          (:= out [url
                   (xt/x:json-decode (. opts ["body"]))
                   (. opts ["method"])])))
    (xt/x:notify-http "localhost"
                      8080
                      {:a 1}
                      "id"
                      "key"
                      {})
    out)
  => ["http://localhost:8080"
      {"id" "id"
       "key" "key"
       "type" "data"
       "value" {"a" 1}}
      "POST"])

^{:refer xt.lang.common-spec/x:b64-encode :added "4.1"}
(fact "encodes base64 strings"

  (!.js
    (xt/x:b64-encode "hello"))
  => "aGVsbG8="

  (!.lua
    (xt/x:b64-encode "hello"))
  => "aGVsbG8="

  (!.py
    (xt/x:b64-encode "hello"))
  => "aGVsbG8=")

^{:refer xt.lang.common-spec/x:b64-decode :added "4.1"}
(fact "decodes base64 strings"

  (!.js
    (xt/x:b64-decode "aGVsbG8="))
  => "hello"

  (!.lua
    (xt/x:b64-decode "aGVsbG8="))
  => "hello"

  (!.py
    (xt/x:b64-decode "aGVsbG8="))
  => "hello")

^{:refer xt.lang.common-spec/x:cache :added "4.1"}
(fact "selects the global cache store"

  (!.js
    (:= (!:G window)
        {:localStorage  {:name "local"}
         :sessionStorage {:name "session"}})
    (. (xt/x:cache "GLOBAL") ["name"]))
  => "local"

  (!.lua
    (:= (!:G window)
        {:localStorage  {:name "local"}
         :sessionStorage {:name "session"}})
    (. (xt/x:cache "GLOBAL") ["name"]))
  => "local"

  (!.py
    (:= (!:G window)
        {:localStorage  {:name "local"}
         :sessionStorage {:name "session"}})
    (. (xt/x:cache "GLOBAL") ["name"]))
  => "local")

^{:refer xt.lang.common-spec/x:cache-list :added "4.1"}
(fact "lists cache keys"

  (!.js
    (:= (!:G window)
        {:localStorage  {"_keys" ["a" "b"]}
         :sessionStorage {"_keys" []}})
    (xt/x:cache-list))
  => ["a" "b"]

  (!.lua
    (:= (!:G window)
        {:localStorage  {"_keys" ["a" "b"]}
         :sessionStorage {"_keys" []}})
    (xt/x:cache-list))
  => ["a" "b"]

  (!.py
    (:= (!:G window)
        {:localStorage  {"_keys" ["a" "b"]}
         :sessionStorage {"_keys" []}})
    (xt/x:cache-list))
  => ["a" "b"])

^{:refer xt.lang.common-spec/x:cache-flush :added "4.1"}
(fact "flushes cache stores"

  (!.js
    (var out nil)
    (var cache {:clear (fn []
                         (:= out "flushed"))})
    (xt/x:cache-flush cache)
    out)
  => "flushed"

  (!.lua
    (var out nil)
    (var cache {:clear (fn []
                         (:= out "flushed"))})
    (xt/x:cache-flush cache)
    out)
  => "flushed"

  (!.py
    (var out nil)
    (var cache {:clear (fn []
                         (:= out "flushed"))})
    (xt/x:cache-flush cache)
    out)
  => "flushed")

^{:refer xt.lang.common-spec/x:cache-get :added "4.1"}
(fact "reads cache values"

  (!.js
    (var cache {:getItem (fn [k]
                           (return (+ "value:" k)))})
    (xt/x:cache-get cache "key"))
  => "value:key"

  (!.lua
    (var cache {:getItem (fn [k]
                           (return (+ "value:" k)))})
    (xt/x:cache-get cache "key"))
  => "value:key"

  (!.py
    (var cache {:getItem (fn [k]
                           (return (+ "value:" k)))})
    (xt/x:cache-get cache "key"))
  => "value:key")

^{:refer xt.lang.common-spec/x:cache-set :added "4.1"}
(fact "writes cache values"

  (!.js
    (var out nil)
    (var cache {:setItem (fn [k v]
                           (:= out [k v])
                           (return v))})
    [(xt/x:cache-set cache "key" "value")
     out])
  => ["value" ["key" "value"]]

  (!.lua
    (var out nil)
    (var cache {:setItem (fn [k v]
                           (:= out [k v])
                           (return v))})
    [(xt/x:cache-set cache "key" "value")
     out])
  => ["value" ["key" "value"]]

  (!.py
    (var out nil)
    (var cache {:setItem (fn [k v]
                           (:= out [k v])
                           (return v))})
    [(xt/x:cache-set cache "key" "value")
     out])
  => ["value" ["key" "value"]])

^{:refer xt.lang.common-spec/x:cache-del :added "4.1"}
(fact "deletes cache values"

  (!.js
    (var out nil)
    (var cache {:removeItem (fn [k]
                              (:= out k))})
    (xt/x:cache-del cache "key")
    out)
  => "key"

  (!.lua
    (var out nil)
    (var cache {:removeItem (fn [k]
                              (:= out k))})
    (xt/x:cache-del cache "key")
    out)
  => "key"

  (!.py
    (var out nil)
    (var cache {:removeItem (fn [k]
                              (:= out k))})
    (xt/x:cache-del cache "key")
    out)
  => "key")

^{:refer xt.lang.common-spec/x:cache-incr :added "4.1"}
(fact "increments cached numeric values"

  (!.js
    (var state {"count" "2"})
    (var cache {:getItem (fn [k]
                           (return (. state [k])))
                :setItem (fn [k v]
                           (:= (. state [k]) v)
                           (return v))})
    (xt/x:cache-incr cache "count" 3))
  => 5

  (!.lua
    (var state {"count" "2"})
    (var cache {:getItem (fn [k]
                           (return (. state [k])))
                :setItem (fn [k v]
                           (:= (. state [k]) v)
                           (return v))})
    (xt/x:cache-incr cache "count" 3))
  => 5

  (!.py
    (var state {"count" "2"})
    (var cache {:getItem (fn [k]
                           (return (. state [k])))
                :setItem (fn [k v]
                           (:= (. state [k]) v)
                           (return v))})
    (xt/x:cache-incr cache "count" 3))
  => 5)

^{:refer xt.lang.common-spec/x:slurp :added "4.1"}
(fact "keeps the slurp wrapper intact"

  (:arglists (meta #'xt/x:slurp))
  => '([path]))

^{:refer xt.lang.common-spec/x:spit :added "4.1"}
(fact "keeps the spit wrapper intact"

  (:arglists (meta #'xt/x:spit))
  => '([path value]))

^{:refer xt.lang.common-spec/x:json-encode :added "4.1"}
(fact "encodes lua data structures as json"

  (!.js (xt/x:json-encode {:a 1}))
  => #"\{\"a\":\s*1\}"

  (!.lua (xt/x:json-encode {:a 1}))
  => #"\{\"a\":\s*1\}"

  (!.py (xt/x:json-encode {:a 1}))
  => #"\{\"a\":\s*1\}")

^{:refer xt.lang.common-spec/x:json-decode :added "4.1"}
(fact "decodes json strings into lua data structures"

  (!.js (xt/x:json-decode "{\"a\":1}"))
  => {"a" 1}

  (!.lua (xt/x:json-decode "{\"a\":1}"))
  => {"a" 1}

  (!.py (xt/x:json-decode "{\"a\":1}"))
  => {"a" 1})

^{:refer xt.lang.common-spec/x:shell :added "4.1"}
(fact "executes shell commands asynchronously"

  (notify/wait-on :js
    (xt/x:shell "printf hello"
                {:success (fn [res]
                            (repl/notify res))
                 :error   (fn [err]
                            (repl/notify "ERR"))}))
  => #"hello"

  (notify/wait-on :lua
    (xt/x:shell "printf hello"
                {:success (fn [res]
                            (repl/notify res))
                 :error   (fn [err]
                            (repl/notify "ERR"))}))
  => #"hello"

  (notify/wait-on :python
    (xt/x:shell "printf hello"
                {:success (fn [res]
                            (repl/notify res))
                 :error   (fn [err]
                            (repl/notify "ERR"))}))
  => #"hello")

^{:refer xt.lang.common-spec/x:thread-spawn :added "4.1"}
(fact "spawns js promise-backed threads"

  (notify/wait-on :js
    (-> (xt/x:thread-spawn (fn []
                             (return "OK")))
        (. (then (repl/>notify)))))
  => "OK"

  (notify/wait-on :lua
    (-> (xt/x:thread-spawn (fn []
                             (return "OK")))
        (. (then (repl/>notify)))))
  => "OK"

  (notify/wait-on :python
    (-> (xt/x:thread-spawn (fn []
                             (return "OK")))
        (. (then (repl/>notify)))))
  => "OK")

^{:refer xt.lang.common-spec/x:thread-join :added "4.1"}
(fact "throws for unsupported js thread joins"

  (!.js
    (xt/x:thread-join {}))
  => (throws)

  (!.lua
    (xt/x:thread-join {}))
  => (throws)

  (!.py
    (xt/x:thread-join {}))
  => (throws))

^{:refer xt.lang.common-spec/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  (notify/wait-on :js
    (-> (xt/x:with-delay (fn []
                           (return "LATER"))
                         20)
        (. (then (repl/>notify)))))
  => "LATER"

  (notify/wait-on :lua
    (-> (xt/x:with-delay (fn []
                           (return "LATER"))
                         20)
        (. (then (repl/>notify)))))
  => "LATER"

  (notify/wait-on :python
    (-> (xt/x:with-delay (fn []
                           (return "LATER"))
                         20)
        (. (then (repl/>notify)))))
  => "LATER")

^{:refer xt.lang.common-spec/x:start-interval :added "4.1"}
(fact "keeps the start-interval wrapper intact"

  (:arglists (meta #'xt/x:start-interval))
  => '([ms f]))

^{:refer xt.lang.common-spec/x:stop-interval :added "4.1"}
(fact "keeps the stop-interval wrapper intact"

  (:arglists (meta #'xt/x:stop-interval))
  => '([id]))

^{:refer xt.lang.common-spec/x:uri-encode :added "4.1"}
(fact "encodes uri components"

  (!.js
    (xt/x:uri-encode "hello world"))
  => "hello%20world"

  (!.lua
    (xt/x:uri-encode "hello world"))
  => "hello%20world"

  (!.py
    (xt/x:uri-encode "hello world"))
  => "hello%20world")

^{:refer xt.lang.common-spec/x:uri-decode :added "4.1"}
(fact "decodes uri components"

  (!.js
    (xt/x:uri-decode "hello%20world"))
  => "hello world"

  (!.lua
    (xt/x:uri-decode "hello%20world"))
  => "hello world"

  (!.py
    (xt/x:uri-decode "hello%20world"))
  => "hello world")

(comment
  

  )
