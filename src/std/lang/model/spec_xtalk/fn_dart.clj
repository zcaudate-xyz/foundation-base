(ns std.lang.model.spec-xtalk.fn-dart)

;;
;; CORE
;;

(defn dart-tf-x-len
  [[_ arr]]
  (list '. arr 'length))

(defn dart-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn dart-tf-x-print
  [[_ & args]]
  (apply list 'print args))

(defn dart-tf-x-err
  [[_ msg]]
  (list 'throw (list 'Exception msg)))

(defn dart-tf-x-type-native
  [[_ obj]]
  (list '. obj (list 'runtimeType.toString)))

(def +dart-core+
  {:x-len            {:macro #'dart-tf-x-len         :emit :macro :value true}
   :x-cat            {:macro #'dart-tf-x-cat         :emit :macro :value true}
   :x-print          {:macro #'dart-tf-x-print       :emit :macro :value true}
   :x-err            {:macro #'dart-tf-x-err         :emit :macro}
   :x-type-native    {:macro #'dart-tf-x-type-native :emit :macro :value true}})

;;
;; MATH
;;

(defn dart-tf-x-m-abs   [[_ n]]   (list '. n '(abs)))
(defn dart-tf-x-m-ceil  [[_ n]]   (list '. n '(ceil)))
(defn dart-tf-x-m-floor [[_ n]]   (list '. n '(floor)))
(defn dart-tf-x-m-sqrt  [[_ n]]   (list 'sqrt n))
(defn dart-tf-x-m-pow   [[_ b e]] (list 'pow b e))
(defn dart-tf-x-m-max   [[_ a b]] (list 'max a b))
(defn dart-tf-x-m-min   [[_ a b]] (list 'min a b))
(defn dart-tf-x-m-loge  [[_ n]]   (list 'log n))
(defn dart-tf-x-m-log10 [[_ n]]   (list '/ (list 'log n) (list 'log 10)))
(defn dart-tf-x-m-sin   [[_ n]]   (list 'sin n))
(defn dart-tf-x-m-cos   [[_ n]]   (list 'cos n))
(defn dart-tf-x-m-tan   [[_ n]]   (list 'tan n))
(defn dart-tf-x-m-asin  [[_ n]]   (list 'asin n))
(defn dart-tf-x-m-acos  [[_ n]]   (list 'acos n))
(defn dart-tf-x-m-atan  [[_ n]]   (list 'atan n))
(defn dart-tf-x-m-exp   [[_ n]]   (list 'exp n))

(def +dart-math+
  {:x-m-abs           {:macro #'dart-tf-x-m-abs   :emit :macro :value true}
   :x-m-ceil          {:macro #'dart-tf-x-m-ceil  :emit :macro :value true}
   :x-m-floor         {:macro #'dart-tf-x-m-floor :emit :macro :value true}
   :x-m-sqrt          {:macro #'dart-tf-x-m-sqrt  :emit :macro :value true}
   :x-m-pow           {:macro #'dart-tf-x-m-pow   :emit :macro :value true}
   :x-m-max           {:macro #'dart-tf-x-m-max   :emit :macro :value true}
   :x-m-min           {:macro #'dart-tf-x-m-min   :emit :macro :value true}
   :x-m-loge          {:macro #'dart-tf-x-m-loge  :emit :macro :value true}
   :x-m-log10         {:macro #'dart-tf-x-m-log10 :emit :macro :value true}
   :x-m-sin           {:macro #'dart-tf-x-m-sin   :emit :macro :value true}
   :x-m-cos           {:macro #'dart-tf-x-m-cos   :emit :macro :value true}
   :x-m-tan           {:macro #'dart-tf-x-m-tan   :emit :macro :value true}
   :x-m-asin          {:macro #'dart-tf-x-m-asin  :emit :macro :value true}
   :x-m-acos          {:macro #'dart-tf-x-m-acos  :emit :macro :value true}
   :x-m-atan          {:macro #'dart-tf-x-m-atan  :emit :macro :value true}
   :x-m-exp           {:macro #'dart-tf-x-m-exp   :emit :macro :value true}})

;;
;; TYPE
;;

(defn dart-tf-x-to-string
  [[_ e]]
  (list '. e '(toString)))

(defn dart-tf-x-to-number
  [[_ e]]
  (list 'double.parse e))

(defn dart-tf-x-is-string?
  [[_ e]]
  (list :% e (list :- " is String")))

(defn dart-tf-x-is-number?
  [[_ e]]
  (list :% e (list :- " is num")))

(defn dart-tf-x-is-integer?
  [[_ e]]
  (list :% e (list :- " is int")))

(defn dart-tf-x-is-boolean?
  [[_ e]]
  (list :% e (list :- " is bool")))

(defn dart-tf-x-is-function?
  [[_ e]]
  (list :% e (list :- " is Function")))

(defn dart-tf-x-is-object?
  [[_ e]]
  (list :% e (list :- " is Map")))

(defn dart-tf-x-is-array?
  [[_ e]]
  (list :% e (list :- " is List")))

(def +dart-type+
  {:x-to-string       {:macro #'dart-tf-x-to-string    :emit :macro :value true}
   :x-to-number       {:macro #'dart-tf-x-to-number    :emit :macro :value true}
   :x-is-string?      {:macro #'dart-tf-x-is-string?   :emit :macro :value true}
   :x-is-number?      {:macro #'dart-tf-x-is-number?   :emit :macro :value true}
   :x-is-integer?     {:macro #'dart-tf-x-is-integer?  :emit :macro :value true}
   :x-is-boolean?     {:macro #'dart-tf-x-is-boolean?  :emit :macro :value true}
   :x-is-function?    {:macro #'dart-tf-x-is-function? :emit :macro :value true}
   :x-is-object?      {:macro #'dart-tf-x-is-object?   :emit :macro :value true}
   :x-is-array?       {:macro #'dart-tf-x-is-array?    :emit :macro :value true}})

;;
;; ARR
;;

(defn dart-tf-x-arr-push
  [[_ arr item]]
  (list '. arr (list 'add item)))

(defn dart-tf-x-arr-pop
  [[_ arr]]
  (list '. arr '(removeLast)))

(defn dart-tf-x-arr-push-first
  [[_ arr item]]
  (list '. arr (list 'insert 0 item)))

(defn dart-tf-x-arr-pop-first
  [[_ arr]]
  (list '. arr (list 'removeAt 0)))

(def +dart-arr+
  {:x-arr-push        {:macro #'dart-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'dart-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'dart-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'dart-tf-x-arr-pop-first  :emit :macro}})

;;
;; STRING
;;

(defn dart-tf-x-str-split
  [[_ s sep]]
  (list '. s (list 'split sep)))

(defn dart-tf-x-str-join
  [[_ sep arr]]
  (list '. arr (list 'join sep)))

(defn dart-tf-x-str-index-of
  [[_ s substr]]
  (list '. s (list 'indexOf substr)))

(defn dart-tf-x-str-substring
  [[_ s start & [end]]]
  (if end
    (list '. s (list 'substring start end))
    (list '. s (list 'substring start))))

(defn dart-tf-x-str-to-upper
  [[_ s]]
  (list '. s '(toUpperCase)))

(defn dart-tf-x-str-to-lower
  [[_ s]]
  (list '. s '(toLowerCase)))

(defn dart-tf-x-str-trim
  [[_ s]]
  (list '. s '(trim)))

(defn dart-tf-x-str-trim-left
  [[_ s]]
  (list '. s '(trimLeft)))

(defn dart-tf-x-str-trim-right
  [[_ s]]
  (list '. s '(trimRight)))

(defn dart-tf-x-str-replace
  [[_ s tok repl]]
  (list '. s (list 'replaceAll tok repl)))

(def +dart-str+
  {:x-str-split       {:macro #'dart-tf-x-str-split      :emit :macro :value true}
   :x-str-join        {:macro #'dart-tf-x-str-join       :emit :macro :value true}
   :x-str-index-of    {:macro #'dart-tf-x-str-index-of   :emit :macro :value true}
   :x-str-substring   {:macro #'dart-tf-x-str-substring  :emit :macro :value true}
   :x-str-to-upper    {:macro #'dart-tf-x-str-to-upper   :emit :macro :value true}
   :x-str-to-lower    {:macro #'dart-tf-x-str-to-lower   :emit :macro :value true}
   :x-str-trim        {:macro #'dart-tf-x-str-trim       :emit :macro :value true}
   :x-str-trim-left   {:macro #'dart-tf-x-str-trim-left  :emit :macro :value true}
   :x-str-trim-right  {:macro #'dart-tf-x-str-trim-right :emit :macro :value true}
   :x-str-replace     {:macro #'dart-tf-x-str-replace    :emit :macro :value true}})

;;
;; JSON
;;

(defn dart-tf-x-json-encode
  [[_ obj]]
  (list 'jsonEncode obj))

(defn dart-tf-x-json-decode
  [[_ s]]
  (list 'jsonDecode s))

(def +dart-js+
  {:x-json-encode     {:macro #'dart-tf-x-json-encode :emit :macro :value true}
   :x-json-decode     {:macro #'dart-tf-x-json-decode :emit :macro :value true}})

(def +dart+
  (merge +dart-core+
         +dart-math+
         +dart-type+
         +dart-arr+
         +dart-str+
         +dart-js+))
