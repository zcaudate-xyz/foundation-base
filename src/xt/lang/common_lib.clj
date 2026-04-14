(ns xt.lang.common-lib
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [identity fn? cat print
                            nil? inc dec zero? pos?
                            neg? even? odd?]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]]})

;;
;; TYPE
;;

(defspec.xt type-native [:fn [:xt/any] :xt/str])

(defn.xt type-native
  "gets the native type"
  {:added "4.0"}
  ([obj]
   (xt/x:type-native obj)))

(defspec.xt type-class [:fn [:xt/any] :xt/str])

(defn.xt type-class
  "gets the type of an object"
  {:added "4.1"}
  ([x]
   (var ntype (xt/x:type-native x))
   (if (xt/x:is-object? x)
     (return (xt/x:get-key x "::" "object"))
     (return ntype))))

(defspec.xt to-string [:fn [:xt/num] :xt/str])

(defn.xt to-string
  "converts an object into a string"
  {:added "4.0"}
  ([x] (return (xt/x:to-string x))))

(defspec.xt to-number [:fn [:xt/str] :xt/num])

(defn.xt to-number
  "converts a string to a number"
  {:added "4.0"}
  ([x] (return (xt/x:to-number x))))


;;
;; TYPE PREDICATE
;;

(defspec.xt nil? [:fn [:xt/any] :xt/bool])

(defn.xt nil?
  "checks that value is nil"
  {:added "4.0"}
  ([x] (return (xt/x:nil? x))))

(defspec.xt not-nil? [:fn [:xt/any] :xt/bool])

(defn.xt not-nil?
  "checks that value is not nil"
  {:added "4.0"}
  ([x] (return (xt/x:not-nil? x))))

(defspec.xt len [:fn [:xt/any] :xt/int])

(defn.xt len
  "gets length of a value"
  {:added "4.1"}
  [x]
  (return (xt/x:len x)))

(defspec.xt first [:fn [[:xt/array :xt/any]] :xt/any])

(defn.xt first
  "gets the first item of an array"
  {:added "4.1"}
  [arr]
  (return (xt/x:first arr)))

(defspec.xt second [:fn [[:xt/array :xt/any]] :xt/any])

(defn.xt second
  "gets the second item of an array"
  {:added "4.1"}
  [arr]
  (return (xt/x:second arr)))

(defn.xt get-key
  "gets a key from an object"
  {:added "4.1"}
  [obj key]
  (return (xt/x:get-key obj key)))

(defn.xt obj-keys
  "gets object keys"
  {:added "4.1"}
  [obj]
  (return (xt/x:obj-keys obj)))

(defn.xt json-encode
  "encodes an object to json"
  {:added "4.1"}
  [obj]
  (return (xt/x:json-encode obj)))

(defn.xt json-decode
  "decodes json to an object"
  {:added "4.1"}
  [s]
  (return (xt/x:json-decode s)))

(defn.xt cat
  "concats two strings"
  {:added "4.1"}
  [a b]
  (return (xt/x:cat a b)))

(defn.xt join
  "joins an array with a separator"
  {:added "4.1"}
  [separator arr]
  (return (str/join separator arr)))

(defn.xt arr-map
  "maps a function across an array"
  {:added "4.1"}
  [arr f]
  (return (xtd/arr-map arr f)))

(defn.xt get-in
  "gets a nested path from an object"
  {:added "4.1"}
  [obj path]
  (return (xtd/get-in obj path)))

(defn.xt set-in
  "sets a nested path in an object"
  {:added "4.1"}
  [obj path v]
  (return (xtd/set-in obj path v)))

(defn.xt obj-omit
  "omits keys from an object"
  {:added "4.1"}
  [obj keys]
  (return (xtd/obj-omit obj keys)))

(defspec.xt is-boolean? [:fn [:xt/any] :xt/bool])

(defn.xt is-boolean?
  "checks if object is an fnay"
  {:added "4.1"}
  [x]
  (return (xt/x:is-boolean? x)))

(defspec.xt is-integer? [:fn [:xt/any] :xt/bool])

(defn.xt is-integer?
  "checks if object is an fnay"
  {:added "4.1"}
  [x]
  (return (xt/x:is-integer? x)))

(defspec.xt is-number? [:fn [:xt/any] :xt/bool])

(defn.xt is-number?
  "checks if object is an fnay"
  {:added "4.1"}
  [x]
  (return (xt/x:is-number? x)))

(defspec.xt is-string? [:fn [:xt/any] :xt/bool])

(defn.xt is-string?
  "checks if object is an fnay"
  {:added "4.1"}
  [x]
  (return (xt/x:is-string? x)))

(defspec.xt is-function? [:fn [:xt/any] :xt/bool])

(defn.xt is-function?
  "checks if object is an fnay"
  {:added "4.1"}
  [x]
  (return (xt/x:is-function? x)))

(defspec.xt is-array? [:fn [:xt/any] :xt/bool])

(defn.xt is-array?
  "checks if object is an array"
  {:added "4.1"}
  [x]
  (return (xt/x:is-array? x)))

(defspec.xt is-object? [:fn [:xt/any] :xt/bool])

(defn.xt is-object?
  "checks if object is a map type"
  {:added "4.1"}
  [x]
  (return (xt/x:is-object? x)))



;;
;; PROTO
;;

