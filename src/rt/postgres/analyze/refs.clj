(ns rt.postgres.analyze.refs
  "Cross-reference analysis for deftype.pg and defn.pg forms.
   
   Builds dependency graphs between types, functions, and enums.
   Detects undefined references, circular dependencies, and orphaned entities."
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [rt.postgres.analyze.deftype :as deftype]
            [rt.postgres.analyze.defn :as defn-analyze]
            [rt.postgres.analyze.defenum :as defenum]))

;;
;; Symbol resolution
;;

(defn resolve-local-ref
  "Resolves a local reference (prefixed with -/) to a fully qualified name
   using the namespace context.
   
   -/User in namespace gwdb.core.system.type-system-user -> User (local)"
  [sym ns-name]
  (when (and (symbol? sym) (= "-" (namespace sym)))
    {:local true
     :name (symbol (name sym))
     :source-ns ns-name}))

(defn resolve-aliased-ref
  "Resolves an aliased reference (e.g., tsb/Rev) using the require aliases.
   
   tsb/Rev with alias tsb -> gwdb.core.system.type-system-base -> Rev"
  [sym aliases]
  (when (and (symbol? sym) (namespace sym) (not= "-" (namespace sym)))
    (let [alias-str (namespace sym)
          target-ns (get aliases alias-str)]
      {:local false
       :alias alias-str
       :name (symbol (name sym))
       :target-ns target-ns})))

;;
;; Index building
;;

