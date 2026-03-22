(ns std.object.framework.base-test
  (:require [std.object.framework.read :as read]
            [std.object.framework.write :as write]
            [std.object.query :as reflect]
            [std.protocol.object :as object])
  (:use code.test))

(defmethod object/-meta-write test.DogBuilder
  [_]
  {:empty (fn [] (test.DogBuilder.))
   :methods (write/write-setters test.DogBuilder)})

(defmethod object/-meta-read test.DogBuilder
  [_]
  {:methods (read/read-fields test.DogBuilder)})

(defmethod print-method test.DogBuilder
  [v ^java.io.Writer w]
  (.write w (str (read/to-data v))))

(defmethod object/-meta-read test.Dog
  [_]
  {:methods (read/read-getters test.Dog)})

(defmethod object/-meta-write test.Dog
  [_]
  {:from-map (fn [m] (-> (dissoc m :species)
                         ^test.DogBuilder (write/from-map test.DogBuilder)
                         (.build)))})

(defmethod object/-meta-read test.Cat
  [_]
  {:methods (read/read-getters test.Cat)})

(defmethod object/-meta-write test.Cat
  [_]
  {:from-map (fn [m] (test.Cat. (:name m)))
   :methods (write/write-fields test.Cat)})

(defmethod object/-meta-write test.Pet
  [_]
  {:from-map (fn [m]
               (let [data (dissoc m :species)]
                 (case (:species m)
                   "dog" (write/from-map data test.Dog)
                   "cat" (write/from-map data test.Cat))))})

(defmethod object/-meta-read test.Pet
  [_]
  {:methods (read/read-getters test.Pet)})

(defmethod object/-meta-write test.PersonBuilder
  [_]
  {:empty (fn [] (test.PersonBuilder.))
   :methods (write/write-setters test.PersonBuilder)})

(defmethod object/-meta-read test.PersonBuilder
  [_]
  {:methods (read/read-fields test.PersonBuilder)})

(defmethod object/-meta-write test.Person
  [_]
  {:from-map (fn [m] (-> m
                         ^test.PersonBuilder (write/from-map test.PersonBuilder)
                         (.build)))})

(defmethod object/-meta-read test.Person
  [_]
  {:methods (read/read-getters test.Person)})

(comment
  (-> {:name "chris" :age 30 :pets [{:name "slurp" :species "dog"}]}
      (write/from-map test.Person)
      (read/to-data))
  (.hashCode test.Pet)
  (./javac '[test])

  ((:from-map (write/meta-write test.Pet))
   {:name "slurp" :species "dog"})

  ((:empty (write/meta-write test.DogBuilder)))

  (-> {:name "fido"}
      (write/from-map test.DogBuilder))

  (read/meta-read test.DogBuilder)
  (read/to-map (-> {:name "fido"}
                   (write/from-map test.DogBuilder)))

  (write/from-map {:species "dog"
                   :name "fido"}
                  test.Pet)
  ((:from-map (write/meta-write test.Pet))
   {:species "dog"
    :name "fido"})

  (write/from-map {:name "fido"}
                  test.Dog)

  (-> {:name "chris" :age 30 :pets []}
      (write/from-map Person)
      (read/to-data)))
