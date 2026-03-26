(ns rt.postgres.base.compile
  (:require [rt.postgres.base.compile.server-api :as server-api]
            [rt.postgres.base.compile.server-db :as server-db]))

(def +targets+
  (merge server-db/+targets+
         server-api/+targets+))

(defn infer-sync-spec
  "Compatibility wrapper for DB-server sync inference."
  {:added "4.0"}
  [fn-def]
  (server-db/infer-sync-spec fn-def))

(defn db-sync-merge
  "Compatibility wrapper for the default DB-server sync helper."
  {:added "4.0"}
  [output table-names]
  (server-db/db-sync-merge output table-names))

(defn target-entry
  "Compatibility wrapper that dispatches to the split target namespaces."
  {:added "4.0"}
  [fn-def target]
  (cond
    (contains? server-db/+targets+ target)
    (server-db/target-entry fn-def target)

    (contains? server-api/+targets+ target)
    (server-api/target-entry fn-def target)))

(defn emit-target
  "Compatibility wrapper that dispatches to the split target namespaces."
  {:added "4.0"}
  [fn-def target]
  (cond
    (contains? server-db/+targets+ target)
    (server-db/emit-target fn-def target)

    (contains? server-api/+targets+ target)
    (server-api/emit-target fn-def target)))

(defn emit-targets
  "Compatibility wrapper that emits across both split target namespaces."
  {:added "4.0"}
  [fn-def & [targets]]
  (let [targets (or targets (concat (server-db/list-targets)
                                    (server-api/list-targets)))]
    (into {}
          (keep (fn [target]
                  (when-let [out (emit-target fn-def target)]
                    [target out])))
          targets)))

(defn list-targets
  "Lists all supported generation targets across the split namespaces."
  {:added "4.0"}
  []
  (concat (server-db/list-targets)
          (server-api/list-targets)))
