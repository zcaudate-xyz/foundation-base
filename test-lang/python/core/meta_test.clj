(ns python.core.meta-test
  (:require [python.core.meta :refer :all])
  (:use code.test))

^{:refer python.core.meta/fetch-html :added "3.0"}
(fact "fetches the doc containing builtin descriptions")

^{:refer python.core.meta/get-html :added "3.0"}
(fact "gets from file or fetch from source")

^{:refer python.core.meta/build-props :added "3.0"}
(fact "helper function for individual functions")

^{:refer python.core.meta/build-builtins :added "3.0"}
(fact "build builtin map from html")

^{:refer python.core.meta/get-builtins :added "3.0"}
(fact "gets the builtin map")

^{:refer python.core.meta/clean :added "3.0"}
(fact "cleans html and edn function")

^{:refer python.core.meta/create :added "3.0"}
(fact "creates the meta descriptions for the builtins")
