(ns std.lang.model-annex.spec-xtalk.fn-ruby
  (:require [std.lang.base.emit-common :as common]
            [std.lang.base.preprocess-base :as preprocess-base]
            [std.lib.template :as template]))

(declare ruby-tf-x-return-encode)

(def ^:private ruby-concurrent-promise-sym
  (symbol "Concurrent::Promise"))

(def ^:private ruby-concurrent-promise-new-sym
  (symbol "Concurrent::Promise.new"))

(def ^:private ruby-net-http-sym
  (symbol "Net::HTTP"))

(defn ruby-concurrent-promise-run
  [thunk]
  (list '. (list '. (list ruby-concurrent-promise-new-sym thunk)
                 (list 'execute))
        (list 'wait)))

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

(defn ruby-tf-x-ex-native?
  [[_ err]]
  (list '. err (list 'is_a? 'Exception)))

(defn ruby-tf-x-ex-new
  [[_ message & [data]]]
  (if (some? data)
    (template/$
     (. (fn []
          (var err (. RuntimeError (new ~message)))
          (. err (instance_variable_set "@xt_data" ~data))
          (return err))
        (call)))
    (list '. 'RuntimeError (list 'new message))))

(defn ruby-tf-x-ex-message
  [[_ err]]
  (template/$
   (:? (x:ex-native? ~err)
       (. ~err message)
       nil)))

(defn ruby-tf-x-ex-data
  [[_ err]]
  (template/$
   (:? (x:ex-native? ~err)
       (. ~err (instance_variable_get "@xt_data"))
       nil)))

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
      (list '. obj (list 'delete k)))
    (list ':= var nil)))

(defn ruby-tf-x-apply
  [[_ f args]]
  (list '. f (list 'call (list :.. args))))

(defn ruby-tf-x-type-native
  [[_ obj]]
  (let [current (gensym "__obj__")]
    (template/$
     (. (fn []
          (var ~current ~obj)
          (if (. ~current nil?)
            (return nil)
            (if (. ~current (is_a? Array))
              (return "array")
              (if (. ~current (is_a? Hash))
                (return "object")
                (if (. ~current (respond_to? :call))
                  (return "function")
                  (if (or (. ~current (is_a? TrueClass))
                          (. ~current (is_a? FalseClass)))
                    (return "boolean")
                    (if (. ~current (is_a? Numeric))
                      (return "number")
                      (if (. ~current (is_a? String))
                        (return "string")
                        (return (. (. (. ~current class) name) downcase))))))))))
        (call)))))

(defn ruby-tf-x-unpack
  [[_ arr]]
  (list :.. arr))

(def +ruby-core+
  {:x-cat            {:macro #'ruby-tf-x-cat  :emit :macro :value true}
   :x-len            {:macro #'ruby-tf-x-len  :emit :macro}
    :x-err            {:emit :alias :raw 'raise}
   :x-ex-native?     {:macro #'ruby-tf-x-ex-native? :emit :macro}
   :x-ex-new         {:macro #'ruby-tf-x-ex-new     :emit :macro}
   :x-ex-message     {:macro #'ruby-tf-x-ex-message :emit :macro}
   :x-ex-data        {:macro #'ruby-tf-x-ex-data    :emit :macro}
   :x-eval           {:emit :alias :raw 'eval}
   :x-print          {:macro #'ruby-tf-x-print :emit :macro :value true}
   :x-random         {:emit :alias :raw 'rand :value true}
   :x-now-ms         {:macro #'ruby-tf-x-now-ms :emit :macro}
   :x-del            {:macro #'ruby-tf-x-del   :emit :macro}
   :x-apply          {:macro #'ruby-tf-x-apply :emit :macro}
   :x-type-native    {:macro #'ruby-tf-x-type-native :emit :macro}
   :x-unpack         {:macro #'ruby-tf-x-unpack :emit :macro}})

;;
;; MATH
;;

(defn ruby-tf-x-m-abs   [[_ num]] (list '. num 'abs))
(defn ruby-tf-x-m-mod   [[_ num denom]] (list 'mod num denom))

(defn ruby-tf-x-m-max   [[_ & args]] (list '. (vec args) 'max))
(defn ruby-tf-x-m-min   [[_ & args]] (list '. (vec args) 'min))
(defn ruby-tf-x-m-pow   [[_ base exp]] (list 'pow base exp))
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
  (list '. e (list 'is_a? 'String)))

(defn ruby-tf-x-is-number?
  [[_ e]]
  (list '. e (list 'is_a? 'Numeric)))

(defn ruby-tf-x-is-integer?
  [[_ e]]
  (list '. e (list 'is_a? 'Integer)))

(defn ruby-tf-x-is-boolean?
  [[_ e]]
  (list 'or (list '== e 'true) (list '== e 'false)))

(defn ruby-tf-x-is-object?
  [[_ e]]
  (list '. e (list 'is_a? 'Hash)))

(defn ruby-tf-x-is-array?
  [[_ e]]
  (list '. e (list 'is_a? 'Array)))

(defn ruby-tf-x-is-function?
  [[_ e]]
  (list '. e (list 'respond_to? :call)))

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

(defn ruby-tf-x-arr-clone
  [[_ arr]]
  (list '. arr (list 'dup)))

(defn ruby-tf-x-arr-each
  [[_ arr f]]
  (let [idx   (gensym "idx__")
        total (gensym "total__")]
    (template/$
     (. (fn []
          (var ~total (. ~arr length))
          (var ~idx 0)
          (while (< ~idx ~total)
            (. ~f (call (. ~arr [~idx])))
            (:= ~idx (+ ~idx 1)))
          (return true))
        (call)))))

(defn ruby-tf-x-arr-every
  [[_ arr pred]]
  (let [idx   (gensym "idx__")
        total (gensym "total__")]
    (template/$
     (. (fn []
          (var ~total (. ~arr length))
          (var ~idx 0)
          (while (< ~idx ~total)
            (if (not (. ~pred (call (. ~arr [~idx]))))
              (return false))
            (:= ~idx (+ ~idx 1)))
          (return true))
        (call)))))

(defn ruby-tf-x-arr-sort
  [[_ arr key-fn comp-fn]]
  (let [tmp   (gensym "tmp__")
        total (gensym "total__")
        i     (gensym "i__")
        j     (gensym "j__")
        left  (gensym "left__")
        right (gensym "right__")]
    (template/$
     (. (fn []
          (var ~total (. ~arr length))
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
          (return ~arr))
        (call)))))

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
   :x-arr-clone       {:macro #'ruby-tf-x-arr-clone      :emit :macro}
   :x-arr-each        {:macro #'ruby-tf-x-arr-each       :emit :macro}
   :x-arr-every       {:macro #'ruby-tf-x-arr-every      :emit :macro}
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
    (let [idx (gensym "idx__")]
      (template/$
       (. (fn []
            (var ~idx (. ~s (index ~tok)))
            (return (:? (. ~idx nil?) -1 ~idx)))
          (call))))))

(defn ruby-tf-x-str-substring
  ([[_ s start & args]]
    (if (empty? args)
      (list '. s (list 'slice start (list '. s 'length)))
      (let [stop (first args)]
        (list '. s (list 'slice start (list '- stop start)))))))

(defn ruby-tf-x-str-to-upper
  ([[_ s]]
   (list '. s (list 'upcase))))

(defn ruby-tf-x-str-to-lower
  ([[_ s]]
   (list '. s (list 'downcase))))

(defn ruby-tf-x-str-char
  ([[_ s i]]
   (list '. (list '. s [i]) 'ord)))

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
   (list 'sprintf (list '+ "%." (list '. digits 'to_s) "f") n)))

(defn ruby-tf-x-str-starts-with
  ([[_ s prefix]]
   (list '. s (list 'start_with? prefix))))

(defn ruby-tf-x-str-ends-with
  ([[_ s suffix]]
   (list '. s (list 'end_with? suffix))))

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
  {})

(defn- ruby-tf-x-lu-key
  [k]
  (template/$
   (. (fn []
        (if (or (. ~k nil?)
                (. ~k (is_a? Numeric))
                (. ~k (is_a? String))
                (. ~k (is_a? Symbol))
                (. ~k (is_a? TrueClass))
                (. ~k (is_a? FalseClass)))
          (return ~k)
          (return (. ~k object_id))))
      (call))))

(defn ruby-tf-x-lu-eq
  [[_ a b]]
  (list '== (list '. a 'object_id)
        (list '. b 'object_id)))

(defn ruby-tf-x-lu-get
  [[_ h k default]]
  (let [key-id (ruby-tf-x-lu-key k)]
    (if default
      (list :? (list '. h (list 'key? key-id))
            (list '. h [key-id])
            default)
      (list '. h [key-id]))))

(defn ruby-tf-x-lu-set
  [[_ h k v]]
  (list ':= (list '. h [(ruby-tf-x-lu-key k)]) v))

(defn ruby-tf-x-lu-del
  [[_ h k]]
  (list '. h (list 'delete (ruby-tf-x-lu-key k))))

(defn ruby-tf-x-obj-clone
  [[_ obj]]
  (template/$
   (. (fn []
        (if (or (. ~obj (is_a? Hash))
                (. ~obj (is_a? Array)))
          (return (. Marshal (load (. Marshal (dump ~obj)))))
          (return ~obj)))
      (call))))

(defn ruby-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list 'and
          (list '. obj (list 'key? key))
          (list '== (list '. obj [key]) check))
    (list '. obj (list 'key? key))))

(def +ruby-lu+
  {:x-lu-create        {:macro #'ruby-tf-x-lu-create      :emit :macro}
   :x-lu-eq            {:macro #'ruby-tf-x-lu-eq          :emit :macro}
   :x-lu-get           {:macro #'ruby-tf-x-lu-get         :emit :macro}
   :x-lu-set           {:macro #'ruby-tf-x-lu-set         :emit :macro}
   :x-lu-del           {:macro #'ruby-tf-x-lu-del         :emit :macro}
   :x-has-key?         {:macro #'ruby-tf-x-has-key?       :emit :macro}
   :x-obj-clone        {:macro #'ruby-tf-x-obj-clone      :emit :macro}})

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

(defn ruby-tf-x-pwd
  [[_]]
  (list 'or
        (list '. 'ENV ["PWD"])
        (list '. 'Dir 'pwd)))

(defn ruby-tf-x-file-resolve
  [[_ root path]]
  (list '. 'File (list 'expand_path path root)))

(defn ruby-tf-x-file-slurp
  [[_ path & args]]
  (case (count args)
    1
    (let [[cb] args]
      (template/$
       (try
         (. ~cb (call nil (. File (read ~path))))
         (catch e
           (. ~cb (call e nil))))))
    2
    (let [[success-fn error-fn] args]
      (template/$
       (try
         (. ~success-fn (call (. File (read ~path))))
         (catch e
           (. ~error-fn (call e))))))
    (let [[_opts success-fn error-fn] args]
      (template/$
       (try
         (. ~success-fn (call (. File (read ~path))))
         (catch e
           (. ~error-fn (call e))))))))

(defn ruby-tf-x-file-spit
  [[_ path content & args]]
  (case (count args)
    1
    (let [[cb] args]
      (template/$
       (try
         (. File (write ~path ~content))
         (. ~cb (call nil ~path))
         (catch e
           (. ~cb (call e nil))))))
    2
    (let [[success-fn error-fn] args]
      (template/$
       (try
         (. File (write ~path ~content))
         (. ~success-fn (call ~path))
         (catch e
           (. ~error-fn (call e))))))
    (let [[_opts success-fn error-fn] args]
      (template/$
       (try
         (. File (write ~path ~content))
         (. ~success-fn (call ~path))
         (catch e
           (. ~error-fn (call e))))))))

(defn ruby-tf-x-shell
  [[_ s & args]]
  (case (count args)
    0
    (list (symbol "`") s)
    2
    (let [[root cb] args
          current-root (gensym "root__")
          result (gensym "result__")
          stdout (gensym "stdout__")
          stderr (gensym "stderr__")
          status (gensym "status__")]
      (template/$
       (do (require "open3")
           (var ~current-root ~root)
           (try
             (var ~result (if (or (. ~current-root nil?) (== ~current-root ""))
                             (. Open3 (capture3 "sh" "-lc" ~s))
                             (:- "Open3.capture3(\"sh\", \"-lc\", " ~s ", chdir: " ~current-root ")")))
             (var ~stdout (. ~result [0]))
             (var ~stderr (. ~result [1]))
             (var ~status (. ~result [2]))
             (if (. ~status success?)
                (. ~cb (call nil ~stdout))
               (. ~cb (call {:code (. ~status exitstatus)
                             :err ~stderr
                             :out ~stdout}
                            nil)))
             (return ["async"])
              (catch e
                (. ~cb (call e nil))
                (return ["async"]))))))
    (let [[root success-fn error-fn] args
          current-root (gensym "root__")
          result (gensym "result__")
          stdout (gensym "stdout__")
          stderr (gensym "stderr__")
          status (gensym "status__")]
      (template/$
       (do (require "open3")
           (var ~current-root ~root)
           (try
             (var ~result (if (or (. ~current-root nil?) (== ~current-root ""))
                             (. Open3 (capture3 "sh" "-lc" ~s))
                             (:- "Open3.capture3(\"sh\", \"-lc\", " ~s ", chdir: " ~current-root ")")))
             (var ~stdout (. ~result [0]))
             (var ~stderr (. ~result [1]))
             (var ~status (. ~result [2]))
             (if (. ~status success?)
                (. ~success-fn (call ~stdout))
               (. ~error-fn (call {:code (. ~status exitstatus)
                                   :err ~stderr
                                   :out ~stdout})))
             (return ["async"])
             (catch e
               (. ~error-fn (call e))
               (return ["async"]))))))))

(def +ruby-file+
  {:x-file-resolve    {:macro #'ruby-tf-x-file-resolve   :emit :macro}
   :x-file-slurp      {:macro #'ruby-tf-x-file-slurp     :emit :macro
                       :op-spec {:allow-blocks true}}
   :x-file-spit       {:macro #'ruby-tf-x-file-spit      :emit :macro
                       :op-spec {:allow-blocks true}}})

(def +ruby-shell+
  {:x-pwd             {:macro #'ruby-tf-x-pwd            :emit :macro}
   :x-shell           {:macro #'ruby-tf-x-shell          :emit :macro
                       :op-spec {:allow-blocks true}}})

(defn ruby-tf-x-promise
  [[_ thunk]]
  (let [promise (gensym "promise__")]
    (list '.
          (list 'fn []
                (list 'var promise {"__type__" "xt.promise"
                                    "value" nil
                                    "reason" nil})
                (list 'try
                      (list ':= (list '. promise ["value"])
                            (list '. thunk (list 'call)))
                      (list 'catch 'e
                            (list ':= (list '. promise ["reason"]) 'e)))
                (list 'return promise))
          (list 'call))))

(defn ruby-tf-x-promise-all
  [[_ promises]]
  (let [items   (gensym "items__")
        total   (gensym "total__")
        idx     (gensym "idx__")
        current (gensym "current__")
        out     (gensym "out__")]
    (template/$
     (. (fn [~items]
          (var ~total (. ~items length))
          (var ~idx 0)
          (var ~out [])
          (while (< ~idx ~total)
            (var ~current (. ~items [~idx]))
            (if (not (x:promise-native? ~current))
              (:= ~current (x:promise (fn [] (return ~current)))))
            (if (not (. (. ~current ["reason"]) nil?))
              (return ~current))
            (. ~out (push (. ~current ["value"])))
            (:= ~idx (+ ~idx 1)))
          (return (x:promise (fn [] (return ~out)))))
         (call ~promises)))))

(defn ruby-tf-x-promise-then
  [[_ promise thunk]]
  (let [current     (gensym "current__")
        next-thunk  (list 'fn []
                          (list '. thunk
                                (list 'call
                                      (list '. current ["value"]))))
        next-promise (list 'x:promise next-thunk)]
    (list '.
          (list 'fn []
                (list 'var current promise)
                (list 'if (list 'not (list '. (list '. current ["reason"]) 'nil?))
                      (list 'return current)
                      (list 'return next-promise)))
          (list 'call))))

(defn ruby-tf-x-promise-catch
  [[_ promise thunk]]
  (let [current      (gensym "current__")
        next-thunk   (list 'fn []
                           (list '. thunk
                                 (list 'call
                                       (list '. current ["reason"]))))
        next-promise (list 'x:promise next-thunk)]
    (list '.
          (list 'fn []
                (list 'var current promise)
                (list 'if (list '. (list '. current ["reason"]) 'nil?)
                      (list 'return current)
                      (list 'return next-promise)))
          (list 'call))))

(defn ruby-tf-x-promise-finally
  [[_ promise thunk]]
  (let [current (gensym "current__")
        cleanup (gensym "cleanup__")
        cleanup-thunk (list 'fn []
                            (list '. thunk 'call))
        cleanup-promise (list 'x:promise cleanup-thunk)]
    (list '.
          (list 'fn []
                (list 'var current promise)
                (list 'var cleanup cleanup-promise)
                (list 'if (list 'not (list '. (list '. cleanup ["reason"]) 'nil?))
                      (list 'return cleanup)
                      (list 'return current)))
          (list 'call))))

(defn ruby-tf-x-promise-native?
  [[_ value]]
  (template/$
   (and (== "object" (x:type-native ~value))
        (== "xt.promise" (. ~value ["__type__"])))))

(defn ruby-tf-x-with-delay
  [[_ ms thunk]]
  (template/$
   (x:promise
    (fn []
      (sleep (/ ~ms 1000.0))
      (. ~thunk (call))))))

(def +ruby-promise+
  {:x-promise          {:macro #'ruby-tf-x-promise         :emit :macro}
   :x-promise-all      {:macro #'ruby-tf-x-promise-all     :emit :macro}
   :x-promise-then     {:macro #'ruby-tf-x-promise-then    :emit :macro}
   :x-promise-catch    {:macro #'ruby-tf-x-promise-catch   :emit :macro}
   :x-promise-finally  {:macro #'ruby-tf-x-promise-finally :emit :macro}
   :x-promise-native?  {:macro #'ruby-tf-x-promise-native? :emit :macro}
   :x-with-delay       {:macro #'ruby-tf-x-with-delay      :emit :macro}})

;; ITER
;;

(defn ruby-tf-x-iter-from-obj
  [[_ obj]]
  (list '. (list '. obj 'to_a) 'each))

(defn ruby-tf-x-iter-from-arr
  [[_ arr]]
  (list '. arr 'each))

(defn ruby-tf-x-iter-from
  [[_ obj]]
  (list '. obj 'each))

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
  (list '. [] 'each))

(defn ruby-tf-x-iter-next
  [[_ it]]
  (list '. it 'next))

(defn ruby-tf-x-iter-has?
  [[_ obj]]
  (list 'or
        (list '. obj (list 'is_a? 'Array))
        (list '. obj (list 'is_a? 'Enumerator))))

(defn ruby-tf-x-iter-native?
  [[_ it]]
  (list '. it (list 'is_a? 'Enumerator)))

(defn ruby-tf-x-iter-generator
  [[_ thunk]]
  (let [grammar  preprocess-base/*macro-grammar*
        mopts    preprocess-base/*macro-opts*
        iterator (gensym "__iter__")
        thunk-str (common/emit-wrapping thunk grammar mopts)]
    (list ':-
          (str "Enumerator.new do |"
               (common/*emit-fn* iterator grammar mopts)
               "|\n  "
               thunk-str
               ".call("
               (common/*emit-fn* iterator grammar mopts)
               ")\nend"))))

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
  [[_ host port _opts cb]]
  (template/$
   (do (require "socket")
       (try
         (var conn (. TCPSocket (new ~host ~port)))
         (. ~cb (call nil conn))
         (catch e
           (. ~cb (call e nil)))))))

(defn ruby-tf-x-socket-send
  [[_ conn value]]
  (list '. conn (list 'puts value)))

(defn ruby-tf-x-socket-close
  [[_ conn]]
  (list '. conn (list 'close)))

(def +ruby-network+
  {:x-socket-connect   {:macro #'ruby-tf-x-socket-connect   :emit :macro
                        :op-spec {:allow-blocks true}}
   :x-socket-send      {:macro #'ruby-tf-x-socket-send      :emit :macro}
   :x-socket-close     {:macro #'ruby-tf-x-socket-close     :emit :macro}})

(defn ruby-tf-x-notify-http
  [[_ host port value id key opts]]
  (let [out-type (ruby-tf-x-type-native `[_ ~value])
        http-sym ruby-net-http-sym
        path     (gensym "path__")
        payload  (gensym "payload__")
        path-val (gensym "path_value__")]
    (template/$
     (do (require "json")
         (require "net/http")
         (try
           (var ~path "/")
           (if (not (. ~opts nil?))
             (do (var ~path-val (. ~opts ["path"]))
                 (if (not (. ~path-val nil?))
                   (:= ~path ~path-val))))
           (var ~payload nil)
           (try
             (:= ~payload (JSON.generate {:id ~id
                                          :key ~key
                                          :type "data"
                                          :return ~out-type
                                          :value ~value}))
             (catch e
               (:= ~payload (JSON.generate {:id ~id
                                            :key ~key
                                            :type "raw"
                                            :return "raw"
                                            :value (. e to_s)}))))
           (. (. ~http-sym (new ~host ~port)) (post ~path ~payload))
           (return ["async"])
           (catch e
             (return ["unable to connect" (. e to_s)])))))))

(def +ruby-http+
  {:x-notify-http      {:macro #'ruby-tf-x-notify-http      :emit :macro
                        :op-spec {:allow-blocks true}}})

;;
;; RETURN
;;

(defn ruby-tf-x-return-encode
  ([[_ out id key]]
    (let [out-type (ruby-tf-x-type-native `[_ ~out])
          payload  (gensym "payload__")
          error    (gensym "error__")]
       (template/$ (do (require "json")
                       (var ~payload {:type "data"
                                      :return ~out-type
                                      :value ~out})
                       (if (not (. ~id nil?))
                         (:= (. ~payload ["id"]) ~id))
                       (if (not (. ~key nil?))
                         (:= (. ~payload ["key"]) ~key))
                       (var ~error {:type "raw"
                                    :return "raw"
                                    :value nil})
                       (if (not (. ~id nil?))
                         (:= (. ~error ["id"]) ~id))
                       (if (not (. ~key nil?))
                         (:= (. ~error ["key"]) ~key))
                       (try
                         (return (JSON.generate ~payload))
                         (catch e
                           (:= (. ~error ["value"]) (. e to_s))
                           (return (JSON.generate ~error)))))))))

(defn ruby-tf-x-return-wrap
  ([[_ f encode-fn]]
     (if (and (symbol? encode-fn)
              (= "return-encode" (name encode-fn)))
       (template/$ (do (require "json")
                       (try
                         (:= out (if (. ~f (is_a? Proc))
                                   (. ~f (call))
                                   ~f))
                          (catch e
                              (return (JSON.generate {:type "error"
                                                      :value (. e to_s)}))))
                        (return (~encode-fn out nil nil))))
        (template/$ (do (require "json")
                        (try
                         (:= out (if (. ~f (is_a? Proc))
                                   (. ~f (call))
                                   ~f))
                          (catch e
                              (return (JSON.generate {:type "error"
                                                      :value (. e to_s)}))))
                        (var encoded nil)
                        (if (== 1 (. ~encode-fn arity))
                          (:= encoded (. ~encode-fn (call out)))
                        (:= encoded (. ~encode-fn (call out nil nil))))
                      (return encoded))))))

(defn ruby-tf-x-return-eval
  ([[_ s wrap-fn]]
    (if (and (symbol? wrap-fn)
             (= "return-wrap" (name wrap-fn)))
      (template/$ (do (require "json")
                      (try
                        (:= out (eval ~s))
                        (return (~wrap-fn out))
                        (catch e
                            (return (JSON.generate {:type "error"
                                                    :value (. e to_s)}))))))
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
          +ruby-shell+
          +ruby-file+
          +ruby-promise+
          +ruby-iter+
          +ruby-network+
          +ruby-http+
          +ruby-return+))
