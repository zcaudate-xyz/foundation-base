(ns xt.lang.base-lib
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.template :as template])
  (:refer-clojure :exclude [abs bit-and bit-or bit-xor type get-in identity inc dec zero? pos? neg? even? odd? max min mod quot cat eval apply print nil? fn? first second nth replace fn? last sort sort-by throw]))

(l/script :xtalk
  {:require [[xt.lang.base-macro :as k]]})

(l/intern-macros :xtalk 'xt.lang.base-macro)

(defspec.xt AnyArray
  [:xt/array :xt/any])

(defspec.xt AnyDict
  [:xt/dict :xt/str :xt/any])

(defspec.xt UnaryFn
  [:fn [:xt/any] :xt/any])

(defspec.xt Predicate
  [:fn [:xt/any] :xt/bool])

(defspec.xt MaybeUnaryFn
  [:fn [:xt/any] [:xt/maybe :xt/any]])

(defspec.xt CounterFn
  [:fn [] :xt/num])

(defspec.xt Pair
  [:xt/tuple :xt/str :xt/any])

(defspec.xt StringPair
  [:xt/tuple [:xt/maybe :xt/str] :xt/str])

(defspec.xt prototype-create
  [:fn [AnyDict] AnyDict])

(defspec.xt type-native
  [:fn [:xt/any] :xt/str])

(defspec.xt type-class
  [:fn [:xt/any] :xt/str])

(defspec.xt fn?
  [:fn [:xt/any] :xt/bool])

(defspec.xt arr?
  [:fn [:xt/any] :xt/bool])

(defspec.xt obj?
  [:fn [:xt/any] :xt/bool])

(defspec.xt id-fn
  [:fn [AnyDict] [:xt/maybe :xt/any]])

(defspec.xt key-fn
  [:fn [:xt/any] UnaryFn])

(defspec.xt eq-fn
  [:fn [:xt/any :xt/any] Predicate])

(defspec.xt inc-fn
  [:fn [[:xt/maybe :xt/num]] CounterFn])

(defspec.xt identity
  [:fn [:xt/any] :xt/any])

(defspec.xt noop
  [:fn [] :xt/nil])

(defspec.xt T
  [:fn [:xt/any] :xt/bool])

(defspec.xt F
  [:fn [:xt/any] :xt/bool])

(defspec.xt step-nil
  [:fn [:xt/any :xt/any] :xt/nil])

(defspec.xt step-thrush
  [:fn [:xt/any UnaryFn] :xt/any])

(defspec.xt step-call
  [:fn [UnaryFn :xt/any] :xt/any])

(defspec.xt step-push
  [:fn [AnyArray :xt/any] AnyArray])

(defspec.xt step-set-key
  [:fn [AnyDict :xt/str :xt/any] AnyDict])

(defspec.xt step-set-fn
  [:fn [AnyDict :xt/str] UnaryFn])

(defspec.xt step-set-pair
  [:fn [AnyDict Pair] AnyDict])

(defspec.xt step-del-key
  [:fn [AnyDict :xt/str] AnyDict])

(defspec.xt starts-with?
  [:fn [:xt/str :xt/str] :xt/bool])

(defspec.xt ends-with?
  [:fn [:xt/str :xt/str] :xt/bool])

(defspec.xt capitalize
  [:fn [:xt/str] :xt/str])

(defspec.xt decapitalize
  [:fn [:xt/str] :xt/str])

(defspec.xt pad-left
  [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defspec.xt pad-right
  [:fn [:xt/str :xt/num :xt/str] :xt/str])

(defspec.xt pad-lines
  [:fn [:xt/str :xt/num] :xt/str])

(defspec.xt mod-pos
  [:fn [:xt/num :xt/num] :xt/num])

(defspec.xt mod-offset
  [:fn [:xt/num :xt/num] :xt/num])

(defspec.xt gcd
  [:fn [:xt/num :xt/num] :xt/num])

(defspec.xt lcm
  [:fn [:xt/num :xt/num] :xt/num])

(defspec.xt mix
  [:fn [:xt/num :xt/num :xt/num] :xt/num])

(defspec.xt sign
  [:fn [:xt/num] :xt/num])

(defspec.xt round
  [:fn [:xt/num] :xt/num])

(defspec.xt clamp
  [:fn [:xt/num :xt/num :xt/num] :xt/num])

(defspec.xt bit-count
  [:fn [:xt/num] :xt/num])

(defspec.xt sym-full
  [:fn [[:xt/maybe :xt/str] :xt/str] :xt/str])

(defspec.xt sym-name
  [:fn [:xt/str] :xt/str])

(defspec.xt sym-ns
  [:fn [:xt/str] [:xt/maybe :xt/str]])

(defspec.xt sym-pair
  [:fn [:xt/str] StringPair])

(defspec.xt is-empty?
  [:fn [:xt/any] :xt/bool])

(defspec.xt arr-lookup
  [:fn [AnyArray] AnyDict])

(defspec.xt arr-every
  [:fn [AnyArray Predicate] :xt/bool])

(defspec.xt arr-some
  [:fn [AnyArray Predicate] :xt/bool])

(defspec.xt arr-map
  [:fn [AnyArray UnaryFn] AnyArray])

(defspec.xt arr-clone
  [:fn [AnyArray] AnyArray])

(defspec.xt arr-append
  [:fn [AnyArray AnyArray] AnyArray])

(defspec.xt arr-slice
  [:fn [AnyArray :xt/num :xt/num] AnyArray])

(defspec.xt arr-filter
  [:fn [AnyArray Predicate] AnyArray])

(defspec.xt arr-keep
  [:fn [AnyArray MaybeUnaryFn] AnyArray])

(defspec.xt arr-group-by
  [:fn [AnyArray UnaryFn] [:xt/dict :xt/str AnyArray]])

(defspec.xt arr-range
  [:fn [:xt/any] AnyArray])

(defspec.xt arrayify
  [:fn [:xt/any] AnyArray])

(defspec.xt obj-empty?
  [:fn [AnyDict] :xt/bool])

(defspec.xt obj-not-empty?
  [:fn [AnyDict] :xt/bool])

(defspec.xt obj-first-key
  [:fn [AnyDict] [:xt/maybe :xt/str]])

(defspec.xt obj-first-val
  [:fn [AnyDict] [:xt/maybe :xt/any]])

(defspec.xt obj-keys
  [:fn [AnyDict] [:xt/array :xt/str]])

(defspec.xt obj-vals
  [:fn [AnyDict] AnyArray])

(defspec.xt obj-pairs
  [:fn [AnyDict] [:xt/array Pair]])

(defspec.xt obj-clone
  [:fn [AnyDict] AnyDict])

(defspec.xt obj-assign
  [:fn [AnyDict AnyDict] AnyDict])

(defspec.xt obj-del
  [:fn [AnyDict :xt/str] AnyDict])

(defspec.xt obj-pick
  [:fn [AnyDict [:xt/array :xt/str]] AnyDict])

(defspec.xt obj-omit
  [:fn [AnyDict [:xt/array :xt/str]] AnyDict])

(defspec.xt get-in
  [:fn [AnyDict AnyArray] :xt/any])

(defspec.xt path-fn
  [:fn [AnyArray] UnaryFn])

(defspec.xt not-empty?
  [:fn [:xt/any] :xt/bool])

(defspec.xt get-data
  [:fn [:xt/any] :xt/any])

(defspec.xt get-spec
  [:fn [:xt/any] :xt/any])

(defspec.xt split-long
  [:fn [:xt/str :xt/num] [:xt/array :xt/str]])

(defspec.xt str-rand
  [:fn [:xt/num] :xt/str])

(defspec.xt prototype-spec
  [:fn [AnyArray] AnyDict])

(defspec.xt with-delay
  [:fn [[:fn [] :xt/any] :xt/num] :xt/any])

;;
;; TYPE
;;

(defn.xt prototype-create
  "creates the prototype map"
  {:added "4.0"}
  ([m]
   (xt/x:prototype-create m)))

(defn.xt type-native
  "gets the native type"
  {:added "4.0"}
  ([obj]
   (xt/x:type-native obj)))

(defn.xt type-class
  "gets the type of an object"
  {:added "4.0"}
  ([x]
   (var ntype (-/type-native x))
   (if (== ntype "object")
     (return (xt/x:get-key x "::" ntype))
     (return ntype))))

(defn.xt
  fn?
  "checks if object is a function type"
  {:added "4.0"}
  ([x]
   (return (xt/x:is-function? x))))

(defn.xt
  arr?
  "checks if object is an array"
  {:added "4.0"}
  ([x]
   (return (xt/x:is-array? x))))

(defn.xt
  obj?
  "checks if object is a map type"
  {:added "4.0"}
  ([x]
   (return (xt/x:is-object? x))))

(defn.xt
  id-fn
  "gets the id for an object"
  {:added "4.0"}
  ([x]
   (return (xt/x:get-key x "id"))))

(defn.xt
  key-fn
  "creates a key access function"
  {:added "4.0"}
  ([k]
   (return (fn:> [x] (xt/x:get-key x k)))))

(defn.xt
  eq-fn
  "creates a eq comparator function"
  {:added "4.0"}
  ([k v]
   (return (fn:> [x]
             (:? (-/fn? v)
                 (v (xt/x:get-key x k))
                 (== v (xt/x:get-key x k)))))))

(defn.xt
  inc-fn
  "creates a increment function by closure"
  {:added "4.0"}
  ([init]
   (var i := init)
   (when (xt/x:nil? i)
     (:= i -1))
   (var inc-fn
        (fn []
          (:= i (+ i 1))
          (return i)))
   (return inc-fn)))


(defn.xt
  identity
  "identity function"
  {:added "4.0"}
  ([x] (return x)))

(defn.xt
  noop
  "always a no op"
  {:added "4.0"}
  ([] (return nil)))

(defn.xt
  T
  "always true"
  {:added "4.0"}
  ([x] (return true)))

(defn.xt
  F
  "always false"
  {:added "4.0"}
  ([x] (return false)))


;;
;; ACCUMULATOR
;;

(defn.xt step-nil
  "nil step for fold"
  {:added "4.0"}
  [obj pair]
  (return nil))

(defn.xt step-thrush
  "thrush step for fold"
  {:added "4.0"}
  ([x f]
   (return (f x))))

(defn.xt step-call
  "call step for fold"
  {:added "4.0"}
  ([f x]
   (return (f x))))

(defn.xt step-push
  "step to push element into arr"
  {:added "4.0"}
  ([arr e]
   (xt/x:arr-push arr e)
   (return arr)))

(defn.xt step-set-key
  "step to set key in object"
  {:added "4.0"}
  ([obj k v]
   (xt/x:set-key obj k v)
   (return obj)))

(defn.xt step-set-fn
  "creates a set key function"
  {:added "4.0"}
  ([obj k]
   (return (fn:> [v] (-/step-set-key obj k v)))))

(defn.xt step-set-pair
  "step to set key value pair in object"
  {:added "4.0"}
  ([obj e]
   (xt/x:set-key obj
              (xt/x:first e)
              (xt/x:second e))
   (return obj)))

(defn.xt step-del-key
  "step to delete key in object"
  {:added "4.0"}
  ([obj k]
   (xt/x:del-key obj k)
   (return obj)))


;;
;; STRING
;;

(defn.xt starts-with?
  "check for starts with"
  {:added "4.0"}
  [s match]
  (return (== (xt/x:str-substring s
                               (xt/x:offset)
                               (xt/x:str-len match))
              match)))

(defn.xt ends-with?
  "check for ends with"
  {:added "4.0"}
  [s match]
  (return (== match
              (xt/x:str-substring
               s
               (xt/x:offset (- (xt/x:str-len s)
                            (xt/x:str-len match)))
               (xt/x:str-len s)))))


(defn.xt capitalize
  "uppercases the first letter"
  {:added "4.0"}
  ([s]
   (return (xt/x:cat (xt/x:str-to-upper
                   (xt/x:str-substring s
                                    (xt/x:offset 0)
                                    1))
                  (xt/x:str-substring s
                                   (xt/x:offset 1))))))

(defn.xt decapitalize
  "lowercases the first letter"
  {:added "4.0"}
  ([s]
   (return (xt/x:cat (xt/x:str-to-lower
                   (xt/x:str-substring s
                                    (xt/x:offset 0)
                                    1))
                  (xt/x:str-substring s
                                   (xt/x:offset 1))))))

(defn.xt pad-left
  "pads string with n chars on left"
  {:added "4.0"}
  ([s n ch]
   (var l := (- n (xt/x:offset (xt/x:str-len s))))
   (var out := s)
   (xt/for:index [i [0 l]]
     (:= out (xt/x:cat ch out)))
   (return out)))

(defn.xt pad-right
  "pads string with n chars on right"
  {:added "4.0"}
  ([s n ch]
   (var l := (- n (xt/x:offset (xt/x:str-len s))))
   (var out := s)
   (xt/for:index [i [0 l]]
     (:= out (xt/x:cat out ch)))
   (return out)))

(defn.xt pad-lines
  "pad lines with starting chars"
  {:added "4.0"}
  [s n ch]
  (var lines (xt/x:split s "\n"))
  (var out := "")
  (xt/for:array [line lines]
    (when (< 0 (xt/x:len out))
      (:= out (xt/x:cat out "\n")))
    (:= out (xt/x:cat out (-/pad-left "" n " ") line)))
  (return out))


;;
;; NUMBER
;;

(defn.xt mod-pos
  "gets the positive mod"
  {:added "4.0"}
  [val modulo]
  (var out (mod val modulo))
  (return
   (:? (< out 0)
       (+ out modulo)
       out)))

(defn.xt mod-offset
  "calculates the closet offset"
  {:added "4.0"}
  [pval nval modulo]
  (var offset (mod (- nval pval) modulo))
  (cond (> (xt/x:abs offset)
           (/ modulo 2))
        (cond (> offset 0)
              (return (- offset modulo))

              :else
              (return (+ offset modulo)))

        :else
        (return offset)))

(defn.xt gcd
  "greatest common denominator"
  {:added "4.0"}
  [a b]
  (return (:? (== 0 b)
              a
              (-/gcd b (mod a b)))))

(defn.xt lcm
  "lowest common multiple"
  {:added "4.0"}
  [a b]
  (return (/ (* a b)
             (-/gcd a b))))

(defn.xt mix
  "mixes two values with a fraction"
  {:added "4.0"}
  [x0 x1 v f]
  (when (xt/x:nil? f)
    (:= f -/identity))
  (return (+ x0 (* (- x1 x0)
                   (f v)))))

(defn.xt sign
  "gets the sign of"
  {:added "4.0"}
  [x]
  (cond (== x 0) (return 0)
        (< x 0)  (return -1)
        :else (return 1)))

(defn.xt round
  "rounds to the nearest integer"
  {:added "4.0"}
  [x]
  (return (xt/x:floor (+ x 0.5))))

(defn.xt clamp
  "clamps a value between min and max"
  {:added "4.0"}
  [min max v]
  (cond (< v min)
        (return min)

        (< max v)
        (return max)

        :else
        (return v)))

(defn.xt bit-count
  "get the bit count"
  {:added "4.0"}
  [x]
  (var v0 (- x (-/bit-and (-/bit-rshift x 1) (:- "0x55555555"))))
  (var v1 (+ (-/bit-and v0 (:- "0x33333333"))
             (-/bit-and (-/bit-rshift v0 2) (:- "0x33333333"))))
  (return
   (-/bit-rshift
    (* (-/bit-and (+ v1 (-/bit-rshift v1 4))
                  (:- "0xF0F0F0F"))
       (:- "0x1010101"))
    24)))


;;
;; SYMBOL
;;

(defn.xt sym-full
  "creates a sym"
  {:added "4.0"}
  [ns name]
  (if (xt/x:nil? ns)
    (return name)
    (return (xt/x:cat ns "/" name))))

(defn.xt sym-name
  "gets the name part of the sym"
  {:added "4.0"}
  [sym]
  (var idx (xt/x:str-index-of sym "/"))
  (return (xt/x:str-substring sym
                           (xt/x:offset (- idx (xt/x:offset-len))))))

(defn.xt sym-ns
  "gets the namespace part of the sym"
  {:added "4.0"}
  [sym]
  (var idx (xt/x:str-index-of sym "/"))
  (if (< 0 idx)
    (return (xt/x:str-substring sym 0 (-  idx (xt/x:offset))))
    (return nil)))

(defn.xt sym-pair
  "gets the sym pair"
  {:added "4.0"}
  [sym]
  (return [(-/sym-ns sym)
           (-/sym-name sym)]))

;;
;; FN
;;

(defn.xt is-empty?
  "checks that array is empty"
  {:added "4.0"}
  [res]
  (cond (xt/x:nil? res) (return true)
        (xt/x:is-string? res) (return (== 0 (xt/x:str-len res)))
        (-/arr? res) (return (== 0 (xt/x:len res)))
        (-/obj? res)
        (do (xt/for:object [[k v] res]
              (return false))
            (return true))

        :else
        (do (xt/x:err (xt/x:cat "Invalid type - "
                          (-/type-native res)
                          " - "
                          (xt/x:to-string res))))))

(defn.xt arr-lookup
  "constructs a lookup given keys"
  {:added "4.0"}
  [arr]
  (var out := {})
  (xt/for:array [k arr]
    (xt/x:set-key out k true))
  (return out))

(defn.xt arr-omit
  "emits index from new array"
  {:added "4.0"}
  ([arr i]
   (var out := [])
   (xt/for:array [[j e] arr]
     (when (not= (xt/x:offset i) j)
       (xt/x:arr-push out e)))
   (return out)))

(defn.xt arr-reverse
  "reverses the array"
  {:added "4.0"}
  [arr]
  (var out := [])
  (xt/for:index [i [(xt/x:len arr)
                   (xt/x:offset)
                   -1]]
    (xt/x:arr-push out (xt/x:get-idx arr (xt/x:offset-rev i))))
  (return out))
  
(defn.xt arr-zip
  "zips two arrays together into a map"
  {:added "4.0"}
  [ks vs]
  (var out := {})
  (xt/for:array [[i k] ks]
    (xt/x:copy-key out vs [k i]))
  (return out))

(defn.xt arr-map
  "maps a function across an array"
  {:added "4.0"}
  ([arr f]
   (var out := [])
   (xt/for:array [e arr]
     (xt/x:arr-push out (f e)))
   (return out)))

(defn.xt arr-clone
  "clones an array"
  {:added "4.0"}
  ([arr]
   (var out := [])
   (xt/for:array [e arr]
     (xt/x:arr-push out e))
   (return out)))

(defn.xt arr-append
  "appends to the end of an array"
  {:added "4.0"}
  ([arr other]
   (xt/for:array [e other]
     (xt/x:arr-push arr e))
   (return arr)))

(defn.xt arr-slice
  "slices an array"
  {:added "4.0"}
  ([arr start finish]
   (var out := [])
   (xt/for:index [i [(xt/x:offset start) (or finish
                                         (-/len arr))]]
     (xt/x:arr-push out (xt/x:get-idx arr i)))
   (return out)))

(defn.xt arr-rslice
  "gets the reverse of a slice"
  {:added "4.0"}
  ([arr start finish]
   (var out := [])
   (xt/for:index [i [(xt/x:offset start) finish]]
     (xt/x:arr-push-first out (xt/x:get-idx arr i)))
   (return out)))

(defn.xt arr-tail
  "gets the tail of the array"
  {:added "4.0"}
  ([arr n]
   (var t  (xt/x:len arr))
   (return (-/arr-rslice arr (xt/x:max (- t n) 0) t))))

(defn.xt arr-mapcat
  "maps an array function, concatenting results"
  {:added "4.0"}
  [arr f]
  (var out := [])
  (xt/for:array [e arr]
    (var res (f e))
    (if (xt/x:not-nil? res)
      (xt/for:array [v res]
        (xt/x:arr-push out v))))
  (return out))

(defn.xt arr-partition
  "partitions an array into arrays of length n"
  {:added "4.0"}
  [arr n]
  (var out := [])
  (var i := 0)
  (var sarr := [])
  (xt/for:array [e arr]
    (when (== i n)
      (xt/x:arr-push out sarr)
      (:= i 0)
      (:= sarr []))
    (xt/x:arr-push sarr e)
    (:= i (+ i 1)))
  (when (< 0 (xt/x:len sarr))
    (xt/x:arr-push out sarr))
  (return out))

(defn.xt arr-filter
  "applies a filter across an array"
  {:added "4.0"}
  ([arr pred]
   (var out := [])
   (xt/for:array [e arr]
     (if (pred e)
       (xt/x:arr-push out e)))
   (return out)))

(defn.xt arr-keep
  "keeps items in an array if output is not nil"
  {:added "4.0"}
  [arr f]
  (var out := [])
  (xt/for:array [e arr]
    (var v (f e))
    (if (xt/x:not-nil? v)
      (xt/x:arr-push out v)))
  (return out))

(defn.xt arr-keepf
  "keeps items in an array with transform if predicate holds"
  {:added "4.0"}
  [arr pred f]
  (var out := [])
  (xt/for:array [e arr]
   (if (pred e)
     (xt/x:arr-push out (f e))))
  (return out))

(defn.xt arr-juxt
  "constructs a map given a array of pairs"
  {:added "4.0"}
  [arr key-fn val-fn]
  (var out := {})
  (when (xt/x:not-nil? arr)
    (xt/for:array [e arr]
      (xt/x:set-key out (key-fn e)
                 (val-fn e))))
  (return out))

(defn.xt arr-foldl
  "performs reduce on an array"
  {:added "4.0"}
  [arr f init]
  (var out := init)
  (xt/for:array [e arr]
    (:= out (f out e)))
  (return out))

(defn.xt arr-foldr
  "performs right reduce"
  {:added "4.0"}
  [arr f init]
  (var out := init)
  (xt/for:index [i [(xt/x:len arr)
                   (xt/x:offset)
                   -1]]
    (:= out (f out (xt/x:get-idx arr (xt/x:offset-rev i)))))
  (return out))

(defn.xt arr-pipel
  "thrushes an input through a function pipeline"
  {:added "4.0"}
  [arr e]
  (return (-/arr-foldl arr -/step-thrush e)))

(defn.xt arr-piper
  "thrushes an input through a function pipeline from reverse"
  {:added "4.0"}
  [arr e]
  (return (-/arr-foldr arr -/step-thrush e)))

(defn.xt arr-group-by
  "groups elements by key and view functions"
  {:added "4.0"}
  ([arr key-fn view-fn]
   (var out := {})
   (when (xt/x:not-nil? arr)
     (xt/for:array [e arr]
      (var g := (key-fn e))
      (var garr := (xt/x:get-key out g []))
      (xt/x:set-key out g [])
      (xt/x:arr-push garr (view-fn e))
      (xt/x:set-key out g garr)))
   (return out)))

(defn.xt arr-range
  "creates a range array"
  {:added "4.0"}
  [x]
  (var arr    := (:? (xt/x:is-array? x) x [x]))
  (var arrlen := (xt/x:len arr))
  (var start  := (:? (< 1 arrlen) (xt/x:first arr) 0))
  (var finish := (:? (< 1 arrlen) (xt/x:second arr) (xt/x:first arr)))
  (var step   := (:? (< 2 arrlen) (xt/x:nth arr 2) 1))
  (var out := [start])
  (var i := (+ step start))
  (cond (and (< 0 step)
             (< start finish))
        (while (< i finish)
          (xt/x:arr-push out i)
          (:= i (+ i step)))
        
        (and (> 0 step)
             (< finish start))
        (while (> i finish)
          (xt/x:arr-push out i)
          (:= i (+ i step)))
        
        :else (return []))
  (return out))

(defn.xt arr-intersection
  "gets the intersection of two arrays"
  {:added "4.0"}
  [arr other]
  (var lu  := (-/arr-lookup arr) :inline)
  (var out := [])
  (xt/for:array [e other]
    (if (xt/x:has-key? lu e)
      (xt/x:arr-push out e)))
  (return out))

(defn.xt arr-difference
  "gets the difference of two arrays"
  {:added "4.0"}
  [arr other]
  (var lu  := (-/arr-lookup arr) :inline)
  (var out := [])
  (xt/for:array [e other]
    (if (not (xt/x:has-key? lu e))
      (xt/x:arr-push out e)))
  (return out))

(defn.xt arr-union
  "gets the union of two arrays"
  {:added "4.0"}
  [arr other]
  (var lu := {})
  (xt/for:array [e arr]
    (xt/x:set-key lu e e))
  (xt/for:array [e other]
    (xt/x:set-key lu e e))

  (var out := [])
  (xt/for:object [[_ v] lu]
    (xt/x:arr-push out v))
  (return out))

(defn.xt arr-sort
  "arr-sort using key function and comparator"
  {:added "4.0"}
  [arr key-fn comp-fn]
  (var out := (xt/x:arr-clone arr))
  (xt/x:arr-sort out key-fn comp-fn)
  (return out))

(defn.xt arr-sorted-merge
  "performs a merge on two sorted arrays"
  {:added "4.0"}
  [arr brr comp-fn]
  (:= arr (or arr []))
  (:= brr (or brr []))
  (var alen (xt/x:len arr))
  (var blen (xt/x:len brr))
  (var i 0)
  (var j 0)
  (var k 0)
  (var out := [])
  (while (and (< i alen)
              (< j blen))
    (var aitem (xt/x:get-idx arr (xt/x:offset i)))
    (var bitem (xt/x:get-idx brr (xt/x:offset j)))
    (cond (comp-fn aitem bitem)
          (do (:= i (+ i 1)) 
              (xt/x:arr-push out aitem))

          :else
          (do (:= j (+ j 1))
              (xt/x:arr-push out bitem))))

  (while (< i alen)
    (var aitem (xt/x:get-idx arr (xt/x:offset i)))
    (:= i (+ i 1))
    (xt/x:arr-push out aitem))

  (while (< j blen)
    (var bitem (xt/x:get-idx brr (xt/x:offset j)))
    (:= j (+ j 1))
    (xt/x:arr-push out bitem))
  (return out))

(defn.xt arr-shuffle
  "shuffles the array"
  {:added "4.0"}
  [arr]
  (var tmp-val := nil)
  (var tmp-idx := nil)
  (var total (xt/x:len arr))
  (xt/for:index [i [(xt/x:offset) total]]
    (:= tmp-idx (+ (xt/x:offset) (xt/x:floor (* (xt/x:random) total))))
    (:= tmp-val (xt/x:get-idx arr tmp-idx))
    (xt/x:set-idx arr tmp-idx (xt/x:get-idx arr i))
    (xt/x:set-idx arr i tmp-val))
  (return arr))

(defn.xt arr-pushl
  "pushs an element into array"
  {:added "4.0"}
  [arr v n]
  (xt/x:arr-push arr v)
  (when (> (xt/x:len arr) n)
    (xt/x:arr-pop-first arr))
  (return arr))

(defn.xt arr-pushr
  "pushs an element into array"
  {:added "4.0"}
  [arr v n]
  (xt/x:arr-push-first arr v)
  (when (> (xt/x:len arr) n)
    (xt/x:arr-pop arr))
  (return arr))

(defn.xt arr-join
  "joins array with string"
  {:added "4.0"}
  [arr s]
  (return (-/join s arr)))

(defn.xt arr-interpose
  "puts element between array"
  {:added "4.0"}
  [arr elem]
  (var out := [])
  (-/for:array [e arr]
    (xt/x:arr-push out e)
    (xt/x:arr-push out elem))
  (xt/x:arr-pop out)
  (return out))

(defn.xt arr-repeat
  "repeat function or value n times"
  {:added "4.0"}
  [x n]
  (var out := [])
  (-/for:index [i [0 (- n (xt/x:offset))]]
    (xt/x:arr-push out (:? (xt/x:is-function? x)
                        (x)
                        x)))
  (return out))

(defn.xt arr-random
  "gets a random element from array"
  {:added "4.0"}
  [arr]
  (var idx (xt/x:floor (* (xt/x:len arr) (xt/x:random))))
  (return (xt/x:get-idx arr (xt/x:offset idx))))

(defn.xt arr-normalise
  "normalises array elements to 1"
  {:added "4.0"}
  [arr]
  (var total (-/arr-foldl arr -/add 0))
  (return (-/arr-map arr (fn:> [x] (/ x total)))))

(defn.xt arr-sample
  "samples array according to probability"
  {:added "4.0"}
  [arr dist]
  (var q (xt/x:random))
  (xt/for:array [[i p] dist]
    (:= q (- q p))
    (when (< q 0)
      (return (. arr [i])))))

(defn.xt arrayify
  "makes something into an array"
  {:added "4.0"}
  [x]
  (return
   (:? (xt/x:is-array? x)
       x
       
       (== nil x)
       []
       
       :else [x])))

;;
;; OBJ
;;

(defn.xt obj-empty?
  "checks that object is empty"
  {:added "4.0"}
  [obj]
  (xt/for:object [[k _] obj]
    (return false))
  (return true))

(defn.xt obj-not-empty?
  "checks that object is not empty"
  {:added "4.0"}
  [obj]
  (xt/for:object [[k _] obj]
    (return true))
  (return false))

(defn.xt obj-first-key
  "gets the first key"
  {:added "4.0"}
  [obj]
  (xt/for:object [[k _] obj]
    (return k))
  (return nil))

(defn.xt obj-first-val
  "gets the first val"
  {:added "4.0"}
  [obj]
  (xt/for:object [[_ v] obj]
    (return v))
  (return nil))

(defn.xt obj-keys
  "gets keys of an object"
  {:added "4.0"}
  [obj]
  (var out := [])
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k _] obj]
      (xt/x:arr-push out k)))
  (return out))

