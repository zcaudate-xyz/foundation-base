(ns rt.postgres.typed
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.grammar.typed-shape :as shape]))

;; ─────────────────────────────────────────────────────────────────────────────
;; Type Registry API
;; ─────────────────────────────────────────────────────────────────────────────

(defn clear-registry!
  "Clears the global type registry."
  []
  (types/clear-registry!))

(defn register-type!
  "Registers a type in the global registry."
  [sym type-ref]
  (types/register-type! sym type-ref))

(defn get-type
  "Retrieves a type from the global registry."
  [sym]
  (types/get-type sym))

;; ─────────────────────────────────────────────────────────────────────────────
;; Introspection API
;; ─────────────────────────────────────────────────────────────────────────────

(defn get-table-shape
  "Gets the shape for a registered table by name."
  [table-name]
  (when-let [table (types/get-type table-name)]
    (when (types/table-def? table)
      (shape/table->shape table))))

(defn list-tables
  "Returns all registered table definitions."
  []
  (filter types/table-def? (vals @types/*type-registry*)))

(defn list-functions
  "Returns all registered function definitions."
  []
  (filter types/fn-def? (vals @types/*type-registry*)))

(defn list-enums
  "Returns all registered enum definitions."
  []
  (filter types/enum-def? (vals @types/*type-registry*)))

;; ─────────────────────────────────────────────────────────────────────────────
;; Runtime Integration API
;; ─────────────────────────────────────────────────────────────────────────────

(defn load-runtime-tables
  "Loads tables from (pg/app app-name) runtime output.
   Input: {:TableName [:col1 {:type :uuid} ...] ...}"
  [tables-map]
  (into {}
        (map (fn [[table-name entries]]
               [table-name (parse/parse-runtime-table table-name entries)]))
        tables-map))

(defn register-runtime-tables!
  "Registers runtime tables into the global type registry."
  [runtime-tables]
  (doseq [[table-name table-def] runtime-tables]
    (swap! types/*type-registry* assoc table-name table-def)))
