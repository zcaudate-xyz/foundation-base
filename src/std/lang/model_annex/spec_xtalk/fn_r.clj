^{:no-test true}
(ns std.lang.model-annex.spec-xtalk.fn-r
  (:require [std.lang.base.grammar-xtalk :as default]
            [std.lib.template :as template]))

;;
;; CORE
;;

(defn r-tf-x-del
  [[_ obj]]
  (list := obj nil))

(defn r-tf-x-cat
  [[_ & args]]
  (apply list 'paste (concat args [:sep ""])))

(defn r-tf-x-len
  [[_ arr]]
  (list 'length arr))

(defn r-tf-x-err
  [[_ msg]]
  (list 'stop msg))

(defn r-tf-x-eval
  [[_ s]]
  (list 'eval (list 'parse :text s)))

(defn r-tf-x-apply
  [[_ f args]]
  (list 'do.call f args))

(defn r-tf-x-print
  ([[_ & args]]
   (apply list 'print args)))

(defn r-tf-x-shell
  ([[_ s cm]]
   (template/$ (system ~s))))

(defn r-tf-x-random
  [_]
  '(runif 1))

(defn r-tf-x-type-native
  [[_ obj]]
  (template/$
   (do (var t := (typeof ~obj))
       (cond (is.null ~obj)
             (return "nil")

             (== t "list")
             (if (not (is.null (names ~obj)))
               (return "object")
               (return "array"))

             (== t "closure")
             (return "function")

             (and (is.atomic ~obj)
                  (not (is.null ~obj))
                  (not (== 1 (length ~obj))))
             (return "array")

             (== t "double")
             (return "number")

             (== t "character")
             (return "string")

             (== t "logical")
             (return "boolean")

             :else (return t)))))

(def +r-core+
  {:x-del            {:macro #'r-tf-x-del   :emit :macro}
   :x-cat            {:macro #'r-tf-x-cat   :emit :macro}
   :x-len            {:macro #'r-tf-x-len   :emit :macro}
   :x-err            {:macro #'r-tf-x-err     :emit :macro}
   :x-eval           {:macro #'r-tf-x-eval    :emit :macro}
   :x-random         {:macro #'r-tf-x-random  :emit :macro}
   :x-apply          {:macro #'r-tf-x-apply   :emit :macro}
   :x-print          {:macro #'r-tf-x-print         :emit :macro}
   :x-shell          {:macro #'r-tf-x-shell         :emit :macro}
   :x-now-ms         {:default '(floor (* 1000 (as.numeric (Sys.time)))) :emit :unit}
   :x-type-native    {:macro #'r-tf-x-type-native    :emit :macro}
   :x-unpack         {:emit :throw}})

;;
;; GLOBAL
;;

(defn r-tf-x-global-has?
  [[_ sym]]
  (list 'not (list 'x:nil? (list 'tryCatch (list 'get (str sym) :envir '.GlobalEnv)
                                 :error '(fn:> [e] nil)))))

(defn r-tf-x-global-set
  [[_ sym val]]
  (list 'assign (str sym) val :envir '.GlobalEnv))

(defn r-tf-x-global-del
  [[_ sym]]
  (list 'assign (str sym) nil :envir '.GlobalEnv))

(def +r-global+
  {:x-global-has?   {:macro #'r-tf-x-global-has?  :emit :macro}
   :x-global-set    {:macro #'r-tf-x-global-set   :emit :macro}
   :x-global-del    {:macro #'r-tf-x-global-del   :emit :macro}})

;;
;; CUSTOM
;;

(defn r-tf-x-not-nil?
  [[_ obj]]
  (list 'not (list 'is.null obj)))

(defn r-tf-x-nil?
  [[_ obj]]
  (list 'is.null obj))

(defn r-propagate-symbol
  [sym]
  (when (symbol? sym)
    (let [name (str sym)]
      (template/$
       (when (exists ~name
                         :envir (parent.env (environment))
                         :inherits true)
         (assign ~name
                 ~sym
                 :envir (parent.env (environment))))))))

(defn r-tf-x-has-key?
  [[_ obj key check]]
  (let [present (template/$
                 (:? (x:is-object? ~obj)
                     (not (is.na (match (as.character ~key)
                                        (names ~obj))))
                     (and (is.numeric ~key)
                          (>= ~key 1)
                          (<= ~key (length ~obj)))))]
    (if check
      (template/$
       (and ~present
            (== ~check (x:get-key ~obj ~key nil))))
      present)))

(defn r-tf-x-set-key
  [[_ obj key value]]
  (let [sync (r-propagate-symbol obj)]
    (if sync
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               ~value)
           ~sync
           ~obj))
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               ~value)
           ~obj)))))

(defn r-tf-x-del-key
  [[_ obj key]]
  (let [sync (r-propagate-symbol obj)]
    (if sync
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               nil)
           ~sync
           ~obj))
      (template/$
       (do (:= (. ~obj [(:? (x:is-object? ~obj)
                            (as.character ~key)
                            ~key)])
               nil)
           ~obj)))))

(defn r-tf-x-get-key
  [[_ obj key default]]
  (let [val (template/$
             (:? (x:is-object? ~obj)
                 (. ~obj [(as.character ~key)])
                 (:? (and (is.numeric ~key)
                          (>= ~key 1)
                          (<= ~key (length ~obj)))
                     (. ~obj [~key])
                     nil)))]
    (if default
      (list :?
            (list 'x:has-key? obj key)
            val
            default)
      val)))

(def +r-custom+
  {:x-not-nil?         {:macro #'r-tf-x-not-nil? :emit :macro}
   :x-nil?             {:macro #'r-tf-x-nil?  :emit :macro}
   :x-has-key?         {:macro #'r-tf-x-has-key? :emit :macro}
   :x-set-key          {:macro #'r-tf-x-set-key :emit :macro}
   :x-del-key          {:macro #'r-tf-x-del-key :emit :macro}
   :x-get-key          {:macro #'r-tf-x-get-key :emit :macro}})

;;
;; MATH
;;

(defn r-tf-x-m-abs   [[_ num]] (list 'abs num))
(defn r-tf-x-m-acos  [[_ num]] (list 'acos num))
(defn r-tf-x-m-asin  [[_ num]] (list 'asin num))
(defn r-tf-x-m-atan  [[_ num]] (list 'atan num))
(defn r-tf-x-m-ceil  [[_ num]] (list 'ceiling num))
(defn r-tf-x-m-cos   [[_ num]] (list 'cos num))
(defn r-tf-x-m-cosh  [[_ num]] (list 'cosh num))
(defn r-tf-x-m-exp   [[_ num]] (list 'exp num))
(defn r-tf-x-m-floor [[_ num]] (list 'floor num))
(defn r-tf-x-m-loge  [[_ num]] (list 'log num))
(defn r-tf-x-m-log10 [[_ num]] (list 'log10 num))
(defn r-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn r-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn r-tf-x-m-mod   [[_ num denom]] (list 'mod num denom))
(defn r-tf-x-m-quot  [[_ num denom]] (list 'floor
                                             (list '/ num denom)))
(defn r-tf-x-m-pow   [[_ base n]] (list 'pow base n))
(defn r-tf-x-m-sin   [[_ num]] (list 'sin num))
(defn r-tf-x-m-sinh  [[_ num]] (list 'sinh num))
(defn r-tf-x-m-sqrt  [[_ num]] (list 'sqrt num))
(defn r-tf-x-m-tan   [[_ num]] (list 'tan num))
(defn r-tf-x-m-tanh  [[_ num]] (list 'tanh num))

(def +r-math+
  {:x-m-abs           {:macro #'r-tf-x-m-abs,                 :emit :macro}
   :x-m-acos          {:macro #'r-tf-x-m-acos,                :emit :macro}
   :x-m-asin          {:macro #'r-tf-x-m-asin,                :emit :macro}
   :x-m-atan          {:macro #'r-tf-x-m-atan,                :emit :macro}
   :x-m-ceil          {:macro #'r-tf-x-m-ceil,                :emit :macro}
   :x-m-cos           {:macro #'r-tf-x-m-cos,                 :emit :macro}
   :x-m-cosh          {:macro #'r-tf-x-m-cosh,                :emit :macro}
   :x-m-exp           {:macro #'r-tf-x-m-exp,                 :emit :macro}
   :x-m-floor         {:macro #'r-tf-x-m-floor,               :emit :macro}
   :x-m-loge          {:macro #'r-tf-x-m-loge,                :emit :macro}
   :x-m-log10         {:macro #'r-tf-x-m-log10,               :emit :macro}
   :x-m-max           {:macro #'r-tf-x-m-max,                 :emit :macro}
   :x-m-min           {:macro #'r-tf-x-m-min,                 :emit :macro}
   :x-m-mod           {:macro #'r-tf-x-m-mod,                 :emit :macro}
   :x-m-pow           {:macro #'r-tf-x-m-pow,                 :emit :macro}
   :x-m-quot          {:macro #'r-tf-x-m-quot,                :emit :macro}
   :x-m-sin           {:macro #'r-tf-x-m-sin,                 :emit :macro}
   :x-m-sinh          {:macro #'r-tf-x-m-sinh,                :emit :macro}
   :x-m-sqrt          {:macro #'r-tf-x-m-sqrt,                :emit :macro}
   :x-m-tan           {:macro #'r-tf-x-m-tan,                 :emit :macro}
   :x-m-tanh          {:macro #'r-tf-x-m-tanh,                :emit :macro}})

;;
;; TYPE
;;

(defn r-tf-x-to-string
  [[_ e]]
  (list 'toString e))

(defn r-tf-x-to-number
  [[_ e]]
  (list 'as.numeric e))

(defn r-tf-x-is-string?
  [[_ e]]
  (list '== "character" (list 'typeof e)))

(defn r-tf-x-is-number?
  [[_ e]]
  (list 'is.numeric e))

(defn r-tf-x-is-integer?
  [[_ e]]
  (template/$ (and (is.numeric ~e)
                   (== 1 (length ~e))
                   (== ~e (floor ~e)))))

(defn r-tf-x-is-boolean?
  [[_ e]]
  (list '== "logical" (list 'typeof e)))

(defn r-tf-x-is-function?
  [[_ e]]
  (list '== "closure" (list 'typeof e)))

(defn r-tf-x-is-object?
  [[_ e]]
  (template/$ (and (== "list" (typeof ~e))
             (not (is.null (names ~e))))))

(defn r-tf-x-is-array?
  [[_ e]]
  (template/$
   (or (and (== "list" (typeof ~e))
            (is.null (names ~e)))
       (and (is.atomic ~e)
            (not (is.null ~e))
            (not (== 1 (length ~e)))))))

(def +r-type+
  {:x-to-string      {:macro #'r-tf-x-to-string :emit :macro}
   :x-to-number      {:macro #'r-tf-x-to-number :emit :macro}
   :x-is-string?     {:macro #'r-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'r-tf-x-is-number? :emit :macro}
   :x-is-integer?    {:macro #'r-tf-x-is-integer? :emit :macro}
   :x-is-boolean?    {:macro #'r-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'r-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'r-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'r-tf-x-is-array? :emit :macro}})

;;
;; LU
;;

(defn r-tf-x-lu-key
  [obj]
  (let [scalar? (template/$ (and (== 1 (length ~obj))
                                 (or (is.character ~obj)
                                     (is.numeric ~obj)
                                     (is.logical ~obj))))]
    (list ':?
          (list 'is.null obj)
          "__xt_nil__"
          (list ':?
                (list '== "environment" (list 'typeof obj))
                (list 'paste "env" (list 'format obj) :sep ":")
                (list ':?
                      scalar?
                      (list 'paste (list 'typeof obj)
                            (list 'as.character obj)
                            :sep ":")
                      (list 'tracemem obj))))))

(defn r-tf-x-lu-create
  "creates an environment-backed lookup table"
  {:added "4.1"}
  ([[_]]
   (list 'new.env :hash true :parent (list 'emptyenv))))

(defn r-tf-x-lu-eq
  "converts map to array"
  {:added "4.0"}
  ([[_ o1 o2]]
   (list '== (r-tf-x-lu-key o1) (r-tf-x-lu-key o2))))

(defn r-tf-x-lu-get
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj default]]
   (list 'get0 (r-tf-x-lu-key obj)
         :envir lu
         :inherits false
         :ifnotfound default)))

(defn r-tf-x-lu-set
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj gid]]
   (list 'assign (r-tf-x-lu-key obj)
         gid
         :envir lu)))

(defn r-tf-x-lu-del
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (let [key (r-tf-x-lu-key obj)]
     (list ':?
           (list 'exists key :envir lu :inherits false)
           (list 'rm :list key :envir lu :inherits false)
           nil))))

(def +r-lu+
  {:x-lu-create      {:macro #'r-tf-x-lu-create :emit :macro}
   :x-lu-eq          {:macro #'r-tf-x-lu-eq :emit :macro}
   :x-lu-get         {:macro #'r-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'r-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'r-tf-x-lu-del :emit :macro}})

;;
;; ARR
;;   

(defn r-tf-x-arr-push
  [[_ arr item]]
  (let [sync (r-propagate-symbol arr)]
    (if sync
      (template/$
       (do (:= ~arr
               (append (as.list ~arr)
                       (list ~item)))
           ~sync
           ~arr))
      (template/$
       (do (:= ~arr
               (append (as.list ~arr)
                       (list ~item)))
           ~arr)))))

(defn r-tf-x-arr-pop
  [[_ arr]]
  (let [sync (r-propagate-symbol arr)]
    (if sync
      (template/$
       (do (:= ~arr
               (head (as.list ~arr) -1))
           ~sync
           ~arr))
      (template/$
       (do (:= ~arr
               (head (as.list ~arr) -1))
           ~arr)))))

(defn r-tf-x-arr-push-first
  [[_ arr item]]
  (let [sync (r-propagate-symbol arr)]
    (if sync
      (template/$
       (do (:= ~arr
               (append (list ~item)
                       (as.list ~arr)))
           ~sync
           ~arr))
      (template/$
       (do (:= ~arr
               (append (list ~item)
                       (as.list ~arr)))
           ~arr)))))

(defn r-tf-x-arr-pop-first
  [[_ arr]]
  (let [sync (r-propagate-symbol arr)]
    (if sync
      (template/$
       (do (:= ~arr
               (tail (as.list ~arr) -1))
           ~sync
           ~arr))
      (template/$
       (do (:= ~arr
               (tail (as.list ~arr) -1))
           ~arr)))))

(defn r-tf-x-arr-insert
  [[_ arr idx item]]
  (let [sync (r-propagate-symbol arr)]
    (if sync
      (template/$
       (do (:= ~arr
               (append (as.list ~arr)
                       (list ~item)
                       :after (- ~idx 1)))
           ~sync
           ~arr))
      (template/$
       (do (:= ~arr
               (append (as.list ~arr)
                       (list ~item)
                       :after (- ~idx 1)))
           ~arr)))))

(def +r-arr+
  {:x-arr-push        {:macro #'r-tf-x-arr-push    :emit :macro}
   :x-arr-pop         {:macro #'r-tf-x-arr-pop     :emit :macro}
   :x-arr-push-first  {:macro #'r-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'r-tf-x-arr-pop-first  :emit :macro}
   :x-arr-insert      {:macro #'r-tf-x-arr-insert :emit :macro}})

;;
;; STRING
;;

(defn r-tf-x-str-len
  ([[_ s]]
   (list 'nchar s)))

(defn r-tf-x-str-split
  ([[_ s tok]]
   (list 'unlist (list 'strsplit s tok :fixed true))))

(defn r-tf-x-str-join
  ([[_ s arr]]
   (list 'paste arr :collapse s)))

(defn r-tf-x-str-index-of
  ([[_ s tok]]
   (list 'x:get-idx (list 'gregexpr :text s :pattern tok) 1)))

(defn r-tf-x-str-substring
  ([[_ s start & [end]]]
   (list 'substr s
         start
         (if end
           (list ':? (list 'x:nil? end)
                 (list 'nchar s)
                 end)
           (list 'nchar s)))))

(defn r-tf-x-str-char
  ([[_ s i]]
   (list '. (list 'utf8ToInt (list 'substr s i i)) [1])))

(defn r-tf-x-str-to-upper
  ([[_ s]]
   (list 'toupper s)))

(defn r-tf-x-str-to-lower
  ([[_ s]]
   (list 'tolower s)))

(defn r-tf-x-str-to-fixed
  ([[_ n digits]]
   (list 'sprintf (list 'paste0 "%." digits "f") n)))

(defn r-tf-x-str-replace
  ([[_ s tok replacement]]
   (list 'gsub tok replacement s)))

(defn r-tf-x-str-trim
  ([[_ s]]
   (list 'trimws s)))

(defn r-tf-x-str-trim-left
  ([[_ s]]
   (list 'trimws s :which "left")))

(defn r-tf-x-str-trim-right
  ([[_ s]]
   (list 'trimws s :which "right")))

(defn r-tf-x-str-comp
  [[_ a b]]
  (list '< a b))

(def +r-str+
  {:x-str-len        {:macro #'r-tf-x-str-len        :emit :macro}
   :x-str-split      {:macro #'r-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'r-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'r-tf-x-str-index-of   :emit :macro}
   :x-str-substring  {:macro #'r-tf-x-str-substring  :emit :macro}
   :x-str-char       {:macro #'r-tf-x-str-char       :emit :macro}
   :x-str-to-upper   {:macro #'r-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower   {:macro #'r-tf-x-str-to-lower   :emit :macro}
   :x-str-to-fixed   {:macro #'r-tf-x-str-to-fixed   :emit :macro}
   :x-str-replace    {:macro #'r-tf-x-str-replace    :emit :macro}
   :x-str-trim       {:macro #'r-tf-x-str-trim       :emit :macro}
   :x-str-trim-left  {:macro #'r-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right {:macro #'r-tf-x-str-trim-right :emit :macro}
   :x-str-comp       {:macro #'r-tf-x-str-comp       :emit :macro}})


;;
;; JSON
;;

(defn r-tf-x-json-encode
  ([[_ obj]]
   (list 'toJSON obj :auto-unbox true)))

(defn r-tf-x-json-decode
  ([[_ s]]
   (list 'fromJSON s)))

(def +r-js+
  {:x-json-encode      {:macro #'r-tf-x-json-encode      :emit :macro}
   :x-json-decode      {:macro #'r-tf-x-json-decode      :emit :macro}})



;;
;; COM
;;

(defn r-tf-x-return-encode
  ([[_ out id key]]
   (template/$
     (do* (library "jsonlite")
          (var outtype := (x:type-native ~out))
          (var payload := {:type "data"
                           :return outtype
                           :value ~out})
          (when (not (is.null ~id))
            (:= (. payload ["id"]) ~id))
          (when (not (is.null ~key))
            (:= (. payload ["key"]) ~key))
         (when (== "function" outtype)
           (:= (. payload ["type"]) "raw")
           (:= (. payload ["value"]) (toString ~out)))
         (tryCatch
          (block
            (return (toJSON payload :auto-unbox true :null "null")))
          :error (fn [err]
                   (do (:= (. payload ["type"]) "raw")
                       (:= (. payload ["value"]) (toString ~out))
                       (return (toJSON payload :auto-unbox true :null "null")))))))))

(defn r-tf-x-return-wrap
  ([[_ f encode-fn]]
   (template/$
     (do* (library "jsonlite")
          (tryCatch
           (block
             (var out := (~f))
             (~encode-fn out nil nil))
           :error (fn [err]
                    (toJSON {:type "error"
                             :return "error"
                            :value (toString err)}
                           :auto-unbox true :null "null")))))))

(defn r-tf-x-return-eval
  ([[_ s wrap-fn]]
   (template/$ (return (~wrap-fn
                 (fn []
                   (eval (parse :text ~s))))))))

(def +r-return+
  {:x-return-encode  {:macro #'r-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'r-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'r-tf-x-return-eval     :emit :macro}})

(defn r-tf-x-socket-connect
  ([[_ host port opts]]
   (template/$ (do* (return (socketConnection :port ~port :blocking true))))))

(defn r-tf-x-socket-send
  ([[_ conn s]]
   (template/$ (writeLines ~s ~conn :sep "\n"))))

(defn r-tf-x-socket-close
  ([[_ conn]]
   (template/$ (close ~conn))))

(def +r-socket+
  {:x-socket-connect      {:macro #'r-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'r-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'r-tf-x-socket-close        :emit :macro}})

;;
;; ITER
;;

(defn r-tf-x-iter-mark
  [value]
  (list 'structure value :class "xt_iterator"))

(defn r-tf-x-iter-from-obj
  [[_ obj]]
  (r-tf-x-iter-mark
   (template/$
    (lapply (names ~obj)
            (fn [k]
              (list k (. ~obj [k])))))))

(defn r-tf-x-iter-from-arr
  [[_ arr]]
  (r-tf-x-iter-mark arr))

(defn r-tf-x-iter-from
  [[_ obj]]
  (r-tf-x-iter-mark
   (template/$ (. ~obj ["iterator"]))))

(defn r-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (template/$
   (if (not (== (length ~it0) (length ~it1)))
     (return false)
     (do (for [i :in (seq_len (length ~it0))]
           (if (not (~eq-fn (. ~it0 [i])
                            (. ~it1 [i])))
             (return false)))
         (return true)))))

(defn r-tf-x-iter-next
  [[_ it]]
  (template/$
   (:? (== 0 (length ~it))
       nil
       (. ~it [1]))))

(defn r-tf-x-iter-has?
  [[_ obj]]
  (template/$
   (isTRUE
    (and (== "list" (typeof ~obj))
         (not (is.null (names ~obj)))
         (x:has-key? ~obj "iterator")))))

(defn r-tf-x-iter-native?
  [[_ it]]
  (list 'isTRUE (list 'inherits it "xt_iterator")))

(def +r-iter+
  {:x-iter-from-obj       {:macro #'r-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr       {:macro #'r-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from           {:macro #'r-tf-x-iter-from           :emit :macro}
   :x-iter-eq             {:macro #'r-tf-x-iter-eq             :emit :macro}
   :x-iter-null           {:default '(structure (list) :class "xt_iterator")
                           :emit :unit}
   :x-iter-next           {:macro #'r-tf-x-iter-next           :emit :macro}
   :x-iter-has?           {:macro #'r-tf-x-iter-has?           :emit :macro}
   :x-iter-native?        {:macro #'r-tf-x-iter-native?        :emit :macro}})

;;
;; THREAD
;;

(def +r-thread+
  {})

;;
;; PROMISE
;;

(def +r-promise+
  {})

(def +r+
  (merge +r-core+
         +r-global+
         +r-custom+
         +r-math+
         +r-type+
         +r-lu+
         +r-arr+
         +r-str+
         +r-js+
         +r-return+
         +r-socket+
         +r-iter+
         +r-thread+
         +r-promise+))


(comment

  
;;
;; OBJ
;;


(defn r-tf-x-obj-keys
  [[_ m]]
  (list 'names m))

(defn r-tf-x-obj-vals
  [[_ m]]
  (list 'c (list 'unlist  m)))

(def +r-obj+
  {:x-obj-keys       {:macro #'r-tf-x-obj-keys       :emit :macro}
   :x-obj-vals       {:macro #'r-tf-x-obj-vals       :emit :macro}})

;;
;; FN
;;

(def +r-fn+
  {})

(defn r-tf-x-arr-clone
  [[_ arr]]
  (list 'append [] arr))

(defn r-tf-x-arr-slice
  [[_ arr start end]]
  (list :% arr \[ (list :to (+ start 1) end) \]))

:x-arr-clone       {:macro #'r-tf-x-arr-clone   :emit :macro}
:x-arr-slice       {:macro #'r-tf-x-arr-slice   :emit :macro}

  
         
  )
