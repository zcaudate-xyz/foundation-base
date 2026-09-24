(ns lang-main.ui-components.web-index-main
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit   {:native {:suppress true}
                     :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react.ext-box :as ext-box]
             [js.react.ext-route :as ext-route]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.helper-browser :as helper-browser]
             [js.lib.rn-expo :as x :include [:lib]]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [xt.event.base-box :as base-box]
             [component.web-native :as web-native]
             [component.web-melbourne :as web-melbourne]
             [component.web-tama :as web-tama]
             [component.web-tama-slim :as web-tama-slim]
             [component.web-pune-frame :as web-pune-frame]]
   :export [MODULE]
   :file   "App.js"})

(defglobal.js Global
  (base-box/make-box
   {:init false
    :l0 "00-native"
    :l1 "000-intro"}))

(defglobal.js Screens
  (base-box/make-box {}))

(defrun.js ^{:rt/init true}
  __screen__
  (base-box/set-data
   -/Screens
   []
   (tab ["00-native"      (web-native/raw-controls)]
        ["01-melbourne"   (web-melbourne/melbourne-controls)]
        ["02-slim"        (web-melbourne/slim-controls)]
        ["03-pune-frame"  (web-pune-frame/pune-frame-controls)]
        ["04-tama"       (web-tama-slim/tama-controls)])))

(defn.js formatSection
  [value]
  (return (:? (== value "00-native") "Native"
              (== value "01-melbourne") "Melbourne"
              (== value "02-slim") "Slim"
              (== value "03-pune-frame") "Pune"
              (== value "04-tama") "Tama"
              :else value)))

(defn.js formatExample
  [value]
  (var parts (string/split value "-"))
  (return (:? (> (xt/x:len parts) 1)
              (string/join " " (data/arr-slice parts 1 nil))
              value)))

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
     {:style {:width 240
              :flexDirection "row"
              :alignItems "center"}}
     [:% n/View
      {:style {:width 10
               :height 10
               :marginRight 10
               :borderRadius 5
               :backgroundColor "#60a5fa"}}]
     [:% n/View
      {:style {:flexDirection "column"}}
      [:% n/Text
       {:style {:color "#f8fafc"
                :fontSize 15
                :fontWeight "800"
                :letterSpacing 1.1}}
       "FOUNDATION"]
      [:% n/Text
       {:style {:marginTop 2
                :color "#64748b"
                :fontSize 10
                :fontWeight "700"
                :letterSpacing 0.5}}
       "FOUNDATION-BASE"]]]
    [:% n/View
     {:style {:flex 1
              :paddingHorizontal 24}}
     [:% n/Text
      {:style {:color "#f8fafc"
               :fontSize 16
               :fontWeight "700"}}
      "Component lab"]
     [:% n/Text
      {:style {:marginTop 3
               :color "#94a3b8"
               :fontSize 12}}
      "Composable primitives for React Native Web"]]
    [:% n/View
     {:style {:paddingHorizontal 12
              :paddingVertical 6
              :borderRadius 999
              :backgroundColor "#172554"}}
     [:% n/Text
      {:style {:color "#bfdbfe"
               :fontSize 10
               :fontWeight "800"
               :letterSpacing 0.7}}
      "LIVE WEB DEMO"]]]))

