(ns xtbench.dart.lang.spec-base-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as xts]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-iter :as xti]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-base/for:array :added "4.1"}
(fact "iterates arrays in order"

  (!.dt
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])

^{:refer xt.lang.spec-base/for:object :added "4.1"}
(fact "iterates object key value pairs"

  (!.dt
    (var out [])
    (xt/for:object [[k v] {:a 1 :b 2}]
      (xt/x:arr-push out [k v]))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.spec-base/for:index :added "4.1"}
(fact "iterates a numeric range"

  (!.dt
    (var out [])
    (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]]
      (xt/x:arr-push out i))
    out)
  => [0 2])

^{:refer xt.lang.spec-base/for:iter :added "4.1"}
(fact "expands to the canonical iterator form"

  (!.dt
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3])

^{:refer xt.lang.spec-base/return-run :added "4.1"}
(fact "supports final returns through for:return"

  (!.dt
    (xt/return-run [resolve reject]
      (resolve "OK")))
  => (throws))

^{:refer xt.lang.spec-base/for:return :added "4.1"}
(fact "dispatches success and error branches"

  (!.dt
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                               (resolve "OK"))]
      {:success (:= out ok)
       :error   (:= out err)})
    out)
  => "OK"

  (!.dt
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                               (reject "ERR"))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR")

^{:refer xt.lang.spec-base/for:try :added "4.1"}
(fact "expands to the canonical try form"

  (!.dt
    (var add (fn []
               (xt/for:try [[ok err] (do:> (xt/x:err "ERROR"))]
                 {:success (return ok)
                  :error   (return "ERR")})))
    (add))
  => "ERR")

^{:refer xt.lang.spec-base/x:get-idx :added "4.1"}
(fact "reads the first indexed value"

  (!.dt
    (xt/x:get-idx ["a" "b" "c"] (xt/x:offset 0)))
  => "a")

^{:refer xt.lang.spec-base/x:set-idx :added "4.1"}
(fact "writes an indexed value"

  (!.dt
    (var out ["a" "b" "c"])
    (xt/x:set-idx out (xt/x:offset 1) "B")
    out)
  => ["a" "B" "c"])

^{:refer xt.lang.spec-base/x:first :added "4.1"}
(fact "gets the first array element"

  (!.dt
    (xt/x:first ["a" "b" "c"]))
  => "a")

^{:refer xt.lang.spec-base/x:second :added "4.1"}
(fact "gets the second array element"

  (!.dt
    (xt/x:second ["a" "b" "c"]))
  => "b")

^{:refer xt.lang.spec-base/x:last :added "4.1"}
(fact "gets the last array element"

  (!.dt
    (xt/x:last ["a" "b" "c" "d"]))
  => "d")

^{:refer xt.lang.spec-base/x:second-last :added "4.1"}
(fact "gets the element before the last"

  (!.dt
    (xt/x:second-last ["a" "b" "c" "d"]))
  => "c")

^{:refer xt.lang.spec-base/x:arr-remove :added "4.1"}
(fact "removes an element from an array"

  (!.dt
    (do (var out ["a" "b" "c" "d"])
        (xt/x:arr-remove out 1)
        out))
  => ["a" "c" "d"])

^{:refer xt.lang.spec-base/x:arr-push :added "4.1"}
(fact "pushes an element onto an array"

  (!.dt
    (var out ["a" "b" "c"])
    (xt/x:arr-push out "D")
    out)
  => ["a" "b" "c" "D"])

^{:refer xt.lang.spec-base/x:arr-pop :added "4.1"}
(fact "pops the last element from an array"

  (!.dt
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop out) out])
  => ["d" ["a" "b" "c"]])

^{:refer xt.lang.spec-base/x:arr-push-first :added "4.1"}
(fact "pushes an element to the front of an array"

  (!.dt
    (var out ["a" "b" "c"])
    (xt/x:arr-push-first out "D")
    out)
  => ["D" "a" "b" "c"])

^{:refer xt.lang.spec-base/x:arr-pop-first :added "4.1"}
(fact "pops the first element from an array"

  (!.dt
    (var out ["a" "b" "c" "d"])
    [(xt/x:arr-pop-first out) out])
  => ["a" ["b" "c" "d"]])

^{:refer xt.lang.spec-base/x:arr-insert :added "4.1"}
(fact "inserts an element into an array"

  (!.dt
    (var out ["a" "b" "c"])
    (xt/x:arr-insert out (xt/x:offset 1) "D")
    out)
  => ["a" "D" "b" "c"])

