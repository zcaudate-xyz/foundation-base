(ns js.react-native.physical-base
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.helper-theme-default :as helper-theme-default]]})

(defn.js Tag
  "listens to a single indicator to set ref"
  {:added "4.0"}
  ([#{[indicator
       style
       (:= format (fn:> [v] (j/toFixed v 4)))
       (:.. rprops)]}]
   (var refLabel (a/useListenSingle indicator (fn:> [v] {:text (format v)})))
   (return [:% n/TextInput
            #{[:ref refLabel
               :editable false
               :style [{:width 50
                        :fontSize 12}
                       (n/PlatformSelect {:ios {:fontFamily "Courier"}
                                          :default {:fontFamily "monospace"}})
	               (n/PlatformSelect {:web {:userSelect "none"
                                                :cursor "default"}})
                       (:.. (j/arrayify style))]
               (:.. rprops)]}])))

;;
;;

(defn.js transformInner
  "allow inner components access to chords and indicators"
  {:added "4.0"}
  ([indicators chord inner transformFn]
   (return (j/map (j/filter inner k/identity)
                  (fn [#{[component
                          (:.. iprops)]}
                       i]
                    (var tprops (transformFn
                                 (j/assign {:key i
                                            :component  component
                                            :indicators indicators
                                            :chord      chord}
                                           iprops)))
                    (return (r/createElement
                             component
                             tprops)))))))

(defn.js transformProps
  "higher order react function or transforming props"
  {:added "4.0"}
  ([props]
   (let [#{[chord
            style
            allowRef
            component
            (:= indicators {})
            (:= refLink (r/ref))
            (:= children [])
            (:= inner [])
            (:= size "md")
            (:= transformations {})
            (:.. rprops)]} props
         ostyles (:? (k/arr? style) style [style])
         sstyle  (or (. helper-theme-default/FontSize [size])
                     {:fontSize size})
         refChord   (r/useFollowRef chord)
         getChord   (r/const (fn:> (r/curr refChord)))]
     (r/init []
       (a/listenTransformations refLink
                                indicators
                                transformations
                                getChord))
     (var tchildren [(:.. (j/arrayify children))
                     (:.. (-/transformInner indicators chord inner -/transformProps))])
     (return (j/assign
              {(:? (or allowRef
                       (not (k/fn? component)))
                   "ref"
                   "refLink") refLink
               :style [sstyle
                       (:.. ostyles)]}
              rprops
              (:? (k/not-empty? tchildren)
                  {:children tchildren}))))))

(defn.js transformInnerFn
  "transforms inner props"
  {:added "4.0"}
  [#{indicators
     chord}]
  (return (fn:> [inner]
            (-/transformInner
             indicators
             chord
             inner
             -/transformProps))))

(defn.js Box
  "demo of indicators"
  {:added "4.0"}
  ([#{[(:= addons [])
       (:.. props)]}]
   (var tprops (-/transformProps props))
   (var innerFn  (-/transformInnerFn props))
   (return [:<>
            [:% n/View #{[(:.. tprops)]}]
            (innerFn addons)])))

(defn.js Text
  "creates a text element"
  {:added "4.0"}
  ([#{[(:= addons [])
       (:.. props)]}]
   (var tprops (-/transformProps props))
   (var innerFn  (-/transformInnerFn props))
   (return [:<>
            [:% n/TextInput
             #{[:editable false
                :multiline true
                (:.. tprops)]}]
            (innerFn addons)])))

