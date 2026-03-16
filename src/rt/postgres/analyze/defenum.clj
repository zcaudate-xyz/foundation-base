(ns rt.postgres.analyze.defenum
  "Static analysis for defenum.pg forms.
   
   Extracts enum name, values, and metadata from forms like:
   
     (defenum.pg ^{} EnumUserType [:participant :creator])
     (defenum.pg EnumClassType [\"Global\" \"User\" \"Organisation\"])")

;;
;; defenum.pg form structure:
;;   (defenum.pg ^{metadata} Name [values...])
;;   (defenum.pg Name [values...])
;;

(defn analyze-defenum
  "Analyzes a defenum.pg form and returns a map with:
   - :name       - enum symbol name
   - :values     - vector of enum values (keywords or strings)
   - :metadata   - metadata map from ^{...}
   - :ns-prefix  - the namespace prefix if name contains /
   
   Example input:
     (defenum.pg ^{} EnumUserType [:participant :creator])
   
   Returns:
     {:name 'EnumUserType
      :values [:participant :creator]
      :metadata {}}"
  [form]
  (when (and (list? form) (= 'defenum.pg (first form)))
    (let [parts (rest form)
          ;; Find the symbol name (first symbol after defenum.pg)
          sym (first (filter symbol? parts))
          sym-meta (or (meta sym) {})
          ;; Find the values vector (first vector or list of values)
          values (first (filter #(or (vector? %) (and (list? %) (not (symbol? (first %))))) parts))
          ;; Normalize values
          values (vec values)]
      {:name sym
       :values values
       :metadata (dissoc sym-meta :line :column :file)
       :value-types (cond
                      (every? keyword? values) :keyword
                      (every? string? values) :string
                      :else :mixed)
       :count (count values)})))

(defn enum-value-names
  "Returns the string names of enum values, regardless of whether
   they are keywords or strings."
  [enum-analysis]
  (mapv (fn [v]
          (if (keyword? v) (name v) (str v)))
        (:values enum-analysis)))

(defn validate-defenum
  "Validates a defenum analysis result. Returns a vector of issues found."
  [enum-analysis]
  (let [{:keys [name values value-types]} enum-analysis
        issues (transient [])]
    
    ;; Check naming convention
    (when (and name (not (re-matches #"^Enum[A-Z].*" (str name))))
      (conj! issues {:level :warning
                     :message (str "Enum name '" name "' does not follow EnumXxx convention")
                     :name name}))
    
    ;; Check for empty values
    (when (empty? values)
      (conj! issues {:level :error
                     :message (str "Enum '" name "' has no values")
                     :name name}))
    
    ;; Check for mixed value types
    (when (= :mixed value-types)
      (conj! issues {:level :warning
                     :message (str "Enum '" name "' has mixed value types (keywords and strings)")
                     :name name}))
    
    ;; Check for duplicates
    (let [dupes (filter (fn [[_ cnt]] (> cnt 1))
                        (frequencies (enum-value-names enum-analysis)))]
      (when (seq dupes)
        (conj! issues {:level :error
                       :message (str "Enum '" name "' has duplicate values: " (keys dupes))
                       :name name})))
    
    (persistent! issues)))
