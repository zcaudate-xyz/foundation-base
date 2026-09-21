(ns pune.ui-market-delta
  (:require [lang.core :as  lang]
            [std.lib :as h]))

(lang/script :js
  {:runtime :websocket
   :config {:id :playground/web-basic
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react-native.helper-color :as c]
             [js.react :as r]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as ui-base]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js Delta
  {:added "4.0"}
  [#{value}]
  (var [changed setChanged] (r/local false))
  (var [color setColor]     (r/local "green"))
  (var [prev  setPrev]      (r/local value))
  (var changing (a/useBinaryIndicator changed))
  (var count    (r/ref 0))
  (r/watch [value]
    (when (not= prev value)
      (r/curr:set count (+ (r/curr count) 1))
      (setColor (:? (< prev value) "green" "firebrick"))
      (setPrev value)
      (setChanged true)
      (setTimeout (fn []
                      (r/curr:set count (- (r/curr count) 1))
                      (when (< (r/curr count) 1)
                        (setChanged false)))
                    600)))
  (return [:% n/Row
           {:style {:flexDirection "row-reverse"
                    :width 200}}
           [:% n/Fill]
           [:% ui-base/Box
            {:indicators #{changing}
             :chord #{color}
             :style {:width 35
                     
                     :height 24
                     :borderRadius 3
                     :textAlign "center"
                     :alignItems "center"
                     :justifyContent "center"}
             :inner [{:component n/Text
                      :children [value]
                      :style {:textAlign "center"
                              :fontSize 14
                              :fontWeight "800"}
                      :transformations
                      {:changing (fn [v]
                                   (return
                                    {:style {:color
                                             (c/mix ["#333"
                                                     "white"]
                                                    v)}}))}}]
             :addons [{:component n/Icon
                       :name (:? (== "green" color)
                                 "chevron-up"
                                 "chevron-down")
                       :style {:paddingTop 0
                               :marginRight 3
                               :transform []
                               :fontSize 25
                               :color color
                               :zIndex 100}
                       :transformations
                       {:changing (fn [v #{color}]
                                    (return
                                     {:style {:opacity (- 1 (* 0.1 v))
                                              :transform [{:translateX 3}
                                                          {:scale (+ 1 (* 0.6 v))}]
                                              :color color}}))}}]
             :transformations
             {:changing (fn [v #{color}]
                          (return
                           {:style {;;:opacity (- 1 v) #_(- 0.8 (* 0.5 v))
                                    :backgroundColor
                                    (c/mix ["white"
                                            "#eee"]
                                           v)}}))}}]]))

(def.js MODULE (!:module))

(comment
  (def +++
    (h/suppress
     (!.js
      (Object.assign play.web-001-rn.main/I01_FORM
                {"02h-ui-market"  play.web-001-rn.main-demo/UiDeltaExamples})))))
