(ns js.react-native.ui-router-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.ui-router :as ui-router]
             [js.core :as j]]
   })

^{:refer js.react-native.ui-router/useTransition :added "4.0" :unchecked true}
(fact "creates all props involved with transition"
  ^:hidden
  
  (defn.js UseTransitionDemo
    []
    (return
     (n/EnclosedCode 
      {:label "js.react-native.ui-router/useTransition"
       #_#_:style {:height 300}} 
      [:% n/View
       {:style {:backfaceVisibility "hidden"
                :height 100
                :width 100
                :backgroundColor "red"
                :transform [{"rotateY" (+ j/PI "rad")}]}}] ))))

^{:refer js.react-native.ui-router/RouterImpl :added "4.0" :unchecked true}
(fact "creates the transitioning router")

^{:refer js.react-native.ui-router/Router :added "4.0" :unchecked true}
(fact "creates a Router"
  ^:hidden
  
  (defn.js RouterDemo
    []
    (var [routeKey setRouteKey]     (r/local "b2"))
    (var [transition setRouteTransition] (r/local "from_top"))
    (var routeComponentFn
         (fn [routeKey]
           (return (. {:a1 (fn:> [:% n/View {:style {:backfaceVisibility "hidden"
                                                     :backgroundColor "orange"
                                                     :flex 1}}])
                       :b2 (fn:> [:% n/View {:style {:backfaceVisibility "hidden"
                                                     :backgroundColor "pink"
                                                     :flex 1}}])
                       :c3 (fn:> [:% n/View {:style {:backfaceVisibility "hidden"
                                                     :backgroundColor "cyan"
                                                     :flex 1}}])
                       :d4 (fn:> [:% n/View {:style {:backfaceVisibility "hidden"
                                                     :backgroundColor "black"
                                                     :flex 1}}])}
                      [routeKey]))))
    (return
     (n/EnclosedCode 
      {:label "js.react-native.ui-router/Router"
       :style {:height 400}} 
      [:% n/Tabs
       {:data ["a1"
               "b2"
               "c3"
               "d4"]
        :value routeKey 
        :setValue setRouteKey}] 
      [:% n/Tabs
       {:data ["from_top"
               "from_bottom"
               "from_left"
               "from_right"
               "flip_vertical"
               "flip_horizontal"]
        :value  transition
        :setValue setRouteTransition}] 
      [:% ui-router/Router
       #{routeKey
         routeComponentFn
         transition
         {
          :debug true
          :fade 0.2
          :style {:height 200
                  :width 350}}}])))
  
  
  )
