(ns
 xtbench.python.lang.common-spec-test
 (:use code.test)
 (:require
  [clojure.set :as set]
  [std.lang :as l]
  [std.lang.model.spec-lua :as lua]
  [xt.lang.common-notify :as notify]))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-spec :as xt]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-spec/for:array, :added "4.1"}
(fact
 "iterates arrays in order"
 (!.py
  (var out [])
  (xt/for:array
   [e [1 2 3 4]]
   (when (> e 3) (break))
   (xt/x:arr-push out e))
  out)
 =>
 [1 2 3])

^{:refer xt.lang.common-spec/for:object, :added "4.1"}
(fact
 "iterates object key value pairs"
 (!.py
  (var out [])
  (xt/for:object [[k v] {:a 1, :b 2}] (xt/x:arr-push out [k v]))
  out)
 =>
 (contains [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.common-spec/for:index, :added "4.1"}
(fact
 "iterates a numeric range"
 (!.py
  (var out [])
  (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]] (xt/x:arr-push out i))
  out)
 =>
 [0 2])

^{:refer xt.lang.common-spec/for:iter, :added "4.1"}
(fact
 "expands to the canonical iterator form"
 (!.py
  (var out [])
  (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])] (xt/x:arr-push out e))
  out)
 =>
 [1 2 3])

^{:refer xt.lang.common-spec/return-run, :added "4.1"}
(fact
 "supports final returns through for:return"
 (!.py (xt/return-run [resolve reject] (resolve "OK")))
 =>
 (throws))

^{:refer xt.lang.common-spec/for:return, :added "4.1"}
(fact
 "dispatches success and error branches"
 (!.py
  (var out nil)
  (xt/for:return
   [[ok err] (xt/return-run [resolve reject] (resolve "OK"))]
   {:success (:= out ok), :error (:= out err)})
  out)
 =>
 "OK"
 (!.py
  (var out nil)
  (xt/for:return
   [[ok err] (xt/return-run [resolve reject] (reject "ERR"))]
   {:success (:= out ok), :error (:= out err)})
  out)
 =>
 "ERR")

^{:refer xt.lang.common-spec/for:try, :added "4.1"}
(fact
 "expands to the canonical try form"
 (!.py
  (var
   add
   (fn
    []
    (xt/for:try
     [[ok err] (do:> (xt/x:err "ERROR"))]
     {:success (return ok), :error (return "ERR")})))
  (add))
 =>
 "ERR")

^{:refer xt.lang.common-spec/for:async, :added "4.1"}
(fact
 "expands to the canonical async form"
 (notify/wait-on
  :python
  (for:async
   [[ok err] (xt/return-run [resolve reject] (resolve "OK"))]
   {:success (repl/notify ok),
    :error (repl/notify err),
    :finally (return true)}))
 =>
 "OK"
 (notify/wait-on
  :python
  (for:async
   [[ok err] (xt/return-run [resolve reject] (reject "ERR"))]
   {:success (repl/notify ok),
    :error (repl/notify err),
    :finally (return true)}))
 =>
 "ERR")

^{:refer xt.lang.common-spec/x:get-idx, :added "4.1"}
(fact
 "reads the first indexed value"
 (!.py (xt/x:get-idx ["a" "b" "c"] (xt/x:offset 0)))
 =>
 "a")

^{:refer xt.lang.common-spec/x:set-idx, :added "4.1"}
(fact
 "writes an indexed value"
 (!.py
  (var out ["a" "b" "c"])
  (xt/x:set-idx out (xt/x:offset 1) "B")
  out)
 =>
 ["a" "B" "c"])

^{:refer xt.lang.common-spec/x:first, :added "4.1"}
(fact
 "gets the first array element"
 (!.py (xt/x:first ["a" "b" "c"]))
 =>
 "a")

^{:refer xt.lang.common-spec/x:second, :added "4.1"}
(fact
 "gets the second array element"
 (!.py (xt/x:second ["a" "b" "c"]))
 =>
 "b")

^{:refer xt.lang.common-spec/x:last, :added "4.1"}
(fact
 "gets the last array element"
 (!.py (xt/x:last ["a" "b" "c" "d"]))
 =>
 "d")

^{:refer xt.lang.common-spec/x:second-last, :added "4.1"}
(fact
 "gets the element before the last"
 (!.py (xt/x:second-last ["a" "b" "c" "d"]))
 =>
 "c")

^{:refer xt.lang.common-spec/x:arr-remove, :added "4.1"}
(fact
 "removes an element from an array"
 (!.py
  (do
   (var out ["a" "b" "c" "d"])
   (xt/x:arr-remove out (xt/x:offset 1))
   out))
 =>
 ["a" "c" "d"])

^{:refer xt.lang.common-spec/x:arr-push, :added "4.1"}
(fact
 "pushes an element onto an array"
 (!.py (var out ["a" "b" "c"]) (xt/x:arr-push out "D") out)
 =>
 ["a" "b" "c" "D"])

^{:refer xt.lang.common-spec/x:arr-pop, :added "4.1"}
(fact
 "pops the last element from an array"
 (!.py (var out ["a" "b" "c" "d"]) [(xt/x:arr-pop out) out])
 =>
 ["d" ["a" "b" "c"]])

^{:refer xt.lang.common-spec/x:arr-push-first, :added "4.1"}
(fact
 "pushes an element to the front of an array"
 (!.py (var out ["a" "b" "c"]) (xt/x:arr-push-first out "D") out)
 =>
 ["D" "a" "b" "c"])

^{:refer xt.lang.common-spec/x:arr-pop-first, :added "4.1"}
(fact
 "pops the first element from an array"
 (!.py (var out ["a" "b" "c" "d"]) [(xt/x:arr-pop-first out) out])
 =>
 ["a" ["b" "c" "d"]])

^{:refer xt.lang.common-spec/x:arr-insert, :added "4.1"}
(fact
 "inserts an element into an array"
 (!.py
  (var out ["a" "b" "c"])
  (xt/x:arr-insert out (xt/x:offset 1) "D")
  out)
 =>
 ["a" "D" "b" "c"])

