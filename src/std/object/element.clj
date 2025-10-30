(ns std.object.element
  (:require [std.object.element.common :as common]
            [std.object.element.class :deps true]
            [std.object.element.impl.constructor :deps true]
            [std.object.element.impl.field :deps true]
            [std.object.element.impl.method :deps true]
            [std.object.element.impl.multi :deps true]
            [std.object.element.impl.type :as type]
            [std.lib.invoke :refer [definvoke]]
            [std.lib.class :as class])
  (:refer-clojure :exclude [instance?]))

(definvoke to-element
  "converts a `java.reflect` object to a `std.object.element` one
 
   (element/to-element (first (.getMethods String)))
   ;; #elem[equals :: (java.lang.String, java.lang.Object) -> boolean]"
  {:added "3.0"}
  [:memoize {:arglists '([obj])
             :function common/-to-element}])

(defn element-params
  "returns the arglist of a particular element
 
   (-> (first (.getMethods String))
       (element/to-element)
       (element/element-params))
   ;;=> '([java.lang.String java.lang.Object])"
  {:added "3.0"}
  ([elem]
   (common/-element-params elem)))

(defn class-info
  "Lists class information
 
   (element/class-info String)
   => (contains {:name \"java.lang.String\"
                 :hash anything
                 :modifiers #{:instance :class :public :final}})"
  {:added "3.0"}
  ([obj]
   (select-keys (type/seed :class (common/context-class obj))
                [:name :hash :modifiers])))

(defn class-hierarchy
  "Lists the class and interface hierarchy for the class
   
   (element/class-hierarchy String)
   => [java.lang.String
       [java.lang.Object
        #{java.lang.CharSequence
          java.lang.constant.ConstantDesc
          java.lang.constant.Constable
          java.lang.Comparable
          java.io.Serializable}]]"
  {:added "3.0"}
  ([obj]
   (let [t (common/context-class obj)]
     (vec (cons t (class/ancestor:tree t))))))

(defn constructor?
  "checks if if an element is a constructor
 
   (-> (.getConstructors String)
       (first)
       (element/to-element)
       (element/constructor?))
   => true"
  {:added "3.0"}
  ([elem]
   (-> elem :tag (= :constructor))))

(defn method?
  "checks if an element is a method
 
   (-> (.getMethods String)
       (first)
       (element/to-element)
       (element/method?))
   => true"
  {:added "3.0"}
  ([elem]
   (-> elem :tag (= :method))))

(defn field?
  "checks if an element is a field
 
   (-> (.getFields String)
       (first)
       (element/to-element)
       (element/field?))
   => true"
  {:added "3.0"}
  ([elem]
   (-> elem :tag (= :field))))

(defn static?
  "checks if an element is a static one
 
   (->> (.getMethods String)
        (map element/to-element)
        (filter element/static?)
        first)
   ;;#elem[valueOf :: (int) -> java.lang.String]"
  {:added "3.0"}
  ([elem]
   (and (-> elem :modifiers :static boolean)
        (not (constructor? elem)))))

(defn instance?
  "checks if an element is non static
 
   (->> (.getMethods String)
        (map element/to-element)
        (filter element/instance?)
        first)
   ;;#elem[equals :: (java.lang.String, java.lang.Object) -> boolean]"
  {:added "3.0"}
  ([elem]
   (-> elem :modifiers :instance boolean)))

(defn public?
  "checks if an element is public
 
   (->> (.getMethods String)
        (map element/to-element)
        (filter element/public?)
        first)
   ;;#elem[equals :: (java.lang.String, java.lang.Object) -> boolean]"
  {:added "3.0"}
  ([elem]
   (-> elem :modifiers :public boolean)))

(defn private?
  "checks if an element is private
 
   (->> (.getDeclaredFields String)
        (map element/to-element)
        (filter element/private?)
        first)
   ;;#elem[value :: (java.lang.String) | byte[]]"
  {:added "3.0"}
  ([elem]
   (-> elem :modifiers :private boolean)))

(defn protected?
  "checks if an element is protected"
  {:added "3.0"}
  ([elem]
   (-> elem :modifiers :protected boolean)))

(defn plain?
  "checks if an element is neither public or private"
  {:added "3.0"}
  ([elem]
   (-> elem :modifiers :plain boolean)))
