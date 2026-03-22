(ns std.lib.mutable-test
  (:require [std.lib.mutable :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [set update]))

(defmutable Hello [name value])

^{:refer std.lib.mutable/defmutable :added "3.0"}
(fact "allows definition of a mutable datastructure"

  (def -h- (Hello. "dog" 1))
  
  (get -h- :name) => "dog")

^{:refer std.lib.mutable/mutable:fields :added "3.0"}
(fact "returns all the fields of"

  (mutable:fields (Hello. "dog" 1))
  => [:name :value])

^{:refer std.lib.mutable/mutable:set :added "3.0"}
(fact "sets the value of a given field"

  (-> (mutable:set -h- :name "cat")
      :name)
  => "cat")

^{:refer std.lib.mutable/mutable:set-new :added "3.0"}
(fact "sets the value of a given field only when it is nil"

  (-> (Hello. "cat" nil)
      (mutable:set-new :name "dog"))
  => (throws)^:hidden

  (-> (Hello. nil nil)
      (mutable:set-new :name "dog")
      :name)
  => "dog")

^{:refer std.lib.mutable/mutable:update :added "3.0"}
(fact "applies a function to a given field"

  (-> (Hello. "dog" 123)
      (mutable:update :value inc)
      :value)
  => 124)

^{:refer std.lib.mutable/mutable:clone :added "3.0"}
(fact "creates a copy of the object"

  (-> (Hello. "dog" 123)
      (mutable:clone)
      ((juxt :name :value)))
  => ["dog" 123])

^{:refer std.lib.mutable/mutable:copy :added "3.0"}
(fact "copies values from a given source"

  (-> (Hello. "dog" 123)
      (mutable:copy (Hello. "cat" 456))
      ((juxt :name :value)))
  => ["cat" 456])


(comment
  (./import)
  )
