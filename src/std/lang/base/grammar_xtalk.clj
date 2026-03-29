(ns std.lang.base.grammar-xtalk
  (:require [std.lang.base.emit-preprocess :as preprocess]))

(defn tf-throw
  "wrapper for throw transform"
  {:added "4.0"}
  [[_ obj]]
  (list 'throw obj))

(defn tf-add
  "wrapper for add transform"
  {:added "4.1"}
  [[_ a b]]
  (list '+ a b))

(defn tf-sub
  "wrapper for sub transform"
  {:added "4.1"}
  [[_ a b]]
  (list '- a b))

(defn tf-mul
  "wrapper for mul transform"
  {:added "4.1"}
  [[_ a b]]
  (list '* a b))

(defn tf-div
  "wrapper for div transform"
  {:added "4.1"}
  [[_ a b]]
  (list '/ a b))

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
  [[_ m]] (list 'return m))

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
      (list 'or val default)
      val)))

(defn tf-get-key
  "get-key transform"
  {:added "4.0"}
  [[_ obj k default]]
  (let [val (if (symbol? obj)
              (list '. obj [k])
              (list '. (list 'quote (list obj)) [k]))]
    (if default
      (list 'or val default)
      val)))

(defn tf-set-key
  "set-key transform"
  {:added "4.0"}
  [[_ obj k v]]
  (list := (list '. obj [k])
        v))

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
  (list 'x:get-idx arr 0))

(defn tf-second
  "gets the second element of an indexed collection"
  {:added "4.1"}
  [[_ arr]]
  (list 'x:get-idx arr 1))

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

(defn tf-lt-string
  "checks if string a sorts before b"
  {:added "4.1"}
  [[_ a b]]
  (list 'x:arr-str-comp a b))

(defn tf-gt-string
  "checks if string a sorts after b"
  {:added "4.1"}
  [[_ a b]]
  (list 'x:arr-str-comp b a))

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

(def +xt-common-basic+
  [{:op :x-del            :symbol #{'x:del}             :emit :abstract}
   {:op :x-cat            :symbol #{'x:cat}             :emit :abstract}
   {:op :x-len            :symbol #{'x:len}             :emit :abstract}
   {:op :x-err            :symbol #{'x:err}             :emit :abstract}
   {:op :x-type-native    :symbol #{'x:type-native}     :emit :abstract}])

(def +xt-common-index+
  [{:op :x-offset         :symbol #{'x:offset}          :macro #'tf-offset      :emit :macro}
   {:op :x-offset-rev     :symbol #{'x:offset-rev}      :macro #'tf-offset-rev  :emit :macro}
   {:op :x-offset-len     :symbol #{'x:offset-len}      :macro #'tf-offset-len  :emit :macro}
   {:op :x-offset-rlen    :symbol #{'x:offset-rlen}     :macro #'tf-offset-rlen :emit :macro}])

(def +xt-common-number+
  [{:op :x-add            :symbol #{'x:add}             :macro #'tf-add    :emit :macro}
   {:op :x-sub            :symbol #{'x:sub}             :macro #'tf-sub    :emit :macro}
   {:op :x-mul            :symbol #{'x:mul}             :macro #'tf-mul    :emit :macro}
   {:op :x-div            :symbol #{'x:div}             :macro #'tf-div    :emit :macro}
   {:op :x-neg            :symbol #{'x:neg}             :macro #'tf-neg    :emit :macro}
   {:op :x-inc            :symbol #{'x:inc}             :macro #'tf-inc    :emit :macro}
   {:op :x-dec            :symbol #{'x:dec}             :macro #'tf-dec    :emit :macro}
   {:op :x-zero?          :symbol #{'x:zero?}           :macro #'tf-zero?       :emit :macro}
   {:op :x-pos?           :symbol #{'x:pos?}            :macro #'tf-pos?        :emit :macro}
   {:op :x-neg?           :symbol #{'x:neg?}            :macro #'tf-neg?        :emit :macro}
   {:op :x-even?          :symbol #{'x:even?}           :macro #'tf-even?       :emit :macro}
   {:op :x-odd?           :symbol #{'x:odd?}            :macro #'tf-odd?        :emit :macro}
   {:op :x-eq             :symbol #{'x:eq}              :macro #'tf-eq     :emit :macro}
   {:op :x-neq            :symbol #{'x:neq}             :macro #'tf-neq    :emit :macro}
   {:op :x-lt             :symbol #{'x:lt}              :macro #'tf-lt     :emit :macro}
   {:op :x-lte            :symbol #{'x:lte}             :macro #'tf-lte    :emit :macro}
   {:op :x-gt             :symbol #{'x:gt}              :macro #'tf-gt     :emit :macro}
   {:op :x-gte            :symbol #{'x:gte}             :macro #'tf-gte    :emit :macro}])

(def +xt-common-nil+
  [{:op :x-not-nil?       :symbol #{'x:not-nil?}        :macro #'tf-not-nil?    :emit :macro}
   {:op :x-nil?           :symbol #{'x:nil?}            :macro #'tf-eq-nil?     :emit :macro}])

(def +xt-common-primitives+
  [{:op :x-to-string      :symbol #{'x:to-string}       :emit :abstract}
   {:op :x-to-number      :symbol #{'x:to-number}       :emit :abstract}
   {:op :x-is-string?     :symbol #{'x:is-string?}      :emit :abstract}
   {:op :x-is-number?     :symbol #{'x:is-number?}      :emit :abstract}
   {:op :x-is-integer?    :symbol #{'x:is-integer?}     :emit :abstract}
   {:op :x-is-boolean?    :symbol #{'x:is-boolean?}     :emit :abstract}])

(def +xt-common-lu+       
  [{:op :x-lu-create      :symbol #{'x:lu-create}       :emit :unit :default {}}
   {:op :x-lu-eq          :symbol #{'x:lu-eq}           :macro #'tf-lu-eq :emit :macro}
   {:op :x-lu-get         :symbol #{'x:lu-get}          :emit :abstract}
   {:op :x-lu-set         :symbol #{'x:lu-set}          :emit :abstract}
   {:op :x-lu-del         :symbol #{'x:lu-del}          :emit :abstract}])

(def +xt-common-object+
  [{:op :x-is-object?     :symbol #{'x:is-object?}      :emit :abstract}
   {:op :x-has-key?       :symbol #{'x:has-key?}        :emit :abstract}
   {:op :x-del-key        :symbol #{'x:del-key}         :macro #'tf-del-key     :emit :macro}
   {:op :x-get-key        :symbol #{'x:get-key}         :macro #'tf-get-key     :emit :macro}
   {:op :x-get-path       :symbol #{'x:get-path}        :macro #'tf-get-path    :emit :macro}
   {:op :x-set-key        :symbol #{'x:set-key}         :macro #'tf-set-key     :emit :macro}
   {:op :x-copy-key       :symbol #{'x:copy-key}        :macro #'tf-copy-key    :emit :macro}
   {:op :x-obj-keys       :symbol #{'x:obj-keys}        :emit :hard-link :raw 'xt.lang.common-data/obj-keys}
   {:op :x-obj-vals       :symbol #{'x:obj-vals}        :emit :hard-link :raw 'xt.lang.base-lib/obj-vals}
   {:op :x-obj-pairs      :symbol #{'x:obj-pairs}       :emit :hard-link :raw 'xt.lang.base-lib/obj-pairs}
   {:op :x-obj-clone      :symbol #{'x:obj-clone}       :emit :hard-link :raw 'xt.lang.base-lib/obj-clone}
   {:op :x-obj-assign     :symbol #{'x:obj-assign}      :emit :hard-link :raw 'xt.lang.base-lib/obj-assign}
   {:op :x-obj-from-pairs :symbol #{'x:obj-from-pairs}  :emit :hard-link :raw 'xt.lang.base-lib/obj-from-pairs}])

(def +xt-common-array+
  [{:op :x-is-array?      :symbol #{'x:is-array?}            :emit :abstract}
   {:op :x-get-idx        :symbol #{'x:get-idx}              :macro #'tf-get-key     :emit :macro}
   {:op :x-set-idx        :symbol #{'x:set-idx}              :macro #'tf-set-key     :emit :macro}
   {:op :x-arr-first           :symbol #{'x:arr-first}       :macro #'tf-first   :emit :macro}
   {:op :x-arr-second          :symbol #{'x:arr-second}      :macro #'tf-second  :emit :macro}
   {:op :x-arr-last            :symbol #{'x:arr-last}        :macro #'tf-last    :emit :macro}
   {:op :x-arr-second-last     :symbol #{'x:arr-second-last} :macro #'tf-second-last :emit :macro}
   {:op :x-arr-remove      :symbol #{'x:arr-remove}       :emit :abstract}
   {:op :x-arr-push        :symbol #{'x:arr-push}         :emit :abstract}
   {:op :x-arr-pop         :symbol #{'x:arr-pop}          :emit :abstract}
   {:op :x-arr-push-first  :symbol #{'x:arr-push-first}   :emit :abstract}
   {:op :x-arr-pop-first   :symbol #{'x:arr-pop-first}    :emit :abstract}
   {:op :x-arr-insert      :symbol #{'x:arr-insert}       :emit :abstract}
   {:op :x-arr-str-comp    :symbol #{'x:arr-str-comp}     :emit :abstract}
   {:op :x-arr-slice       :symbol #{'x:arr-splice}       :emit :hard-link :raw 'xt.lang.base-lib/arr-slice}
   {:op :x-arr-reverse     :symbol #{'x:arr-reverse}      :emit :hard-link :raw 'xt.lang.base-lib/arr-reverse}])

(def +xt-common-print+
  [{:op :x-print          :symbol #{'x:print}           :emit :abstract}])

(def +xt-common-string+
  [{:op :x-str-len         :symbol #{'x:str-len}         :emit :alias :raw 'x:len}
   {:op :x-lt-string       :symbol #{'x:lt-string}       :macro #'tf-lt-string :emit :macro}
   #{'x:arr-str-comp}
   {:op :x-gt-string       :symbol #{'x:gt-string}       :macro #'tf-gt-string :emit :macro}
   #{'x:arr-str-comp}
   {:op :x-str-pad-left    :symbol #{'x:str-pad-left}    :emit :hard-link
    :raw 'xt.lang.common-string/pad-left}
   {:op :x-str-pad-right   :symbol #{'x:str-pad-right}   :emit :hard-link
    :raw 'xt.lang.common-string/pad-right}
   {:op :x-str-starts-with :symbol #{'x:str-starts-with} :emit :hard-link
    :raw 'xt.lang.common-string/starts-with?}
   {:op :x-str-ends-with   :symbol #{'x:str-ends-with}   :emit :hard-link
    :raw 'xt.lang.common-string/ends-with?}
   {:op :x-str-char        :symbol #{'x:str-char}        :emit :abstract}
   {:op :x-str-format      :symbol #{'x:str-format}      :emit :abstract}
   {:op :x-str-split       :symbol #{'x:str-split}       :emit :abstract}
   {:op :x-str-join        :symbol #{'x:str-join}        :emit :abstract}
   {:op :x-str-index-of    :symbol #{'x:str-index-of}    :emit :abstract}
   {:op :x-str-substring   :symbol #{'x:str-substring}   :emit :abstract}
   {:op :x-str-to-upper    :symbol #{'x:str-to-upper}    :emit :abstract}
   {:op :x-str-to-lower    :symbol #{'x:str-to-lower}    :emit :abstract}
   {:op :x-str-to-fixed    :symbol #{'x:str-to-fixed}    :emit :abstract}
   {:op :x-str-replace     :symbol #{'x:str-replace}     :emit :abstract}
   {:op :x-str-trim        :symbol #{'x:str-trim}        :emit :abstract}
   {:op :x-str-trim-left   :symbol #{'x:str-trim-left}   :emit :abstract}
   {:op :x-str-trim-right  :symbol #{'x:str-trim-right}  :emit :abstract}])

(def +xt-common-math+       
  [{:op :x-m-abs          :symbol #{'x:m-abs}        :emit :abstract}
   {:op :x-m-acos         :symbol #{'x:m-acos}       :emit :abstract}
   {:op :x-m-asin         :symbol #{'x:m-asin}       :emit :abstract}
   {:op :x-m-atan         :symbol #{'x:m-atan}       :emit :abstract}
   {:op :x-m-ceil         :symbol #{'x:m-ceil}       :emit :abstract}
   {:op :x-m-cos          :symbol #{'x:m-cos}        :emit :abstract}
   {:op :x-m-cosh         :symbol #{'x:m-cosh}       :emit :abstract}
   {:op :x-m-exp          :symbol #{'x:m-exp}        :emit :abstract}
   {:op :x-m-floor        :symbol #{'x:m-floor}      :emit :abstract}
   {:op :x-m-loge         :symbol #{'x:m-loge}       :emit :abstract}
   {:op :x-m-log10        :symbol #{'x:m-log10}      :emit :abstract}
   {:op :x-m-max          :symbol #{'x:m-max}        :emit :abstract}
   {:op :x-m-mod          :symbol #{'x:m-mod}        :emit :abstract}
   {:op :x-m-min          :symbol #{'x:m-min}        :emit :abstract}
   {:op :x-m-pow          :symbol #{'x:m-pow}        :emit :abstract}
   {:op :x-m-quot         :symbol #{'x:m-quot}       :emit :abstract}
   {:op :x-m-sin          :symbol #{'x:m-sin}        :emit :abstract}
   {:op :x-m-sinh         :symbol #{'x:m-sinh}       :emit :abstract}
   {:op :x-m-sqrt         :symbol #{'x:m-sqrt}       :emit :abstract}
   {:op :x-m-tan          :symbol #{'x:m-tan}        :emit :abstract}
   {:op :x-m-tanh         :symbol #{'x:m-tanh}       :emit :abstract}])

;;
;; XTALK LANGUAGE SPECIFIC INTERFACES
;;

(def +xt-functional-base+
  [{:op :x-is-function?   :symbol #{'x:is-function?}    :emit :abstract}
   {:op :x-callback       :symbol #{'x:callback}        :emit :unit :default nil}
   {:op :x-identity       :symbol #{'x:identity}        :emit :hard-link
    :raw 'xt.lang.common-lib/identity}])

(def +xt-functional-invoke+
  [{:op :x-eval           :symbol #{'x:eval}            :emit :abstract}
   {:op :x-apply          :symbol #{'x:apply}           :emit :abstract}])

(def +xt-functional-return+
  [{:op :x-return-encode   :symbol #{'x:return-encode}   :emit :abstract}
   {:op :x-return-wrap     :symbol #{'x:return-wrap}     :emit :abstract}
   {:op :x-return-eval     :symbol #{'x:return-eval}     :emit :abstract}])

(def +xt-functional-array+
  [{:op :x-arr-sort        :symbol #{'x:arr-sort}         :emit :abstract}
   {:op :x-arr-clone       :symbol #{'x:arr-clone}        :emit :hard-link :raw 'xt.lang.base-lib/arr-clone}
   {:op :x-arr-each        :symbol #{'x:arr-each}         :emit :hard-link :raw 'xt.lang.base-lib/arr-each}
   {:op :x-arr-every       :symbol #{'x:arr-every}        :emit :hard-link :raw 'xt.lang.base-lib/arr-every}
   {:op :x-arr-some        :symbol #{'x:arr-some}         :emit :hard-link :raw 'xt.lang.base-lib/arr-some}
   {:op :x-arr-map         :symbol #{'x:arr-map}          :emit :hard-link :raw 'xt.lang.base-lib/arr-map}
   {:op :x-arr-append      :symbol #{'x:arr-append}       :emit :hard-link :raw 'xt.lang.base-lib/arr-append}
   {:op :x-arr-filter      :symbol #{'x:arr-filter}       :emit :hard-link :raw 'xt.lang.base-lib/arr-filter}
   {:op :x-arr-keep        :symbol #{'x:arr-keep}         :emit :hard-link :raw 'xt.lang.base-lib/arr-keep}
   {:op :x-arr-foldl       :symbol #{'x:arr-foldl}        :emit :hard-link :raw 'xt.lang.base-lib/arr-foldl}
   {:op :x-arr-foldr       :symbol #{'x:arr-foldr}        :emit :hard-link :raw 'xt.lang.base-lib/arr-foldr}
   {:op :x-arr-find        :symbol #{'x:arr-find}         :emit :hard-link :raw 'xt.lang.base-lib/arr-find}])

(def +xt-functional-future+
  [{:op :x-future-run        :symbol #{'x:future-run}        :emit :abstract}
   {:op :x-future-then       :symbol #{'x:future-then}       :emit :abstract}
   {:op :x-future-catch      :symbol #{'x:future-catch}      :emit :abstract}
   {:op :x-future-finally    :symbol #{'x:future-finally}    :emit :abstract}
   {:op :x-future-cancel     :symbol #{'x:future-cancel}     :emit :abstract}
   {:op :x-future-status     :symbol #{'x:future-status}     :emit :abstract}
   {:op :x-future-await      :symbol #{'x:future-await}      :emit :abstract}
   {:op :x-future-from-async :symbol #{'x:future-from-async} :emit :abstract}])

(def +xt-functional-iter+      
  [{:op :x-iter-from-obj  :symbol #{'x:iter-from-obj}   :emit :abstract}
   {:op :x-iter-from-arr  :symbol #{'x:iter-from-arr}   :emit :abstract}
   {:op :x-iter-from      :symbol #{'x:iter-from}       :emit :abstract}
   {:op :x-iter-eq        :symbol #{'x:iter-eq}         :emit :abstract}
   {:op :x-iter-null      :symbol #{'x:iter-null}       :emit :abstract}
   {:op :x-iter-next      :symbol #{'x:iter-next}       :emit :abstract}
   {:op :x-iter-has?      :symbol #{'x:iter-has?}       :emit :abstract}
   {:op :x-iter-native?   :symbol #{'x:iter-native?}    :emit :abstract}])

;;
;; XTALK LANGUAGE SPECIFIC INTERFACES
;;



(def +xt-lang-global+
  [{:op :x-global-set     :symbol #{'x:global-set}      :macro #'tf-global-set   :emit :macro}
   {:op :x-global-del     :symbol #{'x:global-del}      :macro #'tf-global-del   :emit :macro}
   {:op :x-global-has?    :symbol #{'x:global-has?}     :macro #'tf-global-has?  :emit :macro}])

(def +xt-lang-proto+
  [{:op :x-this            :symbol #{'x:this}            :emit :abstract}
   {:op :x-proto-get       :symbol #{'x:proto-get}       :emit :abstract}
   {:op :x-proto-set       :symbol #{'x:proto-set}       :emit :abstract}
   {:op :x-proto-create    :symbol #{'x:proto-create}    :macro #'tf-proto-create   :emit :macro}
   {:op :x-proto-tostring  :symbol #{'x:proto-tostring}  :emit :abstract}])

(def +xt-lang-bit+
  [{:op :x-bit-and         :symbol #{'x:bit-and}          :macro #'tf-bit-and  :emit :macro}
   {:op :x-bit-or          :symbol #{'x:bit-or}           :macro #'tf-bit-or  :emit :macro}
   {:op :x-bit-lshift      :symbol #{'x:bit-lshift}       :macro #'tf-bit-lshift  :emit :macro}
   {:op :x-bit-rshift      :symbol #{'x:bit-rshift}       :macro #'tf-bit-rshift  :emit :macro}
   {:op :x-bit-xor         :symbol #{'x:bit-xor}          :macro #'tf-bit-xor  :emit :macro}])

(def +xt-lang-throw+
  [{:op :x-throw          :symbol #{'x:throw}           :macro #'tf-throw  :emit :macro}])

(def +xt-lang-unpack+
  [{:op :x-unpack         :symbol #{'x:unpack}          :emit :abstract}])

(def +xt-lang-random+
  [{:op :x-random         :symbol #{'x:random}          :emit :abstract}])

(def +xt-lang-time+
  [{:op :x-now-ms         :symbol #{'x:now-ms}          :emit :abstract}])



;;
;; XTALK NOTIFY/LINK SPECIFICATION
;;

(def +xt-notify-socket+
  [{:op :x-notify-socket   :symbol #{'x:notify-socket}   :emit :abstract}])

(def +xt-notify-http+
  [{:op :x-notify-http     :symbol #{'x:notify-http}    :emit :hard-link
    :raw 'xt.lang.base-repl/notify-socket-http}])

(def +xt-network-socket+
  [{:op :x-socket-connect  :symbol #{'x:socket-connect}   :emit :abstract}
   {:op :x-socket-send     :symbol #{'x:socket-send}      :emit :abstract}
   {:op :x-socket-close    :symbol #{'x:socket-close}     :emit :abstract}])

(def +xt-network-ws+
  [{:op :x-ws-connect      :symbol #{'x:ws-connect}       :emit :abstract}
   {:op :x-ws-send         :symbol #{'x:ws-send}          :emit :abstract}
   {:op :x-ws-close        :symbol #{'x:ws-close}         :emit :abstract}])

(def +xt-network-client-basic+
  [{:op :x-client-basic    :symbol #{'x:client-basic}    :emit :abstract}])

(def +xt-network-client-ws+
  [{:op :x-client-ws       :symbol #{'x:client-ws}       :emit :abstract}])

(def +xt-network-server-basic+
  [{:op :x-server-basic    :symbol #{'x:server-basic}    :emit :abstract}])

(def +xt-network-server-ws+
  [{:op :x-server-ws       :symbol #{'x:server-ws}       :emit :abstract}])


;;
;; XTALK RUNTIME SPECIFIC INTERFACES
;;

(def +xt-runtime-cache+
  [{:op :x-cache          :symbol #{'x:cache}           :emit :abstract}
   {:op :x-cache-list     :symbol #{'x:cache-list}      :emit :abstract}
   {:op :x-cache-flush    :symbol #{'x:cache-flush}     :emit :abstract}
   {:op :x-cache-get      :symbol #{'x:cache-get}       :emit :abstract}
   {:op :x-cache-set      :symbol #{'x:cache-set}       :emit :abstract}
   {:op :x-cache-del      :symbol #{'x:cache-del}       :emit :abstract}
   {:op :x-cache-incr     :symbol #{'x:cache-incr}      :emit :abstract}])

(def +xt-runtime-thread+
  [{:op :x-thread-spawn   :symbol #{'x:thread-spawn}    :emit :abstract}
   {:op :x-thread-join    :symbol #{'x:thread-join}     :emit :abstract}
   {:op :x-with-delay     :symbol #{'x:with-delay}      :emit :abstract}
   {:op :x-start-interval :symbol #{'x:start-interval}  :emit :abstract}
   {:op :x-stop-interval  :symbol #{'x:stop-interval}   :emit :abstract}])

(def +xt-runtime-shell+
  [{:op :x-shell          :symbol #{'x:shell}           :emit :abstract}])

(def +xt-runtime-file+
  [{:op :x-slurp          :symbol #{'x:slurp}         :emit :abstract}
   {:op :x-spit           :symbol #{'x:spit}          :emit :abstract}])

(def +xt-runtime-b64+
  [{:op :x-b64-encode      :symbol #{'x:b64-encode}     :emit :abstract}
   {:op :x-b64-decode      :symbol #{'x:b64-decode}     :emit :abstract}])

(def +xt-runtime-uri+
  [{:op :x-uri-encode      :symbol #{'x:uri-encode}     :emit :abstract}
   {:op :x-uri-decode      :symbol #{'x:uri-decode}     :emit :abstract}])

(def +xt-runtime-js+
  [{:op :x-json-encode      :symbol #{'x:json-encode}       :emit :abstract}
   {:op :x-json-decode      :symbol #{'x:json-decode}       :emit :abstract}])