(defn.xt obj-vals
  "gets vals of an object"
  {:added "4.0"}
  [obj]
  (var out := [])
  (when (xt/x:not-nil? obj)
    (xt/for:object [[_ v] obj]
      (xt/x:arr-push out v)))
  (return out))

(defn.xt obj-pairs
  "creates entry pairs from object"
  {:added "4.0"}
  ([obj]
   (var out := [])
   (when (xt/x:not-nil? obj)
     (xt/for:object [[k v] obj]
       (xt/x:arr-push out [k v])))
   (return out)))

(defn.xt obj-clone
  "clones an object"
  {:added "4.0"}
  [obj]
  (var out := {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
      (xt/x:set-key out k v)))
  (return out))

(defn.xt obj-assign
  "merges key value pairs from into another"
  {:added "4.0"}
  [obj m]
  (when (xt/x:nil? obj)
    (:= obj {}))
  (if (xt/x:not-nil? m)
    (xt/for:object [[k v] m]
      (xt/x:set-key obj k v)))
  (return obj))

(defn.xt obj-assign-nested
  "merges objects at a nesting level"
  {:added "4.0"}
  ([obj m]
   (when (xt/x:nil? obj)
     (:= obj {}))
   (when (xt/x:not-nil? m)
     (xt/for:object [[k mv] m]
       (var v := (xt/x:get-key obj k))
       (cond (and (-/obj? mv)
                  (-/obj? v))
             (xt/x:set-key obj k (-/obj-assign-nested v mv))
             
             :else
             (xt/x:set-key obj k mv))))
   (return obj)))

