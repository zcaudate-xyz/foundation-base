(ns code.migrate
  (:require [clojure.java.io :as io]
            [code.migrate.catalog :as catalog]
            [code.migrate.engine :as engine]
            [code.migrate.probe :as probe]
            [code.migrate.source :as source]
            [code.migrate.test :as test]))

(def +catalog-resource+ "code/migrate/catalog.edn")

(defn workspace-root
  "returns the configured Hara workspace root"
  {:added "4.1"}
  []
  (or (System/getenv "HARA_WORKSPACE_ROOT")
      "../../workspace"))

(defn catalog-path
  "returns Foundation's authoritative migration catalog path"
  {:added "4.1"}
  []
  (.getPath (io/file (io/resource +catalog-resource+))))

(defn load-catalog
  "loads the authoritative or an explicitly supplied migration catalog"
  {:added "4.1"}
  ([] (catalog/load-catalog (catalog-path)))
  ([path] (catalog/load-catalog path)))

(defn analyze-unit
  "returns diagnostics and evidence without returning generated source"
  {:added "4.1"}
  ([unit]
   (analyze-unit unit (load-catalog)))
  ([unit migration-catalog]
   (let [result (engine/migrate-unit unit migration-catalog)]
     (select-keys result
                  [:unit/kind :source/path :target/path
                   :source/checksum :output/checksum
                   :applied :diagnostics :changed]))))

(defn plan-unit
  "returns a deterministic migration plan including generated source"
  {:added "4.1"}
  ([unit]
   (plan-unit unit (load-catalog)))
  ([unit migration-catalog]
   (case (:unit/kind unit)
     :source (source/migrate-unit unit migration-catalog)
     :test (test/migrate-unit unit migration-catalog)
     (throw (ex-info "Migration unit requires :source or :test kind"
                     {:unit/kind (:unit/kind unit)})))))

(defn migrate-unit
  "returns a generated unit or rejects blocking diagnostics"
  {:added "4.1"}
  ([unit]
   (migrate-unit unit (load-catalog)))
  ([unit migration-catalog]
   (let [result (plan-unit unit migration-catalog)]
     (if (seq (:diagnostics result))
       (throw (ex-info "Migration unit has blocking diagnostics"
                       {:unit (select-keys unit
                                           [:unit/kind :source/path])
                        :diagnostics (:diagnostics result)}))
       result))))

(defn migrate-pair
  "migrates one required source/test pair"
  {:added "4.1"}
  ([source-unit test-unit]
   (migrate-pair source-unit test-unit (load-catalog)))
  ([source-unit test-unit migration-catalog]
   (when-not (= [:source :test]
                [(:unit/kind source-unit) (:unit/kind test-unit)])
     (throw (ex-info "Migration requires a source/test pair"
                     {:kinds [(:unit/kind source-unit)
                              (:unit/kind test-unit)]})))
   {:source (migrate-unit source-unit migration-catalog)
    :test (migrate-unit test-unit migration-catalog)}))

(defn verify
  "verifies a migrated source/test pair in a fresh native Hara process"
  {:added "4.1"}
  [pair options]
  (probe/verify-pair pair
                     (assoc options
                            :migration/catalog
                            (or (:migration/catalog options)
                                (load-catalog)))))
