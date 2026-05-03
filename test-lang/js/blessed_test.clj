(ns js.blessed-test
  (:require [js.blessed :as b])
  (:use code.test))

^{:refer js.blessed/canvas :added "4.0" :unchecked true}
(fact "creates a drawille canvas")

^{:refer js.blessed/createScreen :added "4.0" :unchecked true}
(fact "creates a screen")

^{:refer js.blessed/run :added "4.0" :unchecked true}
(fact "runs the component in the shell")
