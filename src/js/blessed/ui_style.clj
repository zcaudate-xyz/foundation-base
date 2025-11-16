(ns js.blessed.ui-style
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]]})


;;
;; component helpers
;;

(defn.js getColor
  "helper function to get color props"
  {:added "4.0"}
  [props]
  (return (k/obj-pick props ["bg"
                             "fg"
                             "style"])))

(defn.js getLayout
  "helper function to get layout props"
  {:added "4.0"}
  [props]
  (return (k/obj-pick props ["width"
                             "height"
                             "left"
                             "right"
                             "top"
                             "bottom"])))

(defn.js getTopProps
  "helper function for top layout props"
  {:added "4.0"}
  [props noShrink]
  (return (k/obj-assign (-/getLayout props)
                        {:bg "black"
                         :shrink (not noShrink)})))

(defn.js omitLayoutProps
  "helper function for stripping layout props"
  {:added "4.0"}
  [props]
  (return (k/obj-assign
           (k/obj-clone props)
           {:left nil
            :right nil
            :top nil
            :bottom nil})))

;;
;; style
;;


(defn.js styleMinimal
  "gets the minimal style"
  {:added "4.0"}
  [color]
  (return {:bold false
           :fg "white"
           :bg "black"
           :hover {:bold false
                   :fg color
                   :bg "black"
                   :border {:fg color
                            :bg "black"}}
           :border {:fg "white"
                    :bg "black"}}))

(defn.js styleSmall
  "gets the small style"
  {:added "4.0"}
  [color]
  (return (:? (== "gray" color) {:bg "gray" :bold false :fg "blue" :hover {:bg "gray" :bold true :fg "white"}} {:bg color :bold true :fg "black" :hover {:bg "gray" :bold true :fg color}})))

(defn.js styleInvert
  "gets the invert style"
  {:added "4.0"}
  [color]
  (return (:? (== "gray" color) {:bg "gray" :bold true :border {:bg "gray" :fg "gray"} :fg "blue" :hover {:bg "blue" :bold true :border {:bg "blue" :fg "blue"} :fg "gray"}} {:bg color :bold true :border {:bg color :fg color} :fg "gray" :hover {:bg "gray" :bold true :border {:bg "gray" :fg "gray"} :fg color}})))

(def.js styleInvertBusy
  {:bold false
   :fg "white"
   :bg "gray"
   :border {:fg "gray"
            :bg "gray"}})


(def.js styleInvertDisabled
  {:bold false
   :bg "gray"
   :fg "blue"
   :hover {:bold false
           :bg "gray"
           :fg "black"
           :border {:bg "gray"
                    :fg "black"}}
   :border {:bg "gray"
            :fg "black"}})

(defn.js styleListView
  "gets the list view style"
  {:added "4.0"}
  ([color]
   (return {:border {:fg "black"}
            :bg "black"
            :selected {:bg color
                       :fg "gray"}
            :scrollbar {:fg color}})))

(def.js styleScrollBar
  {:style {:bg "gray"
           :fg "gray"}
   :track true})

;;
;;
;;

