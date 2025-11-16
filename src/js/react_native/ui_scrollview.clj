(ns js.react-native.ui-scrollview
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [xt.lang.base-lib :as k]]})

(defn.js ScrollViewImpl
  "creates a non global enhanced scrollview"
  {:added "4.0"}
  [#{[brand
      children
      isTransition
      style
      styleContainer
      styleBackground
      styleIndicator
      (:.. rprops)]}]
  (var [contentHeight
        setContentHeight] (r/local 1))
  (var [visibleHeight
        setVisibleHeight] (r/local 0))
  (var showScroll (> contentHeight
                     visibleHeight))
  (var visibleRatio (/ visibleHeight
                       contentHeight))
  (var visibleDiff  (- 1 visibleRatio))
  (var contentOffset (a/val 0))
  (var contentMargin 1)
  (var isMounted (r/useIsMounted))
  (return
   [:% n/View
    {:style [{:flex 1
              :flexDirection "row-reverse"}
             (:.. (j/arrayify styleContainer))]}
    (:? (not isTransition)
        [:% n/View
         {:style
          [{:backgroundColor "red",
            :flexDirection "row-reverse",
            :height "100%",
            :position "absolute",
            :width (:? showScroll 7 0),
            :zIndex 10000}
           (:.. (j/arrayify styleBackground))]}
         (:? showScroll
             [:% a/Box
              {:key "indicator",
               :style
               [{:backgroundColor "blue",
                 :height (* visibleRatio visibleHeight),
                 :marginTop (* 2 contentMargin),
                 :transform [{:translateY contentOffset}],
                 :width 6}
                (:.. (j/arrayify styleIndicator))]}])])
    [:% n/ScrollView
     #{[:style [{:flex 1}
                (:.. (j/arrayify style))]
        :onContentSizeChange (fn [contentWidth contentHeight]
                               (when (isMounted)
                                 (setContentHeight contentHeight)))
        :showsVerticalScrollIndicator false
        :onLayout (fn [#{nativeEvent}]
                    (when (isMounted)
                      (setVisibleHeight
                       (. nativeEvent layout height))))
        :onScroll (fn [#{nativeEvent}]
                    (a/setValue contentOffset
                                (- (k/clamp 0
                                            (* visibleDiff visibleHeight)
                                            (* (. nativeEvent contentOffset y)
                                               visibleRatio))
                                   contentMargin)))
        :scrollEventThrottle 16
        #_(:.. rprops)]}
     children]]))

(defn.js ScrollView
  "creates a scrollview"
  {:added "4.0"}
  [#{[brand
      children
      style
      styleContainer
      styleBackground
      styleIndicator
      (:.. rprops)]}]
  (var #{Consumer} n/Global)
  (return [:% Consumer
           (fn [#{isTransition}]
             (return
              [:% -/ScrollViewImpl
               #{[brand
                  children
                  isTransition
                  style
                  styleContainer
                  styleBackground
                  styleIndicator
                  (:.. rprops)]}]))]))

(comment
  (l/lib:unused))
