(ns kmi.lang.runtime.primitive
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [empty]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [kmi.lang.common-util :as util]
             [kmi.lang.type-list :as list]
             [kmi.lang.type-vector :as vec]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-hashset :as hs]
             [kmi.lang.common-hash :as common-hash]
             [xt.lang.spec-base :as xt]]})

(defn.xt math-pow [x y]
  (return (xt/x:m-pow x y)))

(defn.xt json-parse [source]
  (return (xt/x:json-decode source)))

(defn.xt json-stringify [value]
  (return (xt/x:json-encode value)))

(defn.xt console-log [args]
  (var out "")
  (xt/for:array [arg args]
    (when (< 0 (xt/x:len out))
      (:= out (xt/x:cat out " ")))
    (:= out (xt/x:cat out (util/show arg))))
  (xt/x:print out)
  (return nil))

(def.xt MATH {"PI" 3.141592653589793
              "pow" -/math-pow})
(def.xt CONSOLE {"log" {"type" "kmi.native"
                         "fn" -/console-log}})
(def.xt JSON-GLOBAL {"parse" -/json-parse
                     "stringify" -/json-stringify})

(defn.xt count-value
  "counts a value, dispatching between host and kmi types"
  {:added "4.1"}
  [x]
  (cond (util/is-managed? x)
        (return (p/size x))

        (or (xt/x:is-string? x)
            (xt/x:is-array? x))
        (return (xt/x:len x))

        true
        (return 0)))

(defn.xt first-value
  "returns the first element of a collection"
  {:added "4.1"}
  [x]
  (var arr (p/to-array x))
  (return (xt/x:first arr)))

(defn.xt rest-value
  "returns the remaining elements of a collection as a vector"
  {:added "4.1"}
  [x]
  (var arr (p/to-array x))
  (return (vec/vector-from-array (xt/x:arr-slice arr 1 (xt/x:len arr)))))

(defn.xt nth-value
  "returns the nth element of a collection"
  {:added "4.1"}
  [x i]
  (return (p/nth x i)))

(defn.xt str-value-array
  "concatenates an array of values into a string"
  {:added "4.1"}
  [args]
  (var out "")
  (xt/for:array [a args]
    (:= out (xt/x:cat out (util/show a))))
  (return out))

(defn.xt str-value
  "concatenates values into a string"
  {:added "4.1"}
  [(:.. args)]
  (return (-/str-value-array args)))

(defn.xt plus-array
  "adds an array of numbers"
  {:added "4.1"}
  [args]
  (when (== 0 (xt/x:len args))
    (return 0))
  (var out nil)
  (xt/for:array [a args]
    (if (xt/x:nil? out)
      (:= out a)
      (:= out (+ out a))))
  (return out))

(defn.xt plus
  "adds numbers"
  {:added "4.1"}
  [(:.. args)]
  (return (-/plus-array args)))

(defn.xt minus
  "subtracts numbers"
  {:added "4.1"}
  [a b]
  (return (- a b)))

(defn.xt multiply-array
  "multiplies an array of numbers"
  {:added "4.1"}
  [args]
  (when (== 0 (xt/x:len args))
    (return 1))
  (var out nil)
  (xt/for:array [a args]
    (if (xt/x:nil? out)
      (:= out a)
      (:= out (* out a))))
  (return out))

(defn.xt get-value-array
  "looks up a hashmap key from a fixed argument array"
  {:added "4.1"}
  [args]
  (var default-val nil)
  (when (> (xt/x:len args) 2)
    (:= default-val (xt/x:get-idx args (xt/x:offset 2))))
  (return (hm/hashmap-lookup-key
           (xt/x:get-idx args (xt/x:offset 0))
           (xt/x:get-idx args (xt/x:offset 1))
           default-val)))

(defn.xt multiply
  "multiplies numbers"
  {:added "4.1"}
  [(:.. args)]
  (return (-/multiply-array args)))

(defn.xt divide
  "divides numbers"
  {:added "4.1"}
  [a b]
  (return (/ a b)))

