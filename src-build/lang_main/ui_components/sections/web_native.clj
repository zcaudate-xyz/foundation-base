(ns lang-main.ui-components.sections.web-native
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react-native :as n :include [:fn]]
             [js.react-native-test :as react-native-test]
             [js.react-native.react-test :as react-test]
             [js.react-native.react-lazy-test :as react-lazy-test]
             [js.react-native.animate-test :as animate-test]
             [js.react-native.ext-box-test :as ext-box-test]
             [js.react-native.ext-log-test :as ext-log-test]
             [js.react-native.ext-model-test :as ext-model-test]
             [js.react-native.ext-form-test :as ext-form-test]
             [js.react-native.ext-route-test :as ext-route-test]
             [js.react-native.ui-frame-test :as ui-frame-test]
             [js.react-native.helper-browser-test :as helper-browser-test]
             [js.react-native.model-roller-impl-test :as model-roller-impl-test]
             [js.react-native.physical-base-test :as physical-base-test]
             [js.react-native.physical-edit-test :as physical-edit-test]
             [js.react-native.physical-dnd-test :as physical-dnd-test]
             [js.react-native.physical-layout-test :as physical-layout-test]
             [js.react-native.physical-play-test :as physical-play-test]
             [js.react-native.physical-carosel-test :as physical-carosel-test]
             [js.react-native.physical-modal-test :as physical-modal-test]
             [js.react-native.ui-autocomplete-test :as ui-autocomplete-test]
             [js.react-native.ui-button-test :as ui-button-test]
             [js.react-native.ui-input-test :as ui-input-test]
             [js.react-native.ui-picker-test :as ui-picker-test]
             [js.react-native.ui-slider-test :as ui-slider-test]
             [js.react-native.ui-spinner-test :as ui-spinner-test]
             [js.react-native.ui-swiper-test :as ui-swiper-test]
             [js.react-native.ui-range-test :as ui-range-test]
             [js.react-native.ui-notify-test :as ui-notify-test]
             [js.react-native.ui-modal-test :as ui-modal-test]
             [js.react-native.ui-router-test :as ui-router-test]
             [js.react-native.ui-check-box-test :as ui-check-box-test]
             [js.react-native.ui-radio-box-test :as ui-radio-box-test]
             [js.react-native.ui-scrollview-test :as ui-scrollview-test]
             [js.react-native.ui-toggle-button-test :as ui-toggle-button-test]
             [js.react-native.ui-toggle-switch-test :as ui-toggle-switch-test]
             [js.react-native.ui-tooltip-test :as ui-tooltip-test]
             [js.react-native.ui-util-test :as ui-util-test]]
   })

(defrun.js __import__
  (:- :import (quote [React]) :from "'react'"))

(defn.js IntroCard
  [#{[title
       text
       accent]}]
  (return
   [:% n/View
    {:style {:flex 1
             :minWidth 190
             :marginRight 12
             :marginBottom 12
             :padding 18
             :borderWidth 1
             :borderColor "#dbe4f0"
             :borderRadius 14
             :backgroundColor "#ffffff"}}
    [:% n/View
     {:style {:width 34
              :height 5
              :marginBottom 15
              :borderRadius 3
              :backgroundColor accent}}]
    [:% n/Text
     {:style {:marginBottom 7
              :color "#0f172a"
              :fontSize 15
              :fontWeight "800"}}
     title]
    [:% n/Text
     {:style {:color "#64748b"
              :fontSize 12
              :lineHeight 18}}
     text]]))

(defn.js IntroLink
  [#{[label
       route
       onNavigate]}]
  (return
   [:% n/TouchableOpacity
    {:onPress (fn []
                (if onNavigate
                  (onNavigate route)))
     :style {:marginRight 8
             :marginBottom 8
             :paddingHorizontal 14
             :paddingVertical 10
             :borderRadius 9
             :backgroundColor "#eff6ff"
             :borderWidth 1
             :borderColor "#bfdbfe"}}
    [:% n/Text
     {:style {:color "#1d4ed8"
              :fontSize 12
              :fontWeight "800"}}
     label]]))

