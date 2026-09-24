(ns pune.layout-toplevel
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.ui-frame :as ui-frame]
             [melbourne.base-palette :as base-palette]]
   :export [MODULE]})

(defn.js LayoutMain
  "constructs the main layout"
  {:added "0.1"}
  [#{[design
      mini
      showAuth
      showGuest
      header
      headerProps
      consoleView
      consoleProps
      consoleShow
      body
      bodyProps
      menu
      menuProps
      (:.. rprops)]}]
  (var palette (base-palette/designPalette design))
  (var bodyView (r/% (or body n/View)
                                 (Object.assign #{design}
                                           bodyProps)))
  (var headerVisible (and showGuest
                          (or (not mini)
                              (not showAuth))))
  (var menuVisible (not showGuest))
  
  (var miniProps
       {:bottomSize 45
        :bottomComponent menu
        :bottomProps (Object.assign #{design mini} menuProps)
        :bottomVisible  menuVisible
        :bottomFade true
        :bottomStyle {:backgroundColor (base-palette/getColor
                                        palette
                                        {:key "background"
                                         :tone "sharpen"})}})
  (var normalProps
       {:leftComponent menu
        :leftProps (Object.assign #{design mini}
                            menuProps)
        :leftVisible  menuVisible
        :leftFade true
        :leftStyle {:backgroundColor (base-palette/getColor
                                      palette
                                      {:key "background"})}
        :bottomSize 400
        :bottomStyle {:backgroundColor (base-palette/getColor
                                        palette
                                        {:key "neutral"})}
        :bottomVisible (:? mini
                           menuVisible
                           consoleShow)
        :bottomComponent consoleView
        :bottomProps (Object.assign #{design}
                               consoleProps)})
  (var frameProps
       (:? mini
           (Object.assign miniProps rprops)
           (Object.assign normalProps rprops)))
  (return
   [:% ui-frame/Frame
    #{[:topComponent header
       :topProps (Object.assign #{design}
                           headerProps)
       :topStyle {:backgroundColor (base-palette/getColor
                                    palette
                                    {:key "primary"})}
       :topSize 60
       :topVisible   headerVisible
       (:.. frameProps)]}
    bodyView]))

(def.js MODULE (!:module))