(defn.js useChordDisabled
  "constructs chord and indicator for disable flag"
  {:added "4.0"}
  [#{[disabled
,      (:= indicatorParams {})]}]
  (var dindicator      (a/useBinaryIndicator disabled (or (. indicatorParams ["disabled"])
                                                          {:default {:duration 400}})))
  (return {:indicators {:disabled  dindicator}
           :chord      #{disabled}}))

(defn.js useChordHighlighted
  "constructs chord and indicator for highlighted flag"
  {:added "4.0"}
  [#{[highlighted
      (:= indicatorParams {})]}]
  (var hindicator      (a/useBinaryIndicator highlighted (or (. indicatorParams ["highlighted"])
                                                          {:default {:duration 200}})))
  (return {:indicators {:highlighted  hindicator}
           :chord      #{highlighted}}))

(defn.js useChordHoverable
  "constructs chord and indicator for hovering motion"
  {:added "4.0"}
  [#{[(:= indicatorParams {})]}]
  (var [hovering setHovering] (r/local false))
  (var hindicator      (a/usePressIndicator hovering (or (. indicatorParams ["hovering"])
                                                          {:default {:duration 200}})))
  (return #{[:indicators {:hovering  hindicator}
             :chord   #{hovering}
             hovering setHovering]}))

(defn.js useChordPressable
  "constructs chord and indicator for pressing motion"
  {:added "4.0"}
  [#{[(:= indicatorParams {})]}]
  (var [pressing setPressing] (r/local false))
  (var pindicator      (a/usePressIndicator  pressing (or (. indicatorParams ["pressing"])
                                                          {:default {:duration 250}})))
  (return #{[:indicators {:pressing  pindicator}
             :chord   #{pressing}
             pressing setPressing]}))

(defn.js useChordEmptyable
  "constructs chord and indicator for empty value on inputs"
  {:added "4.0"}
  [#{[value
      (:= indicatorParams {})]}]
  (var [emptying setEmptying]  (r/local (k/is-empty? value)))
  (var eindicator      (a/useBinaryIndicator emptying
                                             (or (. indicatorParams ["emptying"])
                                                 {:default {:duration 200}})))
  (return #{[:indicators {:emptying  eindicator}
             :chord   #{emptying}
             emptying setEmptying]}))

(defn.js useChordFocusable
  "constructs chord and indicator for focusing motion"
  {:added "4.0"}
  [#{[(:= indicatorParams {})]}]
  (var [focusing setFocusing]  (r/local false))
  (var findicator      (a/useBinaryIndicator focusing
                                             (or (. indicatorParams ["focusing"])
                                                 {:type "spring"
                                                  :default {:damping 20
                                                            :mass 2}})))
  (return #{[:indicators {:focusing  findicator}
             :chord   #{focusing}
             focusing setFocusing]}))

(defn.js useChordDraggable
  "constructs chord and indicator for drag enable and drag capture motion"
  {:added "4.0"}
  [#{[(:= indicatorParams {})]}]
  (var [dragEnabled  setDragEnabled]   (r/local false))
  (var [dragCaptured setDragCaptured]  (r/local false))
  (var eindicator      (a/useBinaryIndicator dragEnabled
                                             (or (. indicatorParams ["dragEnabled"])
                                                 {:default {:duration 200}})))
  (var cindicator      (a/useBinaryIndicator dragCaptured
                                             (or (. indicatorParams ["dragCaptured"])
                                                 {:default {:duration 400}})))
  (return #{[:indicators {:dragEnabled  eindicator
                          :dragCaptured cindicator}
             :chord   #{dragEnabled
                        dragCaptured}
             dragEnabled  setDragEnabled
             dragCaptured setDragCaptured]}))

(defn.js mergeChords
  "merges all chords, indicators and methods"
  {:added "4.0"}
  [chords]
  (var allIndicators {})
  (var allChord {})
  (var out {})
  (k/for:array [e chords]
    (var #{[indicators
            chord
            (:.. rprops)]} e)
    (j/assign out rprops)
    (j/assign allIndicators indicators)
    (j/assign allChord chord))
  (return (j/assign out
                    {:indicators allIndicators
                     :chord   allChord})))

