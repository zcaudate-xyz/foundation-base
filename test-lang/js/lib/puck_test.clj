(ns js.lib.puck-test
  (:require [js.lib.puck :refer :all])
  (:use code.test))

^{:refer js.lib.puck/init-components :added "4.0" :unchecked true}
(fact "registers puck components"

  (init-components)
  => true)

^{:refer js.lib.puck/generate-blocks :added "4.0" :unchecked true}
(fact "generates a block layout for puck components"

  (some? (generate-blocks))
  => true)