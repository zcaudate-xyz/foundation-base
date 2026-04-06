(ns jvm.tool-test
  (:require [jvm.tool :refer :all])
  (:use code.test))

^{:refer jvm.tool/hotkey-set :added "4.0"}
(fact "set the hotkey function"
  (let [original @#'hotkey-0]
    (try
      (hotkey-set 0 (constantly :ok))
      [(fn? @#'hotkey-0)
       (@#'hotkey-0)]
      (finally
        (hotkey-set 0 original))))
  => [true :ok])
