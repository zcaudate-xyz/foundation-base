(ns code.migrate.source
  (:require [code.migrate.engine :as engine]))

(defn migrate-unit
  "migrates a source unit through source-owned rules only"
  {:added "4.1"}
  [unit migration-catalog]
  (when-not (= :source (:unit/kind unit))
    (throw (ex-info "Source migration requires a :source unit"
                    {:unit/kind (:unit/kind unit)})))
  (engine/migrate-unit unit migration-catalog))
