(ns jvm.classloader.common-test
  (:require [jvm.classloader.common :refer :all])
  (:use code.test))

^{:refer jvm.classloader.common/to-url :added "3.0"}
(fact "constructs a `java.net.URL` object from a string"

  (str (to-url "/dev/null"))
  => "file:/dev/null/")

^{:refer jvm.classloader.common/bytecode-version :added "4.0"}
(fact "gets the bytecode version of a class file"

  (bytecode-version "target/classes/test/Cat.class")
  => (contains-in
      {:minor-version int?
       :major-version int?
       :jdk-version string?}))
