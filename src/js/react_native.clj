(ns js.react-native
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle {:default  [["react-native" :as [* ReactNative]]]
            :localize [["react-native-localize" :as [* RNLocalize]]]
            :svg      [["react-native-svg" :as [* RNSvg]]]
            :safearea [["react-native-safe-area-context" :as [* RNSafeArea]]]
            :gesture  [["react-native-gesture-handler" :as [* RNGestureHandler]]]
            :error    [["react-native-error-boundary" :as RNErrorBoundary]]
            :video    [["react-native-video" :as RNVideo]]
            :gradient [["expo-linear-gradient" :as [* ExpoLinearGradient]]]
            :nav      {:default  [["@react-navigation/native" :as [* RNNav]]]
                       :stack    [["@react-navigation/stack"  :as [* RNNavStack]]]
                       :drawer   [["@react-navigation/drawer" :as [* RNNavDrawer]]]
                       :tabs     [["@react-navigation/bottom-tabs" :as [* RNNavTabs]]]}
            :icon     {:material   [["react-native-vector-icons/MaterialCommunityIcons"
                                     :as RNIcon]]
                       :feather    [["react-native-vector-icons/Feather" :as RNIcon]]
                       :fa         [["react-native-vector-icons/FontAwesome" :as RNIcon]]
                       :fa5        [["react-native-vector-icons/FontAwesome5" :as RNIcon]]
                       :ionicons   [["react-native-vector-icons/Ionicons" :as RNIcon]]
                       :entypo     [["react-native-vector-icons/Entypo" :as RNIcon]]
                       :ant        [["react-native-vector-icons/AntDesign" :as RNIcon]]}}
   :require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react.helper-portal :as helper-portal]
             [xt.lang.base-lib :as k]]
   :import [["react-native" :as [* ReactNative]]]})

(def +StyleLayout+
  #{:alignContent
    :alignItems
    :alignSelf
    :aspectRatio
    :borderBottomWidth
    :borderEndWidth
    :borderLeftWidth
    :borderRightWidth
    :borderStartWidth
    :borderTopWidth
    :borderWidth
    :bottom
    :direction
    :display
    :end
    :flex
    :flexBasis
    :flexDirection
    :flexGrow
    :flexShrink
    :flexWrap
    :height
    :justifyContent
    :left
    :margin
    :marginBottom
    :marginEnd
    :marginHorizontal
    :marginLeft
    :marginRight
    :marginStart
    :marginTop
    :marginVertical
    :maxHeight
    :maxWidth
    :minHeight
    :minWidth
    :overflow
    :padding
    :paddingBottom
    :paddingEnd
    :paddingHorizontal
    :paddingLeft
    :paddingRight
    :paddingStart
    :paddingTop
    :paddingVertical
    :position
    :right
    :start
    :top
    :width
    :zIndex})

(def +StyleShadow+
  #{:shadowColor
    :shadowOffset 
    :shadowOpacity 
    :shadowRadius})

(def +StyleText+
  #{:color
    :fontStyle
    :fontSize
    :fontFamily
    :fontWeight
    :includeFontPadding
    :fontVariant
    :letterSpacing
    :lineHeight
    :textAlign
    :textAlignVertical
    :textDecorationColor
    :textDecorationLine
    :textDecorationStyle
    :textShadowColor
    :textShadowOffset
    :textShadowRadius
    :textTransform
    :writingDirection})

(def +StyleView+
  #{:backfaceVisibility
    :backgroundColor
    :borderBottomColor
    :borderBottomEndRadius
    :borderBottomLeftRadius
    :borderBottomRightRadius
    :borderBottomStartRadius
    :borderBottomWidth
    :borderColor
    :borderEndColor
    :borderLeftColor
    :borderLeftWidth
    :borderRadius
    :borderRightColor
    :borderRightWidth
    :borderStartColor
    :borderStyle
    :borderTopColor
    :borderTopEndRadius
    :borderTopLeftRadius
    :borderTopRightRadius
    :borderTopStartRadius
    :borderTopWidth
    :borderWidth
    :elevation
    :opacity})

