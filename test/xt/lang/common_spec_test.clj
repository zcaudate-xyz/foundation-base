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

(^{:refer xt.lang.common-spec/for:iter :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/for:iter [e it] body)))
    true)
   => true)

(^{:refer xt.lang.common-spec/for:try :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/for:try [[ok err] statement] {:success success, :error error})))
    true)
   => true)

(^{:refer xt.lang.common-spec/for:async :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/for:async [[ok err] statement] {:success success, :error error, :finally finally})))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:set-idx :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:set-idx arr idx value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:first :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:first arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:second :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:second arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:last :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:last arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:second-last :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:second-last arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-push :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-push arr value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-pop :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-pop arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-push-first :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-push-first arr value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-pop-first :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-pop-first arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-insert :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-insert arr idx value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-slice :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-slice arr start end)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-reverse :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-reverse arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:del :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:del var)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:len :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:len value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:err :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:err message)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:type-native :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:type-native value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:offset :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:offset n)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:offset-rev :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:offset-rev n)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:offset-len :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:offset-len n)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:offset-rlen :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:offset-rlen n)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:lu-create :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:lu-create)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:lu-eq :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:lu-eq x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:lu-get :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:lu-get lookup key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:lu-set :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:lu-set lookup key value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:lu-del :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:lu-del lookup key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-abs :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-abs value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-acos :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-acos value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-asin :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-asin value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-atan :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-atan value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-ceil :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-ceil value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-cos :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-cos value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-cosh :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-cosh value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-exp :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-exp value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-floor :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-floor value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-loge :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-loge value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-log10 :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-log10 value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-max :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-max x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-mod :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-mod x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-min :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-min x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-pow :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-pow x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-quot :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-quot x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-sin :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-sin value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-sinh :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-sinh value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-sqrt :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-sqrt value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-tan :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-tan value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:m-tanh :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:m-tanh value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:not-nil? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:not-nil? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:nil? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:nil? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:sub :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:sub x y body)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:mul :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:mul x y body)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:div :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:div x y body)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:neg :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:neg x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:inc :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:inc x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:dec :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:dec x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:zero? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:zero? x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:pos? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:pos? x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:neg? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:neg? x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:even? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:even? x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:odd? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:odd? x)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:eq :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:eq x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:neq :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:neq x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:lt :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:lt x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:lte :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:lte x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:gt :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:gt x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:gte :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:gte x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:del-key :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:del-key obj key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:get-key :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:get-key obj key default)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:get-path :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:get-path obj path default)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:set-key :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:set-key obj key value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:copy-key :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:copy-key dst src key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:obj-vals :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:obj-vals obj)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:obj-pairs :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:obj-pairs obj)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:obj-clone :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:obj-clone obj)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:obj-assign :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:obj-assign obj other)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:obj-from-pairs :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:obj-from-pairs pairs)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:to-string :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:to-string value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:to-number :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:to-number value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:is-string? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:is-string? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:is-number? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:is-number? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:is-integer? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:is-integer? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:is-boolean? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:is-boolean? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:is-object? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:is-object? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:is-array? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:is-array? value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:print :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:print value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-comp :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-comp x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-lt :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-lt x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-gt :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-gt x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-pad-left :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-pad-left value len pad)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-pad-right :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-pad-right value len pad)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-starts-with :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-starts-with value prefix)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-ends-with :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-ends-with value suffix)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-char :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-char value idx)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-format :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-format template values)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-split :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-split value separator)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-join :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-join separator coll)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-index-of :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-index-of value pattern from)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-substring :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-substring value start len)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-to-upper :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-to-upper value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-to-lower :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-to-lower value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-to-fixed :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-to-fixed value digits)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-replace :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-replace value match replacement)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-trim :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-trim value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-trim-left :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-trim-left value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:str-trim-right :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:str-trim-right value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-sort :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-sort arr key-fn compare-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-each :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-each arr f)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-every :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-every arr pred)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-some :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-some arr pred)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-map :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-map arr f)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-append :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-append arr value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-filter :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-filter arr pred)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-keep :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-keep arr f)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-foldl :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-foldl arr init f)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-foldr :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-foldr arr init f)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:arr-find :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:arr-find arr pred)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:callback :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:callback)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-run :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-run thunk)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-then :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-then task on-ok)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-catch :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-catch task on-err)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-finally :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-finally task on-done)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-cancel :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-cancel task)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-status :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-status task)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-await :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-await task timeout-ms default)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:future-from-async :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:future-from-async executor)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:eval :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:eval value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:apply :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:apply f args)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-from-obj :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-from-obj obj)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-from-arr :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-from-arr arr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-from :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-from value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-eq :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-eq iter0 iter1 eq-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-null :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-null)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-next :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-next iter)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-has? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-has? iter)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:iter-native? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:iter-native? iter)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:return-encode :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:return-encode out id key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:return-wrap :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:return-wrap callbock encode-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:return-eval :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:return-eval expr wrap-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:bit-and :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:bit-and x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:bit-or :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:bit-or x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:bit-lshift :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:bit-lshift x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:bit-rshift :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:bit-rshift x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:bit-xor :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:bit-xor x y)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:global-set :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:global-set sym value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:global-del :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:global-del sym)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:global-has? :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:global-has? sym)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:this :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:this)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:proto-get :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:proto-get obj key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:proto-set :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:proto-set obj key value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:proto-create :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:proto-create value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:proto-tostring :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:proto-tostring value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:random :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:random)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:throw :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:throw value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:now-ms :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:now-ms)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:unpack :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:unpack value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:client-basic :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:client-basic host port connect-fn eval-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:client-ws :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:client-ws host port opts connect-fn eval-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:server-basic :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:server-basic config)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:server-ws :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:server-ws config)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:socket-connect :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:socket-connect host port opts cb)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:socket-send :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:socket-send conn message)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:socket-close :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:socket-close conn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:ws-connect :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:ws-connect host port opts)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:ws-send :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:ws-send conn value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:ws-close :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:ws-close conn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:notify-http :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:notify-http host port value id key encode-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:notify-socket :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:notify-socket host port value id key connect-fn encode-fn)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:b64-encode :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:b64-encode value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:b64-decode :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:b64-decode value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:cache :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:cache name)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:cache-list :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:cache-list)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:cache-flush :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:cache-flush cache)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:cache-get :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:cache-get cache key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:cache-set :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:cache-set cache key value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:cache-del :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:cache-del cache key)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:cache-incr :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:cache-incr cache key val)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:slurp :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:slurp path)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:spit :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:spit path value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:json-encode :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:json-encode value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:json-decode :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:json-decode expr)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:shell :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:shell command opts)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:thread-spawn :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:thread-spawn f)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:thread-join :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:thread-join thread)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:with-delay :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:with-delay ms value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:start-interval :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:start-interval ms f)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:stop-interval :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:stop-interval id)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:uri-encode :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:uri-encode value)))
    true)
   => true)

