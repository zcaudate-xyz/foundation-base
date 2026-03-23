(ns rt.postgres.compile.json-schema
  (:require [rt.postgres.compile.common :as compile.common]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-shape :as shape]))

(declare shape->json-schema)

(defn- field->json-schema
  "Converts a field descriptor to JSON Schema."
  [field-info]
  (let [t (:type field-info)]
    (cond
      (:is-ref? field-info)
      {:type "string" :format "uuid"}

      (:shape field-info)
      (shape->json-schema (:shape field-info))

      (= :array t)
      {:type "array" :items (or (field->json-schema (:items field-info)) {:type "object"})}

      :else (compile.common/resolve-type field-info :jschema))))

(defn shape->json-schema
  "Converts a JsonbShape to JSON Schema."
  [shape]
  (let [fields (:fields shape)
        properties (into (sorted-map)
                         (map (fn [[k v]] [(types/emitted-key k) (field->json-schema v)]))
                         fields)
        required (mapv types/emitted-key
                       (filter #(not (:nullable? (get fields %))) (keys fields)))]
    (cond-> {:type "object" :properties properties}
      (seq required) (assoc :required required)
      :always (assoc :additionalProperties false))))

(defn generate-json-schema
  "Generates JSON Schema for tables and enums."
  []
  (let [all (compile.common/unique-defs (vals @types/*type-registry*))
        tables (filter types/table-def? all)
        enums (filter types/enum-def? all)]
    {:$schema "http://json-schema.org/draft-07/schema#"
     :definitions (into (sorted-map)
                        (concat
                         (map (fn [t] [(compile.common/def-name t) (shape->json-schema (shape/table->shape t))]) tables)
                         (map (fn [e] [(compile.common/def-name e) {:type "string" :enum (mapv name (:values e))}]) enums)))}))

;; Compatibility aliases while older callers still use jschema naming.
(def shape->jschema shape->json-schema)
(def generate-jschema generate-json-schema)
