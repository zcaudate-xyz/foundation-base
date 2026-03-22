(ns std.object.element-test
  (:require [std.object.element :as element])
  (:use code.test)
  (:refer-clojure :exclude [instance?]))

^{:refer std.object.element/to-element :added "3.0"}
(fact "converts a `java.reflect` object to a `std.object.element` one"

  (element/to-element (first (.getMethods String)))
  ;; #elem[equals :: (java.lang.String, java.lang.Object) -> boolean]
  )

^{:refer std.object.element/element-params :added "3.0"}
(fact "returns the arglist of a particular element"

  (-> (first (.getMethods String))
      (element/to-element)
      (element/element-params))
  ;;=> '([java.lang.String java.lang.Object])
  )

^{:refer std.object.element/class-info :added "3.0"}
(fact "Lists class information"

  (element/class-info String)
  => (contains {:name "java.lang.String"
                :hash anything
                :modifiers #{:instance :class :public :final}}))

^{:refer std.object.element/class-hierarchy :added "3.0"}
(fact "Lists the class and interface hierarchy for the class"
  
  (element/class-hierarchy String)
  => [java.lang.String
      [java.lang.Object
       #{java.lang.CharSequence
         java.lang.constant.ConstantDesc
         java.lang.constant.Constable
         java.lang.Comparable
         java.io.Serializable}]])

^{:refer std.object.element/constructor? :added "3.0"}
(fact "checks if if an element is a constructor"

  (-> (.getConstructors String)
      (first)
      (element/to-element)
      (element/constructor?))
  => true)

^{:refer std.object.element/method? :added "3.0"}
(fact "checks if an element is a method"

  (-> (.getMethods String)
      (first)
      (element/to-element)
      (element/method?))
  => true)

^{:refer std.object.element/field? :added "3.0"}
(fact "checks if an element is a field"

  (-> (.getFields String)
      (first)
      (element/to-element)
      (element/field?))
  => true)

^{:refer std.object.element/static? :added "3.0"}
(fact "checks if an element is a static one"

  (->> (.getMethods String)
       (map element/to-element)
       (filter element/static?)
       first)
  ;;#elem[valueOf :: (int) -> java.lang.String]
  )

^{:refer std.object.element/instance? :added "3.0"}
(fact "checks if an element is non static"

  (->> (.getMethods String)
       (map element/to-element)
       (filter element/instance?)
       first)
  ;;#elem[equals :: (java.lang.String, java.lang.Object) -> boolean]
  )

^{:refer std.object.element/public? :added "3.0"}
(fact "checks if an element is public"

  (->> (.getMethods String)
       (map element/to-element)
       (filter element/public?)
       first)
  ;;#elem[equals :: (java.lang.String, java.lang.Object) -> boolean]
  )

^{:refer std.object.element/private? :added "3.0"}
(fact "checks if an element is private"

  (->> (.getDeclaredFields String)
       (map element/to-element)
       (filter element/private?)
       first)
  ;;#elem[value :: (java.lang.String) | byte[]]
  )

^{:refer std.object.element/protected? :added "3.0"}
(fact "checks if an element is protected")

^{:refer std.object.element/plain? :added "3.0"}
(fact "checks if an element is neither public or private")
