(ns app.nextjs.devtools.ui
  "Only the small UI primitives needed by the copied devtool panel; no application layouts."
  (:require [lang.core :as l]))
(l/script :js
  {:require [[js.tamagui :as tm]
             [js.react :as r]]})
(def.js ButtonNormal tm/Button)
(def.js ThemeGlobalContext (r/createContext nil))
(defn.js useThemeGlobal [] (return (r/useContext -/ThemeGlobalContext)))
(defn.js t [text] (return text))
(defn.js useWindowDimensions
  []
  (var read
       (fn []
         (return {:width (:? (== (typeof window) "undefined") 0 window.innerWidth)
                  :height (:? (== (typeof window) "undefined") 0 window.innerHeight)})))
  (var [size setSize] (r/useState {:width 0 :height 0}))
  (r/useEffect (fn []
                 (var update (fn [] (setSize (read))))
                 (update)
                 (. window (addEventListener "resize" update))
                 (return (fn [] (. window (removeEventListener "resize" update)))))
               [])
  (return size))

(defn.js PopoverMenu
  "A menu component that appears as a popover, typically triggered by a button click."
  {:added "0.1"}
  [{:# [open onOpenChange icon button buttonProps contentProps arrowProps triggerProps children]
    :.. [props]}]
  (return
   (r/createElement
    tm/Popover
    (Object.assign {}
                   props
                   {:allowFlip true
                    :onOpenChange onOpenChange
                    :open open
                    :resize true
                    :size "$6"
                    :stayInFrame true})
    (r/createElement tm/PopoverTrigger
                     (Object.assign {} triggerProps {:asChild true})
                     (or button
                         (r/createElement tm/Button (Object.assign {} buttonProps {:icon icon}))))
    (r/createElement tm/PopoverContent
                     (Object.assign {:animation ["quick" {:opacity {:overshootClamping true}}]
                                     :borderColor "$borderColor"
                                     :borderWidth 1
                                     :elevate true
                                     :elevation 5
                                     :enterStyle {:opacity 0 :y -10}
                                     :exitStyle {:opacity 0 :y -10}
                                     :padding "$4"}
                                    contentProps)
                     (r/createElement tm/PopoverArrow
                                      (Object.assign {:borderColor "$borderColor" :borderWidth 1}
                                                     arrowProps))
                     children))))
