(ns js.lib.react-hook-form-test
  (:use code.test)
  (:require [js.lib.react-hook-form :refer :all]))

^{:refer js.lib.react-hook-form/useFormState :added "4.1"}
(fact "is defined"

  (var? #'useFormState)
  => true)

^{:refer js.lib.react-hook-form/useFormStateMap :added "4.1"}
(fact "is defined"

  (var? #'useFormStateMap)
  => true)

^{:refer js.lib.react-hook-form/useControls :added "4.1"}
(fact "is defined"

  (var? #'useControls)
  => true)

^{:refer js.lib.react-hook-form/mergeContexts :added "4.1"}
(fact "is defined"

  (var? #'mergeContexts)
  => true)