(def +StyleImage+
  #{:backfaceVisibility
    :backgroundColor
    :borderBottomLeftRadius
    :borderBottomRightRadius
    :borderColor
    :borderRadius
    :borderTopLeftRadius
    :borderTopRightRadius
    :borderWidth
    :opacity
    :overflow
    :overlayColor
    :resizeMode
    :tintColor})

(def +LayoutEvent+
  {:layout
   {:width  number?
    :height number?
    :x number?
    :y number?}
   :target number?})

(def +PressEvent+
  {:changedTouches vector?
   :identifier number?
   :locationX number?
   :locationY number?
   :pageX number?
   :pageY number?
   :target number?
   :timestamp number?
   :touches vector?})

(def +Rect+
  {:top    number?
   :left   number?
   :right  number?
   :bottom number?})

(def +ViewToken+
  {:item {:key string?},
   :key string?
   :index number?
   :isViewable boolean?})

;;
;; React Native
;;

(def +rn-component+
  '[ActivityIndicator 
    Button
    FlatList
    Image
    ImageBackground
    KeyboardAvoidingView
    Modal
    Pressable
    RefreshControl
    ScrollView
    SectionList
    StatusBar
    Switch
    
    Text
    TextInput
    
    TouchableHighlight
    TouchableOpacity
    TouchableWithoutFeedback

    View
    [Frame View]
    [Hidden View]
    [Padding View]
    VirtualizedList
    
    ;; Android
    DrawerLayoutAndroid
    TouchableNativeFeedback

    ;; IOS
    InputAccessoryView
    SafeAreaView])

(def +rn-api+
  '[AccessibilityInfo
    Alert
    Animated
    Appearance
    AppRegistry
    AppState
    DevSettings
    Dimensions
    Easing
    InteractionManager
    Keyboard
    LayoutAnimation
    Linking
    PanResponder
    PixelRatio
    Platform
    PlatformColor
    NativeModules
    Share
    StyleSheet
    Systrace
    Transforms
    Vibration
    useColorScheme
    useWindowDimensions

    ;; Android
    BackHandler
    PermissionAndroid
    ToastAndroid
    
    ;; iOS
    ActionSheetIOS
    DynamicColorIOS])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactNative"
                                   :tag "js"}]
  +rn-component+
  +rn-api+)

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactNative.Alert"
                                   :tag "js"}]
  [alert
   prompt])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactNative.Appearance"
                                   :tag "js"}]
  [getColorScheme])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactNative.Vibration"
                                   :tag "js"}]
  [vibrate 
   [vibrateStop cancel]])

(def +svg+
  '[[Svg default]
    Circle
    Ellipse
    G
    [SvgText Text]
    TSpan
    TextPath
    Path
    Polygon
    Polyline
    Line
    Rect
    Use
    [SvgImage Image]
    Symbol
    Defs
    LinearGradient
    RadialGradient
    Stop
    ClipPath
    Pattern
    Mask
    Marker
    ForeignObject
    [parseSvg parse]
    SvgAst
    SvgFromUri
    SvgFromXml
    SvgUri
    SvgXml
    SvgCss
    SvgCssUri
    SvgWithCss
    SvgWithCssUri
    inlineStyles
    LocalSvg
    WithLocalSvg
    loadLocalRawResource
    Shape
    RNSVGMarker
    RNSVGMask
    RNSVGPattern
    RNSVGClipPath
    RNSVGRadialGradient
    RNSVGLinearGradient
    RNSVGDefs
    RNSVGSymbol
    RNSVGImage
    RNSVGUse
    RNSVGTextPath
    RNSVGTSpan
    RNSVGText
    RNSVGGroup
    RNSVGPath
    RNSVGLine
    RNSVGEllipse
    RNSVGCircle
    RNSVGRect
    RNSVGSvg
    RNSVGForeignObject])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "RNSvg"
                                   :tag "js"}]
  +svg+)


;;
;; Navigation
;;


(def +nav+
  '[useNavigation
    useNavigationState
    useFocusEffect
    useRoute
    useIsFocused
    useLinkTo
    useLinking
    useScrollToTop
    NavigationContainer
    [NavDarkTheme DarkTheme]
    [NavDefaultTheme DefaultTheme]
    SwitchActions
    StackActions
    DrawerActions
    CommonActions
    TabActions])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "RNNav"
                                   :tag "js"}]
  +nav+)

