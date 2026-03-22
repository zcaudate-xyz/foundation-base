(ns jvm.classloader.base-classloader-test
  (:require [jvm.classloader.base-classloader :refer :all])
  (:use code.test))

^{:refer jvm.classloader.base-classloader/all-class-urls :added "4.0"}
(fact "runs all class urls"
  ^:hidden
  
  (all-class-urls)
  => vector?)


(comment
  (type (first @+class-urls+))
  
  (.getProtectionDomain String)
  (get-loaded-jars)
  (Class/forName (.getName (first (.getDefinedPackages (ClassLoader/getSystemClassLoader))))
                 true
                 )
  
  (seq (.getDefinedPackages (ClassLoader/getSystemClassLoader)))

  (.? (first (.getDefinedPackages (ClassLoader/getSystemClassLoader))))

  (System/getProperty "java.class.path"))
