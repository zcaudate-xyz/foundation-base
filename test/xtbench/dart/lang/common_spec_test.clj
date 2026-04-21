(ns
 xtbench.dart.lang.common-spec-test
 (:use code.test)
 (:require
  [clojure.set :as set]
  [std.lang :as l]
  [std.lang.model.spec-lua :as lua]
  [xt.lang.common-notify :as notify]))

(l/script-
 :dart
 {:runtime :twostep, :require [[xt.lang.common-spec :as xt]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-spec/for:array, :added "4.1"}
(fact
 "iterates arrays in order"
 (!.dt
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
 (set
  (!.dt
   (var out [])
   (xt/for:object [[k v] {:a 1, :b 2}] (xt/x:arr-push out [k v]))
   out))
 =>
 #{["b" 2] ["a" 1]})

^{:refer xt.lang.common-spec/for:index, :added "4.1"}
(fact
 "iterates a numeric range"
 (!.dt
  (var out [])
  (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]] (xt/x:arr-push out i))
  out)
 =>
 [0 2])

^{:refer xt.lang.common-spec/for:return, :added "4.1"}
(fact
 "dispatches success and error branches"
 [(!.dt
   (var out nil)
   (xt/for:return
    [[ok err] (xt/return-run [resolve reject] (resolve "OK"))]
    {:success (:= out ok), :error (:= out err)})
   out)
  (!.dt
   (var out nil)
   (xt/for:return
    [[ok err] (xt/return-run [resolve reject] (reject "ERR"))]
    {:success (:= out ok), :error (:= out err)})
   out)]
 =>
 ["OK" "ERR"])

^{:refer xt.lang.common-spec/return-run, :added "4.1"}
(fact
 "normalises success and error callbacks"
 (!.dt
  (var out nil)
  (xt/for:return
   [[ok err] (xt/return-run [resolve reject] (resolve "OK"))]
   {:success (:= out ok), :error (:= out err)})
  out)
 =>
 "OK")

^{:refer xt.lang.common-spec/return-run, :added "4.1"}
(fact
 "can compute a value before resolving"
 (!.dt
  (var out nil)
  (xt/for:return
   [[ok err]
    (xt/return-run
     [resolve reject]
     (var total (+ 1 2 3))
     (resolve total))]
   {:success (:= out ok), :error (:= out err)})
  out)
 =>
 6)

^{:refer xt.lang.common-spec/return-run, :added "4.1"}
(fact
 "can branch between resolve and reject"
 [(!.dt
   (var out nil)
   (var value 5)
   (xt/for:return
    [[ok err]
     (xt/return-run
      [resolve reject]
      (if (< value 10) (reject "small") (resolve value)))]
    {:success (:= out ok), :error (:= out err)})
   out)
  (!.dt
   (var out nil)
   (var value 12)
   (xt/for:return
    [[ok err]
     (xt/return-run
      [resolve reject]
      (if (< value 10) (reject "small") (resolve value)))]
    {:success (:= out ok), :error (:= out err)})
   out)]
 =>
 ["small" 12])

^{:refer xt.lang.common-spec/return-run, :added "4.1"}
(fact
 "supports final returns through for:return"
 (!.dt
  ((fn
    []
    (xt/for:return
     [[ok err] (xt/return-run [resolve reject] (resolve "OK"))]
     {:success ok, :error err, :final true})
    (return "MISS"))))
 =>
 "OK")

^{:refer xt.lang.common-spec/x:return-run, :added "4.1"}
(fact
 "can be used directly inside for:return"
 (!.dt
  (var out nil)
  (xt/for:return
   [[ok err] (xt/x:return-run (fn [resolve reject] (reject "ERR")))]
   {:success (:= out ok), :error (:= out err)})
  out)
 =>
 "ERR")

^{:refer xt.lang.common-spec/x:get-idx, :added "4.1"}
(fact
 "reads the first indexed value"
 (!.dt (xt/x:get-idx [10 20 30] (xt/x:offset 0)))
 =>
 10)

^{:refer xt.lang.common-spec/x:set-idx, :added "4.1"}
(fact
 "writes an indexed value"
 (!.dt (var out [10 20 30]) (xt/x:set-idx out (xt/x:offset 1) 99) out)
 =>
 [10 99 30])

^{:refer xt.lang.common-spec/x:first, :added "4.1"}
(fact
 "gets the first array element"
 (!.dt (xt/x:first [10 20 30]))
 =>
 10)

^{:refer xt.lang.common-spec/x:second, :added "4.1"}
(fact
 "gets the second array element"
 (!.dt (xt/x:second [10 20 30]))
 =>
 20)

^{:refer xt.lang.common-spec/x:last, :added "4.1"}
(fact "gets the last array element" (!.dt (xt/x:last [10 20 30])) => 30)

^{:refer xt.lang.common-spec/x:second-last, :added "4.1"}
(fact
 "gets the element before the last"
 (!.dt (xt/x:second-last [10 20 30]))
 =>
 20)

^{:refer xt.lang.common-spec/x:arr-remove, :added "4.1"}
(fact
 "removes an element from an array"
 (!.dt
  ((fn
    []
    (var out [0 1 2 3])
    (xt/x:arr-remove out (xt/x:offset 1))
    (return out))))
 =>
 [0 2 3])

^{:refer xt.lang.common-spec/x:arr-push, :added "4.1"}
(fact
 "pushes an element onto an array"
 (!.dt (var out [1 2 3]) (xt/x:arr-push out 4) out)
 =>
 [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-pop, :added "4.1"}
(fact
 "pops the last element from an array"
 (!.dt (var out [1 2 3 4]) (xt/x:arr-pop out))
 =>
 4)

^{:refer xt.lang.common-spec/x:arr-push-first, :added "4.1"}
(fact
 "pushes an element to the front of an array"
 (!.dt (var out [1 2 3]) (xt/x:arr-push-first out 0) out)
 =>
 [0 1 2 3])

^{:refer xt.lang.common-spec/x:arr-pop-first, :added "4.1"}
(fact
 "pops the first element from an array"
 (!.dt (var out [0 1 2 3]) (xt/x:arr-pop-first out))
 =>
 0)

^{:refer xt.lang.common-spec/x:arr-insert, :added "4.1"}
(fact
 "inserts an element into an array"
 (!.dt (var out [1 2 3]) (xt/x:arr-insert out (xt/x:offset 1) 9) out)
 =>
 [1 9 2 3])

^{:refer xt.lang.common-spec/x:arr-slice, :added "4.1"}
(fact
 "slices a range from an array"
 (!.dt (xt/x:arr-slice [1 2 3] (xt/x:offset 0) (xt/x:offset 1)))
 =>
 [1])

^{:refer xt.lang.common-spec/x:arr-reverse, :added "4.1"}
(fact "reverses an array" (!.dt (xt/x:arr-reverse [1 2 3])) => [3 2 1])

^{:refer xt.lang.common-spec/x:cat, :added "4.1"}
(fact
 "concatenates strings"
 (!.dt (xt/x:cat "hello" "-" "world"))
 =>
 "hello-world")

^{:refer xt.lang.common-spec/x:len, :added "4.1"}
(fact "gets the collection length" (!.dt (xt/x:len [1 2 3])) => 3)

^{:refer xt.lang.common-spec/x:to-string, :added "4.1"}
(fact "converts a value to a string" (!.dt (xt/x:to-string 12)) => "12")

^{:refer xt.lang.common-spec/x:to-number, :added "4.1"}
(fact
 "converts a string to a number"
 (!.dt (xt/x:to-number "12.5"))
 =>
 12.5)

^{:refer xt.lang.common-spec/x:not-nil?, :added "4.1"}
(fact "checks for non-nil values" (!.dt (xt/x:not-nil? 0)) => true)

^{:refer xt.lang.common-spec/x:nil?, :added "4.1"}
(fact "checks for nil values" (!.dt (xt/x:nil? nil)) => true)

^{:refer xt.lang.common-spec/x:is-string?, :added "4.1"}
(fact "recognises strings" (!.dt (xt/x:is-string? "abc")) => true)

^{:refer xt.lang.common-spec/x:is-number?, :added "4.1"}
(fact "recognises numbers" (!.dt (xt/x:is-number? 1.5)) => true)

^{:refer xt.lang.common-spec/x:is-integer?, :added "4.1"}
(fact "recognises integers" (!.dt (xt/x:is-integer? 2)) => true)

^{:refer xt.lang.common-spec/x:is-boolean?, :added "4.1"}
(fact "recognises booleans" (!.dt (xt/x:is-boolean? true)) => true)

^{:refer xt.lang.common-spec/x:is-object?, :added "4.1"}
(fact "recognises objects" (!.dt (xt/x:is-object? {:a 1})) => true)

^{:refer xt.lang.common-spec/x:is-array?, :added "4.1"}
(fact "recognises arrays" (!.dt (xt/x:is-array? [1 2])) => true)

^{:refer xt.lang.common-spec/x:add, :added "4.1"}
(fact "adds numbers" (!.dt (xt/x:add 1 2 3)) => 6)

^{:refer xt.lang.common-spec/x:sub, :added "4.1"}
(fact "subtracts numbers" (!.dt (xt/x:sub 10 3 2)) => 5)

^{:refer xt.lang.common-spec/x:mul, :added "4.1"}
(fact "multiplies numbers" (!.dt (xt/x:mul 2 3 4)) => 24)

^{:refer xt.lang.common-spec/x:div, :added "4.1"}
(fact "divides numbers" (!.dt (xt/x:div 20 5)) => (approx 4))

^{:refer xt.lang.common-spec/x:neg, :added "4.1"}
(fact "negates a number" (!.dt (xt/x:neg 2)) => -2)

^{:refer xt.lang.common-spec/x:inc, :added "4.1"}
(fact "increments a number" (!.dt (xt/x:inc 2)) => 3)

^{:refer xt.lang.common-spec/x:dec, :added "4.1"}
(fact "decrements a number" (!.dt (xt/x:dec 2)) => 1)

^{:refer xt.lang.common-spec/x:zero?, :added "4.1"}
(fact "checks whether a number is zero" (!.dt (xt/x:zero? 0)) => true)

^{:refer xt.lang.common-spec/x:pos?, :added "4.1"}
(fact
 "checks whether a number is positive"
 (!.dt (xt/x:pos? 2))
 =>
 true)

^{:refer xt.lang.common-spec/x:neg?, :added "4.1"}
(fact
 "checks whether a number is negative"
 (!.dt (xt/x:neg? -2))
 =>
 true)

^{:refer xt.lang.common-spec/x:even?, :added "4.1"}
(fact "checks whether a number is even" (!.dt (xt/x:even? 4)) => true)

^{:refer xt.lang.common-spec/x:odd?, :added "4.1"}
(fact "checks whether a number is odd" (!.dt (xt/x:odd? 5)) => true)

^{:refer xt.lang.common-spec/x:eq, :added "4.1"}
(fact "checks equality" (!.dt (xt/x:eq 2 2)) => true)

^{:refer xt.lang.common-spec/x:neq, :added "4.1"}
(fact "checks inequality" (!.dt (xt/x:neq 2 3)) => true)

^{:refer xt.lang.common-spec/x:lt, :added "4.1"}
(fact "checks less than" (!.dt (xt/x:lt 2 3)) => true)

^{:refer xt.lang.common-spec/x:lte, :added "4.1"}
(fact "checks less than or equal" (!.dt (xt/x:lte 3 3)) => true)

^{:refer xt.lang.common-spec/x:gt, :added "4.1"}
(fact "checks greater than" (!.dt (xt/x:gt 4 3)) => true)

^{:refer xt.lang.common-spec/x:gte, :added "4.1"}
(fact "checks greater than or equal" (!.dt (xt/x:gte 4 4)) => true)

^{:refer xt.lang.common-spec/x:has-key?, :added "4.1"}
(fact
 "checks whether an object has a key"
 (!.dt (var obj {:a 1}) (xt/x:has-key? obj "a"))
 =>
 true)

^{:refer xt.lang.common-spec/x:get-key, :added "4.1"}
(fact
 "gets a value by key with a fallback"
 (!.dt (xt/x:get-key {} "missing" "fallback"))
 =>
 "fallback")

^{:refer xt.lang.common-spec/x:get-path, :added "4.1"}
(fact
 "gets a nested value by path"
 (!.dt (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
 =>
 2)

^{:refer xt.lang.common-spec/x:set-key, :added "4.1"}
(fact
 "sets a key on an object"
 (!.dt (var out {:a 1}) (xt/x:set-key out "b" 2) out)
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:copy-key, :added "4.1"}
(fact
 "copies a key from another object"
 (!.dt (var out {:a 1}) (xt/x:copy-key out {:a 9} ["c" "a"]) out)
 =>
 {"a" 1, "c" 9})

^{:refer xt.lang.common-spec/x:obj-keys, :added "4.1"}
(fact
 "lists object keys"
 (set (!.dt (xt/x:obj-keys {:a 1, :b 2})))
 =>
 #{"a" "b"})

^{:refer xt.lang.common-spec/x:obj-vals, :added "4.1"}
(fact
 "lists object values"
 (set (!.dt (xt/x:obj-vals {:a 1, :b 2})))
 =>
 #{1 2})

^{:refer xt.lang.common-spec/x:obj-pairs, :added "4.1"}
(fact
 "lists object pairs"
 (set (!.dt (xt/x:obj-pairs {:a 1, :b 2})))
 =>
 #{["b" 2] ["a" 1]})

^{:refer xt.lang.common-spec/x:obj-clone, :added "4.1"}
(fact
 "clones an object"
 (!.dt
  (var src {:a 1})
  (var out (xt/x:obj-clone src))
  (xt/x:set-key src "b" 2)
  out)
 =>
 {"a" 1})

^{:refer xt.lang.common-spec/x:obj-assign, :added "4.1"}
(fact
 "assigns object keys"
 (!.dt (xt/x:obj-assign {:a 1} {:b 2}))
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:obj-from-pairs, :added "4.1"}
(fact
 "creates an object from pairs"
 (!.dt (xt/x:obj-from-pairs [["a" 1] ["b" 2]]))
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:str-len, :added "4.1"}
(fact "gets the string length" (!.dt (xt/x:str-len "hello")) => 5)

^{:refer xt.lang.common-spec/x:str-pad-left, :added "4.1"}
(fact
 "pads a string on the left"
 (!.dt (xt/x:str-pad-left "7" 3 "0"))
 =>
 "007")

^{:refer xt.lang.common-spec/x:str-pad-right, :added "4.1"}
(fact
 "pads a string on the right"
 (!.dt (xt/x:str-pad-right "7" 3 "0"))
 =>
 "700")

^{:refer xt.lang.common-spec/x:str-starts-with, :added "4.1"}
(fact
 "checks the string prefix"
 (!.dt (xt/x:str-starts-with "hello" "he"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-ends-with, :added "4.1"}
(fact
 "checks the string suffix"
 (!.dt (xt/x:str-ends-with "hello" "lo"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-char, :added "4.1"}
(fact
 "gets the character code at an index"
 (!.dt (xt/x:str-char "abc" 1))
 =>
 98)

^{:refer xt.lang.common-spec/x:str-split, :added "4.1"}
(fact
 "splits a string"
 (!.dt (xt/x:str-split "a/b/c" "/"))
 =>
 ["a" "b" "c"])

^{:refer xt.lang.common-spec/x:str-join, :added "4.1"}
(fact
 "joins string parts"
 (!.dt (xt/x:str-join "-" ["a" "b" "c"]))
 =>
 "a-b-c")

^{:refer xt.lang.common-spec/x:str-index-of, :added "4.1"}
(fact
 "finds the index of a substring"
 (!.dt (xt/x:str-index-of "hello/world" "/" 0))
 =>
 5)

^{:refer xt.lang.common-spec/x:str-substring, :added "4.1"}
(fact
 "gets a substring"
 (!.dt (xt/x:str-substring "hello/world" 3 8))
 =>
 "lo/wo")

^{:refer xt.lang.common-spec/x:str-to-upper, :added "4.1"}
(fact
 "converts a string to upper case"
 (!.dt (xt/x:str-to-upper "hello"))
 =>
 "HELLO")

^{:refer xt.lang.common-spec/x:str-to-lower, :added "4.1"}
(fact
 "converts a string to lower case"
 (!.dt (xt/x:str-to-lower "HELLO"))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:str-to-fixed, :added "4.1"}
(fact
 "formats a number with fixed decimals"
 (!.dt (xt/x:str-to-fixed 1.2 2))
 =>
 "1.20")

^{:refer xt.lang.common-spec/x:arr-clone, :added "4.1"}
(fact
 "clones an array"
 (!.dt
  (var src [1 2])
  (var out (xt/x:arr-clone src))
  (xt/x:arr-push src 3)
  out)
 =>
 [1 2])

^{:refer xt.lang.common-spec/x:arr-each, :added "4.1"}
(fact
 "iterates each element in an array"
 (!.dt
  (var out [])
  (xt/x:arr-each [1 2 3] (fn [e] (xt/x:arr-push out (* e 2))))
  out)
 =>
 [2 4 6])

^{:refer xt.lang.common-spec/x:arr-map, :added "4.1"}
(fact
 "maps an array"
 (!.dt (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
 =>
 [2 4 6])

^{:refer xt.lang.common-spec/x:arr-assign, :added "4.1"}
(fact
 "appends one array to another"
 (!.dt (xt/x:arr-assign [1 2] [3 4]))
 =>
 [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-filter, :added "4.1"}
(fact
 "filters an array"
 (!.dt (xt/x:arr-filter [1 2 3 4] (fn [e] (return (xt/x:even? e)))))
 =>
 [2 4])

^{:refer xt.lang.common-spec/x:arr-keep, :added "4.1"}
(fact
 "keeps transformed non-nil values from an array"
 (!.dt
  (xtd/arr-keep
   [1 2 3]
   (fn [e] (when (xt/x:odd? e) (return (* e 10))))))
 =>
 [10 30])

^{:refer xt.lang.common-spec/x:is-function?, :added "4.1"}
(fact
 "recognises function values"
 (!.dt (xt/x:is-function? (fn [x] (return x))))
 =>
 true)

^{:refer xt.lang.common-spec/x:callback, :added "4.1"}
(fact
 "returns the empty callback token in lua"
 (!.dt (xt/x:nil? (xt/x:callback)))
 =>
 true)

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
 [])

(fact
 "all non-control wrappers keep matching defspec contracts"
 (let
  [macros
   (set (common-spec-defs :macro))
   specs
   (set (common-spec-defs :spec))]
  [(set/difference specs macros)
   (set/difference
    (set/difference macros +common-spec-control-forms+)
    specs)])
 =>
 [#{} #{}])

(fact
 "the lua test namespace imports the shared helper modules used by common-spec"
 (let
  [text (slurp "test/xt/lang/common_spec_test.clj")]
  (mapv
   (fn* [p1__12382#] (boolean (re-find (re-pattern p1__12382#) text)))
   ["\\[xt\\.lang\\.common-spec :as xt\\]"
    "\\[xt\\.lang\\.common-data :as xtd\\]"
    "\\[xt\\.lang\\.common-repl :as repl\\]"
    "\\[xt\\.lang\\.common-string :as xts\\]"]))
 =>
 [true true true true])

^{:refer xt.lang.common-spec/for:iter, :added "4.1"}
(fact
 "expands to the canonical iterator form"
 (!.dt
  (var out [])
  (xt/for:iter [e (xt/x:iter-from-arr [1 2 3])] (xt/x:arr-push out e))
  out)
 =>
 [1 2 3])

^{:refer xt.lang.common-spec/for:try, :added "4.1"}
(fact
 "expands to the canonical try form"
 (emits-lua?
  '(for:try
    [[ok err] (call (x:callback))]
    {:success (return ok), :error (return err)})
  #"pcall")
 =>
 true)

^{:refer xt.lang.common-spec/for:async, :added "4.1"}
(fact
 "expands to the canonical async form"
 (emits-lua?
  '(for:async
    [[ok err] (call (x:callback))]
    {:success (return ok),
     :error (return err),
     :finally (return true)})
  #"ngx\.thread\.spawn")
 =>
 true)

^{:refer xt.lang.common-spec/x:del, :added "4.1"}
(fact
 "expands and emits a lua delete form"
 (!.dt (var out {:a 1, :b 2}) (xt/x:del (. out ["a"])) out)
 =>
 {"b" 2})

^{:refer xt.lang.common-spec/x:err, :added "4.1"}
(fact
 "expands and emits a lua error form"
 (emits-lua? '(x:err "boom") #"error")
 =>
 true)

^{:refer xt.lang.common-spec/x:type-native, :added "4.1"}
(fact
 "expands and emits the lua type helper"
 (emits-lua? '(x:type-native obj) #"type")
 =>
 true)

^{:refer xt.lang.common-spec/x:offset, :added "4.1"}
(fact "uses the grammar base offset" (!.dt (xt/x:offset 10)) => 10)

^{:refer xt.lang.common-spec/x:offset-rev, :added "4.1"}
(fact
 "uses the reverse grammar offset"
 (!.dt (xt/x:offset-rev 10))
 =>
 9)

^{:refer xt.lang.common-spec/x:offset-len, :added "4.1"}
(fact "uses the length grammar offset" (!.dt (xt/x:offset-len 10)) => 9)

^{:refer xt.lang.common-spec/x:offset-rlen, :added "4.1"}
(fact
 "uses the reverse length grammar offset"
 (!.dt (xt/x:offset-rlen 10))
 =>
 10)

^{:refer xt.lang.common-spec/x:lu-create, :added "4.1"}
(fact
 "creates a lookup table wrapper"
 (!.dt
  (var lu (xt/x:lu-create))
  (xt/x:lu-set lu "k" 1)
  (xt/x:lu-get lu "k"))
 =>
 1)

^{:refer xt.lang.common-spec/x:lu-eq, :added "4.1"}
(fact
 "compares lookup keys using lua identity"
 (!.dt (var obj {:id 1}) (xt/x:lu-eq obj obj))
 =>
 true)

^{:refer xt.lang.common-spec/x:lu-get, :added "4.1"}
(fact
 "reads values from a lookup table"
 (!.dt
  (var lu (xt/x:lu-create))
  (var obj {:id 1})
  (xt/x:lu-set lu obj "value")
  (xt/x:lu-get lu obj))
 =>
 "value")

^{:refer xt.lang.common-spec/x:lu-set, :added "4.1"}
(fact
 "writes values into a lookup table"
 (!.dt
  (var lu (xt/x:lu-create))
  (var obj {:id 1})
  (xt/x:lu-set lu obj "value")
  (xt/x:lu-get lu obj))
 =>
 "value")

^{:refer xt.lang.common-spec/x:lu-del, :added "4.1"}
(fact
 "removes values from a lookup table"
 (!.dt
  (var lu (xt/x:lu-create))
  (var obj {:id 1})
  (xt/x:lu-set lu obj "value")
  (xt/x:lu-del lu obj)
  (xt/x:lu-get lu obj))
 =>
 nil)

^{:refer xt.lang.common-spec/x:m-abs, :added "4.1"}
(fact "computes absolute values" (!.dt (xt/x:m-abs -3)) => 3)

^{:refer xt.lang.common-spec/x:m-acos, :added "4.1"}
(fact "computes inverse cosine" (!.dt (xt/x:m-acos 1)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-asin, :added "4.1"}
(fact "computes inverse sine" (!.dt (xt/x:m-asin 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-atan, :added "4.1"}
(fact "computes inverse tangent" (!.dt (xt/x:m-atan 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-ceil, :added "4.1"}
(fact "rounds numbers upward" (!.dt (xt/x:m-ceil 1.2)) => 2)

^{:refer xt.lang.common-spec/x:m-cos, :added "4.1"}
(fact "computes cosine" (!.dt (xt/x:m-cos 0)) => (approx 1))

^{:refer xt.lang.common-spec/x:m-cosh, :added "4.1"}
(fact "computes hyperbolic cosine" (!.dt (xt/x:m-cosh 0)) => (approx 1))

^{:refer xt.lang.common-spec/x:m-exp, :added "4.1"}
(fact
 "computes the exponential function"
 (!.dt (xt/x:m-exp 0))
 =>
 (approx 1))

^{:refer xt.lang.common-spec/x:m-floor, :added "4.1"}
(fact "rounds numbers downward" (!.dt (xt/x:m-floor 1.8)) => 1)

^{:refer xt.lang.common-spec/x:m-loge, :added "4.1"}
(fact
 "computes the natural logarithm"
 (!.dt (xt/x:m-loge 1))
 =>
 (approx 0))

^{:refer xt.lang.common-spec/x:m-log10, :added "4.1"}
(fact
 "computes the base-10 logarithm"
 (!.dt (xt/x:m-log10 100))
 =>
 (approx 2))

^{:refer xt.lang.common-spec/x:m-max, :added "4.1"}
(fact "computes the maximum value" (!.dt (xt/x:m-max 3 5)) => 5)

^{:refer xt.lang.common-spec/x:m-mod, :added "4.1"}
(fact "computes modulo values" (!.dt (xt/x:m-mod 10 3)) => 1)

^{:refer xt.lang.common-spec/x:m-min, :added "4.1"}
(fact "computes the minimum value" (!.dt (xt/x:m-min 3 5)) => 3)

^{:refer xt.lang.common-spec/x:m-pow, :added "4.1"}
(fact "raises numbers to a power" (!.dt (xt/x:m-pow 2 4)) => 16)

^{:refer xt.lang.common-spec/x:m-quot, :added "4.1"}
(fact "computes integer quotients" (!.dt (xt/x:m-quot 7 2)) => 3)

^{:refer xt.lang.common-spec/x:m-sin, :added "4.1"}
(fact "computes sine" (!.dt (xt/x:m-sin 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-sinh, :added "4.1"}
(fact "computes hyperbolic sine" (!.dt (xt/x:m-sinh 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-sqrt, :added "4.1"}
(fact "computes square roots" (!.dt (xt/x:m-sqrt 9)) => (approx 3))

^{:refer xt.lang.common-spec/x:m-tan, :added "4.1"}
(fact "computes tangent" (!.dt (xt/x:m-tan 0)) => (approx 0))

^{:refer xt.lang.common-spec/x:m-tanh, :added "4.1"}
(fact
 "computes hyperbolic tangent"
 (!.dt (xt/x:m-tanh 0))
 =>
 (approx 0))

^{:refer xt.lang.common-spec/x:del-key, :added "4.1"}
(fact
 "deletes keys from objects"
 (!.dt (var out {:a 1, :b 2}) (xt/x:del-key out "a") out)
 =>
 {"b" 2})

^{:refer xt.lang.common-spec/x:print, :added "4.1"}
(fact "expands and emits a lua print form")

^{:refer xt.lang.common-spec/x:str-comp, :added "4.1"}
(fact
 "compares strings by sort order"
 (!.dt (xt/x:str-comp "abc" "abd"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-lt, :added "4.1"}
(fact
 "checks whether one string sorts before another"
 (!.dt (xt/x:str-lt "abc" "abd"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-gt, :added "4.1"}
(fact
 "checks whether one string sorts after another"
 (!.dt (xt/x:str-gt "abd" "abc"))
 =>
 true)

^{:refer xt.lang.common-spec/x:str-format, :added "4.1"}
(fact
 "keeps the string format wrapper intact"
 (:arglists (meta #'xt/x:str-format))
 =>
 '([template values]))

^{:refer xt.lang.common-spec/x:str-replace, :added "4.1"}
(fact
 "replaces matching substrings"
 (!.dt (xt/x:str-replace "hello-world" "-" "/"))
 =>
 "hello/world")

^{:refer xt.lang.common-spec/x:str-trim, :added "4.1"}
(fact
 "trims whitespace from both sides"
 (!.dt (xt/x:str-trim "  hello  "))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:str-trim-left, :added "4.1"}
(fact
 "trims whitespace from the left side"
 (!.dt (xt/x:str-trim-left "  hello"))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:str-trim-right, :added "4.1"}
(fact
 "trims whitespace from the right side"
 (!.dt (xt/x:str-trim-right "hello  "))
 =>
 "hello")

^{:refer xt.lang.common-spec/x:arr-sort, :added "4.1"}
(fact
 "sorts arrays using key and compare functions"
 (!.dt
  (var out [{:id 3} {:id 1} {:id 2}])
  (xt/x:arr-sort
   out
   (fn [e] (return (xt/x:get-key e "id")))
   (fn [a b] (return (xt/x:lt a b))))
  out)
 =>
 [{"id" 1} {"id" 2} {"id" 3}])

^{:refer xt.lang.common-spec/x:arr-every, :added "4.1"}
(fact
 "checks whether every array element matches a predicate"
 (!.dt (xt/x:arr-every [2 4 6] (fn [e] (return (xt/x:even? e)))))
 =>
 true)

^{:refer xt.lang.common-spec/x:arr-some, :added "4.1"}
(fact
 "checks whether any array element matches a predicate"
 (!.dt (xt/x:arr-some [1 3 4] (fn [e] (return (xt/x:even? e)))))
 =>
 true)

^{:refer xt.lang.common-spec/x:arr-foldl, :added "4.1"}
(fact
 "folds arrays from the left"
 (!.dt (xt/x:arr-foldl [1 2 3] (fn [out e] (return (+ out e))) 0))
 =>
 6)

^{:refer xt.lang.common-spec/x:arr-foldr, :added "4.1"}
(fact
 "folds arrays from the right"
 (!.dt
  (xt/x:arr-foldr [1 2 3] (fn [out e] (return (+ (* 10 out) e))) 0))
 =>
 321)

^{:refer xt.lang.common-spec/x:arr-find, :added "4.1"}
(fact
 "keeps the find wrapper pointed at the canonical op"
 (:arglists (meta #'xt/x:arr-find))
 =>
 '([arr pred]))

^{:refer xt.lang.common-spec/x:future-run, :added "4.1"}
(fact
 "expands and emits a lua future runner"
 (emits-lua? '(x:future-run thunk) #"state")
 =>
 true)

^{:refer xt.lang.common-spec/x:future-then, :added "4.1"}
(fact
 "expands and emits a lua future continuation"
 (emits-lua? '(x:future-then task on-ok) #"pcall")
 =>
 true)

^{:refer xt.lang.common-spec/x:future-catch, :added "4.1"}
(fact
 "expands and emits a lua future error handler"
 (emits-lua? '(x:future-catch task on-err) #"pcall")
 =>
 true)

^{:refer xt.lang.common-spec/x:future-finally, :added "4.1"}
(fact
 "expands and emits a lua future finalizer"
 (emits-lua? '(x:future-finally task on-done) #"return")
 =>
 true)

^{:refer xt.lang.common-spec/x:future-cancel, :added "4.1"}
(fact
 "expands and emits a lua future cancellation"
 (emits-lua? '(x:future-cancel task) #"cancelled")
 =>
 true)

^{:refer xt.lang.common-spec/x:future-status, :added "4.1"}
(fact
 "expands and emits a lua future status lookup"
 (emits-lua? '(x:future-status task) #"state")
 =>
 true)

^{:refer xt.lang.common-spec/x:future-await, :added "4.1"}
(fact
 "expands and emits a lua future await"
 (emits-lua? '(x:future-await task 1000 default) #"default")
 =>
 true)

^{:refer xt.lang.common-spec/x:future-from-async, :added "4.1"}
(fact
 "expands and emits a lua async future bridge"
 (emits-lua? '(x:future-from-async executor) #"executor")
 =>
 true)

^{:refer xt.lang.common-spec/x:eval, :added "4.1"}
(fact
 "expands and emits a lua eval form"
 (emits-lua? '(x:eval "1 + 1") #"loadstring")
 =>
 true)

^{:refer xt.lang.common-spec/x:apply, :added "4.1"}
(fact
 "expands and emits a lua apply form"
 (emits-lua? '(x:apply f args) #"unpack")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-from-obj, :added "4.1"}
(fact
 "expands and emits an object iterator"
 (emits-lua? '(x:iter-from-obj obj) #"coroutine\.wrap")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-from-arr, :added "4.1"}
(fact
 "expands and emits an array iterator"
 (emits-lua? '(x:iter-from-arr arr) #"coroutine\.wrap")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-from, :added "4.1"}
(fact
 "expands and emits a generic iterator"
 (emits-lua? '(x:iter-from obj) #"coroutine\.wrap")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-eq, :added "4.1"}
(fact
 "expands and emits iterator equality checks"
 (emits-lua? '(x:iter-eq it0 it1 eq-fn) #"for")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-null, :added "4.1"}
(fact
 "expands and emits an empty iterator"
 (emits-lua? '(x:iter-null) #"coroutine\.wrap")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-next, :added "4.1"}
(fact
 "expands and emits an iterator advance call"
 (emits-lua? '(x:iter-next it) #"it")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-has?, :added "4.1"}
(fact
 "expands and emits an iterator predicate"
 (emits-lua? '(x:iter-has? it) #"iterator")
 =>
 true)

^{:refer xt.lang.common-spec/x:iter-native?, :added "4.1"}
(fact
 "expands and emits a native iterator predicate"
 (emits-lua? '(x:iter-native? it) #"type")
 =>
 true)

^{:refer xt.lang.common-spec/x:return-encode, :added "4.1"}
(fact
 "expands and emits the lua return encoder"
 (emits-lua? '(x:return-encode out "id" "key") #"cjson\.encode")
 =>
 true)

^{:refer xt.lang.common-spec/x:return-wrap, :added "4.1"}
(fact
 "expands and emits the lua return wrapper"
 (emits-lua? '(x:return-wrap callback encode-fn) #"pcall")
 =>
 true)

^{:refer xt.lang.common-spec/x:return-eval, :added "4.1"}
(fact
 "expands and emits the lua return eval form"
 (emits-lua? '(x:return-eval "1 + 1" wrap-fn) #"loadstring")
 =>
 true)

^{:refer xt.lang.common-spec/x:bit-and, :added "4.1"}
(fact "computes bitwise and" (!.dt (xt/x:bit-and 6 3)) => 2)

^{:refer xt.lang.common-spec/x:bit-or, :added "4.1"}
(fact "computes bitwise or" (!.dt (xt/x:bit-or 6 3)) => 7)

^{:refer xt.lang.common-spec/x:bit-lshift, :added "4.1"}
(fact "computes bitwise left shifts" (!.dt (xt/x:bit-lshift 3 2)) => 12)

^{:refer xt.lang.common-spec/x:bit-rshift, :added "4.1"}
(fact
 "computes bitwise right shifts"
 (!.dt (xt/x:bit-rshift 12 2))
 =>
 3)

^{:refer xt.lang.common-spec/x:bit-xor, :added "4.1"}
(fact "computes bitwise xor" (!.dt (xt/x:bit-xor 6 3)) => 5)

^{:refer xt.lang.common-spec/x:global-set, :added "4.1"}
(fact
 "writes values to the shared global map"
 (emits-lua? '(x:global-set SYM 1) #"SYM")
 =>
 true)

^{:refer xt.lang.common-spec/x:global-del, :added "4.1"}
(fact
 "removes values from the shared global map"
 (emits-lua? '(x:global-del SYM) #"nil")
 =>
 true)

^{:refer xt.lang.common-spec/x:global-has?, :added "4.1"}
(fact
 "checks whether the shared global map contains a value"
 (emits-lua? '(x:global-has? SYM) #"SYM")
 =>
 true)

^{:refer xt.lang.common-spec/x:this, :added "4.1"}
(fact
 "expands and emits the lua self binding"
 (emits-lua? '(x:this) #"self")
 =>
 true)

^{:refer xt.lang.common-spec/x:proto-get, :added "4.1"}
(fact
 "expands and emits prototype lookup"
 (emits-lua? '(x:proto-get obj key) #"getmetatable")
 =>
 true)

^{:refer xt.lang.common-spec/x:proto-set, :added "4.1"}
(fact
 "expands and emits prototype assignment"
 (emits-lua? '(x:proto-set obj proto value) #"setmetatable")
 =>
 true)

^{:refer xt.lang.common-spec/x:proto-create, :added "4.1"}
(fact
 "expands to a canonical prototype constructor"
 (emits-lua? '(x:proto-create {:a 1}) #"return")
 =>
 true)

^{:refer xt.lang.common-spec/x:proto-tostring, :added "4.1"}
(fact
 "expands and emits the lua tostring metamethod key"
 (:arglists (meta #'xt/x:proto-tostring))
 =>
 '([value]))

^{:refer xt.lang.common-spec/x:random, :added "4.1"}
(fact
 "expands and emits the lua random function"
 (emits-lua? '(x:random) #"math\.random")
 =>
 true)

^{:refer xt.lang.common-spec/x:throw, :added "4.1"}
(fact
 "expands to the canonical throw form"
 (:arglists (meta #'xt/x:throw))
 =>
 '([value]))

^{:refer xt.lang.common-spec/x:now-ms, :added "4.1"}
(fact
 "expands and emits a millisecond time expression"
 (!.dt (> (xt/x:now-ms) 0))
 =>
 true)

^{:refer xt.lang.common-spec/x:unpack, :added "4.1"}
(fact
 "expands and emits the lua unpack helper"
 (emits-lua? '(x:unpack args) #"unpack")
 =>
 true)

^{:refer xt.lang.common-spec/x:client-basic, :added "4.1"}
(fact
 "expands and emits the lua basic client loop"
 (emits-lua?
  '(x:client-basic "localhost" 8080 connect-fn eval-fn)
  #"receive")
 =>
 true)

^{:refer xt.lang.common-spec/x:client-ws, :added "4.1"}
(fact
 "expands and emits the lua websocket client loop"
 (emits-lua?
  '(x:client-ws "localhost" 8080 {} connect-fn eval-fn)
  #"recv")
 =>
 true)

^{:refer xt.lang.common-spec/x:server-basic, :added "4.1"}
(fact
 "keeps the basic server wrapper intact"
 (emits-lua? '(x:server-basic config) #"server")
 =>
 true)

^{:refer xt.lang.common-spec/x:server-ws, :added "4.1"}
(fact
 "keeps the websocket server wrapper intact"
 (emits-lua? '(x:server-ws config) #"server")
 =>
 true)

^{:refer xt.lang.common-spec/x:socket-connect, :added "4.1"}
(fact
 "expands and emits a lua socket connect form"
 (emits-lua?
  '(x:socket-connect "localhost" 8080 {} callback)
  #"connect")
 =>
 true)

^{:refer xt.lang.common-spec/x:socket-send, :added "4.1"}
(fact
 "expands and emits a lua socket send form"
 (emits-lua? '(x:socket-send conn "PING") #"send")
 =>
 true)

^{:refer xt.lang.common-spec/x:socket-close, :added "4.1"}
(fact
 "expands and emits a lua socket close form"
 (emits-lua? '(x:socket-close conn) #"close")
 =>
 true)

^{:refer xt.lang.common-spec/x:ws-connect, :added "4.1"}
(fact
 "expands and emits a lua websocket connect form"
 (emits-lua? '(x:ws-connect "localhost" 8080 {}) #"websocket")
 =>
 true)

^{:refer xt.lang.common-spec/x:ws-send, :added "4.1"}
(fact
 "expands and emits a lua websocket send form"
 (emits-lua? '(x:ws-send wb "PING") #"send")
 =>
 true)

^{:refer xt.lang.common-spec/x:ws-close, :added "4.1"}
(fact
 "expands and emits a lua websocket close form"
 (emits-lua? '(x:ws-close wb) #"close")
 =>
 true)

^{:refer xt.lang.common-spec/x:notify-http, :added "4.1"}
(fact
 "keeps the notify-http wrapper intact"
 (emits-lua?
  '(x:notify-http "localhost" 8080 value "id" "key" encode-fn)
  #"http")
 =>
 true)

^{:refer xt.lang.common-spec/x:notify-socket, :added "4.1"}
(fact
 "keeps the notify-socket wrapper intact"
 (:arglists (meta #'xt/x:notify-socket))
 =>
 '([host port value id key connect-fn encode-fn]))

^{:refer xt.lang.common-spec/x:b64-encode, :added "4.1"}
(fact
 "expands and emits the lua base64 encoder"
 (emits-lua? '(x:b64-encode "hello") #"ngx\.encode")
 =>
 true)

^{:refer xt.lang.common-spec/x:b64-decode, :added "4.1"}
(fact
 "expands and emits the lua base64 decoder"
 (emits-lua? '(x:b64-decode "aGVsbG8=") #"ngx\.decode")
 =>
 true)

^{:refer xt.lang.common-spec/x:cache, :added "4.1"}
(fact
 "expands and emits a lua cache lookup"
 (emits-lua? '(x:cache "GLOBAL") #"ngx\.shared")
 =>
 true)

^{:refer xt.lang.common-spec/x:cache-list, :added "4.1"}
(fact
 "expands and emits a lua cache key listing"
 (emits-lua? '(x:cache-list) #"keys")
 =>
 true)

^{:refer xt.lang.common-spec/x:cache-flush, :added "4.1"}
(fact
 "expands and emits a lua cache flush"
 (emits-lua? '(x:cache-flush cache) #"flush_all")
 =>
 true)

^{:refer xt.lang.common-spec/x:cache-get, :added "4.1"}
(fact
 "expands and emits a lua cache get"
 (emits-lua? '(x:cache-get cache "key") #"get")
 =>
 true)

^{:refer xt.lang.common-spec/x:cache-set, :added "4.1"}
(fact
 "expands and emits a lua cache set"
 (emits-lua? '(x:cache-set cache "key" "value") #"set")
 =>
 true)

^{:refer xt.lang.common-spec/x:cache-del, :added "4.1"}
(fact
 "expands and emits a lua cache delete"
 (emits-lua? '(x:cache-del cache "key") #"delete")
 =>
 true)

^{:refer xt.lang.common-spec/x:cache-incr, :added "4.1"}
(fact
 "expands and emits a lua cache increment"
 (emits-lua? '(x:cache-incr cache "key" 1) #"incr")
 =>
 true)

^{:refer xt.lang.common-spec/x:slurp, :added "4.1"}
(fact
 "keeps the slurp wrapper intact"
 (:arglists (meta #'xt/x:slurp))
 =>
 '([path]))

^{:refer xt.lang.common-spec/x:spit, :added "4.1"}
(fact
 "keeps the spit wrapper intact"
 (:arglists (meta #'xt/x:spit))
 =>
 '([path value]))

^{:refer xt.lang.common-spec/x:json-encode, :added "4.1"}
(fact
 "encodes lua data structures as json"
 (!.dt (xt/x:json-encode {:a 1}))
 =>
 #"\{\"a\":\s*1\}")

^{:refer xt.lang.common-spec/x:json-decode, :added "4.1"}
(fact
 "decodes json strings into lua data structures"
 (!.dt (xt/x:json-decode "{\"a\":1}"))
 =>
 {"a" 1})

^{:refer xt.lang.common-spec/x:shell, :added "4.1"}
(fact
 "expands and emits the lua shell helper"
 (emits-lua? '(x:shell "ls" opts) #"io\.popen")
 =>
 true)

^{:refer xt.lang.common-spec/x:thread-spawn, :added "4.1"}
(fact
 "expands and emits a lua thread spawn"
 (emits-lua? '(x:thread-spawn thunk) #"ngx\.thread\.spawn")
 =>
 true)

^{:refer xt.lang.common-spec/x:thread-join, :added "4.1"}
(fact
 "expands and emits a lua thread join"
 (emits-lua? '(x:thread-join thread) #"ngx\.thread\.wait")
 =>
 true)

^{:refer xt.lang.common-spec/x:with-delay, :added "4.1"}
(fact
 "expands and emits a delayed lua computation"
 (emits-lua? '(x:with-delay thunk 100) #"sleep")
 =>
 true)

^{:refer xt.lang.common-spec/x:start-interval, :added "4.1"}
(fact
 "keeps the start-interval wrapper intact"
 (:arglists (meta #'xt/x:start-interval))
 =>
 '([ms f]))

^{:refer xt.lang.common-spec/x:stop-interval, :added "4.1"}
(fact
 "keeps the stop-interval wrapper intact"
 (:arglists (meta #'xt/x:stop-interval))
 =>
 '([id]))

^{:refer xt.lang.common-spec/x:uri-encode, :added "4.1"}
(fact
 "expands and emits the lua uri encoder"
 (emits-lua? '(x:uri-encode "hello world") #"ngx\.escape")
 =>
 true)

^{:refer xt.lang.common-spec/x:uri-decode, :added "4.1"}
(fact
 "expands and emits the lua uri decoder"
 (emits-lua? '(x:uri-decode "hello%20world") #"ngx\.unescape")
 =>
 true)
