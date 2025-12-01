(ns std.lang.model.spec-xtalk.fn-perl
  (:require [std.lib :as h]))

;;
;; CORE
;;

(defn perl-tf-x-len
  [[_ arr]]
  (list 'scalar arr))

(defn perl-tf-x-cat
  [[_ & args]]
  (apply list 'concat args))

(defn perl-tf-x-print
  [[_ & args]]
  (apply list 'print (concat args ["\n"])))

(def +perl-core+
  {:x-len            {:macro #'perl-tf-x-len  :emit :macro :symbol #{'x:len}}
   :x-cat            {:macro #'perl-tf-x-cat  :emit :macro :value true :symbol #{'x:cat}}
   :x-print          {:macro #'perl-tf-x-print :emit :macro :symbol #{'x:print}}})

;;
;; MATH
;;

(defn perl-tf-x-m-abs   [[_ n]] (list 'abs n))
(defn perl-tf-x-m-cos   [[_ n]] (list 'cos n))
(defn perl-tf-x-m-sin   [[_ n]] (list 'sin n))
(defn perl-tf-x-m-sqrt  [[_ n]] (list 'sqrt n))
(defn perl-tf-x-m-log   [[_ n]] (list 'log n))
(defn perl-tf-x-m-exp   [[_ n]] (list 'exp n))
(defn perl-tf-x-m-pow   [[_ b p]] (list '** b p))

(def +perl-math+
  {:x-m-abs           {:macro #'perl-tf-x-m-abs :emit :macro :value true :symbol #{'x:m-abs}}
   :x-m-cos           {:macro #'perl-tf-x-m-cos :emit :macro :value true :symbol #{'x:m-cos}}
   :x-m-sin           {:macro #'perl-tf-x-m-sin :emit :macro :value true :symbol #{'x:m-sin}}
   :x-m-sqrt          {:macro #'perl-tf-x-m-sqrt :emit :macro :value true :symbol #{'x:m-sqrt}}
   :x-m-loge          {:macro #'perl-tf-x-m-log :emit :macro :value true :symbol #{'x:m-loge}}
   :x-m-exp           {:macro #'perl-tf-x-m-exp :emit :macro :value true :symbol #{'x:m-exp}}
   :x-m-pow           {:macro #'perl-tf-x-m-pow :emit :macro :value true :symbol #{'x:m-pow}}})

;;
;; ARR
;;

(defn perl-tf-x-arr-push
  [[_ arr item]]
  (list 'push arr item))

(defn perl-tf-x-arr-pop
  [[_ arr]]
  (list 'pop arr))

(defn perl-tf-x-arr-push-first
  [[_ arr item]]
  (list 'unshift arr item))

(defn perl-tf-x-arr-pop-first
  [[_ arr]]
  (list 'shift arr))

(def +perl-arr+
  {:x-arr-push        {:macro #'perl-tf-x-arr-push       :emit :macro :symbol #{'x:arr-push}}
   :x-arr-pop         {:macro #'perl-tf-x-arr-pop        :emit :macro :symbol #{'x:arr-pop}}
   :x-arr-push-first  {:macro #'perl-tf-x-arr-push-first :emit :macro :symbol #{'x:arr-push-first}}
   :x-arr-pop-first   {:macro #'perl-tf-x-arr-pop-first  :emit :macro :symbol #{'x:arr-pop-first}}})

;;
;; STRING
;;

(defn perl-tf-x-str-split
  ([[_ s tok]]
   (list 'split tok s)))

(defn perl-tf-x-str-join
  ([[_ s arr]]
   (list 'join s arr)))

(defn perl-tf-x-str-index-of
  ([[_ s tok]]
   (list 'index s tok)))

(defn perl-tf-x-str-substring
  ([[_ s start & [len]]]
   (if len
     (list 'substr s start len)
     (list 'substr s start))))

(defn perl-tf-x-str-to-upper
  ([[_ s]]
   (list 'uc s)))

(defn perl-tf-x-str-to-lower
  ([[_ s]]
   (list 'lc s)))

(def +perl-str+
  {:x-str-split       {:macro #'perl-tf-x-str-split      :emit :macro :symbol #{'x:str-split}}
   :x-str-join        {:macro #'perl-tf-x-str-join       :emit :macro :symbol #{'x:str-join}}
   :x-str-index-of    {:macro #'perl-tf-x-str-index-of   :emit :macro :symbol #{'x:str-index-of}}
   :x-str-substring   {:macro #'perl-tf-x-str-substring  :emit :macro :symbol #{'x:str-substring}}
   :x-str-to-upper    {:macro #'perl-tf-x-str-to-upper   :emit :macro :symbol #{'x:str-to-upper}}
   :x-str-to-lower    {:macro #'perl-tf-x-str-to-lower   :emit :macro :symbol #{'x:str-to-lower}}})

(def +perl+
  (merge +perl-core+
         +perl-math+
         +perl-arr+
         +perl-str+))
