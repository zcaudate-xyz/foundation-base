(ns std.object.framework.map-like-test
  (:require [std.object.framework.map-like :refer :all]
            [std.object.framework.write :as write])
  (:use code.test))

^{:refer std.object.framework.map-like/key-selection :added "3.0"}
(fact "selects map based on keys"

  (key-selection {:a 1 :b 2} [:a] nil)
  => {:a 1}

  (key-selection {:a 1 :b 2} nil [:a])
  => {:b 2})

^{:refer std.object.framework.map-like/read-proxy-functions :added "3.0"}
(fact "creates a proxy access through a field in the object"

  (read-proxy-functions {:school [:name :raw]})
  => '{:name {:type java.lang.Object
              :fn (clojure.core/fn [obj]
                    (clojure.core/let [proxy (std.object.framework.access/get obj :school)]
                      (std.object.framework.access/get proxy :name)))},
       :raw  {:type java.lang.Object
              :fn (clojure.core/fn [obj]
                    (clojure.core/let [proxy (std.object.framework.access/get obj :school)]
                      (std.object.framework.access/get proxy :raw)))}})

^{:refer std.object.framework.map-like/write-proxy-functions :added "3.0"}
(fact "creates a proxy access through a field in the object"

  (write-proxy-functions {:school [:name :raw]})
  => '{:name {:type java.lang.Object
              :fn (clojure.core/fn [obj v]
                    (clojure.core/let [proxy (std.object.framework.access/get obj :school)]
                      (std.object.framework.access/set proxy :name v)))},
       :raw  {:type java.lang.Object
              :fn (clojure.core/fn [obj v]
                    (clojure.core/let [proxy (std.object.framework.access/get obj :school)]
                      (std.object.framework.access/set proxy :raw v)))}})

^{:refer std.object.framework.map-like/extend-map-read :added "3.0"}
(fact "creates the forms for read methods"

  (extend-map-read 'test.Cat {:read :fields})
  => '{:methods (std.object.framework.map-like/key-selection
                 (std.object.framework.read/read-fields test.Cat) nil nil)})

^{:refer std.object.framework.map-like/extend-map-write :added "3.0"}
(fact "creates the forms for write methods"

  (extend-map-write 'test.Cat {:write {:methods :class}})
  => '{:methods (std.object.framework.write/write-setters test.Cat)})

^{:refer std.object.framework.map-like/extend-map-like :added "3.0"}
(fact "creates an entry for map-like classes"

  (extend-map-like test.DogBuilder
                   {:tag "build.dog"
                    :write {:empty (fn [] (test.DogBuilder.))
                            :methods :class}
                    :read :fields})

  (extend-map-like test.Dog {:tag "dog"
                             :write  {:methods :fields
                                      :from-map (fn [m] (-> (dissoc m :species)
                                                            ^test.DogBuilder (write/from-map test.DogBuilder)
                                                            (.build)))}
                             :exclude [:species]})

  (with-out-str
    (prn (write/from-data {:name "hello"} test.Dog)))
  => "#dog{:name \"hello\"}\n"

  (extend-map-like test.Cat {:tag "cat"
                             :write  {:from-map (fn [m] (test.Cat. (:name m)))
                                      :methods :class}
                             :exclude [:species]})

  (extend-map-like test.Pet {:tag "pet"
                             :write {:methods :class
                                     :from-map (fn [m] (case (:species m)
                                                         "dog" (write/from-map m test.Dog)
                                                         "cat" (write/from-map m test.Cat)))}})

  (with-out-str
    (prn (write/from-data {:name "hello" :species "cat"} test.Pet)))
  => "#cat{:name \"hello\"}\n")

(comment
  (./javac)
  (code.manage/import))
