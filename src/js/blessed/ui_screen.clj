(ns js.blessed.ui-screen
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.lib.valtio :as v]
             [js.react :as r]
             [js.blessed :as b]
             [js.blessed.ui-style :as ui-style]]})

;;
;;
;;

(defglobal.js Dimension
  (v/proxy {:height nil :width nil}))

(defglobal.js Mouse
  (v/proxy {:x nil :y nil}))

(defn.js ScreenMouse
  "component that updates mouse position"
  {:added "4.0"}
  ([]
   (let [ref   (r/ref)]
     (r/init []
       (let [#{screen} (r/curr ref)]
         (. screen
            (on "mouse"
                (fn [#{x y}] (j/assign -/Mouse #{x y}))))))
     (return [:box {:ref ref
                    :bg "yellow"
                    :height 0
                    :width  0}]))))

(defn.js ScreenMeasure
  "component that measures then screen"
  {:added "4.0"}
  ([]
   (var ref (r/ref))
   (r/init []
     (var #{screen} (r/curr ref))
     (var measureFn (fn []
                      (var #{height width} screen)
                      (j/assign -/Dimension #{height width})))
     (. screen (on "resize" measureFn))
     (measureFn))
   (return [:box {:ref ref
                  :bg "yellow"
                  :height 0
                  :width  0}])))

(defn.js GridLayout
  "component that implements grid layout"
  {:added "4.0"}
  ([props]
   (let [#{bg color
           display items} props
         #{height width keyFn
           viewFn center} (j/assign {:height 15
                                     :width 40
                                     :keyFn  (fn:> [item i] i)
                                     :viewFn (fn:> [item] item)}
                                    display)
         _ (:= items (or items []))
         [full setFull] (r/local 0)
         row-count (j/max 1 (Math.floor (/ full width)))
         dims (v/val -/Dimension)
         grid (r/ref {})
         _    (r/run []
                (let [curr (r/curr grid)
                      width (k/get-key curr "width")]
                  (if (and curr
                           (not= width full))
                    (setFull width))))
         bopts {:keys true
                :mouse true
                :scrollable true
                :style {:bg bg}
                :scrollbar {:style {:bg (or color "gray")
                                    :fg (or color "gray")}
                            :track true}}
         bprops (k/obj-assign-nested (ui-style/getLayout props) bopts)]
     (return [:box #{(:.. bprops)}
              #_[:box {:bottom 0
                     :height 1
                     :content (j/inspect dims)}]
              [:box {:ref grid
                     :style {:bg bg}
                     :height (-> (k/len items)
                                 (/ row-count)
                                 (j/ceil)
                                 (* height))
                     :left 0 :right 1}
               (j/map items
                      (fn [item i]
                        (var view (viewFn item))
                        (return
                         [:box {:key (keyFn item i)
                                :height height
                                :width width
                                :style {:bg bg}
                                :top  (-> (/ i row-count)
                                          (Math.floor)
                                          (* height))
                                :left (:? (== row-count 1) 0 (* width (mod i row-count)))}
                          view])))]]))))
