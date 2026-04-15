(ns std.lang.base.grammar-xtalk
  (:require [clojure.string :as str]
            [std.block.template :as gen]
            [std.lib.foundation :as f]
            [std.lang.base.emit-preprocess :as preprocess]))

(defn tf-throw
  "wrapper for throw transform"
  {:added "4.0"}
  [[_ obj]]
  (list 'throw obj))

(defn tf-add
  "wrapper for add transform"
  {:added "4.1"}
  [[_ a b & more]]
  (apply list '+ a b more))

(defn tf-sub
  "wrapper for sub transform"
  {:added "4.1"}
  [[_ a b & more]]
  (apply list '- a b more))

(defn tf-mul
  "wrapper for mul transform"
  {:added "4.1"}
  [[_ a b & more]]
  (apply list '* a b more))

(defn tf-div
  "wrapper for div transform"
  {:added "4.1"}
  [[_ a b & more]]
  (apply list '/ a b more))

(defn tf-neg
  "wrapper for neg transform"
  {:added "4.1"}
  [[_ x]]
  (list '- x))

(defn tf-inc
  "wrapper for inc transform"
  {:added "4.1"}
  [[_ x]]
  (list '+ x 1))

(defn tf-dec
  "wrapper for dec transform"
  {:added "4.1"}
  [[_ x]]
  (list '- x 1))

(defn tf-eq
  "wrapper for eq transform"
  {:added "4.1"}
  [[_ a b]]
  (list '== a b))

(defn tf-neq
  "wrapper for neq transform"
  {:added "4.1"}
  [[_ a b]]
  (list 'not= a b))

(defn tf-lt
  "wrapper for lt transform"
  {:added "4.1"}
  [[_ a b]]
  (list '< a b))

(defn tf-lte
  "wrapper for lte transform"
  {:added "4.1"}
  [[_ a b]]
  (list '<= a b))

(defn tf-gt
  "wrapper for gt transform"
  {:added "4.1"}
  [[_ a b]]
  (list '> a b))

(defn tf-gte
  "wrapper for gte transform"
  {:added "4.1"}
  [[_ a b]]
  (list '>= a b))

(defn tf-zero?
  "wrapper for zero? transform"
  {:added "4.1"}
  [[_ x]]
  (list '== x 0))

(defn tf-pos?
  "wrapper for pos? transform"
  {:added "4.1"}
  [[_ x]]
  (list '> x 0))

(defn tf-neg?
  "wrapper for neg? transform"
  {:added "4.1"}
  [[_ x]]
  (list '< x 0))

(defn tf-even?
  "wrapper for even? transform"
  {:added "4.1"}
  [[_ x]]
  (list '== 0 (list 'mod x 2)))

(defn tf-odd?
  "wrapper for odd? transform"
  {:added "4.1"}
  [[_ x]]
  (list 'not (list '== 0 (list 'mod x 2))))

(defn tf-eq-nil?
  "equals nil transform"
  {:added "4.0"}
  [[_ obj]]
  (list '== nil obj))

(defn tf-not-nil?
  "not nil transform"
  {:added "4.0"}
  [[_ obj]]
  (list 'not= nil obj))

(defn tf-proto-create
  "creates the prototype map"
  {:added "4.0"}
  [[_ m]]
  (list 'return m))

(defn tf-has-key?
  "has key default transform"
  {:added "4.0"}
  [[_ obj key check]]
  (let [val (list 'not= (list 'x:get-key obj key) nil)]
    (if check
      (list '== check val)
      val)))

(defn tf-get-path
  "get-in transform"
  {:added "4.0"}
  [[_ obj ks default]]
  (let [val (if (symbol? obj)
              (apply list '. obj (map vector ks))
              (apply list '. (list 'quote (list obj)) (map vector ks)))]
    (if default
      (list ':? (list 'x:nil? val) default val)
      val)))

(defn tf-get-key
  "get-key transform"
  {:added "4.0"}
  [[_ obj k default]]
  (let [val (if (symbol? obj)
              (list '. obj [k])
              (list '. (list 'quote (list obj)) [k]))]
    (if default
      (list ':? (list 'x:nil? val) default val)
      val)))

(defn tf-set-key
  "set-key transform"
  {:added "4.0"}
  [[_ obj k v]]
  (list := (list '. obj [k]) v))

(defn tf-del-key
  "del-key transform"
  {:added "4.0"}
  [[_ obj k]]
  (list 'x:del (list '. obj [k])))

(defn tf-copy-key
  "copy-key transform"
  {:added "4.0"}
  [[_ dst src idx]]
  (let [[dk sk] (if (vector? idx)
                  idx
                  [idx idx])]
    (tf-set-key [nil dst dk (tf-get-key [nil src sk])])))

;;
;;
;;

(defn tf-grammar-offset
  "del-key transform"
  {:added "4.0"}
  []
  (let [grammar preprocess/*macro-grammar*]
    (or (get-in grammar [:default :index :offset]) 0)))

(defn tf-grammar-end-inclusive
  "gets the end inclusive flag"
  {:added "4.0"}
  []
  (let [grammar preprocess/*macro-grammar*]
    (get-in grammar [:default :index :end-inclusive])))

(defn tf-offset-base
  "calculates the offset"
  {:added "4.0"}
  [offset n]
  (if (nil? n)
    offset
    (cond (zero? offset) n
          
          (integer? n)
          (+ n offset)
          
          :else
          (list '+ n offset))))

(defn tf-offset
  "gets the offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (tf-grammar-offset)
                  n))

(defn tf-offset-rev
  "gets the reverse offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (- (tf-grammar-offset) 1)
                  n))

(defn tf-offset-len
  "gets the length offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (if (tf-grammar-end-inclusive)
                    0 -1) 
                  n))

(defn tf-offset-rlen
  "gets the reverse length offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (if (tf-grammar-end-inclusive)
                    -1 0) 
                  n))

(defn tf-first
  "gets the first element of an indexed collection"
  {:added "4.1"}
  [[_ arr]]
  (list 'x:get-idx arr (list 'x:offset 0)))

(defn tf-second
  "gets the second element of an indexed collection"
  {:added "4.1"}
  [[_ arr]]
  (list 'x:get-idx arr (list 'x:offset 1)))

(defn tf-last
  "gets the last element of an indexed collection"
  {:added "4.1"}
  [[_ arr]]
  (list 'x:get-idx arr (list 'x:offset-len (list 'x:len arr))))

(defn tf-second-last
  "gets the second last element of an indexed collection"
  {:added "4.1"}
  [[_ arr]]
  (list 'x:get-idx arr (list '+ (list 'x:len arr)
                             (list 'x:offset -2))))

(defn tf-str-lt
  "checks if string a sorts before b"
  {:added "4.1"}
  [[_ a b]]
  (list 'x:str-comp a b))

(defn tf-str-gt
  "checks if string a sorts after b"
  {:added "4.1"}
  [[_ a b]]
  (list 'x:str-comp b a))

;;
;; GLOBAL
;;

(defn tf-global-set
  "default global set transform"
  {:added "4.0"}
  [[_ sym val]]
  (list 'x:set-key '!:G (str sym) val))

(defn tf-global-has?
  "default global has transform"
  {:added "4.0"}
  [[_ sym]]
  (list 'not (list 'x:nil? (list 'x:get-key '!:G (str sym)))))

(defn tf-global-del
  "default global del transform"
  {:added "4.0"}
  [[_ sym val]]
  (list 'x:set-key '!:G (str sym) nil))

;;
;; LU
;;

(defn tf-lu-eq
  "lookup equals transform"
  {:added "4.0"}
  [[_ o1 o2]]
  (list '== o1 o2))


;;
;; BIT
;;

(defn tf-bit-and
  "bit and transform"
  {:added "4.0"}
  [[_ i1 i2]]
  (list 'b:& i1 i2))

(defn tf-bit-or
  "bit or transform"
  {:added "4.0"}
  [[_ i1 i2]]
  (list 'b:| i1 i2))

(defn tf-bit-lshift
  "bit left shift transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'b:<< x n))

(defn tf-bit-rshift
  "bit right shift transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'b:>> x n))

(defn tf-bit-xor
  "bit xor transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'b:xor x n))


;;
;; XTALK COMMON INTERFACES
;;

;; common-lib
(def +xt-common-basic+
  [{:op :x-del            :symbol #{'x:del}             :emit :abstract  
    :op-spec    {:macro-only true
                 :arglists '([var])
                 :type [:fn [:xt/any] :xt/unknown]}}
   {:op :x-cat            :symbol #{'x:cat}             :emit :abstract  
    :op-spec    {:variadic true
                 :arglists '([x y])
                 :type [:fn [:xt/str :xt/str] :xt/str]}}
   {:op :x-len            :symbol #{'x:len}             :emit :abstract
    :op-spec    {:arglists '([value])
                 :type [:fn [:xt/any] :xt/int]}}
   {:op :x-err            :symbol #{'x:err}             :emit :abstract
    :op-spec    {:macro-only true
                 :type [:fn [:xt/any] :xt/any]
                 :arglists '([message])}}
   {:op :x-type-native    :symbol #{'x:type-native}     :emit :abstract
    :op-spec    {:template-only true
                 :type [:fn [:xt/any] :xt/string]
                 :arglists '([value])}}])

(def +xt-common-index+
  [{:op :x-offset         :symbol #{'x:offset}          :macro #'tf-offset      :emit :macro
    :op-spec {:arglists '([] [n])
              :type [:fn [:xt/int] :xt/int]}}
   {:op :x-offset-rev     :symbol #{'x:offset-rev}      :macro #'tf-offset-rev  :emit :macro
    :op-spec {:arglists '([] [n])
              :type [:fn [:xt/int] :xt/int]}}
   {:op :x-offset-len     :symbol #{'x:offset-len}      :macro #'tf-offset-len  :emit :macro
    :op-spec {:arglists '([] [n])
              :type [:fn [:xt/int] :xt/int]}}
   {:op :x-offset-rlen    :symbol #{'x:offset-rlen}     :macro #'tf-offset-rlen :emit :macro
    :op-spec {:arglists '([] [n])
              :type [:fn [:xt/int] :xt/int]}}])

(def +xt-common-number+
  [{:op :x-add            :symbol #{'x:add}             :macro #'tf-add    :emit :macro
    :op-spec {:variadic true
              :arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-sub            :symbol #{'x:sub}             :macro #'tf-sub    :emit :macro
    :op-spec {:variadic true
              :arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-mul            :symbol #{'x:mul}             :macro #'tf-mul    :emit :macro
    :op-spec {:variadic true
              :arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-div            :symbol #{'x:div}             :macro #'tf-div    :emit :macro
    :op-spec {:variadic true
              :arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-neg            :symbol #{'x:neg}             :macro #'tf-neg    :emit :macro
    :op-spec {:arglists '([x])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-inc            :symbol #{'x:inc}             :macro #'tf-inc    :emit :macro
    :op-spec {:type [:fn [:xt/num] :xt/num]
              :arglists '([x])}}
   {:op :x-dec            :symbol #{'x:dec}             :macro #'tf-dec    :emit :macro
    :op-spec {:type [:fn [:xt/num] :xt/num]
              :arglists '([x])}}
   {:op :x-zero?          :symbol #{'x:zero?}           :macro #'tf-zero?  :emit :macro
    :op-spec {:arglists '([x])
              :type [:fn [:xt/num] :xt/bool]}}
   {:op :x-pos?           :symbol #{'x:pos?}            :macro #'tf-pos?   :emit :macro
    :op-spec {:arglists '([x])
              :type [:fn [:xt/num] :xt/bool]}}
   {:op :x-neg?           :symbol #{'x:neg?}            :macro #'tf-neg?   :emit :macro
    :op-spec {:arglists '([x])
              :type [:fn [:xt/num] :xt/bool]}}
   {:op :x-even?          :symbol #{'x:even?}           :macro #'tf-even?  :emit :macro
    :op-spec {:arglists '([x])
              :type [:fn [:xt/num] :xt/bool]}}
   {:op :x-odd?           :symbol #{'x:odd?}            :macro #'tf-odd?   :emit :macro
    :op-spec {:arglists '([x])
              :type [:fn [:xt/num] :xt/bool]}}
   {:op :x-eq             :symbol #{'x:eq}              :macro #'tf-eq     :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/any :xt/any] :xt/bool]}}
   {:op :x-neq            :symbol #{'x:neq}             :macro #'tf-neq    :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/any :xt/any] :xt/bool]}}
   {:op :x-lt             :symbol #{'x:lt}              :macro #'tf-lt     :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/bool]}}
   {:op :x-lte            :symbol #{'x:lte}             :macro #'tf-lte    :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/bool]}}
   {:op :x-gt             :symbol #{'x:gt}              :macro #'tf-gt     :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/bool]}}
   {:op :x-gte            :symbol #{'x:gte}             :macro #'tf-gte    :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/bool]}}])

(def +xt-common-nil+
  [{:op :x-not-nil?       :symbol #{'x:not-nil?}        :macro #'tf-not-nil?    :emit :macro
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-nil?           :symbol #{'x:nil?}            :macro #'tf-eq-nil?     :emit :macro
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}])

(def +xt-common-primitives+
  [{:op :x-to-string      :symbol #{'x:to-string}       :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/str]}}
   {:op :x-to-number      :symbol #{'x:to-number}       :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/num]}}
   {:op :x-is-string?     :symbol #{'x:is-string?}      :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-is-number?     :symbol #{'x:is-number?}      :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-is-integer?    :symbol #{'x:is-integer?}     :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-is-boolean?    :symbol #{'x:is-boolean?}     :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-is-object?     :symbol #{'x:is-object?}      :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-is-array?      :symbol #{'x:is-array?}       :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}])

(def +xt-common-lu+
  [{:op :x-lu-create      :symbol #{'x:lu-create}       :emit :abstract
    :op-spec {:arglists '([])
              :type [:fn [] :xt/any]}}
   {:op :x-lu-eq          :symbol #{'x:lu-eq}           :macro #'tf-lu-eq :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/any :xt/any] :xt/bool]}}
   {:op :x-lu-get         :symbol #{'x:lu-get}          :emit :abstract
    :op-spec {:type [:fn [:xt/any :xt/any :xt/any] :xt/any]
              :arglists '([lookup key])}}
   {:op :x-lu-set         :symbol #{'x:lu-set}          :emit :abstract
    :op-spec {:type [:fn [:xt/any :xt/any :xt/any] :xt/any]
              :arglists '([lookup key value])}}
   {:op :x-lu-del         :symbol #{'x:lu-del}          :emit :abstract
    :op-spec {:type [:fn [:xt/any :xt/any] :xt/any]
              :arglists '([lookup key])}}])

(def +xt-common-object+
  [{:op :x-has-key?       :symbol #{'x:has-key?}        :emit :abstract
    :op-spec  {:type [:fn [:xt/obj :xt/str :xt/any] :xt/bool]
               :arglists '([obj key] [obj key check])}}
   {:op :x-del-key        :symbol #{'x:del-key}         :macro #'tf-del-key     :emit :macro
    :op-spec {:type [:fn [:xt/obj :xt/str] :xt/any]
              :arglists '([obj key])}}
    {:op :x-get-key        :symbol #{'x:get-key}         :macro #'tf-get-key     :emit :macro
     :op-spec {:type [:fn [:xt/obj :xt/str  :xt/any] :xt/any]
               :arglists '([obj key] [obj key default])}}
    {:op :x-get-path       :symbol #{'x:get-path}        :macro #'tf-get-path    :emit :macro
     :op-spec {:type [:fn [:xt/obj [:xt/array :xt/str] :xt/any] :xt/any]
               :arglists '([obj path] [obj path default])}}
   {:op :x-set-key        :symbol #{'x:set-key}         :macro #'tf-set-key     :emit :macro
    :op-spec   {:type [:fn [:xt/obj [:xt/array :xt/str] :xt/any] :xt/self]
                :arglists '([obj key value])}}
   {:op :x-copy-key       :symbol #{'x:copy-key}        :macro #'tf-copy-key    :emit :macro
    :op-spec   {:type [:fn [:xt/obj :xt/obj :xt/str :xt/any] :xt/self]
                :arglists '([dst src key])}}
   {:op :x-obj-keys       :symbol #{'x:obj-keys}        :emit :hard-link :raw 'xt.lang.common-data/obj-keys
    :op-spec   {:type [:fn [:xt/obj] [:xt/array :xt/str]]
                :arglists '([obj])}}
   {:op :x-obj-vals       :symbol #{'x:obj-vals}        :emit :hard-link :raw 'xt.lang.common-data/obj-vals
    :op-spec   {:type [:fn [:xt/obj] [:xt/array :xt/any]]
                :arglists '([obj])}}
   {:op :x-obj-pairs      :symbol #{'x:obj-pairs}       :emit :hard-link :raw 'xt.lang.common-data/obj-pairs
    :op-spec   {:type [:fn [:xt/obj] [:xt/array [:xt/tuple :xt/str :xt/any]]]
                :arglists '([obj])}}
   {:op :x-obj-clone      :symbol #{'x:obj-clone}       :emit :hard-link :raw 'xt.lang.common-data/obj-clone
    :op-spec   {:type [:fn [:xt/obj] :xt/obj]
                :arglists '([obj])}}
   {:op :x-obj-assign     :symbol #{'x:obj-assign}      :emit :hard-link :raw 'xt.lang.common-data/obj-assign
    :op-spec   {:type [:fn [:xt/obj :xt/obj] :xt/obj]
                :arglists '([obj other])}}
   {:op :x-obj-from-pairs :symbol #{'x:obj-from-pairs}  :emit :hard-link :raw 'xt.lang.common-data/obj-from-pairs
    :op-spec   {:type [:fn [[:xt/array [:xt/tuple :xt/str :xt/any]]] :xt/obj]
                :arglists '([pairs])}}])

(def +xt-common-array+
  [{:op :x-get-idx         :symbol #{'x:get-idx}          :macro #'tf-get-key     :emit :macro
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/int :xt/any] :xt/any]
              :arglists '([arr idx] [arr idx default])}}
   {:op :x-set-idx         :symbol #{'x:set-idx}          :macro #'tf-set-key     :emit :macro
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/int] :xt/self]
              :arglists '([arr idx value])}}
   {:op :x-arr-first       :symbol #{'x:first}        :macro #'tf-first       :emit :macro
    :op-spec  {:type [:fn [[:xt/array :xt/any]] :xt/any]
               :arglists '([arr])}}
   {:op :x-arr-second      :symbol #{'x:second}       :macro #'tf-second      :emit :macro
    :op-spec  {:type [:fn [[:xt/array :xt/any]] :xt/any]
               :arglists '([arr])}}
   {:op :x-arr-last        :symbol #{'x:last}         :macro #'tf-last        :emit :macro
    :op-spec  {:type [:fn [[:xt/array :xt/any]] :xt/any]
               :arglists '([arr])}}
   {:op :x-arr-second-last :symbol #{'x:second-last}  :macro #'tf-second-last :emit :macro
    :op-spec  {:type [:fn [[:xt/array :xt/any]] :xt/any]
               :arglists '([arr])}}
   {:op :x-arr-remove      :symbol #{'x:arr-remove}       :emit :abstract
    :op-spec  {:type [:fn [[:xt/array :xt/any] :xt/num] :xt/any]
               :arglists '([arr idx])}}
   {:op :x-arr-push        :symbol #{'x:arr-push}         :emit :abstract
    :op-spec  {:type [:fn [[:xt/array :xt/any] :xt/any] :xt/self]
               :arglists '([arr value])}}
   {:op :x-arr-pop         :symbol #{'x:arr-pop}          :emit :abstract
    :op-spec   {:type [:fn [[:xt/array :xt/any] :xt/any] :xt/any]
                :arglists '([arr])}}
   {:op :x-arr-push-first  :symbol #{'x:arr-push-first}   :emit :abstract
    :op-spec   {:type [:fn [[:xt/array :xt/any] :xt/any] :xt/self]
                :arglists '([arr value])}}
   {:op :x-arr-pop-first   :symbol #{'x:arr-pop-first}    :emit :abstract
    :op-spec   {:type [:fn [[:xt/array :xt/any]] :xt/any]
                :arglists '([arr])}}
   {:op :x-arr-insert      :symbol #{'x:arr-insert}       :emit :abstract
    :op-spec   {:type [:fn [[:xt/array :xt/any] :xt/any] :xt/self]
                :arglists '([arr idx value])}}
   {:op :x-arr-slice       :symbol #{'x:arr-slice}       :emit :hard-link :raw 'xt.lang.common-data/arr-slice
    :op-spec   {:type [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]]
                :arglists '([arr start] [arr start end])}}
   {:op :x-arr-reverse     :symbol #{'x:arr-reverse}      :emit :hard-link :raw 'xt.lang.common-data/arr-reverse
    :op-spec   {:type [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]]
                :arglists '([arr])}}])

(def +xt-common-print+
  [{:op :x-print          :symbol #{'x:print}           :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/any]}}])

;; common-string

(def +xt-common-string+
  [{:op :x-str-len         :symbol #{'x:str-len}         :emit :alias :raw 'x:len
    :op-spec   {:arglists '([value])
                :type [:fn [:xt/str] :xt/int]}}
   {:op :x-str-comp    :symbol #{'x:str-comp}     :emit :abstract
    :op-spec   {:arglists '([x y])
                :type [:fn [:xt/str :xt/str] :xt/bool]}}
   {:op :x-str-lt       :symbol #{'x:str-lt}       :macro #'tf-str-lt :emit :macro
    :op-spec    {:arglists '([x y])
                 :type [:fn [:xt/str :xt/str] :xt/bool]}}
   {:op :x-str-gt       :symbol #{'x:str-gt}       :macro #'tf-str-gt :emit :macro
    :op-spec    {:arglists '([x y])
                 :type [:fn [:xt/str :xt/str] :xt/bool]}}
   {:op :x-str-pad-left    :symbol #{'x:str-pad-left}    :emit :hard-link
    :raw 'xt.lang.common-string/pad-left
    :op-spec   {:type [:fn [:xt/str :xt/num :xt/str] :xt/str]
                :arglists '([value len pad])}}
   {:op :x-str-pad-right   :symbol #{'x:str-pad-right}   :emit :hard-link
    :raw 'xt.lang.common-string/pad-right
    :op-spec   {:type [:fn [:xt/str :xt/num :xt/str] :xt/str]
                :arglists '([value len pad])}}
   {:op :x-str-starts-with :symbol #{'x:str-starts-with} :emit :hard-link
    :raw 'xt.lang.common-string/starts-with?
    :op-spec   {:arglists '([value prefix])
                :type [:fn [:xt/str :xt/str] :xt/bool]}}
   {:op :x-str-ends-with   :symbol #{'x:str-ends-with}   :emit :hard-link
    :raw 'xt.lang.common-string/ends-with?
    :op-spec    {:arglists '([value suffix])
                 :type [:fn [:xt/str :xt/str] :xt/bool]}}
   {:op :x-str-char        :symbol #{'x:str-char}        :emit :abstract
    :op-spec    {:type [:fn [:xt/str :xt/num] :xt/str]
                 :arglists '([value idx])}}
   {:op :x-str-format      :symbol #{'x:str-format}      :emit :abstract
    :op-spec    {:arglists '([template values])
                 :type [:fn [:xt/str [:xt/array :xt/any]] :xt/str]}}
   {:op :x-str-split       :symbol #{'x:str-split}       :emit :abstract
    :op-spec    {:arglists '([value separator])
                 :type [:fn [:xt/str :xt/str] [:xt/array :xt/str]]}}
   {:op :x-str-join        :symbol #{'x:str-join}        :emit :abstract
    :op-spec    {:arglists '([separator coll])
                 :type [:fn [:xt/str [:xt/array :xt/any]] :xt/str]}}
   {:op :x-str-index-of    :symbol #{'x:str-index-of}    :emit :abstract
    :op-spec    {:type [:fn [:xt/str :xt/str :xt/num] :xt/str]
                 :arglists '([value pattern] [value pattern from])}}
   {:op :x-str-substring   :symbol #{'x:str-substring}   :emit :abstract
    :op-spec    {:type [:fn [:xt/str :xt/num :xt/num] :xt/str]
                 :arglists '([value start] [value start len])}}
   {:op :x-str-to-upper    :symbol #{'x:str-to-upper}    :emit :abstract
    :op-spec     {:arglists '([value])
                  :type [:fn [:xt/str] :xt/str]}}
   {:op :x-str-to-lower    :symbol #{'x:str-to-lower}    :emit :abstract
    :op-spec     {:arglists '([value])
                  :type  [:fn [:xt/str] :xt/str]}}
   {:op :x-str-to-fixed    :symbol #{'x:str-to-fixed}    :emit :abstract
    :op-spec     {:arglists '([value digits])
                  :type [:fn [:xt/num :xt/int] :xt/str]}}
   {:op :x-str-replace     :symbol #{'x:str-replace}     :emit :abstract
    :op-spec     {:arglists '([value match replacement])
                  :type [:fn [:xt/str :xt/str :xt/str] :xt/str]}}
   {:op :x-str-trim        :symbol #{'x:str-trim}        :emit :abstract
    :op-spec     {:arglists '([value])
                  :type [:fn [:xt/str] :xt/str]}}
   {:op :x-str-trim-left   :symbol #{'x:str-trim-left}   :emit :abstract
    :op-spec      {:arglists '([value])
                   :type [:fn [:xt/str] :xt/str]}}
   {:op :x-str-trim-right  :symbol #{'x:str-trim-right}  :emit :abstract
    :op-spec      {:arglists '([value])
                   :type [:fn [:xt/str] :xt/str]}}])

;; common-math

(def +xt-common-math+       
  [{:op :x-m-abs :symbol #{'x:m-abs} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-acos :symbol #{'x:m-acos} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-asin :symbol #{'x:m-asin} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-atan :symbol #{'x:m-atan} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-ceil :symbol #{'x:m-ceil} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-cos :symbol #{'x:m-cos} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-cosh :symbol #{'x:m-cosh} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-exp :symbol #{'x:m-exp} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-floor :symbol #{'x:m-floor} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-loge :symbol #{'x:m-loge} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-log10 :symbol #{'x:m-log10} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-max :symbol #{'x:m-max} :emit :abstract
    :op-spec {:variadic true
              :arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-m-mod :symbol #{'x:m-mod} :emit :abstract
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-m-min :symbol #{'x:m-min} :emit :abstract
    :op-spec {:variadic true
              :arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-m-pow :symbol #{'x:m-pow} :emit :abstract
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-m-quot :symbol #{'x:m-quot} :emit :abstract
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/num :xt/num] :xt/num]}}
   {:op :x-m-sin :symbol #{'x:m-sin} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-sinh :symbol #{'x:m-sinh} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-sqrt :symbol #{'x:m-sqrt} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-tan :symbol #{'x:m-tan} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}
   {:op :x-m-tanh :symbol #{'x:m-tanh} :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/num] :xt/num]}}])

;;
;; XTALK LANGUAGE SPECIFIC INTERFACES
;;

(def +xt-functional-base+
  [{:op :x-is-function?   :symbol #{'x:is-function?}    :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-callback       :symbol #{'x:callback}        :emit :unit :default nil
    :op-spec {:type [:fn [] [:fn [:xt/any] nil]]
              :arglists '([])}}])

(def +xt-functional-invoke+
  [{:op :x-eval           :symbol #{'x:eval}            :emit :abstract
    :op-spec {:type [:fn [:xt/str] :xt/any]
              :arglists '([value])}}
   {:op :x-apply          :symbol #{'x:apply}           :emit :abstract
    :op-spec {:type [:fn [:xt/fn [:xt/array :xt/any]] :xt/any]
              :arglists '([f args])}}])

(def +xt-functional-return+
  [{:op :x-return-run      :symbol #{'x:return-run}      :emit :abstract
    :op-spec {:template-only true
              :arglists '([runner])}}
   {:op :x-return-encode   :symbol #{'x:return-encode}   :emit :abstract
    :op-spec {:template-only true
              :type [:fn [:xt/any :xt/str :xt/str] :xt/str]
              :arglists '([out id key])}}
   {:op :x-return-wrap     :symbol #{'x:return-wrap}     :emit :abstract
    :op-spec {:template-only true
              :type [:fn [[:fn [:xt/any] :xt/any]] [:fn [:xt/any] :xt/str]]
              :arglists '([callbock encode-fn])}}
   {:op :x-return-eval     :symbol #{'x:return-eval}     :emit :abstract
    :op-spec {:template-only true
              :type [:fn [:xt/str] :xt/str]
              :arglists '([expr wrap-fn])}}])

(def +xt-functional-array+
  [{:op :x-arr-sort        :symbol #{'x:arr-sort}         :emit :abstract
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/fn :xt/fn] :xt/self]
              :arglists '([arr key-fn comp-fn])}}
   {:op :x-arr-clone       :symbol #{'x:arr-clone}        :emit :hard-link :raw 'xt.lang.common-data/arr-clone
    :op-spec {:type [:fn [[:xt/array :xt/any]] [:xt/array :xt/any]]
              :arglists '([arr])}}
   {:op :x-arr-each        :symbol #{'x:arr-each}         :emit :hard-link :raw 'xt.lang.common-data/arr-each
    :op-spec {:type [:fn [[:xt/array :xt/any]] nil]
              :arglists '([arr f])}}
   {:op :x-arr-every       :symbol #{'x:arr-every}        :emit :hard-link :raw 'xt.lang.common-data/arr-every
    :op-spec {:arglists '([arr pred])
              :type [:fn [[:xt/array :xt/any] :xt/fn] :xt/bool]}}
   {:op :x-arr-some        :symbol #{'x:arr-some}         :emit :hard-link :raw 'xt.lang.common-data/arr-some
    :op-spec {:arglists '([arr pred])
              :type [:fn [[:xt/array :xt/any] :xt/fn] :xt/bool]}}
   {:op :x-arr-map         :symbol #{'x:arr-map}          :emit :hard-link :raw 'xt.lang.common-data/arr-map
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]]
              :arglists '([arr f])}}
   {:op :x-arr-append      :symbol #{'x:arr-append}       :emit :hard-link :raw 'xt.lang.common-data/arr-append
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/any] [:xt/array :xt/any]]
              :arglists '([arr value])}}
   {:op :x-arr-filter      :symbol #{'x:arr-filter}       :emit :hard-link :raw 'xt.lang.common-data/arr-filter
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]]
              :arglists '([arr pred])}}
   {:op :x-arr-keep        :symbol #{'x:arr-keep}         :emit :hard-link :raw 'xt.lang.common-data/arr-keep
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/fn] [:xt/array :xt/any]]
              :arglists '([arr f])}}
   {:op :x-arr-foldl       :symbol #{'x:arr-foldl}        :emit :hard-link :raw 'xt.lang.common-data/arr-foldl
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/fn :xt/any] :xt/any]
              :arglists '([arr f init])}}
   {:op :x-arr-foldr       :symbol #{'x:arr-foldr}        :emit :hard-link :raw 'xt.lang.common-data/arr-foldr
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/fn :xt/any] :xt/any]
              :arglists '([arr f init])}}
   {:op :x-arr-find        :symbol #{'x:arr-find}         :emit :hard-link :raw 'xt.lang.common-data/arr-find
    :op-spec {:type [:fn [[:xt/array :xt/any] :xt/fn] :xt/any]
              :arglists '([arr pred])}}])

(def +xt-functional-future+
  [{:op :x-future-run        :symbol #{'x:future-run}        :emit :abstract
    :op-spec {:template-only true
              :arglists '([thunk])}}
   {:op :x-future-then       :symbol #{'x:future-then}       :emit :abstract
    :op-spec {:template-only true
              :arglists '([task on-ok])}}
   {:op :x-future-catch      :symbol #{'x:future-catch}      :emit :abstract
    :op-spec {:arglists '([task on-err])}}
   {:op :x-future-finally    :symbol #{'x:future-finally}    :emit :abstract
    :op-spec {:template-only true
              :arglists '([task on-done])}}
   {:op :x-future-cancel     :symbol #{'x:future-cancel}     :emit :abstract
    :op-spec {:template-only true
              :arglists '([task])}}
   {:op :x-future-status     :symbol #{'x:future-status}     :emit :abstract
    :op-spec {:arglists '([task])}}
   {:op :x-future-await      :symbol #{'x:future-await}      :emit :abstract
    :op-spec {:template-only true
              :arglists '([task timeout-ms default])}}
   {:op :x-future-from-async :symbol #{'x:future-from-async} :emit :abstract
    :op-spec {:template-only true
              :arglists '([executor])}}])

(def +xt-functional-iter+      
  [{:op :x-iter-from-obj  :symbol #{'x:iter-from-obj}   :emit :abstract
    :op-spec {:arglists '([obj])}}
   {:op :x-iter-from-arr  :symbol #{'x:iter-from-arr}   :emit :abstract
    :op-spec {:arglists '([arr])}}
   {:op :x-iter-from      :symbol #{'x:iter-from}       :emit :abstract
    :op-spec {:arglists '([value])}}
   {:op :x-iter-eq        :symbol #{'x:iter-eq}         :emit :abstract
    :op-spec {:template-only true
              :arglists '([iter0 iter1 eq-fn])
              :type [:fn [:xt/any :xt/any :xt/fn] :xt/bool]}}
   {:op :x-iter-null      :symbol #{'x:iter-null}       :emit :abstract
    :op-spec {:arglists '([])}}
   {:op :x-iter-next      :symbol #{'x:iter-next}       :emit :abstract
    :op-spec {:arglists '([iter])}}
   {:op :x-iter-has?      :symbol #{'x:iter-has?}       :emit :abstract
    :op-spec {:arglists '([iter])
              :type [:fn [:xt/any] :xt/bool]}}
   {:op :x-iter-native?   :symbol #{'x:iter-native?}    :emit :abstract
    :op-spec {:arglists '([iter])
              :type [:fn [:xt/any] :xt/bool]}}])

;;
;; XTALK LANGUAGE SPECIFIC INTERFACES
;;


(def +xt-lang-global+
  [{:op :x-global-set     :symbol #{'x:global-set}      :macro #'tf-global-set   :emit :macro
    :op-spec {:arglists '([sym value])}}
   {:op :x-global-del     :symbol #{'x:global-del}      :macro #'tf-global-del   :emit :macro
    :op-spec {:arglists '([sym])}}
   {:op :x-global-has?    :symbol #{'x:global-has?}     :macro #'tf-global-has?  :emit :macro
    :op-spec {:arglists '([sym])
              :type [:fn [:xt/any] :xt/bool]}}])

(def +xt-lang-proto+
  [{:op :x-this            :symbol #{'x:this}            :emit :abstract
    :op-spec {:arglists '([])}}
   {:op :x-proto-get       :symbol #{'x:proto-get}       :emit :abstract
    :op-spec {:arglists '([obj key])}}
   {:op :x-proto-set       :symbol #{'x:proto-set}       :emit :abstract
    :op-spec {:arglists '([obj key value])}}
   {:op :x-proto-create    :symbol #{'x:proto-create}    :macro #'tf-proto-create   :emit :macro
    :op-spec {:template-only true
              :arglists '([value])}}
   {:op :x-proto-tostring  :symbol #{'x:proto-tostring}  :emit :abstract
    :op-spec {:arglists '([value])}}])

(def +xt-lang-bit+
  [{:op :x-bit-and         :symbol #{'x:bit-and}          :macro #'tf-bit-and  :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/int :xt/int] :xt/int]}}
   {:op :x-bit-or          :symbol #{'x:bit-or}           :macro #'tf-bit-or  :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/int :xt/int] :xt/int]}}
   {:op :x-bit-lshift      :symbol #{'x:bit-lshift}       :macro #'tf-bit-lshift  :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/int :xt/int] :xt/int]}}
   {:op :x-bit-rshift      :symbol #{'x:bit-rshift}       :macro #'tf-bit-rshift  :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/int :xt/int] :xt/int]}}
   {:op :x-bit-xor         :symbol #{'x:bit-xor}          :macro #'tf-bit-xor  :emit :macro
    :op-spec {:arglists '([x y])
              :type [:fn [:xt/int :xt/int] :xt/int]}}])

(def +xt-lang-throw+
  [{:op :x-throw          :symbol #{'x:throw}           :macro #'tf-throw  :emit :macro
    :op-spec {:macro-only true
              :type [:fn [:xt/any] :xt/any]
              :arglists '([value])}}])

(def +xt-lang-unpack+
  [{:op :x-unpack         :symbol #{'x:unpack}          :emit :abstract
    :op-spec {:macro-only true
              :type  [:fn [[:xt/array :xt/any]] [:xt/tuple :xt/any]]
              :arglists '([value])}}])

(def +xt-lang-random+
  [{:op :x-random         :symbol #{'x:random}          :emit :abstract
    :op-spec {:type  [:fn [] :xt/num]
              :arglists '([])}}])

(def +xt-lang-time+
  [{:op :x-now-ms         :symbol #{'x:now-ms}          :emit :abstract
    :op-spec {:arglists '([])
              :type [:fn [] :xt/int]}}])



;;
;; XTALK NOTIFY/LINK SPECIFICATION
;;

(def +xt-notify-socket+
  [{:op :x-notify-socket   :symbol #{'x:notify-socket}   :emit :abstract
    :op-spec {:template-only true
              :type  [:fn [:xt/str :xt/num :xt/any :xt/str :xt/str :xt/fn :xt/fn] :xt/any]
              :arglists '([host port value id key connect-fn encode-fn])}}])

(def +xt-notify-http+
  [{:op :x-notify-http     :symbol #{'x:notify-http}     :emit :hard-link
    :raw 'xt.lang.common-repl/notify-socket-http
    :op-spec {:template-only true
              :type  [:fn [:xt/str :xt/num :xt/any :xt/str :xt/str :xt/fn] :xt/any]
              :arglists '([host port value id key encode-fn])}}])

(def +xt-network-socket+
  [{:op :x-socket-connect  :symbol #{'x:socket-connect}   :emit :abstract
    :op-spec {:template-only true
              :arglists '([host port opts cb])
              :type [:fn [:xt/str :xt/int :xt/any :xt/any] :xt/any]}}
   {:op :x-socket-send     :symbol #{'x:socket-send}      :emit :abstract
    :op-spec {:arglists '([conn message])
              :type [:fn [:xt/any :xt/str] :xt/any]}}
   {:op :x-socket-close    :symbol #{'x:socket-close}     :emit :abstract
    :op-spec {:arglists '([conn])
              :type [:fn [:xt/any] :xt/any]}}])

(def +xt-network-ws+
  [{:op :x-ws-connect      :symbol #{'x:ws-connect}       :emit :abstract
    :op-spec {:template-only true
              :arglists '([ host port opts])
              :type [:fn [:xt/str :xt/int :xt/any] :xt/any]}}
   {:op :x-ws-send         :symbol #{'x:ws-send}          :emit :abstract
    :op-spec {:arglists '([conn value])
              :type [:fn [:xt/any :xt/str] :xt/any]}}
   {:op :x-ws-close        :symbol #{'x:ws-close}         :emit :abstract
    :op-spec {:arglists '([conn])
              :type [:fn [:xt/any] :xt/any]}}])

(def +xt-network-client-basic+
  [{:op :x-client-basic    :symbol #{'x:client-basic}    :emit :abstract
    :op-spec {:template-only true
              :arglists '([host port connect-fn eval-fn])
              :type [:fn [:xt/str :xt/int :xt/fn :xt/fn] :xt/any]}}])

(def +xt-network-client-ws+
  [{:op :x-client-ws       :symbol #{'x:client-ws}       :emit :abstract
    :op-spec {:template-only true
              :arglists '([host port opts connect-fn eval-fn])
              :type [:fn [:xt/str :xt/int :xt/any :xt/fn :xt/fn] :xt/any]}}])


;;
;; XTALK RUNTIME SPECIFIC INTERFACES
;;

(def +xt-runtime-cache+
  [{:op :x-cache          :symbol #{'x:cache}           :emit :abstract
    :op-spec {:arglists '([name])
              :type [:fn [:xt/str] :xt/any]}}
   {:op :x-cache-list     :symbol #{'x:cache-list}      :emit :abstract
    :op-spec {:arglists '([])
              :type [:fn [] [:xt/array :xt/str]]}}
   {:op :x-cache-flush    :symbol #{'x:cache-flush}     :emit :abstract
    :op-spec {:arglists '([cache])
              :type [:fn [:xt/any] :xt/self]}}
   {:op :x-cache-get      :symbol #{'x:cache-get}       :emit :abstract
    :op-spec {:arglists '([cache key])
              :type [:fn [:xt/any :xt/str] :xt/str]}}
   {:op :x-cache-set      :symbol #{'x:cache-set}       :emit :abstract
    :op-spec {:arglists '([cache key value])
              :type [:fn [:xt/any :xt/str :xt/str] :xt/str]}}
   {:op :x-cache-del      :symbol #{'x:cache-del}       :emit :abstract
    :op-spec {:arglists '([cache key])
              :type [:fn [:xt/any :xt/str] :xt/str]}}
   {:op :x-cache-incr     :symbol #{'x:cache-incr}      :emit :abstract
    :op-spec {:template-only true
              :arglists '([cache key val])
              :type [:fn [:xt/any :xt/str :xt/int] :xt/int]}}])


(def +xt-runtime-thread+
  [{:op :x-thread-spawn   :symbol #{'x:thread-spawn}    :emit :abstract
    :op-spec {:template-only true
              :arglists '([f])
              :type [:fn [:xt/fn] :xt/any]}}
   {:op :x-thread-join    :symbol #{'x:thread-join}     :emit :abstract
    :op-spec {:arglists '([thread])
              :type [:fn [:xt/any] :xt/any]}}
   {:op :x-with-delay     :symbol #{'x:with-delay}      :emit :abstract
    :op-spec {:template-only true
              :arglists '([ms value])
              :type [:fn [:xt/int :xt/any] :xt/any]}}
   {:op :x-start-interval :symbol #{'x:start-interval}  :emit :abstract
    :op-spec {:arglists '([ms f])
              :type [:fn [:xt/int :xt/fn] :xt/any]}}
   {:op :x-stop-interval  :symbol #{'x:stop-interval}   :emit :abstract
    :op-spec {:arglists '([id])
              :type [:fn [:xt/str] :xt/any]}}])

(def +xt-runtime-shell+
  [{:op :x-shell          :symbol #{'x:shell}           :emit :abstract
    :op-spec {:template-only true
              :type [:fn [:xt/str :xt/any] :xt/any]
              :arglists '([command opts])}}])

(def +xt-runtime-file+
  [{:op :x-slurp          :symbol #{'x:slurp}         :emit :abstract
    :op-spec {:arglists '([path])
              :type [:fn [:xt/str] :xt/str]}}
   {:op :x-spit           :symbol #{'x:spit}          :emit :abstract
    :op-spec {:arglists '([path value])
              :type [:fn [:xt/str :xt/str] :xt/any]}}])

(def +xt-runtime-b64+
  [{:op :x-b64-encode      :symbol #{'x:b64-encode}     :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/str] :xt/str]}}
   {:op :x-b64-decode      :symbol #{'x:b64-decode}     :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/str] :xt/str]}}])

(def +xt-runtime-uri+
  [{:op :x-uri-encode      :symbol #{'x:uri-encode}     :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/str] :xt/str]}}
   {:op :x-uri-decode      :symbol #{'x:uri-decode}     :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/str] :xt/str]}}])

(def +xt-runtime-js+
  [{:op :x-json-encode      :symbol #{'x:json-encode}       :emit :abstract
    :op-spec {:arglists '([value])
              :type [:fn [:xt/any] :xt/str]}}
   {:op :x-json-decode      :symbol #{'x:json-decode}       :emit :abstract
    :op-spec {:arglists '([expr])
              :type [:fn [:xt/str] :xt/any]}}])