(defn.js ShowcaseFrame
  [#{[target family title onNavigate]}]
  (var accent
       (:? (== family "Melbourne") "#10b981"
           (== family "Slim") "#8b5cf6"
           :else "#60a5fa"))
  (return
   [:% n/View
    {:style {:maxWidth 1120
             :width "100%"
             :alignSelf "center"
             :paddingBottom 28}}
    [:% n/View
     {:style {:backgroundColor "#ffffff"
              :borderWidth 1
              :borderColor "#e2e8f0"
              :borderRadius 22
              :shadowColor "#0f172a"
              :shadowOffset {:width 0 :height 10}
              :shadowOpacity 0.07
              :shadowRadius 22
              :elevation 3
              :overflow "hidden"}}
     [:% n/View
      {:style {:paddingHorizontal 22
               :paddingTop 18
               :paddingBottom 16
               :borderBottomWidth 1
               :borderBottomColor "#eef2f7"
               :flexDirection "row"
               :alignItems "center"}}
      [:% n/View
       {:style {:width 8
                :height 34
                :borderRadius 4
                :marginRight 12
                :backgroundColor accent}}]
      [:% n/View
       {:style {:flex 1}}
       [:% n/Text
        {:style {:color "#0f172a"
                 :fontSize 11
                 :fontWeight "800"
                 :letterSpacing 0.7
                 :textTransform "uppercase"}}
        family]
       [:% n/Text
        {:style {:marginTop 4
                 :color "#1e293b"
                 :fontSize 18
                 :fontWeight "800"}}
        title]]
      [:% n/View
       {:style {:paddingHorizontal 10
                :paddingVertical 6
                :borderRadius 999
                :backgroundColor "#f1f5f9"}}
       [:% n/Text
        {:style {:color "#64748b"
                 :fontSize 9
                 :fontWeight "800"
                 :letterSpacing 0.6}}
        "INTERACTIVE"]]]
     [:% n/View
      {:style {:padding 18
               :alignItems "center"
               :backgroundColor "#fbfdff"}}
      (r/% target {:onNavigate onNavigate})]]]))

(defn.js AppMain
  []
  (var route (ext-route/makeRoute
              (or (helper-browser/getHashRoute)
                  "00-native/000-intro")))
  (helper-browser/useHashRoute route)
  (var [l0 setL0] (ext-route/useRouteSegment route [] "00-native"))
  (var [l1 setL1] (ext-route/useRouteSegment route [l0] "000-intro"))
  (var tree (ext-box/listenBox -/Screens []))
  (var onNavigate (fn [target] (setL1 target)))
  (var displayFn
       (fn [Target _branch _parents _root]
         (return
          [:% n/View
           {:style {:flex 1
                    :padding 28
                    :overflow "auto"
                    :backgroundColor "#f8fafc"}}
           (:? (xt/x:nil? Target)
               [:% n/View]
               (== l1 "000-intro")
               [:% Target {:onNavigate onNavigate}]
               :else
               [:% -/ShowcaseFrame
                #{[:target Target
                   :family (-/formatSection l0)
                   :title (-/formatExample l1)
                   :onNavigate onNavigate]}])])))
  (return
   [:% n/View
    {:style {:position "absolute"
             :top 0
             :bottom 0
             :left 0
             :right 0
             :backgroundColor "#f8fafc"}}
    [:% -/AppHeader]
    [:% n/TreePane
     {:key (xt/x:cat l0 ":" l1)
      :tree tree
      :levels
      [{:type "tabs"
        :initial l0
        :setInitial setL0
        :tabsFormat -/formatSection
        :styleTabs {:paddingHorizontal 18
                    :paddingVertical 12
                    :backgroundColor "#0b1120"
                    :borderBottomWidth 1
                    :borderBottomColor "#1e293b"
                    :alignItems "center"}
        :styleTabsText {:color "#94a3b8"
                         :fontSize 12
                         :fontWeight "700"
                         :paddingHorizontal 13
                         :paddingVertical 8
                         :marginRight 6
                         :borderRadius 999
                         :textTransform "capitalize"}
        :styleTabsSelected {:backgroundColor "#2563eb"
                            :color "#ffffff"
                            :fontWeight "800"}}
       {:type "list"
        :initial l1
        :setInitial setL1
        :listWidth 220
        :listFormat -/formatExample
        :styleList {:backgroundColor "#0f172a"
                    :paddingHorizontal 12
                    :paddingVertical 16
                    :borderRightWidth 1
                    :borderRightColor "#1e293b"}
        :styleListText {:color "#94a3b8"
                         :fontSize 12
                         :fontWeight "600"
                         :width 196
                         :paddingHorizontal 12
                         :paddingVertical 10
                         :borderRadius 8
                         :textTransform "capitalize"}
        :styleListSelected {:backgroundColor "#1d4ed8"
                            :color "#ffffff"
                            :fontWeight "800"}
        :displayFn displayFn}]}]]))

(defn.js AppScratch
  []
  (return [:% n/View]))

(defrun.js ^{:rt/init true}
  __main__
  (base-box/set-data -/Global ["Main"] -/AppMain))

(defn.js App
  []
  (var #{Main} (ext-box/listenBox -/Global []))
  (return [:% Main]))

(def.js MODULE
  (x/registerRootComponent -/App))
