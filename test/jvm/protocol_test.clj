(ns jvm.protocol-test
  (:require [jvm.protocol :refer :all])
  (:use code.test))

^{:refer jvm.protocol/-load-class :added "4.0"}
(fact  "loads a class from various sources")

^{:refer jvm.protocol/-rep :added "4.0"}
(fact  "multimethod definition for coercing to a rep")

^{:refer jvm.protocol/-artifact :added "4.0"}
(fact "multimethod definition for coercing to an artifact type")
