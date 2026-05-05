(ns hara.model.sql.spec-common.common
  (:require [clojure.string :as str]
            [hara.common.emit :as emit]
            [hara.common.emit-helper :as helper]
            [hara.lang.book :as book]
            [hara.lang.pointer :as ptr]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]))

(declare sql-enum-values)

(defn sql-dialect
  "gets the active sql dialect"
  {:added "4.1"}
  [grammar mopts]
  (merge (or (:dialect grammar) {})
         (or (:dialect mopts) {})))

(defn sql-string
  "renders a sql string literal"
  {:added "4.1"}
  [s]
  (helper/pr-single s))

(defn sql-ident-base
  "renders the base identifier name"
  {:added "4.1"}
  [x]
  (-> (cond (keyword? x) (name x)
            (symbol? x)  (name x)
            (string? x)  x
            :else        (str x))
      (str/replace "-" "_")))

(defn sql-ident
  "renders a sql identifier"
  {:added "4.1"}
  ([x]
   (sql-ident x nil nil))
  ([x grammar mopts]
   (let [{:keys [identifier-style]
          :or {identifier-style :quoted}} (sql-dialect grammar mopts)
         base (sql-ident-base x)]
     (case identifier-style
       :plain base
       :upper (str/upper-case base)
       (str "\""
            (str/replace base "\"" "\"\"")
            "\"")))))

(defn sql-qualified-ident
  "renders a qualified identifier"
  {:added "4.1"}
  ([x schema]
   (sql-qualified-ident x schema nil nil))
  ([x schema grammar mopts]
   (if schema
     (str (sql-ident schema grammar mopts)
          "."
          (sql-ident x grammar mopts))
     (sql-ident x grammar mopts))))

