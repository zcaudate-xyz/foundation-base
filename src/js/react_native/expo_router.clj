(ns js.react-native.expo-router
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:import [["expo-router" :as [* ExpoRouter]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ExpoRouter"
                                   :tag "js"}]
  [Stack
   Stack.Screen
   Slot
   Tabs
   Tabs.Screen])
