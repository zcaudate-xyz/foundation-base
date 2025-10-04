(ns js.react.ext-box-test
  (:use code.test)
  (:require [js.react.ext-box :refer :all]))

^{:refer js.react.ext-box/createBox :added "4.0"}
(fact "creates a box for react")

^{:refer js.react.ext-box/useListenBox :added "4.0"}
(fact "listens to the box out")

^{:refer js.react.ext-box/useBox :added "4.0"}
(fact "getters and setters for the box")

^{:refer js.react.ext-box/attachLocalStorage :added "4.0"}
(fact "attaches localstorage to the box")
