(ns std.lang.model-annex.spec-xtalk.fn-php
  (:require [std.lib.foundation :as f]
            [std.lib.template :as template]))

;;
;; CORE
;;

(defn php-tf-x-len
  [[_ arr]]
  (list 'count arr))

(defn php-tf-x-cat
  [[_ & args]]
  (apply list 'concat args))

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
;; CUSTOM
;;

(defn php-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list 'and
          (list 'array_key_exists key obj)
          (list '== check (list :% obj [key])))
    (list 'array_key_exists key obj)))

(def +php-custom+
  {:x-has-key? {:macro #'php-tf-x-has-key? :emit :macro}})

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
;; LU
;;

(defn php-tf-x-lu-get
  [[_ lu obj]]
  (list :% lu [(list 'spl_object_id obj)]))

(defn php-tf-x-lu-set
  [[_ lu obj gid]]
  (list ':= (list :% lu [(list 'spl_object_id obj)]) gid))

(defn php-tf-x-lu-del
  [[_ lu obj]]
  (list 'unset (list :% lu [(list 'spl_object_id obj)])))

(def +php-lu+
  {:x-lu-get         {:macro #'php-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'php-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'php-tf-x-lu-del :emit :macro}})

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

(defn php-tf-x-arr-insert
  [[_ arr idx e]]
  (list 'array_splice arr idx 0 [e]))

(defn php-tf-x-arr-remove
  [[_ arr idx]]
  (list 'array_splice arr idx 1))

(defn php-tf-x-arr-sort
  [[_ arr & more]]
  (let [[f0 f1] more]
    (cond (nil? f0)
          (list 'sort arr)

          (nil? f1)
          (list 'usort arr f0)

          :else
          (list 'usort arr
                (template/$
                 (fn [a b]
                   (return (:? (~f1
                                (~f0 a)
                                (~f0 b))
                               -1 1))))))))

(defn php-tf-x-str-comp
  [[_ a b]]
  (list '< (list 'strcmp a b) 0))

(def +php-arr+
  {:x-arr-push        {:macro #'php-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'php-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'php-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'php-tf-x-arr-pop-first  :emit :macro}
   :x-arr-slice       {:macro #'php-tf-x-arr-slice      :emit :macro}
   :x-arr-insert      {:macro #'php-tf-x-arr-insert     :emit :macro}
   :x-arr-remove      {:macro #'php-tf-x-arr-remove     :emit :macro}
   :x-arr-sort        {:macro #'php-tf-x-arr-sort       :emit :macro}
   :x-str-comp        {:macro #'php-tf-x-str-comp       :emit :macro}})

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

(defn php-tf-x-str-format
  ([[_ fmt & args]]
   (apply list 'sprintf fmt args)))

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

(defn php-tf-x-str-to-fixed
  ([[_ s & [digits]]]
   (list 'number_format s (or digits 0) "." "")))

(defn php-tf-x-str-trim
  ([[_ s]]
   (list 'trim s)))

(defn php-tf-x-str-trim-left
  ([[_ s]]
   (list 'ltrim s)))

(defn php-tf-x-str-trim-right
  ([[_ s]]
   (list 'rtrim s)))

(def +php-str+
  {:x-str-char        {:macro #'php-tf-x-str-char       :emit :macro}
   :x-str-format      {:macro #'php-tf-x-str-format     :emit :macro}
   :x-str-split       {:macro #'php-tf-x-str-split      :emit :macro}
   :x-str-join        {:macro #'php-tf-x-str-join       :emit :macro}
   :x-str-index-of    {:macro #'php-tf-x-str-index-of   :emit :macro}
   :x-str-substring   {:macro #'php-tf-x-str-substring  :emit :macro}
   :x-str-to-upper    {:macro #'php-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower    {:macro #'php-tf-x-str-to-lower   :emit :macro}
   :x-str-to-fixed    {:macro #'php-tf-x-str-to-fixed   :emit :macro}
   :x-str-replace     {:macro #'php-tf-x-str-replace    :emit :macro}
   :x-str-trim        {:macro #'php-tf-x-str-trim       :emit :macro}
   :x-str-trim-left   {:macro #'php-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right  {:macro #'php-tf-x-str-trim-right :emit :macro}})

;;
;; JSON
;;

(def +php-json+
  {:x-json-encode      {:emit :alias :raw 'json_encode}
   :x-json-decode      {:emit :alias :raw 'json_decode}})

;;
;; ITER
;;

(defn php-tf-x-iter-from-obj
  [[_ obj]]
  (template/$
   (array_map
    (fn [k]
      (return [k (:% ~obj [k])]))
    (array_keys ~obj))))

(defn php-tf-x-iter-from-arr
  [[_ arr]]
  arr)

(defn php-tf-x-iter-from
  [[_ obj]]
  obj)

(defn php-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (template/$
   (if (!= (count ~it0) (count ~it1))
     (return false)
     (do (for [i (range 0 (count ~it0))]
           (if (not (~eq-fn (:% ~it0 [i])
                            (:% ~it1 [i])))
             (return false)))
         (return true)))))

(defn php-tf-x-iter-next
  [[_ it]]
  (list 'array_shift it))

(defn php-tf-x-iter-has?
  [[_ obj]]
  (list 'and
        (list 'is_array obj)
        (list '> (list 'count obj) 0)))

(defn php-tf-x-iter-native?
  [[_ it]]
  (list 'is_array it))

(def +php-iter+
  {:x-iter-from-obj       {:macro #'php-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr       {:macro #'php-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from           {:macro #'php-tf-x-iter-from           :emit :macro}
   :x-iter-eq             {:macro #'php-tf-x-iter-eq             :emit :macro}
   :x-iter-null           {:default [] :emit :unit}
   :x-iter-next           {:macro #'php-tf-x-iter-next           :emit :macro}
   :x-iter-has?           {:macro #'php-tf-x-iter-has?           :emit :macro}
   :x-iter-native?        {:macro #'php-tf-x-iter-native?        :emit :macro}})

;;
;; SOCKET
;;

(defn php-tf-x-socket-connect
  [[_ host & [port opts]]]
  (if (some? port)
    (list 'fsockopen host port)
    (list 'fsockopen host)))

(defn php-tf-x-socket-send
  [[_ conn value]]
  (list 'fwrite conn value))

(defn php-tf-x-socket-close
  [[_ conn]]
  (list 'fclose conn))

(def +php-socket+
  {:x-socket-connect      {:macro #'php-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'php-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'php-tf-x-socket-close        :emit :macro}})

;;
;; THREAD
;;

(defn php-tf-x-with-delay
  [[_ ms thunk]]
  (template/$
   (do (usleep (* ~ms 1000))
       (return (~thunk)))))

(def +php-thread+
  {:x-with-delay     {:macro #'php-tf-x-with-delay     :emit :macro}})

;;
;; FILE
;;

(defn php-tf-x-file-slurp
  [[_ path opts cb]]
  (list 'call_user_func_array cb [nil (list 'file_get_contents path)]))

(defn php-tf-x-file-spit
  [[_ path content opts cb]]
  (list 'do
        (list 'file_put_contents path content)
        (list 'call_user_func_array cb [nil path])))

(def +php-file+
  {:x-file-slurp     {:macro #'php-tf-x-file-slurp       :emit :macro}
   :x-file-spit      {:macro #'php-tf-x-file-spit        :emit :macro}})

;;
;; RETURN
;;

(defn php-tf-x-return-encode
  ([[_ out id key]]
   (template/$ (do (try
                     (return (json_encode {:id  ~id
                                           :key ~key
                                           :type  "data"
                                           :value  ~out}))
                     (catch Exception $e
                       (return (json_encode {:id ~id
                                             :key ~key
                                             :type  "raw"
                                             :value (concat "" ~out)}))))))))

(defn php-tf-x-return-wrap
  ([[_ f encode-fn]]
   (template/$ (do (try
                    (:= out (call_user_func_array ~f []))
                    (catch Exception $e
                      (return (json_encode {:type "error"
                                            :value (concat "" $e)}))))
                    (return (~encode-fn out nil nil))))))

(defn php-tf-x-return-eval
  ([[_ s wrap-fn]]
   (template/$ (return (~wrap-fn
                        (:- "function () use ($s) {\n  return eval($s);\n}"))))))

(def +php-return+
  {:x-return-encode  {:macro #'php-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'php-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'php-tf-x-return-eval     :emit :macro}})

(def +php+
  (merge +php-core+
         +php-custom+
         +php-math+
         +php-type+
         +php-lu+
         +php-arr+
         +php-str+
         +php-json+
         +php-return+
         +php-iter+
         +php-socket+
         +php-thread+
         +php-file+))
