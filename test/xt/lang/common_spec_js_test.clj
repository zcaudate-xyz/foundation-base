(ns xt.lang.common-spec-js-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-string :as xts]]})

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
  => [1 2 3])

^{:refer xt.lang.common-spec/for:object :added "4.1"}
(fact "iterates object key value pairs"
  
  (set
   (!.js
     (var out [])
     (xt/for:object [[k v] {:a 1 :b 2}]
       (xt/x:arr-push out [k v]))
     out))
  => #{["a" 1] ["b" 2]})

^{:refer xt.lang.common-spec/for:index :added "4.1"}
(fact "iterates a numeric range"
  
  (!.js
    (var out [])
    (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]]
      (xt/x:arr-push out i))
    out)
  => [0 2])

^{:refer xt.lang.common-spec/for:return :added "4.1"}
(fact "dispatches success and error branches"
  
  [(!.js
     (var out nil)
     (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                 (resolve "OK"))]
       {:success (:= out ok)
        :error (:= out err)})
     out)
   (!.js
     (var out nil)
     (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                 (reject "ERR"))]
       {:success (:= out ok)
        :error (:= out err)})
     out)]
  => ["OK" "ERR"])

^{:refer xt.lang.common-spec/return-run :added "4.1"}
(fact "normalises success and error callbacks"
  (!.js
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                (resolve "OK"))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "OK")

^{:refer xt.lang.common-spec/return-run :added "4.1"}
(fact "can compute a value before resolving"
  (!.js
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                (var total (+ 1 2 3))
                                (resolve total))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => 6)

^{:refer xt.lang.common-spec/return-run :added "4.1"}
(fact "can branch between resolve and reject"
  [(!.js
     (var out nil)
     (var value 5)
     (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                 (if (< value 10)
                                   (reject "small")
                                   (resolve value)))]
       {:success (:= out ok)
        :error (:= out err)})
     out)
   (!.js
     (var out nil)
     (var value 12)
     (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                 (if (< value 10)
                                   (reject "small")
                                   (resolve value)))]
       {:success (:= out ok)
        :error (:= out err)})
     out)]
  => ["small" 12])

^{:refer xt.lang.common-spec/return-run :added "4.1"}
(fact "supports final returns through for:return"
  (!.js
    ((fn []
       (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                   (resolve "OK"))]
         {:success ok
          :error err
          :final true})
       (return "MISS"))))
  => "OK")

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
  => "ERR")

^{:refer xt.lang.common-spec/x:get-idx :added "4.1"}
(fact "reads the first indexed value"
  (!.js
    (xt/x:get-idx [10 20 30] (xt/x:offset 0)))
  => 10)

^{:refer xt.lang.common-spec/x:set-idx :added "4.1"}
(fact "writes an indexed value"
  (!.js
    (var out [10 20 30])
    (xt/x:set-idx out (xt/x:offset 1) 99)
    out)
  => [10 99 30])

^{:refer xt.lang.common-spec/x:first :added "4.1"}
(fact "gets the first array element"
  (!.js
    (xt/x:first [10 20 30]))
  => 10)

^{:refer xt.lang.common-spec/x:second :added "4.1"}
(fact "gets the second array element"
  (!.js
    (xt/x:second [10 20 30]))
  => 20)

^{:refer xt.lang.common-spec/x:last :added "4.1"}
(fact "gets the last array element"
  (!.js
    (xt/x:last [10 20 30]))
  => 30)

^{:refer xt.lang.common-spec/x:second-last :added "4.1"}
(fact "gets the element before the last"
  (!.js
    (xt/x:second-last [10 20 30]))
  => 20)

^{:refer xt.lang.common-spec/x:arr-remove :added "4.1"}
(fact "removes an element from an array"
  (!.js
    (var out [0 1 2 3])
    (xt/x:arr-remove out (xt/x:offset 1))
    out)
  => [0 2 3])

