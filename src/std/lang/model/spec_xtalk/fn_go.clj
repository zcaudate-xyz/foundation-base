(ns std.lang.model.spec-xtalk.fn-go
  (:require [std.lib :as h]))

(defn- add-sym [m]
  (h/map-entries (fn [[k v]]
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

(def +go-core+
  (add-sym
   {:x-print          {:macro #'go-tf-x-print  :emit :macro :value true}
    :x-len            {:macro #'go-tf-x-len    :emit :macro :value true}
    :x-cat            {:macro #'go-tf-x-cat    :emit :macro :value true}}))

;;
;; MATH
;;

(defn go-tf-x-m-abs   [[_ n]] (list 'math.Abs n))
(defn go-tf-x-m-max   [[_ a b]] (list 'math.Max a b))
(defn go-tf-x-m-min   [[_ a b]] (list 'math.Min a b))
(defn go-tf-x-m-ceil  [[_ n]] (list 'math.Ceil n))
(defn go-tf-x-m-floor [[_ n]] (list 'math.Floor n))
(defn go-tf-x-m-sqrt  [[_ n]] (list 'math.Sqrt n))
(defn go-tf-x-m-pow   [[_ b e]] (list 'math.Pow b e))

(def +go-math+
  (add-sym
   {:x-m-abs           {:macro #'go-tf-x-m-abs   :emit :macro :value true}
    :x-m-max           {:macro #'go-tf-x-m-max   :emit :macro :value true}
    :x-m-min           {:macro #'go-tf-x-m-min   :emit :macro :value true}
    :x-m-ceil          {:macro #'go-tf-x-m-ceil  :emit :macro :value true}
    :x-m-floor         {:macro #'go-tf-x-m-floor :emit :macro :value true}
    :x-m-sqrt          {:macro #'go-tf-x-m-sqrt  :emit :macro :value true}
    :x-m-pow           {:macro #'go-tf-x-m-pow   :emit :macro :value true}}))

;;
;; STRING
;;

(defn go-tf-x-str-split [[_ s sep]] (list 'strings.Split s sep))
(defn go-tf-x-str-join  [[_ sep arr]] (list 'strings.Join arr sep))
(defn go-tf-x-str-index-of [[_ s substr]] (list 'strings.Index s substr))
(defn go-tf-x-str-to-upper [[_ s]] (list 'strings.ToUpper s))
(defn go-tf-x-str-to-lower [[_ s]] (list 'strings.ToLower s))
(defn go-tf-x-str-trim [[_ s]] (list 'strings.TrimSpace s))

(def +go-str+
  (add-sym
   {:x-str-split       {:macro #'go-tf-x-str-split      :emit :macro :value true}
    :x-str-join        {:macro #'go-tf-x-str-join       :emit :macro :value true}
    :x-str-index-of    {:macro #'go-tf-x-str-index-of   :emit :macro :value true}
    :x-str-to-upper    {:macro #'go-tf-x-str-to-upper   :emit :macro :value true}
    :x-str-to-lower    {:macro #'go-tf-x-str-to-lower   :emit :macro :value true}
    :x-str-trim        {:macro #'go-tf-x-str-trim       :emit :macro :value true}}))

;;
;; ARR
;;

(defn go-tf-x-arr-push
  [[_ arr item]]
  (list := arr (list 'append arr item)))

(def +go-arr+
  (add-sym
   {:x-arr-push        {:macro #'go-tf-x-arr-push       :emit :macro}}))

(def +go+
  (merge +go-core+
         +go-math+
         +go-str+
         +go-arr+))
