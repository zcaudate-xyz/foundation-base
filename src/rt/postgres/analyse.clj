(ns rt.postgres.analyze
  "Main API for static analysis of deftype.pg and defn.pg forms.
   
   Provides a unified interface for parsing, analyzing, and reporting
   on the PostgreSQL DSL forms used in the gwdb codebase.
   
   Usage:
     ;; Analyze a single file
     (analyze-file \"src/gwdb/core/system/type_system_user.clj\")
   
     ;; Analyze a directory
     (analyze-directory \"src/gwdb\")
   
     ;; Analyze source string
     (analyze-source \"(deftype.pg User ... [...])\")"
  (:require [rt.postgres.analyze.parse :as parse]
            [rt.postgres.analyze.deftype :as deftype]
            [rt.postgres.analyze.defn :as defn-analyze]
            [rt.postgres.analyze.defenum :as defenum]
            [rt.postgres.analyze.refs :as refs]
            [rt.postgres.analyze.report :as report]))

;;
;; Single form analysis
;;

(defn analyze-form
  "Analyzes a single pg form (deftype.pg, defn.pg, or defenum.pg).
   Returns the appropriate analysis map based on the form type."
  [form]
  (cond
    (and (list? form) (= 'deftype.pg (first form)))
    (assoc (deftype/analyze-deftype form) :form-type :deftype)
    
    (and (list? form) (= 'defn.pg (first form)))
    (assoc (defn-analyze/analyze-defn form) :form-type :defn)
    
    (and (list? form) (= 'defenum.pg (first form)))
    (assoc (defenum/analyze-defenum form) :form-type :defenum)
    
    :else nil))

(defn validate-form
  "Validates a single pg form and returns validation issues."
  [form]
  (let [analysis (analyze-form form)]
    (when analysis
      (case (:form-type analysis)
        :deftype (deftype/validate-deftype analysis)
        :defn    (defn-analyze/validate-defn analysis)
        :defenum (defenum/validate-defenum analysis)
        []))))

;;
;; Source string analysis
;;

(defn analyze-source
  "Analyzes a Clojure source string containing pg forms.
   Returns a map with:
   - :ns        - namespace info
   - :script    - script config  
   - :types     - analyzed deftype.pg forms
   - :functions - analyzed defn.pg forms
   - :enums     - analyzed defenum.pg forms
   - :issues    - all validation issues"
  [source]
  (let [parsed (parse/parse-source source)
        forms (:forms parsed)
        
        types (->> forms
                   (filter #(= :deftype (:type %)))
                   (keep #(deftype/analyze-deftype (:form %)))
                   vec)
        
        functions (->> forms
                       (filter #(= :defn (:type %)))
                       (keep #(defn-analyze/analyze-defn (:form %)))
                       vec)
        
        enums (->> forms
                   (filter #(= :defenum (:type %)))
                   (keep #(defenum/analyze-defenum (:form %)))
                   vec)
        
        issues (concat
                (mapcat deftype/validate-deftype types)
                (mapcat defn-analyze/validate-defn functions)
                (mapcat defenum/validate-defenum enums))]
    
    {:ns (:ns parsed)
     :script (:script parsed)
     :types types
     :functions functions
     :enums enums
     :issues (vec issues)
     :stats {:type-count (count types)
             :fn-count (count functions)
             :enum-count (count enums)
             :issue-count (count issues)}}))

;;
;; File analysis
;;

(defn analyze-file
  "Analyzes a single Clojure source file.
   Returns the same structure as analyze-source plus :file key."
  [path]
  (let [source (slurp path)
        result (analyze-source source)]
    (assoc result :file path)))

;;
;; Directory analysis
;;

(defn analyze-directory
  "Analyzes all .clj files under a directory.
   Returns a map with:
   - :files       - per-file analysis results
   - :cross-refs  - cross-reference summary
   - :report      - formatted summary report string"
  [dir-path]
  (let [parsed-files (parse/parse-directory dir-path)
        
        file-analyses (mapv (fn [parsed]
                              (let [forms (:forms parsed)
                                    types (->> forms
                                               (filter #(= :deftype (:type %)))
                                               (keep #(deftype/analyze-deftype (:form %)))
                                               vec)
                                    functions (->> forms
                                                   (filter #(= :defn (:type %)))
                                                   (keep #(defn-analyze/analyze-defn (:form %)))
                                                   vec)
                                    enums (->> forms
                                               (filter #(= :defenum (:type %)))
                                               (keep #(defenum/analyze-defenum (:form %)))
                                               vec)]
                                (assoc parsed
                                       :types types
                                       :functions functions
                                       :enums enums)))
                            parsed-files)
        
        cross-refs (refs/build-cross-ref-summary parsed-files)
        summary-report (report/generate-summary-report cross-refs)]
    
    {:files file-analyses
     :cross-refs cross-refs
     :report summary-report}))

;;
;; Query helpers
;;

(defn find-type
  "Finds a type by name across analysis results."
  [analysis type-name]
  (let [type-sym (if (symbol? type-name) type-name (symbol type-name))]
    (->> (:files analysis)
         (mapcat :types)
         (filter #(= type-sym (:name %)))
         first)))

(defn find-function
  "Finds a function by name across analysis results."
  [analysis fn-name]
  (let [fn-sym (if (symbol? fn-name) fn-name (symbol fn-name))]
    (->> (:files analysis)
         (mapcat :functions)
         (filter #(= fn-sym (:name %)))
         first)))

(defn find-enum
  "Finds an enum by name across analysis results."
  [analysis enum-name]
  (let [enum-sym (if (symbol? enum-name) enum-name (symbol enum-name))]
    (->> (:files analysis)
         (mapcat :enums)
         (filter #(= enum-sym (:name %)))
         first)))

(defn all-types
  "Returns all types from analysis results."
  [analysis]
  (->> (:files analysis) (mapcat :types) vec))

(defn all-functions
  "Returns all functions from analysis results."
  [analysis]
  (->> (:files analysis) (mapcat :functions) vec))

(defn all-enums
  "Returns all enums from analysis results."
  [analysis]
  (->> (:files analysis) (mapcat :enums) vec))

(defn all-issues
  "Returns all validation issues across all files."
  [analysis]
  (let [files (:files analysis)]
    (->> files
         (mapcat (fn [f]
                   (concat
                    (mapcat deftype/validate-deftype (:types f))
                    (mapcat defn-analyze/validate-defn (:functions f))
                    (mapcat defenum/validate-defenum (:enums f)))))
         vec)))

;;
;; Convenience functions
;;

(defn types-referencing
  "Finds all types that reference a given type name."
  [analysis type-name]
  (let [type-sym (if (symbol? type-name) type-name (symbol type-name))]
    (->> (all-types analysis)
         (filter (fn [t]
                   (some #(= (str type-sym) (name %))
                         (concat (:refs t) (:foreign-refs t)))))
         vec)))

(defn functions-using-type
  "Finds all functions that reference a given type name in their body."
  [analysis type-name]
  (let [type-str (str type-name)]
    (->> (all-functions analysis)
         (filter (fn [f]
                   (some #(= type-str (name %))
                         (get-in f [:body-refs :type-refs]))))
         vec)))

(defn print-report
  "Prints a human-readable report of the analysis."
  [analysis]
  (println (:report analysis)))
