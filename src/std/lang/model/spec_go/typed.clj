(ns std.lang.model.spec-go.typed
  (:require [clojure.string :as str]
            [std.lang.typed.xtalk-analysis :as analysis]))

(declare emit-go-type)
(declare lossy-go-type)

(def ^:dynamic *emit-options*
  {:strict? false
   :lossy-fallback "any"})

(defn sanitize-ident
  [s]
  (-> (str s)
      (str/replace #"[^A-Za-z0-9_]" "_")
      (str/replace #"^[0-9]" "_$0")))

(defn capitalize-ident
  [s]
  (let [s (sanitize-ident s)]
    (if (seq s)
      (str (str/upper-case (subs s 0 1)) (subs s 1))
      "X")))

(defn named-go-ident
  [type-name current-ns]
  (cond
    (symbol? type-name)
    (if (= current-ns (some-> type-name namespace symbol))
      (sanitize-ident (name type-name))
      (sanitize-ident (str (namespace type-name) "_" (name type-name))))

    (string? type-name)
    (sanitize-ident type-name)

    :else
    "any"))

(defn maybe-go-type
  [item current-ns]
  (let [rendered (emit-go-type item current-ns)]
    (if (#{"string" "bool" "int" "float64" "any"} rendered)
      "any"
      (str "*" rendered))))

(defn lossy-go-type
  [kind]
  (if (:strict? *emit-options*)
    (throw (ex-info "Lossy xtalk->go type conversion"
                    {:kind kind
                     :fallback (:lossy-fallback *emit-options*)}))
    (:lossy-fallback *emit-options*)))

(defn emit-go-type
  [type current-ns]
  (case (:kind type)
    :primitive
    (case (:name type)
      :xt/str "string"
      :xt/bool "bool"
      :xt/int "int"
      :xt/num "float64"
      :xt/kw "string"
      :xt/nil "any"
      :xt/any "any"
      :xt/unknown "any"
      :xt/obj "map[string]any"
      :xt/fn "func(...any) any"
      "any")

    :named
    (named-go-ident (:name type) current-ns)

    :maybe
    (maybe-go-type (:item type) current-ns)

    :union
    (lossy-go-type :union)

    :intersection
    (lossy-go-type :intersection)

    :tuple
    (lossy-go-type :tuple)

    :array
    (str "[]" (emit-go-type (:item type) current-ns))

    :dict
    (str "map[" (emit-go-type (:key type) current-ns) "]"
         (emit-go-type (:value type) current-ns))

    :record
    (if (:open type)
      "map[string]any"
      "struct")

    :fn
    (str "func("
         (->> (:inputs type)
              (map-indexed (fn [idx input]
                             (str "arg" idx " " (emit-go-type input current-ns))))
              (str/join ", "))
         ") "
         (emit-go-type (:output type) current-ns))

    :apply
    (lossy-go-type :apply)

    "any"))

(defn emit-struct-field
  [{:keys [name type optional?]} current-ns]
  (let [field-name (capitalize-ident name)
        field-type (emit-go-type (if (and optional?
                                          (= :maybe (:kind type)))
                                   (:item type)
                                   type)
                                 current-ns)]
    (str "  " field-name " " field-type " `json:\"" name "\"`")))

(defn emit-struct-type
  [type current-ns]
  (str "struct {\n"
       (->> (:fields type)
            (map #(emit-struct-field % current-ns))
            (str/join "\n"))
       "\n}"))

(defn emit-spec-declaration
  [spec]
  (let [current-ns (some-> spec :ns symbol)
        name (sanitize-ident (:name spec))
        type (:type spec)]
    (str "type " name " "
         (if (= :record (:kind type))
           (emit-struct-type type current-ns)
           (emit-go-type type current-ns)))))

(defn emit-function-declaration
  [fn-def]
  (let [current-ns (some-> fn-def :ns symbol)
        name (sanitize-ident (:name fn-def))]
    (str "type " name " func("
         (->> (:inputs fn-def)
              (map-indexed (fn [idx input]
                             (str "arg" idx " " (emit-go-type (:type input) current-ns))))
              (str/join ", "))
         ") "
         (emit-go-type (:output fn-def) current-ns))))

(defn emit-value-declaration
  [value-def]
  (let [current-ns (some-> value-def :ns symbol)]
    (str "var "
         (sanitize-ident (:name value-def))
         " "
         (emit-go-type (:type value-def) current-ns))))

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
