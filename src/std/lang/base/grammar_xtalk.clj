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
;; X-LANG
;;

(defn xtalk-intrinsic
  ([m]
   (xtalk-intrinsic m #{}))
  ([m requires]
   (merge {:class :intrinsic
           :requires (set requires)
           :intrinsic/callable false
           :intrinsic/template nil
           :intrinsic/fn nil
           :default nil}
          m)))

(defn xtalk-callable-intrinsic
  ([m]
   (xtalk-callable-intrinsic m #{}))
  ([{:keys [symbol]
     :as m} requires]
   (xtalk-intrinsic
    (merge {:intrinsic/callable true
            :intrinsic/template (:macro m)
            :intrinsic/fn (first symbol)}
           m)
    requires)))

(defn xtalk-adapter
  ([m]
   (xtalk-adapter m #{}))
  ([m requires]
   (merge {:class :adapter
           :requires (set requires)
           :default nil}
          m)))

(def +op-xtalk-core-value+
  [(xtalk-callable-intrinsic {:op :x-identity       :symbol #{'x:identity}        :type :hard-link
                     :raw 'xt.lang.common-base/identity})
   (xtalk-callable-intrinsic {:op :x-add            :symbol #{'x:add}             :macro #'tf-add    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-sub            :symbol #{'x:sub}             :macro #'tf-sub    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-mul            :symbol #{'x:mul}             :macro #'tf-mul    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-div            :symbol #{'x:div}             :macro #'tf-div    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-neg            :symbol #{'x:neg}             :macro #'tf-neg    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-inc            :symbol #{'x:inc}             :macro #'tf-inc    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-dec            :symbol #{'x:dec}             :macro #'tf-dec    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-eq             :symbol #{'x:eq}              :macro #'tf-eq     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-neq            :symbol #{'x:neq}             :macro #'tf-neq    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-lt             :symbol #{'x:lt}              :macro #'tf-lt     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-lte            :symbol #{'x:lte}             :macro #'tf-lte    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-gt             :symbol #{'x:gt}              :macro #'tf-gt     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-gte            :symbol #{'x:gte}             :macro #'tf-gte    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-del            :symbol #{'x:del}             :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-cat            :symbol #{'x:cat}             :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-len            :symbol #{'x:len}             :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-err            :symbol #{'x:err}             :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-throw          :symbol #{'x:throw}           :macro #'tf-throw  :emit :macro})])

(def +op-xtalk-core-invoke+
  [(xtalk-callable-intrinsic {:op :x-eval           :symbol #{'x:eval}            :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-apply          :symbol #{'x:apply}           :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-unpack         :symbol #{'x:unpack}          :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-print          :symbol #{'x:print}           :emit :abstract})])

(def +op-xtalk-runtime+
  [(xtalk-callable-intrinsic {:op :x-random         :symbol #{'x:random}          :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-now-ms         :symbol #{'x:now-ms}          :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-type-native    :symbol #{'x:type-native}     :emit :abstract})])

(def +op-xtalk-system+
  [(xtalk-adapter {:op :x-shell          :symbol #{'x:shell}           :emit :abstract})])

(def +op-xtalk-task+
  [(xtalk-adapter {:op :x-task-run       :symbol #{'x:task-run}        :emit :abstract})
   (xtalk-adapter {:op :x-task-then      :symbol #{'x:task-then}       :emit :abstract})
   (xtalk-adapter {:op :x-task-catch     :symbol #{'x:task-catch}      :emit :abstract})
   (xtalk-adapter {:op :x-task-finally   :symbol #{'x:task-finally}    :emit :abstract})
   (xtalk-adapter {:op :x-task-cancel    :symbol #{'x:task-cancel}     :emit :abstract})
   (xtalk-adapter {:op :x-task-status    :symbol #{'x:task-status}     :emit :abstract})
   (xtalk-adapter {:op :x-task-await     :symbol #{'x:task-await}      :emit :abstract})
   (xtalk-adapter {:op :x-task-from-async :symbol #{'x:task-from-async} :emit :abstract})])

(def +op-xtalk-proto+
  [(xtalk-intrinsic {:op :x-this            :symbol #{'x:this}            :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-proto-get       :symbol #{'x:proto-get}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-proto-set       :symbol #{'x:proto-set}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-proto-create    :symbol #{'x:proto-create}    :macro #'tf-proto-create   :emit :macro})
   (xtalk-callable-intrinsic {:op :x-proto-tostring  :symbol #{'x:proto-tostring}  :emit :abstract})])

(def +op-xtalk-global+
  [(xtalk-callable-intrinsic {:op :x-global-set     :symbol #{'x:global-set}      :macro #'tf-global-set   :emit :macro})
   (xtalk-callable-intrinsic {:op :x-global-del     :symbol #{'x:global-del}      :macro #'tf-global-del   :emit :macro})
   (xtalk-callable-intrinsic {:op :x-global-has?    :symbol #{'x:global-has?}     :macro #'tf-global-has?  :emit :macro})])

(def +op-xtalk-predicate+
  [(xtalk-callable-intrinsic {:op :x-not-nil?       :symbol #{'x:not-nil?}        :macro #'tf-not-nil?    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-nil?           :symbol #{'x:nil?}            :macro #'tf-eq-nil?     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-zero?          :symbol #{'x:zero?}           :macro #'tf-zero?       :emit :macro})
   (xtalk-callable-intrinsic {:op :x-pos?           :symbol #{'x:pos?}            :macro #'tf-pos?        :emit :macro})
   (xtalk-callable-intrinsic {:op :x-neg?           :symbol #{'x:neg?}            :macro #'tf-neg?        :emit :macro})
   (xtalk-callable-intrinsic {:op :x-even?          :symbol #{'x:even?}           :macro #'tf-even?       :emit :macro})
   (xtalk-callable-intrinsic {:op :x-odd?           :symbol #{'x:odd?}            :macro #'tf-odd?        :emit :macro})
   (xtalk-callable-intrinsic {:op :x-is-empty?      :symbol #{'x:is-empty?}       :type :hard-link
                     :raw 'xt.lang.common-data/is-empty?})
   (xtalk-callable-intrinsic {:op :x-not-empty?     :symbol #{'x:not-empty?}      :type :hard-link
                     :raw 'xt.lang.common-data/not-empty?})
   (xtalk-callable-intrinsic {:op :x-has-key?       :symbol #{'x:has-key?}        :macro #'tf-has-key?    :emit :macro}
                    #{'x:get-key})])

(def +op-xtalk-access+
  [(xtalk-callable-intrinsic {:op :x-del-key        :symbol #{'x:del-key}         :macro #'tf-del-key     :emit :macro}
                    #{'x:del})
   (xtalk-callable-intrinsic {:op :x-get-key        :symbol #{'x:get-key}         :macro #'tf-get-key     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-get-path       :symbol #{'x:get-path}        :macro #'tf-get-path    :emit :macro})
   (xtalk-callable-intrinsic {:op :x-get-in         :symbol #{'x:get-in}          :type :hard-link
                     :raw 'xt.lang.common-data/get-in})
   (xtalk-callable-intrinsic {:op :x-get-idx        :symbol #{'x:get-idx}         :macro #'tf-get-key     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-set-key        :symbol #{'x:set-key}         :macro #'tf-set-key     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-set-in         :symbol #{'x:set-in}          :type :hard-link
                     :raw 'xt.lang.common-data/set-in})
   (xtalk-callable-intrinsic {:op :x-set-idx        :symbol #{'x:set-idx}         :macro #'tf-set-key     :emit :macro})
   (xtalk-callable-intrinsic {:op :x-copy-key       :symbol #{'x:copy-key}        :macro #'tf-copy-key    :emit :macro}
                    #{'x:get-key 'x:set-key})
   (xtalk-callable-intrinsic {:op :x-walk           :symbol #{'x:walk}            :type :hard-link
                     :raw 'xt.lang.base-lib/walk})])

(def +op-xtalk-index+
  [(xtalk-callable-intrinsic {:op :x-offset         :symbol #{'x:offset}          :macro #'tf-offset      :emit :macro})
   (xtalk-callable-intrinsic {:op :x-offset-rev     :symbol #{'x:offset-rev}      :macro #'tf-offset-rev  :emit :macro})
   (xtalk-callable-intrinsic {:op :x-offset-len     :symbol #{'x:offset-len}      :macro #'tf-offset-len  :emit :macro})
   (xtalk-callable-intrinsic {:op :x-offset-rlen    :symbol #{'x:offset-rlen}     :macro #'tf-offset-rlen :emit :macro})])

(def +op-xtalk-callback+
  [(xtalk-adapter {:op :x-callback       :symbol #{'x:callback}        :emit :unit :default nil})])

(def +op-xtalk-math+       
  [(xtalk-callable-intrinsic {:op :x-m-abs          :symbol #{'x:m-abs}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-acos         :symbol #{'x:m-acos}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-asin         :symbol #{'x:m-asin}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-atan         :symbol #{'x:m-atan}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-ceil         :symbol #{'x:m-ceil}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-cos          :symbol #{'x:m-cos}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-cosh         :symbol #{'x:m-cosh}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-exp          :symbol #{'x:m-exp}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-floor        :symbol #{'x:m-floor}      :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-loge         :symbol #{'x:m-loge}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-log10        :symbol #{'x:m-log10}      :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-max          :symbol #{'x:m-max}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-mod          :symbol #{'x:m-mod}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-min          :symbol #{'x:m-min}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-pow          :symbol #{'x:m-pow}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-quot         :symbol #{'x:m-quot}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-sin          :symbol #{'x:m-sin}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-sinh         :symbol #{'x:m-sinh}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-sqrt         :symbol #{'x:m-sqrt}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-tan          :symbol #{'x:m-tan}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-m-tanh         :symbol #{'x:m-tanh}       :emit :abstract})])

(def +op-xtalk-type+
  [(xtalk-callable-intrinsic {:op :x-to-string      :symbol #{'x:to-string}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-to-number      :symbol #{'x:to-number}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-is-string?     :symbol #{'x:is-string?}      :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-is-number?     :symbol #{'x:is-number?}      :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-is-integer?    :symbol #{'x:is-integer?}     :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-is-boolean?    :symbol #{'x:is-boolean?}     :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-is-function?   :symbol #{'x:is-function?}    :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-is-object?     :symbol #{'x:is-object?}      :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-is-array?      :symbol #{'x:is-array?}       :emit :abstract})])

(def +op-xtalk-bit+
  [(xtalk-callable-intrinsic {:op :x-bit-and         :symbol #{'x:bit-and}          :macro #'tf-bit-and  :emit :macro})
   (xtalk-callable-intrinsic {:op :x-bit-or          :symbol #{'x:bit-or}           :macro #'tf-bit-or  :emit :macro})
   (xtalk-callable-intrinsic {:op :x-bit-lshift      :symbol #{'x:bit-lshift}       :macro #'tf-bit-lshift  :emit :macro})
   (xtalk-callable-intrinsic {:op :x-bit-rshift      :symbol #{'x:bit-rshift}       :macro #'tf-bit-rshift  :emit :macro})
   (xtalk-callable-intrinsic {:op :x-bit-xor         :symbol #{'x:bit-xor}          :macro #'tf-bit-xor  :emit :macro})])

(def +op-xtalk-lu+       
  [(xtalk-callable-intrinsic {:op :x-lu-create      :symbol #{'x:lu-create}       :emit :unit :default {}})
   (xtalk-callable-intrinsic {:op :x-lu-eq          :symbol #{'x:lu-eq}           :macro #'tf-lu-eq :emit :macro})
   (xtalk-callable-intrinsic {:op :x-lu-get         :symbol #{'x:lu-get}          :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-lu-set         :symbol #{'x:lu-set}          :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-lu-del         :symbol #{'x:lu-del}          :emit :abstract})])

(def +op-xtalk-obj+
  [(xtalk-intrinsic {:op :x-obj-keys       :symbol #{'x:obj-keys}          :type :hard-link
                     :raw 'xt.lang.common-data/obj-keys})
   (xtalk-intrinsic {:op :x-obj-vals       :symbol #{'x:obj-vals}          :type :hard-link
                     :raw 'xt.lang.base-lib/obj-vals})
   (xtalk-intrinsic {:op :x-obj-pairs      :symbol #{'x:obj-pairs}         :type :hard-link
                     :raw 'xt.lang.base-lib/obj-pairs})
   (xtalk-intrinsic {:op :x-obj-clone      :symbol #{'x:obj-clone}         :type :hard-link
                     :raw 'xt.lang.base-lib/obj-clone})
   (xtalk-intrinsic {:op :x-obj-assign     :symbol #{'x:obj-assign}        :type :hard-link
                     :raw 'xt.lang.base-lib/obj-assign})
   (xtalk-intrinsic {:op :x-obj-assign-nested :symbol #{'x:obj-assign-nested} :type :hard-link
                     :raw 'xt.lang.base-lib/obj-assign-nested})
   (xtalk-intrinsic {:op :x-obj-assign-with :symbol #{'x:obj-assign-with} :type :hard-link
                     :raw 'xt.lang.base-lib/obj-assign-with})
   (xtalk-intrinsic {:op :x-obj-from-pairs :symbol #{'x:obj-from-pairs}   :type :hard-link
                     :raw 'xt.lang.base-lib/obj-from-pairs})
   (xtalk-intrinsic {:op :x-obj-map        :symbol #{'x:obj-map}           :type :hard-link
                     :raw 'xt.lang.base-lib/obj-map})])

(def +op-xtalk-arr+
  [(xtalk-callable-intrinsic {:op :x-first           :symbol #{'x:first}            :macro #'tf-first   :emit :macro}
                             #{'x:get-idx})
   (xtalk-callable-intrinsic {:op :x-second          :symbol #{'x:second}           :macro #'tf-second  :emit :macro}
                             #{'x:get-idx})
   (xtalk-callable-intrinsic {:op :x-last            :symbol #{'x:last}             :macro #'tf-last    :emit :macro}
                             #{'x:get-idx 'x:offset-len 'x:len})
   (xtalk-callable-intrinsic {:op :x-second-last     :symbol #{'x:second-last}      :macro #'tf-second-last :emit :macro}
                             #{'x:get-idx 'x:len 'x:offset})
   (xtalk-callable-intrinsic {:op :x-arrayify        :symbol #{'x:arrayify}         :type :hard-link
                     :raw 'xt.lang.common-data/arrayify})
   (xtalk-callable-intrinsic {:op :x-arr-clone       :symbol #{'x:arr-clone}        :type :hard-link
                     :raw 'xt.lang.base-lib/arr-clone})
   (xtalk-callable-intrinsic {:op :x-arr-each        :symbol #{'x:arr-each}         :type :hard-link
                     :raw 'xt.lang.base-lib/arr-each})
   (xtalk-callable-intrinsic {:op :x-arr-every       :symbol #{'x:arr-every}        :type :hard-link
                     :raw 'xt.lang.base-lib/arr-every})
   (xtalk-callable-intrinsic {:op :x-arr-some        :symbol #{'x:arr-some}         :type :hard-link
                     :raw 'xt.lang.base-lib/arr-some})
   (xtalk-callable-intrinsic {:op :x-arr-map         :symbol #{'x:arr-map}          :type :hard-link
                     :raw 'xt.lang.base-lib/arr-map})
   (xtalk-callable-intrinsic {:op :x-arr-append      :symbol #{'x:arr-append}       :type :hard-link
                     :raw 'xt.lang.base-lib/arr-append})
   (xtalk-callable-intrinsic {:op :x-arr-filter      :symbol #{'x:arr-filter}       :type :hard-link
                     :raw 'xt.lang.base-lib/arr-filter})
   (xtalk-callable-intrinsic {:op :x-arr-keep        :symbol #{'x:arr-keep}         :type :hard-link
                     :raw 'xt.lang.base-lib/arr-keep})
   (xtalk-callable-intrinsic {:op :x-arr-lookup      :symbol #{'x:arr-lookup}       :type :hard-link
                     :raw 'xt.lang.base-lib/arr-lookup})
   (xtalk-callable-intrinsic {:op :x-arr-juxt        :symbol #{'x:arr-juxt}         :type :hard-link
                     :raw 'xt.lang.base-lib/arr-juxt})
   (xtalk-callable-intrinsic {:op :x-arr-foldl       :symbol #{'x:arr-foldl}        :type :hard-link
                     :raw 'xt.lang.base-lib/arr-foldl})
   (xtalk-callable-intrinsic {:op :x-arr-foldr       :symbol #{'x:arr-foldr}        :type :hard-link
                     :raw 'xt.lang.base-lib/arr-foldr})
   (xtalk-callable-intrinsic {:op :x-arr-slice       :symbol #{'x:arr-splice}       :type :hard-link
                     :raw 'xt.lang.base-lib/arr-slice})
   (xtalk-callable-intrinsic {:op :x-arr-reverse     :symbol #{'x:arr-reverse}      :type :hard-link
                     :raw 'xt.lang.base-lib/arr-reverse})
   (xtalk-callable-intrinsic {:op :x-arr-find        :symbol #{'x:arr-find}         :type :hard-link
                     :raw 'xt.lang.base-lib/arr-find})
   (xtalk-callable-intrinsic {:op :x-arr-remove      :symbol #{'x:arr-remove}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-arr-push        :symbol #{'x:arr-push}         :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-arr-pop         :symbol #{'x:arr-pop}          :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-arr-push-first  :symbol #{'x:arr-push-first}   :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-arr-pop-first   :symbol #{'x:arr-pop-first}    :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-arr-insert      :symbol #{'x:arr-insert}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-arr-sort        :symbol #{'x:arr-sort}         :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-arr-str-comp    :symbol #{'x:arr-str-comp}     :emit :abstract})])

(def +op-xtalk-str+
  [(xtalk-callable-intrinsic {:op :x-str-len         :symbol #{'x:str-len}         :emit :alias :raw 'x:len}
                    #{'x:len})
   (xtalk-callable-intrinsic {:op :x-lt-string      :symbol #{'x:lt-string}       :macro #'tf-lt-string :emit :macro}
                             #{'x:arr-str-comp})
   (xtalk-callable-intrinsic {:op :x-gt-string      :symbol #{'x:gt-string}       :macro #'tf-gt-string :emit :macro}
                             #{'x:arr-str-comp})
   (xtalk-callable-intrinsic {:op :x-str-pad-left    :symbol #{'x:str-pad-left}    :type :hard-link
                     :raw 'xt.lang.common-string/pad-left})
   (xtalk-callable-intrinsic {:op :x-str-pad-right   :symbol #{'x:str-pad-right}   :type :hard-link
                     :raw 'xt.lang.common-string/pad-right})
   (xtalk-callable-intrinsic {:op :x-str-starts-with :symbol #{'x:str-starts-with} :type :hard-link
                     :raw 'xt.lang.common-string/starts-with?})
   (xtalk-callable-intrinsic {:op :x-str-ends-with   :symbol #{'x:str-ends-with}   :type :hard-link
                     :raw 'xt.lang.common-string/ends-with?})
   (xtalk-callable-intrinsic {:op :x-str-char        :symbol #{'x:str-char}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-format      :symbol #{'x:str-format}      :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-split       :symbol #{'x:str-split}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-join        :symbol #{'x:str-join}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-index-of    :symbol #{'x:str-index-of}    :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-substring   :symbol #{'x:str-substring}   :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-to-upper    :symbol #{'x:str-to-upper}    :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-to-lower    :symbol #{'x:str-to-lower}    :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-to-fixed    :symbol #{'x:str-to-fixed}    :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-replace     :symbol #{'x:str-replace}     :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-trim        :symbol #{'x:str-trim}        :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-trim-left   :symbol #{'x:str-trim-left}   :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-str-trim-right  :symbol #{'x:str-trim-right}  :emit :abstract})])

(def +op-xtalk-js+
  [(xtalk-callable-intrinsic {:op :x-json-encode      :symbol #{'x:json-encode}       :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-json-decode      :symbol #{'x:json-decode}       :emit :abstract})])

(def +op-xtalk-return+
  [(xtalk-intrinsic {:op :x-return-encode   :symbol #{'x:return-encode}   :emit :abstract})
   (xtalk-intrinsic {:op :x-return-wrap     :symbol #{'x:return-wrap}     :emit :abstract})
   (xtalk-intrinsic {:op :x-return-eval     :symbol #{'x:return-eval}     :emit :abstract})])

(def +op-xtalk-socket+
  [(xtalk-adapter {:op :x-socket-connect  :symbol #{'x:socket-connect}   :emit :abstract})
   (xtalk-adapter {:op :x-socket-send     :symbol #{'x:socket-send}      :emit :abstract})
   (xtalk-adapter {:op :x-socket-close    :symbol #{'x:socket-close}     :emit :abstract})])

(def +op-xtalk-ws+
  [(xtalk-adapter {:op :x-ws-connect      :symbol #{'x:ws-connect}       :emit :abstract})
   (xtalk-adapter {:op :x-ws-send         :symbol #{'x:ws-send}          :emit :abstract})
   (xtalk-adapter {:op :x-ws-close        :symbol #{'x:ws-close}         :emit :abstract})])

(def +op-xtalk-iter+      
  [(xtalk-intrinsic {:op :x-iter-from-obj  :symbol #{'x:iter-from-obj}   :emit :abstract})
   (xtalk-intrinsic {:op :x-iter-from-arr  :symbol #{'x:iter-from-arr}   :emit :abstract})
   (xtalk-intrinsic {:op :x-iter-from      :symbol #{'x:iter-from}       :emit :abstract})
   (xtalk-intrinsic {:op :x-iter-eq        :symbol #{'x:iter-eq}         :emit :abstract})
   (xtalk-intrinsic {:op :x-iter-null      :symbol #{'x:iter-null}       :emit :unit :default '(return)})
   (xtalk-intrinsic {:op :x-iter-next      :symbol #{'x:iter-next}       :emit :abstract})
   (xtalk-intrinsic {:op :x-iter-has?      :symbol #{'x:iter-has?}       :emit :abstract})
   (xtalk-intrinsic {:op :x-iter-native?   :symbol #{'x:iter-native?}    :emit :abstract})])

(def +op-xtalk-cache+
  [(xtalk-adapter {:op :x-cache          :symbol #{'x:cache}           :emit :abstract})
   (xtalk-adapter {:op :x-cache-list     :symbol #{'x:cache-list}      :emit :abstract})
   (xtalk-adapter {:op :x-cache-flush    :symbol #{'x:cache-flush}     :emit :abstract})
   (xtalk-adapter {:op :x-cache-get      :symbol #{'x:cache-get}       :emit :abstract})
   (xtalk-adapter {:op :x-cache-set      :symbol #{'x:cache-set}       :emit :abstract})
   (xtalk-adapter {:op :x-cache-del      :symbol #{'x:cache-del}       :emit :abstract})
   (xtalk-adapter {:op :x-cache-incr     :symbol #{'x:cache-incr}      :emit :abstract})])

(def +op-xtalk-thread+
  [(xtalk-adapter {:op :x-thread-spawn   :symbol #{'x:thread-spawn}    :emit :abstract})
   (xtalk-adapter {:op :x-thread-join    :symbol #{'x:thread-join}     :emit :abstract})
   (xtalk-adapter {:op :x-with-delay     :symbol #{'x:with-delay}      :emit :abstract})
   (xtalk-adapter {:op :x-start-interval :symbol #{'x:start-interval}  :emit :abstract})
   (xtalk-adapter {:op :x-stop-interval  :symbol #{'x:stop-interval}   :emit :abstract})])

(def +op-xtalk-file+
  [(xtalk-adapter {:op :x-slurp          :symbol #{'x:slurp}         :emit :abstract})
   (xtalk-adapter {:op :x-spit           :symbol #{'x:spit}          :emit :abstract})])

(def +op-xtalk-b64+
  [(xtalk-callable-intrinsic {:op :x-b64-encode      :symbol #{'x:b64-encode}     :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-b64-decode      :symbol #{'x:b64-decode}     :emit :abstract})])

(def +op-xtalk-uri+
  [(xtalk-callable-intrinsic {:op :x-uri-encode      :symbol #{'x:uri-encode}     :emit :abstract})
   (xtalk-callable-intrinsic {:op :x-uri-decode      :symbol #{'x:uri-decode}     :emit :abstract})])

(def +op-xtalk-notify+
  [(xtalk-adapter {:op :x-notify-socket   :symbol #{'x:notify-socket}   :emit :abstract})])

(def +op-xtalk-service+
  [(xtalk-adapter {:op :x-client-basic    :symbol #{'x:client-basic}    :emit :abstract})
   (xtalk-adapter {:op :x-client-ws       :symbol #{'x:client-ws}       :emit :abstract})
   (xtalk-adapter {:op :x-server-basic    :symbol #{'x:server-basic}    :emit :abstract})
   (xtalk-adapter {:op :x-server-ws       :symbol #{'x:server-ws}       :emit :abstract})])

(def +op-xtalk-special+
  [(xtalk-adapter {:op :x-notify-http     :symbol #{'x:notify-http}    :type :hard-link
                   :raw 'xt.lang.base-repl/notify-socket-http})])

(comment
  (./reload-specs)

  )


(comment
  (def +op-xtalk-arr-generic+
  '[{:op :x-arr-every       :symbol #{'x:arr-every}   :emit :alias
     :raw xt.lang.base-lib/arr-every}
    {:op :x-arr-some        :symbol #{'x:arr-some}
     :raw xt.lang.base-lib/arr-every}
    {:op :x-arr-foldl       :symbol #{'x:arr-foldl}
     :raw xt.lang.base-lib/arr-foldl}
    {:op :x-arr-foldr       :symbol #{'x:arr-foldr}         :macro #'tf-arr-foldr   :emit :macro}
    {:op :x-arr-reverse     :symbol #{'x:arr-reverse}       :macro #'tf-arr-reverse :emit :macro}])  
  {:op :x-str-pad-left    :symbol #{'x:str-pad-left}    :macro #'tf-str-pad-left  :emit :macro}
  {:op :x-str-pad-right   :symbol #{'x:str-pad-right}   :macro #'tf-str-pad-right :emit :macro}
  {:op :x-str-starts-with :symbol #{'x:str-starts-with} :macro #'tf-str-starts-with :emit :macro}
  {:op :x-str-ends-with   :symbol #{'x:str-ends-with}   :macro #'tf-str-ends-with :emit :macro}
  )
