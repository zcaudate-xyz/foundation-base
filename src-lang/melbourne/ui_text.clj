(ns melbourne.ui-text
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [js.react-native.ui-tooltip :as ui-tooltip]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-font :as base-font]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-toggle-button :as ui-toggle-button]
             [melbourne.ui-group :as ui-group]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js Row
  "creates a centered row"
  {:added "0.1"}
  [#{[style
      children
      (:.. rprops)]}]
  (return
   [:% n/View
    #{[:style [{:flexDirection "row"
                :alignItems "center"
                :padding 3
                :height 30}
               (:.. (data/arrayify style))]
       (:.. rprops)]}
    children]))

(defn.js createHeaderFn
  "seed function for the headers"
  {:added "0.1"}
  [font]
  (return
   (fn [#{[design
           variant
           (:.. rprops)]}]
     (return
      [:% ui-static/Text
       #{[:design design
          :variant (Object.assign
                    {:font  font
                     :fg {:key "primary"
                          :tone "flatten"}}
                    variant)
          (:.. rprops)]}]))))

(defn.js createTextFn
  "seed function for the text"
  {:added "0.1"}
  [font fontStyle fontOverride]
  (return
   (fn [#{[design
           variant
           style
           (:.. rprops)]}]
     (return
      [:% ui-static/Text
       #{[:design design
          :variant (Object.assign
                    {:font font
                     :fg {:key "neutral"
                          :tone "flatten"}}
                    fontOverride
                    variant)
          :style [base-font/fontFamily
                  fontStyle
                  (:.. (data/arrayify style))]
          (:.. rprops)]}]))))

(def.js ^{:arglists '([#{[design
                          variant
                          (:.. rprops)]}])}
  H1 (-/createHeaderFn "h1"))

(def.js ^{:arglists '([#{[design
                         variant
                         (:.. rprops)]}])}
  H2 (-/createHeaderFn "h2"))

(def.js ^{:arglists '([#{[design
                         variant
                         (:.. rprops)]}])}
  H3 (-/createHeaderFn "h3"))

(def.js ^{:arglists '([#{[design
                         variant
                         (:.. rprops)]}])}
  H4 (-/createHeaderFn "h4"))

(def.js ^{:arglists '([#{[design
                         variant
                         (:.. rprops)]}])}
  H5 (-/createHeaderFn "h5"))

(def.js ^{:arglists '([#{[design
                         variant
                         (:.. rprops)]}])}
  H6 (-/createHeaderFn "h6"))

(def.js ^{:arglists '([#{[design
                          variant
                         (:.. rprops)]}])}
  Bold (-/createTextFn "bold"))

(def.js ^{:arglists '([#{[design
                          variant
                         (:.. rprops)]}])}
  P (-/createTextFn "p"))

(def.js ^{:arglists '([#{[design
                          variant
                         (:.. rprops)]}])}
  Caption (-/createTextFn "caption"
                          (Object.assign
                           {:opacity 0.8
                            :fontSize 10
                            :fontWeight "500"
                            :padding 15}
                           (n/PlatformSelect {:ios {:fontFamily "Courier"}
                                              :default {:fontFamily "monospace"}}))
                          {:bg {:key "background"
                                :mix "primary"
                                :ratio 4}}))

(defn.js ActivityIndicator
  "creates the icon"
  {:added "0.1"}
  [#{[refLink
      name
      size
      style
      design
      variant
      (:.. rprops)]}]
  (var palette  (base-palette/designPalette design))
  (var #{fg} (Object.assign
                 {:fg {:key "primary"}}
                 variant))
  (return
   [:% n/ActivityIndicator
    #{[:ref refLink
       :style style
       :size size
       :color (base-palette/getColor palette fg)
       (:.. rprops)]}]))

(defn.js Icon
  "creates the icon"
  {:added "0.1"}
  [#{[refLink
      name
      size
      style
      design
      variant
      (:.. rprops)]}]
  (var palette  (base-palette/designPalette design))
  (var #{fg bg} (Object.assign
                 {:fg {:key "neutral"}}
                 variant))
  (return
   [:% n/Icon
    #{[:ref refLink
       :style [{:backgroundColor (:? bg (base-palette/getColor palette bg))
                :color (base-palette/getColor palette fg)}
               (:.. (data/arrayify style))]
       :size size
       :name name
       (:.. rprops)]}]))