(defn.xt obj-assign-with
  "merges second into first given a function"
  {:added "4.0"}
  [obj m f]
  (when (xt/x:not-nil? m)
    (var input (or m {}))
    (xt/for:object [[k mv] input]
      (xt/x:set-key obj k 
                 (:? (xt/x:has-key? obj k)
                     (f (xt/x:get-key obj k)
                        mv)
                     mv))))
  (return obj))

(defn.xt obj-from-pairs
  "creates an object from pairs"
  {:added "4.0"}
  ([pairs]
   (var out := {})
   (xt/for:array [pair pairs]
     (xt/x:set-key out
                (xt/x:first pair)
                (xt/x:second pair)))
   (return out)))

(defn.xt obj-del
  "deletes multiple keys"
  {:added "4.0"}
  [obj ks]
  (xt/for:array [k ks]
   (xt/x:del-key obj k))
  (return obj))

(defn.xt ^{:static/template true}
  obj-del-all
  "deletes all keys"
  {:added "4.0"}
  [obj]
  (xt/for:array [k (-/obj-keys obj)]
    (xt/x:del-key obj k))
  (return obj))

(defn.xt obj-pick
  "select keys in object"
  {:added "4.0"}
  [obj ks]
  (var out := {})
  (when (xt/x:nil? obj)
    (return out))
  (xt/for:array [k ks]
    (var v (xt/x:get-key obj k))
    (if (xt/x:not-nil? v)
      (xt/x:set-key out k v)))
  (return out))

