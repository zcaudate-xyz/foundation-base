^{:no-test true}
(ns std.lang.model.spec-xtalk.fn-r
  (:require [std.lib :as h]
            [std.lang.base.grammar-xtalk :as default]))

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
   (h/$ (system ~s))))

(defn r-tf-x-random
  [_]
  '(runif 1))

(defn r-tf-x-type-native
  [[_ obj]]
  (h/$ (do (var t := (typeof ~obj))
           (cond (== t "list")
                 (if (not (is.null (names ~obj)))
                   (return "object")
                   (return "array"))

                 (== t "closure")
                 (return "function")

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
   :x-type-native    {:macro #'r-tf-x-type-native    :emit :macro}})

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

(defn r-tf-x-has-key?
  [[_ obj key check]]
  (if check
    (list '== check (list 'x:get-key obj key nil))
    (list :- (list '% key) "%in%" (list 'names obj))))

(defn r-tf-x-get-key
  [[_ obj key default]]
  (let [val (default/tf-get-key [_ obj key])]
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

(defn r-tf-x-is-boolean?
  [[_ e]]
  (list '== "logical" (list 'typeof e)))

(defn r-tf-x-is-function?
  [[_ e]]
  (list '== "closure" (list 'typeof e)))

(defn r-tf-x-is-object?
  [[_ e]]
  (h/$ (and (== "list" (typeof ~e))
            (not (is.null (names ~e))))))

(defn r-tf-x-is-array?
  [[_ e]]
  (h/$ (and (== "list" (typeof ~e))
            (is.null (names ~e)))))

(def +r-type+
  {:x-to-string      {:macro #'r-tf-x-to-string :emit :macro}
   :x-to-number      {:macro #'r-tf-x-to-number :emit :macro}
   :x-is-string?     {:macro #'r-tf-x-is-string? :emit :macro}
   :x-is-number?     {:macro #'r-tf-x-is-number? :emit :macro}
   :x-is-boolean?    {:macro #'r-tf-x-is-boolean? :emit :macro}
   :x-is-function?   {:macro #'r-tf-x-is-function? :emit :macro}
   :x-is-object?     {:macro #'r-tf-x-is-object? :emit :macro}
   :x-is-array?      {:macro #'r-tf-x-is-array? :emit :macro}})

;;
;; LU
;;

(defn r-tf-x-lu-eq
  "converts map to array"
  {:added "4.0"}
  ([[_ o1 o2]]
   (list '== (list 'tracemem o1) (list 'tracemem o2))))

(defn r-tf-x-lu-get
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (h/$ (. ~lu [(tracemem ~obj)]))))

(defn r-tf-x-lu-set
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj gid]]
   (h/$ (:= (. ~lu [(tracemem ~obj)]) ~gid))))

(defn r-tf-x-lu-del
  "converts map to array"
  {:added "4.0"}
  ([[_ lu obj]]
   (h/$ (:= (. ~lu [(tracemem ~obj)])
            nil))))

(def +r-lu+
  {:x-lu-eq          {:macro #'r-tf-x-lu-eq}
   :x-lu-get         {:macro #'r-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'r-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'r-tf-x-lu-del :emit :macro}})

;;
;; ARR
;;   

(defn r-tf-x-arr-push
  [[_ arr item]]
  (h/$ (:= (. ~arr [(+ (length ~arr) 1)])
            ~item)))

(defn r-tf-x-arr-pop
  [[_ arr]]
  (list := (list '. arr [(list 'length arr)])
        nil))

(defn r-tf-x-arr-push-first
  [[_ arr item]]
  (list := arr (list 'append [item] arr)))

(defn r-tf-x-arr-pop-first
  [[_ arr]]
  (list := (list '. arr [1]) nil))

(def +r-arr+
  {:x-arr-push        {:macro #'r-tf-x-arr-push    :emit :macro}
   :x-arr-pop         {:macro #'r-tf-x-arr-pop     :emit :macro}
   :x-arr-push-first  {:macro #'r-tf-x-arr-push-first :emit :macro}
   :x-arr-pop-first   {:macro #'r-tf-x-arr-pop-first  :emit :macro}
   :x-arr-insert      {:emit :throw}})

;;
;; STRING
;;

(defn r-tf-x-str-len
  ([[_ s]]
   (list 'nchar s)))

(defn r-tf-x-str-split
  ([[_ s tok]]
   (list 'unlist (list 'strsplit s tok))))

(defn r-tf-x-str-join
  ([[_ s arr]]
   (list 'paste arr :collapse s)))

(defn r-tf-x-str-index-of
  ([[_ s tok]]
   (list 'x:get-idx (list 'gregexpr :text s :pattern tok) 1)))

(defn r-tf-x-str-substring
  ([[_ s start & [end]]]
   (list 'substr s start (or end (list 'nchar s)))))

(defn r-tf-x-str-to-upper
  ([[_ s]]
   (list 'toupper s)))

(defn r-tf-x-str-to-lower
  ([[_ s]]
   (list 'tolower s)))

(defn r-tf-x-str-replace
  ([[_ s tok replacement]]
   (list 'gsub tok replacement s)))

(def +r-str+
  {:x-str-len        {:macro #'r-tf-x-str-len        :emit :macro}
   :x-str-split      {:macro #'r-tf-x-str-split      :emit :macro}
   :x-str-join       {:macro #'r-tf-x-str-join       :emit :macro}
   :x-str-index-of   {:macro #'r-tf-x-str-index-of   :emit :macro}
   :x-str-substring  {:macro #'r-tf-x-str-substring  :emit :macro}
   :x-str-to-upper      {:macro #'r-tf-x-str-to-upper      :emit :macro}
   :x-str-to-lower      {:macro #'r-tf-x-str-to-lower      :emit :macro}
   :x-str-replace    {:macro #'r-tf-x-str-replace    :emit :macro}})


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
   (h/$ (do* (library "jsonlite")
             (tryCatch (block (toJSON {:type "data"
                                       :value ~out
                                       :id ~id
                                       :key ~key}
                                      :auto-unbox true :null "null"))
                       :error (fn [err]
                                (toJSON {:type "raw"
                                         :value (toString out)
                                         :id ~id
                                         :key ~key}
                                        :auto-unbox true :null "null")))))))

(defn r-tf-x-return-wrap
  ([[_ f encode-fn]]
   (h/$ (do* (library "jsonlite")
             (tryCatch
              (block
               (:= out (~f))
               (~encode-fn out nil nil))
              :error   (fn [err]
                         (paste "{\"type\":\"error\",\"value\":"
                                (toJSON (toString err))
                                "}")))))))

(defn r-tf-x-return-eval
  ([[_ s wrap-fn]]
   (h/$ (return (~wrap-fn
                 (fn []
                   (eval (parse :text ~s))))))))

(def +r-return+
  {:x-return-encode  {:macro #'r-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'r-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'r-tf-x-return-eval     :emit :macro}})

(defn r-tf-x-socket-connect
  ([[_ host port opts]]
   (h/$ (do* (return (socketConnection :port ~port :blocking true))))))

(defn r-tf-x-socket-send
  ([[_ conn s]]
   (h/$ (writeLines ~s ~conn :sep "\n"))))

(defn r-tf-x-socket-close
  ([[_ conn]]
   (h/$ (close ~conn))))

(def +r-socket+
  {:x-socket-connect      {:macro #'r-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'r-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'r-tf-x-socket-close        :emit :macro}})

;;
;; ITER
;;

(def +r-iter+
  {})

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
