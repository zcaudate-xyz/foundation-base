(ns std.protocol.object-test
  (:require [std.protocol.object :refer :all])
  (:use code.test))

^{:refer std.protocol.object/-meta-read :added "3.0"}
(comment "accesses class meta information for reading from object")

^{:refer std.protocol.object/-meta-write :added "3.0"}
(comment "accesses class meta information for writing to the object")