(defn.js Avatar
  "creates the avatar"
  {:added "0.1"}
  [props]
  (var #{[design
          variant
          image
          color
          text
          style
          styleImage
          styleText
          (:= size 40)
          (:= imageProps {})
          (:= textProps {})]} props)
  
  (when (lib/is-string? image)
    (:= image (xt/x:json-decode image)))
  
  (var imageElem (:? (data/not-empty? image)
                     [:% n/Image
                      #{[:style [{:height size
                                  :width size
                                  :borderRadius (/ size 2)}
                                 styleImage]
                         :source (. image url)
                         (:.. imageProps)]}]
                     
                     [:% ui-static/Text
                      #{[:design design
                         :variant (Object.assign {:fg {:key "background"}}
                                            (data/get-in design ["variant" "text"]))
                         :style [(n/PlatformSelect {:web {:cursor "default" :userSelect "none"}})
                                 {:fontWeight 500}
                                 (:.. (data/arrayify styleText))]
                         (:.. textProps)]}
                      (j/toUpperCase (or text ""))]))
  (var styleInput [{:height size
                    :width size
                    :borderRadius (/ size 2)
                    :justifyContent "center"
                    :alignItems "center"}
                   (:.. (data/arrayify style))])
  (cond color
        (return [:% n/View
                 {:style styleInput}
                 imageElem])

        :else
        (return
         [:% ui-static/Div
          {:design design
           :variant (Object.assign
                     (:? (data/not-empty? image)
                         {}
                         {:bg {:key "neutral" :mix "primary" :ratio 2}})
                     variant)
           :style styleInput}
          imageElem])))

(defn.js activeButtonTheme
  "creates the active button theme"
  {:added "0.1"}
  [bg fg bgPressed fgPressed]
  (return
   {:fg fg
    :bg bg
    :disabled {:bg bg
               :fg {:key (. bg key)
                    :mix (. fg key)
                    :ratio 2}}
    :hovered {:bg {:raw 1}
              :fg {:raw 1}}
    :pressed {:bg (or bgPressed fg)
              :fg (or fgPressed bg)}}))

(defn.js minorButtonTheme
  "creates the minor button theme"
  {:added "0.1"}
  [bg fg]
  (return
   {:fg fg
    :bg bg
    :disabled {:bg bg
               :fg {:key (. bg key)
                    :mix (. fg key)
                    :ratio 3}}
    :hovered {:bg {:raw 1}
              :fg {:raw 1}}
    :pressed {:bg {:raw 1}
              
              :fg {:key (. fg key)
                   :mix (. bg key)
                   :ratio 2}}}))

(defn.js minorToggleTheme
  "creates the minor toggle theme"
  {:added "0.1"}
  [bg fg fgActive]
  (return
   {:fg fg
    :bg bg
    :disabled {:bg bg
               :fg {:key (. bg key)
                    :mix (. fg key)
                    :ratio 3}}
    :hovered {:bg {:raw 1}
              :fg {:raw 1}}
    :pressed {:bg {:raw 1}
              :fg {:raw 1}}
    :active  {:fg (or fgActive fg)
              :bg bg
              :outlined true}}))

(defn.js createMinorFn
  "seed function for the minor components"
  {:added "0.1"}
  [component variantFn themeSelected styleOverride propsOverride]
  (return
   (fn [#{[design
           variant
           style
           selected
           text
           icon
           (:.. rprops)]}]
     (:= text (:? icon
                  (r/% n/Icon (Object.assign {:key "icon"} icon))
                  text))
     (var mprops (Object.assign
                  {:design design
                   :variant (Object.assign
                             {:font "text"}
                             (variantFn)
                             variant)
                   :text  text
                   :style [{:padding 5
                            :marginTop 2
                            :marginHorizontal 5
                            :fontSize 12
                            :borderRadius 2
                            :borderStyle "solid" 
                            :borderWidth 1}
                           styleOverride
                           (:.. (data/arrayify style))]
                   :transformations {:bg nil}
                   :outlined (or themeSelected selected)
                   :selected selected}
                  propsOverride))
     (return
      (r/% component
           (Object.assign mprops rprops))))))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  ButtonMinor (-/createMinorFn ui-button/Button
                               (fn:>
                                 (-/minorButtonTheme {:key "background"}
                                                     {:key "primary"}))
                               true))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  ToggleMinor (-/createMinorFn ui-toggle-button/ToggleButton
                               (fn:>
                                 (-/minorToggleTheme {:key "background"}
                                                     {:key "primary"}
                                                     {:key "primary"}))))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  ToggleTabMinor (-/createMinorFn ui-toggle-button/ToggleButton
                                  (fn:>
                                    (-/minorToggleTheme {:key "background"}
                                                        {:key "neutral"}
                                                        {:key "primary"}))))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  TabsMinor (-/createMinorFn ui-group/Tabs
                             (fn:>
                               (-/minorToggleTheme {:key "background"}
                                                   {:key "neutral"}
                                                   {:key "primary"}))
                             true
                             {}
                             {:styleContainer {:margin 0
                                               :marginHorizontal 2}}))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  EnumMinor (-/createMinorFn ui-group/EnumMulti
                             (fn:>
                               (-/minorToggleTheme {:key "background"}
                                                   {:key "neutral"}
                                                   {:key "primary"}))
                             true
                             {}
                             {:styleContainer {:margin 0
                                               :marginHorizontal 2}}))


