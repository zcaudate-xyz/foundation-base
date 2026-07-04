(ns indigo.prompt.translate-figma-make-test
  (:require [indigo.prompt.translate-figma-make :refer :all])
  (:use code.test))

^{:refer indigo.prompt.translate-figma-make/defrule :added "4.0"}
(fact "defrule is a no-op macro returning nil"
  (defrule :x {:from ["a"] :to (var x 1)})
  => nil)