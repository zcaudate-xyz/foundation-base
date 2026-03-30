(ns std.lang.model.spec-js.ts
  (:require [clojure.string :as str]
            [std.lang.model.spec-xtalk.mixer :as mixer]
            [std.lang.typed.xtalk-common :as types]))

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
    (if-let [type-ns (some-> type-name namespace symbol)]
      (if (= current-ns type-ns)
        (sanitize-ts-ident (clojure.core/name type-name))
        (sanitize-ts-ident (str (namespace type-name) "_" (clojure.core/name type-name))))
      (sanitize-ts-ident (clojure.core/name type-name)))

    (string? type-name)
    (sanitize-ts-ident type-name)

    :else
    "unknown"))

(defn export-ts-ident
  [decl-name]
  (sanitize-ts-ident decl-name))

(defn ns->module-path
  [ns-sym]
  (str "./"
       (-> (str ns-sym)
           (str/replace #"\." "/")
           (str/replace #"-" "_"))))

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

(defn collect-type-refs
  [type]
  (case (:kind type)
    :named
    (if (symbol? (:name type))
      #{(:name type)}
      #{})

    :maybe
    (collect-type-refs (:item type))

    :union
    (into #{} (mapcat collect-type-refs) (:types type))

    :intersection
    (into #{} (mapcat collect-type-refs) (:types type))

    :tuple
    (into #{} (mapcat collect-type-refs) (:types type))

    :array
    (collect-type-refs (:item type))

    :dict
    (into (collect-type-refs (:key type))
          (collect-type-refs (:value type)))

    :record
    (let [field-refs (into #{} (mapcat (comp collect-type-refs :type)) (:fields type))
           open-refs (if-let [open (:open type)]
                       (into (collect-type-refs (:key open))
                             (collect-type-refs (:value open)))
                       #{})]
      (into field-refs open-refs))

    :fn
    (into (into #{} (mapcat collect-type-refs) (:inputs type))
           (collect-type-refs (:output type)))

    :apply
    (let [target-refs (if (symbol? (:target type))
                        #{(:target type)}
                        #{})
          arg-refs (into #{} (mapcat collect-type-refs) (:args type))]
      (into target-refs arg-refs))

    #{}))

(defn fn-type
  [fn-def]
  {:kind :fn
   :inputs (mapv :type (:inputs fn-def))
   :output (:output fn-def)})

(defn analysis-import-groups
  [{:keys [ns specs functions values]}]
  (let [spec-refs (mapcat (comp collect-type-refs :type) specs)
        fn-refs (mapcat (fn [fn-def]
                          (collect-type-refs (fn-type fn-def)))
                        functions)
        value-refs (mapcat (comp collect-type-refs :type) values)
        all-refs (concat spec-refs fn-refs value-refs)]
    (->> all-refs
         set
         (remove #(= ns (some-> % namespace symbol)))
         (group-by #(some-> % namespace symbol))
         (remove (comp nil? key))
         (sort-by (comp str key)))))

(defn emit-import-item
  [sym current-ns]
  (let [export-name (export-ts-ident (name sym))
        local-name (named-ts-ident sym current-ns)]
    (if (= export-name local-name)
      export-name
      (str export-name " as " local-name))))

(defn emit-import-declaration
  [current-ns [import-ns refs]]
  (str "import type { "
       (->> refs
            (sort-by name)
            (map #(emit-import-item % current-ns))
            (str/join ", "))
       " } from "
       "\""
       (ns->module-path import-ns)
       "\";"))

(defn emit-imports
  [analysis]
  (let [current-ns (:ns analysis)]
    (->> (analysis-import-groups analysis)
         (map #(emit-import-declaration current-ns %))
         (str/join "\n"))))

(defn emit-spec-declaration
  [spec]
  (let [current-ns (some-> spec :ns symbol)
        type (:type spec)
        name (export-ts-ident (:name spec))]
    (if (= :record (:kind type))
      (str "export interface " name " "
           (emit-ts-type type current-ns))
      (str "export type " name
            " = "
            (emit-ts-type type current-ns)
            ";"))))

(defn emit-function-arg
  [arg current-ns]
  (str (sanitize-ts-ident (:name arg))
       ": "
       (emit-ts-type (:type arg) current-ns)))

(defn emit-function-declaration
  [fn-def]
  (let [current-ns (some-> fn-def :ns symbol)]
    (str "export type "
         (export-ts-ident (:name fn-def))
         " = ("
         (->> (:inputs fn-def)
              (map-indexed (fn [idx input]
                             (str "arg" idx ": " (emit-ts-type (:type input) current-ns))))
              (str/join ", "))
         ") => "
         (emit-ts-type (:output fn-def) current-ns)
         ";")))

(defn emit-value-declaration
  [value-def]
  (let [current-ns (some-> value-def :ns symbol)]
    (str "export declare const "
         (export-ts-ident (:name value-def))
         ": "
         (emit-ts-type (:type value-def) current-ns)
         ";")))

(defn emitted-specs
  [{:keys [specs functions values]}]
  (let [fn-names    (set (map :name functions))
        value-names (set (map :name values))]
    (remove (fn [spec]
              (let [kind (get-in spec [:type :kind])
                    name (:name spec)]
                (or (and (= :fn kind)
                         (contains? fn-names name))
                    (and (not= :fn kind)
                         (contains? value-names name)))))
            specs)))

(defn emit-analysis-declarations
  [{:keys [specs functions values] :as analysis}]
  (->> (concat
        (when-let [imports (not-empty (emit-imports analysis))]
          [imports])
        (map emit-spec-declaration (emitted-specs analysis))
        (map emit-function-declaration functions)
        (map emit-value-declaration values))
        (str/join "\n\n")))

(defn declaration-output-path
  [output]
  (if (str/includes? output ".")
    (str/replace output #"\.[^./]+$" ".d.ts")
    (str output ".d.ts")))

(defn module-dts-artifact
  [{:keys [main runtime-output]}]
  {:output (declaration-output-path runtime-output)
   :body   (-> main
               mixer/mix-namespace
               emit-analysis-declarations)})

(defn emit-namespace-declarations
  [ns-sym]
  (-> ns-sym
      mixer/mix-namespace
      emit-analysis-declarations))
