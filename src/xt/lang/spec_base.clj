(ns xt.lang.spec-base
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defmacro.xt ^{:style/indent 1}
  for:array
  "iterates arrays in order"
  {:added "4.1"}
  ([[e arr] & body]
   (clojure.core/apply list 'for:array [e arr] body)))

(defmacro.xt ^{:style/indent 1}
  for:object
  "iterates object key value pairs"
  {:added "4.1"}
  ([[[k v] obj] & body]
   (clojure.core/apply list 'for:object [[k v] obj] body)))

(defmacro.xt ^{:style/indent 1}
  for:index
  "iterates a numeric range"
  {:added "4.1"}
  ([[i [start stop step]] & body]
   (clojure.core/apply list 'for:index [i [start stop step]] body)))

(defmacro.xt ^{:style/indent 1}
  for:iter
  "expands to the canonical iterator form"
  {:added "4.1"}
  ([[e it] & body]
   (apply list 'for:iter [e it] body)))

(defmacro.xt ^{:style/indent 1}
  return-run
  "supports final returns through for:return"
  {:added "4.1"}
  ([[resolve reject] & body]
   (list 'x:return-run
         (clojure.core/apply list 'fn [resolve reject] body))))

(defmacro.xt ^{:style/indent 1}
  for:return
  "dispatches success and error branches"
  {:added "4.1"}
  ([[[ok err] statement] {:keys [success error final]}]
   (list 'for:return [[ok err] statement]
         {:success success
          :error error
          :final final})))

(defmacro.xt ^{:style/indent 1}
  for:try
  "expands to the canonical try form"
  {:added "4.1"}
  ([[[ok err] statement] {:keys [success error]}]
   (list 'for:try [[ok err] statement]
         {:success success
          :error error})))

(defmacro.xt ^{:style/indent 1}
  for:async
  "expands to the canonical async form"
  {:added "4.1"}
  ([[[ok err] statement] {:keys [success error finally]}]
   (list 'for:async [[ok err] statement]
         {:success success
          :error error
          :finally finally})))

(defspec.xt x:get-idx [:fn [[:xt/array :xt/any] :xt/int :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:get-idx
  "reads the first indexed value"
  {:added "4.1"}
  ([arr idx] (list (quote x:get-idx) arr idx)) 
  ([arr idx default] (list (quote x:get-idx) arr idx default)))

(defspec.xt x:set-idx [:fn [[:xt/array :xt/any] :xt/int] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:set-idx
  "writes an indexed value"
  {:added "4.1"}
  ([arr idx value] (list (quote x:set-idx) arr idx value)))

(defspec.xt x:first [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:first
  "gets the first array element"
  {:added "4.1"}
  ([arr] (list (quote x:first) arr)))

(defspec.xt x:second [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:second
  "gets the second array element"
  {:added "4.1"}
  ([arr] (list (quote x:second) arr)))

(defspec.xt x:last [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:last
  "gets the last array element"
  {:added "4.1"}
  ([arr] (list (quote x:last) arr)))

(defspec.xt x:second-last [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:second-last
  "gets the element before the last"
  {:added "4.1"}
  ([arr] (list (quote x:second-last) arr)))

(defspec.xt x:arr-remove [:fn [[:xt/array :xt/any] :xt/num] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-remove
  "removes an element from an array"
  {:added "4.1"}
  ([arr idx] (list (quote x:arr-remove) arr idx)))

(defspec.xt x:arr-push [:fn [[:xt/array :xt/any] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-push
  "pushes an element onto an array"
  {:added "4.1"}
  ([arr value] (list (quote x:arr-push) arr value)))

(defspec.xt x:arr-pop [:fn [[:xt/array :xt/any] :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-pop
  "pops the last element from an array"
  {:added "4.1"}
  ([arr] (list (quote x:arr-pop) arr)))

(defspec.xt x:arr-push-first [:fn [[:xt/array :xt/any] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-push-first
  "pushes an element to the front of an array"
  {:added "4.1"}
  ([arr value] (list (quote x:arr-push-first) arr value)))

(defspec.xt x:arr-pop-first [:fn [[:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-pop-first
  "pops the first element from an array"
  {:added "4.1"}
  ([arr] (list (quote x:arr-pop-first) arr)))

(defspec.xt x:arr-insert [:fn [[:xt/array :xt/any] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-insert
  "inserts an element into an array"
  {:added "4.1"}
  ([arr idx value] (list (quote x:arr-insert) arr idx value)))

(defspec.xt x:arr-slice [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-slice
  "slices a range from an array"
  {:added "4.1"}
  ([arr start] (list (quote x:arr-slice) arr start)) 
  ([arr start end] (list (quote x:arr-slice) arr start end)))

(defspec.xt x:arr-reverse [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-reverse
  "reverses an array"
  {:added "4.1"}
  ([arr] (list (quote x:arr-reverse) arr)))

(defspec.xt x:del [:fn [:xt/any] :xt/unknown])

(defmacro.xt ^{:standalone true :is-template false} 
  x:del
  "expands and emits a lua delete form"
  {:added "4.1"}
  ([var] (list (quote x:del) var)))

(defspec.xt x:cat [:fn [:xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:cat
  "concatenates strings"
  {:added "4.1"}
  [x y & more] (apply list (quote x:cat) x y more))

(defspec.xt x:len [:fn [:xt/any] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:len
  "gets the collection length"
  {:added "4.1"}
  ([value] (list (quote x:len) value)))

(defspec.xt x:err [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:err
  "expands and emits a lua error form"
  {:added "4.1"}
  ([message] (list (quote x:err) message)))

(defspec.xt x:type-native [:fn [:xt/any] :xt/string])

(defmacro.xt ^{:standalone true :is-template true} 
  x:type-native
  "expands and emits the lua type helper"
  {:added "4.1"}
  ([value] (list (quote x:type-native) value)))

(defspec.xt x:offset [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset
  "uses the grammar base offset"
  {:added "4.1"}
  ([] (list (quote x:offset))) 
  ([n] (list (quote x:offset) n)))

(defspec.xt x:offset-rev [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset-rev
  "uses the reverse grammar offset"
  {:added "4.1"}
  ([] (list (quote x:offset-rev))) 
  ([n] (list (quote x:offset-rev) n)))

(defspec.xt x:offset-len [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset-len
  "uses the length grammar offset"
  {:added "4.1"}
  ([] (list (quote x:offset-len))) 
  ([n] (list (quote x:offset-len) n)))

(defspec.xt x:offset-rlen [:fn [:xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:offset-rlen
  "uses the reverse length grammar offset"
  {:added "4.1"}
  ([] (list (quote x:offset-rlen))) 
  ([n] (list (quote x:offset-rlen) n)))

(defspec.xt x:lu-create [:fn [] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-create
  "creates a lookup table wrapper"
  {:added "4.1"}
  ([] (list (quote x:lu-create))))

(defspec.xt x:lu-eq [:fn [:xt/any :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-eq
  "compares lookup keys using lua identity"
  {:added "4.1"}
  ([x y] (list (quote x:lu-eq) x y)))

(defspec.xt x:lu-get [:fn [:xt/any :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-get
  "reads values from a lookup table"
  {:added "4.1"}
  ([lookup key] (list (quote x:lu-get) lookup key)) )

(defspec.xt x:lu-set [:fn [:xt/any :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-set
  "writes values into a lookup table"
  {:added "4.1"}
  ([lookup key value] (list (quote x:lu-set) lookup key value)))

(defspec.xt x:lu-del [:fn [:xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lu-del
  "removes values from a lookup table"
  {:added "4.1"}
  ([lookup key] (list (quote x:lu-del) lookup key)))

(defspec.xt x:m-abs [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-abs
  "computes absolute values"
  {:added "4.1"}
  ([value] (list (quote x:m-abs) value)))

(defspec.xt x:m-acos [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-acos
  "computes inverse cosine"
  {:added "4.1"}
  ([value] (list (quote x:m-acos) value)))

(defspec.xt x:m-asin [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-asin
  "computes inverse sine"
  {:added "4.1"}
  ([value] (list (quote x:m-asin) value)))

(defspec.xt x:m-atan [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-atan
  "computes inverse tangent"
  {:added "4.1"}
  ([value] (list (quote x:m-atan) value)))

(defspec.xt x:m-ceil [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-ceil
  "rounds numbers upward"
  {:added "4.1"}
  ([value] (list (quote x:m-ceil) value)))

(defspec.xt x:m-cos [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-cos
  "computes cosine"
  {:added "4.1"}
  ([value] (list (quote x:m-cos) value)))

(defspec.xt x:m-cosh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-cosh
  "computes hyperbolic cosine"
  {:added "4.1"}
  ([value] (list (quote x:m-cosh) value)))

(defspec.xt x:m-exp [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-exp
  "computes the exponential function"
  {:added "4.1"}
  ([value] (list (quote x:m-exp) value)))

(defspec.xt x:m-floor [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-floor
  "rounds numbers downward"
  {:added "4.1"}
  ([value] (list (quote x:m-floor) value)))

(defspec.xt x:m-loge [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-loge
  "computes the natural logarithm"
  {:added "4.1"}
  ([value] (list (quote x:m-loge) value)))

(defspec.xt x:m-log10 [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-log10
  "computes the base-10 logarithm"
  {:added "4.1"}
  ([value] (list (quote x:m-log10) value)))

(defspec.xt x:m-max [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-max
  "computes the maximum value"
  {:added "4.1"}
  ([x y] (list (quote x:m-max) x y)))

(defspec.xt x:m-mod [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-mod
  "computes modulo values"
  {:added "4.1"}
  ([x y] (list (quote x:m-mod) x y)))

(defspec.xt x:m-min [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-min
  "computes the minimum value"
  {:added "4.1"}
  ([x y] (list (quote x:m-min) x y)))

(defspec.xt x:m-pow [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-pow
  "raises numbers to a power"
  {:added "4.1"}
  ([x y] (list (quote x:m-pow) x y)))

(defspec.xt x:m-quot [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-quot
  "computes integer quotients"
  {:added "4.1"}
  ([x y] (list (quote x:m-quot) x y)))

(defspec.xt x:m-sin [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-sin
  "computes sine"
  {:added "4.1"}
  ([value] (list (quote x:m-sin) value)))

(defspec.xt x:m-sinh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-sinh
  "computes hyperbolic sine"
  {:added "4.1"}
  ([value] (list (quote x:m-sinh) value)))

(defspec.xt x:m-sqrt [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-sqrt
  "computes square roots"
  {:added "4.1"}
  ([value] (list (quote x:m-sqrt) value)))

(defspec.xt x:m-tan [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-tan
  "computes tangent"
  {:added "4.1"}
  ([value] (list (quote x:m-tan) value)))

(defspec.xt x:m-tanh [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:m-tanh
  "computes hyperbolic tangent"
  {:added "4.1"}
  ([value] (list (quote x:m-tanh) value)))

(defspec.xt x:not-nil? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:not-nil?
  "checks for non-nil values"
  {:added "4.1"}
  ([value] (list (quote x:not-nil?) value)))

(defspec.xt x:nil? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:nil?
  "checks for nil values"
  {:added "4.1"}
  ([value] (list (quote x:nil?) value)))

(defspec.xt x:add [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:add
  "adds numbers"
  {:added "4.1"}
  [x y & more] (apply list (quote x:add) x y more))

(defspec.xt x:sub [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:sub
  "subtracts numbers"
  {:added "4.1"}
  [x y & more] (apply list (quote x:sub) x y more))

(defspec.xt x:mul [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:mul
  "multiplies numbers"
  {:added "4.1"}
  [x y & more] (apply list (quote x:mul) x y more))

(defspec.xt x:div [:fn [:xt/num :xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:div
  "divides numbers"
  {:added "4.1"}
  [x y & more] (apply list (quote x:div) x y more))

(defspec.xt x:neg [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:neg
  "negates a number"
  {:added "4.1"}
  ([x] (list (quote x:neg) x)))

(defspec.xt x:inc [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:inc
  "increments a number"
  {:added "4.1"}
  ([x] (list (quote x:inc) x)))

(defspec.xt x:dec [:fn [:xt/num] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:dec
  "decrements a number"
  {:added "4.1"}
  ([x] (list (quote x:dec) x)))

(defspec.xt x:zero? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:zero?
  "checks whether a number is zero"
  {:added "4.1"}
  ([x] (list (quote x:zero?) x)))

(defspec.xt x:pos? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:pos?
  "checks whether a number is positive"
  {:added "4.1"}
  ([x] (list (quote x:pos?) x)))

(defspec.xt x:neg? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:neg?
  "checks whether a number is negative"
  {:added "4.1"}
  ([x] (list (quote x:neg?) x)))

(defspec.xt x:even? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:even?
  "checks whether a number is even"
  {:added "4.1"}
  ([x] (list (quote x:even?) x)))

(defspec.xt x:odd? [:fn [:xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:odd?
  "checks whether a number is odd"
  {:added "4.1"}
  ([x] (list (quote x:odd?) x)))

(defspec.xt x:eq [:fn [:xt/any :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:eq
  "checks equality"
  {:added "4.1"}
  ([x y] (list (quote x:eq) x y)))

(defspec.xt x:neq [:fn [:xt/any :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:neq
  "checks inequality"
  {:added "4.1"}
  ([x y] (list (quote x:neq) x y)))

(defspec.xt x:lt [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lt
  "checks less than"
  {:added "4.1"}
  ([x y] (list (quote x:lt) x y)))

(defspec.xt x:lte [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:lte
  "checks less than or equal"
  {:added "4.1"}
  ([x y] (list (quote x:lte) x y)))

(defspec.xt x:gt [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:gt
  "checks greater than"
  {:added "4.1"}
  ([x y] (list (quote x:gt) x y)))

(defspec.xt x:gte [:fn [:xt/num :xt/num] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:gte
  "checks greater than or equal"
  {:added "4.1"}
  ([x y] (list (quote x:gte) x y)))

(defspec.xt x:has-key? [:fn [:xt/obj :xt/str :xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:has-key?
  "checks whether an object has a key"
  {:added "4.1"}
  ([obj key] (list (quote x:has-key?) obj key)) 
  ([obj key check] (list (quote x:has-key?) obj key check)))

(defspec.xt x:del-key [:fn [:xt/obj :xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:del-key
  "deletes keys from objects"
  {:added "4.1"}
  ([obj key] (list (quote x:del-key) obj key)))

(defspec.xt x:get-key [:fn [:xt/obj :xt/str :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:get-key
  "gets a value by key with a fallback"
  {:added "4.1"}
  ([obj key] (list (quote x:get-key) obj key)) 
  ([obj key default] (list (quote x:get-key) obj key default)))

(defspec.xt x:get-path [:fn [:xt/obj [:xt/array :xt/str] :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:get-path
  "gets a nested value by path"
  {:added "4.1"}
  ([obj path] (list (quote x:get-path) obj path)) 
  ([obj path default] (list (quote x:get-path) obj path default)))

(defspec.xt x:set-key [:fn [:xt/obj [:xt/array :xt/str] :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:set-key
  "sets a key on an object"
  {:added "4.1"}
  ([obj key value] (list (quote x:set-key) obj key value)))

(defspec.xt x:copy-key [:fn [:xt/obj :xt/obj :xt/str :xt/any] :xt/self])

(defmacro.xt ^{:standalone true :is-template false} 
  x:copy-key
  "copies a key from another object"
  {:added "4.1"}
  ([dst src key] (list (quote x:copy-key) dst src key)))

(defspec.xt x:obj-keys [:fn [:xt/obj] [:xt/array :xt/str]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-keys
  "lists object keys"
  {:added "4.1"}
  ([obj] (list (quote x:obj-keys) obj)))

(defspec.xt x:obj-vals [:fn [:xt/obj] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-vals
  "lists object values"
  {:added "4.1"}
  ([obj] (list (quote x:obj-vals) obj)))

(defspec.xt x:obj-pairs [:fn [:xt/obj] [:xt/array [:xt/tuple :xt/str :xt/any]]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-pairs
  "lists object pairs"
  {:added "4.1"}
  ([obj] (list (quote x:obj-pairs) obj)))

(defspec.xt x:obj-clone [:fn [:xt/obj] :xt/obj])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-clone
  "clones an object"
  {:added "4.1"}
  ([obj] (list (quote x:obj-clone) obj)))

(defspec.xt x:obj-assign [:fn [:xt/obj :xt/obj] :xt/obj])

(defmacro.xt ^{:standalone true :is-template false} 
  x:obj-assign
  "assigns object keys"
  {:added "4.1"}
  ([obj other] (list (quote x:obj-assign) obj other)))

(defspec.xt x:to-string [:fn [:xt/any] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:to-string
  "converts a value to a string"
  {:added "4.1"}
  ([value] (list (quote x:to-string) value)))

(defspec.xt x:to-number [:fn [:xt/any] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:to-number
  "converts a string to a number"
  {:added "4.1"}
  ([value] (list (quote x:to-number) value)))

(defspec.xt x:is-string? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-string?
  "recognises strings"
  {:added "4.1"}
  ([value] (list (quote x:is-string?) value)))

(defspec.xt x:is-number? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-number?
  "recognises numbers"
  {:added "4.1"}
  ([value] (list (quote x:is-number?) value)))

(defspec.xt x:is-integer? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-integer?
  "recognises integers"
  {:added "4.1"}
  ([value] (list (quote x:is-integer?) value)))

(defspec.xt x:is-boolean? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-boolean?
  "recognises booleans"
  {:added "4.1"}
  ([value] (list (quote x:is-boolean?) value)))

(defspec.xt x:is-object? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-object?
  "recognises objects"
  {:added "4.1"}
  ([value] (list (quote x:is-object?) value)))

(defspec.xt x:is-array? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-array?
  "recognises arrays"
  {:added "4.1"}
  ([value] (list (quote x:is-array?) value)))

(defspec.xt x:print [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:print
  "expands and emits a lua print form"
  {:added "4.1"}
  ([value] (list (quote x:print) value)))

(defspec.xt x:str-len [:fn [:xt/str] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-len
  "gets the string length"
  {:added "4.1"}
  ([value] (list (quote x:str-len) value)))

(defspec.xt x:str-comp [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-comp
  "compares strings by sort order"
  {:added "4.1"}
  ([x y] (list (quote x:str-comp) x y)))

(defspec.xt x:str-lt [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-lt
  "checks whether one string sorts before another"
  {:added "4.1"}
  ([x y] (list (quote x:str-lt) x y)))

(defspec.xt x:str-gt [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-gt
  "checks whether one string sorts after another"
  {:added "4.1"}
  ([x y] (list (quote x:str-gt) x y)))

(defspec.xt x:str-pad-left [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-pad-left
  "pads a string on the left"
  {:added "4.1"}
  ([value len pad] (list (quote x:str-pad-left) value len pad)))

(defspec.xt x:str-pad-right [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-pad-right
  "pads a string on the right"
  {:added "4.1"}
  ([value len pad] (list (quote x:str-pad-right) value len pad)))

(defspec.xt x:str-starts-with [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-starts-with
  "checks the string prefix"
  {:added "4.1"}
  ([value prefix] (list (quote x:str-starts-with) value prefix)))

(defspec.xt x:str-ends-with [:fn [:xt/str :xt/str] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-ends-with
  "checks the string suffix"
  {:added "4.1"}
  ([value suffix] (list (quote x:str-ends-with) value suffix)))

(defspec.xt x:str-char [:fn [:xt/str :xt/num] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-char
  "gets the character code at an index"
  {:added "4.1"}
  ([value idx] (list (quote x:str-char) value idx)))

(defspec.xt x:str-split [:fn [:xt/str :xt/str] [:xt/array :xt/str]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-split
  "splits a string"
  {:added "4.1"}
  ([value separator] (list (quote x:str-split) value separator)))

(defspec.xt x:str-join [:fn [:xt/str [:xt/array :xt/any]] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-join
  "joins string parts"
  {:added "4.1"}
  ([separator coll] (list (quote x:str-join) separator coll)))

(defspec.xt x:str-index-of [:fn [:xt/str :xt/str :xt/num] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-index-of
  "finds the index of a substring"
  {:added "4.1"}
  ([value pattern] (list (quote x:str-index-of) value pattern)) 
  ([value pattern from] (list (quote x:str-index-of) value pattern from)))

(defspec.xt x:str-substring [:fn [:xt/str :xt/num [:xt/maybe :xt/num]] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-substring
  "gets a substring"
  {:added "4.1"}
  ([value start] (list (quote x:str-substring) value start)) 
  ([value start finish] (list (quote x:str-substring) value start finish)))

(defspec.xt x:str-to-upper [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-to-upper
  "converts a string to upper case"
  {:added "4.1"}
  ([value] (list (quote x:str-to-upper) value)))

(defspec.xt x:str-to-lower [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-to-lower
  "converts a string to lower case"
  {:added "4.1"}
  ([value] (list (quote x:str-to-lower) value)))

(defspec.xt x:str-to-fixed [:fn [:xt/num :xt/int] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-to-fixed
  "formats a number with fixed decimals"
  {:added "4.1"}
  ([value digits] (list (quote x:str-to-fixed) value digits)))

(defspec.xt x:str-replace [:fn [:xt/str :xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-replace
  "replaces matching substrings"
  {:added "4.1"}
  ([value match replacement] (list (quote x:str-replace) value match replacement)))

(defspec.xt x:str-trim [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-trim
  "trims whitespace from both sides"
  {:added "4.1"}
  ([value] (list (quote x:str-trim) value)))

(defspec.xt x:str-trim-left [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-trim-left
  "trims whitespace from the left side"
  {:added "4.1"}
  ([value] (list (quote x:str-trim-left) value)))

(defspec.xt x:str-trim-right [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:str-trim-right
  "trims whitespace from the right side"
  {:added "4.1"}
  ([value] (list (quote x:str-trim-right) value)))

(defspec.xt x:arr-sort [:fn [[:xt/array :xt/any] :fn] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-sort
  "sorts arrays using key and compare functions"
  {:added "4.1"}
  ([arr key-fn compare-fn] (list (quote x:arr-sort) arr key-fn compare-fn)))

(defspec.xt x:arr-clone [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-clone
  "clones an array"
  {:added "4.1"}
  ([arr] (list (quote x:arr-clone) arr)))

(defspec.xt x:arr-each [:fn [[:xt/array :xt/any]] nil])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-each
  "iterates each element in an array"
  {:added "4.1"}
  ([arr f] (list (quote x:arr-each) arr f)))

(defspec.xt x:arr-every [:fn [[:xt/array :xt/any] :xt/fn] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-every
  "checks whether every array element matches a predicate"
  {:added "4.1"}
  ([arr pred] (list (quote x:arr-every) arr pred)))

(defspec.xt x:arr-some [:fn [[:xt/array :xt/any] :xt/fn] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-some
  "checks whether any array element matches a predicate"
  {:added "4.1"}
  ([arr pred] (list (quote x:arr-some) arr pred)))

(defspec.xt x:arr-map [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-map
  "maps an array"
  {:added "4.1"}
  ([arr f] (list (quote x:arr-map) arr f)))

(defspec.xt x:arr-assign [:fn [[:xt/array :xt/any] :xt/any] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-assign
  "appends one array to another"
  {:added "4.1"}
  ([arr value] (list (quote x:arr-assign) arr value)))

(defspec.xt x:arr-concat [:fn [[:xt/array :xt/any] :xt/any] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false}
  x:arr-concat
  "concatenates arrays into a new array"
  {:added "4.1"}
  ([arr value] (list (quote x:arr-concat) arr value)))

(defspec.xt x:arr-filter [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-filter
  "filters an array"
  {:added "4.1"}
  ([arr pred] (list (quote x:arr-filter) arr pred)))

(defspec.xt x:arr-foldl [:fn [[:xt/array :xt/any] :xt/any :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-foldl
  "folds arrays from the left"
  {:added "4.1"}
  ([arr init f] (list (quote x:arr-foldl) arr init f)))

(defspec.xt x:arr-foldr [:fn [[:xt/array :xt/any] :xt/any :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-foldr
  "folds arrays from the right"
  {:added "4.1"}
  ([arr init f] (list (quote x:arr-foldr) arr init f)))

(defspec.xt x:arr-find [:fn [[:xt/array :xt/any] :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:arr-find
  "keeps the find wrapper pointed at the canonical op"
  {:added "4.1"}
  ([arr pred] (list (quote x:arr-find) arr pred)))

(defspec.xt x:is-function? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:is-function?
  "recognises function values"
  {:added "4.1"}
  ([value] (list (quote x:is-function?) value)))

(defspec.xt x:callback [:fn [] [:fn [:xt/any] nil]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:callback
  "dispatches node-style callbacks through for:return"
  {:added "4.1"}
  ([] (list (quote x:callback))))

(defspec.xt x:return-run nil)

(defmacro.xt ^{:standalone true :is-template false}
  x:return-run
  "can be used directly inside for:return"
  {:added "4.1"}
  ([runner] (list (quote x:return-run) runner)))

(defspec.xt x:eval [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:eval
  "evaluates javascript expressions"
  {:added "4.1"}
  ([value] (list (quote x:eval) value)))

(defspec.xt x:apply [:fn [:xt/fn [:xt/array :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:apply
  "applies array arguments to functions"
  {:added "4.1"}
  ([f args] (list (quote x:apply) f args)))

(defspec.xt x:iter-from-obj nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-from-obj
  "creates iterators over object pairs"
  {:added "4.1"}
  ([obj] (list (quote x:iter-from-obj) obj)))

(defspec.xt x:iter-from-arr nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-from-arr
  "creates iterators over arrays"
  {:added "4.1"}
  ([arr] (list (quote x:iter-from-arr) arr)))

(defspec.xt x:iter-from nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-from
  "creates generic iterators from iterable values"
  {:added "4.1"}
  ([value] (list (quote x:iter-from) value)))

(defspec.xt x:iter-eq [:fn [:xt/any :xt/any :xt/fn] :xt/bool])

(defmacro.xt ^{:standalone true :is-template true} 
  x:iter-eq
  "checks iterator equality in js"
  {:added "4.1"}
  ([iter0 iter1 eq-fn] (list (quote x:iter-eq) iter0 iter1 eq-fn)))

(defspec.xt x:iter-null nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-null
  "creates empty iterators"
  {:added "4.1"}
  ([] (list (quote x:iter-null))))

(defspec.xt x:iter-next nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-next
  "advances iterators"
  {:added "4.1"}
  ([iter] (list (quote x:iter-next) iter)))

(defspec.xt x:iter-has? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-has?
  "checks whether values are iterable"
  {:added "4.1"}
  ([iter] (list (quote x:iter-has?) iter)))

(defspec.xt x:iter-native? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:iter-native?
  "checks whether values are iterator instances"
  {:added "4.1"}
  ([iter] (list (quote x:iter-native?) iter)))

(defspec.xt x:return-encode [:fn [:xt/any :xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template true} 
  x:return-encode
  "encodes return payloads as json"
  {:added "4.1"}
  ([out id key] (list (quote x:return-encode) out id key)))

(defspec.xt x:return-wrap [:fn [[:fn [:xt/any] :xt/any]] [:fn [:xt/any] :xt/str]])

(defmacro.xt ^{:standalone true :is-template true} 
  x:return-wrap
  "wraps return values through encoder functions"
  {:added "4.1"}
  ([callbock encode-fn] (list (quote x:return-wrap) callbock encode-fn)))

(defspec.xt x:return-eval [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true :is-template true} 
  x:return-eval
  "evaluates code through wrapped return handlers"
  {:added "4.1"}
  ([expr wrap-fn] (list (quote x:return-eval) expr wrap-fn)))

(defspec.xt x:bit-and [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-and
  "computes bitwise and"
  {:added "4.1"}
  ([x y] (list (quote x:bit-and) x y)))

(defspec.xt x:bit-or [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-or
  "computes bitwise or"
  {:added "4.1"}
  ([x y] (list (quote x:bit-or) x y)))

(defspec.xt x:bit-lshift [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-lshift
  "computes bitwise left shifts"
  {:added "4.1"}
  ([x y] (list (quote x:bit-lshift) x y)))

(defspec.xt x:bit-rshift [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-rshift
  "computes bitwise right shifts"
  {:added "4.1"}
  ([x y] (list (quote x:bit-rshift) x y)))

(defspec.xt x:bit-xor [:fn [:xt/int :xt/int] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:bit-xor
  "computes bitwise xor"
  {:added "4.1"}
  ([x y] (list (quote x:bit-xor) x y)))

(defspec.xt x:global-set nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:global-set
  "writes values to the shared global map"
  {:added "4.1"}
  ([sym value] (list (quote x:global-set) sym value)))

(defspec.xt x:global-del nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:global-del
  "removes values from the shared global map"
  {:added "4.1"}
  ([sym] (list (quote x:global-del) sym)))

(defspec.xt x:global-has? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true :is-template false} 
  x:global-has?
  "checks whether the shared global map contains a value"
  {:added "4.1"}
  ([sym] (list (quote x:global-has?) sym)))

(defspec.xt x:proto-get nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:proto-get
  "retrieves object prototypes"
  {:added "4.1"}
  ([obj] (list (quote x:proto-get) obj)))

(defspec.xt x:proto-set nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:proto-set
  "assigns object prototypes"
  {:added "4.1"}
  ([obj proto] (list (quote x:proto-set) obj proto)))

(defspec.xt x:proto-create nil)

(defmacro.xt ^{:standalone true :is-template true} 
  x:proto-create
  "creates prototypes with this-bound methods"
  {:added "4.1"}
  ([m] (list (quote x:proto-create) m)))

(defspec.xt x:proto-tostring nil)

(defmacro.xt ^{:standalone true :is-template false} 
  x:proto-tostring
  "expands and emits the lua tostring metamethod key"
  {:added "4.1"}
  ([] (list (quote x:proto-tostring))))

(defspec.xt x:random [:fn [] :xt/num])

(defmacro.xt ^{:standalone true :is-template false} 
  x:random
  "returns javascript random values"
  {:added "4.1"}
  ([] (list (quote x:random))))

(defspec.xt x:throw [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:throw
  "expands to the canonical throw form"
  {:added "4.1"}
  ([value] (list (quote x:throw) value)))

(defspec.xt x:now-ms [:fn [] :xt/int])

(defmacro.xt ^{:standalone true :is-template false} 
  x:now-ms
  "expands and emits a millisecond time expression"
  {:added "4.1"}
  ([] (list (quote x:now-ms))))

(defspec.xt x:unpack [:fn [[:xt/array :xt/any]] [:xt/tuple :xt/any]])

(defmacro.xt ^{:standalone true :is-template false} 
  x:unpack
  "spreads arrays into positional arguments"
  {:added "4.1"}
  ([value] (list (quote x:unpack) value)))

(defspec.xt x:json-encode [:fn [:xt/any] :xt/str])

(defmacro.xt ^{:standalone true :is-template false} 
  x:json-encode
  "encodes lua data structures as json"
  {:added "4.1"}
  ([value] (list (quote x:json-encode) value)))

(defspec.xt x:json-decode [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false} 
  x:json-decode
  "decodes json strings into lua data structures"
  {:added "4.1"}
  ([expr] (list (quote x:json-decode) expr)))
