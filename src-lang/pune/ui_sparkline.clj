(ns pune.ui-sparkline
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n :include [:fn :svg]]
             [js.react :as r :include [:fn]]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [melbourne.base-palette :as base-palette]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js getPath
  [values width height maxValue minValue]
  (when (data/is-empty? values)
    (return ""))
  (var out [])
  (var maxX (- (xt/x:len values) 1))
  (var maxY (+ (or maxValue
                   (Math.max (:.. values)))
               2))
  (var minY (- (or minValue
                   (Math.min (:.. values)))
               2))
  (xt/for:array [[i v] values]
    (x:arr-push out (xt/x:cat (Math.round (/ (* width i)
                                       maxX))
                           ","
                           (- height
                              (* height
                                 (/ (- v minY)
                                    (- maxY minY)))))))
  (return (+ "M " (j/join out " L "))))

(defn.js Sparkline
  [#{design
     variant
     values
     width
     height
     style
     pathStyle
     maxValue
     minValue}]
  (var path (-/getPath values width height
                       maxValue
                       minValue))
  (var palette  (base-palette/designPalette design))
  (var __variant (Object.assign
                  {:fg {:key "primary"}}
                  variant))
  (return
   [:% n/Svg
    {:height height
     :width width
     :style (Object.assign
             {:backgroundColor (:? (. __variant bg)
                                   (base-palette/getColor
                                    palette
                                    (. __variant bg)))}
             style)}
    (r/% n/Path
         (Object.assign
          {:d path
           :fill "none"
           :stroke (base-palette/getColor
                    palette
                    (. __variant fg))
           :strokeWidth 1}
          pathStyle))]))

(def.js MODULE (!:module))
