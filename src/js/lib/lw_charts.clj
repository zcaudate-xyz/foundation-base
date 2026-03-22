(ns js.lib.lw-charts
  (:require [std.lang :as l]
            [std.lib.foundation]))

(l/script :js
  {:import [["lightweight-charts" :as [* LWCharts]]]})

(def +lw-charts+
  '[ColorType
    CrosshairMode
    LasPriceAnimationMode
    LastPriceAnimationMode
    LineStyle
    LineType
    PriceLineSource
    PriceScaleMode
    TickMarkType
    TrackingModeExitMode
    createChart
    isBusinessDay
    isUTCTimestamp
    version])

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "LWCharts"
                                   :tag "js"
                                   :shrink true}]
  +lw-charts+)