(defn.js useIndicatorCapture
  "allow capture of indicators from a top level element"
  {:added "4.0"}
  [#{chord
     onChord
     indicators
     onIndicators}]
  (r/init []
    (when onIndicators
      (onIndicators indicators)))
  (r/watch [(JSON.stringify chord)]
    (when onChord (onChord chord))))

;;
;;
;;

(defn.js useHoverable
  "helper function for hoverable targets"
  {:added "4.0"}
  [#{[indicators
      chord
      (:.. rprops)]}]
  (var disabled    (-/useChordDisabled rprops))
  (var hoverable   (-/useChordHoverable rprops))
  (var merged  (-/mergeChords [disabled
                               hoverable
                               #{indicators
                                 chord}]))
  (var props (j/assign {} rprops merged))
  (-/useIndicatorCapture props)
  (return props))

(defn.js HoverableTarget
  "an element that responds when mouse is hovering"
  {:added "4.0"}
  ([#{[disabled
       outlined
       chord
       onChord
       onHoverIn
       onHoverOut
       indicators
       
       onIndicators
       (:= indicatorParams {})
       (:= addons [])
       (:= refLink  (r/ref))
       (:.. rprops)]}]
   (var hoverable   (-/useHoverable #{[disabled
                                       :chord (j/assign #{outlined} chord)
                                       onChord
                                       indicators
                                       indicatorParams
                                       onIndicators]}))
   (var #{hovering
          setHovering}  hoverable)
   (var innerFn         (-/transformInnerFn hoverable))
   (var tprops          (-/transformProps (j/assign rprops hoverable)))
   (return [:<>
            [:% n/View
             #{[:ref refLink
                :onMouseEnter  (fn [e] (setHovering true)
                                 (if onHoverIn (onHoverIn e)))
                :onMouseLeave  (fn [e] (setHovering false)
                                 (if onHoverOut (onHoverOut e)))
                (:.. tprops)]}]
            (innerFn addons)])))

(defn.js useTouchable
  "helper function for all touchable type components"
  {:added "4.0"}
  [#{[indicators
      chord
      (:.. rprops)]}]
  (var disabled    (-/useChordDisabled rprops))
  (var highlighted (-/useChordHighlighted rprops))
  (var hoverable   (-/useChordHoverable rprops))
  (var pressable   (-/useChordPressable rprops))
  (var merged  (-/mergeChords [disabled
                               highlighted
                               hoverable
                               pressable
                               #{indicators
                                 chord}]))
  (var props (j/assign {} rprops merged))
  (-/useIndicatorCapture props)
  (return props))

(defn.js TouchableBasePressing
  "base touchable with pressing indicator"
  {:added "4.0"}
  ([#{[style
       children
       disabled
       highlighted
       outlined
       chord
       onChord
       onPressIn
       onPressOut
       onPress
       onHoverIn
       onHoverOut
       indicators
       
       onIndicators
       (:= indicatorParams {})
       (:= addons [])
       (:= inner  [])
       (:= refLink  (r/ref))
       (:.. rprops)]}]
   (var touchable   (-/useTouchable #{[disabled
                                        highlighted
                                        outlined
                                        :chord (j/assign #{outlined} chord)
                                        onChord
                                        indicators
                                        indicatorParams
                                        
                                       onIndicators]}))
   (var #{pressing
          setPressing
          hovering
          setHovering}  touchable)
   (var innerFn         (-/transformInnerFn touchable))
   (return
    [:<>
     [:% n/Pressable
      #{[:disabled disabled
         :style (k/arrayify style)
         :onPressIn  (fn [e] (setPressing true)
                       (if onPressIn (onPressIn e)))
         :onPressOut (fn [e] (setPressing false)
                       (if onPressOut (onPressOut e)))
         :onHoverIn  (fn [e] (setHovering true)
                       (if onHoverIn (onHoverIn e)))
         :onHoverOut (fn [e] (setHovering false)
                       (if onHoverOut (onHoverOut e)))
         :onMouseUp  (fn []
                       (setPressing false))
         :onMouseDown   (fn [e]
                          (. e nativeEvent (preventDefault)))
         :onMouseMove   (fn [e]
                          (. e nativeEvent (preventDefault)))
         :onPress onPress
         #_(:.. rprops)]}
      [:% n/View
       {:ref refLink
        :style [#_{:flex 1}
                (n/PlatformSelect
                 {:web {:userSelect "none"}})]}
       (innerFn inner)
       children]]
     (innerFn addons)])))