(defn.xt obj-omit
  "new object with missing keys"
  {:added "4.0"}
  [obj ks]
  (var out := {})
  (var lu := {})
  (xt/for:array [k ks]
    (xt/x:set-key lu k true))
  (xt/for:object [[k v] obj]
    (if (not (xt/x:has-key? lu k))
      (xt/x:set-key out k v)))
  (return out))

(defn.xt obj-transpose
  "obj-transposes a map"
  {:added "4.0"}
  [obj]
  (var out := {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
      (xt/x:set-key out v k)))
  (return out))

(defn.xt obj-nest
  "creates a nested object"
  {:added "4.0"}
  [arr v]
  (var idx := (xt/x:len arr))
  (var out := v)
  (while true
    (if (== idx 0)
      (return out))
    (var nested := {})
    (var k (xt/x:get-idx arr (xt/x:offset-rev idx)))
    (xt/x:set-key nested k out)
    (:= out nested)
    (:= idx (- idx 1))))

(defn.xt obj-map
  "maps a function across the values of an object"
  {:added "4.0"}
  [obj f]
  (var out := {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
      (xt/x:set-key out k (f v))))
  (return out))

(defn.xt obj-filter
  "applies a filter across the values of an object"
  {:added "4.0"}
  [obj pred]
  (var out := {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k v] obj]
       (if (pred v)
         (xt/x:set-key out k v))))
  (return out))

