(ns js.lib.radix-test
  (:require [js.lib.radix :refer :all])
  (:use code.test))

^{:refer js.lib.radix/generate-blocks :added "4.0" :unchecked true}
(fact "generates a block layout for radix components"

  (some? (generate-blocks))
  => true)

^{:refer js.lib.radix/init-components :added "4.0" :unchecked true}
(fact "registers radix components"

  (init-components)
  => true)