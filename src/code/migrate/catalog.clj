(ns code.migrate.catalog
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def +phases+
  [:namespace :dependency :definition :expression
   :test :cleanup :verification])

(def +required-rule-keys+
  #{:rule/id :rule/drift :rule/pathway :rule/layer :rule/phase :rule/kind
    :rule/match :rule/rewrite :rule/safety :rule/disposition :rule/evidence})

(def +required-target-keys+
  #{:target/id :target/order :target/status :target/invariants
    :target/promotion-gates :target/source :target/test})

(def +required-pathway-target-keys+
  #{:input/path :output/path :namespace :rules :options})

(def +pathways+ #{:source :test})
(def +drifts+ #{:clojure :foundation})

(defn read-edn
  "reads one EDN document without evaluating tagged values"
  {:added "4.1"}
  [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (edn/read reader)))

(defn duplicate-values
  "returns values which occur more than once"
  {:added "4.1"}
  [values]
  (->> values
       frequencies
       (keep (fn [[value n]]
               (when (> n 1) value)))
       (sort-by str)
       vec))

(defn target-pathway-key
  "returns the nested catalog key for one migration pathway"
  {:added "4.1"}
  [pathway]
  (when-not (contains? +pathways+ pathway)
    (throw (ex-info "Unknown migration pathway"
                    {:pathway pathway})))
  (keyword "target" (name pathway)))

(defn target-for-pathway
  "normalizes one nested target pathway for the migration engine"
  {:added "4.1"}
  [target pathway]
  (let [config  (get target (target-pathway-key pathway))
        options (:options config)
        common  (select-keys target
                             [:target/id :target/order :target/status
                              :target/invariants :target/promotion-gates])]
    (merge common
           {:unit/kind pathway
            :target/rules (vec (:rules config))
            :target/input-path (:input/path config)
            :target/output-path (:output/path config)
            :target/namespace (:namespace config)
            :target/overrides (vec (:overrides options))}
           (case pathway
             :source
             {:target/source-namespace (:namespace config)
              :target/source-path (:input/path config)
              :target/target-source-path (:output/path config)
              :target/struct-fields (:struct-fields options)}

             :test
             {:target/test-namespace (:namespace config)
              :target/test-path (:input/path config)
              :target/target-test-path (:output/path config)
              :target/source-namespace (:source/namespace options)
              :target/test-alias (:alias options)}))))

(defn missing-rule-keys
  [rules]
  (keep (fn [rule]
          (let [keys (->> +required-rule-keys+
                          (remove #(contains? rule %))
                          (sort-by str)
                          vec)]
            (when (seq keys)
              {:rule/id (:rule/id rule)
               :missing keys})))
        rules))

(defn missing-target-keys
  [targets]
  (mapcat
   (fn [target]
     (let [root-missing (->> +required-target-keys+
                             (remove #(contains? target %))
                             (sort-by str)
                             vec)
           pathway-missing
           (keep (fn [pathway]
                   (let [config (get target (target-pathway-key pathway))
                         missing (->> +required-pathway-target-keys+
                                      (remove #(contains? config %))
                                      (sort-by str)
                                      vec)]
                     (when (seq missing)
                       {:target/id (:target/id target)
                        :pathway pathway
                        :missing missing})))
                 [:source :test])]
       (concat
        (when (seq root-missing)
          [{:target/id (:target/id target)
            :missing root-missing}])
        pathway-missing)))
   targets))

(defn target-rule-findings
  [catalog]
  (let [rules-by-id (into {} (map (juxt :rule/id identity)
                                  (:migration/rules catalog)))]
    (mapcat
     (fn [target]
       (mapcat
        (fn [pathway]
          (keep (fn [rule-id]
                  (let [rule (get rules-by-id rule-id)]
                    (cond
                      (nil? rule)
                      {:type :catalog/unknown-target-rule
                       :target/id (:target/id target)
                       :pathway pathway
                       :rule/id rule-id}

                      (not= pathway (:rule/pathway rule))
                      {:type :catalog/cross-pathway-rule
                       :target/id (:target/id target)
                       :pathway pathway
                       :rule/id rule-id
                       :rule/pathway (:rule/pathway rule)})))
                (get-in target [(target-pathway-key pathway) :rules])))
        [:source :test]))
     (:migration/targets catalog))))

(defn validate-catalog
  "returns deterministic structural findings for a migration catalog"
  {:added "4.1"}
  [catalog]
  (let [rules           (:migration/rules catalog)
        targets         (:migration/targets catalog)
        ids             (map :rule/id rules)
        unknown-phases  (remove (set +phases+) (map :rule/phase rules))
        missing-rules   (vec (missing-rule-keys rules))
        missing-targets (vec (missing-target-keys targets))
        bad-pathways    (remove +pathways+ (map :rule/pathway rules))
        bad-drifts      (remove +drifts+ (map :rule/drift rules))
        target-rules    (vec (target-rule-findings catalog))]
    (cond-> []
      (not= :code-migration-spec (:document/type catalog))
      (conj {:type :catalog/type
             :actual (:document/type catalog)})

      (seq (duplicate-values ids))
      (conj {:type :catalog/duplicate-rule-ids
             :ids (duplicate-values ids)})

      (seq unknown-phases)
      (conj {:type :catalog/unknown-phases
             :phases (vec unknown-phases)})

      (seq missing-rules)
      (conj {:type :catalog/missing-rule-keys
             :rules missing-rules})

      (seq missing-targets)
      (conj {:type :catalog/missing-target-keys
             :targets missing-targets})

      (seq bad-pathways)
      (conj {:type :catalog/unknown-pathways
             :pathways (vec bad-pathways)})

      (seq bad-drifts)
      (conj {:type :catalog/unknown-drifts
             :drifts (vec bad-drifts)})

      (seq target-rules)
      (into target-rules))))

(defn load-rule-documents
  "loads rule documents relative to their catalog manifest"
  {:added "4.1"}
  [path catalog]
  (let [parent (.getParentFile (.getCanonicalFile (io/file path)))]
    (mapv (fn [relative]
            (let [document (read-edn (io/file parent relative))
                  pathway (:rule/pathway document)]
              (when-not (= :code-migration-rules (:document/type document))
                (throw (ex-info "Invalid migration rule document"
                                {:path relative
                                 :document/type (:document/type document)})))
              (when-not (contains? +pathways+ pathway)
                (throw (ex-info "Invalid migration rule pathway"
                                {:path relative
                                 :rule/pathway pathway})))
              (when-let [mismatched
                         (seq (remove #(= pathway (:rule/pathway %))
                                      (:migration/rules document)))]
                (throw (ex-info "Rule crosses its owning document pathway"
                                {:path relative
                                 :rule/pathway pathway
                                 :rule/ids (mapv :rule/id mismatched)})))
              document))
          (:migration/rule-paths catalog))))

(defn load-catalog
  "loads a migration catalog or throws with its structural findings"
  {:added "4.1"}
  [path]
  (let [manifest  (read-edn path)
        documents (load-rule-documents path manifest)
        catalog   (assoc manifest
                         :migration/rules
                         (vec (mapcat :migration/rules documents))
                         :migration/rule-documents
                         (mapv #(select-keys % [:document/id :rule/pathway])
                               documents))
        findings (validate-catalog catalog)]
    (if (seq findings)
      (throw (ex-info "Invalid migration catalog"
                      {:path (str path)
                       :findings findings}))
      catalog)))

(defn rules-for-pathway
  "returns only rules owned by one generation pathway"
  {:added "4.1"}
  [catalog pathway]
  (->> (:migration/rules catalog)
       (filter #(= pathway (:rule/pathway %)))
       vec))

(defn rules-for-phase
  "returns catalog rules for one ordered migration phase"
  {:added "4.1"}
  [catalog phase]
  (->> (:migration/rules catalog)
       (filter #(= phase (:rule/phase %)))
       vec))

(defn target-by-id
  "returns one declared migration target in its default source pathway"
  {:added "4.1"}
  [catalog target-id]
  (when-let [target (first (filter #(= target-id (:target/id %))
                                   (:migration/targets catalog)))]
    (target-for-pathway target :source)))