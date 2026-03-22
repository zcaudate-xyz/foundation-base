(ns code.framework.link.java-test
  (:require [clojure.java.io :as io]
            [code.framework.link.java :refer :all])
  (:use code.test))

^{:refer code.framework.link.java/get-class :added "3.0"}
(fact "grabs the symbol of the class in the java file"
  (get-class
   (io/file "test-java/test/Cat.java"))
  => 'test.Cat)

^{:refer code.framework.link.java/get-imports :added "3.0"}
(fact "grabs the symbol of the class in the java file"
  (get-imports
   (io/file "test-java/test/Cat.java"))
  => '())
