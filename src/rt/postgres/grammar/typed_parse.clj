(ns rt.postgres.grammar.typed-parse
  "Parse rt.postgres DSL forms and extract type information.
   Purely static approach - no runtime evaluation required."
  (:require [rt.postgres.grammar.typed-common :as types]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; ─────────────────────────────────────────────────────────────────────────────
;; Source file reading
;; ─────────────────────────────────────────────────────────────────────────────

(defn read-forms
  "Reads all top-level forms from a Clojure source file."
  [file-path]
  (with-open [r (java.io.PushbackReader. (io/reader file-path))]
    (let [eof (Object.)]
      (loop [forms []]
        (let [form (try
                     (read {:eof eof :read-cond :allow} r)
                     (catch Exception e
                       eof))]
          (if (identical? form eof)
            forms
            (recur (conj forms form))))))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Form identification
;; ─────────────────────────────────────────────────────────────────────────────

(defn deftype? [form] (and (seq? form) (= "deftype.pg" (name (first form)))))
(defn defenum? [form] (and (seq? form) (= "defenum.pg" (name (first form)))))
(defn defn? [form] (and (seq? form) (= "defn.pg" (name (first form)))))
(defn script? [form] (and (seq? form) (= "script" (name (first form))) (= :postgres (second form))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Parsing Logic
;; ─────────────────────────────────────────────────────────────────────────────

(defn- parse-schema [form]
  (let [opts (nth form 2 nil)
        all-schema (get-in opts [:static :all :schema])
        seed-schema (get-in opts [:static :seed :schema])]
    (or (first all-schema) (first seed-schema))))

(defn- extract-aliases
  "Extracts namespace aliases from require forms.
   Input: [[ns1 :as alias1] [ns2 :as alias2]]
   Output: {alias1 ns1, alias2 ns2}"
  [require-forms]
  (reduce (fn [acc form]
            (if (and (vector? form) (= 3 (count form)))
              (let [[ns-sym _ alias-sym] form]
                (if (= :as (second form))
                  (assoc acc alias-sym ns-sym)
                  acc))
              acc))
          {}
          require-forms))

(defn- parse-aliases [form]
  "Extracts namespace aliases from a script form's :require option."
  (let [opts (nth form 2 nil)
        require-forms (get-in opts [:require])]
    (extract-aliases require-forms)))

(defn- parse-process-constraints [process]
  (when (sequential? process)
    (reduce (fn [acc directive]
              (if (sequential? directive)
                (let [fn-name (name (first directive))
                      args (rest directive)]
                  (case fn-name
                    "as-limit-length" (assoc acc :max-length (first args))
                    "as-lower-formatted" (assoc acc :format :handle)
                    "as-email" (assoc acc :format :email)
                    "as-url" (assoc acc :format :uri)
                    acc))
                acc))
            {}
            process)))

(defn parse-column-spec [[col-name col-opts]]
  (let [opts (if (map? col-opts) col-opts {})
        base-type (get opts :type :unknown)
        required? (boolean (get opts :required false))
        enum-ref (:enum opts)
        ref-info (:ref opts)
        sql-process (get-in opts [:sql :process])
        type-ref (cond
                   enum-ref (types/make-type-ref :enum (:ns enum-ref) base-type)
                   (= :ref base-type) (types/make-type-ref :ref (:ns ref-info) base-type)
                   :else (types/make-type-ref :primitive nil base-type))]
    (types/make-column-def
     col-name type-ref
     {:required required?
      :default (or (get-in opts [:sql :default]) (get opts :default))
      :constraints (merge (when required? {:required true})
                          (when (get opts :unique) {:unique true})
                          (when (get opts :primary) {:primary true})
                          (parse-process-constraints sql-process))
      :enum-ref enum-ref
      :scope (get opts :scope)
      :map-schema (get opts :map)
      :ref-info ref-info})))

(defn parse-deftype [form ns-name dbschema]
  (let [rest-form (rest form)
        meta-data (meta (first form))
        [type-sym & after-name] rest-form
        type-name (name type-sym)
        entity-meta (or (:! (meta type-sym)) (:! meta-data))
        [docstring remaining] (if (string? (first after-name)) [(first after-name) (rest after-name)] [nil after-name])
        [attr-map remaining] (if (and (map? (first remaining)) (:added (first remaining))) [(first remaining) (rest remaining)] [nil remaining])
        combined-meta (merge meta-data (meta type-sym) attr-map)
        col-vec (first remaining)
        columns (when (vector? col-vec) (->> (partition 2 col-vec) (mapv #(parse-column-spec [(keyword (name (first %))) (second %)]))))
        primary-keys (->> columns (filter #(get-in % [:constraints :primary])) (mapv :name))
        addons (when (and entity-meta (seq? entity-meta))
                 (let [e-args (when (symbol? (first entity-meta)) (second entity-meta))]
                   (when (map? e-args) (:addons e-args))))]
    (types/make-table-def ns-name type-name columns
                          (cond
                            (empty? primary-keys) :id
                            (= 1 (count primary-keys)) (first primary-keys)
                            :else primary-keys)
                          addons entity-meta
                          (or (:static/schema combined-meta) dbschema))))

(defn parse-defenum [form ns-name dbschema]
  (let [enum-sym (second form)
        enum-name (name enum-sym)
        remaining (drop 2 form)
        values-vec (first (filter vector? remaining))]
    (types/->EnumDef ns-name enum-name (set (map keyword values-vec)) dbschema)))

(defn parse-fn-inputs [args-form]
  (when (sequential? args-form)
    (loop [result [] items (seq args-form) current-type nil]
      (if-not items result
        (let [item (first items)]
          (cond
            (keyword? item) (recur result (next items) item)
            (symbol? item) (recur (conj result (types/->FnArg item (or current-type :unknown) (when current-type [current-type]))) (next items) nil)
            :else (recur result (next items) current-type)))))))

(defn parse-defn [form ns-name dbschema]
  (let [fn-sym (second form)
        fn-name (name fn-sym)
        remaining (drop 2 form)
        [docstring remaining] (if (string? (first remaining)) [(first remaining) (rest remaining)] [nil remaining])
        [attr-map remaining] (if (and (map? (first remaining)) (not (contains? (first remaining) :type))) [(first remaining) (rest remaining)] [nil remaining])
        combined-meta (merge (meta (first form)) (meta fn-sym) attr-map)
        [args-form body] (if (vector? (first remaining)) [(first remaining) (rest remaining)] [(first (first remaining)) (rest (first remaining))])
        inputs (parse-fn-inputs args-form)
        return-type (or (get combined-meta :-) [:jsonb])]
    (types/make-fn-def ns-name fn-name inputs return-type
                       (merge combined-meta
                              {:raw-body (vec body)
                               :expose (get-in combined-meta [:api/flags :expose])
                               :docstring docstring})
                       (or (:static/schema combined-meta) dbschema))))

(defn- transform-col-opts
  "Transforms app/runtime column opts to deftype.pg-style parse input."
  [opts]
  (if-let [link (get-in opts [:ref :link])]
    (assoc opts :ref {:key (keyword (str (:module link)) (name (:id link)))})
    opts))

(defn parse-runtime-table
  "Parses a table from app/common-application table entries."
  [table-name table-entries & [dbschema]]
  (let [columns (->> (partition 2 table-entries)
                     (mapv (fn [[col-name opts]]
                             (parse-column-spec
                              [col-name (transform-col-opts opts)]))))
        primary-key (->> columns
                         (filter #(get-in % [:constraints :primary]))
                         first
                         :name
                         (or :id))]
    (types/make-table-def (namespace table-name)
                          (name table-name)
                          columns primary-key nil nil dbschema)))

(defn analyze-tables
  "Builds typed table defs directly from common-application tables."
  [tables-map & [dbschema]]
  {:enums []
   :functions []
   :tables (->> tables-map
                (map (fn [[table-name entries]]
                       (parse-runtime-table table-name entries dbschema)))
                (vec))})

;; ─────────────────────────────────────────────────────────────────────────────
;; Analysis API
;; ─────────────────────────────────────────────────────────────────────────────

(defn analyze-file [file-path]
  (let [forms (read-forms file-path)
        ns-name (-> (str file-path) (str/replace #"^.*src/" "") (str/replace #"\.clj$" "") (str/replace #"/" ".") (str/replace #"_" "-"))
        script-form (some #(when (script? %) %) forms)
        dbschema (when script-form (parse-schema script-form))
        aliases (when script-form (parse-aliases script-form))]
    (reduce (fn [acc form]
              (cond
                (deftype? form) (update acc :tables conj (parse-deftype form ns-name dbschema))
                (defenum? form) (update acc :enums conj (parse-defenum form ns-name dbschema))
                (defn? form) (update acc :functions conj
                                     (assoc-in (parse-defn form ns-name dbschema)
                                               [:body-meta :aliases] aliases))
                :else acc))
            {:enums [] :tables [] :functions []}
            forms)))

(defn register-types! [analysis]
  (doseq [enum (:enums analysis)]
    (let [k (symbol (or (:ns enum) "") (:name enum))]
      (types/register-type! k enum)))
  (doseq [table (:tables analysis)]
    (let [k (symbol (or (:ns table) "") (:name table))]
      (types/register-type! k table)))
  (doseq [func (:functions analysis)]
    (let [k (symbol (or (:ns func) "") (:name func))]
      (types/register-type! k func)))
  analysis)

(defn analyze-namespace [ns-sym]
  (let [ns-str (str ns-sym)
        rel-path (-> ns-str (str/replace #"\." "/") (str/replace #"-" "_") (str ".clj"))
        file-paths [(str "src/" rel-path) (str "clojure/src/" rel-path)]
        target-file (first (filter #(.exists (io/file %)) file-paths))]
    (if target-file
      (analyze-file target-file)
      {:enums [] :tables [] :functions []})))
