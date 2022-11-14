(ns code.link.java-test
  (:use code.test)
  (:require [code.link.java :refer :all]
            [clojure.java.io :as io]))

^{:refer code.link.java/get-class :added "3.0"}
(fact "grabs the symbol of the class in the java file"
  (get-class
   (io/file "test-java/test/Cat.java"))
  => 'test.Cat)

^{:refer code.link.java/get-imports :added "3.0"}
(fact "grabs the symbol of the class in the java file"
  (get-imports
   (io/file "test-java/test/Cat.java"))
  => '())