(defn.xt obj-keep
  "applies a transform across the values of an object, keeping non-nil values"
  {:added "4.0"}
  [obj f]
  (var out := {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k e] obj]
      (var v (f e))
      (if (xt/x:not-nil? v)
        (xt/x:set-key out k v))))
  (return out))

(defn.xt obj-keepf
  "applies a transform and filter across the values of an object"
  {:added "4.0"}
  [obj pred f]
  (var out := {})
  (when (xt/x:not-nil? obj)
    (xt/for:object [[k e] obj]
      (if (pred e)
        (xt/x:set-key out k (f e)))))
  (return out))

(defn.xt obj-intersection
  "finds the intersection between map lookups"
  {:added "4.0"}
  [obj other]
  (var out := [])
  (xt/for:object [[k _] other]
    (if (xt/x:has-key? obj k)
      (xt/x:arr-push out k)))
  (return out))

(defn.xt obj-difference
  "finds the difference between two map lookups"
  {:added "4.0"}
  [obj other]
  (var out := [])
  (xt/for:object [[k _] other]
    (if (not (xt/x:has-key? obj k))
      (xt/x:arr-push out k)))
  (return out))

(defn.xt obj-keys-nested
  "gets nested keys"
  {:added "4.0"}
  [m path]
  (var out [])
  (xt/for:object [[k v] m]
    (var npath [(xt/x:unpack path)])
    (xt/x:arr-push npath k)
    
    (cond (-/obj? v)
          (do (xt/for:array [e (-/obj-keys-nested v npath)]
                (xt/x:arr-push out e)))
          
          :else
          (xt/x:arr-push out [npath v])))
  (return out))

