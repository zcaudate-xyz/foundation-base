(ns rt.postgres.compile.ts-schema
  (:require [clojure.string :as str]
            [rt.postgres.compile.common :as compile.common]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-shape :as shape]))

(declare shape->ts-interface)

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

      :else (compile.common/resolve-type field-info :ts))))

(defn field->ts
  "Converts a field to TypeScript property declaration."
  [[k v]]
  (let [ts-type (type->ts v)
        optional (if (:nullable? v) "?" "")]
    (str "  " (types/typescript-key k) optional ": " ts-type ";")))

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
  (let [all (compile.common/unique-defs (vals @types/*type-registry*))
        tables (filter types/table-def? all)
        enums (filter types/enum-def? all)
        enum-types (map (fn [e]
                          (str "export type " (compile.common/def-name e) " = "
                               (str/join " | " (sort (map #(str "\"" (name %) "\"") (:values e)))) ";"))
                        enums)
        table-interfaces (map (fn [t]
                                (shape->ts-interface (shape/table->shape t) (compile.common/def-name t)))
                              tables)]
    (str/join "\n\n" (concat enum-types table-interfaces))))

;; Compatibility alias while older callers still use typescript naming.
(def generate-typescript generate-ts-schema)
