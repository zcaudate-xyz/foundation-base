(ns js.lib.rn-gesture
  (:require [std.json :as json]
            [std.lang :as l]
            [std.lib.env :as env]
            [std.lib.foundation :as f]))

(l/script :js
  {:import [["react-native-gesture-handler" :as [* rnGesture]]]})

;;
;; Gestures
;;


(defn- get-gesture-symbols
  ([]
   (let [all (json/read (env/sys:resource-content "assets/js.core/react-native-gesture-handler.json"))]
     (vec (sort (map symbol (keys all)))))))

(def +gesture+
  '[BaseButton BorderlessButton Directions DrawerLayout DrawerLayoutAndroid
    FlatList FlingGestureHandler ForceTouchGestureHandler GestureHandlerRootView
    LongPressGestureHandler NativeViewGestureHandler PanGestureHandler
    PinchGestureHandler RawButton RectButton RotationGestureHandler
    ScrollView State Swipeable Switch TapGestureHandler TextInput TouchableHighlight
    TouchableNativeFeedback TouchableOpacity TouchableWithoutFeedback
    createNativeWrapper gestureHandlerRootHOC])

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "rnGesture"
                                   :tag "js"}]
  
  +gesture+)

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "rnGesture.Directions"
                                   :tag "js"}]
  [DOWN LEFT RIGHT UP])

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "rnGesture.State"
                                   :tag "js"}]
  [ACTIVE BEGAN CANCELLED END FAILED UNDETERMINED])
