(ns std.lang.model.spec-xtalk.fn-erlang
  (:require [std.lib :as h]
            [std.string :as str]))

;;
;; HELPER
;;

(defn erlang-tf-x-fn
  ([[_ & args]]
   (cons 'fn args)))

;;
;; GLOBAL
;;

(def +erlang-global+
  {})

;;
;; MATH
;;

(defn erlang-tf-x-m-abs   [[_ num]] (list 'abs num))
(defn erlang-tf-x-m-acos  [[_ num]] (list :call "math" "acos" num))
(defn erlang-tf-x-m-asin  [[_ num]] (list :call "math" "asin" num))
(defn erlang-tf-x-m-atan  [[_ num]] (list :call "math" "atan" num))
(defn erlang-tf-x-m-ceil  [[_ num]] (list :call "math" "ceil" num))
(defn erlang-tf-x-m-cos   [[_ num]] (list :call "math" "cos" num))
(defn erlang-tf-x-m-cosh  [[_ num]] (list :call "math" "cosh" num))
(defn erlang-tf-x-m-exp   [[_ num]] (list :call "math" "exp" num))
(defn erlang-tf-x-m-floor [[_ num]] (list :call "math" "floor" num))
(defn erlang-tf-x-m-loge  [[_ num]] (list :call "math" "log" num))
(defn erlang-tf-x-m-log10 [[_ num]] (list :call "math" "log10" num))
(defn erlang-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn erlang-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn erlang-tf-x-m-mod   [[_ num denom]] (list 'rem num denom))
(defn erlang-tf-x-m-pow   [[_ base n]] (list :call "math" "pow" base n))
(defn erlang-tf-x-m-quot  [[_ num denom]] (list 'div num denom))
(defn erlang-tf-x-m-sin   [[_ num]] (list :call "math" "sin" num))
(defn erlang-tf-x-m-sinh  [[_ num]] (list :call "math" "sinh" num))
(defn erlang-tf-x-m-sqrt  [[_ num]] (list :call "math" "sqrt" num))
(defn erlang-tf-x-m-tan   [[_ num]] (list :call "math" "tan" num))
(defn erlang-tf-x-m-tanh  [[_ num]] (list :call "math" "tanh" num))

(def +erlang-math+
  {:x-m-abs           {:macro #'erlang-tf-x-m-abs,                 :emit :macro}
   :x-m-acos          {:macro #'erlang-tf-x-m-acos,                :emit :macro}
   :x-m-asin          {:macro #'erlang-tf-x-m-asin,                :emit :macro}
   :x-m-atan          {:macro #'erlang-tf-x-m-atan,                :emit :macro}
   :x-m-ceil          {:macro #'erlang-tf-x-m-ceil,                :emit :macro}
   :x-m-cos           {:macro #'erlang-tf-x-m-cos,                 :emit :macro}
   :x-m-cosh          {:macro #'erlang-tf-x-m-cosh,                :emit :macro}
   :x-m-exp           {:macro #'erlang-tf-x-m-exp,                 :emit :macro}
   :x-m-floor         {:macro #'erlang-tf-x-m-floor,               :emit :macro}
   :x-m-loge          {:macro #'erlang-tf-x-m-loge,                :emit :macro}
   :x-m-log10         {:macro #'erlang-tf-x-m-log10,               :emit :macro}
   :x-m-max           {:macro #'erlang-tf-x-m-max,                 :emit :macro}
   :x-m-min           {:macro #'erlang-tf-x-m-min,                 :emit :macro}
   :x-m-mod           {:macro #'erlang-tf-x-m-mod,                 :emit :macro}
   :x-m-pow           {:macro #'erlang-tf-x-m-pow,                 :emit :macro}
   :x-m-quot          {:macro #'erlang-tf-x-m-quot,                :emit :macro}
   :x-m-sin           {:macro #'erlang-tf-x-m-sin,                 :emit :macro}
   :x-m-sinh          {:macro #'erlang-tf-x-m-sinh,                :emit :macro}
   :x-m-sqrt          {:macro #'erlang-tf-x-m-sqrt,                :emit :macro}
   :x-m-tan           {:macro #'erlang-tf-x-m-tan,                 :emit :macro}
   :x-m-tanh          {:macro #'erlang-tf-x-m-tanh,                :emit :macro}})

;;
;; TYPE
;;

(defn erlang-tf-x-to-string
  [[_ e]]
  (list :call "integer_to_list" e))

(defn erlang-tf-x-to-number
  [[_ e]]
  (list :call "list_to_integer" e))

(defn erlang-tf-x-is-string?
  [[_ e]]
  (list :call "is_list" e))

(defn erlang-tf-x-is-number?
  [[_ e]]
  (list :call "is_number" e))

(defn erlang-tf-x-is-integer?
  [[_ e]]
  (list :call "is_integer" e))

(defn erlang-tf-x-is-boolean?
  [[_ e]]
  (list :call "is_boolean" e))

(defn erlang-tf-x-is-function?
  [[_ e]]
  (list :call "is_function" e))

(defn erlang-tf-x-is-object?
  [[_ e]]
  (list :call "is_map" e))

(defn erlang-tf-x-is-array?
  [[_ e]]
  (list :call "is_list" e))

(def +erlang-type+
  {:x-to-string      {:macro #'erlang-tf-x-to-string :emit :macro}
   :x-to-number      {:macro #'erlang-tf-x-to-number :emit :macro}
   :x-is-string?     {:macro #'erlang-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'erlang-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'erlang-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'erlang-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'erlang-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'erlang-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'erlang-tf-x-is-array? :emit :macro}})

;;
;; OBJ
;;

(defn erlang-tf-x-obj-keys
  [[_ obj]]
  (list :call "maps" "keys" obj))

(defn erlang-tf-x-obj-vals
  [[_ obj]]
  (list :call "maps" "values" obj))

(defn erlang-tf-x-obj-pairs
  [[_ obj]]
  (list :call "maps" "to_list" obj))

(defn erlang-tf-x-obj-clone
  [[_ obj]]
  obj) ;; immutable

(def +erlang-obj+
  {:x-obj-keys    {:macro #'erlang-tf-x-obj-keys   :emit :macro}
   :x-obj-vals    {:macro #'erlang-tf-x-obj-vals   :emit :macro}
   :x-obj-pairs   {:macro #'erlang-tf-x-obj-pairs  :emit :macro}
   :x-obj-clone   {:macro #'erlang-tf-x-obj-clone  :emit :macro}})

;;
;; ARR
;;

(defn erlang-tf-x-arr-clone
  [[_ arr]]
  arr) ;; immutable

(defn erlang-tf-x-arr-slice
  [[_ arr start end]]
  (list :call "lists" "sublist" arr (list '+ start 1) (list '- end start)))

(defn erlang-tf-x-arr-push
  [[_ arr item]]
  (list :call "lists" "append" arr (list 'list item)))

(defn erlang-tf-x-arr-pop
  [[_ arr]]
  (list :call "lists" "droplast" arr))

(defn erlang-tf-x-arr-reverse
  [[_ arr]]
  (list :call "lists" "reverse" arr))

(defn erlang-tf-x-arr-push-first
  [[_ arr item]]
  (list 'list* item arr))

(defn erlang-tf-x-arr-pop-first
  [[_ arr]]
  (list 'tl arr))

(defn erlang-tf-x-arr-insert
  [[_ arr idx e]]
  (list 'let ['(tuple L1 L2) (list :call "lists" "split" idx arr)]
        (list :call "lists" "append" 'L1 (list :call "lists" "append" (list 'list e) 'L2))))

(defn erlang-tf-x-arr-sort
  [[_ arr key-fn compare-fn]]
  (list :call "lists" "sort" arr))

(def +erlang-arr+
  {:x-arr-clone       {:macro #'erlang-tf-x-arr-clone      :emit :macro}
   :x-arr-slice       {:macro #'erlang-tf-x-arr-slice      :emit :macro}
   :x-arr-reverse     {:macro #'erlang-tf-x-arr-reverse    :emit :macro}
   :x-arr-push        {:macro #'erlang-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'erlang-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'erlang-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'erlang-tf-x-arr-pop-first  :emit :macro}
   :x-arr-insert      {:macro #'erlang-tf-x-arr-insert     :emit :macro}
   :x-arr-sort        {:macro #'erlang-tf-x-arr-sort       :emit :macro}})

;;
;; STRING
;;

(defn erlang-tf-x-str-char
  ([[_ s i]]
   (list :call "lists" "nth" (list '+ i 1) s)))

(defn erlang-tf-x-str-split
  ([[_ s tok]]
   (list :call "string" "split" s tok "all")))

(defn erlang-tf-x-str-join
  ([[_ s arr]]
   (list :call "string" "join" arr s)))

(defn erlang-tf-x-str-index-of
  ([[_ s tok]]
   (list :call "string" "str" s tok)))

(defn erlang-tf-x-str-substring
  ([[_ s start & [end]]]
   (list :call "string" "slice" s start (if end (list '- end start) 'infinity))))

(defn erlang-tf-x-str-to-upper
  ([[_ s]]
   (list :call "string" "to_upper" s)))

(defn erlang-tf-x-str-to-lower
  ([[_ s]]
   (list :call "string" "to_lower" s)))

(defn erlang-tf-x-str-replace
  ([[_ s tok replacement]]
   (list :call "string" "replace" s tok replacement "all")))

(def +erlang-str+
  {:x-str-char       {:macro #'erlang-tf-x-str-char      :emit :macro}
   :x-str-split      {:macro #'erlang-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'erlang-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'erlang-tf-x-str-index-of   :emit :macro}
   :x-str-substring  {:macro #'erlang-tf-x-str-substring  :emit :macro}
   :x-str-to-upper   {:macro #'erlang-tf-x-str-to-upper      :emit :macro}
   :x-str-to-lower   {:macro #'erlang-tf-x-str-to-lower      :emit :macro}
   :x-str-replace    {:macro #'erlang-tf-x-str-replace    :emit :macro}})

;;
;; JSON
;;

(defn erlang-tf-x-json-encode
  ([[_ obj]]
   (list :call "json" "encode" obj)))

(defn erlang-tf-x-json-decode
  ([[_ s]]
   (list :call "json" "decode" s)))

(def +erlang-js+
  {:x-json-encode      {:macro #'erlang-tf-x-json-encode      :emit :macro}
   :x-json-decode      {:macro #'erlang-tf-x-json-decode      :emit :macro}})

;;
;; RETURN
;;

(defn erlang-tf-x-return-encode
  ([[_ out id key]]
   (list :call "json" "encode"
         (hash-map "id" id
                   "key" key
                   "type" "data"
                   "value" out))))

(defn erlang-tf-x-return-wrap
  ([[_ f encode-fn]]
   (list 'try
         (list 'let
               (list 'out (list f))
               (list encode-fn 'out nil nil))
         (list 'catch 'error 'E
               (list :call "json" "encode"
                     (hash-map "type" "error"
                               "value" (list :call "list_to_binary"
                                             (list :call "io_lib" "format" "~p" (list 'list 'E)))))))))

(defn erlang-tf-x-return-eval
  ([[_ s wrap-fn]]
   (list 'EvalHelper s wrap-fn)))

(def +erlang-return+
  {:x-return-encode  {:macro #'erlang-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'erlang-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'erlang-tf-x-return-eval     :emit :macro}})

(def +erlang+
  (merge +erlang-global+
         +erlang-math+
         +erlang-type+
         +erlang-obj+
         +erlang-arr+
         +erlang-str+
         +erlang-js+
         +erlang-return+))
