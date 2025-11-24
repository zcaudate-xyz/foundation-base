(ns code.ai.base-client-test
  (:use code.test)
  (:require [code.ai.base-client :refer :all]))

^{:refer code.ai.base-client/write-log :added "4.0"}
(fact "writes to the log")