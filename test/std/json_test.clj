(ns std.json-test
  (:use code.test)
  (:require [std.json :refer :all])
  (:refer-clojure :exclude [read]))

^{:refer std.json/clojure-module :added "3.0"}
(fact "adds serializers for clojure objects"

  (clojure-module {})
  => com.fasterxml.jackson.databind.module.SimpleModule)

^{:refer std.json/object-mapper :added "3.0"}
(fact "creates an object-mapper for json output"

  (object-mapper {})
  => com.fasterxml.jackson.databind.ObjectMapper)

^{:refer std.json/read :added "3.0"}
(fact "reads json as clojure data"

  (read "{\"a\":1,\"b\":2}")
  => {"a" 1, "b" 2})

^{:refer std.json/write :added "3.0"}
(fact "writes clojure data to json"

  (write {:a 1 :b 2}) => "{\"a\":1,\"b\":2}")

^{:refer std.json/write-pp :added "4.0"}
(fact "pretty print json output")

^{:refer std.json/write-bytes :added "3.0"}
(fact "writes clojure data to json bytes"

  (String. (write-bytes {:a 1 :b 2}))
  => "{\"a\":1,\"b\":2}")

^{:refer std.json/write-to :added "3.0"}
(fact "writes clojure data to a sink"

  (def -out- (java.io.ByteArrayOutputStream.))

  (write-to -out- {:a 1 :b 2})

  (.toString ^java.io.ByteArrayOutputStream -out-)
  => "{\"a\":1,\"b\":2}")

^{:refer std.json/sys:resource-json :added "4.0"}
(fact "returns cached json map of on a resource")
