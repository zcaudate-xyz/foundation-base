(ns rt.postgres.analyze.report
  "Reporting and validation output for static analysis results.
   
   Generates human-readable reports from analysis data, including
   schema summaries, dependency trees, and validation issue lists."
  (:require [clojure.string :as str]
            [rt.postgres.analyze.deftype :as deftype]
            [rt.postgres.analyze.defn :as defn-analyze]
            [rt.postgres.analyze.defenum :as defenum]))

;;
;; Formatting helpers
;;

(defn- indent
  "Indents a string by n spaces."
  [n s]
  (str (apply str (repeat n " ")) s))

(defn- section-header
  "Formats a section header."
  [title]
  (str "\n" title "\n" (apply str (repeat (count title) "=")) "\n"))

(defn- sub-header
  "Formats a sub-section header."
  [title]
  (str "\n  " title "\n  " (apply str (repeat (count title) "-")) "\n"))

;;
;; Type report
;;

(defn format-column
  "Formats a single column for display."
  [col]
  (let [{:keys [name type required unique scope ref enum]} col
        attrs (cond-> []
                required (conj "NOT NULL")
                unique   (conj "UNIQUE")
                scope    (conj (str "scope:" (clojure.core/name scope)))
                ref      (conj (str "-> " (:ns ref)))
                enum     (conj (str "enum:" (:ns enum))))]
    (str "    " (clojure.core/name name)
         " : " (clojure.core/name type)
         (when (seq attrs) (str "  [" (str/join ", " attrs) "]")))))

(defn format-deftype
  "Formats a deftype analysis for display."
  [analysis]
  (let [{:keys [name docstring entity columns params]} analysis
        lines [(str "  TYPE: " name)
               (when docstring (str "    doc: " (subs docstring 0 (min 60 (count docstring)))
                                    (when (> (count docstring) 60) "...")))
               (when entity
                 (str "    entity: " (pr-str entity)))
               (str "    columns: (" (count columns) ")")]]
    (str/join "\n"
              (concat (remove nil? lines)
                      (map format-column columns)
                      (when (:has-partition analysis)
                        [(str "    partition: " (pr-str (:partition-by params)))])))))

;;
;; Function report
;;

(defn format-param
  "Formats a single parameter for display."
  [{:keys [name type convention]}]
  (str (clojure.core/name type) " " name
       (when convention (str " [" (clojure.core/name convention) "]"))))

(defn format-defn
  "Formats a defn analysis for display."
  [analysis]
  (let [{:keys [name docstring language api-flags props params body-refs]} analysis
        param-str (str/join ", " (map format-param params))
        refs (:type-refs body-refs)
        ops (:pg-ops body-refs)]
    (str/join "\n"
              (remove nil?
                      [(str "  FN: " name "(" param-str ")")
                       (when docstring (str "    doc: " (subs docstring 0 (min 60 (count docstring)))
                                            (when (> (count docstring) 60) "...")))
                       (when (not= :default language) (str "    lang: " language))
                       (when api-flags (str "    api: " (pr-str api-flags)))
                       (when props (str "    props: " (pr-str props)))
                       (when (seq refs) (str "    type-refs: " (str/join ", " (map str refs))))
                       (when (seq ops) (str "    pg-ops: " (str/join ", " (map str ops))))]))))

;;
;; Enum report
;;

(defn format-defenum
  "Formats a defenum analysis for display."
  [analysis]
  (let [{:keys [name values count]} analysis]
    (str "  ENUM: " name " [" count " values: "
         (str/join ", " (map str (take 5 values)))
         (when (> count 5) "...")
         "]")))

;;
;; Validation report
;;

(defn- issue-icon
  "Returns an icon for the issue level."
  [level]
  (case level
    :error   "ERROR"
    :warning "WARN"
    :info    "INFO"
    "?"))

(defn format-issue
  "Formats a single validation issue."
  [issue]
  (str "  [" (issue-icon (:level issue)) "] " (:message issue)))

(defn format-validation-report
  "Formats all validation issues for display."
  [issues]
  (if (empty? issues)
    "  No issues found."
    (str/join "\n" (map format-issue issues))))

;;
;; Full report
;;

