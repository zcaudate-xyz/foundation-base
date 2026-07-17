(ns hara.model.spec-xtalk.fn-dart
  (:require [std.lib.collection :as collection]
            [std.lib.template :as template]))

(defn dart-method0
  [obj method]
  ;; Wrap the receiver so negative literals emit as `(-1.2).ceil()`
  ;; rather than `-1.2.ceil()`, which Dart parses with unary-minus precedence.
  (list :% (list :- "(") obj (list :- ").") method (list :- "()")))

(defn dart-call
  [f & args]
  (list 'Function.apply f (vec args)))

(defn dart-runtime-type-string
  [obj]
  (dart-method0 (list '. obj 'runtimeType) 'toString))

(defn dart-string-compare-expr
  [a b]
  (list '. (dart-method0 a 'toString)
        (list 'compareTo (dart-method0 b 'toString))))

(defn dart-comparison-form
  [a b numeric-op string-op]
  (let [string-side? (fn [x]
                       (list '== "String" (dart-runtime-type-string x)))]
    (list ':?
          (list 'or (string-side? a)
                (string-side? b))
          (list string-op (dart-string-compare-expr a b) 0)
          (list numeric-op a b))))

(defn dart-is-map
  [obj]
  (list :% (list :- "(") obj (list :- " is Map)")))

(defn dart-map-get
  [obj key]
  (list :% (list :- "((") obj (list :- (str " as Map)[\"" key "\"])"))))

(defn dart-xt-exception?
  [obj]
  (list 'and
        (dart-is-map obj)
        (list '== "xt.exception" (dart-map-get obj "__type__"))))

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

(defn dart-tf-x-construct
  [[_ ctor args]]
  (list 'Function.apply ctor args))

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

(defn dart-tf-x-ex-native?
  [[_ value]]
  (dart-xt-exception? value))

(defn dart-tf-x-ex-new
  [[_ message & [data]]]
  {"__type__" "xt.exception"
   "message" message
   "data" data})

(defn dart-tf-x-ex-message
  [[_ value]]
  (list ':?
        (dart-xt-exception? value)
        (dart-map-get value "message")
        nil))

(defn dart-tf-x-ex-data
  [[_ value]]
  (list ':?
        (dart-xt-exception? value)
        (dart-map-get value "data")
        nil))

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

(defn dart-tf-x-lt
  [[_ a b]]
  (dart-comparison-form a b '< '<))

(defn dart-tf-x-lte
  [[_ a b]]
  (dart-comparison-form a b '<= '<=))

(defn dart-tf-x-gt
  [[_ a b]]
  (dart-comparison-form a b '> '>))

(defn dart-tf-x-gte
  [[_ a b]]
  (dart-comparison-form a b '>= '>=))

(def +dart-core+
  {:x-print    {:macro #'dart-tf-x-print    :emit :macro :value true}
   :x-len      {:macro #'dart-tf-x-len      :emit :macro :value true}
   :x-cat      {:macro #'dart-tf-x-cat      :emit :macro :value true}
   :x-apply    {:macro #'dart-tf-x-apply    :emit :macro}
   :x-construct {:macro #'dart-tf-x-construct :emit :macro}
    :x-err      {:emit :alias :raw 'throw}
    :x-ex-native? {:macro #'dart-tf-x-ex-native? :emit :macro}
    :x-ex-new   {:macro #'dart-tf-x-ex-new   :emit :macro}
    :x-ex-message {:macro #'dart-tf-x-ex-message :emit :macro}
    :x-ex-data  {:macro #'dart-tf-x-ex-data  :emit :macro}
    :x-now-ms   {:macro #'dart-tf-x-now-ms   :emit :macro}
    :x-random   {:macro #'dart-tf-x-random   :emit :macro}
    :x-type-native {:macro #'dart-tf-x-type-native :emit :macro}
    :x-del      {:macro #'dart-tf-x-del      :emit :macro}
   :x-eval     {:macro #'dart-tf-x-eval     :emit :macro}
   :x-has-key? {:macro #'dart-tf-x-has-key? :emit :macro}
   :x-del-key  {:macro #'dart-tf-x-del-key  :emit :macro}
   :x-lt       {:macro #'dart-tf-x-lt       :emit :macro}
   :x-lte      {:macro #'dart-tf-x-lte      :emit :macro}
   :x-gt       {:macro #'dart-tf-x-gt       :emit :macro}
   :x-gte      {:macro #'dart-tf-x-gte      :emit :macro}
   :x-unpack   {:emit :alias :raw '...}})


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
(defn dart-tf-x-m-max
  [[_ & args]]
  (reduce (fn [acc arg]
            (list ':? (list '> acc arg) acc arg))
          args))

(defn dart-tf-x-m-min
  [[_ & args]]
  (reduce (fn [acc arg]
            (list ':? (list '< acc arg) acc arg))
          args))
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

(defn dart-json-compact-expr
  [value]
  (template/$
   (:- "(() {\n"
       "  final omitNilKeys = <String>{\"db/remove\", \"select_method\", \"select_control\", \"return_method\", \"return_query\", \"return_count\", \"return_id\", \"return_bulk\", \"return_omit\", \"data_only\", \"model_id\", \"view_id\"};\n"
       "  dynamic compact(dynamic value) {\n"
       "    if (value is Function) {\n"
       "      return null;\n"
       "    }\n"
       "    if (value is Map) {\n"
       "      final out = <dynamic, dynamic>{};\n"
       "      value.forEach((key, entry) {\n"
       "        if (!(entry is Function)) {\n"
       "          final next = compact(entry);\n"
       "          if (next != null || !(key is String && omitNilKeys.contains(key))) {\n"
       "            out[key] = next;\n"
       "          }\n"
       "        }\n"
       "      });\n"
       "      return out;\n"
       "    }\n"
       "    if (value is List) {\n"
       "      return List<dynamic>.from(value.where((entry) => !(entry is Function))\n"
       "                                       .map((entry) => compact(entry)));\n"
       "    }\n"
       "    return value;\n"
       "  }\n"
       "  return compact("
       ~value
       ");\n"
       "})()")))

(defn dart-tf-x-obj-keys
  [[_ obj]]
  (template/$
   (:- "List<dynamic>.from(("
       ~obj
       ").keys)")))

(defn dart-tf-x-obj-vals
  [[_ obj]]
  (template/$
   (:- "List<dynamic>.from(("
       ~obj
       ").values)")))

(defn dart-tf-x-obj-pairs
  [[_ obj]]
  (template/$
   (:- "List<List<dynamic>>.from(("
       ~obj
       ").entries.map((entry) => [entry.key, entry.value]))")))

(def +dart-obj+
  {:x-obj-keys   {:macro #'dart-tf-x-obj-keys   :emit :macro}
   :x-obj-vals   {:macro #'dart-tf-x-obj-vals   :emit :macro}
   :x-obj-pairs  {:macro #'dart-tf-x-obj-pairs  :emit :macro}})

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
   :x-m-pow    {:macro #'dart-tf-x-m-pow    :emit :macro}
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
  (list '< (dart-string-compare-expr a b) 0))

(def +dart-arr+
  {:x-arr-slice       {:macro #'dart-tf-x-arr-slice      :emit :macro :type :template}
   :x-arr-push        {:macro #'dart-tf-x-arr-push       :emit :macro :type :template}
   :x-arr-push-first  {:macro #'dart-tf-x-arr-push-first :emit :macro :type :template}
   :x-arr-pop         {:macro #'dart-tf-x-arr-pop        :emit :macro :type :template}
   :x-arr-pop-first   {:macro #'dart-tf-x-arr-pop-first  :emit :macro :type :template}
   :x-arr-insert      {:macro #'dart-tf-x-arr-insert     :emit :macro :type :template}
   :x-arr-remove      {:macro #'dart-tf-x-arr-remove     :emit :macro :type :template}
   #_#_:x-arr-sort        {:macro #'dart-tf-x-arr-sort       :emit :macro}
   :x-str-comp        {:macro #'dart-tf-x-str-comp       :emit :macro}})

(defn dart-tf-x-iter-eq
  [[_ a b eq-fn]]
  (template/$
   ((fn []
      (while (. ~a (moveNext))
        (if (not (. ~b (moveNext)))
          (return false))
        (if (not (~eq-fn (. ~a current) (. ~b current)))
          (return false)))
      (return (not (. ~b (moveNext))))))))
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
(defn dart-tf-x-prototype-get [[_ obj]]
  (list 'x:get-key obj "_xt_proto" nil))
(defn dart-tf-x-prototype-set [[_ obj prototype]]
  (template/$
   ((fn []
      (x:set-key ~obj "_xt_proto" ~prototype)
      (return ~obj)))))
(defn dart-tf-x-prototype-method [[_ obj key]]
  (let [direct (gensym "direct")
        proto  (gensym "proto")]
    (template/$
     ((fn []
        (var ~direct (x:get-key ~obj ~key nil))
        (if (not= nil ~direct)
          (return ~direct))
        (var ~proto (proto:get ~obj))
        (if (== nil ~proto)
          (return nil))
        (return (x:get-key ~proto ~key nil)))))))
(defn dart-tf-x-prototype-tostring [[_ obj]] '"toString")

(def +dart-proto+
  {:prototype-create         {:macro #'dart-tf-x-prototype-create      :emit :macro}
   :prototype-get            {:macro #'dart-tf-x-prototype-get         :emit :macro}
   :prototype-set            {:macro #'dart-tf-x-prototype-set         :emit :macro}
   :prototype-method         {:macro #'dart-tf-x-prototype-method      :emit :macro}
   :prototype-tostring       {:macro #'dart-tf-x-prototype-tostring    :emit :macro}})

(defn dart-tf-x-return-encode
  [[_ out id key]]
  (let [outtype (gensym "outtype")
        outstr  (gensym "outstr")
        compact (gensym "compact")
        compact-expr (dart-json-compact-expr out)
        rtype-expr (dart-runtime-type-string out)
        outstr-expr  (dart-method0 out 'toString)]
    (template/$
     (do (if (== ~out nil)
           (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "nil" "value" nil})))
          (var ~outtype ~rtype-expr)
          (var ~outstr ~outstr-expr)
          (var ~compact ~compact-expr)
          (if (== "String" ~outtype)
            (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "string" "value" ~out})))
          (if (or (== "int" ~outtype)
                  (== "double" ~outtype)
                  (== "num" ~outtype))
            (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "number" "value" ~out})))
          (if (== "bool" ~outtype)
            (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "boolean" "value" ~out})))
           (if (or (== "List" ~outtype)
                   (. ~outtype (contains "List")))
             (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "array" "value" ~compact})))
           (if (or (== "Map" ~outtype)
                   (. ~outtype (contains "Map")))
             (return (json.encode {"id" ~id "key" ~key "type" "data" "return" "object" "value" ~compact})))
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

(def +dart-return+
  {:x-return-encode  {:macro #'dart-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'dart-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'dart-tf-x-return-eval     :emit :macro}})

(defn dart-tf-x-with-delay
  [[_ ms thunk]]
  (let [call (dart-call thunk)]
    (template/$
     (. (Future.delayed
         (:- "Duration(milliseconds: " ~ms ")"))
        (then (fn [_]
                (return (Future.sync (fn []
                                       (return ~call))))))))))

(defn dart-tf-x-async-run
  [[_ thunk]]
  (list 'Future.sync thunk))

(defn dart-tf-x-promise
  [[_ thunk]]
  (list 'Future.sync thunk))

(defn dart-tf-x-promise-new
  [[_ thunk]]
  (let [call (dart-call thunk
                        (list :- "completer.complete")
                        (list :- "completer.completeError"))]
    (template/$
     (Future.sync
      (fn []
        (var completer (:- "Completer<dynamic>()"))
        ~call
        (return (. completer future)))))))

(defn dart-tf-x-promise-all
  [[_ promises]]
  (template/$
   (:- "Future.wait(List<Future<dynamic>>.from(("
       ~promises
       ").map((entry) => Future.sync(() => entry))))")))

(defn dart-tf-x-promise-then
  [[_ promise thunk]]
  (let [call (dart-call thunk 'value)]
    (list :%
          (list :- "((Future.sync(() => ")
          promise
          (list :- ")) as Future<dynamic>).then((value) async { return await ")
          call
          (list :- "; })"))))

(defn dart-tf-x-promise-catch
  [[_ promise thunk]]
  (let [call (dart-call thunk 'err)]
    (list :%
          (list :- "(() async { try { return await ((Future.sync(() => ")
          promise
          (list :- ")) as Future<dynamic>); } catch (err) { return await Future.sync(() => ")
          call
          (list :- "); } })()"))))

(defn dart-tf-x-promise-finally
  [[_ promise thunk]]
  (let [call (dart-call thunk)]
    (list :%
          (list :- "((Future.sync(() => ")
          promise
          (list :- ")) as Future<dynamic>).whenComplete(() async { await ")
          call
          (list :- "; })"))))

(defn dart-tf-x-promise-native?
  [[_ value]]
  (let [rtype-expr (dart-runtime-type-string value)]
    (list 'and
          (list 'not= nil value)
          (list 'or
                (list '== "Future" rtype-expr)
                (list '. rtype-expr (list 'startsWith "Future<"))))))

(def +dart-promise+
  {:x-async-run        {:macro #'dart-tf-x-async-run        :emit :macro}
   :x-promise          {:macro #'dart-tf-x-promise          :emit :macro}
   :x-promise-new      {:macro #'dart-tf-x-promise-new      :emit :macro}
   :x-promise-all      {:macro #'dart-tf-x-promise-all      :emit :macro}
   :x-promise-then     {:macro #'dart-tf-x-promise-then     :emit :macro}
   :x-promise-catch    {:macro #'dart-tf-x-promise-catch    :emit :macro}
   :x-promise-finally  {:macro #'dart-tf-x-promise-finally  :emit :macro}
   :x-promise-native?  {:macro #'dart-tf-x-promise-native?  :emit :macro}})

(def +dart-thread+
  {:x-with-delay      {:macro #'dart-tf-x-with-delay      :emit :macro}})

(defn dart-tf-x-socket-connect
  [[_ host port opts cb]]
  (let [call-ok  (dart-call cb nil 'conn)
        call-err (dart-call cb 'err nil)]
    (template/$
     (. (Socket.connect ~host ~port)
        (then (fn [conn]
               (return ~call-ok)))
        (catchError (fn [err]
                      (return ~call-err)))))))

(defn dart-tf-x-socket-send
  [[_ conn s]]
  (template/$
   (. ~conn (write ~s))))

(defn dart-tf-x-socket-close
  [[_ conn]]
  (template/$
   (. (. ~conn (flush))
      (then (fn [_]
              (. ~conn (destroy))
              (return nil))))))

(def +dart-socket+
  {:x-socket-connect {:macro #'dart-tf-x-socket-connect :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-socket-send    {:macro #'dart-tf-x-socket-send    :emit :macro}
   :x-socket-close   {:macro #'dart-tf-x-socket-close   :emit :macro}})

#_#_
(defn dart-tf-x-notify-http
  [[_ host port value id key opts]]
  (template/$
   (. (. (HttpClient)
         (post ~host ~port "/"))
      (then (fn [req]
              (. req (write (xt.lang.common-lib/return-encode ~value ~id ~key)))
              (return (. req (close)))))
      (then (fn [_]
              (return ["async"])))
      (catchError (fn [_]
                    (return ["unable to connect"]))))))

(def +dart-http+
  {:x-notify-http {:macro #'dart-tf-x-notify-http :emit :macro
                   :op-spec {:allow-blocks true}}})

(defn dart-tf-x-pwd
  [[_]]
  (template/$
   (:- "(Platform.environment[\"PWD\"] ?? Directory.current.path)")))

(defn dart-tf-x-file-resolve
  [[_ root path]]
  (template/$
   ((fn []
      (var base ~root)
      (return (:? (or (== nil base)
                      (. ~path (startsWith "/")))
                  ~path
                  (+ base "/" ~path)))))))

(defn dart-tf-x-file-slurp
  [[_ filename cb]]
  (let [call-ok  (dart-call cb nil 'out)
        call-err (dart-call cb 'err nil)]
    (template/$
     (. (. (File ~filename) (readAsString))
        (then (fn [out]
                (return ~call-ok)))
        (catchError (fn [err]
                      (return ~call-err)))))))

(defn dart-tf-x-file-spit
  [[_ filename s cb]]
  (let [call-ok  (dart-call cb nil filename)
        call-err (dart-call cb 'err nil)]
    (template/$
     (. (Directory (. (. (File ~filename) parent) path))
        (create :recursive true)
        (then (fn [_]
                (return (. (. (File ~filename) (writeAsString ~s))
                           (then (fn [_]
                                   (return ~call-ok)))
                           (catchError (fn [err]
                                         (return ~call-err)))))))
        (catchError (fn [err]
                      (return ~call-err)))))))

(def +dart-file+
  {:x-file-resolve   {:macro #'dart-tf-x-file-resolve    :emit :macro}
   :x-file-slurp     {:macro #'dart-tf-x-file-slurp      :emit :macro
                      :op-spec {:allow-blocks true}}
   :x-file-spit      {:macro #'dart-tf-x-file-spit       :emit :macro
                      :op-spec {:allow-blocks true}}})


(defn dart-tf-x-shell
  [[_ s root cb]]
  (template/$
   (do (var shell_command
             (:? (or (== nil ~root)
                     (== "" ~root))
                 ~s
                 (+ "cd " ~root " && " ~s)))
       (return (. (Process.run "sh" (:- "<String>[\"-lc\",shell_command]"))
                  (then (fn [result]
                          (if (not= 0 (. result exitCode))
                            (return (~cb {:code (. result exitCode)
                                          :err  (. result stderr)
                                          :out  (. result stdout)}
                                     nil))
                            (return (~cb nil (. result stdout))))))
                  (catchError (fn [err]
                                (return (~cb err nil)))))))))

(def +dart-shell+
  {:x-pwd      {:macro #'dart-tf-x-pwd      :emit :macro}
   :x-shell    {:macro #'dart-tf-x-shell    :emit :macro
                :op-spec {:allow-blocks true}}})

(def +dart+
  (merge +dart-core+
         +dart-math+
         +dart-type+
         +dart-str+
         +dart-lu+
         +dart-obj+
         +dart-json+
         +dart-arr+
         +dart-iter+
         +dart-proto+
         +dart-return+
         +dart-socket+
         
         #_+dart-http+
         +dart-file+
         +dart-promise+
         +dart-thread+
         +dart-shell+))
