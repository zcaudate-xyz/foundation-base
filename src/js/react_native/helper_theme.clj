(ns js.react-native.helper-theme
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react-native.helper-color :as c]
             [xt.lang.base-lib :as k]]})

;;
;; Pairs
;;

(def.js ThemeLookup
  {:bg {:default     "bgNormal"
        :normal      "bgNormal"
        :pressing    "bgPressed"
        :hovering    "bgHovered"
        :active      "bgActive"
        :focusing    "bgActive"
        :disabled    "bgDisabled"
        :highlighted "bgHighlighted"}
   :fg {:default     "fgNormal"
        :normal      "fgNormal"
        :pressing    "fgPressed"
        :hovering    "fgHovered"
        :active      "fgActive"
        :focusing    "fgActive"
        :disabled    "fgDisabled"
        :highlighted "fgHighlighted"}})

(def.js StageMap
  {:pressing k/identity
   :focusing k/identity
   :active   k/identity
   :hovering (fn:> [hovering #{pressing}]
               (j/max hovering pressing))
   :disabled    k/identity
   :highlighted k/identity})

(def.js StageStatics
  {:pressing false
   :focusing false
   :hovering false
   :active false})


(defn.js transformColor
  "transforms a color given a set of indicators, the type and pipeline"
  {:added "4.0"}
  [indValues
   theme
   type
   initial
   stages
   stageMap]
  (:= initial (or initial "default"))
  (var __fns  (:? (k/nil? stageMap)
                  -/StageMap
                  (j/assign {} -/StageMap stageMap)))
  (var lu (k/get-key -/ThemeLookup type))
  (var colorInit (k/get-key theme (k/get-key lu initial)))
  (return (k/arr-foldl stages
                       (fn [colorOut stageKey]
                         (var tf   (k/get-key __fns stageKey))
                         (var val  (k/get-key indValues stageKey))
                         (var colorStage  (k/get-key theme (k/get-key lu stageKey)))
                         (return (c/interpolate
                                  [colorOut
                                   colorStage]
                                  (tf val indValues))))
                       colorInit)))

(defn.js mergeProps
  "merges the transformed props"
  {:added "4.0"}
  [arr]
  (var out {:style []})
  (k/for:array [e arr]
    (var #{[style
            (:.. rprops)]} (or e {}))
    (k/for:array [s (j/arrayify style)]
      (x:arr-push out.style s))
    (j/assign out rprops))
  (return out))

(defn.js createCombinedTransformations
  "creates a combined transformations function"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:= transformations {})]}]
  (var bgCustom (or (k/get-key transformations "bg")
                    (fn:>)))
  (var fgCustom (or (k/get-key transformations "fg")
                    (fn:>)))
  (when (not (and (k/fn? bgCustom)
                  (k/fn? fgCustom)))
    (k/err "Themed transformations require functions."))
  (var #{bg fg} themePipeline)
  (var bgInitial  (. bg ["initial"]))
  (var bgStages   (. bg ["stages"]))
  (var bgStageMap (. bg ["values"]))
  (var fgInitial  (. fg ["initial"]))
  (var fgStages   (. fg ["stages"]))
  (var fgStageMap (. fg ["values"]))
  (return (fn [indValues chord]
            (var bgProps (bgCustom indValues chord))
            (var fgProps (fgCustom indValues chord))
            (var bgColor (c/toHSL (-/transformColor indValues theme "bg" bgInitial bgStages bgStageMap)))
            (var fgColor (c/toHSL (-/transformColor indValues theme "fg" fgInitial fgStages fgStageMap)))
            (var styleProps {:style {:backgroundColor bgColor
                                     :color fgColor
                                     :borderColor (:? (. chord ["outlined"])
                                                      fgColor
                                                      bgColor)}})
            (return (-/mergeProps [bgProps
                                   fgProps
                                   styleProps])))))

(defn.js createSingleTransformations
  "creates the transformation for split `fg`/`bg` controls"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:= transformations {})]}
   type
   colorKeys]
  (var custom (or (k/get-key transformations type)
                  (fn:>)))
  (when (not (k/fn? custom))
    (k/err "Themed transformations require functions."))
  (var pipe     (k/get-key themePipeline type))
  (var initial  (. pipe ["initial"]))
  (var stages   (. pipe ["stages"]))
  (var stageMap (. pipe ["values"]))
  (return (fn [indValues chord]
            (var customProps (custom indValues chord))
            (var color (c/toHSL (-/transformColor indValues theme type initial stages stageMap)))
            (var style {})
            (k/for:array [key colorKeys]
              (k/set-key style key color))
            (return (-/mergeProps [customProps
                                   {:style style}])))))

(defn.js combinedStatic
  "creates a static style from transform function"
  {:added "4.0"}
  [#{[disabled
      highlighted
      (:.. rprops)]}
   more
   transformFn]
  (var indFn (fn:> [flag] (:? flag 1 0)))
  (var chord (j/assign {}
                       -/StageStatics
                       #{disabled
                         highlighted}
                       more))
  (var indValues (k/obj-map chord indFn))
  (return (. (transformFn indValues chord)
             ["style"])))

(defn.js prepThemeCombined
  "prepares the combined theme"
  {:added "4.0"}
  [props]
  (var transformFn (-/createCombinedTransformations props))
  (var styleStatic (-/combinedStatic props {} transformFn))
  (return [styleStatic transformFn]))

(defn.js prepThemeSingle
  "prepares the static style and dynamic transform function"
  {:added "4.0"}
  [props
   type
   colorKeys]
  (var transformFn (-/createSingleTransformations props type colorKeys))
  (var styleStatic (-/combinedStatic props {} transformFn))
  (return [styleStatic transformFn]))

