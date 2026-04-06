(ns jvm.reflect.print-test
  (:require [jvm.reflect.print :refer :all]
            [std.object.query :as query])
  (:use code.test))

^{:refer jvm.reflect.print/format-type :added "3.0"}
(fact "returns a nice looking version of the class"

  (format-type String) => "String"

  (format-type (type {})) => "PersistentArrayMap"

  (format-type Byte/TYPE) => "byte"

  (format-type (type (char-array ()))) => "char[]")

^{:refer jvm.reflect.print/order-modifiers :added "3.0"}
(fact "orders elements based on modifiers for printing"
  (order-modifiers #{:public :static :final})
  => ":final :public :static")

^{:refer jvm.reflect.print/col-color :added "3.0"}
(fact "returns the column color"
  (col-color 0 :cyan :instance :public :name +col-colors+ +matrix+)
  => #{:cyan :bold})

^{:refer jvm.reflect.print/col-settings :added "3.0"}
(fact "returns the column settings"
  (mapv :id (col-settings [:method :instance :public]))
  => [:name :type :params :modifiers])

^{:refer jvm.reflect.print/class-elements :added "3.0"}
(fact "returns all class elements for a given category"
  (class-elements (query/query-class String []) [:method :instance])
  => seq?)

^{:refer jvm.reflect.print/print-elements :added "3.0"}
(fact "prints all elements in a given category"
  (print-elements "PUBLIC"
                  []
                  (col-settings [:method :instance :public]))
  => nil)

^{:refer jvm.reflect.print/category-title :added "3.0"}
(fact "creates a category title"
  (category-title [:method :instance])
  => "INSTANCE METHOD")

^{:refer jvm.reflect.print/print-classname :added "3.0"}
(fact "prints the classname with title"
  (print-classname "CLASS" String)
  => nil)

^{:refer jvm.reflect.print/print-category :added "3.0"}
(fact "prints a given category"
  (print-category (query/query-class String []) [:method :instance])
  => vector?)

^{:refer jvm.reflect.print/print-class :added "3.0"}
(fact "prints a given class"
  (print-class (query/query-class String []))
  => vector?)

^{:refer jvm.reflect.print/sort-elements :added "3.0"}
(fact "sorts elements given a comparator"
  (->> (sort-elements [{:name "b" :params []}
                       {:name "a" :params []}])
       (map :name))
  => ["a" "b"])

(comment

  (code.manage/scaffold)
  (def elems01 (sort-elements elems0))

  (reset! +class-elements-cache+ {})

  (print-class elems01 {:class String})
  (print-class elems0 {:class String})

  (time (print-category elems0 [:constructor] {:class String}))

  (print-elements "PUBLIC"
                  (class-elements elems0 [:constructor :public])
                  (col-settings [:constructor :public])
                  {:options {:padding 5}})

  (print-elements "PRIVATE"
                  (class-elements elems0 [:constructor :private])
                  (col-settings [:constructor :private])
                  {:options {:padding 5}})

  (print-elements "PLAIN"
                  (class-elements elems0 [:constructor :plain])
                  (col-settings [:constructor :plain])
                  {:options {:padding 5}})

  (time (class-elements elems0 [:constructor]))
  (time (class-elements elems0 [:constructor :private]))
  (time (class-elements elems0 [:constructor :public]))
  (time (class-elements elems0 [:method :private :static]))

  (def elems0 (std.object.query/query-class String []))
  (def elems1 (std.object.query/query-hierarchy String []))
  (def elems2 (std.object.query/query-class "1" []))
  (class-data elems [:constructor])
  (count elems1)
  (count elems0)

  (class-elements elems2 [:method :static])
  (class-elements elems2 [:method :static :private]))
