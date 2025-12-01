(ns std.lang.model.spec-xtalk.fn-ruby
  (:require [std.lib :as h]))

;;
;; CORE
;;

(defn ruby-tf-x-len
  [[_ arr]]
  (list '. arr 'length))

(defn ruby-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn ruby-tf-x-print
  [[_ & args]]
  (apply list 'puts args))

(defn ruby-tf-x-random
  [_]
  '(rand))

(defn ruby-tf-x-now-ms
  [_]
  (list '. (list '* (list '. 'Time.now 'to_f) 1000) 'to_i))

(def +ruby-core+
  {:x-cat            {:macro #'ruby-tf-x-cat  :emit :macro :value true
                      :raw "(lambda { |*args| args.join('') })"}
   :x-len            {:macro #'ruby-tf-x-len  :emit :macro}
   :x-err            {:emit :alias :raw 'raise}
   :x-eval           {:emit :alias :raw 'eval}
   :x-print          {:macro #'ruby-tf-x-print :emit :macro :value true}
   :x-random         {:emit :alias :raw 'rand :value true}
   :x-now-ms         {:macro #'ruby-tf-x-now-ms :emit :macro}})

;;
;; MATH
;;

(defn ruby-tf-x-m-mod   [[_ num denom]] (list '% num denom))

(def +ruby-math+
  {:x-m-abs           {:emit :alias :raw 'abs  :value true}
   :x-m-cos           {:emit :alias :raw 'Math.cos  :value true}
   :x-m-exp           {:emit :alias :raw 'Math.exp  :value true}
   :x-m-loge          {:emit :alias :raw 'Math.log  :value true}
   :x-m-sin           {:emit :alias :raw 'Math.sin  :value true}
   :x-m-sqrt          {:emit :alias :raw 'Math.sqrt :value true}
   :x-m-mod           {:macro #'ruby-tf-x-m-mod,      :emit :macro}})

;;
;; ARR
;;

(defn ruby-tf-x-arr-push
  [[_ arr item]]
  (list '. arr (list 'push item)))

(defn ruby-tf-x-arr-pop
  [[_ arr]]
  (list '. arr (list 'pop)))

(defn ruby-tf-x-arr-push-first
  [[_ arr item]]
  (list '. arr (list 'unshift item)))

(defn ruby-tf-x-arr-pop-first
  [[_ arr]]
  (list '. arr (list 'shift)))

(defn ruby-tf-x-arr-insert
  [[_ arr idx e]]
  (list '. arr (list 'insert idx e)))

(defn ruby-tf-x-arr-remove
  [[_ arr idx]]
  (list '. arr (list 'delete_at idx)))

(defn ruby-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (list '. arr (list 'sort!))) ;; simplified

(def +ruby-arr+
  {:x-arr-push        {:macro #'ruby-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'ruby-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'ruby-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'ruby-tf-x-arr-pop-first  :emit :macro}
   :x-arr-remove      {:macro #'ruby-tf-x-arr-remove     :emit :macro}
   :x-arr-insert      {:macro #'ruby-tf-x-arr-insert     :emit :macro}})

;;
;; STRING
;;

(defn ruby-tf-x-str-split
  ([[_ s tok]]
   (list '. s (list 'split tok))))

(defn ruby-tf-x-str-join
  ([[_ s arr]]
   (list '. arr (list 'join s))))

(defn ruby-tf-x-str-index-of
  ([[_ s tok]]
   (list '. s (list 'index tok))))

(defn ruby-tf-x-str-substring
  ([[_ s start & args]]
   (if (empty? args)
     (list '. s (list 'slice start))
     (list '. s (list 'slice start (first args))))))

(defn ruby-tf-x-str-to-upper
  ([[_ s]]
   (list '. s (list 'upcase))))

(defn ruby-tf-x-str-to-lower
  ([[_ s]]
   (list '. s (list 'downcase))))

(def +ruby-str+
  {:x-str-split       {:macro #'ruby-tf-x-str-split      :emit :macro}
   :x-str-join        {:macro #'ruby-tf-x-str-join       :emit :macro}
   :x-str-index-of    {:macro #'ruby-tf-x-str-index-of   :emit :macro}
   :x-str-substring   {:macro #'ruby-tf-x-str-substring  :emit :macro}
   :x-str-to-upper    {:macro #'ruby-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower    {:macro #'ruby-tf-x-str-to-lower   :emit :macro}})


(def +ruby+
  (merge +ruby-core+
         +ruby-math+
         +ruby-arr+
         +ruby-str+))
