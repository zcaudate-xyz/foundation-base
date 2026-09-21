(ns component.web-native-index
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.string :as str]
            [net.http :as http]))

(l/script :js
  {;;:runtime :websocket
   :config {:bench true
            :id :dev/web-main
            :emit {:native {:suppress true}
                   :lang/jsx false}}
   :require [[js.react.ext-box :as ext-box]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.lib.rn-expo :as x :include [:lib]]
             [xt.lang.spec-base :as xt]
             [xt.event.base-box :as base-box]
             [component.web-native :as web-native]]
   
   :file   "App.js"})

(defrun.js __import__
  (:- :import (quote [React]) :from "'react'"))

(defglobal.js Global
  (base-box/make-box {:l0 "00a-native-text"}))

(defglobal.js Screens
  (base-box/make-box {}))

(defrun.js ^{:rt/init true}
  __screen__
  (base-box/set-data
   -/Screens
   []
   (web-native/raw-controls)))

(defn.js AppHeader
  []
  (return
   [:% n/View
    {:style {:height 72
            :paddingHorizontal 24
            :backgroundColor "#0b1120"
            :borderBottomWidth 1
            :borderBottomColor "#1e293b"
            :flexDirection "row"
            :alignItems "center"}}
    [:% n/View
     {:style {:width 250
             :flexDirection "row"
             :alignItems "center"}}
     [:% n/View
      {:style {:width 10
              :height 10
              :borderRadius 5
              :marginRight 10
              :backgroundColor "#60a5fa"}}]
     [:% n/Text
      {:style {:color "#f8fafc"
              :fontSize 16
              :fontWeight "800"
              :letterSpacing 0.8}}
      "FOUNDATION"]]
    [:% n/View
     {:style {:flex 1
             :paddingHorizontal 24}}
     [:% n/Text
      {:style {:color "#f8fafc"
              :fontSize 17
              :fontWeight "700"}}
      "React Native component lab"]
     [:% n/Text
      {:style {:color "#94a3b8"
              :fontSize 12
              :marginTop 3}}
      "Explore interactive building blocks for the web"]]
    [:% n/View
     {:style {:backgroundColor "#172554"
             :borderRadius 999
             :paddingHorizontal 12
             :paddingVertical 6}}
     [:% n/Text
      {:style {:color "#bfdbfe"
              :fontSize 10
              :fontWeight "800"
              :letterSpacing 0.7}}
      "LIVE"]]]))

(defn.js AppMain
  []
  (var [l0 setL0] (ext-box/useBox -/Global ["l0"]))
  (var tree (ext-box/listenBox -/Screens []))
  (return
   [:% n/View
    {:style {:position "absolute",
            :top 0,
            :bottom 0,
            :left 0,
            :right 0,
            :backgroundColor "#f8fafc"}}
    [:% -/AppHeader]
    [:% n/TreePane
     {:tree tree,
      :levels
      [{:type "list",
        :initial l0,
        :setInitial setL0,
        :listWidth 250,
        :styleList {:backgroundColor "#0f172a"
                     :paddingHorizontal 12
                     :paddingVertical 18
                     :borderRightWidth 1
                     :borderRightColor "#1e293b"}
        :styleListText {:color "#94a3b8"
                         :fontSize 11
                         :fontWeight "600"
                         :paddingVertical 9
                         :paddingHorizontal 12}
        :styleListSelected {:backgroundColor "#2563eb"
                            :color "#ffffff"
                            :fontWeight "800"
                            :borderRadius 8}
        :displayFn n/displayTarget}]}]]))

(defrun.js ^{:rt/init true}
  __main__
  (base-box/set-data -/Global ["Main"] -/AppMain))

(defn.js clearScratch
  []
  (base-box/del-data -/Global ["Scratch"]))

(defn.js App []
  (var #{Main} (ext-box/listenBox -/Global []))
  (return [:% Main]))

(def.js MODULE
  (x/registerRootComponent -/App))

(comment
  (l/rt:restart)
  
  (std.make/build-triggered)
  (!.js
   (+ 1 2 3))
  
  (!.js
   (alert "hello"))
  
  (!.js
   (console.log "blah"))

  (!.js
   (+ 1 2 3)))