;;
;; FLAT
;;

(defn.xt to-flat
  "flattens pairs of object into array"
  {:added "4.0"}
  ([obj]
   (var out := [])
   (cond (-/obj? obj)
         (xt/for:object [[k v] obj]
           (xt/x:arr-push out k)
           (xt/x:arr-push out v))
         
         (-/arr? obj)
         (xt/for:array [e obj]
           (xt/x:arr-push out (xt/x:first e))
           (xt/x:arr-push out (xt/x:second e))))
   (return out)))

(defn.xt from-flat
  "creates object from flattened pair array"
  {:added "4.0"}
  ([arr f init]
   (var out := init)
   (var k := nil)
   (xt/for:array [[i e] arr]
     (if (xt/x:even? (xt/x:offset i))
       (:= k e)
       (:= out (f out k e))))
   (return out)))

(defn.xt get-in
  "gets item in object"
  {:added "4.0"}
  [obj arr]
  (cond (xt/x:nil? obj)
        (return nil)
        
        (== 0 (xt/x:len arr))
        (return obj)

        (== 1 (xt/x:len arr))
        (return (xt/x:get-key obj (xt/x:first arr)))

        :else
        (do (var total := (xt/x:len arr))
            (var i := 0)
            (var curr := obj)
            (while (< i total)
              (var k (xt/x:get-idx arr (xt/x:offset i)))
              (:= curr (xt/x:get-key curr k))
              (if (xt/x:nil? curr)
                (return nil)
                (:= i (+ i 1))))
            (return curr))))

(defn.xt path-fn
  "TODO"
  {:added "4.0"}
  ([path]
   (return
    (fn:> [x] (-/get-in x path)))))

(defn.xt set-in
  "sets item in object"
  {:added "4.0"}
  [obj arr v]
  (cond (== 0 (xt/x:len (or arr [])))
        (return obj)
        
        (not (-/obj? obj))
        (return (-/obj-nest arr v))
        
        :else
        (do (var k := (xt/x:first arr))
            (var narr := (xt/x:arr-clone arr))
            (xt/x:arr-pop-first narr)
            (var child := (xt/x:get-key obj k))
            (if (== 0 (xt/x:len narr))
              (xt/x:set-key obj k v)
              (xt/x:set-key obj k (-/set-in child narr v)))
            (return obj))))

(defn.xt memoize-key
  "memoize for functions of single argument"
  {:added "4.0"}
  [f]
  (var cache {})
  (var cache-fn (fn [key]
                  (var res (f key))
                  (xt/x:set-key cache key res)
                  (return res)))
  (return (fn [key]
            (return (or (xt/x:get-key cache key)
                        (cache-fn key))))))

(defn.xt not-empty?
  "checks that array is not empty"
  {:added "4.0"}
  [res]
  (cond (xt/x:nil? res) (return false)
        (xt/x:is-string? res) (return (< 0 (xt/x:str-len res)))
        (-/arr? res) (return (< 0 (xt/x:len res)))
        (-/obj? res)
        (do (xt/for:object [[i v] res]
              (return true))
            (return false))

        :else
        (do (xt/x:err (xt/x:cat "Invalid type - "
                          (-/type-native res)
                          " - "
                          (xt/x:to-string res))))))

;;
;; NESTED
;;

(defn.xt eq-nested-loop
  "switch for nested check"
  {:added "4.0"}
  [src dst eq-obj eq-arr cache]
  (cond (and (-/obj? src) (-/obj? dst))
        (if (and cache
                 (xt/x:lu-get cache src)
                 (xt/x:lu-get cache dst))
          (return true)
          (return (eq-obj src dst eq-obj eq-arr (or cache (xt/x:lu-create)))))
        
        (and (-/arr? src) (-/arr? dst))
        (if (and cache
                 (xt/x:lu-get cache src)
                 (xt/x:lu-get cache dst))
          (return true)
          (return (eq-arr src dst eq-obj eq-arr (or cache (xt/x:lu-create)))))
        
        :else
        (return (== src dst))))

(defn.xt eq-nested-obj
  "checking object equality"
  {:added "4.0"}
  [src dst eq-obj eq-arr cache]
  (xt/x:lu-set cache src src)
  (xt/x:lu-set cache dst dst)
  (var ks-src (-/obj-keys src))
  (var ks-dst (-/obj-keys dst))
  (if (not= (xt/x:len ks-src) (xt/x:len ks-dst))
    (return false))
  (xt/for:array [k ks-src]
    (if (not (-/eq-nested-loop (xt/x:get-key src k)
                               (xt/x:get-key dst k)
                               eq-obj
                               eq-arr
                               cache))
      (return false)))
  (return true))

(defn.xt eq-nested-arr
  "checking aray equality"
  {:added "4.0"}
  [src-arr dst-arr eq-obj eq-arr cache]
  (xt/x:lu-set cache src-arr src-arr)
  (xt/x:lu-set cache dst-arr dst-arr)
  (if (not= (xt/x:len src-arr) (xt/x:len dst-arr))
    (return false))
  (xt/for:array [[i v] src-arr]
    (if (not (-/eq-nested-loop v
                               (. dst-arr [i])
                               eq-obj
                               eq-arr
                               cache))
      (return false)))
  (return true))

