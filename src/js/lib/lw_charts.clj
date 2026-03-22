(ns js.lib.lw-charts
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

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

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "LWCharts"
                                   :tag "js"
                                   :shrink true}]
  +lw-charts+)
