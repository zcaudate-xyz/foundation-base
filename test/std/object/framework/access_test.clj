(ns std.object.framework.access-test
  (:require [std.object.framework :as framework]
            [std.object.framework.access :as access :refer :all]
            [std.object.framework.base-test]
            [std.object.framework.write :as write])
  (:use code.test)
  (:refer-clojure :exclude [get set get-in keys]))

^{:refer std.object.framework.access/meta-clear :added "3.0"}
(fact "clears all meta-read and meta-write definitions for a class")

^{:refer std.object.framework.access/get-with-keyword :added "3.0"}
(fact "access the fields of an object with keyword"

  (get-with-keyword {:a 1} :a)
  => 1

  (get-with-keyword (test.Cat. "spike")
                    :name)
  => "spike")

^{:refer std.object.framework.access/get-with-array :added "3.0"}
(fact "access the fields of an object with an array of keywords"

  (get-with-array {:a 1} [:a])
  => {:a 1}

  (get-with-array (test.Cat. "spike")
                  [:name])
  => {:name "spike"})

^{:refer std.object.framework.access/get :added "3.0"}
(fact "accessor with either keyword or array lookup"

  (access/get (test.Cat. "spike") :name)
  => "spike")

^{:refer std.object.framework.access/get-in :added "3.0"}
(fact "accesses the nested object using specifiedb path"

  (access/get-in (test.Cat. "spike") [:name]))

^{:refer std.object.framework.access/keys :added "3.0"}
(fact "gets all keys of an object"

  (access/keys (test.Cat. "spike"))
  => (contains [:name]))

^{:refer std.object.framework.access/set-with-keyword :added "3.0"}
(fact "sets the fields of an object with keyword"

  (std.object.framework/map-like
   test.Cat
   {:tag "cat"
    :read :all
    :write :all})

  (-> (doto (test.Cat. "spike")
        (set-with-keyword :name "fluffy"))
      (access/get :name))
  => (any "fluffy"
          (throws)))

^{:refer std.object.framework.access/set :added "3.0"}
(fact "sets the fields of an object with a map"

  (-> (doto (test.Cat. "spike")
        (access/set {:name "fluffy"}))
      (access/get :name))
  => (any "fluffy"
          (throws)))

(comment
  (.hashCode (type (test.Cat. "spike")))

  (-> (test.Cat. "spike") type write/meta-write :methods :name))
