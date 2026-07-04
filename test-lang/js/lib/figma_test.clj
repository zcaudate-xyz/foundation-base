(ns js.lib.figma-test
  (:require [js.lib.figma :refer :all])
  (:use code.test))

^{:refer js.lib.figma/generate-blocks :added "4.0" :unchecked true}
(fact "generates a block layout for figma components"

  (some? (generate-blocks))
  => true)

^{:refer js.lib.figma/init-components :added "4.0" :unchecked true}
(fact "registers figma components"

  (init-components)
  => true)