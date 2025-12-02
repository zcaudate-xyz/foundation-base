(ns js.react-native.model-context-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script- :js
  {:runtime :basic
   :require [[js.react-native.model-context :as  model-context]
             [js.core :as j]
             [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.react-native.model-context/innerCoordinate :added "4.0" :unchecked true}
(fact "gets the plane position"
  ^:hidden
  
  (model-context/innerCoordinate
   {:position "centered"
    :margin 20
    :parent {:height  300
             :width   300}
    :height 50
    :width 50})
  => {"width" 50, "top" 125, "height" 50, "left" 125})

^{:refer js.react-native.model-context/contextCoordinateMain :added "4.0" :unchecked true}
(fact "gets the context main coordinate"
  ^:hidden
  
  (model-context/contextCoordinateMain
   {:margin 10
    :host {:height 300
           :width 100}
    :content {:height 50
              :width 50}
    :position "top"})
  => {"top" -60})

^{:refer js.react-native.model-context/contextCoordinateCross :added "4.0" :unchecked true}
(fact  "gets the context cross coordinate"
  ^:hidden
  
  (model-context/contextCoordinateCross
   {:margin 10
    :host {:height 300
           :width 100}
    :content {:height 50
              :width 50}
    :position "top"
    :alignment "center"})
  => {"left" 25})

^{:refer js.react-native.model-context/contextCoordinate :added "4.0" :unchecked true}
(fact  "gets the context coordinate"
  ^:hidden
  
  (model-context/contextCoordinate
   {:margin 10
    :marginCross 5
    :host {:height 300
           :width 100}
    :content {:height 50
              :width 50}
    :position "top"
    :alignment "center"})
  => {"top" -60, "left" 25})

^{:refer js.react-native.model-context/getTranslationOffset :added "4.0" :unchecked true}
(fact "gets the plane transition function"
  ^:hidden
  
  (model-context/getTranslationOffset
   {:transition "from_top"
    :margin 20
    :height 50
    :width 50})
  => ["translateY" -90]

  (model-context/getTranslationOffset
   {:transition "from_left"
    :margin 20
    :height 50
    :width 50})
  => ["translateX" -90])

^{:refer js.react-native.model-context/getScalarFn :added "4.0" :unchecked true}
(fact "gets a scalar function from input"
  ^:hidden
  
  (!.js
   ((model-context/getScalarFn {:zoom 0.3} "zoom")
    0.8))
  => 0.8599999999999999)


^{:refer js.react-native.model-context/animateOffset :added "4.0" :unchecked true}
(fact "gets the animated offset"
  ^:hidden
  
  (model-context/animateOffset
   {:height 10
    :width 10
    :margin 2}
   {:type "negative"
    :aspect "width"})
  => -14)

^{:refer js.react-native.model-context/animateIn :added "4.0" :unchecked true}
(fact "creates the animateIn function"
  ^:hidden
  
  (!.js
   ((model-context/animateIn {:transition "from_right"
                              :height 10
                              :width 10
                              :margin 2})
    0.5))
  => {"translateX" 7})

^{:refer js.react-native.model-context/animateOut :added "4.0" :unchecked true}
(fact "creates the animateOut function"
  ^:hidden
  
  (!.js
   ((model-context/animateOut {:transition "from_right"
                               :height 10
                              :width 10
                               :margin 2})
    0.5))
  => {"translateX" 7})
