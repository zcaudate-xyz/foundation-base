(ns std.lang.model-annex.spec-xtalk.fn-ruby
  (:require [std.lib.template :as template]))

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
  (list '= var nil))

(defn ruby-tf-x-apply
  [[_ f args]]
  (list '. f (list 'call (list '* args))))

(defn ruby-tf-x-shell
  [[_ s opts]]
  (list (symbol "`") s))

(defn ruby-tf-x-type-native
  [[_ obj]]
  (list '. obj 'class))

(defn ruby-tf-x-unpack
  [[_ arr]]
  (list '* arr))

(def +ruby-core+
  {:x-cat            {:macro #'ruby-tf-x-cat  :emit :macro :value true
                       :raw "(lambda { |*args| args.join('') })"}
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
   :x-unpack         {:macro #'ruby-tf-x-unpack :emit :macro}
   :x-client-basic   {:emit :alias :raw 'client-basic}})

;;
;; MATH
;;

(defn ruby-tf-x-m-mod   [[_ num denom]] (list '% num denom))

(defn ruby-tf-x-m-max   [[_ & args]] (list '. (vec args) 'max))
(defn ruby-tf-x-m-min   [[_ & args]] (list '. (vec args) 'min))
(defn ruby-tf-x-m-pow   [[_ base exp]] (list '** base exp))
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
  {:x-m-abs           {:emit :alias :raw 'abs  :value true}
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
  (list '. e (list 'is_a? 'Object)))

(defn ruby-tf-x-is-array?
  [[_ e]]
  (list '. e (list 'is_a? 'Array)))

(defn ruby-tf-x-is-function?
  [[_ e]]
  (list '. e (list 'respond_to? ':call)))

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
  (list '. arr (list 'sort!))) ;; simplified

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
     (list '. s (list 'slice start))
     (list '. s (list 'slice start (first args))))))

(defn ruby-tf-x-str-to-upper
  ([[_ s]]
   (list '. s (list 'upcase))))

(defn ruby-tf-x-str-to-lower
  ([[_ s]]
   (list '. s (list 'downcase))))

(defn ruby-tf-x-str-char
  ([[_ s i]]
   (list 'ord (list '. s (list 'slice i 1)))))

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
   :x-str-format      {:macro #'ruby-tf-x-str-format     :emit :macro}
   :x-str-to-fixed    {:macro #'ruby-tf-x-str-to-fixed   :emit :macro}
   :x-str-starts-with {:macro #'ruby-tf-x-str-starts-with :emit :macro}
   :x-str-ends-with   {:macro #'ruby-tf-x-str-ends-with  :emit :macro}})

;;
;; LOOKUP
;;

(defn ruby-tf-x-lu-create
  [[_ & args]]
  '{})

(defn ruby-tf-x-lu-get
  [[_ h k default]]
  (if default
    (list '. h (list 'fetch k default))
    (list '. h (list '[] k))))

(defn ruby-tf-x-lu-set
  [[_ h k v]]
  (list '= (list '. h (list '[] k)) v))

(defn ruby-tf-x-lu-del
  [[_ h k]]
  (list '. h (list 'delete k)))

(defn ruby-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list '== check (list 'x:get-key obj key nil))
    (list '. obj (list 'has_key? key))))

(def +ruby-lu+
  {:x-lu-create        {:macro #'ruby-tf-x-lu-create      :emit :macro}
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

(def +ruby-b64+
  {:x-b64-encode      {:macro #'ruby-tf-x-b64-encode    :emit :macro}
   :x-b64-decode      {:macro #'ruby-tf-x-b64-decode    :emit :macro}})

;;
;; FILE
;;

(defn ruby-tf-x-slurp
  [[_ path]]
  (list '. 'File (list 'read path)))

(defn ruby-tf-x-spit
  [[_ path content]]
  (list '. 'File (list 'write path content)))

(def +ruby-file+
  {:x-slurp           {:macro #'ruby-tf-x-slurp          :emit :macro}
   :x-spit            {:macro #'ruby-tf-x-spit           :emit :macro}})

;;
;; URI
;;

(defn ruby-tf-x-uri-encode
  [[_ s]]
  (list '. 'URI (list 'encode s)))

(defn ruby-tf-x-uri-decode
  [[_ s]]
  (list '. 'URI (list 'decode s)))

(def +ruby-uri+
  {:x-uri-encode      {:macro #'ruby-tf-x-uri-encode    :emit :macro}
   :x-uri-decode      {:macro #'ruby-tf-x-uri-decode    :emit :macro}})

;;
;; PROTO
;;

(defn ruby-tf-x-proto-get
  [[_ obj]]
  (list '. obj 'class))

(defn ruby-tf-x-proto-set
  [[_ obj prototype]]
  obj)  ;; ignore, cannot set prototype in Ruby

(defn ruby-tf-x-proto-create
  [[_ prototype]]
  '{})

(defn ruby-tf-x-proto-tostring
  [[_ obj]]
  (list '. obj 'to_s))

(def +ruby-proto+
  {:x-proto-get       {:macro #'ruby-tf-x-proto-get     :emit :macro}
   :x-proto-set       {:macro #'ruby-tf-x-proto-set     :emit :macro}
   :x-proto-create    {:macro #'ruby-tf-x-proto-create  :emit :macro}
    :x-proto-tostring  {:macro #'ruby-tf-x-proto-tostring :emit :macro}})

;;
;; THREAD
;;

(defn ruby-tf-x-thread-spawn
  [[_ thunk]]
  (list 'Thread.new (list 'fn [] (list '. thunk 'call))))

(defn ruby-tf-x-thread-join
  [[_ thread]]
  (list '. thread 'join))

(defn ruby-tf-x-with-delay
  [[_ thunk ms]]
  (list 'do
        (list 'sleep (list '/ ms 1000.0))
        (list '. thunk 'call)))

(defn ruby-tf-x-start-interval
  [[_ thunk ms]]
  (list 'Thread.new
        (list 'fn []
              (list 'while 'true
                    (list 'do
                          (list '. thunk 'call)
                          (list 'sleep (list '/ ms 1000.0)))))))

(defn ruby-tf-x-stop-interval
  [[_ instance]]
  (list '. instance 'kill))

(def +ruby-thread+
  {:x-thread-spawn    {:macro #'ruby-tf-x-thread-spawn   :emit :macro}
   :x-thread-join     {:macro #'ruby-tf-x-thread-join    :emit :macro}
   :x-with-delay      {:macro #'ruby-tf-x-with-delay     :emit :macro}
   :x-start-interval  {:macro #'ruby-tf-x-start-interval :emit :macro}
   :x-stop-interval   {:macro #'ruby-tf-x-stop-interval  :emit :macro}})

;; ITER
;;

(defn ruby-tf-x-iter-from-obj
  [[_ obj]]
  (list '. obj 'to_a))

(defn ruby-tf-x-iter-from-arr
  [[_ arr]]
  arr)

(defn ruby-tf-x-iter-from
  [[_ obj]]
  obj)

(defn ruby-tf-x-iter-eq
  [[_ it0 it1 eq-fn]]
  (template/$
   (if (!= (. ~it0 length) (. ~it1 length))
     (return false)
     (do (for [i (range 0 (. ~it0 length))]
           (if (not (~eq-fn (:% ~it0 [i])
                            (:% ~it1 [i])))
             (return false)))
         (return true)))))

(defn ruby-tf-x-iter-next
  [[_ it]]
  (list '. it 'shift))

(defn ruby-tf-x-iter-has?
  [[_ obj]]
  (list 'and
        (list '. obj 'is_a? 'Array)
        (list '> (list '. obj 'length) 0)))

(defn ruby-tf-x-iter-native?
  [[_ it]]
  (list '. it 'is_a? 'Array))

(def +ruby-iter+
  {:x-iter-from-obj       {:macro #'ruby-tf-x-iter-from-obj       :emit :macro}
   :x-iter-from-arr       {:macro #'ruby-tf-x-iter-from-arr       :emit :macro}
   :x-iter-from           {:macro #'ruby-tf-x-iter-from           :emit :macro}
   :x-iter-eq             {:macro #'ruby-tf-x-iter-eq             :emit :macro}
   :x-iter-null           {:default [] :emit :unit}
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

(defn ruby-tf-x-notify-socket
  [[_ message]]
  (vector "async" message))

(defn ruby-tf-x-ws-connect
  [[_ url & [opts]]]
  (ruby-tf-x-socket-connect [nil url nil opts]))

(defn ruby-tf-x-ws-send
  [[_ conn value]]
  (ruby-tf-x-socket-send [nil conn value]))

(defn ruby-tf-x-ws-close
  [[_ conn]]
  (ruby-tf-x-socket-close [nil conn]))

(def +ruby-network+
  {:x-socket-connect   {:macro #'ruby-tf-x-socket-connect   :emit :macro}
   :x-socket-send      {:macro #'ruby-tf-x-socket-send      :emit :macro}
   :x-socket-close     {:macro #'ruby-tf-x-socket-close     :emit :macro}
   :x-notify-socket    {:macro #'ruby-tf-x-notify-socket    :emit :macro}
   :x-ws-connect       {:macro #'ruby-tf-x-ws-connect       :emit :macro}
   :x-ws-send          {:macro #'ruby-tf-x-ws-send          :emit :macro}
   :x-ws-close         {:macro #'ruby-tf-x-ws-close         :emit :macro}
   :x-client-ws        {:emit :alias :raw 'client-ws}
   :x-server-basic     {:emit :alias :raw 'server-basic}
   :x-server-ws        {:emit :alias :raw 'server-ws}})

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
                                      :value  ~out}))
              (catch e
                  (return (JSON.generate {:id ~id
                                          :key ~key
                                          :type  "raw"
                                          :value (. e (to_s))}))))))))

(defn ruby-tf-x-return-wrap
  ([[_ f encode-fn]]
   (template/$ (do (:- "require 'json'")
            (try
              (:= out (. ~f (call)))
              (catch e
                (return (JSON.generate {:type "error"
                                        :value (. e (to_s))}))))
            (return (~encode-fn out nil nil))))))

(defn ruby-tf-x-return-eval
  ([[_ s wrap-fn]]
   (template/$ (return (~wrap-fn (fn []
                            (return (eval ~s))))))))

(def +ruby-return+
  {:x-return-encode  {:macro #'ruby-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'ruby-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'ruby-tf-x-return-eval     :emit :macro}})

(def +ruby+
  (merge +ruby-core+
         +ruby-math+
         +ruby-type+
         +ruby-arr+
         +ruby-str+
         +ruby-lu+
         +ruby-json+
         +ruby-b64+
         +ruby-file+
           +ruby-uri+
            +ruby-proto+
            +ruby-thread+
            +ruby-iter+
          +ruby-network+
          +ruby-return+))
