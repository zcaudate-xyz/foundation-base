(ns xt.runtime.interface-collection-test
  (:use code.test)
  (:require [xt.runtime.interface-collection :refer :all]))

^{:refer xt.runtime.interface-collection/start-string :added "4.0"}
(fact "gets the start string of the collection")

^{:refer xt.runtime.interface-collection/end-string :added "4.0"}
(fact "gets the end string of the collection")

^{:refer xt.runtime.interface-collection/sep-string :added "4.0"}
(fact "gets the separator string of the collection")

^{:refer xt.runtime.interface-collection/is-ordered? :added "4.0"}
(fact "checks if the collection is ordered")

^{:refer xt.runtime.interface-collection/coll-size :added "4.0"}
(fact "gets the size of the collection")

^{:refer xt.runtime.interface-collection/coll-hash-ordered :added "4.0"}
(fact "computes the hash for an ordered collection")

^{:refer xt.runtime.interface-collection/coll-hash-unordered :added "4.0"}
(fact "computes the hash for an unordered collection")

^{:refer xt.runtime.interface-collection/coll-show :added "4.0"}
(fact "returns the string representation of the collection")

^{:refer xt.runtime.interface-collection/coll-into-iter :added "4.0"}
(fact "populates collection from iterator")

^{:refer xt.runtime.interface-collection/coll-into-array :added "4.0"}
(fact "populates collection from array")

^{:refer xt.runtime.interface-collection/coll-eq :added "4.0"}
(fact "checks equality of two collections")
