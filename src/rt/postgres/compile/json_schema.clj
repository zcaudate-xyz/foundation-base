(ns rt.postgres.compile.json-schema
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-shape :as shape]))

(declare shape->json-schema)

(defn resolve-type
  [t target]
  (let [base-type (cond
                    (keyword? t) t
                    (types/type-ref? t) (if (= :primitive (:kind t)) (:name t) (:kind t))
                    (map? t) (let [inner-type (:type t)]
                               (cond
                                 (keyword? inner-type) inner-type
                                 (types/type-ref? inner-type) (if (= :primitive (:kind inner-type))
                                                                 (:name inner-type)
                                                                 (:kind inner-type))
                                 :else (or (:kind t) :unknown)))
                    :else :unknown)]
    (or (get-in types/+type-formats+ [base-type target])
        (case base-type
          :enum (if (and (map? t) (:enum-ref t))
                  (case target
                    :openapi {:$ref (str "#/components/schemas/" (name (get-in t [:enum-ref :ns])))}
                    :jschema {:$ref (str "#/definitions/" (name (get-in t [:enum-ref :ns])))}
                    :ts (name (get-in t [:enum-ref :ns])))
                  (get-in types/+type-formats+ [:text target]))
          (case target
            :openapi {:type "string"}
            :jschema {:type "string"}
            :ts "string")))))

(defn field->json-schema
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

      :else (resolve-type field-info :jschema))))

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
  (let [all (->> (vals @types/*type-registry*)
                 (group-by (fn [x] (some-> x :name name)))
                 vals
                 (map first))
        tables (filter types/table-def? all)
        enums (filter types/enum-def? all)]
    {:$schema "http://json-schema.org/draft-07/schema#"
     :definitions (into (sorted-map)
                        (concat
                         (map (fn [t] [(some-> t :name name) (shape->json-schema (shape/table->shape t))]) tables)
                         (map (fn [e] [(some-> e :name name) {:type "string" :enum (mapv name (:values e))}]) enums)))}))
