(ns code.heal-test
  (:use code.test)
  (:require [code.heal :as heal]))

^{:refer code.heal/heal-code-single :added "4.0"}
(fact "helper function for heal-code"
  ^:hidden

  (code.project/in-context
   (heal/heal-code-single {}))
  => {:changed [], :updated false, :path "test/code/heal_test.clj"})

^{:refer code.heal/heal-code :added "4.0"}
(fact "helper function to fix parents")

^{:refer code.heal/print-rainbox :added "4.0"}
(fact "prints out the code in rainbow"

  (heal/print-rainbox
   (slurp "test/code/heal_test.clj")))
