(ns lang-main.ui-components.sections.web-pune-frame
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n :include [:fn]]
             [pune.ui-breadcrumb-test :as ui-breadcrumb-test]
             [pune.ui-console-test :as ui-console-test]
             [pune.ui-depthchart-test :as ui-depthchart-test]
             [pune.ui-menu-vert-test :as ui-menu-vert-test]
             [pune.ui-sidemenu-test :as ui-sidemenu-test]
             [pune.ui-searchbar-test :as ui-searchbar-test]
             [pune.ui-submenu-test :as ui-submenu-test]
             [pune.ui-sidebar-test :as ui-sidebar-test]
             [pune.ui-notify-base-test :as ui-topnotify-test]
             #_[pune.ui-notify-alerts-test :as ui-notify-alerts-test]
             [pune.ui-page-test :as ui-page-test]
             [pune.ui-sparkline-test :as ui-sparkline-test]
             
             [pune.ui-market-delta-test :as ui-market-delta-test]
             [pune.ui-market-ladder-test :as ui-market-ladder-old-test]
             [pune.ui-market-live-test :as ui-market-live-test]
             #_#_
             [pune.ui-metamask-contract-test :as ui-metamask-contract-test]
             [pune.ui-metamask-user-test :as ui-metamask-user-test]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [lang-main.ui-components.sections.web-pune-frame-demo :as web-pune-frame-demo]
             [lang-main.ui-components.sections.web-pune-frame-graph :as web-pune-frame-graph]]
   :export [MODULE]})

(defrun.js __import__
  (:- :import (quote [React]) :from "'react'"))

(defn.js BreadcrumbExamples
  []
  (return
   [:<>
    [:% ui-breadcrumb-test/BreadcrumbDemo]]))

(defn.js ConsoleExamples
  []
  (return
   [:<>
    [:% ui-console-test/ConsoleDemo]]))

(defn.js DepthchartExamples
  []
  (return
   [:<>
    [:% ui-depthchart-test/MarketDepthChartDemo]]))


(defn.js MainMenuExamples
  []
  (return
   [:<>
    [:% ui-menu-vert-test/MainMenuSeperatorDemo]
    [:% ui-menu-vert-test/MainMenuButtonDemo]
    [:% ui-menu-vert-test/MainMenuToggleDemo]
    [:% ui-menu-vert-test/MainMenuRouteDemo]
    [:% ui-menu-vert-test/MainMenuMiniContextDemo]
    [:% ui-menu-vert-test/MainMenuDemo]]))

(defn.js SubMenuExamples
  []
  (return
   [:<>
    [:% ui-submenu-test/SubMenuToggleDemo]
    [:% ui-submenu-test/SubMenuRouteDemo]
    [:% ui-submenu-test/SubMenuDemo]]))

(defn.js SideBarExamples
  []
  (return
   [:<>
    #_[:% ui-metamask-user-test/MetamaskUserDemo]]))

(defn.js SideMenuExamples
  []
  (return
   [:<>
    [:% ui-sidemenu-test/SideMenuTitleDemo]
    [:% ui-sidemenu-test/SideMenuListDemo]
    [:% ui-sidemenu-test/SideMenuFloatingDemo]]))

(defn.js SearchBarExamples
  []
  (return
   [:<> [:% ui-searchbar-test/SearchBarDemo]]))

(defn.js NotifyAlertsExamples
  []
  (return
   [:<>
    #_[:% ui-notify-alerts-test/NotifyAlertsDemo]]))

(defn.js TopNotifyExamples
  []
  (return
   [:<>
    [:% ui-topnotify-test/TopNotifyInnerDemo]
    [:% ui-topnotify-test/TopNotifyDemo]]))


(defn.js TopNotifyExamples
  []
  (return
   [:<>
    [:% ui-topnotify-test/TopNotifyInnerDemo]
    [:% ui-topnotify-test/TopNotifyDemo]]))


(defn.js PageExamples
  []
  (return
   [:<>
    [:% ui-page-test/PageLayoutHeaderDemo]
    [:% ui-page-test/PageLayoutMenuDemo]
    [:% ui-page-test/PageLayoutDemo]]))

(defn.js SparklineExamples
  []
  (return
   [:<>
    [:% ui-sparkline-test/SparklineDemo]]))

(defn.js DeltaExamples
  []
  (return
   [:<>
    [:% ui-market-delta-test/DeltaDemo]]))

(defn.js LadderExamples
  []
  (return
   [:<>
    [:% ui-market-ladder-old-test/MarketLadderTextDemo]
    [:% ui-market-ladder-old-test/MarketLadderRowDemo]
    [:% ui-market-ladder-old-test/MarketLadderDemo]]))

(defn.js LiveExamples
  []
  (return
   [:<>
    [:% ui-market-live-test/MarketLiveRowDemo]
    [:% ui-market-live-test/MarketLiveDemo]]))



(defn.js MetamaskContractExamples
  []
  (return
   [:<>
    #_[:% ui-metamask-contract-test/MetamaskContractDemo]]))

(defn.js MetamaskUserExamples
  []
  (return
   [:<>
    #_[:% ui-metamask-user-test/MetamaskUserDemo]]))

(defn.js FrameDemo
  []
  (return
   [:% n/View
    {:style {:height 700
             :maxWidth 650}}
    [:% web-pune-frame-demo/FrameMain]]))

(defn.js ChartDemo
  []
  (return
   [:% n/View
    {:style {:height 700
             :maxWidth 650}}
    [:% web-pune-frame-graph/Demo002LightweightCharts]]))

(defn.js pune-frame-controls
  []
  (return
   (tab ["101-sidemenu"     -/SideMenuExamples]
        ["102-mainmenu"     -/MainMenuExamples]
        ["103-submenu"       -/SubMenuExamples]
        ["104-topnotify"     -/TopNotifyExamples]
        ["104a-notifyalert"  -/NotifyAlertsExamples]
        ["105-console"       -/ConsoleExamples]
        ["106-breadcrumb"    -/BreadcrumbExamples]
        ["108-sidebar"       -/SideBarExamples]
        ["109-searchbar"     -/SearchBarExamples]
        ["201-page"          -/PageExamples]
        ["600-sparkline"     -/SparklineExamples]
        ["601-depthchart"    -/DepthchartExamples]
        ["602-ladder"        -/LadderExamples]
        ["603-live"          -/LiveExamples]
        ["605-delta"         -/DeltaExamples]
        ["701-mm-contract"   -/MetamaskContractExamples]
        ["702-mm-user"       -/MetamaskUserExamples]
        ["900-frame"         -/FrameDemo]
        ["901-chart"         -/ChartDemo])))

(def.js MODULE (!:module))





(comment)
