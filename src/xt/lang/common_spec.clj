(ns xt.lang.common-spec
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defmacro.xt ^{:style/indent 1}
  for:array
  ([[e arr] & body]
   (clojure.core/apply list 'for:array [e arr] body)))

(defmacro.xt ^{:style/indent 1}
  for:object
  ([[[k v] obj] & body]
   (clojure.core/apply list 'for:object [[k v] obj] body)))

(defmacro.xt ^{:style/indent 1}
  for:index
  ([[i [start stop step]] & body]
   (clojure.core/apply list 'for:index [i [start stop step]] body)))

(defmacro.xt ^{:style/indent 1}
  for:iter
  ([[e it] & body]
   (apply list 'for:iter [e it] body)))

(defmacro.xt ^{:style/indent 1}
  return-run
  ([[resolve reject] & body]
   (list 'x:return-run
         (clojure.core/apply list 'fn [resolve reject] body))))

(defmacro.xt ^{:style/indent 1}
  for:return
  ([[[ok err] statement] {:keys [success error final]}]
   (list 'for:return [[ok err] statement]
         {:success success
          :error error
          :final final})))

(defmacro.xt ^{:style/indent 1}
  for:try
  ([[[ok err] statement] {:keys [success error]}]
   (list 'for:try [[ok err] statement]
         {:success success
          :error error})))

(defmacro.xt ^{:style/indent 1}
  for:async
  ([[[ok err] statement] {:keys [success error finally]}]
   (list 'for:async [[ok err] statement]
         {:success success
          :error error
          :finally finally})))

(defspec.xt x:get-idx [:fn [[:xt/array :xt/any] :xt/int :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:get-idx
  ([arr idx] (list (quote x:get-idx) arr idx)) 
  ([arr idx default] (list (quote x:get-idx) arr idx default)))

(defspec.xt x:set-idx [:fn [[:xt/array :xt/any] :xt/int] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:set-idx
  ([arr idx value] (list (quote x:set-idx) arr idx value)))

(defspec.xt x:first [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:first
  ([arr] (list (quote x:first) arr)))

(defspec.xt x:second [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:second
  ([arr] (list (quote x:second) arr)))

(defspec.xt x:last [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:last
  ([arr] (list (quote x:last) arr)))

(defspec.xt x:second-last [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:second-last
  ([arr] (list (quote x:second-last) arr)))

(defspec.xt x:arr-remove [:fn [[:xt/array :xt/any] :xt/num] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-remove
  ([arr idx] (list (quote x:arr-remove) arr idx)))

(defspec.xt x:arr-push [:fn [[:xt/array :xt/any] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-push
  ([arr value] (list (quote x:arr-push) arr value)))

(defspec.xt x:arr-pop [:fn [[:xt/array :xt/any] :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-pop
  ([arr] (list (quote x:arr-pop) arr)))

(defspec.xt x:arr-push-first [:fn [[:xt/array :xt/any] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-push-first
  ([arr value] (list (quote x:arr-push-first) arr value)))

(defspec.xt x:arr-pop-first [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-pop-first
  ([arr] (list (quote x:arr-pop-first) arr)))

(defspec.xt x:arr-insert [:fn [[:xt/array :xt/any] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-insert
  ([arr idx value] (list (quote x:arr-insert) arr idx value)))

(defspec.xt x:arr-slice [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-slice
  ([arr start] (list (quote x:arr-slice) arr start)) 
  ([arr start end] (list (quote x:arr-slice) arr start end)))

(defspec.xt x:arr-reverse [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-reverse
  ([arr] (list (quote x:arr-reverse) arr)))

(defspec.xt x:del [:fn [:xt/any] :xt/unknown])

(defmacro.xt ^{:standalone true :is-template false} 
  x:del
  ([var] (list (quote x:del) var)))

(defspec.xt x:cat [:fn [:xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cat
  [x y & more] (apply list (quote x:cat) x y more))

(defspec.xt x:len [:fn [:xt/any] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:len
  ([value] (list (quote x:len) value)))

(defspec.xt x:err [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:err
  ([message] (list (quote x:err) message)))

(defspec.xt x:type-native [:fn [:xt/any] :xt/string])

(defmacro.xt ^{:standalone true :is-template true} 
  x:type-native
  ([value] (list (quote x:type-native) value)))

(defspec.xt x:offset [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset
  ([] (list (quote x:offset))) 
  ([n] (list (quote x:offset) n)))

(defspec.xt x:offset-rev [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset-rev
  ([] (list (quote x:offset-rev))) 
  ([n] (list (quote x:offset-rev) n)))

(defspec.xt x:offset-len [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset-len
  ([] (list (quote x:offset-len))) 
  ([n] (list (quote x:offset-len) n)))

(defspec.xt x:offset-rlen [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset-rlen
  ([] (list (quote x:offset-rlen))) 
  ([n] (list (quote x:offset-rlen) n)))

(defspec.xt x:lu-create [:fn [] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-create
  ([] (list (quote x:lu-create))))

(defspec.xt x:lu-eq [:fn [:xt/any :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-eq
  ([x y] (list (quote x:lu-eq) x y)))

(defspec.xt x:lu-get [:fn [:xt/any :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-get
  ([lookup key] (list (quote x:lu-get) lookup key)) )

(defspec.xt x:lu-set [:fn [:xt/any :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-set
  ([lookup key value] (list (quote x:lu-set) lookup key value)))

(defspec.xt x:lu-del [:fn [:xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-del
  ([lookup key] (list (quote x:lu-del) lookup key)))

(defspec.xt x:m-abs [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-abs
  ([value] (list (quote x:m-abs) value)))

(defspec.xt x:m-acos [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-acos
  ([value] (list (quote x:m-acos) value)))

(defspec.xt x:m-asin [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-asin
  ([value] (list (quote x:m-asin) value)))

(defspec.xt x:m-atan [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-atan
  ([value] (list (quote x:m-atan) value)))

(defspec.xt x:m-ceil [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-ceil
  ([value] (list (quote x:m-ceil) value)))

(defspec.xt x:m-cos [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-cos
  ([value] (list (quote x:m-cos) value)))

(defspec.xt x:m-cosh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-cosh
  ([value] (list (quote x:m-cosh) value)))

(defspec.xt x:m-exp [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-exp
  ([value] (list (quote x:m-exp) value)))

(defspec.xt x:m-floor [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-floor
  ([value] (list (quote x:m-floor) value)))

(defspec.xt x:m-loge [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-loge
  ([value] (list (quote x:m-loge) value)))

(defspec.xt x:m-log10 [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-log10
  ([value] (list (quote x:m-log10) value)))

(defspec.xt x:m-max [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-max
  ([x y] (list (quote x:m-max) x y)))

(defspec.xt x:m-mod [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-mod
  ([x y] (list (quote x:m-mod) x y)))

(defspec.xt x:m-min [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-min
  ([x y] (list (quote x:m-min) x y)))

(defspec.xt x:m-pow [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-pow
  ([x y] (list (quote x:m-pow) x y)))

(defspec.xt x:m-quot [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-quot
  ([x y] (list (quote x:m-quot) x y)))

(defspec.xt x:m-sin [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-sin
  ([value] (list (quote x:m-sin) value)))

(defspec.xt x:m-sinh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-sinh
  ([value] (list (quote x:m-sinh) value)))

(defspec.xt x:m-sqrt [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-sqrt
  ([value] (list (quote x:m-sqrt) value)))

(defspec.xt x:m-tan [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-tan
  ([value] (list (quote x:m-tan) value)))

(defspec.xt x:m-tanh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-tanh
  ([value] (list (quote x:m-tanh) value)))

(defspec.xt x:not-nil? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:not-nil?
  ([value] (list (quote x:not-nil?) value)))

(defspec.xt x:nil? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:nil?
  ([value] (list (quote x:nil?) value)))

(defspec.xt x:add [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:add
  [x y & more] (apply list (quote x:add) x y more))

(defspec.xt x:sub [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:sub
  [x y & more] (apply list (quote x:sub) x y more))

(defspec.xt x:mul [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:mul
  [x y & more] (apply list (quote x:mul) x y more))

(defspec.xt x:div [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:div
  [x y & more] (apply list (quote x:div) x y more))

(defspec.xt x:neg [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:neg
  ([x] (list (quote x:neg) x)))

(defspec.xt x:inc [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:inc
  ([x] (list (quote x:inc) x)))

(defspec.xt x:dec [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:dec
  ([x] (list (quote x:dec) x)))

(defspec.xt x:zero? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:zero?
  ([x] (list (quote x:zero?) x)))

(defspec.xt x:pos? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:pos?
  ([x] (list (quote x:pos?) x)))

(defspec.xt x:neg? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:neg?
  ([x] (list (quote x:neg?) x)))

(defspec.xt x:even? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:even?
  ([x] (list (quote x:even?) x)))

(defspec.xt x:odd? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:odd?
  ([x] (list (quote x:odd?) x)))

(defspec.xt x:eq [:fn [:xt/any :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:eq
  ([x y] (list (quote x:eq) x y)))

(defspec.xt x:neq [:fn [:xt/any :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:neq
  ([x y] (list (quote x:neq) x y)))

(defspec.xt x:lt [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lt
  ([x y] (list (quote x:lt) x y)))

(defspec.xt x:lte [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lte
  ([x y] (list (quote x:lte) x y)))

(defspec.xt x:gt [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:gt
  ([x y] (list (quote x:gt) x y)))

(defspec.xt x:gte [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:gte
  ([x y] (list (quote x:gte) x y)))

(defspec.xt x:has-key? [:fn [:xt/obj :xt/str :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:has-key?
  ([obj key] (list (quote x:has-key?) obj key)) 
  ([obj key check] (list (quote x:has-key?) obj key check)))

(defspec.xt x:del-key [:fn [:xt/obj :xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:del-key
  ([obj key] (list (quote x:del-key) obj key)))

(defspec.xt x:get-key [:fn [:xt/obj :xt/str :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:get-key
  ([obj key] (list (quote x:get-key) obj key)) 
  ([obj key default] (list (quote x:get-key) obj key default)))

(defspec.xt x:get-path [:fn [:xt/obj [:xt/array :xt/str] :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:get-path
  ([obj path] (list (quote x:get-path) obj path)) 
  ([obj path default] (list (quote x:get-path) obj path default)))

(defspec.xt x:set-key [:fn [:xt/obj [:xt/array :xt/str] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:set-key
  ([obj key value] (list (quote x:set-key) obj key value)))

(defspec.xt x:copy-key [:fn [:xt/obj :xt/obj :xt/str :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:copy-key
  ([dst src key] (list (quote x:copy-key) dst src key)))

(defspec.xt x:obj-keys [:fn [:xt/obj] [:xt/array :xt/str]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-keys
  ([obj] (list (quote x:obj-keys) obj)))

(defspec.xt x:obj-vals [:fn [:xt/obj] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-vals
  ([obj] (list (quote x:obj-vals) obj)))

(defspec.xt x:obj-pairs [:fn [:xt/obj] [:xt/array [:xt/tuple :xt/str :xt/any]]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-pairs
  ([obj] (list (quote x:obj-pairs) obj)))

(defspec.xt x:obj-clone [:fn [:xt/obj] :xt/obj])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-clone
  ([obj] (list (quote x:obj-clone) obj)))

(defspec.xt x:obj-assign [:fn [:xt/obj :xt/obj] :xt/obj])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-assign
  ([obj other] (list (quote x:obj-assign) obj other)))

(defspec.xt x:to-string [:fn [:xt/any] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:to-string
  ([value] (list (quote x:to-string) value)))

(defspec.xt x:to-number [:fn [:xt/any] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:to-number
  ([value] (list (quote x:to-number) value)))

(defspec.xt x:is-string? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-string?
  ([value] (list (quote x:is-string?) value)))

(defspec.xt x:is-number? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-number?
  ([value] (list (quote x:is-number?) value)))

(defspec.xt x:is-integer? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-integer?
  ([value] (list (quote x:is-integer?) value)))

(defspec.xt x:is-boolean? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-boolean?
  ([value] (list (quote x:is-boolean?) value)))

(defspec.xt x:is-object? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-object?
  ([value] (list (quote x:is-object?) value)))

(defspec.xt x:is-array? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-array?
  ([value] (list (quote x:is-array?) value)))

(defspec.xt x:print [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:print
  ([value] (list (quote x:print) value)))

(defspec.xt x:str-len [:fn [:xt/str] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-len
  ([value] (list (quote x:str-len) value)))

(defspec.xt x:str-comp [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-comp
  ([x y] (list (quote x:str-comp) x y)))

(defspec.xt x:str-lt [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-lt
  ([x y] (list (quote x:str-lt) x y)))

(defspec.xt x:str-gt [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-gt
  ([x y] (list (quote x:str-gt) x y)))

(defspec.xt x:str-pad-left [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-pad-left
  ([value len pad] (list (quote x:str-pad-left) value len pad)))

(defspec.xt x:str-pad-right [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-pad-right
  ([value len pad] (list (quote x:str-pad-right) value len pad)))

(defspec.xt x:str-starts-with [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-starts-with
  ([value prefix] (list (quote x:str-starts-with) value prefix)))

(defspec.xt x:str-ends-with [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-ends-with
  ([value suffix] (list (quote x:str-ends-with) value suffix)))

(defspec.xt x:str-char [:fn [:xt/str :xt/num] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-char
  ([value idx] (list (quote x:str-char) value idx)))

(defspec.xt x:str-split [:fn [:xt/str :xt/str] [:xt/array :xt/str]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-split
  ([value separator] (list (quote x:str-split) value separator)))

(defspec.xt x:str-join [:fn [:xt/str [:xt/array :xt/any]] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-join
  ([separator coll] (list (quote x:str-join) separator coll)))

(defspec.xt x:str-index-of [:fn [:xt/str :xt/str :xt/num] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-index-of
  ([value pattern] (list (quote x:str-index-of) value pattern)) 
  ([value pattern from] (list (quote x:str-index-of) value pattern from)))

(defspec.xt x:str-substring [:fn [:xt/str :xt/num :xt/num] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-substring
  ([value start] (list (quote x:str-substring) value start)) 
  ([value start len] (list (quote x:str-substring) value start len)))

(defspec.xt x:str-to-upper [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-to-upper
  ([value] (list (quote x:str-to-upper) value)))

(defspec.xt x:str-to-lower [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-to-lower
  ([value] (list (quote x:str-to-lower) value)))

(defspec.xt x:str-to-fixed [:fn [:xt/num :xt/int] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-to-fixed
  ([value digits] (list (quote x:str-to-fixed) value digits)))

(defspec.xt x:str-replace [:fn [:xt/str :xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-replace
  ([value match replacement] (list (quote x:str-replace) value match replacement)))

(defspec.xt x:str-trim [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-trim
  ([value] (list (quote x:str-trim) value)))

(defspec.xt x:str-trim-left [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-trim-left
  ([value] (list (quote x:str-trim-left) value)))

(defspec.xt x:str-trim-right [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-trim-right
  ([value] (list (quote x:str-trim-right) value)))

(defspec.xt x:arr-sort [:fn [[:xt/array :xt/any] :fn] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-sort
  ([arr key-fn compare-fn] (list (quote x:arr-sort) arr key-fn compare-fn)))

(defspec.xt x:arr-clone [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-clone
  ([arr] (list (quote x:arr-clone) arr)))

(defspec.xt x:arr-each [:fn [[:xt/array :xt/any]] nil])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-each
  ([arr f] (list (quote x:arr-each) arr f)))

(defspec.xt x:arr-every [:fn [[:xt/array :xt/any] :xt/fn] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-every
  ([arr pred] (list (quote x:arr-every) arr pred)))

(defspec.xt x:arr-some [:fn [[:xt/array :xt/any] :xt/fn] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-some
  ([arr pred] (list (quote x:arr-some) arr pred)))

(defspec.xt x:arr-map [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-map
  ([arr f] (list (quote x:arr-map) arr f)))

(defspec.xt x:arr-assign [:fn [[:xt/array :xt/any] :xt/any] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-assign
  ([arr value] (list (quote x:arr-assign) arr value)))

(defspec.xt x:arr-filter [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-filter
  ([arr pred] (list (quote x:arr-filter) arr pred)))

(defspec.xt x:arr-keep [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-keep
  ([arr f] (list (quote x:arr-keep) arr f)))

(defspec.xt x:arr-foldl [:fn [[:xt/array :xt/any] :xt/any :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-foldl
  ([arr init f] (list (quote x:arr-foldl) arr init f)))

(defspec.xt x:arr-foldr [:fn [[:xt/array :xt/any] :xt/any :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-foldr
  ([arr init f] (list (quote x:arr-foldr) arr init f)))

(defspec.xt x:arr-find [:fn [[:xt/array :xt/any] :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-find
  ([arr pred] (list (quote x:arr-find) arr pred)))

(defspec.xt x:is-function? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-function?
  ([value] (list (quote x:is-function?) value)))

(defspec.xt x:callback [:fn [] [:fn [:xt/any] nil]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:callback
  ([] (list (quote x:callback))))

(defspec.xt x:return-run nil)

(defmacro.xt ^{:standalone true :is-template false}
  x:return-run
  ([runner] (list (quote x:return-run) runner)))

(defspec.xt x:future-run nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:future-run
  ([thunk] (list (quote x:future-run) thunk)))

(defspec.xt x:future-then nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:future-then
  ([task on-ok] (list (quote x:future-then) task on-ok)))

(defspec.xt x:future-catch nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:future-catch
  ([task on-err] (list (quote x:future-catch) task on-err)))

(defspec.xt x:future-finally nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:future-finally
  ([task on-done] (list (quote x:future-finally) task on-done)))

(defspec.xt x:future-cancel nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:future-cancel
  ([task] (list (quote x:future-cancel) task)))

(defspec.xt x:future-status nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:future-status
  ([task] (list (quote x:future-status) task)))

(defspec.xt x:future-await nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:future-await
  ([task timeout-ms default] (list (quote x:future-await) task timeout-ms default)))

(defspec.xt x:future-from-async nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:future-from-async
  ([executor] (list (quote x:future-from-async) executor)))

(defspec.xt x:eval [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:eval
  ([value] (list (quote x:eval) value)))

(defspec.xt x:apply [:fn [:xt/fn [:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:apply
  ([f args] (list (quote x:apply) f args)))

(defspec.xt x:iter-from-obj nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-from-obj
  ([obj] (list (quote x:iter-from-obj) obj)))

(defspec.xt x:iter-from-arr nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-from-arr
  ([arr] (list (quote x:iter-from-arr) arr)))

(defspec.xt x:iter-from nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-from
  ([value] (list (quote x:iter-from) value)))

(defspec.xt x:iter-eq [:fn [:xt/any :xt/any :xt/fn] :xt/bool])

(defmacro.xt ^{:standalone true :is-template true} 
  x:iter-eq
  ([iter0 iter1 eq-fn] (list (quote x:iter-eq) iter0 iter1 eq-fn)))

(defspec.xt x:iter-null nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-null
  ([] (list (quote x:iter-null))))

(defspec.xt x:iter-next nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-next
  ([iter] (list (quote x:iter-next) iter)))

(defspec.xt x:iter-has? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-has?
  ([iter] (list (quote x:iter-has?) iter)))

(defspec.xt x:iter-native? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-native?
  ([iter] (list (quote x:iter-native?) iter)))

(defspec.xt x:return-encode [:fn [:xt/any :xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template true} 
  x:return-encode
  ([out id key] (list (quote x:return-encode) out id key)))

(defspec.xt x:return-wrap [:fn [[:fn [:xt/any] :xt/any]] [:fn [:xt/any] :xt/str]])

(defmacro.xt ^{:standalone true :is-template true} 
  x:return-wrap
  ([callbock encode-fn] (list (quote x:return-wrap) callbock encode-fn)))

(defspec.xt x:return-eval [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template true} 
  x:return-eval
  ([expr wrap-fn] (list (quote x:return-eval) expr wrap-fn)))

(defspec.xt x:bit-and [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-and
  ([x y] (list (quote x:bit-and) x y)))

(defspec.xt x:bit-or [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-or
  ([x y] (list (quote x:bit-or) x y)))

(defspec.xt x:bit-lshift [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-lshift
  ([x y] (list (quote x:bit-lshift) x y)))

(defspec.xt x:bit-rshift [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-rshift
  ([x y] (list (quote x:bit-rshift) x y)))

(defspec.xt x:bit-xor [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-xor
  ([x y] (list (quote x:bit-xor) x y)))

(defspec.xt x:global-set nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:global-set
  ([sym value] (list (quote x:global-set) sym value)))

(defspec.xt x:global-del nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:global-del
  ([sym] (list (quote x:global-del) sym)))

(defspec.xt x:global-has? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:global-has?
  ([sym] (list (quote x:global-has?) sym)))

(defspec.xt x:this nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:this
  ([] (list (quote x:this))))

(defspec.xt x:proto-get nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:proto-get
  ([obj key] (list (quote x:proto-get) obj key)))

(defspec.xt x:proto-set nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:proto-set
  ([obj key value] (list (quote x:proto-set) obj key value)))

(defspec.xt x:proto-create nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:proto-create
  ([value] (list (quote x:proto-create) value)))

(defspec.xt x:proto-tostring nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:proto-tostring
  ([value] (list (quote x:proto-tostring) value)))

(defspec.xt x:random [:fn [] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:random
  ([] (list (quote x:random))))

(defspec.xt x:throw [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:throw
  ([value] (list (quote x:throw) value)))

(defspec.xt x:now-ms [:fn [] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:now-ms
  ([] (list (quote x:now-ms))))

(defspec.xt x:unpack [:fn [[:xt/array :xt/any]] [:xt/tuple :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:unpack
  ([value] (list (quote x:unpack) value)))

(defspec.xt x:client-basic [:fn [:xt/str :xt/int :xt/fn :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:client-basic
  ([host port connect-fn eval-fn] (list (quote x:client-basic) host port connect-fn eval-fn)))

(defspec.xt x:client-ws [:fn [:xt/str :xt/int :xt/any :xt/fn :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:client-ws
  ([host port opts connect-fn eval-fn] (list (quote x:client-ws) host port opts connect-fn eval-fn)))

(defspec.xt x:server-basic nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:server-basic
  ([config] (list (quote x:server-basic) config)))

(defspec.xt x:server-ws nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:server-ws
  ([config] (list (quote x:server-ws) config)))

(defspec.xt x:socket-connect [:fn [:xt/str :xt/int :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:socket-connect
  ([host port opts cb] (list (quote x:socket-connect) host port opts cb)))

(defspec.xt x:socket-send [:fn [:xt/any :xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:socket-send
  ([conn message] (list (quote x:socket-send) conn message)))

(defspec.xt x:socket-close [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:socket-close
  ([conn] (list (quote x:socket-close) conn)))

(defspec.xt x:ws-connect [:fn [:xt/str :xt/int :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:ws-connect
  ([host port opts] (list (quote x:ws-connect) host port opts)))

(defspec.xt x:ws-send [:fn [:xt/any :xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:ws-send
  ([conn value] (list (quote x:ws-send) conn value)))

(defspec.xt x:ws-close [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:ws-close
  ([conn] (list (quote x:ws-close) conn)))

(defspec.xt x:notify-http [:fn [:xt/str :xt/num :xt/any :xt/str :xt/str :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:notify-http
  ([host port value id key encode-fn] (list (quote x:notify-http) host port value id key encode-fn)))

(defspec.xt x:notify-socket [:fn [:xt/str :xt/num :xt/any :xt/str :xt/str :xt/fn :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:notify-socket
  ([host port value id key connect-fn encode-fn] (list (quote x:notify-socket) host port value id key connect-fn encode-fn)))

(defspec.xt x:b64-encode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:b64-encode
  ([value] (list (quote x:b64-encode) value)))

(defspec.xt x:b64-decode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:b64-decode
  ([value] (list (quote x:b64-decode) value)))

(defspec.xt x:cache [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cache
  ([name] (list (quote x:cache) name)))

(defspec.xt x:cache-list [:fn [] [:xt/array :xt/str]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cache-list
  ([] (list (quote x:cache-list))))

(defspec.xt x:cache-flush [:fn [:xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cache-flush
  ([cache] (list (quote x:cache-flush) cache)))

(defspec.xt x:cache-get [:fn [:xt/any :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cache-get
  ([cache key] (list (quote x:cache-get) cache key)))

(defspec.xt x:cache-set [:fn [:xt/any :xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cache-set
  ([cache key value] (list (quote x:cache-set) cache key value)))

(defspec.xt x:cache-del [:fn [:xt/any :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cache-del
  ([cache key] (list (quote x:cache-del) cache key)))

(defspec.xt x:cache-incr [:fn [:xt/any :xt/str :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template true} 
  x:cache-incr
  ([cache key val] (list (quote x:cache-incr) cache key val)))

(defspec.xt x:slurp [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:slurp
  ([path] (list (quote x:slurp) path)))

(defspec.xt x:spit [:fn [:xt/str :xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:spit
  ([path value] (list (quote x:spit) path value)))

(defspec.xt x:json-encode [:fn [:xt/any] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:json-encode
  ([value] (list (quote x:json-encode) value)))

(defspec.xt x:json-decode [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:json-decode
  ([expr] (list (quote x:json-decode) expr)))

(defspec.xt x:shell [:fn [:xt/str :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:shell
  ([command opts] (list (quote x:shell) command opts)))

(defspec.xt x:thread-spawn [:fn [:xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:thread-spawn
  ([f] (list (quote x:thread-spawn) f)))

(defspec.xt x:thread-join [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:thread-join
  ([thread] (list (quote x:thread-join) thread)))

(defspec.xt x:with-delay [:fn [:xt/int :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:with-delay
  ([ms value] (list (quote x:with-delay) ms value)))

(defspec.xt x:start-interval [:fn [:xt/int :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:start-interval
  ([ms f] (list (quote x:start-interval) ms f)))

(defspec.xt x:stop-interval [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:stop-interval
  ([id] (list (quote x:stop-interval) id)))

(defspec.xt x:uri-encode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:uri-encode
  ([value] (list (quote x:uri-encode) value)))

(defspec.xt x:uri-decode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:uri-decode
  ([value] (list (quote x:uri-decode) value)))
