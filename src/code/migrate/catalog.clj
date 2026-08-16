(ns code.migrate.catalog
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def +phases+
  [:namespace :dependency :definition :expression
   :test :cleanup :verification])

(def +required-rule-keys+
  #{:rule/id :rule/drift :rule/pathway :rule/layer :rule/phase :rule/kind :rule/match
    :rule/safety :rule/disposition :rule/evidence})

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
       vec))

(defn validate-catalog
  "returns deterministic structural findings for a migration catalog"
  {:added "4.1"}
  [catalog]
  (let [rules          (:migration/rules catalog)
        ids            (map :rule/id rules)
        unknown-phases (remove (set +phases+) (map :rule/phase rules))
        missing        (keep (fn [rule]
                               (let [keys (remove #(contains? rule %)
                                                  +required-rule-keys+)]
                                 (when (seq keys)
                                   {:rule/id (:rule/id rule)
                                    :missing (vec keys)})))
                             rules)
        bad-pathways   (remove +pathways+ (map :rule/pathway rules))
        bad-drifts     (remove +drifts+ (map :rule/drift rules))]
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

      (seq missing)
      (conj {:type :catalog/missing-rule-keys
             :rules (vec missing)})

      (seq bad-pathways)
      (conj {:type :catalog/unknown-pathways
             :pathways (vec bad-pathways)})

      (seq bad-drifts)
      (conj {:type :catalog/unknown-drifts
             :drifts (vec bad-drifts)}))))

(defn load-rule-documents
  "loads rule documents relative to their catalog manifest"
  {:added "4.1"}
  [path catalog]
  (let [parent (.getParentFile (.getCanonicalFile (io/file path)))]
    (mapv (fn [relative]
            (let [document (read-edn (io/file parent relative))]
              (when-not (= :code-migration-rules (:document/type document))
                (throw (ex-info "Invalid migration rule document"
                                {:path relative
                                 :document/type (:document/type document)})))
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
    (assoc target
           :unit/kind :source
           :target/rules (:target/source-rules target))))