(defn.xt eq-nested
  "checking for nested equality"
  {:added "4.0"}
  [obj m]
  (return (-/eq-nested-loop
           obj m
           -/eq-nested-obj
           -/eq-nested-arr
           nil)))

(defn.xt eq-shallow
  "TODO"
  {:added "4.0"}
  [obj m]
  (return (-/eq-nested-loop
           obj m
           -/eq
           -/eq
           nil)))

(defn.xt obj-diff
  "diffs only keys within map"
  {:added "4.0"}
  [obj m]
  (if (xt/x:nil? m)   (return {}))
  (if (xt/x:nil? obj) (return m))
  (var out := {})
  (xt/for:object [[k v] m]
    (if (not (-/eq-nested (xt/x:get-key obj k)
                          (xt/x:get-key m k)))
      (xt/x:set-key out k v)))
  (return out))

(defn.xt obj-diff-nested
  "diffs nested keys within map"
  {:added "4.0"}
  [obj m]
  (if (xt/x:nil? m)   (return {}))
  (if (xt/x:nil? obj) (return m))
  (var out := {})
  (var ks (-/obj-keys m))
  (xt/for:array [k ks]
    (var v   (xt/x:get-key obj k))
    (var mv  (xt/x:get-key m k))
    (cond (and (-/obj? v) (-/obj? mv))
          (do (var dv (-/obj-diff-nested v mv))
              (if (-/obj-not-empty? dv)
                (xt/x:set-key out k dv)))
          (not (-/eq-nested v mv))
          (xt/x:set-key out k mv)))
  (return out))

(defn.xt sort
  "dumb version of arr-sort"
  {:added "4.0"}
  [arr]
  (return (-/arr-sort arr -/identity xt/x:lt)))

(defn.xt objify
  "decodes object if string"
  {:added "4.0"}
  [v]
  (cond (xt/x:is-string? v)
        (return (xt/x:json-decode v))

        :else (return v)))

(defn.xt template-entry
  "gets data from a structure using template"
  {:added "4.0"}
  [obj template props]
  (cond (-/fn? template)
        (return (template obj props))

        (-/nil? template)
        (return obj)

        (-/arr? template)
        (return (-/get-in obj template))
        
        :else
        (return template)))

(defn.xt template-fn
  "gets data from a structure using template"
  {:added "4.0"}
  [template]
  (return (fn:> [obj props] (-/template-entry obj template props))))

(defn.xt template-multi
  "gets data from a structure using template"
  {:added "4.0"}
  [arr]
  (var template-fn
       (fn [entry props]
         (-/for:array [template arr]
           (var out (-/template-entry entry template props))
           (when (xt/x:not-nil? out)
             (return out)))))
  (return template-fn))

(defn.xt sort-by
  "sorts arrow by comparator"
  {:added "4.0"}
  [arr inputs]
  (var keys    (-/arr-map inputs (fn:> [e] (:? (-/arr? e) (-/first e) e))))
  (var inverts (-/arr-map inputs (fn:> [e] (:? (-/arr? e) (-/second e) false))))
  (var get-fn
       (fn [e key]
         (cond (-/fn? key)
               (return (key e))

               :else (return (xt/x:get-key e key)))))
  (var key-fn
       (fn:> [e] (-/arr-map keys (fn:> [key] (get-fn e key)))))
  (var comp-fn
       (fn [a0 a1]
         (-/for:array [[i v0] a0]
           (var v1 (. a1 [i]))
           (var invert (. inverts [i]))
           (when (not= v0 v1)
             (cond invert
                   (cond (-/is-number? v0)
                         (return (-/lt v1 v0))
                         
                         :else
                         (return (-/lt-string (-/to-string v1)
                                              (-/to-string v0))))

                   :else
                   (cond (-/is-number? v0)
                         (return (-/lt v0 v1))
                         
                         :else
                         (return (-/lt-string (-/to-string v0)
                                              (-/to-string v1)))))))
         (return false)))
  (return
   (-/arr-sort arr key-fn comp-fn)))

(defn.xt sort-edges-build
  "builds an edge with links"
  {:added "4.0"}
  [nodes edge]
  (var n-from := (xt/x:first edge))
  (var n-to   := (xt/x:second edge))
  (if (not (xt/x:has-key? nodes n-from))
    (xt/x:set-key nodes n-from {:id n-from
                         :links []}))
  (if (not (xt/x:has-key? nodes n-to))
    (xt/x:set-key nodes n-to {:id n-to
                      :links []}))
  (var links := (. nodes [n-from] ["links"]))
  (xt/x:arr-push links n-to))

(defn.xt ^{:static/template true}
  sort-edges-visit
  "walks over the list of edges"
  {:added "4.0"}
  [nodes visited sorted id ancestors]
  (if (xt/x:get-key visited id) (return))
  (var node := (. nodes [id]))
  (if (not node)
    (xt/x:err (xt/x:cat "Not available: " id)))
  (do (:= ancestors (or ancestors []))
      (xt/x:arr-push ancestors id)
      (xt/x:set-key visited id true)
      (var input (. node ["links"]))
      (xt/for:array [aid input]
        (-/sort-edges-visit nodes visited sorted aid (xt/x:arr-clone ancestors))))
  (xt/x:arr-push-first sorted id))

(defn.xt sort-edges
  "sort edges given a list"
  {:added "4.0"}
  [edges]
  (var nodes   := {})
  (var sorted  := [])
  (var visited := {})
  (xt/for:array [e edges]
    (-/sort-edges-build nodes e))
  (xt/for:object [[id _] nodes]
    (-/sort-edges-visit nodes visited sorted id nil))
  (return sorted))

(defn.xt ^{:static/template true}
  sort-topo
  "sorts in topological order"
  {:added "4.0"}
  [input]
  (var edges := [])
  (xt/for:array [link input]
    (var root (xt/x:first link))
    (var deps (xt/x:second link))
    (xt/for:array [d deps]
      (xt/x:arr-push edges [root d])))
  (return (xt/x:arr-reverse (-/sort-edges edges))))

(defn.xt clone-shallow
  "shallow clones an object or array"
  {:added "4.0"}
  [obj]
  (cond (xt/x:nil? obj) (return obj)
        (-/obj? obj) (return (-/obj-clone obj))
        (-/arr? obj)  (return (-/arr-clone obj))
        :else (return obj)))

(defn.xt clone-nested-loop
  "clone nested objects loop"
  {:added "4.0"}
  [obj cache]
  (when (xt/x:nil? obj)
    (return obj))
  
  (var cached-output (xt/x:lu-get cache obj))
  (cond cached-output (return cached-output)
        
        (-/obj? obj)
        (do (var out := {})
            (xt/x:lu-set cache obj out)
            (xt/for:object [[k v] obj]
              (xt/x:set-key out k (-/clone-nested-loop v cache)))
            (return out))
        
        (-/arr? obj)
        (do (var out := [])
            (xt/x:lu-set cache obj out)
            (xt/for:array [e obj]
              (xt/x:arr-push out (-/clone-nested-loop e cache)))
            (return out))

        :else
        (return obj)))