;;
;;
;;

(defn.js accentButtonTheme
  "creates the accent button theme"
  {:added "0.1"}
  [bg fg]
  (return
   {:fg fg
    :bg bg
    :hovered {:bg {:raw 1}
              :fg {:raw 1}}
    :pressed  {:bg {:key (. fg key)
                    :mix (. bg key)
                    :ratio 4}
               :fg {:key (. bg key)
                    :mix (. fg key)
                    :ratio 4}}
    :disabled {:bg {:key (. fg key)
                    :mix (. bg key)
                    :ratio 4}
               :fg {:key (. bg key)
                    :mix (. fg key)
                    :ratio 7}}}))

(defn.js accentToggleTheme
  "creates the accent toggle theme"
  {:added "0.1"}
  [bg fg bgActive fgActive]
  (return
   {:fg fg
    :bg bg
    :hovered  {:bg {:raw 1}
               :fg {:raw 1}}
    :pressed  {:bg {:raw 1}
               :fg {:raw 1}}
    :disabled {:bg bg
               :fg {:key (. bg key)
                    :mix (. fg key)
                    :ratio 2}}
    :active  {:fg (or fgActive bg)
              :bg (or bgActive fg)}}))

(defn.js createAccentFn
  "seed function for the accent components"
  {:added "0.1"}
  [component variantFn themeSelected styleOverride propsOverride]
  (return
   (fn [#{[design
           variant
           style
           selected
           text
           icon
           (:.. rprops)]}]
     (:= text (:? icon
                  (r/% n/Icon (Object.assign {:key "icon"} icon))
                  text))
     (var mprops {:design design
                  :variant (Object.assign
                            {:font "text"}
                            (variantFn)
                            variant)
                  :text  text
                  :style [{:padding 5
                           :marginTop 2
                           :marginHorizontal 5
                           :fontSize 12
                           :borderRadius 2
                           :borderStyle "solid" 
                           :borderWidth 1}
                          styleOverride
                          (:.. (data/arrayify style))]
                  :transformations {:bg nil}
                  :outlined (or themeSelected selected)
                  :selected selected})
     (return
      (r/% component
           (Object.assign mprops propsOverride rprops))))))



(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  ButtonAccent (-/createAccentFn ui-button/Button
                                 (fn:>
                                   (-/accentButtonTheme
                                    {:key "primary"}
                                    {:key "background"
                                     :tone "augment"}))))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  ToggleAccent (-/createAccentFn ui-toggle-button/ToggleButton
                                 (fn:>
                                   (-/accentToggleTheme
                                    {:key "primary"}
                                    {:key "background"}
                                    {:key "background"}
                                    {:key "primary"}))))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  ToggleTabAccent (-/createAccentFn ui-toggle-button/ToggleButton
                                    (fn:>
                                      (-/accentToggleTheme
                                       {:key "background"}
                                       {:key "primary"}
                                       {:key "primary"}
                                       {:key "background"}))))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  TabsAccent (-/createAccentFn ui-group/Tabs
                               (fn:>
                                 (-/accentToggleTheme
                                  {:key "background"}
                                  {:key "primary"}
                                  {:key "primary"}
                                  {:key "background"}))
                               nil
                               {}
                               {:styleContainer {:margin 0
                                                 :marginHorizontal 2}}))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  TabsAccentNeutral (-/createAccentFn ui-group/Tabs
                                      (fn:>
                                        (-/accentToggleTheme
                                         {:key "background"}
                                         {:key "neutral"}
                                         {:key "neutral"}
                                         {:key "background"}))
                                      nil
                                      {}
                                      {:styleContainer {:margin 0
                                                        :marginHorizontal 2}}))

