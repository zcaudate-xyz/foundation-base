(ns xt.lang.common-spec-test
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua
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
  
  (!.lua
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
   (!.lua
     (var out [])
     (xt/for:object [[k v] {:a 1 :b 2}]
       (xt/x:arr-push out [k v]))
     out))
  => #{["a" 1] ["b" 2]})

^{:refer xt.lang.common-spec/for:index :added "4.1"}
(fact "iterates a numeric range"
  
  (!.lua
    (var out [])
    (xt/for:index [i [0 (xt/x:offset-rlen 4) 2]]
      (xt/x:arr-push out i))
    out)
  => [0 2])

^{:refer xt.lang.common-spec/for:return :added "4.1"}
(fact "dispatches success and error branches"
  
  [(!.lua
     (var out nil)
     (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                 (resolve "OK"))]
       {:success (:= out ok)
        :error (:= out err)})
     out)
   (!.lua
     (var out nil)
     (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                 (reject "ERR"))]
       {:success (:= out ok)
        :error (:= out err)})
     out)]
  => ["OK" "ERR"])

^{:refer xt.lang.common-spec/return-run :added "4.1"}
(fact "normalises success and error callbacks"
  (!.lua
    (var out nil)
    (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                (resolve "OK"))]
      {:success (:= out ok)
       :error (:= out err)})
    out)
  => "OK")