(defn.js IntroPage
  [#{onNavigate}]
  (return
   [:% n/ScrollView
    {:style {:flex 1
             :backgroundColor "#f8fafc"}
     :contentContainerStyle {:maxWidth 1080
                             :width "100%"
                             :alignSelf "center"
                             :padding 32
                             :paddingBottom 80}}
    [:% n/View
     {:style {:padding 30
              :borderRadius 20
              :backgroundColor "#0f172a"
              :shadowColor "#0f172a"
              :shadowOffset {:width 0
                             :height 8}
              :shadowOpacity 0.16
              :shadowRadius 18
              :elevation 3}}
     [:% n/View
      {:style {:flexDirection "row"
               :alignItems "center"
               :marginBottom 20}}
      [:% n/View
       {:style {:width 10
                :height 10
                :marginRight 9
                :borderRadius 5
                :backgroundColor "#60a5fa"}}]
      [:% n/Text
       {:style {:color "#93c5fd"
                :fontSize 11
                :fontWeight "800"
                :letterSpacing 1.2}}
       "FOUNDATION BASE  /  JS.REACT-NATIVE"]]
     [:% n/Text
      {:style {:maxWidth 700
               :color "#f8fafc"
               :fontSize 34
               :fontWeight "800"
               :lineHeight 42}}
      "Build interfaces from small, expressive pieces."]
     [:% n/Text
      {:style {:maxWidth 680
               :marginTop 14
               :color "#cbd5e1"
               :fontSize 15
               :lineHeight 24}}
      "This is an interactive tour of the React Native building blocks in foundation-base. Every panel is live: change a value, press a button, and see the component respond."]
     [:% n/View
      {:style {:flexDirection "row"
               :flexWrap "wrap"
               :marginTop 24}}
      [:% n/View
       {:style {:marginRight 8
                :marginBottom 8
                :paddingHorizontal 10
                :paddingVertical 6
                :borderRadius 999
                :backgroundColor "#172554"}}
       [:% n/Text
        {:style {:color "#bfdbfe"
                 :fontSize 10
                 :fontWeight "800"
                 :letterSpacing 0.5}}
        "LIVE PREVIEWS"]]
      [:% n/View
       {:style {:marginRight 8
                :marginBottom 8
                :paddingHorizontal 10
                :paddingVertical 6
                :borderRadius 999
                :backgroundColor "#172554"}}
       [:% n/Text
        {:style {:color "#bfdbfe"
                 :fontSize 10
                 :fontWeight "800"
                 :letterSpacing 0.5}}
        "SOURCE INCLUDED"]]
      [:% n/View
       {:style {:marginBottom 8
                :paddingHorizontal 10
                :paddingVertical 6
                :borderRadius 999
                :backgroundColor "#172554"}}
       [:% n/Text
        {:style {:color "#bfdbfe"
                 :fontSize 10
                 :fontWeight "800"
                 :letterSpacing 0.5}}
        "WEB READY"]]]]
    [:% n/View
     {:style {:marginTop 30
              :marginBottom 12}}
     [:% n/Text
      {:style {:color "#0f172a"
               :fontSize 22
               :fontWeight "800"}}
      "A guided component lab"]
     [:% n/Text
      {:style {:maxWidth 720
               :marginTop 8
               :color "#64748b"
               :fontSize 14
               :lineHeight 21}}
      "Use the navigation rail to move from core primitives to stateful interactions, physical gestures, and ready-to-use UI components. Start anywhere, then follow the nested examples as a map of the library."]]
    [:% n/View
     {:style {:flexDirection "row"
              :flexWrap "wrap"}}
     [:% -/IntroCard
      {:title "Foundations"
       :text "Text, layout, containers, tabs, lists, and trees."
       :accent "#2563eb"}]
     [:% -/IntroCard
      {:title "State & motion"
       :text "React helpers, events, animation, transitions, and indicators."
       :accent "#7c3aed"}]
     [:% -/IntroCard
      {:title "Physical UI"
       :text "Touch, gestures, drag-and-drop, layout, and rollers."
       :accent "#ea580c"}]
     [:% -/IntroCard
      {:title "Components"
       :text "Inputs, buttons, forms, navigation, modals, and utilities."
       :accent "#059669"}]]
    [:% n/View
     {:style {:marginTop 8
              :padding 24
              :borderRadius 14
              :backgroundColor "#eaf2ff"
              :borderWidth 1
              :borderColor "#c7dcff"}}
     [:% n/Text
      {:style {:color "#172554"
               :fontSize 17
               :fontWeight "800"}}
      "Jump right in"]
     [:% n/Text
      {:style {:marginTop 7
               :marginBottom 15
               :color "#475569"
               :fontSize 13
               :lineHeight 20}}
      "Use a shortcut below, or browse the full catalogue from the navigation rail."]
     [:% n/View
      {:style {:flexDirection "row"
               :flexWrap "wrap"}}
      [:% -/IntroLink
       {:label "Native primitives"
        :route "00a-native-text"
        :onNavigate onNavigate}]
      [:% -/IntroLink
       {:label "UI controls"
        :route "06-ui-button"
        :onNavigate onNavigate}]
      [:% -/IntroLink
       {:label "Forms & data"
        :route "00g-ext-form"
        :onNavigate onNavigate}]
      [:% -/IntroLink
       {:label "Animation"
        :route "01a-ani-base"
        :onNavigate onNavigate}]]]
    [:% n/View
     {:style {:marginTop 18
              :padding 24
              :borderWidth 1
              :borderColor "#dbe4f0"
              :borderRadius 14
              :backgroundColor "#ffffff"}}
     [:% n/Text
      {:style {:color "#0f172a"
               :fontSize 17
               :fontWeight "800"}}
      "How to explore"]
     [:% n/Text
      {:style {:marginTop 12
               :color "#475569"
               :fontSize 13
               :lineHeight 23}}
      "1  Select a section from the left rail.\n2  Interact with the controls inside each example.\n3  Press CODE to reveal the source behind a demo.\n4  Use nested tabs and lists to drill into related features."]
     [:% n/View
      {:style {:height 1
               :marginVertical 20
               :backgroundColor "#e2e8f0"}}]
     [:% n/Text
      {:style {:color "#64748b"
               :fontSize 12
               :lineHeight 19}}
      "The examples are generated from the same Clojure-based DSL that powers the library. They are documentation, experiments, and a set of composable patterns in one place."]]]))

(defn.js ReactExamples
  []
  (return
   [:<>
    [:% react-test/UseRefreshDemo]
    [:% react-test/UseFollowRefDemo]
    [:% react-test/UseGetCountDemo]
    [:% react-test/UseMountedCallbackDemo]
    [:% react-test/UseFollowDelayedDemo]
    [:% react-test/UseIsMountedDemo]
    
    [:% react-test/UseIntervalDemo]
    [:% react-test/UseTimeoutDemo]
    [:% react-test/UseCountdownDemo]
    [:% react-test/UseNowDemo]
    [:% react-test/UseChangingDemo]
    [:% react-test/UseTreeDemo]]))

(defn.js ReactLazyExamples
  []
  (return
   [:<>
    [:% react-lazy-test/UseLazyDemo]]))

(defn.js NativeExamples
  []
  (return
   [:<>
    [:% react-native-test/EnclosedDemo]
    [:% react-native-test/EnclosedCodeContainerDemo]
    [:% react-native-test/EnclosedCodeDemo]
    [:% react-native-test/RowDemo]
    [:% react-native-test/FillDemo]
    [:% react-native-test/H1Demo]
    [:% react-native-test/H2Demo]
    [:% react-native-test/H3Demo]
    [:% react-native-test/H4Demo]
    [:% react-native-test/H5Demo]
    [:% react-native-test/CaptionDemo]]))

(defn.js GroupExamples
  []
  (return
   [:<>
    [:% react-native-test/TabsIndexedDemo]
    [:% react-native-test/TabsDemo]
    [:% react-native-test/TabsMultiIndexedDemo]
    [:% react-native-test/TabsMultiDemo]
    [:% react-native-test/ListIndexedDemo]
    [:% react-native-test/ListDemo]]))

(defn.js TreeExamples
  []
  (return
   [:<>
    [:% react-native-test/TabsPaneDemo]
    [:% react-native-test/ListPaneDemo]
    [:% react-native-test/TreePaneDemo]]))

(defn.js DataExamples
  []
  (return
   [:<>
    [:% react-native-test/BaseIndicatorDemo]
    [:% react-native-test/ToggleIndicatorDemo]
    [:% react-native-test/RecordListDemo]
    [:% react-native-test/TextDisplayDemo]]))

(defn.js PortalExamples
  []
  (return
   [:<>
    [:%  react-native-test/PortalDemo]
    [:%  react-native-test/UsePortalLayoutsDemo]]))

(defn.js NativeModalExamples
  []
  (return
   [:<>
    [:% physical-modal-test/GetPositionDemo]
    [:% physical-modal-test/DisplayModalDemo]]))

(defn.js ViewExamples
  []
  (return
   [:<>
    [:% ext-model-test/ListenViewDemo]
    [:% ext-model-test/ListenViewOutputDemo]
    [:% ext-model-test/ListenViewOutputMultiDemo]]))

(defn.js RouteExamples
  []
  (return
   [:<>
    [:% ext-route-test/UseRouteSegmentDemo]
    [:% helper-browser-test/UseHashRouteDemo]]))

(defn.js BoxExamples
  []
  (return
   [:<>
    [:% ext-box-test/UseBoxDemo]]))

(defn.js LogExamples
  []
  (return
   [:<>
    [:% ext-log-test/ListenLogLatestDemo]]))

(defn.js FormExamples
  []
  (return
   [:<>
    [:% ext-form-test/RegistrationFormDemo]]))

(defn.js AnimateExamples
  []
  (return
   [:<>
    [:% animate-test/ValDemo]
    [:% animate-test/DeriveDemo]
    [:% animate-test/ListenSingleDemo]
    [:% animate-test/UseListenSingleDemo]
    [:% animate-test/ListenArrayDemo]
    [:% animate-test/UseListenArrayDemo]
    [:% animate-test/ListenMapDemo]
    [:% animate-test/ListenTransformationsDemo]]))

(defn.js AnimateTransitionExamples
  []
  (return
   [:<>
    [:% animate-test/CreateTransitionDemo]
    [:% animate-test/RunWithCancelDemo]
    [:% animate-test/RunWithOneDemo]
    [:% animate-test/RunWithAllDemo]]))

(defn.js AnimateIndicatorExamples
  []
  (return
   [:<>
    [:% animate-test/UseBinaryIndicatorDemo]
    [:% animate-test/UseIndexIndicatorDemo]
    [:% animate-test/UsePressIndicatorDemo]
    [:% animate-test/UseLinearIndicatorDemo]
    [:% animate-test/UseCircularIndicatorDemo]
    [:% animate-test/UseShowingDemo]
    [:% animate-test/UsePositionDemo]
    [:% animate-test/UseRangeDemo]]))

(defn.js PhysicalDisplayExamples
  []
  (return
   [:<>
    [:% physical-base-test/TagDemo]
    [:% physical-base-test/BoxDemo]
    [:% physical-base-test/TextDemo]]))


(defn.js PhysicalTouchExamples
  []
  (return
   [:<>
    [:% physical-base-test/TouchableBasePressingDemo]
    [:% physical-base-test/TouchableBinaryDemo]
    [:% physical-base-test/TouchableInputDemo]]))

(defn.js PhysicalEditExamples
  []
  (return
   [:<>
    [:% physical-edit-test/CreatePanDemo]
    [:% physical-edit-test/CreatePanVelocityDemo]
    [:% physical-edit-test/ProgressDemo]]))

(defn.js PhysicalDndExamples
  []
  (return
   [:<>
    [:% physical-dnd-test/DragAndDropDemo]]))


(defn.js PhysicalLayoutExamples
  []
  (return
   [:<>
    [:% physical-layout-test/GridDemo]]))


(defn.js PhysicalPlayExamples
  []
  (return
   [:<>
    [:% physical-play-test/DigitRollerStaticDemo]
    [:% physical-play-test/DigitRollerSingleDemo]
    [:% physical-play-test/DigitRollerDoubleDemo]
    [:% physical-play-test/DigitClockDemo]]))

(defn.js ModelRollerExamples
  []
  (return
   [:<>
    [:% model-roller-impl-test/DigitRollerManualDemo]
    [:% model-roller-impl-test/DigitRollerPanDemo]]))

(defn.js PhysicalCaroselExamples
  []
  (return
   [:<>
    [:% physical-carosel-test/DigitCaroselManualDemo]]))

(defn.js UiLayoutFrameExamples
  []
  (return
   [:<>
    [:% ui-frame-test/FramePaneDemo]
    [:% ui-frame-test/FrameDemo]]))

(defn.js UiButtonExamples
  []
  (return
   [:<>
    [:% ui-button-test/ButtonSimpleDemo]
    [:% ui-button-test/ButtonOpacityDemo]
    [:% ui-button-test/ButtonSizeDemo]
    [:% ui-button-test/ButtonFractionDemo]]))

(defn.js UiAutocompleteExamples
  []
  (return
   [:<>
    [:% ui-autocomplete-test/AutocompleteModalDemo]
    [:% ui-autocomplete-test/AutocompleteDemo]]))

(defn.js UiInputExamples
  []
  (return
   [:<>
    [:% ui-input-test/InputSimpleDemo]
    [:% ui-input-test/InputDemo]]))

(defn.js UiCheckBoxExamples
  []
  (return
   [:<>
    [:% ui-check-box-test/CheckBoxSimpleDemo]]))

(defn.js UiRadioBoxExamples
  []
  (return
   [:<>
    [:% ui-radio-box-test/RadioBoxSimpleDemo]]))


(defn.js UiToggleButtonExamples
  []
  (return
   [:<>
    [:% ui-toggle-button-test/ToggleButtonSimpleDemo]]))

(defn.js UiToggleSwitchExamples
  []
  (return
   [:<>
    [:% ui-toggle-switch-test/ToggleSwitchSimpleDemo]
    [:% ui-toggle-switch-test/ToggleSwitchSquareDemo]]))

(defn.js UiSliderExamples
  []
  (return
   [:<>
    [:% ui-slider-test/SliderHDemo]
    [:% ui-slider-test/SliderVDemo]]))

(defn.js UiRangeExamples
  []
  (return
   [:<>
    [:% ui-range-test/RangeHDemo]
    [:% ui-range-test/RangeVDemo]]))

(defn.js UiSpinnerExamples
  []
  (return
   [:<>
    [:% ui-spinner-test/SpinnerStaticDemo]
    [:% ui-spinner-test/SpinnerDigitDemo]
    [:% ui-spinner-test/SpinnerValuesDemo]
    [:% ui-spinner-test/SpinnerDemo]]))

(defn.js UiModalExamples
  []
  (return
   [:<>
    [:% ui-modal-test/ModalDemo]]))

(defn.js UiScrollViewExamples
  []
  (return
   [:<>
    [:% ui-scrollview-test/ScrollViewDemo]]))

(defn.js UiNotifyExamples
  []
  (return
   [:<>
    [:% ui-notify-test/NotifyDemo]]))

(defn.js UiRouterExamples
  []
  (return
   [:<>
    [:% ui-router-test/UseTransitionDemo]
    [:% ui-router-test/RouterDemo]]))


(defn.js UiPickerExamples
  []
  (return
   [:<>
    [:% ui-picker-test/PickerIndexedDemo]]))

(defn.js UiSwiperExamples
  []
  (return
   [:<>
    [:% ui-swiper-test/SwiperDemo]]))

(defn.js UiTooltipExamples
  []
  (return
   [:<>
    [:% ui-tooltip-test/TooltipDemo]]))

(defn.js UiUtilExamples
  []
  (return
   [:<>
    [:% ui-util-test/PageDemo]
    [:% ui-util-test/FadeDemo]
    [:% ui-util-test/FoldInnerDemo]
    [:% ui-util-test/FoldDemo]]))

(defn.js raw-controls
  []
  (return
   (tab ["000-intro"         -/IntroPage]
        ["000-react"         -/ReactExamples]
        ["000-react-lazy"    -/ReactLazyExamples]
        ["00a-native-text"   -/NativeExamples]
        ["00a-portal-test"   -/PortalExamples]
        ["00b-native-group"  -/GroupExamples]
        ["00c-native-tree"   -/TreeExamples]
        ["00d-native-data"   -/DataExamples]
        ["00e-native-modal"  -/NativeModalExamples]
        ["00f-ext-model"      -/ViewExamples]
        ["00g-ext-form"      -/FormExamples]
        ["00h-ext-route"     -/RouteExamples]
        ["00j-ext-box"       -/BoxExamples]
        ["00k-ext-log"       -/LogExamples]
        
        ["01a-ani-base"         -/AnimateExamples]
        ["01b-ani-transition"   -/AnimateTransitionExamples]
        ["01c-ani-indicators"   -/AnimateIndicatorExamples]
        ["02a-phy-display"      -/PhysicalDisplayExamples]
        ["02b-phy-touch"        -/PhysicalTouchExamples]
        ["02c-phy-edit"         -/PhysicalEditExamples]
        ["02d-phy-dnd"          -/PhysicalDndExamples]
        ["02e-phy-layout"       -/PhysicalLayoutExamples]
        ["02k-phy-play"         -/PhysicalPlayExamples]
        
        ["03a-model-roller"    -/ModelRollerExamples]
        ["03b-carosel"         -/PhysicalCaroselExamples]

        ["06-ui-autocomplete"  -/UiAutocompleteExamples]
        ["06-ui-button"        -/UiButtonExamples]
        ["06-ui-checkbox"      -/UiCheckBoxExamples]
        ["06-ui-frame"         -/UiLayoutFrameExamples]
        ["06-ui-input"         -/UiInputExamples]
        ["06-ui-modal"         -/UiModalExamples]
        ["06-ui-notify"        -/UiNotifyExamples]
        ["06-ui-radiobox"      -/UiRadioBoxExamples]
        ["06-ui-range"         -/UiRangeExamples]
        ["06-ui-router"        -/UiRouterExamples]
        ["06-ui-picker"        -/UiPickerExamples]
        ["06-ui-scrollview"    -/UiScrollViewExamples]
        ["06-ui-slider"        -/UiSliderExamples]
        ["06-ui-spinner"       -/UiSpinnerExamples]
        ["06-ui-switch"        -/UiToggleSwitchExamples]
        ["06-ui-swiper"        -/UiSwiperExamples]
        ["06-ui-toggle"        -/UiToggleButtonExamples]
        ["06-ui-tooltip"       -/UiTooltipExamples]
        ["06-ui-util"          -/UiUtilExamples])))
