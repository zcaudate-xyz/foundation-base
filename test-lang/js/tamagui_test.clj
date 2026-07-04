(ns js.tamagui-test
  (:require [js.tamagui :refer :all])
  (:use code.test))

^{:refer js.tamagui/generate-blocks :added "4.0" :unchecked true}
(fact "generates a block layout for tamagui components"

  (some? (generate-blocks))
  => true)

^{:refer js.tamagui/init-components :added "4.0" :unchecked true}
(fact "registers tamagui components"

  (init-components)
  => true)