(def$.js createDrawerNavigator RNNavDrawer.createDrawerNavigator)

(def$.js createStackNavigator RNNavStack.createStackNavigator)

(def$.js createBottomTabNavigator RNNavTabs.createBottomTabNavigator)

(h/template-entries [l/tmpl-macro {:base "RNNav"
                                   :inst "nav"
                                   :tag "js"}]
  [[[navAddListener addListener] [type cb]]
   [[navCanGoBack canGoBack] [type cb]]
   [[navParent dangerouslyGetParent] []]
   [[navState  dangerouslyGetstate] []]
   [[navDispatch dispatch] [action]]
   [[navCurrentOption getCurrentOptions] []]
   [[navCurrentRoute getCurrentRoute] []]
   [[navGoBack goBack]]
   [navigate [path] {:optional [opts]}]
   [[navRemoveListener removeListener] [type cb]]
   [[navReset reset] []]
   [[navResetRoot resetRoot]  []]
   [[navSetParams setParams]  [params]]])


;;
;; LinearGradient
;;

(def$.js ExpoLinearGradient (. ExpoLinearGradient LinearGradient))

;;
;; Video
;;

(def$.js Video RNVideo)

;;
;; Icon
;;

(def$.js Icon RNIcon)

(def$.js PlatformSelect ReactNative.Platform.select)

;;
;; GestureHandler
;;


(h/template-entries [l/tmpl-entry {:type :fragment
                                     :base "RNGestureHandler"
                                     :tag "js"}]
  [Swipeable
   PanGestureHandler
   TapGestureHandler
   LongPressGestureHandler
   RotationGestureHandler
   FlingGestureHandler
   PinchGestureHandler])

(h/template-entries [l/tmpl-entry {:type :fragment
                                     :base "RNSafeArea"
                                     :tag "js"}]
  [SafeAreaView
   SafeAreaProvider
   useSafeAreaInsets])

(def$.js ErrorBoundary RNErrorBoundary)


(defmacro.js isWeb
  "checks that env is web"
  {:added "4.0"}
  []
  '(== "web" (. ReactNative.Platform OS)))

(defmacro.js isTablet
  "checks that env is tablet"
  {:added "4.0"}
  []
  '(== true (. ReactNative.Platform isPad)))


;;
;; format
;;

(defn.js format-obj
  "formats an object"
  {:added "4.0"}
  [e]
  (var s (or (JSON.stringify e nil 2)
             ""))
  (var arr (k/split s "\n"))
  (return (+ (-> (k/arr-slice arr 1 (- (k/len arr) 1))
                 (k/arr-map (fn:> [l] (k/substring l 2)))
                 (k/arr-join "\n" )))))

(defn.js format-entry
  "formats an entry"
  {:added "4.0"}
  [e]
  (var out (-/format-obj e))
  (return (-> out
              (k/replace "\"" "")
              (k/replace "," ""))))

(defn.js measure
  "measures the element"
  {:added "4.0"}
  [elem f]
  (:= f (or f (fn:>)))
  (return
   (new Promise
        (fn [resolve reject]
          (if (and elem elem.measure)
            (. elem
               (measure
                (fn [fx fy width height px py]
                  (var out #{fx fy width height px py})
                  (f out)
                  (resolve out))))
            (do (k/LOG! "NOT MEASURED" elem)
                (resolve {:fx 0 :fy 0 :width 0 :height 0 :px 0 :py 0})))))))

(defn.js measureRef
  "measures the element in a ref"
  {:added "4.0"}
  [ref f]
  (return
   (:? (r/curr ref)
       (-/measure (r/curr ref) f))))

;;
;;
;;


(defn.js Enclosed
  "creates a enclosed section with label"
  {:added "0.1"}
  [#{[label
      styleLabel
      children
      style
      (:.. rprops)]}]
  (return
   [:% -/View
    #{[:style [{:margin 10
                :padding 10
                :borderStyle "solid"
	        :borderWidth 1
	        :borderColor "#aaa"
                :maxWidth 600}
               (:.. (j/arrayify style))]
       (:.. rprops)]}
    [:% -/Text
     {:style [{:position "absolute"
               :top -12
               :fontSize 10
               :padding 5
               :color "#666"
               :backgroundColor "#fff"}
              (-/PlatformSelect
               {:web {:userSelect "none"}})
              (:.. (j/arrayify styleLabel))]}
     label]
    children]))