(defn.xt less-than
  {:added "4.1"}
  [a b]
  (return (< a b)))

(defn.xt greater-than
  {:added "4.1"}
  [a b]
  (return (> a b)))

(defn.xt less-than-or-equal
  {:added "4.1"}
  [a b]
  (return (<= a b)))

(defn.xt greater-than-or-equal
  {:added "4.1"}
  [a b]
  (return (>= a b)))

(defn.xt equal
  {:added "4.1"}
  [a b]
  (return (util/eq a b)))

(defn.xt not-equal
  {:added "4.1"}
  [a b]
  (return (not (util/eq a b))))

(defn.xt not-value
  {:added "4.1"}
  [x]
  (return (not x)))

(defn.xt type-value
  "returns the kmi/native class tag"
  {:added "4.1"}
  [x]
  (return (common-hash/native-class x)))

(defn.xt list-value-array
  {:added "4.1"}
  [args]
  (return (list/list-from-array args)))

(defn.xt list-value
  {:added "4.1"}
  [(:.. args)]
  (return (-/list-value-array args)))

(defn.xt vector-value-array
  {:added "4.1"}
  [args]
  (return (vec/vector-from-array args)))

(defn.xt vector-value
  {:added "4.1"}
  [(:.. args)]
  (return (-/vector-value-array args)))

(defn.xt hash-map-value-array
  {:added "4.1"}
  [args]
  (return (hm/hashmap-from-array args)))

(defn.xt hash-map-value
  {:added "4.1"}
  [(:.. args)]
  (return (-/hash-map-value-array args)))

(defn.xt hash-set-value-array
  {:added "4.1"}
  [args]
  (return (hs/hashset-from-array args)))

(defn.xt hash-set-value
  {:added "4.1"}
  [(:.. args)]
  (return (-/hash-set-value-array args)))

(defn.xt native-variadic
  "wraps an array-taking function for portable variadic dispatch"
  {:added "4.1"}
  [f]
  (return {"type" "kmi.native"
           "fn" f}))

(defn.xt apply-value
  "applies a function to an array of args"
  {:added "4.1"}
  [f args]
  (return (xt/x:apply f args)))

(def.xt PRIMITIVES
  {"+" (-/native-variadic -/plus-array)
   "-" -/minus
   "*" (-/native-variadic -/multiply-array)
   "/" -/divide
   "<" -/less-than
   ">" -/greater-than
   "<=" -/less-than-or-equal
   ">=" -/greater-than-or-equal
   "=" -/equal
   "==" -/equal
   "not=" -/not-equal
   "not" -/not-value
   "str" (-/native-variadic -/str-value-array)
   "count" -/count-value
   "first" -/first-value
   "rest" -/rest-value
   "nth" -/nth-value
   "list" (-/native-variadic -/list-value-array)
   "vector" (-/native-variadic -/vector-value-array)
   "hash-map" (-/native-variadic -/hash-map-value-array)
   "hash-set" (-/native-variadic -/hash-set-value-array)
   "get" (-/native-variadic -/get-value-array)
   "assoc" hm/hashmap-assoc
   "dissoc" hm/hashmap-dissoc
   "keys" hm/hashmap-keys
   "vals" hm/hashmap-vals
   "type" -/type-value
   "apply" -/apply-value
   "Math" -/MATH
   "console" -/CONSOLE
   "JSON" -/JSON-GLOBAL})

(defn.xt init-runtime
  "seeds a runtime with the primitive library in kmi.core"
  {:added "4.1"}
  [runtime]
  (var rt (xt/x:obj-clone runtime))
  (var namespaces (xt/x:obj-clone (xt/x:get-key rt "namespaces")))
  (var kmi-core (xt/x:obj-clone (xt/x:get-key namespaces "kmi.core")))
  (xt/x:set-key kmi-core "vars" (xt/x:obj-clone -/PRIMITIVES))
  (xt/x:set-key namespaces "kmi.core" kmi-core)
  (xt/x:set-key rt "namespaces" namespaces)
  (return rt))
