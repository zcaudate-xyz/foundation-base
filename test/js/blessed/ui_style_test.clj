(ns js.blessed.ui-style-test
  (:use code.test)
  (:require [js.blessed.ui-style :refer :all]))

^{:refer js.blessed.ui-style/getColor :added "4.0" :unchecked true}
(fact "helper function to get color props")

^{:refer js.blessed.ui-style/getLayout :added "4.0" :unchecked true}
(fact "helper function to get layout props")

^{:refer js.blessed.ui-style/getTopProps :added "4.0" :unchecked true}
(fact "helper function for top layout props")

^{:refer js.blessed.ui-style/omitLayoutProps :added "4.0" :unchecked true}
(fact "helper function for stripping layout props")

^{:refer js.blessed.ui-style/styleMinimal :added "4.0" :unchecked true}
(fact "gets the minimal style")

^{:refer js.blessed.ui-style/styleSmall :added "4.0" :unchecked true}
(fact "gets the small style")

^{:refer js.blessed.ui-style/styleInvert :added "4.0" :unchecked true}
(fact "gets the invert style")

^{:refer js.blessed.ui-style/styleListView :added "4.0" :unchecked true}
(fact "gets the list view style")