(defn.js EnclosedCodeContainer
  "creates a enclosed section with label"
  {:added "4.0"}
  [#{[code
      label
      children]}]
  (var [showCode setShowCode] (r/local))
  (return
   [:% -/Enclosed
    {:label label}
    [:% -/View
     {:style {:position "absolute"
              :right -10
              :top -20
              :transform [{:scale 0.6}]
              :zIndex 10}}
     [:% -/Button
      {:title (:? showCode "DEMO" "CODE")
       :onPress (fn [] (setShowCode (not showCode)))}]]
    (:? showCode
        [:% -/Text code]
        children)]))

(defmacro.js EnclosedCode
  "creates an enclosed code section"
  {:added "4.0"}
  [props & children]
  (let [code (l/emit-str (apply list 'do children) (l/macro-opts))]
    (apply vector
           :% 'js.react-native/EnclosedCodeContainer
           (assoc props :code (list (list 'fn:> code)))
           children)))

(defn.js Row
  "constructs a row"
  {:added "0.1"}
  [#{[refLink
      style
      (:.. rprops)]}]
  (return
   (r/% -/View
        (j/assign {:ref refLink
                   :style [{:flexDirection "row"}
                           (:.. (j/arrayify style))]}
                  rprops))))

(defn.js Fill
  "fills space"
  {:added "0.1"}
  [#{[style
      (:.. rprops)]}]
  (return [:% -/View
           #{[:style [{:flex 1
                       :zIndex -10000}
                      (:.. (j/arrayify style))]
              (:.. rprops)]}]))

(defn.js H1
  "creates an H1 element"
  {:added "0.1"}
  [#{[text
      style
      (:.. rprops)]}]
  (return [:% -/Text
           #{[:style [{:color "#555"
                       :marginTop 20
                       :margin 10
                       :fontSize 28
                       :fontWeight "800"}
                      (:.. (j/arrayify style))]
              (:.. rprops)]}
           text]))

(defn.js H2
  "creates an H2 element"
  {:added "0.1"}
  [#{[text
      style
      (:.. rprops)]}]
  (return [:% -/Text
           #{[:style [{:color "#555"
                       :marginTop 20
                       :margin 10
                       :fontSize 24
                       :fontWeight "800"}
                      (:.. (j/arrayify style))]
              (:.. rprops)]}
           text]))

(defn.js H3
  "creates an H3 element"
  {:added "0.1"}
  [#{[text
      style
      (:.. rprops)]}]
  (return [:% -/Text
           #{[:style [{:color "#555"
                       :fontSize 20
                       :fontWeight "800"}
                      (:.. (j/arrayify style))]
              (:.. rprops)]}
           text]))

(defn.js H4
  "creates an H4 element"
  {:added "0.1"}
  [#{[text
      style
      (:.. rprops)]}]
  (return [:% -/Text
           #{[:style [{:color "#555"
                       :fontSize 16
                       :fontWeight "400"}
                      (:.. (j/arrayify style))]
              (:.. rprops)]}
           text]))

(defn.js H5
  "creates an H5 element"
  {:added "0.1"}
  [#{[text
      style
      (:.. rprops)]}]
  (return [:% -/Text
           #{[:style [{:color "#555"
                      :fontSize 12}
                      (:.. (j/arrayify style))]
              (:.. rprops)]}
           text]))

(defn.js Caption
  "creates an Caption element"
  {:added "0.1"}
  [#{[text
      styleText
      textProps
      style
      (:.. rprops)]}]
  (return
   [:% -/View
    #{[:style [{:backgroundColor "#ddd"}
               (:.. (j/arrayify style))]
       (:.. rprops)]}
    [:% -/Text
     #{[:style [{:fontSize 11
                 :padding 5
                 :color "#666"}
                (-/PlatformSelect {:ios {:fontFamily "Courier"}
                                   :default {:fontFamily "monospace"}})
                (:.. (j/arrayify styleText))]
        (:.. textProps)]}
     text]]))

;;
;; TABS
;;