^{:refer xt.lang.common-spec/x:arr-slice, :added "4.1"}
(fact
 "slices a range from an array"
 (!.py
  (xt/x:arr-slice
   ["a" "b" "c" "d" "e"]
   (xt/x:offset 1)
   (xt/x:offset 3)))
 =>
 ["b" "c"])

^{:refer xt.lang.common-spec/x:arr-reverse, :added "4.1"}
(fact
 "reverses an array"
 (!.py (xt/x:arr-reverse ["a" "b" "c"]))
 =>
 ["c" "b" "a"])

^{:refer xt.lang.common-spec/x:del, :added "4.1"}
(fact
 "expands and emits a lua delete form"
 (!.py (var out {:a 1, :b 2}) (xt/x:del (. out ["a"])) out)
 =>
 {"b" 2})

^{:refer xt.lang.common-spec/x:cat, :added "4.1"}
(fact
 "concatenates strings"
 (!.py (xt/x:cat "hello" "-" "world"))
 =>
 "hello-world")

^{:refer xt.lang.common-spec/x:len, :added "4.1"}
(fact "gets the collection length" (!.py (xt/x:len ["a" "b" "c"])) => 3)

^{:refer xt.lang.common-spec/x:err, :added "4.1"}
(fact
 "expands and emits a lua error form"
 (!.py (var err-fn (fn [] (xt/x:err "ERR"))) (err-fn))
 =>
 (throws))

^{:refer xt.lang.common-spec/x:type-native, :added "4.1"}
(fact
 "expands and emits the lua type helper"
 (!.py
  (var type-fn (fn [obj] (xt/x:type-native obj)))
  [(type-fn {}) (type-fn [])])
 =>
 ["object" "array"])

^{:refer xt.lang.common-spec/x:offset, :added "4.1"}
(fact
 "uses the grammar base offset"
 ^#:seedgen{:check {:lua {:expect 11}}}
 (!.py (xt/x:offset 10))
 =>
 10)

^{:refer xt.lang.common-spec/x:offset-rev, :added "4.1"}
(fact
 "uses the reverse grammar offset"
 ^#:seedgen{:check {:lua {:expect 10}}}
 (!.py (xt/x:offset-rev 10))
 =>
 9)

^{:refer xt.lang.common-spec/x:offset-len, :added "4.1"}
(fact
 "uses the length grammar offset"
 ^#:seedgen{:check {:lua {:expect 9}}}
 (!.py (xt/x:offset-len 10))
 =>
 9)

^{:refer xt.lang.common-spec/x:offset-rlen, :added "4.1"}
(fact
 "uses the reverse length grammar offset"
 ^#:seedgen{:check {:lua {:expect 9}}}
 (!.py (xt/x:offset-rlen 10))
 =>
 10)

^{:refer xt.lang.common-spec/x:lu-create, :added "4.1"}
(fact
 "creates a lookup table wrapper"
 (!.py
  (var lu (xt/x:lu-create))
  (var lu-A1 {"A" "A"})
  (var lu-A2 {"A" "A"})
  (xt/x:lu-set lu lu-A1 "A1")
  (xt/x:lu-set lu lu-A2 "A2")
  [(xt/x:lu-get lu lu-A1) (xt/x:lu-get lu lu-A2)])
 =>
 ["A1" "A2"])

^{:refer xt.lang.common-spec/x:lu-eq, :added "4.1"}
(fact
 "compares lookup keys using lua identity"
 (!.py
  (var obj-a {:id 1})
  (var obj-b {:id 1})
  [(xt/x:lu-eq obj-a obj-a)
   (xt/x:lu-eq obj-a obj-b)
   (xt/x:lu-eq obj-b obj-b)])
 =>
 [true false true])

^{:refer xt.lang.common-spec/x:lu-get, :added "4.1"}
(fact
 "reads values from a lookup table"
 (!.py
  (var lu (xt/x:lu-create))
  (var lu-key {:id 1})
  (xt/x:lu-set lu lu-key "value")
  (xt/x:lu-get lu lu-key))
 =>
 "value")

^{:refer xt.lang.common-spec/x:lu-set, :added "4.1"}
(fact
 "writes values into a lookup table"
 (!.py
  (var lu (xt/x:lu-create))
  (var lu-key {:id 1})
  (xt/x:lu-set lu lu-key "value")
  (xt/x:lu-get lu lu-key))
 =>
 "value")

^{:refer xt.lang.common-spec/x:lu-del, :added "4.1"}
(fact
 "removes values from a lookup table"
 (!.py
  (var lu (xt/x:lu-create))
  (var lu-key {:id 1})
  (xt/x:lu-set lu lu-key "value")
  (xt/x:lu-del lu lu-key)
  (xt/x:lu-get lu lu-key))
 =>
 nil)

^{:refer xt.lang.common-spec/x:m-abs, :added "4.1"}
(fact "computes absolute values" (!.py (xt/x:m-abs -3)) => 3)