(defn build-type-index
  "Builds an index of all deftype.pg definitions across parsed files.
   Returns a map of type-name -> {:ns namespace :analysis analysis :file file}."
  [parsed-files]
  (->> parsed-files
       (mapcat (fn [{:keys [ns forms file]}]
                 (->> forms
                      (filter #(= :deftype (:type %)))
                      (map (fn [{:keys [form]}]
                             (when-let [analysis (deftype/analyze-deftype form)]
                               [(:name analysis)
                                {:ns (:name ns)
                                 :analysis analysis
                                 :file file}]))))))
       (remove nil?)
       (into {})))

(defn build-fn-index
  "Builds an index of all defn.pg definitions across parsed files.
   Returns a map of fn-name -> {:ns namespace :analysis analysis :file file}."
  [parsed-files]
  (->> parsed-files
       (mapcat (fn [{:keys [ns forms file]}]
                 (->> forms
                      (filter #(= :defn (:type %)))
                      (map (fn [{:keys [form]}]
                             (when-let [analysis (defn-analyze/analyze-defn form)]
                               [(:name analysis)
                                {:ns (:name ns)
                                 :analysis analysis
                                 :file file}]))))))
       (remove nil?)
       (into {})))

(defn build-enum-index
  "Builds an index of all defenum.pg definitions across parsed files.
   Returns a map of enum-name -> {:ns namespace :analysis analysis :file file}."
  [parsed-files]
  (->> parsed-files
       (mapcat (fn [{:keys [ns forms file]}]
                 (->> forms
                      (filter #(= :defenum (:type %)))
                      (map (fn [{:keys [form]}]
                             (when-let [analysis (defenum/analyze-defenum form)]
                               [(:name analysis)
                                {:ns (:name ns)
                                 :analysis analysis
                                 :file file}]))))))
       (remove nil?)
       (into {})))

(defn build-alias-map
  "Builds a map of alias -> namespace from script require declarations.
   
   Given script config like:
     {:require [[rt.postgres :as pg]
                [gwdb.core.system.type-system-base :as tsb]]}
   
   Returns: {\"pg\" rt.postgres, \"tsb\" gwdb.core.system.type-system-base}"
  [script-config]
  (when-let [requires (:require (:config script-config))]
    (->> requires
         (filter vector?)
         (keep (fn [req]
                 (let [pairs (partition 2 (rest req))]
                   (when-let [alias (second (first (filter #(= :as (first %)) pairs)))]
                     [(str alias) (first req)]))))
         (into {}))))

;;
;; Dependency graph
;;

(defn build-type-deps
  "Builds a dependency map for types: type-name -> set of type names it depends on."
  [type-index]
  (->> type-index
       (map (fn [[type-name {:keys [analysis ns]}]]
              (let [refs (:refs analysis)
                    enums (:enums analysis)
                    foreign (:foreign-refs analysis)
                    ;; Resolve -/ references to local type names
                    local-refs (->> (concat refs enums foreign)
                                    (filter #(and (symbol? %) (= "-" (namespace %))))
                                    (map #(symbol (name %)))
                                    set)
                    ;; Non-local refs (qualified)
                    external-refs (->> (concat refs enums foreign)
                                       (filter #(and (symbol? %) (not= "-" (namespace %))))
                                       (map #(symbol (name %)))
                                       set)]
                [type-name (set/union local-refs external-refs)])))
       (into {})))

(defn build-fn-deps
  "Builds a dependency map for functions: fn-name -> {:types #{...} :fns #{...}}."
  [fn-index]
  (->> fn-index
       (map (fn [[fn-name {:keys [analysis]}]]
              (let [{:keys [type-refs fn-refs]} (:body-refs analysis)]
                [fn-name {:types (set (map #(symbol (name %)) type-refs))
                          :fns (set (map #(symbol (name %)) fn-refs))}])))
       (into {})))

;;
;; Validation
;;

(defn find-undefined-type-refs
  "Finds type references that don't resolve to any known type or enum.
   Returns a vector of {:source type-name :ref symbol :kind :type/:enum}."
  [type-index enum-index]
  (let [all-type-names (set (keys type-index))
        all-enum-names (set (keys enum-index))
        all-names (set/union all-type-names all-enum-names)]
    (->> type-index
         (mapcat (fn [[type-name {:keys [analysis]}]]
                   (let [refs (set/union (:refs analysis) (:enums analysis) (:foreign-refs analysis))
                         resolved-names (->> refs
                                             (map #(symbol (name %)))
                                             set)
                         undefined (set/difference resolved-names all-names)]
                     (map (fn [undef]
                            {:source type-name
                             :ref undef
                             :kind (if (str/starts-with? (str undef) "Enum") :enum :type)})
                          undefined))))
         vec)))

(defn find-undefined-fn-refs
  "Finds function references that don't resolve to any known function.
   Only checks local (-/) references."
  [fn-index type-index]
  (let [all-fn-names (set (keys fn-index))
        all-type-names (set (keys type-index))]
    (->> fn-index
         (mapcat (fn [[fn-name {:keys [analysis]}]]
                   (let [local-refs (:local-refs (:body-refs analysis))
                         local-fn-refs (->> local-refs
                                            (filter #(Character/isLowerCase (first (name %))))
                                            (map #(symbol (name %)))
                                            set)
                         local-type-refs (->> local-refs
                                              (filter #(Character/isUpperCase (first (name %))))
                                              (map #(symbol (name %)))
                                              set)
                         undef-fns (set/difference local-fn-refs all-fn-names)
                         undef-types (set/difference local-type-refs all-type-names)]
                     (concat
                      (map (fn [u] {:source fn-name :ref u :kind :function}) undef-fns)
                      (map (fn [u] {:source fn-name :ref u :kind :type}) undef-types)))))
         vec)))

(defn find-circular-type-deps
  "Detects circular dependencies in the type dependency graph.
   Returns a vector of cycles found (each cycle is a vector of type names)."
  [type-deps]
  (let [visited (atom #{})
        path (atom [])
        cycles (atom [])]
    (letfn [(dfs [node]
              (when (and (not (contains? @visited node))
                         (contains? type-deps node))
                (if (some #{node} @path)
                  ;; Found a cycle
                  (let [cycle-start (.indexOf @path node)
                        cycle (conj (vec (drop cycle-start @path)) node)]
                    (swap! cycles conj cycle))
                  (do
                    (swap! path conj node)
                    (doseq [dep (get type-deps node)]
                      (dfs dep))
                    (swap! path (comp vec butlast))
                    (swap! visited conj node)))))]
      (doseq [node (keys type-deps)]
        (reset! visited #{})
        (reset! path [])
        (dfs node)))
    @cycles))

(defn find-orphaned-types
  "Finds types that are never referenced by any other type or function."
  [type-index fn-index]
  (let [all-type-names (set (keys type-index))
        ;; Types referenced by other types
        type-refs (->> type-index
                       (mapcat (fn [[_ {:keys [analysis]}]]
                                 (map #(symbol (name %))
                                      (concat (:refs analysis) (:foreign-refs analysis)))))
                       set)
        ;; Types referenced by functions
        fn-type-refs (->> fn-index
                          (mapcat (fn [[_ {:keys [analysis]}]]
                                    (map #(symbol (name %))
                                         (get-in analysis [:body-refs :type-refs]))))
                          (map #(symbol (name %)))
                          set)
        all-refs (set/union type-refs fn-type-refs)]
    (set/difference all-type-names all-refs)))

;;
;; Summary
;;

(defn build-cross-ref-summary
  "Builds a comprehensive cross-reference summary from parsed files.
   Returns a map with indices, dependency graphs, and validation results."
  [parsed-files]
  (let [type-index (build-type-index parsed-files)
        fn-index (build-fn-index parsed-files)
        enum-index (build-enum-index parsed-files)
        type-deps (build-type-deps type-index)
        fn-deps (build-fn-deps fn-index)]
    {:type-index type-index
     :fn-index fn-index
     :enum-index enum-index
     :type-deps type-deps
     :fn-deps fn-deps
     :undefined-type-refs (find-undefined-type-refs type-index enum-index)
     :undefined-fn-refs (find-undefined-fn-refs fn-index type-index)
     :circular-deps (find-circular-type-deps type-deps)
     :orphaned-types (find-orphaned-types type-index fn-index)
     :stats {:type-count (count type-index)
             :fn-count (count fn-index)
             :enum-count (count enum-index)}}))
