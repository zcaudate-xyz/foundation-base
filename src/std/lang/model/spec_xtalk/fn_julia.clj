(ns std.lang.model.spec-xtalk.fn-julia
  (:require [std.lib :as h]))

(defn julia-tf-x-del
  [[_ obj]]
  (list 'delete! obj))

(defn julia-tf-x-cat
  [[_ & args]]
  (apply list '* args))

(defn julia-tf-x-len
  [[_ arr]]
  (list 'length arr))

(defn julia-tf-x-get-key
  [[_ obj key default]]
  (list 'get obj key default))

(defn julia-tf-x-err
  [[_ msg]]
  (list 'error msg))

(defn julia-tf-x-eval
  [[_ s]]
  (list 'eval (list 'Meta.parse s)))

(defn julia-tf-x-apply
  [[_ f args]]
  (list f (list :... args)))

(defn julia-tf-x-random
  [_]
  (list 'rand))

(defn julia-tf-x-print
  ([[_ & args]]
   (apply list 'println args)))

(defn julia-tf-x-type-native
  [[_ obj]]
  (list 'string (list 'typeof obj)))

(def +julia-core+
  {:x-del            {:macro #'julia-tf-x-del    :emit :macro}
   :x-cat            {:macro #'julia-tf-x-cat    :emit :macro}
   :x-len            {:macro #'julia-tf-x-len    :emit :macro}
   :x-err            {:macro #'julia-tf-x-err    :emit :macro}
   :x-eval           {:macro #'julia-tf-x-eval   :emit :macro}
   :x-apply          {:macro #'julia-tf-x-apply  :emit :macro}
   :x-unpack         {:raw :... :emit :alias}
   :x-random         {:macro #'julia-tf-x-random :emit :macro}
   :x-print          {:macro #'julia-tf-x-print  :emit :macro}
   :x-now-ms         {:default '(round (* 1000 (time))) :emit :unit}
   :x-get-key        {:macro #'julia-tf-x-get-key :emit :macro}
   :x-type-native    {:macro #'julia-tf-x-type-native :emit :macro}})

;;
;; GLOBAL
;;

(def +julia-global+
  {})

;;
;; MATH
;;

(defn julia-tf-x-m-abs   [[_ num]] (list 'abs num))
(defn julia-tf-x-m-acos  [[_ num]] (list 'acos num))
(defn julia-tf-x-m-asin  [[_ num]] (list 'asin num))
(defn julia-tf-x-m-atan  [[_ num]] (list 'atan num))
(defn julia-tf-x-m-ceil  [[_ num]] (list 'ceil num))
(defn julia-tf-x-m-cos   [[_ num]] (list 'cos num))
(defn julia-tf-x-m-cosh  [[_ num]] (list 'cosh num))
(defn julia-tf-x-m-exp   [[_ num]] (list 'exp num))
(defn julia-tf-x-m-floor [[_ num]] (list 'floor num))
(defn julia-tf-x-m-loge  [[_ num]] (list 'log num))
(defn julia-tf-x-m-log10 [[_ num]] (list 'log10 num))
(defn julia-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn julia-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn julia-tf-x-m-mod   [[_ num denom]] (list :% num denom))
(defn julia-tf-x-m-pow   [[_ base n]] (list '^ base n))
(defn julia-tf-x-m-quot  [[_ num denom]] (list 'div num denom))
(defn julia-tf-x-m-sin   [[_ num]] (list 'sin num))
(defn julia-tf-x-m-sinh  [[_ num]] (list 'sinh num))
(defn julia-tf-x-m-sqrt  [[_ num]] (list 'sqrt num))
(defn julia-tf-x-m-tan   [[_ num]] (list 'tan num))
(defn julia-tf-x-m-tanh  [[_ num]] (list 'tanh num))

(def +julia-math+
  {:x-m-abs           {:macro #'julia-tf-x-m-abs,                 :emit :macro}
   :x-m-acos          {:macro #'julia-tf-x-m-acos,                :emit :macro}
   :x-m-asin          {:macro #'julia-tf-x-m-asin,                :emit :macro}
   :x-m-atan          {:macro #'julia-tf-x-m-atan,                :emit :macro}
   :x-m-ceil          {:macro #'julia-tf-x-m-ceil,                :emit :macro}
   :x-m-cos           {:macro #'julia-tf-x-m-cos,                 :emit :macro}
   :x-m-cosh          {:macro #'julia-tf-x-m-cosh,                :emit :macro}
   :x-m-exp           {:macro #'julia-tf-x-m-exp,                 :emit :macro}
   :x-m-floor         {:macro #'julia-tf-x-m-floor,               :emit :macro}
   :x-m-loge          {:macro #'julia-tf-x-m-loge,                :emit :macro}
   :x-m-log10         {:macro #'julia-tf-x-m-log10,               :emit :macro}
   :x-m-max           {:macro #'julia-tf-x-m-max,                 :emit :macro}
   :x-m-min           {:macro #'julia-tf-x-m-min,                 :emit :macro}
   :x-m-mod           {:macro #'julia-tf-x-m-mod,                 :emit :macro}
   :x-m-pow           {:macro #'julia-tf-x-m-pow,                 :emit :macro}
   :x-m-quot          {:macro #'julia-tf-x-m-quot,                :emit :macro}
   :x-m-sin           {:macro #'julia-tf-x-m-sin,                 :emit :macro}
   :x-m-sinh          {:macro #'julia-tf-x-m-sinh,                :emit :macro}
   :x-m-sqrt          {:macro #'julia-tf-x-m-sqrt,                :emit :macro}
   :x-m-tan           {:macro #'julia-tf-x-m-tan,                 :emit :macro}
   :x-m-tanh          {:macro #'julia-tf-x-m-tanh,                :emit :macro}})

;;
;; TYPE
;;

(defn julia-tf-x-to-string
  [[_ e]]
  (list 'string e))

(defn julia-tf-x-to-number
  [[_ e]]
  (list 'parse 'Float64 e))

(defn julia-tf-x-is-string?
  [[_ e]]
  (list 'isa e 'String))

(defn julia-tf-x-is-number?
  [[_ e]]
  (list 'isa e 'Number))

(defn julia-tf-x-is-integer?
  [[_ e]]
  (list 'isa e 'Integer))

(defn julia-tf-x-is-boolean?
  [[_ e]]
  (list 'isa e 'Bool))

(defn julia-tf-x-is-function?
  [[_ e]]
  (list 'isa e 'Function))

(defn julia-tf-x-is-object?
  [[_ e]]
  (list 'isa e 'Dict))

(defn julia-tf-x-is-array?
  [[_ e]]
  (list 'isa e 'AbstractArray))

(def +julia-type+
  {:x-to-string      {:macro #'julia-tf-x-to-string :emit :macro}
   :x-to-number      {:macro #'julia-tf-x-to-number :emit :macro}
   :x-is-string?     {:macro #'julia-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'julia-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'julia-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'julia-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'julia-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'julia-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'julia-tf-x-is-array? :emit :macro}})

;;
;; OBJ
;;

(defn julia-tf-x-obj-keys
  [[_ obj]]
  (list 'collect (list 'keys obj)))

(defn julia-tf-x-obj-vals
  [[_ obj]]
  (list 'collect (list 'values obj)))

(defn julia-tf-x-obj-pairs
  [[_ obj]]
  (list 'collect obj))

(defn julia-tf-x-obj-clone
  [[_ obj]]
  (list 'copy obj))

(def +julia-obj+
  {:x-obj-keys    {:macro #'julia-tf-x-obj-keys   :emit :macro}
   :x-obj-vals    {:macro #'julia-tf-x-obj-vals   :emit :macro}
   :x-obj-pairs   {:macro #'julia-tf-x-obj-pairs  :emit :macro}
   :x-obj-clone   {:macro #'julia-tf-x-obj-clone  :emit :macro}})

;;
;; ARR
;;

(defn julia-tf-x-arr-clone
  [[_ arr]]
  (list 'copy arr))

(defn julia-tf-x-arr-slice
  [[_ arr start end]]
  (list 'getindex arr (list :to (list '+ start 1) end)))

(defn julia-tf-x-arr-push
  [[_ arr item]]
  (list 'push! arr item))

(defn julia-tf-x-arr-pop
  [[_ arr]]
  (list 'pop! arr))

(defn julia-tf-x-arr-reverse
  [[_ arr]]
  (list 'reverse arr))

(defn julia-tf-x-arr-push-first
  [[_ arr item]]
  (list 'pushfirst! arr item))

(defn julia-tf-x-arr-pop-first
  [[_ arr]]
  (list 'popfirst! arr))

(defn julia-tf-x-arr-insert
  [[_ arr idx e]]
  (list 'insert! arr (list '+ idx 1) e))

(defn julia-tf-x-arr-sort
  [[_ arr key-fn compare-fn]]
  (list 'sort! arr :lt (list 'fn '[a b]
                             (list '< (list key-fn 'a) (list key-fn 'b)))))

(def +julia-arr+
  {:x-arr-clone       {:macro #'julia-tf-x-arr-clone      :emit :macro}
   :x-arr-slice       {:macro #'julia-tf-x-arr-slice      :emit :macro}
   :x-arr-reverse     {:macro #'julia-tf-x-arr-reverse    :emit :macro}
   :x-arr-push        {:macro #'julia-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'julia-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'julia-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'julia-tf-x-arr-pop-first  :emit :macro}
   :x-arr-insert      {:macro #'julia-tf-x-arr-insert     :emit :macro}
   :x-arr-sort        {:macro #'julia-tf-x-arr-sort       :emit :macro}})

;;
;; STRING
;;

(defn julia-tf-x-str-char
  ([[_ s i]]
   (list 'Int (list 'getindex s (list '+ i 1)))))

(defn julia-tf-x-str-split
  ([[_ s tok]]
   (list 'split s tok)))

(defn julia-tf-x-str-join
  ([[_ s arr]]
   (list 'join arr s)))

(defn julia-tf-x-str-index-of
  ([[_ s tok]]
   (list 'findfirst tok s)))

(defn julia-tf-x-str-substring
  ([[_ s start & [end]]]
   (h/$ (getindex ~s (~(list :to (list '+ start 1) (or end '(end))))))))

(defn julia-tf-x-str-to-upper
  ([[_ s]]
   (list 'uppercase s)))

(defn julia-tf-x-str-to-lower
  ([[_ s]]
   (list 'lowercase s)))

(defn julia-tf-x-str-replace
  ([[_ s tok replacement]]
   (list 'replace s (list '=> tok replacement))))

(def +julia-str+
  {:x-str-char       {:macro #'julia-tf-x-str-char      :emit :macro}
   :x-str-split      {:macro #'julia-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'julia-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'julia-tf-x-str-index-of   :emit :macro}
   :x-str-substring  {:macro #'julia-tf-x-str-substring  :emit :macro}
   :x-str-to-upper   {:macro #'julia-tf-x-str-to-upper      :emit :macro}
   :x-str-to-lower   {:macro #'julia-tf-x-str-to-lower      :emit :macro}
   :x-str-replace    {:macro #'julia-tf-x-str-replace    :emit :macro}})

;;
;; JSON
;;

(defn julia-tf-x-json-encode
  ([[_ obj]]
   (list '. 'JSON (list 'json obj))))

(defn julia-tf-x-json-decode
  ([[_ s]]
   (list '. 'JSON (list 'parse s))))

(def +julia-js+
  {:x-json-encode      {:macro #'julia-tf-x-json-encode      :emit :macro}
   :x-json-decode      {:macro #'julia-tf-x-json-decode      :emit :macro}})

(def +julia+
  (merge +julia-core+
         +julia-global+
         +julia-math+
         +julia-type+
         +julia-obj+
         +julia-arr+
         +julia-str+
         +julia-js+))