^{:refer xt.lang.common-spec/return-run :added "4.1"}
(fact "can compute a value before resolving"
  (!.lua
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
  [(!.lua
     (var out nil)
     (var value 5)
     (xt/for:return [[ok err] (xt/return-run [resolve reject]
                                 (if (< value 10)
                                   (reject "small")
                                   (resolve value)))]
       {:success (:= out ok)
        :error (:= out err)})
     out)
   (!.lua
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
  (!.lua
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
  (!.lua
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
  (!.lua
    (xt/x:get-idx [10 20 30] (xt/x:offset 0)))
  => 10)

^{:refer xt.lang.common-spec/x:set-idx :added "4.1"}
(fact "writes an indexed value"
  (!.lua
    (var out [10 20 30])
    (xt/x:set-idx out (xt/x:offset 1) 99)
    out)
  => [10 99 30])

^{:refer xt.lang.common-spec/x:first :added "4.1"}
(fact "gets the first array element"
  (!.lua
    (xt/x:first [10 20 30]))
  => 10)

^{:refer xt.lang.common-spec/x:second :added "4.1"}
(fact "gets the second array element"
  (!.lua
    (xt/x:second [10 20 30]))
  => 20)

^{:refer xt.lang.common-spec/x:last :added "4.1"}
(fact "gets the last array element"
  (!.lua
    (xt/x:last [10 20 30]))
  => 30)

^{:refer xt.lang.common-spec/x:second-last :added "4.1"}
(fact "gets the element before the last"
  (!.lua
    (xt/x:second-last [10 20 30]))
  => 20)

^{:refer xt.lang.common-spec/x:arr-remove :added "4.1"}
(fact "removes an element from an array"
  (!.lua
    (var out [0 1 2 3])
    (xt/x:arr-remove out (xt/x:offset 1))
    out)
  => [0 1 3])

^{:refer xt.lang.common-spec/x:arr-push :added "4.1"}
(fact "pushes an element onto an array"
  (!.lua
    (var out [1 2 3])
    (xt/x:arr-push out 4)
    out)
  => [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-pop :added "4.1"}
(fact "pops the last element from an array"
  (!.lua
    (var out [1 2 3 4])
    (xt/x:arr-pop out))
  => 4)

^{:refer xt.lang.common-spec/x:arr-push-first :added "4.1"}
(fact "pushes an element to the front of an array"
  (!.lua
    (var out [1 2 3])
    (xt/x:arr-push-first out 0)
    out)
  => [0 1 2 3])

^{:refer xt.lang.common-spec/x:arr-pop-first :added "4.1"}
(fact "pops the first element from an array"
  (!.lua
    (var out [0 1 2 3])
    (xt/x:arr-pop-first out))
  => 0)

^{:refer xt.lang.common-spec/x:arr-insert :added "4.1"}
(fact "inserts an element into an array"
  (!.lua
    (var out [1 2 3])
    (xt/x:arr-insert out (xt/x:offset 1) 9)
    out)
  => [1 9 2 3])

^{:refer xt.lang.common-spec/x:arr-slice :added "4.1"}
(fact "slices a range from an array"
  (!.lua
    (xt/x:arr-slice [1 2 3] (xt/x:offset 0) (xt/x:offset 1)))
  => [2])

^{:refer xt.lang.common-spec/x:arr-reverse :added "4.1"}
(fact "reverses an array"
  (!.lua
    (xt/x:arr-reverse [1 2 3]))
  => [3 2 1])

^{:refer xt.lang.common-spec/x:cat :added "4.1"}
(fact "concatenates strings"
  (!.lua
    (xt/x:cat "hello" "-" "world"))
  => "hello-world")

^{:refer xt.lang.common-spec/x:len :added "4.1"}
(fact "gets the collection length"
  (!.lua
    (xt/x:len [1 2 3]))
  => 3)

^{:refer xt.lang.common-spec/x:to-string :added "4.1"}
(fact "converts a value to a string"
  (!.lua
    (xt/x:to-string 12))
  => "12")

^{:refer xt.lang.common-spec/x:to-number :added "4.1"}
(fact "converts a string to a number"
  (!.lua
    (xt/x:to-number "12.5"))
  => 12.5)

^{:refer xt.lang.common-spec/x:not-nil? :added "4.1"}
(fact "checks for non-nil values"
  (!.lua
    (xt/x:not-nil? 0))
  => true)

^{:refer xt.lang.common-spec/x:nil? :added "4.1"}
(fact "checks for nil values"
  (!.lua
    (xt/x:nil? nil))
  => true)

^{:refer xt.lang.common-spec/x:is-string? :added "4.1"}
(fact "recognises strings"
  (!.lua
    (xt/x:is-string? "abc"))
  => true)

^{:refer xt.lang.common-spec/x:is-number? :added "4.1"}
(fact "recognises numbers"
  (!.lua
    (xt/x:is-number? 1.5))
  => true)

^{:refer xt.lang.common-spec/x:is-integer? :added "4.1"}
(fact "recognises integers"
  (!.lua
    (xt/x:is-integer? 2))
  => true)

^{:refer xt.lang.common-spec/x:is-boolean? :added "4.1"}
(fact "recognises booleans"
  (!.lua
    (xt/x:is-boolean? true))
  => true)

^{:refer xt.lang.common-spec/x:is-object? :added "4.1"}
(fact "recognises objects"
  (!.lua
    (xt/x:is-object? {:a 1}))
  => true)

^{:refer xt.lang.common-spec/x:is-array? :added "4.1"}
(fact "recognises arrays"
  (!.lua
    (xt/x:is-array? [1 2]))
  => true)

^{:refer xt.lang.common-spec/x:add :added "4.1"}
(fact "adds numbers"
  (!.lua
    (xt/x:add 1 2 3))
  => 6)

^{:refer xt.lang.common-spec/x:sub :added "4.1"}
(fact "subtracts numbers"
  (!.lua
    (xt/x:sub 10 3 2))
  => 5)

^{:refer xt.lang.common-spec/x:mul :added "4.1"}
(fact "multiplies numbers"
  (!.lua
    (xt/x:mul 2 3 4))
  => 24)

^{:refer xt.lang.common-spec/x:div :added "4.1"}
(fact "divides numbers"
  (!.lua
    (xt/x:div 20 5))
  => 4)

^{:refer xt.lang.common-spec/x:neg :added "4.1"}
(fact "negates a number"
  (!.lua
    (xt/x:neg 2))
  => -2)

^{:refer xt.lang.common-spec/x:inc :added "4.1"}
(fact "increments a number"
  (!.lua
    (xt/x:inc 2))
  => 3)

^{:refer xt.lang.common-spec/x:dec :added "4.1"}
(fact "decrements a number"
  (!.lua
    (xt/x:dec 2))
  => 1)

^{:refer xt.lang.common-spec/x:zero? :added "4.1"}
(fact "checks whether a number is zero"
  (!.lua
    (xt/x:zero? 0))
  => true)

^{:refer xt.lang.common-spec/x:pos? :added "4.1"}
(fact "checks whether a number is positive"
  (!.lua
    (xt/x:pos? 2))
  => true)

^{:refer xt.lang.common-spec/x:neg? :added "4.1"}
(fact "checks whether a number is negative"
  (!.lua
    (xt/x:neg? -2))
  => true)

^{:refer xt.lang.common-spec/x:even? :added "4.1"}
(fact "checks whether a number is even"
  (!.lua
    (xt/x:even? 4))
  => true)

^{:refer xt.lang.common-spec/x:odd? :added "4.1"}
(fact "checks whether a number is odd"
  (!.lua
    (xt/x:odd? 5))
  => true)

^{:refer xt.lang.common-spec/x:eq :added "4.1"}
(fact "checks equality"
  (!.lua
    (xt/x:eq 2 2))
  => true)

^{:refer xt.lang.common-spec/x:neq :added "4.1"}
(fact "checks inequality"
  (!.lua
    (xt/x:neq 2 3))
  => true)

^{:refer xt.lang.common-spec/x:lt :added "4.1"}
(fact "checks less than"
  (!.lua
    (xt/x:lt 2 3))
  => true)

^{:refer xt.lang.common-spec/x:lte :added "4.1"}
(fact "checks less than or equal"
  (!.lua
    (xt/x:lte 3 3))
  => true)

^{:refer xt.lang.common-spec/x:gt :added "4.1"}
(fact "checks greater than"
  (!.lua
    (xt/x:gt 4 3))
  => true)

^{:refer xt.lang.common-spec/x:gte :added "4.1"}
(fact "checks greater than or equal"
  (!.lua
    (xt/x:gte 4 4))
  => true)

^{:refer xt.lang.common-spec/x:has-key? :added "4.1"}
(fact "checks whether an object has a key"
  (!.lua
    (var obj {:a 1})
    (xt/x:has-key? obj "a"))
  => true)

^{:refer xt.lang.common-spec/x:get-key :added "4.1"}
(fact "gets a value by key with a fallback"
  (!.lua
    (xt/x:get-key {} "missing" "fallback"))
  => "fallback")

^{:refer xt.lang.common-spec/x:get-path :added "4.1"}
(fact "gets a nested value by path"
  (!.lua
    (xt/x:get-path {:nested {:b 2}} ["nested" "b"]))
  => 2)

^{:refer xt.lang.common-spec/x:set-key :added "4.1"}
(fact "sets a key on an object"
  (!.lua
    (var out {:a 1})
    (xt/x:set-key out "b" 2)
    out)
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:copy-key :added "4.1"}
(fact "copies a key from another object"
  (!.lua
    (var out {:a 1})
    (xt/x:copy-key out {:a 9} ["c" "a"])
    out)
  => {"a" 1, "c" 9})

^{:refer xt.lang.common-spec/x:obj-keys :added "4.1"}
(fact "lists object keys"
  (set
   (!.lua
     (xt/x:obj-keys {:a 1 :b 2})))
  => #{"a" "b"})

^{:refer xt.lang.common-spec/x:obj-vals :added "4.1"}
(fact "lists object values"
  (set
   (!.lua
     (xt/x:obj-vals {:a 1 :b 2})))
  => #{1 2})

^{:refer xt.lang.common-spec/x:obj-pairs :added "4.1"}
(fact "lists object pairs"
  (set
   (!.lua
     (xt/x:obj-pairs {:a 1 :b 2})))
  => #{["a" 1] ["b" 2]})

^{:refer xt.lang.common-spec/x:obj-clone :added "4.1"}
(fact "clones an object"
  (!.lua
    (var src {:a 1})
    (var out (xt/x:obj-clone src))
    (xt/x:set-key src "b" 2)
    out)
  => {"a" 1})

^{:refer xt.lang.common-spec/x:obj-assign :added "4.1"}
(fact "assigns object keys"
  (!.lua
    (xt/x:obj-assign {:a 1} {:b 2}))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:obj-from-pairs :added "4.1"}
(fact "creates an object from pairs"
  (!.lua
    (xt/x:obj-from-pairs [["a" 1] ["b" 2]]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.common-spec/x:str-len :added "4.1"}
(fact "gets the string length"
  (!.lua
    (xt/x:str-len "hello"))
  => 5)

^{:refer xt.lang.common-spec/x:str-pad-left :added "4.1"}
(fact "pads a string on the left"
  (!.lua
    (xt/x:str-pad-left "7" 3 "0"))
  => "007")

^{:refer xt.lang.common-spec/x:str-pad-right :added "4.1"}
(fact "pads a string on the right"
  (!.lua
    (xt/x:str-pad-right "7" 3 "0"))
  => "700")

^{:refer xt.lang.common-spec/x:str-starts-with :added "4.1"}
(fact "checks the string prefix"
  (!.lua
    (xt/x:str-starts-with "hello" "he"))
  => true)

^{:refer xt.lang.common-spec/x:str-ends-with :added "4.1"}
(fact "checks the string suffix"
  (!.lua
    (xt/x:str-ends-with "hello" "lo"))
  => true)

^{:refer xt.lang.common-spec/x:str-char :added "4.1"}
(fact "gets the character code at an index"
  (!.lua
    (xt/x:str-char "abc" 1))
  => 97)

^{:refer xt.lang.common-spec/x:str-split :added "4.1"}
(fact "splits a string"
  (!.lua
    (xt/x:str-split "a/b/c" "/"))
  => ["a" "b" "c"])

^{:refer xt.lang.common-spec/x:str-join :added "4.1"}
(fact "joins string parts"
  (!.lua
    (xt/x:str-join "-" ["a" "b" "c"]))
  => "a-b-c")

^{:refer xt.lang.common-spec/x:str-index-of :added "4.1"}
(fact "finds the index of a substring"
  (!.lua
    (xt/x:str-index-of "hello/world" "/" 0))
  => 6)

^{:refer xt.lang.common-spec/x:str-substring :added "4.1"}
(fact "gets a substring"
  (!.lua
    (xt/x:str-substring "hello/world" 3 8))
  => "llo/wo")

^{:refer xt.lang.common-spec/x:str-to-upper :added "4.1"}
(fact "converts a string to upper case"
  (!.lua
    (xt/x:str-to-upper "hello"))
  => "HELLO")

^{:refer xt.lang.common-spec/x:str-to-lower :added "4.1"}
(fact "converts a string to lower case"
  (!.lua
    (xt/x:str-to-lower "HELLO"))
  => "hello")

^{:refer xt.lang.common-spec/x:str-to-fixed :added "4.1"}
(fact "formats a number with fixed decimals"
  (!.lua
    (xt/x:str-to-fixed 1.2 2))
  => "1.20")

^{:refer xt.lang.common-spec/x:arr-clone :added "4.1"}
(fact "clones an array"
  (!.lua
    (var src [1 2])
    (var out (xt/x:arr-clone src))
    (xt/x:arr-push src 3)
    out)
  => [1 2])

^{:refer xt.lang.common-spec/x:arr-each :added "4.1"}
(fact "iterates each element in an array"
  (!.lua
    (var out [])
    (xt/x:arr-each [1 2 3] (fn [e]
                             (xt/x:arr-push out (* e 2))))
    out)
  => [2 4 6])

^{:refer xt.lang.common-spec/x:arr-map :added "4.1"}
(fact "maps an array"
  (!.lua
    (xt/x:arr-map [1 2 3] (fn [e] (return (* e 2)))))
  => [2 4 6])

^{:refer xt.lang.common-spec/x:arr-append :added "4.1"}
(fact "appends one array to another"
  (!.lua
    (xt/x:arr-append [1 2] [3 4]))
  => [1 2 3 4])

^{:refer xt.lang.common-spec/x:arr-filter :added "4.1"}
(fact "filters an array"
  (!.lua
    (xt/x:arr-filter [1 2 3 4] (fn [e] (return (xt/x:even? e)))))
  => [2 4])

^{:refer xt.lang.common-spec/x:arr-keep :added "4.1"}
(fact "keeps transformed non-nil values from an array"
  (!.lua
    (xt/x:arr-keep [1 2 3] (fn [e]
                             (when (xt/x:odd? e)
                               (return (* e 10))))))
  => [10 30])

^{:refer xt.lang.common-spec/x:is-function? :added "4.1"}
(fact "recognises function values"
  (!.lua
    (xt/x:is-function? (fn [x] (return x))))
  => true)

^{:refer xt.lang.common-spec/x:callback :added "4.1"}
(fact "returns the empty callback token in lua"
  (!.lua
    (xt/x:nil? (xt/x:callback)))
  => true)


(def ^:private +common-spec-control-forms+
  '#{for:array for:object for:index for:iter
     return-run for:return for:try for:async})

(defn- common-spec-defs [kind]
  (let [text (slurp "src/xt/lang/common_spec.clj")
        pattern (case kind
                  :macro #"\(defmacro\.xt[^\n]*\n\s+([^\s\)]+)"
                  :spec  #"\(defspec\.xt\s+([^\s\)]+)")]
    (->> (re-seq pattern text)
         (map second)
         (map symbol)
         (sort)
         (vec))))

(defn- common-spec-publics []
  (->> (ns-publics 'xt.lang.common-spec)
       keys
       sort
       vec))

;; Macro Smoke Coverage

(fact "keeps source macros and public wrappers in sync"
  (let [macros (common-spec-defs :macro)
        specs  (common-spec-defs :spec)
        publics (common-spec-publics)]
    [(count macros)
     (count specs)
     (set macros)
     (set publics)
     (set/difference (set macros) (set specs))])
  => [205
      197
      (set (common-spec-publics))
      (set (common-spec-publics))
      +common-spec-control-forms+])

(fact "all public wrappers expose arglists metadata"
  (->> (ns-publics 'xt.lang.common-spec)
       (keep (fn [[sym var]]
               (when-not (:arglists (meta var))
                 sym)))
       vec)
  => [])

(fact "all non-control wrappers keep matching defspec contracts"
  (let [macros (set (common-spec-defs :macro))
        specs  (set (common-spec-defs :spec))]
    [(set/difference specs macros)
     (set/difference (set/difference macros +common-spec-control-forms+) specs)])
  => [#{} #{}])

(fact "the lua test namespace imports the shared helper modules used by common-spec"
  (let [text (slurp "test/xt/lang/common_spec_test.clj")]
    (mapv #(boolean (re-find (re-pattern %) text))
          ["\\[xt\\.lang\\.common-spec :as xt\\]"
           "\\[xt\\.lang\\.common-data :as xtd\\]"
           "\\[xt\\.lang\\.common-repl :as repl\\]"
           "\\[xt\\.lang\\.common-string :as xts\\]"]))
  => [true true true true])


^{:refer xt.lang.common-spec/for:iter :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:try :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:async :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:err :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:type-native :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset-rev :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset-len :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset-rlen :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-create :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-eq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-get :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-abs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-acos :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-asin :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-atan :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-ceil :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-cos :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-cosh :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-exp :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-floor :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-loge :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-log10 :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-max :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-mod :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-min :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-pow :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-quot :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-sin :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-sinh :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-sqrt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-tan :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-tanh :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:del-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:print :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-comp :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-lt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-gt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-format :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-replace :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-trim :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-trim-left :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-trim-right :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-sort :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-every :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-some :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-foldl :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-foldr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-find :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-run :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-then :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-catch :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-finally :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-cancel :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-status :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-await :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-from-async :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:eval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:apply :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-from-obj :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-from-arr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-from :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-eq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-null :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-next :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-has? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-native? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:return-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:return-wrap :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:return-eval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-and :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-or :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-lshift :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-rshift :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-xor :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:global-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:global-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:global-has? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:this :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-get :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-create :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-tostring :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:random :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:throw :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:now-ms :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:unpack :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:client-basic :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:client-ws :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:server-basic :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:server-ws :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:socket-connect :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:socket-send :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:socket-close :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:ws-connect :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:ws-send :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:ws-close :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:notify-http :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:notify-socket :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:b64-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:b64-decode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-list :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-flush :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-get :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-incr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:slurp :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:spit :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:json-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:json-decode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:shell :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:thread-spawn :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:thread-join :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:with-delay :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:start-interval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:stop-interval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:uri-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:uri-decode :added "4.1"}
(fact "TODO")