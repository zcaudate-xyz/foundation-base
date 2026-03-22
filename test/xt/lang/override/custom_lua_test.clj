(ns xt.lang.override.custom-lua-test
  (:require [std.lang :as l]
            [xt.lang.override.custom-lua :refer :all])
  (:use code.test))

(l/script :lua
  {:runtime :oneshot
   :layout :full
   :require [[xt.lang :as k]
             [xt.lang.base-lib :as lib :include [:fn]]]})

^{:refer xt.lang.override.custom-lua/pad-left :added "4.0"}
(fact "override for pad left"
  ^:hidden
  
  (!.lua
   (k/pad-left "000" 5 "-"))
  => "--000")

^{:refer xt.lang.override.custom-lua/pad-right :added "4.0"}
(fact "override for pad right"
  ^:hidden
  
  (!.lua
   (k/pad-right "000" 5 "-"))
  => "000--")
