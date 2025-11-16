(ns js.react-native.helper-theme-default
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]]})

(def.js FontSize
  {:xxl  {:fontSize 40}
   :xl   {:fontSize 32}
   :lg   {:fontSize 24}
   :md   {:fontSize 18}
   :sm   {:fontSize 14}
   :xs   {:fontSize 12}
   :xxs  {:fontSize 10}})

(def.js defaultPressing
  (fn:> [pressing #{disabled}]
    (j/max pressing disabled)))

(def.js defaultHovering
  (fn:> [hovering #{focusing}]
    (j/max hovering focusing)))

(def.js PressDefaultPipeline
  {:fg {:initial "default"
        :stages  ["highlighted"
                  "hovering"
                  "pressing"
                  "disabled"]
        :values   {:pressing -/defaultPressing}}
   :bg {:initial "default"
        :stages ["highlighted"
                 "hovering"
                 "pressing"
                 "disabled"]
        :values   {:pressing -/defaultPressing}}})

(def.js BinaryDefaultPipeline
  {:fg {:initial "default"
        :stages ["active"
                 "highlighted"
                 "hovering"
                 "pressing"
                 "disabled"]
        :values   {:pressing -/defaultPressing}}
   :bg {:initial "default"
        :stages ["active"
                 "highlighted"
                 "hovering"
                 "pressing"
                 "disabled"]
        :values   {:pressing -/defaultPressing}}})

(def.js InputDefaultPipeline
  {:fg {:initial "default"
        :stages  [;;"hovering"
                  "highlighted"
                  "focusing"
                  "disabled"]
        :values    {:hovering -/defaultHovering}}
   :bg {:initial "default"
        :stages  [;;"hovering"
                  "highlighted"
                  "focusing"
                  "disabled"]
        :values    {:hovering -/defaultHovering}}})

;;
;; Input Type Widgets
;;

(def.js centerFn
  (fn [n]
    (return (fn [x]
              (if (< x 50)
                (return (+ x n))
                (return (- x n)))))))

(def.js BaseBg
  {:bgNormal        "#f4f4f4"
   :bgHovered       (-/centerFn 10)
   :bgPressed       "#333"
   :bgDisabled      "#ccc"
   :bgHighlighted   "yellow"
   :bgActive        "#555"})

(def.js BaseFg
  {:fgNormal        "#333"
   :fgHovered       (-/centerFn 20)
   :fgPressed       "#ccc"
   :fgDisabled      "#888"
   :fgHighlighted   "black"
   :fgActive        "white"})

(def.js BaseTheme
  (j/assign {}
            -/BaseBg
            -/BaseFg))

;;
;; Input
;;

(def.js InputDefaultFgTheme
  (j/assign {}
            -/BaseFg
            {:fgActive       "white"
             :bgActive       "#333"}))

(def.js InputDefaultBgTheme
  (j/assign {}
            -/BaseBg
            {:bgActive       "#333"}))

(def.js InputDefaultTheme
  (j/assign {}
            -/InputDefaultBgTheme
            -/InputDefaultFgTheme))

(def.js InputDefaultStyle
  {:padding 6
   :margin 2
   :borderRadius 2})

;;
;; Button
;;

(def.js ButtonDefaultTheme
  (j/assign {}
            -/BaseTheme))

(def.js ButtonDefaultStyle
  {:padding 6
   :fontWeight "500"
   :fontSize 16
   :paddingHorizontal 10})

;;
;; Button
;;

(def.js CheckBoxDefaultTheme
  (j/assign {} -/BaseTheme))

(def.js CheckBoxDefaultStyle
  {:padding 0
   :borderRadius 2
   :fontSize 16
   :borderStyle "solid" 
   :borderWidth 2})


(def.js RadioBoxDefaultTheme
  (j/assign {}
            -/BaseTheme))

(def.js ToggleSwitchDefaultTheme
  (j/assign {}
            -/BaseTheme))