(defn.xt clone-nested
  "cloning nested objects"
  {:added "4.0"}
  [obj]
  (cond (not (or (-/obj? obj)
                 (-/arr? obj)))
        (return obj)

        :else
        (return (-/clone-nested-loop obj (xt/x:lu-create)))))

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

(defn.xt walk
  "walks over object"
  {:added "4.0"}
  [obj pre-fn post-fn]
  (:= obj (pre-fn obj))
  (cond (xt/x:nil? obj)
        (return (post-fn obj))
        
        (-/obj? obj)
        (do (var out := {})
            (-/for:object [[k v] obj]
              (xt/x:set-key out k (-/walk v pre-fn post-fn)))
            (return (post-fn out)))

        (-/arr? obj)
        (do (var out := [])
            (-/for:array [e obj]
              (xt/x:arr-push out (-/walk e pre-fn post-fn)))
            (return (post-fn out)))

        :else
        (return (post-fn obj))))

(defn.xt get-data
  "gets only data (for use with json)"
  {:added "4.0"}
  [obj]
  (var data-fn
       (fn [obj]
         (if (or (xt/x:is-string? obj)
                 (xt/x:is-number? obj)
                 (xt/x:is-boolean? obj)
                 (xt/x:is-object? obj)
                 (xt/x:is-array? obj)
                 (xt/x:nil? obj))
           (return obj)
           (return (xt/x:cat "<" (-/type-native obj) ">")))))
  (return (-/walk obj -/identity data-fn)))

(defn.xt get-spec
  "creates a get-spec of a datastructure"
  {:added "4.0"}
  [obj]
  (var spec-fn
       (fn [obj]
         (if (not (or (-/obj? obj)
                      (-/arr? obj)))
           (return (-/type-native obj))
           (return obj))))
  (return (-/walk obj -/identity spec-fn)))

;;
;;
;;

(defn.xt split-long
  "splits a long string"
  {:added "4.0"}
  [s lineLen]
  (when (-/is-empty? s)
    (return ""))
  (:= lineLen (or lineLen 50))
  (var total (-/len s))
  (var lines (-/ceil (/ total lineLen)))
  (var out [])
  (-/for:index [i [0  lines 1]]
    (var line (-/substring s
                           (* i lineLen)
                           (* (+ i 1) lineLen)))
    (when (<  0 (xt/x:str-len line))
      (xt/x:arr-push out line)))
  (return out))

(defn.xt str-rand
  "TODO"
  {:added "4.0"}
  [n]
  (var choices ["A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N"
                "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"
                "a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n"
                "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"
                "0" "1" "2" "3" "4" "5" "6" "7" "8"])
  (var out "")
  (xt/for:index [i [0 n]]
               (:= out (xt/x:cat out (-/arr-random choices))))
  (return out))

(defn.xt prototype-spec
  "creates the spec map from interface definitions"
  {:added "4.0"}
  [spec-arr]
  (var acc-fn
       (fn [acc e]
         (var [spec-i spec-map] e)
         (xt/for:array [key spec-i]
                      (when (xt/x:nil? (-/get-key spec-map key))
                        (-/err (-/cat "NOT VALID."
                                      (xt/x:json-encode {:required key
                                                      :actual (-/obj-keys spec-map)})))))
         (return (-/obj-assign
                  acc
                  spec-map))))
  (return
   (-/arr-foldl spec-arr acc-fn {})))

;;
;; THREAD
;;
  
(defn.xt with-delay
  "sets a delay"
  {:added "4.0"}
  ([thunk ms]
   (xt/x:with-delay thunk ms)))

;;
;; METADATA
;;

(defn meta:info-fn
  "the function to get meta info"
  {:added "4.0"}
  [& [m]]
  (let [{:keys [namespace id]} (:entry (l/macro-opts))
        {:keys [line]} (meta (l/macro-form))]
    (merge
     {:meta/fn    (str (or namespace (env/ns-sym)) "/" id)
      :meta/line  line}
     m)))

(defmacro.xt meta:info
  "macro to inject meta info"
  {:added "4.0"}
  [& [m]]
  (meta:info-fn m))

(defmacro.xt LOG!
  "logging with meta info"
  {:added "4.0"}
  [& args]
  (let [{:keys [label]} (meta (l/macro-form))
        {:meta/keys [fn line]} (meta:info-fn)]
    (clojure.core/apply list 'x:print (clojure.core/str
                                       label
                                       " "
                                       fn)
                        line "\n\n" args)))

;;
;; TRACE
;;

(defn.xt trace-log
  "gets the current trace log"
  {:added "4.0"}
  []
  (if (xt/x:global-has? TRACE)
    (return (!:G TRACE))
    (do (xt/x:global-set TRACE [])
        (return (!:G TRACE)))))

(defn.xt trace-log-clear
  "resets the trace log"
  {:added "4.0"}
  []
  (do (xt/x:global-set TRACE [])
      (return (!:G TRACE))))

(defn.xt trace-log-add
  "adds an entry to the log"
  {:added "4.0"}
  [data tag opts]
  (var log (-/trace-log))
  (var m (-/obj-assign
          {:tag tag
           :data data
           :time (xt/x:now-ms)}
          opts))
  (xt/x:arr-push log m)
  (return (xt/x:len log)))

(defn.xt trace-filter
  "filters out traced entries"
  {:added "4.0"}
  [tag]
  (return (-/arr-filter (-/trace-log) (fn:> [e] (== tag (xt/x:get-key e "tag"))))))

(defn.xt trace-last-entry
  "gets the last entry"
  {:added "4.0"}
  [tag]
  (var log (-/trace-log))
  (if (== nil tag)
    (return (-/last log))
    (do (var tagged (-/trace-filter tag))
        (return (-/last tagged)))))

(defn.xt trace-data
  "gets the trace data"
  {:added "4.0"}
  [tag]
  (return (-/arr-map (-/trace-log) (fn:> [e] (xt/x:get-key e "data")))))

(defn.xt trace-last
  "gets the last value"
  {:added "4.0"}
  [tag]
  (return (xt/x:get-key (-/trace-last-entry tag) "data")))

(defmacro.xt TRACE!
  "performs a trace call"
  {:added "4.0"}
  [data & [tag]]
  (let [pos   (meta (l/macro-form))
        ns    (env/ns-sym)
        opts  (assoc (select-keys pos [:line :column])
                     :ns (str ns))]
    (list `trace-log-add data (or tag (f/sid)) opts)))

(defn.xt trace-run
  "run helper for `RUN!` macro"
  {:added "4.0"}
  [f]
  (-/trace-log-clear)
  (f)
  (return (-/trace-log)))

(defmacro.xt RUN!
  "runs a form, saving trace forms"
  {:added "4.0"}
  [& body]
  (template/$ (do (var f (fn [] ~@body))
           (return (xt.lang.base-lib/trace-run f)))))