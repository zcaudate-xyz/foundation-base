(ns net.http.common-test
  (:require [net.http.common :refer :all])
  (:use code.test))

^{:refer net.http.common/-write-value :added "0.5"}
(fact "writes the string value of the datastructure according to format"

  (-write-value {:a 1} :edn)
  => "{:a 1}")

^{:refer net.http.common/-read-value :added "0.5"}
(fact "read the string value of the datastructure according to format"

  (-read-value "{:a 1}" :edn)
  => {:a 1})

^{:refer net.http.common/-read-body :added "0.5"}
(fact "reads the body of the request can be expanded"

  (-read-body "{:a 1}" :edn)
  => {:a 1})

^{:refer net.http.common/-create-server :added "0.5"}
(fact "multimethod entrypoint for server construction")
