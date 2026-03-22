(ns std.protocol.time-test
  (:require [std.protocol.time :refer :all])
  (:use code.test))

^{:refer std.protocol.time/-time-meta :added "3.0"}
(comment "accesses the meta properties of a class")

^{:refer std.protocol.time/-from-long :added "3.0"}
(comment "creates a time representation from a long")

^{:refer std.protocol.time/-now :added "3.0"}
(comment "creates a representation of the current time")

^{:refer std.protocol.time/-from-length :added "3.0"}
(comment "creates a representation of a duration")

^{:refer std.protocol.time/-formatter :added "3.0"}
(comment "create a representation of a formatter")

^{:refer std.protocol.time/-format :added "3.0"}
(comment "a general function to format what time looks like")

^{:refer std.protocol.time/-parser :added "3.0"}
(comment "creates a parser for parsing strings")

^{:refer std.protocol.time/-parse :added "3.0"}
(comment "generic parse function for a string representation")