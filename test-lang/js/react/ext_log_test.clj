(ns js.react.ext-log-test
  (:require [js.react.ext-log :refer :all])
  (:use code.test))

^{:refer js.react.ext-log/makeLog :added "4.0" :unchecked true}
(fact "creates a log for react")

^{:refer js.react.ext-log/listenLogLatest :added "4.0" :unchecked true}
(fact "uses the latest log entry")
