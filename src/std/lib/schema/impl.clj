(ns std.lib.schema.impl
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.schema.base :as base]
            [std.lib.schema.find :as find]
            [std.lib.schema.ref :as ref]))

(defn simplify
  "helper function for easier display of spirit schema"
  {:added "3.0"}
  ([flat]
   (->> flat
        (reduce-kv (fn [out k [attr]]
                     (let [card-str (condp = (:cardinality attr)
                                      :many "<*>"
                                      "")
                           type-str (condp = (:type attr)
                                      :ref (str "&" (name (-> attr :ref :ns)))
                                      (name (:type attr)))]
                       (assoc out k (keyword (str type-str card-str)))))
                   {})
        (coll/tree-nestify))))

(defrecord Schema [flat tree lu vec]
  Object
  (toString [this]
    (str "#schema" {:tables (count tree)})))

(defmethod print-method Schema
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn create-lookup
  "lookup from flat schema mainly for reverse refs"
  {:added "3.0"}
  ([fschm]
   (reduce-kv (fn [out k [attr]]
                (cond (find/is-reverse-ref? attr)
                      (assoc out (-> attr :ref :key) k k k)

                      :else
                      (assoc out k k)))
              {} fschm)))

(defn create-flat-schema
  "creates a flat schema from an input map"
  {:added "3.0"}
  ([m]
   (create-flat-schema m (base/all-auto-defaults base/base-meta)))
  ([m defaults]
   (let [fschm (->> (coll/tree-flatten m)
                    (map base/attr-add-ident)
                    (map #(base/attr-add-defaults % defaults)) ;; meta/all-auto-defaults
                    (into {}))]
     (merge fschm
            (ref/ref-attrs fschm)))))

(defn vec->map
  "turns a vec schema to a map"
  {:added "3.0"}
  ([v]
   (->> (apply hash-map v)
        (coll/map-vals (fn [columns]
                      (->> columns
                           (partition 2)
                           (map-indexed (fn [i [name attrs]]
                                          [name [(assoc attrs :order i)]]))
                           (into {})))))))

(defn schema-map
  "creates a schema from a map"
  {:added "3.0"}
  ([m]
   (schema-map m (base/all-auto-defaults base/base-meta)))
  ([m defaults]
   (let [flat (create-flat-schema m defaults)
         tree (coll/tree-nestify flat)
         lu   (create-lookup flat)]
     (Schema. flat tree lu nil))))

(defn schema
  "creates an extended schema for use by spirit"
  {:added "3.0"}
  ([x]
   (schema x (base/all-auto-defaults base/base-meta)))
  ([x defaults]
   (cond (vector? x)
         (let [schema (-> (vec->map x)
                          (schema-map defaults))]
           (assoc schema :vec x))

         (map? x)
         (schema-map x defaults))))

(defn schema?
  "checks if object is a schema"
  {:added "3.0"}
  ([obj]
   (instance? Schema obj)))
