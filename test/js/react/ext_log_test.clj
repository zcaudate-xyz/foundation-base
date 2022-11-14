(ns js.react.ext-log-test
  (:use code.test)
  (:require [js.react.ext-log :refer :all]))

^{:refer js.react.ext-log/makeLog :added "4.0"}
(fact "creates a log for react")

^{:refer js.react.ext-log/listenLogLatest :added "4.0"}
(fact "uses the latest log entry")
