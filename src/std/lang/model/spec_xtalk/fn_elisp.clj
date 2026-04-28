(ns std.lang.model.spec-xtalk.fn-elisp)

(defn elisp-begin
  [forms]
  (if (= 1 (count forms))
    (first forms)
    (cons 'progn forms)))

(defn elisp-tf-x-print
  [[_ & args]]
  (elisp-begin
   (map (fn [arg]
          (list 'princ arg))
        args)))

(defn elisp-tf-x-len
  [[_ obj]]
  (list 'if
        (list 'hash-table-p obj)
        (list 'hash-table-count obj)
        (list 'length obj)))

(defn elisp-tf-x-cat
  [[_ & args]]
  (apply list 'concat args))

(defn elisp-tf-x-apply
  [[_ f args]]
  (list 'apply f args))

(defn elisp-tf-x-eval
  [[_ s]]
  (list 'eval s))

(defn elisp-tf-x-random
  [_]
  '(random))

(defn elisp-tf-x-now-ms
  [_]
  '(floor (* 1000 (float-time))))

(defn elisp-tf-x-type-native
  [[_ obj]]
  (list 'cond
        (list (list 'null obj)         "nil")
        (list (list 'eq obj t)         "boolean")
        (list (list 'stringp obj)      "string")
        (list (list 'numberp obj)      "number")
        (list (list 'functionp obj)    "function")
        (list (list 'vectorp obj)      "array")
        (list (list 'hash-table-p obj) "object")
        (list (list 'listp obj)        "list")
        (list (list 'symbolp obj)      "symbol")
        (list t                        "unknown")))

(def +elisp-core+
  {:x-print       {:macro #'elisp-tf-x-print       :emit :macro :value true}
   :x-len         {:macro #'elisp-tf-x-len         :emit :macro :value true}
   :x-cat         {:macro #'elisp-tf-x-cat         :emit :macro :value true}
   :x-apply       {:macro #'elisp-tf-x-apply       :emit :macro}
   :x-eval        {:macro #'elisp-tf-x-eval        :emit :macro}
   :x-random      {:macro #'elisp-tf-x-random      :emit :macro :value true}
   :x-now-ms      {:macro #'elisp-tf-x-now-ms      :emit :macro :value true}
   :x-type-native {:macro #'elisp-tf-x-type-native :emit :macro}})

(defn elisp-tf-x-get-key
  [[_ obj key default]]
  (if (some? default)
    (list 'gethash key obj default)
    (list 'gethash key obj)))

(defn elisp-tf-x-has-key?
  [[_ obj key check]]
  (let [missing '__xt_missing
        value   '__xt_value]
    (if (some? check)
      (list 'let
            (list (list missing (list 'make-symbol "__xt_missing"))
                  (list value   (list 'gethash key obj missing)))
            (list 'and
                  (list 'not (list 'eq value missing))
                  (list 'equal check value)))
      (list 'let
            (list (list missing (list 'make-symbol "__xt_missing"))
                  (list value   (list 'gethash key obj missing)))
            (list 'not (list 'eq value missing))))))

(defn elisp-tf-x-del-key
  [[_ obj key]]
  (list 'progn
        (list 'remhash key obj)
        obj))

(defn elisp-tf-x-obj-keys
  [[_ obj]]
  (list 'hash-table-keys obj))

(def +elisp-object+
  {:x-get-key   {:macro #'elisp-tf-x-get-key   :emit :macro :value true}
   :x-has-key?  {:macro #'elisp-tf-x-has-key?  :emit :macro :value true}
   :x-del-key   {:macro #'elisp-tf-x-del-key   :emit :macro}
   :x-obj-keys  {:macro #'elisp-tf-x-obj-keys  :emit :macro :value true}})

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

(defn elisp-tf-x-arr-push
  [[_ arr value]]
  (list 'vconcat arr (list 'vector value)))

(defn elisp-tf-x-arr-pop
  [[_ arr]]
  (list 'aref arr (list '- (list 'length arr) 1)))

(def +elisp-array+
  {:x-get-idx   {:macro #'elisp-tf-x-get-idx   :emit :macro :value true}
   :x-set-idx   {:macro #'elisp-tf-x-set-idx   :emit :macro}
   :x-arr-push  {:macro #'elisp-tf-x-arr-push  :emit :macro :value true}
   :x-arr-pop   {:macro #'elisp-tf-x-arr-pop   :emit :macro :value true}})

(defn elisp-tf-x-str-join
  [[_ sep coll]]
  (list 'mapconcat 'identity coll sep))

(defn elisp-tf-x-str-split
  [[_ s sep]]
  (list 'split-string s sep))

(defn elisp-tf-x-to-string
  [[_ x]]
  (list 'format "%s" x))

(defn elisp-tf-x-to-number
  [[_ x]]
  (list 'string-to-number x))

(def +elisp-string+
  {:x-str-join  {:macro #'elisp-tf-x-str-join  :emit :macro :value true}
   :x-str-split {:macro #'elisp-tf-x-str-split :emit :macro :value true}
   :x-to-string {:macro #'elisp-tf-x-to-string :emit :macro :value true}
   :x-to-number {:macro #'elisp-tf-x-to-number :emit :macro :value true}})

(defn elisp-tf-x-m-abs   [[_ n]] (list 'abs n))
(defn elisp-tf-x-m-max   [[_ & args]] (apply list 'max args))
(defn elisp-tf-x-m-min   [[_ & args]] (apply list 'min args))
(defn elisp-tf-x-m-floor [[_ n]] (list 'floor n))
(defn elisp-tf-x-m-ceil  [[_ n]] (list 'ceiling n))
(defn elisp-tf-x-m-sqrt  [[_ n]] (list 'sqrt n))
(defn elisp-tf-x-m-pow   [[_ b e]] (list 'expt b e))

(def +elisp-math+
  {:x-m-abs   {:macro #'elisp-tf-x-m-abs   :emit :macro :value true}
   :x-m-max   {:macro #'elisp-tf-x-m-max   :emit :macro :value true}
   :x-m-min   {:macro #'elisp-tf-x-m-min   :emit :macro :value true}
   :x-m-floor {:macro #'elisp-tf-x-m-floor :emit :macro :value true}
   :x-m-ceil  {:macro #'elisp-tf-x-m-ceil  :emit :macro :value true}
   :x-m-sqrt  {:macro #'elisp-tf-x-m-sqrt  :emit :macro :value true}
   :x-m-pow   {:macro #'elisp-tf-x-m-pow   :emit :macro :value true}})

(def +elisp+
  (merge +elisp-core+
         +elisp-object+
         +elisp-array+
         +elisp-string+
         +elisp-math+))
