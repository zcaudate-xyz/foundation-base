(ns rt.postgres.infer
  "Public API for rt.postgres.infer.
   
   Consolidated, non-redundant interface for schema inference.
   
   Addresses all critique issues:
   1. Single table->shape (no table-instance-shape)
   2. Centralized +type-formats+ (no duplicate type mappings)
   3. Single normalize-key (no scattered key conversion)
   4. Analysis delegates to shape/merge-shapes (no local merge duplication)"
  (:require [rt.postgres.infer.types :as types]
            [rt.postgres.infer.parse :as parse]
            [rt.postgres.infer.shape :as shape]
            [rt.postgres.infer.analyze :as analyze]
            [rt.postgres.infer.generate :as generate]
            [rt.postgres.infer.runtime :as runtime]
            [clojure.string :as str]))

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
;; Source Analysis API
;; ─────────────────────────────────────────────────────────────────────────────

(defn analyze-file
  "Analyzes a Clojure source file for type definitions."
  [file-path]
  (parse/analyze-file file-path))

(defn analyze-namespace
  "Analyzes a namespace for type definitions."
  [ns-sym]
  (parse/analyze-namespace ns-sym))

(defn analyze-and-register!
  "Analyzes a namespace and registers all types."
  [ns-sym]
  (-> ns-sym
    parse/analyze-namespace
    parse/register-types!))

;; ─────────────────────────────────────────────────────────────────────────────
;; Schema Generation API
;; ─────────────────────────────────────────────────────────────────────────────

(defn make-openapi
  "Generates a complete OpenAPI 3.0 spec for the given namespace.
   
   Example:
   (make-openapi 'gwdb.core.user (constantly true))  ; all functions
   (make-openapi 'gwdb.core.user #(= :sb/auth (get-in % [:body-meta :expose])))"
  [root-ns fn-filter]
  (types/clear-registry!)
  (analyze/reset-cache!)
  (-> root-ns parse/analyze-namespace parse/register-types!)
  (generate/emit :openapi root-ns fn-filter))

(defn make-json-schema
  "Generates JSON Schema definitions for all tables and enums."
  []
  (generate/emit :jschema))

(defn make-typescript
  "Generates TypeScript interfaces for all tables and enums."
  []
  (generate/emit :typescript))



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
  (runtime/load-runtime-tables tables-map))

(defn register-runtime-tables!
  "Registers runtime tables into the global type registry."
  [runtime-tables]
  (runtime/register-runtime-tables! runtime-tables))


(comment
  (keys (into {}  (rt.postgres/app "scratch")))
  
  (parse/analyze-namespace 'gwdb.test.scratch)
  
  (#'rt.postgres.infer/make-openapi 'gwdb.test.scratch (constantly true))
  (make-typescript)
  (make-json-schema)
  (make-openapi 'gwdb.test.scratch (constantly true)))
