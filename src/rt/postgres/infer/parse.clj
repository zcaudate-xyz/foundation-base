(ns rt.postgres.infer.parse
    "Parse rt.postgres DSL forms and extract type information.
   Purely static approach - no runtime evaluation required."
    (:require [rt.postgres.infer.types :as types]
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

;; ─────────────────────────────────────────────────────────────────────────────
;; Parsing Logic
;; ─────────────────────────────────────────────────────────────────────────────

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

(defn parse-deftype [form ns-name]
      (let [rest-form (rest form)
            meta-data (meta (first form))
            [type-sym & after-name] rest-form
            type-name (name type-sym)
            entity-meta (or (:! (meta type-sym)) (:! meta-data))
            [docstring remaining] (if (string? (first after-name)) [(first after-name) (rest after-name)] [nil after-name])
            [attr-map remaining] (if (and (map? (first remaining)) (:added (first remaining))) [(first remaining) (rest remaining)] [nil remaining])
            col-vec (first remaining)
            columns (when (vector? col-vec) (->> (partition 2 col-vec) (mapv #(parse-column-spec [(keyword (name (first %))) (second %)]))))
            addons (when (and entity-meta (seq? entity-meta))
                         (let [e-args (when (symbol? (first entity-meta)) (second entity-meta))]
                              (when (map? e-args) (:addons e-args))))]
           (types/make-table-def ns-name type-name columns
                                 (or (->> columns (filter #(get-in % [:constraints :primary])) first :name) :id)
                                 addons entity-meta)))

(defn parse-defenum [form ns-name]
      (let [enum-sym (second form)
            enum-name (name enum-sym)
            remaining (drop 2 form)
            values-vec (first (filter vector? remaining))]
           (types/make-enum-def ns-name enum-name (set (map keyword values-vec)))))

(defn parse-fn-inputs [args-form]
      (when (sequential? args-form)
            (loop [result [] items (seq args-form) current-type nil]
                  (if-not items result
                          (let [item (first items)]
                               (cond
                                (keyword? item) (recur result (next items) item)
                                (symbol? item) (recur (conj result (types/->FnArg item (or current-type :unknown) (when current-type [current-type]))) (next items) nil)
                                :else (recur result (next items) current-type)))))))

(defn parse-defn [form ns-name]
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
                                      :docstring docstring}))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Analysis API
;; ─────────────────────────────────────────────────────────────────────────────

(defn analyze-file [file-path]
      (let [forms (read-forms file-path)
            ns-name (-> (str file-path) (str/replace #"^.*src/" "") (str/replace #"\.clj$" "") (str/replace #"/" ".") (str/replace #"_" "-"))]
           (reduce (fn [acc form]
                       (cond
                        (deftype? form) (update acc :tables conj (parse-deftype form ns-name))
                        (defenum? form) (update acc :enums conj (parse-defenum form ns-name))
                        (defn? form) (update acc :functions conj (parse-defn form ns-name))
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
