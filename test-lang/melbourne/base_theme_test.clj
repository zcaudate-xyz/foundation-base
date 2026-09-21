(ns melbourne.base-theme-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-palette :as base-palette]]
   :export [MODULE]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer melbourne.base-theme/themeBase :added "4.0"}
(fact "gets theme colors given palette"
  ^:hidden
  
  (!.js
   (base-theme/themeBase
    (base-palette/createPalette "type" "purple")
    {:fg {:key "primary"}
     :bg {:key "neutral"}}))
  => ["#5f4abd" "#333"])

^{:refer melbourne.base-theme/themeNormal :added "4.0"}
(fact "gets theme normal given palette"
  ^:hidden
  
  (!.js
   (base-theme/themeNormal
    (base-palette/createPalette "type" "purple")
    {:fg {:key "primary"}
     :bg {:key "neutral"}}))
  => {"fgNormal" "#5f4abd", "bgNormal" "#333"})

^{:refer melbourne.base-theme/themeActive :added "4.0"}
(fact  "gets theme normal given palette"
  ^:hidden
  
  (!.js
   (base-theme/themeActive
    (base-palette/createPalette "type" "purple")
    {:fg {:key "primary"}
     :bg {:key "neutral"}}))
  => {"fgActive" "#333", "bgActive" "#5f4abd"})

^{:refer melbourne.base-theme/themeHovered :added "4.0"}
(fact "gets the theme hovered given palette"
  ^:hidden
  
  (!.js
   (base-theme/themeHovered
    (base-palette/createPalette "type" "purple")
    {:hovered {:fg {:key "primary"}}}))
  => {"bgHovered" 0.7, "fgHovered" "#5f4abd"})

^{:refer melbourne.base-theme/themePressed :added "4.0"}
(fact "gets the theme pressed given palette"
  ^:hidden
  
  (!.js
   (base-theme/themePressed
    (base-palette/createPalette "type" "purple")
    {:pressed {:fg {:key "primary"}}}))
  => {"bgPressed" -0.7, "fgPressed" "#5f4abd"})

^{:refer melbourne.base-theme/themeHighlighted :added "4.0"}
(fact "gets the theme highlighted given palette"
  ^:hidden
  
  (!.js
   (base-theme/themeHighlighted
    (base-palette/createPalette "type" "purple")
    {:highlighted {:fg {:key "primary"}}}))
  => {"bgHighlighted" "#9c1f1f",
      "fgHighlighted" "#5f4abd"})

^{:refer melbourne.base-theme/themeDisabled :added "4.0"}
(fact "gets the theme disabled given palette"
  ^:hidden
  
  (!.js
   (base-theme/themeDisabled
    (base-palette/createPalette "type" "purple")
    {:disabled {:fg {:key "primary"}}}))
  => {"fgDisabled" "#5f4abd",
      "bgDisabled" "hsl(0,0.00%,84.17%)"})


^{:refer melbourne.base-theme/themeUiButton :added "4.0"}
(fact "gets theme for button"
  ^:hidden
  
  (!.js
   (base-theme/themeUiButton
    (base-palette/createPalette "type" "purple")
    {:fg {:key "primary"}
     :bg {:key "neutral"}}))
  => {"fgDisabled" "hsl(0,0.00%,65.83%)",
      "bgDisabled" "hsl(0,0.00%,84.17%)",
      "bgHovered" 0.7,
      "bgPressed" -0.7,
      "fgNormal" "#5f4abd",
      "bgNormal" "#333",
      "fgPressed" 1,
      "fgHovered" 1})

^{:refer melbourne.base-theme/themeUiState :added "4.0"}
(fact "gets theme for single state items"
  ^:hidden
  
  (!.js
   (base-theme/themeUiState
    (base-palette/createPalette "type" "purple")
    {:fg {:key "primary"}
     :bg {:key "neutral"}}))
  => map?)

^{:refer melbourne.base-theme/themeUiInput :added "4.0"}
(fact "gets theme for inputs"
  ^:hidden
  
  (!.js
   (base-theme/themeUiInput
    (base-palette/createPalette "type" "purple")
    {:fg {:key "primary"}
     :bg {:key "neutral"}}))
  => map?)
