(ns std.lang.model.spec-dart.typed
  (:require [clojure.string :as str]
            [std.lang.typed.xtalk-analysis :as analysis]))

(declare emit-dart-type)
(declare lossy-dart-type)

(def ^:dynamic *emit-options*
  {:strict? false
   :lossy-fallback "Object?"})

(defn sanitize-ident
  [s]
  (-> (str s)
      (str/replace #"[^A-Za-z0-9_]" "_")
      (str/replace #"^[0-9]" "_$0")))

(defn lower-camel
  [s]
  (let [base (sanitize-ident s)]
    (if (str/includes? base "_")
      (let [parts (str/split base #"_")
            head (str/lower-case (first parts))
            tail (map str/capitalize (rest parts))]
        (str (or head "x") (apply str tail)))
      (str (str/lower-case (subs base 0 1))
           (subs base 1)))))

(defn upper-camel
  [s]
  (let [base (sanitize-ident s)]
    (if (str/includes? base "_")
      (->> (str/split base #"_")
           (map str/capitalize)
           (apply str))
      (str (str/upper-case (subs base 0 1))
           (subs base 1)))))

(defn named-dart-ident
  [type-name current-ns]
  (cond
    (symbol? type-name)
    (if (= current-ns (some-> type-name namespace symbol))
      (upper-camel (name type-name))
      (upper-camel (str (namespace type-name) "_" (name type-name))))

    (string? type-name)
    (upper-camel type-name)

    :else
    "Object?"))

(defn emit-dart-type
  [type current-ns]
  (case (:kind type)
    :primitive
    (case (:name type)
      :xt/str "String"
      :xt/bool "bool"
      :xt/int "int"
      :xt/num "double"
      :xt/kw "String"
      :xt/nil "Null"
      :xt/any "Object?"
      :xt/unknown "Object?"
      :xt/obj "Map<String, Object?>"
      :xt/fn "Object? Function(List<Object?>)"
      "Object?")

    :named
    (named-dart-ident (:name type) current-ns)

    :maybe
    (str (emit-dart-type (:item type) current-ns) "?")

    :union
    (lossy-dart-type :union)

    :intersection
    (lossy-dart-type :intersection)

    :tuple
    (lossy-dart-type :tuple)

    :array
    (str "List<" (emit-dart-type (:item type) current-ns) ">")

    :dict
    (str "Map<" (emit-dart-type (:key type) current-ns)
         ", " (emit-dart-type (:value type) current-ns) ">")

    :record
    "Map<String, Object?>"

    :fn
    (str (emit-dart-type (:output type) current-ns)
         " Function("
         (->> (:inputs type)
              (map #(emit-dart-type % current-ns))
              (str/join ", "))
         ")")

    :apply
    (lossy-dart-type :apply)

    "Object?"))

(defn lossy-dart-type
  [kind]
  (if (:strict? *emit-options*)
    (throw (ex-info "Lossy xtalk->dart type conversion"
                    {:kind kind
                     :fallback (:lossy-fallback *emit-options*)}))
    (:lossy-fallback *emit-options*)))

(defn maybe-unwrapped
  [type optional?]
  (if (and optional?
           (= :maybe (:kind type)))
    (:item type)
    type))

(defn emit-class-field
  [{:keys [name type optional?]} current-ns]
  (let [base-type (maybe-unwrapped type optional?)]
    (str "  final "
         (emit-dart-type base-type current-ns)
         (when optional? "?")
         " "
         (lower-camel name)
         ";")))

(defn emit-class-constructor
  [class-name fields]
  (str "  const " class-name "({"
       (->> fields
            (map (fn [{:keys [name optional?]}]
                   (str (when-not optional? "required ") "this." (lower-camel name))))
            (str/join ", "))
       "});"))

(defn emit-record-class
  [name type current-ns]
  (str "class " name " {\n"
       (->> (:fields type)
            (map #(emit-class-field % current-ns))
            (str/join "\n"))
       (when (seq (:fields type)) "\n")
       (emit-class-constructor name (:fields type))
       "\n}"))

(defn emit-spec-declaration
  [spec]
  (let [current-ns (some-> spec :ns symbol)
        name (upper-camel (:name spec))
        type (:type spec)]
    (if (= :record (:kind type))
      (emit-record-class name type current-ns)
      (str "typedef " name " = " (emit-dart-type type current-ns) ";"))))

(defn emit-function-declaration
  [fn-def]
  (let [current-ns (some-> fn-def :ns symbol)
        name (upper-camel (:name fn-def))]
    (str "typedef " name " = "
         (emit-dart-type (:output fn-def) current-ns)
         " Function("
         (->> (:inputs fn-def)
              (map-indexed (fn [idx input]
                             (str (emit-dart-type (:type input) current-ns)
                                  " arg"
                                  idx)))
              (str/join ", "))
         ");")))

(defn emit-value-declaration
  [value-def]
  (let [current-ns (some-> value-def :ns symbol)]
    (str "late final "
         (emit-dart-type (:type value-def) current-ns)
         " "
         (lower-camel (:name value-def))
         ";")))

(defn emit-analysis-declarations
  ([analysis]
   (emit-analysis-declarations analysis {}))
  ([{:keys [specs functions values]} opts]
   (binding [*emit-options* (merge *emit-options* opts)]
     (->> (concat (map emit-spec-declaration specs)
                  (map emit-function-declaration functions)
                  (map emit-value-declaration values))
          (str/join "\n\n")))))

(defn emit-namespace-declarations
  ([ns-sym]
   (emit-namespace-declarations ns-sym {}))
  ([ns-sym opts]
   (-> ns-sym
       analysis/analyze-namespace
       (emit-analysis-declarations opts))))