(defn sql-type-name
  "renders a sql type name"
  {:added "4.1"}
  ([type]
   (sql-type-name type nil nil))
  ([type grammar mopts]
   (let [{:keys [type-alias]} (sql-dialect grammar mopts)]
     (cond (vector? type)
           (str/join " " (map #(sql-type-name % grammar mopts) type))

           (keyword? type)
           (or (get type-alias type)
               (-> type name (str/replace "-" " ") str/upper-case))

           (symbol? type)
           (sql-ident type grammar mopts)

           (string? type)
           type

           :else
           (str type)))))

(defn sql-sym-meta
  "normalises sql symbol metadata"
  {:added "4.1"}
  [sym]
  (let [{return :-
         schema :schema
         :or {return [:text]}
         :as msym} (meta sym)]
    (assoc msym
           :- return
           :schema schema
           :static/return return)))

(defn sql-hydrate
  "hydrates top level sql entries with static metadata"
  {:added "4.1"}
  [[op sym & body] grammar _mopts]
  (let [reserved (collection/qualified-keys (get-in grammar [:reserved op])
                                            :static)
        static   (merge reserved
                        (select-keys (meta sym)
                                     [:static/return
                                      :static/dbtype
                                      :static/enum-values]))]
    [static
     (apply list op
            (with-meta sym (merge (meta sym) static))
            body)]))

(defn sql-indent
  "indents text by a fixed number of spaces"
  {:added "4.1"}
  [text spaces]
  (let [pad (apply str (repeat spaces " "))]
    (->> (str/split-lines text)
         (map (fn [line]
                (if (str/blank? line)
                  line
                  (str pad line))))
         (str/join "\n"))))

(defn sql-resolve-entry
  "resolves a language entry from a symbol"
  {:added "4.1"}
  [sym]
  (when (symbol? sym)
    (when-let [v (resolve sym)]
      (let [val @v]
        (cond (nil? val) nil
              (book/book-entry? val) val
              :else (f/suppress (ptr/get-entry val)))))))

(defn sql-enum-entry
  "returns enum entry metadata if present"
  {:added "4.1"}
  [type]
  (let [entry (sql-resolve-entry type)]
    (when (= :enum (:static/dbtype entry))
      entry)))

(defn sql-enum-values-from-type
  "returns enum values for a type if it resolves to an enum entry"
  {:added "4.1"}
  [type]
  (:static/enum-values (sql-enum-entry type)))

(defn sql-render
  "renders a generic sql value"
  {:added "4.1"}
  [value grammar mopts]
  (let [{:keys [bool-literal]
         :or {bool-literal {true "TRUE" false "FALSE"}}} (sql-dialect grammar mopts)]
    (cond (nil? value) "NULL"
          (string? value) (sql-string value)
          (keyword? value) (sql-string (name value))
          (boolean? value) (get bool-literal value)
          (number? value) (str value)
          (or (list? value)
              (vector? value)
              (map? value)
              (set? value))
          (emit/emit-main value grammar mopts)
          (symbol? value) (sql-ident value grammar mopts)
          :else (str value))))

(defn sql-body
  "renders a sql function body"
  {:added "4.1"}
  [body grammar mopts]
  (->> body
       (map (fn [form]
              (let [line (if (string? form)
                           form
                           (emit/emit-main form grammar mopts))]
                (cond-> line
                  (and (not (str/blank? line))
                       (not (str/ends-with? line ";")))
                  (str ";")))))
       (remove str/blank?)
       (str/join "\n")))

(defn sql-column-spec
  "normalises a table column spec"
  {:added "4.1"}
  [spec]
  (when (odd? (count spec))
    (f/error "Column spec must contain pairs" {:spec spec}))
  (mapv vec (partition 2 spec)))

(defn sql-column-name
  "renders the column name"
  {:added "4.1"}
  [k {:keys [type]} grammar mopts]
  (if (= :ref type)
    (sql-ident (str (sql-ident-base k) "_id") grammar mopts)
    (sql-ident k grammar mopts)))

(defn sql-column-type
  "renders the column type"
  {:added "4.1"}
  [{:keys [type ref ref-type]} grammar mopts]
  (cond (= :ref type)
        (sql-type-name (or ref-type
                           (:type ref)
                           :uuid)
                       grammar
                       mopts)

        :else
        (sql-type-name type grammar mopts)))

(defn sql-reference-target
  "renders the referenced table"
  {:added "4.1"}
  [ref grammar mopts]
  (let [table (or (:table ref)
                  (:name ref)
                  (:ns ref)
                  ref)]
    (cond (map? table)
          (sql-qualified-ident (:name table) (:schema table) grammar mopts)

          (symbol? table)
          (sql-ident table grammar mopts)

          (keyword? table)
          (sql-ident table grammar mopts)

          (string? table)
          (sql-ident table grammar mopts)

          :else
          (sql-ident table grammar mopts))))

(defn sql-reference-column
  "renders the referenced column"
  {:added "4.1"}
  [ref grammar mopts]
  (sql-ident (or (:column ref) :id) grammar mopts))

(defn sql-column-definition
  "renders a sql column definition"
  {:added "4.1"}
  [[k attrs] grammar mopts]
  (when-not (map? attrs)
    (f/error "Column attrs must be a map" {:column k :attrs attrs}))
  (let [{:keys [primary required unique default ref sql type]} attrs
        {:keys [enum-column-mode enum-column-type]
         :or {enum-column-mode :native
              enum-column-type "VARCHAR2(255)"}} (sql-dialect grammar mopts)
        enum-values (sql-enum-values-from-type type)
        cname (sql-column-name k attrs grammar mopts)
        ctype (if (and enum-values
                       (not= :native enum-column-mode))
                enum-column-type
                (sql-column-type attrs grammar mopts))
        pieces (cond-> [cname ctype]
                 primary  (conj "PRIMARY KEY")
                 required (conj "NOT NULL")
                 unique   (conj "UNIQUE")
                 (contains? attrs :default)
                 (conj "DEFAULT" (sql-render default grammar mopts))
                 ref
                 (conj "REFERENCES"
                       (sql-reference-target ref grammar mopts)
                       (str "(" (sql-reference-column ref grammar mopts) ")"))
                 (and enum-values (= :check enum-column-mode))
                 (conj (str "CHECK (" cname
                            " IN ("
                            (str/join ", " (sql-enum-values enum-values))
                            "))"))
                 (get-in sql [:raw])
                 (conj (get-in sql [:raw])))]
    (str/join " " pieces)))

(defn sql-enum-values
  "renders enum literal values"
  {:added "4.1"}
  [values]
  (mapv (fn [value]
          (cond (keyword? value) (sql-string (name value))
                (symbol? value)  (sql-string (name value))
                :else            (sql-string (str value))))
        values))
