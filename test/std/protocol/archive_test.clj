(ns std.protocol.archive-test
  (:require [std.protocol.archive :as archive])
  (:use code.test))

^{:refer std.protocol.archive/-open :added "3.0"}
(comment "allows the opening of zip and jar files")