(ns rt.postgres.typed
  "Public API for rt.postgres typed analysis and compile targets.
   
   Consolidated, non-redundant interface for schema inference.
   
   Addresses all critique issues:
   1. Single table->shape (no table-instance-shape)
   2. Centralized +type-formats+ (no duplicate type mappings)
   3. Single normalize-key (no scattered key conversion)
   4. Analysis delegates to shape/merge-shapes (no local merge duplication)"
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.grammar.typed-shape :as shape]
            [rt.postgres.grammar.typed-analyze :as analyze]
            [rt.postgres.compile.json-openapi :as compile.json-openapi]
            [rt.postgres.compile.json-schema :as compile.json-schema]
            [rt.postgres.compile.ts-schema :as compile.ts-schema]
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

(defn make-function-report
  "Generates a JSON-friendly infer report for one function in a namespace."
  [ns-sym fn-sym]
  (analyze/reset-cache!)
  (let [analysis (-> ns-sym
                     parse/analyze-namespace
                     parse/register-types!)
        fn-name (name fn-sym)]
    (when-let [fn-def (some #(when (= fn-name (:name %)) %)
                            (:functions analysis))]
      (analyze/infer-report fn-def))))

(defn report-json
  "Serializes an infer report to JSON."
  ([report]
   (analyze/report-json report))
  ([report pretty?]
   (analyze/report-json report pretty?)))

(defn make-function-json
  "Generates JSON for one function infer report in a namespace."
  ([ns-sym fn-sym]
   (when-let [report (make-function-report ns-sym fn-sym)]
     (report-json report)))
  ([ns-sym fn-sym pretty?]
   (when-let [report (make-function-report ns-sym fn-sym)]
     (report-json report pretty?))))

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
  (compile.json-openapi/generate-openapi root-ns fn-filter))

(defn make-json-schema
  "Generates JSON Schema definitions for all tables and enums."
  []
  (compile.json-schema/generate-json-schema))

(defn make-typescript
  "Generates TypeScript interfaces for all tables and enums."
  []
  (compile.ts-schema/generate-ts-schema))



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


(comment
  (keys (into {}  (rt.postgres/app "scratch")))
  
  (parse/analyze-namespace 'gwdb.test.scratch)
  
  (#'rt.postgres.typed/make-openapi 'gwdb.test.scratch (constantly true))
  (make-typescript)
  (make-json-schema)
  (make-openapi 'gwdb.test.scratch (constantly true)))
