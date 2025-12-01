(ns js.react.helper-data-test
  (:use code.test)
  (:require [js.react.helper-data :refer :all]))

^{:refer js.react.helper-data/wrapMemoize :added "4.0" :unchecked true}
(fact "wraps a component to pass data")

^{:refer js.react.helper-data/useWrappedComponent :added "4.0" :unchecked true}
(fact "uses a wrappedComponent")

^{:refer js.react.helper-data/wrapData :added "4.0" :unchecked true}
(fact "wraps the data")

^{:refer js.react.helper-data/wrapForward :added "4.0" :unchecked true}
(fact "allows :ref to be passed")
