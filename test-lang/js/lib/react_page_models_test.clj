(ns js.lib.react-page-models-test
  (:require [js.lib.react-page-models :refer [usePageModels]]
            [code.test :refer [fact]]))

^{:refer js.lib.react-page-models/usePageModels :added "4.1" :unchecked true}
(fact "is defined"

  (var? #'usePageModels)
  => true)