(defn.js useTree
  "a generic tree function"
  {:added "4.0"}
  [#{tree
     root
     parents
     initial
     setInitial
     branchesFn
     targetFn
     formatFn
     displayFn}]
  (:= formatFn  (or formatFn -/format-entry))
  (:= displayFn (or displayFn
                    (fn:> [target _branch _parents _root]
                          [:% -/Caption
                           {:text (formatFn target)
                            :style {:flex 1}}])))
  (return (r/useTree #{tree
                       root
                       parents
                       initial
                       setInitial
                       branchesFn
                       targetFn
                       formatFn
                       displayFn})))

(defn.js TabsIndexed
  "creates an enum tabs view"
  {:added "0.1"}
  ([#{[items
       onChange
       checkIndex
       setIndex
       index
       styleText
       styleSelected
       (:= format k/identity)]}]
   (var [internal setInternal] (r/local (or index 0)))
   (r/run []
     (if (and (k/is-number? index)
              (not= internal index))
       (setInternal index)))
   (return
    [:% -/View
     {:style {:flexDirection "row"
              :flexWrap "wrap"}}
     (j/map items
            (fn [item i]
              (var text (format item))
              (var selected (== i internal))
              (return
               [:% -/TouchableOpacity
                {:key item
                 :style {:transform [{:scale 0.8}]}
                 :onPress (fn []
                            (setInternal i)
                            (if setIndex (setIndex i))
                            (if onChange (onChange (k/get-key items i))))}
                [:% -/Text {:key item
                            :style [(j/assign {:color  "#888"
                                               :padding 2}
                                              styleText)
                                    (:? selected
                                        (j/assign {:backgroundColor "#888"
                                                   :borderRadius 3
                                                   :color "white"
                                                   :padding 2}
                                                  styleSelected))]}
                 text]])))])))

(defn.js Tabs
  "creates an enum data view"
  {:added "0.1"}
  ([#{[data
       valueFn
       value
       setValue
       (:.. rprops)]}]
   (var #{setIndex
          items
          index} (r/convertIndex #{data
                                   valueFn
                                   value
                                   setValue}))
   (return [:% -/TabsIndexed
            #{[setIndex
               items
               index
               (:.. rprops)]}])))

(defn.js TabsPane
  "creates a  Tabs Pane"
  {:added "0.1"}
  [#{[listWidth
      tabsFormat
      styleTabs
      styleTabsText
      styleTabsSelected
      scroll
      
      tree
      parents
      root
      initial
      setInitial
      branchesFn
      targetFn
      formatFn
      displayFn]}]
  (var #{branch
         setBranch
         branches
         view}  (-/useTree #{tree
                             parents
                             root
                             initial
                             setInitial
                             branchesFn
                             targetFn
                             formatFn
                             displayFn}))
  (return
   [:% -/View
    {:style {:flex 1}}
    [:% -/Tabs
     {:value branch
      :setValue (fn [k]
                  (setBranch k))
      :data branches
      :format tabsFormat
      :style styleTabs
      :styleText styleTabsText
      :styleSelected styleTabsSelected}]
    (:? scroll
        [:% -/ScrollView view]
        view)]))

;;
;; LIST
;;

(defn.js ListIndexed
  "creates a list view"
  {:added "0.1"}
  ([#{[items
       onChange
       index
       setIndex
       initial
       style
       styleText
       styleSelected
       (:= format k/identity)]}]
   (if (not initial) (:= initial index))
   (var [internal setInternal] (r/local (or initial 0)))
   (r/watch [index]
     (when (not= internal index)
       (if onChange (onChange index))
       (setInternal index)))
   (return
    [:% -/FlatList
     {:data  (k/arr-map items format)
      :keyExtractor k/identity
      :renderItem (fn [e]
                    (var #{item} e)
                    (var selected (== (. e ["index"]) internal))
                    (return
                     [:% -/TouchableOpacity
                      {:key (. e ["index"])
                       :onPress (fn []
                                  (setInternal (. e ["index"]))
                                  (setIndex (. e ["index"])))}
                      [:% -/Text
                       {:style [(-/PlatformSelect {:ios {:fontFamily "Courier"}
                                                   :default {:fontFamily "monospace"}})
                                (j/assign {:fontSize 10
                                           :padding 2
                                           :color "#888"}
                                          styleText)
                                (:? selected
                                    (j/assign {:backgroundColor "#888"
                                               :color "white"}
                                              styleSelected))]}
                       (format item)]]))}])))

