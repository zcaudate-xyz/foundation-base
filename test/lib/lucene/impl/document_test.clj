(ns lib.lucene.impl.document-test
  (:use code.test)
  (:require [lib.lucene.impl.document :refer :all]
            [std.object :as object]))

^{:refer lib.lucene.impl.document/field-type-display :added "3.0"}
(fact "returns field types that are not defaults"

  (field-type-display {:tokenized false
                       :stored true})
  => (contains {:tokenized false}))

^{:refer lib.lucene.impl.document/field-type-set-index :added "3.0"}
(fact "sets the field type index value"

  (field-type-set-index (field-type {}) #{:doc})
  => org.apache.lucene.document.FieldType)

^{:refer lib.lucene.impl.document/field-type-get-index :added "3.0"}
(fact "gets the field type index value"

  (field-type-get-index (field-type {}))
  => #{:freq :doc})

^{:refer lib.lucene.impl.document/field-type :added "3.0"}
(fact "constructs a field type"

  (field-type {:tokenized false :index #{}})
  ;; #field.type{:tokenized false, :index []}
  => org.apache.lucene.document.FieldType)

^{:refer lib.lucene.impl.document/doc-values-type :added "3.0"}
(fact "returns the doc values type enum"

  (doc-values-type "NONE")
  => org.apache.lucene.index.DocValuesType/NONE)

^{:refer lib.lucene.impl.document/field-display :added "3.0"}
(fact "displays the field"
  (field-display {:name "hello" :type {} :length 5})
  => (contains {:name "hello"}))

^{:refer lib.lucene.impl.document/field-construct :added "3.0"}
(fact "constructs a field"

  (field-construct "hello" "world" {})
  ;; #field{:name "hello", :type {:index [:freq :doc]}, :length 5}
  => org.apache.lucene.document.Field)

^{:refer lib.lucene.impl.document/field-value :added "3.0"}
(fact "setter for field value"
  (-> (field-value (field-construct "hello" "" {})
                   "world")
      (.stringValue))
  => "world")

^{:refer lib.lucene.impl.document/field :added "3.0"}
(fact "constructor for field" ^:hidden

  (field {:name "hello" :value "world" :type {}})
  => org.apache.lucene.document.Field)

^{:refer lib.lucene.impl.document/document-set-fields :added "3.0"}
(fact "setters for document fields"

  (-> (document {})
      (document-set-fields [{:name "hello" :value "world" :type {}}]))
  => org.apache.lucene.document.Document)

^{:refer lib.lucene.impl.document/document-display :added "3.0"}
(fact "display function for document"
  (document-display {:fields [{:name "hello"}]})
  => map?)

^{:refer lib.lucene.impl.document/document :added "3.0"}
(fact "constructs a document"

  (document {:fields [{:name "hello" :value "world" :type {}}]})
  => org.apache.lucene.document.Document)

^{:refer lib.lucene.impl.document/to-map :added "3.0"}
(fact "turns the document into a map"

  (-> (document {:fields [{:name "hello" :value "world" :type {}}]})
      to-map)
  => {:hello "world"})

^{:refer lib.lucene.impl.document/from-map :added "3.0"}
(fact "turns a map into a document"

  (from-map {:hello "world"} nil)
  => org.apache.lucene.document.Document)
