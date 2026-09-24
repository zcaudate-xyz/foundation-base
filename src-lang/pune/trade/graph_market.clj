(ns pune.trade.graph-market
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit   {:native {:suppress true}
                     :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n]
             [js.react-native.helper-color :as c]
             [js.lib.lw-charts :as lw]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [melbourne.base-palette :as base-palette]]
   :export [MODULE]})

(defn.js GraphMarket
  [#{design
     dataPrice
     dataVolume
     height
     prediction
     decimal
     allotment
     fraction}]
  (var chartRef (r/ref))
  (var palette (base-palette/designPalette design))
  (r/watch [dataPrice
            dataVolume
            height
            design]
    (var chart
         (lw/createChart
          (r/curr chartRef)
          {:width (. (r/curr chartRef)
                     clientWidth)
           :height height}))
    (var handleResize
         (fn []
           (. chart (applyOptions
                     {:width (. (r/curr chartRef)
                                clientWidth)
                      :height (. (r/curr chartRef)
                                 clientHeight)}))))
    (. chart (timeScale) (fitContent))

    (var volumeSeries (. chart (addHistogramSeries
                                {:color (c/toRGB
                                         (base-palette/getColorRaw
                                          palette
                                          "primary"
                                          "mix"
                                          "background"
                                          7))
                                 :title "Volume"
                                 :priceScaleId "left"
                                 :autoscaleInfoProvider
                                 (fn:>
                                   {:priceRange {:minValue 0
                                                 :maxValue 10000}})})))
    (. volumeSeries (setData dataVolume))
    (. volumeSeries (applyOptions
                     {:priceFormat
                      {:precision 0
                       :minMove 1}}))
    (var priceSeries (. chart (addLineSeries
                               {:color (:? (== prediction "no")
                                           (c/toRGB
                                            (base-palette/getColorRaw
                                             palette
                                             "error"
                                             "flatten"))
                                           (c/toRGB
                                            (base-palette/getColorRaw
                                             palette
                                             "primary"
                                             "flatten")))
                                :title "Price"
                                :priceScaleId "right"
                                :autoscaleInfoProvider
                                (fn:>
                                  {:priceRange {:minValue (- (* 5 fraction))
                                                :maxValue (+ (* fraction allotment)
                                                             (* 5 fraction))}})})))
    (. priceSeries (setData dataPrice))
    (. priceSeries (applyOptions
                    {:priceFormat
                     {:type "price"
                      :precision decimal
                      :minMove fraction}}))
    
    (. chart (applyOptions
              {:layout {:background {:color (base-palette/getColorRaw
                                             palette
                                             "background"
                                             "sharpen")}
                        :textColor (base-palette/getColorRaw
                                    palette
                                    "neutral"
                                    "sharpen")
                        :fontSize 9}
               :grid {:horzLines {:visible false}
                      :vertLines {:style 4
                                  :color (base-palette/getColorRaw
                                          palette
                                          "background"
                                          "flatten")}}
               :leftPriceScale
               {:visible false
                :scaleMargins {:top 0.3
                               :bottom 0.3}}
               
               :rightPriceScale
               {:visible true
                :scaleMargins {:top 0.02
                               :bottom 0}}
               :timeScale {:visible true
                           :timeVisible true
                           :secondsVisible true
                           :lockVisibleTimeRangeOnResize true}}))
    (. window (addEventListener "resize" handleResize))
    (return (fn []
              (. window (removeEventListener
                         "resize" handleResize))
              (. chart (remove)))))
  (return #_[:% n/View
           {:style {:padding 10
                    :backgroundColor (base-palette/getColorRaw
                                      palette
                                      "background"
                                      "sharpen")}}]
          [:% n/View
           {:ref chartRef
            :style {:margin -5}}]))

(def.js MODULE (!:module))

(comment

  (j/toRadix 100 5)
  (!.js
   (var x 100)
   (. x (toString 16)))
  (+ 1 2 3))