(defn.js List
  "creates a list"
  {:added "0.1"}
  ([#{[data
       valueFn
       value
       setValue
       (:.. rprops)]}]
   (var #{setIndex
          items
          index} (r/convertIndex #{data
                                   valueFn
                                   value
                                   setValue}))
   (return [:% -/ListIndexed
            #{[setIndex
               items
               index
               (:.. rprops)]}])))

(defn.js ListPane
  "creates a  List Pane"
  {:added "0.1"}
  [#{[listWidth
      listFormat
      styleList
      styleListText
      styleListSelected
      (:= direction "row")
      scroll
      tree
      parents
      root
      initial
      setInitial
      branchesFn
      targetFn
      formatFn
      displayFn]}]
  (var #{branch
         setBranch
         branches
         view} (-/useTree #{tree
                            parents
                            root
                            initial
                            setInitial
                            branchesFn
                            targetFn
                            formatFn
                            displayFn}))
  (return
   [:% -/View
    {:style {:flexDirection direction
             :flex 1}}
    [:% -/View
     {:style {:width (or listWidth 200)
              :height "100%"
              :overflow "auto"}}
     [:% -/List
      {:value branch
       :setValue (fn [k]
                   (setBranch k))
       :format listFormat
       :data branches
       :style styleList
       :styleText styleListText
       :styleSelected styleListSelected}]]
    (:? scroll
        [:% -/ScrollView
         view]
        [:% -/View
         {:style {:flex 1}}
         view])]))


;;
;; GROUP
;;

(defn.js TabsMultiIndexed
  "creates a multi tab select indexed"
  {:added "4.0"}
  ([#{[items
       setIndices
       indices
       style
       onChange
       styleText
       styleSelected
       (:= format k/identity)]}]
   (var itemFn
        (fn [item i]
          (var text (format item))
          (var selected (. indices [i]))
          (return
           [:% -/TouchableOpacity
            {:key item
             :style {:transform [{:scale 0.8}]}
             :onPress (fn []
                        (var changed
                             (j/map indices
                                    (fn [e ei]
                                      (return (:? (== ei i) (not e) e)))))
                        (setIndices changed)
                        (if onChange (onChange changed)))}
            [:% -/Text {:key item
                        :style [(j/assign {:color  "#888"
                                           :padding 2}
                                          styleText)
                                (:? selected
                                    (j/assign {:backgroundColor "#888"
                                               :borderRadius 3
                                               :color "white"
                                               :padding 2}
                                              styleSelected))]}
             text]])))
   (return [:% -/View
            {:style {:flexDirection "row"
                     :flexWrap "wrap"}}
            (j/map items itemFn)])))

(defn.js TabsMulti
  "creates a multi tab select
   
   (defn.js TabsMultiDemo
     []
     (var [values setValues] (r/local [\"A\" \"C\"]))
     (return
     (n/EnclosedCode 
 {:label \"js.react-native/TabsMulti\"} 
 [:% n/TabsMulti
        {:data [\"A\" \"B\" \"C\" \"D\"]
         :values values
         :setValues setValues
         :format (fn:> [s] (+ \" \" s \" \"))}] 
 [:% n/Caption
        {:text (k/json-encode #{values})
         :style {:marginTop 10}}])))"
  {:added "4.0"}
  ([#{[data
       valueFn
       values
       setValues
       (:.. rprops)]}]
   (let [#{setIndices
           items
           indices} (r/convertIndices #{data
                                        valueFn
                                        values
                                        setValues})]
     (return [:% -/TabsMultiIndexed
              #{[setIndices
                 items
                 indices
                 (:.. rprops)]}]))))

;;
;;

(defn.js TreePane
  "creates a  Tree Pane"
  {:added "0.1"}
  [#{[tree
      (:= root tree)
      (:= parents [])
      levels]}]
  (when (k/is-empty? levels)
    (return [:% -/Text "NO DATA"]))
  (var [level (:.. more)] levels)
  (var #{type} level)
  (var Pane (:? (== type "list")
                -/ListPane
                -/TabsPane))
  (var isFinal (== 1 (k/len levels)))
  (when isFinal
    (return [:% Pane #{[tree
                        root
                        parents
                        (:.. level)]}]))
  (var formatFn k/identity)
  (var displayFn (fn [newTree branch parents root]
                   (return [:% -/TreePane
                            {:key branch
                             :tree newTree
                             :root root
                             :parents [(:.. parents) branch]
                             :levels more}])))
  (return [:% Pane #{[tree
                      root
                      parents
                      displayFn
                      (:.. level)]}]))

