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

(def.xt MATH
  {"PI" 3.141592653589793
   "pow" (fn [x y] (return (xt/x:m-pow x y)))})

(def.xt CONSOLE {})

(def.xt JSON-GLOBAL
  {"parse" (fn [s] (return (xt/x:json-decode s)))
   "stringify" (fn [x] (return (xt/x:json-encode x)))})

(def.xt PROCESS {})

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
  (if (== 0 (xt/x:len arr))
    (return nil)
    (return (xt/x:first arr))))

(defn.xt rest-value
  "returns the remaining elements of a collection as a vector"
  {:added "4.1"}
  [x]
  (var arr (p/to-array x))
  (return (vec/vector [ (xt/x:unpack (xt/x:arr-slice arr 1 (xt/x:len arr)))])))

(defn.xt nth-value
  "returns the nth element of a collection"
  {:added "4.1"}
  [x i]
  (return (p/nth x i)))

(defn.xt str-value
  "concatenates values into a string"
  {:added "4.1"}
  [(:.. args)]
  (when (and (== 1 (xt/x:len args))
             (xt/x:is-array? (xt/x:first args)))
    (:= args (xt/x:first args)))
  (var out "")
  (xt/for:array [a args]
    (:= out (xt/x:cat out (util/show a))))
  (return out))

(defn.xt plus
  "adds numbers"
  {:added "4.1"}
  [(:.. args)]
  (when (and (== 1 (xt/x:len args))
             (xt/x:is-array? (xt/x:first args)))
    (:= args (xt/x:first args)))
  (when (== 0 (xt/x:len args))
    (return 0))
  (var out (xt/x:first args))
  (xt/for:index [i [(xt/x:offset 1) (xt/x:len args)]]
    (:= out (+ out (xt/x:get-idx args i))))
  (return out))

(defn.xt minus
  "subtracts numbers"
  {:added "4.1"}
  [a b]
  (return (- a b)))

(defn.xt multiply
  "multiplies numbers"
  {:added "4.1"}
  [(:.. args)]
  (when (and (== 1 (xt/x:len args))
             (xt/x:is-array? (xt/x:first args)))
    (:= args (xt/x:first args)))
  (when (== 0 (xt/x:len args))
    (return 1))
  (var out (xt/x:first args))
  (xt/for:index [i [(xt/x:offset 1) (xt/x:len args)]]
    (:= out (* out (xt/x:get-idx args i))))
  (return out))

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
  (return (or (xt/x:nil? x)
              (== false x))))

(defn.xt type-value
  "returns the kmi/native class tag"
  {:added "4.1"}
  [x]
  (return (common-hash/native-class x)))

(defn.xt list-value
  {:added "4.1"}
  [(:.. args)]
  (when (and (== 1 (xt/x:len args))
             (xt/x:is-array? (xt/x:first args)))
    (:= args (xt/x:first args)))
  (return (list/list args)))

(defn.xt vector-value
  {:added "4.1"}
  [(:.. args)]
  (when (and (== 1 (xt/x:len args))
             (xt/x:is-array? (xt/x:first args)))
    (:= args (xt/x:first args)))
  (return (vec/vector args)))

(defn.xt hash-map-value
  {:added "4.1"}
  [(:.. args)]
  (when (and (== 1 (xt/x:len args))
             (xt/x:is-array? (xt/x:first args)))
    (:= args (xt/x:first args)))
  (return (hm/hashmap args)))

(defn.xt hash-set-value
  {:added "4.1"}
  [(:.. args)]
  (when (and (== 1 (xt/x:len args))
             (xt/x:is-array? (xt/x:first args)))
    (:= args (xt/x:first args)))
  (return (hs/hashset args)))

(defn.xt variadic-fn?
  "checks for primitives that receive their arguments as one array on Dart"
  {:added "4.1"}
  [f]
  (return (or (== f -/str-value)
              (== f -/plus)
              (== f -/multiply)
              (== f -/list-value)
              (== f -/vector-value)
              (== f -/hash-map-value)
              (== f -/hash-set-value))))

(defn.xt invoke-variadic
  "invokes an array-backed variadic primitive portably"
  {:added "4.1"}
  [f args]
  (cond (== f -/str-value)      (return (-/str-value args))
        (== f -/plus)           (return (-/plus args))
        (== f -/multiply)       (return (-/multiply args))
        (== f -/list-value)     (return (-/list-value args))
        (== f -/vector-value)   (return (-/vector-value args))
        (== f -/hash-map-value) (return (-/hash-map-value args))
        (== f -/hash-set-value) (return (-/hash-set-value args))
        :else                   (return nil)))

(defn.xt apply-value
  "applies a function to an array of args"
  {:added "4.1"}
  [f args]
  (if (-/variadic-fn? f)
    (return (-/invoke-variadic f args))
    (return (xt/x:apply f args))))

(defn.xt get-value
  "looks up a hashmap key with a nil default"
  {:added "4.1"}
  [m k]
  (return (hm/hashmap-lookup-key m k nil)))

(def.xt PRIMITIVES
  {"+" -/plus
   "-" -/minus
   "*" -/multiply
   "/" -/divide
   "<" -/less-than
   ">" -/greater-than
   "<=" -/less-than-or-equal
   ">=" -/greater-than-or-equal
   "=" -/equal
   "==" -/equal
   "not=" -/not-equal
   "not" -/not-value
   "str" -/str-value
   "count" -/count-value
   "first" -/first-value
   "rest" -/rest-value
   "nth" -/nth-value
   "list" -/list-value
   "vector" -/vector-value
   "hash-map" -/hash-map-value
   "hash-set" -/hash-set-value
   "get" -/get-value
   "assoc" hm/hashmap-assoc
   "dissoc" hm/hashmap-dissoc
   "keys" hm/hashmap-keys
   "vals" hm/hashmap-vals
   "type" -/type-value
   "apply" -/apply-value
   "Math" -/MATH
   "console" -/CONSOLE
   "JSON" -/JSON-GLOBAL
   "process" -/PROCESS})

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
