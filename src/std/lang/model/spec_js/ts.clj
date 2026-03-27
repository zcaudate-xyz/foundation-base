(ns std.lang.model.spec-js.ts
  (:require [clojure.string :as str]
            [std.lang.typed.xtalk-analysis :as analysis]))

(declare emit-ts-type)

(defn valid-ts-ident?
  [s]
  (boolean (re-matches #"[A-Za-z_$][A-Za-z0-9_$]*" s)))

(defn sanitize-ts-ident
  [s]
  (let [out (-> (str s)
                (str/replace #"[^A-Za-z0-9_$]" "_")
                (str/replace #"^[0-9]" "_$0"))]
    (if (seq out) out "_")))

(defn named-ts-ident
  [type-name current-ns]
  (cond
    (symbol? type-name)
    (if (= current-ns (some-> type-name namespace symbol))
      (sanitize-ts-ident (clojure.core/name type-name))
      (sanitize-ts-ident (str (namespace type-name) "_" (clojure.core/name type-name))))

    (string? type-name)
    (sanitize-ts-ident type-name)

    :else
    "unknown"))

(defn quoted-prop
  [s]
  (str "\"" s "\""))

(defn prop-ident
  [s]
  (if (valid-ts-ident? s)
    s
    (quoted-prop s)))

(defn unwrap-maybe
  [type]
  (if (= :maybe (:kind type))
    (:item type)
    type))

(defn emit-record-fields
  [fields current-ns]
  (->> fields
       (map (fn [{:keys [name type optional?]}]
              (str "  "
                   (prop-ident name)
                   (when optional? "?")
                   ": "
                   (emit-ts-type (if optional? (unwrap-maybe type) type)
                                 current-ns)
                   ";")))
       (str/join "\n")))

(defn emit-open-record
  [open current-ns]
  (when open
    (str "  [key: "
         (emit-ts-type (:key open) current-ns)
         "]: "
         (emit-ts-type (:value open) current-ns)
         ";")))

(defn emit-ts-type
  [type current-ns]
  (case (:kind type)
    :primitive
    (case (:name type)
      :xt/str "string"
      :xt/bool "boolean"
      :xt/int "number"
      :xt/num "number"
      :xt/kw "string"
      :xt/nil "null"
      :xt/any "any"
      :xt/unknown "unknown"
      :xt/obj "Record<string, unknown>"
      :xt/fn "(...args: unknown[]) => unknown"
      "unknown")

    :named
    (named-ts-ident (:name type) current-ns)

    :maybe
    (str (emit-ts-type (:item type) current-ns) " | null")

    :union
    (str/join " | " (map #(emit-ts-type % current-ns) (:types type)))

    :intersection
    (str/join " & " (map #(emit-ts-type % current-ns) (:types type)))

    :tuple
    (str "[" (str/join ", " (map #(emit-ts-type % current-ns) (:types type))) "]")

    :array
    (str "Array<" (emit-ts-type (:item type) current-ns) ">")

    :dict
    (str "Record<" (emit-ts-type (:key type) current-ns)
         ", "
         (emit-ts-type (:value type) current-ns)
         ">")

    :record
    (let [lines (cond-> [(emit-record-fields (:fields type) current-ns)]
                  (:open type) (conj (emit-open-record (:open type) current-ns)))]
      (str "{\n"
           (str/join "\n" (remove str/blank? lines))
           "\n}"))

    :fn
    (str "("
         (->> (:inputs type)
              (map-indexed (fn [idx input]
                             (str "arg" idx ": " (emit-ts-type input current-ns))))
              (str/join ", "))
         ") => "
         (emit-ts-type (:output type) current-ns))

    :apply
    (str (named-ts-ident (:target type) current-ns)
         "<"
         (str/join ", " (map #(emit-ts-type % current-ns) (:args type)))
         ">")

    "unknown"))

(defn emit-spec-declaration
  [spec]
  (let [current-ns (some-> spec :ns symbol)
        type (:type spec)
        name (sanitize-ts-ident (:name spec))]
    (if (= :record (:kind type))
      (str "export interface " name " "
           (emit-ts-type type current-ns))
      (str "export type " name
           " = "
           (emit-ts-type type current-ns)
           ";"))))

(defn emit-analysis-declarations
  [{:keys [specs]}]
  (->> specs
       (map emit-spec-declaration)
       (str/join "\n\n")))

(defn emit-namespace-declarations
  [ns-sym]
  (-> ns-sym
      analysis/analyze-namespace
      emit-analysis-declarations))
