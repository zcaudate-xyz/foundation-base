(ns std.lang.model.spec-xtalk.fn-scheme)

(defn scheme-begin
  [forms]
  (if (= 1 (count forms))
    (first forms)
    (cons 'begin forms)))

(defn scheme-tf-x-print
  [[_ & args]]
  (scheme-begin
   (map (fn [arg]
          (list 'display arg))
        args)))

(defn scheme-tf-x-len
  [[_ obj]]
  (list 'cond
        (list (list 'vector? obj) (list 'vector-length obj))
        (list (list 'string? obj) (list 'string-length obj))
        (list (list 'hash? obj) (list 'hash-count obj))
        (list 'else (list 'length obj))))

(defn scheme-tf-x-cat
  [[_ & args]]
  (apply list 'string-append args))

(defn scheme-tf-x-apply
  [[_ f args]]
  (list 'apply f args))

(defn scheme-tf-x-eval
  [[_ s]]
  (list 'eval s))

(defn scheme-tf-x-random
  [_]
  '(random))

(defn scheme-tf-x-now-ms
  [_]
  '(current-inexact-milliseconds))

(defn scheme-tf-x-type-native
  [[_ obj]]
  (list 'cond
        (list (list 'null? obj)      "nil")
        (list (list 'string? obj)    "string")
        (list (list 'number? obj)    "number")
        (list (list 'boolean? obj)   "boolean")
        (list (list 'procedure? obj) "function")
        (list (list 'vector? obj)    "array")
        (list (list 'hash? obj)      "object")
        (list (list 'pair? obj)      "list")
        (list 'else                  "unknown")))

(def +scheme-core+
  {:x-print       {:macro #'scheme-tf-x-print       :emit :macro :value true}
   :x-len         {:macro #'scheme-tf-x-len         :emit :macro :value true}
   :x-cat         {:macro #'scheme-tf-x-cat         :emit :macro :value true}
   :x-apply       {:macro #'scheme-tf-x-apply       :emit :macro}
   :x-eval        {:macro #'scheme-tf-x-eval        :emit :macro}
   :x-random      {:macro #'scheme-tf-x-random      :emit :macro :value true}
   :x-now-ms      {:macro #'scheme-tf-x-now-ms      :emit :macro :value true}
   :x-type-native {:macro #'scheme-tf-x-type-native :emit :macro}})

(defn scheme-tf-x-get-key
  [[_ obj key default]]
  (if (some? default)
    (list 'hash-ref obj key default)
    (list 'hash-ref obj key)))

(defn scheme-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list 'and
          (list 'hash-has-key? obj key)
          (list 'equal? check (list 'hash-ref obj key)))
    (list 'hash-has-key? obj key)))

(defn scheme-tf-x-del-key
  [[_ obj key]]
  (list 'hash-remove obj key))

(defn scheme-tf-x-obj-keys
  [[_ obj]]
  (list 'hash-keys obj))

(def +scheme-object+
  {:x-get-key   {:macro #'scheme-tf-x-get-key   :emit :macro :value true}
   :x-has-key?  {:macro #'scheme-tf-x-has-key?  :emit :macro :value true}
   :x-del-key   {:macro #'scheme-tf-x-del-key   :emit :macro}
   :x-obj-keys  {:macro #'scheme-tf-x-obj-keys  :emit :macro :value true}})

(defn scheme-tf-x-get-idx
  [[_ arr idx default]]
  (if (some? default)
    (list 'if
          (list 'and (list '>= idx 0)
                (list '< idx (list 'vector-length arr)))
          (list 'vector-ref arr idx)
          default)
    (list 'vector-ref arr idx)))

(defn scheme-tf-x-set-idx
  [[_ arr idx value]]
  (list 'begin
        (list 'vector-set! arr idx value)
        arr))

(defn scheme-tf-x-arr-push
  [[_ arr value]]
  (list 'vector-append arr (list 'vector value)))

(defn scheme-tf-x-arr-pop
  [[_ arr]]
  (list 'let
        (list (list 'idx (list '- (list 'vector-length arr) 1)))
        (list 'vector-ref arr 'idx)))

(def +scheme-array+
  {:x-get-idx   {:macro #'scheme-tf-x-get-idx   :emit :macro :value true}
   :x-set-idx   {:macro #'scheme-tf-x-set-idx   :emit :macro}
   :x-arr-push  {:macro #'scheme-tf-x-arr-push  :emit :macro :value true}
   :x-arr-pop   {:macro #'scheme-tf-x-arr-pop   :emit :macro :value true}})

(defn scheme-tf-x-str-join
  [[_ sep coll]]
  (list 'string-join coll sep))

(defn scheme-tf-x-str-split
  [[_ s sep]]
  (list 'string-split s sep))

(defn scheme-tf-x-to-string
  [[_ x]]
  (list 'format "~a" x))

(defn scheme-tf-x-to-number
  [[_ x]]
  (list 'string->number x))

(def +scheme-string+
  {:x-str-join  {:macro #'scheme-tf-x-str-join  :emit :macro :value true}
   :x-str-split {:macro #'scheme-tf-x-str-split :emit :macro :value true}
   :x-to-string {:macro #'scheme-tf-x-to-string :emit :macro :value true}
   :x-to-number {:macro #'scheme-tf-x-to-number :emit :macro :value true}})

(defn scheme-tf-x-m-abs   [[_ n]] (list 'abs n))
(defn scheme-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn scheme-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn scheme-tf-x-m-floor [[_ n]] (list 'floor n))
(defn scheme-tf-x-m-ceil  [[_ n]] (list 'ceiling n))
(defn scheme-tf-x-m-sqrt  [[_ n]] (list 'sqrt n))
(defn scheme-tf-x-m-pow   [[_ b e]] (list 'expt b e))

(def +scheme-math+
  {:x-m-abs   {:macro #'scheme-tf-x-m-abs   :emit :macro :value true}
   :x-m-max   {:macro #'scheme-tf-x-m-max   :emit :macro :value true}
   :x-m-min   {:macro #'scheme-tf-x-m-min   :emit :macro :value true}
   :x-m-floor {:macro #'scheme-tf-x-m-floor :emit :macro :value true}
   :x-m-ceil  {:macro #'scheme-tf-x-m-ceil  :emit :macro :value true}
   :x-m-sqrt  {:macro #'scheme-tf-x-m-sqrt  :emit :macro :value true}
   :x-m-pow   {:macro #'scheme-tf-x-m-pow   :emit :macro :value true}})

(def +scheme+
  (merge +scheme-core+
         +scheme-object+
         +scheme-array+
         +scheme-string+
         +scheme-math+))
