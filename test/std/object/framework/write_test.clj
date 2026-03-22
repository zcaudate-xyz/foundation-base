(ns std.object.framework.write-test
  (:require [std.object.framework.base-test]
            [std.object.framework.read :as read]
            [std.object.framework.write :as write]
            [std.object.query :as reflect]
            [std.protocol.object :as object])
  (:use code.test)
  (:import [test PersonBuilder Person Dog DogBuilder Cat Pet]))

^{:refer std.object.framework.write/+write-template+ :added "3.0"}
(comment "constructs the write function template")

^{:refer std.object.framework.write/meta-write :added "3.0"}
(fact "access read-attributes with caching"

  (write/meta-write DogBuilder)
  => (contains-in {:class test.DogBuilder
                   :empty fn?,
                   :methods {:name {:type java.lang.String, :fn fn?}}}))

^{:refer std.object.framework.write/meta-write-exact :added "3.0"}
(fact "access write attributes for the exact class"

  (write/meta-write-exact Object)
  => nil)

^{:refer std.object.framework.write/meta-write-exact? :added "3.0"}
(fact "checks if write attributes are avaliable"

  (write/meta-write-exact? Object)
  => false)

^{:refer std.object.framework.write/write-fields :added "3.0"}
(fact "write fields of an object from reflection"

  (-> (write/write-fields Dog)
      keys)
  => [:name :species])

^{:refer std.object.framework.write/write-all-fields :added "3.0"}
(fact "all write fields of an object from reflection"

  (-> (write/write-all-fields {})
      keys)
  => [:-hash :-hasheq :-meta :array])

^{:refer std.object.framework.write/create-write-method-form :added "3.0"}
(fact "create a write method from the template"

  (-> ^test.Cat
   ((eval (-> (write/create-write-method-form (reflect/query-class Cat ["setName" :#])
                                              "set"
                                              write/+write-template+
                                              {})
              second
              :fn)) (test.Cat. "spike") "fluffy")
      (.getName))
  => "fluffy")

^{:refer std.object.framework.write/write-setters-form :added "3.0"}
(fact "constructs forms for a given object"

  (write/write-setters-form DogBuilder)
  => '{:name {:type java.lang.String,
              :fn (clojure.core/fn setName [obj val]
                    (clojure.core/or (. obj (setName val)) obj))}})

^{:refer std.object.framework.write/write-setters :added "3.0"}
(fact "write fields of an object through setter methods"

  (write/write-setters Dog)
  => {}

  (keys (write/write-setters DogBuilder))
  => [:name])

^{:refer std.object.framework.write/write-all-setters :added "3.0"}
(fact "write all setters of an object and base classes"

  (write/write-all-setters Dog)
  => {}

  (keys (write/write-all-setters DogBuilder))
  => [:name])

^{:refer std.object.framework.write/write-ex :added "3.0"}
(fact "writes a getter method that throws an an exception"

  ((write/write-ex :hello) nil nil)
  => (throws))

^{:refer std.object.framework.write/write-constructor :added "3.0"}
(fact "returns constructor element for a given class"

  (write/write-constructor Cat [:name])
  ;;#elem[new :: (java.lang.String) -> test.Cat]
  => ifn?)

^{:refer std.object.framework.write/write-setter-element :added "3.0"}
(fact "also constructs array elements"

  (write/write-setter-element {:element Dog} [{:name "fido"}])
  => (contains [test.Dog]))

^{:refer std.object.framework.write/write-setter-value :added "3.0"}
(fact "sets the property given keyword and value"

  (-> ^test.Cat
   (write/write-setter-value {:name {:type String
                                     :fn (fn [^test.Cat cat name] (.setName cat name))}}
                             (Cat. "lido")
                             :name
                             "tim")
      (.getName))
  => "tim")

^{:refer std.object.framework.write/from-empty :added "3.0"}
(fact "creates the object from an empty object constructor"

  (write/from-empty {:name "chris" :pet "dog"}
                    (fn [] (java.util.Hashtable.))
                    {:name {:type String
                            :fn (fn [^java.util.Hashtable obj v]
                                  (.put obj "hello" (keyword v))
                                  obj)}
                     :pet  {:type String
                            :fn (fn [^java.util.Hashtable obj v]
                                  (.put obj "pet" (keyword v))
                                  obj)}})
  => {"pet" :dog, "hello" :chris})

^{:refer std.object.framework.write/from-constructor :added "3.0"}
(fact "creates the object from a constructor"

  (-> {:name "spike"}
      (write/from-constructor {:fn (fn [name] (Cat. name))
                               :params [:name]}
                              {:name {:type String}}
                              test.Cat))
  ;; #test.Cat{:name "spike", :species "cat"}
  => test.Cat)

^{:refer std.object.framework.write/from-map :added "3.0"}
(fact "creates the object from a map"

  (-> {:name "chris" :age 30 :pets [{:name "slurp" :species "dog"}
                                    {:name "happy" :species "cat"}]}
      (write/from-map test.Person)
      (read/to-data))
  => (contains-in
      {:name "chris",
       :age 30,
       :pets [{:name "slurp"}
              {:name "happy"}]}))

^{:refer std.object.framework.write/from-data :added "3.0"}
(fact "creates the object from data"

  (-> (write/from-data ["hello"] (Class/forName "[Ljava.lang.String;"))
      seq)
  => ["hello"])

(comment
  (./import)

  (write/meta-write-exact test.Person))
