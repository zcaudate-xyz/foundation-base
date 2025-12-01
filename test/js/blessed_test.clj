(ns js.blessed-test
  (:use code.test)
  (:require [js.blessed :as b]))

^{:refer js.blessed/canvas :added "4.0" :unchecked true}
(fact "creates a drawille canvas")

^{:refer js.blessed/createScreen :added "4.0" :unchecked true}
(fact "creates a screen")

^{:refer js.blessed/run :added "4.0" :unchecked true}
(fact "runs the component in the shell")
