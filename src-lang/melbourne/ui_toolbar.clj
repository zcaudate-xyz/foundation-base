(ns melbourne.ui-toolbar
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.lib.link :as link]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.ui-tooltip :as ui-tooltip]
             [js.react-native.ui-util :as ui-util]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-text :as ui-text]]
   :export [MODULE]})

(defn.js minorStandard
  "style for standard minor buttons"
  {:added "0.1"}
  []
  (return
   (ui-text/minorToggleTheme
    {:key "primary"
     #_#_:tone "flatten"}
    {:key "background"}
    {:key "background"
     :tone "standard"})))

(defn.js accentStandard
  "style for standard accent button"
  {:added "0.1"}
  []
  (return
   (ui-text/accentToggleTheme
    {:key "primary"}
    {:key "background"}
    {:key "background"}
    {:key "primary"})))

(defn.js accentToggleSubtle
  "style for accent subtle toggle"
  {:added "0.1"}
  [noChange]
  (return
   (ui-text/accentToggleTheme
    (Object.assign {:key "primary"}
              (:? noChange
                  {:mix "background"
                   :ratio 1}))
    (:? noChange
        {:key "neutral"}
        {:key "background"
         :tone "augment"})
    {:key "primary"
     :mix "background"
     :ratio 1}
    {:key "background"})))

(defn.js accentToggleDeep
  "style for accent deep toggle"
  {:added "0.1"}
  []
  (return
   (ui-text/accentToggleTheme
    {:key "primary"
     :tone "flatten"}
    {:key "background"
     :tone "augment"}
    {:key "background"
     :tone "augment"}
    {:key "primary"
     :tone "flatten"})))

(defn.js accentTabsSubtle
  "style for accent subtle tabs"
  {:added "0.1"}
  []
  (return
   (ui-text/accentToggleTheme
    {:key "primary"}
    {:key "background"
     :tone "augment"}
    {:key "primary"
     :mix "background"
     :ratio 1}
    {:key "background"})))

(defn.js minorNoBanner
  "style for no banner minor button"
  {:added "0.1"}
  []
  (return
   (ui-text/minorToggleTheme
    {:key "background"
     :tone "augment"}
    {:key "primary"
     :tone "standard"}
    {:key "primary"
     :tone "standard"})))

(defn.js accentNoBanner
  "style for no banner accent button"
  {:added "0.1"}
  []
  (return (ui-text/accentToggleTheme
           {:key "background"
            :tone "augment"}
           {:key "primary"
            :tone "standard"}
           {:key "primary"
            :tone "standard"}
           {:key "background"
            :tone "augment"})))

(defn.js ToolbarOverlayTooltip
  "creates the toolbar overlay ()"
  {:added "0.1"}
  [#{[design
      hostRef
      style
      visible
      setVisible
      children
      (:.. rprops)]}]
  (var palette (base-palette/designPalette design))
  
  (return
   [:% ui-tooltip/Tooltip
    #{[:hostRef hostRef
       :visible visible
       :setVisible setVisible
       :position "left_edge"
       :alignment "start"
       :arrow {:animate true
               :placement "none"
               :backdrop true
               :backdropStyle {:backgroundColor (base-palette/getColor
                                                 palette
                                                 {:key "neutral"})
                               :opacity 0.4}}]}
    [:% n/View
     {:style [style
              {:minWidth 360}]}
     children]]))

(defn.js ToolbarOverlay
  "creates the toolbar overlay"
  {:added "0.1"}
  [#{[design
      visible
      setVisible
      children
      (:.. rprops)]}]
  (var hostRef  (r/ref))
  (return
   [:% n/View
    {:ref hostRef}
    [:% -/ToolbarOverlayTooltip
     #{[design
        visible
        setVisible
        hostRef
        (:.. rprops)]}
     children]]))

(defn.js Toolbar
  "creates the toolbar overlay ()"
  {:added "0.1"}
  [#{[design
      variant
      hostRef
      style
      noBanner
      accent
      children]}]
  (return
   [:% ui-static/Div
    {:design design
     :variant (Object.assign
               {:bg (:? noBanner
                        {:key "background"
                         :tone "augment"}
                        {:key "primary"})}
               variant)
     :style [{:flexDirection "row"
              #_#_:alignItems "center"
              :flex 1}
             (:.. (data/arrayify style))]}
    children]))

(defn.js ToolbarAnnex
  "creates a toolbar annex"
  {:added "0.1"}
  [#{[(:= component n/View)
      mini
      design
      visible
      setVisible
      (:.. rprops)]}]
  (var onClose (fn:> (setVisible false)))
  (var cprops (Object.assign #{design
                          mini
                          onClose}
                        rprops))
  (var innerElem (r/% component cprops))
  (return
   (:? mini
       [:% ui-util/Fold
        {:visible visible}
        innerElem]
       [:% -/ToolbarOverlay
        #{visible setVisible design}
        innerElem])))

(def.js MODULE (!:module))
