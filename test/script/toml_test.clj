(ns script.toml-test
  (:use code.test)
  (:require [script.toml :refer :all])
  (:refer-clojure :exclude [read]))

^{:refer script.toml/java->clojure :added "3.0"}
(fact "converts java object to map"
  (java->clojure (java.util.HashMap. {"a" 1}) :keywordize)
  => {:a 1})

^{:refer script.toml/clojure->java :added "3.0"}
(fact "converts map to java object"
  (clojure->java {:a 1})
  => java.util.HashMap)

^{:refer script.toml/read :added "3.0"}
(fact "reads toml to map"
  (read "a = 1" :keywordize)
  => {:a 1})

^{:refer script.toml/write :added "3.0"}
(fact "writes map to toml"
  (write {:a 1})
  => "a = 1\n")
