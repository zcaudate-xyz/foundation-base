(ns js.tamagui.ui-input
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import [["tamagui" :as [* Tamagui]]]
   :require [[js.tamagui :as tm]
             [js.react :as r]
             [js.react.helper-data :as data]
             [js.core :as j]]
   :export [MODULE]})

(def.js defaultContextValues
  {:size "$true"
   :scaleIcon 1.2
   :color nil})

(def.js InputContext (tm/createStyledContext
                      -/defaultContextValues))

(def.js defaultInputGroupStyles
  {:size "$true"
   :fontFamily "$body"
   :borderWidth 1
   :outlineWidth 0
   :color "$color"
   :borderColor "$borderColor"
   :backgroundColor "$color2"
   :minWidth 0
   :hoverStyle {:borderColor "$borderColorHover"}
   :focusStyle {:outlineColor "$outlineColor"
                :outlineWidth 2
                :outlineStyle "solid"
                :borderColor "$borderColorFocus"}})

(def.js InputGroupFrame
  (tm/styled tm/XGroup
             {:justifyContent "space-between"
              :context -/InputContext
              :variants
              {:unstyled {:false -/defaultInputGroupStyles}
               :scaleIcon
               {":number" {}}
               :applyFocusStyle
               {":boolean"
                (fn [val input]
                  (if val
                    (return (or (. input props focusStyle)
                                (. -/defaultInputGroupStyles focusStyle)))))}
               :size {"...size"
                      (fn [val input]
                        {:borderRadius (. input tokens radius [val])})}}
              :defaultVariants
              {:unstyled process.env.TAMAGUI_HEADLESS === "1"}}))

(def.js FocusContext
  (tm/createStyledContext
   {:setFocused (fn [_val] (return {}))
    :focused false}))

(def.js InputGroupImpl
  (. -/InputGroupFrame
     (styleable
      (fn [props fRef]
        (var [focused setFocused] (r/useState false))
        (return
         (r/createElement
          (. -/FocusContext Provider)
          {:focused focused
           :setFocused setFocused}
          (r/createElement
           -/InputGroupFrame
           (j/assign {:applyFocusStyle focused
                      :ref fRef}
                     props))))))))

(def.js InputFrame
  (tm/styled tm/Input
             {:unstyled true
              :context -/InputContext}))

(def.js InputImpl
  (. -/InputFrame
     (styleable
      (fn [props fRef]
        (var #{setFocused} (. -/FocusContext (useStyledContext)))
        (var #{size} (. -/InputContext (useStyledContext)))
        (return
         (r/createElement
          tm/View
          {:flex 1}
          (r/createElement
           -/InputFrame
           (j/assign
            {:ref fRef
             :onFocus (fn [] (setFocused true))
             :onBlur (fn [] (setFocused false))
             :size size}
            props))))))))

(def.js InputSection
  (tm/styled (. tm/XGroup Item)
             {:justifyContent "center"
              :alignItems "center"
              :context -/InputContext}))

(def.js InputButton
  (tm/styled tm/Button
             {:context -/InputContext,
              :justifyContent "center",
              :alignItems "center",
              :variants
              {:size
               {"...size"
                (fn [val input]
                  (:= val (or val "$true"))
                  (return
                   {:paddingHorizontal 0,
                    :height val,
                    :borderRadius
                    (:? (=== (typeof val)
                             "number")
                        (* val 0.2)
                        (. input tokens radius [val]))}))}}}))

(def.js InputIconFrame
  (tm/styled tm/View
             {:context -/InputContext,
              :justifyContent "center",
              :alignItems "center",
              :variants
              {:size
               {"...size"
                (fn [val input]
                  (return
                   {:paddingHorizontal (. input tokens space [val])}))}}}))

(defn.js getIconSize
  "gets the icon size"
  {:added "4.0"}
  [size scale]
  (return
   (:? (=== (typeof size) "number")
       (* size 0.5)
       (* (tm/getFontSize size)
          scale))))

(def.js InputIcon
  (. -/InputIconFrame
     (styleable
      (fn [props fRef]
        (var ctx (. -/InputContext (useStyledContext)))
        (var theme (tm/useTheme))
        (var color (tm/getVariable
                    (or (. ctx color)
                        (. theme [(. ctx color)] (get "web")))))
        (var iconSize (-/getIconSize (. ctx size)
                                     (or (. ctx scaleIcon) 1)))
        (var getThemedIcon
             (tm/useGetThemedIcon {:size iconSize
                                   :color color}))
        (return
         (r/createElement -/InputIconFrame
                          (j/assignNew props {:ref fRef
                                              :children nil})
                          (getThemedIcon (. props children))))))))

(def.js InputContainerFrame
  {:context -/InputContext
   :flexDirection "column"
   :variants
   {:size
    {"...size"
     (fn [val input]
       (return
        {:gap (* 0.3 (. input space [val] val))}))}
    :color
    {"...color" (fn [] (return {}))}
    :gapScale
    {":number" {}}}
   :defaultVariants {:size "$4"}})

(def.js InputLabel
  {:context -/InputContext
   :flexDirection "column"
   :variants
   {:size
    {"...size"
     (fn [val input]
       (return
        {:gap (* 0.3 (. input space [val] val))}))}
    :color
    {"...color" (fn [] (return {}))}
    :gapScale
    {":number" {}}}
   :defaultVariants {:size "$4"}})



(comment
  ;; ...(isWeb ? { tabIndex: 0 } : { focusable: true }),
  
  )

(def.js MODULE (!:module))
