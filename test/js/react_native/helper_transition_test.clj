(ns js.react-native.helper-transition-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.react-native.helper-color :as c]
             [js.react-native.helper-theme :as helper-theme]
             [js.react-native.helper-theme-default :as helper-theme-default]
             [js.react-native.helper-transition :as helper-transition]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.react-native.helper-transition/absoluteAnimateFn :added "4.0" :unchecked true
  :setup [(def +out+
            [{"style" {"transform" [{"translateX" -120}
                                    {"scale" 0.2}],
                       "opacity" 0}}
             {"style" {"transform" [{"translateX" -96}
                                    {"scale" 0.36000000000000004}],
                       "opacity" 0.2}}
             {"style" {"transform" [{"translateX" -60}
                                    {"scale" 0.6000000000000001}],
                       "opacity" 0.5}}
             {"style" {"transform" [{"translateX" -23.999999999999993}
                                    {"scale" 0.8400000000000001}],
                       "opacity" 0.8}}
             {"style" {"transform" [{"translateX" 0}
                                    {"scale" 1}],
                       "opacity" 1}}])]}
(fact "creates the animate function"
  ^:hidden
  
  (!.js
   (var f (helper-transition/absoluteAnimateFn
           {:transition "from_left"
            :effect {:zoom 0.2}
            :position "bottom"
            :margin 10
            :height 100
            :width 100}))
   (j/map [0 0.2 0.5 0.8 1] f))
  => vector?)
  
^{:refer js.react-native.helper-transition/absoluteAnimateProgress :added "4.0" :unchecked true}
(fact "applies the animate function"
  ^:hidden

  (!.js
   (var f (fn [progress]
            (return
             (helper-transition/absoluteAnimateProgress
              {:transition "from_left"
               :effect {:zoom 0.2}
               :position "bottom"
               :margin 10
               :height 100
               :width 100}
              progress))))
   (j/map [0 0.2 0.5 0.8 1] f))
  => vector?)

^{:refer js.react-native.helper-transition/relativeAnimateProgress :added "4.0" :unchecked true}
(fact "applies the relative animate function"
  ^:hidden
  
  (!.js
   (var f (fn [progress]
            (return
             (helper-transition/relativeAnimateProgress
              {:effect {:zoom 0.2}
               :xOffset 100
               :yOffset 100}
              progress))))
   (j/map [0 0.2 0.5 0.8 1] f))
  => [{"style"
       {"transform"
        [{"translateX" 100} {"translateY" 100} {"scale" 0.2}],
        "opacity" 0}}
      {"style"
       {"transform"
        [{"translateX" 80}
         {"translateY" 80}
         {"scale" 0.36000000000000004}],
        "opacity" 0.2}}
      {"style"
       {"transform"
        [{"translateX" 50}
         {"translateY" 50}
         {"scale" 0.6000000000000001}],
        "opacity" 0.5}}
      {"style"
       {"transform"
        [{"translateX" 19.999999999999996}
         {"translateY" 19.999999999999996}
         {"scale" 0.8400000000000001}],
        "opacity" 0.8}}
      {"style"
       {"transform" [{"translateX" 0} {"translateY" 0} {"scale" 1}],
        "opacity" 1}}])
  