(def.js ^{:arglists '([#{[design
                          variant
                          style
                          (:.. rprops)]}])}
  EnumAccent (-/createAccentFn ui-group/EnumMulti
                               (fn:>
                                 (-/accentToggleTheme
                                  {:key "background"}
                                  {:key "primary"}
                                  {:key "primary"}
                                  {:key "background"}))
                               nil
                               {}
                               {:styleContainer {:margin 0
                                                 :marginHorizontal 2}}))



;;
;;

(defn.js ButtonTooltipOverlay
  "makes the confirm tooltip overlay"
  {:added "0.1"}
  [props]
  (var #{[hostRef
          visible
          setVisible
          onPress
          design
          variant
          (:= tooltip {})
          mainComponent]} props)
  (var palette  (base-palette/designPalette design))
  (var #{[(:= arrow {})
          (:.. rtooltip)]} tooltip)
  (var [fgColor
        bgColor] (base-theme/themeBase
                  palette
                  (Object.assign {:fg {:key "neutral"}
                             :bg {:key "primary"
                                  #_#_:tone "flatten"}}
                            (. arrow variant))))
  (return
   (r/% ui-tooltip/Tooltip
        (Object.assign
         #{visible setVisible
           {:hostRef hostRef
            :position "bottom"
            :alignment "center"
            :arrow  (Object.assign {:placement "host"
                               :baseHeight 5
                               :baselength 30
                               :color bgColor
                               :backdrop true}
                              arrow)}}
         rtooltip)
        (r/% mainComponent props))))

(defn.js ButtonTooltip
  "creates a button with tooltip confirmation"
  {:added "4.0"}
  [#{[(:= component "minor")
      design
      variant
      text
      icon
      tooltip
      onPress
      onChange
      style
      (:.. rprops)]}]
  (var [visible setVisible] (r/local (fn:> false)))
  (var hostRef (r/ref))
  (var ButtonComponent (or (. {:minor  -/ButtonMinor
                               :accent -/ButtonAccent}
                              [component])
                           component))
  (r/watch [visible]
    (when onChange (onChange visible)))
  (return
   [:<>
    (r/% ButtonComponent
         #{[:refLink hostRef
            design variant icon text style
            :selected visible
            :onPress (fn:> (setVisible (not visible)))]})
    (r/% -/ButtonTooltipOverlay
         (Object.assign
          #{hostRef
            {:design design
             :variant (Object.assign
                       {}
                       (data/get-in design ["variant" "tooltip"])
                       (data/get-in tooltip ["overlay" "variant"]))}
            visible
            setVisible
            onPress
            tooltip}
          rprops))]))

;;
;;
;;
;;
;;

(defn.js ConfirmTooltip
  "creates a button with tooltip confirmation"
  {:added "4.0"}
  [props]
  (var #{[confirmText
          (:.. rprops)]} props)
  (var mainComponent
       (r/const (fn [props]
                  (var #{design
                         variant
                         setVisible
                         onPress} props)
                  (return
                   [:% -/ButtonAccent
                    #{[design variant
                       :style {:marginHorizontal 2
                               :marginVertical 0}
                       :text (or confirmText "CONFIRM")
                       :onPress (fn []
                                  (. (jc/future (return (onPress)))
                                     (then (fn:> (setVisible false)))))]}]))))
  (return
   (r/% -/ButtonTooltip
        (Object.assign #{mainComponent}
                  props))))

(defn.js TextAltImpl
  "creates a visual viewer"
  {:added "0.1"}
  [#{[design
      (:= init false)
      disabled
      value
      children]}]
  (var [showText setShowText] (r/local init))
  (return
   [:<>
    (:? (not disabled)
        (r/% n/View
             {:style {:position "absolute"
                      :right 2
                      :top 2
                      :zIndex 100}}
             [:% -/ButtonAccent
              #{design
                {:text (:? showText "D" "T")
                 :onPress (fn:> (setShowText (not showText)))}}]))
    (:? (and (not disabled) showText)
        (r/% ui-static/ScrollView
             #{design}
             [:% ui-static/Text
              #{design}
              (n/format-entry value)])
        (r/% n/View
             {:style {:flex 1}}
             children))]))

(defn.js TextAlt
  "creates the text alt demo"
  {:added "0.1"}
  [#{[design
      init
      disabled
      value
      children]}]
  (var #{Consumer} n/Global)
  (return [:% Consumer
           (fn [#{isDev}]
             (return
              [:% -/TextAltImpl
               #{[design
                  init
                  :disabled (or disabled
                                (not isDev))
                  value
                  children]}]))]))

(def.js MODULE (!:module))  