(defn.js displayTarget
  "helper function for target display"
  {:added "4.0"}
  [Target]
  (if (k/nil? Target)
    (return [:% -/View])
    (return [:% -/View
             {:style {:flex 1
                      :padding 10
                      :overflow "auto"}}
             [:% Target]])))
  
(defn.js BaseIndicator
  "displays a base indicator"
  {:added "4.0"}
  [#{color
     waiting
     onPress
     label
     content
     styleText
     cardStyle}]
  (return
   [:% -/TouchableOpacity
    {:onPress onPress
     :disabled waiting}
    [:% -/View
     {:style {:flexDirection "row"
              :alignItems "center"}}
     [:% -/Text
      {:style {:fontSize 11
               :padding 10
               :fontWeight "900"
               :color "#555"}}
      label]
     [:% -/Padding {:style {:flex 1}}]
     [:% -/Text
      {:style [{:fontSize 11
                :padding 3
                :paddingLeft 8
                :paddingRight 8
                :marginRight 10
                :fontWeight "900"
                :width 70
                :borderRadius 5
                :backgroundColor "rgb(112, 168, 195)"
                :color "white"
                #_#_:opacity 0.7}
               (:.. (j/arrayify (or styleText [])))
               (:? waiting
                   {:textAlign "center"})]}
      (:? waiting [:% -/ActivityIndicator {:animating waiting :color "#aaa" :style {:height 5 :top 5 :transform [{:scale 0.7}]}}] content)]]]))

(defn.js ToggleIndicator
  "displays a toggle indicator"
  {:added "4.0"}
  [#{active
     waiting
     onPress
     label}]
  (return [:% -/BaseIndicator
           {:content (:? active
                         "ON"
                         "OFF") 
            :label  label
            :styleText (:? active
                           {:textAlign "left"
                            :backgroundColor "rgb(134, 215, 134)"}
                           {:textAlign "right"
                            :backgroundColor "rgb(144, 55, 55)"})
            :waiting waiting
            :onPress onPress}]))

(defn.js RecordList
  "displays a record list"
  {:added "4.0"}
  [#{entry columns}]
  (return
   [:<>
    (j/map columns
           (fn [[label key f]]
             (:= f (or f k/identity))
             (var val (k/get-key entry key))
             (var output (f val entry))
             (return
              [:% -/View
               {:key key
                :style {:flexDirection "row"}}
               [:% -/Text
                {:style
                 {:fontSize 11
                  :fontWeight "700"
                  :color "#777"}}
                label]
               [:% -/Padding {:style {:flex 1}}]
               [:% -/Text
                {:style
                 {:fontSize 11
                  :color "#777"
                  :fontWeight "500"}}
                (+ "" output)]])))]))

(defn.js TextDisplay
  "displays a slab of text"
  {:added "4.0"}
  [#{[content
      style
      styleText
      (:.. rprops)]}]
  (var text (or content
                (-/format-entry rprops)))
  (var clipboard (and (!:G navigator)
                      (. (!:G navigator) clipboard)))
  (return
   [:<>
    [:% -/View
     {:style [{:flex 1
               :padding 10
               :backgroundColor "#bbb"
               :overflow "auto"}
              (:.. (j/arrayify style))]}
     [:% -/Text
      {:style [(-/PlatformSelect {:ios {:fontFamily "Courier"}
                                  :default {:fontFamily "monospace"}})
               {:color "#333"
                :fontSize 10}
               (:.. (j/arrayify styleText))]}
      text]]
    (:? clipboard
        [:% -/View
         {:style {:position "absolute"
                  :top 10
                  :right 10}}
         [:% -/Button
          {:title "Copy"
           :onPress (fn []
                      (. clipboard
                         (writeText (-/format-obj rprops))))}]])]))

