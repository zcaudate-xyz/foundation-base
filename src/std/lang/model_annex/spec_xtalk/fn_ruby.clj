(ns std.lang.model-annex.spec-xtalk.fn-ruby
  (:require [std.lib.template :as template]))

(defn ruby-raw
  [& parts]
  (apply list ':- parts))

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

(defn ruby-tf-x-del
  [[_ var]]
  (if (and (seq? var)
           (= '. (first var))
           (= 3 (count var))
           (vector? (nth var 2))
           (= 1 (count (nth var 2))))
    (let [obj (second var)
          k   (first (nth var 2))]
      (ruby-raw "(" (list '% obj) ").delete(" (list '% k) ")"))
    (list ':= var nil)))

(defn ruby-tf-x-apply
  [[_ f args]]
  (list '. f (list 'call (list :% (list ':- "*") args))))

(defn ruby-tf-x-shell
  [[_ s opts]]
  (list (symbol "`") s))

(defn ruby-tf-x-type-native
  [[_ obj]]
  (ruby-raw "(" (list '% obj) ").is_a?(Array) ? \"array\" : ("
            (list '% obj) ").is_a?(Hash) ? \"object\" : ("
            (list '% obj) ").class.name.downcase"))

(defn ruby-tf-x-unpack
  [[_ arr]]
  (list :% (list ':- "*") arr))

(def +ruby-core+
  {:x-cat            {:macro #'ruby-tf-x-cat  :emit :macro :value true}
   :x-len            {:macro #'ruby-tf-x-len  :emit :macro}
   :x-err            {:emit :alias :raw 'raise}
   :x-eval           {:emit :alias :raw 'eval}
   :x-print          {:macro #'ruby-tf-x-print :emit :macro :value true}
   :x-random         {:emit :alias :raw 'rand :value true}
   :x-now-ms         {:macro #'ruby-tf-x-now-ms :emit :macro}
   :x-del            {:macro #'ruby-tf-x-del   :emit :macro}
   :x-apply          {:macro #'ruby-tf-x-apply :emit :macro}
   :x-shell          {:macro #'ruby-tf-x-shell :emit :macro}
   :x-type-native    {:macro #'ruby-tf-x-type-native :emit :macro}
   :x-unpack         {:macro #'ruby-tf-x-unpack :emit :macro}})

;;
;; MATH
;;

(defn ruby-tf-x-m-abs   [[_ num]] (ruby-raw "(" (list '% num) ").abs"))
(defn ruby-tf-x-m-mod   [[_ num denom]] (ruby-raw "(" (list '% num) " % " (list '% denom) ")"))

(defn ruby-tf-x-m-max   [[_ & args]] (list '. (vec args) 'max))
(defn ruby-tf-x-m-min   [[_ & args]] (list '. (vec args) 'min))
(defn ruby-tf-x-m-pow   [[_ base exp]] (ruby-raw "(" (list '% base) " ** " (list '% exp) ")"))
(defn ruby-tf-x-m-quot  [[_ num denom]] (list '. num (list 'div denom)))
(defn ruby-tf-x-m-floor [[_ num]] (list '. num 'floor))
(defn ruby-tf-x-m-ceil  [[_ num]] (list '. num 'ceil))
(defn ruby-tf-x-m-acos  [[_ num]] (list 'Math.acos num))
(defn ruby-tf-x-m-asin  [[_ num]] (list 'Math.asin num))
(defn ruby-tf-x-m-atan  [[_ num]] (list 'Math.atan num))
(defn ruby-tf-x-m-cosh  [[_ num]] (list 'Math.cosh num))
(defn ruby-tf-x-m-sinh  [[_ num]] (list 'Math.sinh num))
(defn ruby-tf-x-m-tan   [[_ num]] (list 'Math.tan num))
(defn ruby-tf-x-m-tanh  [[_ num]] (list 'Math.tanh num))
(defn ruby-tf-x-m-log10 [[_ num]] (list 'Math.log10 num))

(def +ruby-math+
  {:x-m-abs           {:macro #'ruby-tf-x-m-abs      :emit :macro}
   :x-m-cos           {:emit :alias :raw 'Math.cos  :value true}
   :x-m-exp           {:emit :alias :raw 'Math.exp  :value true}
   :x-m-loge          {:emit :alias :raw 'Math.log  :value true}
   :x-m-sin           {:emit :alias :raw 'Math.sin  :value true}
   :x-m-sqrt          {:emit :alias :raw 'Math.sqrt :value true}
   :x-m-mod           {:macro #'ruby-tf-x-m-mod,      :emit :macro}
   :x-m-max           {:macro #'ruby-tf-x-m-max,      :emit :macro}
   :x-m-min           {:macro #'ruby-tf-x-m-min,      :emit :macro}
   :x-m-pow           {:macro #'ruby-tf-x-m-pow,      :emit :macro}
   :x-m-quot          {:macro #'ruby-tf-x-m-quot,     :emit :macro}
   :x-m-floor         {:macro #'ruby-tf-x-m-floor,    :emit :macro}
   :x-m-ceil          {:macro #'ruby-tf-x-m-ceil,     :emit :macro}
   :x-m-acos          {:macro #'ruby-tf-x-m-acos,     :emit :macro}
   :x-m-asin          {:macro #'ruby-tf-x-m-asin,     :emit :macro}
   :x-m-atan          {:macro #'ruby-tf-x-m-atan,     :emit :macro}
   :x-m-cosh          {:macro #'ruby-tf-x-m-cosh,     :emit :macro}
   :x-m-sinh          {:macro #'ruby-tf-x-m-sinh,     :emit :macro}
   :x-m-tan           {:macro #'ruby-tf-x-m-tan,      :emit :macro}
   :x-m-tanh          {:macro #'ruby-tf-x-m-tanh,     :emit :macro}
   :x-m-log10         {:macro #'ruby-tf-x-m-log10,    :emit :macro}})

;;
;; TYPE
;;

(defn ruby-tf-x-is-string?
  [[_ e]]
  (ruby-raw "(" (list '% e) ").is_a?(String)"))

(defn ruby-tf-x-is-number?
  [[_ e]]
  (ruby-raw "(" (list '% e) ").is_a?(Numeric)"))

(defn ruby-tf-x-is-integer?
  [[_ e]]
  (ruby-raw "(" (list '% e) ").is_a?(Integer)"))

(defn ruby-tf-x-is-boolean?
  [[_ e]]
  (list 'or (list '== e 'true) (list '== e 'false)))

(defn ruby-tf-x-is-object?
  [[_ e]]
  (ruby-raw "(" (list '% e) ").is_a?(Hash)"))

(defn ruby-tf-x-is-array?
  [[_ e]]
  (ruby-raw "(" (list '% e) ").is_a?(Array)"))

(defn ruby-tf-x-is-function?
  [[_ e]]
  (ruby-raw "(" (list '% e) ").respond_to?(:call)"))

(defn ruby-tf-x-to-string
  [[_ e]]
  (list '. e 'to_s))

(defn ruby-tf-x-to-number
  [[_ e]]
  (list '. e 'to_f))

(def +ruby-type+
  {:x-is-string?      {:macro #'ruby-tf-x-is-string?    :emit :macro}
   :x-is-number?      {:macro #'ruby-tf-x-is-number?    :emit :macro}
   :x-is-integer?     {:macro #'ruby-tf-x-is-integer?   :emit :macro}
   :x-is-boolean?     {:macro #'ruby-tf-x-is-boolean?   :emit :macro}
   :x-is-object?      {:macro #'ruby-tf-x-is-object?    :emit :macro}
   :x-is-array?       {:macro #'ruby-tf-x-is-array?     :emit :macro}
   :x-is-function?    {:macro #'ruby-tf-x-is-function?  :emit :macro}
   :x-to-string       {:macro #'ruby-tf-x-to-string     :emit :macro}
   :x-to-number       {:macro #'ruby-tf-x-to-number     :emit :macro}})

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
  (let [tmp   (gensym "tmp__")
        total (gensym "total__")
        i     (gensym "i__")
        j     (gensym "j__")
        left  (gensym "left__")
        right (gensym "right__")]
    (template/$
     (do (var ~total (. ~arr length))
         (for:index [~i [0 (- ~total 1)]]
           (for:index [~j [(+ ~i 1) ~total]]
             (var ~left (. ~arr [~i]))
             (var ~right (. ~arr [~j]))
             (when (. ~comp-fn
                      (call (. ~key-fn (call ~right))
                            (. ~key-fn (call ~left))))
               (var ~tmp ~left)
               (:= (. ~arr [~i]) ~right)
               (:= (. ~arr [~j]) ~tmp))))
         (return ~arr)))))

(defn ruby-tf-x-str-comp
  [[_ a b]]
  (list '< a b))

(def +ruby-arr+
  {:x-arr-push        {:macro #'ruby-tf-x-arr-push       :emit :macro}
   :x-arr-pop         {:macro #'ruby-tf-x-arr-pop        :emit :macro}
   :x-arr-push-first  {:macro #'ruby-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'ruby-tf-x-arr-pop-first  :emit :macro}
   :x-arr-remove      {:macro #'ruby-tf-x-arr-remove     :emit :macro}
   :x-arr-insert      {:macro #'ruby-tf-x-arr-insert     :emit :macro}
   :x-arr-sort        {:macro #'ruby-tf-x-arr-sort       :emit :macro}
   :x-str-comp        {:macro #'ruby-tf-x-str-comp       :emit :macro}})

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
      (ruby-raw "(" (list '% s) ")[(" (list '% start) ")..-1]")
      (let [end (first args)]
        (ruby-raw "(" (list '% s) ")[(" (list '% start) ")..." (list '% end) "]")))))

(defn ruby-tf-x-str-to-upper
  ([[_ s]]
   (list '. s (list 'upcase))))

(defn ruby-tf-x-str-to-lower
  ([[_ s]]
   (list '. s (list 'downcase))))

(defn ruby-tf-x-str-char
  ([[_ s i]]
   (ruby-raw "(" (list '% s) ")[" (list '% i) "].ord")))

(defn ruby-tf-x-str-replace
  ([[_ s tok replacement]]
   (list '. s (list 'gsub tok replacement))))

(defn ruby-tf-x-str-trim
  ([[_ s]]
   (list '. s 'strip)))

(defn ruby-tf-x-str-trim-left
  ([[_ s]]
   (list '. s 'lstrip)))

(defn ruby-tf-x-str-trim-right
  ([[_ s]]
   (list '. s 'rstrip)))

(defn ruby-tf-x-str-format
  ([[_ fmt & args]]
   (apply list 'sprintf fmt args)))

(defn ruby-tf-x-str-to-fixed
  ([[_ n digits]]
   (list 'sprintf (str "%." digits "f") n)))

(defn ruby-tf-x-str-starts-with
  ([[_ s prefix]]
   (ruby-raw "(" (list '% s) ").start_with?(" (list '% prefix) ")")))

(defn ruby-tf-x-str-ends-with
  ([[_ s suffix]]
   (ruby-raw "(" (list '% s) ").end_with?(" (list '% suffix) ")")))

(def +ruby-str+
  {:x-str-split       {:macro #'ruby-tf-x-str-split      :emit :macro}
   :x-str-join        {:macro #'ruby-tf-x-str-join       :emit :macro}
   :x-str-index-of    {:macro #'ruby-tf-x-str-index-of   :emit :macro}
   :x-str-substring   {:macro #'ruby-tf-x-str-substring  :emit :macro}
   :x-str-to-upper    {:macro #'ruby-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower    {:macro #'ruby-tf-x-str-to-lower   :emit :macro}
   :x-str-char        {:macro #'ruby-tf-x-str-char       :emit :macro}
   :x-str-replace     {:macro #'ruby-tf-x-str-replace    :emit :macro}
   :x-str-trim        {:macro #'ruby-tf-x-str-trim       :emit :macro}
   :x-str-trim-left   {:macro #'ruby-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right  {:macro #'ruby-tf-x-str-trim-right :emit :macro}
   :x-str-to-fixed    {:macro #'ruby-tf-x-str-to-fixed   :emit :macro}
   :x-str-starts-with {:macro #'ruby-tf-x-str-starts-with :emit :macro}
   :x-str-ends-with   {:macro #'ruby-tf-x-str-ends-with  :emit :macro}})

;;
;; LOOKUP
;;

(defn ruby-tf-x-lu-create
  [[_ & args]]
  (ruby-raw "{}.compare_by_identity"))

(defn ruby-tf-x-lu-eq
  [[_ a b]]
  (ruby-raw "(" (list '% a) ").equal?(" (list '% b) ")"))

(defn ruby-tf-x-lu-get
  [[_ h k default]]
  (if default
    (ruby-raw "(" (list '% h) ").key?(" (list '% k) ") ? ("
              (list '% h) ")[" (list '% k) "] : "
              (list '% default))
    (ruby-raw "(" (list '% h) ")[" (list '% k) "]")))

(defn ruby-tf-x-lu-set
  [[_ h k v]]
  (ruby-raw "(" (list '% h) ")[" (list '% k) "] = " (list '% v)))

(defn ruby-tf-x-lu-del
  [[_ h k]]
  (ruby-raw "(" (list '% h) ").delete(" (list '% k) ")"))

(defn ruby-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (ruby-raw "(" (list '% obj) ").key?(" (list '% key) ") && ("
              (list '% obj) ")[" (list '% key) "] == "
              (list '% check))
    (ruby-raw "(" (list '% obj) ").key?(" (list '% key) ")")))

(def +ruby-lu+
  {:x-lu-create        {:macro #'ruby-tf-x-lu-create      :emit :macro}
   :x-lu-eq            {:macro #'ruby-tf-x-lu-eq          :emit :macro}
   :x-lu-get           {:macro #'ruby-tf-x-lu-get         :emit :macro}
   :x-lu-set           {:macro #'ruby-tf-x-lu-set         :emit :macro}
   :x-lu-del           {:macro #'ruby-tf-x-lu-del         :emit :macro}
   :x-has-key?         {:macro #'ruby-tf-x-has-key?       :emit :macro}})

;;
;; JSON
;;

(defn ruby-tf-x-json-encode
  [[_ obj]]
  (list 'JSON.generate obj))

(defn ruby-tf-x-json-decode
  [[_ s]]
  (list 'JSON.parse s))

(def +ruby-json+
  {:x-json-encode      {:macro #'ruby-tf-x-json-encode    :emit :macro}
   :x-json-decode      {:macro #'ruby-tf-x-json-decode    :emit :macro}})

;;
;; B64
;;

(defn ruby-tf-x-b64-encode
  [[_ obj]]
  (list '. 'Base64 (list 'encode64 obj)))

(defn ruby-tf-x-b64-decode
  [[_ s]]
  (list '. 'Base64 (list 'decode64 s)))

;;
;; FILE
;;

(defn ruby-tf-x-file-slurp
  [[_ path opts cb]]
  (list '. cb (list 'call nil (list '. 'File (list 'read path)))))

(defn ruby-tf-x-file-spit
  [[_ path content opts cb]]
  (list 'do
        (list '. 'File (list 'write path content))
        (list '. cb (list 'call nil path))))

(def +ruby-file+
  {:x-file-slurp      {:macro #'ruby-tf-x-file-slurp     :emit :macro}
   :x-file-spit       {:macro #'ruby-tf-x-file-spit      :emit :macro}})

;;
;; THREAD
;;

(defn ruby-tf-x-with-delay
  [[_ thunk ms]]
  (list 'do
        (list 'sleep (list '/ ms 1000.0))
        (list '. thunk 'call)))

(def +ruby-thread+
  {:x-with-delay      {:macro #'ruby-tf-x-with-delay     :emit :macro}})

;; ITER
;;

(defn ruby-tf-x-iter-from-obj
  [[_ obj]]
  (list :% (list '. obj 'to_a) (list ':- ".each")))

(defn ruby-tf-x-iter-from-arr
  [[_ arr]]
  (list :% arr (list ':- ".each")))

(defn ruby-tf-x-iter-from
  [[_ obj]]
  (list :% obj (list ':- ".each")))

(defn ruby-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (let [arr0 (gensym "arr0__")
         arr1 (gensym "arr1__")
         i    (gensym "i__")
         same (gensym "same__")]
    (template/$
     (. (fn [~arr0 ~arr1]
          (if (not= (. ~arr0 length) (. ~arr1 length))
            (return false))
          (var ~same true)
          (var ~i 0)
          (while (< ~i (. ~arr0 length))
            (if (not (. ~eq-fn
                         (call (. ~arr0 [~i])
                               (. ~arr1 [~i]))))
              (do (:= ~same false)
                  (break)))
            (:= ~i (+ ~i 1)))
          (return ~same))
         (call (. ~it0 to_a)
               (. ~it1 to_a))))))

(defn ruby-tf-x-iter-null
  [[_]]
  (list :% [] (list ':- ".each")))

(defn ruby-tf-x-iter-next
  [[_ it]]
  (list '. it 'next))

(defn ruby-tf-x-iter-has?
  [[_ obj]]
  (list 'or
        (list :% obj (list ':- ".is_a?(Array)"))
        (list :% obj (list ':- ".is_a?(Enumerator)"))))

(defn ruby-tf-x-iter-native?
  [[_ it]]
  (list :% it (list ':- ".is_a?(Enumerator)")))

(def +ruby-iter+
  {:x-iter-from-obj       {:macro #'ruby-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr       {:macro #'ruby-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from           {:macro #'ruby-tf-x-iter-from           :emit :macro}
   :x-iter-eq             {:macro #'ruby-tf-x-iter-eq             :emit :macro}
   :x-iter-null           {:macro #'ruby-tf-x-iter-null           :emit :macro}
   :x-iter-next           {:macro #'ruby-tf-x-iter-next           :emit :macro}
   :x-iter-has?           {:macro #'ruby-tf-x-iter-has?           :emit :macro}
   :x-iter-native?        {:macro #'ruby-tf-x-iter-native?        :emit :macro}})

;; NETWORK
;;

(defn ruby-tf-x-socket-connect
  [[_ host & [port opts]]]
  (if (some? port)
    (list 'TCPSocket.new host port)
    (list 'TCPSocket.new host)))

(defn ruby-tf-x-socket-send
  [[_ conn value]]
  (list '. conn 'puts value))

(defn ruby-tf-x-socket-close
  [[_ conn]]
  (list '. conn 'close))

(def +ruby-network+
  {:x-socket-connect   {:macro #'ruby-tf-x-socket-connect   :emit :macro
                        :op-spec {:allow-blocks true}}
   :x-socket-send      {:macro #'ruby-tf-x-socket-send      :emit :macro}
   :x-socket-close     {:macro #'ruby-tf-x-socket-close     :emit :macro}})

;;
;; RETURN
;;

(defn ruby-tf-x-return-encode
  ([[_ out id key]]
    (template/$ (do (:- "require 'json'")
                    (try
                      (return (JSON.generate {:id  ~id
                                              :key ~key
                                              :type  "data"
                                              :return (x:type-native ~out)
                                              :value  ~out}))
                      (catch e
                          (return (JSON.generate {:id ~id
                                                  :key ~key
                                                  :type  "raw"
                                                  :return "raw"
                                                  :value (. e (to_s))}))))))))

(defn ruby-tf-x-return-wrap
  ([[_ f encode-fn]]
    (if (and (symbol? encode-fn)
             (= "return-encode" (name encode-fn)))
      (template/$ (do (:- "require 'json'")
                      (try
                        (:= out (. ~f (call)))
                        (catch e
                            (return (JSON.generate {:type "error"
                                                    :value (. e (to_s))}))))
                      (return (~encode-fn out nil nil))))
      (template/$ (do (:- "require 'json'")
                      (try
                        (:= out (. ~f (call)))
                        (catch e
                            (return (JSON.generate {:type "error"
                                                    :value (. e (to_s))}))))
                      (var encoded nil)
                      (if (== 1 (. ~encode-fn arity))
                        (:= encoded (. ~encode-fn (call out)))
                        (:= encoded (. ~encode-fn (call out nil nil))))
                      (return encoded))))))

(defn ruby-tf-x-return-eval
  ([[_ s wrap-fn]]
    (if (and (symbol? wrap-fn)
             (= "return-wrap" (name wrap-fn)))
      (template/$ (return (~wrap-fn (fn []
                                      (return (eval ~s))))))
      (template/$ (return (. ~wrap-fn
                             (call (fn []
                                     (return (eval ~s))))))))))

(def +ruby-return+
  {:x-return-encode  {:macro #'ruby-tf-x-return-encode   :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-return-wrap    {:macro #'ruby-tf-x-return-wrap     :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-return-eval    {:macro #'ruby-tf-x-return-eval     :emit :macro
                      :op-spec {:allow-blocks true}}})

(def +ruby+
  (merge +ruby-core+
         +ruby-math+
         +ruby-type+
         +ruby-arr+
         +ruby-str+
         +ruby-lu+
         +ruby-json+
         +ruby-file+
         +ruby-thread+
         +ruby-iter+
         +ruby-network+
         +ruby-return+))
