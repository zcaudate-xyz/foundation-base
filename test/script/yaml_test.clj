(ns script.yaml-test
  (:use code.test)
  (:require [script.yaml :refer :all])
  (:refer-clojure :exclude [read]))

^{:refer script.yaml/make-dumper-options :added "3.0"}
(fact "creates encoding options"
  (make-dumper-options :flow-style :block)
  => org.yaml.snakeyaml.DumperOptions)

^{:refer script.yaml/make-yaml :added "3.0"}
(fact "Make a yaml encoder/decoder with some given options."
  (make-yaml :unsafe true) => org.yaml.snakeyaml.Yaml)

^{:refer script.yaml/mark :added "3.0"}
(fact  "Mark some data with start and end positions."
  (mark {:line 1} {:line 2} "data")
  => (contains {:start {:line 1} :end {:line 2} :unmark "data"}))

^{:refer script.yaml/marked? :added "3.0"}
(fact "Let us know whether this piece of data is marked with source positions."
  (marked? (mark {} {} "")) => true)

^{:refer script.yaml/unmark :added "3.0"}
(fact "Strip the source information from this piece of data, if it exists."
  (unmark (mark {} {} "data")) => "data")

^{:refer script.yaml/write :added "3.0"}
(fact "writes map to yaml"
  (write {:a 1})
  => "{a: 1}\n")

^{:refer script.yaml/read :added "3.0"}
(fact "reads map from yaml"
  (read "a: 1")
  => {:a 1})
