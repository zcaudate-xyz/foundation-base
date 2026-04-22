(ns std.object.framework.read-test
  (:require [std.object.framework.base-test]
            [std.object.framework.read :as read]
            [std.object.framework.write :as write]
            [std.object.query :as reflect]
            [std.protocol.object :as object])
  (:use code.test)
  (:import [test PersonBuilder Person Dog DogBuilder Cat Pet]))

^{:refer std.object.framework.read/meta-read :added "3.0"}
(fact "access read attributes"

  (read/meta-read Pet)
  => (contains-in {:class test.Pet
                   :methods {:name {:fn fn?}
                             :species {:fn fn?}}}))

^{:refer std.object.framework.read/meta-read-exact :added "3.0"}
(fact "access read attributes for the exact class"

  (read/meta-read-exact Object)
  => nil)

^{:refer std.object.framework.read/meta-read-exact? :added "3.0"}
(fact "checks if read attributes are avaliable"

  (read/meta-read-exact? Object)
  => false)

^{:refer std.object.framework.read/read-fields :added "3.0"}
(fact "fields of an object from reflection"
  (-> (read/read-fields Dog)
      keys)
  => [:name :species])

^{:refer std.object.framework.read/read-all-fields :added "3.0"}
(fact "all fields of an object from reflection"

  (-> (read/read-all-fields {})
      keys)
  => [:-hash :-hasheq :-meta :array])

^{:refer std.object.framework.read/+read-template+ :added "3.0"}
(fact "returns a function template given tag and name"

  (read/+read-template+ 'String 'hello)
  => '(clojure.core/fn hello [obj] (. obj (hello))))

^{:refer std.object.framework.read/create-read-method-form :added "3.0"}
(fact "creates a method based on a template"
  (eval (read/create-read-method-form (reflect/query-class Dog ["getName" :#])
                                      "get"
                                      read/+read-template+
                                      nil))
  => (contains-in [:name {:type String :fn fn?}]))

^{:refer std.object.framework.read/read-getters-form :added "3.0"}
(fact "creates the form for read-getters"

  (read/read-getters-form Dog)
  => '{:name {:type java.lang.String,
              :fn (clojure.core/fn getName [obj] (. obj (getName)))},
       :species {:type java.lang.String,
                 :fn (clojure.core/fn getSpecies [obj] (. obj (getSpecies)))}})

^{:refer std.object.framework.read/parse-opts :added "3.0"}
(fact "returns map for a given input"

  (def -opts- {:a 1})
  (read/parse-opts '-opts-)
  => {:a 1}

  (read/parse-opts {})
  => {})

^{:refer std.object.framework.read/read-getters :added "3.0"}
(fact "returns fields of an object through getter methods"
  (-> (read/read-getters Dog)
      keys)
  => [:name :species])

^{:refer std.object.framework.read/read-all-getters :added "3.0"}
(fact "returns fields of an object and base classes"
  (-> (read/read-all-getters Dog)
      keys)
  => [:class :name :species])

^{:refer std.object.framework.read/read-ex :added "3.0"}
(fact "creates a getter method that throws an an exception"

  ((read/read-ex :hello) nil)
  => (throws))

^{:refer std.object.framework.read/to-data :added "3.0"}
(fact "creates the object from a string or map"

  (read/to-data "hello")
  => "hello"

  (read/to-data (write/from-map {:name "hello" :species "dog"} Pet))
  => (contains {:name "hello"}))

^{:refer std.object.framework.read/to-map :added "3.0"}
(fact "creates a map from an object"

  (read/to-map (Cat. "spike"))
  => (contains {:name "spike"}))

(comment
  (write/meta-write Cat)

  (code.manage/import))