^{:refer xt.lang.common-spec/x:arr-push :added "4.1"}
(fact "pushes an element onto an array"
  (!.js
    (var out [1 2 3])
    (xt/x:arr-push out 4)
    out)
  => [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-pop :added "4.1"}
(fact "pops the last element from an array"
  (!.js
    (var out [1 2 3 4])
    (xt/x:arr-pop out))
  => 4)

^{:refer xt.lang.common-spec/x:arr-push-first :added "4.1"}
(fact "pushes an element to the front of an array"
  (!.js
    (var out [1 2 3])
    (xt/x:arr-push-first out 0)
    out)
  => [0 1 2 3])

^{:refer xt.lang.common-spec/x:arr-pop-first :added "4.1"}
(fact "pops the first element from an array"
  (!.js
    (var out [0 1 2 3])
    (xt/x:arr-pop-first out))
  => 0)

^{:refer xt.lang.common-spec/x:arr-insert :added "4.1"}
(fact "inserts an element into an array"
  (!.js
    (var out [1 2 3])
    (xt/x:arr-insert out (xt/x:offset 1) 9)
    out)
  => [1 9 2 3])

^{:refer xt.lang.common-spec/x:arr-slice :added "4.1"}
(fact "slices a range from an array"
  (!.js
    (xt/x:arr-slice [1 2 3] (xt/x:offset 0) (xt/x:offset 1)))
  => [1])

^{:refer xt.lang.common-spec/x:arr-reverse :added "4.1"}
(fact "reverses an array"
  (!.js
    (xt/x:arr-reverse [1 2 3]))
  => [3 2 1])

^{:refer xt.lang.common-spec/x:cat :added "4.1"}
(fact "concatenates strings"
  (!.js
    (xt/x:cat "hello" "-" "world"))
  => "hello-world")

^{:refer xt.lang.common-spec/x:len :added "4.1"}
(fact "gets the collection length"
  (!.js
    (xt/x:len [1 2 3]))
  => 3)

^{:refer xt.lang.common-spec/x:to-string :added "4.1"}
(fact "converts a value to a string"
  (!.js
    (xt/x:to-string 12))
  => "12")

^{:refer xt.lang.common-spec/x:to-number :added "4.1"}
(fact "converts a string to a number"
  (!.js
    (xt/x:to-number "12.5"))
  => 12.5)

^{:refer xt.lang.common-spec/x:not-nil? :added "4.1"}
(fact "checks for non-nil values"
  (!.js
    (xt/x:not-nil? 0))
  => true)

^{:refer xt.lang.common-spec/x:nil? :added "4.1"}
(fact "checks for nil values"
  (!.js
    (xt/x:nil? nil))
  => true)

^{:refer xt.lang.common-spec/x:is-string? :added "4.1"}
(fact "recognises strings"
  (!.js
    (xt/x:is-string? "abc"))
  => true)

^{:refer xt.lang.common-spec/x:is-number? :added "4.1"}
(fact "recognises numbers"
  (!.js
    (xt/x:is-number? 1.5))
  => true)

^{:refer xt.lang.common-spec/x:is-integer? :added "4.1"}
(fact "recognises integers"
  (!.js
    (xt/x:is-integer? 2))
  => true)

^{:refer xt.lang.common-spec/x:is-boolean? :added "4.1"}
(fact "recognises booleans"
  (!.js
    (xt/x:is-boolean? true))
  => true)

^{:refer xt.lang.common-spec/x:is-object? :added "4.1"}
(fact "recognises objects"
  (!.js
    (xt/x:is-object? {:a 1}))
  => true)

^{:refer xt.lang.common-spec/x:is-array? :added "4.1"}
(fact "recognises arrays"
  (!.js
    (xt/x:is-array? [1 2]))
  => true)

^{:refer xt.lang.common-spec/x:add :added "4.1"}
(fact "adds numbers"
  (!.js
    (xt/x:add 1 2 3))
  => 6)

^{:refer xt.lang.common-spec/x:sub :added "4.1"}
(fact "subtracts numbers"
  (!.js
    (xt/x:sub 10 3 2))
  => 5)

^{:refer xt.lang.common-spec/x:mul :added "4.1"}
(fact "multiplies numbers"
  (!.js
    (xt/x:mul 2 3 4))
  => 24)

^{:refer xt.lang.common-spec/x:div :added "4.1"}
(fact "divides numbers"
  (!.js
    (xt/x:div 20 5))
  => 4)

^{:refer xt.lang.common-spec/x:neg :added "4.1"}
(fact "negates a number"
  (!.js
    (xt/x:neg 2))
  => -2)

^{:refer xt.lang.common-spec/x:inc :added "4.1"}
(fact "increments a number"
  (!.js
    (xt/x:inc 2))
  => 3)

^{:refer xt.lang.common-spec/x:dec :added "4.1"}
(fact "decrements a number"
  (!.js
    (xt/x:dec 2))
  => 1)

^{:refer xt.lang.common-spec/x:zero? :added "4.1"}
(fact "checks whether a number is zero"
  (!.js
    (xt/x:zero? 0))
  => true)

^{:refer xt.lang.common-spec/x:pos? :added "4.1"}
(fact "checks whether a number is positive"
  (!.js
    (xt/x:pos? 2))
  => true)

^{:refer xt.lang.common-spec/x:neg? :added "4.1"}
(fact "checks whether a number is negative"
  (!.js
    (xt/x:neg? -2))
  => true)

^{:refer xt.lang.common-spec/x:even? :added "4.1"}
(fact "checks whether a number is even"
  (!.js
    (xt/x:even? 4))
  => true)

^{:refer xt.lang.common-spec/x:odd? :added "4.1"}
(fact "checks whether a number is odd"
  (!.js
    (xt/x:odd? 5))
  => true)

^{:refer xt.lang.common-spec/x:eq :added "4.1"}
(fact "checks equality"
  (!.js
    (xt/x:eq 2 2))
  => true)

^{:refer xt.lang.common-spec/x:neq :added "4.1"}
(fact "checks inequality"
  (!.js
    (xt/x:neq 2 3))
  => true)

^{:refer xt.lang.common-spec/x:lt :added "4.1"}
(fact "checks less than"
  (!.js
    (xt/x:lt 2 3))
  => true)

^{:refer xt.lang.common-spec/x:lte :added "4.1"}
(fact "checks less than or equal"
  (!.js
    (xt/x:lte 3 3))
  => true)

^{:refer xt.lang.common-spec/x:gt :added "4.1"}
(fact "checks greater than"
  (!.js
    (xt/x:gt 4 3))
  => true)

^{:refer xt.lang.common-spec/x:gte :added "4.1"}
(fact "checks greater than or equal"
  (!.js
    (xt/x:gte 4 4))
  => true)

^{:refer xt.lang.common-spec/x:has-key? :added "4.1"}
(fact "checks whether an object has a key"
  (!.js
    (var obj {:a 1})
    (xt/x:has-key? obj "a"))
  => true)

^{:refer xt.lang.common-spec/x:get-key :added "4.1"}
(fact "gets a value by key with a fallback"
  (!.js
    (xt/x:get-key {} "missing" "fallback"))
  => "fallback")

^{:refer xt.lang.common-spec/x:get-path :added "4.1"}
(fact "gets a nested value by path"
  (!.js
    (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
  => 2)

^{:refer xt.lang.common-spec/x:set-key :added "4.1"}
(fact "sets a key on an object"
  (!.js
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
  => {"a" 1, "c" 9})

^{:refer xt.lang.common-spec/x:obj-keys :added "4.1"}
(fact "lists object keys"
  (set
   (!.js
     (xt/x:obj-keys {:a 1 :b 2})))
  => #{"a" "b"})

^{:refer xt.lang.common-spec/x:obj-vals :added "4.1"}
(fact "lists object values"
  (set
   (!.js
     (xt/x:obj-vals {:a 1 :b 2})))
  => #{1 2})

^{:refer xt.lang.common-spec/x:obj-pairs :added "4.1"}
(fact "lists object pairs"
  (set
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
  => {"a" 1})

^{:refer xt.lang.common-spec/x:obj-assign :added "4.1"}
(fact "assigns object keys"
  (!.js
    (xt/x:obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:obj-from-pairs :added "4.1"}
(fact "creates an object from pairs"
  (!.js
    (xt/x:obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:str-len :added "4.1"}
(fact "gets the string length"
  (!.js
    (xt/x:str-len "hello"))
  => 5)

^{:refer xt.lang.common-spec/x:str-pad-left :added "4.1"}
(fact "pads a string on the left"
  (!.js
    (xt/x:str-pad-left "7" 3 "0"))
  => "007")

^{:refer xt.lang.common-spec/x:str-pad-right :added "4.1"}
(fact "pads a string on the right"
  (!.js
    (xt/x:str-pad-right "7" 3 "0"))
  => "700")

^{:refer xt.lang.common-spec/x:str-starts-with :added "4.1"}
(fact "checks the string prefix"
  (!.js
    (xt/x:str-starts-with "hello" "he"))
  => true)

^{:refer xt.lang.common-spec/x:str-ends-with :added "4.1"}
(fact "checks the string suffix"
  (!.js
    (xt/x:str-ends-with "hello" "lo"))
  => true)

^{:refer xt.lang.common-spec/x:str-char :added "4.1"}
(fact "gets the character code at an index"
  (!.js
    (xt/x:str-char "abc" 1))
  => 98)

^{:refer xt.lang.common-spec/x:str-split :added "4.1"}
(fact "splits a string"
  (!.js
    (xt/x:str-split "a/b/c" "/"))
  => ["a" "b" "c"])

^{:refer xt.lang.common-spec/x:str-join :added "4.1"}
(fact "joins string parts"

  (!.js
    (xt/x:str-join "-" ["a" "b" "c"]))
  => "a-b-c")

^{:refer xt.lang.common-spec/x:str-index-of :added "4.1"}
(fact "finds the index of a substring"
  
  (!.js
    (xt/x:str-index-of "hello/world" "/" 0))
  => 5)

^{:refer xt.lang.common-spec/x:str-substring :added "4.1"}
(fact "gets a substring"
  (!.js
    (xt/x:str-substring "hello/world" 3 8))
  => "lo/wo")

^{:refer xt.lang.common-spec/x:str-to-upper :added "4.1"}
(fact "converts a string to upper case"
  (!.js
    (xt/x:str-to-upper "hello"))
  => "HELLO")

^{:refer xt.lang.common-spec/x:str-to-lower :added "4.1"}
(fact "converts a string to lower case"
  (!.js
    (xt/x:str-to-lower "HELLO"))
  => "hello")

^{:refer xt.lang.common-spec/x:str-to-fixed :added "4.1"}
(fact "formats a number with fixed decimals"
  (!.js
    (xt/x:str-to-fixed 1.2 2))
  => "1.20")

^{:refer xt.lang.common-spec/x:arr-clone :added "4.1"}
(fact "clones an array"
  (!.js
    (var src [1 2])
    (var out (xt/x:arr-clone src))
    (xt/x:arr-push src 3)
    out)
  => [1 2])

^{:refer xt.lang.common-spec/x:arr-each :added "4.1"}
(fact "iterates each element in an array"
  (!.js
    (var out [])
    (xt/x:arr-each [1 2 3] (fn [e]
                             (xt/x:arr-push out (* e 2))))
    out)
  => [2 4 6])

^{:refer xt.lang.common-spec/x:arr-map :added "4.1"}
(fact "maps an array"
  (!.js
    (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
  => [2 4 6])

^{:refer xt.lang.common-spec/x:arr-assign :added "4.1"}
(fact "appends one array to another"
  (!.js
    (xt/x:arr-assign [1 2] [3 4]))
  => [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-concat :added "4.1"}
(fact "concatenates arrays into a new array"
  (!.js
    (var src [1 2])
    [(xt/x:arr-concat src [3 4]) src])
  => [[1 2 3 4] [1 2]])

^{:refer xt.lang.common-spec/x:arr-filter :added "4.1"}
(fact "filters an array"
  (!.js
    (xt/x:arr-filter [1 2 3 4] (fn [e] (return (xt/x:even? e)))))
  => [2 4])

^{:refer xt.lang.common-spec/x:arr-keep :added "4.1"}
(fact "keeps transformed non-nil values from an array"
  (!.js
    (xtd/arr-keep [1 2 3] (fn [e]
                             (when (xt/x:odd? e)
                               (return (* e 10))))))
  => [10 30])

^{:refer xt.lang.common-spec/x:is-function? :added "4.1"}
(fact "recognises function values"
  (!.js
    (xt/x:is-function? (fn [x] (return x))))
  => true)

^{:refer xt.lang.common-spec/x:callback :added "4.1"}
(fact "dispatches node-style callbacks through for:return"
  [(!.js
     (var out nil)
     (var success (fn [cb]
                    (cb nil "OK")))
     (xt/for:return [[ret err] (success (xt/x:callback))]
       {:success (:= out ret)
        :error   (:= out err)})
     out)
   (!.js
     (var out nil)
     (var failure (fn [cb]
                    (cb "ERR" nil)))
     (xt/for:return [[ret err] (failure (xt/x:callback))]
       {:success (:= out ret)
        :error   (:= out err)})
     out)]
  => ["OK" "ERR"])
