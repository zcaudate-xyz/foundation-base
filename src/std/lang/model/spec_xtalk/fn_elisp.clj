(ns std.lang.model.spec-xtalk.fn-elisp
  (:require [std.lib.collection :as collection]
            [std.lib.template :as template]))

(defn elisp-begin
  [forms]
  (if (= 1 (count forms))
    (first forms)
    (cons 'progn forms)))

(defn elisp-vector-subseq
  ([arr start]
   (list 'seq-subseq arr start))
  ([arr start end]
   (list 'seq-subseq arr start end)))

(defn elisp-vector->list
  [value]
  (list 'append value nil))

;;
;; CORE
;;

(defn elisp-tf-x-del
  [[_ target]]
  (cond (and (collection/form? target)
             (= '. (first target))
             (= 3 (count target)))
        (let [prop (nth target 2)]
          (list 'x:del-key (second target)
                (if (and (vector? prop) (= 1 (count prop)))
                  (first prop)
                  prop)))

        (symbol? target)
        (list 'setq target nil)

        :else
        nil))

(defn elisp-tf-x-print
  [[_ & args]]
  (elisp-begin
   (concat
    (map (fn [arg]
           (list 'princ arg))
         args)
    [nil])))

(defn elisp-tf-x-len
  [[_ obj]]
  (list 'if
        (list 'vectorp obj)
        (list 'length obj)
        (list 'if
              (list 'hash-table-p obj)
              (list 'hash-table-count obj)
              (list 'length obj))))

(defn elisp-tf-x-cat
  [[_ & args]]
  (apply list 'concat args))

(defn elisp-tf-x-apply
  [[_ f args]]
  (list 'apply f (elisp-vector->list args)))

(defn elisp-tf-x-div
  [[_ a b & more]]
  (reduce (fn [acc v]
            (list '/ acc v))
          (list '/ (list 'float a) b)
          more))

(defn elisp-tf-x-err
  [[_ s & [data]]]
  (if (some? data)
    (list 'signal (list 'intern "error") (list 'list s data))
    (list 'error "%s" s)))

(defn elisp-tf-x-eval
  [[_ s]]
  (list 'let
        ['out (list 'calc-eval s)]
        (list 'if
              (list 'string-match-p "^-?[0-9]+\\(?:\\.[0-9]+\\)?$" 'out)
              (list 'string-to-number 'out)
              'out)))

(defn elisp-tf-x-random
  [_]
  '(/ (float (random 1000000)) 1000000.0))

(defn elisp-tf-x-now-ms
  [_]
  '(floor (* 1000 (float-time))))

(defn elisp-tf-x-ex-native?
  [[_ err]]
  (list 'or
        (list 'and
              (list 'vectorp err)
              (list '= 3 (list 'length err))
              (list 'equal "__xt_error__" (list 'aref err 0)))
        (list 'and
              (list 'consp err)
              (list 'equal (list 'car err) (list 'intern "error")))))

(defn elisp-tf-x-ex-new
  [[_ message & [data]]]
  (list 'vector "__xt_error__" message data))

(defn elisp-tf-x-ex-message
  [[_ err]]
  (list 'if
        (list 'vectorp err)
        (list 'aref err 1)
        (list 'if
              (list 'x:ex-native? err)
              (list 'nth 1 err)
              nil)))

(defn elisp-tf-x-ex-data
  [[_ err]]
  (list 'if
        (list 'vectorp err)
        (list 'aref err 2)
        (list 'if
              (list 'x:ex-native? err)
              (list 'nth 2 err)
              nil)))

(defn elisp-tf-x-type-native
  [[_ obj]]
  (list 'br*
        (list 'if     (list 'null obj)         "nil")
        (list 'elseif (list 'eq obj 't)        "boolean")
        (list 'elseif (list 'stringp obj)      "string")
        (list 'elseif (list 'numberp obj)      "number")
        (list 'elseif (list 'functionp obj)    "function")
        (list 'elseif (list 'vectorp obj)      "array")
        (list 'elseif (list 'hash-table-p obj) "object")
        (list 'elseif (list 'listp obj)        "list")
        (list 'elseif (list 'symbolp obj)      "symbol")
        (list 'else "unknown")))

(def +elisp-core+
  {:x-del         {:macro #'elisp-tf-x-del         :emit :macro}
   :x-print       {:macro #'elisp-tf-x-print       :emit :macro :value true}
   :x-len         {:macro #'elisp-tf-x-len         :emit :macro :value true}
   :x-cat         {:macro #'elisp-tf-x-cat         :emit :macro :value true}
   :x-div         {:macro #'elisp-tf-x-div         :emit :macro :value true}
   :x-apply       {:macro #'elisp-tf-x-apply       :emit :macro}
   :x-err         {:macro #'elisp-tf-x-err         :emit :macro}
   :x-eval        {:macro #'elisp-tf-x-eval        :emit :macro}
   :x-random      {:macro #'elisp-tf-x-random      :emit :macro :value true}
   :x-now-ms      {:macro #'elisp-tf-x-now-ms      :emit :macro :value true}
   :x-ex-native?  {:macro #'elisp-tf-x-ex-native?  :emit :macro :value true}
   :x-ex-new      {:macro #'elisp-tf-x-ex-new      :emit :macro :value true}
   :x-ex-message  {:macro #'elisp-tf-x-ex-message  :emit :macro :value true}
   :x-ex-data     {:macro #'elisp-tf-x-ex-data     :emit :macro :value true}
   :x-type-native {:macro #'elisp-tf-x-type-native :emit :macro}})

;;
;; GLOBAL
;;

(defn elisp-tf-x-global-set
  [[_ sym value]]
  (list 'progn
        (list 'puthash (name sym) value '__xt_globals__)
        value))

(defn elisp-tf-x-global-del
  [[_ sym]]
  (list 'progn
        (list 'remhash (name sym) '__xt_globals__)
        nil))

(defn elisp-tf-x-global-has?
  [[_ sym]]
  (list 'do
        (list 'var 'missing (list 'make-symbol "__xt_missing"))
        (list 'var 'value (list 'gethash (name sym) '__xt_globals__ 'missing))
        (list 'not (list 'eq 'value 'missing))))

(def +elisp-global+
  {:x-global-set  {:macro #'elisp-tf-x-global-set  :emit :macro}
   :x-global-del  {:macro #'elisp-tf-x-global-del  :emit :macro}
   :x-global-has? {:macro #'elisp-tf-x-global-has? :emit :macro :value true}})

;;
;; TYPE
;;

(defn elisp-tf-x-to-string
  [[_ x]]
  (list 'format "%s" x))

(defn elisp-tf-x-to-number
  [[_ x]]
  (list 'if
        (list 'numberp x)
        x
        (list 'string-to-number x)))

(defn elisp-tf-x-is-string?
  [[_ x]]
  (list 'stringp x))

(defn elisp-tf-x-is-number?
  [[_ x]]
  (list 'numberp x))

(defn elisp-tf-x-is-integer?
  [[_ x]]
  (list 'integerp x))

(defn elisp-tf-x-is-boolean?
  [[_ x]]
  (list 'or (list 'eq x 't)
        (list 'null x)))

(defn elisp-tf-x-is-function?
  [[_ x]]
  (list 'functionp x))

(defn elisp-tf-x-is-object?
  [[_ x]]
  (list 'hash-table-p x))

(defn elisp-tf-x-is-array?
  [[_ x]]
  (list 'vectorp x))

(def +elisp-type+
  {:x-to-string    {:macro #'elisp-tf-x-to-string    :emit :macro :value true}
   :x-to-number    {:macro #'elisp-tf-x-to-number    :emit :macro :value true}
   :x-is-string?   {:macro #'elisp-tf-x-is-string?   :emit :macro :value true}
   :x-is-number?   {:macro #'elisp-tf-x-is-number?   :emit :macro :value true}
   :x-is-integer?  {:macro #'elisp-tf-x-is-integer?  :emit :macro :value true}
   :x-is-boolean?  {:macro #'elisp-tf-x-is-boolean?  :emit :macro :value true}
   :x-is-function? {:macro #'elisp-tf-x-is-function? :emit :macro :value true}
   :x-is-object?   {:macro #'elisp-tf-x-is-object?   :emit :macro :value true}
   :x-is-array?    {:macro #'elisp-tf-x-is-array?    :emit :macro :value true}})

;;
;; LU
;;

(defn elisp-tf-x-lu-create
  [_]
  (list 'make-hash-table :test (list 'intern "eq")))

(defn elisp-tf-x-lu-eq
  [[_ x y]]
  (list 'eq x y))

(defn elisp-tf-x-lu-get
  [[_ lu obj]]
  (list 'gethash obj lu))

(defn elisp-tf-x-lu-set
  [[_ lu obj gid]]
  (list 'progn
        (list 'puthash obj gid lu)
        lu))

(defn elisp-tf-x-lu-del
  [[_ lu obj]]
  (list 'progn
        (list 'remhash obj lu)
        lu))

(def +elisp-lu+
  {:x-lu-create {:macro #'elisp-tf-x-lu-create :emit :macro :value true}
   :x-lu-eq     {:macro #'elisp-tf-x-lu-eq     :emit :macro :value true}
   :x-lu-get    {:macro #'elisp-tf-x-lu-get    :emit :macro :value true}
   :x-lu-set    {:macro #'elisp-tf-x-lu-set    :emit :macro}
   :x-lu-del    {:macro #'elisp-tf-x-lu-del    :emit :macro}})

;;
;; OBJECT
;;

(defn elisp-tf-x-get-key
  [[_ obj key default]]
  (if (some? default)
    (list 'gethash key obj default)
    (list 'gethash key obj)))

(defn elisp-tf-x-get-path
  [[_ obj path default]]
  (let [default (if (some? default) default nil)]
    (if (vector? path)
      (reduce (fn [acc key]
                (list 'x:get-key acc key default))
              obj
              path)
      (list 'x:get-key obj path default))))

(defn elisp-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list 'do
          (list 'var 'missing (list 'make-symbol "__xt_missing"))
          (list 'var 'value (list 'gethash key obj 'missing))
          (list 'and
                (list 'not (list 'eq 'value 'missing))
                (list 'equal check 'value)))
    (list 'do
          (list 'var 'missing (list 'make-symbol "__xt_missing"))
          (list 'var 'value (list 'gethash key obj 'missing))
          (list 'not (list 'eq 'value 'missing)))))

(defn elisp-tf-x-del-key
  [[_ obj key]]
  (list 'progn
        (list 'remhash key obj)
        obj))

(defn elisp-tf-x-set-key
  [[_ obj key value]]
  (list 'progn
        (list 'puthash key value obj)
        obj))

(defn elisp-tf-x-copy-key
  [[_ dst src key]]
  (if (vector? key)
    (list 'x:set-key dst (first key) (list 'x:get-key src (second key) nil))
    (list 'x:set-key dst key (list 'x:get-key src key nil))))

(defn elisp-tf-x-obj-keys
  [[_ obj]]
  (list 'vconcat (list 'hash-table-keys obj)))

(defn elisp-tf-x-obj-vals
  [[_ obj]]
  (list 'vconcat
        (list 'mapcar
              (list 'lambda '(k) (list 'gethash 'k obj))
              (list 'hash-table-keys obj))))

(defn elisp-tf-x-obj-pairs
  [[_ obj]]
  (list 'vconcat
        (list 'mapcar
              (list 'lambda '(k) (list 'vector 'k (list 'gethash 'k obj)))
              (list 'hash-table-keys obj))))

(defn elisp-tf-x-obj-clone
  [[_ obj]]
  (list 'do
        (list 'var 'out (list 'make-hash-table :test (list 'intern "equal")))
        (list 'maphash
              (list 'lambda '(k v) (list 'puthash 'k 'v 'out))
              obj)
        'out))

(defn elisp-tf-x-obj-assign
  [[_ obj other]]
  (let [target (if (symbol? obj) obj (gensym "obj__"))]
    (if (symbol? obj)
      (list 'progn
            (list 'maphash
                  (list 'lambda '(k v) (list 'puthash 'k 'v target))
                  other)
            target)
      (list 'let
            [target obj]
            (list 'maphash
                  (list 'lambda '(k v) (list 'puthash 'k 'v target))
                  other)
            target))))

(def +elisp-object+
  {:x-get-key    {:macro #'elisp-tf-x-get-key    :emit :macro :value true}
   :x-get-path   {:macro #'elisp-tf-x-get-path   :emit :macro :value true}
   :x-set-key    {:macro #'elisp-tf-x-set-key    :emit :macro :value true}
   :x-copy-key   {:macro #'elisp-tf-x-copy-key   :emit :macro :value true}
   :x-has-key?   {:macro #'elisp-tf-x-has-key?   :emit :macro :value true}
   :x-del-key    {:macro #'elisp-tf-x-del-key    :emit :macro}
   :x-obj-keys   {:macro #'elisp-tf-x-obj-keys   :emit :macro :value true}
   :x-obj-vals   {:macro #'elisp-tf-x-obj-vals   :emit :macro :value true}
   :x-obj-pairs  {:macro #'elisp-tf-x-obj-pairs  :emit :macro :value true}
   :x-obj-clone  {:macro #'elisp-tf-x-obj-clone  :emit :macro :value true}
   :x-obj-assign {:macro #'elisp-tf-x-obj-assign :emit :macro :value true}})

;;
;; ARRAY
;;

(defn elisp-tf-x-get-idx
  [[_ arr idx default]]
  (if (some? default)
    (list 'if
          (list 'and (list '>= idx 0)
                (list '< idx (list 'length arr)))
          (list 'aref arr idx)
          default)
    (list 'aref arr idx)))

(defn elisp-tf-x-set-idx
  [[_ arr idx value]]
  (list 'progn
        (list 'aset arr idx value)
        arr))

(defn elisp-tf-x-arr-clone
  [[_ arr]]
  (elisp-vector-subseq arr 0))

(defn elisp-tf-x-arr-slice
  [[_ arr start end]]
  (elisp-vector-subseq arr start end))

(defn elisp-tf-x-arr-reverse
  [[_ arr]]
  (list 'vconcat (list 'reverse (elisp-vector->list arr))))

(defn elisp-tf-x-arr-concat
  [[_ arr other]]
  (list 'vconcat arr other))

(defn elisp-tf-x-arr-push
  [[_ arr value]]
  (if (symbol? arr)
    (list 'progn
          (list 'setq arr (list 'vconcat arr (list 'vector value)))
          arr)
    (list 'vconcat arr (list 'vector value))))

(defn elisp-tf-x-arr-pop
  [[_ arr]]
  (if (symbol? arr)
    (list 'do
          (list 'var 'idx (list '- (list 'length arr) 1))
          (list 'var 'value (list 'aref arr 'idx))
          (list ':= arr (elisp-vector-subseq arr 0 'idx))
          'value)
    (list 'aref arr (list '- (list 'length arr) 1))))

(defn elisp-tf-x-arr-push-first
  [[_ arr value]]
  (if (symbol? arr)
    (list 'progn
          (list 'setq arr (list 'vconcat (list 'vector value) arr))
          arr)
    (list 'vconcat (list 'vector value) arr)))

(defn elisp-tf-x-arr-pop-first
  [[_ arr]]
  (if (symbol? arr)
    (list 'do
          (list 'var 'value (list 'aref arr 0))
          (list ':= arr (elisp-vector-subseq arr 1))
          'value)
    (list 'aref arr 0)))

(defn elisp-tf-x-arr-insert
  [[_ arr idx value]]
  (let [expr (list 'vconcat
                   (elisp-vector-subseq arr 0 idx)
                   (list 'vector value)
                   (elisp-vector-subseq arr idx))]
    (if (symbol? arr)
      (list 'progn
            (list 'setq arr expr)
            arr)
      expr)))

(defn elisp-tf-x-arr-remove
  [[_ arr idx]]
  (let [expr (list 'vconcat
                   (elisp-vector-subseq arr 0 idx)
                   (elisp-vector-subseq arr (list '+ idx 1)))]
    (if (symbol? arr)
      (list 'progn
            (list 'setq arr expr)
            arr)
      expr)))

(defn elisp-tf-x-arr-assign
  [[_ arr other]]
  (let [expr (list 'vconcat arr other)]
    (if (symbol? arr)
      (list 'progn
            (list 'setq arr expr)
            arr)
      expr)))

(defn elisp-tf-x-arr-some
  [[_ arr pred]]
  (list 'if
        (list 'seq-some pred (elisp-vector->list arr))
        't
        nil))

(defn elisp-tf-x-arr-each
  [[_ arr f]]
  (list 'progn
        (list 'mapc
              (list 'lambda '(e)
                    (list 'funcall f 'e))
              (elisp-vector->list arr))
        arr))

(defn elisp-tf-x-arr-every
  [[_ arr pred]]
  (list 'if
        (list 'seq-every-p
              (list 'lambda '(e)
                    (list 'funcall pred 'e))
              (elisp-vector->list arr))
        't
        nil))

(defn elisp-tf-x-arr-map
  [[_ arr f]]
  (list 'vconcat
        (list 'mapcar
              (list 'lambda '(e)
                    (list 'funcall f 'e))
              (elisp-vector->list arr))))

(defn elisp-tf-x-arr-filter
  [[_ arr pred]]
  (list 'vconcat
        (list 'seq-filter
              (list 'lambda '(e)
                    (list 'funcall pred 'e))
              (elisp-vector->list arr))))

(defn elisp-tf-x-arr-foldl
  [[_ arr f init]]
  (list 'seq-reduce
        (list 'lambda '(acc e)
              (list 'funcall f 'acc 'e))
        (elisp-vector->list arr)
        init))

(defn elisp-tf-x-arr-foldr
  [[_ arr f init]]
  (list 'seq-reduce
        (list 'lambda '(acc e)
              (list 'funcall f 'acc 'e))
        (list 'reverse (elisp-vector->list arr))
        init))

(defn elisp-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (let [sorted (list 'vconcat
                     (list 'sort
                           (elisp-vector->list arr)
                           (list 'lambda '(a b)
                                 (list 'funcall comp-fn
                                       (list 'funcall key-fn 'a)
                                       (list 'funcall key-fn 'b)))))]
    (if (symbol? arr)
      (list 'progn
            (list 'setq arr sorted)
            arr)
      sorted)))

(def +elisp-array+
  {:x-get-idx        {:macro #'elisp-tf-x-get-idx        :emit :macro :value true}
   :x-set-idx        {:macro #'elisp-tf-x-set-idx        :emit :macro}
   :x-arr-clone      {:macro #'elisp-tf-x-arr-clone      :emit :macro :value true}
   :x-arr-slice      {:macro #'elisp-tf-x-arr-slice      :emit :macro :value true}
   :x-arr-reverse    {:macro #'elisp-tf-x-arr-reverse    :emit :macro :value true}
   :x-arr-concat     {:macro #'elisp-tf-x-arr-concat     :emit :macro :value true}
   :x-arr-push       {:macro #'elisp-tf-x-arr-push       :emit :macro :value true}
   :x-arr-pop        {:macro #'elisp-tf-x-arr-pop        :emit :macro :value true}
   :x-arr-push-first {:macro #'elisp-tf-x-arr-push-first :emit :macro :value true}
   :x-arr-pop-first  {:macro #'elisp-tf-x-arr-pop-first  :emit :macro :value true}
   :x-arr-insert     {:macro #'elisp-tf-x-arr-insert     :emit :macro :value true}
   :x-arr-remove     {:macro #'elisp-tf-x-arr-remove     :emit :macro :value true}
   :x-arr-assign     {:macro #'elisp-tf-x-arr-assign     :emit :macro :value true}
   :x-arr-some       {:macro #'elisp-tf-x-arr-some       :emit :macro :value true}
   :x-arr-each       {:macro #'elisp-tf-x-arr-each       :emit :macro :value true}
   :x-arr-every      {:macro #'elisp-tf-x-arr-every      :emit :macro :value true}
   :x-arr-map        {:macro #'elisp-tf-x-arr-map        :emit :macro :value true}
   :x-arr-filter     {:macro #'elisp-tf-x-arr-filter     :emit :macro :value true}
   :x-arr-foldl      {:macro #'elisp-tf-x-arr-foldl      :emit :macro :value true}
   :x-arr-foldr      {:macro #'elisp-tf-x-arr-foldr      :emit :macro :value true}
   :x-arr-sort       {:macro #'elisp-tf-x-arr-sort       :emit :macro :value true}})

;;
;; STRING
;;

(defn elisp-tf-x-str-comp
  [[_ a b]]
  (list 'string-lessp a b))

(defn elisp-tf-x-str-char
  [[_ s i]]
  (list 'aref s i))

(defn elisp-tf-x-str-split
  [[_ s sep]]
  (list 'vconcat (list 'split-string s sep)))

(defn elisp-tf-x-str-len
  [[_ s]]
  (list 'length s))

(defn elisp-tf-x-str-join
  [[_ sep coll]]
  (list 'mapconcat
        (list 'lambda '(x) 'x)
        (elisp-vector->list coll)
        sep))

(defn elisp-tf-x-str-index-of
  [[_ s tok start]]
  (let [start (or start 0)]
    (list 'do
          (list 'var 'pos
                (list 'string-match-p
                      (list 'regexp-quote tok)
                      s
                      start))
          (list 'if 'pos 'pos -1))))

(defn elisp-tf-x-str-substring
  [[_ s start & [end]]]
  (if (some? end)
    (list 'substring-no-properties s start end)
    (list 'substring-no-properties s start)))

(defn elisp-tf-x-str-to-upper
  [[_ s]]
  (list 'upcase s))

(defn elisp-tf-x-str-to-lower
  [[_ s]]
  (list 'downcase s))

(defn elisp-tf-x-str-to-fixed
  [[_ num digits]]
  (list 'format
        (list 'concat "%." (list 'number-to-string digits) "f")
        num))

(defn elisp-tf-x-str-replace
  [[_ s tok replacement]]
  (list 'replace-regexp-in-string
        (list 'regexp-quote tok)
        replacement
        s
        't
        't))

(defn elisp-tf-x-str-trim
  [[_ s]]
  (list 'string-trim s))

(defn elisp-tf-x-str-trim-left
  [[_ s]]
  (list 'string-trim-left s))

(defn elisp-tf-x-str-trim-right
  [[_ s]]
  (list 'string-trim-right s))

(defn elisp-tf-x-str-pad-left
  [[_ s n ch]]
  (list 'if
        (list '>= (list 'length s) n)
        s
        (list 'concat
              (list 'make-string (list '- n (list 'length s))
                    (list 'aref ch 0))
              s)))

(defn elisp-tf-x-str-pad-right
  [[_ s n ch]]
  (list 'if
        (list '>= (list 'length s) n)
        s
        (list 'concat
              s
              (list 'make-string (list '- n (list 'length s))
                    (list 'aref ch 0)))))

(defn elisp-tf-x-str-starts-with
  [[_ s prefix]]
  (list 'string-prefix-p prefix s))

(defn elisp-tf-x-str-ends-with
  [[_ s suffix]]
  (list 'string-suffix-p suffix s))

(def +elisp-string+
  {:x-str-comp        {:macro #'elisp-tf-x-str-comp        :emit :macro :value true}
   :x-str-char        {:macro #'elisp-tf-x-str-char        :emit :macro :value true}
   :x-str-len         {:macro #'elisp-tf-x-str-len         :emit :macro :value true}
   :x-str-split       {:macro #'elisp-tf-x-str-split       :emit :macro :value true}
   :x-str-join        {:macro #'elisp-tf-x-str-join        :emit :macro :value true}
   :x-str-index-of    {:macro #'elisp-tf-x-str-index-of    :emit :macro :value true}
   :x-str-substring   {:macro #'elisp-tf-x-str-substring   :emit :macro :value true}
   :x-str-to-upper    {:macro #'elisp-tf-x-str-to-upper    :emit :macro :value true}
   :x-str-to-lower    {:macro #'elisp-tf-x-str-to-lower    :emit :macro :value true}
   :x-str-to-fixed    {:macro #'elisp-tf-x-str-to-fixed    :emit :macro :value true}
   :x-str-replace     {:macro #'elisp-tf-x-str-replace     :emit :macro :value true}
   :x-str-trim        {:macro #'elisp-tf-x-str-trim        :emit :macro :value true}
   :x-str-trim-left   {:macro #'elisp-tf-x-str-trim-left   :emit :macro :value true}
   :x-str-trim-right  {:macro #'elisp-tf-x-str-trim-right  :emit :macro :value true}
   :x-str-pad-left    {:macro #'elisp-tf-x-str-pad-left    :emit :macro :value true}
   :x-str-pad-right   {:macro #'elisp-tf-x-str-pad-right   :emit :macro :value true}
   :x-str-starts-with {:macro #'elisp-tf-x-str-starts-with :emit :macro :value true}
   :x-str-ends-with   {:macro #'elisp-tf-x-str-ends-with   :emit :macro :value true}})

;;
;; MATH
;;

(defn elisp-tf-x-m-abs   [[_ n]] (list 'abs n))
(defn elisp-tf-x-m-acos  [[_ n]] (list 'acos n))
(defn elisp-tf-x-m-asin  [[_ n]] (list 'asin n))
(defn elisp-tf-x-m-atan  [[_ n]] (list 'atan n))
(defn elisp-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn elisp-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn elisp-tf-x-m-mod   [[_ n d]] (list 'mod n d))
(defn elisp-tf-x-m-quot  [[_ n d]] (list 'floor (list '/ n d)))
(defn elisp-tf-x-m-floor [[_ n]] (list 'floor n))
(defn elisp-tf-x-m-ceil  [[_ n]] (list 'ceiling n))
(defn elisp-tf-x-m-cos   [[_ n]] (list 'cos n))
(defn elisp-tf-x-m-cosh  [[_ n]]
  (list '/
        (list '+
              (list 'exp n)
              (list 'exp (list '- n)))
        2.0))
(defn elisp-tf-x-m-exp   [[_ n]] (list 'exp n))
(defn elisp-tf-x-m-loge  [[_ n]] (list 'log n))
(defn elisp-tf-x-m-log10 [[_ n]] (list '/ (list 'log n) (list 'log 10)))
(defn elisp-tf-x-m-sin   [[_ n]] (list 'sin n))
(defn elisp-tf-x-m-sinh  [[_ n]]
  (list '/
        (list '-
              (list 'exp n)
              (list 'exp (list '- n)))
        2.0))
(defn elisp-tf-x-m-sqrt  [[_ n]] (list 'sqrt n))
(defn elisp-tf-x-m-tan   [[_ n]] (list 'tan n))
(defn elisp-tf-x-m-tanh  [[_ n]]
  (list '/
        (list 'x:m-sinh n)
        (list 'x:m-cosh n)))
(defn elisp-tf-x-m-pow   [[_ b e]] (list 'expt b e))

(def +elisp-math+
  {:x-m-abs   {:macro #'elisp-tf-x-m-abs   :emit :macro :value true}
   :x-m-acos  {:macro #'elisp-tf-x-m-acos  :emit :macro :value true}
   :x-m-asin  {:macro #'elisp-tf-x-m-asin  :emit :macro :value true}
   :x-m-atan  {:macro #'elisp-tf-x-m-atan  :emit :macro :value true}
   :x-m-max   {:macro #'elisp-tf-x-m-max   :emit :macro :value true}
   :x-m-min   {:macro #'elisp-tf-x-m-min   :emit :macro :value true}
   :x-m-mod   {:macro #'elisp-tf-x-m-mod   :emit :macro :value true}
   :x-m-quot  {:macro #'elisp-tf-x-m-quot  :emit :macro :value true}
   :x-m-floor {:macro #'elisp-tf-x-m-floor :emit :macro :value true}
   :x-m-ceil  {:macro #'elisp-tf-x-m-ceil  :emit :macro :value true}
   :x-m-cos   {:macro #'elisp-tf-x-m-cos   :emit :macro :value true}
   :x-m-cosh  {:macro #'elisp-tf-x-m-cosh  :emit :macro :value true}
   :x-m-exp   {:macro #'elisp-tf-x-m-exp   :emit :macro :value true}
   :x-m-loge  {:macro #'elisp-tf-x-m-loge  :emit :macro :value true}
   :x-m-log10 {:macro #'elisp-tf-x-m-log10 :emit :macro :value true}
   :x-m-sin   {:macro #'elisp-tf-x-m-sin   :emit :macro :value true}
   :x-m-sinh  {:macro #'elisp-tf-x-m-sinh  :emit :macro :value true}
   :x-m-sqrt  {:macro #'elisp-tf-x-m-sqrt  :emit :macro :value true}
   :x-m-tan   {:macro #'elisp-tf-x-m-tan   :emit :macro :value true}
   :x-m-tanh  {:macro #'elisp-tf-x-m-tanh  :emit :macro :value true}
   :x-m-pow   {:macro #'elisp-tf-x-m-pow   :emit :macro :value true}})

;;
;; RETURN / JSON
;;

(defn elisp-tf-x-json-encode
  [[_ value]]
  (list 'json-serialize (list 'xt-json-normalize value)))

(defn elisp-tf-x-json-decode
  [[_ expr]]
  (list 'json-parse-string expr
        :object-type (list 'intern "hash-table")
        :array-type (list 'intern "array")
        :null-object nil
        :false-object :false))

(defn elisp-tf-x-return-encode
  [[_ out id key]]
  (list 'let
        ['ts (list 'x:type-native out)]
        (list 'condition-case 'err
              (list 'json-serialize
                    (list 'list
                          (list 'cons (list 'intern "id") id)
                          (list 'cons (list 'intern "key") key)
                          (list 'cons (list 'intern "type") "data")
                          (list 'cons (list 'intern "return") 'ts)
                          (list 'cons (list 'intern "value") (list 'xt-json-normalize out))))
              (list 'error
                    (list 'json-serialize
                          (list 'list
                                (list 'cons (list 'intern "id") id)
                                (list 'cons (list 'intern "key") key)
                                (list 'cons (list 'intern "type") "raw")
                                (list 'cons (list 'intern "return") 'ts)
                                (list 'cons (list 'intern "value") (list 'format "%S" out))))))))

(defn elisp-tf-x-return-wrap
  [[_ f encode-fn]]
  (list 'condition-case 'err
        (list 'funcall encode-fn (list 'funcall f))
        (list 'error
              (list 'json-serialize
                    (list 'list
                          (list 'cons (list 'intern "type") "error")
                          (list 'cons (list 'intern "value") (list 'error-message-string 'err)))))))

(defn elisp-tf-x-return-eval
  [[_ s wrap-fn]]
  (list 'funcall wrap-fn
        (list 'lambda '()
              (list 'do
                    (list 'var 'out (list 'calc-eval s))
                    (list 'if
                          (list 'string-match-p "^-?[0-9]+\\(?:\\.[0-9]+\\)?$" 'out)
                          (list 'string-to-number 'out)
                          'out)))))

(def +elisp-json+
  {:x-json-encode   {:macro #'elisp-tf-x-json-encode   :emit :macro :value true}
   :x-json-decode   {:macro #'elisp-tf-x-json-decode   :emit :macro :value true}
   :x-return-encode {:macro #'elisp-tf-x-return-encode :emit :macro :value true}
   :x-return-wrap   {:macro #'elisp-tf-x-return-wrap   :emit :macro :value true}
   :x-return-eval   {:macro #'elisp-tf-x-return-eval   :emit :macro :value true}})

;;
;; BIT
;;

(defn elisp-tf-x-bit-and
  [[_ x y]]
  (list 'logand x y))

(defn elisp-tf-x-bit-or
  [[_ x y]]
  (list 'logior x y))

(defn elisp-tf-x-bit-xor
  [[_ x y]]
  (list 'logxor x y))

(defn elisp-tf-x-bit-lshift
  [[_ x y]]
  (list 'lsh x y))

(defn elisp-tf-x-bit-rshift
  [[_ x y]]
  (list 'lsh x (list '- y)))

(def +elisp-bit+
  {:x-bit-and    {:macro #'elisp-tf-x-bit-and    :emit :macro :value true}
   :x-bit-or     {:macro #'elisp-tf-x-bit-or     :emit :macro :value true}
   :x-bit-xor    {:macro #'elisp-tf-x-bit-xor    :emit :macro :value true}
   :x-bit-lshift {:macro #'elisp-tf-x-bit-lshift :emit :macro :value true}
   :x-bit-rshift {:macro #'elisp-tf-x-bit-rshift :emit :macro :value true}})

;;
;; ITER
;;

(defn elisp-tf-x-iter-from-arr
  [[_ arr]]
  (list 'vector "__xt_iter__" arr 0))

(defn elisp-tf-x-iter-from-obj
  [[_ obj]]
  (list 'x:iter-from-arr (list 'x:obj-pairs obj)))

(defn elisp-tf-x-iter-from
  [[_ obj]]
  (list 'if
        (list 'x:iter-native? obj)
        obj
        (list 'x:iter-from-arr obj)))

(defn elisp-tf-x-iter-next
  [[_ it]]
  (list 'do
        (list 'var 'values (list 'aref it 1))
        (list 'var 'idx (list 'aref it 2))
        (list 'if
              (list '< 'idx (list 'length 'values))
              (list 'prog1
                    (list 'aref 'values 'idx)
                    (list 'aset it 2 (list '+ 'idx 1)))
              "__xt_iter_end__")))

(defn elisp-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (list 'do
        (list 'var 'cmp eq-fn)
        (list 'var 'result 't)
        (list 'var 'done nil)
        (list 'while (list 'not 'done)
              (list 'do
                    (list 'var 'x0 (list 'x:iter-next it0))
                    (list 'var 'x1 (list 'x:iter-next it1))
                    (list 'if
                          (list 'equal 'x0 "__xt_iter_end__")
                          (list 'do
                                (list ':= 'result (list 'equal 'x1 "__xt_iter_end__"))
                                (list ':= 'done 't))
                          (list 'if
                                (list 'equal 'x1 "__xt_iter_end__")
                                (list 'do
                                      (list ':= 'result nil)
                                      (list ':= 'done 't))
                                (list 'if
                                      (list 'not (list 'funcall 'cmp 'x0 'x1))
                                      (list 'do
                                            (list ':= 'result nil)
                                            (list ':= 'done 't))
                                      nil)))))
        'result))

(defn elisp-tf-x-iter-null
  [_]
  '(vector "__xt_iter__" [] 0))

(defn elisp-tf-x-iter-has?
  [[_ obj]]
  (list 'or
        (list 'vectorp obj)
        (list 'x:iter-native? obj)))

(defn elisp-tf-x-iter-native?
  [[_ it]]
  (list 'and
        (list 'vectorp it)
        (list '= 3 (list 'length it))
        (list 'equal "__xt_iter__" (list 'aref it 0))))

(def +elisp-iter+
  {:x-iter-from-obj {:macro #'elisp-tf-x-iter-from-obj :emit :macro :value true}
   :x-iter-from-arr {:macro #'elisp-tf-x-iter-from-arr :emit :macro :value true}
   :x-iter-from     {:macro #'elisp-tf-x-iter-from     :emit :macro :value true}
   :x-iter-eq       {:macro #'elisp-tf-x-iter-eq       :emit :macro :value true}
   :x-iter-null     {:macro #'elisp-tf-x-iter-null     :emit :macro :value true}
   :x-iter-next     {:macro #'elisp-tf-x-iter-next     :emit :macro :value true}
   :x-iter-has?     {:macro #'elisp-tf-x-iter-has?     :emit :macro :value true}
   :x-iter-native?  {:macro #'elisp-tf-x-iter-native?  :emit :macro :value true}})

(defn elisp-tf-x-prototype-create
  [[_ m]]
  (list 'xt-proto-create m))

(defn elisp-tf-x-prototype-get
  [[_ obj]]
  (list 'xt-proto-get obj))

(defn elisp-tf-x-prototype-set
  [[_ obj prototype]]
  (list 'xt-proto-set obj prototype))

(defn elisp-tf-x-prototype-method
  [[_ obj key]]
  (list 'xt-proto-method obj key))

(def +elisp-proto+
  {:prototype-create {:macro #'elisp-tf-x-prototype-create :emit :macro
                      :op-spec {:allow-blocks true}}
   :prototype-get    {:macro #'elisp-tf-x-prototype-get    :emit :macro}
   :prototype-set    {:macro #'elisp-tf-x-prototype-set    :emit :macro}
   :prototype-method {:macro #'elisp-tf-x-prototype-method :emit :macro}})

;;
;; PROMISE
;;

(defn elisp-tf-x-promise
  [[_ thunk]]
  (list 'xt-promise thunk))

(defn elisp-tf-x-async-run
  [[_ thunk]]
  (template/$
   (make-thread ~thunk)))

(defn elisp-tf-x-promise-all
  [[_ promises]]
  (list 'xt-promise-all promises))

(defn elisp-tf-x-promise-then
  [[_ promise thunk]]
  (list 'xt-promise-then promise thunk))

(defn elisp-tf-x-promise-catch
  [[_ promise thunk]]
  (list 'xt-promise-catch promise thunk))

(defn elisp-tf-x-promise-finally
  [[_ promise thunk]]
  (list 'xt-promise-finally promise thunk))

(defn elisp-tf-x-promise-native?
  [[_ value]]
  (list 'xt-promise-native-p value))

(defn elisp-tf-x-with-delay
  [[_ ms thunk]]
  (list 'xt-with-delay ms thunk))

(def +elisp-promise+
  {:x-async-run        {:macro #'elisp-tf-x-async-run        :emit :macro :value true}
   :x-promise          {:macro #'elisp-tf-x-promise          :emit :macro}
   :x-promise-all      {:macro #'elisp-tf-x-promise-all      :emit :macro}
   :x-promise-then     {:macro #'elisp-tf-x-promise-then     :emit :macro}
   :x-promise-catch    {:macro #'elisp-tf-x-promise-catch    :emit :macro}
   :x-promise-finally  {:macro #'elisp-tf-x-promise-finally  :emit :macro}
   :x-promise-native?  {:macro #'elisp-tf-x-promise-native?  :emit :macro}
   :x-with-delay       {:macro #'elisp-tf-x-with-delay       :emit :macro :value true}})

;;
;; SOCKET
;;

(defn elisp-tf-x-socket-connect
  [[_ host port _opts cb]]
  (list 'condition-case 'err
        (list 'do
              (list 'var 'conn (list 'open-network-stream "xt-elisp-socket" nil host port))
              (list 'funcall cb nil 'conn))
        (list 'error
              (list 'funcall cb 'err nil))))

(defn elisp-tf-x-socket-send
  [[_ conn s]]
  (list 'do
        (list 'process-send-string conn s)
        (list 'accept-process-output conn 0 50)
        conn))

(defn elisp-tf-x-socket-close
  [[_ conn]]
  (list 'when
        (list 'process-live-p conn)
        (list 'do
              (list 'ignore-errors
                    (list 'process-send-eof conn))
              (list 'accept-process-output conn 0 50)
              (list 'delete-process conn))))

(def +elisp-socket+
  {:x-socket-connect {:macro #'elisp-tf-x-socket-connect :emit :macro :value true}
   :x-socket-send    {:macro #'elisp-tf-x-socket-send    :emit :macro :value true}
   :x-socket-close   {:macro #'elisp-tf-x-socket-close   :emit :macro :value true}})

;;
;; HTTP
;;

(defn elisp-tf-x-notify-http
  [[_ host port value id key opts]]
  (let [path-sym     (gensym "path__")
        scheme-sym   (gensym "scheme__")
        url-sym      (gensym "url__")
        payload-sym  (gensym "payload__")
        exit-sym     (gensym "exit__")]
    (list 'condition-case 'err
          (list 'let*
                (list (list path-sym (list 'or
                                           (list 'and opts
                                                 (list 'gethash "path" opts))
                                           "/"))
                      (list scheme-sym (list 'or
                                             (list 'and opts
                                                   (list 'gethash "scheme" opts))
                                             "http"))
                      (list url-sym (list 'concat
                                          scheme-sym
                                          "://"
                                          host
                                          ":"
                                          (list 'format "%s" port)
                                          path-sym))
                      (list payload-sym (list 'return-encode value id key)))
                (list 'if
                      (list 'not (list 'equal scheme-sym "http"))
                      (list 'vector "unable to connect")
                      (list 'with-temp-buffer
                            (list 'setq exit-sym
                                  (list 'call-process
                                        "curl"
                                        nil
                                        't
                                        nil
                                        "-sS"
                                        "-X"
                                        "POST"
                                        "-H"
                                        "Content-Type: application/json"
                                        "--data-binary"
                                        payload-sym
                                        url-sym))
                            (list 'if
                                  (list 'equal exit-sym 0)
                                  (list 'vector "async")
                                  (list 'vector "unable to connect")))))
          (list 'error
                (list 'vector "unable to connect")))))

(def +elisp-http+
  {:x-notify-http {:macro #'elisp-tf-x-notify-http :emit :macro
                   :value/standalone true
                   :op-spec {:allow-blocks true}}})

;;
;; SHELL
;;

(defn elisp-tf-x-pwd
  [[_]]
  (list 'directory-file-name 'default-directory))

(defn elisp-tf-x-shell
  [[_ s root cb]]
  (let [exit-sym (gensym "exit__")
        out-sym  (gensym "out__")
        err-sym  (gensym "err__")
        obj-sym  (gensym "obj__")]
    (list 'condition-case err-sym
          (list 'let
                (list (list 'default-directory (list 'or root 'default-directory)))
                (list 'with-temp-buffer
                      (list 'let*
                            (list (list exit-sym
                                        (list 'call-process-shell-command s nil 't nil))
                                  (list out-sym
                                        (list 'buffer-string)))
                            (list 'if
                                  (list 'equal exit-sym 0)
                                  (list 'funcall cb nil out-sym)
                                  (list 'let
                                        (list (list obj-sym
                                                    (list 'make-hash-table :test (list 'intern "equal"))))
                                        (list 'puthash "code" exit-sym obj-sym)
                                        (list 'puthash "err" out-sym obj-sym)
                                        (list 'puthash "out" out-sym obj-sym)
                                        (list 'funcall cb obj-sym nil))))))
          (list 'error
                (list 'funcall cb err-sym nil)))))

(def +elisp-shell+
  {:x-pwd   {:macro #'elisp-tf-x-pwd   :emit :macro}
   :x-shell {:macro #'elisp-tf-x-shell :emit :macro
             :op-spec {:allow-blocks true}}})

;;
;; FILE
;;

(defn elisp-tf-x-file-resolve
  [[_ root path]]
  (list 'expand-file-name path root))

(defn elisp-tf-x-file-slurp
  [[_ path cb]]
  (list 'condition-case 'err
        (list 'with-temp-buffer
              (list 'insert-file-contents path)
              (list 'funcall cb nil (list 'buffer-string)))
        (list 'error
              (list 'funcall cb 'err nil))))

(defn elisp-tf-x-file-spit
  [[_ path content cb]]
  (let [dir-sym (gensym "dir__")]
    (list 'condition-case 'err
          (list 'let
                (list (list dir-sym (list 'file-name-directory path)))
                (list 'when dir-sym
                      (list 'make-directory dir-sym 't))
                (list 'with-temp-file path
                      (list 'insert (list 'format "%s" content)))
                (list 'funcall cb nil path))
          (list 'error
                (list 'funcall cb 'err nil)))))

(def +elisp-file+
  {:x-file-resolve {:macro #'elisp-tf-x-file-resolve :emit :macro}
   :x-file-slurp   {:macro #'elisp-tf-x-file-slurp   :emit :macro
                    :op-spec {:allow-blocks true}}
   :x-file-spit    {:macro #'elisp-tf-x-file-spit    :emit :macro
                    :op-spec {:allow-blocks true}}})

(def +elisp+
  (merge +elisp-core+
         +elisp-global+
         +elisp-type+
         +elisp-lu+
         +elisp-object+
         +elisp-array+
         +elisp-string+
         +elisp-math+
         +elisp-json+
         +elisp-bit+
         +elisp-iter+
         +elisp-promise+
         +elisp-socket+
         +elisp-http+
         +elisp-shell+
         +elisp-file+))