(defn generate-file-report
  "Generates a report for a single parsed file."
  [{:keys [ns forms file]}]
  (let [ns-name (or (:name ns) "unknown")
        deftypes (filter #(= :deftype (:type %)) forms)
        defns (filter #(= :defn (:type %)) forms)
        defenums (filter #(= :defenum (:type %)) forms)
        
        type-analyses (keep #(deftype/analyze-deftype (:form %)) deftypes)
        fn-analyses (keep #(defn-analyze/analyze-defn (:form %)) defns)
        enum-analyses (keep #(defenum/analyze-defenum (:form %)) defenums)
        
        type-issues (mapcat deftype/validate-deftype type-analyses)
        fn-issues (mapcat defn-analyze/validate-defn fn-analyses)
        enum-issues (mapcat defenum/validate-defenum enum-analyses)
        all-issues (concat type-issues fn-issues enum-issues)]
    
    (str/join "\n"
              (remove nil?
                      [(str "File: " (or file "stdin"))
                       (str "Namespace: " ns-name)
                       
                       (when (seq enum-analyses)
                         (str (sub-header "Enums")
                              (str/join "\n" (map format-defenum enum-analyses))))
                       
                       (when (seq type-analyses)
                         (str (sub-header "Types")
                              (str/join "\n\n" (map format-deftype type-analyses))))
                       
                       (when (seq fn-analyses)
                         (str (sub-header "Functions")
                              (str/join "\n\n" (map format-defn fn-analyses))))
                       
                       (when (seq all-issues)
                         (str (sub-header "Issues")
                              (format-validation-report all-issues)))]))))

(defn generate-summary-report
  "Generates a summary report across all parsed files."
  [cross-ref-summary]
  (let [{:keys [stats undefined-type-refs undefined-fn-refs
                circular-deps orphaned-types]} cross-ref-summary]
    (str/join "\n"
              (remove nil?
                      [(section-header "Schema Summary")
                       (str "  Types:     " (:type-count stats))
                       (str "  Functions: " (:fn-count stats))
                       (str "  Enums:     " (:enum-count stats))
                       
                       (when (seq undefined-type-refs)
                         (str (sub-header "Undefined Type References")
                              (str/join "\n"
                                        (map (fn [{:keys [source ref kind]}]
                                               (str "  " source " -> " ref " (" (name kind) ")"))
                                             undefined-type-refs))))
                       
                       (when (seq undefined-fn-refs)
                         (str (sub-header "Undefined Function References")
                              (str/join "\n"
                                        (map (fn [{:keys [source ref kind]}]
                                               (str "  " source " -> " ref " (" (name kind) ")"))
                                             undefined-fn-refs))))
                       
                       (when (seq circular-deps)
                         (str (sub-header "Circular Dependencies")
                              (str/join "\n"
                                        (map (fn [cycle]
                                               (str "  " (str/join " -> " (map str cycle))))
                                             circular-deps))))
                       
                       (when (seq orphaned-types)
                         (str (sub-header "Orphaned Types (unreferenced)")
                              (str/join "\n"
                                        (map (fn [t] (str "  " t))
                                             (sort-by str orphaned-types)))))]))))

;;
;; Data export
;;

(defn export-schema-map
  "Exports the analysis as a structured map suitable for JSON/EDN serialization."
  [cross-ref-summary]
  (let [{:keys [type-index fn-index enum-index stats]} cross-ref-summary]
    {:stats stats
     :types (->> type-index
                 (map (fn [[k {:keys [analysis file]}]]
                        [(str k) (-> analysis
                                     (update :name str)
                                     (update :refs #(set (map str %)))
                                     (update :enums #(set (map str %)))
                                     (update :foreign-refs #(set (map str %)))
                                     (assoc :file file))]))
                 (into {}))
     :functions (->> fn-index
                     (map (fn [[k {:keys [analysis file]}]]
                            [(str k) (-> analysis
                                         (update :name str)
                                         (dissoc :body)
                                         (assoc :file file))]))
                     (into {}))
     :enums (->> enum-index
                 (map (fn [[k {:keys [analysis file]}]]
                        [(str k) (-> analysis
                                     (update :name str)
                                     (assoc :file file))]))
                 (into {}))}))
