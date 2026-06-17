(ns hara.model.spec-xtalk.fn-gdscript
  (:require [std.lib.template :as template]))

;;
;; CORE
;;

(defn gdscript-tf-x-del
  [[_ obj]]
  (list := obj nil))

(defn gdscript-tf-x-del-key
  [[_ obj key]]
  (list '. obj (list 'erase key)))

(defn gdscript-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn gdscript-tf-x-len
  [[_ arr]]
  (list '. arr '(size)))

(defn gdscript-tf-x-get-key
  [[_ obj key default]]
  (let [val (list '. obj (list 'get key))]
    (if default
      (list 'or val default)
      val)))

(defn gdscript-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list '== check (list 'x:get-key obj key nil))
    (list 'not= nil (list '. obj (list 'get key)))))

(defn gdscript-tf-x-err
  [[_ msg]]
  (list 'push_error msg))

(defn gdscript-tf-x-ex-native?
  [[_ err]]
  (list 'is_instance_of err 'Error))

(defn gdscript-tf-x-ex-new
  [[_ message & [data]]]
  (if (some? data)
    (template/$ (Error.new ~message ~data))
    (template/$ (Error.new ~message))))

(defn gdscript-tf-x-ex-message
  [[_ err]]
  (template/$ (:? (x:ex-native? ~err)
                  (. ~err ["message"])
                  nil)))

(defn gdscript-tf-x-ex-data
  [[_ err]]
  (template/$ (:? (x:ex-native? ~err)
                  (. ~err ["data"])
                  nil)))

(defn gdscript-tf-x-eval
  [[_ s]]
  ;; GDScript has no direct eval; return the string as a marker.
  (str "__XT_EVAL__(" s ")"))

(defn gdscript-tf-x-apply
  [[_ f args]]
  (list f (list :* args)))

(defn gdscript-tf-x-random
  [_]
  '(. (RandomNumberGenerator) (randf)))

(defn gdscript-tf-x-print
  ([[_ & args]]
   (apply list 'print args)))

(defn gdscript-tf-x-type-native
  [[_ obj]]
  (template/$
   (cond (== null ~obj)
         (return "null")

         (is_instance_of ~obj 'Array)
         (return "array")

         (is_instance_of ~obj 'Dictionary)
         (return "object")

         (== TYPE_BOOL (typeof ~obj))
         (return "boolean")

         (== TYPE_CALLABLE (typeof ~obj))
         (return "function")

         (or (== TYPE_INT (typeof ~obj))
             (== TYPE_FLOAT (typeof ~obj)))
         (return "number")

         (== TYPE_STRING (typeof ~obj))
         (return "string")

         :else
         (return "object"))))

(def +gdscript-core+
  {:x-del            {:macro #'gdscript-tf-x-del    :emit :macro}
   :x-del-key        {:macro #'gdscript-tf-x-del-key :emit :macro}
   :x-cat            {:macro #'gdscript-tf-x-cat    :emit :macro}
   :x-len            {:macro #'gdscript-tf-x-len    :emit :macro}
   :x-err            {:macro #'gdscript-tf-x-err     :emit :macro}
   :x-ex-native?     {:macro #'gdscript-tf-x-ex-native? :emit :macro}
   :x-ex-new         {:macro #'gdscript-tf-x-ex-new     :emit :macro}
   :x-ex-message     {:macro #'gdscript-tf-x-ex-message :emit :macro}
   :x-ex-data        {:macro #'gdscript-tf-x-ex-data    :emit :macro}
   :x-eval           {:macro #'gdscript-tf-x-eval    :emit :macro}
   :x-apply          {:macro #'gdscript-tf-x-apply   :emit :macro}
   :x-unpack         {:raw :*  :emit :alias}
   :x-random         {:macro #'gdscript-tf-x-random  :emit :macro}
   :x-print          {:macro #'gdscript-tf-x-print         :emit :macro}
   :x-now-ms         {:default '(* 1000 (. (Time) (get_time_dict_from_system)["unix"])) :emit :unit}
   :x-type-native    {:macro #'gdscript-tf-x-type-native   :emit :macro}})

;;
;; GLOBAL
;;

(defn gdscript-tf-x-global-has?
  [[_ sym]]
  (list 'not= (symbol sym) nil))

(defn gdscript-tf-x-global-set
  [[_ sym val]]
  (list ':= (symbol sym) val))

(defn gdscript-tf-x-global-del
  [[_ sym]]
  (list ':= (symbol sym) nil))

(def +gdscript-global+
  {:x-global-has?   {:macro #'gdscript-tf-x-global-has?  :emit :macro}
   :x-global-set    {:macro #'gdscript-tf-x-global-set   :emit :macro}
   :x-global-del    {:macro #'gdscript-tf-x-global-del   :emit :macro}})

;;
;; CUSTOM
;;

(def +gdscript-custom+
  {:x-get-key        {:macro #'gdscript-tf-x-get-key}
   :x-has-key?       {:macro #'gdscript-tf-x-has-key? :emit :macro}})

;;
;; MATH
;;

(defn gdscript-tf-x-m-abs   [[_ num]] (list 'abs num))
(defn gdscript-tf-x-m-acos  [[_ num]] (list '. (list 'sqrt -1) 'acos)) ;; placeholder
(defn gdscript-tf-x-m-asin  [[_ num]] (list '. (list 'sqrt -1) 'asin))
(defn gdscript-tf-x-m-atan  [[_ num]] (list '. (list 'sqrt -1) 'atan))
(defn gdscript-tf-x-m-ceil  [[_ num]] (list 'ceil num))
(defn gdscript-tf-x-m-cos   [[_ num]] (list 'cos num))
(defn gdscript-tf-x-m-cosh  [[_ num]] (list 'cosh num))
(defn gdscript-tf-x-m-exp   [[_ num]] (list 'exp num))
(defn gdscript-tf-x-m-floor [[_ num]] (list 'floor num))
(defn gdscript-tf-x-m-loge  [[_ num]] (list 'log num))
(defn gdscript-tf-x-m-log10 [[_ num]] (list '. (list 'log num) '/ (list 'log 10)))
(defn gdscript-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn gdscript-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn gdscript-tf-x-m-mod   [[_ num denom]] (list 'fposmod num denom))
(defn gdscript-tf-x-m-pow   [[_ num denom]] (list 'pow num denom))
(defn gdscript-tf-x-m-quot  [[_ num denom]] (list 'floor (list '/ num denom)))
(defn gdscript-tf-x-m-sin   [[_ num]] (list 'sin num))
(defn gdscript-tf-x-m-sinh  [[_ num]] (list 'sinh num))
(defn gdscript-tf-x-m-sqrt  [[_ num]] (list 'sqrt num))
(defn gdscript-tf-x-m-tan   [[_ num]] (list 'tan num))
(defn gdscript-tf-x-m-tanh  [[_ num]] (list 'tanh num))

(def +gdscript-math+
  {:x-m-abs           {:macro #'gdscript-tf-x-m-abs   :emit :macro}
   :x-m-acos          {:macro #'gdscript-tf-x-m-acos  :emit :macro}
   :x-m-asin          {:macro #'gdscript-tf-x-m-asin  :emit :macro}
   :x-m-atan          {:macro #'gdscript-tf-x-m-atan  :emit :macro}
   :x-m-ceil          {:macro #'gdscript-tf-x-m-ceil  :emit :macro}
   :x-m-cos           {:macro #'gdscript-tf-x-m-cos   :emit :macro}
   :x-m-cosh          {:macro #'gdscript-tf-x-m-cosh  :emit :macro}
   :x-m-exp           {:macro #'gdscript-tf-x-m-exp   :emit :macro}
   :x-m-floor         {:macro #'gdscript-tf-x-m-floor :emit :macro}
   :x-m-loge          {:macro #'gdscript-tf-x-m-loge  :emit :macro}
   :x-m-log10         {:macro #'gdscript-tf-x-m-log10 :emit :macro}
   :x-m-max           {:macro #'gdscript-tf-x-m-max   :emit :macro}
   :x-m-min           {:macro #'gdscript-tf-x-m-min   :emit :macro}
   :x-m-mod           {:macro #'gdscript-tf-x-m-mod   :emit :macro}
   :x-m-pow           {:macro #'gdscript-tf-x-m-pow   :emit :macro}
   :x-m-quot          {:macro #'gdscript-tf-x-m-quot  :emit :macro}
   :x-m-sin           {:macro #'gdscript-tf-x-m-sin   :emit :macro}
   :x-m-sinh          {:macro #'gdscript-tf-x-m-sinh  :emit :macro}
   :x-m-sqrt          {:macro #'gdscript-tf-x-m-sqrt  :emit :macro}
   :x-m-tan           {:macro #'gdscript-tf-x-m-tan   :emit :macro}
   :x-m-tanh          {:macro #'gdscript-tf-x-m-tanh  :emit :macro}})

;;
;; ARRAY
;;

(defn gdscript-tf-x-arr-push
  [[_ arr e]]
  (list '. arr (list 'append e)))

(defn gdscript-tf-x-arr-pop
  [[_ arr]]
  (list '. arr '(pop_back)))

(defn gdscript-tf-x-arr-slice
  [[_ arr start end]]
  (list '. arr (list 'slice start end)))

(def +gdscript-array+
  {:x-arr-push        {:macro #'gdscript-tf-x-arr-push :emit :macro}
   :x-arr-pop         {:macro #'gdscript-tf-x-arr-pop  :emit :macro}
   :x-arr-slice       {:macro #'gdscript-tf-x-arr-slice :emit :macro}})

;;
;; STRING
;;

(defn gdscript-tf-x-str-split
  [[_ s sep]]
  (list '. s (list 'split sep)))

(defn gdscript-tf-x-str-replace
  [[_ s old new]]
  (list '. s (list 'replace old new)))

(defn gdscript-tf-x-str-trim
  [[_ s]]
  (list '. s (list 'strip_edges)))

(defn gdscript-tf-x-str-to-lower
  [[_ s]]
  (list '. s (list 'to_lower)))

(defn gdscript-tf-x-str-to-upper
  [[_ s]]
  (list '. s (list 'to_upper)))

(defn gdscript-tf-x-str-join
  [[_ arr sep]]
  (list '. sep (list 'join arr)))

(def +gdscript-string+
  {:x-str-split       {:macro #'gdscript-tf-x-str-split    :emit :macro}
   :x-str-replace     {:macro #'gdscript-tf-x-str-replace  :emit :macro}
   :x-str-trim        {:macro #'gdscript-tf-x-str-trim     :emit :macro}
   :x-str-to-lower    {:macro #'gdscript-tf-x-str-to-lower :emit :macro}
   :x-str-to-upper    {:macro #'gdscript-tf-x-str-to-upper :emit :macro}
   :x-str-join        {:macro #'gdscript-tf-x-str-join     :emit :macro}})

;;
;; AGGREGATE
;;

(def +gdscript+
  (merge +gdscript-core+
         +gdscript-global+
         +gdscript-custom+
         +gdscript-math+
         +gdscript-array+
         +gdscript-string+))
