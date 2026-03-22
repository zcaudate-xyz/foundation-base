(ns jvm.artifact.common-test
  (:require [jvm.artifact.common :refer :all])
  (:use code.test))

^{:refer jvm.artifact.common/resource-entry-symbol :added "3.0"}
(fact "creates a path based on a symbol"

  (resource-entry-symbol 'code.test)
  => "code/test.clj"

  (resource-entry-symbol 'clojure.lang.AFn)
  => "clojure/lang/AFn.class")

^{:refer jvm.artifact.common/resource-entry :added "3.0"}
(fact "creates a entry-path based on input"

  (resource-entry "hello/world.txt")
  => "hello/world.txt"

  (resource-entry 'version-clj.core)
  => "version_clj/core.clj"

  (resource-entry java.io.File)
  => "java/io/File.class")
