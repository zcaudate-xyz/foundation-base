(ns xt.lang.base-macro
  (:require [std.lang :as l])
  (:refer-clojure :exclude
                  [abs bit-and bit-or bit-xor
                   identity inc dec zero? pos? neg? even? odd?
                   max min mod quot cat eval apply print
                   nil? fn? first second nth replace last throw]))

(l/script :xtalk
  {})

(defmacro.xt ^{:style/indent 1}
  for:array
  "helper function to `for:array`"
  {:added "4.0"}
  ([[e arr] & body]
   (clojure.core/apply list 'for:array [e arr] body)))

(defmacro.xt ^{:style/indent 1}
  for:object
  "helper function to `for:object`"
  {:added "4.0"}
  ([[[k v] obj] & body]
   (clojure.core/apply list 'for:object [[k v] obj] body)))

(defmacro.xt ^{:style/indent 1}
  for:index
  "for index call"
  {:added "4.0"}
  ([[i [start stop step]] & body]
   (clojure.core/apply list 'for:index [i [start stop step]] body)))

(defmacro.xt ^{:style/indent 1}
  for:return
  "defines a return construct"
  {:added "4.0"}
  ([[[ok err] statement] {:keys [success error final]}]
   (list 'for:return [[ok err] statement]
         {:success success
          :error error
          :final final})))

(defmacro.xt ^{:style/indent 1}
  for:try
  "performs try/catch block"
  {:added "4.0"}
  ([[[ok err] statement] {:keys [success error]}]
   (list 'for:try [[ok err] statement]
         {:success success
          :error error})))

(defmacro.xt ^{:style/indent 1}
  for:async
  "performs an async call"
  {:added "4.0"}
  ([[[ok err] statement] {:keys [success error finally]}]
   (list 'for:async [[ok err] statement]
         {:success success
          :error error
          :finally finally})))

(defmacro.xt
  invoke
  "calls the function on rest of arguments"
  {:added "4.0"}
  ([f & args]
   (clojure.core/apply list f args)))

(defmacro.xt ^{:standalone true}
  add
  "performs add operation"
  {:added "4.0"}
  [a b]
  (list '+ a b))

(defmacro.xt ^{:standalone true}
  sub
  "performs sub operation"
  {:added "4.0"}
  [a b]
  (list '- a b))

(defmacro.xt ^{:standalone true}
  mul
  "perform multiply operation"
  {:added "4.0"}
  [a b]
  (list '* a b))

(defmacro.xt ^{:standalone true}
  div
  "perform divide operation"
  {:added "4.0"}
  [a b]
  (list '/ a b))

(defmacro.xt ^{:standalone true}
  gt
  "greater than"
  {:added "4.0"}
  [a b]
  (list '> a b))

(defmacro.xt ^{:standalone true}
  lt
  "less than"
  {:added "4.0"}
  [a b]
  (list '< a b))

(defmacro.xt ^{:standalone true}
  gte
  "greater than or equal to"
  {:added "4.0"}
  [a b]
  (list '>= a b))

(defmacro.xt ^{:standalone true}
  lte
  "less than or equal to"
  {:added "4.0"}
  [a b]
  (list '<= a b))

(defmacro.xt ^{:standalone true}
  eq
  "equal to"
  {:added "4.0"}
  [a b]
  (list '== a b))

(defmacro.xt ^{:standalone true}
  neq
  "not equal to"
  {:added "4.0"}
  [a b]
  (list 'not= a b))

(defmacro.xt ^{:standalone true}
  neg
  "negative function"
  {:added "4.0"}
  ([x] (list '- (list 'quote x))))

(defmacro.xt ^{:standalone true}
  inc
  "increment function"
  {:added "4.0"}
  ([x] (list '+ x 1)))

(defmacro.xt ^{:standalone true}
  dec
  "decrement function"
  {:added "4.0"}
  ([x] (list '- x 1)))

(defmacro.xt ^{:standalone true}
  zero?
  "zero check"
  {:added "4.0"}
  ([x]
   (list '== x 0)))

(defmacro.xt ^{:standalone true}
  pos?
  "positive check"
  {:added "4.0"}
  ([x]
   (list '> x 0)))

(defmacro.xt ^{:standalone true}
  neg?
  "negative check"
  {:added "4.0"}
  ([x]
   (list '< x 0)))

(defmacro.xt ^{:standalone true}
  even?
  "even check"
  {:added "4.0"}
  ([x] (list '== 0 (list 'mod x 2))))

(defmacro.xt ^{:standalone true}
  odd?
  "odd check"
  {:added "4.0"}
  ([x] (list 'not (list '== 0 (list 'mod x 2)))))

(defmacro.xt ^{:standalone true}
  lt-string
  "checks if a is ordered before b"
  {:added "4.0"}
  [a b]
  (list 'x:arr-str-comp a b))

(defmacro.xt ^{:standalone true}
  gt-string
  "checks if a is ordered before b"
  {:added "4.0"}
  [a b]
  (list 'x:arr-str-comp b a))



;;
;; PROTO
;;

(defmacro.xt ^{:standalone true}
  this
  "gets the current"
  {:added "4.0"}
  [] (list 'x:this))

(defmacro.xt ^{:standalone true}
  set-proto
  "sets the prototype"
  {:added "4.0"}
  [obj prototype]
  (list 'x:proto-set obj prototype))

(defmacro.xt ^{:standalone true}
  get-proto
  "gets the prototype"
  {:added "4.0"}
  [obj]
  (list 'x:proto-get obj))

;;
;; XLANG MATH
;;

(defmacro.xt ^{:standalone true}
  abs
  "gets the absolute value"
  {:added "4.0"}
  [num] (list 'x:m-abs num))

(defmacro.xt ^{:standalone true}
  acos
  "gets the arc cos value"
  {:added "4.0"}
  [num] (list 'x:m-acos num))

(defmacro.xt ^{:standalone true}
  asin
  "gets the arc sin value"
  {:added "4.0"}
  [num] (list 'x:m-asin num))

(defmacro.xt ^{:standalone true}
  atan
  "gets the arc tan value"
  {:added "4.0"}
  [num] (list 'x:m-atan num))

(defmacro.xt ^{:standalone true}
  ceil
  "gets the ceiling"
  {:added "4.0"}
  [num] (list 'x:m-ceil num))

(defmacro.xt ^{:standalone true}
  cos
  "gets the cos value"
  {:added "4.0"}
  [num] (list 'x:m-cos num))

(defmacro.xt ^{:standalone true}
  cosh
  "gets the cosh value"
  {:added "4.0"}
  [num] (list 'x:m-cosh num))

(defmacro.xt ^{:standalone true}
  exp
  "gets the `e^x` value"
  {:added "4.0"}
  [num] (list 'x:m-exp num))

(defmacro.xt ^{:standalone true}
  floor
  "gets the floor"
  {:added "4.0"}
  [num] (list 'x:m-floor num))

(defmacro.xt ^{:standalone true}
  loge
  "gets the natural log"
  {:added "4.0"}
  [num] (list 'x:m-loge num))

(defmacro.xt ^{:standalone true}
  log10
  "gets the log base 10"
  {:added "4.0"}
  [num] (list 'x:m-log10 num))

(defmacro.xt ^{:standalone 'x:m-max}
  max
  "gets the maximum value"
  {:added "4.0"}
  [& args] (clojure.core/apply list 'x:m-max args))

(defmacro.xt ^{:standalone 'x:m-max}
  min
  "gets the minimum value"
  {:added "4.0"}
  [& args] (clojure.core/apply list 'x:m-min args))

(defmacro.xt ^{:standalone true}
  mod
  "gets the mod"
  {:added "4.0"}
  [num denom] (list 'mod num denom))

(defmacro.xt ^{:standalone true}
  quot
  "gets the quotient"
  {:added "4.0"}
  [base n] (list 'x:m-quot base n))

(defmacro.xt ^{:standalone true}
  pow
  "gets the power"
  {:added "4.0"}
  [base n] (list 'x:m-pow base n))

(defmacro.xt ^{:standalone true}
  sin
  "gets the sin"
  {:added "4.0"}
  [num] (list 'x:m-sin num))

(defmacro.xt ^{:standalone true}
  sinh
  "gets the sinh"
  {:added "4.0"}
  [num] (list 'x:m-sinh num))

(defmacro.xt ^{:standalone true}
  sqrt
  "gets the square root"
  {:added "4.0"}
  [num] (list 'x:m-sqrt num))

(defmacro.xt ^{:standalone true}
  tan
  "gets the tan"
  {:added "4.0"}
  [num] (list 'x:m-tan num))

(defmacro.xt ^{:standalone true}
  tanh
  "gets the tanh"
  {:added "4.0"}
  [num] (list 'x:m-tanh num))
  
;;
;; COMMON
;;

(defmacro.xt ^{:standalone 'x:cat}
  cat
  "concat strings together"
  {:added "4.0"}
  ([& args]
   (clojure.core/apply list 'x:cat args)))

(defmacro.xt ^{:standalone true}
  len
  "gets the length of an array"
  {:added "4.0"}
  ([arr]
   (list 'x:len (list 'quote (list arr)))))

(defmacro.xt ^{:standalone true}
  err
  "throws an error"
  {:added "4.0"}
  ([s & [data]]
   (if data
     (list 'x:err s data)
     (list 'x:err s))))

(defmacro.xt
  throw
  "base version of throw"
  {:added "4.0"}
  [exception]
  (list 'x:throw exception))

(defmacro.xt ^{:standalone true}
  eval
  "evaluates a string"
  {:added "4.0"}
  ([s]
   (list 'x:eval s)))

(defmacro.xt ^{:standalone true}
  apply
  "applies a function to an array"
  {:added "4.0"}
  ([f args]
   (list 'x:apply f args)))

(defmacro.xt ^{:standalone 'x:print}
  print
  "prints a string (for debugging)"
  {:added "4.0"}
  ([& args]
   (clojure.core/apply list 'x:print args)))

(defmacro.xt ^{:standalone true}
  unpack
  "unpacks an array into another"
  {:added "4.0"}
  ([arr]
   (list 'x:unpack arr)))

(defmacro.xt ^{:standalone true}
  now-ms
  "gets the current millisecond time"
  {:added "4.0"}
  ([]
   (list 'x:now-ms)))

(defmacro.xt ^{:standalone true}
  random
  "generates a random float"
  {:added "4.0"}
  ([]
   (list 'x:random)))

;;
;; XLANG TYPE
;;


(defmacro.xt ^{:standalone true}
  not-nil?
  "checks that value is not nil"
  {:added "4.0"}
  ([x]
   (list 'x:not-nil? x)))

(defmacro.xt ^{:standalone true}
  nil?
  "checks that value is nil"
  {:added "4.0"}
  ([x]
   (list 'x:nil? x)))

(defmacro.xt ^{:standalone true}
  to-string
  "converts an object into a string"
  {:added "4.0"}
  ([obj]
   (list 'x:to-string obj)))

(defmacro.xt ^{:standalone true}
  to-number
  "converts a string to a number"
  {:added "4.0"}
  ([obj]
   (list 'x:to-number obj)))

(defmacro.xt ^{:standalone true}
  is-string?
  "checks if object is a string"
  {:added "4.0"}
  ([obj]
   (list 'x:is-string? obj)))

(defmacro.xt ^{:standalone true}
  is-number?
  "checks if object is a number"
  {:added "4.0"}
  ([obj]
   (list 'x:is-number? obj)))

(defmacro.xt ^{:standalone true}
  is-integer?
  "checks that number is an integer"
  {:added "4.0"}
  ([obj]
   (list 'x:is-integer? obj)))

(defmacro.xt ^{:standalone true}
  is-boolean?
  "checks if object is a boolean"
  {:added "4.0"}
  ([obj]
   (list 'x:is-boolean? obj)))

;;
;; XLANG TYPES
;;

(defmacro.xt ^{:standalone true}
  is-function?
  "checks if object is a function type"
  {:added "4.0"}
  ([x]
   (list 'x:is-function? x)))

(defmacro.xt ^{:standalone true}
  is-array?
  "checks if object is an is-arrayay"
  {:added "4.0"}
  ([x]
   (list 'x:is-array? x)))

(defmacro.xt ^{:standalone true}
  is-object?
  "checks if is-objectect is a map type"
  {:added "4.0"}
  ([x]
   (list 'x:is-object? x)))

;;
;; XLANG OFFSET
;;

(defmacro.xt ^{:standalone true}
  first
  "gets the first item"
  {:added "4.0"}
  ([arr]
   (list 'x:get-idx arr '(x:offset))))

(defmacro.xt ^{:standalone true}
  second
  "gets the second item"
  {:added "4.0"}
  ([arr]
   (list 'x:get-idx arr '(x:offset 1))))

(defmacro.xt ^{:standalone true}
  nth
  "gets the nth item (index 0)"
  {:added "4.0"}
  ([arr i]
   (list 'x:get-idx arr (list 'x:offset i))))

(defmacro.xt ^{:standalone true}
  last
  "gets the last item"
  {:added "4.0"}
  ([arr]
   (list 'x:get-idx arr (list '+
                               (list 'x:len arr)
                               (list 'x:offset -1)))))

(defmacro.xt ^{:standalone true}
  second-last
  "gets the second-last item"
  {:added "4.0"}
  ([arr]
   (list 'x:get-idx arr (list '+
                              (list 'x:len arr)
                              (list 'x:offset -2)))))

;;
;; XLANG BIT
;;

(defmacro.xt ^{:standalone true}
  bit-and
  "bit and operation"
  {:added "4.0"}
  ([a b] (list 'x:bit-and a b)))

(defmacro.xt ^{:standalone true}
  bit-or
  "bit or operation"
  {:added "4.0"}
  ([a b] (list 'x:bit-or a b)))

(defmacro.xt ^{:standalone true}
  bit-xor
  "bit xor operation"
  {:added "4.0"}
  ([a b] (list 'x:bit-xor a b)))

(defmacro.xt ^{:standalone true}
  bit-lshift
  "bit left shift"
  {:added "4.0"}
  ([x n] (list 'x:bit-lshift x n)))

(defmacro.xt ^{:standalone true}
  bit-rshift
  "bit right shift"
  {:added "4.0"}
  ([x n] (list 'x:bit-rshift x n)))


;;
;; XLANG LU
;;

(defmacro.xt ^{:standalone true}
  lu-create
  "creates a lookup"
  {:added "4.0"}
  ([]
   (list 'x:lu-create)))

(defmacro.xt ^{:standalone true}
  lu-eq
  "equality of objects"
  {:added "4.0"}
  ([obj m]
   (list 'x:lu-eq obj m)))

(defmacro.xt ^{:standalone true}
  lu-get
  "gets value given an object"
  {:added "4.0"}
  ([lu obj]
   (list 'x:lu-get lu obj)))

(defmacro.xt ^{:standalone true}
  lu-set
  "sets value given an object"
  {:added "4.0"}
  ([lu obj val]
   (list 'x:lu-set lu obj val)))

(defmacro.xt ^{:standalone true}
  lu-del
  "deletes value given an object"
  {:added "4.0"}
  ([lu obj]
   (list 'x:lu-del lu obj)))

;;
;; XLANG GLOBAL
;;

(defmacro.xt ^{:standalone true}
  global-set
  "sets the global variable"
  {:added "4.0"}
  ([k val]
   (list 'x:global-set k val)))

(defmacro.xt ^{:standalone true}
  global-del
  "deletes a global var"
  {:added "4.0"}
  ([k]
   (list 'x:global-del k)))

(defmacro.xt ^{:standalone true}
  global-has?
  "has a global var"
  {:added "4.0"}
  ([k]
   (list 'x:global-has? k)))


;;
;; XLANG LU
;;   

(defmacro.xt ^{:standalone true}
  has-key?
  "checks that key in contained in object"
  {:added "4.0"}
  ([obj k]
   (list 'x:has-key? obj k)))

(defmacro.xt ^{:standalone true}
  del-key
  "deletes a key from object"
  {:added "4.0"}
  ([obj k]
   (list 'x:del-key obj k)))

(defmacro.xt ^{:standalone '(fn [obj k d]
                              (return (x:get-key obj k d)))}
  get-key
  "gets a value"
  {:added "4.0"}
  ([obj k & [default]]
   (list 'x:get-key obj k default)))

(defmacro.xt
  get-path
  "gets the value in the path"
  {:added "4.0"}
  ([obj path & [default]]
   (list 'x:get-path obj path default)))

(defmacro.xt ^{:standalone '(fn [arr i d]
                              (return (x:get-idx arr i d)))}
  get-idx
  "gets a value in the array (no offsets)"
  {:added "4.0"}
  ([arr i & [default]]
   (list 'x:get-idx arr i default)))

(defmacro.xt ^{:standalone true}
  set-key
  "sets a key in the object"
  {:added "4.0"}
  ([obj k val]
   (list 'x:set-key obj k val)))

(defmacro.xt ^{:standalone true}
  set-idx
  "sets an index in the array"
  {:added "4.0"}
  ([arr i val]
   (list 'x:set-idx arr i val)))

(defmacro.xt ^{:standalone true}
  copy-key
  "copies a key"
  {:added "4.0"}
  ([obj src k-or-arr]
   (list 'x:copy-key obj src k-or-arr)))

(defmacro.xt ^{:standalone true}
  swap-key
  "swaps a value in the key with a function"
  {:added "4.0"}
  ([obj k f & args]
   (assert (symbol? obj) "Needs to be a symbol")
   (list 'do*
         (list 'x:set-key obj k
               (clojure.core/apply list f (list 'x:get-key obj k)
                                   args)))))

;;
;; XLANG STRING
;;

(defmacro.xt ^{:standalone true}
  get-char
  "gets a char from string"
  {:added "4.0"}
  [s i]
  (list 'x:str-char s i))

(defmacro.xt ^{:standalone true}
  split
  "splits a string using a token"
  {:added "4.0"}
  ([s tok]
   (list 'x:str-split s tok)))

(defmacro.xt ^{:standalone true}
  join
  "joins an array using a seperator"
  {:added "4.0"}
  ([s arr]
   (list 'x:str-join s arr)))

(defmacro.xt ^{:standalone true}
  replace
  "replaces a string with another"
  {:added "4.0"}
  ([s tok replacement]
   (list 'x:str-replace s tok replacement)))

(defmacro.xt ^{:standalone true}
  index-of
  "returns index of character in string"
  {:added "4.0"}
  ([s tok]
   (list '- (list 'x:str-index-of s tok)
         (list 'x:offset))))

(defmacro.xt ^{:standalone true}
  substring
  "gets the substring"
  {:added "4.0"}
  ([s start & more]
   (clojure.core/apply list
                       'x:str-substring s
                       (list 'x:offset start)
                       more)))

(defmacro.xt ^{:standalone true}
  to-uppercase
  "converts string to uppercase"
  {:added "4.0"}
  ([s]
   (list 'x:str-to-upper s)))

(defmacro.xt ^{:standalone true}
  to-lowercase
  "converts string to lowercase"
  {:added "4.0"}
  ([s]
   (list 'x:str-to-lower s)))

(defmacro.xt ^{:standalone true}
  to-fixed
  "to fixed decimal places"
  {:added "4.0"}
  ([n digits]
   (list 'x:str-to-fixed n digits)))

(defmacro.xt ^{:standalone true}
  trim
  "trims a string"
  {:added "4.0"}
  ([s]
   (list 'x:str-trim s)))

(defmacro.xt ^{:standalone true}
  trim-left
  "trims a string on left"
  {:added "4.0"}
  ([s]
   (list 'x:str-trim-left s)))

(defmacro.xt ^{:standalone true}
  trim-right
  "trims a string on right"
  {:added "4.0"}
  ([s]
   (list 'x:str-trim-right s)))

(defmacro.xt ^{:standalone true}
  b64-encode
  "base64 encode"
  {:added "4.0"}
  ([s]
   (list 'x:b64-encode s)))

(defmacro.xt ^{:standalone true}
  b64-decode
  "base64 decode"
  {:added "4.0"}
  ([s]
   (list 'x:b64-decode s)))

(defmacro.xt ^{:standalone true}
  uri-encode
  "encodes string as uri"
  {:added "4.0"}
  ([s]
   (list 'x:uri-encode s)))

(defmacro.xt ^{:standalone true}
  uri-decode
  "decodes string as uri"
  {:added "4.0"}
  ([s]
   (list 'x:uri-decode s)))


;;
;; XLANG JSON
;;

(defmacro.xt ^{:standalone true}
  json-encode
  "encodes object to json"
  {:added "4.0"}
  ([obj]
   (list 'x:json-encode obj)))

(defmacro.xt ^{:standalone true}
  json-decode
  "decodes json to object"
  {:added "4.0"}
  ([s]
   (list 'x:json-decode s)))

(defmacro.xt  ^{:standalone true}
  json-push
  "pushes an element into a json string"
  {:added "4.0"}
  [json e]
  (list 'x:cat (list 'x:str-substring
                     json
                     '(x:offset 0)
                     (list '- (list 'x:str-len json) 1))
        ","
        e
        "]"))

(defmacro.xt  ^{:standalone true}
  json-push-first
  "pushes an element as the first element of a json string"
  {:added "4.0"}
  [json e]
  (list 'x:cat "[" e
        ","
        (list 'x:str-substring json
              '(x:offset 1))))


;;
;; XLANG CORE 
;;

(defmacro.xt ^{:standalone true}
  x:del
  "deletes a value"
  {:added "4.0"}
  ([obj]
   (list 'x:del obj)))

(defmacro.xt ^{:standalone 'x:shell}
  x:shell
  "calls a shell command"
  {:added "4.0"}
  ([& args]
   (clojure.core/apply list 'x:shell args)))

(defmacro.xt ^{:standalone true}
  x:type-native
  "gets the type native call"
  {:added "4.0"}
  ([x]
   (list 'x:type-native x)))

;;
;; XLANG CUSTOM
;;

(defmacro.xt ^{:standalone true}
  x:offset
  "gets the index offset"
  {:added "4.0"}
  ([& [n]]
   (list 'x:offset n)))

(defmacro.xt ^{:standalone true}
  x:offset
  "gets the index offset"
  {:added "4.0"}
  ([& [n]]
   (list 'x:offset n)))

(defmacro.xt ^{:standalone true}
  x:offset-rev
  "gets the reverse offset"
  {:added "4.0"}
  ([& [n]]
   (list 'x:offset-rev n)))

(defmacro.xt ^{:standalone true}
  x:offset-len
  "gets the offset length"
  {:added "4.0"}
  ([& [n]]
   (list 'x:offset-len n)))

(defmacro.xt ^{:standalone true}
  x:offset-rlen
  "gets the reverse offset length"
  {:added "4.0"}
  ([& [n]]
   (list 'x:offset-rlen n)))

(defmacro.xt ^{:standalone true}
  x:callback
  "gets the callback token"
  {:added "4.0"}
  ([]
   (list 'x:callback)))

;;
;; XLANG ARR
;;

(defmacro.xt ^{:standalone true}
  x:arr-push
  "pushes an element into the end of the array"
  {:added "4.0"}
  ([arr e]
   (list 'x:arr-push arr e)))

(defmacro.xt ^{:standalone true}
  x:arr-pop
  "pops an element out from end of the array"
  {:added "4.0"}
  ([arr]
   (list 'x:arr-pop arr)))

(defmacro.xt ^{:standalone true}
  x:arr-push-first
  "pushes an element into the start of the array"
  {:added "4.0"}
  ([arr e]
   (list 'x:arr-push-first arr e)))

(defmacro.xt ^{:standalone true}
  x:arr-pop-first
  "pops an element out from end of the array"
  {:added "4.0"}
  ([arr]
   (list 'x:arr-pop-first arr)))

(defmacro.xt ^{:standalone true}
  x:arr-insert
  "inserts an element into the array"
  {:added "4.0"}
  ([arr i e]
   (list 'x:arr-insert arr i e)))

;;
;; Thread Spawn
;;

(defmacro.xt ^{:standalone true}
  thread-spawn
  "spawns a thread"
  {:added "4.0"}
  ([thunk]
   (list 'x:thread-spawn thunk)))
