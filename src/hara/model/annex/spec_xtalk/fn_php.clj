(ns hara.model.annex.spec-xtalk.fn-php
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
(defn php-tf-x-construct
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
   :x-construct      {:macro #'php-tf-x-construct :emit :macro}
   :x-apply          {:macro #'php-tf-x-apply :emit :macro}
   :x-unpack         {:emit :alias :raw :...}
   :x-print          {:macro #'php-tf-x-print :emit :macro :value true}
   :x-random         {:macro #'php-tf-x-random :emit :macro :value true}
   :x-shell          {:macro #'php-tf-x-shell :emit :macro}
   :x-now-ms         {:macro #'php-tf-x-now-ms :emit :macro}
   :x-type-native    {:macro #'php-tf-x-type-native :emit :macro}})

;;
;; EXCEPTIONS
;;

(defn php-tf-x-ex-new
  "creates a PHP Exception with optional data payload"
  {:added "4.1"}
  [[_ message & [data]]]
  (if (some? data)
    (template/$
     (do (var e := (new Exception ~message))
         (:= e->data ~data)
         (return e)))
    (list 'new 'Exception message)))

(defn php-tf-x-ex-message
  "gets the exception message"
  {:added "4.1"}
  [[_ err]]
  (list '. err 'getMessage))

(defn php-tf-x-ex-data
  "gets the exception data payload"
  {:added "4.1"}
  [[_ err]]
  (list '. err ["data"]))

(defn php-tf-x-ex-native?
  "checks whether value is a native PHP exception"
  {:added "4.1"}
  [[_ err]]
  (list 'instanceof err 'Throwable))

(def +php-ex+
  {:x-ex             {:macro #'php-tf-x-ex-new    :emit :macro}
   :x-ex-new         {:macro #'php-tf-x-ex-new    :emit :macro}
   :x-ex-message     {:macro #'php-tf-x-ex-message :emit :macro}
   :x-ex-data        {:macro #'php-tf-x-ex-data    :emit :macro}
   :x-ex-native?     {:macro #'php-tf-x-ex-native? :emit :macro}})

;;
;; ASYNC
;;

(defn php-tf-x-async-run
  "PHP is synchronous; run the thunk immediately"
  {:added "4.1"}
  [[_ thunk]]
  (list 'call_user_func_array thunk []))

(def +php-async+
  {:x-async-run     {:macro #'php-tf-x-async-run :emit :macro}})

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

(defn php-tf-x-m-pow [[_ base n]] (list 'call_user_func_array "pow" [base n]))

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
   :x-m-pow           {:macro #'php-tf-x-m-pow :emit :macro :value true}
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
  (list 'and (list 'is_array e)
        (list 'not (list 'array_is_list e))))

(defn php-tf-x-is-array?
  [[_ e]]
  (list 'and (list 'is_array e)
        (list 'array_is_list e)))

(def +php-type+
  {:x-to-string      {:emit :alias :raw 'strval}
   :x-to-number      {:emit :alias :raw 'floatval}
   :x-is-string?     {:macro #'php-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'php-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'php-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'php-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:emit :alias :raw 'is_callable}
   :x-is-object?     {:macro #'php-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'php-tf-x-is-array? :emit :macro}})

;;
;; OBJ
;;

(defn php-tf-x-obj-keys
  [[_ obj]]
  (list 'array_keys obj))

(defn php-tf-x-obj-vals
  [[_ obj]]
  (list 'array_values obj))

(defn php-tf-x-obj-pairs
  [[_ obj]]
  (template/$
   ((fn []
      (var $out := [])
      (for:object [[$k $v] ~obj]
        (x:arr-push $out [$k $v]))
      (return $out)))))

(defn php-tf-x-obj-clone
  [[_ obj]]
  (list 'array_merge [] obj))

(defn php-tf-x-obj-assign
  [[_ obj m]]
  (list 'array_merge obj m))

(def +php-obj+
  {:x-obj-keys      {:macro #'php-tf-x-obj-keys    :emit :macro}
   :x-obj-vals      {:macro #'php-tf-x-obj-vals    :emit :macro}
   :x-obj-pairs     {:macro #'php-tf-x-obj-pairs   :emit :macro}
   :x-obj-clone     {:macro #'php-tf-x-obj-clone   :emit :macro}
   :x-obj-assign    {:macro #'php-tf-x-obj-assign  :emit :macro}})

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

(defn php-tf-x-lu-eq
  [[_ o1 o2]]
  (list '=== o1 o2))

(def +php-lu+
  {:x-lu-create      {:emit :unit :default []}
   :x-lu-eq          {:macro #'php-tf-x-lu-eq :emit :macro}
   :x-lu-get         {:macro #'php-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'php-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'php-tf-x-lu-del :emit :macro}})

;;
;; FOR
;;

(defn php-tf-for-array
  "custom for:array code"
  {:added "4.1"}
  [[_ [lhs rhs] & body]]
  (if (vector? lhs)
    (let [[i v] lhs]
      (cond
        (= i '_)
        (apply list 'foreach [(list 'array_values rhs) v] body)

        (= v '_)
        (apply list 'foreach [(list 'array_keys rhs) i] body)

        :else
        (template/$
         (foreach [(array_keys ~rhs) ~i]
           (var ~v := (:% ~rhs [~i]))
           ~@body))))
    ;; A collection literal cannot be placed directly in the grammar's
    ;; foreach parameter slot: it is interpreted as additional parameters.
    ;; array_values also gives for:array the intended dense value iteration.
    (apply list 'foreach [(list 'array_values rhs) lhs] body)))

(defn php-tf-for-index
  "lowers an exclusive numeric range to PHP's inclusive range helper"
  {:added "4.1"}
  [[_ [i [start end step]] & body]]
  (let [step (or step 1)]
    (apply list 'foreach
           [(list 'range start (list '- end step) step) i]
           body)))

(defn php-tf-for-object
  "custom for:object code"
  {:added "4.1"}
  [[_ [lhs rhs] & body]]
  (if (vector? lhs)
    (let [[k v] lhs]
      (cond
        (= k '_)
        (apply list 'foreach [(list 'array_values rhs) v] body)

        (= v '_)
        (apply list 'foreach [(list 'array_keys rhs) k] body)

        :else
        (template/$
         (foreach [(array_keys ~rhs) ~k]
           (var ~v := (:% ~rhs [~k]))
           ~@body))))
    (apply list 'foreach [(list 'array_values rhs) lhs] body)))

(defn php-tf-for-iter
  "custom for:iter code"
  {:added "4.1"}
  [[_ [e it] & body]]
  (apply list 'foreach [it e] body))

(def +php-for+
  {:for-index  {:macro #'php-tf-for-index :emit :macro}
   :for-array  {:macro #'php-tf-for-array :emit :macro}
   :for-object {:macro #'php-tf-for-object :emit :macro}
   :for-iter   {:macro #'php-tf-for-iter :emit :macro}})

;;
;; ARR
;;

(defn php-tf-x-arr-push
  [[_ arr item]]
  (list 'array_push arr item))

(defn php-tf-x-arr-assign
  [[_ arr other]]
  (template/$
   (do (for:array [e ~other]
         (x:arr-push ~arr e))
       (return ~arr))))

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

(defn php-tf-x-arr-clone
  [[_ arr]]
  (list 'array_merge [] arr))

(defn php-tf-x-arr-each
  [[_ arr f]]
  (list 'array_walk arr f))

(defn php-tf-x-arr-every
  [[_ arr pred]]
  (template/$
   ((fn []
      (for:array [$e ~arr]
        (if (not (~pred $e))
          (return false)))
      (return true)))))

(defn php-tf-x-arr-some
  [[_ arr pred]]
  (template/$
   ((fn []
      (for:array [$e ~arr]
        (if (~pred $e)
          (return true)))
      (return false)))))

(defn php-tf-x-arr-map
  [[_ arr f]]
  (list 'array_values (list 'array_map f arr)))

(defn php-tf-x-arr-filter
  [[_ arr pred]]
  (list 'array_values (list 'array_filter arr pred)))

(defn php-tf-x-arr-foldl
  [[_ arr f init]]
  (list 'array_reduce arr f init))

(defn php-tf-x-arr-foldr
  [[_ arr f init]]
  (list 'array_reduce (list 'array_reverse arr) f init))

(defn php-tf-x-arr-find
  [[_ arr pred]]
  (template/$
   ((fn []
      (for:array [$i (array_keys ~arr)]
        (if (~pred (:% ~arr [$i]))
          (return $i)))
      (return -1)))))

(def +php-arr+
  {:x-arr-push        {:macro #'php-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'php-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'php-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'php-tf-x-arr-pop-first  :emit :macro}
   :x-arr-slice       {:macro #'php-tf-x-arr-slice      :emit :macro}
   :x-arr-insert      {:macro #'php-tf-x-arr-insert     :emit :macro}
   :x-arr-remove      {:macro #'php-tf-x-arr-remove     :emit :macro}
   :x-arr-sort        {:macro #'php-tf-x-arr-sort       :emit :macro}
   :x-arr-assign      {:macro #'php-tf-x-arr-assign     :emit :macro}
   :x-arr-clone       {:macro #'php-tf-x-arr-clone      :emit :macro}
   :x-arr-each        {:macro #'php-tf-x-arr-each       :emit :macro}
   :x-arr-every       {:macro #'php-tf-x-arr-every      :emit :macro}
   :x-arr-some        {:macro #'php-tf-x-arr-some       :emit :macro}
   :x-arr-map         {:macro #'php-tf-x-arr-map        :emit :macro}
   :x-arr-filter      {:macro #'php-tf-x-arr-filter     :emit :macro}
   :x-arr-foldl       {:macro #'php-tf-x-arr-foldl      :emit :macro}
   :x-arr-foldr       {:macro #'php-tf-x-arr-foldr      :emit :macro}
   :x-arr-find        {:macro #'php-tf-x-arr-find       :emit :macro}
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
   (if-let [end (first args)]
     (list 'substr s start (list '- end start))
     (list 'substr s start))))

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

(defn php-tf-x-str-len
  [[_ s]]
  (list 'strlen s))

(defn php-tf-x-str-pad-left
  [[_ s n pad]]
  (list 'str_pad s n pad 'STR_PAD_LEFT))

(defn php-tf-x-str-pad-right
  [[_ s n pad]]
  (list 'str_pad s n pad 'STR_PAD_RIGHT))

(defn php-tf-x-str-starts-with
  [[_ s prefix]]
  (list 'str_starts_with s prefix))

(defn php-tf-x-str-ends-with
  [[_ s suffix]]
  (list 'str_ends_with s suffix))


(def +php-str+
  {:x-str-len         {:macro #'php-tf-x-str-len         :emit :macro}
   :x-str-pad-left    {:macro #'php-tf-x-str-pad-left    :emit :macro}
   :x-str-pad-right   {:macro #'php-tf-x-str-pad-right   :emit :macro}
   :x-str-starts-with {:macro #'php-tf-x-str-starts-with :emit :macro}
   :x-str-ends-with   {:macro #'php-tf-x-str-ends-with   :emit :macro}
   :x-str-char        {:macro #'php-tf-x-str-char       :emit :macro}
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
  (list 'new 'ArrayIterator arr))

(defn php-tf-x-iter-from
  [[_ obj]]
  (list 'new 'ArrayIterator obj))

(defn php-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (template/$
   (do (var $i0 := ~it0)
        (var $i1 := ~it1)
        (while (and (. $i0 (valid))
                    (. $i1 (valid)))
          (if (not (~eq-fn (. $i0 (current))
                           (. $i1 (current))))
            (return false))
          (. $i0 (next))
          (. $i1 (next)))
        (return (and (not (. $i0 (valid)))
                     (not (. $i1 (valid))))))))

(defn php-tf-x-iter-next
  [[_ it]]
  (template/$
   (do (var $res := (. ~it (current)))
       (. ~it (next))
       (return $res))))

(defn php-tf-x-iter-has?
  [[_ obj]]
  (list 'array_is_list obj))

(defn php-tf-x-iter-native?
  [[_ it]]
  (list 'instanceof it 'ArrayIterator))

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
  {})

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
         +php-ex+
         +php-async+
         +php-math+
         +php-type+
         +php-lu+
         +php-obj+
         +php-for+
         +php-arr+
         +php-str+
         +php-json+
         +php-return+
         +php-iter+
         +php-socket+
         +php-thread+
         +php-file+))
