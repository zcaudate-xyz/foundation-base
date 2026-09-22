(ns js.lib.react-hook-form-test
  (:require [lang.core :as l]
            [lang.core.impl :as impl]
            [js.lib.react-hook-form :refer :all])
  (:use code.test)
  )

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
(fact "emits a JavaScript rest parameter and spreads contexts"

  (let [emitted (impl/emit-str
                 '(defn mergeContexts
                    [(:.. contexts)]
                    (return (Object.assign {} (:.. contexts))))
                 {:lang :js
                  :layout :flat})]
    {:defined (var? #'mergeContexts)
     :emitted emitted})
  => {:defined true
      :emitted "function mergeContexts(...contexts){\n  return Object.assign({},...contexts);\n}"})
