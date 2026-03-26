(ns rt.postgres.base.compile.ts-schema
  (:require [clojure.string :as str]
            [rt.postgres.base.typed.typed-common :as types]
            [rt.postgres.base.typed.typed-shape :as shape]
            [rt.postgres.base.compile.json-schema :as json-schema]))

(declare type->ts)
(declare shape->ts-interface)

(defn field->ts
  "Converts a field to TypeScript property declaration."
  [[k v]]
  (let [ts-type (type->ts v)
        optional (if (:nullable? v) "?" "")]
    (str "  " (types/typescript-key k) optional ": " ts-type ";")))

(defn type->ts
  "Converts a type descriptor to TypeScript type string."
  [field-info]
  (let [t (:type field-info)]
    (cond
      (:is-ref? field-info)
      "string"

      (:shape field-info)
      (shape->ts-interface (:shape field-info) nil)

      (= :array t)
      (str (type->ts (:items field-info)) "[]")

      (:enum-ref field-info)
      (name (get-in field-info [:enum-ref :ns]))

      :else (json-schema/resolve-type field-info :ts))))

(defn shape->ts-interface
  "Converts a JsonbShape to TypeScript interface or inline object."
  ([shape name]
   (let [fields (:fields shape)
         field-strs (map field->ts (sort-by (comp types/emitted-key key) fields))]
     (if name
       (str "export interface " name " {\n"
            (str/join "\n" field-strs)
            "\n}")
       (str "{ " (str/join " " (map #(str/replace % #"^  " "") field-strs)) " }"))))
  ([shape]
   (shape->ts-interface shape nil)))

(defn generate-ts-schema
  "Generates TypeScript interfaces for all tables and enums."
  []
  (let [all (->> (vals @types/*type-registry*)
                 (group-by (fn [x] (some-> x :name name)))
                 vals
                 (map first))
        tables (filter types/table-def? all)
        enums (filter types/enum-def? all)
        enum-types (map (fn [e]
                          (str "export type " (some-> e :name name) " = "
                               (str/join " | " (sort (map #(str "\"" (name %) "\"") (:values e)))) ";"))
                        enums)
        table-interfaces (map (fn [t]
                                (shape->ts-interface (shape/table->shape t) (some-> t :name name)))
                              tables)]
    (str/join "\n\n" (concat enum-types table-interfaces))))
