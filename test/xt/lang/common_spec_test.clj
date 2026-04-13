(ns xt.lang.common-spec-test
  (:require [std.lang :as l]
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

(^{:refer xt.lang.common-spec/for:array :added "4.1"}
 fact "iterates arrays in order"
   
   (!.lua
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
   => [1 2 3])

(^{:refer xt.lang.common-spec/for:object :added "4.1"}
 fact "iterates object key value pairs"
   
   (set
    (!.lua
     (var out [])
     (xt/for:object [[k v] {:a 1 :b 2}]
       (xt/x:arr-push out [k v]))
     out))
   => #{["a" 1] ["b" 2]})

(^{:refer xt.lang.common-spec/for:index :added "4.1"}
 fact "iterates a numeric range"
   
   (!.lua
    (var out [])
    (xt/for:index [i [0 4 2]]
      (xt/x:arr-push out i))
    out)
   => [0 2 4])

(^{:refer xt.lang.common-spec/for:return :added "4.1"}
 fact "dispatches success and error branches"
   
   [(!.lua
     (var out nil)
     (xt/for:return [[ok err] (unpack ["OK" nil])]
       {:success (:= out ok)
        :error (:= out err)})
     out)
    (!.lua
     (var out nil)
     (xt/for:return [[ok err] (unpack [nil "ERR"])]
       {:success (:= out ok)
        :error (:= out err)})
     out)]
   => ["OK" "ERR"])

(^{:refer xt.lang.common-spec/x:get-idx :added "4.1"}
 fact "reads and writes indexed values"
   
   [(!.lua
     [(xt/x:get-idx [10 20 30] (xt/x:offset 0))
      (xt/x:get-idx [10 20 30] (xt/x:offset 1))
      (xt/x:get-idx [10 20 30] 99 "fallback")])
    (!.lua
     (var out [10 20 30])
     (xt/x:set-idx out (xt/x:offset 1) 99)
     [(xt/x:first out)
      (xt/x:second out)
      (xt/x:last out)
      (xt/x:second-last out)])]
   => [[10 20 "fallback"]
       [10 99 30 99]])

(^{:refer xt.lang.common-spec/x:arr-remove :added "4.1"}
 fact "supports common array mutation helpers"
   
   (!.lua
    (var out [1 2 3])
    (xt/x:arr-push out 4)
    (xt/x:arr-push-first out 0)
    (xt/x:arr-insert out (xt/x:offset 2) 9)
    (xt/x:arr-remove out (xt/x:offset 1))
   [(xt/x:arr-pop-first out)
     (xt/x:arr-pop out)
     out
     (xt/x:arr-slice out (xt/x:offset 0) (xt/x:offset 1))
     (xt/x:arr-reverse out)])
   => [0 4 [1 2 3] [2] [3 2 1]])

(^{:refer xt.lang.common-spec/x:cat :added "4.1"}
 fact "covers basic value and type helpers"
   
   (!.lua
    [(xt/x:cat "hello" "-" "world")
     (xt/x:len [1 2 3])
     (xt/x:to-string 12)
     (xt/x:to-number "12.5")
     (xt/x:not-nil? 0)
     (xt/x:nil? nil)
     (xt/x:is-string? "abc")
     (xt/x:is-number? 1.5)
     (xt/x:is-integer? 2)
     (xt/x:is-boolean? true)
     (xt/x:is-object? {:a 1})
     (xt/x:is-array? [1 2])])
   => ["hello-world" 3 "12" 12.5 true true true true true true true true])

(^{:refer xt.lang.common-spec/x:add :added "4.1"}
 fact "wraps arithmetic and comparison helpers"
 

 (!.lua
   [(xt/x:add 1 2 3)
    (xt/x:sub 10 3 2)
    (xt/x:mul 2 3 4)
    (xt/x:div 20 5)
    (xt/x:neg 2)
    (xt/x:inc 2)
    (xt/x:dec 2)
    (xt/x:zero? 0)
    (xt/x:pos? 2)
    (xt/x:neg? -2)
    (xt/x:even? 4)
    (xt/x:odd? 5)
    (xt/x:eq 2 2)
    (xt/x:neq 2 3)
    (xt/x:lt 2 3)
    (xt/x:lte 3 3)
    (xt/x:gt 4 3)
    (xt/x:gte 4 4)])
   => [6 5 24 4 -2 3 1 true true true true true true true true true true true])

(^{:refer xt.lang.common-spec/x:has-key? :added "4.1"}
 fact "supports key and path access"
   
   [(!.lua
     (var obj {:a 1
               :nested {:b 2}})
     [(xt/x:has-key? obj "a")
      (xt/x:has-key? obj "missing")
      (xt/x:get-key obj "a")
      (xt/x:get-key obj "missing" "fallback")
      (xt/x:get-path obj ["nested" "b"])
      (xt/x:get-path obj ["nested" "c"] "fallback")])
    (!.lua
     (var out {:a 1})
     (xt/x:set-key out "b" 2)
     (xt/x:copy-key out {:a 9} ["c" "a"])
     out)]
   => [[true false 1 "fallback" 2 "fallback"]
       {"a" 1, "b" 2, "c" 9}])

(^{:refer xt.lang.common-spec/x:obj-keys :added "4.1"}
 fact "covers object conversion helpers"
   
   [(set (!.lua (xt/x:obj-keys {:a 1 :b 2})))
    (set (!.lua (xt/x:obj-vals {:a 1 :b 2})))
    (set (!.lua (xt/x:obj-pairs {:a 1 :b 2})))
    (!.lua
     (var src {:a 1})
     (var out (xt/x:obj-clone src))
     (xt/x:set-key src "b" 2)
     out)
    (!.lua (xt/x:obj-assign {:a 1} {:b 2}))
    (!.lua (xt/x:obj-from-pairs [["a" 1] ["b" 2]]))]
   => [#{"a" "b"}
       #{1 2}
       #{["a" 1] ["b" 2]}
       {"a" 1}
       {"a" 1, "b" 2}
       {"a" 1, "b" 2}])

(^{:refer xt.lang.common-spec/x:str-len :added "4.1"}
 fact "covers string helpers"
   
   (!.lua
    [(xt/x:str-len "hello")
     (xt/x:str-pad-left "7" 3 "0")
     (xt/x:str-pad-right "7" 3 "0")
     (xt/x:str-starts-with "hello" "he")
     (xt/x:str-ends-with "hello" "lo")
     (xt/x:str-char "abc" 1)
     (xt/x:str-split "a/b/c" "/")
     (xt/x:str-join "-" ["a" "b" "c"])
     (xt/x:str-index-of "hello/world" "/" 0)
     (xt/x:str-substring "hello/world" 3 8)
     (xt/x:str-to-upper "hello")
     (xt/x:str-to-lower "HELLO")
     (xt/x:str-to-fixed 1.2 2)])
   => [5 "007" "700" true true 97 ["a" "b" "c"]
       "a-b-c" 6 "llo/wo" "HELLO" "hello" "1.20"])

(^{:refer xt.lang.common-spec/x:arr-clone :added "4.1"}
 fact "covers higher-order array helpers"
   
   [(!.lua
     (var src [1 2])
     (var out (xt/x:arr-clone src))
     (xt/x:arr-push src 3)
     out)
    (!.lua
     (var out [])
     (xt/x:arr-each [1 2 3] (fn [e]
                              (xt/x:arr-push out (* e 2))))
     out)
    (!.lua
     [(xt/x:arr-map [1 2 3] (fn [e] (return (* e 2))))
      (xt/x:arr-append [1 2] [3 4])
      (xt/x:arr-filter [1 2 3 4] (fn [e] (return (xt/x:even? e))))
      (xt/x:arr-keep [1 2 3] (fn [e]
                               (when (xt/x:odd? e)
                                 (return (* e 10)))))]
     )]
   => [[1 2]
       [2 4 6]
       [[2 4 6] [1 2 3 4] [2 4] [10 30]]])

(^{:refer xt.lang.common-spec/x:is-function? :added "4.1"}
 fact "recognises function values"
   
   (!.lua
    [(xt/x:is-function? (fn [x] (return x)))
     (xt/x:is-function? 1)
     (xt/x:nil? (xt/x:callback))])
   => [true false true])


(defn expands-to-form? [form expected]
  (let [expanded (macroexpand-1 form)]
    (or (= expanded expected)
        (= expanded form))))

(^{:refer xt.lang.common-spec/for:iter :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/for:iter [e it] body)
                     '(for:iter [e it] body))
   => true)

(^{:refer xt.lang.common-spec/for:try :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/for:try [[ok err] statement] {:success success, :error error})
                     '(for:try [[ok err] statement] {:success success, :error error}))
   => true)

(^{:refer xt.lang.common-spec/for:async :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/for:async [[ok err] statement] {:success success, :error error, :finally finally})
                     '(for:async [[ok err] statement] {:success success, :error error, :finally finally}))
   => true)

(^{:refer xt.lang.common-spec/x:set-idx :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:set-idx arr idx value)
                     '(x:set-idx arr idx value))
   => true)

(^{:refer xt.lang.common-spec/x:first :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:first arr)
                     '(x:first arr))
   => true)

(^{:refer xt.lang.common-spec/x:second :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:second arr)
                     '(x:second arr))
   => true)

(^{:refer xt.lang.common-spec/x:last :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:last arr)
                     '(x:last arr))
   => true)

(^{:refer xt.lang.common-spec/x:second-last :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:second-last arr)
                     '(x:second-last arr))
   => true)

(^{:refer xt.lang.common-spec/x:arr-push :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-push arr value)
                     '(x:arr-push arr value))
   => true)

(^{:refer xt.lang.common-spec/x:arr-pop :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-pop arr)
                     '(x:arr-pop arr))
   => true)

(^{:refer xt.lang.common-spec/x:arr-push-first :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-push-first arr value)
                     '(x:arr-push-first arr value))
   => true)

(^{:refer xt.lang.common-spec/x:arr-pop-first :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-pop-first arr)
                     '(x:arr-pop-first arr))
   => true)

(^{:refer xt.lang.common-spec/x:arr-insert :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-insert arr idx value)
                     '(x:arr-insert arr idx value))
   => true)

(^{:refer xt.lang.common-spec/x:arr-slice :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-slice arr start end)
                     '(x:arr-slice arr start end))
   => true)

(^{:refer xt.lang.common-spec/x:arr-reverse :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-reverse arr)
                     '(x:arr-reverse arr))
   => true)

(^{:refer xt.lang.common-spec/x:del :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:del var)
                     '(x:del var))
   => true)

(^{:refer xt.lang.common-spec/x:len :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:len value)
                     '(x:len value))
   => true)

(^{:refer xt.lang.common-spec/x:err :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:err message)
                     '(x:err message))
   => true)

(^{:refer xt.lang.common-spec/x:type-native :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:type-native value)
                     '(x:type-native value))
   => true)

(^{:refer xt.lang.common-spec/x:offset :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:offset n)
                     '(x:offset n))
   => true)

(^{:refer xt.lang.common-spec/x:offset-rev :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:offset-rev n)
                     '(x:offset-rev n))
   => true)

(^{:refer xt.lang.common-spec/x:offset-len :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:offset-len n)
                     '(x:offset-len n))
   => true)

(^{:refer xt.lang.common-spec/x:offset-rlen :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:offset-rlen n)
                     '(x:offset-rlen n))
   => true)

(^{:refer xt.lang.common-spec/x:lu-create :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:lu-create)
                     '(x:lu-create))
   => true)

(^{:refer xt.lang.common-spec/x:lu-eq :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:lu-eq x y)
                     '(x:lu-eq x y))
   => true)

(^{:refer xt.lang.common-spec/x:lu-get :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:lu-get lookup key)
                     '(x:lu-get lookup key))
   => true)

(^{:refer xt.lang.common-spec/x:lu-set :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:lu-set lookup key value)
                     '(x:lu-set lookup key value))
   => true)

(^{:refer xt.lang.common-spec/x:lu-del :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:lu-del lookup key)
                     '(x:lu-del lookup key))
   => true)

(^{:refer xt.lang.common-spec/x:m-abs :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-abs value)
                     '(x:m-abs value))
   => true)

(^{:refer xt.lang.common-spec/x:m-acos :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-acos value)
                     '(x:m-acos value))
   => true)

(^{:refer xt.lang.common-spec/x:m-asin :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-asin value)
                     '(x:m-asin value))
   => true)

(^{:refer xt.lang.common-spec/x:m-atan :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-atan value)
                     '(x:m-atan value))
   => true)

(^{:refer xt.lang.common-spec/x:m-ceil :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-ceil value)
                     '(x:m-ceil value))
   => true)

(^{:refer xt.lang.common-spec/x:m-cos :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-cos value)
                     '(x:m-cos value))
   => true)

(^{:refer xt.lang.common-spec/x:m-cosh :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-cosh value)
                     '(x:m-cosh value))
   => true)

(^{:refer xt.lang.common-spec/x:m-exp :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-exp value)
                     '(x:m-exp value))
   => true)

(^{:refer xt.lang.common-spec/x:m-floor :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-floor value)
                     '(x:m-floor value))
   => true)

(^{:refer xt.lang.common-spec/x:m-loge :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-loge value)
                     '(x:m-loge value))
   => true)

(^{:refer xt.lang.common-spec/x:m-log10 :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-log10 value)
                     '(x:m-log10 value))
   => true)

(^{:refer xt.lang.common-spec/x:m-max :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-max x y)
                     '(x:m-max x y))
   => true)

(^{:refer xt.lang.common-spec/x:m-mod :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-mod x y)
                     '(x:m-mod x y))
   => true)

(^{:refer xt.lang.common-spec/x:m-min :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-min x y)
                     '(x:m-min x y))
   => true)

(^{:refer xt.lang.common-spec/x:m-pow :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-pow x y)
                     '(x:m-pow x y))
   => true)

(^{:refer xt.lang.common-spec/x:m-quot :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-quot x y)
                     '(x:m-quot x y))
   => true)

(^{:refer xt.lang.common-spec/x:m-sin :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-sin value)
                     '(x:m-sin value))
   => true)

(^{:refer xt.lang.common-spec/x:m-sinh :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-sinh value)
                     '(x:m-sinh value))
   => true)

(^{:refer xt.lang.common-spec/x:m-sqrt :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-sqrt value)
                     '(x:m-sqrt value))
   => true)

(^{:refer xt.lang.common-spec/x:m-tan :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-tan value)
                     '(x:m-tan value))
   => true)

(^{:refer xt.lang.common-spec/x:m-tanh :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:m-tanh value)
                     '(x:m-tanh value))
   => true)

(^{:refer xt.lang.common-spec/x:not-nil? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:not-nil? value)
                     '(x:not-nil? value))
   => true)

(^{:refer xt.lang.common-spec/x:nil? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:nil? value)
                     '(x:nil? value))
   => true)

(^{:refer xt.lang.common-spec/x:sub :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:sub x y body)
                     '(x:sub x y body))
   => true)

(^{:refer xt.lang.common-spec/x:mul :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:mul x y body)
                     '(x:mul x y body))
   => true)

(^{:refer xt.lang.common-spec/x:div :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:div x y body)
                     '(x:div x y body))
   => true)

(^{:refer xt.lang.common-spec/x:neg :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:neg x)
                     '(x:neg x))
   => true)

(^{:refer xt.lang.common-spec/x:inc :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:inc x)
                     '(x:inc x))
   => true)

(^{:refer xt.lang.common-spec/x:dec :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:dec x)
                     '(x:dec x))
   => true)

(^{:refer xt.lang.common-spec/x:zero? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:zero? x)
                     '(x:zero? x))
   => true)

(^{:refer xt.lang.common-spec/x:pos? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:pos? x)
                     '(x:pos? x))
   => true)

(^{:refer xt.lang.common-spec/x:neg? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:neg? x)
                     '(x:neg? x))
   => true)

(^{:refer xt.lang.common-spec/x:even? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:even? x)
                     '(x:even? x))
   => true)

(^{:refer xt.lang.common-spec/x:odd? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:odd? x)
                     '(x:odd? x))
   => true)

(^{:refer xt.lang.common-spec/x:eq :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:eq x y)
                     '(x:eq x y))
   => true)

(^{:refer xt.lang.common-spec/x:neq :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:neq x y)
                     '(x:neq x y))
   => true)

(^{:refer xt.lang.common-spec/x:lt :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:lt x y)
                     '(x:lt x y))
   => true)

(^{:refer xt.lang.common-spec/x:lte :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:lte x y)
                     '(x:lte x y))
   => true)

(^{:refer xt.lang.common-spec/x:gt :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:gt x y)
                     '(x:gt x y))
   => true)

(^{:refer xt.lang.common-spec/x:gte :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:gte x y)
                     '(x:gte x y))
   => true)

(^{:refer xt.lang.common-spec/x:del-key :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:del-key obj key)
                     '(x:del-key obj key))
   => true)

(^{:refer xt.lang.common-spec/x:get-key :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:get-key obj key default)
                     '(x:get-key obj key default))
   => true)

(^{:refer xt.lang.common-spec/x:get-path :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:get-path obj path default)
                     '(x:get-path obj path default))
   => true)

(^{:refer xt.lang.common-spec/x:set-key :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:set-key obj key value)
                     '(x:set-key obj key value))
   => true)

(^{:refer xt.lang.common-spec/x:copy-key :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:copy-key dst src key)
                     '(x:copy-key dst src key))
   => true)

(^{:refer xt.lang.common-spec/x:obj-vals :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:obj-vals obj)
                     '(x:obj-vals obj))
   => true)

(^{:refer xt.lang.common-spec/x:obj-pairs :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:obj-pairs obj)
                     '(x:obj-pairs obj))
   => true)

(^{:refer xt.lang.common-spec/x:obj-clone :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:obj-clone obj)
                     '(x:obj-clone obj))
   => true)

(^{:refer xt.lang.common-spec/x:obj-assign :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:obj-assign obj other)
                     '(x:obj-assign obj other))
   => true)

(^{:refer xt.lang.common-spec/x:obj-from-pairs :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:obj-from-pairs pairs)
                     '(x:obj-from-pairs pairs))
   => true)

(^{:refer xt.lang.common-spec/x:to-string :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:to-string value)
                     '(x:to-string value))
   => true)

(^{:refer xt.lang.common-spec/x:to-number :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:to-number value)
                     '(x:to-number value))
   => true)

(^{:refer xt.lang.common-spec/x:is-string? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:is-string? value)
                     '(x:is-string? value))
   => true)

(^{:refer xt.lang.common-spec/x:is-number? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:is-number? value)
                     '(x:is-number? value))
   => true)

(^{:refer xt.lang.common-spec/x:is-integer? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:is-integer? value)
                     '(x:is-integer? value))
   => true)

(^{:refer xt.lang.common-spec/x:is-boolean? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:is-boolean? value)
                     '(x:is-boolean? value))
   => true)

(^{:refer xt.lang.common-spec/x:is-object? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:is-object? value)
                     '(x:is-object? value))
   => true)

(^{:refer xt.lang.common-spec/x:is-array? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:is-array? value)
                     '(x:is-array? value))
   => true)

(^{:refer xt.lang.common-spec/x:print :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:print value)
                     '(x:print value))
   => true)

(^{:refer xt.lang.common-spec/x:str-comp :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-comp x y)
                     '(x:str-comp x y))
   => true)

(^{:refer xt.lang.common-spec/x:str-lt :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-lt x y)
                     '(x:str-lt x y))
   => true)

(^{:refer xt.lang.common-spec/x:str-gt :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-gt x y)
                     '(x:str-gt x y))
   => true)

(^{:refer xt.lang.common-spec/x:str-pad-left :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-pad-left value len pad)
                     '(x:str-pad-left value len pad))
   => true)

(^{:refer xt.lang.common-spec/x:str-pad-right :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-pad-right value len pad)
                     '(x:str-pad-right value len pad))
   => true)

(^{:refer xt.lang.common-spec/x:str-starts-with :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-starts-with value prefix)
                     '(x:str-starts-with value prefix))
   => true)

(^{:refer xt.lang.common-spec/x:str-ends-with :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-ends-with value suffix)
                     '(x:str-ends-with value suffix))
   => true)

(^{:refer xt.lang.common-spec/x:str-char :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-char value idx)
                     '(x:str-char value idx))
   => true)

(^{:refer xt.lang.common-spec/x:str-format :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-format template values)
                     '(x:str-format template values))
   => true)

(^{:refer xt.lang.common-spec/x:str-split :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-split value separator)
                     '(x:str-split value separator))
   => true)

(^{:refer xt.lang.common-spec/x:str-join :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-join separator coll)
                     '(x:str-join separator coll))
   => true)

(^{:refer xt.lang.common-spec/x:str-index-of :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-index-of value pattern from)
                     '(x:str-index-of value pattern from))
   => true)

(^{:refer xt.lang.common-spec/x:str-substring :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-substring value start len)
                     '(x:str-substring value start len))
   => true)

(^{:refer xt.lang.common-spec/x:str-to-upper :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-to-upper value)
                     '(x:str-to-upper value))
   => true)

(^{:refer xt.lang.common-spec/x:str-to-lower :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-to-lower value)
                     '(x:str-to-lower value))
   => true)

(^{:refer xt.lang.common-spec/x:str-to-fixed :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-to-fixed value digits)
                     '(x:str-to-fixed value digits))
   => true)

(^{:refer xt.lang.common-spec/x:str-replace :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-replace value match replacement)
                     '(x:str-replace value match replacement))
   => true)

(^{:refer xt.lang.common-spec/x:str-trim :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-trim value)
                     '(x:str-trim value))
   => true)

(^{:refer xt.lang.common-spec/x:str-trim-left :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-trim-left value)
                     '(x:str-trim-left value))
   => true)

(^{:refer xt.lang.common-spec/x:str-trim-right :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:str-trim-right value)
                     '(x:str-trim-right value))
   => true)

(^{:refer xt.lang.common-spec/x:arr-sort :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-sort arr key-fn compare-fn)
                     '(x:arr-sort arr key-fn compare-fn))
   => true)

(^{:refer xt.lang.common-spec/x:arr-each :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-each arr f)
                     '(x:arr-each arr f))
   => true)

(^{:refer xt.lang.common-spec/x:arr-every :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-every arr pred)
                     '(x:arr-every arr pred))
   => true)

(^{:refer xt.lang.common-spec/x:arr-some :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-some arr pred)
                     '(x:arr-some arr pred))
   => true)

(^{:refer xt.lang.common-spec/x:arr-map :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-map arr f)
                     '(x:arr-map arr f))
   => true)

(^{:refer xt.lang.common-spec/x:arr-append :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-append arr value)
                     '(x:arr-append arr value))
   => true)

(^{:refer xt.lang.common-spec/x:arr-filter :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-filter arr pred)
                     '(x:arr-filter arr pred))
   => true)

(^{:refer xt.lang.common-spec/x:arr-keep :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-keep arr f)
                     '(x:arr-keep arr f))
   => true)

(^{:refer xt.lang.common-spec/x:arr-foldl :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-foldl arr init f)
                     '(x:arr-foldl arr init f))
   => true)

(^{:refer xt.lang.common-spec/x:arr-foldr :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-foldr arr init f)
                     '(x:arr-foldr arr init f))
   => true)

(^{:refer xt.lang.common-spec/x:arr-find :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:arr-find arr pred)
                     '(x:arr-find arr pred))
   => true)

(^{:refer xt.lang.common-spec/x:callback :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:callback)
                     '(x:callback))
   => true)

(^{:refer xt.lang.common-spec/x:future-run :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-run thunk)
                     '(x:future-run thunk))
   => true)

(^{:refer xt.lang.common-spec/x:future-then :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-then task on-ok)
                     '(x:future-then task on-ok))
   => true)

(^{:refer xt.lang.common-spec/x:future-catch :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-catch task on-err)
                     '(x:future-catch task on-err))
   => true)

(^{:refer xt.lang.common-spec/x:future-finally :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-finally task on-done)
                     '(x:future-finally task on-done))
   => true)

(^{:refer xt.lang.common-spec/x:future-cancel :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-cancel task)
                     '(x:future-cancel task))
   => true)

(^{:refer xt.lang.common-spec/x:future-status :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-status task)
                     '(x:future-status task))
   => true)

(^{:refer xt.lang.common-spec/x:future-await :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-await task timeout-ms default)
                     '(x:future-await task timeout-ms default))
   => true)

(^{:refer xt.lang.common-spec/x:future-from-async :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:future-from-async executor)
                     '(x:future-from-async executor))
   => true)

(^{:refer xt.lang.common-spec/x:eval :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:eval value)
                     '(x:eval value))
   => true)

(^{:refer xt.lang.common-spec/x:apply :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:apply f args)
                     '(x:apply f args))
   => true)

(^{:refer xt.lang.common-spec/x:iter-from-obj :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-from-obj obj)
                     '(x:iter-from-obj obj))
   => true)

(^{:refer xt.lang.common-spec/x:iter-from-arr :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-from-arr arr)
                     '(x:iter-from-arr arr))
   => true)

(^{:refer xt.lang.common-spec/x:iter-from :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-from value)
                     '(x:iter-from value))
   => true)

(^{:refer xt.lang.common-spec/x:iter-eq :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-eq iter0 iter1 eq-fn)
                     '(x:iter-eq iter0 iter1 eq-fn))
   => true)

(^{:refer xt.lang.common-spec/x:iter-null :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-null)
                     '(x:iter-null))
   => true)

(^{:refer xt.lang.common-spec/x:iter-next :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-next iter)
                     '(x:iter-next iter))
   => true)

(^{:refer xt.lang.common-spec/x:iter-has? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-has? iter)
                     '(x:iter-has? iter))
   => true)

(^{:refer xt.lang.common-spec/x:iter-native? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:iter-native? iter)
                     '(x:iter-native? iter))
   => true)

(^{:refer xt.lang.common-spec/x:return-encode :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:return-encode out id key)
                     '(x:return-encode out id key))
   => true)

(^{:refer xt.lang.common-spec/x:return-wrap :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:return-wrap callbock encode-fn)
                     '(x:return-wrap callbock encode-fn))
   => true)

(^{:refer xt.lang.common-spec/x:return-eval :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:return-eval expr wrap-fn)
                     '(x:return-eval expr wrap-fn))
   => true)

(^{:refer xt.lang.common-spec/x:bit-and :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:bit-and x y)
                     '(x:bit-and x y))
   => true)

(^{:refer xt.lang.common-spec/x:bit-or :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:bit-or x y)
                     '(x:bit-or x y))
   => true)

(^{:refer xt.lang.common-spec/x:bit-lshift :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:bit-lshift x y)
                     '(x:bit-lshift x y))
   => true)

(^{:refer xt.lang.common-spec/x:bit-rshift :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:bit-rshift x y)
                     '(x:bit-rshift x y))
   => true)

(^{:refer xt.lang.common-spec/x:bit-xor :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:bit-xor x y)
                     '(x:bit-xor x y))
   => true)

(^{:refer xt.lang.common-spec/x:global-set :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:global-set sym value)
                     '(x:global-set sym value))
   => true)

(^{:refer xt.lang.common-spec/x:global-del :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:global-del sym)
                     '(x:global-del sym))
   => true)

(^{:refer xt.lang.common-spec/x:global-has? :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:global-has? sym)
                     '(x:global-has? sym))
   => true)

(^{:refer xt.lang.common-spec/x:this :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:this)
                     '(x:this))
   => true)

(^{:refer xt.lang.common-spec/x:proto-get :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:proto-get obj key)
                     '(x:proto-get obj key))
   => true)

(^{:refer xt.lang.common-spec/x:proto-set :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:proto-set obj key value)
                     '(x:proto-set obj key value))
   => true)

(^{:refer xt.lang.common-spec/x:proto-create :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:proto-create value)
                     '(x:proto-create value))
   => true)

(^{:refer xt.lang.common-spec/x:proto-tostring :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:proto-tostring value)
                     '(x:proto-tostring value))
   => true)

(^{:refer xt.lang.common-spec/x:random :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:random)
                     '(x:random))
   => true)

(^{:refer xt.lang.common-spec/x:throw :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:throw value)
                     '(x:throw value))
   => true)

(^{:refer xt.lang.common-spec/x:now-ms :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:now-ms)
                     '(x:now-ms))
   => true)

(^{:refer xt.lang.common-spec/x:unpack :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:unpack value)
                     '(x:unpack value))
   => true)

(^{:refer xt.lang.common-spec/x:client-basic :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:client-basic host port connect-fn eval-fn)
                     '(x:client-basic host port connect-fn eval-fn))
   => true)

(^{:refer xt.lang.common-spec/x:client-ws :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:client-ws host port opts connect-fn eval-fn)
                     '(x:client-ws host port opts connect-fn eval-fn))
   => true)

(^{:refer xt.lang.common-spec/x:server-basic :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:server-basic config)
                     '(x:server-basic config))
   => true)

(^{:refer xt.lang.common-spec/x:server-ws :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:server-ws config)
                     '(x:server-ws config))
   => true)

(^{:refer xt.lang.common-spec/x:socket-connect :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:socket-connect host port opts cb)
                     '(x:socket-connect host port opts cb))
   => true)

(^{:refer xt.lang.common-spec/x:socket-send :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:socket-send conn message)
                     '(x:socket-send conn message))
   => true)

(^{:refer xt.lang.common-spec/x:socket-close :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:socket-close conn)
                     '(x:socket-close conn))
   => true)

(^{:refer xt.lang.common-spec/x:ws-connect :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:ws-connect host port opts)
                     '(x:ws-connect host port opts))
   => true)

(^{:refer xt.lang.common-spec/x:ws-send :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:ws-send conn value)
                     '(x:ws-send conn value))
   => true)

(^{:refer xt.lang.common-spec/x:ws-close :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:ws-close conn)
                     '(x:ws-close conn))
   => true)

(^{:refer xt.lang.common-spec/x:notify-http :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:notify-http host port value id key encode-fn)
                     '(x:notify-http host port value id key encode-fn))
   => true)

(^{:refer xt.lang.common-spec/x:notify-socket :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:notify-socket host port value id key connect-fn encode-fn)
                     '(x:notify-socket host port value id key connect-fn encode-fn))
   => true)

(^{:refer xt.lang.common-spec/x:b64-encode :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:b64-encode value)
                     '(x:b64-encode value))
   => true)

(^{:refer xt.lang.common-spec/x:b64-decode :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:b64-decode value)
                     '(x:b64-decode value))
   => true)

(^{:refer xt.lang.common-spec/x:cache :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:cache name)
                     '(x:cache name))
   => true)

(^{:refer xt.lang.common-spec/x:cache-list :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:cache-list)
                     '(x:cache-list))
   => true)

(^{:refer xt.lang.common-spec/x:cache-flush :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:cache-flush cache)
                     '(x:cache-flush cache))
   => true)

(^{:refer xt.lang.common-spec/x:cache-get :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:cache-get cache key)
                     '(x:cache-get cache key))
   => true)

(^{:refer xt.lang.common-spec/x:cache-set :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:cache-set cache key value)
                     '(x:cache-set cache key value))
   => true)

(^{:refer xt.lang.common-spec/x:cache-del :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:cache-del cache key)
                     '(x:cache-del cache key))
   => true)

(^{:refer xt.lang.common-spec/x:cache-incr :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:cache-incr cache key val)
                     '(x:cache-incr cache key val))
   => true)

(^{:refer xt.lang.common-spec/x:slurp :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:slurp path)
                     '(x:slurp path))
   => true)

(^{:refer xt.lang.common-spec/x:spit :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:spit path value)
                     '(x:spit path value))
   => true)

(^{:refer xt.lang.common-spec/x:json-encode :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:json-encode value)
                     '(x:json-encode value))
   => true)

(^{:refer xt.lang.common-spec/x:json-decode :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:json-decode expr)
                     '(x:json-decode expr))
   => true)

(^{:refer xt.lang.common-spec/x:shell :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:shell command opts)
                     '(x:shell command opts))
   => true)

(^{:refer xt.lang.common-spec/x:thread-spawn :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:thread-spawn f)
                     '(x:thread-spawn f))
   => true)

(^{:refer xt.lang.common-spec/x:thread-join :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:thread-join thread)
                     '(x:thread-join thread))
   => true)

(^{:refer xt.lang.common-spec/x:with-delay :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:with-delay ms value)
                     '(x:with-delay ms value))
   => true)

(^{:refer xt.lang.common-spec/x:start-interval :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:start-interval ms f)
                     '(x:start-interval ms f))
   => true)

(^{:refer xt.lang.common-spec/x:stop-interval :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:stop-interval id)
                     '(x:stop-interval id))
   => true)

(^{:refer xt.lang.common-spec/x:uri-encode :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:uri-encode value)
                     '(x:uri-encode value))
   => true)

(^{:refer xt.lang.common-spec/x:uri-decode :added "4.1"}
 fact "expands to xtalk form"
   (expands-to-form? '(xt/x:uri-decode value)
                     '(x:uri-decode value))
   => true)
