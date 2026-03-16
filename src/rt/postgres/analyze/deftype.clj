(ns rt.postgres.analyze.deftype
  "Static analysis for deftype.pg forms.
   
   Extracts type name, columns, metadata, partitions, and references from
   forms like:
   
     (deftype.pg ^{:! (et/E {:class :0d/entry :addons [...]})}
       User \"docstring\" {:added \"0.1\"}
       [:handle {:type :citext :scope :-/info :unique true}
        :type   {:type :enum :enum {:ns -/EnumUserType}}
        :org    {:type :ref :ref {:ns -/Organisation}}]
       {:partition-by {:strategy :hash :columns [:org-id]}})"
  (:require [clojure.string :as str]))

;;
;; Column analysis
;;

(defn parse-column-spec
  "Parses a single column key-value pair from the spec vector.
   Returns a map describing the column."
  [[col-key attrs]]
  (let [{:keys [type scope primary required unique enum ref foreign
                sql map process web generated ignore priority]} attrs]
    (cond-> {:name col-key
             :type type}
      scope     (assoc :scope scope)
      primary   (assoc :primary primary)
      required  (assoc :required true)
      unique    (assoc :unique unique)
      enum      (assoc :enum enum)
      ref       (assoc :ref ref)
      foreign   (assoc :foreign foreign)
      sql       (assoc :sql sql)
      map       (assoc :map-spec map)
      process   (assoc :process process)
      web       (assoc :web web)
      generated (assoc :generated generated)
      ignore    (assoc :ignore true)
      priority  (assoc :priority priority))))

(defn parse-columns
  "Parses the column spec vector into a sequence of column maps.
   The spec is a flat vector of alternating keys and attr maps:
   [:col1 {:type ...} :col2 {:type ...}]"
  [spec]
  (when (vector? spec)
    (->> (partition 2 spec)
         (mapv parse-column-spec))))

;;
;; Entity metadata extraction
;;

(defn extract-entity-config
  "Extracts the entity configuration from the :! metadata key.
   The :! key typically contains (et/E {...}) which we parse
   as a list with the config map inside."
  [meta-bang]
  (cond
    ;; Direct map
    (map? meta-bang)
    meta-bang
    
    ;; (et/E {:class ...}) - a function call form
    (and (list? meta-bang)
         (>= (count meta-bang) 2))
    (let [config (second meta-bang)]
      (if (map? config) config {}))
    
    :else {}))

;;
;; Main analysis
;;

(defn analyze-deftype
  "Analyzes a deftype.pg form and returns a map with:
   - :name         - type symbol name
   - :docstring    - documentation string
   - :attr-map     - {:added ...} map
   - :metadata     - full metadata from ^{...}
   - :entity       - entity config from :! key (class, addons, etc.)
   - :columns      - vector of column analysis maps
   - :params       - additional params map (partition-by, etc.)
   - :refs         - set of referenced types (from :ref columns)
   - :enums        - set of referenced enums (from :enum columns)
   
   Example form:
     (deftype.pg ^{:! (et/E {:class :0d/entry})}
       User \"docstring\" {:added \"0.1\"}
       [:handle {:type :citext}]
       {:partition-by ...})"
  [form]
  (when (and (list? form) (= 'deftype.pg (first form)))
    (let [parts (rest form)
          ;; Extract symbol (first symbol)
          sym (first (filter symbol? parts))
          sym-meta (or (meta sym) {})
          
          ;; After the symbol, find docstring, attr-map, spec vector, and params
          after-sym (rest (drop-while #(not (= % sym)) parts))
          
          ;; Docstring (optional)
          docstring (when (string? (first after-sym))
                      (first after-sym))
          after-doc (if docstring (rest after-sym) after-sym)
          
          ;; Attr map like {:added "0.1"} (optional)
          attr-map (when (and (map? (first after-doc))
                              (not (vector? (first after-doc))))
                     (first after-doc))
          after-attr (if attr-map (rest after-doc) after-doc)
          
          ;; Column spec vector
          spec (first (filter vector? after-attr))
          
          ;; Params map (after the spec vector)
          after-spec (rest (drop-while #(not (vector? %)) after-attr))
          params (first (filter map? after-spec))
          
          ;; Parse columns
          columns (parse-columns spec)
          
          ;; Extract entity config from :! metadata
          entity-config (when-let [bang (:! sym-meta)]
                          (extract-entity-config bang))
          
          ;; Collect referenced types (from :ref columns)
          refs (->> columns
                    (filter :ref)
                    (map (comp :ns :ref))
                    (remove nil?)
                    set)
          
          ;; Collect referenced enums
          enums (->> columns
                     (filter :enum)
                     (map (comp :ns :enum))
                     (remove nil?)
                     set)
          
          ;; Collect foreign key references
          foreign-refs (->> columns
                            (filter :foreign)
                            (mapcat (fn [col]
                                      (map (fn [[_ v]] (:ns v)) (:foreign col))))
                            (remove nil?)
                            set)]
      
      {:name sym
       :docstring docstring
       :attr-map attr-map
       :metadata (dissoc sym-meta :line :column :file)
       :entity entity-config
       :columns (vec columns)
       :params params
       :refs refs
       :enums enums
       :foreign-refs foreign-refs
       :column-count (count columns)
       :has-partition (boolean (:partition-by params))})))

;;
;; Column queries
;;

(defn ref-columns
  "Returns columns that are :ref type from an analyzed deftype."
  [analysis]
  (filter #(= :ref (:type %)) (:columns analysis)))

(defn enum-columns
  "Returns columns that are :enum type from an analyzed deftype."
  [analysis]
  (filter #(= :enum (:type %)) (:columns analysis)))

(defn required-columns
  "Returns columns marked as :required from an analyzed deftype."
  [analysis]
  (filter :required (:columns analysis)))

(defn unique-columns
  "Returns columns with :unique constraints from an analyzed deftype."
  [analysis]
  (filter :unique (:columns analysis)))

(defn primary-columns
  "Returns columns marked as :primary from an analyzed deftype."
  [analysis]
  (filter :primary (:columns analysis)))

;;
;; Validation
;;

(defn validate-deftype
  "Validates a deftype analysis result. Returns a vector of issues."
  [analysis]
  (let [{:keys [name columns entity params]} analysis
        issues (transient [])]
    
    ;; Check name starts with uppercase
    (when (and name (not (Character/isUpperCase (first (str name)))))
      (conj! issues {:level :warning
                     :message (str "Type name '" name "' should start with uppercase")
                     :name name}))
    
    ;; Check for columns without :type
    (doseq [col columns]
      (when-not (:type col)
        (conj! issues {:level :error
                       :message (str "Column '" (:name col) "' in type '" name "' has no :type")
                       :name name
                       :column (:name col)})))
    
    ;; Check ref columns have :ns
    (doseq [col (ref-columns analysis)]
      (when-not (get-in col [:ref :ns])
        (conj! issues {:level :error
                       :message (str "Ref column '" (:name col) "' in type '" name "' missing :ref :ns")
                       :name name
                       :column (:name col)})))
    
    ;; Check enum columns have :ns
    (doseq [col (enum-columns analysis)]
      (when-not (get-in col [:enum :ns])
        (conj! issues {:level :error
                       :message (str "Enum column '" (:name col) "' in type '" name "' missing :enum :ns")
                       :name name
                       :column (:name col)})))
    
    ;; Check entity class is specified
    (when (and entity (not (:class entity)))
      (conj! issues {:level :warning
                     :message (str "Type '" name "' has entity config but no :class specified")
                     :name name}))
    
    (persistent! issues)))
