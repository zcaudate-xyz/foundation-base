(ns postgres.sample.scratch-v3.state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]
   :export [MODULE]})

(def.xt EV_DB_SYNC "currency/db-sync")
(def.xt EV_DB_REMOVE "currency/db-remove")

(defn.xt sync-tables
  "returns a predicate that matches updates for any table in the set"
  {:added "4.1"}
  [tables]
  (var arr (:? (xt/x:is-array? tables) tables [tables]))
  (var lu {})
  (xt/for:array [table arr]
    (xt/x:set-key lu table true))
  (return (fn [m]
            (var body (or (. m ["body"]) {}))
            (xt/for:object [[table _] body]
              (when (. lu [table])
                (return true)))
            (return false))))

(defn.xt db-sync
  "builds the event-sync map used by curated api descriptors"
  {:added "4.1"}
  [tables]
  (return {-/EV_DB_SYNC   (-/sync-tables tables)
           -/EV_DB_REMOVE (-/sync-tables tables)}))

(def.xt MODULE (!:module))
