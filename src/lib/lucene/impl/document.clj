(ns lib.lucene.impl.document
  (:require [std.lib.collection]
            [std.lib.foundation]
            [std.object :as object]
            [std.object.framework.read :as read]
            [std.object.framework.write :as write])
  (:import (org.apache.lucene.document FieldType Field Document) (org.apache.lucene.index DocValuesType IndexOptions)))

(defonce +getter-opts+
  (assoc read/+read-get-opts+ :prefix ""))

(def +field-type-defaults+
  {:tokenized true
   :stored true
   :index #{:doc :freq}
   :omit-norms false
   :doc-values-type "NONE"})

(def +field-type+
  (select-keys +field-type-defaults+ [:stored :index]))

(defn field-type-display
  "returns field types that are not defaults
 
   (field-type-display {:tokenized false
                        :stored true})
   => (contains {:tokenized false})"
  {:added "3.0"}
  ([m]
   (reduce (fn [out [k v]]
             (let [res (get m k)]
               (if (not= v res)
                 (assoc out k res)
                 out)))
           {}
           +field-type-defaults+)))

(def +field-type-lu+
  {#{}     IndexOptions/NONE
   #{:doc} IndexOptions/DOCS
   #{:doc :freq} IndexOptions/DOCS_AND_FREQS
   #{:doc :freq :position} IndexOptions/DOCS_AND_FREQS_AND_POSITIONS
   #{:doc :freq :position :offset} IndexOptions/DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS})

(def +field-type-rlu+
  (std.lib.collection/transpose +field-type-lu+))

(defn field-type-set-index
  "sets the field type index value
 
   (field-type-set-index (field-type {}) #{:doc})"
  {:added "3.0"}
  ([^FieldType ft value]
   (let [options (or (get +field-type-lu+ value)
                     (throw (ex-info "Not valid" {:input value})))]
     (.setIndexOptions ft options)
     ft)))

(defn field-type-get-index
  "gets the field type index value
 
   (field-type-get-index (field-type {}))
   => #{:freq :doc}"
  {:added "3.0"}
  ([^FieldType ft]
   (get +field-type-rlu+ (.indexOptions ft))))

(object/map-like
 FieldType
 {:tag "field.type"
  :display field-type-display
  :read  {:methods (-> (object/read-getters FieldType +getter-opts+)
                       (dissoc :freeze :to-string)
                       (merge {:index {:type Object
                                       :fn field-type-get-index}}))}
  :write {:empty (fn [] (FieldType.))
          :methods (-> (object/write-setters FieldType)
                       (merge {:index {:type Object
                                       :fn field-type-set-index}}))}})

(defn field-type
  "constructs a field type
 
   (field-type {:tokenized false :index #{}})
   ;; #field.type{:tokenized false, :index []}
   => org.apache.lucene.document.FieldType"
  {:added "3.0"}
  ([{:keys [tokenized stored omit-norms doc-values-type] :as m}]
   (-> (merge +field-type+ m)
       (object/from-data FieldType))))

(defn doc-values-type
  "returns the doc values type enum
 
   (doc-values-type \"NONE\")
   => org.apache.lucene.index.DocValuesType/NONE"
  {:added "3.0"}
  ([s]
   (object/from-data s DocValuesType)))

(defn field-display
  "displays the field"
  {:added "3.0"}
  ([m]
   (select-keys m [:name :type :length])))

(defn field-construct
  "constructs a field
 
   (field-construct \"hello\" \"world\" {})
   ;; #field{:name \"hello\", :type {:index [:freq :doc]}, :length 5}
   => org.apache.lucene.document.Field"
  {:added "3.0"}
  ([^String name ^String value type]
   (Field. name value ^FieldType (field-type type))))

(defn field-value
  "setter for field value
   (-> (field-value (field-construct \"hello\" \"\" {})
                    \"world\")
       (.stringValue))
   => \"world\""
  {:added "3.0"}
  (^Field [^Field field value]
   (.setStringValue field value) field))

(object/map-like
 Field
 {:tag "field"
  :display field-display
  :read  {:methods (merge (object/read-getters Field +getter-opts+)
                          {:type   {:type java.util.Map
                                    :fn (fn [^Field field]
                                          (field-type-display (object/to-data (.fieldType field))))}
                           :value  {:type String
                                    :fn (fn [^Field field] (.stringValue field))}
                           :length {:type Long
                                    :fn (fn [^Field field] (count (.stringValue field)))}})}
  :write {:construct  {:fn field-construct
                       :params [:name :value :type]}
          :methods (assoc (object/write-setters Field)
                          :name  {:type String}
                          :type  {:type java.util.Map}
                          :value {:type String
                                  :fn field-value})}})

(defn field
  "constructor for field"
  {:added "3.0"}
  ([{:keys [name value type] :as m}]
   (object/from-data m Field)))

(defn document-set-fields
  "setters for document fields
 
   (-> (document {})
       (document-set-fields [{:name \"hello\" :value \"world\" :type {}}]))"
  {:added "3.0"}
  ([^Document document fields]
   (doseq [field fields]
     (.add document (object/from-data field Field))) document))

(defn document-display
  "display function for document"
  {:added "3.0"}
  ([m]
   (update m :fields (partial mapv field-display))))

(object/map-like
 Document
 {:tag "document"
  :display document-display
  :read  {:methods :class}
  :write {:empty   (fn [] (Document.))
          :methods (merge (object/write-setters Document)
                          {:fields {:type java.util.List
                                    :fn document-set-fields}})}})

(defn document
  "constructs a document
 
   (document {:fields [{:name \"hello\" :value \"world\" :type {}}]})"
  {:added "3.0"}
  ([{:keys [fields] :as m}]
   (object/from-data m Document)))

(defn to-map
  "turns the document into a map
 
   (-> (document {:fields [{:name \"hello\" :value \"world\" :type {}}]})
       to-map)
   => {:hello \"world\"}"
  {:added "3.0"}
  ([^Document document]
   (into {} (for [^Field f (.getFields document)]
              [(keyword (.name f)) (.stringValue f)]))))

(defn from-map
  "turns a map into a document
 
   (from-map {:hello \"world\"} nil)
   => org.apache.lucene.document.Document"
  {:added "3.0"}
  ([m]
   (from-map m nil))
  ([m template]
   (let [fields (mapv (fn [[k v]]
                        {:name  (name k)
                         :value (std.lib.foundation/strn v)
                         :type  (merge +field-type+ (get template k))})
                      m)]
     (document {:fields fields}))))