^{:refer xt.lang.spec-base/x:arr-slice :added "4.1"}
(fact "slices a range from an array"

  (!.dt
    (xt/x:arr-slice ["a" "b" "c" "d" "e"]
                    1
                    3))
  => ["b" "c"])

^{:refer xt.lang.spec-base/x:arr-reverse :added "4.1"}
(fact "reverses an array"

  (!.dt
    (xt/x:arr-reverse ["a" "b" "c"]))
  => ["c" "b" "a"])

^{:refer xt.lang.spec-base/x:del :added "4.1"}
(fact "expands and emits a lua delete form"

  (!.dt
    (var out {:a 1 :b 2})
    (xt/x:del (. out ["a"]))
    out)
  => {"b" 2})

^{:refer xt.lang.spec-base/x:cat :added "4.1"}
(fact "concatenates strings"

  (!.dt
    (xt/x:cat "hello" "-" "world"))
  => "hello-world")

^{:refer xt.lang.spec-base/x:len :added "4.1"}
(fact "gets the collection length"

  (!.dt
    (xt/x:len ["a" "b" "c"]))
  => 3)

^{:refer xt.lang.spec-base/x:err :added "4.1"}
(fact "expands and emits a lua error form"

  (!.dt
    (var err-fn (fn []
                  (xt/x:err "ERR")))
    (err-fn))
  => #"(?s)Unhandled exception:.*ERR.*")

^{:refer xt.lang.spec-base/x:type-native :added "4.1"}
(fact "expands and emits the lua type helper"

  (!.dt
    (var type-fn (fn [obj]
                   (return
                    (xt/x:type-native obj))))
    [(type-fn {})
     (type-fn [])
     (type-fn [1])])
  => ["object" "array" "array"])

^{:refer xt.lang.spec-base/x:offset :added "4.1"}
(fact "uses the grammar base offset"

  (!.dt    
    (xt/x:offset 10))
  => 10)

^{:refer xt.lang.spec-base/x:offset-rev :added "4.1"}
(fact "uses the reverse grammar offset"

  (!.dt
    (xt/x:offset-rev 10))
  => 9)

^{:refer xt.lang.spec-base/x:offset-len :added "4.1"}
(fact "uses the length grammar offset"

  (!.dt
    (xt/x:offset-len 10))
  => 9)

^{:refer xt.lang.spec-base/x:offset-rlen :added "4.1"}
(fact "uses the reverse length grammar offset"

  (!.dt
    (xt/x:offset-rlen 10))
  => 10)

^{:refer xt.lang.spec-base/x:lu-create :added "4.1"}
(fact "creates a lookup table wrapper"

  (!.dt
    (var lu (xt/x:lu-create))
    (var lu-A1 {"A" "A"})
    (var lu-A2 {"A" "A"})
    (xt/x:lu-set lu lu-A1 "A1")
    (xt/x:lu-set lu lu-A2 "A2")
    [(xt/x:lu-get lu lu-A1)
     (xt/x:lu-get lu lu-A2)])
  => ["A1" "A2"])

^{:refer xt.lang.spec-base/x:lu-eq :added "4.1"}
(fact "compares lookup keys using lua identity"

  (!.dt
    (var obj-a {:id 1})
    (var obj-b {:id 1})
    [(xt/x:lu-eq obj-a obj-a)
     (xt/x:lu-eq obj-a obj-b)
     (xt/x:lu-eq obj-b obj-b)])
  => [true false true])

^{:refer xt.lang.spec-base/x:lu-get :added "4.1"}
(fact "reads values from a lookup table"

  (!.dt
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value")

^{:refer xt.lang.spec-base/x:lu-set :added "4.1"}
(fact "writes values into a lookup table"

  (!.dt
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-get lu lu-key))
  => "value")

^{:refer xt.lang.spec-base/x:lu-del :added "4.1"}
(fact "removes values from a lookup table"

  (!.dt
    (var lu (xt/x:lu-create))
    (var lu-key {:id 1})
    (xt/x:lu-set lu lu-key "value")
    (xt/x:lu-del lu lu-key)
    (xt/x:lu-get lu lu-key))
  => nil)

^{:refer xt.lang.spec-base/x:m-abs :added "4.1"}
(fact "computes absolute values"

  (!.dt (xt/x:m-abs -3))
  => 3)

^{:refer xt.lang.spec-base/x:m-acos :added "4.1"}
(fact "computes inverse cosine"

  (!.dt (xt/x:m-acos 1))
  => (approx 0))

^{:refer xt.lang.spec-base/x:m-asin :added "4.1"}
(fact "computes inverse sine"

  (!.dt (xt/x:m-asin 0))
  => (approx 0))

^{:refer xt.lang.spec-base/x:m-atan :added "4.1"}
(fact "computes inverse tangent"

  (!.dt (xt/x:m-atan 0))
  => (approx 0))

^{:refer xt.lang.spec-base/x:m-ceil :added "4.1"}
(fact "rounds numbers upward"

  (!.dt (xt/x:m-ceil 1.2))
  => 2)

^{:refer xt.lang.spec-base/x:m-cos :added "4.1"}
(fact "computes cosine"

  (!.dt (xt/x:m-cos 0))
  => (approx 1))

^{:refer xt.lang.spec-base/x:m-cosh :added "4.1"}
(fact "computes hyperbolic cosine"

  (!.dt (xt/x:m-cosh 0))
  => (approx 1))

^{:refer xt.lang.spec-base/x:m-exp :added "4.1"}
(fact "computes the exponential function"

  (!.dt (xt/x:m-exp 0))
  => (approx 1))

^{:refer xt.lang.spec-base/x:m-floor :added "4.1"}
(fact "rounds numbers downward"

  (!.dt (xt/x:m-floor 1.8))
  => 1)

^{:refer xt.lang.spec-base/x:m-loge :added "4.1"}
(fact "computes the natural logarithm"

  (!.dt (xt/x:m-loge 1))
  => (approx 0))

^{:refer xt.lang.spec-base/x:m-log10 :added "4.1"}
(fact "computes the base-10 logarithm"

  (!.dt (xt/x:m-log10 100))
  => (approx 2))

^{:refer xt.lang.spec-base/x:m-max :added "4.1"}
(fact "computes the maximum value"

  (!.dt (xt/x:m-max 3 5))
  => 5)

^{:refer xt.lang.spec-base/x:m-mod :added "4.1"}
(fact "computes modulo values"

  (!.dt (xt/x:m-mod 10 3))
  => 1)

^{:refer xt.lang.spec-base/x:m-min :added "4.1"}
(fact "computes the minimum value"

  (!.dt (xt/x:m-min 3 5))
  => 3)

^{:refer xt.lang.spec-base/x:m-pow :added "4.1"}
(fact "raises numbers to a power"

  (!.dt (xt/x:m-pow 2 4))
  => 16)

^{:refer xt.lang.spec-base/x:m-quot :added "4.1"}
(fact "computes integer quotients"

  (!.dt (xt/x:m-quot 7 2))
  => 3)

^{:refer xt.lang.spec-base/x:m-sin :added "4.1"}
(fact "computes sine"

  (!.dt (xt/x:m-sin 0))
  => (approx 0))

^{:refer xt.lang.spec-base/x:m-sinh :added "4.1"}
(fact "computes hyperbolic sine"

  (!.dt (xt/x:m-sinh 0))
  => (approx 0))

^{:refer xt.lang.spec-base/x:m-sqrt :added "4.1"}
(fact "computes square roots"

  (!.dt (xt/x:m-sqrt 9))
  => (approx 3))

^{:refer xt.lang.spec-base/x:m-tan :added "4.1"}
(fact "computes tangent"

  (!.dt (xt/x:m-tan 0))
  => (approx 0))

^{:refer xt.lang.spec-base/x:m-tanh :added "4.1"}
(fact "computes hyperbolic tangent"

  (!.dt (xt/x:m-tanh 0))
  => (approx 0))

^{:refer xt.lang.spec-base/x:not-nil? :added "4.1"}
(fact "checks for non-nil values"

  (!.dt
    (xt/x:not-nil? 0))
  => true)

^{:refer xt.lang.spec-base/x:nil? :added "4.1"}
(fact "checks for nil values"

  (!.dt
    (xt/x:nil? nil))
  => true)

^{:refer xt.lang.spec-base/x:add :added "4.1"}
(fact "adds numbers"

  (!.dt
    (xt/x:add 1 2 3))
  => 6)

^{:refer xt.lang.spec-base/x:sub :added "4.1"}
(fact "subtracts numbers"

  (!.dt
    (xt/x:sub 10 3 2))
  => 5)

^{:refer xt.lang.spec-base/x:mul :added "4.1"}
(fact "multiplies numbers"

  (!.dt
    (xt/x:mul 2 3 4))
  => 24)

^{:refer xt.lang.spec-base/x:div :added "4.1"}
(fact "divides numbers"

  (!.dt
    (xt/x:div 20 5))
  => (approx 4))

^{:refer xt.lang.spec-base/x:neg :added "4.1"}
(fact "negates a number"

  (!.dt
    (xt/x:neg 2))
  => -2)

^{:refer xt.lang.spec-base/x:inc :added "4.1"}
(fact "increments a number"

  (!.dt
    (xt/x:inc 2))
  => 3)

^{:refer xt.lang.spec-base/x:dec :added "4.1"}
(fact "decrements a number"

  (!.dt
    (xt/x:dec 2))
  => 1)

^{:refer xt.lang.spec-base/x:zero? :added "4.1"}
(fact "checks whether a number is zero"

  (!.dt
    (xt/x:zero? 0))
  => true)

^{:refer xt.lang.spec-base/x:pos? :added "4.1"}
(fact "checks whether a number is positive"

  (!.dt
    (xt/x:pos? 2))
  => true)

^{:refer xt.lang.spec-base/x:neg? :added "4.1"}
(fact "checks whether a number is negative"

  (!.dt
    (xt/x:neg? -2))
  => true)

^{:refer xt.lang.spec-base/x:even? :added "4.1"}
(fact "checks whether a number is even"

  (!.dt
    (xt/x:even? 4))
  => true)

^{:refer xt.lang.spec-base/x:odd? :added "4.1"}
(fact "checks whether a number is odd"

  (!.dt
    (xt/x:odd? 5))
  => true)

^{:refer xt.lang.spec-base/x:eq :added "4.1"}
(fact "checks equality"

  (!.dt
    (xt/x:eq 2 2))
  => true)

^{:refer xt.lang.spec-base/x:neq :added "4.1"}
(fact "checks inequality"

  (!.dt
    (xt/x:neq 2 3))
  => true)

^{:refer xt.lang.spec-base/x:lt :added "4.1"}
(fact "checks less than"

  (!.dt
    (xt/x:lt 2 3))
  => true)

^{:refer xt.lang.spec-base/x:lte :added "4.1"}
(fact "checks less than or equal"

  (!.dt
    (xt/x:lte 3 3))
  => true)

^{:refer xt.lang.spec-base/x:gt :added "4.1"}
(fact "checks greater than"

  (!.dt
    (xt/x:gt 4 3))
  => true)

^{:refer xt.lang.spec-base/x:gte :added "4.1"}
(fact "checks greater than or equal"

  (!.dt
    (xt/x:gte 4 4))
  => true)

^{:refer xt.lang.spec-base/x:has-key? :added "4.1"}
(fact "checks whether an object has a key"

  (!.dt
    (var obj {:a 1})
    (xt/x:has-key? obj "a"))
  => true)

^{:refer xt.lang.spec-base/x:del-key :added "4.1"}
(fact "deletes keys from objects"

  (!.dt
    (var out {:a 1 :b 2})
    (xt/x:del-key out "a")
    out)
  => {"b" 2})

^{:refer xt.lang.spec-base/x:get-key :added "4.1"}
(fact "gets a value by key with a fallback"

  (!.dt
    (xt/x:get-key {} "missing" "fallback"))
  => "fallback")

^{:refer xt.lang.spec-base/x:get-path :added "4.1"}
(fact "gets a nested value by path"

  (!.dt
    (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
  => 2)

^{:refer xt.lang.spec-base/x:set-key :added "4.1"}
(fact "sets a key on an object"

  (!.dt
    (var out {:a 1})
    (xt/x:set-key out "b" 2)
    out)
  => {"a" 1, "b" 2})

^{:refer xt.lang.spec-base/x:copy-key :added "4.1"}
(fact "copies a key from another object"

  (!.dt
    (var out {:a 1})
    (xt/x:copy-key out {:a 9} ["c" "a"])
    out)
  => {"a" 1, "c" 9})

^{:refer xt.lang.spec-base/x:obj-keys :added "4.1"}
(fact "lists object keys"

  (!.dt
    (xt/x:obj-keys {:a 1 :b 2}))
  => (just ["a" "b"] :in-any-order))

^{:refer xt.lang.spec-base/x:obj-vals :added "4.1"}
(fact "lists object values"

  (!.dt
    (xt/x:obj-vals {:a 1 :b 2}))
  => (just [1 2] :in-any-order))

^{:refer xt.lang.spec-base/x:obj-pairs :added "4.1"}
(fact "lists object pairs"

  (!.dt
    (xt/x:obj-pairs {:a 1 :b 2}))
  => (just [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.spec-base/x:obj-clone :added "4.1"}
(fact "clones an object"

  (!.dt
    (var src {:a 1})
    (var out (xt/x:obj-clone src))
    (xt/x:set-key src "b" 2)
    out)
  => {"a" 1})

^{:refer xt.lang.spec-base/x:obj-assign :added "4.1"}
(fact "assigns object keys"

  (!.dt
    (xt/x:obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.spec-base/x:to-string :added "4.1"}
(fact "converts a value to a string"

  (!.dt
    (xt/x:to-string 12))
  => "12")

^{:refer xt.lang.spec-base/x:to-number :added "4.1"}
(fact "converts a string to a number"

  (!.dt
    (xt/x:to-number "12.5"))
  => 12.5)

^{:refer xt.lang.spec-base/x:is-string? :added "4.1"}
(fact "recognises strings"

  (!.dt
    (xt/x:is-string? "abc"))
  => true)

^{:refer xt.lang.spec-base/x:is-number? :added "4.1"}
(fact "recognises numbers"

  (!.dt
    (xt/x:is-number? 1.5))
  => true)

^{:refer xt.lang.spec-base/x:is-integer? :added "4.1"}
(fact "recognises integers"

  (!.dt
    (xt/x:is-integer? 2))
  => true)

^{:refer xt.lang.spec-base/x:is-boolean? :added "4.1"}
(fact "recognises booleans"

  (!.dt
    (xt/x:is-boolean? true))
  => true)

^{:refer xt.lang.spec-base/x:is-object? :added "4.1"}
(fact "recognises objects"

  (!.dt
    (xt/x:is-object? {:a 1}))
  => true)

^{:refer xt.lang.spec-base/x:is-array? :added "4.1"}
(fact "recognises arrays"

  (!.dt
    (xt/x:is-array? [1 2]))
  => true)

^{:refer xt.lang.spec-base/x:print :added "4.1"}
(fact "expands and emits a lua print form"

  (!.dt
    (xt/x:nil? (xt/x:print "hello")))
  => true)

^{:refer xt.lang.spec-base/x:str-len :added "4.1"}
(fact "gets the string length"

  (!.dt
    (xt/x:str-len "hello"))
  => 5)

^{:refer xt.lang.spec-base/x:str-comp :added "4.1"}
(fact "compares strings by sort order"

  (!.dt (xt/x:str-comp "abc" "abd"))
  => true)

^{:refer xt.lang.spec-base/x:str-lt :added "4.1"}
(fact "checks whether one string sorts before another"

  (!.dt (xt/x:str-lt "abc" "abd"))
  => true)

^{:refer xt.lang.spec-base/x:str-gt :added "4.1"}
(fact "checks whether one string sorts after another"

  (!.dt (xt/x:str-gt "abd" "abc"))
  => true)

^{:refer xt.lang.spec-base/x:str-pad-left :added "4.1"}
(fact "pads a string on the left"

  (!.dt
    (xt/x:str-pad-left "7" 3 "0"))
  => "007")

^{:refer xt.lang.spec-base/x:str-pad-right :added "4.1"}
(fact "pads a string on the right"

  (!.dt
    (xt/x:str-pad-right "7" 3 "0"))
  => "700")

^{:refer xt.lang.spec-base/x:str-starts-with :added "4.1"}
(fact "checks the string prefix"

  (!.dt
    (xt/x:str-starts-with "hello" "he"))
  => true)

^{:refer xt.lang.spec-base/x:str-ends-with :added "4.1"}
(fact "checks the string suffix"

  (!.dt
    (xt/x:str-ends-with "hello" "lo"))
  => true)

^{:refer xt.lang.spec-base/x:str-char :added "4.1"}
(fact "gets the character code at an index"

  (!.dt
    (xt/x:str-char "abc" (xt/x:offset 1)))
  => 98)

^{:refer xt.lang.spec-base/x:str-split :added "4.1"}
(fact "splits a string"

  (!.dt
    (xt/x:str-split "a/b/c" "/"))
  => ["a" "b" "c"])

^{:refer xt.lang.spec-base/x:str-join :added "4.1"}
(fact "joins string parts"

  (!.dt
    (xt/x:str-join "-" ["a" "b" "c"]))
  => "a-b-c")

^{:refer xt.lang.spec-base/x:str-index-of :added "4.1"}
(fact "finds the index of a substring"

  (!.dt
    (xt/x:str-index-of "hello/world" "/" (xt/x:offset 0)))
  => 5)

^{:refer xt.lang.spec-base/x:str-substring :added "4.1"}
(fact "gets a substring"

  (!.dt
    [(xt/x:str-substring "hello/world" (xt/x:offset 3))
     (xt/x:str-substring "hello/world" (xt/x:offset 3) 8)])
  => ["lo/world" "lo/wo"])

^{:refer xt.lang.spec-base/x:str-to-upper :added "4.1"}
(fact "converts a string to upper case"

  (!.dt
    (xt/x:str-to-upper "hello"))
  => "HELLO")

^{:refer xt.lang.spec-base/x:str-to-lower :added "4.1"}
(fact "converts a string to lower case"

  (!.dt
    (xt/x:str-to-lower "HELLO"))
  => "hello")

^{:refer xt.lang.spec-base/x:str-to-fixed :added "4.1"}
(fact "formats a number with fixed decimals"

  (!.dt
    (xt/x:str-to-fixed 1.2 2))
  => "1.20")

^{:refer xt.lang.spec-base/x:str-replace :added "4.1"}
(fact "replaces matching substrings"

  (!.dt (xt/x:str-replace "hello-world" "-" "/"))
  => "hello/world")

^{:refer xt.lang.spec-base/x:str-trim :added "4.1"}
(fact "trims whitespace from both sides"

  (!.dt (xt/x:str-trim "  hello  "))
  => "hello")

^{:refer xt.lang.spec-base/x:str-trim-left :added "4.1"}
(fact "trims whitespace from the left side"

  (!.dt (xt/x:str-trim-left "  hello"))
  => "hello")

^{:refer xt.lang.spec-base/x:str-trim-right :added "4.1"}
(fact "trims whitespace from the right side"

  (!.dt (xt/x:str-trim-right "hello  "))
  => "hello")

^{:refer xt.lang.spec-base/x:arr-sort :added "4.1"}
(fact "sorts arrays using key and compare functions"

  (!.dt
    (var out [{:id 3} {:id 1} {:id 2}])
    (xt/x:arr-sort out
                   (fn [e] (return (xt/x:get-key e "id")))
                   (fn [a b] (return (xt/x:lt a b))))
    out)
  => [{"id" 1} {"id" 2} {"id" 3}])

^{:refer xt.lang.spec-base/x:arr-clone :added "4.1"}
(fact "clones an array"

  (!.dt
    (var src [1 2])
    (var out (xt/x:arr-clone src))
    (xt/x:arr-push src 3)
    out)
  => [1 2])

^{:refer xt.lang.spec-base/x:arr-each :added "4.1"}
(fact "iterates each element in an array"

  (!.dt
    (var out [])
    (xt/x:arr-each [1 2 3]
                   (fn [e]
                     (xt/x:arr-push out (* e 2))))
    out)
  => [2 4 6])

^{:refer xt.lang.spec-base/x:arr-every :added "4.1"}
(fact "checks whether every array element matches a predicate"

  (!.dt
    (xt/x:arr-every [2 4 6]
                    (fn [e] (return (xt/x:even? e)))))
  => true)

^{:refer xt.lang.spec-base/x:arr-some :added "4.1"}
(fact "checks whether any array element matches a predicate"

  (!.dt
    (xt/x:arr-some [1 3 4]
                   (fn [e] (return (xt/x:even? e)))))
  => true)

^{:refer xt.lang.spec-base/x:arr-map :added "4.1"}
(fact "maps an array"

  (!.dt
    (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
  => [2 4 6])

^{:refer xt.lang.spec-base/x:arr-assign :added "4.1"}
(fact "appends one array to another"

  (!.dt
    (var out  [1 2])
    (xt/x:arr-assign out [3 4])
    out)
  => [1 2 3 4])

^{:refer xt.lang.spec-base/x:arr-concat :added "4.1"}
(fact "concatenates arrays into a new array"

  (!.dt
    (var src [1 2])
    [(xt/x:arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]])

^{:refer xt.lang.spec-base/x:arr-filter :added "4.1"}
(fact "filters an array"

  (!.dt
    (xt/x:arr-filter [2 3 4 5] (fn [e] (return (xt/x:even? e)))))
  => [2 4])

^{:refer xt.lang.spec-base/x:arr-foldl :added "4.1"}
(fact "folds arrays from the left"

  (!.dt
    (xt/x:arr-foldl [1 2 3 4 5]
                    (fn [out e] (return (+ out e)))
                    0))
  => 15)

^{:refer xt.lang.spec-base/x:arr-foldr :added "4.1"}
(fact "folds arrays from the right"

  (!.dt
    (xt/x:arr-foldr ["a" "b" "c" "d" "e"]
                    (fn [out e] (return (xt/x:cat out e)))
                    ""))
  => "edcba")

^{:refer xt.lang.spec-base/x:is-function? :added "4.1"}
(fact "recognises function values"

  (!.dt
    (xt/x:is-function? (fn [x] (return x))))
  => true)

^{:refer xt.lang.spec-base/x:callback :added "4.1"}
(fact "dispatches node-style callbacks through for:return"

  (!.dt
    (var out nil)
    (var success-fn (fn [cb]
                      (cb nil "OK")))
    (xt/for:return [[ret err] (success-fn (xt/x:callback))]
      {:success (:= out ret)
       :error   (:= out err)})
    out)
  => "OK"

  (!.dt
    (var out nil)
    (var failure-fn (fn [cb]
                      (cb "ERR" nil)))
    (xt/for:return [[ret err] (failure-fn (xt/x:callback))]
      {:success (:= out ret)
       :error   (:= out err)})
    out)
  => "ERR")

^{:refer xt.lang.spec-base/x:return-run :added "4.1"}
(fact "can be used directly inside for:return"

  (!.dt
    (var out nil)
    (xt/for:return [[ok err] (xt/x:return-run
                              (fn [resolve reject]
                                (reject "ERR")))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "ERR")

^{:refer xt.lang.spec-base/x:eval :added "4.1"}
(fact "evaluates javascript expressions"

  (!.dt
    (xt/x:eval "1 + 1"))
  => #"(?s).*eval not supported in Dart.*")

^{:refer xt.lang.spec-base/x:apply :added "4.1"}
(fact "applies array arguments to functions"

  (!.dt
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6)

^{:refer xt.lang.spec-base/x:iter-from-obj :added "4.1"}
(fact "creates iterators over object pairs"

  (!.dt
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-obj {:a 1 :b 2})]
      (xt/x:arr-push out e))
    out)
  => (contains [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.spec-base/x:iter-from-arr :added "4.1"}
(fact "creates iterators over arrays"

  (!.dt
    (var out [])
    (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])]
      (xt/x:arr-push out e))
    out)
  => [1 2 3])

^{:refer xt.lang.spec-base/x:iter-from :added "4.1"}
(fact "creates generic iterators from iterable values"

  (!.dt
    (var out [])
    (xt/for:iter [e (xt/x:iter-from [2 4 6])]
      (xt/x:arr-push out e))
    out)
  => [2 4 6])

^{:refer xt.lang.spec-base/x:iter-eq :added "4.1"}
(fact "checks iterator equality in js"

  (!.dt
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

^{:refer xt.lang.spec-base/x:iter-null :added "4.1"}
(fact "creates empty iterators"

  (!.dt
    (var it (xt/x:iter-null))
    (xt/x:iter-native? it))
  => true)

^{:refer xt.lang.spec-base/x:iter-next :added "4.1"}
(fact "advances iterators"

  (!.dt
    (var it (xt/x:iter-from-arr [1 2 3]))
    (xt/x:iter-native? it))
  => true)

^{:refer xt.lang.spec-base/x:iter-has? :added "4.1"}
(fact "checks whether values are iterable"

  (!.dt
    [(xt/x:iter-has? [1 2 3])
     (xt/x:iter-has? {:a 1})])
  => [true false])

^{:refer xt.lang.spec-base/x:iter-native? :added "4.1"}
(fact "checks whether values are iterator instances"

  (!.dt
    [(xt/x:iter-native? (xt/x:iter-from-arr [1 2 3]))
     (xt/x:iter-native? [1 2 3])])
  => [true false])

^{:refer xt.lang.spec-base/x:return-encode :added "4.1"}
(fact "encodes return payloads as json"

  (!.dt
     (var encode-fn
          (fn [value id key]
            (return
             (xt/x:return-encode value id key))))
     (xt/x:json-decode (encode-fn {:a 1} "id" "key")))
  => {"return" "object", "key" "key", "id" "id", "value" {"a" 1}, "type" "data"}

  (!.dt
    (var encode-fn
         (fn [value id key]
           (return
            (xt/x:return-encode value id key))))
    (xt/x:json-decode (encode-fn "hello" "id" "key")))
  => {"return" "string", "key" "key", "id" "id", "value" "hello", "type" "data"})

^{:refer xt.lang.spec-base/x:return-wrap :added "4.1"}
(fact "wraps return values through encoder functions"

  (!.dt
    (var encode-fn
         (fn [value id key]
           (return
            (xt/x:return-encode value id key))))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (return
            (xt/x:return-wrap gen-fn wrap-fn))))
    (xt/x:json-decode
     (wrap-fn (fn []
                (return 3))
              (fn [out]
                (return
                 (encode-fn out "id-A" "key-B"))))))
  => (contains {"id" "id-A"
                "key" "key-B"
                "type" "data"
                "value" 3}))

^{:refer xt.lang.spec-base/x:return-eval :added "4.1"}
(fact "evaluates code through wrapped return handlers"

  (!.dt
   (var encode-fn
        (fn [value id key]
          (return
           (xt/x:return-encode value id key))))
   (var wrap-fn
        (fn [gen-fn wrap-fn]
          (return
           (xt/x:return-wrap gen-fn wrap-fn))))
   (var eval-fn
        (fn [s re-wrap-fn]
          (return
           (xt/x:return-eval s re-wrap-fn))))
   (xt/x:json-decode
    (eval-fn "1 + 1"
             (fn [f]
               (return
                (wrap-fn f
                         (fn [out]
                           (return
                            (encode-fn out "id-A" "key-B")))))))))
  => #"(?s).*eval not supported in Dart.*")

^{:refer xt.lang.spec-base/x:bit-and :added "4.1"}
(fact "computes bitwise and"

  (!.dt (xt/x:bit-and 6 3))
  => 2)

^{:refer xt.lang.spec-base/x:bit-or :added "4.1"}
(fact "computes bitwise or"

  (!.dt (xt/x:bit-or 6 3))
  => 7)

^{:refer xt.lang.spec-base/x:bit-lshift :added "4.1"}
(fact "computes bitwise left shifts"

  (!.dt (xt/x:bit-lshift 3 2))
  => 12)

^{:refer xt.lang.spec-base/x:bit-rshift :added "4.1"}
(fact "computes bitwise right shifts"

  (!.dt (xt/x:bit-rshift 12 2))
  => 3)

^{:refer xt.lang.spec-base/x:bit-xor :added "4.1"}
(fact "computes bitwise xor"

  (!.dt (xt/x:bit-xor 6 3))
  => 5)

^{:refer xt.lang.spec-base/x:global-set :added "4.1"}
(fact "writes values to the shared global map"

  (!.dt
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

^{:refer xt.lang.spec-base/x:global-del :added "4.1"}
(fact "removes values from the shared global map"

  (!.dt
    (xt/x:global-set COMMON_SPEC_DELETE 1)
    (xt/x:global-del COMMON_SPEC_DELETE)
    (!:G COMMON_SPEC_DELETE))
  => nil)

^{:refer xt.lang.spec-base/x:global-has? :added "4.1"}
(fact "checks whether the shared global map contains a value"

  (!.dt
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

^{:refer xt.lang.spec-base/x:random :added "4.1"}
(fact "returns javascript random values"

  (!.dt
    (var out (xt/x:random))
    (and (>= out 0)
         (< out 1)))
  => true)

^{:refer xt.lang.spec-base/x:throw :added "4.1"}
(fact "expands to the canonical throw form"

  (!.dt
    (do:>
     (x:throw "ERROW")))
  => #"(?s)Unhandled exception:.*ERROW.*")

^{:refer xt.lang.spec-base/x:now-ms :added "4.1"}
(fact "expands and emits a millisecond time expression"

  (!.dt
    (> (xt/x:now-ms) 0))
  => true)

^{:refer xt.lang.spec-base/x:unpack :added "4.1"}
(fact "spreads arrays into positional arguments"

  (!.dt
    (var add-args
         (fn [a b c]
           (return (+ a b c))))
    (add-args (xt/x:unpack [1 2 3])))
  => 6)

^{:refer xt.lang.spec-base/x:json-encode :added "4.1"}
(fact "encodes lua data structures as json"

  (!.dt (xt/x:json-encode {:a 1}))
  => #"\{\"a\":\s*1\}")

^{:refer xt.lang.spec-base/x:json-decode :added "4.1"}
(fact "decodes json strings into lua data structures"

  (!.dt (xt/x:json-decode "{\"a\":1}"))
  => {"a" 1})

(comment

  (code.manage/isolate 'xt.lang.spec-base-test {:suffix "-fix"})
  (s/seedgen-langadd 'xt.lang.spec-base {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.spec-base {:lang [:lua :python] :write true})
  
  )
