(ns js.lib.rn-gesture
  (:require [std.json :as json]
            [std.lang :as l]
            [std.lib.env]
            [std.lib.foundation]))

(l/script :js
  {:import [["react-native-gesture-handler" :as [* rnGesture]]]})

;;
;; Gestures
;;


(defn- get-gesture-symbols
  ([]
   (let [all (json/read (std.lib.env/sys:resource-content "assets/js.core/react-native-gesture-handler.json"))]
     (vec (sort (map symbol (keys all)))))))

(def +gesture+
  '[BaseButton BorderlessButton Directions DrawerLayout DrawerLayoutAndroid
    FlatList FlingGestureHandler ForceTouchGestureHandler GestureHandlerRootView
    LongPressGestureHandler NativeViewGestureHandler PanGestureHandler
    PinchGestureHandler RawButton RectButton RotationGestureHandler
    ScrollView State Swipeable Switch TapGestureHandler TextInput TouchableHighlight
    TouchableNativeFeedback TouchableOpacity TouchableWithoutFeedback
    createNativeWrapper gestureHandlerRootHOC])

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "rnGesture"
                                   :tag "js"}]
  
  +gesture+)

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "rnGesture.Directions"
                                   :tag "js"}]
  [DOWN LEFT RIGHT UP])

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "rnGesture.State"
                                   :tag "js"}]
  [ACTIVE BEGAN CANCELLED END FAILED UNDETERMINED])
