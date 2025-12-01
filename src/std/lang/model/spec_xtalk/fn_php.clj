(ns std.lang.model.spec-xtalk.fn-php
  (:require [std.lib :as h]))

;;
;; CORE
;;

(defn php-tf-x-len
  [[_ arr]]
  (list 'count arr))

(defn php-tf-x-cat
  [[_ & args]]
  (apply list '. args))

(defn php-tf-x-apply
  [[_ f args]]
  (list 'call_user_func_array f args))

(defn php-tf-x-shell
  ([[_ s opts]]
   (list 'shell_exec s)))

(defn php-tf-x-random
  [_]
  (list '/ (list 'rand 0 (list 'getrandmax)) (list 'getrandmax)))

(defn php-tf-x-type-native
  [[_ obj]]
  (list 'gettype obj))

(defn php-tf-x-err
  [[_ msg]]
  (list 'throw (list 'new 'Exception msg)))

(defn php-tf-x-eval
  [[_ s]]
  (list 'eval s))

(defn php-tf-x-print
  ([[_ & args]]
   (apply list 'var_dump args)))

(defn php-tf-x-now-ms
  [_]
  (list '* 1000 (list 'microtime true)))

(def +php-core+
  {:x-del            {:emit :alias :raw 'unset}
   :x-cat            {:macro #'php-tf-x-cat  :emit :macro}
   :x-len            {:macro #'php-tf-x-len  :emit :macro}
   :x-err            {:macro #'php-tf-x-err  :emit :macro}
   :x-eval           {:macro #'php-tf-x-eval :emit :macro}
   :x-apply          {:macro #'php-tf-x-apply :emit :macro}
   :x-unpack         {:emit :alias :raw :...}
   :x-print          {:macro #'php-tf-x-print :emit :macro :value true}
   :x-random         {:macro #'php-tf-x-random :emit :macro :value true}
   :x-shell          {:macro #'php-tf-x-shell :emit :macro}
   :x-now-ms         {:macro #'php-tf-x-now-ms :emit :macro}
   :x-type-native    {:macro #'php-tf-x-type-native :emit :macro}})

;;
;; MATH
;;

(defn php-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn php-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn php-tf-x-m-mod   [[_ num denom]] (list :% num (list :- " % ") denom))
(defn php-tf-x-m-quot  [[_ num denom]] (list 'floor (list '/ num denom)))

(def +php-math+
  {:x-m-abs           {:emit :alias :raw 'abs  :value true}
   :x-m-acos          {:emit :alias :raw 'acos :value true}
   :x-m-asin          {:emit :alias :raw 'asin :value true}
   :x-m-atan          {:emit :alias :raw 'atan :value true}
   :x-m-ceil          {:emit :alias :raw 'ceil :value true}
   :x-m-cos           {:emit :alias :raw 'cos  :value true}
   :x-m-cosh          {:emit :alias :raw 'cosh :value true}
   :x-m-exp           {:emit :alias :raw 'exp  :value true}
   :x-m-floor         {:emit :alias :raw 'floor :value true}
   :x-m-loge          {:emit :alias :raw 'log  :value true}
   :x-m-log10         {:emit :alias :raw 'log10 :value true}
   :x-m-max           {:macro #'php-tf-x-m-max,      :raw 'max :emit :macro :value true}
   :x-m-min           {:macro #'php-tf-x-m-min,      :raw 'min :emit :macro :value true}
   :x-m-mod           {:macro #'php-tf-x-m-mod,      :emit :macro}
   :x-m-pow           {:emit :alias :raw 'pow  :value true}
   :x-m-quot          {:macro #'php-tf-x-m-quot,     :emit :macro}
   :x-m-sin           {:emit :alias :raw 'sin  :value true}
   :x-m-sinh          {:emit :alias :raw 'sinh :value true}
   :x-m-sqrt          {:emit :alias :raw 'sqrt :value true}
   :x-m-tan           {:emit :alias :raw 'tan  :value true}
   :x-m-tanh          {:emit :alias :raw 'tanh :value true}})

;;
;; TYPE
;;

(defn php-tf-x-is-string?
  [[_ e]]
  (list 'is_string e))

(defn php-tf-x-is-number?
  [[_ e]]
  (list 'or (list 'is_int e) (list 'is_float e)))

(defn php-tf-x-is-integer?
  [[_ e]]
  (list 'is_int e))

(defn php-tf-x-is-boolean?
  [[_ e]]
  (list 'is_bool e))

(defn php-tf-x-is-object?
  [[_ e]]
  (list 'is_object e))

(defn php-tf-x-is-array?
  [[_ e]]
  (list 'is_array e))

(def +php-type+
  {:x-to-string      {:emit :alias :raw '(string)}
   :x-to-number      {:emit :alias :raw '(float)}
   :x-is-string?     {:macro #'php-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'php-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'php-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'php-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:emit :alias :raw 'is_callable}
   :x-is-object?     {:macro #'php-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'php-tf-x-is-array? :emit :macro}})

;;
;; ARR
;;

(defn php-tf-x-arr-push
  [[_ arr item]]
  (list 'array_push arr item))

(defn php-tf-x-arr-pop
  [[_ arr]]
  (list 'array_pop arr))

(defn php-tf-x-arr-push-first
  [[_ arr item]]
  (list 'array_unshift arr item))

(defn php-tf-x-arr-pop-first
  [[_ arr]]
  (list 'array_shift arr))

(defn php-tf-x-arr-slice
  [[_ arr start end]]
  (list 'array_slice arr start (if end (list '- end start) nil)))

(def +php-arr+
  {:x-arr-push        {:macro #'php-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'php-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'php-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'php-tf-x-arr-pop-first  :emit :macro}
   :x-arr-slice       {:macro #'php-tf-x-arr-slice      :emit :macro}})

;;
;; STRING
;;

(defn php-tf-x-str-char
  ([[_ s i]]
   (list 'ord (list 'substr s i 1))))

(defn php-tf-x-str-split
  ([[_ s tok]]
   (list 'explode tok s)))

(defn php-tf-x-str-join
  ([[_ s arr]]
   (list 'implode s arr)))

(defn php-tf-x-str-index-of
  ([[_ s tok]]
   (list 'strpos s tok)))

(defn php-tf-x-str-substring
  ([[_ s start & args]]
   (list 'substr s start (first args))))

(defn php-tf-x-str-to-upper
  ([[_ s]]
   (list 'strtoupper s)))

(defn php-tf-x-str-to-lower
  ([[_ s]]
   (list 'strtolower s)))

(defn php-tf-x-str-replace
  ([[_ s tok replacement]]
   (list 'str_replace tok replacement s)))

(defn php-tf-x-str-trim
  ([[_ s]]
   (list 'trim s)))

(def +php-str+
  {:x-str-char        {:macro #'php-tf-x-str-char       :emit :macro}
   :x-str-split       {:macro #'php-tf-x-str-split      :emit :macro}
   :x-str-join        {:macro #'php-tf-x-str-join       :emit :macro}
   :x-str-index-of    {:macro #'php-tf-x-str-index-of   :emit :macro}
   :x-str-substring   {:macro #'php-tf-x-str-substring  :emit :macro}
   :x-str-to-upper    {:macro #'php-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower    {:macro #'php-tf-x-str-to-lower   :emit :macro}
   :x-str-replace     {:macro #'php-tf-x-str-replace    :emit :macro}
   :x-str-trim        {:macro #'php-tf-x-str-trim       :emit :macro}})

;;
;; JSON
;;

(def +php-js+
  {:x-json-encode      {:emit :alias :raw 'json_encode}
   :x-json-decode      {:emit :alias :raw 'json_decode}})

;;
;; RETURN
;;

(defn php-tf-x-return-encode
  ([[_ out id key]]
   (h/$ (do (try
              (return (json_encode {:id  ~id
                                    :key ~key
                                    :type  "data"
                                    :value  ~out}))
              (catch Exception $e
                (return (json_encode {:id ~id
                                      :key ~key
                                      :type  "raw"
                                      :value (. "" ~out)}))))))))

(defn php-tf-x-return-wrap
  ([[_ f encode-fn]]
   (h/$ (do (try
              (:= out (~f))
              (catch Exception $e
                (return (json_encode {:type "error"
                                      :value (. "" $e)}))))
            (return (~encode-fn out nil nil))))))

(defn php-tf-x-return-eval
  ([[_ s wrap-fn]]
   (h/$ (return (~wrap-fn (function []
                            (return (eval ~s))))))))

(def +php-return+
  {:x-return-encode  {:macro #'php-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'php-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'php-tf-x-return-eval     :emit :macro}})

(def +php+
  (merge +php-core+
         +php-math+
         +php-type+
         +php-arr+
         +php-str+
         +php-js+
         +php-return+))
