(ns std.lang.model.spec-xtalk.fn-go
  (:require [std.lib.collection :as collection]))

(defn- add-sym [m]
  (collection/map-entries (fn [[k v]]
                   [k (assoc v :symbol #{(symbol (name k))})])
                 m))

;;
;; CORE
;;

(defn go-tf-x-len
  [[_ arr]]
  (list 'len arr))

(defn go-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn go-tf-x-print
  [[_ & args]]
  (apply list 'fmt.Println args))

(defn go-tf-x-err
  [[_ msg]]
  (list 'fmt.Errorf "%v" msg))

(defn go-tf-x-type-native
  [[_ obj]]
  (list 'fmt.Sprintf "%T" obj))

(def +go-core+
  (add-sym
   {:x-print          {:macro #'go-tf-x-print       :emit :macro :value true}
    :x-len            {:macro #'go-tf-x-len          :emit :macro :value true}
    :x-cat            {:macro #'go-tf-x-cat          :emit :macro :value true}
    :x-err            {:macro #'go-tf-x-err          :emit :macro}
    :x-type-native    {:macro #'go-tf-x-type-native  :emit :macro :value true}}))

;;
;; MATH
;;

(defn go-tf-x-m-abs   [[_ n]]   (list 'math.Abs n))
(defn go-tf-x-m-acos  [[_ n]]   (list 'math.Acos n))
(defn go-tf-x-m-asin  [[_ n]]   (list 'math.Asin n))
(defn go-tf-x-m-atan  [[_ n]]   (list 'math.Atan n))
(defn go-tf-x-m-ceil  [[_ n]]   (list 'math.Ceil n))
(defn go-tf-x-m-cos   [[_ n]]   (list 'math.Cos n))
(defn go-tf-x-m-cosh  [[_ n]]   (list 'math.Cosh n))
(defn go-tf-x-m-exp   [[_ n]]   (list 'math.Exp n))
(defn go-tf-x-m-floor [[_ n]]   (list 'math.Floor n))
(defn go-tf-x-m-loge  [[_ n]]   (list 'math.Log n))
(defn go-tf-x-m-log10 [[_ n]]   (list 'math.Log10 n))
(defn go-tf-x-m-max   [[_ a b]] (list 'math.Max a b))
(defn go-tf-x-m-min   [[_ a b]] (list 'math.Min a b))
(defn go-tf-x-m-mod   [[_ a b]] (list 'math.Mod a b))
(defn go-tf-x-m-sin   [[_ n]]   (list 'math.Sin n))
(defn go-tf-x-m-sinh  [[_ n]]   (list 'math.Sinh n))
(defn go-tf-x-m-sqrt  [[_ n]]   (list 'math.Sqrt n))
(defn go-tf-x-m-pow   [[_ b e]] (list 'math.Pow b e))
(defn go-tf-x-m-tan   [[_ n]]   (list 'math.Tan n))
(defn go-tf-x-m-tanh  [[_ n]]   (list 'math.Tanh n))

(def +go-math+
  (add-sym
   {:x-m-abs           {:macro #'go-tf-x-m-abs   :emit :macro :value true}
    :x-m-acos          {:macro #'go-tf-x-m-acos  :emit :macro :value true}
    :x-m-asin          {:macro #'go-tf-x-m-asin  :emit :macro :value true}
    :x-m-atan          {:macro #'go-tf-x-m-atan  :emit :macro :value true}
    :x-m-ceil          {:macro #'go-tf-x-m-ceil  :emit :macro :value true}
    :x-m-cos           {:macro #'go-tf-x-m-cos   :emit :macro :value true}
    :x-m-cosh          {:macro #'go-tf-x-m-cosh  :emit :macro :value true}
    :x-m-exp           {:macro #'go-tf-x-m-exp   :emit :macro :value true}
    :x-m-floor         {:macro #'go-tf-x-m-floor :emit :macro :value true}
    :x-m-loge          {:macro #'go-tf-x-m-loge  :emit :macro :value true}
    :x-m-log10         {:macro #'go-tf-x-m-log10 :emit :macro :value true}
    :x-m-max           {:macro #'go-tf-x-m-max   :emit :macro :value true}
    :x-m-min           {:macro #'go-tf-x-m-min   :emit :macro :value true}
    :x-m-mod           {:macro #'go-tf-x-m-mod   :emit :macro :value true}
    :x-m-sin           {:macro #'go-tf-x-m-sin   :emit :macro :value true}
    :x-m-sinh          {:macro #'go-tf-x-m-sinh  :emit :macro :value true}
    :x-m-sqrt          {:macro #'go-tf-x-m-sqrt  :emit :macro :value true}
    :x-m-pow           {:macro #'go-tf-x-m-pow   :emit :macro :value true}
    :x-m-tan           {:macro #'go-tf-x-m-tan   :emit :macro :value true}
    :x-m-tanh          {:macro #'go-tf-x-m-tanh  :emit :macro :value true}}))

;;
;; TYPE
;;

(defn go-tf-x-to-string
  [[_ e]]
  (list 'fmt.Sprint e))

(defn go-tf-x-to-number
  [[_ e]]
  (list 'strconv.ParseFloat e 64))

(def +go-type+
  (add-sym
   {:x-to-string       {:macro #'go-tf-x-to-string  :emit :macro :value true}
    :x-to-number       {:macro #'go-tf-x-to-number  :emit :macro :value true}}))

;;
;; STRING
;;

(defn go-tf-x-str-split      [[_ s sep]]         (list 'strings.Split s sep))
(defn go-tf-x-str-join       [[_ sep arr]]       (list 'strings.Join arr sep))
(defn go-tf-x-str-index-of   [[_ s substr]]      (list 'strings.Index s substr))
(defn go-tf-x-str-to-upper   [[_ s]]             (list 'strings.ToUpper s))
(defn go-tf-x-str-to-lower   [[_ s]]             (list 'strings.ToLower s))
(defn go-tf-x-str-trim       [[_ s]]             (list 'strings.TrimSpace s))
(defn go-tf-x-str-trim-left  [[_ s]]             (list 'strings.TrimLeft s " "))
(defn go-tf-x-str-trim-right [[_ s]]             (list 'strings.TrimRight s " "))
(defn go-tf-x-str-replace    [[_ s tok repl]]    (list 'strings.ReplaceAll s tok repl))
(defn go-tf-x-str-substring  [[_ s start & [end]]]
  (if end
    (list '. s (list :to start end))
    (list '. s (list :to start (list 'len s)))))

(def +go-str+
  (add-sym
   {:x-str-split       {:macro #'go-tf-x-str-split      :emit :macro :value true}
    :x-str-join        {:macro #'go-tf-x-str-join       :emit :macro :value true}
    :x-str-index-of    {:macro #'go-tf-x-str-index-of   :emit :macro :value true}
    :x-str-to-upper    {:macro #'go-tf-x-str-to-upper   :emit :macro :value true}
    :x-str-to-lower    {:macro #'go-tf-x-str-to-lower   :emit :macro :value true}
    :x-str-trim        {:macro #'go-tf-x-str-trim       :emit :macro :value true}
    :x-str-trim-left   {:macro #'go-tf-x-str-trim-left  :emit :macro :value true}
    :x-str-trim-right  {:macro #'go-tf-x-str-trim-right :emit :macro :value true}
    :x-str-replace     {:macro #'go-tf-x-str-replace    :emit :macro :value true}
    :x-str-substring   {:macro #'go-tf-x-str-substring  :emit :macro :value true}}))

;;
;; ARR
;;

(defn go-tf-x-arr-push
  [[_ arr item]]
  (list := arr (list 'append arr item)))

(defn go-tf-x-arr-pop
  [[_ arr]]
  (list := arr (list '. arr (list :to 0 (list '- (list 'len arr) 1)))))

(defn go-tf-x-arr-push-first
  [[_ arr item]]
  (list := arr (list 'append (list :vec item) arr)))

(defn go-tf-x-arr-pop-first
  [[_ arr]]
  (list := arr (list '. arr (list :to 1 (list 'len arr)))))

(def +go-arr+
  (add-sym
   {:x-arr-push        {:macro #'go-tf-x-arr-push       :emit :macro}
    :x-arr-pop         {:macro #'go-tf-x-arr-pop        :emit :macro}
    :x-arr-push-first  {:macro #'go-tf-x-arr-push-first :emit :macro}
    :x-arr-pop-first   {:macro #'go-tf-x-arr-pop-first  :emit :macro}}))

(def +go+
  (merge +go-core+
         +go-math+
         +go-type+
         +go-str+
         +go-arr+))