^{:refer xt.lang.common-spec/x:m-acos, :added "4.1"}
(fact "computes inverse cosine" (!.py (xt/x:m-acos 1)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-asin, :added "4.1"}
(fact "computes inverse sine" (!.py (xt/x:m-asin 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-atan, :added "4.1"}
(fact "computes inverse tangent" (!.py (xt/x:m-atan 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-ceil, :added "4.1"}
(fact "rounds numbers upward" (!.py (xt/x:m-ceil 1.2)) => 2)

^{:refer xt.lang.common-spec/x:m-cos, :added "4.1"}
(fact "computes cosine" (!.py (xt/x:m-cos 0)) => (approx 1))

^{:refer xt.lang.common-spec/x:m-cosh, :added "4.1"}
(fact "computes hyperbolic cosine" (!.py (xt/x:m-cosh 0)) => (approx 1))

^{:refer xt.lang.common-spec/x:m-exp, :added "4.1"}
(fact
 "computes the exponential function"
 (!.py (xt/x:m-exp 0))
 =>
 (approx 1))

^{:refer xt.lang.common-spec/x:m-floor, :added "4.1"}
(fact "rounds numbers downward" (!.py (xt/x:m-floor 1.8)) => 1)

^{:refer xt.lang.common-spec/x:m-loge, :added "4.1"}
(fact
 "computes the natural logarithm"
 (!.py (xt/x:m-loge 1))
 =>
 (approx 0))

^{:refer xt.lang.common-spec/x:m-log10, :added "4.1"}
(fact
 "computes the base-10 logarithm"
 (!.py (xt/x:m-log10 100))
 =>
 (approx 2))

^{:refer xt.lang.common-spec/x:m-max, :added "4.1"}
(fact "computes the maximum value" (!.py (xt/x:m-max 3 5)) => 5)

^{:refer xt.lang.common-spec/x:m-mod, :added "4.1"}
(fact "computes modulo values" (!.py (xt/x:m-mod 10 3)) => 1)

^{:refer xt.lang.common-spec/x:m-min, :added "4.1"}
(fact "computes the minimum value" (!.py (xt/x:m-min 3 5)) => 3)

^{:refer xt.lang.common-spec/x:m-pow, :added "4.1"}
(fact "raises numbers to a power" (!.py (xt/x:m-pow 2 4)) => 16)

^{:refer xt.lang.common-spec/x:m-quot, :added "4.1"}
(fact "computes integer quotients" (!.py (xt/x:m-quot 7 2)) => 3)

^{:refer xt.lang.common-spec/x:m-sin, :added "4.1"}
(fact "computes sine" (!.py (xt/x:m-sin 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-sinh, :added "4.1"}
(fact "computes hyperbolic sine" (!.py (xt/x:m-sinh 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-sqrt, :added "4.1"}
(fact "computes square roots" (!.py (xt/x:m-sqrt 9)) => (approx 3))

^{:refer xt.lang.common-spec/x:m-tan, :added "4.1"}
(fact "computes tangent" (!.py (xt/x:m-tan 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-tanh, :added "4.1"}
(fact
 "computes hyperbolic tangent"
 (!.py (xt/x:m-tanh 0))
 =>
 (approx 0))

^{:refer xt.lang.common-spec/x:not-nil?, :added "4.1"}
(fact "checks for non-nil values" (!.py (xt/x:not-nil? 0)) => true)

^{:refer xt.lang.common-spec/x:nil?, :added "4.1"}
(fact "checks for nil values" (!.py (xt/x:nil? nil)) => true)

^{:refer xt.lang.common-spec/x:add, :added "4.1"}
(fact "adds numbers" (!.py (xt/x:add 1 2 3)) => 6)

^{:refer xt.lang.common-spec/x:sub, :added "4.1"}
(fact "subtracts numbers" (!.py (xt/x:sub 10 3 2)) => 5)

^{:refer xt.lang.common-spec/x:mul, :added "4.1"}
(fact "multiplies numbers" (!.py (xt/x:mul 2 3 4)) => 24)

^{:refer xt.lang.common-spec/x:div, :added "4.1"}
(fact "divides numbers" (!.py (xt/x:div 20 5)) => (approx 4))

^{:refer xt.lang.common-spec/x:neg, :added "4.1"}
(fact "negates a number" (!.py (xt/x:neg 2)) => -2)

^{:refer xt.lang.common-spec/x:inc, :added "4.1"}
(fact "increments a number" (!.py (xt/x:inc 2)) => 3)

^{:refer xt.lang.common-spec/x:dec, :added "4.1"}
(fact "decrements a number" (!.py (xt/x:dec 2)) => 1)

^{:refer xt.lang.common-spec/x:zero?, :added "4.1"}
(fact "checks whether a number is zero" (!.py (xt/x:zero? 0)) => true)

^{:refer xt.lang.common-spec/x:pos?, :added "4.1"}
(fact
 "checks whether a number is positive"
 (!.py (xt/x:pos? 2))
 =>
 true)

^{:refer xt.lang.common-spec/x:neg?, :added "4.1"}
(fact
 "checks whether a number is negative"
 (!.py (xt/x:neg? -2))
 =>
 true)

^{:refer xt.lang.common-spec/x:even?, :added "4.1"}
(fact "checks whether a number is even" (!.py (xt/x:even? 4)) => true)

^{:refer xt.lang.common-spec/x:odd?, :added "4.1"}
(fact "checks whether a number is odd" (!.py (xt/x:odd? 5)) => true)

^{:refer xt.lang.common-spec/x:eq, :added "4.1"}
(fact "checks equality" (!.py (xt/x:eq 2 2)) => true)

^{:refer xt.lang.common-spec/x:neq, :added "4.1"}
(fact "checks inequality" (!.py (xt/x:neq 2 3)) => true)

^{:refer xt.lang.common-spec/x:lt, :added "4.1"}
(fact "checks less than" (!.py (xt/x:lt 2 3)) => true)

^{:refer xt.lang.common-spec/x:lte, :added "4.1"}
(fact "checks less than or equal" (!.py (xt/x:lte 3 3)) => true)

^{:refer xt.lang.common-spec/x:gt, :added "4.1"}
(fact "checks greater than" (!.py (xt/x:gt 4 3)) => true)

^{:refer xt.lang.common-spec/x:gte, :added "4.1"}
(fact "checks greater than or equal" (!.py (xt/x:gte 4 4)) => true)

^{:refer xt.lang.common-spec/x:has-key?, :added "4.1"}
(fact
 "checks whether an object has a key"
 (!.py (var obj {:a 1}) (xt/x:has-key? obj "a"))
 =>
 true)

^{:refer xt.lang.common-spec/x:del-key, :added "4.1"}
(fact
 "deletes keys from objects"
 (!.py (var out {:a 1, :b 2}) (xt/x:del-key out "a") out)
 =>
 {"b" 2})

^{:refer xt.lang.common-spec/x:get-key, :added "4.1"}
(fact
 "gets a value by key with a fallback"
 (!.py (xt/x:get-key {} "missing" "fallback"))
 =>
 "fallback")

^{:refer xt.lang.common-spec/x:get-path, :added "4.1"}
(fact
 "gets a nested value by path"
 (!.py (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
 =>
 2)

^{:refer xt.lang.common-spec/x:set-key, :added "4.1"}
(fact
 "sets a key on an object"
 (!.py (var out {:a 1}) (xt/x:set-key out "b" 2) out)
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:copy-key, :added "4.1"}
(fact
 "copies a key from another object"
 (!.py (var out {:a 1}) (xt/x:copy-key out {:a 9} ["c" "a"]) out)
 =>
 {"a" 1, "c" 9})

^{:refer xt.lang.common-spec/x:obj-keys, :added "4.1"}
(fact
 "lists object keys"
 (set (!.py (xt/x:obj-keys {:a 1, :b 2})))
 =>
 #{"a" "b"})

^{:refer xt.lang.common-spec/x:obj-vals, :added "4.1"}
(fact
 "lists object values"
 (set (!.py (xt/x:obj-vals {:a 1, :b 2})))
 =>
 #{1 2})

^{:refer xt.lang.common-spec/x:obj-pairs, :added "4.1"}
(fact
 "lists object pairs"
 (set (!.py (xt/x:obj-pairs {:a 1, :b 2})))
 =>
 #{["b" 2] ["a" 1]})

^{:refer xt.lang.common-spec/x:obj-clone, :added "4.1"}
(fact
 "clones an object"
 (!.py
  (var src {:a 1})
  (var out (xt/x:obj-clone src))
  (xt/x:set-key src "b" 2)
  out)
 =>
 {"a" 1})

^{:refer xt.lang.common-spec/x:obj-assign, :added "4.1"}
(fact
 "assigns object keys"
 (!.py (xt/x:obj-assign {:a 1} {:b 2}))
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:to-string, :added "4.1"}
(fact "converts a value to a string" (!.py (xt/x:to-string 12)) => "12")

^{:refer xt.lang.common-spec/x:to-number, :added "4.1"}
(fact
 "converts a string to a number"
 (!.py (xt/x:to-number "12.5"))
 =>
 12.5)

^{:refer xt.lang.common-spec/x:is-string?, :added "4.1"}
(fact "recognises strings" (!.py (xt/x:is-string? "abc")) => true)

^{:refer xt.lang.common-spec/x:is-number?, :added "4.1"}
(fact "recognises numbers" (!.py (xt/x:is-number? 1.5)) => true)

^{:refer xt.lang.common-spec/x:is-integer?, :added "4.1"}
(fact "recognises integers" (!.py (xt/x:is-integer? 2)) => true)

^{:refer xt.lang.common-spec/x:is-boolean?, :added "4.1"}
(fact "recognises booleans" (!.py (xt/x:is-boolean? true)) => true)

^{:refer xt.lang.common-spec/x:is-object?, :added "4.1"}
(fact "recognises objects" (!.py (xt/x:is-object? {:a 1})) => true)

^{:refer xt.lang.common-spec/x:is-array?, :added "4.1"}
(fact "recognises arrays" (!.py (xt/x:is-array? [1 2])) => true)

^{:refer xt.lang.common-spec/x:print, :added "4.1"}
(fact
 "expands and emits a lua print form"
 (!.py (xt/x:nil? (xt/x:print "hello")))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-len, :added "4.1"}
(fact "gets the string length" (!.py (xt/x:str-len "hello")) => 5)

^{:refer xt.lang.common-spec/x:str-comp, :added "4.1"}
(fact
 "compares strings by sort order"
 (!.py (xt/x:str-comp "abc" "abd"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-lt, :added "4.1"}
(fact
 "checks whether one string sorts before another"
 (!.py (xt/x:str-lt "abc" "abd"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-gt, :added "4.1"}
(fact
 "checks whether one string sorts after another"
 (!.py (xt/x:str-gt "abd" "abc"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-pad-left, :added "4.1"}
(fact
 "pads a string on the left"
 (!.py (xt/x:str-pad-left "7" 3 "0"))
 =>
 "007")

^{:refer xt.lang.common-spec/x:str-pad-right, :added "4.1"}
(fact
 "pads a string on the right"
 (!.py (xt/x:str-pad-right "7" 3 "0"))
 =>
 "700")

^{:refer xt.lang.common-spec/x:str-starts-with, :added "4.1"}
(fact
 "checks the string prefix"
 (!.py (xt/x:str-starts-with "hello" "he"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-ends-with, :added "4.1"}
(fact
 "checks the string suffix"
 (!.py (xt/x:str-ends-with "hello" "lo"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-char, :added "4.1"}
(fact
 "gets the character code at an index"
 (!.py (xt/x:str-char "abc" (xt/x:offset 1)))
 =>
 98)

^{:refer xt.lang.common-spec/x:str-split, :added "4.1"}
(fact
 "splits a string"
 (!.py (xt/x:str-split "a/b/c" "/"))
 =>
 ["a" "b" "c"])

^{:refer xt.lang.common-spec/x:str-join, :added "4.1"}
(fact
 "joins string parts"
 (!.py (xt/x:str-join "-" ["a" "b" "c"]))
 =>
 "a-b-c")

^{:refer xt.lang.common-spec/x:str-index-of, :added "4.1"}
(fact
 "finds the index of a substring"
 ^#:seedgen{:check {:lua {:expect 6}}}
 (!.py (xt/x:str-index-of "hello/world" "/" (xt/x:offset 0)))
 =>
 5)

^{:refer xt.lang.common-spec/x:str-substring, :added "4.1"}
(fact
 "gets a substring"
 (!.py
  (xt/x:str-substring "hello/world" (xt/x:offset 3) (xt/x:offset 8)))
 =>
 "lo/wo")

^{:refer xt.lang.common-spec/x:str-to-upper, :added "4.1"}
(fact
 "converts a string to upper case"
 (!.py (xt/x:str-to-upper "hello"))
 =>
 "HELLO")

^{:refer xt.lang.common-spec/x:str-to-lower, :added "4.1"}
(fact
 "converts a string to lower case"
 (!.py (xt/x:str-to-lower "HELLO"))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:str-to-fixed, :added "4.1"}
(fact
 "formats a number with fixed decimals"
 (!.py (xt/x:str-to-fixed 1.2 2))
 =>
 "1.20")

^{:refer xt.lang.common-spec/x:str-replace, :added "4.1"}
(fact
 "replaces matching substrings"
 (!.py (xt/x:str-replace "hello-world" "-" "/"))
 =>
 "hello/world")

^{:refer xt.lang.common-spec/x:str-trim, :added "4.1"}
(fact
 "trims whitespace from both sides"
 (!.py (xt/x:str-trim "  hello  "))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:str-trim-left, :added "4.1"}
(fact
 "trims whitespace from the left side"
 (!.py (xt/x:str-trim-left "  hello"))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:str-trim-right, :added "4.1"}
(fact
 "trims whitespace from the right side"
 (!.py (xt/x:str-trim-right "hello  "))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:arr-sort, :added "4.1"}
(fact
 "sorts arrays using key and compare functions"
 (!.py
  (var out [{:id 3} {:id 1} {:id 2}])
  (xt/x:arr-sort
   out
   (fn [e] (return (xt/x:get-key e "id")))
   (fn [a b] (return (xt/x:lt a b))))
  out)
 =>
 [{"id" 1} {"id" 2} {"id" 3}])

^{:refer xt.lang.common-spec/x:arr-clone, :added "4.1"}
(fact
 "clones an array"
 (!.py
  (var src [1 2])
  (var out (xt/x:arr-clone src))
  (xt/x:arr-push src 3)
  out)
 =>
 [1 2])

^{:refer xt.lang.common-spec/x:arr-each, :added "4.1"}
(fact
 "iterates each element in an array"
 (!.py
  (var out [])
  (xt/x:arr-each [1 2 3] (fn [e] (xt/x:arr-push out (* e 2))))
  out)
 =>
 [2 4 6])

^{:refer xt.lang.common-spec/x:arr-every, :added "4.1"}
(fact
 "checks whether every array element matches a predicate"
 (!.py (xt/x:arr-every [2 4 6] (fn [e] (return (xt/x:even? e)))))
 =>
 true)

^{:refer xt.lang.common-spec/x:arr-some, :added "4.1"}
(fact
 "checks whether any array element matches a predicate"
 (!.py (xt/x:arr-some [1 3 4] (fn [e] (return (xt/x:even? e)))))
 =>
 true)

^{:refer xt.lang.common-spec/x:arr-map, :added "4.1"}
(fact
 "maps an array"
 (!.py (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
 =>
 [2 4 6])

^{:refer xt.lang.common-spec/x:arr-assign, :added "4.1"}
(fact
 "appends one array to another"
 (!.py (var out [1 2]) (xt/x:arr-assign out [3 4]) out)
 =>
 [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-concat, :added "4.1"}
(fact
 "concatenates arrays into a new array"
 (!.py (var src [1 2]) [(xt/x:arr-concat src [3 4]) src])
 =>
 [[1 2 3 4] [1 2]])

^{:refer xt.lang.common-spec/x:arr-filter, :added "4.1"}
(fact
 "filters an array"
 (!.py (xt/x:arr-filter [2 3 4 5] (fn [e] (return (xt/x:even? e)))))
 =>
 [2 4])

^{:refer xt.lang.common-spec/x:arr-foldl, :added "4.1"}
(fact
 "folds arrays from the left"
 (!.py (xt/x:arr-foldl [1 2 3 4 5] (fn [out e] (return (+ out e))) 0))
 =>
 15)

^{:refer xt.lang.common-spec/x:arr-foldr, :added "4.1"}
(fact
 "folds arrays from the right"
 (!.py
  (xt/x:arr-foldr
   ["a" "b" "c" "d" "e"]
   (fn [out e] (return (xt/x:cat out e)))
   ""))
 =>
 "edcba")

^{:refer xt.lang.common-spec/x:is-function?, :added "4.1"}
(fact
 "recognises function values"
 (!.py (xt/x:is-function? (fn [x] (return x))))
 =>
 true)

^{:refer xt.lang.common-spec/x:callback, :added "4.1"}
(fact
 "dispatches node-style callbacks through for:return"
 [(!.py
   (var out nil)
   (var success (fn [cb] (cb nil "OK")))
   (xt/for:return
    [[ret err] (success (xt/x:callback))]
    {:success (:= out ret), :error (:= out err)})
   out)
  (!.py
   (var out nil)
   (var failure (fn [cb] (cb "ERR" nil)))
   (xt/for:return
    [[ret err] (failure (xt/x:callback))]
    {:success (:= out ret), :error (:= out err)})
   out)]
 =>
 ["OK" "ERR"])

^{:refer xt.lang.common-spec/x:return-run, :added "4.1"}
(fact
 "can be used directly inside for:return"
 (!.py
  (var out nil)
  (xt/for:return
   [[ok err] (xt/x:return-run (fn [resolve reject] (reject "ERR")))]
   {:success (:= out ok), :error (:= out err)})
  out)
 =>
 "ERR")

^{:refer xt.lang.common-spec/x:eval, :added "4.1"}
(fact
 "evaluates javascript expressions"
 (!.py (xt/x:eval "1 + 1"))
 =>
 2)

^{:refer xt.lang.common-spec/x:apply, :added "4.1"}
(fact
 "applies array arguments to functions"
 (!.py (xt/x:apply (fn [a b c] (return (+ a b c))) [1 2 3]))
 =>
 6)

^{:refer xt.lang.common-spec/x:iter-from-obj, :added "4.1"}
(fact
 "creates iterators over object pairs"
 (!.py
  (var out [])
  (xt/for:iter
   [e (xt/x:iter-from-obj {:a 1, :b 2})]
   (xt/x:arr-push out e))
  out)
 =>
 (contains [["a" 1] ["b" 2]] :in-any-order))

^{:refer xt.lang.common-spec/x:iter-from-arr, :added "4.1"}
(fact
 "creates iterators over arrays"
 (!.py
  (var out [])
  (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])] (xt/x:arr-push out e))
  out)
 =>
 [1 2 3])

^{:refer xt.lang.common-spec/x:iter-from, :added "4.1"}
(fact
 "creates generic iterators from iterable values"
 (!.py
  (var out [])
  (xt/for:iter [e (xt/x:iter-from [2 4 6])] (xt/x:arr-push out e))
  out)
 =>
 [2 4 6])

^{:refer xt.lang.common-spec/x:iter-eq, :added "4.1"}
(fact
 "checks iterator equality in js"
 (!.py
  (var eq-fn (fn [it0 it1 eq-fn] (xt/x:iter-eq it0 it1 eq-fn)))
  [(eq-fn
    (xt/x:iter-from-arr [1 2 3])
    (xt/x:iter-from-arr [1 2 3])
    (fn [a b] (return (== a b))))
   (eq-fn
    (xt/x:iter-from-arr [1 2 3])
    (xt/x:iter-from-arr [1 2 4])
    (fn [a b] (return (== a b))))])
 =>
 [true false])

^{:refer xt.lang.common-spec/x:iter-null, :added "4.1"}
(fact
 "creates empty iterators"
 (!.py (xt/x:iter-native? (xt/x:iter-null)))
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-next, :added "4.1"}
(fact
 "advances iterators"
 (!.py (xt/x:iter-next (xt/x:iter-from-arr [1 2 3])))
 =>
 nil)

^{:refer xt.lang.common-spec/x:iter-has?, :added "4.1"}
(fact
 "checks whether values are iterable"
 (!.py [(xt/x:iter-has? [1 2 3]) (xt/x:iter-has? {:a 1})])
 =>
 [true false])

^{:refer xt.lang.common-spec/x:iter-native?, :added "4.1"}
(fact
 "checks whether values are iterator instances"
 (!.py
  [(xt/x:iter-native? (xt/x:iter-from-arr [1 2 3]))
   (xt/x:iter-native? [1 2 3])])
 =>
 [true false])

^{:refer xt.lang.common-spec/x:return-encode, :added "4.1"}
(fact
 "encodes return payloads as json"
 (!.py
  (var encode-fn (fn [value id key] (xt/x:return-encode value id key)))
  (xt/x:json-decode (encode-fn {:a 1} "id" "key")))
 =>
 {"id" "id", "key" "key", "type" "data", "value" {"a" 1}})

^{:refer xt.lang.common-spec/x:return-wrap, :added "4.1"}
(fact
 "wraps return values through encoder functions"
 (!.py
  (var encode-fn (fn [value id key] (xt/x:return-encode value id key)))
  (var wrap-fn (fn [gen-fn wrap-fn] (xt/x:return-wrap gen-fn wrap-fn)))
  (xt/x:json-decode
   (wrap-fn
    (fn [] (return 3))
    (fn [out] (return (encode-fn out "id-A" "key-B"))))))
 =>
 {"id" "id-A",
  "key" "key-B",
  "type" "data",
  "return" "number",
  "value" 3})

^{:refer xt.lang.common-spec/x:return-eval, :added "4.1"}
(fact
 "evaluates code through wrapped return handlers"
 (!.py
  (var encode-fn (fn [value id key] (xt/x:return-encode value id key)))
  (var wrap-fn (fn [gen-fn wrap-fn] (xt/x:return-wrap gen-fn wrap-fn)))
  (var eval-fn (fn [s re-wrap-fn] (xt/x:return-eval s re-wrap-fn)))
  (xt/x:json-decode
   (eval-fn
    "1 + 1"
    (fn
     [f]
     (return
      (wrap-fn
       f
       (fn [out] (return (encode-fn out "id-A" "key-B")))))))))
 =>
 {"return" "number",
  "key" "key-B",
  "id" "id-A",
  "value" 2,
  "type" "data"})

^{:refer xt.lang.common-spec/x:bit-and, :added "4.1"}
(fact "computes bitwise and" (!.py (xt/x:bit-and 6 3)) => 2)

^{:refer xt.lang.common-spec/x:bit-or, :added "4.1"}
(fact "computes bitwise or" (!.py (xt/x:bit-or 6 3)) => 7)

^{:refer xt.lang.common-spec/x:bit-lshift, :added "4.1"}
(fact "computes bitwise left shifts" (!.py (xt/x:bit-lshift 3 2)) => 12)

^{:refer xt.lang.common-spec/x:bit-rshift, :added "4.1"}
(fact
 "computes bitwise right shifts"
 (!.py (xt/x:bit-rshift 12 2))
 =>
 3)

^{:refer xt.lang.common-spec/x:bit-xor, :added "4.1"}
(fact "computes bitwise xor" (!.py (xt/x:bit-xor 6 3)) => 5)

^{:refer xt.lang.common-spec/x:global-set, :added "4.1"}
(fact
 "writes values to the shared global map"
 (!.py
  (xt/x:global-set COMMON_SPEC_GLOBAL 1)
  [(xt/x:global-has? COMMON_SPEC_GLOBAL)
   (!:G COMMON_SPEC_GLOBAL)
   (do
    (xt/x:global-del COMMON_SPEC_GLOBAL)
    (xt/x:global-has? COMMON_SPEC_GLOBAL))])
 =>
 [true 1 false])

^{:refer xt.lang.common-spec/x:global-del, :added "4.1"}
(fact
 "removes values from the shared global map"
 (!.py
  (xt/x:global-set COMMON_SPEC_DELETE 1)
  (xt/x:global-del COMMON_SPEC_DELETE)
  (!:G COMMON_SPEC_DELETE))
 =>
 nil)

^{:refer xt.lang.common-spec/x:global-has?, :added "4.1"}
(fact
 "checks whether the shared global map contains a value"
 (!.py
  (xt/x:global-set COMMON_SPEC_HAS 1)
  [(xt/x:global-has? COMMON_SPEC_HAS)
   (do
    (xt/x:global-del COMMON_SPEC_HAS)
    (xt/x:global-has? COMMON_SPEC_HAS))])
 =>
 [true false])

^{:refer xt.lang.common-spec/x:proto-get, :added "4.1"}
(fact
 "retrieves object prototypes"
 (!.py
  (var proto {:label "proto"})
  (var obj {})
  (xt/x:proto-set obj proto nil)
  (. (xt/x:proto-get obj "label") ["label"]))
 =>
 "proto")

^{:refer xt.lang.common-spec/x:proto-set, :added "4.1"}
(fact
 "assigns object prototypes"
 (!.py
  (var proto {:label "proto"})
  (var obj {})
  (xt/x:proto-set obj proto nil)
  (. obj ["label"]))
 =>
 "proto")

^{:refer xt.lang.common-spec/x:proto-create, :added "4.1"}
(fact
 "creates prototypes with this-bound methods"
 (!.py
  (var
   proto
   (xt/x:proto-create
    {:describe
     (fn [self suffix] (return (+ (. self ["name"]) suffix)))}))
  (var obj {})
  (xt/x:proto-set obj proto nil)
  (:= (. obj ["name"]) "alpha")
  (. obj (describe "!")))
 =>
 "alpha!")

^{:refer xt.lang.common-spec/x:random, :added "4.1"}
(fact
 "returns javascript random values"
 (!.py (var out (xt/x:random)) (and (>= out 0) (< out 1)))
 =>
 true)

^{:refer xt.lang.common-spec/x:now-ms, :added "4.1"}
(fact
 "expands and emits a millisecond time expression"
 (!.py (> (xt/x:now-ms) 0))
 =>
 true)

^{:refer xt.lang.common-spec/x:unpack, :added "4.1"}
(fact
 "spreads arrays into positional arguments"
 (!.py ((fn [a b c] (return (+ a b c))) (xt/x:unpack [1 2 3])))
 =>
 6)

^{:refer xt.lang.common-spec/x:client-basic, :added "4.1"}
(fact
 "streams newline-delimited socket input through eval handlers"
 (notify/wait-on
  :python
  (do
   (var net (require "net"))
   (var port 18181)
   (var
    server
    (.
     net
     (createServer
      (fn [conn] (. conn (write "PING\n")) (. conn (end)))))
    (. server (listen port "127.0.0.1"))
    (xt/x:client-basic
     "127.0.0.1"
     port
     (fn
      [host port opts]
      (return [true (. net (createConnection port host))]))
     (fn [conn line] (. server (close)) (repl/notify line))))))
 =>
 "PING")

^{:refer xt.lang.common-spec/x:client-ws, :added "4.1"}
(fact
 "routes websocket messages through eval handlers"
 (notify/wait-on
  :python
  (do
   (var conn {})
   (xt/x:client-ws
    "localhost"
    8080
    {}
    (fn [host port opts] (return [true conn]))
    (fn [ws value] (repl/notify [(== ws conn) value])))
   ((. conn ["onmessage"]) {:data "PING"})))
 =>
 [true "PING"])

^{:refer xt.lang.common-spec/x:socket-connect, :added "4.1"}
(fact
 "connects sockets and forwards the connection to callbacks"
 (notify/wait-on
  :python
  (do
   (var net (require "net"))
   (var port 18182)
   (var
    server
    (. net (createServer (fn [conn] (. conn (end)))))
    (. server (listen port "127.0.0.1"))
    (xt/x:socket-connect
     "127.0.0.1"
     port
     {}
     (fn
      [err conn]
      (. server (close))
      (repl/notify
       [(xt/x:nil? err) (xt/x:is-function? (. conn ["write"]))]))))))
 =>
 [true true])

^{:refer xt.lang.common-spec/x:socket-send, :added "4.1"}
(fact
 "sends socket messages through write"
 (!.py
  (var out nil)
  (var conn {:write (fn [s] (:= out s))})
  (xt/x:socket-send conn "PING")
  out)
 =>
 "PING")

^{:refer xt.lang.common-spec/x:socket-close, :added "4.1"}
(fact
 "closes sockets through end"
 (!.py
  (var out nil)
  (var conn {:end (fn [] (:= out "closed"))})
  (xt/x:socket-close conn)
  out)
 =>
 "closed")

^{:refer xt.lang.common-spec/x:ws-connect, :added "4.1"}
(fact
 "creates websocket connections from host, port, and options"
 (!.py
  (:= (!:G WebSocket) (fn [url] (:= (. this ["url"]) url)))
  (var
   [ok conn]
   (xt/x:ws-connect "localhost" 8080 {:schema "wss", :url "/socket"}))
  [ok (. conn ["url"])])
 =>
 [true "wss://localhost:8080/socket"])

^{:refer xt.lang.common-spec/x:ws-send, :added "4.1"}
(fact
 "sends websocket frames"
 (!.py
  (var out nil)
  (var conn {:send (fn [s] (:= out s))})
  (xt/x:ws-send conn "PING")
  out)
 =>
 "PING")

^{:refer xt.lang.common-spec/x:ws-close, :added "4.1"}
(fact
 "closes websocket connections"
 (!.py
  (var out nil)
  (var conn {:close (fn [] (:= out "closed"))})
  (xt/x:ws-close conn)
  out)
 =>
 "closed")

^{:refer xt.lang.common-spec/x:notify-http, :added "4.1"}
(fact
 "posts encoded values through fetch"
 (!.py
  (var out nil)
  (:=
   (!:G fetch)
   (fn
    [url opts]
    (:=
     out
     [url (xt/x:json-decode (. opts ["body"])) (. opts ["method"])])))
  (xt/x:notify-http
   "localhost"
   8080
   {:a 1}
   "id"
   "key"
   (fn [value id key] (xt/x:return-encode value id key)))
  out)
 =>
 ["http://localhost:8080"
  {"id" "id", "key" "key", "type" "data", "value" {"a" 1}}
  "POST"])

^{:refer xt.lang.common-spec/x:b64-encode, :added "4.1"}
(fact
 "encodes base64 strings"
 (!.py (xt/x:b64-encode "hello"))
 =>
 "aGVsbG8=")

^{:refer xt.lang.common-spec/x:b64-decode, :added "4.1"}
(fact
 "decodes base64 strings"
 (!.py (xt/x:b64-decode "aGVsbG8="))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:cache, :added "4.1"}
(fact
 "selects the global cache store"
 (!.py
  (:=
   (!:G window)
   {:localStorage {:name "local"}, :sessionStorage {:name "session"}})
  (. (xt/x:cache "GLOBAL") ["name"]))
 =>
 "local")

^{:refer xt.lang.common-spec/x:cache-list, :added "4.1"}
(fact
 "lists cache keys"
 (!.py
  (:=
   (!:G window)
   {:localStorage {"_keys" ["a" "b"]}, :sessionStorage {"_keys" []}})
  (xt/x:cache-list))
 =>
 ["a" "b"])

^{:refer xt.lang.common-spec/x:cache-flush, :added "4.1"}
(fact
 "flushes cache stores"
 (!.py
  (var out nil)
  (var cache {:clear (fn [] (:= out "flushed"))})
  (xt/x:cache-flush cache)
  out)
 =>
 "flushed")

^{:refer xt.lang.common-spec/x:cache-get, :added "4.1"}
(fact
 "reads cache values"
 (!.py
  (var cache {:getItem (fn [k] (return (+ "value:" k)))})
  (xt/x:cache-get cache "key"))
 =>
 "value:key")

^{:refer xt.lang.common-spec/x:cache-set, :added "4.1"}
(fact
 "writes cache values"
 (!.py
  (var out nil)
  (var cache {:setItem (fn [k v] (:= out [k v]) (return v))})
  [(xt/x:cache-set cache "key" "value") out])
 =>
 ["value" ["key" "value"]])

^{:refer xt.lang.common-spec/x:cache-del, :added "4.1"}
(fact
 "deletes cache values"
 (!.py
  (var out nil)
  (var cache {:removeItem (fn [k] (:= out k))})
  (xt/x:cache-del cache "key")
  out)
 =>
 "key")

^{:refer xt.lang.common-spec/x:cache-incr, :added "4.1"}
(fact
 "increments cached numeric values"
 (!.py
  (var state {"count" "2"})
  (var
   cache
   {:getItem (fn [k] (return (. state [k]))),
    :setItem (fn [k v] (:= (. state [k]) v) (return v))})
  (xt/x:cache-incr cache "count" 3))
 =>
 5)

^{:refer xt.lang.common-spec/x:json-encode, :added "4.1"}
(fact
 "encodes lua data structures as json"
 (!.py (xt/x:json-encode {:a 1}))
 =>
 #"\{\"a\":\s*1\}")

^{:refer xt.lang.common-spec/x:json-decode, :added "4.1"}
(fact
 "decodes json strings into lua data structures"
 (!.py (xt/x:json-decode "{\"a\":1}"))
 =>
 {"a" 1})

^{:refer xt.lang.common-spec/x:shell, :added "4.1"}
(fact
 "executes shell commands asynchronously"
 (notify/wait-on
  :python
  (xt/x:shell
   "printf hello"
   {:success (fn [res] (repl/notify res)),
    :error (fn [err] (repl/notify "ERR"))}))
 =>
 #"hello")

^{:refer xt.lang.common-spec/x:thread-spawn, :added "4.1"}
(fact
 "spawns js promise-backed threads"
 (notify/wait-on
  :python
  (->
   (xt/x:thread-spawn (fn [] (return "OK")))
   (. (then (repl/>notify)))))
 =>
 "OK")

^{:refer xt.lang.common-spec/x:thread-join, :added "4.1"}
(fact
 "throws for unsupported js thread joins"
 (!.py (xt/x:thread-join {}))
 =>
 (throws))

^{:refer xt.lang.common-spec/x:with-delay, :added "4.1"}
(fact
 "delays asynchronous js computations"
 (notify/wait-on
  :python
  (->
   (xt/x:with-delay (fn [] (return "LATER")) 20)
   (. (then (repl/>notify)))))
 =>
 "LATER")

^{:refer xt.lang.common-spec/x:uri-encode, :added "4.1"}
(fact
 "encodes uri components"
 (!.py (xt/x:uri-encode "hello world"))
 =>
 "hello%20world")

^{:refer xt.lang.common-spec/x:uri-decode, :added "4.1"}
(fact
 "decodes uri components"
 (!.py (xt/x:uri-decode "hello%20world"))
 =>
 "hello world")

(comment
 (def
  ^{:private true} +common-spec-control-forms+
  '#{for:object
     for:array
     for:async
     for:return
     for:index
     return-run
     for:try
     for:iter})
 (defn-
  common-spec-defs
  [kind]
  (let
   [text
    (slurp "src/xt/lang/common_spec.clj")
    pattern
    (case
     kind
     :macro
     #"\(defmacro\.xt[^\n]*\n\s+([^\s\)]+)"
     :spec
     #"\(defspec\.xt\s+([^\s\)]+)")]
   (->> (re-seq pattern text) (map second) (map symbol) (sort) (vec))))
 (defn-
  common-spec-publics
  []
  (->> (ns-publics 'xt.lang.common-spec) keys sort vec))
 (defn- emit-lua-form [form] (l/emit-as :lua [(list 'fn [] form)]))
 (defn-
  emits-lua?
  [form pattern]
  (boolean (re-find pattern (emit-lua-form form))))
 (fact
  "keeps source macros and public wrappers in sync"
  (let
   [macros
    (common-spec-defs :macro)
    specs
    (common-spec-defs :spec)
    publics
    (common-spec-publics)]
   [(count macros)
    (count specs)
    (set macros)
    (set publics)
    (set/difference (set macros) (set specs))])
  =>
  [205
   197
   (set (common-spec-publics))
   (set (common-spec-publics))
   +common-spec-control-forms+])
 (fact
  "all public wrappers expose arglists metadata"
  (->>
   (ns-publics 'xt.lang.common-spec)
   (keep (fn [[sym var]] (when-not (:arglists (meta var)) sym)))
   vec)
  =>
  []))
