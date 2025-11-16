(ns js.react-native.model-geom
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[xt.lang.base-lib :as k]]})

(def.js POSITION
  {"top"    {:opposite "bottom"
             :sides ["left" "right"]}
   "bottom" {:opposite "top"
             :sides ["right" "left"]}
   "left"   {:opposite "right"
             :sides ["bottom" "top"]}
   "right"  {:opposite "left"
             :sides ["top" "bottom"]}})

(defn.js oppositePosition
  "gets the opposite position"
  {:added "4.0"}
  [position]
  (return (k/get-in -/POSITION [position "opposite"])))

(defn.js triangleBaseStyle
  "constructs a style for triangle"
  {:added "4.0"}
  [color point baseLength baseHeight]
  (var #{opposite
         sides} (k/get-key -/POSITION point))
  (var [s0 s1] sides)
  (var Point    (k/capitalize point))
  (var Opposite (k/capitalize opposite))
  (var S0 (k/capitalize s0))
  (var S1 (k/capitalize s1))
  (return
   {(+ "border" Point "Width") 0
    (+ "border" Opposite "Width") baseHeight 
    (+ "border" S0 "Width") (* baseLength 0.5)
    (+ "border" S1 "Width") (* baseLength 0.5)
    (+ "border" Point "Color") "transparent"
    (+ "border" Opposite "Color") color
    (+ "border" S0 "Color") "transparent"
    (+ "border" S1 "Color") "transparent"}))

