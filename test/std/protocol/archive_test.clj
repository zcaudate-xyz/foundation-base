(ns std.protocol.archive-test
  (:use code.test)
  (:require [std.protocol.archive :as archive]))

^{:refer std.protocol.archive/-open :added "3.0"}
(comment "allows the opening of zip and jar files")