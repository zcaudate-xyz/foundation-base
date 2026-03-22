(ns std.pretty.protocol-test
  (:require [std.pretty.protocol :refer :all])
  (:use code.test))

^{:refer std.pretty.protocol/-serialize-node :added "3.0"}
(fact "an extendable method for defining serializing tags")

^{:refer std.pretty.protocol/-document :added "3.0"}
(fact "representation of a printable document object")

^{:refer std.pretty.protocol/-text :added "3.0"}
(fact "representation of a printable text object")
