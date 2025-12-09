(ns component.web-native
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react-native-test :as react-native-test]
             [js.react-native.react-test :as react-test]
             [js.react-native.react-lazy-test :as react-lazy-test]
             [js.react-native.animate-test :as animate-test]
             [js.react-native.ext-box-test :as ext-box-test]
             [js.react-native.ext-log-test :as ext-log-test]
             [js.react-native.ext-view-test :as ext-view-test]
             [js.react-native.ext-cell-test :as ext-cell-test]
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
    [:% ext-view-test/ListenViewDemo]
    [:% ext-view-test/ListenViewOutputDemo]
    [:% ext-view-test/ListenViewOutputMultiDemo]]))

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

(defn.js CellExamples
  []
  (return
   [:<>
    [:% ext-cell-test/SimpleCellDemo]
    [:% ext-cell-test/SimpleCellViewsDemo]]))

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
   (tab ["000-react"         -/ReactExamples]
        ["000-react-lazy"    -/ReactLazyExamples]
        ["00a-native-text"   -/NativeExamples]
        ["00a-portal-test"   -/PortalExamples]
        ["00b-native-group"  -/GroupExamples]
        ["00c-native-tree"   -/TreeExamples]
        ["00d-native-data"   -/DataExamples]
        ["00e-native-modal"  -/NativeModalExamples]
        ["00f-ext-view"      -/ViewExamples]
        ["00g-ext-form"      -/FormExamples]
        ["00h-ext-cell"      -/CellExamples]
        ["00i-ext-route"     -/RouteExamples]
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


