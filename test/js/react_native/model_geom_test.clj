(ns js.react-native.model-geom-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [js.react-native.model-geom :as geom]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.react-native.model-geom/oppositePosition :added "4.0" :unchecked true}
(fact "gets the opposite position"
  ^:hidden
  
  (geom/oppositePosition "top")
  => "bottom")

^{:refer js.react-native.model-geom/triangleBaseStyle :added "4.0" :unchecked true}
(fact "constructs a style for triangle"
  ^:hidden
  
  (geom/triangleBaseStyle "red" "top" 20 10)
  => {"borderLeftWidth" 10,
   "borderBottomColor" "red",
   "borderBottomWidth" 10,
   "borderRightWidth" 10,
   "borderLeftColor" "transparent",
   "borderTopWidth" 0,
   "borderRightColor" "transparent",
   "borderTopColor" "transparent"}
  

  (geom/triangleBaseStyle "red" "left" 20 10)
  => {"borderLeftWidth" 0,
      "borderBottomColor" "transparent",
      "borderBottomWidth" 10,
      "borderRightWidth" 10,
      "borderLeftColor" "transparent",
      "borderTopWidth" 10,
      "borderRightColor" "red",
      "borderTopColor" "transparent"})