(defn.js TouchableBinary
  "base touchable with single state"
  {:added "4.0"}
  [#{[active
      chord
      indicators
      (:= indicatorParams {})
      (:.. rprops)]}]
  (var aindicator (a/useBinaryIndicator active
                                        (or (. indicatorParams ["active"])
                                            {:default {:duration 300}})))
  (return
   [:% -/TouchableBasePressing
    #{[:chord      (j/assign {} chord #{active})
       :indicators (j/assign {} indicators {:active aindicator})
       :indicatorParams indicatorParams
       (:.. rprops)]}]))

(defn.js useInputable
  "helper function for inputable components"
  {:added "4.0"}
  [#{[indicators
      chord
      (:.. rprops)]}]
  (var disabled    (-/useChordDisabled rprops))
  (var highlighted (-/useChordHighlighted rprops))
  (var hoverable   (-/useChordHoverable rprops))
  (var focusable   (-/useChordFocusable rprops))
  (var emptyable   (-/useChordEmptyable rprops))
  (var merged      (-/mergeChords [disabled
                                   highlighted
                                   hoverable
                                   focusable
                                   emptyable
                                   #{indicators
                                 chord}]))
  (var props (j/assign {} rprops merged))
  (-/useIndicatorCapture props)
  (return props))

(defn.js TouchableInput
  "base touchable with single state"
  {:added "4.0"}
  ([#{[chord
       disabled
       highlighted
       outlined
       value
       onChord
       onFocus
       onBlur
       onHoverIn
       onHoverOut
       indicators
       
       onIndicators
       onChangeText
       (:= indicatorParams {})
       (:= addons [])
       (:= inner [])
       (:= styleContainer {})
       (:= containerProps {})
       (:= refLink (r/ref))
       (:= refInput (r/ref))
       (:.. rprops)]}]
   (when (k/nil? value)
     (:= value ""))
   (var inputable  (-/useInputable #{[disabled
                                       highlighted
                                       value
                                       :chord (j/assign #{outlined} chord)
                                       onChord
                                       indicators
                                       indicatorParams
                                       
                                      onIndicators]}))
   (var #{focusing setFocusing
           hovering setHovering
          emptying setEmptying} inputable)
   (var valueRef (r/ref value))
   (r/watch [value]
     (when (not= value (r/curr valueRef))
       (r/curr:set valueRef value)
       (if onChangeText (onChangeText value))
       (setEmptying (k/is-empty? value))))
   (return
    [:% -/Box
     #{[:refLink refLink
        :style styleContainer
        :inner [(j/assign
                 {:component n/TextInput
                  :refLink refInput
                  :editable      (not disabled)
                  :onMouseEnter  (fn [e] (setHovering true)
                                   (if onHoverIn (onHoverIn e)))
                  :onMouseLeave  (fn [e] (setHovering false)
                                   (if onHoverOut (onHoverOut e)))
                  :onFocus       (fn [e] (setFocusing true)
                                   (if onFocus (onFocus e)))
                  :onBlur       (fn [e] (setFocusing false)
                                  (if onBlur (onBlur e)))
                  :onChangeText (fn [v]
                                  (setEmptying (k/is-empty? v))
                                  (r/curr:set valueRef v)
                                  (if onChangeText (onChangeText v)))
                  :value  value}
                 rprops)
                (:.. inner)]
        :addons addons
        (:.. (j/assign inputable containerProps))]}])))

