(ns js.expo-router
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:macro-only true
   :bundle {:default  [["expo-router" :as ExpoRouter]]}})
