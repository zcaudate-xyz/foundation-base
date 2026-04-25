(ns std.lang.model.spec-xtalk.fn-dart
  (:require [std.lib.collection :as collection]
            [std.lib.template :as template]))

(defn dart-method0
  [obj method]
  ;; Wrap the receiver so negative literals emit as `(-1.2).ceil()`
  ;; rather than `-1.2.ceil()`, which Dart parses with unary-minus precedence.
  (list :% (list :- "(") obj (list :- ").") method (list :- "()")))

(defn dart-runtime-type-string
  [obj]
  (dart-method0 (list '. obj 'runtimeType) 'toString))

(defn dart-tf-x-len
  [[_ arr]]
  (list '. arr 'length))

(defn dart-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn dart-tf-x-print
  [[_ & args]]
  (let [value (if (= 1 (count args))
                (first args)
                (apply list '+ args))]
    (template/$
     ((fn []
        (print ~value)
        (return nil))))))

(defn dart-tf-x-arr-push
  [[_ arr item]]
  (list '. arr (list 'add item)))

(defn dart-tf-x-apply
  [[_ f args]]
  (list 'Function.apply f args))

(defn dart-tf-x-now-ms
  [_]
  (list '. (list 'DateTime.now) 'millisecondsSinceEpoch))

(defn dart-tf-x-random
  [_]
  (dart-method0 (list 'math.Random) 'nextDouble))

(defn dart-tf-x-type-native
  [[_ obj]]
  (let [rtype (gensym "rtype")
        sval  (gensym "sval")
        rtype-expr (dart-runtime-type-string obj)
        sval-expr  (dart-method0 obj 'toString)]
    (template/$
     (do (if (== ~obj nil)
           (return nil))
         (var ~rtype ~rtype-expr)
         (var ~sval ~sval-expr)
         (if (== "String" ~rtype)
           (return "string"))
         (if (or (== "int" ~rtype)
                 (== "double" ~rtype)
                 (== "num" ~rtype))
           (return "number"))
         (if (== "bool" ~rtype)
           (return "boolean"))
         (if (or (. ~rtype (contains "Function"))
                 (. ~rtype (contains "=>"))
                 (. ~sval (startsWith "Closure")))
           (return "function"))
         (if (or (== "List" ~rtype)
                 (. ~rtype (contains "List")))
           (return "array"))
         (if (or (== "Map" ~rtype)
                 (. ~rtype (contains "Map")))
           (return "object"))
         (return (. ~rtype (toLowerCase)))))))

(defn dart-tf-x-del
  [[_ obj]]
  (if (and (seq? obj)
           (= '. (first obj))
           (vector? (nth obj 2 nil))
           (= 1 (count (nth obj 2))))
    (let [target (second obj)
          [key]  (nth obj 2)]
      (list '. target (list 'remove key)))
    (list '. obj (list 'remove nil))))

(defn dart-tf-x-eval
  [[_ s]]
  (list 'throw '"eval not supported in Dart"))

(defn dart-tf-x-has-key?
  [[_ obj key check]]
  (if (some? check)
    (list 'and
          (list '. obj (list 'containsKey key))
          (list '== check (list '. obj [key])))
    (list '. obj (list 'containsKey key))))

(defn dart-tf-x-del-key
  [[_ obj key]]
  (list '. obj (list 'remove key)))

(defn dart-tf-x-shell
  [[_ s opts cb]]
  (template/$
   (do (:- "import 'dart:io';")
       (return (. (Process.run "sh" ["-lc" ~s])
                  (then (fn [result]
                          (if (not= 0 (. result exitCode))
                            (return (~cb {:code (. result exitCode)
                                          :stderr (. result stderr)
                                          :output (. result stdout)}
                                         nil))
                            (return (~cb nil (. result stdout))))))
                  (catchError (fn [err]
                                (return (~cb err nil)))))))))

(def +dart-core+
  {:x-print    {:macro #'dart-tf-x-print    :emit :macro :value true}
   :x-len      {:macro #'dart-tf-x-len      :emit :macro :value true}
   :x-cat      {:macro #'dart-tf-x-cat      :emit :macro :value true}
   :x-apply    {:macro #'dart-tf-x-apply    :emit :macro}
   :x-err      {:emit :alias :raw 'throw}
   :x-now-ms   {:macro #'dart-tf-x-now-ms   :emit :macro}
   :x-random   {:macro #'dart-tf-x-random   :emit :macro}
   :x-type-native {:macro #'dart-tf-x-type-native :emit :macro}
   :x-del      {:macro #'dart-tf-x-del      :emit :macro}
   :x-eval     {:macro #'dart-tf-x-eval     :emit :macro}
   :x-has-key? {:macro #'dart-tf-x-has-key? :emit :macro}
   :x-del-key  {:macro #'dart-tf-x-del-key  :emit :macro}
   :x-unpack   {:emit :alias :raw '...}
   :x-shell    {:macro #'dart-tf-x-shell    :emit :macro}})

(defn dart-tf-x-m-abs [[_ x]] (dart-method0 x 'abs))
(defn dart-tf-x-m-ceil [[_ x]] (dart-method0 x 'ceil))
(defn dart-tf-x-m-floor [[_ x]] (dart-method0 x 'floor))
(defn dart-tf-x-m-sin [[_ x]] (list 'math.sin x))
(defn dart-tf-x-m-cos [[_ x]] (list 'math.cos x))
(defn dart-tf-x-m-tan [[_ x]] (list 'math.tan x))
(defn dart-tf-x-m-asin [[_ x]] (list 'math.asin x))
(defn dart-tf-x-m-acos [[_ x]] (list 'math.acos x))
(defn dart-tf-x-m-atan [[_ x]] (list 'math.atan x))
(defn dart-tf-x-m-sqrt [[_ x]] (list 'math.sqrt x))
(defn dart-tf-x-m-exp [[_ x]] (list 'math.exp x))
(defn dart-tf-x-m-loge [[_ x]] (list 'math.log x))
(defn dart-tf-x-m-log10 [[_ x]] (list '/ (list 'math.log x) 'math.ln10))
(defn dart-tf-x-m-max [[_ & args]] (reduce (fn [acc arg] (list 'math.max acc arg)) args))
(defn dart-tf-x-m-min [[_ & args]] (reduce (fn [acc arg] (list 'math.min acc arg)) args))
(defn dart-tf-x-m-mod [[_ a b]] (list :% a (list :- " % ") b))
(defn dart-tf-x-m-pow [[_ a b]] (list 'math.pow a b))
(defn dart-tf-x-m-quot [[_ a b]] (list :% a (list :- " ~/ ") b))
(defn dart-tf-x-m-cosh [[_ x]]
  (list '/ (list '+ (list 'math.exp x)
                 (list 'math.exp (list '- x)))
        2))
(defn dart-tf-x-m-sinh [[_ x]]
  (list '/ (list '- (list 'math.exp x)
                 (list 'math.exp (list '- x)))
        2))
(defn dart-tf-x-m-tanh [[_ x]]
  (list '/ (list '- (list 'math.exp (list '* 2 x))
                 1)
        (list '+ (list 'math.exp (list '* 2 x))
              1)))

(defn dart-tf-x-to-string [[_ x]]
  (dart-method0 x 'toString))
(defn dart-tf-x-to-number [[_ x]] (list 'num.parse x))
(defn dart-tf-x-is-string? [[_ x]] (list '== "String" (dart-runtime-type-string x)))
(defn dart-tf-x-is-number? [[_ x]]
  (let [rtype-expr (dart-runtime-type-string x)]
    (list 'or
          (list '== "int" rtype-expr)
          (list '== "double" rtype-expr)
          (list '== "num" rtype-expr))))
(defn dart-tf-x-is-integer? [[_ x]]
  (let [rtype-expr (dart-runtime-type-string x)]
    (list '== "int" rtype-expr)))
(defn dart-tf-x-is-boolean? [[_ x]] (list '== "bool" (dart-runtime-type-string x)))
(defn dart-tf-x-is-function? [[_ x]]
  (let [rtype-expr (dart-runtime-type-string x)
        sval-expr  (dart-method0 x 'toString)]
    (list 'or
          (list '. rtype-expr (list 'contains "Function"))
          (list '. rtype-expr (list 'contains "=>"))
          (list '. sval-expr (list 'startsWith "Closure")))))
(defn dart-tf-x-is-object? [[_ x]]
  (let [rtype-expr (dart-runtime-type-string x)]
    (list 'or
          (list '== "Map" rtype-expr)
          (list '. rtype-expr (list 'startsWith "_Map"))
          (list '. rtype-expr (list 'startsWith "LinkedMap")))))
(defn dart-tf-x-is-array? [[_ x]]
  (let [rtype-expr (dart-runtime-type-string x)]
    (list 'or
          (list '. rtype-expr (list 'startsWith "List"))
          (list '. rtype-expr (list 'startsWith "_GrowableList")))))

(def +dart-type+
  {:x-to-string    {:macro #'dart-tf-x-to-string    :emit :macro}
   :x-to-number    {:macro #'dart-tf-x-to-number    :emit :macro}
   :x-is-string?   {:macro #'dart-tf-x-is-string?   :emit :macro}
   :x-is-number?   {:macro #'dart-tf-x-is-number?   :emit :macro}
   :x-is-integer?  {:macro #'dart-tf-x-is-integer?  :emit :macro}
   :x-is-boolean?  {:macro #'dart-tf-x-is-boolean?  :emit :macro}
   :x-is-function? {:macro #'dart-tf-x-is-function? :emit :macro}
   :x-is-object?   {:macro #'dart-tf-x-is-object?   :emit :macro}
   :x-is-array?    {:macro #'dart-tf-x-is-array?    :emit :macro}})

(defn dart-tf-x-str-char [[_ s i]]
  (list '. s (list 'codeUnitAt (list '- i (list 'x:offset)))))
(defn dart-tf-x-str-split [[_ s sep]] (list '. s (list 'split sep)))
(defn dart-tf-x-str-join [[_ sep arr]] (list '. arr (list 'join sep)))
(defn dart-tf-x-str-index-of [[_ s sub & [start]]]
  (list '+ (list '. s (list 'indexOf sub (or start 0)))
        (list 'x:offset)))
(defn dart-tf-x-str-last-index-of [[_ s sub]] (list '. s (list 'lastIndexOf sub)))
(defn dart-tf-x-str-substring
  [[_ s start & [end]]]
  (if end
    (list '. s (list 'substring (list '- start (list 'x:offset)) end))
    (list '. s (list 'substring (list '- start (list 'x:offset))))))
(defn dart-tf-x-str-to-upper [[_ s]] (dart-method0 s 'toUpperCase))
(defn dart-tf-x-str-to-lower [[_ s]] (dart-method0 s 'toLowerCase))
(defn dart-tf-x-str-to-fixed [[_ s n]] (list '. s (list 'toStringAsFixed n)))
(defn dart-tf-x-str-replace [[_ s pattern replacement]] (list '. s (list 'replaceAll pattern replacement)))
(defn dart-tf-x-str-trim [[_ s]] (dart-method0 s 'trim))
(defn dart-tf-x-str-trim-left [[_ s]] (list '. s (list 'trimLeft)))
(defn dart-tf-x-str-trim-right [[_ s]] (list '. s (list 'trimRight)))
(defn dart-tf-x-str-starts-with? [[_ s prefix]] (list '. s (list 'startsWith prefix)))
(defn dart-tf-x-str-ends-with? [[_ s suffix]] (list '. s (list 'endsWith suffix)))
(defn dart-tf-x-str-includes? [[_ s sub]] (list '. s (list 'contains sub)))

(def +dart-str+
  {:x-str-char        {:macro #'dart-tf-x-str-char       :emit :macro}
   :x-str-split       {:macro #'dart-tf-x-str-split      :emit :macro}
   :x-str-join        {:macro #'dart-tf-x-str-join       :emit :macro}
   :x-str-index-of    {:macro #'dart-tf-x-str-index-of   :emit :macro}
   :x-str-substring   {:macro #'dart-tf-x-str-substring  :emit :macro}
   :x-str-to-upper    {:macro #'dart-tf-x-str-to-upper   :emit :macro}
   :x-str-to-lower    {:macro #'dart-tf-x-str-to-lower   :emit :macro}
   :x-str-to-fixed    {:macro #'dart-tf-x-str-to-fixed   :emit :macro}
   :x-str-replace     {:macro #'dart-tf-x-str-replace    :emit :macro}
   :x-str-trim        {:macro #'dart-tf-x-str-trim       :emit :macro}
   :x-str-trim-left   {:macro #'dart-tf-x-str-trim-left  :emit :macro}
   :x-str-trim-right  {:macro #'dart-tf-x-str-trim-right :emit :macro}})

(defn dart-tf-x-lu-create
  ([[_]]
   '{}))

(defn dart-tf-x-lu-get [[_ lu obj]] (list '. lu [obj]))
(defn dart-tf-x-lu-set [[_ lu obj gid]] (list ':= (list '. lu [obj]) gid))
(defn dart-tf-x-lu-del [[_ lu obj]] (list '. lu (list 'remove obj)))

(def +dart-lu+
  {:x-lu-create      {:macro #'dart-tf-x-lu-create :emit :macro}
   :x-lu-get         {:macro #'dart-tf-x-lu-get :emit :macro}
   :x-lu-set         {:macro #'dart-tf-x-lu-set :emit :macro}
   :x-lu-del         {:macro #'dart-tf-x-lu-del :emit :macro}})

(def +dart-json+
  {:x-json-encode {:emit :alias :raw 'jsonEncode}
   :x-json-decode {:emit :alias :raw 'jsonDecode}})

(def +dart-math+
  {:x-m-abs    {:macro #'dart-tf-x-m-abs    :emit :macro}
   :x-m-ceil   {:macro #'dart-tf-x-m-ceil   :emit :macro}
   :x-m-floor  {:macro #'dart-tf-x-m-floor  :emit :macro}
   :x-m-sin    {:macro #'dart-tf-x-m-sin    :emit :macro}
   :x-m-cos    {:macro #'dart-tf-x-m-cos    :emit :macro}
   :x-m-tan    {:macro #'dart-tf-x-m-tan    :emit :macro}
   :x-m-asin   {:macro #'dart-tf-x-m-asin   :emit :macro}
   :x-m-acos   {:macro #'dart-tf-x-m-acos   :emit :macro}
   :x-m-atan   {:macro #'dart-tf-x-m-atan   :emit :macro}
   :x-m-sqrt   {:macro #'dart-tf-x-m-sqrt   :emit :macro}
   :x-m-exp    {:macro #'dart-tf-x-m-exp    :emit :macro}
   :x-m-loge   {:macro #'dart-tf-x-m-loge   :emit :macro}
   :x-m-log10  {:macro #'dart-tf-x-m-log10  :emit :macro}
   :x-m-max    {:macro #'dart-tf-x-m-max    :emit :macro}
   :x-m-min    {:macro #'dart-tf-x-m-min    :emit :macro}
   :x-m-mod    {:macro #'dart-tf-x-m-mod    :emit :macro}
   :x-m-pow    {:emit :alias :raw 'math.pow}
   :x-m-quot   {:macro #'dart-tf-x-m-quot   :emit :macro}
   :x-m-cosh   {:macro #'dart-tf-x-m-cosh   :emit :macro}
   :x-m-sinh   {:macro #'dart-tf-x-m-sinh   :emit :macro}
   :x-m-tanh   {:macro #'dart-tf-x-m-tanh   :emit :macro}})

(defn dart-tf-x-arr-pop [[_ arr]] (dart-method0 arr 'removeLast))
(defn dart-tf-x-arr-slice
  [[_ arr start & [end]]]
  (if end
    (list '. arr (list 'sublist (list '- start (list 'x:offset)) end))
    (list '. arr (list 'sublist (list '- start (list 'x:offset))))))
(defn dart-tf-x-arr-push-first [[_ arr item]] (list '. arr (list 'insert 0 item)))
(defn dart-tf-x-arr-pop-first [[_ arr]] (list '. arr (list 'removeAt 0)))
(defn dart-tf-x-arr-insert [[_ arr idx e]] (list '. arr (list 'insert idx e)))
(defn dart-tf-x-arr-remove [[_ arr idx]] (list '. arr (list 'removeAt idx)))
(defn dart-tf-x-arr-sort [[_ arr key-fn comp-fn]]
  (list '. arr (list 'sort (list 'fn '[a b]
                                 (list 'return
                                       (list ':? (list comp-fn (list key-fn 'a) (list key-fn 'b))
                                             -1
                                             1))))))
(defn dart-tf-x-str-comp [[_ a b]]
  (list '< (list '. (dart-method0 a 'toString) (list 'compareTo (dart-method0 b 'toString)))
        0))

(def +dart-arr+
  {:x-arr-slice       {:macro #'dart-tf-x-arr-slice      :emit :macro :type :template}
   :x-arr-push        {:macro #'dart-tf-x-arr-push       :emit :macro :type :template}
   :x-arr-push-first  {:macro #'dart-tf-x-arr-push-first :emit :macro :type :template}
   :x-arr-pop         {:macro #'dart-tf-x-arr-pop        :emit :macro :type :template}
   :x-arr-pop-first   {:macro #'dart-tf-x-arr-pop-first  :emit :macro :type :template}
   :x-arr-insert      {:macro #'dart-tf-x-arr-insert     :emit :macro :type :template}
   :x-arr-remove      {:macro #'dart-tf-x-arr-remove     :emit :macro :type :template}
   :x-arr-sort        {:macro #'dart-tf-x-arr-sort       :emit :macro}
   :x-str-comp        {:macro #'dart-tf-x-str-comp       :emit :macro}})

(defn dart-tf-x-iter-eq
  [[_ a b eq-fn]]
  (template/$
   (do (while (. ~a (moveNext))
         (if (not (. ~b (moveNext)))
           (return false))
         (if (not (~eq-fn (. ~a current) (. ~b current)))
           (return false)))
       (return (not (. ~b (moveNext)))))))
(defn dart-tf-x-iter-from [[_ x]] (list '. x 'iterator))
(defn dart-tf-x-iter-from-arr [[_ arr]] (list '. arr 'iterator))
(defn dart-tf-x-iter-from-obj
  [[_ obj]]
  (let [entry-fn (list 'fn:> '[e]
                       [(list '. 'e 'key)
                        (list '. 'e 'value)])]
    (list '. (list '. (list '. obj 'entries)
                   (list 'map entry-fn))
          'iterator)))
(defn dart-tf-x-iter-has?
  [[_ iter]]
  (let [rtype-expr (dart-runtime-type-string iter)]
    (list 'or
          (list 'x:iter-native? iter)
          (list '. rtype-expr (list 'contains "Iterable"))
          (list '. rtype-expr (list 'contains "List"))
          (list '. rtype-expr (list 'contains "Set")))))
(defn dart-tf-x-iter-native?
  [[_ iter]]
  (let [rtype-expr (dart-runtime-type-string iter)]
    (list 'and
          (list 'not= nil iter)
          (list '. rtype-expr (list 'contains "Iterator")))))
(defn dart-tf-x-iter-next [[_ iter]] (list '. iter 'current))
(defn dart-tf-x-iter-null [[_]] '(. [] iterator))

(def +dart-iter+
  {:x-iter-eq          {:macro #'dart-tf-x-iter-eq         :emit :macro}
   :x-iter-from        {:macro #'dart-tf-x-iter-from       :emit :macro}
   :x-iter-from-arr    {:macro #'dart-tf-x-iter-from-arr   :emit :macro}
   :x-iter-from-obj    {:macro #'dart-tf-x-iter-from-obj   :emit :macro}
   :x-iter-has?        {:macro #'dart-tf-x-iter-has?       :emit :macro}
   :x-iter-native?     {:macro #'dart-tf-x-iter-native?    :emit :macro}
   :x-iter-next        {:macro #'dart-tf-x-iter-next       :emit :macro}
   :x-iter-null        {:macro #'dart-tf-x-iter-null       :emit :macro}})

(defn dart-tf-x-prototype-create [[_ m]] m)
(defn dart-tf-x-prototype-get [[_ obj _]]
  (list '. obj 'runtimeType))
(defn dart-tf-x-prototype-set [[_ obj prototype _]]
  (list 'throw '"Proto set not supported in Dart"))
(defn dart-tf-x-prototype-tostring [[_ obj]] '"toString")

(def +dart-proto+
  {:prototype-create         {:macro #'dart-tf-x-prototype-create      :emit :macro}
   :prototype-get            {:macro #'dart-tf-x-prototype-get         :emit :macro}
   :prototype-set            {:macro #'dart-tf-x-prototype-set         :emit :macro}
   :prototype-tostring       {:macro #'dart-tf-x-prototype-tostring    :emit :macro}})

(defn dart-tf-x-return-encode
  [[_ out id key]]
  (let [outtype (gensym "outtype")
        outstr  (gensym "outstr")
        rtype-expr (dart-runtime-type-string out)
        outstr-expr  (dart-method0 out 'toString)]
    (template/$
     (do (if (== ~out nil)
           (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "nil" "value" nil})))
         (var ~outtype ~rtype-expr)
         (var ~outstr ~outstr-expr)
         (if (== "String" ~outtype)
           (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "string" "value" ~out})))
         (if (or (== "int" ~outtype)
                 (== "double" ~outtype)
                 (== "num" ~outtype))
           (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "number" "value" ~out})))
         (if (== "bool" ~outtype)
           (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "boolean" "value" ~out})))
         (if (or (== "List" ~outtype)
                 (. ~outtype (contains "List"))
                 (== "Map" ~outtype)
                 (. ~outtype (contains "Map")))
           (return (json.encode {"id" ~id "key" ~key "type" "data" "value" ~out})))
         (if (or (. ~outtype (contains "Function"))
                 (. ~outtype (contains "=>"))
                 (. ~outstr (startsWith "Closure")))
           (return (json.encode {"id" ~id "key" ~key "type" "raw" "return" "function" "value" ~outstr})))
         (return (json.encode {"id" ~id "key" ~key "type" "raw" "return" (. ~outtype (toLowerCase)) "value" ~outstr}))))))

(defn dart-tf-x-return-wrap
  [[_ f encode-fn]]
  (list 'try
        (list 'var 'out (list f))
        (list 'try
              (list 'return (list 'Function.apply encode-fn ['out]))
              (list 'catch '_
                    (list 'return (list 'Function.apply encode-fn ['out nil nil]))))
        (list 'catch 'e
              (list 'return
                    (list 'json.encode
                          '{:type "error"
                            :value {:message (. e (toString))}})))))

(defn dart-tf-x-return-eval
  [[_ s wrap-fn]]
  (list 'throw '"eval not supported in Dart"))

(defn dart-tf-x-thread-spawn
  [[_ f]]
  (list 'throw '"Thread spawn not implemented in Dart"))

(defn dart-tf-x-thread-join
  [[_ thread]]
  (list 'throw '"Thread join not implemented in Dart"))

(defn dart-tf-x-with-delay
  [[_ thunk ms]]
  (template/$
   (Future.delayed
    (:- "Duration(milliseconds: " ~ms ")")
    (fn [] (return (~thunk))))))

(defn dart-tf-x-start-interval
  [[_ ms f]]
  (template/$
   (Timer.periodic
    (:- "Duration(milliseconds: " ~ms ")")
    (fn [timer] (~f)))))

(defn dart-tf-x-stop-interval
  [[_ timer]]
  (list '. timer 'cancel))

(def +dart-thread+
  {:x-thread-spawn    {:macro #'dart-tf-x-thread-spawn    :emit :macro}
   :x-thread-join     {:macro #'dart-tf-x-thread-join     :emit :macro}
   :x-with-delay      {:macro #'dart-tf-x-with-delay      :emit :macro}
   :x-start-interval  {:macro #'dart-tf-x-start-interval  :emit :macro}
   :x-stop-interval   {:macro #'dart-tf-x-stop-interval   :emit :macro}})

(def +dart-return+
  {:x-return-encode  {:macro #'dart-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'dart-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'dart-tf-x-return-eval     :emit :macro}})

(defn dart-tf-x-socket-connect
  [[_ host port opts cb]]
  (template/$
   (do (:- "import 'dart:io';")
       (return (. (Socket.connect ~host ~port)
                  (then (fn [conn]
                          (return (~cb nil conn))))
                  (catchError (fn [err]
                                (return (~cb err nil)))))))))

(defn dart-tf-x-socket-send
  [[_ conn s]]
  (template/$
   (do (:- "import 'dart:io';")
       (. ~conn (write ~s)))))

(defn dart-tf-x-socket-close
  [[_ conn]]
  (template/$
   (return
    (. (. ~conn (flush))
       (then (fn [_]
               (. ~conn (destroy))
               (return nil)))))))
(defn dart-tf-x-socket-connect [[_ host port opts cb]]
  (list 'throw '"Socket not implemented"))

(defn dart-tf-x-socket-send [[_ conn s]]
  (list 'throw '"Socket not implemented"))

(defn dart-tf-x-socket-close [[_ conn]]
  (list 'throw '"Socket not implemented"))

(def +dart-socket+
  {:x-socket-connect {:macro #'dart-tf-x-socket-connect :emit :macro}
   :x-socket-send    {:macro #'dart-tf-x-socket-send    :emit :macro}
   :x-socket-close   {:macro #'dart-tf-x-socket-close   :emit :macro}})

(defn dart-tf-x-b64-encode
  [[_ s]]
  (list 'base64.encode (list 'utf8.encode s)))

(defn dart-tf-x-b64-decode
  [[_ s]]
  (list 'utf8.decode (list 'base64.decode s)))

(def +dart-b64+
  {:x-b64-encode     {:macro #'dart-tf-x-b64-encode      :emit :macro}
   :x-b64-decode     {:macro #'dart-tf-x-b64-decode      :emit :macro}})

(defn dart-tf-x-uri-encode
  [[_ s]]
  (list 'Uri.encodeComponent s))

(defn dart-tf-x-uri-decode
  [[_ s]]
  (list 'Uri.decodeComponent s))

(def +dart-uri+
  {:x-uri-encode     {:macro #'dart-tf-x-uri-encode      :emit :macro}
   :x-uri-decode     {:macro #'dart-tf-x-uri-decode      :emit :macro}})

(defn dart-tf-x-notify-http
  [[_ host port value id key opts]]
  (template/$
   (do (:- "import 'dart:io';")
       (var resolved-opts (:? (xt/x:nil? ~opts) {} ~opts))
       (var #{path} resolved-opts)
       (var output (xt.lang.common-repl/return-encode ~value ~id ~key))
       (var endpoint (:? (xt/x:nil? path) "/" path))
       (var envelope (xt/x:cat "POST "
                               endpoint
                               " HTTP/1.0\r\n"
                               "Host: "
                               ~host
                               ":"
                               (xt/x:to-string ~port)
                               "\r\n"
                               "Content-Length: "
                               (xt/x:to-string (xt/x:len output))
                               "\r\n"
                               "\r\n"
                               output))
       (return (. (Socket.connect ~host ~port)
                  (then (fn [conn]
                          (. conn (write envelope))
                          (return (. (. conn (flush))
                                     (then (fn [_]
                                             (. conn (destroy))
                                             (return nil)))))))
                  (catchError (fn [e]
                                (return ["unable to connect"]))))))))

(def +dart-special+
  {:x-notify-http {:macro #'dart-tf-x-notify-http :emit :macro :type :template}})

(defn dart-tf-x-slurp-file
  [[_ filename opts cb]]
  (list 'throw '"slurp-file not implemented in Dart"))

(defn dart-tf-x-spit-file
  [[_ filename s opts cb]]
  (list 'throw '"spit-file not implemented in Dart"))

(def +dart-file+
  {:x-slurp-file     {:macro #'dart-tf-x-slurp-file      :emit :macro}
   :x-spit-file      {:macro #'dart-tf-x-spit-file       :emit :macro}})

(def +dart+
  (merge +dart-core+
          +dart-math+
          +dart-type+
          +dart-str+
          +dart-lu+
          +dart-json+
          +dart-arr+
          +dart-iter+
          +dart-proto+
          +dart-return+
          +dart-socket+
          +dart-thread+
          +dart-b64+
         +dart-uri+
         +dart-special+
         +dart-file+))