(defn.xt proto-create
  "creates the prototype map"
  {:added "4.0"}
  ([m]
   (xt/x:proto-create m)))

(defn.xt proto-get
  "creates the prototype map"
  {:added "4.0"}
  ([obj key]
   (return (xt/x:proto-get obj key))))

(defn.xt proto-set
  "creates the prototype map"
  {:added "4.0"}
  ([obj key value]
   (return (xt/x:proto-set obj key value))))

(defn.xt proto-tostring
  "creates the prototype map"
  {:added "4.0"}
  ([obj]
   (return (xt/x:proto-tostring obj))))



;;
;; FN.BASIC
;;

(defspec.xt noop [:fn [] :xt/nil])

(defn.xt noop
  "always a no op"
  {:added "4.1"}
  [] (return nil))

(defspec.xt identity [:fn [:xt/any] :xt/any])

(defn.xt identity
  "identity function"
  {:added "4.0"}
  ([x] (return x)))

(defn.xt sort
  "sorts an array with the default ordering"
  {:added "4.1"}
  [arr]
  (return (xtd/arr-sort arr -/identity xt/x:lt)))

(defspec.xt T [:fn [:xt/any] :xt/bool])

(defn.xt T
  "always true"
  {:added "4.1"}
  [x] (return true))

(defspec.xt F [:fn [:xt/any] :xt/bool])

(defn.xt F
  "always false"
  {:added "4.1"}
  [x] (return false))



;;
;; FN.NUMBER
;;

(defspec.xt add [:fn [:xt/num :xt/num] :xt/num])

(defn.xt add
  "performs add operation"
  {:added "4.0"}
  [a b] (return (xt/x:add a b)))

(defspec.xt sub [:fn [:xt/num :xt/num] :xt/num])

(defn.xt sub
  "performs sub operation"
  {:added "4.0"}
  [a b] (return (xt/x:sub a b)))

(defspec.xt mul [:fn [:xt/num :xt/num] :xt/num])

(defn.xt mul
  "perform multiply operation"
  {:added "4.0"}
  [a b] (return (xt/x:mul a b)))

(defspec.xt div [:fn [:xt/num :xt/num] :xt/num])

(defn.xt div
  "perform divide operation"
  {:added "4.0"}
  [a b] (return (xt/x:div a b)))

(defspec.xt gt [:fn [:xt/num :xt/num] :xt/bool])

(defn.xt gt
  "greater than"
  {:added "4.0"}
  [a b] (return (xt/x:gt a b)))

(defspec.xt lt [:fn [:xt/num :xt/num] :xt/bool])

(defn.xt lt
  "less than"
  {:added "4.0"}
  [a b] (return (xt/x:lt a b)))

(defspec.xt gte [:fn [:xt/num :xt/num] :xt/bool])

(defn.xt gte
  "greater than or equal to"
  {:added "4.0"}
  [a b] (return (xt/x:gte a b)))

(defspec.xt lte [:fn [:xt/num :xt/num] :xt/bool])

(defn.xt lte
  "less than or equal to"
  {:added "4.0"}
  [a b] (return (xt/x:lte a b)))

(defspec.xt eq [:fn [:xt/num :xt/num] :xt/bool])

(defn.xt eq
  "equal to"
  {:added "4.0"}
  [a b] (return (xt/x:eq a b)))

(defspec.xt neq [:fn [:xt/num :xt/num] :xt/bool])

(defn.xt neq
  "not equal to"
  {:added "4.0"}
  [a b] (return (xt/x:neq a b)))

(defspec.xt neg [:fn [:xt/num] :xt/num])

(defn.xt neg
  "negative function"
  {:added "4.0"}
  ([x] (return (xt/x:neg x))))

(defspec.xt inc [:fn [:xt/num] :xt/num])

(defn.xt inc
  "increment function"
  {:added "4.0"}
  ([x] (return (xt/x:inc x))))

(defspec.xt dec [:fn [:xt/num] :xt/num])

(defn.xt dec
  "decrement function"
  {:added "4.0"}
  ([x] (return (xt/x:dec x))))

(defspec.xt zero? [:fn [:xt/num] :xt/bool])

(defn.xt zero?
  "zero check"
  {:added "4.0"}
  ([x] (return (xt/x:zero? x))))

(defspec.xt pos? [:fn [:xt/num] :xt/bool])

(defn.xt pos?
  "positive check"
  {:added "4.0"}
  ([x] (return (xt/x:pos? x))))

(defspec.xt neg? [:fn [:xt/num] :xt/bool])

(defn.xt neg?
  "negative check"
  {:added "4.0"}
  ([x] (return  (xt/x:neg? x))))

(defspec.xt even? [:fn [:xt/num] :xt/bool])

(defn.xt even?
  "even check"
  {:added "4.0"}
  ([x] (return (xt/x:even? x))))

(defspec.xt odd? [:fn [:xt/num] :xt/bool])

(defn.xt odd?
  "odd check"
  {:added "4.0"}
  ([x] (return (xt/x:odd? x))))


;;
;; CALLBACK
;;

(defn.xt wrap-callback
  "returns a wrapped callback given map"
  {:added "4.0"}
  [callbacks key]
  (:= callbacks (or callbacks {}))
  (var result-fn
       (fn [result]
         (var f (xt/x:get-key callbacks key))
         (if (xt/x:not-nil? f)
           (return (f result))
           (return result))))
  (return result-fn))
