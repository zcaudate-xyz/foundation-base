(ns js.react-native.ui-spinner-basic-test
  (:require [js.react-native.ui-spinner-basic :refer :all])
  (:use code.test))

^{:refer js.react-native.ui-spinner-basic/spinnerTheme :added "4.0" :unchecked true}
(fact "creates a spinner theme")

^{:refer js.react-native.ui-spinner-basic/useSpinnerPosition :added "4.0" :unchecked true}
(fact "gets the spinner position")

^{:refer js.react-native.ui-spinner-basic/SpinnerStatic :added "4.0" :unchecked true}
(fact "creates a static spinner")

^{:refer js.react-native.ui-spinner-basic/SpinnerBasicValues :added "4.0" :unchecked true}
(fact "creates basic values for spinner")

^{:refer js.react-native.ui-spinner-basic/SpinnerBasic :added "4.0" :unchecked true}
(fact "creates a basic spinner")