(^{:refer xt.lang.common-spec/x:uri-decode :added "4.1"}
 fact "compiles in lua runtime"
   ^{:hidden true}
   (!.lua
    (var _ (fn [] (xt/x:uri-decode value)))
    true)
   => true)


^{:refer xt.lang.common-spec/for:array :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:object :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:index :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:iter :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:return :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:try :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:async :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:get-idx :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:set-idx :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:first :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:second :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:last :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:second-last :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-remove :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-push :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-pop :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-push-first :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-pop-first :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-insert :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-slice :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-reverse :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cat :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:len :added "4.1"}
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

^{:refer xt.lang.common-spec/x:not-nil? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:nil? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:add :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:sub :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:mul :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:div :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:neg :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:inc :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:dec :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:zero? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:pos? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:neg? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:even? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:odd? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:eq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:neq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lte :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:gt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:gte :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:has-key? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:del-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:get-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:get-path :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:set-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:copy-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-keys :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-vals :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-pairs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-clone :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-assign :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-from-pairs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:to-string :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:to-number :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-string? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-number? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-integer? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-boolean? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-object? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-array? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:print :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-len :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-comp :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-lt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-gt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-pad-left :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-pad-right :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-starts-with :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-ends-with :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-char :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-format :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-split :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-join :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-index-of :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-substring :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-to-upper :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-to-lower :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-to-fixed :added "4.1"}
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

^{:refer xt.lang.common-spec/x:arr-clone :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-each :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-every :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-some :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-map :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-append :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-filter :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-keep :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-foldl :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-foldr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-find :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-function? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:callback :added "4.1"}
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