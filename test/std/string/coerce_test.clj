(ns std.string.coerce-test
  (:require [std.string.coerce :refer :all])
  (:use code.test))

^{:refer std.string.coerce/from-string :added "3.0"}
(fact "converts a string to an object"

  (from-string "a" clojure.lang.Symbol)
  => 'a

  (from-string "std.string" clojure.lang.Namespace)
  => (find-ns 'std.string))

^{:refer std.string.coerce/to-string :added "3.0"}
(fact "converts an object to a string"

  (to-string :hello/world)
  => "hello/world"

  (to-string *ns*)
  => "std.string.coerce-test")

^{:refer std.string.coerce/path-separator :added "3.0"}
(fact "returns the default path separator for an object"

  (path-separator clojure.lang.Namespace)
  => "."

  (path-separator clojure.lang.Keyword)
  => "/")

^{:refer std.string.coerce/str:op :added "3.0"}
(fact "wraps a string function such that it can take any string-like input"

  ((str:op str false) :hello 'hello)
  => :hellohello

  ((str:op str true) :hello 'hello)
  => "hellohello")

^{:refer std.string.coerce/str:compare :added "3.0"}
(fact "wraps a function so that it can compare any two string-like inputs"

  ((str:compare =) :hello 'hello)
  => true)