;;
;; Global
;;


(defn.js defaultGlobal
  "constructs the default global data"
  {:added "4.0"}
  []
  (return
   {:isDev        true
    :isTransition false}))

(defglobal.js Global
  (r/createContext (-/defaultGlobal)))

(defn.js GlobalProvider
  "constructs the context for global data"
  {:added "4.0"}
  [#{value
     children}]
  (var #{Provider} -/Global)
  (return
   [:% Provider
    {:value (j/assign (-/defaultGlobal)
                      value)}
    children]))

;;
;; Portal
;;

(defglobal.js PortalRegistery (r/createContext
                               (helper-portal/newRegistry)))

(defn.js PortalProvider
  "constructs an isolated context for portals and gateways to appear"
  {:added "4.0"}
  [#{registry
     children}]
  (var #{Provider} -/PortalRegistery)
  (var value (r/const (or registry
                          (helper-portal/newRegistry))))
  (return
   [:% Provider
    {:value value}
    children]))

(defn.js PortalSinkImpl
  "no context sharing version of `PortalSink`"
  {:added "4.0"}
  [#{[name
      registry
      children
      onSource
      (:.. rprops)]}]
  (var [source setSource] (r/local))
  (var sinkRef (r/ref))
  (r/init []
    (helper-portal/addSink registry name
                              #{sinkRef
                                setSource})
    (return (fn []
              (helper-portal/removeSink registry name))))
  (r/watch [source]
    (when onSource
      (onSource source)))
  (return [:% -/View
           #{[:ref sinkRef
              (:.. rprops)]}
           children
           source]))

(defn.js PortalSink
  "constructs the gateway where items will appear"
  {:added "4.0"}
  [#{[(:= name "default")
      children
      (:.. rprops)]}]
  (var #{Consumer} -/PortalRegistery)
  (return [:% Consumer
           (fn [registry]
             (return
              (r/% -/PortalSinkImpl
                   #{[name registry children (:.. rprops)]})))]))

(defn.js Isolation
  "provides an isolated single scoped gateway"
  {:added "4.0"}
  [props]
  (return
   [:% -/PortalProvider
    [:% -/PortalSink
     #{(:.. props)}]]))

(defn.js PortalImpl
  "no context sharing version of `Portal`"
  {:added "4.0"}
  [#{[target
      registry
      children
      (:= onSink (fn:>))]}]
  (var portalId (r/id))
  (r/run []
    (helper-portal/addSource registry target portalId children))
  (r/init []
    (helper-portal/captureSink registry target portalId onSink)
    (return (fn []
              (helper-portal/removeSource
               registry
               target
               portalId)
              (onSink nil))))
  (return [:% -/View]))

(defn.js Portal
  "constructs a portal"
  {:added "4.0"}
  [#{[(:= target "default")
      (:.. rprops)]}]
  (var #{Consumer} -/PortalRegistery)
  (return [:% Consumer
           (fn [registry]
             (return [:% -/PortalImpl
                      #{[target
                         registry
                         (:.. rprops)]}]))]))

(defn.js usePortalLayouts
  "gets measurements of elements in a portal"
  {:added "4.0"}
  [hostRef setLayouts]
  (var [sinkRef setSinkRef] (r/local))
  (var contentRef (r/ref))
  (var getLayouts
       (fn []
         (var contentElem (or (k/get-in contentRef
                                        ["current"
                                         "children"
                                         0])
                              (k/get-in contentRef
                                        ["current"
                                         "_children"
                                         0])))
         
         (when (and contentElem sinkRef)
           (. (j/onAll
               [(-/measureRef sinkRef)
                (:? hostRef
                    (-/measureRef hostRef)
                    (j/future))
                (-/measure contentElem)])
              (then (fn [[sinkLayout
                          hostLayout
                          contentLayout]]
                      (setLayouts {:sink sinkLayout
                                   :host hostLayout
                                   :content contentLayout})))))))
  (r/watch [sinkRef]
    (when sinkRef (getLayouts)))
  (return #{sinkRef
            setSinkRef
            contentRef
            getLayouts}))

(comment
  (./ns:purge))
