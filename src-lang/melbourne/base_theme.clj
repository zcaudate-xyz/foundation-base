(ns melbourne.base-theme
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]
             [melbourne.base-palette :as base-palette]]
   :export [MODULE]})

(defn.js themeBase
  "gets theme colors given palette"
  {:added "4.0"}
  [palette variant]
  (var #{fg bg} variant)
  (return [(base-palette/getColor palette fg)
           (base-palette/getColor palette bg)]))

(defn.js themeNormal
  "gets theme normal given palette"
  {:added "4.0"}
  [palette variant]
  (var #{fg bg} variant)
  (var [fgNormal
        bgNormal] (-/themeBase palette #{fg bg}))
  (return #{fgNormal
            bgNormal}))

(defn.js themeActive
  "gets theme normal given palette"
  {:added "4.0"}
  [palette #{fg bg active}]
  (var [fgActive
        bgActive] (-/themeBase palette
                               {:fg (or (and active (. active fg)) bg)
                                :bg (or (and active (. active bg)) fg)}))
  (return #{fgActive
            bgActive}))

(defn.js themeHovered
  "gets the theme hovered given palette"
  {:added "4.0"}
  [palette #{hovered}]
  (var #{isDark} palette)
  (var #{[(:= fg {:raw 1})
          (:= bg {:raw (:? isDark -0.7 0.7)})]} (or hovered {}))
  (var [fgHovered
        bgHovered] (-/themeBase palette #{fg bg}))
  (return #{fgHovered
            bgHovered}))

(defn.js themePressed
  "gets the theme pressed given palette"
  {:added "4.0"}
  [palette #{pressed} override]
  (var #{isDark} palette)
  (var #{[(:= fg {:raw 1})
          (:= bg {:raw (:? isDark 0.7 -0.7)})]} (or pressed {}))
  (var [fgPressed
        bgPressed] (-/themeBase palette #{fg bg}))
  (return #{fgPressed
            bgPressed}))

(defn.js themeHighlighted
  "gets the theme highlighted given palette"
  {:added "4.0"}
  [palette #{highlighted}]
  (var #{[(:= fg {:key "background"})
          (:= bg {:key "error"})]} (or highlighted {}))
  (var [fgHighlighted
        bgHighlighted] (-/themeBase palette #{fg bg}))
  (return #{fgHighlighted
            bgHighlighted}))

(defn.js themeDisabled
  "gets the theme disabled given palette"
  {:added "4.0"}
  [palette #{disabled}]
  (var #{[(:= fg {:key    "neutral"
                  :mix "background"
                  :ratio 5})
          (:= bg {:key    "background"
                  :mix "neutral"
                  :ratio 1})]} (or disabled {}))
  (var [fgDisabled
        bgDisabled] (-/themeBase palette  #{fg bg}))
  (return #{fgDisabled
            bgDisabled}))


(defn.js themeUiButton
  "gets theme for button"
  {:added "4.0"}
  [palette variant]
  (return
   (Object.assign
    (-/themeNormal palette variant)
    (-/themePressed palette variant)
    (-/themeHovered palette variant)
    (-/themeDisabled palette variant))))

(defn.js themeUiState
  "gets theme for single state items"
  {:added "4.0"}
  [palette variant]
  (return
   (Object.assign
    (-/themeNormal palette variant)
    (-/themePressed palette variant)
    (-/themeHovered palette variant)
    (-/themeDisabled palette variant)
    (-/themeActive palette  variant))))

(defn.js themeUiInput
  "gets theme for inputs"
  {:added "4.0"}
  [palette variant]
  (return
   (Object.assign
    (-/themeUiState palette variant)
    (-/themeHighlighted palette variant))))

(def.js MODULE (!:module))
