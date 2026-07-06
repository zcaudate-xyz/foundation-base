(ns hara.model.spec-xtalk.fn-scheme
  (:require [std.lib.collection :as collection]
            [std.lib.template :as template]))

(defn scheme-begin
  [forms]
  (if (= 1 (count forms))
    (first forms)
    (cons 'begin forms)))

(defn scheme-vector-slice
  ([arr start]
   (list 'vector-copy arr start))
  ([arr start end]
   (list 'vector-copy arr start end)))

(defn scheme-vector->list
  [value]
  (list 'if
        (list 'vector? value)
        (list 'vector->list value)
        value))

(defn scheme-if-chain
  [pairs fallback]
  (if-let [[[test expr] & more] (seq pairs)]
    (list 'if test expr (scheme-if-chain more fallback))
    fallback))

(defn scheme-truthy-check
  [sym]
  (list 'if
        (list 'null? sym)
        false
        (list 'if
              (list 'equal? sym false)
              false
              true)))

(defn scheme-tf-or
  [[_ & args]]
  (letfn [(or-expr [args]
            (cond (empty? args)
                  false
                  (= 1 (count args))
                  (first args)
                  :else
                  (let [sym (gensym "v__")]
                    (list 'let
                          [sym (first args)]
                          (list 'if
                                (scheme-truthy-check sym)
                                sym
                                (or-expr (rest args)))))))]
    (or-expr args)))

(defn scheme-tf-and
  [[_ & args]]
  (letfn [(and-expr [args]
            (cond (empty? args)
                  true
                  (= 1 (count args))
                  (first args)
                  :else
                  (let [sym (gensym "v__")]
                    (list 'let
                          [sym (first args)]
                          (list 'if
                                (scheme-truthy-check sym)
                                (and-expr (rest args))
                                sym)))))]
    (and-expr args)))

(defn scheme-tf--%%-
  [[_ value]]
  (list 'x:eval value))

(defn scheme-promise-native-expr
  [value]
  (list 'and
        (list 'vector? value)
        (list '= 3 (list 'vector-length value))
        (list 'equal? "__xt_promise__" (list 'vector-ref value 0))))

(defn scheme-promise-rejected-expr
  [value]
  (list 'and
        (scheme-promise-native-expr value)
        (list 'equal? "rejected" (list 'vector-ref value 1))))

(defn scheme-promise-wrap-expr
  [value]
  (list 'if
        (scheme-promise-native-expr value)
        value
        (list 'vector "__xt_promise__" "resolved" value)))

;;
;; CORE
;;

(defn scheme-tf-x-del
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
        (list 'set! target nil)

        :else
        nil))

(defn scheme-tf-x-print
  [[_ & args]]
  (scheme-begin
   (concat
    (map (fn [arg]
           (list 'display arg))
         args)
    [false])))

(defn scheme-tf-x-len
  [[_ obj]]
  (list 'if
        (list 'vector? obj)
        (list 'vector-length obj)
        (list 'if
              (list 'string? obj)
              (list 'string-length obj)
              (list 'if
                    (list 'hash? obj)
                    (list 'hash-count obj)
                    (list 'length obj)))))

(defn scheme-tf-x-cat
  [[_ & args]]
  (apply list 'string-append args))

(defn scheme-tf-x-apply
  [[_ f args]]
  (list 'apply f (scheme-vector->list args)))

(defn scheme-tf-x-div
  [[_ a b & more]]
  (let [expr (reduce (fn [acc v]
                       (list '/ acc v))
                     (list '/ a b)
                     more)]
    (list 'if
          (list 'integer? expr)
          expr
          (list 'exact->inexact expr))))

(defn scheme-tf-x-err
  [[_ s & [data]]]
  (if (some? data)
    (list 'raise (list 'x:ex-new s data))
    (list 'error s)))

(defn scheme-tf-x-eval
  [[_ s]]
  (list 'do
        (list 'var 'parts (list 'string-split s))
        (list 'var 'parsed (list 'string->number s))
        (list 'var 'compact
              (list 'regexp-match
                    (list 'pregexp "^\\s*(-?[0-9]+(?:\\.[0-9]+)?)([+\\-*/])(-?[0-9]+(?:\\.[0-9]+)?)\\s*$")
                    s))
        (scheme-if-chain
         [[(list 'number? 'parsed) 'parsed]
          ['compact
           (list 'do
                 (list 'var 'a (list 'string->number (list 'cadr 'compact)))
                 (list 'var 'op (list 'caddr 'compact))
                 (list 'var 'b (list 'string->number (list 'cadddr 'compact)))
                 (scheme-if-chain
                  [[(list 'equal? 'op "+") (list '+ 'a 'b)]
                   [(list 'equal? 'op "-") (list '- 'a 'b)]
                   [(list 'equal? 'op "*") (list '* 'a 'b)]
                   [(list 'equal? 'op "/") (list '/ 'a 'b)]]
                  s))]
          [(list '= (list 'length 'parts) 3)
           (list 'do
                 (list 'var 'a (list 'string->number (list 'car 'parts)))
                 (list 'var 'op (list 'cadr 'parts))
                 (list 'var 'b (list 'string->number (list 'caddr 'parts)))
                 (scheme-if-chain
                  [[(list 'equal? 'op "+") (list '+ 'a 'b)]
                   [(list 'equal? 'op "-") (list '- 'a 'b)]
                   [(list 'equal? 'op "*") (list '* 'a 'b)]
                   [(list 'equal? 'op "/") (list '/ 'a 'b)]]
                  s))]]
         s)))

(defn scheme-tf-x-random
  [_]
  '(/ (random 1000000) 1000000.0))

(defn scheme-tf-x-now-ms
  [_]
  '(current-inexact-milliseconds))

(defn scheme-tf-x-ex-native?
  [[_ err]]
  (list 'or
        (list 'exn:fail? err)
        (list 'and
              (list 'hash? err)
              (list 'equal? "xt.exception"
                    (list 'hash-ref err "__type__" nil)))
        (list 'and
              (list 'pair? err)
              (list 'hash? (list 'second err))
              (list 'equal? "xt.exception"
                    (list 'hash-ref (list 'second err) "__type__" nil)))))

(defn scheme-tf-x-ex-new
  [[_ message & [data]]]
  {"__type__" "xt.exception"
   "message" message
   "data" data})

(defn scheme-tf-x-ex-message
  [[_ err]]
  (scheme-if-chain
   [[(list 'exn:fail? err)
     (list 'exn-message err)]
    [(list 'and
           (list 'hash? err)
           (list 'equal? "xt.exception"
                 (list 'hash-ref err "__type__" nil)))
     (list 'hash-ref err "message" nil)]
    [(list 'and
           (list 'pair? err)
           (list 'hash? (list 'second err))
           (list 'equal? "xt.exception"
                 (list 'hash-ref (list 'second err) "__type__" nil)))
     (list 'hash-ref (list 'second err) "message" nil)]]
   nil))

(defn scheme-tf-x-ex-data
  [[_ err]]
  (scheme-if-chain
   [[(list 'and
           (list 'hash? err)
           (list 'equal? "xt.exception"
                 (list 'hash-ref err "__type__" nil)))
     (list 'hash-ref err "data" nil)]
    [(list 'and
           (list 'pair? err)
           (list 'hash? (list 'second err))
           (list 'equal? "xt.exception"
                 (list 'hash-ref (list 'second err) "__type__" nil)))
     (list 'hash-ref (list 'second err) "data" nil)]]
   nil))

(defn scheme-tf-x-type-native
  [[_ obj]]
  (scheme-if-chain
   [[(list 'not obj)        "nil"]
    [(list 'string? obj)    "string"]
    [(list 'number? obj)    "number"]
    [(list 'boolean? obj)   "boolean"]
    [(list 'procedure? obj) "function"]
    [(list 'vector? obj)    "array"]
    [(list 'hash? obj)      "object"]
    [(list 'pair? obj)      "list"]]
   "unknown"))

(def +scheme-core+
  {:x-del         {:macro #'scheme-tf-x-del         :emit :macro}
   :x-print       {:macro #'scheme-tf-x-print       :emit :macro :value true}
   :x-len         {:macro #'scheme-tf-x-len         :emit :macro :value true}
   :x-cat         {:macro #'scheme-tf-x-cat         :emit :macro :value true}
   :x-div         {:macro #'scheme-tf-x-div         :emit :macro :value true}
   :x-apply       {:macro #'scheme-tf-x-apply       :emit :macro}
   :x-err         {:macro #'scheme-tf-x-err         :emit :macro}
   :x-eval        {:macro #'scheme-tf-x-eval        :emit :macro}
   :x-random      {:macro #'scheme-tf-x-random      :emit :macro :value true}
   :x-now-ms      {:macro #'scheme-tf-x-now-ms      :emit :macro :value true}
   :x-ex-native?  {:macro #'scheme-tf-x-ex-native?  :emit :macro :value true}
   :x-ex-new      {:macro #'scheme-tf-x-ex-new      :emit :macro :value true}
   :x-ex-message  {:macro #'scheme-tf-x-ex-message  :emit :macro :value true}
   :x-ex-data     {:macro #'scheme-tf-x-ex-data     :emit :macro :value true}
   :x-type-native {:macro #'scheme-tf-x-type-native :emit :macro}})

;;
;; GLOBAL
;;

(defn scheme-tf-x-global-set
  [[_ sym value]]
  (list 'begin
        (list 'hash-set! '__xt_globals__ (name sym) value)
        value))

(defn scheme-tf-x-global-del
  [[_ sym]]
  (list 'begin
        (list 'hash-remove! '__xt_globals__ (name sym))
        nil))

(defn scheme-tf-x-global-has?
  [[_ sym]]
  (list 'hash-has-key? '__xt_globals__ (name sym)))

(def +scheme-global+
  {:x-global-set {:macro #'scheme-tf-x-global-set :emit :macro}
   :x-global-del {:macro #'scheme-tf-x-global-del :emit :macro}
   :x-global-has? {:macro #'scheme-tf-x-global-has? :emit :macro :value true}})

;;
;; TYPE
;;

(defn scheme-tf-x-to-string
  [[_ x]]
  (list 'format "~a" x))

(defn scheme-tf-x-to-number
  [[_ x]]
  (list 'if
        (list 'number? x)
        x
        (list 'string->number x)))

(defn scheme-tf-x-is-string?
  [[_ x]]
  (list 'string? x))

(defn scheme-tf-x-is-number?
  [[_ x]]
  (list 'number? x))

(defn scheme-tf-x-is-integer?
  [[_ x]]
  (list 'integer? x))

(defn scheme-tf-x-is-boolean?
  [[_ x]]
  (list 'boolean? x))

(defn scheme-tf-x-is-function?
  [[_ x]]
  (list 'procedure? x))

(defn scheme-tf-x-is-object?
  [[_ x]]
  (list 'hash? x))

(defn scheme-tf-x-is-array?
  [[_ x]]
  (list 'vector? x))

(def +scheme-type+
  {:x-to-string    {:macro #'scheme-tf-x-to-string    :emit :macro :value true}
   :x-to-number    {:macro #'scheme-tf-x-to-number    :emit :macro :value true}
   :x-is-string?   {:macro #'scheme-tf-x-is-string?   :emit :macro :value true}
   :x-is-number?   {:macro #'scheme-tf-x-is-number?   :emit :macro :value true}
   :x-is-integer?  {:macro #'scheme-tf-x-is-integer?  :emit :macro :value true}
   :x-is-boolean?  {:macro #'scheme-tf-x-is-boolean?  :emit :macro :value true}
   :x-is-function? {:macro #'scheme-tf-x-is-function? :emit :macro :value true}
   :x-is-object?   {:macro #'scheme-tf-x-is-object?   :emit :macro :value true}
   :x-is-array?    {:macro #'scheme-tf-x-is-array?    :emit :macro :value true}})

;;
;; LU
;;

(defn scheme-tf-x-lu-create
  [_]
  '(make-hasheq))

(defn scheme-tf-x-lu-get
  [[_ lu obj]]
  (list 'hash-ref lu obj nil))

(defn scheme-tf-x-lu-set
  [[_ lu obj gid]]
  (list 'begin
        (list 'hash-set! lu obj gid)
        lu))

(defn scheme-tf-x-lu-del
  [[_ lu obj]]
  (list 'begin
        (list 'hash-remove! lu obj)
        lu))

(defn scheme-tf-x-lu-eq
  [[_ a b]]
  (list 'eq? a b))

(def +scheme-lu+
  {:x-lu-create {:macro #'scheme-tf-x-lu-create :emit :macro :value true}
   :x-lu-get    {:macro #'scheme-tf-x-lu-get    :emit :macro :value true}
   :x-lu-set    {:macro #'scheme-tf-x-lu-set    :emit :macro}
   :x-lu-del    {:macro #'scheme-tf-x-lu-del    :emit :macro}
   :x-lu-eq     {:macro #'scheme-tf-x-lu-eq     :emit :macro :value true}})

;;
;; OBJECT
;;

(defn scheme-tf-x-get-key
  [[_ obj key default]]
  (if (some? default)
    (list 'hash-ref obj key (list 'lambda '() default))
    (list 'hash-ref obj key nil)))

(defn scheme-tf-x-get-path
  [[_ obj path default]]
  (let [default (if (some? default) default nil)]
    (if (vector? path)
      (reduce (fn [acc key]
                (list 'x:get-key acc key default))
              obj
              path)
      (list 'x:get-key obj path default))))

(defn scheme-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list 'and
          (list 'hash-has-key? obj key)
          (list 'equal? check (list 'hash-ref obj key)))
    (list 'hash-has-key? obj key)))

(defn scheme-tf-x-del-key
  [[_ obj key]]
  (list 'begin
        (list 'hash-remove! obj key)
        obj))

(defn scheme-tf-x-set-key
  [[_ obj key value]]
  (list 'begin
        (list 'hash-set! obj key value)
        obj))

(defn scheme-tf-x-copy-key
  [[_ dst src key]]
  (if (vector? key)
    (list 'x:set-key
          dst
          (first key)
          (list 'if
                (list 'vector? src)
                (list 'x:get-idx src (second key) nil)
                (list 'x:get-key src (second key) nil)))
    (list 'x:set-key dst key (list 'x:get-key src key nil))))

(defn scheme-tf-x-obj-keys
  [[_ obj]]
  (list 'list->vector (list 'hash-keys obj)))

(defn scheme-tf-x-obj-vals
  [[_ obj]]
  (list 'list->vector
        (list 'for/list
              (list (list (list 'k 'v)
                          (list 'in-hash obj)))
              'v)))

(defn scheme-tf-x-obj-pairs
  [[_ obj]]
  (list 'list->vector
        (list 'for/list
              (list (list (list 'k 'v)
                          (list 'in-hash obj)))
              (list 'vector 'k 'v))))

(defn scheme-tf-x-obj-clone
  [[_ obj]]
  (list 'make-hash (list 'hash->list obj)))

(defn scheme-tf-x-obj-assign
  [[_ obj other]]
  (if (symbol? obj)
    (list 'begin
          (list 'for
                (list (list (list 'k 'v)
                            (list 'in-hash other)))
                (list 'hash-set! obj 'k 'v))
          obj)
    (list 'do
          (list 'var 'out (list 'make-hash (list 'hash->list obj)))
          (list 'for
                (list (list (list 'k 'v)
                            (list 'in-hash other)))
                (list 'hash-set! 'out 'k 'v))
          'out)))

(def +scheme-object+
  {:x-get-key    {:macro #'scheme-tf-x-get-key    :emit :macro :value true}
   :x-get-path   {:macro #'scheme-tf-x-get-path   :emit :macro :value true}
   :x-set-key    {:macro #'scheme-tf-x-set-key    :emit :macro :value true}
   :x-copy-key   {:macro #'scheme-tf-x-copy-key   :emit :macro :value true}
   :x-has-key?   {:macro #'scheme-tf-x-has-key?   :emit :macro :value true}
   :x-del-key    {:macro #'scheme-tf-x-del-key    :emit :macro}
   :x-obj-keys   {:macro #'scheme-tf-x-obj-keys   :emit :macro :value true}
   :x-obj-vals   {:macro #'scheme-tf-x-obj-vals   :emit :macro :value true}
   :x-obj-pairs  {:macro #'scheme-tf-x-obj-pairs  :emit :macro :value true}
   :x-obj-clone  {:macro #'scheme-tf-x-obj-clone  :emit :macro :value true}
   :x-obj-assign {:macro #'scheme-tf-x-obj-assign :emit :macro :value true}})

;;
;; ARRAY
;;

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

(defn scheme-tf-x-arr-clone
  [[_ arr]]
  (scheme-vector-slice arr 0))

(defn scheme-tf-x-arr-slice
  [[_ arr start end]]
  (scheme-vector-slice arr start end))

(defn scheme-tf-x-arr-reverse
  [[_ arr]]
  (list 'list->vector (list 'reverse (list 'vector->list arr))))

(defn scheme-tf-x-arr-concat
  [[_ arr other]]
  (list 'vector-append arr other))

(defn scheme-tf-x-arr-push
  [[_ arr value]]
  (if (symbol? arr)
    (list 'begin
          (list 'set! arr (list 'vector-append arr (list 'vector value)))
          arr)
    (list 'vector-append arr (list 'vector value))))

(defn scheme-tf-x-arr-pop
  [[_ arr]]
  (if (symbol? arr)
    (list 'do
          (list 'var 'idx (list '- (list 'vector-length arr) 1))
          (list 'var 'value (list 'vector-ref arr 'idx))
          (list ':= arr (scheme-vector-slice arr 0 'idx))
          'value)
    (list 'vector-ref arr (list '- (list 'vector-length arr) 1))))

(defn scheme-tf-x-arr-push-first
  [[_ arr value]]
  (if (symbol? arr)
    (list 'begin
          (list 'set! arr (list 'vector-append (list 'vector value) arr))
          arr)
    (list 'vector-append (list 'vector value) arr)))

(defn scheme-tf-x-arr-pop-first
  [[_ arr]]
  (if (symbol? arr)
    (list 'do
          (list 'var 'value (list 'vector-ref arr 0))
          (list ':= arr (scheme-vector-slice arr 1))
          'value)
    (list 'vector-ref arr 0)))

(defn scheme-tf-x-arr-insert
  [[_ arr idx value]]
  (let [expr (list 'vector-append
                   (scheme-vector-slice arr 0 idx)
                   (list 'vector value)
                   (scheme-vector-slice arr idx))]
    (if (symbol? arr)
      (list 'begin
            (list 'set! arr expr)
            arr)
      expr)))

(defn scheme-tf-x-arr-remove
  [[_ arr idx]]
  (let [expr (list 'vector-append
                   (scheme-vector-slice arr 0 idx)
                   (scheme-vector-slice arr (list '+ idx 1)))]
    (if (symbol? arr)
      (list 'begin
            (list 'set! arr expr)
            arr)
      expr)))

(defn scheme-tf-x-arr-assign
  [[_ arr other]]
  (let [expr (list 'vector-append arr other)]
    (if (symbol? arr)
      (list 'begin
            (list 'set! arr expr)
            arr)
      expr)))

(defn scheme-tf-x-arr-each
  [[_ arr f]]
  (list 'begin
        (list 'for
              (list (list 'e (list 'in-vector arr)))
              (list f 'e))
        arr))

(defn scheme-tf-x-arr-every
  [[_ arr pred]]
  (list 'for/and
        (list (list 'e (list 'in-vector arr)))
        (list pred 'e)))

(defn scheme-tf-x-arr-some
  [[_ arr pred]]
  (list 'for/or
        (list (list 'e (list 'in-vector arr)))
        (list pred 'e)))

(defn scheme-tf-x-arr-map
  [[_ arr f]]
  (list 'for/vector
        (list (list 'e (list 'in-vector arr)))
        (list f 'e)))

(defn scheme-tf-x-arr-filter
  [[_ arr pred]]
  (list 'list->vector
        (list 'filter pred (list 'vector->list arr))))

(defn scheme-tf-x-arr-foldl
  [[_ arr f init]]
  (list 'for/fold
        (list (list 'out init))
        (list (list 'e (list 'in-vector arr)))
        (list f 'out 'e)))

(defn scheme-tf-x-arr-foldr
  [[_ arr f init]]
  (list 'for/fold
        (list (list 'out init))
        (list (list 'e (list 'in-list (list 'reverse (list 'vector->list arr)))))
        (list f 'out 'e)))

(defn scheme-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (let [expr (list 'list->vector
                   (list 'sort
                         (list 'vector->list arr)
                         (list 'lambda '(a b)
                               (list comp-fn
                                     (list key-fn 'a)
                                     (list key-fn 'b)))))]
    (if (symbol? arr)
      (list 'begin
            (list 'set! arr expr)
            arr)
      expr)))

(def +scheme-array+
  {:x-get-idx        {:macro #'scheme-tf-x-get-idx        :emit :macro :value true}
   :x-set-idx        {:macro #'scheme-tf-x-set-idx        :emit :macro}
   :x-arr-clone      {:macro #'scheme-tf-x-arr-clone      :emit :macro :value true}
   :x-arr-slice      {:macro #'scheme-tf-x-arr-slice      :emit :macro :value true}
   :x-arr-reverse    {:macro #'scheme-tf-x-arr-reverse    :emit :macro :value true}
   :x-arr-concat     {:macro #'scheme-tf-x-arr-concat     :emit :macro :value true}
   :x-arr-push       {:macro #'scheme-tf-x-arr-push       :emit :macro :value true}
   :x-arr-pop        {:macro #'scheme-tf-x-arr-pop        :emit :macro :value true}
   :x-arr-push-first {:macro #'scheme-tf-x-arr-push-first :emit :macro :value true}
   :x-arr-pop-first  {:macro #'scheme-tf-x-arr-pop-first  :emit :macro :value true}
   :x-arr-insert     {:macro #'scheme-tf-x-arr-insert     :emit :macro :value true}
   :x-arr-remove     {:macro #'scheme-tf-x-arr-remove     :emit :macro :value true}
   :x-arr-assign     {:macro #'scheme-tf-x-arr-assign     :emit :macro :value true}
   :x-arr-each       {:macro #'scheme-tf-x-arr-each       :emit :macro :value true}
   :x-arr-every      {:macro #'scheme-tf-x-arr-every      :emit :macro :value true}
   :x-arr-some       {:macro #'scheme-tf-x-arr-some       :emit :macro :value true}
   :x-arr-map        {:macro #'scheme-tf-x-arr-map        :emit :macro :value true}
   :x-arr-filter     {:macro #'scheme-tf-x-arr-filter     :emit :macro :value true}
   :x-arr-foldl      {:macro #'scheme-tf-x-arr-foldl      :emit :macro :value true}
   :x-arr-foldr      {:macro #'scheme-tf-x-arr-foldr      :emit :macro :value true}
   :x-arr-sort       {:macro #'scheme-tf-x-arr-sort       :emit :macro :value true}})

;;
;; STRING
;;

(defn scheme-tf-x-str-comp
  [[_ a b]]
  (list 'string<? a b))

(defn scheme-tf-x-str-char
  [[_ s i]]
  (list 'char->integer (list 'string-ref s i)))

(defn scheme-tf-x-str-len
  [[_ s]]
  (list 'string-length s))

(defn scheme-tf-x-str-split
  [[_ s sep]]
  (list 'list->vector (list 'string-split s sep)))

(defn scheme-tf-x-str-join
  [[_ sep coll]]
  (list 'string-join (scheme-vector->list coll) sep))

(defn scheme-tf-x-str-index-of
  [[_ s tok start]]
  (let [start (or start 0)]
    (list 'do
          (list 'var 'offset start)
          (list 'var 'sub (list 'xt-string-substring s start (list 'string-length s)))
          (list 'var 'matches
                (list 'regexp-match-positions
                      (list 'regexp-quote tok)
                      'sub))
          (list 'if
                'matches
                (list '+ 'offset
                      (list 'car (list 'car 'matches)))
                -1))))

(defn scheme-tf-x-str-substring
  [[_ s start & [end]]]
  (if (some? end)
    (list 'xt-string-substring s start end)
    (list 'xt-string-substring s start (list 'string-length s))))

(defn scheme-tf-x-str-to-upper
  [[_ s]]
  (list 'string-upcase s))

(defn scheme-tf-x-str-to-lower
  [[_ s]]
  (list 'string-downcase s))

(defn scheme-tf-x-str-to-fixed
  [[_ n digits]]
  (list 'real->decimal-string n digits))

(defn scheme-tf-x-str-replace
  [[_ s tok replacement]]
  (list 'string-replace s tok replacement))

(defn scheme-tf-x-str-trim
  [[_ s]]
  (list 'string-trim s))

(defn scheme-tf-x-str-trim-left
  [[_ s]]
  (list 'regexp-replace (list 'pregexp "^\\s+") s ""))

(defn scheme-tf-x-str-trim-right
  [[_ s]]
  (list 'regexp-replace (list 'pregexp "\\s+$") s ""))

(defn scheme-tf-x-str-pad-left
  [[_ s n ch]]
  (list 'if
        (list '>= (list 'string-length s) n)
        s
        (list 'string-append
              (list 'make-string (list '- n (list 'string-length s))
                    (list 'string-ref ch 0))
              s)))

(defn scheme-tf-x-str-pad-right
  [[_ s n ch]]
  (list 'if
        (list '>= (list 'string-length s) n)
        s
        (list 'string-append
              s
              (list 'make-string (list '- n (list 'string-length s))
                    (list 'string-ref ch 0)))))

(defn scheme-tf-x-str-starts-with
  [[_ s prefix]]
  (list 'and
        (list '<= (list 'string-length prefix) (list 'string-length s))
        (list 'equal? prefix
              (list 'xt-string-substring s 0 (list 'string-length prefix)))))

(defn scheme-tf-x-str-ends-with
  [[_ s suffix]]
  (list 'and
        (list '<= (list 'string-length suffix) (list 'string-length s))
        (list 'equal? suffix
              (list 'xt-string-substring s
                    (list '- (list 'string-length s)
                          (list 'string-length suffix))
                    (list 'string-length s)))))

(def +scheme-string+
  {:x-str-comp        {:macro #'scheme-tf-x-str-comp        :emit :macro :value true}
   :x-str-char        {:macro #'scheme-tf-x-str-char        :emit :macro :value true}
   :x-str-len         {:macro #'scheme-tf-x-str-len         :emit :macro :value true}
   :x-str-split       {:macro #'scheme-tf-x-str-split       :emit :macro :value true}
   :x-str-join        {:macro #'scheme-tf-x-str-join        :emit :macro :value true}
   :x-str-index-of    {:macro #'scheme-tf-x-str-index-of    :emit :macro :value true}
   :x-str-substring   {:macro #'scheme-tf-x-str-substring   :emit :macro :value true}
   :x-str-to-upper    {:macro #'scheme-tf-x-str-to-upper    :emit :macro :value true}
   :x-str-to-lower    {:macro #'scheme-tf-x-str-to-lower    :emit :macro :value true}
   :x-str-to-fixed    {:macro #'scheme-tf-x-str-to-fixed    :emit :macro :value true}
   :x-str-replace     {:macro #'scheme-tf-x-str-replace     :emit :macro :value true}
   :x-str-trim        {:macro #'scheme-tf-x-str-trim        :emit :macro :value true}
   :x-str-trim-left   {:macro #'scheme-tf-x-str-trim-left   :emit :macro :value true}
   :x-str-trim-right  {:macro #'scheme-tf-x-str-trim-right  :emit :macro :value true}
   :x-str-pad-left    {:macro #'scheme-tf-x-str-pad-left    :emit :macro :value true}
   :x-str-pad-right   {:macro #'scheme-tf-x-str-pad-right   :emit :macro :value true}
   :x-str-starts-with {:macro #'scheme-tf-x-str-starts-with :emit :macro :value true}
   :x-str-ends-with   {:macro #'scheme-tf-x-str-ends-with   :emit :macro :value true}})

;;
;; MATH
;;

(defn scheme-tf-x-m-abs   [[_ n]] (list 'abs n))
(defn scheme-tf-x-m-acos  [[_ n]] (list 'acos n))
(defn scheme-tf-x-m-asin  [[_ n]] (list 'asin n))
(defn scheme-tf-x-m-atan  [[_ n]] (list 'atan n))
(defn scheme-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn scheme-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn scheme-tf-x-m-mod   [[_ n d]] (list 'modulo n d))
(defn scheme-tf-x-m-quot  [[_ n d]] (list 'inexact->exact (list 'floor (list '/ n d))))
(defn scheme-tf-x-m-floor [[_ n]] (list 'inexact->exact (list 'floor n)))
(defn scheme-tf-x-m-ceil  [[_ n]] (list 'inexact->exact (list 'ceiling n)))
(defn scheme-tf-x-m-cos   [[_ n]] (list 'cos n))
(defn scheme-tf-x-m-cosh  [[_ n]]
  (list '/
        (list '+
              (list 'exp n)
              (list 'exp (list '- n)))
        2.0))
(defn scheme-tf-x-m-exp   [[_ n]] (list 'exp n))
(defn scheme-tf-x-m-loge  [[_ n]] (list 'log n))
(defn scheme-tf-x-m-log10 [[_ n]] (list '/ (list 'log n) (list 'log 10)))
(defn scheme-tf-x-m-sin   [[_ n]] (list 'sin n))
(defn scheme-tf-x-m-sinh  [[_ n]]
  (list '/
        (list '-
              (list 'exp n)
              (list 'exp (list '- n)))
        2.0))
(defn scheme-tf-x-m-sqrt  [[_ n]] (list 'sqrt n))
(defn scheme-tf-x-m-tan   [[_ n]] (list 'tan n))
(defn scheme-tf-x-m-tanh  [[_ n]]
  (list '/
        (list 'x:m-sinh n)
        (list 'x:m-cosh n)))
(defn scheme-tf-x-m-pow   [[_ b e]] (list 'expt b e))

(def +scheme-math+
  {:x-m-abs   {:macro #'scheme-tf-x-m-abs   :emit :macro :value true}
   :x-m-acos  {:macro #'scheme-tf-x-m-acos  :emit :macro :value true}
   :x-m-asin  {:macro #'scheme-tf-x-m-asin  :emit :macro :value true}
   :x-m-atan  {:macro #'scheme-tf-x-m-atan  :emit :macro :value true}
   :x-m-max   {:macro #'scheme-tf-x-m-max   :emit :macro :value true}
   :x-m-min   {:macro #'scheme-tf-x-m-min   :emit :macro :value true}
   :x-m-mod   {:macro #'scheme-tf-x-m-mod   :emit :macro :value true}
   :x-m-quot  {:macro #'scheme-tf-x-m-quot  :emit :macro :value true}
   :x-m-floor {:macro #'scheme-tf-x-m-floor :emit :macro :value true}
   :x-m-ceil  {:macro #'scheme-tf-x-m-ceil  :emit :macro :value true}
   :x-m-cos   {:macro #'scheme-tf-x-m-cos   :emit :macro :value true}
   :x-m-cosh  {:macro #'scheme-tf-x-m-cosh  :emit :macro :value true}
   :x-m-exp   {:macro #'scheme-tf-x-m-exp   :emit :macro :value true}
   :x-m-loge  {:macro #'scheme-tf-x-m-loge  :emit :macro :value true}
   :x-m-log10 {:macro #'scheme-tf-x-m-log10 :emit :macro :value true}
   :x-m-sin   {:macro #'scheme-tf-x-m-sin   :emit :macro :value true}
   :x-m-sinh  {:macro #'scheme-tf-x-m-sinh  :emit :macro :value true}
   :x-m-sqrt  {:macro #'scheme-tf-x-m-sqrt  :emit :macro :value true}
   :x-m-tan   {:macro #'scheme-tf-x-m-tan   :emit :macro :value true}
   :x-m-tanh  {:macro #'scheme-tf-x-m-tanh  :emit :macro :value true}
   :x-m-pow   {:macro #'scheme-tf-x-m-pow   :emit :macro :value true}})

;;
;; BIT
;;

(defn scheme-tf-x-bit-and
  [[_ x y]]
  (list 'bitwise-and x y))

(defn scheme-tf-x-bit-or
  [[_ x y]]
  (list 'bitwise-ior x y))

(defn scheme-tf-x-bit-xor
  [[_ x y]]
  (list 'bitwise-xor x y))

(defn scheme-tf-x-bit-lshift
  [[_ x y]]
  (list 'arithmetic-shift x y))

(defn scheme-tf-x-bit-rshift
  [[_ x y]]
  (list 'arithmetic-shift x (list '- y)))

(def +scheme-bit+
  {:x-bit-and    {:macro #'scheme-tf-x-bit-and    :emit :macro :value true}
   :x-bit-or     {:macro #'scheme-tf-x-bit-or     :emit :macro :value true}
   :x-bit-xor    {:macro #'scheme-tf-x-bit-xor    :emit :macro :value true}
   :x-bit-lshift {:macro #'scheme-tf-x-bit-lshift :emit :macro :value true}
   :x-bit-rshift {:macro #'scheme-tf-x-bit-rshift :emit :macro :value true}})

;;
;; JSON / RETURN
;;

(defn scheme-tf-x-json-encode
  [[_ value]]
  (list 'jsexpr->string (list 'xt-json-normalize value)))

(defn scheme-tf-x-json-decode
  [[_ expr]]
  (list 'string->jsexpr expr))

(defn scheme-tf-x-return-encode
  [[_ out id key]]
  (list 'xt-return-encode out id key))

(defn scheme-tf-x-return-wrap
  [[_ callback encode-fn]]
  (list encode-fn (list callback)))

(defn scheme-tf-x-return-eval
  [[_ expr wrap-fn]]
  (list wrap-fn (list 'lambda '() (list 'x:eval expr))))

(def +scheme-json+
  {:x-json-encode   {:macro #'scheme-tf-x-json-encode   :emit :macro :value true}
   :x-json-decode   {:macro #'scheme-tf-x-json-decode   :emit :macro :value true}
   :x-return-encode {:macro #'scheme-tf-x-return-encode :emit :macro :value true}
   :x-return-wrap   {:macro #'scheme-tf-x-return-wrap   :emit :macro :value true}
   :x-return-eval   {:macro #'scheme-tf-x-return-eval   :emit :macro :value true}})

;;
;; ITER
;;

(defn scheme-tf-x-iter-from-arr
  [[_ arr]]
  (list 'vector "__xt_iter__" arr (list 'box 0)))

(defn scheme-tf-x-iter-from-obj
  [[_ obj]]
  (list 'x:iter-from-arr (list 'x:obj-pairs obj)))

(defn scheme-tf-x-iter-from
  [[_ obj]]
  (list 'if
        (list 'x:iter-native? obj)
        obj
        (list 'x:iter-from-arr obj)))

(defn scheme-tf-x-iter-next
  [[_ it]]
  (list 'do
        (list 'var 'values (list 'vector-ref it 1))
        (list 'var 'index-box (list 'vector-ref it 2))
        (list 'var 'out "__xt_iter_end__")
        (list 'if
              (list '< (list 'unbox 'index-box)
                    (list 'vector-length 'values))
              (list 'do
                    (list 'var 'idx (list 'unbox 'index-box))
                    (list 'var 'value (list 'vector-ref 'values 'idx))
                    (list 'set-box! 'index-box (list '+ 'idx 1))
                    (list ':= 'out 'value))
              nil)
        'out))

(defn scheme-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (list 'do
        (list 'var 'result true)
        (list 'var 'done false)
        (list 'while (list 'not 'done)
              (list 'do
                    (list 'var 'x0 (list 'x:iter-next it0))
                    (list 'var 'x1 (list 'x:iter-next it1))
                    (list 'if
                          (list 'equal? 'x0 "__xt_iter_end__")
                          (list 'do
                                (list ':= 'result (list 'equal? 'x1 "__xt_iter_end__"))
                                (list ':= 'done true))
                          (list 'if
                                (list 'equal? 'x1 "__xt_iter_end__")
                                (list 'do
                                      (list ':= 'result false)
                                      (list ':= 'done true))
                                (list 'if
                                      (list 'not (list eq-fn 'x0 'x1))
                                      (list 'do
                                            (list ':= 'result false)
                                            (list ':= 'done true))
                                      nil)))))
        'result))

(defn scheme-tf-x-iter-null
  [_]
  '(vector "__xt_iter__" (vector) (box 0)))

(defn scheme-tf-x-iter-has?
  [[_ obj]]
  (list 'or
        (list 'vector? obj)
        (list 'x:iter-native? obj)))

(defn scheme-tf-x-iter-native?
  [[_ it]]
  (list 'and
        (list 'vector? it)
        (list '= 3 (list 'vector-length it))
        (list 'equal? "__xt_iter__" (list 'vector-ref it 0))))

(def +scheme-iter+
  {:x-iter-from-obj {:macro #'scheme-tf-x-iter-from-obj :emit :macro :value true}
   :x-iter-from-arr {:macro #'scheme-tf-x-iter-from-arr :emit :macro :value true}
   :x-iter-from     {:macro #'scheme-tf-x-iter-from     :emit :macro :value true}
   :x-iter-eq       {:macro #'scheme-tf-x-iter-eq       :emit :macro :value true}
   :x-iter-null     {:macro #'scheme-tf-x-iter-null     :emit :macro :value true}
   :x-iter-next     {:macro #'scheme-tf-x-iter-next     :emit :macro :value true}
   :x-iter-has?     {:macro #'scheme-tf-x-iter-has?     :emit :macro :value true}
   :x-iter-native?  {:macro #'scheme-tf-x-iter-native?  :emit :macro :value true}})

(defn scheme-tf-x-prototype-create
  [[_ m]]
  m)

(defn scheme-tf-x-prototype-get
  [[_ obj]]
  (list 'hash-ref obj "_xt_proto" false))

(defn scheme-tf-x-prototype-set
  [[_ obj prototype]]
  (list 'begin
        (list 'hash-set! obj "_xt_proto" prototype)
        obj))

(defn scheme-tf-x-prototype-method
  [[_ obj key]]
  (let [direct (gensym "d__")]
    (list 'let
          [direct (list 'hash-ref obj key nil)]
          (list 'if
                (scheme-truthy-check direct)
                direct
                (list 'hash-ref (list 'hash-ref obj "_xt_proto" nil) key nil)))))

(def +scheme-proto+
  {:prototype-create {:macro #'scheme-tf-x-prototype-create :emit :macro
                      :op-spec {:allow-blocks true}}
   :prototype-get    {:macro #'scheme-tf-x-prototype-get    :emit :macro}
   :prototype-set    {:macro #'scheme-tf-x-prototype-set    :emit :macro}
   :prototype-method {:macro #'scheme-tf-x-prototype-method :emit :macro}})

(defn scheme-tf-x-promise
  [[_ thunk]]
  (list 'xt-promise thunk))

(defn scheme-tf-x-promise-new
  [[_ thunk]]
  (list 'xt-promise-new thunk))

(defn scheme-tf-x-async-run
  [[_ thunk]]
  (template/$
   (thread ~thunk)))

(defn scheme-tf-x-promise-all
  [[_ promises]]
  (list 'xt-promise-all promises))

(defn scheme-tf-x-promise-then
  [[_ promise thunk]]
  (list 'xt-promise-then promise thunk))

(defn scheme-tf-x-promise-catch
  [[_ promise thunk]]
  (list 'xt-promise-catch promise thunk))

(defn scheme-tf-x-promise-finally
  [[_ promise thunk]]
  (list 'xt-promise-finally promise thunk))

(defn scheme-tf-x-promise-native?
  [[_ value]]
  (list 'xt-promise-native? value))

(defn scheme-tf-x-with-delay
  [[_ ms thunk]]
  (list 'xt-with-delay ms thunk))

(def +scheme-promise+
  {:x-async-run       {:macro #'scheme-tf-x-async-run       :emit :macro :value true}
   :x-promise         {:macro #'scheme-tf-x-promise         :emit :macro}
   :x-promise-new     {:macro #'scheme-tf-x-promise-new     :emit :macro}
   :x-promise-all     {:macro #'scheme-tf-x-promise-all     :emit :macro}
   :x-promise-then    {:macro #'scheme-tf-x-promise-then    :emit :macro}
   :x-promise-catch   {:macro #'scheme-tf-x-promise-catch   :emit :macro}
   :x-promise-finally {:macro #'scheme-tf-x-promise-finally :emit :macro}
   :x-promise-native? {:macro #'scheme-tf-x-promise-native? :emit :macro}
   :x-with-delay      {:macro #'scheme-tf-x-with-delay      :emit :macro :value true}})

(defn scheme-tf-x-socket-connect
  [[_ host port _opts cb]]
  (let [handler-pair (list (list 'lambda '(e) true)
                           (list 'lambda '(e)
                                 (list cb 'e 'null)))
        bindings (list (list (list 'in 'out)
                             (list 'tcp-connect host port)))
        let-body (list cb 'null (list 'vector 'in 'out))
        with-body (list 'let-values bindings let-body)]
    (list 'with-handlers (list handler-pair) with-body)))

(defn scheme-tf-x-socket-send
  [[_ conn s]]
  (list 'begin
        (list 'display s (list 'vector-ref conn 1))
        (list 'flush-output (list 'vector-ref conn 1))
        conn))

(defn scheme-tf-x-socket-close
  [[_ conn]]
  (list 'begin
        (list 'close-input-port (list 'vector-ref conn 0))
        (list 'close-output-port (list 'vector-ref conn 1))
        false))

(def +scheme-socket+
  {:x-socket-connect {:macro #'scheme-tf-x-socket-connect :emit :macro :value true}
   :x-socket-send    {:macro #'scheme-tf-x-socket-send    :emit :macro :value true}
   :x-socket-close   {:macro #'scheme-tf-x-socket-close   :emit :macro :value true}})

;;
;; HTTP
;;

(defn scheme-tf-x-notify-http
  [[_ host port value id key opts]]
  (list 'xt-notify-http host port value id key opts))

(def +scheme-http+
  {:x-notify-http {:macro #'scheme-tf-x-notify-http :emit :macro
                   :value/standalone true
                   :op-spec {:allow-blocks true}}})

;;
;; SHELL
;;

(defn scheme-tf-x-pwd
  [[_]]
  '(xt-pwd))

(defn scheme-tf-x-shell
  [[_ s root cb]]
  (list 'xt-shell s root cb))

(def +scheme-shell+
  {:x-pwd   {:macro #'scheme-tf-x-pwd   :emit :macro :value true}
   :x-shell {:macro #'scheme-tf-x-shell :emit :macro
             :op-spec {:allow-blocks true}}})

;;
;; FILE
;;

(defn scheme-tf-x-file-resolve
  [[_ root path]]
  (list 'xt-file-resolve root path))

(defn scheme-tf-x-file-slurp
  [[_ path cb]]
  (list 'xt-file-slurp path cb))

(defn scheme-tf-x-file-spit
  [[_ path content cb]]
  (list 'xt-file-spit path content cb))

(def +scheme-file+
  {:x-file-resolve {:macro #'scheme-tf-x-file-resolve :emit :macro :value true}
   :x-file-slurp   {:macro #'scheme-tf-x-file-slurp   :emit :macro
                    :op-spec {:allow-blocks true}}
   :x-file-spit    {:macro #'scheme-tf-x-file-spit    :emit :macro
                    :op-spec {:allow-blocks true}}})

(def +scheme+
  (merge +scheme-core+
         +scheme-global+
         +scheme-type+
         +scheme-lu+
         +scheme-object+
         +scheme-array+
         +scheme-string+
         +scheme-math+
         +scheme-bit+
         +scheme-json+
         +scheme-iter+
         +scheme-promise+
         +scheme-socket+
         +scheme-http+
         +scheme-shell+
         +scheme-file+))
