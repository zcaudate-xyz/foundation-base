(ns kmi.lang.interface-collection-test
  (:require [kmi.lang.interface-collection :refer :all])
  (:use code.test))

^{:refer kmi.lang.interface-collection/start-string :added "4.0"}
(fact "gets the start string of the collection")

^{:refer kmi.lang.interface-collection/end-string :added "4.0"}
(fact "gets the end string of the collection")

^{:refer kmi.lang.interface-collection/sep-string :added "4.0"}
(fact "gets the separator string of the collection")

^{:refer kmi.lang.interface-collection/is-ordered? :added "4.0"}
(fact "checks if the collection is ordered")

^{:refer kmi.lang.interface-collection/coll-size :added "4.0"}
(fact "gets the size of the collection")

^{:refer kmi.lang.interface-collection/coll-hash-ordered :added "4.0"}
(fact "computes the hash for an ordered collection")

^{:refer kmi.lang.interface-collection/coll-hash-unordered :added "4.0"}
(fact "computes the hash for an unordered collection")

^{:refer kmi.lang.interface-collection/coll-show :added "4.0"}
(fact "returns the string representation of the collection")

^{:refer kmi.lang.interface-collection/coll-into-iter :added "4.0"}
(fact "populates collection from iterator")

^{:refer kmi.lang.interface-collection/coll-into-array :added "4.0"}
(fact "populates collection from array")

^{:refer kmi.lang.interface-collection/coll-eq :added "4.0"}
(fact "checks equality of two collections")
