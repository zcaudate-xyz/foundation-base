(ns js.react-native.expo-router
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle {:default  [["expo-router" :as [* ExpoRouter]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoRouter"
                                   :tag "js"}]
  [Stack
   Stack.Screen
   Slot
   Tabs
   Tabs.Screen])
